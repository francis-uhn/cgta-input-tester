package ca.cgta.dataviewer.server;

import javax.servlet.http.HttpSession;

import ca.cgta.dataviewer.client.SecurityService;
import ca.cgta.dataviewer.shared.ActiveUser;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server-side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class SecurityServiceImpl extends RemoteServiceServlet implements
		SecurityService {

	/** Return a data representation of the active user - based on the session information **/
	public String userAccountServer() throws IllegalArgumentException {
		HttpSession session = getThreadLocalRequest().getSession(true);
		
		String retVal;
		
		// Certainly not guaranteed to be set, or exist:
		retVal = (String) session.getAttribute("ActiveUser");
		return retVal;
	}
	
	/** Perform the ActiveDirectory lookup, and set the session information if the credentials are valid **/
	// If LOGIN_OK is not returned from here, the returned text will display as an error message.
	public String loginServer(String username, String password) {
		HttpSession session = getThreadLocalRequest().getSession(true);
		
		// Check if there is already a user logged in:
		if(userAccountServer() != null) {
			AuditLogger.logString("A user is already logged in, not authenticating an additional user.");
			return null;
		}
		
		//	ActiveUser loggedInUser = new ActiveUser();
		//	loggedInUser.UserName = "Test User";
		//	session.setAttribute("ActiveUser", loggedInUser.toString());
		
		ActiveDirectoryAuthentication AD_Auth = new ca.cgta.dataviewer.server.ActiveDirectoryAuthentication("UHN.ca");
		boolean AuthenticateResult = false;
		
		try {
			AuthenticateResult = AD_Auth.authenticate(username, password);
		} catch (Exception e) {
			session.setAttribute("ActiveUser", "Exception occured Authenticating - " + username);
			AuditLogger.logString("Exception occured attempting authentication: " + username);
			session.invalidate();
			return "Exception occured attempting authentication";
		}

		if(AuthenticateResult) {
			session.setAttribute("ActiveUser", username);
			AuditLogger.logString("Successful Login: " + username);
			return "LOGIN_OK";	
		} else {
			AuditLogger.logString("Invalid username or password: " + username);
			session.invalidate();
		}
		
		session.setAttribute("ActiveUser", username);
		
		// If LOGIN_OK is not returned from here, the returned text will display as an error message.
		return "Invalid username or password";
	}
	
	/** Destroy the current session, log the user out, and confirm they have been logged out **/
	// If LOGGED_OUT is returned from here, the client is satisfied we have successfully logged out
	public String logoutServer() {
		HttpSession session = getThreadLocalRequest().getSession(true);
		session.invalidate();
		// session.setAttribute("ActiveUser", username);
		return "LOGGED_OUT";
		
	}
	
	/*	Original function, from sample GreetingServiceImpl.
	public String greetServer(String input) throws IllegalArgumentException {
		// Verify that the input is valid. 
		if (!FieldVerifier.isValidName(input)) {
			// If the input is not valid, throw an IllegalArgumentException back to
			// the client.
			throw new IllegalArgumentException(
					"Name must be at least 4 characters long");
		}

		String serverInfo = getServletContext().getServerInfo();
		String userAgent = getThreadLocalRequest().getHeader("User-Agent");

		// Escape data from the client to avoid cross-site script vulnerabilities.
		input = escapeHtml(input);
		userAgent = escapeHtml(userAgent);

		if(input.indexOf("cgtauser") >= 0) {
			return "Hello, " + input + "!<br><br>I am running " + serverInfo
					+ ".<br><br>It looks like you are using:<br>" + userAgent
					+ "LOGIN_OK";
		}
		
		return "Hello, " + input + "!<br><br>I am running " + serverInfo
				+ ".<br><br>It looks like you are using:<br>" + userAgent
				+ input.indexOf("cgtauser") + "was the found index";
	}
	*/

	
	/**
	 * Escape an html string. Escaping data received from the client helps to
	 * prevent cross-site script vulnerabilities.
	 * 
	 * @param html the html string to escape
	 * @return the escaped string
	 */
	private String escapeHtml(String html) {
		if (html == null) {
			return null;
		}
		return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;");
	}
}
