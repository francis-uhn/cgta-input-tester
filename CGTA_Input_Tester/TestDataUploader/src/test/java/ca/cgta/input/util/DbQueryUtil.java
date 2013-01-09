package ca.cgta.input.util;

import java.util.List;

import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;

import ca.cgta.input.listener.Persister;
import ca.cgta.input.model.inner.ClinicalDocumentData;
import ca.cgta.input.model.inner.ClinicalDocumentSection;
import ca.cgta.input.model.inner.Visit;
import ca.cgta.input.model.outer.ClinicalDocumentContainer;
import ca.cgta.input.model.outer.PatientWithVisitsContainer;


public class DbQueryUtil {
    
  

	
	
	/**
	 * 
	 * 
	 * @param args
	 * @throws Exception ...
	 */
	public static void main(String args[]) throws Exception{
	    
	    String hspId = "2.16.840.1.113883.3.239.23.8";
	    String sysId = "2.16.840.1.113883.3.239.23.8.101.1";
        String mrn = "99990";
        String placerGroup = "";
        String url = "http://uhnvprx01t.uhn.ca:5984";	       
        String dbName = "cgta_input_test_db";
        String user = "admin";
        String pwd = "denali6194";
//        String dbName = "neal_test_db";	    
        
        
	    HttpClient httpClient = new StdHttpClient.Builder().url(url).username(user).password(pwd).connectionTimeout(10000).build();
        CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);        
        StdCouchDbConnector connector = new StdCouchDbConnector(dbName, dbInstance);        
        Persister.setConnector(connector);
//        ViewUploader.uploadAllViews(connector);	    
	    
        getClinDocWithJpg(hspId, sysId, placerGroup);
        getPatientWithVisitDoc(hspId, sysId, mrn);         
        
	    
	}
	
	
   
        
    /**
     * 
     *   
     * @throws Exception ...
     */    
    private static void getClinDocWithJpg(String hspId, String sysId, String placerGroupNumber) throws Exception {
        
        String id = "CDOC_" + hspId + "___" + sysId + "___" + placerGroupNumber;
        
        ClinicalDocumentContainer retVal = Persister.getConnector().get(ClinicalDocumentContainer.class, id);
        
        System.out.println(retVal.getId());
        
        List<ClinicalDocumentSection> clinDocSections = retVal.getDocument().mySections;
        
        if (clinDocSections == null) {
            return;
        }
        
        for (ClinicalDocumentSection clinicalDocumentSection : clinDocSections) {
            
            System.out.println(clinicalDocumentSection);            
            
            List<ClinicalDocumentData> cDatas = clinicalDocumentSection.myData;
            
            if (cDatas == null) {
                continue;
            }
            
            for (ClinicalDocumentData clinicalDocumentData : cDatas) {
                System.out.println(clinicalDocumentData);
            }            
        }
                
    }
    
    
    /**
     * 
     *   
     * @throws Exception ...
     */    
    private static void getPatientWithVisitDoc(String hspId, String sysId, String mrn) throws Exception {
        
        String id = "PWV_" + hspId + "___" + sysId + "___" + mrn; 
        PatientWithVisitsContainer retVal;
        retVal = Persister.getConnector().get(PatientWithVisitsContainer.class, id);        
        System.out.println(retVal.getId());
        System.out.println(retVal.getDocument().myPatient);
        retVal.getDocument().ensureVisits();
        List<Visit> visits = retVal.getDocument().myVisits;        
        for (Visit visit : visits) {
            System.out.println(visit);            
        }
        
        
        
                
    }    
    
    
    
 
	

}
