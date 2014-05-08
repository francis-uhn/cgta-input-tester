package ca.cgta.dataviewer.server;

import ca.cgta.dataviewer.client.SearchDocumentIDService;
import ca.cgta.dataviewer.server.AuditLogger;
import ca.cgta.dataviewer.shared.FieldVerifier;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;


/**
 * The server-side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class SearchDocumentIDServiceImpl extends RemoteServiceServlet implements
		SearchDocumentIDService {
	
	public String searchDocumentIDServer(String input)  throws IllegalArgumentException {
		// Verify that the input is valid. 
		if (!FieldVerifier.isValidName(input)) {
			// If the input is not valid, throw an IllegalArgumentException back to
			// the client.
			throw new IllegalArgumentException(
					"Invalid Input to DocumentID Search");
		}
		
		// Escape data from the client to avoid cross-site script vulnerabilities.
		input = escapeHtml(input);
		
		String clientIP   = getThreadLocalRequest().getRemoteAddr();
		
		// Log the search.  If the Audit log fails, do not perform the search. 
		if(AuditLogger.logString(clientIP + " SearchDocumentID: " + input)) {
			// Successfully logged - perform the search:
			//String outV3String = DatabaseService.main();
			//String outputString = HL7V3Translator.v3Translator(outV3String);
			
			String outputString = HL7V3Translator.v3Translator(input);
			
			return "<h2>Clinical Data search results for '" + input + "':</h2><br>" + outputString;
			
		} else {
			// Unsuccessfully logged - do not perform the search:
			return "Unable to log SearchDocumentID search, thus unable to perform search";
		}
		
	}

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
