package api;

import java.io.BufferedReader;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;

import db.DBConnection;

public class RpcParser {
	
	/**
	 * Read the HTTP request and convert it to a JSON object.
	 */
	public static JSONObject parseInput(HttpServletRequest request) {
		StringBuffer sb = new StringBuffer();
		String line = null;
		try {
			// Retrieves the body of the request as character data using a BufferedReader
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			reader.close();
			return new JSONObject(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Write a JSON object into the HTTP response and send it to the client.
	 */
	public static void writeOutput(HttpServletResponse response, JSONObject obj) {
		try {
			// tell the browser that server is returning response in JSON format
			response.setContentType("application/json");
			// allow all viewers to view the response
			response.addHeader("Access-Control-Allow-Origin", "*");
			// returns a PrintWriter object that can send character text to the client.
			PrintWriter out = response.getWriter();
			out.print(obj);
			// calling flush() on the PrintWriter commits the response. 
			out.flush();
			// close the response
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Write a JSON array into the HTTP response and send it to the client.
	 */
	public static void writeOutput(HttpServletResponse response, JSONArray array) {
		try {
			response.setContentType("application/json");
			response.addHeader("Access-Control-Allow-Origin", "*");
			PrintWriter out = response.getWriter();
			out.print(array);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Verify if the session is valid.
	 * Step 1: check if the session is logged in. 
	 * Step 2: check with the db if the user id and password in the session are correct.
	 * Step 3: check if the user id in the url and in the session match
	 * @param request
	 * @param connection
	 * @return
	 */
	public static boolean sessionValid(HttpServletRequest request, DBConnection connection) {
		HttpSession session = request.getSession();
		if (session.getAttribute("user") == null || session.getAttribute("password") == null) {
			return false;
		}
		String user = (String) session.getAttribute("user");
		String pwd = (String) session.getAttribute("password");
		if (!connection.verifyLogin(user, pwd)) {
			return false;
		}
		String user_in_url = request.getParameter("user_id");
		if (user_in_url != null && !user_in_url.equals(user)) {
			return false;
		}
		return true;
	}

}
