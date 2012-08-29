package ca.cgta.input.converter;

import static org.junit.Assert.*;

import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.cgta.input.model.inner.ClinicalDocumentData;
import ca.cgta.input.model.inner.ClinicalDocumentSection;
import ca.cgta.input.model.outer.ClinicalDocumentGroup;
import ca.cgta.input.model.outer.PatientWithVisits;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v25.message.ADT_A01;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;

public class ConverterTest {

	private static final Logger ourLog = LoggerFactory.getLogger(ConverterTest.class);
	
	@Test
	public void testValidateTsUpToMinute() throws JAXBException {
		
		assertTrue(new Converter().validateTsWithAtLeastMinutePrecisionAndAddFailure("", "201201010000-0400"));
		assertTrue(new Converter().validateTsWithAtLeastMinutePrecisionAndAddFailure("", "20120101000000-0400"));
		assertTrue(new Converter().validateTsWithAtLeastMinutePrecisionAndAddFailure("", "20120101000000.0-0400"));
		assertTrue(new Converter().validateTsWithAtLeastMinutePrecisionAndAddFailure("", "20120101000000.00-0400"));
		assertTrue(new Converter().validateTsWithAtLeastMinutePrecisionAndAddFailure("", "20120101000000.000-0400"));
		assertTrue(new Converter().validateTsWithAtLeastMinutePrecisionAndAddFailure("", "20120101000000.0000-0400"));
		
		assertFalse(new Converter().validateTsWithAtLeastMinutePrecisionAndAddFailure("", "20120101000000.00000-0400"));
		assertFalse(new Converter().validateTsWithAtLeastMinutePrecisionAndAddFailure("", "20120101000000.-0400"));
		assertFalse(new Converter().validateTsWithAtLeastMinutePrecisionAndAddFailure("", "20120101000"));
		assertFalse(new Converter().validateTsWithAtLeastMinutePrecisionAndAddFailure("", "2012010100"));
		assertFalse(new Converter().validateTsWithAtLeastMinutePrecisionAndAddFailure("", "201201010"));
		assertFalse(new Converter().validateTsWithAtLeastMinutePrecisionAndAddFailure("", "20120101"));
		assertFalse(new Converter().validateTsWithAtLeastMinutePrecisionAndAddFailure("", "2012010"));
		assertFalse(new Converter().validateTsWithAtLeastMinutePrecisionAndAddFailure("", "201201"));
		assertFalse(new Converter().validateTsWithAtLeastMinutePrecisionAndAddFailure("", "20120"));
		assertFalse(new Converter().validateTsWithAtLeastMinutePrecisionAndAddFailure("", "2012"));
		assertFalse(new Converter().validateTsWithAtLeastMinutePrecisionAndAddFailure("", "201"));
		assertFalse(new Converter().validateTsWithAtLeastMinutePrecisionAndAddFailure("", "20"));
		assertFalse(new Converter().validateTsWithAtLeastMinutePrecisionAndAddFailure("", "2"));
		assertFalse(new Converter().validateTsWithAtLeastMinutePrecisionAndAddFailure("", ""));
	}

	@Test
	public void testToNumber() throws Exception {
		
		Converter c = new Converter();
		int value = c.toNumber("TP", "1");
		assertEquals(1, value);
		assertTrue(c.getFailures().isEmpty());
		
		c = new Converter();
		value = c.toNumber("TP", "0");
		assertEquals(0, value);
		assertTrue(c.getFailures().isEmpty());

		c = new Converter();
		value = c.toNumber("TP", "999");
		assertEquals(999, value);
		assertTrue(c.getFailures().isEmpty());

		c = new Converter();
		value = c.toNumber("TP", "99JAMES");
		assertEquals(0, value);
		assertTrue(c.getFailures().size() == 1);
		assertTrue(c.getFailures().get(0).getFailureCode() == FailureCode.F019);
		
	}
	
	@Test
	public void testToNumberDecimal() throws Exception {
		
		Converter c = new Converter();
		double value = c.toNumberDecimal("TP", "1");
		assertEquals(1.0, value, 0.0001);
		assertTrue(c.getFailures().isEmpty());
		
		c = new Converter();
		value = c.toNumberDecimal("TP", "1.0");
		assertEquals(1.0, value, 0.0001);
		assertTrue(c.getFailures().isEmpty());

		c = new Converter();
		value = c.toNumberDecimal("TP", "1.00000000000000000000001");
		assertEquals(1.0, value, 0.0001);
		assertTrue(c.getFailures().isEmpty());

		c = new Converter();
		value = c.toNumberDecimal("TP", "0");
		assertEquals(0.0, value, 0.0001);
		assertTrue(c.getFailures().isEmpty());

		c = new Converter();
		value = c.toNumberDecimal("TP", "999");
		assertEquals(999, value, 0.0001);
		assertTrue(c.getFailures().isEmpty());

		c = new Converter();
		value = c.toNumberDecimal("TP", "999.99");
		assertEquals(999.99, value, 0.0001);
		assertTrue(c.getFailures().isEmpty());

		c = new Converter();
		value = c.toNumberDecimal("TP", "99JAMES");
		assertEquals(0, value, 0.0001);
		assertTrue(c.getFailures().size() == 1);
		assertTrue(c.getFailures().toString(), c.getFailures().get(0).getFailureCode() == FailureCode.F131);
		
	}

	
	//@Test
	public void testBrCharacters() throws HL7Exception, JAXBException {
		
		String input = "MSH|^~\\&|2.16.840.1.113883.3.239.23.8^2.16.840.1.113883.3.239.23.8.101.1|1330^HSP_Assigned|ConnectingGTA|ConnectingGTA|20120713101438-0400|38272347521144bbb|ORU^R01^ORU_R01|Q117384795T106786317|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r" + 
				"PID|1||1010101010^GH^^^JHN^^^^CANON&&HL70363~4010583^^^2.16.840.1.113883.3.239.23.8&2.16.840.1.113883.3.239.23.8.101.1^MR||CGTA^TESTD^^^MRS.^^L||19620910000000-0400|F|||15 QUAKER CRT^^TORONTO^CANON^K8U7J6^CA^H||(416)555-2828^PRN||EN^^HL70296||DEC|4111148^^^^FINNBR|||||||0\r" + 
				"PV1|1|E|EMG^EED^^2.16.840.1.113883.3.239.23.8&2.16.840.1.113883.3.239.23.8.100.1^^^NYGH|AER||||||ER||||HOME|||||2012000492^^^2.16.840.1.113883.3.239.23.8&2.16.840.1.113883.3.239.23.8.101.1^VN||||||||||||||||||||NY||AC|||20120713090300-0400\r" + 
				"ORC|RE\r" + 
				"OBR|1|167837841^2.16.840.1.113883.3.239.23.8^2.16.840.1.113883.3.239.23.8.101.1|^2.16.840.1.113883.3.239.23.8^2.16.840.1.113883.3.239.23.8.101.1|1859^US ABDOMEN^2.16.840.1.113883.3.239.23.8.102.2|||20120713095926-0400|20120713095926-0400|||||||CD:1397&RAD TYPE|478^IU^SIMON^^^^^^1.3.6.1.4.1.12201.1.2.1.5&2.16.840.1.113883.3.239.23.8&2.16.840.1.113883.3.239.23.8.101.1||||^HNA_ACCN~3936858^HNA_ACCNID~3905579^HNA_PACSID||20120713095927||RAD|F|||||CD:11346|^DOE RADNET TRANSCRIPTION\r" + 
				"OBX|1|FT|CD:20809^US REPORT^2.16.840.1.113883.3.239.23.8.102.3||US ABDOMEN\\.BR\\\\.BR\\\\.BR\\THE FOLLOWING ORDER WAS PLACED VIA DEPARTMENT ORDER ENTRY AND FINALED BY THE TECHNOLOGIST. THE REPORT IS TYPED IN RADNET TRANSCRIPTION AND SIGNED OUT VIA CASE SIGN OUT.||||||F|||20120713101430||9006^BASS^ARTHUR\r" + 
				"NTE|1|P|TRANSCRIBED ON  :  13-JUL-2012             REPORT BY: BASS, ARTHUR, MD FRCPC\\.BR\\TRANSCRIPTIONIST:  MBC                           SIGNED BY: BASS, ARTHUR, MD FRCPC                                13-JUL-2012\\.BR\\\\.BR\\THIS REPORT IS GENERATED USING VOICE RECOGNITION TECHNOLOGY. IF YOU HAVE ANY CONCERNS REGARDING THE\\.BR\\ACCURACY OF THIS REPORT, PLEASE CALL 416-756-6186.";
		
		ORU_R01 msg = new ORU_R01();
		msg.parse(input);
		
		List<ClinicalDocumentGroup> docs = new Converter().convertClinicalDocument(msg);
		ClinicalDocumentGroup doc = docs.get(0);
		
		ClinicalDocumentSection sec1 = doc.mySections.get(0);
		ClinicalDocumentData data = sec1.myData.get(0);
		
		String value = data.myValue;
		Assert.assertTrue(value, value.startsWith("US ABDOMEN<br><br><br>THE"));
		
	}
	
	@Test
	public void testIam31isNotRequired() throws Exception {
		
		String inputMessage = "MSH|^~\\&|2.16.840.1.113883.3.239.23.7^2.16.840.1.113883.3.239.23.7.101.1|SHSC|ConnectingGTA|ConnectingGTA|20120720105156-0500||ADT^A60^ADT_A60|          79|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r" + 
				"EVN||20120620104423-0500|||\r" + 
				"PID|1||7070701^^^2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.1^MR||ONE^TEST^A^^^^L||19500315000000-0500|M||||||||||||||\r" + 
				"PV1|1|I|||||||||||||||||22211A^^^2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.1^VN|||||||||||||||||||||||||||||||\r" + 
				"IAM|1||^ASA^2.16.840.1.113883.3.239.23.7.102.1||HIVES||||||||20120620000000-0500||\r" + 
				"IAM|2||^LATEX^2.16.840.1.113883.3.239.23.7.102.1||HIVES||||||||20120620000000-0500||\r" + 
				"IAM|3||^CODEINE^2.16.840.1.113883.3.239.23.7.102.1||INTOLERANCE - REFLUX||||||||20120620000000-0500||\r" + 
				"IAM|4||000476^PENICILLINS^2.16.840.1.113883.3.239.23.7.102.1||HIVES||||||||20120620000000-0500||\r" + 
				"IAM|5||^FISH^2.16.840.1.113883.3.239.23.7.102.1||||||||||20120620000000-0500||\r";
		
		ADT_A01 adt = new ADT_A01();
		adt.parse(inputMessage);
		Converter converter = new Converter(false);
		PatientWithVisits pwv = converter.convertPatientWithVisits(adt);
		
		List<Failure> failures = converter.getFailures();
		ourLog.info("Failures was: " + failures);
		
		for (Failure failure : failures) {
	        if (failure.getFailureCode() == FailureCode.F103) {
	        	fail(failure.toString());
	        }
        }

		inputMessage = "MSH|^~\\&|2.16.840.1.113883.3.239.23.7^2.16.840.1.113883.3.239.23.7.101.1|SHSC|ConnectingGTA|ConnectingGTA|20120720105156-0500||ADT^A60^ADT_A60|          79|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r" + 
				"EVN||20120620104423-0500|||\r" + 
				"PID|1||7070701^^^2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.1^MR||ONE^TEST^A^^^^L||19500315000000-0500|M||||||||||||||\r" + 
				"PV1|1|I|||||||||||||||||22211A^^^2.16.840.1.113883.3.239.23.7&2.16.840.1.113883.3.239.23.7.101.1^VN|||||||||||||||||||||||||||||||\r" + 
				"IAM|1||^ASA^2.16.840.1.113883.3.239.23.7.102.1||HIVES||||||||20120620000000-0500||\r" + 
				"IAM|2||^LATEX^2.16.840.1.113883.3.239.23.7.102.1||HIVES||||||||20120620000000-0500||\r" + 
				"IAM|3||^^2.16.840.1.113883.3.239.23.7.102.1||INTOLERANCE - REFLUX||||||||20120620000000-0500||\r" + 
				"IAM|4||000476^PENICILLINS^2.16.840.1.113883.3.239.23.7.102.1||HIVES||||||||20120620000000-0500||\r" + 
				"IAM|5||^FISH^2.16.840.1.113883.3.239.23.7.102.1||||||||||20120620000000-0500||\r";
		
		adt = new ADT_A01();
		adt.parse(inputMessage);
		converter = new Converter(false);
		pwv = converter.convertPatientWithVisits(adt);
		
		failures = converter.getFailures();
		ourLog.info("Failures was: " + failures);
		
		boolean found = false;
		for (Failure failure : failures) {
	        if (failure.getFailureCode() == FailureCode.F103) {
	        	found = true;
	        }
        }
	
		assertTrue(found);
	}
	
	
	@Test
	public void testCanonicalProvidersAreReadCorrectly() throws HL7Exception, JAXBException {
		
		String inputMessage = "MSH|^~\\&|2.16.840.1.113883.3.239.23.9^2.16.840.1.113883.3.239.23.9.101.1|CVH^0731|cGTA|cGTA|20120817100400-0400|340975709474jjj|ADT^A01^ADT_A01|ADT-MITRA.1.4677|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r" + 
				"EVN||201208171003\r" + 
				"PID|1||0000000560^^^2.16.840.1.113883.3.239.23.9&2.16.840.1.113883.3.239.23.9.101.1^MR~2000011722^AD^^^JHN^^^^CANON&Ontario&HL70363||CVHCGTA3^MARY^^^^^L||19660221000000-0400|F|||77 OVERTHERE STREET^^OVERTHERE^CANON^L5L 3A7^Canada^H||905-777-7777^PRN^PH\r" + 
				"NK1|1|CVHCGTA3^EMERG3^^^^^L|DEP^Dependant^HL70063|77 OVERTHERE STREET^^OVERTHERE^CANON^L5L 3A7^Canada^H|905-777-7777^PRN^PH\r" + 
				"PV1|1|I|2B^2B2156^1^2.16.840.1.113883.3.239.23.9&2.16.840.1.113883.3.239.23.9.101.1|C|||65340^ABBOTT^LAURA^^^^^^2.16.840.1.113883.4.347|||ACUTE|||||||68031^AASHEIM^LISE^HOLM^^^^^2.16.840.1.113883.4.347||AC000373/12^^^2.16.840.1.113883.3.239.23.9&2.16.840.1.113883.3.239.23.9.101.1^VN|||||||||||||||||||||||||20120817100300-0400\r" + 
				"PV2|||TEST^TEST";

		ADT_A01 adt = new ADT_A01();
		adt.parse(inputMessage);
		Converter converter = new Converter(false);
		PatientWithVisits pwv = converter.convertPatientWithVisits(adt);
		
		List<Failure> failures = converter.getFailures();
		ourLog.info("Failures was: " + failures);
		
		boolean found = false;
		for (Failure failure : failures) {
	        if (failure.getFailureCode() == FailureCode.F028) {
	        	found = true;
	        }
        }
	
		assertFalse(found);

		assertEquals(1, pwv.myVisits.get(0).myAttendingDoctors.size());
		assertEquals("65340", pwv.myVisits.get(0).myAttendingDoctors.get(0).myId);
		assertTrue(pwv.myVisits.get(0).myAttendingDoctors.get(0).myIdType, pwv.myVisits.get(0).myAttendingDoctors.get(0).myIdType.contains("Licence Number"));

		
		
	}

	/**
	 * This one DOES have a badly formatted provider
	 */
	@Test
	public void testCanonicalProvidersAreReadCorrectlyV2() throws HL7Exception, JAXBException {
		
		String inputMessage = "MSH|^~\\&|2.16.840.1.113883.3.239.23.10^2.16.840.1.113883.3.239.23.10.101.1||ConnectingGTA||20120824120323-0500|2387527832459kkk|ADT^A04^ADT_A04|20120824120324035179|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r" + 
				"EVN||20120824115901-0500\r" + 
				"PID|1||0001432^^^2.16.840.1.113883.3.239.23.10&2.16.840.1.113883.3.239.23.10.101.1^MR~\"\"^^^^JHN^^^^CANON&&HL70363||BBKTEST^A^NEG^^^^L||19880128000000-0500|F|||100 QWAY WEST^\"\"^MISS^CANON^L5B 1B8^CANADA^H||(905)848-7580^PRN|^WPN|^^HL70296\r" + 
				"ROL|||PP^Primary Care Provider^HL70443|4167^ZZZ WORKLOAD^^^^^^^2.16.840.1.113883.4.347&2.16.840.1.113883.3.239.23.10&2.16.840.1.113883.3.239.23.10.101.1\r" + 
				"PV1|1|O|M-LAB^^^2.16.840.1.113883.3.239.23.10&2.16.840.1.113883.3.239.23.10.100.1||||4167^ZZZ WORKLOAD^^^^^^^2.16.840.1.113883.4.347&2.16.840.1.113883.3.239.23.10&2.16.840.1.113883.3.239.23.10.101.1|||||||||N|||LB000006/12^^^2.16.840.1.113883.3.239.23.10&2.16.840.1.113883.3.239.23.10.101.1^AN|||||||||||||||||||||||||20120824115800-0500\r" + 
				"DG1|1||^TESTING SCENARIOS";

		ADT_A01 adt = new ADT_A01();
		adt.parse(inputMessage);
		Converter converter = new Converter(false);
		PatientWithVisits pwv = converter.convertPatientWithVisits(adt);
		
		List<Failure> failures = converter.getFailures();
		ourLog.info("Failures was: " + failures);
		
		boolean found = false;
		for (Failure failure : failures) {
	        if (failure.getFailureCode() == FailureCode.F028) {
	        	found = true;
	        }
        }
	
		assertTrue(found);

	}
	
	
	
	
}
