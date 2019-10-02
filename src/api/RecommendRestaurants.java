package api;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;

import db.DBConnection;

/**
 * Servlet implementation class RecommendRestaurants
 */
@WebServlet("/recommendation")
public class RecommendRestaurants extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public RecommendRestaurants() {
        super();
    }
    
    /**
     * Get and return JSON array of recommended restaurants for a user id.
     */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DBConnection connection = ServletDBConnection.getDBConnection();
		// verify session
		if (!RpcParser.sessionValid(request, connection)) {
			response.setStatus(403);
			return;
		}
		JSONArray array = new JSONArray();
		// getParameterMap() returns an immutable java.util.Map containing parameter names as keys and parameter values 
		// as map values. The keys  are of type String. The values are of type String array.
		if (request.getParameterMap().containsKey("user_id")) {
			String userId = request.getParameter("user_id");
			array = connection.recommendRestaurants(userId);
		}
		RpcParser.writeOutput(response, array);
	}

}
