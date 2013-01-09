/*
 * Created on Apr 2, 2012
 * DocAttachmentsTester.java
 * 
 */
package ca.cgta.input;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.ektorp.Attachment;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.cgta.couchdb.tools.AllDataPurger;

/**
 * 
 * @author <a href="mailto:neal.acharya@uhn.on.ca">Neal Acharya</a>
 * @version $Revision:$ updated on $Date:$ by $Author:$
 */
public class DocAttachmentsTester {
    
    private static final String ADDRESS = "http://uhnvprx01t.uhn.ca:5984";
    private static final String DB = "neal_test_db";
    private static final Logger ourLog = LoggerFactory.getLogger(DocAttachmentsTester.class);
    private static Base64 ourBase64 = new Base64();
    private static final String user = "admin";
    private static final String pwd = "denali6194";

    /**
     * 
     * @param args ...
     * @throws Exception 
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException, Exception {
        

        // Create connector
        HttpClient httpClient = new StdHttpClient.Builder().url(ADDRESS).username(user).password(pwd).connectionTimeout(10000)
                .build();
        CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
        CouchDbConnector connector = new StdCouchDbConnector(DB, dbInstance);
        
        // Purge docs from the db        
        AllDataPurger.purgeAllData(connector);

        // Create doc
        SampleDoc sampleDoc = new SampleDoc();
        sampleDoc.myField1 = "val1";
        sampleDoc.myField2 = "val2";
        sampleDoc.myField3 = "val3";

        // Obtain electronic doc as Base64 encoded string
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "nephew.jpg");
        byte[] bytes = IOUtils.toByteArray(is);
        String encodeString = Base64.encodeBase64String(bytes);

        // Create attachment
        Attachment attachment = new Attachment("nephew", encodeString, "image/jpeg");

        // Create doc container, add doc and attachment
        SampleDocWithAttachmentContainer testDocContainer = new SampleDocWithAttachmentContainer(sampleDoc);
        testDocContainer.addAttachment(attachment);

        // Store doc container
        String id = "TD_1";
        connector.create(id, testDocContainer);
        ourLog.info("Document with attachment created. You can access the attachment here:  http://uhnvprx01t.uhn.ca:5984/neal_test_db/TD_1/nephew ");
            
    }

}
