package ca.cgta.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.ektorp.ViewQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ca.cgta.couchdb.tools.AllDataPurger;
import ca.cgta.couchdb.tools.ViewUploader;
import ca.cgta.input.converter.Converter;
import ca.cgta.input.listener.Persister;
import ca.cgta.input.model.inner.Ce;
import ca.cgta.input.model.inner.ClinicalDocumentData;
import ca.cgta.input.model.inner.ClinicalDocumentSection;
import ca.cgta.input.model.inner.Cx;
import ca.cgta.input.model.inner.Ei;
import ca.cgta.input.model.inner.Note;
import ca.cgta.input.model.inner.Patient;
import ca.cgta.input.model.inner.Pl;
import ca.cgta.input.model.inner.Visit;
import ca.cgta.input.model.inner.Xad;
import ca.cgta.input.model.inner.Xcn;
import ca.cgta.input.model.inner.Xpn;
import ca.cgta.input.model.inner.Xtn;
import ca.cgta.input.model.outer.ClinicalDocumentContainer;
import ca.cgta.input.model.outer.ClinicalDocumentGroup;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.parser.PipeParser;

public class OruTest {
    
    
    private static DateFormat ourTsFormat = new SimpleDateFormat("yyyyMMddHHmm");
    private static DateFormat ourTsLongFormat = new SimpleDateFormat("yyyyMMddHHmmssZ");
    private static DateFormat ourTsYearFormat = new SimpleDateFormat("yyyy");
    private static DateFormat ourTsMonthFormat = new SimpleDateFormat("yyyyMM");
    private static DateFormat ourTsSecFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private static DateFormat ourDtFormat = new SimpleDateFormat("yyyyMMdd");

	@Before
	public void before() throws IOException, Exception {
		Persister.setUnitTestMode(true);
		AllDataPurger.purgeAllData(); 
		ViewUploader.uploadAllViews();
		
	}
	
	
	
    /**
     * Testing OruR01 with an electronic doc stored in a single observation
     *   
     * @throws Exception ...
     */
    @Test
    public void testOruR01a() throws Exception {
        
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "sampleJpg.jpg");
        byte[] bytes = IOUtils.toByteArray(is);
        String encodedEdVal = Base64.encodeBase64String(bytes);
        encodedEdVal = encodedEdVal.replaceAll("\n", "");
        encodedEdVal = encodedEdVal.replaceAll("\r", "");
        
        //System.out.println(encodedString);
                
        String message1 = 
                "MSH|^~\\&|EPR|UHNG|||201205011031||ORU^R01^ORU_R01|31768|T|2.5\r" +
                "PID|1||7005728^^^UHN^MR^G^4265~00000000000^AA^^CANON^JHN^G||Smith^Joseph^John^Junior^Mr^MD^L~Smith^Joe^^^^^A|Blanche^^^^^^L|19310313|M|||26 RIVINGTON AVE^^Goderich^ON^N7A3Y2^CAN^H~7 WOODPLUMPTON ROAD^^Port Stanley^ON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English|||||||||||||||N\r" +                 
                "PV1|1|I|JS12^123^4^G^^Q Building^12^JS12-123-4 GIM Inpatient Unit||||38946^Blake^Donald^Thor^^^^^|38946^Blake^Donald^Thor^^^^^|38946^Blake^Donald^Thor^^^^^|GIM|||||||38946^Blake^Donald^Thor^^^^^||284675^^^2.16.840.1.113883.3.59.3:0947^VN^G\r" +                 
                "ORC|1|7777^EPR^2.16.840.1.113883.3.59.3:947|||CM\r" +                
                "OBR|1|7777^EPR^2.16.840.1.113883.3.59.3:947||50111^OR/Procedure Note^HL70396|||20110126124300-0500|20110126125000-0500||||||||||N|||||||F|7776&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.1||||||5555&Smith&John&&&&Dr&&2.16.840.1.113883.4.347\r" +
                "NTE|1||Note1||\r" +
                "NTE|2||Note2||\r" +
                "NTE|3||Note3||\r" +
                "OBX|1|DT|10017^Date Dictated^HL70396||12 Jan 2012||||||F\r" + 
                "OBX|2|ST|1126527^Dictated by^HL70396||John Smith, MD||||||F\r" + 
                "OBX|3|NM|14007^Pressure^HL70396||0|ml|-2 - 4|N|||F\r" +        
                "OBX|4|ED|14009^Picture^HL70396||^NS^JPEG^Base64^"+encodedEdVal+"||||||F\r" +
                "NTE|1||Note1||\r" +
                "NTE|2||Note2||\r" +
                "NTE|3||Note3||\r";
        
       
        
        List<ClinicalDocumentGroup> clinDocs = UhnConverter.convertOru(message1);
        Persister.persist(clinDocs);
                
        ViewQuery query = new ViewQuery().viewName("allDocuments").designDocId("_design/application");
        List<ClinicalDocumentContainer> clinDocContainers = Persister.getConnector().queryView(query, ClinicalDocumentContainer.class);
        
        ClinicalDocumentContainer clinDocContainer = clinDocContainers.get(0);
        Patient patient = clinDocContainer.getDocument().myPatient;
        Visit visit = clinDocContainer.getDocument().myVisit;
        Ei placerGroupNumber = clinDocContainer.getDocument().myPlacerGroupNumber;
        ClinicalDocumentSection clinDocSection = clinDocContainer.getDocument().mySections.get(0);
        
        
        //check ClinicalDocumentGroup (patient result group)  

        //check lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > clinDocContainer.getDocument().myRecordUpdatedDate.getTime());
      
        
        //check patient
        assertEquals("M", patient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("193103130000"), patient.myDateOfBirth);
        assertEquals(null, patient.myDeathDateAndTime);
        assertEquals("N", patient.myDeathIndicator);
        assertEquals("Blanche", patient.myMothersMaidenName.myLastName);
        
        
        Cx patId = patient.myPatientIds.get(0);
        assertEquals("7005728", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Cx patId2 = patient.myPatientIds.get(1);
        assertEquals("00000000000", patId2.myIdNumber);
        assertEquals("CANON", patId2.myAssigningJurisdictionId);
        assertEquals("JHN", patId2.myIdTypeCode);
        
        
        Xpn patName = patient.myPatientNames.get(0);
        assertEquals("Smith", patName.myLastName);
        assertEquals("Joseph", patName.myFirstName);
        
        
        Xad patAddresses = patient.myPatientAddresses.get(0);
        assertEquals("26 RIVINGTON AVE", patAddresses.myStreetAddress);
        assertEquals("Goderich", patAddresses.myCity);
        
                
        Xtn patPhone = patient.myPhoneNumbers.get(0);
        assertEquals("1 (416) 340-4800", patPhone.myPhoneNumber);
        
        Ce lang = patient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
               
       
        //check visit
        assertEquals("284675", visit.myVisitNumber.myIdNumber);        
        assertEquals("I", visit.myPatientClassCode);
        assertEquals(null, visit.myAdmitDate);
        assertEquals("GIM", visit.myHospitalService);
        Pl loc = visit.myAssignedPatientLocation;
        assertEquals("JS12", loc.myPointOfCare);
        assertEquals("123", loc.myRoom);
        assertEquals("4", loc.myBed);
        
        
        Xcn admitDoc = visit.myAdmittingDoctors.get(0);
        assertEquals("38946", admitDoc.myId);
        assertEquals("Donald", admitDoc.myFirstName);
        assertEquals("Blake", admitDoc.myLastName);
        assertEquals("Thor", admitDoc.myMiddleName);
        
        
        Xcn attendDoc = visit.myAttendingDoctors.get(0);
        assertEquals("38946", attendDoc.myId);
        assertEquals("Donald", attendDoc.myFirstName);
        assertEquals("Blake", attendDoc.myLastName);
        assertEquals("Thor", attendDoc.myMiddleName);
        
        
        Xcn refDoc = visit.myReferringDoctors.get(0);
        assertEquals("38946", refDoc.myId);
        assertEquals("Donald", refDoc.myFirstName);
        assertEquals("Blake", refDoc.myLastName);
        assertEquals("Thor", refDoc.myMiddleName);

        
        assertEquals("7777", placerGroupNumber.myId);
        assertEquals("1.3.6.1.4.1.12201", placerGroupNumber.myFacilityId);
        assertEquals("1.3.6.1.4.1.12201.101.1", placerGroupNumber.mySystemId);
        
        
        //check ClinicalDocumentSection (order observation group)        
        
        assertEquals("NORMAL", clinDocSection.myConfidentiality.name());
        assertEquals("7776", clinDocSection.myParentSectionId.myId);
        assertEquals(ourTsLongFormat.parse("20110126124300-0500"), clinDocSection.myDate);
        assertEquals(ourTsLongFormat.parse("20110126125000-0500"), clinDocSection.myEndDate);
        assertEquals("5555", clinDocSection.myPrincipalInterpreter.myId);        
        assertEquals("Smith", clinDocSection.myPrincipalInterpreter.myLastName);
        assertEquals("John", clinDocSection.myPrincipalInterpreter.myFirstName);
        assertEquals("7777", clinDocSection.mySectionId.myId);
        assertEquals("50111", clinDocSection.mySectionCode.myCode);
        assertEquals("OR/Procedure Note", clinDocSection.mySectionName);
        assertEquals("F", clinDocSection.myStatusCode);
        assertEquals("Final", clinDocSection.myStatus);
        
        List<Note> odrNotes = clinDocSection.myNotes;
        assertEquals(3, odrNotes.size());
        assertEquals("Note1", odrNotes.get(0).myNoteText);
        assertEquals("Note2", odrNotes.get(1).myNoteText);
        assertEquals("Note3", odrNotes.get(2).myNoteText);

        
        
        //check ClinicalDocumentData (observation groups)
        
        List<ClinicalDocumentData> clinDocDataList = clinDocSection.myData;
        assertEquals(4,clinDocDataList.size());        
        
        assertEquals("DT", clinDocDataList.get(0).myDataType);
        assertEquals("10017", clinDocDataList.get(0).myCode.myCode);
        assertEquals("Date Dictated", clinDocDataList.get(0).myCode.myText);
        assertEquals("F", clinDocDataList.get(0).myDataStatusCode);
        assertEquals("Final", clinDocDataList.get(0).myDataStatus);
        assertEquals("2012-01-12", clinDocDataList.get(0).myValue);
        
        
        assertEquals("ST", clinDocDataList.get(1).myDataType);
        assertEquals("1126527", clinDocDataList.get(1).myCode.myCode);
        assertEquals("Dictated by", clinDocDataList.get(1).myCode.myText);
        assertEquals("F", clinDocDataList.get(1).myDataStatusCode);
        assertEquals("Final", clinDocDataList.get(1).myDataStatus);
        assertEquals("John Smith, MD", clinDocDataList.get(1).myValue);


        assertEquals("NM", clinDocDataList.get(2).myDataType);
        assertEquals("14007", clinDocDataList.get(2).myCode.myCode);
        assertEquals("Pressure", clinDocDataList.get(2).myCode.myText);
        assertEquals("F", clinDocDataList.get(2).myDataStatusCode);
        assertEquals("Final", clinDocDataList.get(2).myDataStatus);
        assertEquals("0", clinDocDataList.get(2).myValue);
        assertEquals("N", clinDocDataList.get(2).myAbnormalFlagCode);
        assertEquals("Normal", clinDocDataList.get(2).myAbnormalFlagName.toString());
        assertEquals("-2 - 4", clinDocDataList.get(2).myRefRange);
        
        
        assertEquals("ED", clinDocDataList.get(3).myDataType);
        assertEquals("14009", clinDocDataList.get(3).myCode.myCode);
        assertEquals("Picture", clinDocDataList.get(3).myCode.myText);
        assertEquals("F", clinDocDataList.get(3).myDataStatusCode);
        assertEquals("Final", clinDocDataList.get(3).myDataStatus);
        assertEquals(encodedEdVal, clinDocDataList.get(3).myValue);
        assertEquals("NS", clinDocDataList.get(3).myEncapsulatedDataType);
        assertEquals("JPEG", clinDocDataList.get(3).myEncapsulatedDataSubType);
        assertEquals("image/jpeg", clinDocDataList.get(3).myEncapsulatedDataMimeType);
        
        List<Note> obsNotes = clinDocDataList.get(3).myNotes;
        assertEquals(3, obsNotes.size());
        assertEquals("Note1", obsNotes.get(0).myNoteText);
        assertEquals("Note2", obsNotes.get(1).myNoteText);
        assertEquals("Note3", obsNotes.get(2).myNoteText);
                
    }
    
    
    
    /**
     * Testing OruR01 with an electronic doc stored accross multiple observations 
     *   
     * @throws Exception ...
     */
    @Test
    public void testOruR01b() throws Exception {
        
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "sampleGif.gif");
        byte[] bytes = IOUtils.toByteArray(is);
        String encodedEdVal = Base64.encodeBase64String(bytes);
        encodedEdVal = encodedEdVal.replaceAll("\r", "");
        String EdObxSegs = createEdObxSegments(encodedEdVal);
        
                
        String message1 = 
                "MSH|^~\\&|EPR|UHNG|||201205011031||ORU^R01^ORU_R01|31768|T|2.5\r" +
                "PID|1||7005728^^^UHN^MR^G^4265~00000000000^AA^^CANON^JHN^G||Smith^Joseph^John^Junior^Mr^MD^L~Smith^Joe^^^^^A|Blanche^^^^^^L|19310313|M|||26 RIVINGTON AVE^^Goderich^ON^N7A3Y2^CAN^H~7 WOODPLUMPTON ROAD^^Port Stanley^ON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English|||||||||||||||N\r" +                 
                "PV1|1|I|JS12^123^4^G^^Q Building^12^JS12-123-4 GIM Inpatient Unit||||38946^Blake^Donald^Thor^^^^^|38946^Blake^Donald^Thor^^^^^|38946^Blake^Donald^Thor^^^^^|GIM|||||||38946^Blake^Donald^Thor^^^^^||284675^^^2.16.840.1.113883.3.59.3:0947^VN^G\r" +                 
                "ORC|1|7777^EPR^2.16.840.1.113883.3.59.3:947|||CM\r" +                
                "OBR|1|7777^EPR^2.16.840.1.113883.3.59.3:947||50111^OR/Procedure Note^HL70396|||20110126124300-0500|20110126125000-0500||||||||||N|||||||F|7776&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.1||||||5555&Smith&John&&&&Dr&&2.16.840.1.113883.4.347\r" +                 
                "NTE|1||Note1||\r" +
                "NTE|2||Note2||\r" +
                "NTE|3||Note3||\r" +
                "OBX|1|DT|10017^Date Dictated^HL70396||12 Jan 2012||||||F\r" + 
                "OBX|2|ST|1126527^Dictated by^HL70396||John Smith, MD||||||F\r" + 
                "OBX|3|NM|14007^Pressure^HL70396||0|ml|-2 - 4|N|||F\r" + 
                 EdObxSegs +
                "NTE|1||Note1||\r" +
                "NTE|2||Note2||\r" +
                "NTE|3||Note3||\r";
        
       
        
        List<ClinicalDocumentGroup> clinDocs = UhnConverter.convertOru(message1);
        Persister.persist(clinDocs);
                
        ViewQuery query = new ViewQuery().viewName("allDocuments").designDocId("_design/application");
        List<ClinicalDocumentContainer> clinDocContainers = Persister.getConnector().queryView(query, ClinicalDocumentContainer.class);
        
        ClinicalDocumentContainer clinDocContainer = clinDocContainers.get(0);
        Patient patient = clinDocContainer.getDocument().myPatient;
        Visit visit = clinDocContainer.getDocument().myVisit;
        Ei placerGroupNumber = clinDocContainer.getDocument().myPlacerGroupNumber;
        ClinicalDocumentSection clinDocSection = clinDocContainer.getDocument().mySections.get(0);
        
        
        //check ClinicalDocumentGroup (patient result group)       
        
        //check lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > clinDocContainer.getDocument().myRecordUpdatedDate.getTime());
        
        
        
        //check patient
        assertEquals("M", patient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("193103130000"), patient.myDateOfBirth);
        assertEquals(null, patient.myDeathDateAndTime);
        assertEquals("N", patient.myDeathIndicator);
        assertEquals("Blanche", patient.myMothersMaidenName.myLastName);
        
        
        Cx patId = patient.myPatientIds.get(0);
        assertEquals("7005728", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Cx patId2 = patient.myPatientIds.get(1);
        assertEquals("00000000000", patId2.myIdNumber);
        assertEquals("CANON", patId2.myAssigningJurisdictionId);
        assertEquals("JHN", patId2.myIdTypeCode);
        
        
        Xpn patName = patient.myPatientNames.get(0);
        assertEquals("Smith", patName.myLastName);
        assertEquals("Joseph", patName.myFirstName);
        
        
        Xad patAddresses = patient.myPatientAddresses.get(0);
        assertEquals("26 RIVINGTON AVE", patAddresses.myStreetAddress);
        assertEquals("Goderich", patAddresses.myCity);
        
                
        Xtn patPhone = patient.myPhoneNumbers.get(0);
        assertEquals("1 (416) 340-4800", patPhone.myPhoneNumber);
        
        Ce lang = patient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
               
       
        //check visit
        assertEquals("284675", visit.myVisitNumber.myIdNumber);        
        assertEquals("I", visit.myPatientClassCode);
        assertEquals(null, visit.myAdmitDate);
        assertEquals("GIM", visit.myHospitalService);
        Pl loc = visit.myAssignedPatientLocation;
        assertEquals("JS12", loc.myPointOfCare);
        assertEquals("123", loc.myRoom);
        assertEquals("4", loc.myBed);
        
        
        Xcn admitDoc = visit.myAdmittingDoctors.get(0);
        assertEquals("38946", admitDoc.myId);
        assertEquals("Donald", admitDoc.myFirstName);
        assertEquals("Blake", admitDoc.myLastName);
        assertEquals("Thor", admitDoc.myMiddleName);
        
        
        Xcn attendDoc = visit.myAttendingDoctors.get(0);
        assertEquals("38946", attendDoc.myId);
        assertEquals("Donald", attendDoc.myFirstName);
        assertEquals("Blake", attendDoc.myLastName);
        assertEquals("Thor", attendDoc.myMiddleName);
        
        
        Xcn refDoc = visit.myReferringDoctors.get(0);
        assertEquals("38946", refDoc.myId);
        assertEquals("Donald", refDoc.myFirstName);
        assertEquals("Blake", refDoc.myLastName);
        assertEquals("Thor", refDoc.myMiddleName);

        
        assertEquals("7777", placerGroupNumber.myId);
        assertEquals("1.3.6.1.4.1.12201", placerGroupNumber.myFacilityId);
        assertEquals("1.3.6.1.4.1.12201.101.1", placerGroupNumber.mySystemId);
        
        
        //check ClinicalDocumentSection (order observation group)
        assertEquals("NORMAL", clinDocSection.myConfidentiality.name());
        assertEquals("7776", clinDocSection.myParentSectionId.myId);
        assertEquals(ourTsLongFormat.parse("20110126124300-0500"), clinDocSection.myDate);
        assertEquals(ourTsLongFormat.parse("20110126125000-0500"), clinDocSection.myEndDate);
        assertEquals("5555", clinDocSection.myPrincipalInterpreter.myId);        
        assertEquals("Smith", clinDocSection.myPrincipalInterpreter.myLastName);
        assertEquals("John", clinDocSection.myPrincipalInterpreter.myFirstName);
        assertEquals("7777", clinDocSection.mySectionId.myId);
        assertEquals("50111", clinDocSection.mySectionCode.myCode);
        assertEquals("OR/Procedure Note", clinDocSection.mySectionName);
        assertEquals("F", clinDocSection.myStatusCode);
        assertEquals("Final", clinDocSection.myStatus);
        
        List<Note> odrNotes = clinDocSection.myNotes;
        assertEquals(3, odrNotes.size());
        assertEquals("Note1", odrNotes.get(0).myNoteText);
        assertEquals("Note2", odrNotes.get(1).myNoteText);
        assertEquals("Note3", odrNotes.get(2).myNoteText);

        
        
        //check ClinicalDocumentData (observation groups)
        
        List<ClinicalDocumentData> clinDocDataList = clinDocSection.myData;
        assertEquals(4,clinDocDataList.size());        
        
        assertEquals("DT", clinDocDataList.get(0).myDataType);
        assertEquals("10017", clinDocDataList.get(0).myCode.myCode);
        assertEquals("Date Dictated", clinDocDataList.get(0).myCode.myText);
        assertEquals("F", clinDocDataList.get(0).myDataStatusCode);
        assertEquals("Final", clinDocDataList.get(0).myDataStatus);
        assertEquals("2012-01-12", clinDocDataList.get(0).myValue);
        
        
        assertEquals("ST", clinDocDataList.get(1).myDataType);
        assertEquals("1126527", clinDocDataList.get(1).myCode.myCode);
        assertEquals("Dictated by", clinDocDataList.get(1).myCode.myText);
        assertEquals("F", clinDocDataList.get(1).myDataStatusCode);
        assertEquals("Final", clinDocDataList.get(1).myDataStatus);
        assertEquals("John Smith, MD", clinDocDataList.get(1).myValue);


        assertEquals("NM", clinDocDataList.get(2).myDataType);
        assertEquals("14007", clinDocDataList.get(2).myCode.myCode);
        assertEquals("Pressure", clinDocDataList.get(2).myCode.myText);
        assertEquals("F", clinDocDataList.get(2).myDataStatusCode);
        assertEquals("Final", clinDocDataList.get(2).myDataStatus);
        assertEquals("0", clinDocDataList.get(2).myValue);
        assertEquals("N", clinDocDataList.get(2).myAbnormalFlagCode);
        assertEquals("Normal", clinDocDataList.get(2).myAbnormalFlagName.toString());
        assertEquals("-2 - 4", clinDocDataList.get(2).myRefRange);
        
        String storedEdVal = encodedEdVal.replaceAll("\n", "");
        
        assertEquals("ED", clinDocDataList.get(3).myDataType);
        assertEquals("14009", clinDocDataList.get(3).myCode.myCode);
        assertEquals("Picture", clinDocDataList.get(3).myCode.myText);
        assertEquals("F", clinDocDataList.get(3).myDataStatusCode);
        assertEquals("Final", clinDocDataList.get(3).myDataStatus);
        assertEquals(storedEdVal, clinDocDataList.get(3).myValue);
        assertEquals("NS", clinDocDataList.get(3).myEncapsulatedDataType);
        assertEquals("GIF", clinDocDataList.get(3).myEncapsulatedDataSubType);
        assertEquals("image/gif", clinDocDataList.get(3).myEncapsulatedDataMimeType);
        
        List<Note> obrNotes = clinDocDataList.get(3).myNotes;
        assertEquals(3, obrNotes.size());
        assertEquals("Note1", obrNotes.get(0).myNoteText);
        assertEquals("Note2", obrNotes.get(1).myNoteText);
        assertEquals("Note3", obrNotes.get(2).myNoteText);
        
        //System.out.println(storedEdVal);
                
    }    
    
    
    
    /**
     * Testing OruR01 with multiple orders, each one belonging to the same placer group.
     * Bypass UHNconvertor since it won't process this type of message
     *   
     * @throws Exception ...
     */
    @Test
    public void testOruR01c() throws Exception {
        
                
        String message1 = 
                "MSH|^~\\&|1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1|UHNG|||20120501103100-0500|2954864636aaa|ORU^R01^ORU_R01|31768|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r" +
                "PID|1||7005728^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR^G~00000000000^AA^^^JHN^G^^^CANON&&HL70363||Smith^Joseph^John^Junior^Mr^MD^L~Smith^Joe^^^^^A|Blanche^^^^^^L|19310313|M|||26 RIVINGTON AVE^^Goderich^CANON^N7A3Y2^CAN^H~7 WOODPLUMPTON ROAD^^Port Stanley^CANON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English^HL70296|||||||||||||||N\r" +                 
                "PV1|1|I|JS12^123^4^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.100.1||||38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|GIM|||||||38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1||284675^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^VN\r" +                 
                "ORC|1|||777^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1\r" +                
                "OBR|1|7777^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1||50111^OR/Procedure Note^1.3.6.1.4.1.12201.102.5|||20110126124300-0500|20110126125000-0500|||||||||||||||||F|7776&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1||||||5555&Smith&John&&&&Dr&&2.16.840.1.113883.4.347\r" +
                "NTE|1||Note1||\r" +
                "NTE|2||Note2||\r" +
                "NTE|3||Note3||\r" +
                "OBX|1|DT|10017^Date Dictated^1.3.6.1.4.1.12201.102.6||20120112||||||F\r" +
                "OBX|2|ST|1126527^Dictated by^1.3.6.1.4.1.12201.102.6||John Smith, MD||||||F\r" +
                "OBX|3|NM|14007^Pressure^1.3.6.1.4.1.12201.102.6||0|ml|-2 - 4|N|||F\r" +
                "NTE|1||Note1||\r" +
                "NTE|2||Note2||\r" +
                "NTE|3||Note3||\r" +
                "ORC|2|||777^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1\r" +
                "OBR|1|7778^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1||50111^OR/Procedure Note2^1.3.6.1.4.1.12201.102.5|||20110126124300-0500|20110126125000-0500|||||||||||||||||F|7776&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1||||||5555&Smith&John&&&&Dr&&2.16.840.1.113883.4.347\r" +
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
                
        ViewQuery query = new ViewQuery().viewName("allDocuments").designDocId("_design/application");
        List<ClinicalDocumentContainer> clinDocContainers = Persister.getConnector().queryView(query, ClinicalDocumentContainer.class);
        //should only be one doc
        assertEquals(1,clinDocContainers.size());
        
        
        //doc1
        String id = "CDOC_1.3.6.1.4.1.12201___1.3.6.1.4.1.12201.101.1___777";
        ClinicalDocumentContainer doc = Persister.getConnector().get(ClinicalDocumentContainer.class, id);
        Patient patient = doc.getDocument().myPatient;
        Visit visit = doc.getDocument().myVisit;
        Ei placerGroupNumber = doc.getDocument().myPlacerGroupNumber;
        assertEquals(2, doc.getDocument().mySections.size());        
        ClinicalDocumentSection clinDocSection1 = doc.getDocument().mySections.get(0);
        ClinicalDocumentSection clinDocSection2 = doc.getDocument().mySections.get(1);
        
        
        //check ClinicalDocumentGroup (patient result group)
        
        //check lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > doc.getDocument().myRecordUpdatedDate.getTime());
        
        
        //check patient
        assertEquals("M", patient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("193103130000"), patient.myDateOfBirth);
        assertEquals(null, patient.myDeathDateAndTime);
        assertEquals("N", patient.myDeathIndicator);
        assertEquals("Blanche", patient.myMothersMaidenName.myLastName);
                
        Cx patId = patient.myPatientIds.get(0);
        assertEquals("7005728", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Cx patId2 = patient.myPatientIds.get(1);
        assertEquals("00000000000", patId2.myIdNumber);
        assertEquals("CANON", patId2.myAssigningJurisdictionId);
        assertEquals("JHN", patId2.myIdTypeCode);
                
        Xpn patName = patient.myPatientNames.get(0);
        assertEquals("Smith", patName.myLastName);
        assertEquals("Joseph", patName.myFirstName);
                
        Xad patAddresses = patient.myPatientAddresses.get(0);
        assertEquals("26 RIVINGTON AVE", patAddresses.myStreetAddress);
        assertEquals("Goderich", patAddresses.myCity);
                        
        Xtn patPhone = patient.myPhoneNumbers.get(0);
        assertEquals("1 (416) 340-4800", patPhone.myPhoneNumber);
        
        Ce lang = patient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
               
        //check visit
        assertEquals("284675", visit.myVisitNumber.myIdNumber);        
        assertEquals("I", visit.myPatientClassCode);
        assertEquals(null, visit.myAdmitDate);
        assertEquals("GIM", visit.myHospitalService);
        Pl loc = visit.myAssignedPatientLocation;
        assertEquals("JS12", loc.myPointOfCare);
        assertEquals("123", loc.myRoom);
        assertEquals("4", loc.myBed);
       
        Xcn admitDoc = visit.myAdmittingDoctors.get(0);
        assertEquals("38946", admitDoc.myId);
        assertEquals("Donald", admitDoc.myFirstName);
        assertEquals("Blake", admitDoc.myLastName);
        assertEquals("Thor", admitDoc.myMiddleName);
                
        Xcn attendDoc = visit.myAttendingDoctors.get(0);
        assertEquals("38946", attendDoc.myId);
        assertEquals("Donald", attendDoc.myFirstName);
        assertEquals("Blake", attendDoc.myLastName);
        assertEquals("Thor", attendDoc.myMiddleName);
                
        Xcn refDoc = visit.myReferringDoctors.get(0);
        assertEquals("38946", refDoc.myId);
        assertEquals("Donald", refDoc.myFirstName);
        assertEquals("Blake", refDoc.myLastName);
        assertEquals("Thor", refDoc.myMiddleName);
        
        assertEquals("777", placerGroupNumber.myId);
        assertEquals("1.3.6.1.4.1.12201", placerGroupNumber.myFacilityId);
        assertEquals("1.3.6.1.4.1.12201.101.1", placerGroupNumber.mySystemId);

                
        //check ClinicalDocumentSection1 (order observation group)
        assertEquals("NORMAL", clinDocSection1.myConfidentiality.name());
        assertEquals("7776", clinDocSection1.myParentSectionId.myId);
        assertEquals(ourTsLongFormat.parse("20110126124300-0500"), clinDocSection1.myDate);
        assertEquals(ourTsLongFormat.parse("20110126125000-0500"), clinDocSection1.myEndDate);
        assertEquals("5555", clinDocSection1.myPrincipalInterpreter.myId);        
        assertEquals("Smith", clinDocSection1.myPrincipalInterpreter.myLastName);
        assertEquals("John", clinDocSection1.myPrincipalInterpreter.myFirstName);
        assertEquals("7777", clinDocSection1.mySectionId.myId);
        assertEquals("50111", clinDocSection1.mySectionCode.myCode);
        assertEquals("OR/Procedure Note", clinDocSection1.mySectionName);
        assertEquals("F", clinDocSection1.myStatusCode);
        assertEquals("Final", clinDocSection1.myStatus);
        
        List<Note> odrNotes = clinDocSection1.myNotes;
        assertEquals(3, odrNotes.size());
        assertEquals("Note1", odrNotes.get(0).myNoteText);
        assertEquals("Note2", odrNotes.get(1).myNoteText);
        assertEquals("Note3", odrNotes.get(2).myNoteText);
        
        //check ClinicalDocumentData for section1 (observation groups)        
        List<ClinicalDocumentData> clinDocDataList = clinDocSection1.myData;
        assertEquals(3,clinDocDataList.size());        
        
        assertEquals("DT", clinDocDataList.get(0).myDataType);
        assertEquals("10017", clinDocDataList.get(0).myCode.myCode);
        assertEquals("Date Dictated", clinDocDataList.get(0).myCode.myText);
        assertEquals("F", clinDocDataList.get(0).myDataStatusCode);
        assertEquals("Final", clinDocDataList.get(0).myDataStatus);
        assertEquals("2012-01-12", clinDocDataList.get(0).myValue);
                
        assertEquals("ST", clinDocDataList.get(1).myDataType);
        assertEquals("1126527", clinDocDataList.get(1).myCode.myCode);
        assertEquals("Dictated by", clinDocDataList.get(1).myCode.myText);
        assertEquals("F", clinDocDataList.get(1).myDataStatusCode);
        assertEquals("Final", clinDocDataList.get(1).myDataStatus);
        assertEquals("John Smith, MD", clinDocDataList.get(1).myValue);

        assertEquals("NM", clinDocDataList.get(2).myDataType);
        assertEquals("14007", clinDocDataList.get(2).myCode.myCode);
        assertEquals("Pressure", clinDocDataList.get(2).myCode.myText);
        assertEquals("F", clinDocDataList.get(2).myDataStatusCode);
        assertEquals("Final", clinDocDataList.get(2).myDataStatus);
        assertEquals("0", clinDocDataList.get(2).myValue);
        assertEquals("N", clinDocDataList.get(2).myAbnormalFlagCode);
        assertEquals("Normal", clinDocDataList.get(2).myAbnormalFlagName.toString());
        assertEquals("-2 - 4", clinDocDataList.get(2).myRefRange);
        
        List<Note> obsNotes = clinDocDataList.get(2).myNotes;
        assertEquals(3, obsNotes.size());
        assertEquals("Note1", obsNotes.get(0).myNoteText);
        assertEquals("Note2", obsNotes.get(1).myNoteText);
        assertEquals("Note3", obsNotes.get(2).myNoteText);

        
        //check ClinicalDocumentSection2 (order observation group)
        assertEquals("NORMAL", clinDocSection2.myConfidentiality.name());
        assertEquals("7776", clinDocSection2.myParentSectionId.myId);
        assertEquals(ourTsLongFormat.parse("20110126124300-0500"), clinDocSection2.myDate);
        assertEquals(ourTsLongFormat.parse("20110126125000-0500"), clinDocSection2.myEndDate);
        assertEquals("5555", clinDocSection2.myPrincipalInterpreter.myId);        
        assertEquals("Smith", clinDocSection2.myPrincipalInterpreter.myLastName);
        assertEquals("John", clinDocSection2.myPrincipalInterpreter.myFirstName);
        assertEquals("7778", clinDocSection2.mySectionId.myId);
        assertEquals("50111", clinDocSection2.mySectionCode.myCode);
        assertEquals("OR/Procedure Note2", clinDocSection2.mySectionName);
        assertEquals("F", clinDocSection2.myStatusCode);
        assertEquals("Final", clinDocSection2.myStatus);
        
        odrNotes = clinDocSection2.myNotes;
        assertEquals(3, odrNotes.size());
        assertEquals("Note1", odrNotes.get(0).myNoteText);
        assertEquals("Note2", odrNotes.get(1).myNoteText);
        assertEquals("Note3", odrNotes.get(2).myNoteText);
        
        //check ClinicalDocumentData section2(observation groups)        
        clinDocDataList = clinDocSection2.myData;
        assertEquals(3,clinDocDataList.size());        
        
        assertEquals("DT", clinDocDataList.get(0).myDataType);
        assertEquals("10017", clinDocDataList.get(0).myCode.myCode);
        assertEquals("Date Dictated", clinDocDataList.get(0).myCode.myText);
        assertEquals("F", clinDocDataList.get(0).myDataStatusCode);
        assertEquals("Final", clinDocDataList.get(0).myDataStatus);
        assertEquals("2012-01-13", clinDocDataList.get(0).myValue);
                
        assertEquals("ST", clinDocDataList.get(1).myDataType);
        assertEquals("1126527", clinDocDataList.get(1).myCode.myCode);
        assertEquals("Dictated by", clinDocDataList.get(1).myCode.myText);
        assertEquals("F", clinDocDataList.get(1).myDataStatusCode);
        assertEquals("Final", clinDocDataList.get(1).myDataStatus);
        assertEquals("John Smith, MD", clinDocDataList.get(1).myValue);

        assertEquals("NM", clinDocDataList.get(2).myDataType);
        assertEquals("14007", clinDocDataList.get(2).myCode.myCode);
        assertEquals("Pressure", clinDocDataList.get(2).myCode.myText);
        assertEquals("F", clinDocDataList.get(2).myDataStatusCode);
        assertEquals("Final", clinDocDataList.get(2).myDataStatus);
        assertEquals("0", clinDocDataList.get(2).myValue);
        assertEquals("N", clinDocDataList.get(2).myAbnormalFlagCode);
        assertEquals("Normal", clinDocDataList.get(2).myAbnormalFlagName.toString());
        assertEquals("-3 - 4", clinDocDataList.get(2).myRefRange);
        
        obsNotes = clinDocDataList.get(2).myNotes;
        assertEquals(3, obsNotes.size());
        assertEquals("Note1", obsNotes.get(0).myNoteText);
        assertEquals("Note2", obsNotes.get(1).myNoteText);
        assertEquals("Note3", obsNotes.get(2).myNoteText);        
        
        
                
    }    
       
    
    
    
    
    /**
     * Testing OruR01 with multiple orders, each one belonging to a different placer group.
     * Bypass UHNconvertor since it won't process this type of message
     *   
     * @throws Exception ...
     */
    @Test
    public void testOruR01d() throws Exception {
        
                
        String message1 = 
                "MSH|^~\\&|1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1|UHNG|||20120501103100-0500|2954864636aaa|ORU^R01^ORU_R01|31768|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r" +
                "PID|1||7005728^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR^G~00000000000^AA^^^JHN^G^^^CANON&&HL70363||Smith^Joseph^John^Junior^Mr^MD^L~Smith^Joe^^^^^A|Blanche^^^^^^L|19310313|M|||26 RIVINGTON AVE^^Goderich^CANON^N7A3Y2^CAN^H~7 WOODPLUMPTON ROAD^^Port Stanley^CANON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English^HL70296|||||||||||||||N\r" +                 
                "PV1|1|I|JS12^123^4^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.100.1||||38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|GIM|||||||38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1||284675^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^VN\r" +                 
                "ORC|1|||777^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1\r" +                
                "OBR|1|7777^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1||50111^OR/Procedure Note^1.3.6.1.4.1.12201.102.5|||20110126124300-0500|20110126125000-0500|||||||||||||||||F|7776&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1||||||5555&Smith&John&&&&Dr&&2.16.840.1.113883.4.347\r" +
                "NTE|1||Note1||\r" +
                "NTE|2||Note2||\r" +
                "NTE|3||Note3||\r" +
                "OBX|1|DT|10017^Date Dictated^1.3.6.1.4.1.12201.102.6||20120112||||||F\r" +
                "OBX|2|ST|1126527^Dictated by^1.3.6.1.4.1.12201.102.6||John Smith, MD||||||F\r" +
                "OBX|3|NM|14007^Pressure^1.3.6.1.4.1.12201.102.6||0|ml|-2 - 4|N|||F\r" +
                "NTE|1||Note1||\r" +
                "NTE|2||Note2||\r" +
                "NTE|3||Note3||\r" +
                "ORC|2|||778^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1\r" +
                "OBR|1|7778^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1||50111^OR/Procedure Note2^1.3.6.1.4.1.12201.102.5|||20110126124300-0500|20110126125000-0500|||||||||||||||||F|7776&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1||||||5555&Smith&John&&&&Dr&&2.16.840.1.113883.4.347\r" +
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
                
        ViewQuery query = new ViewQuery().viewName("allDocuments").designDocId("_design/application");
        List<ClinicalDocumentContainer> clinDocContainers = Persister.getConnector().queryView(query, ClinicalDocumentContainer.class);
        //There should only be 2 docs
        assertEquals(2,clinDocContainers.size());
        
        
        //doc1
        String id = "CDOC_1.3.6.1.4.1.12201___1.3.6.1.4.1.12201.101.1___777";
        ClinicalDocumentContainer doc1 = Persister.getConnector().get(ClinicalDocumentContainer.class, id);
        Patient doc1Patient = doc1.getDocument().myPatient;
        Visit doc1Visit = doc1.getDocument().myVisit;
        Ei doc1PlacerGroupNumber = doc1.getDocument().myPlacerGroupNumber;
        ClinicalDocumentSection doc1ClinDocSection = doc1.getDocument().mySections.get(0);
        
        //doc2
        String id2 = "CDOC_1.3.6.1.4.1.12201___1.3.6.1.4.1.12201.101.1___778";
        ClinicalDocumentContainer doc2 = Persister.getConnector().get(ClinicalDocumentContainer.class, id2);
        Patient doc2Patient = doc2.getDocument().myPatient;
        Visit doc2Visit = doc2.getDocument().myVisit;
        Ei doc2PlacerGroupNumber = doc2.getDocument().myPlacerGroupNumber;
        ClinicalDocumentSection doc2ClinDocSection = doc2.getDocument().mySections.get(0);
        
        //check doc1
        
       //check lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > doc1.getDocument().myRecordUpdatedDate.getTime());

        
        //check ClinicalDocumentGroup (patient result group)  
        //check patient
        assertEquals("M", doc1Patient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("193103130000"), doc1Patient.myDateOfBirth);
        assertEquals(null, doc1Patient.myDeathDateAndTime);
        assertEquals("N", doc1Patient.myDeathIndicator);
        assertEquals("Blanche", doc1Patient.myMothersMaidenName.myLastName);
                
        Cx patId = doc1Patient.myPatientIds.get(0);
        assertEquals("7005728", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Cx patId2 = doc1Patient.myPatientIds.get(1);
        assertEquals("00000000000", patId2.myIdNumber);
        assertEquals("CANON", patId2.myAssigningJurisdictionId);
        assertEquals("JHN", patId2.myIdTypeCode);
                
        Xpn patName = doc1Patient.myPatientNames.get(0);
        assertEquals("Smith", patName.myLastName);
        assertEquals("Joseph", patName.myFirstName);
                
        Xad patAddresses = doc1Patient.myPatientAddresses.get(0);
        assertEquals("26 RIVINGTON AVE", patAddresses.myStreetAddress);
        assertEquals("Goderich", patAddresses.myCity);
                        
        Xtn patPhone = doc1Patient.myPhoneNumbers.get(0);
        assertEquals("1 (416) 340-4800", patPhone.myPhoneNumber);
        
        Ce lang = doc1Patient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
               
        //check visit
        assertEquals("284675", doc1Visit.myVisitNumber.myIdNumber);        
        assertEquals("I", doc1Visit.myPatientClassCode);
        assertEquals(null, doc1Visit.myAdmitDate);
        assertEquals("GIM", doc1Visit.myHospitalService);
        Pl loc = doc1Visit.myAssignedPatientLocation;
        assertEquals("JS12", loc.myPointOfCare);
        assertEquals("123", loc.myRoom);
        assertEquals("4", loc.myBed);
       
        Xcn admitDoc = doc1Visit.myAdmittingDoctors.get(0);
        assertEquals("38946", admitDoc.myId);
        assertEquals("Donald", admitDoc.myFirstName);
        assertEquals("Blake", admitDoc.myLastName);
        assertEquals("Thor", admitDoc.myMiddleName);
                
        Xcn attendDoc = doc1Visit.myAttendingDoctors.get(0);
        assertEquals("38946", attendDoc.myId);
        assertEquals("Donald", attendDoc.myFirstName);
        assertEquals("Blake", attendDoc.myLastName);
        assertEquals("Thor", attendDoc.myMiddleName);
                
        Xcn refDoc = doc1Visit.myReferringDoctors.get(0);
        assertEquals("38946", refDoc.myId);
        assertEquals("Donald", refDoc.myFirstName);
        assertEquals("Blake", refDoc.myLastName);
        assertEquals("Thor", refDoc.myMiddleName);
        
        assertEquals("777", doc1PlacerGroupNumber.myId);
        assertEquals("1.3.6.1.4.1.12201", doc1PlacerGroupNumber.myFacilityId);
        assertEquals("1.3.6.1.4.1.12201.101.1", doc1PlacerGroupNumber.mySystemId);

                
        //check ClinicalDocumentSection (order observation group)
        assertEquals("NORMAL", doc1ClinDocSection.myConfidentiality.name());
        assertEquals("7776", doc1ClinDocSection.myParentSectionId.myId);
        assertEquals(ourTsLongFormat.parse("20110126124300-0500"), doc1ClinDocSection.myDate);
        assertEquals(ourTsLongFormat.parse("20110126125000-0500"), doc1ClinDocSection.myEndDate);
        assertEquals("5555", doc1ClinDocSection.myPrincipalInterpreter.myId);        
        assertEquals("Smith", doc1ClinDocSection.myPrincipalInterpreter.myLastName);
        assertEquals("John", doc1ClinDocSection.myPrincipalInterpreter.myFirstName);
        assertEquals("7777", doc1ClinDocSection.mySectionId.myId);
        assertEquals("50111", doc1ClinDocSection.mySectionCode.myCode);
        assertEquals("OR/Procedure Note", doc1ClinDocSection.mySectionName);
        assertEquals("F", doc1ClinDocSection.myStatusCode);
        assertEquals("Final", doc1ClinDocSection.myStatus);
        
        List<Note> odrNotes = doc1ClinDocSection.myNotes;
        assertEquals(3, odrNotes.size());
        assertEquals("Note1", odrNotes.get(0).myNoteText);
        assertEquals("Note2", odrNotes.get(1).myNoteText);
        assertEquals("Note3", odrNotes.get(2).myNoteText);
        
        //check ClinicalDocumentData (observation groups)        
        List<ClinicalDocumentData> clinDocDataList = doc1ClinDocSection.myData;
        assertEquals(3,clinDocDataList.size());        
        
        assertEquals("DT", clinDocDataList.get(0).myDataType);
        assertEquals("10017", clinDocDataList.get(0).myCode.myCode);
        assertEquals("Date Dictated", clinDocDataList.get(0).myCode.myText);
        assertEquals("F", clinDocDataList.get(0).myDataStatusCode);
        assertEquals("Final", clinDocDataList.get(0).myDataStatus);
        assertEquals("2012-01-12", clinDocDataList.get(0).myValue);
                
        assertEquals("ST", clinDocDataList.get(1).myDataType);
        assertEquals("1126527", clinDocDataList.get(1).myCode.myCode);
        assertEquals("Dictated by", clinDocDataList.get(1).myCode.myText);
        assertEquals("F", clinDocDataList.get(1).myDataStatusCode);
        assertEquals("Final", clinDocDataList.get(1).myDataStatus);
        assertEquals("John Smith, MD", clinDocDataList.get(1).myValue);

        assertEquals("NM", clinDocDataList.get(2).myDataType);
        assertEquals("14007", clinDocDataList.get(2).myCode.myCode);
        assertEquals("Pressure", clinDocDataList.get(2).myCode.myText);
        assertEquals("F", clinDocDataList.get(2).myDataStatusCode);
        assertEquals("Final", clinDocDataList.get(2).myDataStatus);
        assertEquals("0", clinDocDataList.get(2).myValue);
        assertEquals("N", clinDocDataList.get(2).myAbnormalFlagCode);
        assertEquals("Normal", clinDocDataList.get(2).myAbnormalFlagName.toString());
        assertEquals("-2 - 4", clinDocDataList.get(2).myRefRange);
        
        List<Note> obsNotes = clinDocDataList.get(2).myNotes;
        assertEquals(3, obsNotes.size());
        assertEquals("Note1", obsNotes.get(0).myNoteText);
        assertEquals("Note2", obsNotes.get(1).myNoteText);
        assertEquals("Note3", obsNotes.get(2).myNoteText);
        
        
        //check doc2
        
        //check lastUpdateTime
        assertTrue(nowMilli > doc2.getDocument().myRecordUpdatedDate.getTime());
        
        
        //check ClinicalDocumentGroup (patient result group)  
        //check patient
        assertEquals("M", doc2Patient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("193103130000"), doc2Patient.myDateOfBirth);
        assertEquals(null, doc2Patient.myDeathDateAndTime);
        assertEquals("N", doc2Patient.myDeathIndicator);
        assertEquals("Blanche", doc2Patient.myMothersMaidenName.myLastName);
                
        patId = doc2Patient.myPatientIds.get(0);
        assertEquals("7005728", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        patId2 = doc2Patient.myPatientIds.get(1);
        assertEquals("00000000000", patId2.myIdNumber);
        assertEquals("CANON", patId2.myAssigningJurisdictionId);
        assertEquals("JHN", patId2.myIdTypeCode);
                
        patName = doc2Patient.myPatientNames.get(0);
        assertEquals("Smith", patName.myLastName);
        assertEquals("Joseph", patName.myFirstName);
                
        patAddresses = doc2Patient.myPatientAddresses.get(0);
        assertEquals("26 RIVINGTON AVE", patAddresses.myStreetAddress);
        assertEquals("Goderich", patAddresses.myCity);
                        
        patPhone = doc2Patient.myPhoneNumbers.get(0);
        assertEquals("1 (416) 340-4800", patPhone.myPhoneNumber);
        
        lang = doc2Patient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
               
        //check visit
        assertEquals("284675", doc2Visit.myVisitNumber.myIdNumber);        
        assertEquals("I", doc2Visit.myPatientClassCode);
        assertEquals(null, doc2Visit.myAdmitDate);
        assertEquals("GIM", doc2Visit.myHospitalService);
        loc = doc2Visit.myAssignedPatientLocation;
        assertEquals("JS12", loc.myPointOfCare);
        assertEquals("123", loc.myRoom);
        assertEquals("4", loc.myBed);
       
        admitDoc = doc2Visit.myAdmittingDoctors.get(0);
        assertEquals("38946", admitDoc.myId);
        assertEquals("Donald", admitDoc.myFirstName);
        assertEquals("Blake", admitDoc.myLastName);
        assertEquals("Thor", admitDoc.myMiddleName);
                
        attendDoc = doc2Visit.myAttendingDoctors.get(0);
        assertEquals("38946", attendDoc.myId);
        assertEquals("Donald", attendDoc.myFirstName);
        assertEquals("Blake", attendDoc.myLastName);
        assertEquals("Thor", attendDoc.myMiddleName);
                
        refDoc = doc2Visit.myReferringDoctors.get(0);
        assertEquals("38946", refDoc.myId);
        assertEquals("Donald", refDoc.myFirstName);
        assertEquals("Blake", refDoc.myLastName);
        assertEquals("Thor", refDoc.myMiddleName);
        
        assertEquals("778", doc2PlacerGroupNumber.myId);
        assertEquals("1.3.6.1.4.1.12201", doc2PlacerGroupNumber.myFacilityId);
        assertEquals("1.3.6.1.4.1.12201.101.1", doc2PlacerGroupNumber.mySystemId);
                
        //check ClinicalDocumentSection (order observation group)
        assertEquals("NORMAL", doc2ClinDocSection.myConfidentiality.name());
        assertEquals("7776", doc2ClinDocSection.myParentSectionId.myId);
        assertEquals(ourTsLongFormat.parse("20110126124300-0500"), doc2ClinDocSection.myDate);
        assertEquals(ourTsLongFormat.parse("20110126125000-0500"), doc2ClinDocSection.myEndDate);
        assertEquals("5555", doc2ClinDocSection.myPrincipalInterpreter.myId);        
        assertEquals("Smith", doc2ClinDocSection.myPrincipalInterpreter.myLastName);
        assertEquals("John", doc2ClinDocSection.myPrincipalInterpreter.myFirstName);
        assertEquals("7778", doc2ClinDocSection.mySectionId.myId);
        assertEquals("50111", doc2ClinDocSection.mySectionCode.myCode);
        assertEquals("OR/Procedure Note2", doc2ClinDocSection.mySectionName);
        assertEquals("F", doc2ClinDocSection.myStatusCode);
        assertEquals("Final", doc2ClinDocSection.myStatus);
        
        odrNotes = doc2ClinDocSection.myNotes;
        assertEquals(3, odrNotes.size());
        assertEquals("Note1", odrNotes.get(0).myNoteText);
        assertEquals("Note2", odrNotes.get(1).myNoteText);
        assertEquals("Note3", odrNotes.get(2).myNoteText);
        
        //check ClinicalDocumentData (observation groups)        
        clinDocDataList = doc2ClinDocSection.myData;
        assertEquals(3,clinDocDataList.size());        
        
        assertEquals("DT", clinDocDataList.get(0).myDataType);
        assertEquals("10017", clinDocDataList.get(0).myCode.myCode);
        assertEquals("Date Dictated", clinDocDataList.get(0).myCode.myText);
        assertEquals("F", clinDocDataList.get(0).myDataStatusCode);
        assertEquals("Final", clinDocDataList.get(0).myDataStatus);
        assertEquals("2012-01-13", clinDocDataList.get(0).myValue);
                
        assertEquals("ST", clinDocDataList.get(1).myDataType);
        assertEquals("1126527", clinDocDataList.get(1).myCode.myCode);
        assertEquals("Dictated by", clinDocDataList.get(1).myCode.myText);
        assertEquals("F", clinDocDataList.get(1).myDataStatusCode);
        assertEquals("Final", clinDocDataList.get(1).myDataStatus);
        assertEquals("John Smith, MD", clinDocDataList.get(1).myValue);

        assertEquals("NM", clinDocDataList.get(2).myDataType);
        assertEquals("14007", clinDocDataList.get(2).myCode.myCode);
        assertEquals("Pressure", clinDocDataList.get(2).myCode.myText);
        assertEquals("F", clinDocDataList.get(2).myDataStatusCode);
        assertEquals("Final", clinDocDataList.get(2).myDataStatus);
        assertEquals("0", clinDocDataList.get(2).myValue);
        assertEquals("N", clinDocDataList.get(2).myAbnormalFlagCode);
        assertEquals("Normal", clinDocDataList.get(2).myAbnormalFlagName.toString());
        assertEquals("-3 - 4", clinDocDataList.get(2).myRefRange);
        
        obsNotes = clinDocDataList.get(2).myNotes;
        assertEquals(3, obsNotes.size());
        assertEquals("Note1", obsNotes.get(0).myNoteText);
        assertEquals("Note2", obsNotes.get(1).myNoteText);
        assertEquals("Note3", obsNotes.get(2).myNoteText);        
        
        
                
    }
    
    /**
     * Make sure that a second version of a document (i.e. with the same ID) overwrites the existing one
     * when we're in snapshot mode, which is the default
     */
    @Test
    public void testOruUpdateSnapshotMode() throws Exception {
    	
        String message1 = 
                "MSH|^~\\&|EPR|UHNG|||201205011031||ORU^R01^ORU_R01|31768|T|2.5\r" +
                "PID|1||7005728^^^UHN^MR^G^4265~00000000000^AA^^CANON^JHN^G||Smith^Joseph^John^Junior^Mr^MD^L~Smith^Joe^^^^^A|Blanche^^^^^^L|19310313|M|||26 RIVINGTON AVE^^Goderich^ON^N7A3Y2^CAN^H~7 WOODPLUMPTON ROAD^^Port Stanley^ON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English|||||||||||||||N\r" +                 
                "PV1|1|I|JS12^123^4^G^^Q Building^12^JS12-123-4 GIM Inpatient Unit||||38946^Blake^Donald^Thor^^^^^|38946^Blake^Donald^Thor^^^^^|38946^Blake^Donald^Thor^^^^^|GIM|||||||38946^Blake^Donald^Thor^^^^^||284675^^^2.16.840.1.113883.3.59.3:0947^VN^G\r" +                 
                "ORC|1|7777^EPR^2.16.840.1.113883.3.59.3:947|||CM\r" +                
                "OBR|1|7777^EPR^2.16.840.1.113883.3.59.3:947||50111^OR/Procedure Note^HL70396|||20110126124300-0500|20110126125000-0500||||||||||N|||||||F|7776&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.1||||||5555&Smith&John&&&&Dr&&2.16.840.1.113883.4.347\r" +
                "NTE|1||Note1||\r" +
                "NTE|2||Note2||\r" +
                "OBX|1|ST|WholeReport^Whole Report^HL70396||line one||||||F\r" + 
                "OBX|2|ST|WholeReport^Whole Report^HL70396||line two||||||F\r" + 
                "NTE|1||Note1||\r" +
                "NTE|2||Note2||\r";
        
        List<ClinicalDocumentGroup> clinDocs = UhnConverter.convertOru(message1);
        Persister.persist(clinDocs);
                
        ViewQuery query = new ViewQuery().viewName("allDocuments").designDocId("_design/application");
        List<ClinicalDocumentContainer> clinDocContainers = Persister.getConnector().queryView(query, ClinicalDocumentContainer.class);
        
        ClinicalDocumentContainer clinDocContainer = clinDocContainers.get(0);
        ClinicalDocumentSection clinDocSection = clinDocContainer.getDocument().mySections.get(0);
        
        assertEquals("line one<br>line two", clinDocSection.myData.get(0).myValue);

        // Now send again with new contents
        
        message1 = 
                "MSH|^~\\&|EPR|UHNG|||201205011031||ORU^R01^ORU_R01|31768|T|2.5\r" +
                "PID|1||7005728^^^UHN^MR^G^4265~00000000000^AA^^CANON^JHN^G||Smith^Joseph^John^Junior^Mr^MD^L~Smith^Joe^^^^^A|Blanche^^^^^^L|19310313|M|||26 RIVINGTON AVE^^Goderich^ON^N7A3Y2^CAN^H~7 WOODPLUMPTON ROAD^^Port Stanley^ON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English|||||||||||||||N\r" +                 
                "PV1|1|I|JS12^123^4^G^^Q Building^12^JS12-123-4 GIM Inpatient Unit||||38946^Blake^Donald^Thor^^^^^|38946^Blake^Donald^Thor^^^^^|38946^Blake^Donald^Thor^^^^^|GIM|||||||38946^Blake^Donald^Thor^^^^^||284675^^^2.16.840.1.113883.3.59.3:0947^VN^G\r" +                 
                "ORC|1|7777^EPR^2.16.840.1.113883.3.59.3:947|||CM\r" +                
                "OBR|1|7777^EPR^2.16.840.1.113883.3.59.3:947||50111^OR/Procedure Note^HL70396|||20110126124300-0500|20110126125000-0500||||||||||N|||||||F|7776&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.1||||||5555&Smith&John&&&&Dr&&2.16.840.1.113883.4.347\r" +
                "NTE|1||Note1||\r" +
                "NTE|2||Note2||\r" +
                "OBX|1|ST|WholeReport^Whole Report^HL70396||line three||||||F\r" + 
                "OBX|2|ST|WholeReport^Whole Report^HL70396||line four||||||F\r" + 
                "NTE|1||Note1||\r" +
                "NTE|2||Note2||\r";
        
        clinDocs = UhnConverter.convertOru(message1);
        Persister.persist(clinDocs);
                
        query = new ViewQuery().viewName("allDocuments").designDocId("_design/application");
        clinDocContainers = Persister.getConnector().queryView(query, ClinicalDocumentContainer.class);
        
        clinDocContainer = clinDocContainers.get(0);
        clinDocSection = clinDocContainer.getDocument().mySections.get(0);
        
        assertEquals("line three<br>line four", clinDocSection.myData.get(0).myValue);
        
    }
	
    /**
     * Make sure that a second version of a document (i.e. with the same ID) overwrites the existing one
     * when we're in append mode (OBR-19)
     */
    @Test
    public void testOruUpdateAppendMode() throws Exception {
    	
        String message1 = 
                "MSH|^~\\&|EPR|UHNG|||201205011031||ORU^R01^ORU_R01|31768|T|2.5\r" +
                "PID|1||7005728^^^UHN^MR^G^4265~00000000000^AA^^CANON^JHN^G||Smith^Joseph^John^Junior^Mr^MD^L~Smith^Joe^^^^^A|Blanche^^^^^^L|19310313|M|||26 RIVINGTON AVE^^Goderich^ON^N7A3Y2^CAN^H~7 WOODPLUMPTON ROAD^^Port Stanley^ON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English|||||||||||||||N\r" +                 
                "PV1|1|I|JS12^123^4^G^^Q Building^12^JS12-123-4 GIM Inpatient Unit||||38946^Blake^Donald^Thor^^^^^|38946^Blake^Donald^Thor^^^^^|38946^Blake^Donald^Thor^^^^^|GIM|||||||38946^Blake^Donald^Thor^^^^^||284675^^^2.16.840.1.113883.3.59.3:0947^VN^G\r" +                 
                "ORC|1|7777^EPR^2.16.840.1.113883.3.59.3:947|||CM\r" +                
                "OBR|1|7777^EPR^2.16.840.1.113883.3.59.3:947||50111^OR/Procedure Note^HL70396|||20110126124300-0500|20110126125000-0500||||||||||N|A||||||F|7776&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.1||||||5555&Smith&John&&&&Dr&&2.16.840.1.113883.4.347||A\r" +
                "NTE|1||Note1||\r" +
                "NTE|2||Note2||\r" +
                "OBX|1|ST|WholeReport^Whole Report^HL70396||line one||||||F\r" + 
                "OBX|2|ST|WholeReport^Whole Report^HL70396||line two||||||F\r" + 
                "NTE|1||Note1||\r" +
                "NTE|2||Note2||\r";
        
        List<ClinicalDocumentGroup> clinDocs = UhnConverter.convertOru(message1);
        Persister.persist(clinDocs);
                
        ViewQuery query = new ViewQuery().viewName("allDocuments").designDocId("_design/application");
        List<ClinicalDocumentContainer> clinDocContainers = Persister.getConnector().queryView(query, ClinicalDocumentContainer.class);
        
        ClinicalDocumentContainer clinDocContainer = clinDocContainers.get(0);
        ClinicalDocumentSection clinDocSection = clinDocContainer.getDocument().mySections.get(0);
        
        assertEquals("line one<br>line two", clinDocSection.myData.get(0).myValue);

        // Now send again with new contents
        
        message1 = 
                "MSH|^~\\&|EPR|UHNG|||201205011031||ORU^R01^ORU_R01|31768|T|2.5\r" +
                "PID|1||7005728^^^UHN^MR^G^4265~00000000000^AA^^CANON^JHN^G||Smith^Joseph^John^Junior^Mr^MD^L~Smith^Joe^^^^^A|Blanche^^^^^^L|19310313|M|||26 RIVINGTON AVE^^Goderich^ON^N7A3Y2^CAN^H~7 WOODPLUMPTON ROAD^^Port Stanley^ON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English|||||||||||||||N\r" +                 
                "PV1|1|I|JS12^123^4^G^^Q Building^12^JS12-123-4 GIM Inpatient Unit||||38946^Blake^Donald^Thor^^^^^|38946^Blake^Donald^Thor^^^^^|38946^Blake^Donald^Thor^^^^^|GIM|||||||38946^Blake^Donald^Thor^^^^^||284675^^^2.16.840.1.113883.3.59.3:0947^VN^G\r" +                 
                "ORC|1|7777^EPR^2.16.840.1.113883.3.59.3:947|||CM\r" +                
                "OBR|1|7777^EPR^2.16.840.1.113883.3.59.3:947||50111^OR/Procedure Note^HL70396|||20110126124300-0500|20110126125000-0500||||||||||N|A||||||F|7776&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.1||||||5555&Smith&John&&&&Dr&&2.16.840.1.113883.4.347||A\r" +
                "NTE|1||Note1||\r" +
                "NTE|2||Note2||\r" +
                "OBX|1|ST|WholeReport^Whole Report^HL70396||line three||||||F\r" + 
                "OBX|2|ST|WholeReport^Whole Report^HL70396||line four||||||F\r" + 
                "NTE|1||Note1||\r" +
                "NTE|2||Note2||\r";
        
        clinDocs = UhnConverter.convertOru(message1);
        Persister.persist(clinDocs);
                
        query = new ViewQuery().viewName("allDocuments").designDocId("_design/application");
        clinDocContainers = Persister.getConnector().queryView(query, ClinicalDocumentContainer.class);
        
        clinDocContainer = clinDocContainers.get(0);
        clinDocSection = clinDocContainer.getDocument().mySections.get(0);
        
        assertEquals("line one<br>line two", clinDocSection.myData.get(0).myValue);
        assertEquals("line three<br>line four", clinDocSection.myData.get(1).myValue);
        
    }
	    
    /**
     * 
     * @param theEncodedString
     * @return ...
     */
    private String createEdObxSegments(String theEncodedString) {
        
        String[] lines = theEncodedString.split("\n");
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < lines.length; i++) {
            int seq = 4 + i;
            buf.append("OBX|"+ seq + "|ED|14009^Picture^HL70396||^NS^GIF^Base64^"+lines[i]+"||||||F\r");                        
        }
        return buf.toString();
        
    }



    @After
    public void after() {
        Persister.setUnitTestMode(false);
    } 	

}
