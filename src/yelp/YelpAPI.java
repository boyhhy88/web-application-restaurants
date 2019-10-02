package yelp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Run as Java application to test the function.
 */

public class YelpAPI {
	private static final String API_HOST = "api.yelp.com";
	private static final String SEARCH_PATH = "/v3/businesses/search";
	private static final String DEFAULT_TERM = "restaurant";
	private static final int SEARCH_LIMIT = 20;
	private static final String SORT_BY = "best_match";
	private static final String API_KEY = "ALDKRlz2cbb-z8l07DUOoP1DCg1AYnVGaHo0sncTW36OVBzrU-abBMaxsxH6q2PZUGt_qiK5Sx9Ly8leL14r1jRncSGEAcyes1b0_xIN1v_yJ2VuYRDnIGVb7gR0XXYx";
	
	/**
	 * Search and get string of restaurants from yelp API, given the coordinates. 
	 * @param lat
	 * @param lon
	 * @return
	 */
	public String searchForBusinessesByLocation(double lat, double lon) {
		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put("latitude", Double.toString(lat));
		paramsMap.put("longitude", Double.toString(lon));
		paramsMap.put("limit", Integer.toString(SEARCH_LIMIT));
		paramsMap.put("sort_by", SORT_BY);
		paramsMap.put("term", DEFAULT_TERM);
		return sendRequestAndGetResponse(paramsMap);
	}
	
	/**
	 * Sent request to Yelp API with parameters, and get response string.
	 * @param paramsMap
	 * @return
	 */
	private String sendRequestAndGetResponse(Map<String, String> paramsMap) {
		String response = "";
		try {
			String urlString = "https://" + API_HOST + SEARCH_PATH + paramsStringBuilder(paramsMap);
			// Class URL represents a Uniform Resource Locator, a pointer to a "resource" on the World Wide Web. 
			URL url = new URL(urlString);
			// Returns a URLConnection instance that represents a connection to the remote object referred to by the URL. 
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			// Set the method for the URL request
			con.setRequestMethod("GET");
			// set request headers
			con.setRequestProperty("Authorization", "Bearer " + API_KEY);
			// get response
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			response = content.toString();
			in.close();
			// close the connection
			con.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}
	
	/**
	 *  Transform parameter map to string of the required format: ?param1=value1&param2=value2...
	 */
	private String paramsStringBuilder(Map<String, String> paramsMap) throws UnsupportedEncodingException {
		if (paramsMap == null || paramsMap.size() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("?");
		for(Map.Entry<String, String> entry : paramsMap.entrySet()) {
			// problem arises when special characters are used for their values. In general case, HTML handles the 
			// encoding part and automatically processes the special characters and convert them to special characters 
			// for smooth handling of all the operations. However it is not a good practice to rely solely on HTML 
			// features and thus java provides URLEncoder class to explicitly encode the URLs.
			sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
			sb.append("=");
			sb.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
			sb.append("&");
		}
		String result = sb.toString();
		return result.length() > 0 ? result.substring(0, result.length() - 1) : result;
	}
	
	/**
	 * Internal method to test YelpAPI and make sure the configuration is correct.
	 */
	private static void queryAPI(YelpAPI yelpApi, double lat, double lon) {
		String responseString = yelpApi.searchForBusinessesByLocation(lat, lon);
		try {
			JSONObject response = new JSONObject(responseString);
			JSONArray businesses = (JSONArray) response.get("businesses");
			for (int i = 0; i < businesses.length(); i++) {
				JSONObject business = (JSONObject) businesses.get(i);
				System.out.println(business);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Main entry for sample Yelp API requests.
	 */
	public static void main(String[] args) {
		YelpAPI yelpApi = new YelpAPI();
		queryAPI(yelpApi, 47.608, -122.341);
	}
}