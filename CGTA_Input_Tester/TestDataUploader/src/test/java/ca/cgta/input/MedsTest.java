package ca.cgta.input;

import static org.junit.Assert.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ektorp.ViewQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ca.cgta.couchdb.tools.AllDataPurger;
import ca.cgta.couchdb.tools.ViewUploader;
import ca.cgta.input.converter.Converter;
import ca.cgta.input.listener.Persister;
import ca.cgta.input.model.inner.Ce;
import ca.cgta.input.model.inner.Cx;
import ca.cgta.input.model.inner.MedicationAdmin;
import ca.cgta.input.model.inner.MedicationComponent;
import ca.cgta.input.model.inner.MedicationOrder;
import ca.cgta.input.model.inner.Note;
import ca.cgta.input.model.inner.Patient;
import ca.cgta.input.model.inner.Pl;
import ca.cgta.input.model.inner.Visit;
import ca.cgta.input.model.inner.Xad;
import ca.cgta.input.model.inner.Xcn;
import ca.cgta.input.model.inner.Xpn;
import ca.cgta.input.model.inner.Xtn;
import ca.cgta.input.model.outer.MedicationOrderWithAdmins;
import ca.cgta.input.model.outer.MedicationOrderWithAdminsContainer;
import ca.uhn.hl7v2.model.v25.message.RAS_O17;
import ca.uhn.hl7v2.model.v25.message.RDE_O11;
import ca.uhn.hl7v2.parser.PipeParser;

public class MedsTest {
    
    
    private static DateFormat ourTsFormat = new SimpleDateFormat("yyyyMMddHHmm");
//    private static DateFormat ourTsLongFormat = new SimpleDateFormat("yyyyMMddHHmmssZ");
    private static DateFormat ourTsMinFormatWOffset = new SimpleDateFormat("yyyyMMddHHmmZ");
//    private static DateFormat ourTsYearFormat = new SimpleDateFormat("yyyy");
//    private static DateFormat ourTsMonthFormat = new SimpleDateFormat("yyyyMM");
//    private static DateFormat ourTsSecFormat = new SimpleDateFormat("yyyyMMddHHmmss");
//    private static DateFormat ourDtFormat = new SimpleDateFormat("yyyyMMdd");

	@Before
	public void before() throws IOException, Exception {
		Persister.setUnitTestMode(true);
		AllDataPurger.purgeAllData(); 
		ViewUploader.uploadAllViews();
		
    	Logger.getLogger("").setLevel(Level.FINEST);

	}
	
	
	
    /**
     * Testing RDE^O11 containing single order group 
     *   
     * @throws Exception ...
     */
    @Test
    public void testRdeO11a() throws Exception {
        
        
                
        String message1 = 
                "MSH|^~\\&|1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1|UHNG|||20120501103100-0500|2954864636aaa|RDE^O11^RDE_O11|31768|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r" +
                "PID|1||7005728^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR^G~00000000000^AA^^^JHN^G^^^CANON&&HL70363||Smith^Joseph^John^Junior^Mr^MD^L~Smith^Joe^^^^^A|Blanche^^^^^^L|19310313000000-0500|M|||26 RIVINGTON AVE^^Goderich^CANON^N7A3Y2^CAN^H~7 WOODPLUMPTON ROAD^^Port Stanley^CANON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English^HL70296|||||||||||||||N\r" +                 
                "PV1|1|I|JS12^123^4^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.100.1||||38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|GIM|||||||38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1||284675^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^VN\r" +                 
                "ORC|OK|7777^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1||777^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1|\r" +
                "RXE|3^Q24H^INDEF^201204181000-0400^201205221000-0400|QUE25T^quetiapine tab 25 mg^1.3.6.1.4.1.12201.102.4|1|2|MG^milligram|INJ^Injection|^Hold PCA if patient confused or loses IV access~^Hold PCA if patient loses IV access\r" +                
                "NTE|1||Note1||\r" +
                "NTE|2||Note2||\r" +
                "NTE|3||Note3||\r" +
                "RXR|^Intravenous||\r" +
                "RXR|INJ^Injection||||||\r" +
                "RXC|B|AMI150I^amiodarone inj 50 mg per 1mL^1.3.6.1.4.1.12201.102.3|50|MG^mg|\r" +
                "RXC|A|AMI160I^amiodarone inj 60 mg per 1mL^1.3.6.1.4.1.12201.102.3|60|MG^mg|\r";
        
        
        
        Converter c = new Converter();
        RDE_O11 input = new RDE_O11();
        input.setParser(PipeParser.getInstanceWithNoValidation());
        input.parse(message1);
        List<MedicationOrder> medOrders = c.convertMedicationOrder(input);        
        Persister.persistMedicationOrders(medOrders);
                
        ViewQuery query = new ViewQuery().viewName("allView").designDocId("_design/application");
        List<MedicationOrderWithAdminsContainer> medOrderWithAdminsContainers = Persister.getConnector().queryView(query, MedicationOrderWithAdminsContainer.class);
        assertEquals(1,medOrderWithAdminsContainers.size());
        
        Patient patient = medOrderWithAdminsContainers.get(0).getDocument().myOrder.myPatient;
        Visit visit = medOrderWithAdminsContainers.get(0).getDocument().myOrder.myVisit;
        MedicationOrder order =  medOrderWithAdminsContainers.get(0).getDocument().myOrder;
        
        
        //check lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > medOrderWithAdminsContainers.get(0).getDocument().myRecordUpdatedDate.getTime());
       
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

        //check med order
        assertEquals("777", order.myPlacerGroupNumber.myId);
        assertEquals("1.3.6.1.4.1.12201", order.myPlacerGroupNumber.myFacilityId);
        assertEquals("1.3.6.1.4.1.12201.101.1", order.myPlacerGroupNumber.mySystemId);
        assertEquals("7777", order.myPlacerOrderNumber.myId);
        assertEquals("1.3.6.1.4.1.12201", order.myPlacerOrderNumber.myFacilityId);
        assertEquals("1.3.6.1.4.1.12201.101.1", order.myPlacerOrderNumber.mySystemId);
        assertEquals("OK", order.myStatusCode);
        assertEquals("Order verified", order.myStatusName);
        
        assertEquals(3, order.myEncodedOrderQuantityNumber);
        assertEquals("Q24H", order.myEncodedOrderQuantityRepeatPattern);
        assertEquals(ourTsMinFormatWOffset.parse("201204181000-0400"), order.myEncodedOrderQuantityStartTime);
        assertEquals(ourTsMinFormatWOffset.parse("201205221000-0400"), order.myEncodedOrderQuantityEndTime);
        assertEquals("quetiapine tab 25 mg", order.myEncodedOrderGiveCode.myText);
        assertEquals(1.0, order.myEncodedOrderGiveMinimum, 0.00001);
        assertEquals(2.0, order.myEncodedOrderGiveMaximum, 0.00001);
        assertEquals("MG", order.myEncodedOrderGiveUnits.myCode);
        assertEquals("INJ", order.myEncodedOrderGiveDosageForm.myCode);
        assertEquals("Hold PCA if patient confused or loses IV access", order.myEncodedOrderProvidersAdministrationInstructions.get(0).myText);
        assertEquals("Hold PCA if patient loses IV access", order.myEncodedOrderProvidersAdministrationInstructions.get(1).myText);
        
        List<Note> notes = order.myNotes;
        assertEquals(3, notes.size());
        assertEquals("Note1", notes.get(0).myNoteText);
        assertEquals("Note2", notes.get(1).myNoteText);
        assertEquals("Note3", notes.get(2).myNoteText);
        
        
        List<Ce> routes = order.myMedicationRoutes;
        assertEquals(2, routes.size());
        assertEquals("Intravenous", routes.get(0).myText);
        assertEquals("Injection", routes.get(1).myText);
                
 
        List<MedicationComponent> components = order.myMedicationComponents;
        assertEquals(2, components.size());
        assertEquals("B", components.get(0).myComponentType);
        assertEquals("AMI150I", components.get(0).myComponentCode.myCode);
        assertEquals("amiodarone inj 50 mg per 1mL", components.get(0).myComponentCode.myText);
        assertEquals(50, components.get(0).myComponentAmount, 0);
        assertEquals("MG", components.get(0).myComponentUnits.myCode);
        assertEquals("mg", components.get(0).myComponentUnits.myText);
        
        assertEquals("A", components.get(1).myComponentType);
        assertEquals("AMI160I", components.get(1).myComponentCode.myCode);
        assertEquals("amiodarone inj 60 mg per 1mL", components.get(1).myComponentCode.myText);
        assertEquals(60, components.get(1).myComponentAmount, 0);
        assertEquals("MG", components.get(1).myComponentUnits.myCode);
        assertEquals("mg", components.get(1).myComponentUnits.myText);
                
        

                
    }
    
    
    
    
    /**
     * Testing RDE^O11 containing multiple order groups 
     *   
     * @throws Exception ...
     */
    @Test
    public void testRdeO11b() throws Exception {
        
        
                
        String message1 = 
                "MSH|^~\\&|1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1|UHNG|||20120501103100-0500|2954864636aaa|RDE^O11^RDE_O11|31768|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r" +
                "PID|1||7005728^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR^G~00000000000^AA^^^JHN^G^^^CANON&&HL70363||Smith^Joseph^John^Junior^Mr^MD^L~Smith^Joe^^^^^A|Blanche^^^^^^L|19310313000000-0500|M|||26 RIVINGTON AVE^^Goderich^CANON^N7A3Y2^CAN^H~7 WOODPLUMPTON ROAD^^Port Stanley^CANON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English^HL70296|||||||||||||||N\r" +                 
                "PV1|1|I|JS12^123^4^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.100.1||||38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|GIM|||||||38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1||284675^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^VN\r" +                 
                "ORC|OK|7777^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1||777^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1|\r" +
                "RXE|3^Q24H^INDEF^201204181000-0400^201205221000-0400|QUE25T^quetiapine tab 25 mg^1.3.6.1.4.1.12201.102.4|1|2|MG^milligram|INJ^Injection|^Hold PCA if patient confused or loses IV access~^Hold PCA if patient loses IV access\r" +                
                "NTE|1||Note1||\r" +
                "NTE|2||Note2||\r" +
                "NTE|3||Note3||\r" +
                "RXR|^Intravenous||\r" +
                "RXR|INJ^Injection||||||\r" +
                "RXC|B|AMI150I^amiodarone inj 50 mg per 1mL^1.3.6.1.4.1.12201.102.3|50|MG^mg|\r" +
                "RXC|A|AMI160I^amiodarone inj 60 mg per 1mL^1.3.6.1.4.1.12201.102.3|60|MG^mg|\r" +
                "ORC|OK|7778^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1||777^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1|\r" +
                "RXE|4^Q24H^INDEF^201204181000-0400^201205221000-0400|QUE50T^quetiapine tab 50 mg^1.3.6.1.4.1.12201.102.4|1|2|MG^milligram|INJ^Injection|^Hold PCA if patient confused or loses IV access~^Hold PCA if patient loses IV access\r" +                
                "NTE|1||Note1||\r" +
                "NTE|2||Note2||\r" +
                "NTE|3||Note3||\r" +
                "RXR|^Intravenous||\r" +
                "RXR|INJ^Injection||||||\r" +
                "RXC|B|AMI170I^amiodarone inj 70 mg per 1mL^1.3.6.1.4.1.12201.102.3|70|MG^mg|\r" +
                "RXC|A|AMI180I^amiodarone inj 80 mg per 1mL^1.3.6.1.4.1.12201.102.3|80|MG^mg|\r";
                
                
                
        Converter c = new Converter();
        RDE_O11 input = new RDE_O11();
        input.setParser(PipeParser.getInstanceWithNoValidation());
        input.parse(message1);
        List<MedicationOrder> medOrders = c.convertMedicationOrder(input);        
        Persister.persistMedicationOrders(medOrders);
                
        ViewQuery query = new ViewQuery().viewName("allView").designDocId("_design/application");
        List<MedicationOrderWithAdminsContainer> medOrderWithAdminsContainers = Persister.getConnector().queryView(query, MedicationOrderWithAdminsContainer.class);
        assertEquals(2,medOrderWithAdminsContainers.size());
        
        
        //med order 1
        String id = "MEDORDER_1.3.6.1.4.1.12201___1.3.6.1.4.1.12201.101.1___7777";
        MedicationOrderWithAdminsContainer doc1 = Persister.getConnector().get(MedicationOrderWithAdminsContainer.class, id);
        Patient doc1Patient = doc1.getDocument().myOrder.myPatient;
        Visit doc1Visit = doc1.getDocument().myOrder.myVisit;
        MedicationOrder doc1Order =  doc1.getDocument().myOrder;
        
        //med order 2
        String id2 = "MEDORDER_1.3.6.1.4.1.12201___1.3.6.1.4.1.12201.101.1___7778";
        MedicationOrderWithAdminsContainer doc2 = Persister.getConnector().get(MedicationOrderWithAdminsContainer.class, id2);
        Patient doc2Patient = doc2.getDocument().myOrder.myPatient;
        Visit doc2Visit = doc2.getDocument().myOrder.myVisit;
        MedicationOrder doc2Order =  doc2.getDocument().myOrder;
                
        
        //check med order1 data
        
        //check lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > doc1.getDocument().myRecordUpdatedDate.getTime());
        
               
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

        //check med order
        assertEquals("777", doc1Order.myPlacerGroupNumber.myId);
        assertEquals("1.3.6.1.4.1.12201", doc1Order.myPlacerGroupNumber.myFacilityId);
        assertEquals("1.3.6.1.4.1.12201.101.1", doc1Order.myPlacerGroupNumber.mySystemId);
        assertEquals("7777", doc1Order.myPlacerOrderNumber.myId);
        assertEquals("1.3.6.1.4.1.12201", doc1Order.myPlacerOrderNumber.myFacilityId);
        assertEquals("1.3.6.1.4.1.12201.101.1", doc1Order.myPlacerOrderNumber.mySystemId);
        assertEquals("OK", doc1Order.myStatusCode);
        assertEquals("Order verified", doc1Order.myStatusName);
        
        assertEquals(3, doc1Order.myEncodedOrderQuantityNumber);
        assertEquals("Q24H", doc1Order.myEncodedOrderQuantityRepeatPattern);
        assertEquals(ourTsMinFormatWOffset.parse("201204181000-0400"), doc1Order.myEncodedOrderQuantityStartTime);
        assertEquals(ourTsMinFormatWOffset.parse("201205221000-0400"), doc1Order.myEncodedOrderQuantityEndTime);
        assertEquals("quetiapine tab 25 mg", doc1Order.myEncodedOrderGiveCode.myText);
        assertEquals(1, doc1Order.myEncodedOrderGiveMinimum, 0.00001);
        assertEquals(2, doc1Order.myEncodedOrderGiveMaximum, 0.00001);
        assertEquals("MG", doc1Order.myEncodedOrderGiveUnits.myCode);
        assertEquals("INJ", doc1Order.myEncodedOrderGiveDosageForm.myCode);
        assertEquals("Hold PCA if patient confused or loses IV access", doc1Order.myEncodedOrderProvidersAdministrationInstructions.get(0).myText);
        assertEquals("Hold PCA if patient loses IV access", doc1Order.myEncodedOrderProvidersAdministrationInstructions.get(1).myText);
        
        List<Note> notes = doc1Order.myNotes;
        assertEquals(3, notes.size());
        assertEquals("Note1", notes.get(0).myNoteText);
        assertEquals("Note2", notes.get(1).myNoteText);
        assertEquals("Note3", notes.get(2).myNoteText);
        
        
        List<Ce> routes = doc1Order.myMedicationRoutes;
        assertEquals(2, routes.size());
        assertEquals("Intravenous", routes.get(0).myText);
        assertEquals("Injection", routes.get(1).myText);
                
 
        List<MedicationComponent> components = doc1Order.myMedicationComponents;
        assertEquals(2, components.size());
        assertEquals("B", components.get(0).myComponentType);
        assertEquals("AMI150I", components.get(0).myComponentCode.myCode);
        assertEquals("amiodarone inj 50 mg per 1mL", components.get(0).myComponentCode.myText);
        assertEquals(50, components.get(0).myComponentAmount, 0);
        assertEquals("MG", components.get(0).myComponentUnits.myCode);
        assertEquals("mg", components.get(0).myComponentUnits.myText);
        
        assertEquals("A", components.get(1).myComponentType);
        assertEquals("AMI160I", components.get(1).myComponentCode.myCode);
        assertEquals("amiodarone inj 60 mg per 1mL", components.get(1).myComponentCode.myText);
        assertEquals(60, components.get(1).myComponentAmount, 0);
        assertEquals("MG", components.get(1).myComponentUnits.myCode);
        assertEquals("mg", components.get(1).myComponentUnits.myText);                
                

        //check med order2 data
        
        //check lastUpdateTime
        assertTrue(nowMilli > doc2.getDocument().myRecordUpdatedDate.getTime());
        
               
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

        //check med order
        assertEquals("777", doc2Order.myPlacerGroupNumber.myId);
        assertEquals("1.3.6.1.4.1.12201", doc2Order.myPlacerGroupNumber.myFacilityId);
        assertEquals("1.3.6.1.4.1.12201.101.1", doc2Order.myPlacerGroupNumber.mySystemId);
        assertEquals("7778", doc2Order.myPlacerOrderNumber.myId);
        assertEquals("1.3.6.1.4.1.12201", doc2Order.myPlacerOrderNumber.myFacilityId);
        assertEquals("1.3.6.1.4.1.12201.101.1", doc2Order.myPlacerOrderNumber.mySystemId);
        assertEquals("OK", doc2Order.myStatusCode);
        assertEquals("Order verified", doc2Order.myStatusName);
        
        assertEquals(4, doc2Order.myEncodedOrderQuantityNumber);
        assertEquals("Q24H", doc2Order.myEncodedOrderQuantityRepeatPattern);
        assertEquals(ourTsMinFormatWOffset.parse("201204181000-0400"), doc2Order.myEncodedOrderQuantityStartTime);
        assertEquals(ourTsMinFormatWOffset.parse("201205221000-0400"), doc2Order.myEncodedOrderQuantityEndTime);
        assertEquals("quetiapine tab 50 mg", doc2Order.myEncodedOrderGiveCode.myText);
        assertEquals(1, doc2Order.myEncodedOrderGiveMinimum, 0.00001);
        assertEquals(2, doc2Order.myEncodedOrderGiveMaximum, 0.00001);
        assertEquals("MG", doc2Order.myEncodedOrderGiveUnits.myCode);
        assertEquals("INJ", doc2Order.myEncodedOrderGiveDosageForm.myCode);
        assertEquals("Hold PCA if patient confused or loses IV access", doc2Order.myEncodedOrderProvidersAdministrationInstructions.get(0).myText);
        assertEquals("Hold PCA if patient loses IV access", doc2Order.myEncodedOrderProvidersAdministrationInstructions.get(1).myText);
        
        notes = doc2Order.myNotes;
        assertEquals(3, notes.size());
        assertEquals("Note1", notes.get(0).myNoteText);
        assertEquals("Note2", notes.get(1).myNoteText);
        assertEquals("Note3", notes.get(2).myNoteText);
        
        
        routes = doc2Order.myMedicationRoutes;
        assertEquals(2, routes.size());
        assertEquals("Intravenous", routes.get(0).myText);
        assertEquals("Injection", routes.get(1).myText);
                
 
        components = doc2Order.myMedicationComponents;
        assertEquals(2, components.size());
        assertEquals("B", components.get(0).myComponentType);
        assertEquals("AMI170I", components.get(0).myComponentCode.myCode);
        assertEquals("amiodarone inj 70 mg per 1mL", components.get(0).myComponentCode.myText);
        assertEquals(70, components.get(0).myComponentAmount, 0);
        assertEquals("MG", components.get(0).myComponentUnits.myCode);
        assertEquals("mg", components.get(0).myComponentUnits.myText);
        
        assertEquals("A", components.get(1).myComponentType);
        assertEquals("AMI180I", components.get(1).myComponentCode.myCode);
        assertEquals("amiodarone inj 80 mg per 1mL", components.get(1).myComponentCode.myText);
        assertEquals(80, components.get(1).myComponentAmount, 0);
        assertEquals("MG", components.get(1).myComponentUnits.myCode);
        assertEquals("mg", components.get(1).myComponentUnits.myText);                
                



                
    }    
    
    
    
    
    /**
     * Testing RAS^O17 containing single order group 
     *   
     * @throws Exception ...
     */
    @Test
    public void testRas017a() throws Exception {
        
        
                
        String message1 = 
                "MSH|^~\\&|1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1|UHNG|||20120501103100-0500|2954864636aaa|RAS^O17^RAS_O17|31768|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r" +
                "PID|1||7005728^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR^G~00000000000^AA^^^JHN^G^^^CANON&&HL70363||Smith^Joseph^John^Junior^Mr^MD^L~Smith^Joe^^^^^A|Blanche^^^^^^L|19310313000000-0500|M|||26 RIVINGTON AVE^^Goderich^CANON^N7A3Y2^CAN^H~7 WOODPLUMPTON ROAD^^Port Stanley^CANON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English^HL70296|||||||||||||||N\r" +                 
                "PV1|1|I|JS12^123^4^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.100.1||||38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|GIM|||||||38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1||284675^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^VN\r" +                 
                "ORC|OK|7777^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1||777^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1|\r" +
                "RXE|3^Q24H^INDEF^201204181000-0400^201205221000-0400|QUE25T^quetiapine tab 25 mg^1.3.6.1.4.1.12201.102.4|1|2|MG^milligram|INJ^Injection|^Hold PCA if patient confused or loses IV access~^Hold PCA if patient loses IV access\r" +                
                "RXR|^Intravenous||\r" +
                "RXR|INJ^Injection||||||\r" +
                "RXC|B|AMI150I^amiodarone inj 50 mg per 1mL^1.3.6.1.4.1.12201.102.3|50|MG^mg|\r" +
                "RXC|A|AMI160I^amiodarone inj 60 mg per 1mL^1.3.6.1.4.1.12201.102.3|60|MG^mg|\r" +
                "RXA||1|201204250101-0400|201204250201-0400|NITRO^Nitroglycerine^1.3.6.1.4.1.12201.102.2|100|mg||D5WGB^100 mg bottle~^premixed solution|||100mg/min\r" +
                "RXA||2|201204250201-0400|201204250301-0400|NITRO^Nitroglycerine^1.3.6.1.4.1.12201.102.2|100|mg||D5WGB^100 mg bottle~^premixed solution|||100mg/min\r" +
                "RXR|INJ^Injection||||||\r" +
                "RXA||1|201204250101-0400|201204250201-0400|^fish oil|200|mg||D5WGB^200 mg bottle~^premixed solution|||200mg/min\r" +
                "RXA||2|201204250201-0400|201204250301-0400|^fish oil|200|mg||D5WGB^200 mg bottle~^premixed solution|||200mg/min\r" +
                "RXR|^Intravenous||||||\r";
        
        
        
        Converter c = new Converter();
        RAS_O17 input = new RAS_O17();
        input.setParser(PipeParser.getInstanceWithNoValidation());
        input.parse(message1);
        List<MedicationOrderWithAdmins> medOrders = c.convertMedicationAdmin(input);       
        Persister.persistMedicationAdmins(medOrders);
                
        ViewQuery query = new ViewQuery().viewName("allView").designDocId("_design/application");
        List<MedicationOrderWithAdminsContainer> medOrderWithAdminsContainers = Persister.getConnector().queryView(query, MedicationOrderWithAdminsContainer.class);
        assertEquals(1,medOrderWithAdminsContainers.size());
        
        Patient patient = medOrderWithAdminsContainers.get(0).getDocument().myOrder.myPatient;
        Visit visit = medOrderWithAdminsContainers.get(0).getDocument().myOrder.myVisit;
        MedicationOrder order =  medOrderWithAdminsContainers.get(0).getDocument().myOrder;
        assertEquals(4, medOrderWithAdminsContainers.get(0).getDocument().myAdmins.size()); 
        MedicationAdmin admin1 = medOrderWithAdminsContainers.get(0).getDocument().myAdmins.get(0);
        MedicationAdmin admin2 = medOrderWithAdminsContainers.get(0).getDocument().myAdmins.get(1);
        MedicationAdmin admin3 = medOrderWithAdminsContainers.get(0).getDocument().myAdmins.get(2);
        MedicationAdmin admin4 = medOrderWithAdminsContainers.get(0).getDocument().myAdmins.get(3);
        
        
        //check lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > medOrderWithAdminsContainers.get(0).getDocument().myRecordUpdatedDate.getTime());
        
       
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

        //check med order
        assertEquals("777", order.myPlacerGroupNumber.myId);
        assertEquals("1.3.6.1.4.1.12201", order.myPlacerGroupNumber.myFacilityId);
        assertEquals("1.3.6.1.4.1.12201.101.1", order.myPlacerGroupNumber.mySystemId);
        assertEquals("7777", order.myPlacerOrderNumber.myId);
        assertEquals("1.3.6.1.4.1.12201", order.myPlacerOrderNumber.myFacilityId);
        assertEquals("1.3.6.1.4.1.12201.101.1", order.myPlacerOrderNumber.mySystemId);
        assertEquals("OK", order.myStatusCode);
        assertEquals("Order verified", order.myStatusName);
        
        assertEquals(3, order.myEncodedOrderQuantityNumber);
        assertEquals("Q24H", order.myEncodedOrderQuantityRepeatPattern);
        assertEquals(ourTsMinFormatWOffset.parse("201204181000-0400"), order.myEncodedOrderQuantityStartTime);
        assertEquals(ourTsMinFormatWOffset.parse("201205221000-0400"), order.myEncodedOrderQuantityEndTime);
        assertEquals("quetiapine tab 25 mg", order.myEncodedOrderGiveCode.myText);
        assertEquals(1, order.myEncodedOrderGiveMinimum, 0.00001);
        assertEquals(2, order.myEncodedOrderGiveMaximum, 0.00001);
        assertEquals("MG", order.myEncodedOrderGiveUnits.myCode);
        assertEquals("INJ", order.myEncodedOrderGiveDosageForm.myCode);
        assertEquals("Hold PCA if patient confused or loses IV access", order.myEncodedOrderProvidersAdministrationInstructions.get(0).myText);
        assertEquals("Hold PCA if patient loses IV access", order.myEncodedOrderProvidersAdministrationInstructions.get(1).myText);
        
        
        List<Ce> routes = order.myMedicationRoutes;
        assertEquals(2, routes.size());
        assertEquals("Intravenous", routes.get(0).myText);
        assertEquals("Injection", routes.get(1).myText);
                
 
        List<MedicationComponent> components = order.myMedicationComponents;
        assertEquals(2, components.size());
        assertEquals("B", components.get(0).myComponentType);
        assertEquals("AMI150I", components.get(0).myComponentCode.myCode);
        assertEquals("amiodarone inj 50 mg per 1mL", components.get(0).myComponentCode.myText);
        assertEquals(50, components.get(0).myComponentAmount, 0);
        assertEquals("MG", components.get(0).myComponentUnits.myCode);
        assertEquals("mg", components.get(0).myComponentUnits.myText);
        
        assertEquals("A", components.get(1).myComponentType);
        assertEquals("AMI160I", components.get(1).myComponentCode.myCode);
        assertEquals("amiodarone inj 60 mg per 1mL", components.get(1).myComponentCode.myText);
        assertEquals(60, components.get(1).myComponentAmount, 0);
        assertEquals("MG", components.get(1).myComponentUnits.myCode);
        assertEquals("mg", components.get(1).myComponentUnits.myText);        
               
        
        assertEquals(1, admin1.myAdministrationNumber);
        assertEquals(ourTsMinFormatWOffset.parse("201204250101-0400"), admin1.myStartTime);
        assertEquals(ourTsMinFormatWOffset.parse("201204250201-0400"), admin1.myEndTime);
        assertEquals("NITRO", admin1.myAdministeredCode.myCode);
        assertEquals("Nitroglycerine", admin1.myAdministeredCode.myText);
        assertEquals(100, admin1.myAdministeredAmount);
        assertEquals("mg", admin1.myAdministeredUnits.myCode);
        assertEquals("D5WGB", admin1.myAdministrationNotes.get(0).myCode);
        assertEquals("100 mg bottle", admin1.myAdministrationNotes.get(0).myText);
        assertEquals("premixed solution", admin1.myAdministrationNotes.get(1).myText);
        assertEquals("100mg/min", admin1.myAdministeredPerTimeUnit);
        assertEquals("INJ", admin1.myMedicationRoute.myCode);
        assertEquals("Injection", admin1.myMedicationRoute.myText);
        
        
        assertEquals(2, admin2.myAdministrationNumber);
        assertEquals(ourTsMinFormatWOffset.parse("201204250201-0400"), admin2.myStartTime);
        assertEquals(ourTsMinFormatWOffset.parse("201204250301-0400"), admin2.myEndTime);
        assertEquals("NITRO", admin2.myAdministeredCode.myCode);
        assertEquals("Nitroglycerine", admin2.myAdministeredCode.myText);
        assertEquals(100, admin2.myAdministeredAmount);
        assertEquals("mg", admin2.myAdministeredUnits.myCode);
        assertEquals("D5WGB", admin2.myAdministrationNotes.get(0).myCode);
        assertEquals("100 mg bottle", admin2.myAdministrationNotes.get(0).myText);
        assertEquals("premixed solution", admin2.myAdministrationNotes.get(1).myText);
        assertEquals("100mg/min", admin2.myAdministeredPerTimeUnit);
        assertEquals("INJ", admin2.myMedicationRoute.myCode);
        assertEquals("Injection", admin2.myMedicationRoute.myText);
        

        assertEquals(1, admin3.myAdministrationNumber);
        assertEquals(ourTsMinFormatWOffset.parse("201204250101-0400"), admin3.myStartTime);
        assertEquals(ourTsMinFormatWOffset.parse("201204250201-0400"), admin3.myEndTime);        
        assertEquals("fish oil", admin3.myAdministeredCode.myText);
        assertEquals(200, admin3.myAdministeredAmount);
        assertEquals("mg", admin3.myAdministeredUnits.myCode);
        assertEquals("D5WGB", admin3.myAdministrationNotes.get(0).myCode);
        assertEquals("200 mg bottle", admin3.myAdministrationNotes.get(0).myText);
        assertEquals("premixed solution", admin3.myAdministrationNotes.get(1).myText);
        assertEquals("200mg/min", admin3.myAdministeredPerTimeUnit);        
        assertEquals("Intravenous", admin3.myMedicationRoute.myText);
        
        
        assertEquals(2, admin4.myAdministrationNumber);
        assertEquals(ourTsMinFormatWOffset.parse("201204250201-0400"), admin4.myStartTime);
        assertEquals(ourTsMinFormatWOffset.parse("201204250301-0400"), admin4.myEndTime);        
        assertEquals("fish oil", admin4.myAdministeredCode.myText);
        assertEquals(200, admin4.myAdministeredAmount);
        assertEquals("mg", admin4.myAdministeredUnits.myCode);
        assertEquals("D5WGB", admin4.myAdministrationNotes.get(0).myCode);
        assertEquals("200 mg bottle", admin4.myAdministrationNotes.get(0).myText);
        assertEquals("premixed solution", admin4.myAdministrationNotes.get(1).myText);
        assertEquals("200mg/min", admin4.myAdministeredPerTimeUnit);        
        assertEquals("Intravenous", admin4.myMedicationRoute.myText);
        

                
    }
    
    
    /**
     * Testing RAS^O17 containing single order group with order content and the order already exists
     * in the database. In this case we perform a full update of the existing medication order and its medication admins
     *
     *   
     * @throws Exception ...
     */
    @Test
    public void testRas017b() throws Exception {
    
    
            String message1 = 
                "MSH|^~\\&|1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1|UHNG|||20120501103100-0500|2954864636aaa|RDE^O11^RDE_O11|31768|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r" +
                "PID|1||7005728^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR^G~00000000000^AA^^^JHN^G^^^CANON&&HL70363||Smith^Joseph^John^Junior^Mr^MD^L~Smith^Joe^^^^^A|Blanche^^^^^^L|19310313000000-0500|M|||26 RIVINGTON AVE^^Goderich^CANON^N7A3Y2^CAN^H~7 WOODPLUMPTON ROAD^^Port Stanley^CANON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English^HL70296|||||||||||||||N\r" +                 
                "PV1|1|I|JS12^123^4^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.100.1||||38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|GIM|||||||38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1||284675^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^VN\r" +                 
                "ORC|OK|7777^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1||777^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1|\r" +
                "RXE|3^Q24H^INDEF^201204181000-0400^201205221000-0400|QUE25T^quetiapine tab 25 mg^1.3.6.1.4.1.12201.102.4|1|2|MG^milligram|INJ^Injection|^Hold PCA if patient confused or loses IV access~^Hold PCA if patient loses IV access\r" +                
                "NTE|1||Note1||\r" +
                "NTE|2||Note2||\r" +
                "NTE|3||Note3||\r" +
                "RXR|^Intravenous||\r" +
                "RXR|INJ^Injection||||||\r" +
                "RXC|B|AMI150I^amiodarone inj 50 mg per 1mL^1.3.6.1.4.1.12201.102.3|50|MG^mg|\r" +
                "RXC|A|AMI160I^amiodarone inj 60 mg per 1mL^1.3.6.1.4.1.12201.102.3|60|MG^mg|\r";
        
        
                
        String message2 = 
                "MSH|^~\\&|1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1|UHNG|||20120501103100-0500|2954864636aaa|RAS^O17^RAS_O17|31768|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r" +
                "PID|1||7005728^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR^G~00000000000^AA^^^JHN^G^^^CANON&&HL70363||Smith^Joseph^John^Junior^Mr^MD^L~Smith^Joe^^^^^A|Blanche^^^^^^L|19310313000000-0500|M|||26 RIVINGTON AVE^^Goderich^CANON^N7A3Y2^CAN^H~7 WOODPLUMPTON ROAD^^Port Stanley^CANON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English^HL70296|||||||||||||||N\r" +                 
                "PV1|1|I|JS12^123^4^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.100.1||||38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|GIM|||||||38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1||284675^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^VN\r" +                 
                "ORC|OK|7777^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1||777^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1|\r" +
                "RXE|3^Q24H^INDEF^201204181000-0400^201205221000-0400|QUE50T^quetiapine tab 50 mg^1.3.6.1.4.1.12201.102.4|1|2|MG^milligram|INJ^Injection|^Hold PCA if patient confused or loses IV access~^Hold PCA if patient loses IV access\r" +                
                "RXR|^Intravenous2||\r" +
                "RXR|INJ2^Injection2||||||\r" +
                "RXC|B|AMI160I^amiodarone inj 60 mg per 1mL^1.3.6.1.4.1.12201.102.3|60|MG^mg|\r" +
                "RXC|A|AMI170I^amiodarone inj 70 mg per 1mL^1.3.6.1.4.1.12201.102.3|70|MG^mg|\r" +
                "RXA||1|201204250101-0400|201204250201-0400|NITRO^Nitroglycerine^1.3.6.1.4.1.12201.102.2|100|mg||D5WGB^100 mg bottle~^premixed solution|||100mg/min\r" +
                "RXA||2|201204250201-0400|201204250301-0400|NITRO^Nitroglycerine^1.3.6.1.4.1.12201.102.2|100|mg||D5WGB^100 mg bottle~^premixed solution|||100mg/min\r" +
                "RXR|INJ^Injection||||||\r" +
                "RXA||1|201204250101-0400|201204250201-0400|^fish oil|200|mg||D5WGB^200 mg bottle~^premixed solution|||200mg/min\r" +
                "RXA||2|201204250201-0400|201204250301-0400|^fish oil|200|mg||D5WGB^200 mg bottle~^premixed solution|||200mg/min\r" +
                "RXR|^Intravenous||||||\r";
        
        
        
       
        Converter c = new Converter();
        RDE_O11 input = new RDE_O11();
        input.setParser(PipeParser.getInstanceWithNoValidation());
        input.parse(message1);
        List<MedicationOrder> medOrders = c.convertMedicationOrder(input);        
        Persister.persistMedicationOrders(medOrders);        
        
        RAS_O17 input2 = new RAS_O17();
        input2.setParser(PipeParser.getInstanceWithNoValidation());
        input2.parse(message2);
        List<MedicationOrderWithAdmins> medOrdersWadmins = c.convertMedicationAdmin(input2);        
        Persister.persistMedicationAdmins(medOrdersWadmins);
               
        ViewQuery query = new ViewQuery().viewName("allView").designDocId("_design/application");
        List<MedicationOrderWithAdminsContainer> medOrderWithAdminsContainers = Persister.getConnector().queryView(query, MedicationOrderWithAdminsContainer.class);
        assertEquals(1,medOrderWithAdminsContainers.size());
        
        Patient patient = medOrderWithAdminsContainers.get(0).getDocument().myOrder.myPatient;
        Visit visit = medOrderWithAdminsContainers.get(0).getDocument().myOrder.myVisit;
        MedicationOrder order =  medOrderWithAdminsContainers.get(0).getDocument().myOrder;
        assertEquals(4, medOrderWithAdminsContainers.get(0).getDocument().myAdmins.size()); 
        MedicationAdmin admin1 = medOrderWithAdminsContainers.get(0).getDocument().myAdmins.get(0);
        MedicationAdmin admin2 = medOrderWithAdminsContainers.get(0).getDocument().myAdmins.get(1);
        MedicationAdmin admin3 = medOrderWithAdminsContainers.get(0).getDocument().myAdmins.get(2);
        MedicationAdmin admin4 = medOrderWithAdminsContainers.get(0).getDocument().myAdmins.get(3);
        
        
        //check lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > medOrderWithAdminsContainers.get(0).getDocument().myRecordUpdatedDate.getTime());

        
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

        //check med order
        assertEquals("777", order.myPlacerGroupNumber.myId);
        assertEquals("1.3.6.1.4.1.12201", order.myPlacerGroupNumber.myFacilityId);
        assertEquals("1.3.6.1.4.1.12201.101.1", order.myPlacerGroupNumber.mySystemId);
        assertEquals("7777", order.myPlacerOrderNumber.myId);
        assertEquals("1.3.6.1.4.1.12201", order.myPlacerOrderNumber.myFacilityId);
        assertEquals("1.3.6.1.4.1.12201.101.1", order.myPlacerOrderNumber.mySystemId);
        assertEquals("OK", order.myStatusCode);
        assertEquals("Order verified", order.myStatusName);
        
        assertEquals(3, order.myEncodedOrderQuantityNumber);
        assertEquals("Q24H", order.myEncodedOrderQuantityRepeatPattern);
        assertEquals(ourTsMinFormatWOffset.parse("201204181000-0400"), order.myEncodedOrderQuantityStartTime);
        assertEquals(ourTsMinFormatWOffset.parse("201205221000-0400"), order.myEncodedOrderQuantityEndTime);
        assertEquals("quetiapine tab 50 mg", order.myEncodedOrderGiveCode.myText);
        assertEquals(1, order.myEncodedOrderGiveMinimum, 0.00001);
        assertEquals(2, order.myEncodedOrderGiveMaximum, 0.00001);
        assertEquals("MG", order.myEncodedOrderGiveUnits.myCode);
        assertEquals("INJ", order.myEncodedOrderGiveDosageForm.myCode);
        assertEquals("Hold PCA if patient confused or loses IV access", order.myEncodedOrderProvidersAdministrationInstructions.get(0).myText);
        assertEquals("Hold PCA if patient loses IV access", order.myEncodedOrderProvidersAdministrationInstructions.get(1).myText);
        
        
        List<Note> notes = order.myNotes;
        assertEquals(0, notes.size());
        
        
        List<Ce> routes = order.myMedicationRoutes;
        assertEquals(2, routes.size());
        assertEquals("Intravenous2", routes.get(0).myText);
        assertEquals("Injection2", routes.get(1).myText);
                
 
        List<MedicationComponent> components = order.myMedicationComponents;
        assertEquals(2, components.size());
        assertEquals("B", components.get(0).myComponentType);
        assertEquals("AMI160I", components.get(0).myComponentCode.myCode);
        assertEquals("amiodarone inj 60 mg per 1mL", components.get(0).myComponentCode.myText);
        assertEquals(60, components.get(0).myComponentAmount, 0);
        assertEquals("MG", components.get(0).myComponentUnits.myCode);
        assertEquals("mg", components.get(0).myComponentUnits.myText);
        
        assertEquals("A", components.get(1).myComponentType);
        assertEquals("AMI170I", components.get(1).myComponentCode.myCode);
        assertEquals("amiodarone inj 70 mg per 1mL", components.get(1).myComponentCode.myText);
        assertEquals(70, components.get(1).myComponentAmount, 0);
        assertEquals("MG", components.get(1).myComponentUnits.myCode);
        assertEquals("mg", components.get(1).myComponentUnits.myText);        
               
        
        assertEquals(1, admin1.myAdministrationNumber);
        assertEquals(ourTsMinFormatWOffset.parse("201204250101-0400"), admin1.myStartTime);
        assertEquals(ourTsMinFormatWOffset.parse("201204250201-0400"), admin1.myEndTime);
        assertEquals("NITRO", admin1.myAdministeredCode.myCode);
        assertEquals("Nitroglycerine", admin1.myAdministeredCode.myText);
        assertEquals(100, admin1.myAdministeredAmount);
        assertEquals("mg", admin1.myAdministeredUnits.myCode);
        assertEquals("D5WGB", admin1.myAdministrationNotes.get(0).myCode);
        assertEquals("100 mg bottle", admin1.myAdministrationNotes.get(0).myText);
        assertEquals("premixed solution", admin1.myAdministrationNotes.get(1).myText);
        assertEquals("100mg/min", admin1.myAdministeredPerTimeUnit);
        assertEquals("INJ", admin1.myMedicationRoute.myCode);
        assertEquals("Injection", admin1.myMedicationRoute.myText);
        
        
        assertEquals(2, admin2.myAdministrationNumber);
        assertEquals(ourTsMinFormatWOffset.parse("201204250201-0400"), admin2.myStartTime);
        assertEquals(ourTsMinFormatWOffset.parse("201204250301-0400"), admin2.myEndTime);
        assertEquals("NITRO", admin2.myAdministeredCode.myCode);
        assertEquals("Nitroglycerine", admin2.myAdministeredCode.myText);
        assertEquals(100, admin2.myAdministeredAmount);
        assertEquals("mg", admin2.myAdministeredUnits.myCode);
        assertEquals("D5WGB", admin2.myAdministrationNotes.get(0).myCode);
        assertEquals("100 mg bottle", admin2.myAdministrationNotes.get(0).myText);
        assertEquals("premixed solution", admin2.myAdministrationNotes.get(1).myText);
        assertEquals("100mg/min", admin2.myAdministeredPerTimeUnit);
        assertEquals("INJ", admin2.myMedicationRoute.myCode);
        assertEquals("Injection", admin2.myMedicationRoute.myText);
        

        assertEquals(1, admin3.myAdministrationNumber);
        assertEquals(ourTsMinFormatWOffset.parse("201204250101-0400"), admin3.myStartTime);
        assertEquals(ourTsMinFormatWOffset.parse("201204250201-0400"), admin3.myEndTime);        
        assertEquals("fish oil", admin3.myAdministeredCode.myText);
        assertEquals(200, admin3.myAdministeredAmount);
        assertEquals("mg", admin3.myAdministeredUnits.myCode);
        assertEquals("D5WGB", admin3.myAdministrationNotes.get(0).myCode);
        assertEquals("200 mg bottle", admin3.myAdministrationNotes.get(0).myText);
        assertEquals("premixed solution", admin3.myAdministrationNotes.get(1).myText);
        assertEquals("200mg/min", admin3.myAdministeredPerTimeUnit);        
        assertEquals("Intravenous", admin3.myMedicationRoute.myText);
        
        
        assertEquals(2, admin4.myAdministrationNumber);
        assertEquals(ourTsMinFormatWOffset.parse("201204250201-0400"), admin4.myStartTime);
        assertEquals(ourTsMinFormatWOffset.parse("201204250301-0400"), admin4.myEndTime);        
        assertEquals("fish oil", admin4.myAdministeredCode.myText);
        assertEquals(200, admin4.myAdministeredAmount);
        assertEquals("mg", admin4.myAdministeredUnits.myCode);
        assertEquals("D5WGB", admin4.myAdministrationNotes.get(0).myCode);
        assertEquals("200 mg bottle", admin4.myAdministrationNotes.get(0).myText);
        assertEquals("premixed solution", admin4.myAdministrationNotes.get(1).myText);
        assertEquals("200mg/min", admin4.myAdministeredPerTimeUnit);        
        assertEquals("Intravenous", admin4.myMedicationRoute.myText);
        

                
    }    
    
    
    
    /**
     * Testing RAS^O17 containing single order group with no order content and the order already exists
     * in the database. In this case we're only going to update the existing medication admins and leave the med order information alone.
     *
     *   
     * @throws Exception ...
     */
    @Test
    public void testRas017c() throws Exception {
    
    
            String message1 = 
                "MSH|^~\\&|1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1|UHNG|||20120501103100-0500|2954864636aaa|RDE^O11^RDE_O11|31768|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r" +
                "PID|1||7005728^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR^G~00000000000^AA^^^JHN^G^^^CANON&&HL70363||Smith^Joseph^John^Junior^Mr^MD^L~Smith^Joe^^^^^A|Blanche^^^^^^L|19310313000000-0500|M|||26 RIVINGTON AVE^^Goderich^CANON^N7A3Y2^CAN^H~7 WOODPLUMPTON ROAD^^Port Stanley^CANON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English^HL70296|||||||||||||||N\r" +                 
                "PV1|1|I|JS12^123^4^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.100.1||||38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|GIM|||||||38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1||284675^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^VN\r" +                 
                "ORC|OK|7777^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1||777^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1|\r" +
                "RXE|3^Q24H^INDEF^201204181000-0400^201205221000-0400|QUE25T^quetiapine tab 25 mg^1.3.6.1.4.1.12201.102.4|1|2|MG^milligram|INJ^Injection|^Hold PCA if patient confused or loses IV access~^Hold PCA if patient loses IV access\r" +                
                "NTE|1||Note1||\r" +
                "NTE|2||Note2||\r" +
                "NTE|3||Note3||\r" +
                "RXR|^Intravenous||\r" +
                "RXR|INJ^Injection||||||\r" +
                "RXC|B|AMI150I^amiodarone inj 50 mg per 1mL^1.3.6.1.4.1.12201.102.3|50|MG^mg|\r" +
                "RXC|A|AMI160I^amiodarone inj 60 mg per 1mL^1.3.6.1.4.1.12201.102.3|60|MG^mg|\r";
        
        
                
        String message2 = 
                "MSH|^~\\&|1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1|UHNG|||20120501103100-0500|2954864636aaa|RAS^O17^RAS_O17|31768|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r" +
                "PID|1||7005728^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR^G~00000000000^AA^^^JHN^G^^^CANON&&HL70363||Smith^Joseph^John^Junior^Mr^MD^L~Smith^Joe^^^^^A|Blanche^^^^^^L|19310313000000-0500|M|||26 RIVINGTON AVE^^Goderich^CANON^N7A3Y2^CAN^H~7 WOODPLUMPTON ROAD^^Port Stanley^CANON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English^HL70296|||||||||||||||N\r" +                 
                "PV1|1|I|JS12^123^4^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.100.1||||38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|GIM|||||||38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1||284675^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^VN\r" +                 
                "ORC|OK|7777^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1||777^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1|\r" +
                "RXA||1|201204250101-0400|201204250201-0400|NITRO^Nitroglycerine^1.3.6.1.4.1.12201.102.2|100|mg||D5WGB^100 mg bottle~^premixed solution|||100mg/min\r" +
                "RXA||2|201204250201-0400|201204250301-0400|NITRO^Nitroglycerine^1.3.6.1.4.1.12201.102.2|100|mg||D5WGB^100 mg bottle~^premixed solution|||100mg/min\r" +
                "RXR|INJ^Injection||||||\r" +
                "RXA||1|201204250101-0400|201204250201-0400|^fish oil|200|mg||D5WGB^200 mg bottle~^premixed solution|||200mg/min\r" +
                "RXA||2|201204250201-0400|201204250301-0400|^fish oil|200|mg||D5WGB^200 mg bottle~^premixed solution|||200mg/min\r" +
                "RXR|^Intravenous||||||\r";
        
        
        
       
        Converter c = new Converter();
        RDE_O11 input = new RDE_O11();
        input.setParser(PipeParser.getInstanceWithNoValidation());
        input.parse(message1);
        List<MedicationOrder> medOrders = c.convertMedicationOrder(input);        
        Persister.persistMedicationOrders(medOrders);
        
        //Obtain the doc from the db to get the created time
        ViewQuery query = new ViewQuery().viewName("allView").designDocId("_design/application");
        List<MedicationOrderWithAdminsContainer> medOrderWithAdminsContainers = Persister.getConnector().queryView(query, MedicationOrderWithAdminsContainer.class);
        long orderCreatedTime = medOrderWithAdminsContainers.get(0).getDocument().myRecordUpdatedDate.getTime();
        
        RAS_O17 input2 = new RAS_O17();
        input2.setParser(PipeParser.getInstanceWithNoValidation());
        input2.parse(message2);
        List<MedicationOrderWithAdmins> medOrdersWadmins = c.convertMedicationAdmin(input2);        
        Persister.persistMedicationAdmins(medOrdersWadmins);
        
        //Obtain the updated doc from the db
        medOrderWithAdminsContainers = Persister.getConnector().queryView(query, MedicationOrderWithAdminsContainer.class);
        assertEquals(1,medOrderWithAdminsContainers.size());
        
        Patient patient = medOrderWithAdminsContainers.get(0).getDocument().myOrder.myPatient;
        Visit visit = medOrderWithAdminsContainers.get(0).getDocument().myOrder.myVisit;
        MedicationOrder order =  medOrderWithAdminsContainers.get(0).getDocument().myOrder;
        assertEquals(4, medOrderWithAdminsContainers.get(0).getDocument().myAdmins.size()); 
        MedicationAdmin admin1 = medOrderWithAdminsContainers.get(0).getDocument().myAdmins.get(0);
        MedicationAdmin admin2 = medOrderWithAdminsContainers.get(0).getDocument().myAdmins.get(1);
        MedicationAdmin admin3 = medOrderWithAdminsContainers.get(0).getDocument().myAdmins.get(2);
        MedicationAdmin admin4 = medOrderWithAdminsContainers.get(0).getDocument().myAdmins.get(3);
        
        
        //check lastUpdateTime
        assertTrue(medOrderWithAdminsContainers.get(0).getDocument().myRecordUpdatedDate.getTime() > orderCreatedTime);
        
       
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

        //check med order
        assertEquals("777", order.myPlacerGroupNumber.myId);
        assertEquals("1.3.6.1.4.1.12201", order.myPlacerGroupNumber.myFacilityId);
        assertEquals("1.3.6.1.4.1.12201.101.1", order.myPlacerGroupNumber.mySystemId);
        assertEquals("7777", order.myPlacerOrderNumber.myId);
        assertEquals("1.3.6.1.4.1.12201", order.myPlacerOrderNumber.myFacilityId);
        assertEquals("1.3.6.1.4.1.12201.101.1", order.myPlacerOrderNumber.mySystemId);
        assertEquals("OK", order.myStatusCode);
        assertEquals("Order verified", order.myStatusName);
        
        assertEquals(3, order.myEncodedOrderQuantityNumber);
        assertEquals("Q24H", order.myEncodedOrderQuantityRepeatPattern);
        assertEquals(ourTsMinFormatWOffset.parse("201204181000-0400"), order.myEncodedOrderQuantityStartTime);
        assertEquals(ourTsMinFormatWOffset.parse("201205221000-0400"), order.myEncodedOrderQuantityEndTime);
        assertEquals("quetiapine tab 25 mg", order.myEncodedOrderGiveCode.myText);
        assertEquals(1, order.myEncodedOrderGiveMinimum, 0.00001);
        assertEquals(2, order.myEncodedOrderGiveMaximum, 0.00001);
        assertEquals("MG", order.myEncodedOrderGiveUnits.myCode);
        assertEquals("INJ", order.myEncodedOrderGiveDosageForm.myCode);
        assertEquals("Hold PCA if patient confused or loses IV access", order.myEncodedOrderProvidersAdministrationInstructions.get(0).myText);
        assertEquals("Hold PCA if patient loses IV access", order.myEncodedOrderProvidersAdministrationInstructions.get(1).myText);
        
        
        List<Note> notes = order.myNotes;
        assertEquals(3, notes.size());
        assertEquals("Note1", notes.get(0).myNoteText);
        assertEquals("Note2", notes.get(1).myNoteText);
        assertEquals("Note3", notes.get(2).myNoteText);
        
        
        List<Ce> routes = order.myMedicationRoutes;
        assertEquals(2, routes.size());
        assertEquals("Intravenous", routes.get(0).myText);
        assertEquals("Injection", routes.get(1).myText);
                
 
        List<MedicationComponent> components = order.myMedicationComponents;
        assertEquals(2, components.size());
        assertEquals("B", components.get(0).myComponentType);
        assertEquals("AMI150I", components.get(0).myComponentCode.myCode);
        assertEquals("amiodarone inj 50 mg per 1mL", components.get(0).myComponentCode.myText);
        assertEquals(50, components.get(0).myComponentAmount, 0);
        assertEquals("MG", components.get(0).myComponentUnits.myCode);
        assertEquals("mg", components.get(0).myComponentUnits.myText);
        
        assertEquals("A", components.get(1).myComponentType);
        assertEquals("AMI160I", components.get(1).myComponentCode.myCode);
        assertEquals("amiodarone inj 60 mg per 1mL", components.get(1).myComponentCode.myText);
        assertEquals(60, components.get(1).myComponentAmount, 0);
        assertEquals("MG", components.get(1).myComponentUnits.myCode);
        assertEquals("mg", components.get(1).myComponentUnits.myText);        
               
        
        assertEquals(1, admin1.myAdministrationNumber);
        assertEquals(ourTsMinFormatWOffset.parse("201204250101-0400"), admin1.myStartTime);
        assertEquals(ourTsMinFormatWOffset.parse("201204250201-0400"), admin1.myEndTime);
        assertEquals("NITRO", admin1.myAdministeredCode.myCode);
        assertEquals("Nitroglycerine", admin1.myAdministeredCode.myText);
        assertEquals(100, admin1.myAdministeredAmount);
        assertEquals("mg", admin1.myAdministeredUnits.myCode);
        assertEquals("D5WGB", admin1.myAdministrationNotes.get(0).myCode);
        assertEquals("100 mg bottle", admin1.myAdministrationNotes.get(0).myText);
        assertEquals("premixed solution", admin1.myAdministrationNotes.get(1).myText);
        assertEquals("100mg/min", admin1.myAdministeredPerTimeUnit);
        assertEquals("INJ", admin1.myMedicationRoute.myCode);
        assertEquals("Injection", admin1.myMedicationRoute.myText);
        
        
        assertEquals(2, admin2.myAdministrationNumber);
        assertEquals(ourTsMinFormatWOffset.parse("201204250201-0400"), admin2.myStartTime);
        assertEquals(ourTsMinFormatWOffset.parse("201204250301-0400"), admin2.myEndTime);
        assertEquals("NITRO", admin2.myAdministeredCode.myCode);
        assertEquals("Nitroglycerine", admin2.myAdministeredCode.myText);
        assertEquals(100, admin2.myAdministeredAmount);
        assertEquals("mg", admin2.myAdministeredUnits.myCode);
        assertEquals("D5WGB", admin2.myAdministrationNotes.get(0).myCode);
        assertEquals("100 mg bottle", admin2.myAdministrationNotes.get(0).myText);
        assertEquals("premixed solution", admin2.myAdministrationNotes.get(1).myText);
        assertEquals("100mg/min", admin2.myAdministeredPerTimeUnit);
        assertEquals("INJ", admin2.myMedicationRoute.myCode);
        assertEquals("Injection", admin2.myMedicationRoute.myText);
        

        assertEquals(1, admin3.myAdministrationNumber);
        assertEquals(ourTsMinFormatWOffset.parse("201204250101-0400"), admin3.myStartTime);
        assertEquals(ourTsMinFormatWOffset.parse("201204250201-0400"), admin3.myEndTime);        
        assertEquals("fish oil", admin3.myAdministeredCode.myText);
        assertEquals(200, admin3.myAdministeredAmount);
        assertEquals("mg", admin3.myAdministeredUnits.myCode);
        assertEquals("D5WGB", admin3.myAdministrationNotes.get(0).myCode);
        assertEquals("200 mg bottle", admin3.myAdministrationNotes.get(0).myText);
        assertEquals("premixed solution", admin3.myAdministrationNotes.get(1).myText);
        assertEquals("200mg/min", admin3.myAdministeredPerTimeUnit);        
        assertEquals("Intravenous", admin3.myMedicationRoute.myText);
        
        
        assertEquals(2, admin4.myAdministrationNumber);
        assertEquals(ourTsMinFormatWOffset.parse("201204250201-0400"), admin4.myStartTime);
        assertEquals(ourTsMinFormatWOffset.parse("201204250301-0400"), admin4.myEndTime);        
        assertEquals("fish oil", admin4.myAdministeredCode.myText);
        assertEquals(200, admin4.myAdministeredAmount);
        assertEquals("mg", admin4.myAdministeredUnits.myCode);
        assertEquals("D5WGB", admin4.myAdministrationNotes.get(0).myCode);
        assertEquals("200 mg bottle", admin4.myAdministrationNotes.get(0).myText);
        assertEquals("premixed solution", admin4.myAdministrationNotes.get(1).myText);
        assertEquals("200mg/min", admin4.myAdministeredPerTimeUnit);        
        assertEquals("Intravenous", admin4.myMedicationRoute.myText);
        

                
    }
        
    
    
    
    
    /**
     * Testing RAS^O17 containing multiple order groups 
     *   
     * @throws Exception ...
     */
    @Test
    public void testRasO17d() throws Exception {
        
        
                
        String message1 = 
                "MSH|^~\\&|1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1|UHNG|||20120501103100-0500|2954864636aaa|RAS^O17^RAS_O17|31768|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r" +
                "PID|1||7005728^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR^G~00000000000^AA^^^JHN^G^^^CANON&&HL70363||Smith^Joseph^John^Junior^Mr^MD^L~Smith^Joe^^^^^A|Blanche^^^^^^L|19310313000000-0500|M|||26 RIVINGTON AVE^^Goderich^CANON^N7A3Y2^CAN^H~7 WOODPLUMPTON ROAD^^Port Stanley^CANON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com||eng^English^HL70296|||||||||||||||N\r" +                 
                "PV1|1|I|JS12^123^4^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.100.1||||38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1|GIM|||||||38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1||284675^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^VN\r" +                 
                "ORC|OK|7777^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1||777^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1|\r" +
                "RXE|3^Q24H^INDEF^201204181000-0400^201205221000-0400|QUE25T^quetiapine tab 25 mg^1.3.6.1.4.1.12201.102.4|1|2|MG^milligram|INJ^Injection|^Hold PCA if patient confused or loses IV access~^Hold PCA if patient loses IV access\r" +                
                "RXR|^Intravenous||\r" +
                "RXR|INJ^Injection||||||\r" +
                "RXC|B|AMI150I^amiodarone inj 50 mg per 1mL^1.3.6.1.4.1.12201.102.3|50|MG^mg|\r" +
                "RXC|A|AMI160I^amiodarone inj 60 mg per 1mL^1.3.6.1.4.1.12201.102.3|60|MG^mg|\r" +
                "RXA||1|201204250101-0400|201204250201-0400|NITRO^Nitroglycerine^1.3.6.1.4.1.12201.102.2|100|mg||D5WGB^100 mg bottle~^premixed solution|||100mg/min\r" +
                "RXA||2|201204250201-0400|201204250301-0400|NITRO^Nitroglycerine^1.3.6.1.4.1.12201.102.2|100|mg||D5WGB^100 mg bottle~^premixed solution|||100mg/min\r" +
                "RXR|INJ^Injection||||||\r" +
                "RXA||1|201204250101-0400|201204250201-0400|^fish oil|200|mg||D5WGB^200 mg bottle~^premixed solution|||200mg/min\r" +
                "RXA||2|201204250201-0400|201204250301-0400|^fish oil|200|mg||D5WGB^200 mg bottle~^premixed solution|||200mg/min\r" +
                "RXR|^Intravenous||||||\r" + 
                "ORC|OK|7778^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1||777^1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1|\r" +
                "RXE|4^Q24H^INDEF^201204181000-0400^201205221000-0400|QUE50T^quetiapine tab 50 mg^1.3.6.1.4.1.12201.102.4|1|2|MG^milligram|INJ^Injection|^Hold PCA if patient confused or loses IV access~^Hold PCA if patient loses IV access\r" +                
                "RXR|^Intravenous||\r" +
                "RXR|INJ^Injection||||||\r" +
                "RXC|B|AMI170I^amiodarone inj 70 mg per 1mL^1.3.6.1.4.1.12201.102.3|70|MG^mg|\r" +
                "RXC|A|AMI180I^amiodarone inj 80 mg per 1mL^1.3.6.1.4.1.12201.102.3|80|MG^mg|\r" +
                "RXA||1|201204250101-0400|201204250201-0400|NITRO^Nitroglycerine^1.3.6.1.4.1.12201.102.2|200|mg||D5WGB^200 mg bottle~^premixed solution|||200mg/min\r" +
                "RXA||2|201204250201-0400|201204250301-0400|NITRO^Nitroglycerine^1.3.6.1.4.1.12201.102.2|200|mg||D5WGB^200 mg bottle~^premixed solution|||200mg/min\r" +
                "RXR|INJ^Injection||||||\r" +
                "RXA||1|201204250101-0400|201204250201-0400|^fish oil|300|mg||D5WGB^300 mg bottle~^premixed solution|||300mg/min\r" +
                "RXA||2|201204250201-0400|201204250301-0400|^fish oil|300|mg||D5WGB^300 mg bottle~^premixed solution|||300mg/min\r" +
                "RXR|^Intravenous||||||\r";
        
        
        
        
        Converter c = new Converter();
        RAS_O17 input = new RAS_O17();
        input.setParser(PipeParser.getInstanceWithNoValidation());
        input.parse(message1);
        List<MedicationOrderWithAdmins> medOrders = c.convertMedicationAdmin(input);        
        Persister.persistMedicationAdmins(medOrders);
                
        ViewQuery query = new ViewQuery().viewName("allView").designDocId("_design/application");
        List<MedicationOrderWithAdminsContainer> medOrderWithAdminsContainers = Persister.getConnector().queryView(query, MedicationOrderWithAdminsContainer.class);
        assertEquals(2,medOrderWithAdminsContainers.size());        
        
        
        //med order 1
        String id = "MEDORDER_1.3.6.1.4.1.12201___1.3.6.1.4.1.12201.101.1___7777";
        MedicationOrderWithAdminsContainer doc1 = Persister.getConnector().get(MedicationOrderWithAdminsContainer.class, id);
        Patient doc1Patient = doc1.getDocument().myOrder.myPatient;
        Visit doc1Visit = doc1.getDocument().myOrder.myVisit;
        MedicationOrder doc1Order =  doc1.getDocument().myOrder;
        assertEquals(4, doc1.getDocument().myAdmins.size()); 
        MedicationAdmin doc1Admin1 = doc1.getDocument().myAdmins.get(0);
        MedicationAdmin doc1Admin2 = doc1.getDocument().myAdmins.get(1);
        MedicationAdmin doc1Admin3 = doc1.getDocument().myAdmins.get(2);
        MedicationAdmin doc1Admin4 = doc1.getDocument().myAdmins.get(3);
        
        
        //med order 2
        String id2 = "MEDORDER_1.3.6.1.4.1.12201___1.3.6.1.4.1.12201.101.1___7778";
        MedicationOrderWithAdminsContainer doc2 = Persister.getConnector().get(MedicationOrderWithAdminsContainer.class, id2);
        Patient doc2Patient = doc2.getDocument().myOrder.myPatient;
        Visit doc2Visit = doc2.getDocument().myOrder.myVisit;
        MedicationOrder doc2Order =  doc2.getDocument().myOrder;
        assertEquals(4, doc2.getDocument().myAdmins.size()); 
        MedicationAdmin doc2Admin1 = doc2.getDocument().myAdmins.get(0);
        MedicationAdmin doc2Admin2 = doc2.getDocument().myAdmins.get(1);
        MedicationAdmin doc2Admin3 = doc2.getDocument().myAdmins.get(2);
        MedicationAdmin doc2Admin4 = doc2.getDocument().myAdmins.get(3);
        
              
        
        //check med order1 data
        
        //check lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > doc1.getDocument().myRecordUpdatedDate.getTime());
        
               
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

        //check med order
        assertEquals("777", doc1Order.myPlacerGroupNumber.myId);
        assertEquals("1.3.6.1.4.1.12201", doc1Order.myPlacerGroupNumber.myFacilityId);
        assertEquals("1.3.6.1.4.1.12201.101.1", doc1Order.myPlacerGroupNumber.mySystemId);
        assertEquals("7777", doc1Order.myPlacerOrderNumber.myId);
        assertEquals("1.3.6.1.4.1.12201", doc1Order.myPlacerOrderNumber.myFacilityId);
        assertEquals("1.3.6.1.4.1.12201.101.1", doc1Order.myPlacerOrderNumber.mySystemId);
        assertEquals("OK", doc1Order.myStatusCode);
        assertEquals("Order verified", doc1Order.myStatusName);
        
        assertEquals(3, doc1Order.myEncodedOrderQuantityNumber);
        assertEquals("Q24H", doc1Order.myEncodedOrderQuantityRepeatPattern);
        assertEquals(ourTsMinFormatWOffset.parse("201204181000-0400"), doc1Order.myEncodedOrderQuantityStartTime);
        assertEquals(ourTsMinFormatWOffset.parse("201205221000-0400"), doc1Order.myEncodedOrderQuantityEndTime);
        assertEquals("quetiapine tab 25 mg", doc1Order.myEncodedOrderGiveCode.myText);
        assertEquals(1, doc1Order.myEncodedOrderGiveMinimum, 0.00001);
        assertEquals(2, doc1Order.myEncodedOrderGiveMaximum, 0.00001);
        assertEquals("MG", doc1Order.myEncodedOrderGiveUnits.myCode);
        assertEquals("INJ", doc1Order.myEncodedOrderGiveDosageForm.myCode);
        assertEquals("Hold PCA if patient confused or loses IV access", doc1Order.myEncodedOrderProvidersAdministrationInstructions.get(0).myText);
        assertEquals("Hold PCA if patient loses IV access", doc1Order.myEncodedOrderProvidersAdministrationInstructions.get(1).myText);
        
        
        List<Ce> routes = doc1Order.myMedicationRoutes;
        assertEquals(2, routes.size());
        assertEquals("Intravenous", routes.get(0).myText);
        assertEquals("Injection", routes.get(1).myText);
                
 
        List<MedicationComponent> components = doc1Order.myMedicationComponents;
        assertEquals(2, components.size());
        assertEquals("B", components.get(0).myComponentType);
        assertEquals("AMI150I", components.get(0).myComponentCode.myCode);
        assertEquals("amiodarone inj 50 mg per 1mL", components.get(0).myComponentCode.myText);
        assertEquals(50, components.get(0).myComponentAmount, 0);
        assertEquals("MG", components.get(0).myComponentUnits.myCode);
        assertEquals("mg", components.get(0).myComponentUnits.myText);
        
        assertEquals("A", components.get(1).myComponentType);
        assertEquals("AMI160I", components.get(1).myComponentCode.myCode);
        assertEquals("amiodarone inj 60 mg per 1mL", components.get(1).myComponentCode.myText);
        assertEquals(60, components.get(1).myComponentAmount, 0);
        assertEquals("MG", components.get(1).myComponentUnits.myCode);
        assertEquals("mg", components.get(1).myComponentUnits.myText);     
        
        
        //check admin for order
        assertEquals(1, doc1Admin1.myAdministrationNumber);
        assertEquals(ourTsMinFormatWOffset.parse("201204250101-0400"), doc1Admin1.myStartTime);
        assertEquals(ourTsMinFormatWOffset.parse("201204250201-0400"), doc1Admin1.myEndTime);
        assertEquals("NITRO", doc1Admin1.myAdministeredCode.myCode);
        assertEquals("Nitroglycerine", doc1Admin1.myAdministeredCode.myText);
        assertEquals(100, doc1Admin1.myAdministeredAmount);
        assertEquals("mg", doc1Admin1.myAdministeredUnits.myCode);
        assertEquals("D5WGB", doc1Admin1.myAdministrationNotes.get(0).myCode);
        assertEquals("100 mg bottle", doc1Admin1.myAdministrationNotes.get(0).myText);
        assertEquals("premixed solution", doc1Admin1.myAdministrationNotes.get(1).myText);
        assertEquals("100mg/min", doc1Admin1.myAdministeredPerTimeUnit);
        assertEquals("INJ", doc1Admin1.myMedicationRoute.myCode);
        assertEquals("Injection", doc1Admin1.myMedicationRoute.myText);        
        
        assertEquals(2, doc1Admin2.myAdministrationNumber);
        assertEquals(ourTsMinFormatWOffset.parse("201204250201-0400"), doc1Admin2.myStartTime);
        assertEquals(ourTsMinFormatWOffset.parse("201204250301-0400"), doc1Admin2.myEndTime);
        assertEquals("NITRO", doc1Admin2.myAdministeredCode.myCode);
        assertEquals("Nitroglycerine", doc1Admin2.myAdministeredCode.myText);
        assertEquals(100, doc1Admin2.myAdministeredAmount);
        assertEquals("mg", doc1Admin2.myAdministeredUnits.myCode);
        assertEquals("D5WGB", doc1Admin2.myAdministrationNotes.get(0).myCode);
        assertEquals("100 mg bottle", doc1Admin2.myAdministrationNotes.get(0).myText);
        assertEquals("premixed solution", doc1Admin2.myAdministrationNotes.get(1).myText);
        assertEquals("100mg/min", doc1Admin2.myAdministeredPerTimeUnit);
        assertEquals("INJ", doc1Admin2.myMedicationRoute.myCode);
        assertEquals("Injection", doc1Admin2.myMedicationRoute.myText);        

        assertEquals(1, doc1Admin3.myAdministrationNumber);
        assertEquals(ourTsMinFormatWOffset.parse("201204250101-0400"), doc1Admin3.myStartTime);
        assertEquals(ourTsMinFormatWOffset.parse("201204250201-0400"), doc1Admin3.myEndTime);        
        assertEquals("fish oil", doc1Admin3.myAdministeredCode.myText);
        assertEquals(200, doc1Admin3.myAdministeredAmount);
        assertEquals("mg", doc1Admin3.myAdministeredUnits.myCode);
        assertEquals("D5WGB", doc1Admin3.myAdministrationNotes.get(0).myCode);
        assertEquals("200 mg bottle", doc1Admin3.myAdministrationNotes.get(0).myText);
        assertEquals("premixed solution", doc1Admin3.myAdministrationNotes.get(1).myText);
        assertEquals("200mg/min", doc1Admin3.myAdministeredPerTimeUnit);        
        assertEquals("Intravenous", doc1Admin3.myMedicationRoute.myText);        
        
        assertEquals(2, doc1Admin4.myAdministrationNumber);
        assertEquals(ourTsMinFormatWOffset.parse("201204250201-0400"), doc1Admin4.myStartTime);
        assertEquals(ourTsMinFormatWOffset.parse("201204250301-0400"), doc1Admin4.myEndTime);        
        assertEquals("fish oil", doc1Admin4.myAdministeredCode.myText);
        assertEquals(200, doc1Admin4.myAdministeredAmount);
        assertEquals("mg", doc1Admin4.myAdministeredUnits.myCode);
        assertEquals("D5WGB", doc1Admin4.myAdministrationNotes.get(0).myCode);
        assertEquals("200 mg bottle", doc1Admin4.myAdministrationNotes.get(0).myText);
        assertEquals("premixed solution", doc1Admin4.myAdministrationNotes.get(1).myText);
        assertEquals("200mg/min", doc1Admin4.myAdministeredPerTimeUnit);        
        assertEquals("Intravenous", doc1Admin4.myMedicationRoute.myText);        
        
        //check med order2 data
        
        //check lastUpdateTime
        assertTrue(nowMilli > doc2.getDocument().myRecordUpdatedDate.getTime());
        
               
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

        //check med order
        assertEquals("777", doc2Order.myPlacerGroupNumber.myId);
        assertEquals("1.3.6.1.4.1.12201", doc2Order.myPlacerGroupNumber.myFacilityId);
        assertEquals("1.3.6.1.4.1.12201.101.1", doc2Order.myPlacerGroupNumber.mySystemId);
        assertEquals("7778", doc2Order.myPlacerOrderNumber.myId);
        assertEquals("1.3.6.1.4.1.12201", doc2Order.myPlacerOrderNumber.myFacilityId);
        assertEquals("1.3.6.1.4.1.12201.101.1", doc2Order.myPlacerOrderNumber.mySystemId);
        assertEquals("OK", doc2Order.myStatusCode);
        assertEquals("Order verified", doc2Order.myStatusName);
        
        assertEquals(4, doc2Order.myEncodedOrderQuantityNumber);
        assertEquals("Q24H", doc2Order.myEncodedOrderQuantityRepeatPattern);
        assertEquals(ourTsMinFormatWOffset.parse("201204181000-0400"), doc2Order.myEncodedOrderQuantityStartTime);
        assertEquals(ourTsMinFormatWOffset.parse("201205221000-0400"), doc2Order.myEncodedOrderQuantityEndTime);
        assertEquals("quetiapine tab 50 mg", doc2Order.myEncodedOrderGiveCode.myText);
        assertEquals(1, doc2Order.myEncodedOrderGiveMinimum, 0.00001);
        assertEquals(2, doc2Order.myEncodedOrderGiveMaximum, 0.00001);
        assertEquals("MG", doc2Order.myEncodedOrderGiveUnits.myCode);
        assertEquals("INJ", doc2Order.myEncodedOrderGiveDosageForm.myCode);
        assertEquals("Hold PCA if patient confused or loses IV access", doc2Order.myEncodedOrderProvidersAdministrationInstructions.get(0).myText);
        assertEquals("Hold PCA if patient loses IV access", doc2Order.myEncodedOrderProvidersAdministrationInstructions.get(1).myText);
        
        
        routes = doc2Order.myMedicationRoutes;
        assertEquals(2, routes.size());
        assertEquals("Intravenous", routes.get(0).myText);
        assertEquals("Injection", routes.get(1).myText);
                
 
        components = doc2Order.myMedicationComponents;
        assertEquals(2, components.size());
        assertEquals("B", components.get(0).myComponentType);
        assertEquals("AMI170I", components.get(0).myComponentCode.myCode);
        assertEquals("amiodarone inj 70 mg per 1mL", components.get(0).myComponentCode.myText);
        assertEquals(70, components.get(0).myComponentAmount, 0);
        assertEquals("MG", components.get(0).myComponentUnits.myCode);
        assertEquals("mg", components.get(0).myComponentUnits.myText);
        
        assertEquals("A", components.get(1).myComponentType);
        assertEquals("AMI180I", components.get(1).myComponentCode.myCode);
        assertEquals("amiodarone inj 80 mg per 1mL", components.get(1).myComponentCode.myText);
        assertEquals(80, components.get(1).myComponentAmount, 0);
        assertEquals("MG", components.get(1).myComponentUnits.myCode);
        assertEquals("mg", components.get(1).myComponentUnits.myText);            
            

        //check admins for order    
        assertEquals(1, doc2Admin1.myAdministrationNumber);
        assertEquals(ourTsMinFormatWOffset.parse("201204250101-0400"), doc2Admin1.myStartTime);
        assertEquals(ourTsMinFormatWOffset.parse("201204250201-0400"), doc2Admin1.myEndTime);
        assertEquals("NITRO", doc2Admin1.myAdministeredCode.myCode);
        assertEquals("Nitroglycerine", doc2Admin1.myAdministeredCode.myText);
        assertEquals(200, doc2Admin1.myAdministeredAmount);
        assertEquals("mg", doc2Admin1.myAdministeredUnits.myCode);
        assertEquals("D5WGB", doc2Admin1.myAdministrationNotes.get(0).myCode);
        assertEquals("200 mg bottle", doc2Admin1.myAdministrationNotes.get(0).myText);
        assertEquals("premixed solution", doc2Admin1.myAdministrationNotes.get(1).myText);
        assertEquals("200mg/min", doc2Admin1.myAdministeredPerTimeUnit);
        assertEquals("INJ", doc2Admin1.myMedicationRoute.myCode);
        assertEquals("Injection", doc2Admin1.myMedicationRoute.myText);        
        
        assertEquals(2, doc2Admin2.myAdministrationNumber);
        assertEquals(ourTsMinFormatWOffset.parse("201204250201-0400"), doc2Admin2.myStartTime);
        assertEquals(ourTsMinFormatWOffset.parse("201204250301-0400"), doc2Admin2.myEndTime);
        assertEquals("NITRO", doc2Admin2.myAdministeredCode.myCode);
        assertEquals("Nitroglycerine", doc2Admin2.myAdministeredCode.myText);
        assertEquals(200, doc2Admin2.myAdministeredAmount);
        assertEquals("mg", doc2Admin2.myAdministeredUnits.myCode);
        assertEquals("D5WGB", doc2Admin2.myAdministrationNotes.get(0).myCode);
        assertEquals("200 mg bottle", doc2Admin2.myAdministrationNotes.get(0).myText);
        assertEquals("premixed solution", doc2Admin2.myAdministrationNotes.get(1).myText);
        assertEquals("200mg/min", doc2Admin2.myAdministeredPerTimeUnit);
        assertEquals("INJ", doc2Admin2.myMedicationRoute.myCode);
        assertEquals("Injection", doc2Admin2.myMedicationRoute.myText);        

        assertEquals(1, doc2Admin3.myAdministrationNumber);
        assertEquals(ourTsMinFormatWOffset.parse("201204250101-0400"), doc2Admin3.myStartTime);
        assertEquals(ourTsMinFormatWOffset.parse("201204250201-0400"), doc2Admin3.myEndTime);        
        assertEquals("fish oil", doc2Admin3.myAdministeredCode.myText);
        assertEquals(300, doc2Admin3.myAdministeredAmount);
        assertEquals("mg", doc2Admin3.myAdministeredUnits.myCode);
        assertEquals("D5WGB", doc2Admin3.myAdministrationNotes.get(0).myCode);
        assertEquals("300 mg bottle", doc2Admin3.myAdministrationNotes.get(0).myText);
        assertEquals("premixed solution", doc2Admin3.myAdministrationNotes.get(1).myText);
        assertEquals("300mg/min", doc2Admin3.myAdministeredPerTimeUnit);        
        assertEquals("Intravenous", doc2Admin3.myMedicationRoute.myText);        
        
        assertEquals(2, doc2Admin4.myAdministrationNumber);
        assertEquals(ourTsMinFormatWOffset.parse("201204250201-0400"), doc2Admin4.myStartTime);
        assertEquals(ourTsMinFormatWOffset.parse("201204250301-0400"), doc2Admin4.myEndTime);        
        assertEquals("fish oil", doc2Admin4.myAdministeredCode.myText);
        assertEquals(300, doc2Admin4.myAdministeredAmount);
        assertEquals("mg", doc2Admin4.myAdministeredUnits.myCode);
        assertEquals("D5WGB", doc2Admin4.myAdministrationNotes.get(0).myCode);
        assertEquals("300 mg bottle", doc2Admin4.myAdministrationNotes.get(0).myText);
        assertEquals("premixed solution", doc2Admin4.myAdministrationNotes.get(1).myText);
        assertEquals("300mg/min", doc2Admin4.myAdministeredPerTimeUnit);        
        assertEquals("Intravenous", doc2Admin4.myMedicationRoute.myText);
        

                
    }    
        
    
    
    
    
    
 



    @After
    public void after() {
        Persister.setUnitTestMode(false);
    } 	

}
