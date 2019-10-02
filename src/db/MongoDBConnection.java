package db;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;

import model.Restaurant;
import yelp.YelpAPI;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.regex;

public class MongoDBConnection implements DBConnection {
	private MongoClient mongoClient;
	private MongoDatabase db;
	
	/**
	 * Constructor
	 */
	public MongoDBConnection() {
		// connect to local MongoDB server
		mongoClient = new MongoClient();
		db = mongoClient.getDatabase(DBUtil.DB_NAME);
	}
	
	/**
	 * Close db connection.
	 */
	@Override
	public void close() {
		if (mongoClient != null) {
			mongoClient.close();
		}
	}
	
	/**
	 * Given the list of business ids of restaurants visited (liked) by a user, write into the users table.
	 */
	@Override
	public boolean setVisitedRestaurants(String userId, List<String> businessIds) {
		// Updates a single document within the collection based on the filter.
		// The $push operator appends a specified value to an array.
		// $each modifier appends multiple values to the array field.
		UpdateResult result = db.getCollection("users").updateOne(new Document("user_id", userId), // filter
				new Document("$push", new Document("visited", new Document("$each", businessIds))));
		// Returns true if the write was acknowledged.
		return result.wasAcknowledged();
	}
	
	/**
	 * Given the list of business ids of restaurants visited (unliked) by a user, delete from the users table.
	 */
	@Override
	public boolean unsetVisitedRestaurants(String userId, List<String> businessIds) {
		// The $pullAll operator removes all instances of the specified values from an existing array. 
		UpdateResult result = db.getCollection("users").updateOne(new Document("user_id", userId), // filter
				new Document("$pullAll", new Document("visited", businessIds)));
		return result.wasAcknowledged();
	}
	
	/**
	 * Get the set of business ids of all the restaurants visited (liked) by a user.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Set<String> getVisitedRestaurants(String userId) {
		Set<String> set = new HashSet<>();
		// Finds all documents in the collection.
		// $eq creates a filter that matches all documents where the value of the field name equals the specified value.
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id", userId));
		Document document = iterable.first();
		if (document != null && document.containsKey("visited")) {
			List<String> list = (List<String>) document.get("visited");
			set.addAll(list);
		}
		return set;
	}
	
	/**
	 * Get the restaurant JSON object from the database (no distance!!!), given the business id and isVisited.
	 */
	@Override
	public JSONObject getRestaurantsById(String businessId, boolean isVisited) {
		try {
			FindIterable<Document> iterable = db.getCollection("restaurants").find(eq("business_id", businessId));
			Document document = iterable.first();
			if (document == null) {
				return null;
			}
			Restaurant restaurant = new Restaurant(
					document.getString("business_id"),
					document.getString("name"),
					document.getString("categories"),
					document.getString("city"),
					document.getString("state"),
					document.getString("full_address"),
					document.getDouble("stars"),
					document.getString("price"),
					document.getDouble("latitude"),
					document.getDouble("longitude"),
					document.getString("image_url"),
					document.getString("url"),
					-1.0);
			JSONObject obj = restaurant.toJSONObject();
			obj.put("is_visited", isVisited);
			return obj;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Get the set of all categories for a restaurant, given the business id.
	 */
	@Override
	public Set<String> getCategories(String businessId) {
		Set<String> set = new HashSet<>();
		FindIterable<Document> iterable = db.getCollection("restaurants").find(eq("business_id", businessId));
		Document document = iterable.first();
		if (document == null) {
			return set;
		}
		String[] categories = document.getString("categories").split(",");
		for (String category : categories) {
			set.add(category.trim());
		}
		return set;
	}
	
	/**
	 * Get the set of business ids of restaurants, given the category
	 */
	@Override
	public Set<String> getBusinessId(String category) {
		Set<String> set = new HashSet<>();
		// $regex provides regular expression capabilities for pattern matching strings in queries.
		FindIterable<Document> iterable = db.getCollection("restaurants").find(regex("categories", category));
		MongoCursor<Document> iterator = iterable.iterator();
		while (iterator.hasNext()) {
			Document document = iterator.next();
			set.add(document.getString("business_id"));
		}
		return set;
	}
	
	/**
	 * Get the JSON array of recommended restaurants for a user, based on the categories.
	 * Step 1, fetch all the restaurants this user has visited (liked)
	 * Step 2, given all these restaurants, what are the categories
	 * Step 3, given these categories, find all the restaurants in the "restaurants" table that belong to them
	 * Step 4, filter the restaurants that this user has visited (liked)
	 */
	@Override
	public JSONArray recommendRestaurants(String userId) {
		try {
			// step 1
			Set<String> visitedRestaurants = getVisitedRestaurants(userId);
			// step 2
			Set<String> allCategories = new HashSet<>();
			for (String restaurant : visitedRestaurants) {
				allCategories.addAll(getCategories(restaurant));
			}
			// step 3
			Set<String> allRestaurants  = new HashSet<>();
			for (String category : allCategories) {
				Set<String> set = getBusinessId(category);
				allRestaurants.addAll(set);
			}
			// step 4
			Set<JSONObject> diff = new HashSet<>();
			int count = 0;
			for (String businessId : allRestaurants) {
				if (!visitedRestaurants.contains(businessId)) {
					diff.add(getRestaurantsById(businessId, false));
					count++;
					if (count >= DBUtil.MAX_RECOMMENDED_RESTAURANTS) {
						break;
					}
				}
			}
			return new JSONArray(diff);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Search restaurants, given user id and coordinates.
	 * Get the JSON array of these restaurant JSON objects.
	 * Store these restaurants into database (without distance), so we don't need to fetch them from Yelp any more.
	 */
	@Override
	public JSONArray searchRestaurants(String userId, double lat, double lon) {
		try {
			// call Yelp API
			YelpAPI api = new YelpAPI();
			JSONObject response = new JSONObject(api.searchForBusinessesByLocation(lat, lon));
			JSONArray array = (JSONArray) response.get("businesses");
			List<JSONObject> list = new ArrayList<>();
			Set<String> visited = getVisitedRestaurants(userId);
			for (int i = 0; i < array.length(); i++) {
				JSONObject object = array.getJSONObject(i);
				// convert JSON object to Restaurant object and then back to JSON object, so we get only the fields needed
				Restaurant restaurant = new Restaurant(object);
				
				JSONObject obj = restaurant.toJSONObject();
				// check history, add the information of if this restaurant was visited (liked) by this user
				if (visited.contains(restaurant.getBusinessId())) {
					obj.put("is_visited", true);
				} else {
					obj.put("is_visited", false);
				}
				list.add(obj);
				
				// store into database, update the record if it already exists
				String businessId = restaurant.getBusinessId();
				String name = restaurant.getName();
				String categories = restaurant.getCategories();
				String city = restaurant.getCity();
				String state = restaurant.getState();
				String fullAddress = restaurant.getFullAddress();
				double stars = restaurant.getStars();
				String price = restaurant.getPrice();
				double latitude = restaurant.getLatitude();
				double longitude = restaurant.getLongitude();
				String imageUrl = restaurant.getImageUrl();
				String url = restaurant.getUrl();
				// set _id to be the business id, to ensure there is only a single record for each restaurant in the collection.
				// The $set operator replaces the value of a field with the specified value.
				// upsert: When true, updateOne() either: Creates a new document if no documents match the filter; Updates a single document that matches the filter.
				db.getCollection("restaurants").updateOne(new Document("_id", businessId), // filter
				new Document("$set", new Document()
						.append("business_id", businessId)
						.append("name", name)
						.append("categories", categories)
						.append("city", city)
						.append("state", state)
						.append("full_address", fullAddress)
						.append("stars", stars)
						.append("price", price)
						.append("latitude", latitude)
						.append("longitude", longitude)
						.append("image_url", imageUrl)
						.append("url", url)),
				new UpdateOptions().upsert(true));
			}
			return new JSONArray(list);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Create new user
	 */
	@Override
	public boolean signUp(String userId, String password, String firstName, String lastName) {
		try {
			// set _id to be the user id, to ensure there is only a single record for each user in the collection.
			db.getCollection("users").insertOne(new Document()
					.append("_id", userId)
					.append("user_id", userId)
					.append("password", password)
					.append("first_name", firstName)
					.append("last_name", lastName));
			return true;
		} catch (MongoException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Verify if user id and password are correct.
	 */
	@Override
	public boolean verifyLogin(String userId, String password) {
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id", userId));
		Document document = iterable.first();
		return document != null && document.getString("password").equals(password);
	}

	/**
	 * Get the full name, given the user id.
	 */
	@Override
	public String getFirstLastName(String userId) {
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id", userId));
		Document document = iterable.first();
		return document != null ? (document.getString("first_name") + " " + document.getString("last_name")) : "";
	}

}
