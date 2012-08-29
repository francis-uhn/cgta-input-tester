package ca.cgta.input;

import java.io.IOException;
import java.util.List;

import org.ektorp.ViewQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ca.cgta.couchdb.tools.AllDataPurger;
import ca.cgta.couchdb.tools.ViewUploader;
import ca.cgta.input.listener.Persister;
import ca.cgta.input.model.inner.Visit;
import ca.cgta.input.model.outer.PatientWithVisits;
import ca.cgta.input.model.outer.PatientWithVisitsContainer;

import static org.junit.Assert.*;

public class ScenariosTest {

	@Before
	public void before() throws IOException, Exception {
		Persister.setUnitTestMode(true);
		AllDataPurger.purgeAllData(); 
		ViewUploader.uploadAllViews();
		
	}
	
	@Test
	public void testAdtA01() throws Exception {
		
		String message = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A01^ADT_A01|123484|T^|2.5^^||||||CAN||||\r\n" + 
				"EVN|A01|201112021621||||201112021621|G^4265^L\r\n" + 
				"PID|||7012671^^^UHN^MR^^^^^^^~1561561562^^^CANON^JHN^^^^^^^~HN2825^^^UHN^PI^^^^^^^||Pp^Pmh^^^Miss^^L^^^^^201112021621^^~||19600312|F|||100 college st^^NORTH YORK^ON^M5N 1X1^Can^H^^^^^^^~|1811|(416)946-4501^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112021621||||||\r\n" + 
				"PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r\n" + 
				"ROL||UC|PP^Primary Care Provider^15ZRole^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^^^^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|O^Office^^^^|^^^ON^^Can^^^^^^^^|(416) 340-3391^WPN^PH^^^^^^^^^\r\n" + 
				"PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546^Generic^Physician^Moe^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||||||D|||13546^Generic^Physician^Moe^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000514^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||201112021621|||||||V|\r\n" + 
				"PV2||S^Semi^03ZFinbed^^^|^kfkfkfjcjcjcgcgcclcl^03ZAmitRes^^^|||||||||||||||||||N||AI|Elective||||||N|||||||OTH^Self^03ZBrInBy^^^|||||||||||\r\n" + 
				"ROL||UC|AT^Attending Physician^15ZRole^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^2.16.840.1.113883.3.59.3:947^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|1^Hospital^15ZOrgTyp^^^|^^^ON^^Can^^^^^^^^|(416) 340-3391^WPN^PH^^^^^4163403391^^^^\r\n" + 
				"ROL||UC|AD^Admitting Physician^15ZRole^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^2.16.840.1.113883.3.59.3:947^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|1^Hospital^^^^|^^^ON^^Can^^^^^^^^|(416) 340-3391^WPN^PH^^^^^^^^^\r\n" + 
				"ROL||UC|RP^Referring Physician^15ZRole^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^2.16.840.1.113883.3.59.3:947^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|1^Hospital^15ZOrgTyp^^^|^^^ON^^Can^^^^^^^^|(416) 340-3391^WPN^PH^^^^^^^^^\r\n" + 
				"DG1|1||^KFKFKFJCJCJCGCGCCLCL^^^|KFKFKFJCJCJCGCGCCLCL|201112021621|A|||||||||1||D||||\r\n" + 
				"PR1||||||||||||||||||||\r\n" + 
				"ZPV|OTH^Self|F^Standard|||N|||13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||Medical Services^General Internal Medicine^GMED|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|413||3910||^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r\n" + 
				"ZWA||||||||active|\r\n";
		PatientWithVisits converted = UhnConverter.convertAdtOrFail(message);
		Persister.persist(converted);
		
		ViewQuery query = new ViewQuery().viewName("allVisits").designDocId("_design/application");
		List<PatientWithVisitsContainer> pwvContainers = Persister.getConnector().queryView(query, PatientWithVisitsContainer.class);
		
		PatientWithVisitsContainer visitContainer = pwvContainers.get(0);
		Visit visit = visitContainer.getDocument().myVisits.get(0);
		
		assertEquals("11110000514", visit.myVisitNumber.myIdNumber);
		assertEquals("I", visit.myPatientClassCode);
		
	}
	
	@After
	public void after() {
		Persister.setUnitTestMode(false);
	} 

}
