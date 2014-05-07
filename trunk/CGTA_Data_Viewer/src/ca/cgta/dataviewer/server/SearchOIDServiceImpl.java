package ca.cgta.dataviewer.server;

import java.io.File;
import java.io.InputStream;

import au.com.bytecode.opencsv.CSV;
import au.com.bytecode.opencsv.CSVReadProc;
import ca.cgta.dataviewer.client.SearchOIDService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;


/**
 * The server-side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class SearchOIDServiceImpl extends RemoteServiceServlet implements
		SearchOIDService {
	
	private static final CSV csv = CSV
            .separator(';')
            .quote('\'')
            .skipLines(1)
            .charset("UTF-8")
            .create();
	
	private String returnOIDs = "";
	
	public String searchOIDServer(String input)  throws IllegalArgumentException {
		
		String fileName = ""; // C:\\Users\\t33377uhn\\workspace\\cgta-input-tester\\CGTA_Data_Viewer\\table_9004.csv";
		
		//	System.out.println(this.class.getclassloader.getresource("");
		// System.out.println("System path: " + );
	    
		File f = new File(SearchOIDServiceImpl.class.getClassLoader().getResource("").getPath());
		String path = f.getParent();
		fileName = path.substring(path.lastIndexOf("\\")+1,path.length()) + File.separator + "table_9008.csv";
		System.out.println(fileName);  
		
		// CSVReader will be closed after end of processing
        // Less code to process CSV content -> less bugs
		
        csv.read(fileName, new CSVReadProc() {
                public void procRow(int rowIndex, String... values) {
                	//	returnOIDs.concat(values[0] + ":");
                	String[] csvSplit = values[0].split(",");
                	returnOIDs = returnOIDs + csvSplit[0] + "@" + csvSplit[1] + ":";
                	System.out.println(returnOIDs);
                	System.out.println(rowIndex);
                	
                	//	System.out.println(rowIndex + "# " + Arrays.asList(values));    
                }
        });
		
        //	System.out.println("Returning string " + returnOIDs); 
        return returnOIDs;
	}

}
