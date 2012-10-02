package ca.cgta.couchdb.tools;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.ektorp.CouchDbConnector;
import org.ektorp.DocumentNotFoundException;
import org.ektorp.support.DesignDocument;

import ca.cgta.input.listener.Persister;

public class ViewUploader {

	public static void main(String[] args) throws Exception {
		uploadAllViews();
		System.out.println("End");
	}

	public static void uploadAllViews() throws Exception, IOException {
	    CouchDbConnector db = Persister.getConnector();
	    
		uploadAllViews(db);
    }
	
	
    public static void uploadAllViews(CouchDbConnector connector) throws Exception, IOException {
        
        CouchDbConnector db = connector;
        
        String id = "_design/application";
        DesignDocument dd = new DesignDocument(id);
      
        // Add views to the document
        addToDesignDocument(dd, "loadedActsByPid","loadedActs.js");
        addToDesignDocument(dd, "moreThanOne","severalObservations.js");
        addToDesignDocument(dd, "allVisits","allVisits.js");
        addToDesignDocument(dd, "allDocuments", "allDocuments.js");
        addToDesignDocument(dd, "getPatientByMrn", "getPatientByMrn.js","_count");
        addToDesignDocument(dd, "getActiveVisitsByAdmitDate", "getActiveVisitsByAdmitDate.js");
        addToDesignDocument(dd, "unLoadedActsComplex","unLoadedActsComplex.js");
        addToDesignDocument(dd, "allView","allView.js");
        addToDesignDocument(dd, "getEDById", "getEDById.js");
        addToDesignDocument(dd, "getPatientByHealthCard", "getPatientByHealthCard.js", "_count");
        
        
        try {
            DesignDocument exist = db.get(DesignDocument.class, id);
            db.delete(exist);
        } catch (DocumentNotFoundException e) {
            // ignore
        }

        db.create(dd);
    }	
	
	
	
	
	
	private static void addToDesignDocument(DesignDocument dd, String viewName, String filePath) throws IOException
	{
		String viewText = readFileAsString(filePath);
		dd.addView(viewName, new DesignDocument.View(viewText));
	}
	
	private static void addToDesignDocument(DesignDocument dd, String viewName, String filePath,String reduseFunction) throws IOException
	{
		String viewText = readFileAsString(filePath);
		DesignDocument.View v = new DesignDocument.View(viewText);
		v.setReduce(reduseFunction);
		dd.addView(viewName, v);
	}
	

	
    private static String readFileAsString(String filePath) throws java.io.IOException {
        InputStream inputStream = ViewUploader.class.getClassLoader().getResourceAsStream(filePath);
        String retVal = IOUtils.toString(inputStream);
        return retVal;
    }	
	
	
	
}
