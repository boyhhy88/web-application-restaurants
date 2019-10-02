package db;

import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public interface DBConnection {
	
	/**
	 * Close the connection.
	 */
	public void close();
	
	/**
	 * Insert the visited restaurants for a user.
	 * @param userId
	 * @param businessIds
	 */
	public boolean setVisitedRestaurants(String userId, List<String> businessIds);
	
	/**
	 * Delete the visited restaurants for a user.
	 * @param userId
	 * @param businessIds
	 */
	public boolean unsetVisitedRestaurants(String userId, List<String> businessIds);
	
	/**
	 * Get the visited restaurants for a user.
	 * @param userId
	 * @return
	 */
	public Set<String> getVisitedRestaurants(String userId);
	
	/**
	 * Get the restaurant json by id.
	 * @param businessId
	 * @param isVisited
	 * @return
	 */
	public JSONObject getRestaurantsById(String businessId, boolean isVisited);
	
	/**
	 * Get categories based on business id.
	 * @param businessId
	 * @return
	 */
	public Set<String> getCategories(String businessId);
	
	/**
	 * Get business id based on category
	 * @param category
	 * @return
	 */
	public Set<String> getBusinessId(String category);
	
	/**
	 * Recommend restaurants based on user id.
	 * @param userId
	 * @return
	 */
	public JSONArray recommendRestaurants(String userId);
	
	/**
	 * Search restaurants for a user based on coordinates
	 * @param userId
	 * @param lat
	 * @param lon
	 * @return
	 */
	public JSONArray searchRestaurants(String userId, double lat, double lon);
	
	/**
	 * Create new user
	 * @param userId
	 * @param password
	 * @param firstName
	 * @param lastName
	 * @return
	 */
	public boolean signUp(String userId, String password, String firstName, String lastName);
	
	
	/**
	 * Verify if the user id matches the password.
	 * @param userId
	 * @param password
	 * @return
	 */
	public boolean verifyLogin(String userId, String password);
	
	/**
	 * Get user's name based on user id.
	 * @param userId
	 * @return
	 */
	public String getFirstLastName(String userId);
}
