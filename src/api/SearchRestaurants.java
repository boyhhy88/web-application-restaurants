package api;

import java.io.IOException;

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
 * Servlet implementation class SearchRestaurants
 */
@WebServlet("/restaurants")
public class SearchRestaurants extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    public SearchRestaurants() {
        super();
    }
	
    /**
     * Search and return JSON array of restaurants based on coordinates.
     */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DBConnection connection = ServletDBConnection.getDBConnection();
		// verify session
		try {
			if (!RpcParser.sessionValid(request, connection)) {
				response.setStatus(403);
				return;
			}
			if (request.getParameterMap().containsKey("user_id") && request.getParameterMap().containsKey("lat")
					&& request.getParameterMap().containsKey("lon")) {
				JSONArray array = new JSONArray();
				String userId = request.getParameter("user_id");
				// Returns a new double initialized to the value represented by the specified String, as performed by the valueOf method of class Double.
				double lat = Double.parseDouble(request.getParameter("lat"));
				double lon = Double.parseDouble(request.getParameter("lon"));
				array = connection.searchRestaurants(userId, lat, lon);
				RpcParser.writeOutput(response, array);
			} else {
				RpcParser.writeOutput(response, new JSONObject().put("status", "InvalidParameter"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
