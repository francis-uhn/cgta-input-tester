/*
 * Created on Sep 24, 2012
 * ListenerTester.java
 * 
 */
package ca.cgta.input.listener;

import java.net.MalformedURLException;

import javax.servlet.ServletException;
import javax.xml.bind.JAXBException;

import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;

import ca.cgta.input.listener.Listener;
import ca.cgta.input.listener.Persister;
import ca.cgta.input.model.config.Contributor;
import ca.cgta.input.model.config.ContributorConfigFactory;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Application;
import ca.uhn.hl7v2.app.ApplicationException;
import ca.uhn.hl7v2.model.v25.message.ADT_A01;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.ValidationContextImpl;

/**
 * 
 * @author <a href="mailto:neal.acharya@uhn.on.ca">Neal Acharya</a>
 * @version $Revision:$ updated on $Date:$ by $Author:$
 */
public class ListenerTester {

    /**
     * 
     * @param args ...
     * @throws JAXBException 
     * @throws ServletException 
     * @throws MalformedURLException 
     * @throws HL7Exception 
     * @throws ApplicationException 
     */
    public static void main(String[] args) throws JAXBException, ServletException, MalformedURLException, HL7Exception, ApplicationException {
        
        System.setProperty("sail.env.id", "DEV");
        System.setProperty("sail.box.id", "dev2");
        System.setProperty("sail.domain.id", "cgta");
        System.setProperty("sail.services.infrastructure.host", "localhost");
        System.setProperty("sail.services.infrastructure.port", "19080");
        
        
        String msg = 
            "MSH|^~\\&|1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1|UHN^1.3.6.1.4.1.12201|ConnectingGTA|ConnectingGTA|20120919000100-0400|2954864636aaa|ADT^A03^ADT_A03|137768|T|2.5|||NE|AL|CAN|8859/1\r" + 
            "EVN||201209190001\r" + 
            "PID|1||7014670^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR~0000057448^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^JHN^^^^CANON&Ontario&HL70363~SJR62595^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^PI||Sjrh^Testoneop^^^^^L||19700201|M|||21 jump street^^NORTH YORK^ON^M3A 1Y8^Can^H||(416)444-4444^PRN^PH^^1^416^4444444||eng^English^HL70296|||||||||||||||N\r" + 
            "ROL|||PP^Primary Care Provider^HL70443|13546^Generic^Physician^Moe^^Dr.^MD^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|||||||^^^ON^^Can|(416) 340-3391^WPN^PH^^1^416^3403391\r" + 
            "MRG|||||^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1\r" + 
            "PV1|1|O|Ambulatory^^^1.3.6.1.4.1.12201^^^^C|OP||^^^1.3.6.1.4.1.12201||155056^Generic^Use^^^Dr.^MD^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1||||||||N|||31236000006^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^VN|||||||||||||||||||||||||20120917162800-0400|20120917235900-0400\r" + 
            "PV2|||08.9^# l/e\r";
        
        
        String url = "http://uhnvprx01t.uhn.ca:5984";        
        String dbName = "neal_test_db";
        String user = "admin";
        String pwd = "denali6194";
        HttpClient httpClient = new StdHttpClient.Builder().url(url).username(user).password(pwd).connectionTimeout(10000).build();
        CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);        
        StdCouchDbConnector connector = new StdCouchDbConnector(dbName, dbInstance);        
        Persister.setConnector(connector);
        
        Listener listener = new Listener(true);
        listener.init(null);
        ContributorConfigFactory cf = ContributorConfigFactory.getInstance();
        Contributor c = cf.getContributorConfig().getHspId9004ToContributor().get("1.3.6.1.4.1.12201");
        Application app = listener.new MyApplication(c,14018,listener);
        PipeParser parser = new PipeParser();
        parser.setValidationContext(new ValidationContextImpl());
        ADT_A01 hl7Msg = new ADT_A01();
        hl7Msg.setParser(parser);
        hl7Msg.parse(msg);
        app.processMessage(hl7Msg);
        

    }

}
