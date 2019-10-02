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
 * Servlet implementation class SignupServlet
 */
@WebServlet("/SignupServlet")
public class SignupServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public SignupServlet() {
        super();
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DBConnection connection = ServletDBConnection.getDBConnection();
		try {
			JSONObject msg = new JSONObject();
			// get request parameters for user id and password
			String userId = request.getParameter("user_id");
			String pwd = request.getParameter("password");
			String firstName = request.getParameter("first_name");
			String lastName = request.getParameter("last_name");
			if (connection.signUp(userId, pwd, firstName, lastName)) {
				HttpSession session = request.getSession();
				// store both the user id and password in the session, and we will verify them with the db whenever we access a servlet
				session.setAttribute("user", userId);
				session.setAttribute("password", pwd);
				// setting session to expire in 30 minutes
				session.setMaxInactiveInterval(30 * 60);
				// get user's full name
				String name = firstName + " " + lastName;
				msg.put("status", "OK");
				msg.put("user_id", userId);
				msg.put("name", name);
			} else {
				msg.put("status", "error, user ID may exist");
				// 401 UNAUTHORIZED; the request has not been applied because it lacks valid authentication credentials for the target resource.
				response.setStatus(401);
			}
			RpcParser.writeOutput(response, msg);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
