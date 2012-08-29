package ca.cgta.input.val.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

/**
 * @author t21703uhn
 */
public class DocumentLibraryServiceLocalImpl extends DocumentLibraryServiceAbstract {

	private static final long serialVersionUID = 1L;

	Logger logger = Logger.getLogger(DocumentLibraryServiceLocalImpl.class.getName());

	private String basePath = "/opt/glassfish/consoledocs/";


	/**
	 * {@inheritDoc}
	 */
	public void getDocumentImpl(HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		String fileToGet = request.getParameter("getFile");
		logger.log(Level.INFO, "Received download request for: " + fileToGet);

		// Sanitize
		fileToGet = fileToGet.replace("..", "");
		while (fileToGet.startsWith("/")) {
			fileToGet = fileToGet.substring(1);
		}
		while (fileToGet.startsWith("\\")) {
			fileToGet = fileToGet.substring(1);
		}
		
		File file = new File(basePath, fileToGet);
		
		// read and output to requested file
		InputStream in = null;
		ServletOutputStream out = null;
		try {
			in = new BufferedInputStream(new FileInputStream(file));
			out = response.getOutputStream();

			response.setContentType(URLConnection.getFileNameMap().getContentTypeFor(fileToGet));
			response.setHeader("Content-Disposition", "attachment; filename=" + FilenameUtils.getName(fileToGet).replace(" ", "_"));

			IOUtils.copy(in, out);
			
		} catch (Exception e) {
			// Rethrow as ServletException to prevent exposing our base path
			logger.log(Level.SEVERE, "Attempt to retreive inexistent file [" + request.getParameter("getFile") + "]");
			throw new ServletException("File not found!");
		} finally {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public LibraryCategory getLibraryTreeImpl() {
		return getFileTreeImpl(new File(basePath), "");
	}


	/**
	 * Recursive implementation of {@link #getLibraryTreeImpl()}
	 */
	public LibraryCategory getFileTreeImpl(File the, String theRelativePath) {
		while (theRelativePath.startsWith(File.separator)) {
			theRelativePath = theRelativePath.substring(1);
		}
		
		LibraryCategory libRoot = new LibraryCategory(the.getName());

		File[] children = the.listFiles();

		for (int i = 0; i < children.length; i++) {
			File next = children[i];

			if (next.isDirectory()) {
				libRoot.addCategory(getFileTreeImpl(next, theRelativePath + File.separatorChar + next.getName()));
			} else {
				libRoot.addDocument(new LibraryDocument(FilenameUtils.getBaseName(next.getName()), FilenameUtils.getExtension(next.getName()), theRelativePath, next.length()));
			}
		}

		return libRoot;
	}




}
