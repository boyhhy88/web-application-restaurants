package api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;

/**
 * Servlet implementation class VisitHistory
 */
@WebServlet("/history")
public class VisitHistory extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public VisitHistory() {
        super();
        
    }
    
    /** 
     * Get and return the JSON array of visited (liked) restaurants for a user id
     */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DBConnection connection = ServletDBConnection.getDBConnection();
		try {
			// verify session
			if (!RpcParser.sessionValid(request, connection)) {
				response.setStatus(403);
				return;
			}
			if (request.getParameterMap().containsKey("user_id")) {
				JSONArray array = new JSONArray();
				String userId = request.getParameter("user_id");
				Set<String> visited_business_id = connection.getVisitedRestaurants(userId);
				for (String id : visited_business_id) {
					array.put(connection.getRestaurantsById(id, true));
				}
				RpcParser.writeOutput(response, array);
			} else {
				RpcParser.writeOutput(response, new JSONObject().put("status", "InvalidParameter"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Given a JSON object containing user id and business ids of visited (liked) restaurants, add to the db. 
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			DBConnection connection = ServletDBConnection.getDBConnection();
			// verify session
			if (!RpcParser.sessionValid(request, connection)) {
				response.setStatus(403);
				return;
			}
			JSONObject input = RpcParser.parseInput(request);
			// Determine if the JSONObject contains a specific key.
			if (input.has("user_id") && input.has("visited")) {
				String userId = (String) input.get("user_id");
				JSONArray array = (JSONArray) input.get("visited");
				List<String> visitedRestaurants = new ArrayList<>();
				for (int i = 0; i < array.length(); i++) {
					String businessId = (String) array.get(i);
					visitedRestaurants.add(businessId);
				}
				if (connection.setVisitedRestaurants(userId, visitedRestaurants)) {
					RpcParser.writeOutput(response, new JSONObject().put("status", "OK"));
				} else {
					RpcParser.writeOutput(response, new JSONObject().put("status", "Error"));
				}
			} else {
				RpcParser.writeOutput(response, new JSONObject().put("status", "InvalidParameter"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Given a JSON object containing user id and business ids of visited restaurants, delete from the db. 
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			DBConnection connection = ServletDBConnection.getDBConnection();
			// verify session
			if (!RpcParser.sessionValid(request, connection)) {
				response.setStatus(403);
				return;
			}
			JSONObject input = RpcParser.parseInput(request);
			// Determine if the JSONObject contains a specific key.
			if (input.has("user_id") && input.has("visited")) {
				String userId = (String) input.get("user_id");
				JSONArray array = (JSONArray) input.get("visited");
				List<String> visitedRestaurants = new ArrayList<>();
				for (int i = 0; i < array.length(); i++) {
					String businessId = (String) array.get(i);
					visitedRestaurants.add(businessId);
				}
				if (connection.unsetVisitedRestaurants(userId, visitedRestaurants)) {
					RpcParser.writeOutput(response, new JSONObject().put("status", "OK"));
				} else {
					RpcParser.writeOutput(response, new JSONObject().put("status", "Error"));
				}
			} else {
				RpcParser.writeOutput(response, new JSONObject().put("status", "InvalidParameter"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}	
}
