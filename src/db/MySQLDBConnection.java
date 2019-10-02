package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import model.Restaurant;
import yelp.YelpAPI;

public class MySQLDBConnection implements DBConnection {
	private Connection conn;
	
	/**
	 * Constructor
	 */
	public MySQLDBConnection() {
		try {
			System.out.println("Connecting to database:\n" + DBUtil.URL);
			// initialize the Driver class, and in turn the class registers itself with the java.sql.DriverManager per the JDBC specification.
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(DBUtil.URL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Execute sql (write), return whether it is successful or not.
	 * @param query
	 */
	private boolean executeUpdateStatement(String query) {
		if (conn == null) {
			return false;
		}
		try {
			Statement stmt = conn.createStatement();
			System.out.println("\nMySQLDBConnection executing query:\n" + query);
			stmt.executeUpdate(query);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Execute sql (read), get the required db records.
	 * @param query
	 * @return
	 */
	private ResultSet executeFetchStatement(String query) {
		if (conn == null) {
			return null;
		}
		try {
			Statement stmt = conn.createStatement();
			System.out.println("\nMySQLDBConnection executing query:\n" + query);
			// Executes the given SQL statement, which returns a single ResultSet object
			return stmt.executeQuery(query);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Close db connection.
	 */
	@Override
	public void close() {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Given the list of business ids of restaurants visited (liked) by a user, write into the history table.
	 */
	@Override
	public boolean setVisitedRestaurants(String userId, List<String> businessIds) {
		for (String businessId : businessIds) {
			String query = "INSERT INTO history (user_id, business_id) "
					+ "VALUES ("
					+ "'" + userId + "',"
					+ "'" + businessId + "'"
					+ ")";
			if (!executeUpdateStatement(query)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Given the list of business ids of restaurants visited (unliked) by a user, delete from the history table.
	 */
	@Override
	public boolean unsetVisitedRestaurants(String userId, List<String> businessIds) {
		for (String businessId : businessIds) {
			String query = "DELETE FROM history WHERE "
					+ "user_id=" + "\"" + userId + "\" and "
					+ "business_id=" + "\"" + businessId + "\"";
			if (!executeUpdateStatement(query)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Get the set of business ids of all the restaurants visited (liked) by a user.
	 */
	@Override
	public Set<String> getVisitedRestaurants(String userId) {
		Set<String> visitedRestaurants = new HashSet<>();
		try {
			String query = "SELECT business_id from history WHERE user_id='" + userId + "'";
			ResultSet rs = executeFetchStatement(query);
			if (rs == null) {
				return visitedRestaurants;
			}
			// A ResultSet cursor is initially positioned before the first row; the first call to the method 
			// next makes the first row the current row;
			while (rs.next()) {
				visitedRestaurants.add(rs.getString("business_id"));
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return visitedRestaurants;
	}
	
	/**
	 * Get the restaurant JSON object from the database (no distance!!!), given the business id and isVisited.
	 */
	@Override
	public JSONObject getRestaurantsById(String businessId, boolean isVisited) {
		try {
			String query = "SELECT * from restaurants WHERE business_id='" + businessId + "'";
			ResultSet rs = executeFetchStatement(query);
			if (rs != null && rs.next()) {
				Restaurant restaurant = new Restaurant(
						rs.getString("business_id"),
						rs.getString("name"),
						rs.getString("categories"),
						rs.getString("city"),
						rs.getString("state"),
						rs.getString("full_address"),
						rs.getFloat("stars"),
						rs.getString("price"),
						rs.getFloat("latitude"),
						rs.getFloat("longitude"),
						rs.getString("image_url"),
						rs.getString("url"),
						-1.0);
				JSONObject obj = restaurant.toJSONObject();
				obj.put("is_visited", isVisited);
				return obj;
			}
		} catch (Exception e) {
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
		try {
			String query = "SELECT categories from restaurants WHERE business_id='" + businessId + "'";
			ResultSet rs = executeFetchStatement(query);
			if (rs != null && rs.next()) {
				String[] categories = rs.getString("categories").split(",");
				for (String category : categories) {
					// trim() returns a string whose value is this string, with any leading and trailing whitespace removed. 
					set.add(category.trim());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return set;
	}
	
	/**
	 * Get the set of business ids from restaurants table, given the category
	 */
	@Override
	public Set<String> getBusinessId(String category) {
		Set<String> set = new HashSet<>();
		try {
			String query = "SELECT business_id from restaurants WHERE categories LIKE '%" + category + "%'";
			ResultSet rs = executeFetchStatement(query);
			if (rs == null) {
				return set;
			}
			while (rs.next()) {
				String businessId = rs.getString("business_id");
				set.add(businessId);
			}
		} catch (Exception e) {
			e.printStackTrace();
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
				
				// store into database
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
				String query = "INSERT IGNORE INTO restaurants "
						+ "VALUES ("
						+ "\"" + businessId + "\","
						+ "\"" + name + "\","
						+ "\"" + categories + "\","
						+ "\"" + city + "\","
						+ "\"" + state + "\","
						+ stars + ","
						+ "\"" + price + "\","
						+ "\"" + fullAddress + "\","
						+ latitude + ","
						+ longitude + ","
						+ "\"" + imageUrl + "\","
						+ "\"" + url + "\""
						+ ")";
				executeUpdateStatement(query);
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
		String query = "INSERT INTO users "
					+ "VALUES ("
					+ "'" + userId + "',"
					+ "'" + password + "',"
					+ "'" + firstName + "',"
					+ "'" + lastName + "'"
					+ ")";
			if (!executeUpdateStatement(query)) {
				return false;
			}
		return true;
	}
	
	/**
	 * Verify if user id and password are correct.
	 */
	@Override
	public boolean verifyLogin(String userId, String password) {
		try {
			String query = "SELECT user_id from users WHERE user_id='" + userId + "'"
					+ " and password='" + password + "'";
			ResultSet rs = executeFetchStatement(query);
			if (rs != null && rs.next()) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Get the full name, given the user id.
	 */
	@Override
	public String getFirstLastName(String userId) {
		String name = "";
		try {
			String query = "SELECT first_name, last_name from users WHERE user_id='" + userId + "'";
			ResultSet rs = executeFetchStatement(query);
			if (rs != null && rs.next()) {
				name += rs.getString("first_name") + " " + rs.getString("last_name");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return name;
	}

}
