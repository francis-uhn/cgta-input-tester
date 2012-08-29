package ca.cgta.input.util;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;

import ca.cgta.couchdb.tools.ViewUploader;
import ca.cgta.input.UhnConverter;
import ca.cgta.input.converter.Converter;
import ca.cgta.input.listener.Persister;
import ca.cgta.input.model.inner.MedicationOrder;
import ca.cgta.input.model.outer.ClinicalDocumentGroup;
import ca.cgta.input.model.outer.MedicationOrderWithAdmins;
import ca.uhn.hl7v2.model.v25.message.ADT_A01;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.message.RAS_O17;
import ca.uhn.hl7v2.model.v25.message.RDE_O11;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.preparser.PreParser;


public class SampleDataUploader {
    
  

	
	
	/**
	 * Command line args:
	 * u = URL of the couchDb instance to connect to
	 * d = couchDb database name
	 * m = mrn of the Patient to create reports for 
	 * 
	 * Example:
	 * -u http://uhnvprx01t.uhn.ca:5984 -d cgta_input_test_db -m 12345
	 * 
	 * 
	 * @param args
	 * @throws Exception ...
	 */
	public static void main(String args[]) throws Exception{
	    
        
        //create the command line parser
        CommandLineParser parser = new PosixParser();

        //create the Options
        Options options = new Options();

        options.addOption("u", "url", true, "URL of the couchDb instance");
        options.addOption("d", "databaseName", true, "Name of the couchDb database");
        options.addOption("m", "mrn", true, "Patient mrn");
        options.addOption("a", "altfacility", false, "Set this flag to upload using NYGH as the facility instead of UHN");
        
        // parse the command line arguments
        CommandLine cmdLine = null;
        cmdLine = parser.parse(options, args);

        //assign command line args to variables and use defaults if not supplied  
        String url = cmdLine.getOptionValue("u", "http://uhnvprx01t.uhn.ca:5984");
        String dbName = cmdLine.getOptionValue("d", "cgta_input_test_db");
        //String dbName = cmdLine.getOptionValue("d", "neal_test_db");
        String mrn = cmdLine.getOptionValue("m", "12345");
        if (cmdLine.hasOption("a")) {
        	UhnConverter.ourHspOid = "2.16.840.1.113883.3.239.23.8";
        	UhnConverter.ourFacility1Oid = "2.16.840.1.113883.3.239.23.8.100.1";
        	UhnConverter.ourRequestCodingSystemOid = "2.16.840.1.113883.3.239.23.8.102.2";
        	UhnConverter.ourResultCodingSystemOid = "2.16.840.1.113883.3.239.23.8.102.3";
        	UhnConverter.ourSendingSystemOid = "2.16.840.1.113883.3.239.23.8.101.1";
        }
        
	    HttpClient httpClient = new StdHttpClient.Builder().url(url).connectionTimeout(10000).build();
        CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);        
        StdCouchDbConnector connector = new StdCouchDbConnector(dbName, dbInstance);        
        Persister.setConnector(connector);
        ViewUploader.uploadAllViews(connector);	    
	    
        persistClinDoc(mrn);
        persistClinDoc2(mrn);
        persistClinDocWithJpg(mrn);
        persistClinDocWithGif(mrn);
        persistClinDocWithPdf(mrn);
        persistClinDocMultiSection(mrn);
        persistClinDocLab(mrn);
        persistPatientWithVisitsDoc(mrn);
        persistMedicationOrderWithAdminsDoc(mrn);
        persistDeactivatedPatientWithDocs(mrn);
        persistReoccuringPatientWithDocs(mrn);
//        persistMultipleAdt(null);
        
	    
	}
	
	
	
    /**
     * 
     *   
     * @throws Exception ...
     */    
    private static void persistClinDoc(String mrn) throws Exception {
        
        String message1 =
                "MSH|^~\\&|EPR|UHNG|||201205011031||ORU^R01^ORU_R01|31768|T|2.5\r" +
                "PID|1||"+mrn+"^^^UHN^MR^G^4265~9287170261^BL^^CANON^JHN^G||Smith^Joseph^John^Junior^Mr^MD^L~Smith^Joe^^^^^A|Blanche^^^^^^L|19310313|M|||26 RIVINGTON AVE^^Goderich^ON^N7A3Y2^CAN^H~7 WOODPLUMPTON ROAD^^Port Stanley^ON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English|||||||||||||||N\r" +                 
                "PV1|1|I|JS12^123^4^G^^Q Building^12^JS12-123-4 GIM Inpatient Unit||||38946^Blake^Donald^Thor^^^^^|38946^Blake^Donald^Thor^^^^^|38946^Blake^Donald^Thor^^^^^|GIM|||||||38946^Blake^Donald^Thor^^^^^||"+mrn+"VID1^^^2.16.840.1.113883.3.59.3:0947^VN^G\r" +                 
                "ORC|1|"+mrn+"OID1^EPR^2.16.840.1.113883.3.59.3:947|||CM\r" +                
                "OBR|1|"+mrn+"OID1^EPR^2.16.840.1.113883.3.59.3:947||50111^OR/Procedure Note1^HL70396|||20110126124300-0500|20110126125000-0500||||||||||N|||||||F|7776&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.1||||||5555&Smith&John&&&&Dr&&2.16.840.1.113883.4.347\r" +                 
                "OBX|1|DT|10017^Date Dictated^HL70396||12 Jan 2012||||||F\r" + 
                "OBX|2|ST|1126527^Dictated by^HL70396||John Smith, MD||||||F\r" + 
                "OBX|3|NM|14007^Pressure^HL70396||0|ml|-2 - 4|N|||F\r" +
                "NTE|1||This is Note line1||\r" +
                "NTE|2||This is Note line2||\r" +
                "NTE|3||This is Note line3||\r";
        
        List<ClinicalDocumentGroup> clinDocs = UhnConverter.convertOru(message1);
        Persister.persist(clinDocs);
                
    }
    
    
    
    
    
    /**
     * 
     *   
     * @throws Exception ...
     */    
    private static void persistClinDoc2(String mrn) throws Exception {
        
        String message1 =
                "MSH|^~\\&|EPR|UHNG|||201205011031||ORU^R01^ORU_R01|31768|T|2.5\r" +
                "PID|1||"+mrn+"^^^UHN^MR^G^4265~9287170261^BL^^CANON^JHN^G||Smith^Joseph^John^Junior^Mr^MD^L~Smith^Joe^^^^^A|Blanche^^^^^^L|19310313|M|||26 RIVINGTON AVE^^Goderich^ON^N7A3Y2^CAN^H~7 WOODPLUMPTON ROAD^^Port Stanley^ON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English|||||||||||||||N\r" +                 
                "PV1|1|I|JS12^123^4^G^^Q Building^12^JS12-123-4 GIM Inpatient Unit||||38946^Blake^Donald^Thor^^^^^|38946^Blake^Donald^Thor^^^^^|38946^Blake^Donald^Thor^^^^^|GIM|||||||38946^Blake^Donald^Thor^^^^^||"+mrn+"VID2^^^2.16.840.1.113883.3.59.3:0947^VN^G\r" +                 
                "ORC|1|"+mrn+"OID2^EPR^2.16.840.1.113883.3.59.3:947|||CM\r" +                
                "OBR|1|"+mrn+"OID2^EPR^2.16.840.1.113883.3.59.3:947||50111^OR/Procedure Note2^HL70396|||20110126124300-0500|20110126125000-0500||||||||||N|||||||F|7776&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.1||||||5555&Smith&John&&&&Dr&&2.16.840.1.113883.4.347\r" +                 
                "OBX|1|DT|10017^Date Dictated^HL70396||12 Jan 2012||||||F\r" + 
                "OBX|2|ST|1126527^Dictated by^HL70396||John Smith, MD||||||F\r" + 
                "OBX|3|NM|14007^Pressure^HL70396||0|ml|-2 - 4|N|||F\r" +
                "OBX|4|ST|1126528^Commments^HL70396||comment1~comment2~comment3||||||F\r" +
                "NTE|1||This is Note line1||\r" +
                "NTE|2||This is Note line2||\r" +
                "NTE|3||This is Note line3||\r";
        
        List<ClinicalDocumentGroup> clinDocs = UhnConverter.convertOru(message1);
        Persister.persist(clinDocs);
                
    }
    
    
	
		
	
    /**
     * 
     *   
     * @throws Exception ...
     */    
    private static void persistClinDocWithJpg(String mrn) throws Exception {
        
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "sampleJpg.jpg");
        byte[] bytes = IOUtils.toByteArray(is);
        String encodedEdVal = Base64.encodeBase64String(bytes);
        encodedEdVal = encodedEdVal.replaceAll("\n", "");
        encodedEdVal = encodedEdVal.replaceAll("\r", "");
        
        //System.out.println(encodedString);
                
        String message1 =
                "MSH|^~\\&|EPR|UHNG|||201205011031||ORU^R01^ORU_R01|31768|T|2.5\r" +
                "PID|1||"+mrn+"^^^UHN^MR^G^4265~9287170261^BL^^CANON^JHN^G||Smith^Joseph^John^Junior^Mr^MD^L~Smith^Joe^^^^^A|Blanche^^^^^^L|19310313|M|||26 RIVINGTON AVE^^Goderich^ON^N7A3Y2^CAN^H~7 WOODPLUMPTON ROAD^^Port Stanley^ON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English|||||||||||||||N\r" +                 
                "PV1|1|I|JS12^123^4^G^^Q Building^12^JS12-123-4 GIM Inpatient Unit||||38946^Blake^Donald^Thor^^^^^|38946^Blake^Donald^Thor^^^^^|38946^Blake^Donald^Thor^^^^^|GIM|||||||38946^Blake^Donald^Thor^^^^^||"+mrn+"VID3^^^2.16.840.1.113883.3.59.3:0947^VN^G\r" +                 
                "ORC|1|"+mrn+"OID3^EPR^2.16.840.1.113883.3.59.3:947|||CM\r" +                
                "OBR|1|"+mrn+"OID3^EPR^2.16.840.1.113883.3.59.3:947||50111^OR/Procedure Note3^HL70396|||20110126124300-0500|20110126125000-0500||||||||||N|||||||F|7776&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.1||||||5555&Smith&John&&&&Dr&&2.16.840.1.113883.4.347\r" +                 
                "OBX|1|DT|10017^Date Dictated^HL70396||12 Jan 2012||||||F\r" + 
                "OBX|2|ST|1126527^Dictated by^HL70396||John Smith, MD||||||F\r" + 
                "OBX|3|NM|14007^Pressure^HL70396||0|ml|-2 - 4|N|||F\r" +  
                "OBX|4|ED|14009^Picture^HL70396||^NS^JPEG^Base64^"+encodedEdVal+"||||||F\r" +
                "NTE|1||This is Note line1||\r" +
                "NTE|2||This is Note line2||\r" +
                "NTE|3||This is Note line3||\r";
        
        List<ClinicalDocumentGroup> clinDocs = UhnConverter.convertOru(message1);
        Persister.persist(clinDocs);
                
    }
    
    
    
    
    private static void persistClinDocWithGif(String mrn) throws Exception {
        
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "sampleGif.gif");
        byte[] bytes = IOUtils.toByteArray(is);
        String encodedEdVal = Base64.encodeBase64String(bytes);
        encodedEdVal = encodedEdVal.replaceAll("\n", "");
        encodedEdVal = encodedEdVal.replaceAll("\r", "");
        
        //System.out.println(encodedString);
                
        String message1 =
                "MSH|^~\\&|EPR|UHNG|||201205011031||ORU^R01^ORU_R01|31768|T|2.5\r" +
                "PID|1||"+mrn+"^^^UHN^MR^G^4265~9287170261^BL^^CANON^JHN^G||Smith^Joseph^John^Junior^Mr^MD^L~Smith^Joe^^^^^A|Blanche^^^^^^L|19310313|M|||26 RIVINGTON AVE^^Goderich^ON^N7A3Y2^CAN^H~7 WOODPLUMPTON ROAD^^Port Stanley^ON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English|||||||||||||||N\r" +                 
                "PV1|1|I|JS12^123^4^G^^Q Building^12^JS12-123-4 GIM Inpatient Unit||||38946^Blake^Donald^Thor^^^^^|38946^Blake^Donald^Thor^^^^^|38946^Blake^Donald^Thor^^^^^|GIM|||||||38946^Blake^Donald^Thor^^^^^||"+mrn+"VID4^^^2.16.840.1.113883.3.59.3:0947^VN^G\r" +                 
                "ORC|1|"+mrn+"OID4^EPR^2.16.840.1.113883.3.59.3:947|||CM\r" +                
                "OBR|1|"+mrn+"OID4^EPR^2.16.840.1.113883.3.59.3:947||50111^OR/Procedure Note4^HL70396|||20110126124300-0500|20110126125000-0500||||||||||N|||||||F|7776&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.1||||||5555&Smith&John&&&&Dr&&2.16.840.1.113883.4.347\r" +                 
                "OBX|1|DT|10017^Date Dictated^HL70396||12 Jan 2012||||||F\r" + 
                "OBX|2|ST|1126527^Dictated by^HL70396||John Smith, MD||||||F\r" + 
                "OBX|3|NM|14007^Pressure^HL70396||0|ml|-2 - 4|N|||F\r" +
                "OBX|4|ED|14009^Picture^HL70396||^NS^GIF^Base64^"+encodedEdVal+"||||||F\r" +
                "NTE|1||This is Note line1||\r" +
                "NTE|2||This is Note line2||\r" +
                "NTE|3||This is Note line3||\r";
        
       
        
        List<ClinicalDocumentGroup> clinDocs = UhnConverter.convertOru(message1);
        Persister.persist(clinDocs);
                
    }
    
    
    
    private static void persistClinDocWithPdf(String mrn) throws Exception {
        
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "samplePdf.pdf");
        byte[] bytes = IOUtils.toByteArray(is);
        String encodedEdVal = Base64.encodeBase64String(bytes);
        encodedEdVal = encodedEdVal.replaceAll("\n", "");
        encodedEdVal = encodedEdVal.replaceAll("\r", "");
        
        //System.out.println(encodedString);
                
        String message1 = 
                "MSH|^~\\&|EPR|UHNG|||201205011031||ORU^R01^ORU_R01|31768|T|2.5\r" +
                "PID|1||"+mrn+"^^^UHN^MR^G^4265~9287170261^BL^^CANON^JHN^G||Smith^Joseph^John^Junior^Mr^MD^L~Smith^Joe^^^^^A|Blanche^^^^^^L|19310313|M|||26 RIVINGTON AVE^^Goderich^ON^N7A3Y2^CAN^H~7 WOODPLUMPTON ROAD^^Port Stanley^ON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English|||||||||||||||N\r" +                 
                "PV1|1|I|JS12^123^4^G^^Q Building^12^JS12-123-4 GIM Inpatient Unit||||38946^Blake^Donald^Thor^^^^^|38946^Blake^Donald^Thor^^^^^|38946^Blake^Donald^Thor^^^^^|GIM|||||||38946^Blake^Donald^Thor^^^^^||"+mrn+"VID5^^^2.16.840.1.113883.3.59.3:0947^VN^G\r" +                 
                "ORC|1|"+mrn+"OID5^EPR^2.16.840.1.113883.3.59.3:947|||CM\r" +                
                "OBR|1|"+mrn+"OID5^EPR^2.16.840.1.113883.3.59.3:947||50111^OR/Procedure Note5^HL70396|||20110126124300-0500|20110126125000-0500||||||||||N|||||||F|7776&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.1||||||5555&Smith&John&&&&Dr&&2.16.840.1.113883.4.347\r" +                 
                "OBX|1|DT|10017^Date Dictated^HL70396||12 Jan 2012||||||F\r" + 
                "OBX|2|ST|1126527^Dictated by^HL70396||John Smith, MD||||||F\r" + 
                "OBX|3|NM|14007^Pressure^HL70396||0|ml|-2 - 4|N|||F\r" +        
                "OBX|4|ED|14009^File^HL70396||^TEXT^PDF^Base64^"+encodedEdVal+"||||||F\r" +
                "NTE|1||This is Note line1||\r" +
                "NTE|2||This is Note line2||\r" +
                "NTE|3||This is Note line3||\r";
        
       
        
        List<ClinicalDocumentGroup> clinDocs = UhnConverter.convertOru(message1);
        Persister.persist(clinDocs);
                
    }
    
    
    
    /**
     * 
     * This one has multiple order/obeservation groups and each one belongs to the same placer group 
     * @throws Exception ...
     */    
    private static void persistClinDocMultiSection(String mrn) throws Exception {
        
        
        String message1 = 
                "MSH|^~\\&|1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1|UHNG|||20120501103100-0500|2954864636aaa|ORU^R01^ORU_R01|31768|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r" +
                "PID|1||"+mrn+"^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR^G~9287170261^BL^^^JHN^G^^^CANON&&HL70363||Smith^Joseph^John^Junior^Mr^MD^L~Smith^Joe^^^^^A|Blanche^^^^^^L|19310313|M|||26 RIVINGTON AVE^^Goderich^CANON^N7A3Y2^CAN^H~7 WOODPLUMPTON ROAD^^Port Stanley^CANON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English^HL70296|||||||||||||||N\r" +                 
                "PV1|1|I|JS12^123^4^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.100.1||||38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|GIM|||||||38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1||"+mrn+"VID6^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^VN\r" +                 
                "ORC|1|||"+mrn+"OGID6^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1\r" +                
                "OBR|1|"+mrn+"OID6A^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1||50111^OR/Procedure Note6A^1.3.6.1.4.1.12201.102.5|||20110126124300-0500|20110126125000-0500|||||||||||||||||F|7776&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1||||||5555&Smith&John&&&&Dr&&2.16.840.1.113883.4.347\r" +
                "NTE|1||Note1||\r" +
                "NTE|2||Note2||\r" +
                "NTE|3||Note3||\r" +
                "OBX|1|DT|10017^Date Dictated^1.3.6.1.4.1.12201.102.6||20120112||||||F\r" +
                "OBX|2|ST|1126527^Dictated by^1.3.6.1.4.1.12201.102.6||John Smith, MD||||||F\r" +
                "OBX|3|NM|14007^Pressure^1.3.6.1.4.1.12201.102.6||0|ml|-2 - 4|N|||F\r" +
                "NTE|1||Note1||\r" +
                "NTE|2||Note2||\r" +
                "NTE|3||Note3||\r" +
                "ORC|2|||"+mrn+"OGID6^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1\r" +
                "OBR|1|"+mrn+"OID6B^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1||50111^OR/Procedure Note6B^1.3.6.1.4.1.12201.102.5|||20110126124300-0500|20110126125000-0500|||||||||||||||||F|7776&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1||||||5555&Smith&John&&&&Dr&&2.16.840.1.113883.4.347\r" +
                "NTE|1||Note1||\r" +
                "NTE|2||Note2||\r" +
                "NTE|3||Note3||\r" +
                "OBX|1|DT|10017^Date Dictated^1.3.6.1.4.1.12201.102.6||20120113||||||F\r" + 
                "OBX|2|ST|1126527^Dictated by^1.3.6.1.4.1.12201.102.6||John Smith, MD||||||F\r" + 
                "OBX|3|NM|14007^Pressure^1.3.6.1.4.1.12201.102.6||0|ml|-3 - 4|N|||F\r" +
                "NTE|1||Note1||\r" +
                "NTE|2||Note2||\r" +
                "NTE|3||Note3||\r";

        
        Converter c = new Converter();
        ORU_R01 input = new ORU_R01();
        input.setParser(PipeParser.getInstanceWithNoValidation());
        input.parse(message1);
        List<ClinicalDocumentGroup> clinDocs = c.convertClinicalDocument(input);        
        Persister.persist(clinDocs);
                
    }   
    
    
    
    /**
     * 
     *   
     * @throws Exception ...
     */    
    private static void persistClinDocLab(String mrn) throws Exception {
        
        String message1 =
                "MSH|^~\\&|EPR|UHNG|||201205011031||ORU^R01^ORU_R01|31768|T|2.5\r" +
                "PID|1||"+mrn+"^^^UHN^MR^G^4265~9287170261^BL^^CANON^JHN^G||Smith^Joseph^John^Junior^Mr^MD^L~Smith^Joe^^^^^A|Blanche^^^^^^L|19310313|M|||26 RIVINGTON AVE^^Goderich^ON^N7A3Y2^CAN^H~7 WOODPLUMPTON ROAD^^Port Stanley^ON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English|||||||||||||||N\r" +                 
                "PV1|1|I|JS12^123^4^G^^Q Building^12^JS12-123-4 GIM Inpatient Unit||||38946^Blake^Donald^Thor^^^^^|38946^Blake^Donald^Thor^^^^^|38946^Blake^Donald^Thor^^^^^|GIM|||||||38946^Blake^Donald^Thor^^^^^||"+mrn+"VID1^^^2.16.840.1.113883.3.59.3:0947^VN^G\r" +                 
                "ORC|1|"+mrn+"OID7^EPR^2.16.840.1.113883.3.59.3:947|||CM\r" +                
                "OBR|1|"+mrn+"OID7^EPR^2.16.840.1.113883.3.59.3:947||50112^CBC1^HL70396|||20110126124300-0500|20110126125000-0500||||||||||N|||||||F|7776&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.1||||||5555&Smith&John&&&&Dr&&2.16.840.1.113883.4.347\r" +                 
                "OBX|1|DT|10017^Collection Date^HL70396||12 Jan 2012||||||F\r" + 
                "OBX|2|ST|1126527^Collected by^HL70396||John Smith, MD||||||F\r" + 
                "OBX|3|NM|14007^MCV^HL70396||100|fl|80 - 95|A|||F\r" +
                "OBX|4|NM|14008^Erc^HL70396||10|tril/L|4.5 - 6.5|A|||F\r" +
                "OBX|5|NM|14009^Lkc^HL70396||20|bil/L|4.0 - 11.0|A|||F\r" +
                "NTE|1||This is an Lkc Note line1||\r" +
                "NTE|2||This is an Lkc Note line2||\r" +
                "NTE|3||This is an Lkc Note line3||\r" +
                "OBX|6|NM|14010^LUCs^HL70396||12||<=3.0|A|||F\r" +
                "OBX|7|ST|14011^Comments^HL70396||Tomorrow he'll be as dead as Elvis!!|||A|||F\r" +
                "OBX|8|NM|14012^MCHC^HL70396||400|g/L|320 - 370|A|||F\r" +
                "NTE|1||This is a MCHC Note line1||\r" +
                "NTE|2||This is a MCHC Note line2||\r" +
                "NTE|3||This is a MCHC Note line3||\r";
        
        List<ClinicalDocumentGroup> clinDocs = UhnConverter.convertOru(message1);
        Persister.persist(clinDocs);
                
    }    
    
    
    
    
    //The following messages will create a PatientWithVisits doc in the database with data filled into most of the fields    
    private static void persistPatientWithVisitsDoc(String mrn) throws Exception {
               
       String admit = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A04^ADT_A01|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A04|201112021621||||201112021621|G^4265^L\r" +                
                "PID|||"+mrn+"^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR^G~9287170261^BL^^CANON^JHN^^^^^20111201^^||Smith^Joseph^John^Junior^Mr^MD^L~Smitho^Josepho^Johno^Junior^Mr^MD^A|Blanche^^^^^^L|19310313|M|||26 RIVINGTON AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813~7 WOODPLUMPTON ROAD^^Port Stanley^CANON^N0L2A0^CAN^M^^^^^^20120708^20120813||1 (416) 340-4800^PRN^PH^^1^416^3404800^4357^Do not call after 5~^NET^BP^test@example.com^^^^^Do not mail after 11pm||eng^English^HL70296||||||||||||||201112021610|Y\r" +
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" +                 
                "ROL||UC|PP^Primary Care Provider1^HL70443^^^|13546a1^Generica1^Physiciana1^Moe^^Dr.^MD^^UHN^L~13546a2^Generica2^Physiciana2^Moe^^Dr.^MD^^UHN^L|201111071338||||ET02^Physician^15ZEmpTyp^^^|O^Office^^^^|27a Doctor AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813~28a Doctor AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813|1 (416) 340-4800^PRN^PH^^1^416^3404800^4357^Do not call after 5~^NET^BP^test@example.com^^^^^Do not mail after 11pm\r" + 
                "ROL||UC|PP^Primary Care Provider2^HL70443^^^|13546b1^Genericb1^Physicianb1^Moe^^Dr.^MD^^UHN^L~13546b2^Genericb2^Physicianb2^Moe^^Dr.^MD^^UHN^L|201111071338||||ET02^Physician^15ZEmpTyp^^^|1^Hospital^15ZOrgTyp^^^|27b Doctor AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813~28b Doctor AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813|1 (416) 340-4801^PRN^PH^^1^416^3404801^4358^Do not call after 10~^NET^BP^test2@example.com^^^^^Do not mail after 12pm\r" +
                "NK1|1|Wph^Mom^^^^^L^^^^^^^~Wph^Mommy^^^^^L^^^^^^^|PAR^Parent^HL70063^^^|82 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^||||||||||||||||||||||||||||||||||\r" +
                "NK1|2|Wph^Brother^^^^^L^^^^^^^~Wph^BooBoo^^^^^L^^^^^^^|BRO^Brother^HL70063^^^|83 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3601^PRN^PH^^^^^^^^^||||||||||||||||||||||||||||||||||\r" + 
                "PV1||E|ES9 GEN S^424^1^G^4265^^^N^ES 9 424^ES 9 424 1^ES9 GEN S^1521 19 1^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|"+mrn+"VIDE1^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r" + 
                "PV2||S^Semi^03ZFinbed^^^|^kfkfkfjcjcjcgcgcclcl^03ZAmitRes^^^|||||||||||||||||||N||AI|Elective||||||N|||||||OTH^Self^03ZBrInBy^^^|||||||||||\r" + 
                "DG1|1||06^KFKFKFJCJCJCGCGCCLCL-A^2.16.840.1.113883.11.19436^^^|KFKFKFJCJCJCGCGCCLCL-A|201112021621|A|||||||||1||D||||\r" + 
                "DG1|2||06^KFKFKFJCJCJCGCGCCLCL-B^2.16.840.1.113883.11.19436^^^|KFKFKFJCJCJCGCGCCLCL-B|201112021621|A|||||||||1||D||||\r" +
                "PR1||||||||||||||||||||\r" + 
                "ZPV|OTH^Self|F^Standard|||N|||13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||Medical Services^General Internal Medicine^GMED|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|413||3910||^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r" + 
                "ZWA||||||||active|\r";
       
       
       String addSecondVisit = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A04^ADT_A01|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A04|201112021621||||201112021621|G^4265^L\r" +
                "PID|||"+mrn+"^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR^G~9287170261^BL^^CANON^JHN^^^^^20111201^^||Smith^Joseph^John^Junior^Mr^MD^L~Smitho^Josepho^Johno^Junior^Mr^MD^A|Blanche^^^^^^L|19310313|M|||26 RIVINGTON AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813~7 WOODPLUMPTON ROAD^^Port Stanley^CANON^N0L2A0^CAN^M^^^^^^20120708^20120813||1 (416) 340-4800^PRN^PH^^1^416^3404800^4357^Do not call after 5~^NET^BP^test@example.com^^^^^Do not mail after 11pm||eng^English^HL70296||||||||||||||201112021610|Y\r" +
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" +                 
                "ROL||UC|PP^Primary Care Provider1^HL70443^^^|13546a1^Generica1^Physiciana1^Moe^^Dr.^MD^^UHN^L~13546a2^Generica2^Physiciana2^Moe^^Dr.^MD^^UHN^L|201111071338||||ET02^Physician^15ZEmpTyp^^^|O^Office^^^^|27a Doctor AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813~28a Doctor AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813|1 (416) 340-4800^PRN^PH^^1^416^3404800^4357^Do not call after 5~^NET^BP^test@example.com^^^^^Do not mail after 11pm\r" + 
                "ROL||UC|PP^Primary Care Provider2^HL70443^^^|13546b1^Genericb1^Physicianb1^Moe^^Dr.^MD^^UHN^L~13546b2^Genericb2^Physicianb2^Moe^^Dr.^MD^^UHN^L|201111071338||||ET02^Physician^15ZEmpTyp^^^|1^Hospital^15ZOrgTyp^^^|27b Doctor AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813~28b Doctor AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813|1 (416) 340-4801^PRN^PH^^1^416^3404801^4358^Do not call after 10~^NET^BP^test2@example.com^^^^^Do not mail after 12pm\r" +
                "NK1|1|Wph^Mom^^^^^L^^^^^^^~Wph^Mommy^^^^^L^^^^^^^|PAR^Parent^HL70063^^^|82 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^||||||||||||||||||||||||||||||||||\r" +
                "NK1|2|Wph^Brother^^^^^L^^^^^^^~Wph^BooBoo^^^^^L^^^^^^^|BRO^Brother^HL70063^^^|83 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3601^PRN^PH^^^^^^^^^||||||||||||||||||||||||||||||||||\r" +
                "PV1||E|ES9 GEN S^424^2^G^4265^^^N^ES 9 424^ES 9 424 2^ES9 GEN S^1521 19 1^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|"+mrn+"VIDE2^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r" + 
                "PV2||S^Semi^03ZFinbed^^^|^kfkfkfjcjcjcgcgcclcl^03ZAmitRes^^^|||||||||||||||||||N||AI|Elective||||||N|||||||OTH^Self^03ZBrInBy^^^|||||||||||\r" + 
                "DG1|1||06^KFKFKFJCJCJCGCGCCLCL^2.16.840.1.113883.11.19436^^^|KFKFKFJCJCJCGCGCCLCL|201112021621|A|||||||||1||D||||\r" + 
                "PR1||||||||||||||||||||\r" + 
                "ZPV|OTH^Self|F^Standard|||N|||13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||Medical Services^General Internal Medicine^GMED|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|413||3910||^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r" + 
                "ZWA||||||||active|\r";
       
       
       String addThirdVisit = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A04^ADT_A01|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A04|201112021621||||201112021621|G^4265^L\r" +
                "PID|||"+mrn+"^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR^G~9287170261^BL^^CANON^JHN^^^^^20111201^^||Smith^Joseph^John^Junior^Mr^MD^L~Smitho^Josepho^Johno^Junior^Mr^MD^A|Blanche^^^^^^L|19310313|M|||26 RIVINGTON AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813~7 WOODPLUMPTON ROAD^^Port Stanley^CANON^N0L2A0^CAN^M^^^^^^20120708^20120813||1 (416) 340-4800^PRN^PH^^1^416^3404800^4357^Do not call after 5~^NET^BP^test@example.com^^^^^Do not mail after 11pm||eng^English^HL70296||||||||||||||201112021610|Y\r" +
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" +                 
                "ROL||UC|PP^Primary Care Provider1^HL70443^^^|13546a1^Generica1^Physiciana1^Moe^^Dr.^MD^^UHN^L~13546a2^Generica2^Physiciana2^Moe^^Dr.^MD^^UHN^L|201111071338||||ET02^Physician^15ZEmpTyp^^^|O^Office^^^^|27a Doctor AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813~28a Doctor AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813|1 (416) 340-4800^PRN^PH^^1^416^3404800^4357^Do not call after 5~^NET^BP^test@example.com^^^^^Do not mail after 11pm\r" + 
                "ROL||UC|PP^Primary Care Provider2^HL70443^^^|13546b1^Genericb1^Physicianb1^Moe^^Dr.^MD^^UHN^L~13546b2^Genericb2^Physicianb2^Moe^^Dr.^MD^^UHN^L|201111071338||||ET02^Physician^15ZEmpTyp^^^|1^Hospital^15ZOrgTyp^^^|27b Doctor AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813~28b Doctor AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813|1 (416) 340-4801^PRN^PH^^1^416^3404801^4358^Do not call after 10~^NET^BP^test2@example.com^^^^^Do not mail after 12pm\r" +
                "NK1|1|Wph^Mom^^^^^L^^^^^^^~Wph^Mommy^^^^^L^^^^^^^|PAR^Parent^HL70063^^^|82 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^||||||||||||||||||||||||||||||||||\r" +
                "NK1|2|Wph^Brother^^^^^L^^^^^^^~Wph^BooBoo^^^^^L^^^^^^^|BRO^Brother^HL70063^^^|83 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3601^PRN^PH^^^^^^^^^||||||||||||||||||||||||||||||||||\r" +                
                "PV1||E|ES9 GEN S^424^3^G^4265^^^N^ES 9 424^ES 9 424 3^ES9 GEN S^1521 19 1^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|"+mrn+"VIDE3^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r" + 
                "PV2||S^Semi^03ZFinbed^^^|^kfkfkfjcjcjcgcgcclcl^03ZAmitRes^^^|||||||||||||||||||N||AI|Elective||||||N|||||||OTH^Self^03ZBrInBy^^^|||||||||||\r" + 
                "DG1|1||06^KFKFKFJCJCJCGCGCCLCL^2.16.840.1.113883.11.19436^^^|KFKFKFJCJCJCGCGCCLCL|201112021621|A|||||||||1||D||||\r" + 
                "PR1||||||||||||||||||||\r" + 
                "ZPV|OTH^Self|F^Standard|||N|||13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||Medical Services^General Internal Medicine^GMED|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|413||3910||^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r" + 
                "ZWA||||||||active|\r";
       
       
       String dischargeThirdVisit = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A03^ADT_A03|123710|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A03|201112021621||||201112021621|G^4265^L\r" +
                "PID|||"+mrn+"^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR^G~9287170261^BL^^CANON^JHN^^^^^20111201^^||Smith^Joseph^John^Junior^Mr^MD^L~Smitho^Josepho^Johno^Junior^Mr^MD^A|Blanche^^^^^^L|19310313|M|||26 RIVINGTON AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813~7 WOODPLUMPTON ROAD^^Port Stanley^CANON^N0L2A0^CAN^M^^^^^^20120708^20120813||1 (416) 340-4800^PRN^PH^^1^416^3404800^4357^Do not call after 5~^NET^BP^test@example.com^^^^^Do not mail after 11pm||eng^English^HL70296||||||||||||||201112021610|Y\r" +
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" +                 
                "ROL||UC|PP^Primary Care Provider1^HL70443^^^|13546a1^Generica1^Physiciana1^Moe^^Dr.^MD^^UHN^L~13546a2^Generica2^Physiciana2^Moe^^Dr.^MD^^UHN^L|201111071338||||ET02^Physician^15ZEmpTyp^^^|O^Office^^^^|27a Doctor AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813~28a Doctor AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813|1 (416) 340-4800^PRN^PH^^1^416^3404800^4357^Do not call after 5~^NET^BP^test@example.com^^^^^Do not mail after 11pm\r" + 
                "ROL||UC|PP^Primary Care Provider2^HL70443^^^|13546b1^Genericb1^Physicianb1^Moe^^Dr.^MD^^UHN^L~13546b2^Genericb2^Physicianb2^Moe^^Dr.^MD^^UHN^L|201111071338||||ET02^Physician^15ZEmpTyp^^^|1^Hospital^15ZOrgTyp^^^|27b Doctor AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813~28b Doctor AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813|1 (416) 340-4801^PRN^PH^^1^416^3404801^4358^Do not call after 10~^NET^BP^test2@example.com^^^^^Do not mail after 12pm\r" +
                "NK1|1|Wph^Mom^^^^^L^^^^^^^~Wph^Mommy^^^^^L^^^^^^^|PAR^Parent^HL70063^^^|82 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^||||||||||||||||||||||||||||||||||\r" +
                "NK1|2|Wph^Brother^^^^^L^^^^^^^~Wph^BooBoo^^^^^L^^^^^^^|BRO^Brother^HL70063^^^|83 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3601^PRN^PH^^^^^^^^^||||||||||||||||||||||||||||||||||\r" +
                "PV1||E|ES9 GEN S^424^3^G^4265^^^N^ES 9 424^ES 9 424 3^ES9 GEN S^1521 19 1^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|"+mrn+"VIDE3^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|201112021821||||||V|\r" +
                "PV2||S^Semi^03ZFinbed^^^|^kfkfkfjcjcjcgcgcclcl^03ZAmitRes^^^|||||||||||||||||||N||AI|Elective||||||N|||||||OTH^Self^03ZBrInBy^^^|||||||||||\r" +                
                "ZPV|OTH^Self|F^Standard|||N|||13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||Medical Services^General Internal Medicine^GMED|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|413||3910||^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r";
       
       
       
       String addAllergies = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201201111123||ADT^A60^ADT_A60|124423|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A60|201201111123||||201201111123|G^4265^L\r" +
                "PID|||"+mrn+"^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR^G~9287170261^BL^^CANON^JHN^^^^^20111201^^||Smith^Joseph^John^Junior^Mr^MD^L~Smitho^Josepho^Johno^Junior^Mr^MD^A|Blanche^^^^^^L|19310313|M|||26 RIVINGTON AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813~7 WOODPLUMPTON ROAD^^Port Stanley^CANON^N0L2A0^CAN^M^^^^^^20120708^20120813||1 (416) 340-4800^PRN^PH^^1^416^3404800^4357^Do not call after 5~^NET^BP^test@example.com^^^^^Do not mail after 11pm||eng^English^HL70296||||||||||||||201112021610|Y\r" +
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" +                 
                "ROL||UC|PP^Primary Care Provider1^HL70443^^^|13546a1^Generica1^Physiciana1^Moe^^Dr.^MD^^UHN^L~13546a2^Generica2^Physiciana2^Moe^^Dr.^MD^^UHN^L|201111071338||||ET02^Physician^15ZEmpTyp^^^|O^Office^^^^|27a Doctor AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813~28a Doctor AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813|1 (416) 340-4800^PRN^PH^^1^416^3404800^4357^Do not call after 5~^NET^BP^test@example.com^^^^^Do not mail after 11pm\r" + 
                "ROL||UC|PP^Primary Care Provider2^HL70443^^^|13546b1^Genericb1^Physicianb1^Moe^^Dr.^MD^^UHN^L~13546b2^Genericb2^Physicianb2^Moe^^Dr.^MD^^UHN^L|201111071338||||ET02^Physician^15ZEmpTyp^^^|1^Hospital^15ZOrgTyp^^^|27b Doctor AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813~28b Doctor AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813|1 (416) 340-4801^PRN^PH^^1^416^3404801^4358^Do not call after 10~^NET^BP^test2@example.com^^^^^Do not mail after 12pm\r" +
                "NK1|1|Wph^Mom^^^^^L^^^^^^^~Wph^Mommy^^^^^L^^^^^^^|PAR^Parent^HL70063^^^|82 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^||||||||||||||||||||||||||||||||||\r" +
                "NK1|2|Wph^Brother^^^^^L^^^^^^^~Wph^BooBoo^^^^^L^^^^^^^|BRO^Brother^HL70063^^^|83 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3601^PRN^PH^^^^^^^^^||||||||||||||||||||||||||||||||||\r" +
                "PV1||E|ES9 GEN S^424^2^G^4265^^^N^ES 9 424^ES 9 424 2^ES9 GEN S^1521 19 1^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|"+mrn+"VIDE2^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r" + 
                "PV2||S^Semi^03ZFinbed^^^|^kfkfkfjcjcjcgcgcclcl^03ZAmitRes^^^|||||||||||||||||||N||AI|Elective||||||N|||||||OTH^Self^03ZBrInBy^^^|||||||||||\r" + 
                "IAM||DA^Drug Allergy^03ZPAR^^^|K03733-01-001^Contrast Medium/Dye^1.3.6.1.4.1.12201.2.4^^^|MI^Mild|GI upset~Stink|A^Add/Insert|||AL^Allergy^03ZPAR^^^||20120106|sudden reaction|201201060958||SEL^Self^03ZPAR^^^||||\r" +
                "IAM||DA^Drug Allergy^03ZPAR^^^|P00036^Tetracyclines^1.3.6.1.4.1.12201.2.4^^^|MI^Mild|swelling~rash|A^Add/Insert|||AL^Allergy^03ZPAR^^^||20120107|sudden reaction|20120106||SEL^Self^03ZPAR^^^||||\r" +
                "IAM||FA^Food Allergy^03ZPAR^^^|FreeText^No known food allergy/adverse reaction^1.3.6.1.4.1.12201.2.4^^^|MI^Mild|None|A^Add/Insert|||AL^Allergy^03ZPAR^^^||20120108|sudden reaction|201201||SEL^Self^03ZPAR^^^||||\r" +
                "IAM||MA^Miscellaneous Allergy^03ZPAR^^^|FreeText^No known latex/other allergy/adverse reaction^1.3.6.1.4.1.12201.2.4^^^|MI^Mild|None|A^Add/Insert|||AL^Allergy^03ZPAR^^^||20120109|sudden reaction|20120106095800||SEL^Self^03ZPAR^^^||||\r";
           
         
        
        String convert = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112070000||ADT^A06^ADT_A06|123710|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A06|201112151111||||201112151111|G^4265^L\r" +
                "PID|||"+mrn+"^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR^G~9287170261^BL^^CANON^JHN^^^^^20111201^^||Smith^Joseph^John^Junior^Mr^MD^L~Smitho^Josepho^Johno^Junior^Mr^MD^A|Blanche^^^^^^L|19310313|M|||26 RIVINGTON AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813~7 WOODPLUMPTON ROAD^^Port Stanley^CANON^N0L2A0^CAN^M^^^^^^20120708^20120813||1 (416) 340-4800^PRN^PH^^1^416^3404800^4357^Do not call after 5~^NET^BP^test@example.com^^^^^Do not mail after 11pm||eng^English^HL70296||||||||||||||201112021610|Y\r" +
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" +                 
                "ROL||UC|PP^Primary Care Provider1^HL70443^^^|13546a1^Generica1^Physiciana1^Moe^^Dr.^MD^^UHN^L~13546a2^Generica2^Physiciana2^Moe^^Dr.^MD^^UHN^L|201111071338||||ET02^Physician^15ZEmpTyp^^^|O^Office^^^^|27a Doctor AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813~28a Doctor AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813|1 (416) 340-4800^PRN^PH^^1^416^3404800^4357^Do not call after 5~^NET^BP^test@example.com^^^^^Do not mail after 11pm\r" + 
                "ROL||UC|PP^Primary Care Provider2^HL70443^^^|13546b1^Genericb1^Physicianb1^Moe^^Dr.^MD^^UHN^L~13546b2^Genericb2^Physicianb2^Moe^^Dr.^MD^^UHN^L|201111071338||||ET02^Physician^15ZEmpTyp^^^|1^Hospital^15ZOrgTyp^^^|27b Doctor AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813~28b Doctor AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813|1 (416) 340-4801^PRN^PH^^1^416^3404801^4358^Do not call after 10~^NET^BP^test2@example.com^^^^^Do not mail after 12pm\r" +
                "NK1|1|Wph^Mom^^^^^L^^^^^^^~Wph^Mommy^^^^^L^^^^^^^|PAR^Parent^HL70063^^^|82 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^||||||||||||||||||||||||||||||||||\r" +
                "NK1|2|Wph^Brother^^^^^L^^^^^^^~Wph^BooBoo^^^^^L^^^^^^^|BRO^Brother^HL70063^^^|83 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3601^PRN^PH^^^^^^^^^||||||||||||||||||||||||||||||||||\r" +
                "MRG|"+mrn+"^^^^MR^^^^^^^~HN2827^^^^PI^^^^^^^||||"+mrn+"VIDE1^^^EPR^VN^G^4265^^^^^||\r" +
                "PV1||I|ES9 GEN S^424^2^G^4265^^^N^ES 9 424^ES 9 424 2^ES9 GEN S^1521 19 1^|C||^^^G^4265^^^^^^^^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||A|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|"+mrn+"VIDI^^^UHN^VN^G^4265^^^^^|||||||||||||||||1|||G|||||201112021621|||||||V|\r" + 
                "PV2||S^Semi^03ZFinbed^^^|^kfkfkfjcjcjcgcgcclcl^03ZAmitRes^^^|||||||||||||||||||N||AI|Elective||||||N|||||||OTH^Self^03ZBrInBy^^^|||||||||||\r" + 
                "DG1|1||06^KFKFKFJCJCJCGCGCCLCL^2.16.840.1.113883.11.19436^^^|KFKFKFJCJCJCGCGCCLCL|201112021621|A|||||||||1||D||||\r" + 
                "PR1||||||||||||||||||||\r" + 
                "ZPV|OTH^Self|F^Standard|||N|||13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||Medical Services^General Internal Medicine^GMED|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|413||3910||^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r" + 
                "ZWA||||||||active|\r";     
        
        
         String transfer = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201201111123||ADT^A02^ADT_A02|124423|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A02|201201111123||||201201111123|G^4265^L\r" +
                "PID|||"+mrn+"^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR^G~9287170261^BL^^CANON^JHN^^^^^20111201^^||Smith^Joseph^John^Junior^Mr^MD^L~Smitho^Josepho^Johno^Junior^Mr^MD^A|Blanche^^^^^^L|19310313|M|||26 RIVINGTON AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813~7 WOODPLUMPTON ROAD^^Port Stanley^CANON^N0L2A0^CAN^M^^^^^^20120708^20120813||1 (416) 340-4800^PRN^PH^^1^416^3404800^4357^Do not call after 5~^NET^BP^test@example.com^^^^^Do not mail after 11pm||eng^English^HL70296||||||||||||||201112021610|Y\r" +
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" +                 
                "ROL||UC|PP^Primary Care Provider1^HL70443^^^|13546a1^Generica1^Physiciana1^Moe^^Dr.^MD^^UHN^L~13546a2^Generica2^Physiciana2^Moe^^Dr.^MD^^UHN^L|201111071338||||ET02^Physician^15ZEmpTyp^^^|O^Office^^^^|27a Doctor AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813~28a Doctor AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813|1 (416) 340-4800^PRN^PH^^1^416^3404800^4357^Do not call after 5~^NET^BP^test@example.com^^^^^Do not mail after 11pm\r" + 
                "ROL||UC|PP^Primary Care Provider2^HL70443^^^|13546b1^Genericb1^Physicianb1^Moe^^Dr.^MD^^UHN^L~13546b2^Genericb2^Physicianb2^Moe^^Dr.^MD^^UHN^L|201111071338||||ET02^Physician^15ZEmpTyp^^^|1^Hospital^15ZOrgTyp^^^|27b Doctor AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813~28b Doctor AVE^^Goderich^CANON^N7A3Y2^CAN^H^^^^^^20120708^20120813|1 (416) 340-4801^PRN^PH^^1^416^3404801^4358^Do not call after 10~^NET^BP^test2@example.com^^^^^Do not mail after 12pm\r" +
                "NK1|1|Wph^Mom^^^^^L^^^^^^^~Wph^Mommy^^^^^L^^^^^^^|PAR^Parent^HL70063^^^|82 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^||||||||||||||||||||||||||||||||||\r" +
                "NK1|2|Wph^Brother^^^^^L^^^^^^^~Wph^BooBoo^^^^^L^^^^^^^|BRO^Brother^HL70063^^^|83 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3601^PRN^PH^^^^^^^^^||||||||||||||||||||||||||||||||||\r" +
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|R||ES9 GEN S^424^1^G^4265^^^N^ES 9 424^ES 9 424 1^ES9 GEN S^1521 19 1^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|"+mrn+"VIDI^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r" +
                "PV2||S^Semi^03ZFinbed^^^|^kfkfkfjcjcjcgcgcclcl^03ZAmitRes^^^|||||||||||||||||||N||AI|Elective||||||N|||||||OTH^Self^03ZBrInBy^^^|||||||||||\r" + 
                "DG1|1||06^KFKFKFJCJCJCGCGCCLCL^2.16.840.1.113883.11.19436^^^|KFKFKFJCJCJCGCGCCLCL|201112021621|A|||||||||1||D||||\r" + 
                "PR1||||||||||||||||||||\r" + 
                "ZPV|OTH^Self|F^Standard|||N|||13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||Medical Services^General Internal Medicine^GMED|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|413||3910||^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r" + 
                "ZWA||||||||active|\r";
        
        
        
        String priorPatientMsg = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A01^ADT_A01|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A01|201112021621||||201112021621|G^4265^L\r" +
                "PID|||"+mrn+"P^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR^G~9287170261^BL^^CANON^JHN^^^^^20111201^^||Smith^Joseph^John^Junior^Mr^MD^L~Smith^Joe^^^^^A|Blanche^^^^^^L|19310313|M|||26 RIVINGTON AVE^^Goderich^CANON^N7A3Y2^CAN^H~7 WOODPLUMPTON ROAD^^Port Stanley^CANON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English^HL70296|||||||||||||||N\r" +
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|"+mrn+"VIDP^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r";
        
        
        String priorPatientOrderMsg =
                "MSH|^~\\&|EPR|UHNG|||201205011031||ORU^R01^ORU_R01|31768|T|2.5\r" +
                "PID|||"+mrn+"P^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR^G~9287170261^BL^^CANON^JHN^^^^^20111201^^||Smith^Joseph^John^Junior^Mr^MD^L~Smith^Joe^^^^^A|Blanche^^^^^^L|19310313|M|||26 RIVINGTON AVE^^Goderich^CANON^N7A3Y2^CAN^H~7 WOODPLUMPTON ROAD^^Port Stanley^CANON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English^HL70296|||||||||||||||N\r" +
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|"+mrn+"VIDP^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r" +
                "ORC|1|"+mrn+"OIDP^EPR^2.16.840.1.113883.3.59.3:947|||CM\r" +                
                "OBR|1|"+mrn+"OIDP^EPR^2.16.840.1.113883.3.59.3:947||50112^CBCP^HL70396|||20110126124300-0500|20110126125000-0500||||||||||N|||||||F|7776&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.1||||||5555&Smith&John&&&&Dr&&2.16.840.1.113883.4.347\r" +                 
                "OBX|1|DT|10017^Collection Date^HL70396||12 Jan 2012||||||F\r" + 
                "OBX|2|ST|1126527^Collected by^HL70396||John Smith, MD||||||F\r";
        
        
        String priorPatientMedMsg = 
                "MSH|^~\\&|1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1|UHNG|||20120501103100-0500|2954864636aaa|RDE^O11^RDE_O11|31768|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r" +
                "PID|||"+mrn+"P^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR^G~9287170261^BL^^CANON^JHN^^^^^20111201^^||Smith^Joseph^John^Junior^Mr^MD^L~Smith^Joe^^^^^A|Blanche^^^^^^L|19310313|M|||26 RIVINGTON AVE^^Goderich^CANON^N7A3Y2^CAN^H~7 WOODPLUMPTON ROAD^^Port Stanley^CANON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English^HL70296|||||||||||||||N\r" +
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|"+mrn+"VIDP^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r" +
                "ORC|OK|"+mrn+"MEDP^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1||"+mrn+"MEDP^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1|\r" +
                "RXE|3^Q24H^INDEF^201204181000-0400^201205221000-0400|QUE25T^quetiapine tab 25 mgP^1.3.6.1.4.1.12201.102.4|1|2|MG^milligram|INJ^Injection|^Hold PCA if patient confused or loses IV access~^Hold PCA if patient loses IV access\r" +                
                "NTE|1||Note1||\r" +
                "NTE|2||Note2||\r" +
                "NTE|3||Note3||\r" +
                "RXR|^Intravenous||\r" +
                "RXR|INJ^Injection||||||\r" +
                "RXC|B|AMI150I^amiodarone inj 50 mg per 1mL^1.3.6.1.4.1.12201.102.3|50|MG^mg|\r" +
                "RXC|A|AMI160I^amiodarone inj 60 mg per 1mL^1.3.6.1.4.1.12201.102.3|60|MG^mg|\r";
        
                
        
        String mrgMsg = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201202291235||ADT^A40^ADT_A39|126421|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A40|201202291235||||201202291235|G^4265^L\r" +
                "PID|1||"+mrn+"^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR^G~9287170261^BL^^CANON^JHN^^^^^20111201^^||Smith^Joseph^John^Junior^Mr^MD^L~Smith^Joe^^^^^A|Blanche^^^^^^L|19310313|M|||26 RIVINGTON AVE^^Goderich^CANON^N7A3Y2^CAN^H~7 WOODPLUMPTON ROAD^^Port Stanley^CANON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English^HL70296|||||||||||||||N\r" +
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13893^Generic^Moe^PhysicianThree^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "MRG|"+mrn+"P^^^UHN^MR^^^^^^^~~HN2827^^^UHN^PI^^^^^^^||||^^^^^^^^^^^||\r";
        
        
        String mrgMsgVisit = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201202291235||ADT^A42^ADT_A39|126421|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A42|201202291235||||201202291235|G^4265^L\r" +
                "PID|1||"+mrn+"^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR^G~9287170261^BL^^CANON^JHN^^^^^20111201^^||Smith^Joseph^John^Junior^Mr^MD^L~Smith^Joe^^^^^A|Blanche^^^^^^L|19310313|M|||26 RIVINGTON AVE^^Goderich^CANON^N7A3Y2^CAN^H~7 WOODPLUMPTON ROAD^^Port Stanley^CANON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English^HL70296|||||||||||||||N\r" +
                "MRG|"+mrn+"^^^UHN^MR^^^^^^^~~HN2827^^^UHN^PI^^^^^^^||||"+mrn+"VIDE3^^^UHN^VN^G^4265^^^^^||\r" +
                "PV1||E|ES9 GEN S^424^3^G^4265^^^N^ES 9 424^ES 9 424 3^ES9 GEN S^1521 19 1^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|"+mrn+"VIDE3^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r";

                
        
        
       
       Persister.persist(UhnConverter.convertAdtOrFail(admit));
       Persister.persist(UhnConverter.convertAdtOrFail(addSecondVisit));
       Persister.persist(UhnConverter.convertAdtOrFail(addThirdVisit));
       Persister.persist(UhnConverter.convertAdtOrFail(dischargeThirdVisit));
       Persister.persist(UhnConverter.convertAdtOrFail(addAllergies));
       Persister.persist(UhnConverter.convertAdtOrFail(transfer));
       Persister.persist(UhnConverter.convertAdtOrFail(convert));
       Persister.persist(UhnConverter.convertAdtOrFail(priorPatientMsg));       
       Persister.persist(UhnConverter.convertOru(priorPatientOrderMsg));
       Converter c = new Converter();
       RDE_O11 input = new RDE_O11();
       input.setParser(PipeParser.getInstanceWithNoValidation());
       input.parse(priorPatientMedMsg);                
       Persister.persistMedicationOrders(c.convertMedicationOrder(input));
       Persister.persist(UhnConverter.convertAdtOrFail(mrgMsg));
        
                
    }
    
    
    
    
    
    //The following messages will create a deactivated patient with a visit, results, and meds
    private static void persistMedicationOrderWithAdminsDoc(String mrn) throws Exception {
        
        
        String addMedOrder = 
                "MSH|^~\\&|1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1|UHNG|||20120501103100-0500|2954864636aaa|RDE^O11^RDE_O11|31768|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r" +
                "PID|1||"+mrn+"^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR^G~9287170261^BL^^^JHN^G^^^CANON&&HL70363||Smith^Joseph^John^Junior^Mr^MD^L~Smith^Joe^^^^^A|Blanche^^^^^^L|19310313000000-0500|M|||26 RIVINGTON AVE^^Goderich^CANON^N7A3Y2^CAN^H~7 WOODPLUMPTON ROAD^^Port Stanley^CANON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English^HL70296|||||||||||||||N\r" +                 
                "PV1|1|I|JS12^123^4^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.100.1||||38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|GIM|||||||38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1||"+mrn+"VID7^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^VN\r" +                 
                "ORC|OK|"+mrn+"MED1^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1||"+mrn+"MED^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1|\r" +
                "RXE|3^Q24H^INDEF^201204181000-0400^201205221000-0400|QUE25T^quetiapine tab 25 mg^1.3.6.1.4.1.12201.102.4|1|2|MG^milligram|INJ^Injection|^Hold PCA if patient confused or loses IV access~^Hold PCA if patient loses IV access\r" +                
                "NTE|1||Note1||\r" +
                "NTE|2||Note2||\r" +
                "NTE|3||Note3||\r" +
                "RXR|^Intravenous||\r" +
                "RXR|INJ^Injection||||||\r" +
                "RXC|B|AMI150I^amiodarone inj 50 mg per 1mL^1.3.6.1.4.1.12201.102.3|50|MG^mg|\r" +
                "RXC|A|AMI160I^amiodarone inj 60 mg per 1mL^1.3.6.1.4.1.12201.102.3|60|MG^mg|\r";
        
        
                
        String addAdminsToMedOrder = 
                "MSH|^~\\&|1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1|UHNG|||20120501103100-0500|2954864636aaa|RAS^O17^RAS_O17|31768|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r" +
                "PID|1||"+mrn+"^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR^G~9287170261^BL^^^JHN^G^^^CANON&&HL70363||Smith^Joseph^John^Junior^Mr^MD^L~Smith^Joe^^^^^A|Blanche^^^^^^L|19310313000000-0500|M|||26 RIVINGTON AVE^^Goderich^CANON^N7A3Y2^CAN^H~7 WOODPLUMPTON ROAD^^Port Stanley^CANON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English^HL70296|||||||||||||||N\r" +                 
                "PV1|1|I|JS12^123^4^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.100.1||||38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|GIM|||||||38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1||"+mrn+"VID7^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^VN\r" +                 
                "ORC|OK|"+mrn+"MED1^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1||"+mrn+"MED^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1|\r" +
                "RXA||1|201204250101-0400|201204250201-0400|NITRO^Nitroglycerine^1.3.6.1.4.1.12201.102.2|100|mg||D5WGB^100 mg bottle~^premixed solution|||100mg/min\r" +
                "RXA||2|201204250201-0400|201204250301-0400|NITRO^Nitroglycerine^1.3.6.1.4.1.12201.102.2|100|mg||D5WGB^100 mg bottle~^premixed solution|||100mg/min\r" +
                "RXR|INJ^Injection||||||\r" +
                "RXA||1|201204250101-0400|201204250201-0400|^fish oil|200|mg||D5WGB^200 mg bottle~^premixed solution|||200mg/min\r" +
                "RXA||2|201204250201-0400|201204250301-0400|^fish oil|200|mg||D5WGB^200 mg bottle~^premixed solution|||200mg/min\r" +
                "RXR|^Intravenous||||||\r";
        
        
        
       
        Converter c = new Converter();
        RDE_O11 input = new RDE_O11();
        input.setParser(PipeParser.getInstanceWithNoValidation());
        input.parse(addMedOrder);
        List<MedicationOrder> medOrders = c.convertMedicationOrder(input);        
        Persister.persistMedicationOrders(medOrders);        
        
        RAS_O17 input2 = new RAS_O17();
        input2.setParser(PipeParser.getInstanceWithNoValidation());
        input2.parse(addAdminsToMedOrder);
        List<MedicationOrderWithAdmins> medOrdersWadmins = c.convertMedicationAdmin(input2);        
        Persister.persistMedicationAdmins(medOrdersWadmins);        
        
    }
    
    
    
    private static void persistDeactivatedPatientWithDocs(String mrn) throws Exception{
        
        String addPatientMsg = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A01^ADT_A01|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A01|201112021621||||201112021621|G^4265^L\r" +
                "PID|||"+mrn+"D^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR^G~9287170268^BL^^CANON^JHN^^^^^20111201^^||Smith^Dead^Dead^Junior^Mr^MD^L~Smith^Dead^^^^^A|Blanche^^^^^^L|19400313|M|||30 RIVINGTON AVE^^Goderich^CANON^N7A3Y2^CAN^H~70 WOODPLUMPTON ROAD^^Port Stanley^CANON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English^HL70296|||||||||||||||N\r" +
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|"+mrn+"VIDD^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r";
                        
        String addPatientOrderMsg =
                "MSH|^~\\&|EPR|UHNG|||201205011031||ORU^R01^ORU_R01|31768|T|2.5\r" +
                "PID|||"+mrn+"D^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR^G~9287170268^BL^^CANON^JHN^^^^^20111201^^||Smith^Dead^Dead^Junior^Mr^MD^L~Smith^Dead^^^^^A|Blanche^^^^^^L|19400313|M|||30 RIVINGTON AVE^^Goderich^CANON^N7A3Y2^CAN^H~70 WOODPLUMPTON ROAD^^Port Stanley^CANON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English^HL70296|||||||||||||||N\r" +
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|"+mrn+"VIDD^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r" +
                "ORC|1|"+mrn+"OIDD^EPR^2.16.840.1.113883.3.59.3:947|||CM\r" +                
                "OBR|1|"+mrn+"OIDD^EPR^2.16.840.1.113883.3.59.3:947||50112^CBCD^HL70396|||20110126124300-0500|20110126125000-0500||||||||||N|||||||F|7776&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.1||||||5555&Smith&John&&&&Dr&&2.16.840.1.113883.4.347\r" +                 
                "OBX|1|DT|10017^Collection Date^HL70396||12 Jan 2012||||||F\r" + 
                "OBX|2|ST|1126527^Collected by^HL70396||John Smith, MD||||||F\r";
        
        
        String addPatientMedMsg = 
                "MSH|^~\\&|1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1|UHNG|||20120501103100-0500|2954864636aaa|RDE^O11^RDE_O11|31768|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r" +
                "PID|||"+mrn+"D^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR^G~9287170268^BL^^CANON^JHN^^^^^20111201^^||Smith^Dead^Dead^Junior^Mr^MD^L~Smith^Dead^^^^^A|Blanche^^^^^^L|19400313|M|||30 RIVINGTON AVE^^Goderich^CANON^N7A3Y2^CAN^H~70 WOODPLUMPTON ROAD^^Port Stanley^CANON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English^HL70296|||||||||||||||N\r" +
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|"+mrn+"VIDD^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r" +
                "ORC|OK|"+mrn+"MEDD^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1||"+mrn+"MEDD^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1|\r" +
                "RXE|3^Q24H^INDEF^201204181000-0400^201205221000-0400|QUE25T^quetiapine tab 25 mgD^1.3.6.1.4.1.12201.102.4|1|2|MG^milligram|INJ^Injection|^Hold PCA if patient confused or loses IV access~^Hold PCA if patient loses IV access\r" +                
                "NTE|1||Note1||\r" +
                "NTE|2||Note2||\r" +
                "NTE|3||Note3||\r" +
                "RXR|^Intravenous||\r" +
                "RXR|INJ^Injection||||||\r" +
                "RXC|B|AMI150I^amiodarone inj 50 mg per 1mL^1.3.6.1.4.1.12201.102.3|50|MG^mg|\r" +
                "RXC|A|AMI160I^amiodarone inj 60 mg per 1mL^1.3.6.1.4.1.12201.102.3|60|MG^mg|\r";
        
        
        String deactivatePatientMsg = 
                "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201201111142||ADT^A31^ADT_A05|124434|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A31|201201111142||||201201111142|G^4265^L\r" + 
                "PID|||"+mrn+"D^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR^G~9287170268^BL^^CANON^JHN^^^^^20111201^^||Smith^Dead^Dead^Junior^Mr^MD^L~Smith^Dead^^^^^A|Blanche^^^^^^L|19400313|M|||30 RIVINGTON AVE^^Goderich^CANON^N7A3Y2^CAN^H~70 WOODPLUMPTON ROAD^^Port Stanley^CANON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English^HL70296|||||||||||||||N\r" + 
                "ZPD|Y\r"; 
        
       
       Persister.persist(UhnConverter.convertAdtOrFail(addPatientMsg));       
       Persister.persist(UhnConverter.convertOru(addPatientOrderMsg));
       Converter c = new Converter();
       RDE_O11 input = new RDE_O11();
       input.setParser(PipeParser.getInstanceWithNoValidation());
       input.parse(addPatientMedMsg);                
       Persister.persistMedicationOrders(c.convertMedicationOrder(input));
       Persister.persist(UhnConverter.convertAdtOrFail(deactivatePatientMsg));
        
        
    }
    
    
    
    private static void persistReoccuringPatientWithDocs(String mrn) throws Exception{
        
        
       String registerReoccurringOutPatient = 
                "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112151022||ADT^A04^ADT_A01|123766|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A04|201112151022||||201112151022|G^4265^L\r" +
                "PID|||"+mrn+"R^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR^G~9287170269^BL^^CANON^JHN^^^^^20111201^^||Smith^Recuro^Recuro^Junior^Mr^MD^L~Smith^Dead^^^^^A|Blanche^^^^^^L|19400313|M|||30 RIVINGTON AVE^^Goderich^CANON^N7A3Y2^CAN^H~70 WOODPLUMPTON ROAD^^Port Stanley^CANON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English^HL70296|||||||||||||||N\r" +
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "ROL||UC|PP^Primary Care Provider^15ZRole^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^^^^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|O^Office^^^^|^^^ON^^Can^^^^^^^^|(416) 340-3391^WPN^PH^^^^^^^^^\r" + 
                "ROL||UC|PP^Primary Care Provider^15ZRole^^^|13546b^Genericb^Physicianb^Moeb^^Dr.^MD^^UHN^L^^^EI^G^2.16.840.1.113883.3.59.3:947^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|1^Hospital^15ZOrgTyp^^^|^^^ON^^Can^^^^^^^^|(416) 340-3388^WPN^PH^^^^^^^^^\r" +
                "NK1|1|Wph^Mom^^^^^L^^^^^^^|PAR^Parent^03ZRelshp^^^|82 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^~(416)123-1234^PRN^CP^^^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^|N^Next-of-Kin^03ZConRol^^^||||||||||||||||||||||||||||||||\r" +
                "NK1|2|Wph^Brother^^^^^L^^^^^^^|BRO^Brother^03ZRelshp^^^|83 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3601^PRN^PH^^^^^^^^^|(416)525-2525^PRN^PH^^^^^^^^^|C^Emergency Contact^03ZConRol^^^||||||||||||||||||||||||||||||||\r" +
                "PV1||R|8001-CC-Endoscopy^^^G^4265^^^C^^^Endoscopy^112^|||^^^G^4265^^^^^^^  ^|13893^Generic^MoeA^PhysicianThree^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13893^Generic^MoeB^PhysicianThree^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||||||A|||13893^Generic^MoeC^PhysicianThree^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|OP^|"+mrn+"VIDR^^^UHN^VN^G^4265^^^^^||||||||||||||||||||G|||||201112151022|||||||V|\r" + 
                "PV2|||^^^^^|||||||||||||||||||N||AO|||||||N||||||||||||||||||\r" + 
                "DG1|1||||||||||||||||||||\r" + 
                "PR1||||||||||||||||||||\r" + 
                "ZPV|^|^|||N|||13893^Generic^Moe^PhysicianThree^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|13893^Generic^Moe^PhysicianThree^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||^^|13893^Generic^Moe^PhysicianThree^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|||||^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r";
        
        
      String patientArrival1 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112151022||ADT^A10^ADT_A09|123766|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A10|201112151022||||201112151022|G^4265^L\r" + 
                "PID|||"+mrn+"R^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR^G~9287170269^BL^^CANON^JHN^^^^^20111201^^||Smith^Recuro^Recuro^Junior^Mr^MD^L~Smith^Dead^^^^^A|Blanche^^^^^^L|19400313|M|||30 RIVINGTON AVE^^Goderich^CANON^N7A3Y2^CAN^H~70 WOODPLUMPTON ROAD^^Port Stanley^CANON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English^HL70296|||||||||||||||N\r" + 
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "PV1||R|8001-CC-Endoscopy^^^G^4265^^^C^^^Endoscopy^112^|||^^^G^4265^^^^^^^  ^|13893^Generic^MoeA^PhysicianThree^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13893^Generic^MoeB^PhysicianThree^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||||||A|||13893^Generic^MoeC^PhysicianThree^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|OP^|"+mrn+"VIDR^^^UHN^VN^G^4265^^^^^||||||||||||||||||||G|||||201112161022|||||||V|\r" + 
                "PV2|||^^^^^|||||||||||||||||||N||AO|||||||N||||||||||||||||||\r" + 
                "DG1|1||||||||||||||||||||\r"; 
      
      
       String patientArrival2 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112151022||ADT^A10^ADT_A09|123766|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A10|201112151022||||201112151022|G^4265^L\r" +
                "PID|||"+mrn+"R^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR^G~9287170269^BL^^CANON^JHN^^^^^20111201^^||Smith^Recuro^Recuro^Junior^Mr^MD^L~Smith^Dead^^^^^A|Blanche^^^^^^L|19400313|M|||30 RIVINGTON AVE^^Goderich^CANON^N7A3Y2^CAN^H~70 WOODPLUMPTON ROAD^^Port Stanley^CANON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English^HL70296|||||||||||||||N\r" +                
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "PV1||R|8001-CC-Endoscopy^^^G^4265^^^C^^^Endoscopy^112^|||^^^G^4265^^^^^^^  ^|13893^Generic^MoeA^PhysicianThree^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13893^Generic^MoeB^PhysicianThree^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||||||A|||13893^Generic^MoeC^PhysicianThree^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|OP^|"+mrn+"VIDR^^^UHN^VN^G^4265^^^^^||||||||||||||||||||G|||||201112171022|||||||V|\r" + 
                "PV2|||^^^^^|||||||||||||||||||N||AO|||||||N||||||||||||||||||\r" + 
                "DG1|1||||||||||||||||||||\r"; 
                

        
        
       
       Persister.persist(UhnConverter.convertAdtOrFail(registerReoccurringOutPatient));       
       Persister.persist(UhnConverter.convertAdtOrFail(patientArrival1));
       Persister.persist(UhnConverter.convertAdtOrFail(patientArrival2));
        
        
    }
        
    
        
        
    private static void persistMultipleAdt(String mrn) throws Exception {
       
       
       String msg1  = 
           "MSH|^~\\&|2.16.840.1.113883.3.239.23.7^2.16.840.1.113883.3.239.23.7.101.11|SHSC|ConnectingGTA|ConnectingGTA|20120726112613-0500|23498643698hhh|ADT^A01^ADT_A01|          13|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r" + 
           "EVN||201207261122-0500|||\r" + 
           "PID|1||7018743^^^2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.11^MR~3201523978^^^^JHN^^^^CANON&Ontario&HL70363||SUNNYBROOKD^CGTAFOUR^^^^^L||19870112000000-0500|M|||||1(416)480-7704^PRN^PH^^^^^^^~1(416)123-1228X99999^WPN^PH^^^^^^^|||||||||||||||||N\r" + 
           "PV1|1|I|CRCU^CRCU^40^2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.11|E|||000811^SBPHYSONE^TESTA^^^^^^1.3.6.1.4.1.12201.1.2.1.5&2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.11|||Orthopedics|||||||000811^SBPHYSONE^TESTA^^^^^^1.3.6.1.4.1.12201.1.2.1.5&2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.11||12711A^^^2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.11^VN|||||||||||||||||||||||||20120726112200-0500|||\r" + 
           "DG1|1||^TEST^|\r";
       
       String msg2  = 
           "MSH|^~\\&|2.16.840.1.113883.3.239.23.7^2.16.840.1.113883.3.239.23.7.101.11|SHSC|ConnectingGTA|ConnectingGTA|20120726112809-0500|23498643698hhh|ADT^A08^ADT_A08|          14|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r" + 
           "EVN||201207261126-0500|||\r" + 
           "PID|1||7018743^^^2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.11^MR~3201523978^^^^JHN^^^^CANON&Ontario&HL70363||SUNNYBROOKD^CGTAFOUR^^^^^L||19870112000000-0500|M|||10-4700 WILLOW STREET^^WHITBY^CANON^L3X1Z5^CANADA^H||1(905)889-1234^PRN^PH^^^^^^^~1(416)221-1234X77213^WPN^PH^^^^^^^|||||||||||||||||N\r" + 
           "PV1|1|I|CRCU^^^2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.11|E||||||Orthopedics|||||||||12711A^^^2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.11^VN||||||||||||||||||||||||||||\r";
       
       String msg3  = 
           "MSH|^~\\&|2.16.840.1.113883.3.239.23.7^2.16.840.1.113883.3.239.23.7.101.11|SHSC|ConnectingGTA|ConnectingGTA|20120726112931-0500|23498643698hhh|ADT^A02^ADT_A02|          15|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r" + 
           "EVN||201207261127-0500|||\r" + 
           "PID|1||7018743^^^2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.11^MR~3201523978^^^^JHN^^^^CANON&Ontario&HL70363||SUNNYBROOKD^CGTAFOUR^^^^^L||19870112000000-0500|M|||||~|||||||||||||||||N\r" + 
           "PV1|1|I|D6^D643^02^2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.11|||CRCU^CRCU^40^2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.11|000811^SBPHYSONE^TESTA^^^^^^1.3.6.1.4.1.12201.1.2.1.5&2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.11|||Orthopedics|||||||||12711A^^^2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.11^VN|||||||||||||||||||||||||20120726112200-0500|||\r";
       
       String msg4  = 
           "MSH|^~\\&|2.16.840.1.113883.3.239.23.7^2.16.840.1.113883.3.239.23.7.101.11|SHSC|ConnectingGTA|ConnectingGTA|20120726114404-0500|23498643698hhh|ADT^A17^ADT_A17|          16|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r" + 
           "EVN||201207261142-0500|||\r" + 
           "PID|1||7018741^^^2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.11^MR||SUNNYBROOKB^CGTATWO^O'B^^^^L||19700615000000-0500|M||||||||||||||\r" + 
           "PV1|1|I|D6^D643^02^2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.11|||D6^D640^01^2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.11||||General Medicine|||||||||12611A^^^2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.11^VN||||||||||||||||||||||||||||\r" + 
           "PID|2||7018743^^^2.16.840.1.113883.3.239.23.72.16.840.1.113883.3.239.23.7.101.11&^MR||SUNNYBROOKD^CGTAFOUR^^^^^L||19870112000000-0500|M||||||||||||||\r" + 
           "PV1|2|I|D6^D640^01^2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.11|||D6^D643^02^2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.11||||Orthopedics|||||||||12711A^^^2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.11^VN|||||||||||||||||||||||||||\r";
       
       String msg5  = 
           "MSH|^~\\&|2.16.840.1.113883.3.239.23.7^2.16.840.1.113883.3.239.23.7.101.11|SHSC|ConnectingGTA|ConnectingGTA|20120726114518-0500|23498643698hhh|ADT^A03^ADT_A03|          17|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r" + 
           "EVN||201207261143-0500|||\r" + 
           "PID|1||7018743^^^2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.11^MR~3201523978^^^^JHN^^^^CANON&Ontario&HL70363||SUNNYBROOKD^CGTAFOUR^^^^^L||19870112000000-0500|M|||10-4700 WILLOW STREET^^WHITBY^CANON^L3X1Z5^CANADA^H||1(905)889-1234^PRN^PH^^^^^^^~1(416)221-1234X77213^WPN^PH^^^^^^^|||||||||||||||||N\r" + 
           "PV1|1|I|D6^D640^01^2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.11|E|||000811^SBPHYSONE^TESTA^^^^^^1.3.6.1.4.1.12201.1.2.1.5&2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.11|||Orthopedics|||||||||12711A^^^2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.11^VN|||||||||||||||||||||||||20120726112200-0500|20120726114300-0500||\r";
       
       String msg6  = 
           "MSH|^~\\&|2.16.840.1.113883.3.239.23.7^2.16.840.1.113883.3.239.23.7.101.11|SHSC|ConnectingGTA|ConnectingGTA|20120726115416-0500|23498643698hhh|ADT^A13^ADT_A13|          18|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r" + 
           "EVN||201207261152-0500|||\r" + 
           "PID|1||7018743^^^2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.11^MR~3201523978^^^^JHN^^^^CANON&Ontario&HL70363||SUNNYBROOKD^CGTAFOUR^^^^^L||19870112000000-0500|M|||||~|||||||||||||||||N\r" + 
           "PV1|1|I|D6^D640^01^2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.11|E||||||Orthopedics|||||||||12711A^^^2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.11^VN|||||||||||||||||||||||||20120726112200-0500|||\r";
       
       String msg7  = 
           "MSH|^~\\&|2.16.840.1.113883.3.239.23.7^2.16.840.1.113883.3.239.23.7.101.11|SHSC|ConnectingGTA|ConnectingGTA|20120802173714-0500|23498643698hhh|ADT^A03^ADT_A03|         193|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r" + 
           "EVN||201208021735-0500\r" + 
           "PID|1||7018743^^^2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.11^MR~3201523978^^^^JHN^^^^CANON&Ontario&HL70363||SUNNYBROOKD^CGTAFOUR^^^^^L||19870112000000-0500|M|||10-4700 WILLOW STREET^^WHITBY^CANON^L3X1Z5^CANADA^H||1(905)889-1234^PRN^PH~1(416)221-1234X77213^WPN^PH|||||||||||||||||N\r" + 
           "PV1|1|I|D6^D640^01^2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.11|E|||000811^SBPHYSONE^TESTA^^^^^^1.3.6.1.4.1.12201.1.2.1.5&2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.11|||Orthopedics|||||||||12711A^^^2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.11^VN|||||||||||||||||||||||||20120726112200-0500|20120802173500-0500\r";
       
       
       String[] msgs = {/*msg1, msg2, msg3/*, /*msg4,*/ msg5 /*, msg6, msg7*/};
       processMsgs(msgs);
                
    }
    
    
    
    
    /**
     * 
     * @param theMsgs
     * @return ...
     * @throws Exception
     * 
     */
    private static void processMsgs(String[] theMsgs) throws Exception {

        Parser parser = new PipeParser();
        Converter c = new Converter();
        ADT_A01 inputAdt = new ADT_A01();
        inputAdt.setParser(PipeParser.getInstanceWithNoValidation());
        ORU_R01 inputOru = new ORU_R01();
        inputOru.setParser(PipeParser.getInstanceWithNoValidation());
        RDE_O11 inputRde = new RDE_O11();
        inputRde.setParser(PipeParser.getInstanceWithNoValidation());
        RAS_O17 inputRas = new RAS_O17();
        inputRde.setParser(PipeParser.getInstanceWithNoValidation());

        for (int i = 0; i < theMsgs.length; i++) {

            String msgType = PreParser.getFields(theMsgs[i], new String[] { "MSH-9-1" })[0];
            String sendingSys = PreParser.getFields(theMsgs[i], new String[] { "MSH-3-1" })[0];

            if (msgType.equals("ADT")) {
                if (sendingSys.equals("EPR")) {
                    Persister.persist(UhnConverter.convertAdtOrFail(theMsgs[i]));
                }
                else {
                    inputAdt.parse(theMsgs[i]);
                    Persister.persist(c.convertPatientWithVisits(inputAdt));
                }
            }

            if (msgType.equals("ORU")) {
                if (sendingSys.equals("EPR")) {
                    Persister.persist(UhnConverter.convertOru(theMsgs[i]));
                }
                else {
                    inputOru.parse(theMsgs[i]);
                    Persister.persist(c.convertClinicalDocument(inputOru));
                }
            }

            if (msgType.equals("RDE")) {
                inputRde.parse(theMsgs[i]);
                Persister.persistMedicationOrders(c.convertMedicationOrder(inputRde));
            }

            if (msgType.equals("RAS")) {
                inputRas.parse(theMsgs[i]);
                Persister.persistMedicationAdmins(c.convertMedicationAdmin(inputRas));
            }

        }

    }
        
    
   
        

    
    
 
	

}
