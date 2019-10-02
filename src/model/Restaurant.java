package model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Restaurant {
	private String businessId;
	private String name;
	private String categories;
	private String city;
	private String state;
	private String fullAddress;
	private double stars;
	private String price;
	private double latitude;
	private double longitude;
	private String imageUrl;
	private String url;
	private double distance;
	
	/**
	 * Constructor given JSON object. Distance is optional.
	 * @param object
	 */
	public Restaurant(JSONObject object) {
		try {
			if (object != null) {
				this.businessId = object.getString("id"); // business id cannot be empty
				// [] is JSON array, {} is JSON object
				JSONArray jsonArray = object.has("categories") ? (JSONArray) object.get("categories") : new JSONArray();
				List<String> list = new ArrayList<>();
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject subObj = jsonArray.getJSONObject(i);
					// Produce a JSONArray containing the values of the members of this JSONObject.
					JSONArray subArray = subObj.toJSONArray(subObj.names());
					for (int j = 0; j < subArray.length(); j++) {
						list.add(parseString(subArray.getString(j)));
					}
				}
				this.categories = String.join(",", list); // all categories separated by comma
				// Get an optional string associated with a key. It returns an empty string if there is no such key.
				this.name = object.optString("name");
				this.imageUrl = object.optString("image_url");
				// Get an optional double associated with a key, or the defaultValue if there is no such key or if its value is not a number.
				this.stars = object.optDouble("rating", 0.0);
				this.price = object.optString("price");
				this.url = object.optString("url");
				JSONObject coordinates = object.has("coordinates") ? (JSONObject) object.get("coordinates") : new JSONObject();
				this.latitude = coordinates.optDouble("latitude");
				this.longitude = coordinates.optDouble("longitude");
				JSONObject location = object.has("location") ? (JSONObject) object.get("location") : new JSONObject();
				this.city = location.optString("city");
				this.state = location.optString("state");
				this.fullAddress = jsonArrayToString(location.has("display_address") ? (JSONArray) location.get("display_address") : new JSONArray());
				this.distance = object.optDouble("distance", -1.0);
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** 
	 * Constructor given all fields.
	 * @param businessId
	 * @param name
	 * @param categories
	 * @param city
	 * @param state
	 * @param fullAddress
	 * @param stars
	 * @param price
	 * @param latitude
	 * @param longitude
	 * @param imageUrl
	 * @param url
	 * @param distance
	 */
	public Restaurant(String businessId, String name, String categories, String city, String state, String fullAddress,
			double stars, String price, double latitude, double longitude, String imageUrl, String url, double distance) {
		super();
		this.businessId = businessId;
		this.name = name;
		this.categories = categories;
		this.city = city;
		this.state = state;
		this.fullAddress = fullAddress;
		this.stars = stars;
		this.price = price;
		this.latitude = latitude;
		this.longitude = longitude;
		this.imageUrl = imageUrl;
		this.url = url;
		this.distance = distance;
	}

	public String getBusinessId() {
		return businessId;
	}
	public void setBusinessId(String businessId) {
		this.businessId = businessId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCategories() {
		return categories;
	}
	public void setCategories(String categories) {
		this.categories = categories;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getFullAddress() {
		return fullAddress;
	}
	public void setFullAddress(String fullAddress) {
		this.fullAddress = fullAddress;
	}
	public double getStars() {
		return stars;
	}
	public void setStars(double stars) {
		this.stars = stars;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	
	/**
	 * Convert this Restaurant object to a JSON object.
	 */
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("business_id", businessId);
			obj.put("name", name);
			obj.put("categories", stringToJSONArray(categories));
			obj.put("city", city);
			obj.put("state", state);
			obj.put("full_address", fullAddress);
			obj.put("stars", stars);
			obj.put("price", price);
			obj.put("latitude", latitude);
			obj.put("longitude", longitude);
			obj.put("image_url", imageUrl);
			obj.put("url", url);
			obj.put("distance", distance);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj;
	}
	
	/**
	 * Replace " by \" in string; in categories, A/B means A or B, replace / by " or ".
	 */
	public static String parseString(String str) {
		// in Java, use \" to represent ", use \\ to represent \
		return str.replace("\"", "\\\"").replace("/", " or ");
	}
	
	/**
	 * Convert JSON array to string, separated by comma.
	 */
	public static String jsonArrayToString(JSONArray array) {
		StringBuilder sb = new StringBuilder();
		try {
			for (int i = 0; i < array.length(); i++) {
				String obj = (String) array.get(i);
				sb.append(obj);
				if (i != array.length() - 1) {
					sb.append(",");
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	/**
	 * Convert string ("..., ..., ...") to JSON array.
	 */
	public static JSONArray stringToJSONArray(String str) {
		try {
			return new JSONArray("[" + parseString(str) + "]");
		} catch (JSONException e) {
			System.out.println("string to JSON Array error:");
			System.out.println(str);
			e.printStackTrace();
		}
		return null;
	}
	
}
