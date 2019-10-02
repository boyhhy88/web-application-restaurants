package api;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class LogoutServlet
 */
@WebServlet("/LogoutServlet")
public class LogoutServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public LogoutServlet() {
        super();
    }
    
    /**
     * Invalidate the session if exists.
     */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Returns the current HttpSession associated with this request or, if there is no current session 
		// and create is true, returns a new session. If create is false and the request has no valid HttpSession, this method returns null.
		HttpSession session = request.getSession(false);
		if (session != null) {
			// Invalidates this session then unbinds any objects bound to it. 
			session.invalidate();
		}
		// Sends a temporary redirect response to the client using the specified redirect location URL and clears the buffer. 
		response.sendRedirect("index.html");
	}

}
