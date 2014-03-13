package ca.cgta.dataviewer.server;

import java.io.File;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.xml.DOMConfigurator;

import ca.cgta.dataviewer.server.SearchOIDServiceImpl;

/**
 * <p>
 * AuditLogger logs searches to be performed by the utility.
 * </p>
 */
public class AuditLogger {

	/**
	 * Save a line item to the audit configuration specified in audit_log_settings.xml
	 * 
	 * @param the string to log
	 * @return true if log to audit file was successful, false if not
	 */
	public static boolean logString(String strLogItem) {

		// Log the search
		String fileName = ""; // C:\\Users\\t33377uhn\\workspace\\cgta-input-tester\\CGTA_Data_Viewer\\war\\WEB-INF\\audit_log_settings.xml";
	    
		File f = new File(SearchOIDServiceImpl.class.getClassLoader().getResource("").getPath());
		String path = f.getParent();
		fileName = path.substring(path.lastIndexOf("\\")+1,path.length()) + File.separator + "audit_log_settings.xml";
		
		try {
			Logger root = Logger.getRootLogger();
		    Layout layout = new PatternLayout("%p [%t] %c (%F:%L) - %m%n");
		    root.addAppender(new ConsoleAppender(layout, ConsoleAppender.SYSTEM_OUT));
		    
			//	Configuration can be loaded from XML or not:
		    DOMConfigurator.configure(fileName);
		    //	PropertyConfigurator.configure(args[0]);
		    
			Logger c = Logger.getLogger("some.cat");    
			//	c.trace("Hello");
			c.debug(strLogItem);
			return true;
		} catch(ClassCastException e) {
			LogLog.error("Unable to configure Audit Logger", e);
			return false;
		} catch(Exception e) {
			/** TODO: This does NOT catch an invalid file name or invalid drive, don't know why
			 *  Need to fix so the search will not occur of logging files.
			 */
			LogLog.error("Unable to configure Audit Logger", e);
			return false;
		} 
	}
	
	
}
