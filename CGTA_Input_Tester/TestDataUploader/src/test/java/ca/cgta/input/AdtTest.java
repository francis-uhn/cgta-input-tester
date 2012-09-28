package ca.cgta.input;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.ektorp.ViewQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ca.cgta.couchdb.tools.AllDataPurger;
import ca.cgta.couchdb.tools.ViewUploader;
import ca.cgta.input.converter.Converter;
import ca.cgta.input.listener.Persister;
import ca.cgta.input.model.inner.AdverseReaction;
import ca.cgta.input.model.inner.AssociatedParty;
import ca.cgta.input.model.inner.Ce;
import ca.cgta.input.model.inner.Constants;
import ca.cgta.input.model.inner.Cx;
import ca.cgta.input.model.inner.Diagnosis;
import ca.cgta.input.model.inner.Patient;
import ca.cgta.input.model.inner.PersonInRole;
import ca.cgta.input.model.inner.Pl;
import ca.cgta.input.model.inner.Visit;
import ca.cgta.input.model.inner.Xad;
import ca.cgta.input.model.inner.Xcn;
import ca.cgta.input.model.inner.Xpn;
import ca.cgta.input.model.inner.Xtn;
import ca.cgta.input.model.outer.PatientWithVisits;
import ca.cgta.input.model.outer.PatientWithVisitsContainer;
import ca.uhn.hl7v2.model.v25.message.ADT_A01;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Hl7InputStreamMessageStringIterator;
import ca.uhn.hl7v2.validation.impl.ValidationContextImpl;

public class AdtTest {
    
    
    private static DateFormat ourTsFormat = new SimpleDateFormat("yyyyMMddHHmm");
    private static DateFormat ourTsLongFormat = new SimpleDateFormat("yyyyMMddHHmmZ");
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
	 * This message caused a crash when SMH sent it
	 */
	@Test
	public void testFailingSmhMessage() throws Exception {

		String message = "MSH|^~\\&|2.16.840.1.113883.3.239.23.5^2.16.840.1.113883.3.239.23.5.101.5|SMH^2.16.840.1.113883.3.239.23.5.100.1|ConnectingGTA|ConnectingGTA|20120626173123-0400-0400|1238973875fff|ADT^A07^ADT_A07|00000000018079398|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r" + 
				"EVN||20120626173118\r" + 
				"PID|1||4003288^^^2.16.840.1.113883.3.239.23.5&2.16.840.1.113883.3.239.23.5.101.5^MR||CGTA-SMH^ER-ADMIT^^^^^L|CGTA-SMH^^^^^^L|19660215-0400-0400|M|||11 MICHAEL RD^^TORONTO^^M9I8U7^CAN^H||(444)777-8888^PRN^PH^^^444^7778888||ENG^^HL70296|||||||||||||||N\r" + 
				"NK1|1|JONES-T^JENNY^^^^^L|SPO|9 SMITH RD^^TORONTO^^M3M 3M9^CANADA^H|(416)222-4888^PRN^PH^^^416^2224888\r" + 
				"PV1|1|E|ERMAJ^^^2.16.840.1.113883.3.239.23.5&2.16.840.1.113883.3.239.23.5.100.1|E||^^^2.16.840.1.113883.3.239.23.5&2.16.840.1.113883.3.239.23.5.100.1|^GRAHAM^ANTHONY^F^^^^^&2.16.840.1.113883.3.239.23.5&2.16.840.1.113883.3.239.23.5.101.5|||EMA|||||||^^ANTHONY^F^^^^^&2.16.840.1.113883.3.239.23.5&2.16.840.1.113883.3.239.23.5.101.5|||||||||||||||||||||||||||20120626172500-040000-0400";

		PipeParser parser = new PipeParser();
		parser.setValidationContext(new ValidationContextImpl());
		ADT_A01 hl7Msg = new ADT_A01();
		hl7Msg.setParser(parser);
		hl7Msg.parse(message);
		
		PatientWithVisits converted = new Converter().convertPatientWithVisits(hl7Msg);
		Persister.persist(converted);
		
	}
	
    /**
     * Testing inpatient admit A01
     *   
     * @throws Exception ...
     */
    @Test
    public void testAdtA01() throws Exception {
        
        String message1 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A01^ADT_A01|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A01|201112021621||||201112021621|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^20010808^200208080101-0400|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "ROL||UC|PP^Primary Care Provider^15ZRole^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^^^^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|O^Office^^^^|^^^ON^^Can^^^^^^^^|(416) 340-3391^WPN^PH^^^^^^^^^\r" + 
                "ROL||UC|PP^Primary Care Provider^15ZRole^^^|13546b^Genericb^Physicianb^Moeb^^Dr.^MD^^UHN^L^^^EI^G^2.16.840.1.113883.3.59.3:947^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|1^Hospital^15ZOrgTyp^^^|^^^ON^^Can^^^^^^^^|(416) 340-3388^WPN^PH^^^^^^^^^\r" +
                "NK1|1|Wph^Mom^^^^^L^^^^^^^|PAR^Parent^03ZRelshp^^^|82 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^~(416)123-1234^PRN^CP^^^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^|N^Next-of-Kin^03ZConRol^^^||||||||||||||||||||||||||||||||\r" +
                "NK1|2|Wph^Brother^^^^^L^^^^^^^|BRO^Brother^03ZRelshp^^^|83 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3601^PRN^PH^^^^^^^^^|(416)525-2525^PRN^PH^^^^^^^^^|C^Emergency Contact^03ZConRol^^^||||||||||||||||||||||||||||||||\r" + 
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000514^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r" + 
                "PV2||S^Semi^03ZFinbed^^^|^kfkfkfjcjcjcgcgcclcl^03ZAmitRes^^^|||||||||||||||||||N||AI|Elective||||||N|||||||OTH^Self^03ZBrInBy^^^|||||||||||\r" + 
                "DG1|1||06^KFKFKFJCJCJCGCGCCLCL^2.16.840.1.113883.11.19436^^^|KFKFKFJCJCJCGCGCCLCL|201112021621|A|||||||||1||D||||\r" + 
                "PR1||||||||||||||||||||\r" + 
                "ZPV|OTH^Self|F^Standard|||N|||13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||Medical Services^General Internal Medicine^GMED|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|413||3910||^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r" + 
                "ZWA||||||||active|\r";
       
        
        PatientWithVisits converted = UhnConverter.convertAdtOrFail(message1);

        Persister.persist(converted);
        
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
        
        PatientWithVisitsContainer pwvContainer = pwvContainers.get(0);
        Patient patient = pwvContainer.getDocument().myPatient;
        Visit visit = pwvContainer.getDocument().myVisits.get(0);
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > patient.myRecordUpdatedDate.getTime());
        
        
        //check patient
        assertEquals("F", patient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), patient.myDateOfBirth);
        assertEquals(null, patient.myDeathDateAndTime);
        assertEquals("N", patient.myDeathIndicator);
        assertEquals(null, patient.myMothersMaidenName);
        
        
        Cx patId = patient.myPatientIds.get(0);
        assertEquals("7012673", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Xpn patName = patient.myPatientNames.get(0);
        assertEquals("Test", patName.myLastName);
        assertEquals("Majaconversion", patName.myFirstName);
                
        Xad patAddresses = patient.myPatientAddresses.get(0);
        assertEquals("1 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        assertEquals(ourDtFormat.parse("20010808"), patAddresses.myEffectiveDate);
        assertEquals(ourTsLongFormat.parse("200208080101-0400"), patAddresses.myExpirationDate);
        
        Xtn patPhone = patient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        Ce lang = patient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
        
        
        //check patient roles
        List<PersonInRole> roles  = patient.myPersonInRoles;
        assertEquals(2, roles.size());
        PersonInRole rol_1 = patient.myPersonInRoles.get(0);
        PersonInRole rol_2 = patient.myPersonInRoles.get(1);        
        //ROL(1)
        assertEquals("PP",rol_1.myRole.myCode);
        assertEquals("Primary Care Provider",rol_1.myRole.myText);        
        assertEquals("Generic", rol_1.myPersonNames.get(0).myLastName);
        assertEquals("Physician", rol_1.myPersonNames.get(0).myFirstName);
        assertEquals("Moe", rol_1.myPersonNames.get(0).myMiddleName);
        assertEquals("(416) 340-3391", rol_1.myContactInformation.get(0).myPhoneNumber);
        //ROL(2)
        assertEquals("PP",rol_2.myRole.myCode);
        assertEquals("Primary Care Provider",rol_2.myRole.myText);        
        assertEquals("Genericb", rol_2.myPersonNames.get(0).myLastName);
        assertEquals("Physicianb", rol_2.myPersonNames.get(0).myFirstName);
        assertEquals("Moeb", rol_2.myPersonNames.get(0).myMiddleName);
        assertEquals("(416) 340-3388", rol_2.myContactInformation.get(0).myPhoneNumber);
        
        
        //check patient associated parties
        List<AssociatedParty> parties  = patient.myAssociatedParties;
        assertEquals(2, parties.size());
        AssociatedParty party_1 = patient.myAssociatedParties.get(0);
        AssociatedParty party_2 = patient.myAssociatedParties.get(1);        
        //NK1(1)
        assertEquals("Wph",party_1.myNames.get(0).myLastName);
        assertEquals("Mom",party_1.myNames.get(0).myFirstName);
        assertEquals("PAR",party_1.myRelationship.myCode);
        assertEquals("Parent",party_1.myRelationshipName);
        assertEquals("82 Buttonwood Avenue",party_1.myAddresses.get(0).myStreetAddress);
        assertEquals("(416)243-3600",party_1.myContactInformation.get(0).myPhoneNumber);
        //NK1(2)
        assertEquals("Wph",party_2.myNames.get(0).myLastName);
        assertEquals("Brother",party_2.myNames.get(0).myFirstName);
        assertEquals("BRO",party_2.myRelationship.myCode);
        assertEquals("Brother",party_2.myRelationshipName);
        assertEquals("83 Buttonwood Avenue",party_2.myAddresses.get(0).myStreetAddress);
        assertEquals("(416)243-3601",party_2.myContactInformation.get(0).myPhoneNumber);
        
        
        //check visit lastUpdateTime
        assertTrue(nowMilli > visit.myRecordUpdatedDate.getTime());
                                
        
        //check visit
        assertEquals("11110000514", visit.myVisitNumber.myIdNumber);
        assertEquals(Constants.ACTIVE_VISIT_STATUS, visit.myVisitStatus);
        assertEquals("I", visit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112021621"), visit.myAdmitDate);
        assertEquals("hospServ", visit.myHospitalService);
        Pl loc = visit.myAssignedPatientLocation;
        assertEquals("PMH 15C", loc.myPointOfCare);
        assertEquals("413", loc.myRoom);
        assertEquals("2", loc.myBed);
        
        Xcn admitDoc = visit.myAdmittingDoctors.get(0);
        assertEquals("13546c", admitDoc.myId);
        assertEquals("Physician", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("MoeC", admitDoc.myMiddleName);
        
        Xcn attendDoc = visit.myAttendingDoctors.get(0);
        assertEquals("13546a", attendDoc.myId);
        assertEquals("Physician", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("MoeA", attendDoc.myMiddleName);
        
        Xcn refDoc = visit.myReferringDoctors.get(0);
        assertEquals("13546b", refDoc.myId);
        assertEquals("Physician", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("MoeB", refDoc.myMiddleName);
        
         
        Diagnosis dg = visit.myDiagnoses.get(0);
        Ce dgCode = dg.myDiagnosis;
        assertEquals("06", dgCode.myCode);
        assertEquals("KFKFKFJCJCJCGCGCCLCL", dgCode.myText);
        assertEquals("2.16.840.1.113883.11.19436", dgCode.myCodeSystem);
        
        
        
    }
	
	
	
	
	
	
	/**
	 * Testing patient transfer A02
	 * - admit patient into a bed and then transfer him to another bed
	 *  
	 * @throws Exception ...
	 */
	@Test
	public void testAdtA02() throws Exception {
		
		String message1 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A01^ADT_A01|123484|T^|2.5^^||||||CAN||||\r" + 
				"EVN|A01|201112021621||||201112021621|G^4265^L\r" + 
				"PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
				"PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" +				 
                "ROL||UC|PP^Primary Care Provider^15ZRole^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^^^^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|O^Office^^^^|^^^ON^^Can^^^^^^^^|(416) 340-3391^WPN^PH^^^^^^^^^\r" + 
                "ROL||UC|PP^Primary Care Provider^15ZRole^^^|13546b^Genericb^Physicianb^Moeb^^Dr.^MD^^UHN^L^^^EI^G^2.16.840.1.113883.3.59.3:947^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|1^Hospital^15ZOrgTyp^^^|^^^ON^^Can^^^^^^^^|(416) 340-3388^WPN^PH^^^^^^^^^\r" +
                "NK1|1|Wph^Mom^^^^^L^^^^^^^|PAR^Parent^03ZRelshp^^^|82 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^~(416)123-1234^PRN^CP^^^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^|N^Next-of-Kin^03ZConRol^^^||||||||||||||||||||||||||||||||\r" +
                "NK1|2|Wph^Brother^^^^^L^^^^^^^|BRO^Brother^03ZRelshp^^^|83 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3601^PRN^PH^^^^^^^^^|(416)525-2525^PRN^PH^^^^^^^^^|C^Emergency Contact^03ZConRol^^^||||||||||||||||||||||||||||||||\r" + 
				"PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000514^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r" + 
				"PV2||S^Semi^03ZFinbed^^^|^kfkfkfjcjcjcgcgcclcl^03ZAmitRes^^^|||||||||||||||||||N||AI|Elective||||||N|||||||OTH^Self^03ZBrInBy^^^|||||||||||\r" + 
				"DG1|1||06^KFKFKFJCJCJCGCGCCLCL^2.16.840.1.113883.11.19436^^^|KFKFKFJCJCJCGCGCCLCL|201112021621|A|||||||||1||D||||\r" + 
				"PR1||||||||||||||||||||\r" + 
				"ZPV|OTH^Self|F^Standard|||N|||13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||Medical Services^General Internal Medicine^GMED|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|413||3910||^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r" + 
				"ZWA||||||||active|\r";
		
		String message2 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201201111123||ADT^A02^ADT_A02|124423|T^|2.5^^||||||CAN||||\r" + 
				"EVN|A02|201201111123||||201201111123|G^4265^L\r" + 
				"PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
				"PV1||I|ES9 GEN S^424^1^G^4265^^^N^ES 9 424^ES 9 424 1^ES9 GEN S^1521 19 1^|R||PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000514^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r" +
                "PV2||S^Semi^03ZFinbed^^^|^kfkfkfjcjcjcgcgcclcl^03ZAmitRes^^^|||||||||||||||||||N||AI|Elective||||||N|||||||OTH^Self^03ZBrInBy^^^|||||||||||\r" + 
                "DG1|1||||||||||||||||||||\r" +
                "PR1||||||||||||||||||||\r" + 
                "ZPV|OTH^Self|F^Standard|||N|||13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||Medical Services^General Internal Medicine^GMED|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|413||3910||^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r" + 
                "ZWA||||||||active|\r";
		
		
		
		Persister.persist(UhnConverter.convertAdtOrFail(message1));
		Persister.persist(UhnConverter.convertAdtOrFail(message2));
		
		ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
		List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
		
		PatientWithVisitsContainer pwvContainer = pwvContainers.get(0);
		Patient patient = pwvContainer.getDocument().myPatient;
		Visit visit = pwvContainer.getDocument().myVisits.get(0);
		
		
		//check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > patient.myRecordUpdatedDate.getTime());

        
		//check patient
        assertEquals("F", patient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), patient.myDateOfBirth);
        assertEquals(null, patient.myDeathDateAndTime);
        assertEquals("N", patient.myDeathIndicator);
        assertEquals(null, patient.myMothersMaidenName);
        
        
        Cx patId = patient.myPatientIds.get(0);
        assertEquals("7012673", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Xpn patName = patient.myPatientNames.get(0);
        assertEquals("Test", patName.myLastName);
        assertEquals("Majaconversion", patName.myFirstName);
                
        Xad patAddresses = patient.myPatientAddresses.get(0);
        assertEquals("1 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        Xtn patPhone = patient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        Ce lang = patient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
        
        //check patient roles
        List<PersonInRole> roles  = patient.myPersonInRoles;
        assertEquals(2, roles.size());
        PersonInRole rol_1 = patient.myPersonInRoles.get(0);
        PersonInRole rol_2 = patient.myPersonInRoles.get(1);        
        //ROL(1)
        assertEquals("PP",rol_1.myRole.myCode);
        assertEquals("Primary Care Provider",rol_1.myRole.myText);        
        assertEquals("Generic", rol_1.myPersonNames.get(0).myLastName);
        assertEquals("Physician", rol_1.myPersonNames.get(0).myFirstName);
        assertEquals("Moe", rol_1.myPersonNames.get(0).myMiddleName);
        assertEquals("(416) 340-3391", rol_1.myContactInformation.get(0).myPhoneNumber);
        //ROL(2)
        assertEquals("PP",rol_2.myRole.myCode);
        assertEquals("Primary Care Provider",rol_2.myRole.myText);        
        assertEquals("Genericb", rol_2.myPersonNames.get(0).myLastName);
        assertEquals("Physicianb", rol_2.myPersonNames.get(0).myFirstName);
        assertEquals("Moeb", rol_2.myPersonNames.get(0).myMiddleName);
        assertEquals("(416) 340-3388", rol_2.myContactInformation.get(0).myPhoneNumber);
        
        //check patient associated parties
        List<AssociatedParty> parties  = patient.myAssociatedParties;
        assertEquals(2, parties.size());
        AssociatedParty party_1 = patient.myAssociatedParties.get(0);
        AssociatedParty party_2 = patient.myAssociatedParties.get(1);        
        //NK1(1)
        assertEquals("Wph",party_1.myNames.get(0).myLastName);
        assertEquals("Mom",party_1.myNames.get(0).myFirstName);
        assertEquals("PAR",party_1.myRelationship.myCode);
        assertEquals("Parent",party_1.myRelationshipName);
        assertEquals("82 Buttonwood Avenue",party_1.myAddresses.get(0).myStreetAddress);
        assertEquals("(416)243-3600",party_1.myContactInformation.get(0).myPhoneNumber);
        //NK1(2)
        assertEquals("Wph",party_2.myNames.get(0).myLastName);
        assertEquals("Brother",party_2.myNames.get(0).myFirstName);
        assertEquals("BRO",party_2.myRelationship.myCode);
        assertEquals("Brother",party_2.myRelationshipName);
        assertEquals("83 Buttonwood Avenue",party_2.myAddresses.get(0).myStreetAddress);
        assertEquals("(416)243-3601",party_2.myContactInformation.get(0).myPhoneNumber);
        
        
        //check visit lastUpdateTime
        assertTrue(nowMilli > visit.myRecordUpdatedDate.getTime());

        
		//check visit
		assertEquals("11110000514", visit.myVisitNumber.myIdNumber);
		assertEquals(Constants.ACTIVE_VISIT_STATUS, visit.myVisitStatus);
		assertEquals("I", visit.myPatientClassCode);
		assertEquals(ourTsFormat.parse("201112021621"), visit.myAdmitDate);
		assertEquals("hospServ", visit.myHospitalService);
		
		Pl ploc = visit.myPriorPatientLocation;
		assertEquals("PMH 15C", ploc.myPointOfCare);
		assertEquals("413", ploc.myRoom);
		assertEquals("2", ploc.myBed);
				
		Pl cloc = visit.myAssignedPatientLocation;
        assertEquals("ES9 GEN S", cloc.myPointOfCare);
        assertEquals("424", cloc.myRoom);
        assertEquals("1", cloc.myBed);
		
		
		Xcn admitDoc = visit.myAdmittingDoctors.get(0);
		assertEquals("13546c", admitDoc.myId);
		assertEquals("Physician", admitDoc.myFirstName);
		assertEquals("Generic", admitDoc.myLastName);
		assertEquals("MoeC", admitDoc.myMiddleName);
		
		Xcn attendDoc = visit.myAttendingDoctors.get(0);
        assertEquals("13546a", attendDoc.myId);
        assertEquals("Physician", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("MoeA", attendDoc.myMiddleName);
		
		Xcn refDoc = visit.myReferringDoctors.get(0);
        assertEquals("13546b", refDoc.myId);
        assertEquals("Physician", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("MoeB", refDoc.myMiddleName);
        
        Diagnosis dg = visit.myDiagnoses.get(0);
        Ce dgCode = dg.myDiagnosis;
        assertEquals("06", dgCode.myCode);
        assertEquals("KFKFKFJCJCJCGCGCCLCL", dgCode.myText);
        assertEquals("2.16.840.1.113883.11.19436", dgCode.myCodeSystem);
        
        
        
        
		
	}
	
	
	
	
    /**
     * Testing patient discharge A03
     * - admit patient into a bed and then discharge him
     *  
     * @throws Exception ...
     */
    @Test
    public void testAdtA03() throws Exception {
        
        String message1 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A01^ADT_A01|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A01|201112021621||||201112021621|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000514^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r" + 
                "PV2||S^Semi^03ZFinbed^^^|^kfkfkfjcjcjcgcgcclcl^03ZAmitRes^^^|||||||||||||||||||N||AI|Elective||||||N|||||||OTH^Self^03ZBrInBy^^^|||||||||||\r" + 
                "DG1|1||06^KFKFKFJCJCJCGCGCCLCL^2.16.840.1.113883.11.19436|KFKFKFJCJCJCGCGCCLCL|201112021621|A|||||||||1||D||||\r" + 
                "PR1||||||||||||||||||||\r" + 
                "ZPV|OTH^Self|F^Standard|||N|||13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||Medical Services^General Internal Medicine^GMED|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|413||3910||^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r" + 
                "ZWA||||||||active|\r";
        
        String message2 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112070000||ADT^A03^ADT_A03|123710|T^|2.5^^||||||CAN||||\r" + 
        		"EVN|A03|201112070000||||201112070000|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
        		"PV1||I||C||PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||A|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000514^^^UHN^VN^G^4265^^^^^|||||||||||||||||1|||G|||||201112021621|201112052359||||||V|\r" + 
                "PV2||S^Semi^03ZFinbed^^^|^kfkfkfjcjcjcgcgcclcl^03ZAmitRes^^^|||||||||||||||||||N||AI|Elective||||||N|||||||OTH^Self^03ZBrInBy^^^|||||||||||\r" + 
        		"DG1|1||||||||||||||||||||\r" +
                "PR1||||||||||||||||||||\r" + 
        		"IN1|1||001001^^^UHN^^G^4265^^^^^|OHIP^D^001001^^^UHN^^G^4265^^^|||||||||20111201|||Test^Majaconversion^^^^^L^^^^^^^|SEL^Self^15ZRelshp^^^||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^||||||||||||||||||||||||||||H||7012673^^^UHN^PI^G^4265^^^^^||||\r" + 
        		"IN2||||||9287170261||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||\r" + 
        		"IN3|1||||||||||||||||||||||||\r" + 
        		"ZIN||||||||||||||||||\r" + 
        		"ZPV|OTH^Self|^|||N|||3210^Generic^Physician-EM^^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|6835^Generic^Anaesthetist^^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||^^|3210^Generic^Physician-EM^^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|||||^^^^^|3210^Generic^Physician-EM^^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r";
        
        
        
        
        Persister.persist(UhnConverter.convertAdtOrFail(message1));
        Persister.persist(UhnConverter.convertAdtOrFail(message2));
        
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
        
        PatientWithVisitsContainer pwvContainer = pwvContainers.get(0);
        Patient patient = pwvContainer.getDocument().myPatient;
        Visit visit = pwvContainer.getDocument().myVisits.get(0);
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > patient.myRecordUpdatedDate.getTime());
        
        
        //check patient
        assertEquals("F", patient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), patient.myDateOfBirth);
        assertEquals(null, patient.myDeathDateAndTime);
        assertEquals("N", patient.myDeathIndicator);
        assertEquals(null, patient.myMothersMaidenName);
        
        
        Cx patId = patient.myPatientIds.get(0);
        assertEquals("7012673", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Xpn patName = patient.myPatientNames.get(0);
        assertEquals("Test", patName.myLastName);
        assertEquals("Majaconversion", patName.myFirstName);
                
        Xad patAddresses = patient.myPatientAddresses.get(0);
        assertEquals("1 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        Xtn patPhone = patient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        Ce lang = patient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
        
        //check visit lastUpdateTime
        assertTrue(nowMilli > visit.myRecordUpdatedDate.getTime());
        
        //check visit
        assertEquals("11110000514", visit.myVisitNumber.myIdNumber);
        assertEquals(Constants.INACTIVE_VISIT_STATUS, visit.myVisitStatus);
        assertEquals("I", visit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112021621"), visit.myAdmitDate);
        assertEquals(ourTsFormat.parse("201112052359"), visit.myDischargeDates.get(0));
        assertEquals("hospServ", visit.myHospitalService);
        
        Pl loc = visit.myPriorPatientLocation;
        assertEquals("PMH 15C", loc.myPointOfCare);
        assertEquals("413", loc.myRoom);
        assertEquals("2", loc.myBed);
        
                      
        
        Xcn admitDoc = visit.myAdmittingDoctors.get(0);
        assertEquals("13546c", admitDoc.myId);
        assertEquals("Physician", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("MoeC", admitDoc.myMiddleName);
        
        Xcn attendDoc = visit.myAttendingDoctors.get(0);
        assertEquals("13546a", attendDoc.myId);
        assertEquals("Physician", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("MoeA", attendDoc.myMiddleName);
        
        Xcn refDoc = visit.myReferringDoctors.get(0);
        assertEquals("13546b", refDoc.myId);
        assertEquals("Physician", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("MoeB", refDoc.myMiddleName);
        
        Diagnosis dg = visit.myDiagnoses.get(0);
        Ce dgCode = dg.myDiagnosis;
        assertEquals("06", dgCode.myCode);
        assertEquals("KFKFKFJCJCJCGCGCCLCL", dgCode.myText);
        assertEquals("2.16.840.1.113883.11.19436", dgCode.myCodeSystem);
        
        
    }
	
	
	

	
	
	
    /**
     * Testing outpaient registration A04
     * @throws Exception ...
     */
    @Test
    public void testAdtA04() throws Exception {
        
        String message = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112151022||ADT^A04^ADT_A01|123766|T^|2.5^^||||||CAN||||\r" + 
        		"EVN|A04|201112151022||||201112151022|G^4265^L\r" + 
        		"PID|||7007541^^^UHN^MR^^^^^^^~^^^^JHN^^^^^^^~||Medinfo^Hyphen^^^^^L^^^^^200809301409^^~||19630317|M|||20 Dundas Street^^TORONTO^ON^M5G 2C2^Can^H^^^^^^^~|1811|(416)123-4567^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11130000114^^^UHN^VN^^^^^^^~||||||||||||N|||200809301409||||||\r" + 
        		"PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
        		"ROL||UC|PP^Primary Care Provider^15ZRole^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^^^^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|O^Office^^^^|^^^ON^^Can^^^^^^^^|(416) 340-3391^WPN^PH^^^^^^^^^\r" + 
                "ROL||UC|PP^Primary Care Provider^15ZRole^^^|13546b^Genericb^Physicianb^Moeb^^Dr.^MD^^UHN^L^^^EI^G^2.16.840.1.113883.3.59.3:947^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|1^Hospital^15ZOrgTyp^^^|^^^ON^^Can^^^^^^^^|(416) 340-3388^WPN^PH^^^^^^^^^\r" +
                "NK1|1|Wph^Mom^^^^^L^^^^^^^|PAR^Parent^03ZRelshp^^^|82 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^~(416)123-1234^PRN^CP^^^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^|N^Next-of-Kin^03ZConRol^^^||||||||||||||||||||||||||||||||\r" +
                "NK1|2|Wph^Brother^^^^^L^^^^^^^|BRO^Brother^03ZRelshp^^^|83 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3601^PRN^PH^^^^^^^^^|(416)525-2525^PRN^PH^^^^^^^^^|C^Emergency Contact^03ZConRol^^^||||||||||||||||||||||||||||||||\r" +
        		"PV1||O|8001-CC-Endoscopy^^^G^4265^^^C^^^Endoscopy^112^|||^^^G^4265^^^^^^^  ^|13893^Generic^Moe^PhysicianThree^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13893^Generic^Moe^PhysicianThree^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||||||A|||13893^Generic^Moe^PhysicianThree^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|OP^|11130000114^^^UHN^VN^G^4265^^^^^||||||||||||||||||||G|||||201112151022|||||||V|\r" + 
        		"PV2|||^^^^^|||||||||||||||||||N||AO|||||||N||||||||||||||||||\r" + 
        		"DG1|1||||||||||||||||||||\r" + 
        		"PR1||||||||||||||||||||\r" + 
        		"ZPV|^|^|||N|||13893^Generic^Moe^PhysicianThree^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|13893^Generic^Moe^PhysicianThree^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||^^|13893^Generic^Moe^PhysicianThree^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|||||^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r";

        
        PatientWithVisits converted = UhnConverter.convertAdtOrFail(message);
        Persister.persist(converted);
        
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
        
        PatientWithVisitsContainer pwvContainer = pwvContainers.get(0);
        Patient patient = pwvContainer.getDocument().myPatient;
        Visit visit = pwvContainer.getDocument().myVisits.get(0);
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > patient.myRecordUpdatedDate.getTime());
        
        //check patient
        assertEquals("M", patient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("196303170000"), patient.myDateOfBirth);
        assertEquals(null, patient.myDeathDateAndTime);
        assertEquals("N", patient.myDeathIndicator);
        assertEquals(null, patient.myMothersMaidenName);
        
        Cx patId = patient.myPatientIds.get(0);
        assertEquals("7007541", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Xpn patName = patient.myPatientNames.get(0);
        assertEquals("Medinfo", patName.myLastName);
        assertEquals("Hyphen", patName.myFirstName);
                
        Xad patAddresses = patient.myPatientAddresses.get(0);
        assertEquals("20 Dundas Street", patAddresses.myStreetAddress);
        assertEquals("TORONTO", patAddresses.myCity);
        
        Xtn patPhone = patient.myPhoneNumbers.get(0);
        assertEquals("(416)123-4567", patPhone.myPhoneNumber);
        

        //check patient roles
        List<PersonInRole> roles  = patient.myPersonInRoles;
        assertEquals(2, roles.size());
        PersonInRole rol_1 = patient.myPersonInRoles.get(0);
        PersonInRole rol_2 = patient.myPersonInRoles.get(1);        
        //ROL(1)
        assertEquals("PP",rol_1.myRole.myCode);
        assertEquals("Primary Care Provider",rol_1.myRole.myText);        
        assertEquals("Generic", rol_1.myPersonNames.get(0).myLastName);
        assertEquals("Physician", rol_1.myPersonNames.get(0).myFirstName);
        assertEquals("Moe", rol_1.myPersonNames.get(0).myMiddleName);
        assertEquals("(416) 340-3391", rol_1.myContactInformation.get(0).myPhoneNumber);
        //ROL(2)
        assertEquals("PP",rol_2.myRole.myCode);
        assertEquals("Primary Care Provider",rol_2.myRole.myText);        
        assertEquals("Genericb", rol_2.myPersonNames.get(0).myLastName);
        assertEquals("Physicianb", rol_2.myPersonNames.get(0).myFirstName);
        assertEquals("Moeb", rol_2.myPersonNames.get(0).myMiddleName);
        assertEquals("(416) 340-3388", rol_2.myContactInformation.get(0).myPhoneNumber);
        
        
        //check patient associated parties
        List<AssociatedParty> parties  = patient.myAssociatedParties;
        assertEquals(2, parties.size());
        AssociatedParty party_1 = patient.myAssociatedParties.get(0);
        AssociatedParty party_2 = patient.myAssociatedParties.get(1);        
        //NK1(1)
        assertEquals("Wph",party_1.myNames.get(0).myLastName);
        assertEquals("Mom",party_1.myNames.get(0).myFirstName);
        assertEquals("PAR",party_1.myRelationship.myCode);
        assertEquals("Parent",party_1.myRelationshipName);
        assertEquals("82 Buttonwood Avenue",party_1.myAddresses.get(0).myStreetAddress);
        assertEquals("(416)243-3600",party_1.myContactInformation.get(0).myPhoneNumber);
        //NK1(2)
        assertEquals("Wph",party_2.myNames.get(0).myLastName);
        assertEquals("Brother",party_2.myNames.get(0).myFirstName);
        assertEquals("BRO",party_2.myRelationship.myCode);
        assertEquals("Brother",party_2.myRelationshipName);
        assertEquals("83 Buttonwood Avenue",party_2.myAddresses.get(0).myStreetAddress);
        assertEquals("(416)243-3601",party_2.myContactInformation.get(0).myPhoneNumber);
        
       
        //check visit lastUpdateTime
        assertTrue(nowMilli > visit.myRecordUpdatedDate.getTime());
        
        //check visit
        assertEquals("11130000114", visit.myVisitNumber.myIdNumber);
        assertEquals(Constants.ACTIVE_VISIT_STATUS, visit.myVisitStatus);
        assertEquals("O", visit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112151022"), visit.myAdmitDate);
        assertEquals(null, visit.myHospitalService);
        Pl loc = visit.myAssignedPatientLocation;
        assertEquals("8001-CC-Endoscopy", loc.myPointOfCare);
        assertEquals(null, loc.myRoom);
        assertEquals(null, loc.myBed);
        
        Xcn admitDoc = visit.myAdmittingDoctors.get(0);
        assertEquals("13893", admitDoc.myId);
        assertEquals("Moe", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("PhysicianThree", admitDoc.myMiddleName);
        
        Xcn attendDoc = visit.myAttendingDoctors.get(0);
        assertEquals("13893", attendDoc.myId);
        assertEquals("Moe", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("PhysicianThree", attendDoc.myMiddleName);
        
        Xcn refDoc = visit.myReferringDoctors.get(0);
        assertEquals("13893", refDoc.myId);
        assertEquals("Moe", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("PhysicianThree", refDoc.myMiddleName);
        
        
        
    }
    
    
    
    
    /**
     * Testing pre-admit visit processing
     * @throws Exception ...
     */
    @Test
    public void testAdtA05() throws Exception {
        
        String message = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112151022||ADT^A05^ADT_A05|123766|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A05|201112151022||||201112151022|G^4265^L\r" + 
                "PID|||7007541^^^UHN^MR^^^^^^^~^^^^JHN^^^^^^^~||Medinfo^Hyphen^^^^^L^^^^^200809301409^^~||19630317|M|||20 Dundas Street^^TORONTO^ON^M5G 2C2^Can^H^^^^^^^~|1811|(416)123-4567^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11130000114^^^UHN^VN^^^^^^^~||||||||||||N|||200809301409||||||\r" + 
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "ROL||UC|PP^Primary Care Provider^15ZRole^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^^^^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|O^Office^^^^|^^^ON^^Can^^^^^^^^|(416) 340-3391^WPN^PH^^^^^^^^^\r" + 
                "ROL||UC|PP^Primary Care Provider^15ZRole^^^|13546b^Genericb^Physicianb^Moeb^^Dr.^MD^^UHN^L^^^EI^G^2.16.840.1.113883.3.59.3:947^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|1^Hospital^15ZOrgTyp^^^|^^^ON^^Can^^^^^^^^|(416) 340-3388^WPN^PH^^^^^^^^^\r" +
                "NK1|1|Wph^Mom^^^^^L^^^^^^^|PAR^Parent^03ZRelshp^^^|82 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^~(416)123-1234^PRN^CP^^^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^|N^Next-of-Kin^03ZConRol^^^||||||||||||||||||||||||||||||||\r" +
                "NK1|2|Wph^Brother^^^^^L^^^^^^^|BRO^Brother^03ZRelshp^^^|83 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3601^PRN^PH^^^^^^^^^|(416)525-2525^PRN^PH^^^^^^^^^|C^Emergency Contact^03ZConRol^^^||||||||||||||||||||||||||||||||\r" +
                "PV1||O|8001-CC-Endoscopy^^^G^4265^^^C^^^Endoscopy^112^|||^^^G^4265^^^^^^^  ^|13893^Generic^Moe^PhysicianThree^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13893^Generic^Moe^PhysicianThree^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||||||A|||13893^Generic^Moe^PhysicianThree^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|OP^|11130000114^^^UHN^VN^G^4265^^^^^||||||||||||||||||||G|||||201112151022|||||||V|\r" + 
                "PV2|||^^^^^|||||||||||||||||||N||AO|||||||N||||||||||||||||||\r" + 
                "DG1|1||||||||||||||||||||\r" + 
                "PR1||||||||||||||||||||\r" + 
                "ZPV|^|^|||N|||13893^Generic^Moe^PhysicianThree^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|13893^Generic^Moe^PhysicianThree^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||^^|13893^Generic^Moe^PhysicianThree^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|||||^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r";
  
        
        PatientWithVisits converted = UhnConverter.convertAdtOrFail(message);
        Persister.persist(converted);
        
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
        
        PatientWithVisitsContainer pwvContainer = pwvContainers.get(0);
        Patient patient = pwvContainer.getDocument().myPatient;
        Visit visit = pwvContainer.getDocument().myVisits.get(0);
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > patient.myRecordUpdatedDate.getTime());
        
        //check patient
        assertEquals("M", patient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("196303170000"), patient.myDateOfBirth);
        assertEquals(null, patient.myDeathDateAndTime);
        assertEquals("N", patient.myDeathIndicator);
        assertEquals(null, patient.myMothersMaidenName);
        
        Cx patId = patient.myPatientIds.get(0);
        assertEquals("7007541", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Xpn patName = patient.myPatientNames.get(0);
        assertEquals("Medinfo", patName.myLastName);
        assertEquals("Hyphen", patName.myFirstName);
                
        Xad patAddresses = patient.myPatientAddresses.get(0);
        assertEquals("20 Dundas Street", patAddresses.myStreetAddress);
        assertEquals("TORONTO", patAddresses.myCity);
        
        Xtn patPhone = patient.myPhoneNumbers.get(0);
        assertEquals("(416)123-4567", patPhone.myPhoneNumber);
        

        //check patient roles
        List<PersonInRole> roles  = patient.myPersonInRoles;
        assertEquals(2, roles.size());
        PersonInRole rol_1 = patient.myPersonInRoles.get(0);
        PersonInRole rol_2 = patient.myPersonInRoles.get(1);        
        //ROL(1)
        assertEquals("PP",rol_1.myRole.myCode);
        assertEquals("Primary Care Provider",rol_1.myRole.myText);        
        assertEquals("Generic", rol_1.myPersonNames.get(0).myLastName);
        assertEquals("Physician", rol_1.myPersonNames.get(0).myFirstName);
        assertEquals("Moe", rol_1.myPersonNames.get(0).myMiddleName);
        assertEquals("(416) 340-3391", rol_1.myContactInformation.get(0).myPhoneNumber);
        //ROL(2)
        assertEquals("PP",rol_2.myRole.myCode);
        assertEquals("Primary Care Provider",rol_2.myRole.myText);        
        assertEquals("Genericb", rol_2.myPersonNames.get(0).myLastName);
        assertEquals("Physicianb", rol_2.myPersonNames.get(0).myFirstName);
        assertEquals("Moeb", rol_2.myPersonNames.get(0).myMiddleName);
        assertEquals("(416) 340-3388", rol_2.myContactInformation.get(0).myPhoneNumber);
        
        
        //check patient associated parties
        List<AssociatedParty> parties  = patient.myAssociatedParties;
        assertEquals(2, parties.size());
        AssociatedParty party_1 = patient.myAssociatedParties.get(0);
        AssociatedParty party_2 = patient.myAssociatedParties.get(1);        
        //NK1(1)
        assertEquals("Wph",party_1.myNames.get(0).myLastName);
        assertEquals("Mom",party_1.myNames.get(0).myFirstName);
        assertEquals("PAR",party_1.myRelationship.myCode);
        assertEquals("Parent",party_1.myRelationshipName);
        assertEquals("82 Buttonwood Avenue",party_1.myAddresses.get(0).myStreetAddress);
        assertEquals("(416)243-3600",party_1.myContactInformation.get(0).myPhoneNumber);
        //NK1(2)
        assertEquals("Wph",party_2.myNames.get(0).myLastName);
        assertEquals("Brother",party_2.myNames.get(0).myFirstName);
        assertEquals("BRO",party_2.myRelationship.myCode);
        assertEquals("Brother",party_2.myRelationshipName);
        assertEquals("83 Buttonwood Avenue",party_2.myAddresses.get(0).myStreetAddress);
        assertEquals("(416)243-3601",party_2.myContactInformation.get(0).myPhoneNumber);
        
       
        //check visit lastUpdateTime
        assertTrue(nowMilli > visit.myRecordUpdatedDate.getTime());
        
        //check visit
        assertEquals("11130000114", visit.myVisitNumber.myIdNumber);
        assertEquals(Constants.PREADMIT_VISIT_STATUS, visit.myVisitStatus);
        assertEquals("O", visit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112151022"), visit.myAdmitDate);
        assertEquals(null, visit.myHospitalService);
        Pl loc = visit.myAssignedPatientLocation;
        assertEquals("8001-CC-Endoscopy", loc.myPointOfCare);
        assertEquals(null, loc.myRoom);
        assertEquals(null, loc.myBed);
        
        Xcn admitDoc = visit.myAdmittingDoctors.get(0);
        assertEquals("13893", admitDoc.myId);
        assertEquals("Moe", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("PhysicianThree", admitDoc.myMiddleName);
        
        Xcn attendDoc = visit.myAttendingDoctors.get(0);
        assertEquals("13893", attendDoc.myId);
        assertEquals("Moe", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("PhysicianThree", attendDoc.myMiddleName);
        
        Xcn refDoc = visit.myReferringDoctors.get(0);
        assertEquals("13893", refDoc.myId);
        assertEquals("Moe", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("PhysicianThree", refDoc.myMiddleName);
        
        
        
    }    
    
    
    
    
    /**
     * Testing patient visit conversion from type E to I A06
     * - admit an emerg patient
     * - process A06 to create inpatient visit for the patient  
     *  
     * @throws Exception ...
     */
    @Test
    public void testAdtA06a() throws Exception {
        
        String message1 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A04^ADT_A01|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A01|201112021621||||201112021621|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "PV1||E|Emerg^EmergAcute^Exam10^G^4265^^^N^EmergAcute^Exam10^Emerg^185 2 7^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|EP^|11110000514^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r" + 
                "PV2||S^Semi^03ZFinbed^^^|^kfkfkfjcjcjcgcgcclcl^03ZAmitRes^^^|||||||||||||||||||N||AI|Elective||||||N|||||||OTH^Self^03ZBrInBy^^^|||||||||||\r" + 
                "DG1|1||06^KFKFKFJCJCJCGCGCCLCL^2.16.840.1.113883.11.19436|KFKFKFJCJCJCGCGCCLCL|201112021621|A|||||||||1||D||||\r" + 
                "PR1||||||||||||||||||||\r" + 
                "ZPV|OTH^Self|F^Standard|||N|||13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||Medical Services^General Internal Medicine^GMED|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|413||3910||^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r" + 
                "ZWA||||||||active|\r";
        
        String message2 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112070000||ADT^A06^ADT_A06|123710|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A06|201112151111||||201112151111|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000515^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" +
                "MRG|7012673^^^^MR^^^^^^^~HN2827^^^^PI^^^^^^^||||11110000514^^^EPR^VN^G^4265^^^^^||\r" +
                "PV1||I||C||^^^G^4265^^^^^^^^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||A|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000515^^^UHN^VN^G^4265^^^^^|||||||||||||||||1|||G|||||201112021621|||||||V|\r" + 
                "PV2||S^Semi^03ZFinbed^^^|^kfkfkfjcjcjcgcgcclcl^03ZAmitRes^^^|||||||||||||||||||N||AI|Elective||||||N|||||||OTH^Self^03ZBrInBy^^^|||||||||||\r" + 
                "DG1|1||06^KFKFKFJCJCJCGCGCCLCL^2.16.840.1.113883.11.19436|KFKFKFJCJCJCGCGCCLCL|201112021621|A|||||||||1||D||||\r" + 
                "PR1||||||||||||||||||||\r" + 
                "ZPV|OTH^Self|^|||N|||3210^Generic^Physician-EM^^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|6835^Generic^Anaesthetist^^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||^^|3210^Generic^Physician-EM^^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|||||^^^^^|3210^Generic^Physician-EM^^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r";
        
        
        Persister.persist(UhnConverter.convertAdtOrFail(message1));
        Persister.persist(UhnConverter.convertAdtOrFail(message2));
        
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
        
        PatientWithVisitsContainer pwvContainer = pwvContainers.get(0);
        Patient patient = pwvContainer.getDocument().myPatient;
        Visit preCvisit = pwvContainer.getDocument().myVisits.get(0);
        Visit postCvisit = pwvContainer.getDocument().myVisits.get(1);
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > patient.myRecordUpdatedDate.getTime());
        
        //check patient
        assertEquals("F", patient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), patient.myDateOfBirth);
        assertEquals(null, patient.myDeathDateAndTime);
        assertEquals("N", patient.myDeathIndicator);
        assertEquals(null, patient.myMothersMaidenName);
        
        
        Cx patId = patient.myPatientIds.get(0);
        assertEquals("7012673", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Xpn patName = patient.myPatientNames.get(0);
        assertEquals("Test", patName.myLastName);
        assertEquals("Majaconversion", patName.myFirstName);
                
        Xad patAddresses = patient.myPatientAddresses.get(0);
        assertEquals("1 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        Xtn patPhone = patient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        Ce lang = patient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
        
        
        //check visit lastUpdateTime
        assertTrue(nowMilli > preCvisit.myRecordUpdatedDate.getTime());
                        
        
        //check preconv visit
        assertEquals("11110000514", preCvisit.myVisitNumber.myIdNumber);
        assertEquals(Constants.INACTIVE_VISIT_STATUS, preCvisit.myVisitStatus);
        assertEquals("E", preCvisit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112021621"), preCvisit.myAdmitDate);        
        assertEquals("hospServ", preCvisit.myHospitalService);
                
        Pl loc = preCvisit.myAssignedPatientLocation;
        assertEquals("Emerg", loc.myPointOfCare);
        assertEquals("EmergAcute", loc.myRoom);
        assertEquals("Exam10", loc.myBed);
        
        Xcn admitDoc = preCvisit.myAdmittingDoctors.get(0);
        assertEquals("13546c", admitDoc.myId);
        assertEquals("Physician", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("MoeC", admitDoc.myMiddleName);
        
        Xcn attendDoc = preCvisit.myAttendingDoctors.get(0);
        assertEquals("13546a", attendDoc.myId);
        assertEquals("Physician", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("MoeA", attendDoc.myMiddleName);
        
        Xcn refDoc = preCvisit.myReferringDoctors.get(0);
        assertEquals("13546b", refDoc.myId);
        assertEquals("Physician", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("MoeB", refDoc.myMiddleName);
        
        Diagnosis dg = preCvisit.myDiagnoses.get(0);
        Ce dgCode = dg.myDiagnosis;
        assertEquals("06", dgCode.myCode);
        assertEquals("KFKFKFJCJCJCGCGCCLCL", dgCode.myText);
        assertEquals("2.16.840.1.113883.11.19436", dgCode.myCodeSystem);
        
        
        //check visit lastUpdateTime
        assertTrue(nowMilli > postCvisit.myRecordUpdatedDate.getTime());
        
        
        //check postconv visit
        assertEquals("11110000515", postCvisit.myVisitNumber.myIdNumber);
        assertEquals(Constants.ACTIVE_VISIT_STATUS, postCvisit.myVisitStatus);
        assertEquals("11110000514", postCvisit.myPreviousVisitNumbers.get(0).myIdNumber);
        assertEquals("I", postCvisit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112021621"), postCvisit.myAdmitDate);        
        assertEquals("hospServ", postCvisit.myHospitalService);
        
        admitDoc = postCvisit.myAdmittingDoctors.get(0);
        assertEquals("13546c", admitDoc.myId);
        assertEquals("Physician", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("MoeC", admitDoc.myMiddleName);
        
        attendDoc = postCvisit.myAttendingDoctors.get(0);
        assertEquals("13546a", attendDoc.myId);
        assertEquals("Physician", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("MoeA", attendDoc.myMiddleName);
        
        refDoc = postCvisit.myReferringDoctors.get(0);
        assertEquals("13546b", refDoc.myId);
        assertEquals("Physician", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("MoeB", refDoc.myMiddleName);
        
        dg = postCvisit.myDiagnoses.get(0);
        dgCode = dg.myDiagnosis;
        assertEquals("06", dgCode.myCode);
        assertEquals("KFKFKFJCJCJCGCGCCLCL", dgCode.myText);
        assertEquals("2.16.840.1.113883.11.19436", dgCode.myCodeSystem);
        
              
        
        
    }    
    
    
    
    
    /**
     * Testing patient visit conversion from type E to I A06
     * - no admit message received
     * - process A06 to create inpatient visit for the patient  
     *  
     * @throws Exception ...
     */
    @Test
    public void testAdtA06b() throws Exception {
        
        
        String message1 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112070000||ADT^A06^ADT_A06|123710|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A06|201112151111||||201112151111|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000515^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" +
                "MRG|7012673^^^^MR^^^^^^^~HN2827^^^^PI^^^^^^^||||11110000514^^^EPR^VN^G^4265^^^^^||\r" +
                "PV1||I||C||^^^G^4265^^^^^^^^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||A|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000515^^^UHN^VN^G^4265^^^^^|||||||||||||||||1|||G|||||201112021621|||||||V|\r" + 
                "PV2||S^Semi^03ZFinbed^^^|^kfkfkfjcjcjcgcgcclcl^03ZAmitRes^^^|||||||||||||||||||N||AI|Elective||||||N|||||||OTH^Self^03ZBrInBy^^^|||||||||||\r" + 
                "DG1|1||06^KFKFKFJCJCJCGCGCCLCL^2.16.840.1.113883.11.19436|KFKFKFJCJCJCGCGCCLCL|201112021621|A|||||||||1||D||||\r" + 
                "PR1||||||||||||||||||||\r" + 
                "ZPV|OTH^Self|^|||N|||3210^Generic^Physician-EM^^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|6835^Generic^Anaesthetist^^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||^^|3210^Generic^Physician-EM^^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|||||^^^^^|3210^Generic^Physician-EM^^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r";
        
        
        Persister.persist(UhnConverter.convertAdtOrFail(message1));        
        
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
        
        PatientWithVisitsContainer pwvContainer = pwvContainers.get(0);
        Patient patient = pwvContainer.getDocument().myPatient;
        Visit preCvisit = pwvContainer.getDocument().myVisits.get(0);
        Visit postCvisit = pwvContainer.getDocument().myVisits.get(1);
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > patient.myRecordUpdatedDate.getTime());
        
        //check patient
        assertEquals("F", patient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), patient.myDateOfBirth);
        assertEquals(null, patient.myDeathDateAndTime);
        assertEquals("N", patient.myDeathIndicator);
        assertEquals(null, patient.myMothersMaidenName);
        
        
        Cx patId = patient.myPatientIds.get(0);
        assertEquals("7012673", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Xpn patName = patient.myPatientNames.get(0);
        assertEquals("Test", patName.myLastName);
        assertEquals("Majaconversion", patName.myFirstName);
                
        Xad patAddresses = patient.myPatientAddresses.get(0);
        assertEquals("1 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        Xtn patPhone = patient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        Ce lang = patient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
        
        //check visit lastUpdateTime
        assertTrue(nowMilli > preCvisit.myRecordUpdatedDate.getTime());
        
        //check preconv visit (this will only be a stub record that was created from the A06 message)
        assertEquals("11110000514", preCvisit.myVisitNumber.myIdNumber);
        assertEquals(Constants.INACTIVE_VISIT_STATUS, preCvisit.myVisitStatus);
        assertEquals(null, preCvisit.myPatientClassCode);
        assertEquals(null, preCvisit.myAdmitDate);  
        
        //check visit lastUpdateTime
        assertTrue(nowMilli > postCvisit.myRecordUpdatedDate.getTime());
        
        
        //check postconv visit
        assertEquals("11110000515", postCvisit.myVisitNumber.myIdNumber);
        assertEquals(Constants.ACTIVE_VISIT_STATUS, postCvisit.myVisitStatus);
        assertEquals("11110000514", postCvisit.myPreviousVisitNumbers.get(0).myIdNumber);
        assertEquals("I", postCvisit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112021621"), postCvisit.myAdmitDate);        
        assertEquals("hospServ", postCvisit.myHospitalService);
        
        Xcn admitDoc = postCvisit.myAdmittingDoctors.get(0);
        assertEquals("13546c", admitDoc.myId);
        assertEquals("Physician", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("MoeC", admitDoc.myMiddleName);
        
        Xcn attendDoc = postCvisit.myAttendingDoctors.get(0);
        assertEquals("13546a", attendDoc.myId);
        assertEquals("Physician", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("MoeA", attendDoc.myMiddleName);
        
        Xcn refDoc = postCvisit.myReferringDoctors.get(0);
        assertEquals("13546b", refDoc.myId);
        assertEquals("Physician", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("MoeB", refDoc.myMiddleName);
        
        Diagnosis dg = postCvisit.myDiagnoses.get(0);
        Ce dgCode = dg.myDiagnosis;
        assertEquals("06", dgCode.myCode);
        assertEquals("KFKFKFJCJCJCGCGCCLCL", dgCode.myText);
        assertEquals("2.16.840.1.113883.11.19436", dgCode.myCodeSystem);
        
              
        
        
    }  
    
    
    
    /**
     * Testing patient visit conversion from type E to I A06
     * - admit an emerg patient
     * - process A06 to convert emerg visit to inpatient visit
     * - the same visit is used for pre and post conversion visit  
     *  
     * @throws Exception ...
     */
    @Test
    public void testAdtA06c() throws Exception {
        
        String message1 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A04^ADT_A01|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A01|201112021621||||201112021621|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "PV1||E|Emerg^EmergAcute^Exam10^G^4265^^^N^EmergAcute^Exam10^Emerg^185 2 7^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|EP^|11110000514^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r" + 
                "PV2||S^Semi^03ZFinbed^^^|^kfkfkfjcjcjcgcgcclcl^03ZAmitRes^^^|||||||||||||||||||N||AI|Elective||||||N|||||||OTH^Self^03ZBrInBy^^^|||||||||||\r" + 
                "DG1|1||06^KFKFKFJCJCJCGCGCCLCL^2.16.840.1.113883.11.19436|KFKFKFJCJCJCGCGCCLCL|201112021621|A|||||||||1||D||||\r" + 
                "PR1||||||||||||||||||||\r" + 
                "ZPV|OTH^Self|F^Standard|||N|||13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||Medical Services^General Internal Medicine^GMED|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|413||3910||^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r" + 
                "ZWA||||||||active|\r";
        
        String message2 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112070000||ADT^A06^ADT_A06|123710|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A06|201112151111||||201112151111|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" +
                "MRG|7012673^^^^MR^^^^^^^~HN2827^^^^PI^^^^^^^||||11110000514^^^EPR^VN^G^4265^^^^^||\r" +
                "PV1||I||C||^^^G^4265^^^^^^^^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||A|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000514^^^UHN^VN^G^4265^^^^^|||||||||||||||||1|||G|||||201112021621|||||||V|\r" + 
                "PV2||S^Semi^03ZFinbed^^^|^kfkfkfjcjcjcgcgcclcl^03ZAmitRes^^^|||||||||||||||||||N||AI|Elective||||||N|||||||OTH^Self^03ZBrInBy^^^|||||||||||\r" + 
                "DG1|1||06^KFKFKFJCJCJCGCGCCLCL^2.16.840.1.113883.11.19436|KFKFKFJCJCJCGCGCCLCL|201112021621|A|||||||||1||D||||\r" + 
                "PR1||||||||||||||||||||\r" + 
                "ZPV|OTH^Self|^|||N|||3210^Generic^Physician-EM^^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|6835^Generic^Anaesthetist^^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||^^|3210^Generic^Physician-EM^^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|||||^^^^^|3210^Generic^Physician-EM^^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r";
        
        
        Persister.persist(UhnConverter.convertAdtOrFail(message1));
        Persister.persist(UhnConverter.convertAdtOrFail(message2));
        
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
        
        PatientWithVisitsContainer pwvContainer = pwvContainers.get(0);
        Patient patient = pwvContainer.getDocument().myPatient;
        assertEquals(1, pwvContainer.getDocument().myVisits.size());
        Visit postCvisit = pwvContainer.getDocument().myVisits.get(0);
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > patient.myRecordUpdatedDate.getTime());
        
        //check patient
        assertEquals("F", patient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), patient.myDateOfBirth);
        assertEquals(null, patient.myDeathDateAndTime);
        assertEquals("N", patient.myDeathIndicator);
        assertEquals(null, patient.myMothersMaidenName);
        
        
        Cx patId = patient.myPatientIds.get(0);
        assertEquals("7012673", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Xpn patName = patient.myPatientNames.get(0);
        assertEquals("Test", patName.myLastName);
        assertEquals("Majaconversion", patName.myFirstName);
                
        Xad patAddresses = patient.myPatientAddresses.get(0);
        assertEquals("1 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        Xtn patPhone = patient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        Ce lang = patient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
          
        //check visit lastUpdateTime
        assertTrue(nowMilli > postCvisit.myRecordUpdatedDate.getTime());
        
        //check visit after conversion
        assertEquals("11110000514", postCvisit.myVisitNumber.myIdNumber);
        assertEquals(Constants.ACTIVE_VISIT_STATUS, postCvisit.myVisitStatus);
        assertEquals(null, postCvisit.myPreviousVisitNumbers);
        assertEquals("I", postCvisit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112021621"), postCvisit.myAdmitDate);        
        assertEquals("hospServ", postCvisit.myHospitalService);
        
        Xcn admitDoc = postCvisit.myAdmittingDoctors.get(0);
        assertEquals("13546c", admitDoc.myId);
        assertEquals("Physician", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("MoeC", admitDoc.myMiddleName);
        
        Xcn attendDoc = postCvisit.myAttendingDoctors.get(0);
        assertEquals("13546a", attendDoc.myId);
        assertEquals("Physician", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("MoeA", attendDoc.myMiddleName);
        
        Xcn refDoc = postCvisit.myReferringDoctors.get(0);
        assertEquals("13546b", refDoc.myId);
        assertEquals("Physician", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("MoeB", refDoc.myMiddleName);
        
        Diagnosis dg = postCvisit.myDiagnoses.get(0);
        Ce dgCode = dg.myDiagnosis;
        assertEquals("06", dgCode.myCode);
        assertEquals("KFKFKFJCJCJCGCGCCLCL", dgCode.myText);
        assertEquals("2.16.840.1.113883.11.19436", dgCode.myCodeSystem);
        
    }    
    
    
    
    /**
     * Testing patient visit conversion from type E to I A06
     * - no admit message received
     * - process A06 to create inpatient visit for the patient
     * - the same visit is used for pre and post conversion visit  
     *  
     * @throws Exception ...
     */
    @Test
    public void testAdtA06d() throws Exception {
        
        
        String message1 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112070000||ADT^A06^ADT_A06|123710|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A06|201112151111||||201112151111|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000515^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" +
                "MRG|7012673^^^^MR^^^^^^^~HN2827^^^^PI^^^^^^^||||11110000515^^^EPR^VN^G^4265^^^^^||\r" +
                "PV1||I||C||^^^G^4265^^^^^^^^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||A|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000515^^^UHN^VN^G^4265^^^^^|||||||||||||||||1|||G|||||201112021621|||||||V|\r" + 
                "PV2||S^Semi^03ZFinbed^^^|^kfkfkfjcjcjcgcgcclcl^03ZAmitRes^^^|||||||||||||||||||N||AI|Elective||||||N|||||||OTH^Self^03ZBrInBy^^^|||||||||||\r" + 
                "DG1|1||06^KFKFKFJCJCJCGCGCCLCL^2.16.840.1.113883.11.19436|KFKFKFJCJCJCGCGCCLCL|201112021621|A|||||||||1||D||||\r" + 
                "PR1||||||||||||||||||||\r" + 
                "ZPV|OTH^Self|^|||N|||3210^Generic^Physician-EM^^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|6835^Generic^Anaesthetist^^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||^^|3210^Generic^Physician-EM^^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|||||^^^^^|3210^Generic^Physician-EM^^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r";
        
        
        Persister.persist(UhnConverter.convertAdtOrFail(message1));        
        
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
        
        PatientWithVisitsContainer pwvContainer = pwvContainers.get(0);
        Patient patient = pwvContainer.getDocument().myPatient;
        assertEquals(1, pwvContainer.getDocument().myVisits.size());        
        Visit postCvisit = pwvContainer.getDocument().myVisits.get(0);
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > patient.myRecordUpdatedDate.getTime());
        
        //check patient
        assertEquals("F", patient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), patient.myDateOfBirth);
        assertEquals(null, patient.myDeathDateAndTime);
        assertEquals("N", patient.myDeathIndicator);
        assertEquals(null, patient.myMothersMaidenName);
        
        
        Cx patId = patient.myPatientIds.get(0);
        assertEquals("7012673", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Xpn patName = patient.myPatientNames.get(0);
        assertEquals("Test", patName.myLastName);
        assertEquals("Majaconversion", patName.myFirstName);
                
        Xad patAddresses = patient.myPatientAddresses.get(0);
        assertEquals("1 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        Xtn patPhone = patient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        Ce lang = patient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
        
        
        //check visit lastUpdateTime
        assertTrue(nowMilli > postCvisit.myRecordUpdatedDate.getTime());
        
        
        //check visit after conversion
        assertEquals("11110000515", postCvisit.myVisitNumber.myIdNumber);
        assertEquals(Constants.ACTIVE_VISIT_STATUS, postCvisit.myVisitStatus);
        assertEquals(null, postCvisit.myPreviousVisitNumbers);
        assertEquals("I", postCvisit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112021621"), postCvisit.myAdmitDate);        
        assertEquals("hospServ", postCvisit.myHospitalService);
        
        Xcn admitDoc = postCvisit.myAdmittingDoctors.get(0);
        assertEquals("13546c", admitDoc.myId);
        assertEquals("Physician", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("MoeC", admitDoc.myMiddleName);
        
        Xcn attendDoc = postCvisit.myAttendingDoctors.get(0);
        assertEquals("13546a", attendDoc.myId);
        assertEquals("Physician", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("MoeA", attendDoc.myMiddleName);
        
        Xcn refDoc = postCvisit.myReferringDoctors.get(0);
        assertEquals("13546b", refDoc.myId);
        assertEquals("Physician", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("MoeB", refDoc.myMiddleName);
        
        Diagnosis dg = postCvisit.myDiagnoses.get(0);
        Ce dgCode = dg.myDiagnosis;
        assertEquals("06", dgCode.myCode);
        assertEquals("KFKFKFJCJCJCGCGCCLCL", dgCode.myText);
        assertEquals("2.16.840.1.113883.11.19436", dgCode.myCodeSystem);
        
              
        
        
    }  
        
        
    
    
    
    
    
    
    
    /**
     * Testing patient visit conversion from type I to E A07
     * - admit an inpatient
     * - process A07 to create emerg visit for the patient  
     *  
     * @throws Exception ...
     */
    @Test
    public void testAdtA07() throws Exception {
        
        String message1 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A01^ADT_A01|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A01|201112021621||||201112021621|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000514^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r" + 
                "PV2||S^Semi^03ZFinbed^^^|^kfkfkfjcjcjcgcgcclcl^03ZAmitRes^^^|||||||||||||||||||N||AI|Elective||||||N|||||||OTH^Self^03ZBrInBy^^^|||||||||||\r" + 
                "DG1|1||06^KFKFKFJCJCJCGCGCCLCL^2.16.840.1.113883.11.19436|KFKFKFJCJCJCGCGCCLCL|201112021621|A|||||||||1||D||||\r" + 
                "PR1||||||||||||||||||||\r" + 
                "ZPV|OTH^Self|F^Standard|||N|||13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||Medical Services^General Internal Medicine^GMED|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|413||3910||^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r" + 
                "ZWA||||||||active|\r";
        
        String message2 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112070000||ADT^A07^ADT_A06|123710|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A07|201112151111||||201112151111|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000515^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" +
                "MRG|7012673^^^^MR^^^^^^^~HN2827^^^^PI^^^^^^^||||11110000514^^^EPR^VN^G^4265^^^^^||\r" +
                "PV1||E||||^^^G^4265^^^^^^^^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||A|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000515^^^UHN^VN^G^4265^^^^^|||||||||||||||||1|||G|||||201112021621|||||||V|\r" + 
                "PV2||S^Semi^03ZFinbed^^^|^kfkfkfjcjcjcgcgcclcl^03ZAmitRes^^^|||||||||||||||||||N||AI|Elective||||||N|||||||OTH^Self^03ZBrInBy^^^|||||||||||\r" + 
                "DG1|1||06^KFKFKFJCJCJCGCGCCLCL^2.16.840.1.113883.11.19436|KFKFKFJCJCJCGCGCCLCL|201112021621|A|||||||||1||D||||\r" + 
                "PR1||||||||||||||||||||\r" + 
                "ZPV|OTH^Self|^|||N|||3210^Generic^Physician-EM^^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|6835^Generic^Anaesthetist^^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||^^|3210^Generic^Physician-EM^^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|||||^^^^^|3210^Generic^Physician-EM^^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r";
        
        
   
        
        
        
        Persister.persist(UhnConverter.convertAdtOrFail(message1));
        Persister.persist(UhnConverter.convertAdtOrFail(message2));
        
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
        
        PatientWithVisitsContainer pwvContainer = pwvContainers.get(0);
        Patient patient = pwvContainer.getDocument().myPatient;
        Visit preCvisit = pwvContainer.getDocument().myVisits.get(0);
        Visit postCvisit = pwvContainer.getDocument().myVisits.get(1);
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > patient.myRecordUpdatedDate.getTime());
        
        //check patient
        assertEquals("F", patient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), patient.myDateOfBirth);
        assertEquals(null, patient.myDeathDateAndTime);
        assertEquals("N", patient.myDeathIndicator);
        assertEquals(null, patient.myMothersMaidenName);
        
        
        Cx patId = patient.myPatientIds.get(0);
        assertEquals("7012673", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Xpn patName = patient.myPatientNames.get(0);
        assertEquals("Test", patName.myLastName);
        assertEquals("Majaconversion", patName.myFirstName);
                
        Xad patAddresses = patient.myPatientAddresses.get(0);
        assertEquals("1 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        Xtn patPhone = patient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        Ce lang = patient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
        
        //check visit lastUpdateTime
        assertTrue(nowMilli > preCvisit.myRecordUpdatedDate.getTime());
        
        //check preconv visit
        assertEquals("11110000514", preCvisit.myVisitNumber.myIdNumber);
        assertEquals(Constants.INACTIVE_VISIT_STATUS, preCvisit.myVisitStatus);
        assertEquals("I", preCvisit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112021621"), preCvisit.myAdmitDate);        
        assertEquals("hospServ", preCvisit.myHospitalService);
                
        Pl loc = preCvisit.myAssignedPatientLocation;
        assertEquals("PMH 15C", loc.myPointOfCare);
        assertEquals("413", loc.myRoom);
        assertEquals("2", loc.myBed);        
        
        Xcn admitDoc = preCvisit.myAdmittingDoctors.get(0);
        assertEquals("13546c", admitDoc.myId);
        assertEquals("Physician", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("MoeC", admitDoc.myMiddleName);
        
        Xcn attendDoc = preCvisit.myAttendingDoctors.get(0);
        assertEquals("13546a", attendDoc.myId);
        assertEquals("Physician", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("MoeA", attendDoc.myMiddleName);
        
        Xcn refDoc = preCvisit.myReferringDoctors.get(0);
        assertEquals("13546b", refDoc.myId);
        assertEquals("Physician", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("MoeB", refDoc.myMiddleName);
        
        Diagnosis dg = preCvisit.myDiagnoses.get(0);
        Ce dgCode = dg.myDiagnosis;
        assertEquals("06", dgCode.myCode);
        assertEquals("KFKFKFJCJCJCGCGCCLCL", dgCode.myText);
        assertEquals("2.16.840.1.113883.11.19436", dgCode.myCodeSystem);
        
        //check visit lastUpdateTime
        assertTrue(nowMilli > postCvisit.myRecordUpdatedDate.getTime());
        
        //check postconv visit
        assertEquals("11110000515", postCvisit.myVisitNumber.myIdNumber);
        assertEquals(Constants.ACTIVE_VISIT_STATUS, postCvisit.myVisitStatus);
        assertEquals("11110000514", postCvisit.myPreviousVisitNumbers.get(0).myIdNumber);
        assertEquals("E", postCvisit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112021621"), postCvisit.myAdmitDate);        
        assertEquals("hospServ", postCvisit.myHospitalService);  
                
        loc = postCvisit.myAssignedPatientLocation;
        assertEquals(null, loc);
        
        admitDoc = postCvisit.myAdmittingDoctors.get(0);
        assertEquals("13546c", admitDoc.myId);
        assertEquals("Physician", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("MoeC", admitDoc.myMiddleName);
        
        attendDoc = postCvisit.myAttendingDoctors.get(0);
        assertEquals("13546a", attendDoc.myId);
        assertEquals("Physician", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("MoeA", attendDoc.myMiddleName);
        
        refDoc = postCvisit.myReferringDoctors.get(0);
        assertEquals("13546b", refDoc.myId);
        assertEquals("Physician", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("MoeB", refDoc.myMiddleName);
        
        dg = postCvisit.myDiagnoses.get(0);
        dgCode = dg.myDiagnosis;
        assertEquals("06", dgCode.myCode);
        assertEquals("KFKFKFJCJCJCGCGCCLCL", dgCode.myText);
        assertEquals("2.16.840.1.113883.11.19436", dgCode.myCodeSystem);        

        
    }        
    
    
    
    
    
    
    /**
     * Testing patient visit update A08
     * - admit patient into a bed
     * - update patient and visit info
     *  
     * @throws Exception ...
     */
    @Test
    public void testAdtA08() throws Exception {
        
        String message1 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A01^ADT_A01|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A01|201112021621||||201112021621|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "ROL||UC|PP^Primary Care Provider^15ZRole^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^^^^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|O^Office^^^^|^^^ON^^Can^^^^^^^^|(416) 340-3391^WPN^PH^^^^^^^^^\r" + 
                "ROL||UC|PP^Primary Care Provider^15ZRole^^^|13546b^Genericb^Physicianb^Moeb^^Dr.^MD^^UHN^L^^^EI^G^2.16.840.1.113883.3.59.3:947^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|1^Hospital^15ZOrgTyp^^^|^^^ON^^Can^^^^^^^^|(416) 340-3388^WPN^PH^^^^^^^^^\r" +
                "NK1|1|Wph^Mom^^^^^L^^^^^^^|PAR^Parent^03ZRelshp^^^|82 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^~(416)123-1234^PRN^CP^^^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^|N^Next-of-Kin^03ZConRol^^^||||||||||||||||||||||||||||||||\r" +
                "NK1|2|Wph^Brother^^^^^L^^^^^^^|BRO^Brother^03ZRelshp^^^|83 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3601^PRN^PH^^^^^^^^^|(416)525-2525^PRN^PH^^^^^^^^^|C^Emergency Contact^03ZConRol^^^||||||||||||||||||||||||||||||||\r" + 
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000514^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r" + 
                "PV2||S^Semi^03ZFinbed^^^|^kfkfkfjcjcjcgcgcclcl^03ZAmitRes^^^|||||||||||||||||||N||AI|Elective||||||N|||||||OTH^Self^03ZBrInBy^^^|||||||||||\r" + 
                "DG1|1||^KFKFKFJCJCJCGCGCCLCLX|KFKFKFJCJCJCGCGCCLCL|201112021621|A|||||||||1||D||||\r" + 
                "PR1||||||||||||||||||||\r" + 
                "ZPV|OTH^Self|F^Standard|||N|||13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||Medical Services^General Internal Medicine^GMED|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|413||3910||^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r" + 
                "ZWA||||||||active|\r";
        
        String message2 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201201111123||ADT^A08^ADT_A01|124423|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A08|201201111123||||201201111123|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion2^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731231|F|||2 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "ROL||UC|PP^Primary Care Provider^15ZRole^^^|13546^Generic2^Physician2^Moe2^^Dr.^MD^^UHN^L^^^^^^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|O^Office^^^^|^^^ON^^Can^^^^^^^^|(416) 340-3391^WPN^PH^^^^^^^^^\r" + 
                "ROL||UC|PP^Primary Care Provider^15ZRole^^^|13546b^Genericb2^Physicianb2^Moeb2^^Dr.^MD^^UHN^L^^^EI^G^2.16.840.1.113883.3.59.3:947^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|1^Hospital^15ZOrgTyp^^^|^^^ON^^Can^^^^^^^^|(416) 340-3388^WPN^PH^^^^^^^^^\r" +
                "NK1|1|Wph^Mom^^^^^L^^^^^^^|PAR^Parent^03ZRelshp^^^|82 Buttonwood Avenue2^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^~(416)123-1234^PRN^CP^^^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^|N^Next-of-Kin^03ZConRol^^^||||||||||||||||||||||||||||||||\r" +
                "NK1|2|Wph^Brother^^^^^L^^^^^^^|BRO^Brother^03ZRelshp^^^|83 Buttonwood Avenue2^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3601^PRN^PH^^^^^^^^^|(416)525-2525^PRN^PH^^^^^^^^^|C^Emergency Contact^03ZConRol^^^||||||||||||||||||||||||||||||||\r" + 
                "PV1||I|ES9 GEN S^424^1^G^4265^^^N^ES 9 424^ES 9 424 1^ES9 GEN S^1521 19 1^|R|||13546a^Generic^Physician2^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician2^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician2^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000514^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021622|||||||V|\r" +
                "PV2||S^Semi^03ZFinbed^^^|^kfkfkfjcjcjcgcgcclcl^03ZAmitRes^^^|||||||||||||||||||N||AI|Elective||||||N|||||||OTH^Self^03ZBrInBy^^^|||||||||||\r" + 
                "DG1|1||06^KFKFKFJCJCJCGCGCCLCL^2.16.840.1.113883.11.19436|KFKFKFJCJCJCGCGCCLCL|201112021621|A|||||||||1||D||||\r" + 
                "PR1||||||||||||||||||||\r" + 
                "ZPV|OTH^Self|F^Standard|||N|||13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||Medical Services^General Internal Medicine^GMED|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|413||3910||^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r" + 
                "ZWA||||||||active|\r";
        
        
        
        Persister.persist(UhnConverter.convertAdtOrFail(message1));
        Persister.persist(UhnConverter.convertAdtOrFail(message2));
        
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
        
        PatientWithVisitsContainer pwvContainer = pwvContainers.get(0);
        Patient patient = pwvContainer.getDocument().myPatient;
        Visit visit = pwvContainer.getDocument().myVisits.get(0);
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > patient.myRecordUpdatedDate.getTime());
        
        //check patient
        assertEquals("F", patient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312310000"), patient.myDateOfBirth);
        assertEquals(null, patient.myDeathDateAndTime);
        assertEquals("N", patient.myDeathIndicator);
        assertEquals(null, patient.myMothersMaidenName);
        
        
        Cx patId = patient.myPatientIds.get(0);
        assertEquals("7012673", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Xpn patName = patient.myPatientNames.get(0);
        assertEquals("Test", patName.myLastName);
        assertEquals("Majaconversion2", patName.myFirstName);
                
        Xad patAddresses = patient.myPatientAddresses.get(0);
        assertEquals("2 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        Xtn patPhone = patient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        Ce lang = patient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
        
        
        //check patient roles
        List<PersonInRole> roles  = patient.myPersonInRoles;
        assertEquals(2, roles.size());
        PersonInRole rol_1 = patient.myPersonInRoles.get(0);
        PersonInRole rol_2 = patient.myPersonInRoles.get(1);        
        //ROL(1)
        assertEquals("PP",rol_1.myRole.myCode);
        assertEquals("Primary Care Provider",rol_1.myRole.myText);        
        assertEquals("Generic2", rol_1.myPersonNames.get(0).myLastName);
        assertEquals("Physician2", rol_1.myPersonNames.get(0).myFirstName);
        assertEquals("Moe2", rol_1.myPersonNames.get(0).myMiddleName);
        assertEquals("(416) 340-3391", rol_1.myContactInformation.get(0).myPhoneNumber);
        //ROL(2)
        assertEquals("PP",rol_2.myRole.myCode);
        assertEquals("Primary Care Provider",rol_2.myRole.myText);        
        assertEquals("Genericb2", rol_2.myPersonNames.get(0).myLastName);
        assertEquals("Physicianb2", rol_2.myPersonNames.get(0).myFirstName);
        assertEquals("Moeb2", rol_2.myPersonNames.get(0).myMiddleName);
        assertEquals("(416) 340-3388", rol_2.myContactInformation.get(0).myPhoneNumber);
        
        
        //check patient associated parties
        List<AssociatedParty> parties  = patient.myAssociatedParties;
        assertEquals(2, parties.size());
        AssociatedParty party_1 = patient.myAssociatedParties.get(0);
        AssociatedParty party_2 = patient.myAssociatedParties.get(1);        
        //NK1(1)
        assertEquals("Wph",party_1.myNames.get(0).myLastName);
        assertEquals("Mom",party_1.myNames.get(0).myFirstName);
        assertEquals("PAR",party_1.myRelationship.myCode);
        assertEquals("Parent",party_1.myRelationshipName);
        assertEquals("82 Buttonwood Avenue2",party_1.myAddresses.get(0).myStreetAddress);
        assertEquals("(416)243-3600",party_1.myContactInformation.get(0).myPhoneNumber);
        //NK1(2)
        assertEquals("Wph",party_2.myNames.get(0).myLastName);
        assertEquals("Brother",party_2.myNames.get(0).myFirstName);
        assertEquals("BRO",party_2.myRelationship.myCode);
        assertEquals("Brother",party_2.myRelationshipName);
        assertEquals("83 Buttonwood Avenue2",party_2.myAddresses.get(0).myStreetAddress);
        assertEquals("(416)243-3601",party_2.myContactInformation.get(0).myPhoneNumber);
        
        //check visit lastUpdateTime
        assertTrue(nowMilli > visit.myRecordUpdatedDate.getTime());
        
        
        //check visit
        assertEquals("11110000514", visit.myVisitNumber.myIdNumber);
        assertEquals(Constants.ACTIVE_VISIT_STATUS, visit.myVisitStatus);
        assertEquals("I", visit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112021622"), visit.myAdmitDate);
        assertEquals("hospServ", visit.myHospitalService);
        
        Pl cloc = visit.myAssignedPatientLocation;
        assertEquals("ES9 GEN S", cloc.myPointOfCare);
        assertEquals("424", cloc.myRoom);
        assertEquals("1", cloc.myBed);
        
        
        Xcn admitDoc = visit.myAdmittingDoctors.get(0);
        assertEquals("13546c", admitDoc.myId);
        assertEquals("Physician2", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("MoeC", admitDoc.myMiddleName);
        
        Xcn attendDoc = visit.myAttendingDoctors.get(0);
        assertEquals("13546a", attendDoc.myId);
        assertEquals("Physician2", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("MoeA", attendDoc.myMiddleName);
        
        Xcn refDoc = visit.myReferringDoctors.get(0);
        assertEquals("13546b", refDoc.myId);
        assertEquals("Physician2", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("MoeB", refDoc.myMiddleName);
        
        Diagnosis dg = visit.myDiagnoses.get(0);
        Ce dgCode = dg.myDiagnosis;
        assertEquals("06", dgCode.myCode);
        assertEquals("KFKFKFJCJCJCGCGCCLCL", dgCode.myText);
        assertEquals("2.16.840.1.113883.11.19436", dgCode.myCodeSystem);
                
    }    
    
    
    
    
    /**
     * Testing outpaient arrival (re-occurring visit type)
     * - A04 is sent to register the patient with type R visit
     * - A10 messages are sent to indicate patient arrivals during the visit
     * 
     * @throws Exception ...
     */
    @Test
    public void testAdtA10a() throws Exception {
        
        String message1 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112151022||ADT^A04^ADT_A01|123766|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A04|201112151022||||201112151022|G^4265^L\r" + 
                "PID|||7007541^^^UHN^MR^^^^^^^~^^^^JHN^^^^^^^~||Medinfo^Hyphen^^^^^L^^^^^200809301409^^~||19630317|M|||20 Dundas Street^^TORONTO^ON^M5G 2C2^Can^H^^^^^^^~|1811|(416)123-4567^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11130000114^^^UHN^VN^^^^^^^~||||||||||||N|||200809301409||||||\r" + 
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "ROL||UC|PP^Primary Care Provider^15ZRole^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^^^^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|O^Office^^^^|^^^ON^^Can^^^^^^^^|(416) 340-3391^WPN^PH^^^^^^^^^\r" + 
                "ROL||UC|PP^Primary Care Provider^15ZRole^^^|13546b^Genericb^Physicianb^Moeb^^Dr.^MD^^UHN^L^^^EI^G^2.16.840.1.113883.3.59.3:947^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|1^Hospital^15ZOrgTyp^^^|^^^ON^^Can^^^^^^^^|(416) 340-3388^WPN^PH^^^^^^^^^\r" +
                "NK1|1|Wph^Mom^^^^^L^^^^^^^|PAR^Parent^03ZRelshp^^^|82 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^~(416)123-1234^PRN^CP^^^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^|N^Next-of-Kin^03ZConRol^^^||||||||||||||||||||||||||||||||\r" +
                "NK1|2|Wph^Brother^^^^^L^^^^^^^|BRO^Brother^03ZRelshp^^^|83 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3601^PRN^PH^^^^^^^^^|(416)525-2525^PRN^PH^^^^^^^^^|C^Emergency Contact^03ZConRol^^^||||||||||||||||||||||||||||||||\r" +
                "PV1||R|8001-CC-Endoscopy^^^G^4265^^^C^^^Endoscopy^112^|||^^^G^4265^^^^^^^  ^|13893^Generic^Moe^PhysicianThree^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13893^Generic^Moe^PhysicianThree^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||||||A|||13893^Generic^Moe^PhysicianThree^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|OP^|11130000114^^^UHN^VN^G^4265^^^^^||||||||||||||||||||G|||||201112151022|||||||V|\r" + 
                "PV2|||^^^^^|||||||||||||||||||N||AO|||||||N||||||||||||||||||\r" + 
                "DG1|1||||||||||||||||||||\r" + 
                "PR1||||||||||||||||||||\r" + 
                "ZPV|^|^|||N|||13893^Generic^Moe^PhysicianThree^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|13893^Generic^Moe^PhysicianThree^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||^^|13893^Generic^Moe^PhysicianThree^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|||||^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r";
        
        
      String message2 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112151022||ADT^A10^ADT_A09|123766|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A10|201112151022||||201112151022|G^4265^L\r" + 
                "PID|||7007541^^^UHN^MR^^^^^^^~^^^^JHN^^^^^^^~||Medinfo^Hyphen^^^^^L^^^^^200809301409^^~||19630317|M|||20 Dundas Street^^TORONTO^ON^M5G 2C2^Can^H^^^^^^^~|1811|(416)123-4567^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11130000114^^^UHN^VN^^^^^^^~||||||||||||N|||200809301409||||||\r" + 
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "PV1||R|8001-CC-Endoscopy^^^G^4265^^^C^^^Endoscopy^112^|||^^^G^4265^^^^^^^  ^|13893^Generic^Moe^PhysicianThree^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13893^Generic^Moe^PhysicianThree^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||||||A|||13893^Generic^Moe^PhysicianThree^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|OP^|11130000114^^^UHN^VN^G^4265^^^^^||||||||||||||||||||G|||||201112161022|||||||V|\r" + 
                "PV2|||^^^^^|||||||||||||||||||N||AO|||||||N||||||||||||||||||\r" + 
                "DG1|1||||||||||||||||||||\r"; 
      
      
       String message3 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112151022||ADT^A10^ADT_A09|123766|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A10|201112151022||||201112151022|G^4265^L\r" + 
                "PID|||7007541^^^UHN^MR^^^^^^^~^^^^JHN^^^^^^^~||Medinfo^Hyphen^^^^^L^^^^^200809301409^^~||19630317|M|||20 Dundas Street^^TORONTO^ON^M5G 2C2^Can^H^^^^^^^~|1811|(416)123-4567^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11130000114^^^UHN^VN^^^^^^^~||||||||||||N|||200809301409||||||\r" + 
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "PV1||R|8001-CC-Endoscopy^^^G^4265^^^C^^^Endoscopy^112^|||^^^G^4265^^^^^^^  ^|13893^Generic^Moe^PhysicianThree^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13893^Generic^Moe^PhysicianThree^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||||||A|||13893^Generic^Moe^PhysicianThree^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|OP^|11130000114^^^UHN^VN^G^4265^^^^^||||||||||||||||||||G|||||201112171022|||||||V|\r" + 
                "PV2|||^^^^^|||||||||||||||||||N||AO|||||||N||||||||||||||||||\r" + 
                "DG1|1||||||||||||||||||||\r"; 
                

        

        
        Persister.persist(UhnConverter.convertAdtOrFail(message1));
        Persister.persist(UhnConverter.convertAdtOrFail(message2));
        Persister.persist(UhnConverter.convertAdtOrFail(message3));
        
        
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
        
        PatientWithVisitsContainer pwvContainer = pwvContainers.get(0);
        Patient patient = pwvContainer.getDocument().myPatient;
        Visit visit = pwvContainer.getDocument().myVisits.get(0);
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > patient.myRecordUpdatedDate.getTime());
        
        //check patient
        assertEquals("M", patient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("196303170000"), patient.myDateOfBirth);
        assertEquals(null, patient.myDeathDateAndTime);
        assertEquals("N", patient.myDeathIndicator);
        assertEquals(null, patient.myMothersMaidenName);
        
        Cx patId = patient.myPatientIds.get(0);
        assertEquals("7007541", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Xpn patName = patient.myPatientNames.get(0);
        assertEquals("Medinfo", patName.myLastName);
        assertEquals("Hyphen", patName.myFirstName);
                
        Xad patAddresses = patient.myPatientAddresses.get(0);
        assertEquals("20 Dundas Street", patAddresses.myStreetAddress);
        assertEquals("TORONTO", patAddresses.myCity);
        
        Xtn patPhone = patient.myPhoneNumbers.get(0);
        assertEquals("(416)123-4567", patPhone.myPhoneNumber);
        

        //check patient roles
        List<PersonInRole> roles  = patient.myPersonInRoles;
        assertEquals(2, roles.size());
        PersonInRole rol_1 = patient.myPersonInRoles.get(0);
        PersonInRole rol_2 = patient.myPersonInRoles.get(1);        
        //ROL(1)
        assertEquals("PP",rol_1.myRole.myCode);
        assertEquals("Primary Care Provider",rol_1.myRole.myText);        
        assertEquals("Generic", rol_1.myPersonNames.get(0).myLastName);
        assertEquals("Physician", rol_1.myPersonNames.get(0).myFirstName);
        assertEquals("Moe", rol_1.myPersonNames.get(0).myMiddleName);
        assertEquals("(416) 340-3391", rol_1.myContactInformation.get(0).myPhoneNumber);
        //ROL(2)
        assertEquals("PP",rol_2.myRole.myCode);
        assertEquals("Primary Care Provider",rol_2.myRole.myText);        
        assertEquals("Genericb", rol_2.myPersonNames.get(0).myLastName);
        assertEquals("Physicianb", rol_2.myPersonNames.get(0).myFirstName);
        assertEquals("Moeb", rol_2.myPersonNames.get(0).myMiddleName);
        assertEquals("(416) 340-3388", rol_2.myContactInformation.get(0).myPhoneNumber);
        
        
        //check patient associated parties
        List<AssociatedParty> parties  = patient.myAssociatedParties;
        assertEquals(2, parties.size());
        AssociatedParty party_1 = patient.myAssociatedParties.get(0);
        AssociatedParty party_2 = patient.myAssociatedParties.get(1);        
        //NK1(1)
        assertEquals("Wph",party_1.myNames.get(0).myLastName);
        assertEquals("Mom",party_1.myNames.get(0).myFirstName);
        assertEquals("PAR",party_1.myRelationship.myCode);
        assertEquals("Parent",party_1.myRelationshipName);
        assertEquals("82 Buttonwood Avenue",party_1.myAddresses.get(0).myStreetAddress);
        assertEquals("(416)243-3600",party_1.myContactInformation.get(0).myPhoneNumber);
        //NK1(2)
        assertEquals("Wph",party_2.myNames.get(0).myLastName);
        assertEquals("Brother",party_2.myNames.get(0).myFirstName);
        assertEquals("BRO",party_2.myRelationship.myCode);
        assertEquals("Brother",party_2.myRelationshipName);
        assertEquals("83 Buttonwood Avenue",party_2.myAddresses.get(0).myStreetAddress);
        assertEquals("(416)243-3601",party_2.myContactInformation.get(0).myPhoneNumber);
        
       
        //check visit lastUpdateTime
        assertTrue(nowMilli > visit.myRecordUpdatedDate.getTime());
        
        //check visit
        assertEquals("11130000114", visit.myVisitNumber.myIdNumber);
        assertEquals(Constants.ACTIVE_VISIT_STATUS, visit.myVisitStatus);
        assertEquals("R", visit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112151022"), visit.myAdmitDate);
        assertEquals(null, visit.myHospitalService);
        Pl loc = visit.myAssignedPatientLocation;
        assertEquals("8001-CC-Endoscopy", loc.myPointOfCare);
        assertEquals(null, loc.myRoom);
        assertEquals(null, loc.myBed);
        
        Xcn admitDoc = visit.myAdmittingDoctors.get(0);
        assertEquals("13893", admitDoc.myId);
        assertEquals("Moe", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("PhysicianThree", admitDoc.myMiddleName);
        
        Xcn attendDoc = visit.myAttendingDoctors.get(0);
        assertEquals("13893", attendDoc.myId);
        assertEquals("Moe", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("PhysicianThree", attendDoc.myMiddleName);
        
        Xcn refDoc = visit.myReferringDoctors.get(0);
        assertEquals("13893", refDoc.myId);
        assertEquals("Moe", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("PhysicianThree", refDoc.myMiddleName);
        
        
        //check visit arrival times
        List<Date> arrivalDates = visit.myArrivalDates;
        assertEquals(2, arrivalDates.size());
        assertEquals(ourTsFormat.parse("201112161022"), arrivalDates.get(0));
        assertEquals(ourTsFormat.parse("201112171022"), arrivalDates.get(1));        
        
    }    
    
    
    
    /**
     * Testing outpaient arrival (re-occurring visit type)
     * - No A04 is sent to register the patient but A10 messages are sent to indicate patient arrivals during the visit
     * 
     * @throws Exception ...
     */
    @Test
    public void testAdtA10b() throws Exception {
        
       
        
      String message1 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112151022||ADT^A10^ADT_A09|123766|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A10|201112151022||||201112151022|G^4265^L\r" + 
                "PID|||7007541^^^UHN^MR^^^^^^^~^^^^JHN^^^^^^^~||Medinfo^Hyphen^^^^^L^^^^^200809301409^^~||19630317|M|||20 Dundas Street^^TORONTO^ON^M5G 2C2^Can^H^^^^^^^~|1811|(416)123-4567^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11130000114^^^UHN^VN^^^^^^^~||||||||||||N|||200809301409||||||\r" + 
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "PV1||R|8001-CC-Endoscopy^^^G^4265^^^C^^^Endoscopy^112^|||^^^G^4265^^^^^^^  ^|13893^Generic^Moe^PhysicianThree^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13893^Generic^Moe^PhysicianThree^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||||||A|||13893^Generic^Moe^PhysicianThree^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|OP^|11130000114^^^UHN^VN^G^4265^^^^^||||||||||||||||||||G|||||201112161022|||||||V|\r" + 
                "PV2|||^^^^^|||||||||||||||||||N||AO|||||||N||||||||||||||||||\r" + 
                "DG1|1||||||||||||||||||||\r"; 
      
      
       String message2 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112151022||ADT^A10^ADT_A09|123766|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A10|201112151022||||201112151022|G^4265^L\r" + 
                "PID|||7007541^^^UHN^MR^^^^^^^~^^^^JHN^^^^^^^~||Medinfo^Hyphen^^^^^L^^^^^200809301409^^~||19630317|M|||20 Dundas Street^^TORONTO^ON^M5G 2C2^Can^H^^^^^^^~|1811|(416)123-4567^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11130000114^^^UHN^VN^^^^^^^~||||||||||||N|||200809301409||||||\r" + 
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "PV1||R|8001-CC-Endoscopy^^^G^4265^^^C^^^Endoscopy^112^|||^^^G^4265^^^^^^^  ^|13893^Generic^Moe^PhysicianThree^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13893^Generic^Moe^PhysicianThree^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||||||A|||13893^Generic^Moe^PhysicianThree^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|OP^|11130000114^^^UHN^VN^G^4265^^^^^||||||||||||||||||||G|||||201112171022|||||||V|\r" + 
                "PV2|||^^^^^|||||||||||||||||||N||AO|||||||N||||||||||||||||||\r" + 
                "DG1|1||||||||||||||||||||\r"; 
                

        

        
        Persister.persist(UhnConverter.convertAdtOrFail(message1));
        Persister.persist(UhnConverter.convertAdtOrFail(message2));
        
        
        
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
        
        PatientWithVisitsContainer pwvContainer = pwvContainers.get(0);
        Patient patient = pwvContainer.getDocument().myPatient;
        Visit visit = pwvContainer.getDocument().myVisits.get(0);
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > patient.myRecordUpdatedDate.getTime());
        
        //check patient
        assertEquals("M", patient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("196303170000"), patient.myDateOfBirth);
        assertEquals(null, patient.myDeathDateAndTime);
        assertEquals("N", patient.myDeathIndicator);
        assertEquals(null, patient.myMothersMaidenName);
        
        Cx patId = patient.myPatientIds.get(0);
        assertEquals("7007541", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Xpn patName = patient.myPatientNames.get(0);
        assertEquals("Medinfo", patName.myLastName);
        assertEquals("Hyphen", patName.myFirstName);
                
        Xad patAddresses = patient.myPatientAddresses.get(0);
        assertEquals("20 Dundas Street", patAddresses.myStreetAddress);
        assertEquals("TORONTO", patAddresses.myCity);
        
        Xtn patPhone = patient.myPhoneNumbers.get(0);
        assertEquals("(416)123-4567", patPhone.myPhoneNumber);
        
       
        //check visit lastUpdateTime
        assertTrue(nowMilli > visit.myRecordUpdatedDate.getTime());
        
        //check visit
        assertEquals("11130000114", visit.myVisitNumber.myIdNumber);
        assertEquals(Constants.UNDEF_VISIT_STATUS, visit.myVisitStatus);
        assertEquals("R", visit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112161022"), visit.myAdmitDate);
        assertEquals(null, visit.myHospitalService);
        Pl loc = visit.myAssignedPatientLocation;
        assertEquals("8001-CC-Endoscopy", loc.myPointOfCare);
        assertEquals(null, loc.myRoom);
        assertEquals(null, loc.myBed);
        
        Xcn admitDoc = visit.myAdmittingDoctors.get(0);
        assertEquals("13893", admitDoc.myId);
        assertEquals("Moe", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("PhysicianThree", admitDoc.myMiddleName);
        
        Xcn attendDoc = visit.myAttendingDoctors.get(0);
        assertEquals("13893", attendDoc.myId);
        assertEquals("Moe", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("PhysicianThree", attendDoc.myMiddleName);
        
        Xcn refDoc = visit.myReferringDoctors.get(0);
        assertEquals("13893", refDoc.myId);
        assertEquals("Moe", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("PhysicianThree", refDoc.myMiddleName);
        
        
        //check visit arrival times
        List<Date> arrivalDates = visit.myArrivalDates;
        assertEquals(2, arrivalDates.size());
        assertEquals(ourTsFormat.parse("201112161022"), arrivalDates.get(0));
        assertEquals(ourTsFormat.parse("201112171022"), arrivalDates.get(1));        
        
    }    
        
    
    
    
    
    
    /**
     * Testing cancel admit A11
     * - admit a patient then send an A11 to mark the visit as cancelled
     *   
     * @throws Exception ...
     */
    @Test
    public void testAdtA11a() throws Exception {
        
        String message1 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A01^ADT_A01|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A01|201112021621||||201112021621|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "ROL||UC|PP^Primary Care Provider^15ZRole^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^^^^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|O^Office^^^^|^^^ON^^Can^^^^^^^^|(416) 340-3391^WPN^PH^^^^^^^^^\r" + 
                "ROL||UC|PP^Primary Care Provider^15ZRole^^^|13546b^Genericb^Physicianb^Moeb^^Dr.^MD^^UHN^L^^^EI^G^2.16.840.1.113883.3.59.3:947^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|1^Hospital^15ZOrgTyp^^^|^^^ON^^Can^^^^^^^^|(416) 340-3388^WPN^PH^^^^^^^^^\r" +
                "NK1|1|Wph^Mom^^^^^L^^^^^^^|PAR^Parent^03ZRelshp^^^|82 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^~(416)123-1234^PRN^CP^^^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^|N^Next-of-Kin^03ZConRol^^^||||||||||||||||||||||||||||||||\r" +
                "NK1|2|Wph^Brother^^^^^L^^^^^^^|BRO^Brother^03ZRelshp^^^|83 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3601^PRN^PH^^^^^^^^^|(416)525-2525^PRN^PH^^^^^^^^^|C^Emergency Contact^03ZConRol^^^||||||||||||||||||||||||||||||||\r" + 
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000514^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r" + 
                "PV2||S^Semi^03ZFinbed^^^|^kfkfkfjcjcjcgcgcclcl^03ZAmitRes^^^|||||||||||||||||||N||AI|Elective||||||N|||||||OTH^Self^03ZBrInBy^^^|||||||||||\r" + 
                "DG1|1||06^KFKFKFJCJCJCGCGCCLCL^2.16.840.1.113883.11.19436^^^|KFKFKFJCJCJCGCGCCLCL|201112021621|A|||||||||1||D||||\r" + 
                "PR1||||||||||||||||||||\r" + 
                "ZPV|OTH^Self|F^Standard|||N|||13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||Medical Services^General Internal Medicine^GMED|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|413||3910||^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r" + 
                "ZWA||||||||active|\r";
        
        
                String message2 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A11^ADT_A09|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A11|201112021621||||201112021621|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PD1|||UHN^D^^^^UHN^FI^G^4265^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000514^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r" + 
                "PV2|||^^^^^|||||201201101046||||||||||N^||||N||AS|||||||N||||||||||||||||||\r" + 
                "ZPV|OTH^Self|F^Standard|||N|||13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||Medical Services^General Internal Medicine^GMED|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|413||3910||^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r";
        
       
        
        Persister.persist(UhnConverter.convertAdtOrFail(message1));
        Persister.persist(UhnConverter.convertAdtOrFail(message2));        

        
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
        
        PatientWithVisitsContainer pwvContainer = pwvContainers.get(0);
        Patient patient = pwvContainer.getDocument().myPatient;
        Visit visit = pwvContainer.getDocument().myVisits.get(0);
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > patient.myRecordUpdatedDate.getTime());
        
        
        //check patient
        assertEquals("F", patient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), patient.myDateOfBirth);
        assertEquals(null, patient.myDeathDateAndTime);
        assertEquals("N", patient.myDeathIndicator);
        assertEquals(null, patient.myMothersMaidenName);
        
        
        Cx patId = patient.myPatientIds.get(0);
        assertEquals("7012673", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Xpn patName = patient.myPatientNames.get(0);
        assertEquals("Test", patName.myLastName);
        assertEquals("Majaconversion", patName.myFirstName);
                
        Xad patAddresses = patient.myPatientAddresses.get(0);
        assertEquals("1 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        Xtn patPhone = patient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        Ce lang = patient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
        
        
        //check patient roles
        List<PersonInRole> roles  = patient.myPersonInRoles;
        assertEquals(2, roles.size());
        PersonInRole rol_1 = patient.myPersonInRoles.get(0);
        PersonInRole rol_2 = patient.myPersonInRoles.get(1);        
        //ROL(1)
        assertEquals("PP",rol_1.myRole.myCode);
        assertEquals("Primary Care Provider",rol_1.myRole.myText);        
        assertEquals("Generic", rol_1.myPersonNames.get(0).myLastName);
        assertEquals("Physician", rol_1.myPersonNames.get(0).myFirstName);
        assertEquals("Moe", rol_1.myPersonNames.get(0).myMiddleName);
        assertEquals("(416) 340-3391", rol_1.myContactInformation.get(0).myPhoneNumber);
        //ROL(2)
        assertEquals("PP",rol_2.myRole.myCode);
        assertEquals("Primary Care Provider",rol_2.myRole.myText);        
        assertEquals("Genericb", rol_2.myPersonNames.get(0).myLastName);
        assertEquals("Physicianb", rol_2.myPersonNames.get(0).myFirstName);
        assertEquals("Moeb", rol_2.myPersonNames.get(0).myMiddleName);
        assertEquals("(416) 340-3388", rol_2.myContactInformation.get(0).myPhoneNumber);
        
        
        //check patient associated parties
        List<AssociatedParty> parties  = patient.myAssociatedParties;
        assertEquals(2, parties.size());
        AssociatedParty party_1 = patient.myAssociatedParties.get(0);
        AssociatedParty party_2 = patient.myAssociatedParties.get(1);        
        //NK1(1)
        assertEquals("Wph",party_1.myNames.get(0).myLastName);
        assertEquals("Mom",party_1.myNames.get(0).myFirstName);
        assertEquals("PAR",party_1.myRelationship.myCode);
        assertEquals("Parent",party_1.myRelationshipName);
        assertEquals("82 Buttonwood Avenue",party_1.myAddresses.get(0).myStreetAddress);
        assertEquals("(416)243-3600",party_1.myContactInformation.get(0).myPhoneNumber);
        //NK1(2)
        assertEquals("Wph",party_2.myNames.get(0).myLastName);
        assertEquals("Brother",party_2.myNames.get(0).myFirstName);
        assertEquals("BRO",party_2.myRelationship.myCode);
        assertEquals("Brother",party_2.myRelationshipName);
        assertEquals("83 Buttonwood Avenue",party_2.myAddresses.get(0).myStreetAddress);
        assertEquals("(416)243-3601",party_2.myContactInformation.get(0).myPhoneNumber);
                        
        
        //check visit lastUpdateTime
        assertTrue(nowMilli > visit.myRecordUpdatedDate.getTime());
        
        //check visit
        assertEquals("11110000514", visit.myVisitNumber.myIdNumber);
        assertEquals(Constants.CANCELLED_VISIT_STATUS, visit.myVisitStatus);
        assertEquals("I", visit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112021621"), visit.myAdmitDate);
        assertEquals("hospServ", visit.myHospitalService);
        Pl loc = visit.myAssignedPatientLocation;
        assertEquals("PMH 15C", loc.myPointOfCare);
        assertEquals("413", loc.myRoom);
        assertEquals("2", loc.myBed);
        
        Xcn admitDoc = visit.myAdmittingDoctors.get(0);
        assertEquals("13546c", admitDoc.myId);
        assertEquals("Physician", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("MoeC", admitDoc.myMiddleName);
        
        Xcn attendDoc = visit.myAttendingDoctors.get(0);
        assertEquals("13546a", attendDoc.myId);
        assertEquals("Physician", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("MoeA", attendDoc.myMiddleName);
        
        Xcn refDoc = visit.myReferringDoctors.get(0);
        assertEquals("13546b", refDoc.myId);
        assertEquals("Physician", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("MoeB", refDoc.myMiddleName);
        
         
        Diagnosis dg = visit.myDiagnoses.get(0);
        Ce dgCode = dg.myDiagnosis;
        assertEquals("06", dgCode.myCode);
        assertEquals("KFKFKFJCJCJCGCGCCLCL", dgCode.myText);
        assertEquals("2.16.840.1.113883.11.19436", dgCode.myCodeSystem);
        
        
        
    }    
    
    
    
    /**
     * Testing cancel admit A11
     * - A11 message sent but no prior admit message sent 
     *   
     * @throws Exception ...
     */
    @Test
    public void testAdtA11b() throws Exception {
        
        String message1 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A11^ADT_A09|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A11|201112021621||||201112021621|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PD1|||UHN^D^^^^UHN^FI^G^4265^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000514^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r" + 
                "PV2|||^^^^^|||||201201101046||||||||||N^||||N||AS|||||||N||||||||||||||||||\r" + 
                "ZPV|OTH^Self|F^Standard|||N|||13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||Medical Services^General Internal Medicine^GMED|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|413||3910||^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r";
        
       
        
        Persister.persist(UhnConverter.convertAdtOrFail(message1));
                
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
        
        PatientWithVisitsContainer pwvContainer = pwvContainers.get(0);
        Patient patient = pwvContainer.getDocument().myPatient;
        Visit visit = pwvContainer.getDocument().myVisits.get(0);
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > patient.myRecordUpdatedDate.getTime());
       
        
        //check patient
        assertEquals("F", patient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), patient.myDateOfBirth);
        assertEquals(null, patient.myDeathDateAndTime);
        assertEquals("N", patient.myDeathIndicator);
        assertEquals(null, patient.myMothersMaidenName);
        
        
        Cx patId = patient.myPatientIds.get(0);
        assertEquals("7012673", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Xpn patName = patient.myPatientNames.get(0);
        assertEquals("Test", patName.myLastName);
        assertEquals("Majaconversion", patName.myFirstName);
                
        Xad patAddresses = patient.myPatientAddresses.get(0);
        assertEquals("1 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        Xtn patPhone = patient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        Ce lang = patient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
        
        //check visit lastUpdateTime
        assertTrue(nowMilli > visit.myRecordUpdatedDate.getTime());
        
        //check visit
        assertEquals("11110000514", visit.myVisitNumber.myIdNumber);
        assertEquals(Constants.CANCELLED_VISIT_STATUS, visit.myVisitStatus);
        assertEquals("I", visit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112021621"), visit.myAdmitDate);
        assertEquals("hospServ", visit.myHospitalService);
        Pl loc = visit.myAssignedPatientLocation;
        assertEquals("PMH 15C", loc.myPointOfCare);
        assertEquals("413", loc.myRoom);
        assertEquals("2", loc.myBed);
        
        Xcn admitDoc = visit.myAdmittingDoctors.get(0);
        assertEquals("13546c", admitDoc.myId);
        assertEquals("Physician", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("MoeC", admitDoc.myMiddleName);
        
        Xcn attendDoc = visit.myAttendingDoctors.get(0);
        assertEquals("13546a", attendDoc.myId);
        assertEquals("Physician", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("MoeA", attendDoc.myMiddleName);
        
        Xcn refDoc = visit.myReferringDoctors.get(0);
        assertEquals("13546b", refDoc.myId);
        assertEquals("Physician", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("MoeB", refDoc.myMiddleName);
        
         
    }        
    
    
    
    
    
    /**
     * 
     * Testing patient discharge reversal A13
     * - admit patient into a bed
     * - discharge the patient
     * - reverse the discharge
     *  
     * @throws Exception ...
     */
    @Test
    public void testAdtA13() throws Exception {
        
        String message1 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A01^ADT_A01|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A01|201112021621||||201112021621|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000514^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r" + 
                "PV2||S^Semi^03ZFinbed^^^|^kfkfkfjcjcjcgcgcclcl^03ZAmitRes^^^|||||||||||||||||||N||AI|Elective||||||N|||||||OTH^Self^03ZBrInBy^^^|||||||||||\r" + 
                "DG1|1||06^KFKFKFJCJCJCGCGCCLCL^2.16.840.1.113883.11.19436|KFKFKFJCJCJCGCGCCLCL|201112021621|A|||||||||1||D||||\r" + 
                "PR1||||||||||||||||||||\r" + 
                "ZPV|OTH^Self|F^Standard|||N|||13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||Medical Services^General Internal Medicine^GMED|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|413||3910||^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r" + 
                "ZWA||||||||active|\r";
        
        String message2 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112070000||ADT^A03^ADT_A03|123710|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A03|201112070000||||201112070000|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||A|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000514^^^UHN^VN^G^4265^^^^^|||||||||||||||||1|||G|||||201112021621|201112052359||||||V|\r" + 
                "PV2||S^Semi^03ZFinbed^^^|^kfkfkfjcjcjcgcgcclcl^03ZAmitRes^^^|||||||||||||||||||N||AI|Elective||||||N|||||||OTH^Self^03ZBrInBy^^^|||||||||||\r" + 
                "DG1|1||||||||||||||||||||\r" + 
                "PR1||||||||||||||||||||\r" + 
                "IN1|1||001001^^^UHN^^G^4265^^^^^|OHIP^D^001001^^^UHN^^G^4265^^^|||||||||20111201|||Test^Majaconversion^^^^^L^^^^^^^|SEL^Self^15ZRelshp^^^||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^||||||||||||||||||||||||||||H||7012673^^^UHN^PI^G^4265^^^^^||||\r" + 
                "IN2||||||9287170261||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||\r" + 
                "IN3|1||||||||||||||||||||||||\r" + 
                "ZIN||||||||||||||||||\r" + 
                "ZPV|OTH^Self|^|||N|||3210^Generic^Physician-EM^^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|6835^Generic^Anaesthetist^^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||^^|3210^Generic^Physician-EM^^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|||||^^^^^|3210^Generic^Physician-EM^^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r";
        
        
        String message3 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112070000||ADT^A13^ADT_A01|123710|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A13|201112070000||||201112070000|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" +
                "PD1|||UHN^D^^^^UHN^FI^G^4265^^^|3210^Generic^Physician-EM^^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" +
                "PV1||I|^^^G^4265^^^C^^^^^|R||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000514^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|201112052359||||||V|\r" +
                "PV2|||^Cardiac arrest-non traumatic ^03ZAmitRes^^^|||||||||||||||||||N||AE|||||||N|||||||OTH^Self^03ZBrInBy^^^|||||||||||\r" + 
                "ZPV|OTH^Self|F^Standard|||N|||3210^Generic^Physician-EM^^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|6835^Generic^Anaesthetist^^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||^^|3210^Generic^Physician-EM^^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|||4265|201112151112|6^Level 1, Resuscitation^^^^|3210^Generic^Physician-EM^^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r";
        
        
        Persister.persist(UhnConverter.convertAdtOrFail(message1));
        Persister.persist(UhnConverter.convertAdtOrFail(message2));
        Persister.persist(UhnConverter.convertAdtOrFail(message3));
        
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
        
        PatientWithVisitsContainer pwvContainer = pwvContainers.get(0);
        Patient patient = pwvContainer.getDocument().myPatient;
        Visit visit = pwvContainer.getDocument().myVisits.get(0);
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > patient.myRecordUpdatedDate.getTime());
        
        //check patient
        assertEquals("F", patient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), patient.myDateOfBirth);
        assertEquals(null, patient.myDeathDateAndTime);
        assertEquals("N", patient.myDeathIndicator);
        assertEquals(null, patient.myMothersMaidenName);
        
        
        Cx patId = patient.myPatientIds.get(0);
        assertEquals("7012673", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Xpn patName = patient.myPatientNames.get(0);
        assertEquals("Test", patName.myLastName);
        assertEquals("Majaconversion", patName.myFirstName);
                
        Xad patAddresses = patient.myPatientAddresses.get(0);
        assertEquals("1 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        Xtn patPhone = patient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        Ce lang = patient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
        
        
        //check visit lastUpdateTime
        assertTrue(nowMilli > visit.myRecordUpdatedDate.getTime());
                        
        
        //check visit
        assertEquals("11110000514", visit.myVisitNumber.myIdNumber);
        assertEquals(Constants.ACTIVE_VISIT_STATUS, visit.myVisitStatus);
        assertEquals("I", visit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112021621"), visit.myAdmitDate);                
        assertEquals(null, visit.myDischargeDates);
        assertEquals("hospServ", visit.myHospitalService);
        
        
        Pl loc = visit.myAssignedPatientLocation;
        assertEquals("PMH 15C", loc.myPointOfCare);
        assertEquals("413", loc.myRoom);
        assertEquals("2", loc.myBed);
                      
        
        Xcn admitDoc = visit.myAdmittingDoctors.get(0);
        assertEquals("13546c", admitDoc.myId);
        assertEquals("Physician", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("MoeC", admitDoc.myMiddleName);
        
        Xcn attendDoc = visit.myAttendingDoctors.get(0);
        assertEquals("13546a", attendDoc.myId);
        assertEquals("Physician", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("MoeA", attendDoc.myMiddleName);
        
        Xcn refDoc = visit.myReferringDoctors.get(0);
        assertEquals("13546b", refDoc.myId);
        assertEquals("Physician", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("MoeB", refDoc.myMiddleName);
        
        Diagnosis dg = visit.myDiagnoses.get(0);
        Ce dgCode = dg.myDiagnosis;
        assertEquals("06", dgCode.myCode);
        assertEquals("KFKFKFJCJCJCGCGCCLCL", dgCode.myText);
        assertEquals("2.16.840.1.113883.11.19436", dgCode.myCodeSystem);
        
    }   
    
    
    
    /**
     * Test patient bed swap A17
     * - add both patients
     * - swap their beds with A17
     *  
     *   
     * @throws Exception ...
     */
    @SuppressWarnings("null")
    @Test
    public void testAdtA17() throws Exception {
        

        String firstPatientMsg = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A01^ADT_A01|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A01|201112021621||||201112021621|G^4265^L\r" + 
                "PID|||7012672^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000513^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000513^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r";
        
        
        String secondPatientMsg = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A01^ADT_A01|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A01|201112021621||||201112021621|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test2^Majaconversion2^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731229|F|||10 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PV1||I|PMH 15D^414^3^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000514^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r"; 
        
        
        String swapMsg = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201201111352||ADT^A17^ADT_A17|124466|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A17|201201111352||||201201111352|G^4265^L\r" +
                "PID|||7012672^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000513^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" +
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "PV1||I|PMH 15D^414^3^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000513^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r" +
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test2^Majaconversion2^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731229|F|||10 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" +
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000514^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r";
        
       
        
        
        Persister.persist(UhnConverter.convertAdtOrFail(firstPatientMsg));
        Persister.persist(UhnConverter.convertAdtOrFail(secondPatientMsg));        
        Persister.persist(UhnConverter.convertAdtOrFail(swapMsg));
        
        
               
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
        
        //There should be 2 pwv docs now
        assertEquals(2, pwvContainers.size());
        
        Patient firstPat = null;
        Visit firstPatVisit = null;        
        Patient secPat = null;
        Visit secPatVisit = null;        
                        
        for (PatientWithVisitsContainer pwvContainer : pwvContainers) {
            
            if ( "7012672".equals(pwvContainer.getDocument().getMrn().myIdNumber) ) {
                firstPat = pwvContainer.getDocument().myPatient;  
                firstPatVisit = pwvContainer.getDocument().myVisits.get(0);
            }
            
            if ( "7012673".equals(pwvContainer.getDocument().getMrn().myIdNumber) ) {
                secPat = pwvContainer.getDocument().myPatient;  
                secPatVisit = pwvContainer.getDocument().myVisits.get(0);
            }
            
            
        }
        
        //make sure second and first patients and visits exist
        assertTrue(secPat != null);
        assertTrue(secPatVisit != null);
        assertTrue(firstPat != null);
        assertTrue(firstPatVisit != null);
        
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > firstPat.myRecordUpdatedDate.getTime());
        
        
        //check first patient
        assertEquals("F", firstPat.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), firstPat.myDateOfBirth);
        assertEquals(null, firstPat.myDeathDateAndTime);
        assertEquals("N", firstPat.myDeathIndicator);
        assertEquals(null, firstPat.myMothersMaidenName);
        
        
        Cx patId = firstPat.myPatientIds.get(0);
        assertEquals("7012672", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Xpn patName = firstPat.myPatientNames.get(0);
        assertEquals("Test", patName.myLastName);
        assertEquals("Majaconversion", patName.myFirstName);
                
        Xad patAddresses = firstPat.myPatientAddresses.get(0);
        assertEquals("1 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        Xtn patPhone = firstPat.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        Ce lang = firstPat.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
        
        //check visit lastUpdateTime
        assertTrue(nowMilli > firstPatVisit.myRecordUpdatedDate.getTime());
           
        
        //check first patient's visit
        assertEquals("11110000513", firstPatVisit.myVisitNumber.myIdNumber);        
        assertEquals(Constants.ACTIVE_VISIT_STATUS, firstPatVisit.myVisitStatus);        
        assertEquals("I", firstPatVisit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112021621"), firstPatVisit.myAdmitDate);
        assertEquals("hospServ", firstPatVisit.myHospitalService);
        
        Xcn admitDoc = firstPatVisit.myAdmittingDoctors.get(0);
        assertEquals("13546c", admitDoc.myId);
        assertEquals("Physician", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("MoeC", admitDoc.myMiddleName);
        
        Xcn attendDoc = firstPatVisit.myAttendingDoctors.get(0);
        assertEquals("13546a", attendDoc.myId);
        assertEquals("Physician", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("MoeA", attendDoc.myMiddleName);
        
        Xcn refDoc = firstPatVisit.myReferringDoctors.get(0);
        assertEquals("13546b", refDoc.myId);
        assertEquals("Physician", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("MoeB", refDoc.myMiddleName);
        
        //check first patient's assigned and prior locations
        Pl cloc = firstPatVisit.myAssignedPatientLocation;
        assertEquals("PMH 15D", cloc.myPointOfCare);
        assertEquals("414", cloc.myRoom);
        assertEquals("3", cloc.myBed);
                
        Pl ploc = firstPatVisit.myPriorPatientLocation;
        assertEquals("PMH 15C", ploc.myPointOfCare);
        assertEquals("413", ploc.myRoom);
        assertEquals("2", ploc.myBed);
        
        //check patient lastUpdateTime
        assertTrue(nowMilli > secPat.myRecordUpdatedDate.getTime());
                
        //check second patient
        assertEquals("F", secPat.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312290000"), secPat.myDateOfBirth);
        assertEquals(null, secPat.myDeathDateAndTime);
        assertEquals("N", secPat.myDeathIndicator);
        assertEquals(null, secPat.myMothersMaidenName);
        
        
        patId = secPat.myPatientIds.get(0);
        assertEquals("7012673", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        patName = secPat.myPatientNames.get(0);
        assertEquals("Test2", patName.myLastName);
        assertEquals("Majaconversion2", patName.myFirstName);
                
        patAddresses = secPat.myPatientAddresses.get(0);
        assertEquals("10 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        patPhone = secPat.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        lang = secPat.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
        
        //check visit lastUpdateTime
        assertTrue(nowMilli > secPatVisit.myRecordUpdatedDate.getTime());
            
        
        //check second patient's visit
        assertEquals("11110000514", secPatVisit.myVisitNumber.myIdNumber);        
        assertEquals(Constants.ACTIVE_VISIT_STATUS, secPatVisit.myVisitStatus);
        assertEquals("I", secPatVisit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112021621"), secPatVisit.myAdmitDate);
        assertEquals("hospServ", secPatVisit.myHospitalService);
        
        admitDoc = secPatVisit.myAdmittingDoctors.get(0);
        assertEquals("13546c", admitDoc.myId);
        assertEquals("Physician", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("MoeC", admitDoc.myMiddleName);
        
        attendDoc = secPatVisit.myAttendingDoctors.get(0);
        assertEquals("13546a", attendDoc.myId);
        assertEquals("Physician", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("MoeA", attendDoc.myMiddleName);
        
        refDoc = secPatVisit.myReferringDoctors.get(0);
        assertEquals("13546b", refDoc.myId);
        assertEquals("Physician", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("MoeB", refDoc.myMiddleName);
        
        //check second patient's assigned and prior locations
        cloc = secPatVisit.myAssignedPatientLocation;
        assertEquals("PMH 15C", cloc.myPointOfCare);
        assertEquals("413", cloc.myRoom);
        assertEquals("2", cloc.myBed);
                
        ploc = secPatVisit.myPriorPatientLocation;
        assertEquals("PMH 15D", ploc.myPointOfCare);
        assertEquals("414", ploc.myRoom);
        assertEquals("3", ploc.myBed);
        
    }        
    
    
    
    
    /**
     * Testing delete patient encounter A23
     * - admit a patient and then send A23 to mark the visit as deleted 
     *   
     * @throws Exception ...
     */
    @Test
    public void testAdtA23a() throws Exception {
        
        String message1 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A01^ADT_A01|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A01|201112021621||||201112021621|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "ROL||UC|PP^Primary Care Provider^15ZRole^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^^^^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|O^Office^^^^|^^^ON^^Can^^^^^^^^|(416) 340-3391^WPN^PH^^^^^^^^^\r" + 
                "ROL||UC|PP^Primary Care Provider^15ZRole^^^|13546b^Genericb^Physicianb^Moeb^^Dr.^MD^^UHN^L^^^EI^G^2.16.840.1.113883.3.59.3:947^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|1^Hospital^15ZOrgTyp^^^|^^^ON^^Can^^^^^^^^|(416) 340-3388^WPN^PH^^^^^^^^^\r" +
                "NK1|1|Wph^Mom^^^^^L^^^^^^^|PAR^Parent^03ZRelshp^^^|82 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^~(416)123-1234^PRN^CP^^^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^|N^Next-of-Kin^03ZConRol^^^||||||||||||||||||||||||||||||||\r" +
                "NK1|2|Wph^Brother^^^^^L^^^^^^^|BRO^Brother^03ZRelshp^^^|83 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3601^PRN^PH^^^^^^^^^|(416)525-2525^PRN^PH^^^^^^^^^|C^Emergency Contact^03ZConRol^^^||||||||||||||||||||||||||||||||\r" + 
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000514^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r" + 
                "PV2||S^Semi^03ZFinbed^^^|^kfkfkfjcjcjcgcgcclcl^03ZAmitRes^^^|||||||||||||||||||N||AI|Elective||||||N|||||||OTH^Self^03ZBrInBy^^^|||||||||||\r" + 
                "DG1|1||06^KFKFKFJCJCJCGCGCCLCL^2.16.840.1.113883.11.19436^^^|KFKFKFJCJCJCGCGCCLCL|201112021621|A|||||||||1||D||||\r" + 
                "PR1||||||||||||||||||||\r" + 
                "ZPV|OTH^Self|F^Standard|||N|||13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||Medical Services^General Internal Medicine^GMED|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|413||3910||^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r" + 
                "ZWA||||||||active|\r";
        
        
                String message2 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A23^ADT_A21|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A23|201112021621||||201112021621|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PD1|||UHN^D^^^^UHN^FI^G^4265^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000514^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r" + 
                "PV2|||^^^^^|||||201201101046||||||||||N^||||N||AS|||||||N||||||||||||||||||\r";  
                
        
       
        
        Persister.persist(UhnConverter.convertAdtOrFail(message1));
        Persister.persist(UhnConverter.convertAdtOrFail(message2));        

        
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
        
        PatientWithVisitsContainer pwvContainer = pwvContainers.get(0);
        Patient patient = pwvContainer.getDocument().myPatient;
        Visit visit = pwvContainer.getDocument().myVisits.get(0);
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > patient.myRecordUpdatedDate.getTime());
        
        //check patient
        assertEquals("F", patient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), patient.myDateOfBirth);
        assertEquals(null, patient.myDeathDateAndTime);
        assertEquals("N", patient.myDeathIndicator);
        assertEquals(null, patient.myMothersMaidenName);
        
        
        Cx patId = patient.myPatientIds.get(0);
        assertEquals("7012673", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Xpn patName = patient.myPatientNames.get(0);
        assertEquals("Test", patName.myLastName);
        assertEquals("Majaconversion", patName.myFirstName);
                
        Xad patAddresses = patient.myPatientAddresses.get(0);
        assertEquals("1 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        Xtn patPhone = patient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        Ce lang = patient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
        
        
        //check patient roles
        List<PersonInRole> roles  = patient.myPersonInRoles;
        assertEquals(2, roles.size());
        PersonInRole rol_1 = patient.myPersonInRoles.get(0);
        PersonInRole rol_2 = patient.myPersonInRoles.get(1);        
        //ROL(1)
        assertEquals("PP",rol_1.myRole.myCode);
        assertEquals("Primary Care Provider",rol_1.myRole.myText);        
        assertEquals("Generic", rol_1.myPersonNames.get(0).myLastName);
        assertEquals("Physician", rol_1.myPersonNames.get(0).myFirstName);
        assertEquals("Moe", rol_1.myPersonNames.get(0).myMiddleName);
        assertEquals("(416) 340-3391", rol_1.myContactInformation.get(0).myPhoneNumber);
        //ROL(2)
        assertEquals("PP",rol_2.myRole.myCode);
        assertEquals("Primary Care Provider",rol_2.myRole.myText);        
        assertEquals("Genericb", rol_2.myPersonNames.get(0).myLastName);
        assertEquals("Physicianb", rol_2.myPersonNames.get(0).myFirstName);
        assertEquals("Moeb", rol_2.myPersonNames.get(0).myMiddleName);
        assertEquals("(416) 340-3388", rol_2.myContactInformation.get(0).myPhoneNumber);
        
        
        //check patient associated parties
        List<AssociatedParty> parties  = patient.myAssociatedParties;
        assertEquals(2, parties.size());
        AssociatedParty party_1 = patient.myAssociatedParties.get(0);
        AssociatedParty party_2 = patient.myAssociatedParties.get(1);        
        //NK1(1)
        assertEquals("Wph",party_1.myNames.get(0).myLastName);
        assertEquals("Mom",party_1.myNames.get(0).myFirstName);
        assertEquals("PAR",party_1.myRelationship.myCode);
        assertEquals("Parent",party_1.myRelationshipName);
        assertEquals("82 Buttonwood Avenue",party_1.myAddresses.get(0).myStreetAddress);
        assertEquals("(416)243-3600",party_1.myContactInformation.get(0).myPhoneNumber);
        //NK1(2)
        assertEquals("Wph",party_2.myNames.get(0).myLastName);
        assertEquals("Brother",party_2.myNames.get(0).myFirstName);
        assertEquals("BRO",party_2.myRelationship.myCode);
        assertEquals("Brother",party_2.myRelationshipName);
        assertEquals("83 Buttonwood Avenue",party_2.myAddresses.get(0).myStreetAddress);
        assertEquals("(416)243-3601",party_2.myContactInformation.get(0).myPhoneNumber);
        
        //check visit lastUpdateTime
        assertTrue(nowMilli > visit.myRecordUpdatedDate.getTime());
        
        //check visit
        assertEquals("11110000514", visit.myVisitNumber.myIdNumber);
        assertEquals(Constants.DELETED_VISIT_STATUS, visit.myVisitStatus);
        assertEquals("I", visit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112021621"), visit.myAdmitDate);
        assertEquals("hospServ", visit.myHospitalService);
        Pl loc = visit.myAssignedPatientLocation;
        assertEquals("PMH 15C", loc.myPointOfCare);
        assertEquals("413", loc.myRoom);
        assertEquals("2", loc.myBed);
        
        Xcn admitDoc = visit.myAdmittingDoctors.get(0);
        assertEquals("13546c", admitDoc.myId);
        assertEquals("Physician", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("MoeC", admitDoc.myMiddleName);
        
        Xcn attendDoc = visit.myAttendingDoctors.get(0);
        assertEquals("13546a", attendDoc.myId);
        assertEquals("Physician", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("MoeA", attendDoc.myMiddleName);
        
        Xcn refDoc = visit.myReferringDoctors.get(0);
        assertEquals("13546b", refDoc.myId);
        assertEquals("Physician", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("MoeB", refDoc.myMiddleName);
        
         
        Diagnosis dg = visit.myDiagnoses.get(0);
        Ce dgCode = dg.myDiagnosis;
        assertEquals("06", dgCode.myCode);
        assertEquals("KFKFKFJCJCJCGCGCCLCL", dgCode.myText);
        assertEquals("2.16.840.1.113883.11.19436", dgCode.myCodeSystem);
        
        
        
    }    
    
    
    
    /**
     * Testing delete patient encounter A23
     * - sent A23 to delete an encounter but no prior admit message sent 
     *   
     * @throws Exception ...
     */
    @Test
    public void testAdtA23b() throws Exception {
        
        String message1 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A23^ADT_A21|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A23|201112021621||||201112021621|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PD1|||UHN^D^^^^UHN^FI^G^4265^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000514^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r" + 
                "PV2|||^^^^^|||||201201101046||||||||||N^||||N||AS|||||||N||||||||||||||||||\r"; 
                
        
       
        
        Persister.persist(UhnConverter.convertAdtOrFail(message1));
                
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
        
        PatientWithVisitsContainer pwvContainer = pwvContainers.get(0);
        Patient patient = pwvContainer.getDocument().myPatient;
        Visit visit = pwvContainer.getDocument().myVisits.get(0);
        
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > patient.myRecordUpdatedDate.getTime());
        
        //check patient
        assertEquals("F", patient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), patient.myDateOfBirth);
        assertEquals(null, patient.myDeathDateAndTime);
        assertEquals("N", patient.myDeathIndicator);
        assertEquals(null, patient.myMothersMaidenName);
        
        
        Cx patId = patient.myPatientIds.get(0);
        assertEquals("7012673", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Xpn patName = patient.myPatientNames.get(0);
        assertEquals("Test", patName.myLastName);
        assertEquals("Majaconversion", patName.myFirstName);
                
        Xad patAddresses = patient.myPatientAddresses.get(0);
        assertEquals("1 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        Xtn patPhone = patient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        Ce lang = patient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
        
        //check visit lastUpdateTime
        assertTrue(nowMilli > visit.myRecordUpdatedDate.getTime());
        
        //check visit
        assertEquals("11110000514", visit.myVisitNumber.myIdNumber);
        assertEquals(Constants.DELETED_VISIT_STATUS, visit.myVisitStatus);
        assertEquals("I", visit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112021621"), visit.myAdmitDate);
        assertEquals("hospServ", visit.myHospitalService);
        Pl loc = visit.myAssignedPatientLocation;
        assertEquals("PMH 15C", loc.myPointOfCare);
        assertEquals("413", loc.myRoom);
        assertEquals("2", loc.myBed);
        
        Xcn admitDoc = visit.myAdmittingDoctors.get(0);
        assertEquals("13546c", admitDoc.myId);
        assertEquals("Physician", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("MoeC", admitDoc.myMiddleName);
        
        Xcn attendDoc = visit.myAttendingDoctors.get(0);
        assertEquals("13546a", attendDoc.myId);
        assertEquals("Physician", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("MoeA", attendDoc.myMiddleName);
        
        Xcn refDoc = visit.myReferringDoctors.get(0);
        assertEquals("13546b", refDoc.myId);
        assertEquals("Physician", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("MoeB", refDoc.myMiddleName);
        
         
    }        
    
        
    
    
    
    
    
    
    
    
    
    /**
     * Testing add patient/person info A28
     *   
     * @throws Exception ...
     */
    @Test
    public void testAdtA28() throws Exception {
        
        String message1 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112051214||ADT^A28^ADT_A05|123492|T^|2.5^^||||||CAN||||\r" + 
        		"EVN|A28|201112051214||||201112051214|G^4265^L\r" + 
        		"PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112051214^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||^^^^^^^^^^^~||||||||||||N|||201112051214||||||\r" + 
        		"PD1|||UHN^D^^^^UHN^FI^^^^^|3210^Generic^Physician-EM^^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" +
        		"ROL||UC|PP^Primary Care Provider^15ZRole^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^^^^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|O^Office^^^^|^^^ON^^Can^^^^^^^^|(416) 340-3391^WPN^PH^^^^^^^^^\r" + 
                "ROL||UC|PP^Primary Care Provider^15ZRole^^^|13546b^Genericb^Physicianb^Moeb^^Dr.^MD^^UHN^L^^^EI^G^2.16.840.1.113883.3.59.3:947^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|1^Hospital^15ZOrgTyp^^^|^^^ON^^Can^^^^^^^^|(416) 340-3388^WPN^PH^^^^^^^^^\r" +
                "NK1|1|Wph^Mom^^^^^L^^^^^^^|PAR^Parent^03ZRelshp^^^|82 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^~(416)123-1234^PRN^CP^^^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^|N^Next-of-Kin^03ZConRol^^^||||||||||||||||||||||||||||||||\r" +
                "NK1|2|Wph^Brother^^^^^L^^^^^^^|BRO^Brother^03ZRelshp^^^|83 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3601^PRN^PH^^^^^^^^^|(416)525-2525^PRN^PH^^^^^^^^^|C^Emergency Contact^03ZConRol^^^||||||||||||||||||||||||||||||||\r" + 
        		"PV1||||||^^^^^^^^^^^  ^||||||||||||^|^^^^^^^^^^^|||||||||||||||||||||||||||||||||\r";
       
        
        Persister.persist(UhnConverter.convertAdtOrFail(message1));

        
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
        
        PatientWithVisitsContainer pwvContainer = pwvContainers.get(0);
        Patient patient = pwvContainer.getDocument().myPatient;
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > patient.myRecordUpdatedDate.getTime());
        
        //check patient
        assertEquals("F", patient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), patient.myDateOfBirth);
        assertEquals(null, patient.myDeathDateAndTime);
        assertEquals("N", patient.myDeathIndicator);
        assertEquals(null, patient.myMothersMaidenName);
        
        
        Cx patId = patient.myPatientIds.get(0);
        assertEquals("7012673", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Cx patId2 = patient.myPatientIds.get(1);
        assertEquals("9287170261", patId2.myIdNumber);
        assertEquals("JHN", patId2.myIdTypeCode);
        
        Xpn patName = patient.myPatientNames.get(0);
        assertEquals("Test", patName.myLastName);
        assertEquals("Majaconversion", patName.myFirstName);
                
        Xad patAddresses = patient.myPatientAddresses.get(0);
        assertEquals("1 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        Xtn patPhone = patient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        Ce lang = patient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
        
        

        //check patient roles
        List<PersonInRole> roles  = patient.myPersonInRoles;
        assertEquals(2, roles.size());
        PersonInRole rol_1 = patient.myPersonInRoles.get(0);
        PersonInRole rol_2 = patient.myPersonInRoles.get(1);        
        //ROL(1)
        assertEquals("PP",rol_1.myRole.myCode);
        assertEquals("Primary Care Provider",rol_1.myRole.myText);        
        assertEquals("Generic", rol_1.myPersonNames.get(0).myLastName);
        assertEquals("Physician", rol_1.myPersonNames.get(0).myFirstName);
        assertEquals("Moe", rol_1.myPersonNames.get(0).myMiddleName);
        assertEquals("(416) 340-3391", rol_1.myContactInformation.get(0).myPhoneNumber);
        //ROL(2)
        assertEquals("PP",rol_2.myRole.myCode);
        assertEquals("Primary Care Provider",rol_2.myRole.myText);        
        assertEquals("Genericb", rol_2.myPersonNames.get(0).myLastName);
        assertEquals("Physicianb", rol_2.myPersonNames.get(0).myFirstName);
        assertEquals("Moeb", rol_2.myPersonNames.get(0).myMiddleName);
        assertEquals("(416) 340-3388", rol_2.myContactInformation.get(0).myPhoneNumber);
        
        
        //check patient associated parties
        List<AssociatedParty> parties  = patient.myAssociatedParties;
        assertEquals(2, parties.size());
        AssociatedParty party_1 = patient.myAssociatedParties.get(0);
        AssociatedParty party_2 = patient.myAssociatedParties.get(1);        
        //NK1(1)
        assertEquals("Wph",party_1.myNames.get(0).myLastName);
        assertEquals("Mom",party_1.myNames.get(0).myFirstName);
        assertEquals("PAR",party_1.myRelationship.myCode);
        assertEquals("Parent",party_1.myRelationshipName);
        assertEquals("82 Buttonwood Avenue",party_1.myAddresses.get(0).myStreetAddress);
        assertEquals("(416)243-3600",party_1.myContactInformation.get(0).myPhoneNumber);
        //NK1(2)
        assertEquals("Wph",party_2.myNames.get(0).myLastName);
        assertEquals("Brother",party_2.myNames.get(0).myFirstName);
        assertEquals("BRO",party_2.myRelationship.myCode);
        assertEquals("Brother",party_2.myRelationshipName);
        assertEquals("83 Buttonwood Avenue",party_2.myAddresses.get(0).myStreetAddress);
        assertEquals("(416)243-3601",party_2.myContactInformation.get(0).myPhoneNumber);
                
            
        
        //Should be no visit info stored
        assertEquals(null, pwvContainer.getDocument().myVisits);
            
        
    }    
    
    
    
    /**
     * Testing update patient/person info A31
     * - add patient with A28 then update patient with A31
     *   
     * @throws Exception ...
     */
    @Test
    public void testAdtA31() throws Exception {
        
        String message1 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112051214||ADT^A28^ADT_A05|123492|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A28|201112051214||||201112051214|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112051214^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||^^^^^^^^^^^~||||||||||||N|||201112051214||||||\r" + 
                "PD1|||UHN^D^^^^UHN^FI^^^^^|3210^Generic^Physician-EM^^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "PV1||||||^^^^^^^^^^^  ^||||||||||||^|^^^^^^^^^^^|||||||||||||||||||||||||||||||||\r";
        
        String message2 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201201111142||ADT^A31^ADT_A05|124434|T^|2.5^^||||||CAN||||\r" + 
        		"EVN|A31|201201111142||||201201111142|G^4265^L\r" + 
        		"PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test2^Majaconversion^^^Miss^^L^^^^^201201111142^^~Test^Maj^^^Miss^^A^^^^^^^||19731230|F|||11 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3334^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||^^^^^^^^^^^~||||||||||||N|||201201111142||||||\r" + 
        		"PD1|||UHN^D^^^^UHN^FI^^^^^|3210^Generic^Physician-EM^^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
        		"ROL||UC|PP^Primary Care Provider^15ZRole^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^^^^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|O^Office^^^^|^^^ON^^Can^^^^^^^^|(416) 340-3391^WPN^PH^^^^^^^^^\r" + 
                "ROL||UC|PP^Primary Care Provider^15ZRole^^^|13546b^Genericb^Physicianb^Moeb^^Dr.^MD^^UHN^L^^^EI^G^2.16.840.1.113883.3.59.3:947^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|1^Hospital^15ZOrgTyp^^^|^^^ON^^Can^^^^^^^^|(416) 340-3388^WPN^PH^^^^^^^^^\r" +
                "NK1|1|Wph^Mom^^^^^L^^^^^^^|PAR^Parent^03ZRelshp^^^|82 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^~(416)123-1234^PRN^CP^^^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^|N^Next-of-Kin^03ZConRol^^^||||||||||||||||||||||||||||||||\r" +
                "NK1|2|Wph^Brother^^^^^L^^^^^^^|BRO^Brother^03ZRelshp^^^|83 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3601^PRN^PH^^^^^^^^^|(416)525-2525^PRN^PH^^^^^^^^^|C^Emergency Contact^03ZConRol^^^||||||||||||||||||||||||||||||||\r" + 
                "PV1||P|2C Pre Operative Care Unit^^^G^4265^^^C^^^2C POCU^1427^|||^^^G^4265^^^^^^^  ^|13546^Generic^Physician^Moe^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|||M^Medical|||||||13546^Generic^Physician^Moe^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|SP^|11150000130^^^UHN^VN^G^4265^^^^^||||||||||||||||||||G|||||201210121059|||||||V|\r" + 
        		"PV2|||^^^^^|||||201201101314||||||||||||||N|||||||||N||||||||||||||||||\r" + 
        		"ZPV|^|^|||N||324|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|^^^^^^^^UHN^^^^EI^G^4265^^^^^^^^^^^^||^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|||4265||^^^^^|3210^Generic^Physician-EM^^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r";
       
        
        Persister.persist(UhnConverter.convertAdtOrFail(message1));
        Persister.persist(UhnConverter.convertAdtOrFail(message2));

        
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
        
        PatientWithVisitsContainer pwvContainer = pwvContainers.get(0);
        Patient patient = pwvContainer.getDocument().myPatient;
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > patient.myRecordUpdatedDate.getTime());
        
        //check patient
        assertEquals("F", patient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), patient.myDateOfBirth);
        assertEquals(null, patient.myDeathDateAndTime);
        assertEquals("N", patient.myDeathIndicator);
        assertEquals(null, patient.myMothersMaidenName);
        
        
        Cx patId = patient.myPatientIds.get(0);
        assertEquals("7012673", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Cx patId2 = patient.myPatientIds.get(1);
        assertEquals("9287170261", patId2.myIdNumber);
        assertEquals("JHN", patId2.myIdTypeCode);
        
        Xpn patName = patient.myPatientNames.get(0);
        assertEquals("Test2", patName.myLastName);
        assertEquals("Majaconversion", patName.myFirstName);
                
        Xad patAddresses = patient.myPatientAddresses.get(0);
        assertEquals("11 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        Xtn patPhone = patient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3334", patPhone.myPhoneNumber);
        
        Ce lang = patient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
        

        //check patient roles
        List<PersonInRole> roles  = patient.myPersonInRoles;
        assertEquals(2, roles.size());
        PersonInRole rol_1 = patient.myPersonInRoles.get(0);
        PersonInRole rol_2 = patient.myPersonInRoles.get(1);        
        //ROL(1)
        assertEquals("PP",rol_1.myRole.myCode);
        assertEquals("Primary Care Provider",rol_1.myRole.myText);        
        assertEquals("Generic", rol_1.myPersonNames.get(0).myLastName);
        assertEquals("Physician", rol_1.myPersonNames.get(0).myFirstName);
        assertEquals("Moe", rol_1.myPersonNames.get(0).myMiddleName);
        assertEquals("(416) 340-3391", rol_1.myContactInformation.get(0).myPhoneNumber);
        //ROL(2)
        assertEquals("PP",rol_2.myRole.myCode);
        assertEquals("Primary Care Provider",rol_2.myRole.myText);        
        assertEquals("Genericb", rol_2.myPersonNames.get(0).myLastName);
        assertEquals("Physicianb", rol_2.myPersonNames.get(0).myFirstName);
        assertEquals("Moeb", rol_2.myPersonNames.get(0).myMiddleName);
        assertEquals("(416) 340-3388", rol_2.myContactInformation.get(0).myPhoneNumber);
        
        
        //check patient associated parties
        List<AssociatedParty> parties  = patient.myAssociatedParties;
        assertEquals(2, parties.size());
        AssociatedParty party_1 = patient.myAssociatedParties.get(0);
        AssociatedParty party_2 = patient.myAssociatedParties.get(1);        
        //NK1(1)
        assertEquals("Wph",party_1.myNames.get(0).myLastName);
        assertEquals("Mom",party_1.myNames.get(0).myFirstName);
        assertEquals("PAR",party_1.myRelationship.myCode);
        assertEquals("Parent",party_1.myRelationshipName);
        assertEquals("82 Buttonwood Avenue",party_1.myAddresses.get(0).myStreetAddress);
        assertEquals("(416)243-3600",party_1.myContactInformation.get(0).myPhoneNumber);
        //NK1(2)
        assertEquals("Wph",party_2.myNames.get(0).myLastName);
        assertEquals("Brother",party_2.myNames.get(0).myFirstName);
        assertEquals("BRO",party_2.myRelationship.myCode);
        assertEquals("Brother",party_2.myRelationshipName);
        assertEquals("83 Buttonwood Avenue",party_2.myAddresses.get(0).myStreetAddress);
        assertEquals("(416)243-3601",party_2.myContactInformation.get(0).myPhoneNumber);
        
        //Should be no visit info stored
        assertEquals(null, pwvContainer.getDocument().myVisits);
            
        
    }
    
    
    
    /**
     * Testing update patient/person info A31
     * - Add patient A31 and deactivate it 
     * 
     *   
     * @throws Exception ...
     */
    @Test
    public void testAdtA31b() throws Exception {
        
        
        
        
        String message1 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201201111142||ADT^A31^ADT_A05|124434|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A31|201201111142||||201201111142|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test2^Majaconversion^^^Miss^^L^^^^^201201111142^^~Test^Maj^^^Miss^^A^^^^^^^||19731230|F|||11 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3334^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||^^^^^^^^^^^~||||||||||||N|||201201111142||||||\r" + 
                "ZPD|Y\r" + 
                "PD1|||UHN^D^^^^UHN^FI^^^^^|3210^Generic^Physician-EM^^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "ROL||UC|PP^Primary Care Provider^15ZRole^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^^^^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|O^Office^^^^|^^^ON^^Can^^^^^^^^|(416) 340-3391^WPN^PH^^^^^^^^^\r" + 
                "ROL||UC|PP^Primary Care Provider^15ZRole^^^|13546b^Genericb^Physicianb^Moeb^^Dr.^MD^^UHN^L^^^EI^G^2.16.840.1.113883.3.59.3:947^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|1^Hospital^15ZOrgTyp^^^|^^^ON^^Can^^^^^^^^|(416) 340-3388^WPN^PH^^^^^^^^^\r" +
                "NK1|1|Wph^Mom^^^^^L^^^^^^^|PAR^Parent^03ZRelshp^^^|82 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^~(416)123-1234^PRN^CP^^^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^|N^Next-of-Kin^03ZConRol^^^||||||||||||||||||||||||||||||||\r" +
                "NK1|2|Wph^Brother^^^^^L^^^^^^^|BRO^Brother^03ZRelshp^^^|83 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3601^PRN^PH^^^^^^^^^|(416)525-2525^PRN^PH^^^^^^^^^|C^Emergency Contact^03ZConRol^^^||||||||||||||||||||||||||||||||\r" + 
                "PV1||P|2C Pre Operative Care Unit^^^G^4265^^^C^^^2C POCU^1427^|||^^^G^4265^^^^^^^  ^|13546^Generic^Physician^Moe^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|||M^Medical|||||||13546^Generic^Physician^Moe^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|SP^|11150000130^^^UHN^VN^G^4265^^^^^||||||||||||||||||||G|||||201210121059|||||||V|\r" + 
                "PV2|||^^^^^|||||201201101314||||||||||||||N|||||||||N||||||||||||||||||\r" + 
                "ZPV|^|^|||N||324|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|^^^^^^^^UHN^^^^EI^G^4265^^^^^^^^^^^^||^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|||4265||^^^^^|3210^Generic^Physician-EM^^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r";
       
        
        Persister.persist(UhnConverter.convertAdtOrFail(message1));
        
        
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
        
        PatientWithVisitsContainer pwvContainer = pwvContainers.get(0);
        Patient patient = pwvContainer.getDocument().myPatient;
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > patient.myRecordUpdatedDate.getTime());
        
        //check patient
        assertEquals("F", patient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), patient.myDateOfBirth);
        assertEquals(null, patient.myDeathDateAndTime);
        assertEquals("N", patient.myDeathIndicator);
        assertEquals(null, patient.myMothersMaidenName);
        
        
        Cx patId = patient.myPatientIds.get(0);
        assertEquals("7012673", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Cx patId2 = patient.myPatientIds.get(1);
        assertEquals("9287170261", patId2.myIdNumber);
        assertEquals("JHN", patId2.myIdTypeCode);
        
        Xpn patName = patient.myPatientNames.get(0);
        assertEquals("Test2", patName.myLastName);
        assertEquals("Majaconversion", patName.myFirstName);
                
        Xad patAddresses = patient.myPatientAddresses.get(0);
        assertEquals("11 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        Xtn patPhone = patient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3334", patPhone.myPhoneNumber);
        
        Ce lang = patient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
        

        //check patient roles
        List<PersonInRole> roles  = patient.myPersonInRoles;
        assertEquals(2, roles.size());
        PersonInRole rol_1 = patient.myPersonInRoles.get(0);
        PersonInRole rol_2 = patient.myPersonInRoles.get(1);        
        //ROL(1)
        assertEquals("PP",rol_1.myRole.myCode);
        assertEquals("Primary Care Provider",rol_1.myRole.myText);        
        assertEquals("Generic", rol_1.myPersonNames.get(0).myLastName);
        assertEquals("Physician", rol_1.myPersonNames.get(0).myFirstName);
        assertEquals("Moe", rol_1.myPersonNames.get(0).myMiddleName);
        assertEquals("(416) 340-3391", rol_1.myContactInformation.get(0).myPhoneNumber);
        //ROL(2)
        assertEquals("PP",rol_2.myRole.myCode);
        assertEquals("Primary Care Provider",rol_2.myRole.myText);        
        assertEquals("Genericb", rol_2.myPersonNames.get(0).myLastName);
        assertEquals("Physicianb", rol_2.myPersonNames.get(0).myFirstName);
        assertEquals("Moeb", rol_2.myPersonNames.get(0).myMiddleName);
        assertEquals("(416) 340-3388", rol_2.myContactInformation.get(0).myPhoneNumber);
        
        
        //check patient associated parties
        List<AssociatedParty> parties  = patient.myAssociatedParties;
        assertEquals(2, parties.size());
        AssociatedParty party_1 = patient.myAssociatedParties.get(0);
        AssociatedParty party_2 = patient.myAssociatedParties.get(1);        
        //NK1(1)
        assertEquals("Wph",party_1.myNames.get(0).myLastName);
        assertEquals("Mom",party_1.myNames.get(0).myFirstName);
        assertEquals("PAR",party_1.myRelationship.myCode);
        assertEquals("Parent",party_1.myRelationshipName);
        assertEquals("82 Buttonwood Avenue",party_1.myAddresses.get(0).myStreetAddress);
        assertEquals("(416)243-3600",party_1.myContactInformation.get(0).myPhoneNumber);
        //NK1(2)
        assertEquals("Wph",party_2.myNames.get(0).myLastName);
        assertEquals("Brother",party_2.myNames.get(0).myFirstName);
        assertEquals("BRO",party_2.myRelationship.myCode);
        assertEquals("Brother",party_2.myRelationshipName);
        assertEquals("83 Buttonwood Avenue",party_2.myAddresses.get(0).myStreetAddress);
        assertEquals("(416)243-3601",party_2.myContactInformation.get(0).myPhoneNumber);
        
        //Check deactivation indicator
        assertEquals("Y", patient.myDeactivatePatientIndicator);        
        
        //Should be no visit info stored
        assertEquals(null, pwvContainer.getDocument().myVisits);
            
        
    }        
    
    
    
    
    
    /**
     * Test patient merge A40
     * - both prior and current patient exist 
     *   
     * @throws Exception ...
     */
    @Test
    public void testAdtA40a() throws Exception {
        

        String priorPatientMsg = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A01^ADT_A01|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A01|201112021621||||201112021621|G^4265^L\r" + 
                "PID|||7012672^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test2^Majaconversion2^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000513^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000513^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r";
        
        
        String currentPatientMsg = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A01^ADT_A01|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A01|201112021621||||201112021621|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170262^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000514^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r"; 
        
        
        String mrgMsg = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201202291235||ADT^A40^ADT_A39|126421|T^|2.5^^||||||CAN||||\r" + 
        		"EVN|A40|201202291235||||201202291235|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170262^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" +        		 
        		"PD1|||UHN^D^^^^UHN^FI^^^^^|13893^Generic^Moe^PhysicianThree^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
        		"MRG|7012672^^^UHN^MR^^^^^^^~~HN2827^^^UHN^PI^^^^^^^||||^^^^^^^^^^^||\r";
        
       
        
        
        Persister.persist(UhnConverter.convertAdtOrFail(priorPatientMsg));
        Persister.persist(UhnConverter.convertAdtOrFail(currentPatientMsg));
        Persister.persist(UhnConverter.convertAdtOrFail(mrgMsg));

        
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
        
        assertEquals(1, pwvContainers.size());
                
        PatientWithVisitsContainer pwvContainer = pwvContainers.get(0);
        
        Patient curPatient = pwvContainer.getDocument().myPatient;
        Visit curVisit = pwvContainer.getDocument().myVisits.get(0);
        
        Patient priorPatient = pwvContainer.getDocument().myMergedInPatientsWithVisits.get(0).myPatient;
        Visit priorVisit = pwvContainer.getDocument().myMergedInPatientsWithVisits.get(0).myVisits.get(0);
        
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > curPatient.myRecordUpdatedDate.getTime());
        
        
        //check current patient
        assertEquals("F", curPatient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), curPatient.myDateOfBirth);
        assertEquals(null, curPatient.myDeathDateAndTime);
        assertEquals("N", curPatient.myDeathIndicator);
        assertEquals(null, curPatient.myMothersMaidenName);
        
        
        Cx patId = curPatient.myPatientIds.get(0);
        assertEquals("7012673", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Xpn patName = curPatient.myPatientNames.get(0);
        assertEquals("Test", patName.myLastName);
        assertEquals("Majaconversion", patName.myFirstName);
                
        Xad patAddresses = curPatient.myPatientAddresses.get(0);
        assertEquals("1 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        Xtn patPhone = curPatient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        Ce lang = curPatient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
        
        
        //check visit lastUpdateTime
        assertTrue(nowMilli > curVisit.myRecordUpdatedDate.getTime()); 
            
        
        //check current visit
        assertEquals("11110000514", curVisit.myVisitNumber.myIdNumber);
        assertEquals(Constants.ACTIVE_VISIT_STATUS, curVisit.myVisitStatus);
        assertEquals("I", curVisit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112021621"), curVisit.myAdmitDate);
        assertEquals("hospServ", curVisit.myHospitalService);
        Pl loc = curVisit.myAssignedPatientLocation;
        assertEquals("PMH 15C", loc.myPointOfCare);
        assertEquals("413", loc.myRoom);
        assertEquals("2", loc.myBed);
        
        Xcn admitDoc = curVisit.myAdmittingDoctors.get(0);
        assertEquals("13546c", admitDoc.myId);
        assertEquals("Physician", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("MoeC", admitDoc.myMiddleName);
        
        Xcn attendDoc = curVisit.myAttendingDoctors.get(0);
        assertEquals("13546a", attendDoc.myId);
        assertEquals("Physician", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("MoeA", attendDoc.myMiddleName);
        
        Xcn refDoc = curVisit.myReferringDoctors.get(0);
        assertEquals("13546b", refDoc.myId);
        assertEquals("Physician", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("MoeB", refDoc.myMiddleName);
        
        //check patient lastUpdateTime
        assertTrue(nowMilli > priorPatient.myRecordUpdatedDate.getTime());

        
        //check prior patient
        assertEquals("F", priorPatient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), priorPatient.myDateOfBirth);
        assertEquals(null, priorPatient.myDeathDateAndTime);
        assertEquals("N", priorPatient.myDeathIndicator);
        assertEquals(null, priorPatient.myMothersMaidenName);
        
        
        patId = priorPatient.myPatientIds.get(0);
        assertEquals("7012672", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        patName = priorPatient.myPatientNames.get(0);
        assertEquals("Test2", patName.myLastName);
        assertEquals("Majaconversion2", patName.myFirstName);
                
        patAddresses = priorPatient.myPatientAddresses.get(0);
        assertEquals("1 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        patPhone = priorPatient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        lang = priorPatient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
        
        
        //check visit lastUpdateTime
        assertTrue(nowMilli > priorVisit.myRecordUpdatedDate.getTime());
        
            
        
        //check prior visit
        assertEquals("11110000513", priorVisit.myVisitNumber.myIdNumber);        
        assertEquals(Constants.ACTIVE_VISIT_STATUS, priorVisit.myVisitStatus);
        assertEquals("I", priorVisit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112021621"), priorVisit.myAdmitDate);
        assertEquals("hospServ", priorVisit.myHospitalService);
        loc = priorVisit.myAssignedPatientLocation;
        assertEquals("PMH 15C", loc.myPointOfCare);
        assertEquals("413", loc.myRoom);
        assertEquals("2", loc.myBed);
        
        admitDoc = priorVisit.myAdmittingDoctors.get(0);
        assertEquals("13546c", admitDoc.myId);
        assertEquals("Physician", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("MoeC", admitDoc.myMiddleName);
        
        attendDoc = priorVisit.myAttendingDoctors.get(0);
        assertEquals("13546a", attendDoc.myId);
        assertEquals("Physician", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("MoeA", attendDoc.myMiddleName);
        
        refDoc = priorVisit.myReferringDoctors.get(0);
        assertEquals("13546b", refDoc.myId);
        assertEquals("Physician", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("MoeB", refDoc.myMiddleName);
        
        
        
    }    
    
    
    
    
    /**
     * Test patient merge A40
     * - only current patient exists but not the prior 
     *   
     * @throws Exception ...
     */
    @Test
    public void testAdtA40b() throws Exception {
        
        
        String currentPatientMsg = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A01^ADT_A01|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A01|201112021621||||201112021621|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000514^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r"; 
        
        
        String mrgMsg = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201202291235||ADT^A40^ADT_A39|126421|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A40|201202291235||||201202291235|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" +               
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13893^Generic^Moe^PhysicianThree^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "MRG|7012672^^^UHN^MR^^^^^^^||||^^^^^^^^^^^||\r";
        
       

        Persister.persist(UhnConverter.convertAdtOrFail(currentPatientMsg));
        Persister.persist(UhnConverter.convertAdtOrFail(mrgMsg));

        
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
        
        assertEquals(1, pwvContainers.size());
                
        PatientWithVisitsContainer pwvContainer = pwvContainers.get(0);
        
        Patient curPatient = pwvContainer.getDocument().myPatient;
        Visit curVisit = pwvContainer.getDocument().myVisits.get(0);        
        Patient priorPatient = pwvContainer.getDocument().myMergedInPatientsWithVisits.get(0).myPatient;
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > curPatient.myRecordUpdatedDate.getTime());
        
        
        //check current patient
        assertEquals("F", curPatient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), curPatient.myDateOfBirth);
        assertEquals(null, curPatient.myDeathDateAndTime);
        assertEquals("N", curPatient.myDeathIndicator);
        assertEquals(null, curPatient.myMothersMaidenName);
        
        
        Cx patId = curPatient.myPatientIds.get(0);
        assertEquals("7012673", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Xpn patName = curPatient.myPatientNames.get(0);
        assertEquals("Test", patName.myLastName);
        assertEquals("Majaconversion", patName.myFirstName);
                
        Xad patAddresses = curPatient.myPatientAddresses.get(0);
        assertEquals("1 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        Xtn patPhone = curPatient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        Ce lang = curPatient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
        
        //check visit lastUpdateTime
        assertTrue(nowMilli > curVisit.myRecordUpdatedDate.getTime());
        
        //check current visit
        assertEquals("11110000514", curVisit.myVisitNumber.myIdNumber);
        assertEquals(Constants.ACTIVE_VISIT_STATUS, curVisit.myVisitStatus);
        assertEquals("I", curVisit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112021621"), curVisit.myAdmitDate);
        assertEquals("hospServ", curVisit.myHospitalService);
        Pl loc = curVisit.myAssignedPatientLocation;
        assertEquals("PMH 15C", loc.myPointOfCare);
        assertEquals("413", loc.myRoom);
        assertEquals("2", loc.myBed);
        
        Xcn admitDoc = curVisit.myAdmittingDoctors.get(0);
        assertEquals("13546c", admitDoc.myId);
        assertEquals("Physician", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("MoeC", admitDoc.myMiddleName);
        
        Xcn attendDoc = curVisit.myAttendingDoctors.get(0);
        assertEquals("13546a", attendDoc.myId);
        assertEquals("Physician", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("MoeA", attendDoc.myMiddleName);
        
        Xcn refDoc = curVisit.myReferringDoctors.get(0);
        assertEquals("13546b", refDoc.myId);
        assertEquals("Physician", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("MoeB", refDoc.myMiddleName);
        
        //check patient lastUpdateTime
        assertTrue(nowMilli > priorPatient.myRecordUpdatedDate.getTime());
              
        //check prior patient
        patId = priorPatient.myPatientIds.get(0);
        assertEquals("7012672", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);        
        assertEquals(null, priorPatient.myAdministrativeSex);
        assertEquals(null, priorPatient.myDateOfBirth);
        assertEquals(null, priorPatient.myDeathDateAndTime);
        assertEquals(null, priorPatient.myDeathIndicator);
        assertEquals(null, priorPatient.myMothersMaidenName);
        assertEquals(null, priorPatient.myPatientNames);
        assertEquals(null, priorPatient.myPatientAddresses);
        assertEquals(null, priorPatient.myPhoneNumbers);
           
        
        //check prior visit (it should not exist)
        assertEquals(null, pwvContainer.getDocument().myMergedInPatientsWithVisits.get(0).myVisits);     
    }    
        
    
    
    
    /**
     * Test patient merge A40
     * - prior patient exists not the current 
     *   
     * @throws Exception ...
     */
    @Test
    public void testAdtA40c() throws Exception {
        

        String priorPatientMsg = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A01^ADT_A01|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A01|201112021621||||201112021621|G^4265^L\r" + 
                "PID|||7012672^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test2^Majaconversion2^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000513^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000513^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r";
        
        
        String mrgMsg = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201202291235||ADT^A40^ADT_A39|126421|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A40|201202291235||||201202291235|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" +               
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13893^Generic^Moe^PhysicianThree^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "MRG|7012672^^^UHN^MR^^^^^^^||||^^^^^^^^^^^||\r";
        
       
        
        
        Persister.persist(UhnConverter.convertAdtOrFail(priorPatientMsg));        
        Persister.persist(UhnConverter.convertAdtOrFail(mrgMsg));

        
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
        
        assertEquals(1, pwvContainers.size());
                
        PatientWithVisitsContainer pwvContainer = pwvContainers.get(0);
        
        Patient curPatient = pwvContainer.getDocument().myPatient;
        Patient priorPatient = pwvContainer.getDocument().myMergedInPatientsWithVisits.get(0).myPatient;
        Visit priorVisit = pwvContainer.getDocument().myMergedInPatientsWithVisits.get(0).myVisits.get(0);
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > curPatient.myRecordUpdatedDate.getTime());
        
        //check current patient
        assertEquals("F", curPatient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), curPatient.myDateOfBirth);
        assertEquals(null, curPatient.myDeathDateAndTime);
        assertEquals("N", curPatient.myDeathIndicator);
        assertEquals(null, curPatient.myMothersMaidenName);
        
        
        Cx patId = curPatient.myPatientIds.get(0);
        assertEquals("7012673", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Xpn patName = curPatient.myPatientNames.get(0);
        assertEquals("Test", patName.myLastName);
        assertEquals("Majaconversion", patName.myFirstName);
                
        Xad patAddresses = curPatient.myPatientAddresses.get(0);
        assertEquals("1 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        Xtn patPhone = curPatient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        Ce lang = curPatient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
        
        
        //check current visit (it should not exist)
        assertEquals(null, pwvContainer.getDocument().myVisits);    
        
        
        //check patient lastUpdateTime
        assertTrue(nowMilli > priorPatient.myRecordUpdatedDate.getTime());
        
        
        //check prior patient
        assertEquals("F", priorPatient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), priorPatient.myDateOfBirth);
        assertEquals(null, priorPatient.myDeathDateAndTime);
        assertEquals("N", priorPatient.myDeathIndicator);
        assertEquals(null, priorPatient.myMothersMaidenName);
        
        
        patId = priorPatient.myPatientIds.get(0);
        assertEquals("7012672", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        patName = priorPatient.myPatientNames.get(0);
        assertEquals("Test2", patName.myLastName);
        assertEquals("Majaconversion2", patName.myFirstName);
                
        patAddresses = priorPatient.myPatientAddresses.get(0);
        assertEquals("1 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        patPhone = priorPatient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        lang = priorPatient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
        
        //check visit lastUpdateTime
        assertTrue(nowMilli > priorVisit.myRecordUpdatedDate.getTime());
        
        //check prior visit
        assertEquals("11110000513", priorVisit.myVisitNumber.myIdNumber);                 
        assertEquals(Constants.ACTIVE_VISIT_STATUS, priorVisit.myVisitStatus);
        assertEquals("I", priorVisit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112021621"), priorVisit.myAdmitDate);
        assertEquals("hospServ", priorVisit.myHospitalService);
        Pl loc = priorVisit.myAssignedPatientLocation;
        assertEquals("PMH 15C", loc.myPointOfCare);
        assertEquals("413", loc.myRoom);
        assertEquals("2", loc.myBed);
        
        Xcn admitDoc = priorVisit.myAdmittingDoctors.get(0);
        assertEquals("13546c", admitDoc.myId);
        assertEquals("Physician", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("MoeC", admitDoc.myMiddleName);
        
        Xcn attendDoc = priorVisit.myAttendingDoctors.get(0);
        assertEquals("13546a", attendDoc.myId);
        assertEquals("Physician", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("MoeA", attendDoc.myMiddleName);
        
        Xcn refDoc = priorVisit.myReferringDoctors.get(0);
        assertEquals("13546b", refDoc.myId);
        assertEquals("Physician", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("MoeB", refDoc.myMiddleName);
        
        
        
    }        
    
    
    
    /**
     * Test patient merge A40
     * - neither current or prior patient exists 
     *   
     * @throws Exception ...
     */
    @Test
    public void testAdtA40d() throws Exception {
        
        
       
        String mrgMsg = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201202291235||ADT^A40^ADT_A39|126421|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A40|201202291235||||201202291235|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" +               
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13893^Generic^Moe^PhysicianThree^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "MRG|7012672^^^UHN^MR^^^^^^^||||^^^^^^^^^^^||\r";
        
       


        Persister.persist(UhnConverter.convertAdtOrFail(mrgMsg));

        
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
        
        assertEquals(1, pwvContainers.size());
                
        PatientWithVisitsContainer pwvContainer = pwvContainers.get(0);
        
        Patient curPatient = pwvContainer.getDocument().myPatient;
        Patient priorPatient = pwvContainer.getDocument().myMergedInPatientsWithVisits.get(0).myPatient;
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > curPatient.myRecordUpdatedDate.getTime());
        
        
        //check current patient
        assertEquals("F", curPatient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), curPatient.myDateOfBirth);
        
        
        Cx patId = curPatient.myPatientIds.get(0);
        assertEquals("7012673", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Xpn patName = curPatient.myPatientNames.get(0);
        assertEquals("Test", patName.myLastName);
        assertEquals("Majaconversion", patName.myFirstName);
                
        Xad patAddresses = curPatient.myPatientAddresses.get(0);
        assertEquals("1 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        Xtn patPhone = curPatient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        Ce lang = curPatient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
            
        
        //check current visit (it should not exist)                
        assertEquals(null, pwvContainer.getDocument().myVisits);
        
        
        //check patient lastUpdateTime
        assertTrue(nowMilli > priorPatient.myRecordUpdatedDate.getTime());
        
        //check prior patient        
        patId = priorPatient.myPatientIds.get(0);
        assertEquals("7012672", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        assertEquals(null, priorPatient.myAdministrativeSex);
        assertEquals(null, priorPatient.myDateOfBirth);
        assertEquals(null, priorPatient.myDeathDateAndTime);
        assertEquals(null, priorPatient.myDeathIndicator);
        assertEquals(null, priorPatient.myMothersMaidenName);
        assertEquals(null, priorPatient.myPatientNames);
        assertEquals(null, priorPatient.myPatientAddresses);
        assertEquals(null, priorPatient.myPhoneNumbers);
            
        
        //check prior visit (it should not exist)
        assertEquals(null, pwvContainer.getDocument().myMergedInPatientsWithVisits.get(0).myVisits);     
    }     
    
    
    
    /**
     * Test visit merge A42
     * - both toKeep and toRemove visits exist for the patient 
     *   
     * @throws Exception ...
     */
    @Test
    public void testAdtA42a() throws Exception {
        

        String addToKeepVisit = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A01^ADT_A01|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A01|201112021621||||201112021621|G^4265^L\r" + 
                "PID|||7012672^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000513^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000513^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r";
        
        
        String addToRemoveVisit= "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A01^ADT_A01|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A01|201112021621||||201112021621|G^4265^L\r" + 
                "PID|||7012672^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PV1||I|PMH 15C^413^3^G^4265^^^N^P15C 413^P15C 413 3^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000514^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021721|||||||V|\r";
 
        
        
        String mrgMsg = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201202291235||ADT^A42^ADT_A39|126421|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A42|201202291235||||201202291235|G^4265^L\r" +
                "PID|||7012672^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000513^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" +             
                "MRG|7012672^^^UHN^MR^^^^^^^~~HN2827^^^UHN^PI^^^^^^^||||11110000514^^^UHN^VN^G^4265^^^^^||\r" +
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000513^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r"; 
        
        
       
        
        
        Persister.persist(UhnConverter.convertAdtOrFail(addToKeepVisit));
        Persister.persist(UhnConverter.convertAdtOrFail(addToRemoveVisit));
        Persister.persist(UhnConverter.convertAdtOrFail(mrgMsg));

        
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
        
        assertEquals(1, pwvContainers.size());
                
        PatientWithVisitsContainer pwvContainer = pwvContainers.get(0);
        
        Patient patient = pwvContainer.getDocument().myPatient;

        
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > patient.myRecordUpdatedDate.getTime());
        
        
        //check patient
        assertEquals("F", patient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), patient.myDateOfBirth);
        assertEquals(null, patient.myDeathDateAndTime);
        assertEquals("N", patient.myDeathIndicator);
        assertEquals(null, patient.myMothersMaidenName);
        
        
        Cx patId = patient.myPatientIds.get(0);
        assertEquals("7012672", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Xpn patName = patient.myPatientNames.get(0);
        assertEquals("Test", patName.myLastName);
        assertEquals("Majaconversion", patName.myFirstName);
                
        Xad patAddresses = patient.myPatientAddresses.get(0);
        assertEquals("1 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        Xtn patPhone = patient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        Ce lang = patient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
        
        //there should only be one visit
        assertEquals(1, pwvContainer.getDocument().myVisits.size());
        
        Visit visit = pwvContainer.getDocument().myVisits.get(0);        
        
        //check visit lastUpdateTime
        assertTrue(nowMilli > visit.myRecordUpdatedDate.getTime()); 
        
        //check visit
        assertEquals("11110000513", visit.myVisitNumber.myIdNumber);
        assertEquals(Constants.ACTIVE_VISIT_STATUS, visit.myVisitStatus);
        assertEquals("I", visit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112021621"), visit.myAdmitDate);
        assertEquals("hospServ", visit.myHospitalService);
        Pl loc = visit.myAssignedPatientLocation;
        assertEquals("PMH 15C", loc.myPointOfCare);
        assertEquals("413", loc.myRoom);
        assertEquals("2", loc.myBed);
        
        Xcn admitDoc = visit.myAdmittingDoctors.get(0);
        assertEquals("13546c", admitDoc.myId);
        assertEquals("Physician", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("MoeC", admitDoc.myMiddleName);
        
        Xcn attendDoc = visit.myAttendingDoctors.get(0);
        assertEquals("13546a", attendDoc.myId);
        assertEquals("Physician", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("MoeA", attendDoc.myMiddleName);
        
        Xcn refDoc = visit.myReferringDoctors.get(0);
        assertEquals("13546b", refDoc.myId);
        assertEquals("Physician", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("MoeB", refDoc.myMiddleName);
        
        
        
    }      
    
    
    
    /**
     * Test visit merge A42
     * - only the toKeepVisit exist for the patient 
     *   
     * @throws Exception ...
     */
    @Test
    public void testAdtA42b() throws Exception {
        

        String addToKeepVisit = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A01^ADT_A01|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A01|201112021621||||201112021621|G^4265^L\r" + 
                "PID|||7012672^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000513^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000513^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r";
        
       
        
        String mrgMsg = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201202291235||ADT^A42^ADT_A39|126421|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A42|201202291235||||201202291235|G^4265^L\r" +
                "PID|||7012672^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000513^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" +             
                "MRG|7012672^^^UHN^MR^^^^^^^~~HN2827^^^UHN^PI^^^^^^^||||11110000514^^^UHN^VN^G^4265^^^^^||\r" +
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000513^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r"; 
        
        
       
        
        
        Persister.persist(UhnConverter.convertAdtOrFail(addToKeepVisit));
        Persister.persist(UhnConverter.convertAdtOrFail(mrgMsg));

        
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
        
        assertEquals(1, pwvContainers.size());
                
        PatientWithVisitsContainer pwvContainer = pwvContainers.get(0);
        
        Patient patient = pwvContainer.getDocument().myPatient;

        
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > patient.myRecordUpdatedDate.getTime());
        
        
        //check patient
        assertEquals("F", patient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), patient.myDateOfBirth);
        assertEquals(null, patient.myDeathDateAndTime);
        assertEquals("N", patient.myDeathIndicator);
        assertEquals(null, patient.myMothersMaidenName);
        
        
        Cx patId = patient.myPatientIds.get(0);
        assertEquals("7012672", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Xpn patName = patient.myPatientNames.get(0);
        assertEquals("Test", patName.myLastName);
        assertEquals("Majaconversion", patName.myFirstName);
                
        Xad patAddresses = patient.myPatientAddresses.get(0);
        assertEquals("1 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        Xtn patPhone = patient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        Ce lang = patient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
        
        //there should only be one visit
        assertEquals(1, pwvContainer.getDocument().myVisits.size());
        
        Visit visit = pwvContainer.getDocument().myVisits.get(0);        
        
        //check visit lastUpdateTime
        assertTrue(nowMilli > visit.myRecordUpdatedDate.getTime()); 
        
        //check visit
        assertEquals("11110000513", visit.myVisitNumber.myIdNumber);
        assertEquals(Constants.ACTIVE_VISIT_STATUS, visit.myVisitStatus);
        assertEquals("I", visit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112021621"), visit.myAdmitDate);
        assertEquals("hospServ", visit.myHospitalService);
        Pl loc = visit.myAssignedPatientLocation;
        assertEquals("PMH 15C", loc.myPointOfCare);
        assertEquals("413", loc.myRoom);
        assertEquals("2", loc.myBed);
        
        Xcn admitDoc = visit.myAdmittingDoctors.get(0);
        assertEquals("13546c", admitDoc.myId);
        assertEquals("Physician", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("MoeC", admitDoc.myMiddleName);
        
        Xcn attendDoc = visit.myAttendingDoctors.get(0);
        assertEquals("13546a", attendDoc.myId);
        assertEquals("Physician", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("MoeA", attendDoc.myMiddleName);
        
        Xcn refDoc = visit.myReferringDoctors.get(0);
        assertEquals("13546b", refDoc.myId);
        assertEquals("Physician", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("MoeB", refDoc.myMiddleName);
        
    }   
    
    
    
    /**
     * Test visit merge A42
     * - only the toRemove visit exist for the patient 
     *   
     * @throws Exception ...
     */
    @Test
    public void testAdtA42c() throws Exception {
        

        
        
        String addToRemoveVisit= "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A01^ADT_A01|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A01|201112021621||||201112021621|G^4265^L\r" + 
                "PID|||7012672^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PV1||I|PMH 15C^413^3^G^4265^^^N^P15C 413^P15C 413 3^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000514^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021721|||||||V|\r";
 
        
        
        String mrgMsg = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201202291235||ADT^A42^ADT_A39|126421|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A42|201202291235||||201202291235|G^4265^L\r" +
                "PID|||7012672^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000513^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" +             
                "MRG|7012672^^^UHN^MR^^^^^^^~~HN2827^^^UHN^PI^^^^^^^||||11110000514^^^UHN^VN^G^4265^^^^^||\r" +
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000513^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r"; 
        
        
       
        
        

        Persister.persist(UhnConverter.convertAdtOrFail(addToRemoveVisit));
        Persister.persist(UhnConverter.convertAdtOrFail(mrgMsg));

        
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
        
        assertEquals(1, pwvContainers.size());
                
        PatientWithVisitsContainer pwvContainer = pwvContainers.get(0);
        
        Patient patient = pwvContainer.getDocument().myPatient;

        
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > patient.myRecordUpdatedDate.getTime());
        
        
        //check patient
        assertEquals("F", patient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), patient.myDateOfBirth);
        assertEquals(null, patient.myDeathDateAndTime);
        assertEquals("N", patient.myDeathIndicator);
        assertEquals(null, patient.myMothersMaidenName);
        
        
        Cx patId = patient.myPatientIds.get(0);
        assertEquals("7012672", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Xpn patName = patient.myPatientNames.get(0);
        assertEquals("Test", patName.myLastName);
        assertEquals("Majaconversion", patName.myFirstName);
                
        Xad patAddresses = patient.myPatientAddresses.get(0);
        assertEquals("1 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        Xtn patPhone = patient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        Ce lang = patient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
        
        //there should only be one visit
        assertEquals(1, pwvContainer.getDocument().myVisits.size());
        
        Visit visit = pwvContainer.getDocument().myVisits.get(0);        
        
        //check visit lastUpdateTime
        assertTrue(nowMilli > visit.myRecordUpdatedDate.getTime()); 
        
        //check visit
        assertEquals("11110000513", visit.myVisitNumber.myIdNumber);
        assertEquals(Constants.UNDEF_VISIT_STATUS, visit.myVisitStatus);
        assertEquals("I", visit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112021621"), visit.myAdmitDate);
        assertEquals("hospServ", visit.myHospitalService);
        Pl loc = visit.myAssignedPatientLocation;
        assertEquals("PMH 15C", loc.myPointOfCare);
        assertEquals("413", loc.myRoom);
        assertEquals("2", loc.myBed);
        
        Xcn admitDoc = visit.myAdmittingDoctors.get(0);
        assertEquals("13546c", admitDoc.myId);
        assertEquals("Physician", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("MoeC", admitDoc.myMiddleName);
        
        Xcn attendDoc = visit.myAttendingDoctors.get(0);
        assertEquals("13546a", attendDoc.myId);
        assertEquals("Physician", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("MoeA", attendDoc.myMiddleName);
        
        Xcn refDoc = visit.myReferringDoctors.get(0);
        assertEquals("13546b", refDoc.myId);
        assertEquals("Physician", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("MoeB", refDoc.myMiddleName);
        
    }        
    
    
    
    /**
     * Test visit merge A42
     * - both toKeep and toRemove visits do not exist for the patient 
     *   
     * @throws Exception ...
     */
    @Test
    public void testAdtA42d() throws Exception {
        
       
        
        String mrgMsg = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201202291235||ADT^A42^ADT_A39|126421|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A42|201202291235||||201202291235|G^4265^L\r" +
                "PID|||7012672^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000513^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" +             
                "MRG|7012672^^^UHN^MR^^^^^^^~~HN2827^^^UHN^PI^^^^^^^||||11110000514^^^UHN^VN^G^4265^^^^^||\r" +
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000513^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r"; 
        
        
       
        Persister.persist(UhnConverter.convertAdtOrFail(mrgMsg));

        
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
        
        assertEquals(1, pwvContainers.size());
                
        PatientWithVisitsContainer pwvContainer = pwvContainers.get(0);
        
        Patient patient = pwvContainer.getDocument().myPatient;

        
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > patient.myRecordUpdatedDate.getTime());
        
        
        //check patient
        assertEquals("F", patient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), patient.myDateOfBirth);
        assertEquals(null, patient.myDeathDateAndTime);
        assertEquals("N", patient.myDeathIndicator);
        assertEquals(null, patient.myMothersMaidenName);
        
        
        Cx patId = patient.myPatientIds.get(0);
        assertEquals("7012672", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Xpn patName = patient.myPatientNames.get(0);
        assertEquals("Test", patName.myLastName);
        assertEquals("Majaconversion", patName.myFirstName);
                
        Xad patAddresses = patient.myPatientAddresses.get(0);
        assertEquals("1 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        Xtn patPhone = patient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        Ce lang = patient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
        
        //there should only be one visit
        assertEquals(1, pwvContainer.getDocument().myVisits.size());
        
        Visit visit = pwvContainer.getDocument().myVisits.get(0);        
        
        //check visit lastUpdateTime
        assertTrue(nowMilli > visit.myRecordUpdatedDate.getTime()); 
        
        //check visit
        assertEquals("11110000513", visit.myVisitNumber.myIdNumber);
        assertEquals(Constants.UNDEF_VISIT_STATUS, visit.myVisitStatus);
        assertEquals("I", visit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112021621"), visit.myAdmitDate);
        assertEquals("hospServ", visit.myHospitalService);
        Pl loc = visit.myAssignedPatientLocation;
        assertEquals("PMH 15C", loc.myPointOfCare);
        assertEquals("413", loc.myRoom);
        assertEquals("2", loc.myBed);
        
        Xcn admitDoc = visit.myAdmittingDoctors.get(0);
        assertEquals("13546c", admitDoc.myId);
        assertEquals("Physician", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("MoeC", admitDoc.myMiddleName);
        
        Xcn attendDoc = visit.myAttendingDoctors.get(0);
        assertEquals("13546a", attendDoc.myId);
        assertEquals("Physician", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("MoeA", attendDoc.myMiddleName);
        
        Xcn refDoc = visit.myReferringDoctors.get(0);
        assertEquals("13546b", refDoc.myId);
        assertEquals("Physician", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("MoeB", refDoc.myMiddleName);
        
    }          
    
    
    
    
    
    /**
     * Test patient unlink A37
     * - add prior and current patients
     * - merge prior into current
     * - unlink prior from current 
     *   
     * @throws Exception ...
     */
    @SuppressWarnings("null")
    @Test
    public void testAdtA37() throws Exception {
        

        String priorPatientMsg = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A01^ADT_A01|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A01|201112021621||||201112021621|G^4265^L\r" + 
                "PID|||7012672^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test2^Majaconversion2^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000513^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000513^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r";
        
        
        String currentPatientMsg = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A01^ADT_A01|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A01|201112021621||||201112021621|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000514^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r"; 
        
        
        String mrgMsg = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201202291235||ADT^A40^ADT_A39|126421|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A40|201202291235||||201202291235|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" +               
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13893^Generic^Moe^PhysicianThree^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "MRG|7012672^^^UHN^MR^^^^^^^~HN3151^^^^PI^^^^^^^||||^^^^^^^^^^^||\r";
        
        
        String unMrgMsg = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201201111352||ADT^A37^ADT_A37|124466|T^|2.5^^||||||CAN||||\r" + 
        		"EVN|A37|201201111352||||201201111352|G^4265^L\r" +
        		"PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" +
        		"PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
        		"PV1||||||^^^^^^^^^^^  ^||||||||||||^|^^^^^^^^^^^|||||||||||||||||||||||||||||||||\r" +
        		"PID|||7012672^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test2^Majaconversion2^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000513^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" +
        		"PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
        		"PV1||||||^^^^^^^^^^^  ^||||||||||||^|^^^^^^^^^^^|||||||||||||||||||||||||||||||||";
        
       
        
        
        Persister.persist(UhnConverter.convertAdtOrFail(priorPatientMsg));
        Persister.persist(UhnConverter.convertAdtOrFail(currentPatientMsg));
        Persister.persist(UhnConverter.convertAdtOrFail(mrgMsg));
        Persister.persist(UhnConverter.convertAdtOrFail(unMrgMsg));
        
        
               
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
        
        //There should be 2 pwv docs now
        assertEquals(2, pwvContainers.size());
        
        Patient curPatient = null;
        Visit curVisit = null;        
        Patient priorPatient = null;
        Visit priorVisit = null;
                        
        for (PatientWithVisitsContainer pwvContainer : pwvContainers) {
            if ( "7012673".equals(pwvContainer.getDocument().getMrn().myIdNumber) ) {
                curPatient = pwvContainer.getDocument().myPatient;  
                curVisit = pwvContainer.getDocument().myVisits.get(0);
            }
            
            if ( "7012672".equals(pwvContainer.getDocument().getMrn().myIdNumber) ) {
                priorPatient = pwvContainer.getDocument().myPatient;  
                priorVisit = pwvContainer.getDocument().myVisits.get(0);
            }            
            
        }
        
        //make sure current and prior patients and visits exist
        assertTrue(curPatient != null);
        assertTrue(curVisit != null);
        assertTrue(priorPatient != null);
        assertTrue(priorVisit != null);
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > curPatient.myRecordUpdatedDate.getTime());
        
        
        //check current patient
        assertEquals("F", curPatient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), curPatient.myDateOfBirth);
        assertEquals(null, curPatient.myDeathDateAndTime);
        assertEquals("N", curPatient.myDeathIndicator);
        assertEquals(null, curPatient.myMothersMaidenName);
        
        
        Cx patId = curPatient.myPatientIds.get(0);
        assertEquals("7012673", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Xpn patName = curPatient.myPatientNames.get(0);
        assertEquals("Test", patName.myLastName);
        assertEquals("Majaconversion", patName.myFirstName);
                
        Xad patAddresses = curPatient.myPatientAddresses.get(0);
        assertEquals("1 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        Xtn patPhone = curPatient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        Ce lang = curPatient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
        
        //check visit lastUpdateTime
        assertTrue(nowMilli > curVisit.myRecordUpdatedDate.getTime());
            
        
        //check current visit
        assertEquals("11110000514", curVisit.myVisitNumber.myIdNumber);        
        assertEquals(Constants.ACTIVE_VISIT_STATUS, curVisit.myVisitStatus);
        assertEquals("I", curVisit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112021621"), curVisit.myAdmitDate);
        assertEquals("hospServ", curVisit.myHospitalService);
        Pl loc = curVisit.myAssignedPatientLocation;
        assertEquals("PMH 15C", loc.myPointOfCare);
        assertEquals("413", loc.myRoom);
        assertEquals("2", loc.myBed);
        
        Xcn admitDoc = curVisit.myAdmittingDoctors.get(0);
        assertEquals("13546c", admitDoc.myId);
        assertEquals("Physician", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("MoeC", admitDoc.myMiddleName);
        
        Xcn attendDoc = curVisit.myAttendingDoctors.get(0);
        assertEquals("13546a", attendDoc.myId);
        assertEquals("Physician", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("MoeA", attendDoc.myMiddleName);
        
        Xcn refDoc = curVisit.myReferringDoctors.get(0);
        assertEquals("13546b", refDoc.myId);
        assertEquals("Physician", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("MoeB", refDoc.myMiddleName);
        
        //check patient lastUpdateTime
        assertTrue(nowMilli > priorPatient.myRecordUpdatedDate.getTime());
        
        //check prior patient
        assertEquals("F", priorPatient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), priorPatient.myDateOfBirth);
        assertEquals(null, priorPatient.myDeathDateAndTime);
        assertEquals("N", priorPatient.myDeathIndicator);
        assertEquals(null, priorPatient.myMothersMaidenName);
        
        
        patId = priorPatient.myPatientIds.get(0);
        assertEquals("7012672", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        patName = priorPatient.myPatientNames.get(0);
        assertEquals("Test2", patName.myLastName);
        assertEquals("Majaconversion2", patName.myFirstName);
                
        patAddresses = priorPatient.myPatientAddresses.get(0);
        assertEquals("1 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        patPhone = priorPatient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        lang = priorPatient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
        
        
        //check visit lastUpdateTime
        assertTrue(nowMilli > priorVisit.myRecordUpdatedDate.getTime());
            
        
        //check prior visit       
        assertEquals("11110000513", priorVisit.myVisitNumber.myIdNumber);        
        assertEquals(Constants.ACTIVE_VISIT_STATUS, priorVisit.myVisitStatus);        
        assertEquals("I", priorVisit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112021621"), priorVisit.myAdmitDate);
        assertEquals("hospServ", priorVisit.myHospitalService);
        loc = priorVisit.myAssignedPatientLocation;
        assertEquals("PMH 15C", loc.myPointOfCare);
        assertEquals("413", loc.myRoom);
        assertEquals("2", loc.myBed);
        
        admitDoc = priorVisit.myAdmittingDoctors.get(0);
        assertEquals("13546c", admitDoc.myId);
        assertEquals("Physician", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("MoeC", admitDoc.myMiddleName);
        
        attendDoc = priorVisit.myAttendingDoctors.get(0);
        assertEquals("13546a", attendDoc.myId);
        assertEquals("Physician", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("MoeA", attendDoc.myMiddleName);
        
        refDoc = priorVisit.myReferringDoctors.get(0);
        assertEquals("13546b", refDoc.myId);
        assertEquals("Physician", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("MoeB", refDoc.myMiddleName);
        
        
        
        
    }    
        
    
    
    /**
     * Test visit move A45
     * - add patient 1
     * - add patient 2
     * - move visit from patient 1 to 2
     *   
     * @throws Exception ...
     */
    @SuppressWarnings("null")
    @Test
    public void testAdtA45() throws Exception {
        

        String moveFromPatMsg = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A01^ADT_A01|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A01|201112021621||||201112021621|G^4265^L\r" + 
                "PID|||7012672^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test2^Majaconversion2^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000513^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000513^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r";
        
        
        String moveToPatMsg = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A01^ADT_A01|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A01|201112021621||||201112021621|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000514^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r"; 
        
        
        String moveMsg = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201202291235||ADT^A45^ADT_A45|126421|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A45|201202291235||||201202291235|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" +               
                "MRG|7012672^^^UHN^MR^^^^^^^~HN3105^^^^PI^^^^^^^||||11110000513^^^EPR^VN^^^^^^^||\r" + 
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000513^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r";
        
       
        
        Persister.persist(UhnConverter.convertAdtOrFail(moveFromPatMsg));
        Persister.persist(UhnConverter.convertAdtOrFail(moveToPatMsg));
        Persister.persist(UhnConverter.convertAdtOrFail(moveMsg));

        
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);

        //There should be 2 pwv docs now
        assertEquals(2, pwvContainers.size());
        
        Patient moveToPatient = null;
        Visit moveToPatientVisit1 = null;        
        Visit moveToPatientVisit2 = null;
        Patient moveFromPatient = null;
        List<Visit> moveFromPatientVisits = null;
        
                        
        for (PatientWithVisitsContainer pwvContainer : pwvContainers) {
            if ( "7012673".equals(pwvContainer.getDocument().getMrn().myIdNumber) ) {
                                                
                assertEquals(2, pwvContainer.getDocument().myVisits.size());
                moveToPatient = pwvContainer.getDocument().myPatient;
                moveToPatientVisit1 = pwvContainer.getDocument().myVisits.get(0);
                moveToPatientVisit2 = pwvContainer.getDocument().myVisits.get(1);
                
            }   
            if ( "7012672".equals(pwvContainer.getDocument().getMrn().myIdNumber) ) {
                moveFromPatientVisits = pwvContainer.getDocument().myVisits;
                moveFromPatient = pwvContainer.getDocument().myPatient; 
                
            }            
            
        }
        
        //make sure move from/to patients and visits exist
        assertTrue(moveToPatient != null);
        assertTrue(moveToPatientVisit1 != null);
        assertTrue(moveToPatientVisit2 != null);
        assertTrue(moveFromPatient != null);
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > moveToPatient.myRecordUpdatedDate.getTime());
               
                
        //check moveTo patient
        assertEquals("F", moveToPatient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), moveToPatient.myDateOfBirth);
        assertEquals(null, moveToPatient.myDeathDateAndTime);
        assertEquals("N", moveToPatient.myDeathIndicator);
        assertEquals(null, moveToPatient.myMothersMaidenName);
        
        
        Cx patId = moveToPatient.myPatientIds.get(0);
        assertEquals("7012673", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Xpn patName = moveToPatient.myPatientNames.get(0);
        assertEquals("Test", patName.myLastName);
        assertEquals("Majaconversion", patName.myFirstName);
                
        Xad patAddresses = moveToPatient.myPatientAddresses.get(0);
        assertEquals("1 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        Xtn patPhone = moveToPatient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        Ce lang = moveToPatient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
        
        //check visit lastUpdateTime
        assertTrue(nowMilli > moveToPatientVisit1.myRecordUpdatedDate.getTime());
        
        //check moveTo patient's first visit (this is the patient's existing visit)
        assertEquals("11110000514", moveToPatientVisit1.myVisitNumber.myIdNumber);
        assertEquals(Constants.ACTIVE_VISIT_STATUS, moveToPatientVisit1.myVisitStatus);
        assertEquals("I", moveToPatientVisit1.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112021621"), moveToPatientVisit1.myAdmitDate);
        assertEquals("hospServ", moveToPatientVisit1.myHospitalService);
        Pl loc = moveToPatientVisit1.myAssignedPatientLocation;
        assertEquals("PMH 15C", loc.myPointOfCare);
        assertEquals("413", loc.myRoom);
        assertEquals("2", loc.myBed);
        
        Xcn admitDoc = moveToPatientVisit1.myAdmittingDoctors.get(0);
        assertEquals("13546c", admitDoc.myId);
        assertEquals("Physician", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("MoeC", admitDoc.myMiddleName);
        
        Xcn attendDoc = moveToPatientVisit1.myAttendingDoctors.get(0);
        assertEquals("13546a", attendDoc.myId);
        assertEquals("Physician", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("MoeA", attendDoc.myMiddleName);
        
        Xcn refDoc = moveToPatientVisit1.myReferringDoctors.get(0);
        assertEquals("13546b", refDoc.myId);
        assertEquals("Physician", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("MoeB", refDoc.myMiddleName);
        
        //check visit lastUpdateTime
        assertTrue(nowMilli > moveToPatientVisit2.myRecordUpdatedDate.getTime());
        
        //check moveTo patient's second visit (this is the visit that got moved over from the moveFrom patient)
        assertEquals("11110000513", moveToPatientVisit2.myVisitNumber.myIdNumber);        
        assertEquals(Constants.ACTIVE_VISIT_STATUS, moveToPatientVisit2.myVisitStatus);
        assertEquals("I", moveToPatientVisit2.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112021621"), moveToPatientVisit2.myAdmitDate);
        assertEquals("hospServ", moveToPatientVisit2.myHospitalService);
        loc = moveToPatientVisit2.myAssignedPatientLocation;
        assertEquals("PMH 15C", loc.myPointOfCare);
        assertEquals("413", loc.myRoom);
        assertEquals("2", loc.myBed);
        
        admitDoc = moveToPatientVisit2.myAdmittingDoctors.get(0);
        assertEquals("13546c", admitDoc.myId);
        assertEquals("Physician", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("MoeC", admitDoc.myMiddleName);
        
        attendDoc = moveToPatientVisit2.myAttendingDoctors.get(0);
        assertEquals("13546a", attendDoc.myId);
        assertEquals("Physician", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("MoeA", attendDoc.myMiddleName);
        
        refDoc = moveToPatientVisit2.myReferringDoctors.get(0);
        assertEquals("13546b", refDoc.myId);
        assertEquals("Physician", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("MoeB", refDoc.myMiddleName); 
        
        //check patient lastUpdateTime
        assertTrue(nowMilli > moveFromPatient.myRecordUpdatedDate.getTime());
                
        //check moveFrom patient
        assertEquals("F", moveFromPatient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), moveFromPatient.myDateOfBirth);
        assertEquals(null, moveFromPatient.myDeathDateAndTime);
        assertEquals("N", moveFromPatient.myDeathIndicator);
        assertEquals(null, moveFromPatient.myMothersMaidenName);
        
        
        patId = moveFromPatient.myPatientIds.get(0);
        assertEquals("7012672", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        patName = moveFromPatient.myPatientNames.get(0);
        assertEquals("Test2", patName.myLastName);
        assertEquals("Majaconversion2", patName.myFirstName);
                
        patAddresses = moveFromPatient.myPatientAddresses.get(0);
        assertEquals("1 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        patPhone = moveFromPatient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        lang = moveFromPatient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
            
        
        //check moveFrom patient's visit. It shouldn't exist since it got moved over
        assertTrue(moveFromPatientVisits == null || moveFromPatientVisits.isEmpty());
        
    }    
    
    
    
    
    /**
     * Testing Update adverse reaction (A60)
     * - admit a patient then send an A60 to update/add Allergy info
     *   
     * @throws Exception ...
     */
    @Test
    public void testAdtA60a() throws Exception {
        
        String message1 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A01^ADT_A01|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A01|201112021621||||201112021621|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "ROL||UC|PP^Primary Care Provider^15ZRole^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^^^^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|O^Office^^^^|^^^ON^^Can^^^^^^^^|(416) 340-3391^WPN^PH^^^^^^^^^\r" + 
                "ROL||UC|PP^Primary Care Provider^15ZRole^^^|13546b^Genericb^Physicianb^Moeb^^Dr.^MD^^UHN^L^^^EI^G^2.16.840.1.113883.3.59.3:947^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|1^Hospital^15ZOrgTyp^^^|^^^ON^^Can^^^^^^^^|(416) 340-3388^WPN^PH^^^^^^^^^\r" +
                "NK1|1|Wph^Mom^^^^^L^^^^^^^|PAR^Parent^03ZRelshp^^^|82 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^~(416)123-1234^PRN^CP^^^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^|N^Next-of-Kin^03ZConRol^^^||||||||||||||||||||||||||||||||\r" +
                "NK1|2|Wph^Brother^^^^^L^^^^^^^|BRO^Brother^03ZRelshp^^^|83 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3601^PRN^PH^^^^^^^^^|(416)525-2525^PRN^PH^^^^^^^^^|C^Emergency Contact^03ZConRol^^^||||||||||||||||||||||||||||||||\r" + 
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000514^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r" + 
                "PV2||S^Semi^03ZFinbed^^^|^kfkfkfjcjcjcgcgcclcl^03ZAmitRes^^^|||||||||||||||||||N||AI|Elective||||||N|||||||OTH^Self^03ZBrInBy^^^|||||||||||\r" + 
                "DG1|1||06^KFKFKFJCJCJCGCGCCLCL^2.16.840.1.113883.11.19436^^^|KFKFKFJCJCJCGCGCCLCL|201112021621|A|||||||||1||D||||\r" + 
                "PR1||||||||||||||||||||\r" + 
                "ZPV|OTH^Self|F^Standard|||N|||13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||Medical Services^General Internal Medicine^GMED|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|413||3910||^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r" + 
                "ZWA||||||||active|\r";
        
        
                String message2 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A60^ADT_A60|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A60|201112021621||||201112021621|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" +
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000514^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r" + 
                "PV2||S^Semi^03ZFinbed^^^|^kfkfkfjcjcjcgcgcclcl^03ZAmitRes^^^|||||||||||||||||||N||AI|Elective||||||N|||||||OTH^Self^03ZBrInBy^^^|||||||||||\r" +
                "IAM||DA^Drug Allergy^03ZPAR^^^|K03733-01-001^Contrast Medium/Dye^1.3.6.1.4.1.12201.2.4^^^|MI^Mild|GI upset~Stink|A^Add/Insert|||AL^Allergy^03ZPAR^^^||20120106|sudden reaction|201201060958||SEL^Self^03ZPAR^^^||||\r" +
                "IAM||DA^Drug Allergy^03ZPAR^^^|P00036^Tetracyclines^1.3.6.1.4.1.12201.2.4^^^|MI^Mild|swelling~rash|A^Add/Insert|||AL^Allergy^03ZPAR^^^||20120107|sudden reaction|20120106||SEL^Self^03ZPAR^^^||||\r" +
                "IAM||FA^Food Allergy^03ZPAR^^^|FreeText^No known food allergy/adverse reaction^1.3.6.1.4.1.12201.2.4^^^|MI^Mild|None|A^Add/Insert|||AL^Allergy^03ZPAR^^^||20120108|sudden reaction|201201||SEL^Self^03ZPAR^^^||||\r" +
                "IAM||MA^Miscellaneous Allergy^03ZPAR^^^|FreeText^No known latex/other allergy/adverse reaction^1.3.6.1.4.1.12201.2.4^^^|MI^Mild|None|A^Add/Insert|||AL^Allergy^03ZPAR^^^||20120109|sudden reaction|20120106095800||SEL^Self^03ZPAR^^^||||\r";

        
        Persister.persist(UhnConverter.convertAdtOrFail(message1));
        Persister.persist(UhnConverter.convertAdtOrFail(message2));        

        
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
        
        PatientWithVisitsContainer pwvContainer = pwvContainers.get(0);
        Patient patient = pwvContainer.getDocument().myPatient;
        Visit visit = pwvContainer.getDocument().myVisits.get(0);
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > patient.myRecordUpdatedDate.getTime());
        
        
        //check patient
        assertEquals("F", patient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), patient.myDateOfBirth);
        assertEquals(null, patient.myDeathDateAndTime);
        assertEquals("N", patient.myDeathIndicator);
        assertEquals(null, patient.myMothersMaidenName);
        
        
        Cx patId = patient.myPatientIds.get(0);
        assertEquals("7012673", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Xpn patName = patient.myPatientNames.get(0);
        assertEquals("Test", patName.myLastName);
        assertEquals("Majaconversion", patName.myFirstName);
                
        Xad patAddresses = patient.myPatientAddresses.get(0);
        assertEquals("1 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        Xtn patPhone = patient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        Ce lang = patient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
        
        
        //check patient roles
        List<PersonInRole> roles  = patient.myPersonInRoles;
        assertEquals(2, roles.size());
        PersonInRole rol_1 = patient.myPersonInRoles.get(0);
        PersonInRole rol_2 = patient.myPersonInRoles.get(1);        
        //ROL(1)
        assertEquals("PP",rol_1.myRole.myCode);
        assertEquals("Primary Care Provider",rol_1.myRole.myText);        
        assertEquals("Generic", rol_1.myPersonNames.get(0).myLastName);
        assertEquals("Physician", rol_1.myPersonNames.get(0).myFirstName);
        assertEquals("Moe", rol_1.myPersonNames.get(0).myMiddleName);
        assertEquals("(416) 340-3391", rol_1.myContactInformation.get(0).myPhoneNumber);
        //ROL(2)
        assertEquals("PP",rol_2.myRole.myCode);
        assertEquals("Primary Care Provider",rol_2.myRole.myText);        
        assertEquals("Genericb", rol_2.myPersonNames.get(0).myLastName);
        assertEquals("Physicianb", rol_2.myPersonNames.get(0).myFirstName);
        assertEquals("Moeb", rol_2.myPersonNames.get(0).myMiddleName);
        assertEquals("(416) 340-3388", rol_2.myContactInformation.get(0).myPhoneNumber);
        
        
        //check patient associated parties
        List<AssociatedParty> parties  = patient.myAssociatedParties;
        assertEquals(2, parties.size());
        AssociatedParty party_1 = patient.myAssociatedParties.get(0);
        AssociatedParty party_2 = patient.myAssociatedParties.get(1);        
        //NK1(1)
        assertEquals("Wph",party_1.myNames.get(0).myLastName);
        assertEquals("Mom",party_1.myNames.get(0).myFirstName);
        assertEquals("PAR",party_1.myRelationship.myCode);
        assertEquals("Parent",party_1.myRelationshipName);
        assertEquals("82 Buttonwood Avenue",party_1.myAddresses.get(0).myStreetAddress);
        assertEquals("(416)243-3600",party_1.myContactInformation.get(0).myPhoneNumber);
        //NK1(2)
        assertEquals("Wph",party_2.myNames.get(0).myLastName);
        assertEquals("Brother",party_2.myNames.get(0).myFirstName);
        assertEquals("BRO",party_2.myRelationship.myCode);
        assertEquals("Brother",party_2.myRelationshipName);
        assertEquals("83 Buttonwood Avenue",party_2.myAddresses.get(0).myStreetAddress);
        assertEquals("(416)243-3601",party_2.myContactInformation.get(0).myPhoneNumber);
        
      
        //check allergies
        List<AdverseReaction> allergies  = patient.myAdverseReactions;
        assertEquals(4, allergies.size());
        AdverseReaction allergy1 = patient.myAdverseReactions.get(0);
        AdverseReaction allergy2 = patient.myAdverseReactions.get(1);
        AdverseReaction allergy3 = patient.myAdverseReactions.get(2);
        AdverseReaction allergy4 = patient.myAdverseReactions.get(3);
        //IAM(1)
        assertEquals("DA",allergy1.myAllergenTypeCode.myCode);
        assertEquals("K03733-01-001",allergy1.myAllergenCode.myCode);
        assertEquals("Contrast Medium/Dye",allergy1.myAllergenCode.myText);
        assertEquals("MI",allergy1.myAllergySeverityCode.myCode);
        assertEquals("GI upset",allergy1.myAllergyReactionCodes.get(0));
        assertEquals("Stink",allergy1.myAllergyReactionCodes.get(1));
        //assertEquals("AL",allergy1.mySensitivityToCausativeAgentCode.myCode);
        assertEquals(ourDtFormat.parse("20120106"),allergy1.myOnsetDate);
        assertEquals("sudden reaction",allergy1.myOnsetText);
        assertEquals(ourTsFormat.parse("201201060958"),allergy1.myReportedDateTime);
        assertEquals("SEL",allergy1.myRelationshipToPatient.myCode);
        //IAM(2)
        assertEquals("DA",allergy2.myAllergenTypeCode.myCode);
        assertEquals("P00036",allergy2.myAllergenCode.myCode);
        assertEquals("Tetracyclines",allergy2.myAllergenCode.myText);
        assertEquals("MI",allergy2.myAllergySeverityCode.myCode);
        assertEquals("swelling",allergy2.myAllergyReactionCodes.get(0));
        assertEquals("rash",allergy2.myAllergyReactionCodes.get(1));
        //assertEquals("AL",allergy2.mySensitivityToCausativeAgentCode.myCode);
        assertEquals(ourDtFormat.parse("20120107"),allergy2.myOnsetDate);
        assertEquals("sudden reaction",allergy2.myOnsetText);
        assertEquals(ourDtFormat.parse("20120106"),allergy2.myReportedDateTime);
        assertEquals("SEL",allergy2.myRelationshipToPatient.myCode);        
        //IAM(3)
        assertEquals("FA",allergy3.myAllergenTypeCode.myCode);
        assertEquals("FreeText",allergy3.myAllergenCode.myCode);
        assertEquals("No known food allergy/adverse reaction",allergy3.myAllergenCode.myText);
        assertEquals("MI",allergy3.myAllergySeverityCode.myCode);
        assertEquals("None",allergy3.myAllergyReactionCodes.get(0));        
        //assertEquals("AL",allergy3.mySensitivityToCausativeAgentCode.myCode);
        assertEquals(ourDtFormat.parse("20120108"),allergy3.myOnsetDate);
        assertEquals("sudden reaction",allergy3.myOnsetText);        
        assertEquals(ourTsMonthFormat.parse("201201"),allergy3.myReportedDateTime);
        assertEquals("SEL",allergy3.myRelationshipToPatient.myCode);        
        //IAM(4)
        assertEquals("MA",allergy4.myAllergenTypeCode.myCode);
        assertEquals("FreeText",allergy4.myAllergenCode.myCode);
        assertEquals("No known latex/other allergy/adverse reaction",allergy4.myAllergenCode.myText);
        assertEquals("MI",allergy4.myAllergySeverityCode.myCode);
        assertEquals("None",allergy4.myAllergyReactionCodes.get(0));        
        //assertEquals("AL",allergy4.mySensitivityToCausativeAgentCode.myCode);
        assertEquals(ourDtFormat.parse("20120109"),allergy4.myOnsetDate);
        assertEquals("sudden reaction",allergy4.myOnsetText);
        assertEquals(ourTsSecFormat.parse("20120106095800"),allergy4.myReportedDateTime);
        assertEquals("SEL",allergy4.myRelationshipToPatient.myCode); 
        
        //check visit lastUpdateTime
        assertTrue(nowMilli > visit.myRecordUpdatedDate.getTime());
        
        //check visit
        assertEquals("11110000514", visit.myVisitNumber.myIdNumber);
        assertEquals(Constants.ACTIVE_VISIT_STATUS, visit.myVisitStatus);
        assertEquals("I", visit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112021621"), visit.myAdmitDate);
        assertEquals("hospServ", visit.myHospitalService);
        Pl loc = visit.myAssignedPatientLocation;
        assertEquals("PMH 15C", loc.myPointOfCare);
        assertEquals("413", loc.myRoom);
        assertEquals("2", loc.myBed);
        
        Xcn admitDoc = visit.myAdmittingDoctors.get(0);
        assertEquals("13546c", admitDoc.myId);
        assertEquals("Physician", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("MoeC", admitDoc.myMiddleName);
        
        Xcn attendDoc = visit.myAttendingDoctors.get(0);
        assertEquals("13546a", attendDoc.myId);
        assertEquals("Physician", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("MoeA", attendDoc.myMiddleName);
        
        Xcn refDoc = visit.myReferringDoctors.get(0);
        assertEquals("13546b", refDoc.myId);
        assertEquals("Physician", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("MoeB", refDoc.myMiddleName);
        
         
        Diagnosis dg = visit.myDiagnoses.get(0);
        Ce dgCode = dg.myDiagnosis;
        assertEquals("06", dgCode.myCode);
        assertEquals("KFKFKFJCJCJCGCGCCLCL", dgCode.myText);
        assertEquals("2.16.840.1.113883.11.19436", dgCode.myCodeSystem);
        
        
        
    }    
    
    
    
    /**
     * Testing Update adverse reaction (A60)
     * - send an A60 to update/add Allergy info but patient does not exist
     *   
     * @throws Exception ...
     */
    @Test
    public void testAdtA60b() throws Exception {
        
                String message1 = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A60^ADT_A60|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A60|201112021621||||201112021621|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" +
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000514^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r" + 
                "PV2||S^Semi^03ZFinbed^^^|^kfkfkfjcjcjcgcgcclcl^03ZAmitRes^^^|||||||||||||||||||N||AI|Elective||||||N|||||||OTH^Self^03ZBrInBy^^^|||||||||||\r" +
                "IAM||DA^Drug Allergy^03ZPAR^^^|K03733-01-001^Contrast Medium/Dye^1.3.6.1.4.1.12201.2.4^^^|MI^Mild|GI upset~Stink|A^Add/Insert|||AL^Allergy^03ZPAR^^^||20120106|sudden reaction|201201060958||SEL^Self^03ZPAR^^^||||\r" +
                "IAM||DA^Drug Allergy^03ZPAR^^^|P00036^Tetracyclines^1.3.6.1.4.1.12201.2.4^^^|MI^Mild|swelling~rash|A^Add/Insert|||AL^Allergy^03ZPAR^^^||20120107|sudden reaction|20120106||SEL^Self^03ZPAR^^^||||\r" +
                "IAM||FA^Food Allergy^03ZPAR^^^|FreeText^No known food allergy/adverse reaction^1.3.6.1.4.1.12201.2.4^^^|MI^Mild|None|A^Add/Insert|||AL^Allergy^03ZPAR^^^||20120108|sudden reaction|201201||SEL^Self^03ZPAR^^^||||\r" +
                "IAM||MA^Miscellaneous Allergy^03ZPAR^^^|FreeText^No known latex/other allergy/adverse reaction^1.3.6.1.4.1.12201.2.4^^^|MI^Mild|None|A^Add/Insert|||AL^Allergy^03ZPAR^^^||20120109|sudden reaction|20120106095800||SEL^Self^03ZPAR^^^||||\r";
                
        
        Persister.persist(UhnConverter.convertAdtOrFail(message1));
        
        ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
        List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
        
        PatientWithVisitsContainer pwvContainer = pwvContainers.get(0);
        Patient patient = pwvContainer.getDocument().myPatient;
        Visit visit = pwvContainer.getDocument().myVisits.get(0);
        
        //check patient lastUpdateTime
        Date now = new Date();
        long nowMilli = now.getTime();
        assertTrue(nowMilli > patient.myRecordUpdatedDate.getTime());
        
        
        //check patient
        assertEquals("F", patient.myAdministrativeSex);
        assertEquals(ourTsFormat.parse("197312300000"), patient.myDateOfBirth);
        assertEquals(null, patient.myDeathDateAndTime);
        assertEquals("N", patient.myDeathIndicator);
        assertEquals(null, patient.myMothersMaidenName);
        
        
        Cx patId = patient.myPatientIds.get(0);
        assertEquals("7012673", patId.myIdNumber);
        assertEquals("MR", patId.myIdTypeCode);
        
        Xpn patName = patient.myPatientNames.get(0);
        assertEquals("Test", patName.myLastName);
        assertEquals("Majaconversion", patName.myFirstName);
                
        Xad patAddresses = patient.myPatientAddresses.get(0);
        assertEquals("1 Bloor Street ", patAddresses.myStreetAddress);
        assertEquals("Toronto", patAddresses.myCity);
        
        Xtn patPhone = patient.myPhoneNumbers.get(0);
        assertEquals("(415)222-3333", patPhone.myPhoneNumber);
        
        Ce lang = patient.myPrimaryLanguage;
        assertEquals("eng", lang.myCode);
        assertEquals("English", lang.myText);
        
        //check allergies
        List<AdverseReaction> allergies  = patient.myAdverseReactions;
        assertEquals(4, allergies.size());
        AdverseReaction allergy1 = patient.myAdverseReactions.get(0);
        AdverseReaction allergy2 = patient.myAdverseReactions.get(1);
        AdverseReaction allergy3 = patient.myAdverseReactions.get(2);
        AdverseReaction allergy4 = patient.myAdverseReactions.get(3);
        
        
        //IAM(1)
        assertEquals("DA",allergy1.myAllergenTypeCode.myCode);
        assertEquals("K03733-01-001",allergy1.myAllergenCode.myCode);
        assertEquals("Contrast Medium/Dye",allergy1.myAllergenCode.myText);
        assertEquals("MI",allergy1.myAllergySeverityCode.myCode);
        assertEquals("GI upset",allergy1.myAllergyReactionCodes.get(0));
        assertEquals("Stink",allergy1.myAllergyReactionCodes.get(1));
        //assertEquals("AL",allergy1.mySensitivityToCausativeAgentCode.myCode);
        assertEquals(ourDtFormat.parse("20120106"),allergy1.myOnsetDate);
        assertEquals("sudden reaction",allergy1.myOnsetText);
        assertEquals(ourTsFormat.parse("201201060958"),allergy1.myReportedDateTime);
        assertEquals("SEL",allergy1.myRelationshipToPatient.myCode);
        //IAM(2)
        assertEquals("DA",allergy2.myAllergenTypeCode.myCode);
        assertEquals("P00036",allergy2.myAllergenCode.myCode);
        assertEquals("Tetracyclines",allergy2.myAllergenCode.myText);
        assertEquals("MI",allergy2.myAllergySeverityCode.myCode);
        assertEquals("swelling",allergy2.myAllergyReactionCodes.get(0));
        assertEquals("rash",allergy2.myAllergyReactionCodes.get(1));
        //assertEquals("AL",allergy2.mySensitivityToCausativeAgentCode.myCode);
        assertEquals(ourDtFormat.parse("20120107"),allergy2.myOnsetDate);
        assertEquals("sudden reaction",allergy2.myOnsetText);
        assertEquals(ourDtFormat.parse("20120106"),allergy2.myReportedDateTime);
        assertEquals("SEL",allergy2.myRelationshipToPatient.myCode);        
        //IAM(3)
        assertEquals("FA",allergy3.myAllergenTypeCode.myCode);
        assertEquals("FreeText",allergy3.myAllergenCode.myCode);
        assertEquals("No known food allergy/adverse reaction",allergy3.myAllergenCode.myText);
        assertEquals("MI",allergy3.myAllergySeverityCode.myCode);
        assertEquals("None",allergy3.myAllergyReactionCodes.get(0));        
        //assertEquals("AL",allergy3.mySensitivityToCausativeAgentCode.myCode);
        assertEquals(ourDtFormat.parse("20120108"),allergy3.myOnsetDate);
        assertEquals("sudden reaction",allergy3.myOnsetText);
        assertEquals(ourTsMonthFormat.parse("201201"),allergy3.myReportedDateTime);
        assertEquals("SEL",allergy3.myRelationshipToPatient.myCode);        
        //IAM(4)
        assertEquals("MA",allergy4.myAllergenTypeCode.myCode);
        assertEquals("FreeText",allergy4.myAllergenCode.myCode);
        assertEquals("No known latex/other allergy/adverse reaction",allergy4.myAllergenCode.myText);
        assertEquals("MI",allergy4.myAllergySeverityCode.myCode);
        assertEquals("None",allergy4.myAllergyReactionCodes.get(0));        
        //assertEquals("AL",allergy4.mySensitivityToCausativeAgentCode.myCode);
        assertEquals(ourDtFormat.parse("20120109"),allergy4.myOnsetDate);
        assertEquals("sudden reaction",allergy4.myOnsetText);
        assertEquals(ourTsSecFormat.parse("20120106095800"),allergy4.myReportedDateTime);
        assertEquals("SEL",allergy4.myRelationshipToPatient.myCode);     
        
        
        //check visit lastUpdateTime
        assertTrue(nowMilli > visit.myRecordUpdatedDate.getTime());
        
        //check visit
        assertEquals("11110000514", visit.myVisitNumber.myIdNumber);
        assertEquals(Constants.UNDEF_VISIT_STATUS, visit.myVisitStatus);
        assertEquals("I", visit.myPatientClassCode);
        assertEquals(ourTsFormat.parse("201112021621"), visit.myAdmitDate);
        assertEquals("hospServ", visit.myHospitalService);
        Pl loc = visit.myAssignedPatientLocation;
        assertEquals("PMH 15C", loc.myPointOfCare);
        assertEquals("413", loc.myRoom);
        assertEquals("2", loc.myBed);
        
        Xcn admitDoc = visit.myAdmittingDoctors.get(0);
        assertEquals("13546c", admitDoc.myId);
        assertEquals("Physician", admitDoc.myFirstName);
        assertEquals("Generic", admitDoc.myLastName);
        assertEquals("MoeC", admitDoc.myMiddleName);
        
        Xcn attendDoc = visit.myAttendingDoctors.get(0);
        assertEquals("13546a", attendDoc.myId);
        assertEquals("Physician", attendDoc.myFirstName);
        assertEquals("Generic", attendDoc.myLastName);
        assertEquals("MoeA", attendDoc.myMiddleName);
        
        Xcn refDoc = visit.myReferringDoctors.get(0);
        assertEquals("13546b", refDoc.myId);
        assertEquals("Physician", refDoc.myFirstName);
        assertEquals("Generic", refDoc.myLastName);
        assertEquals("MoeB", refDoc.myMiddleName);
        
         
        
        
    }    
    
    @Test
    public void testFailingRvhMessage() throws Exception {

    	String firstMessages = "MSH|^~\\&|2.16.840.1.113883.3.239.23.4^2.16.840.1.113883.3.239.23.4.101.1|RVC|cGTA|cGTA|20120830142039.0080-0400|32986342953eee|ADT^A08|ADT-ITS.1.14110|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r\n" + 
    			"EVN||20120830141700.0000-0400||||20120619111300.0000-0400\r\n" + 
    			"PID|1||Y0000550^^^2.16.840.1.113883.3.239.23.4&2.16.840.1.113883.3.239.23.4.101.1^MR~Y494^^^2.16.840.1.113883.3.239.23.4&2.16.840.1.113883.3.239.23.4.101.1^PI~9^^^^JHN^^^^CAN&Canada&HL70363||ECHN^IDEA^^^^^L~NONE^NONE^^^^^M|IDEAMOM^^^^^^U|20120618000000.0000-0400|F|||999 ROUGE VALLY ROAD - AUG 20^OTHER - UPDATE AUG 20^TORONTO^CANON^M1B 1A1^^H^^1811||^PRN~^OTH|^WPN||S|ANASW|YA00117/12|9\r\n" + 
    			"NK1|1|ECHNMOM^IDEAMOM^^^^^U|MTH|123 ROUGE VALLY ROAD UPDATE^OTHER^TORONTO^CANON^M1B 1A1^^H|||NOK\r\n" + 
    			"NK1|2|ECHNDAD^IDEADAD^^^^^U|UNK|123 ROUGE VALLY ROAD UPDATE^OTHER^TORONTO^CANON^M1B 1A1^^H|||NOT\r\n" + 
    			"NK1|3|UNKNOWN,UNKNOWN^^^^^^U||UNKNOWN^UNKNOWN^UNKNOWN^CANON^M1B 1A1^^H|||EMP||||||UNKNOWN,UNKNOWN\r\n" + 
    			"PV1|1|I|YCCU^Y/OFCCU^3^2.16.840.1.113883.3.239.23.4&2.16.840.1.113883.3.239.23.4.100.2|U|||DANG^D'ANGELO^ANTHONY^^^^^^2.16.840.1.113883.4.347^^^^XX|OANI^OANDASAN^IVY^FELICIDAD^^^^^2.16.840.1.113883.4.347^^^^XX|OBRS^OBRIEN^SHARON^^^^^^2.16.840.1.113883.4.347^^^^XX~ODAJ^ODA^JENY^^^^^^2.16.840.1.113883.4.347^^^^XX|ALC-FUNC||||01|||AKBN^AKBARALI^NURJEHAN^^^^^^2.16.840.1.113883.4.347^^^^XX|IN|YA00117/12^^^2.16.840.1.113883.3.239.23.4&2.16.840.1.113883.3.239.23.4.101.1^AN|SP|||||||||||||||||||RVC||ADM|||20120619111300.0000-0400||||||||N^DOCTOR^NO^FAMILY^^^^^2.16.840.1.113883.4.347^^^^XX\r\n" + 
    			"PV2||AP^ACUTE PRIVATE|NEWBORN|||||||4|72|UNKNOWN|||||||||||||U|||||||||||N||C^CARRIED\r\n" + 
    			"AL1||DA|NKA^NO KNOWN ALLERGIES^^NO KNOWN ALLERGIES|U||20120619\r\n" + 
    			"IN1|\r\n" + 
    			"ROL|1|AD|PP|N^DOCTOR^NO^FAMILY^^^^^2.16.840.1.113883.4.347^^^^XX\r\n" + 
    			"ROL|2|AD|PP|PACK^PACE^KENNETH^^^^^^2.16.840.1.113883.4.347^^^^XX\r\n" + 
    			"ROL|3|AD|PP|DANG^D'ANGELO^ANTHONY^^^^^^2.16.840.1.113883.4.347^^^^XX\r\n" + 
    			"ROL|4|AD|PP|OANI^OANDASAN^IVY^FELICIDAD^^^^^2.16.840.1.113883.4.347^^^^XX\r\n" + 
    			"ROL|5|AD|PP|AKBN^AKBARALI^NURJEHAN^^^^^^2.16.840.1.113883.4.347^^^^XX\r\n" + 
    			"ROL|6|AD|PP|OBRS^OBRIEN^SHARON^^^^^^2.16.840.1.113883.4.347^^^^XX~ODAJ^ODA^JENY^^^^^^2.16.840.1.113883.4.347^^^^XX\r\n" + 
    			"\r\n" + 
    			"MSH|^~\\&|2.16.840.1.113883.3.239.23.4^2.16.840.1.113883.3.239.23.4.101.1|RVC|cGTA|cGTA|20120831153744.1050-0400|32986342953eee|ADT^A08|ADT-ITS.1.14111|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r\n" + 
    			"EVN||20120831153400.0000-0400||||20120619111300.0000-0400\r\n" + 
    			"PID|1||Y0000550^^^2.16.840.1.113883.3.239.23.4&2.16.840.1.113883.3.239.23.4.101.1^MR~Y494^^^2.16.840.1.113883.3.239.23.4&2.16.840.1.113883.3.239.23.4.101.1^PI~9^^^^JHN^^^^CAN&Canada&HL70363||ECHN^IDEA^^^^^L~NONE^NONE^^^^^M|IDEAMOM^^^^^^U|20120618000000.0000-0400|F|||999 ROUGE VALLY ROAD - AUG 20^OTHER - UPDATE AUG 20^TORONTO^CANON^M1B 1A1^^H^^1811||^PRN~^OTH|^WPN||S|ANASW|YA00117/12|9\r\n" + 
    			"NK1|1|ECHNMOM^IDEAMOM^^^^^U|MTH|123 ROUGE VALLY ROAD UPDATE^OTHER^TORONTO^CANON^M1B 1A1^^H|||NOK\r\n" + 
    			"NK1|2|ECHNDAD^IDEADAD^^^^^U|UNK|123 ROUGE VALLY ROAD UPDATE^OTHER^TORONTO^CANON^M1B 1A1^^H|||NOT\r\n" + 
    			"NK1|3|UNKNOWN,UNKNOWN^^^^^^U||UNKNOWN^UNKNOWN^UNKNOWN^CANON^M1B 1A1^^H|||EMP||||||UNKNOWN,UNKNOWN\r\n" + 
    			"PV1|1|I|YCCU^Y/OFCCU^3^2.16.840.1.113883.3.239.23.4&2.16.840.1.113883.3.239.23.4.100.2|R|||59615^D'ANGELO^ANTHONY^JAMES^^^^^2.16.840.1.113883.4.347^^^^XX|64065^OANDASAN^IVY^FELICIDAD^^^^^2.16.840.1.113883.4.347^^^^XX|59607^O'BRIEN^SHARON^ROSE^^^^^2.16.840.1.113883.4.347^^^^XX~66851^ODA^JENNY^WADIE NASSIEF^^^^^2.16.840.1.113883.4.347^^^^XX|ALC-FUNC||||01|||42959^AKBARALI^NURJEHAN^^^^^^2.16.840.1.113883.4.347^^^^XX|IN|YA00117/12^^^2.16.840.1.113883.3.239.23.4&2.16.840.1.113883.3.239.23.4.101.1^AN|SP|||||||||||||||||||RVC||ADM|||20120619111300.0000-0400||||||||4095^Lakeridge Health^Provider Unavailable^^^^^^2.16.840.1.113883.4.347^^^^XX\r\n" + 
    			"PV2||AP^ACUTE PRIVATE|NEWBORN - TEST ESB AUG 31|||||||4|73|UNKNOWN|||||||||||||R|||||||||||N||C^CARRIED\r\n" + 
    			"AL1||DA|NKA^NO KNOWN ALLERGIES^^NO KNOWN ALLERGIES|U||20120619\r\n" + 
    			"IN1|\r\n" + 
    			"ROL|1|AD|PP|4095^Lakeridge Health^Provider Unavailable^^^^^^2.16.840.1.113883.4.347^^^^XX\r\n" + 
    			"ROL|2|AD|PP|68126^PACE^KENNETH^TONY^^^^^2.16.840.1.113883.4.347^^^^XX\r\n" + 
    			"ROL|3|AD|PP|59615^D'ANGELO^ANTHONY^JAMES^^^^^2.16.840.1.113883.4.347^^^^XX\r\n" + 
    			"ROL|4|AD|PP|64065^OANDASAN^IVY^FELICIDAD^^^^^2.16.840.1.113883.4.347^^^^XX\r\n" + 
    			"ROL|5|AD|PP|42959^AKBARALI^NURJEHAN^^^^^^2.16.840.1.113883.4.347^^^^XX\r\n" + 
    			"ROL|6|AD|PP|59607^O'BRIEN^SHARON^ROSE^^^^^2.16.840.1.113883.4.347^^^^XX~66851^ODA^JENNY^WADIE NASSIEF^^^^^2.16.840.1.113883.4.347^^^^XX\r\n" + 
    			"\r\n" + 
    			"MSH|^~\\&|2.16.840.1.113883.3.239.23.4^2.16.840.1.113883.3.239.23.4.101.1|RVC|cGTA|cGTA|20120904150044.9600-0400|32986342953eee|ADT^A08|ADT-ITS.1.14111|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r\n" + 
    			"EVN||20120831153400.0000-0400||||20120619111300.0000-0400\r\n" + 
    			"PID|1||Y0000550^^^2.16.840.1.113883.3.239.23.4&2.16.840.1.113883.3.239.23.4.101.1^MR~Y494^^^2.16.840.1.113883.3.239.23.4&2.16.840.1.113883.3.239.23.4.101.1^PI~9^^^^JHN^^^^CAN&Canada&HL70363||ECHN^IDEA^^^^^L~NONE^NONE^^^^^M|IDEAMOM^^^^^^U|20120618000000.0000-0400|F|||999 ROUGE VALLY ROAD - AUG 20^OTHER - UPDATE AUG 20^TORONTO^CANON^M1B 1A1^^H^^1811||^PRN~^OTH|^WPN||S|ANASW|YA00117/12|9\r\n" + 
    			"NK1|1|ECHNMOM^IDEAMOM^^^^^U|MTH|123 ROUGE VALLY ROAD UPDATE^OTHER^TORONTO^CANON^M1B 1A1^^H|||NOK\r\n" + 
    			"NK1|2|ECHNDAD^IDEADAD^^^^^U|UNK|123 ROUGE VALLY ROAD UPDATE^OTHER^TORONTO^CANON^M1B 1A1^^H|||NOT\r\n" + 
    			"NK1|3|UNKNOWN,UNKNOWN^^^^^^U|UNK|UNKNOWN^UNKNOWN^UNKNOWN^CANON^M1B 1A1^^H|||EMP||||||UNKNOWN,UNKNOWN\r\n" + 
    			"PV1|1|I|YCCU^Y/OFCCU^3^2.16.840.1.113883.3.239.23.4&2.16.840.1.113883.3.239.23.4.100.2|R|||59615^D'ANGELO^ANTHONY^JAMES^^^^^2.16.840.1.113883.4.347^^^^XX|64065^OANDASAN^IVY^FELICIDAD^^^^^2.16.840.1.113883.4.347^^^^XX|59607^O'BRIEN^SHARON^ROSE^^^^^2.16.840.1.113883.4.347^^^^XX~66851^ODA^JENNY^WADIE NASSIEF^^^^^2.16.840.1.113883.4.347^^^^XX|ALC-FUNC||||01|||42959^AKBARALI^NURJEHAN^^^^^^2.16.840.1.113883.4.347^^^^XX|IN|YA00117/12^^^2.16.840.1.113883.3.239.23.4&2.16.840.1.113883.3.239.23.4.101.1^AN|SP|||||||||||||||||||RVC||ADM|||20120619111300.0000-0400||||||||4095^Lakeridge Health^Provider Unavailable^^^^^^2.16.840.1.113883.4.347^^^^XX\r\n" + 
    			"PV2||AP^ACUTE PRIVATE|NEWBORN - TEST ESB AUG 31|||||||4|73|UNKNOWN|||||||||||||R|||||||||||N||C^CARRIED\r\n" + 
    			"AL1||DA|NKA^NO KNOWN ALLERGIES^^NO KNOWN ALLERGIES|U||20120619\r\n" + 
    			"IN1|\r\n" + 
    			"ROL|1|AD|PP|4095^Lakeridge Health^Provider Unavailable^^^^^^2.16.840.1.113883.4.347^^^^XX\r\n" + 
    			"ROL|2|AD|PP|68126^PACE^KENNETH^TONY^^^^^2.16.840.1.113883.4.347^^^^XX\r\n" + 
    			"ROL|3|AD|PP|59615^D'ANGELO^ANTHONY^JAMES^^^^^2.16.840.1.113883.4.347^^^^XX\r\n" + 
    			"ROL|4|AD|PP|64065^OANDASAN^IVY^FELICIDAD^^^^^2.16.840.1.113883.4.347^^^^XX\r\n" + 
    			"ROL|5|AD|PP|42959^AKBARALI^NURJEHAN^^^^^^2.16.840.1.113883.4.347^^^^XX\r\n" + 
    			"ROL|6|AD|PP|59607^O'BRIEN^SHARON^ROSE^^^^^2.16.840.1.113883.4.347^^^^XX~66851^ODA^JENNY^WADIE NASSIEF^^^^^2.16.840.1.113883.4.347^^^^XX\r\n" + 
    			"\r\n" + 
    			"MSH|^~\\&|2.16.840.1.113883.3.239.23.4^2.16.840.1.113883.3.239.23.4.101.1|RVC|cGTA|cGTA|20120905161651.8240-0400|32986342953eee|ADT^A08|ADT-ITS.1.14111|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r\n" + 
    			"EVN||20120831153400.0000-0400||||20120619111300.0000-0400\r\n" + 
    			"PID|1||Y0000550^^^2.16.840.1.113883.3.239.23.4&2.16.840.1.113883.3.239.23.4.101.1^MR~Y494^^^2.16.840.1.113883.3.239.23.4&2.16.840.1.113883.3.239.23.4.101.1^PI~9^^^^JHN^^^^CAN&Canada&HL70363||ECHN^IDEA^^^^^L~NONE^NONE^^^^^M|IDEAMOM^^^^^^U|20120618000000.0000-0400|F|||999 ROUGE VALLY ROAD - AUG 20^OTHER - UPDATE AUG 20^TORONTO^CANON^M1B 1A1^^H^^1811||^PRN~^OTH|^WPN||S|ANASW|YA00117/12|9\r\n" + 
    			"NK1|1|ECHNMOM^IDEAMOM^^^^^U|MTH|123 ROUGE VALLY ROAD UPDATE^OTHER^TORONTO^CANON^M1B 1A1^^H|||NOK\r\n" + 
    			"NK1|2|ECHNDAD^IDEADAD^^^^^U|FTH|123 ROUGE VALLY ROAD UPDATE^OTHER^TORONTO^CANON^M1B 1A1^^H|||NOT\r\n" + 
    			"NK1|3|UNKNOWN,UNKNOWN^^^^^^U|UNK|UNKNOWN^UNKNOWN^UNKNOWN^CANON^M1B 1A1^^H|||EMP||||||UNKNOWN,UNKNOWN\r\n" + 
    			"PV1|1|I|YCCU^Y/OFCCU^3^2.16.840.1.113883.3.239.23.4&2.16.840.1.113883.3.239.23.4.100.2|R|||59615^D'ANGELO^ANTHONY^JAMES^^^^^2.16.840.1.113883.4.347^^^^XX|64065^OANDASAN^IVY^FELICIDAD^^^^^2.16.840.1.113883.4.347^^^^XX|59607^O'BRIEN^SHARON^ROSE^^^^^2.16.840.1.113883.4.347^^^^XX~66851^ODA^JENNY^WADIE NASSIEF^^^^^2.16.840.1.113883.4.347^^^^XX|ALC-FUNC||||01|||42959^AKBARALI^NURJEHAN^^^^^^2.16.840.1.113883.4.347^^^^XX|IN|YA00117/12^^^2.16.840.1.113883.3.239.23.4&2.16.840.1.113883.3.239.23.4.101.1^AN|SP|||||||||||||||||||RVC||ADM|||20120619111300.0000-0400||||||||4095^Lakeridge Health^Provider Unavailable^^^^^^2.16.840.1.113883.4.347^^^^XX\r\n" + 
    			"PV2||AP^ACUTE PRIVATE|NEWBORN - TEST ESB AUG 31|||||||4|73|UNKNOWN|||||||||||||R|||||||||||N||C^CARRIED\r\n" + 
    			"AL1||DA|NKA^NO KNOWN ALLERGIES^^NO KNOWN ALLERGIES|U||20120619\r\n" + 
    			"IN1|\r\n" + 
    			"ROL|1|AD|PP|4095^Lakeridge Health^Provider Unavailable^^^^^^2.16.840.1.113883.4.347^^^^XX\r\n" + 
    			"ROL|2|AD|PP|68126^PACE^KENNETH^TONY^^^^^2.16.840.1.113883.4.347^^^^XX\r\n" + 
    			"ROL|3|AD|PP|59615^D'ANGELO^ANTHONY^JAMES^^^^^2.16.840.1.113883.4.347^^^^XX\r\n" + 
    			"ROL|4|AD|PP|64065^OANDASAN^IVY^FELICIDAD^^^^^2.16.840.1.113883.4.347^^^^XX\r\n" + 
    			"ROL|5|AD|PP|42959^AKBARALI^NURJEHAN^^^^^^2.16.840.1.113883.4.347^^^^XX\r\n" + 
    			"ROL|6|AD|PP|59607^O'BRIEN^SHARON^ROSE^^^^^2.16.840.1.113883.4.347^^^^XX~66851^ODA^JENNY^WADIE NASSIEF^^^^^2.16.840.1.113883.4.347^^^^XX\r\n" + 
    			"";
    	Hl7InputStreamMessageStringIterator iter = new Hl7InputStreamMessageStringIterator(new StringReader(firstMessages));
    	while (iter.hasNext()) {
            ADT_A01 a01 = new ADT_A01();
            a01.parse(iter.next());
    		Persister.persist(new Converter().convertPatientWithVisits(a01));
    	}
    	
    	String message1 = "MSH|^~\\&|2.16.840.1.113883.3.239.23.4^2.16.840.1.113883.3.239.23.4.101.1|RVC|cGTA|cGTA|20120905161651.2770-0400|32986342953eee|ADT^A08|ADT-ITS.1.14109|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r" + 
    			"EVN||20120830122000.0000-0400||||20120619111300.0000-0400\r" + 
    			"PID|1||Y0000550^^^2.16.840.1.113883.3.239.23.4&2.16.840.1.113883.3.239.23.4.101.1^MR~Y494^^^2.16.840.1.113883.3.239.23.4&2.16.840.1.113883.3.239.23.4.101.1^PI~9^^^^JHN^^^^CAN&Canada&HL70363||ECHN^IDEA^^^^^L~NONE^NONE^^^^^M|IDEAMOM^^^^^^U|20120618000000.0000-0400|F|||999 ROUGE VALLY ROAD - AUG 20^OTHER - UPDATE AUG 20^TORONTO^CANON^M1B 1A1^^H^^1811||^PRN~^OTH|^WPN||S|ANASW|YA00117/12|9\r" + 
    			"NK1|1|ECHNMOM^IDEAMOM^^^^^U|MTH|123 ROUGE VALLY ROAD UPDATE^OTHER^TORONTO^CANON^M1B 1A1^^H|||NOK\r" + 
    			"NK1|2|ECHNDAD^IDEADAD^^^^^U|FTH|123 ROUGE VALLY ROAD UPDATE^OTHER^TORONTO^CANON^M1B 1A1^^H|||NOT\r" + 
    			"NK1|3|UNKNOWN,UNKNOWN^^^^^^U|UNK|UNKNOWN^UNKNOWN^UNKNOWN^CANON^M1B 1A1^^H|||EMP||||||UNKNOWN,UNKNOWN\r" + 
    			"PV1|1|I|YCCU^Y/OFCCU^3^2.16.840.1.113883.3.239.23.4&2.16.840.1.113883.3.239.23.4.100.2|U|||59615^D'ANGELO^ANTHONY^JAMES^^^^^2.16.840.1.113883.4.347^^^^XX|64065^OANDASAN^IVY^FELICIDAD^^^^^2.16.840.1.113883.4.347^^^^XX|59607^O'BRIEN^SHARON^ROSE^^^^^2.16.840.1.113883.4.347^^^^XX~66851^ODA^JENNY^WADIE NASSIEF^^^^^2.16.840.1.113883.4.347^^^^XX|ALC-FUNC||||01|||42959^AKBARALI^NURJEHAN^^^^^^2.16.840.1.113883.4.347^^^^XX|IN|YA00117/12^^^2.16.840.1.113883.3.239.23.4&2.16.840.1.113883.3.239.23.4.101.1^AN|SP|||||||||||||||||||RVC||ADM|||20120619111300.0000-0400||||||||4095^Lakeridge Health^Provider Unavailable^^^^^^2.16.840.1.113883.4.347^^^^XX\r" + 
    			"PV2||AP^ACUTE PRIVATE|NEWBORN|||||||3|72|NONE|||||||||||||U|||||||||||N||OTH^OTHER\r" + 
    			"AL1||DA|NKA^NO KNOWN ALLERGIES^^NO KNOWN ALLERGIES|U||20120619\r" + 
    			"IN1|||\r" + 
    			"ROL|1|AD|PP|4095^Lakeridge Health^Provider Unavailable^^^^^^2.16.840.1.113883.4.347^^^^XX\r" + 
    			"ROL|2|AD|PP|68126^PACE^KENNETH^TONY^^^^^2.16.840.1.113883.4.347^^^^XX\r" + 
    			"ROL|3|AD|PP|59615^D'ANGELO^ANTHONY^JAMES^^^^^2.16.840.1.113883.4.347^^^^XX\r" + 
    			"ROL|4|AD|PP|64065^OANDASAN^IVY^FELICIDAD^^^^^2.16.840.1.113883.4.347^^^^XX\r" + 
    			"ROL|5|AD|PP|42959^AKBARALI^NURJEHAN^^^^^^2.16.840.1.113883.4.347^^^^XX\r" + 
    			"ROL|6|AD|PP|59607^O'BRIEN^SHARON^ROSE^^^^^2.16.840.1.113883.4.347^^^^XX~66851^ODA^JENNY^WADIE NASSIEF^^^^^2.16.840.1.113883.4.347^^^^XX\r" + 
    			""; 
    	
        ADT_A01 a01 = new ADT_A01();
        a01.parse(message1);
        
		Persister.persist(new Converter().convertPatientWithVisits(a01));

    }
    
    
    
    @After
    public void after() {
        Persister.setUnitTestMode(false);
    } 	

}
