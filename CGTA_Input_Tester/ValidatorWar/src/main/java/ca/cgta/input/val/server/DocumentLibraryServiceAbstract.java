package ca.cgta.input.val.server;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * @author t21703uhn
 */

public abstract class DocumentLibraryServiceAbstract extends RemoteServiceServlet implements ca.cgta.input.val.client.rpc.DocumentLibraryService {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(DocumentLibraryServiceAbstract.class.getName());


	/**
	 * {@inheritDoc}
	 * 
	 * This method needs to check for attempts to access restricted areas
	 * outside the document library. Subclasses may override the
	 * {@link #getDocumentImpl(HttpServletRequest,HttpServletResponse)} method.
	 */
	public final void doGet(HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		String fileParam = request.getParameter("getFile");
		if (StringUtils.isBlank(fileParam)) {
			logger.log(Level.SEVERE, "Attempt to retreive file with no getFile attribute");
			throw new ServletException("Invalid request.");
		}
		
		//check for attempt to access parent directory
		if (fileParam.contains("..")) {
			logger.log(Level.SEVERE, "Attempt to retreive file using invalid relative path [" + request.getParameter("getFile") + "]");
			throw new ServletException("Invalid request.");
		}

		getDocumentImpl(request, response);
	}


	/**
	 * Helper function for
	 * {@link #doGet(HttpServletRequest,HttpServletResponse)}
	 */
	public abstract void getDocumentImpl(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;


	/**
	 * {@inheritDoc}
	 * 
	 * Subclasses may override the {@link #getLibraryTreeImpl()}
	 */
	public final LibraryCategory getLibraryTree() throws Exception {
		return getLibraryTreeImpl();
	}


	/**
	 * Helper function for {@link #getLibraryTree()}
	 */
	public abstract LibraryCategory getLibraryTreeImpl() throws Exception;

}
