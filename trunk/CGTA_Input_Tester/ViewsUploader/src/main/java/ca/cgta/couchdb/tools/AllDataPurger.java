package ca.cgta.couchdb.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.ektorp.CouchDbConnector;

import ca.cgta.input.listener.Persister;
import ca.cgta.input.model.outer.ClinicalDocumentContainer;

public class AllDataPurger {
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(AllDataPurger.class);
	
	public static void main(String[] args) throws Exception {
		
		purgeAllData();
		
		
	}

	public static void purgeAllData() throws Exception, IOException {
	    CouchDbConnector con = Persister.getConnector();
		List<String> allDocIds = con.getAllDocIds();
		
		ourLog.info("Deleting all data in DB from {}", con.getDatabaseName());
		
		int index = 0;
		Pattern p = Pattern.compile("._rev...([0-9a-z-]+)\\\"");
		for (String next : allDocIds) {
			index++;
			
			ourLog.info("Deleting document {}/{} - {}", new Object[] {index, allDocIds.size(), next});
			InputStream is = con.getAsStream(next);
			StringWriter writer = new StringWriter();
			IOUtils.copy(is, writer);
			String obj = writer.toString();

			ourLog.info("Done streaming document");

			Matcher matcher = p.matcher(obj);
			if (matcher.find()) {
				String rev = matcher.group(1);
				con.delete(next, rev);
			}
			
		}
    }
	
	
    public static void purgeAllData(CouchDbConnector con) throws Exception, IOException {        
        List<String> allDocIds = con.getAllDocIds();
        
        ourLog.info("Deleting all data in DB from {}", con.getDatabaseName());
        
        int index = 0;
        Pattern p = Pattern.compile("._rev...([0-9a-z-]+)\\\"");
        for (String next : allDocIds) {
            index++;
            
            ourLog.info("Deleting document {}/{} - {}", new Object[] {index, allDocIds.size(), next});
            InputStream is = con.getAsStream(next);
            StringWriter writer = new StringWriter();
            IOUtils.copy(is, writer);
            String obj = writer.toString();
            
            Matcher matcher = p.matcher(obj);
            if (matcher.find()) {
                String rev = matcher.group(1);
                con.delete(next, rev);
            }
            
        }
    }
    
    
    public static void purgeClinicalDoc(CouchDbConnector con, String id) throws Exception, IOException {        
        ClinicalDocumentContainer  doc = con.get(ClinicalDocumentContainer.class, id);
        con.delete(doc);
        
    }
    
    
    public static void purgeAllMatchingDocs(CouchDbConnector con, String docIdSubstring)
            throws Exception, IOException {

        List<String> allDocIds = con.getAllDocIds();
        Pattern p = Pattern.compile("._rev...([0-9a-z-]+)\\\"");

        for (String next : allDocIds) {
            if (next.contains(docIdSubstring)) {
                ourLog.info("Deleting document - {}", new Object[] { next });
                InputStream is = con.getAsStream(next);
                StringWriter writer = new StringWriter();
                IOUtils.copy(is, writer);
                String obj = writer.toString();
                Matcher matcher = p.matcher(obj);
                if (matcher.find()) {
                    String rev = matcher.group(1);
                    con.delete(next, rev);
                }
            }
        }

    }    
    
	
	
	
	
}
