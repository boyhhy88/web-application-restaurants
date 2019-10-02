package api;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;

/**
 * Servlet implementation class LoginServlet
 */
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public LoginServlet() {
        super();
    }

    /**
     * GET method is used to verify if the current session is logged in or out.
     */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DBConnection connection = ServletDBConnection.getDBConnection();
		try {
			JSONObject msg = new JSONObject();
			if (!RpcParser.sessionValid(request, connection)) {
				// Sets the status code for this response.
				// 403 FORBIDDEN; the server understood the request but refuses to authorize it.
				response.setStatus(403);
				msg.put("status", "Session Invalid");
			} else {
				// Returns the current session associated with this request, or if the request does not have a session, creates one. 
				HttpSession session = request.getSession();
				// get user id
				String userId = (String) session.getAttribute("user");
				String name = connection.getFirstLastName(userId);
				msg.put("status", "OK");
				msg.put("user_id", userId);
				msg.put("name", name);
			}
			RpcParser.writeOutput(response, msg);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * POST method is the login implementation. It verifies the username and password with the database.
	 * If they match, store attributes in the session to mark this session as logged in.
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DBConnection connection = ServletDBConnection.getDBConnection();
		try {
			JSONObject msg = new JSONObject();
			// get request parameters for user id and password
			String userId = request.getParameter("user_id");
			String pwd = request.getParameter("password");
			if (connection.verifyLogin(userId, pwd)) {
				HttpSession session = request.getSession();
				// store both the user id and password in the session, and we will verify them with the db whenever we access a servlet
				session.setAttribute("user", userId);
				session.setAttribute("password", pwd);
				// setting session to expire in 30 minutes
				session.setMaxInactiveInterval(30 * 60);
				// get user name
				String name = connection.getFirstLastName(userId);
				msg.put("status", "OK");
				msg.put("user_id", userId);
				msg.put("name", name);
			} else {
				// 401 UNAUTHORIZED; the request has not been applied because it lacks valid authentication credentials for the target resource.
				response.setStatus(401);
			}
			RpcParser.writeOutput(response, msg);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
