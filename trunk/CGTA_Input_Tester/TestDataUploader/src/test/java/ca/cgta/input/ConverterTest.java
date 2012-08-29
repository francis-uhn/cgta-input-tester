package ca.cgta.input;

import static org.junit.Assert.*;

import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.cgta.input.converter.Converter;
import ca.cgta.input.model.outer.ClinicalDocumentGroup;
import ca.cgta.input.model.outer.PatientWithVisits;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v25.message.ADT_A01;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.parser.EncodingNotSupportedException;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.ValidationContextImpl;

import com.google.gson.GsonBuilder;

public class ConverterTest {

	private Logger ourLog = LoggerFactory.getLogger(ConverterTest.class);
	
	@Test
	public void testJoinTextualObxSegments() throws EncodingNotSupportedException, HL7Exception, JAXBException {
		
		String message = "MSH|^~\\&|ULTRA|TML|OLIS|OLIS|200905011130||ORU^R01^ORU_R01|20169838|T|2.5\r" + 
				"EVN||200905011130\r" + 
				"PID|1||7005728^^^2.16.840.1.113883.3.59.3:0947&QCPR^MR~00000000000^AA^^^JHN^^^^CANON&Ontario&HL70363~A93745H^^^^PPN^^^^CAN&&HL70363||Smith^Joseph^John^Junior^Mr^MD^L~Smith^Joe^^^^^A|Blanche^^^^^^L|19310313|M|||26 RIVINGTON AVE^^Goderich^ON^N7A3Y2^CAN^H~7 WOODPLUMPTON ROAD^^Port Stanley^ON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com|||||||||||||||||N\r" + 
				"PV1|1|I|JS12^123^4^2.16.840.1.113883.3.59.1:4197^^^Q Building^12^JS12-123-4 GIM Inpatient Unit||||38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&2.16.840.1.113883.3.59.3:0947&QCPR|38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&2.16.840.1.113883.3.59.3:0947&QCPR|38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&2.16.840.1.113883.3.59.3:0947&QCPR|GIM|||||||38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&2.16.840.1.113883.3.59.3:0947&QCPR||284675^^^2.16.840.1.113883.3.59.3:0947 &QCPR^VN\r" + 
				"ORC|1|||7777^2.16.840.1.113883.3.59.3:0947^QCPR\r" + 
				"OBR|1|7777^2.16.840.1.113883.3.59.3:0947^QCPR||50111^OR/Procedure Note^1.3.6.1.4.1.12201.1.1.1.0001|||20110126124300-0500|||||||||||N|||||||F|||||||5555&Smith&John&&&&Dr&&1.3.6.1.4.1.12201.1.2.1.5&2.16.840.1.113883.3.59.3:0947&QCPR\r" + 
				"OBX|1|DT|10017^Date Dictated^1.3.6.1.4.1.12201.1.1.1.0002||20120112||||||F\r" + 
				"OBX|2|ST|1126527^Dictated by^1.3.6.1.4.1.12201.1.1.1.0002||John Smith, MD||||||F\r" + 
				"OBX|3|NM|14002^Blank Copies^1.3.6.1.4.1.12201.1.1.1.0002||0||||||F\r" + 
				"OBX|4|TX|14001^Medical Records Report^1.3.6.1.4.1.12201.1.1.1.0002|1|PROCEDURE: Right axillary node dissection. ||||||F\r" + 
				"OBX|5|TX|14001^Medical Records Report^1.3.6.1.4.1.12201.1.1.1.0002|2|DATE OF PROCEDURE: January 12, 2011 ||||||F\r" + 
				"OBX|6|TX|14001^Medical Records Report^1.3.6.1.4.1.12201.1.1.1.0002|3|SURGEON: Dr. John Smith||||||F\r" + 
				"OBX|7|TX|14001^Medical Records Report^1.3.6.1.4.1.12201.1.1.1.0002|4|CLINICAL NOTE: Mr Jones is a 1000 year old man who was diagnosed with ||||||F\r" + 
				"OBX|8|TX|14001^Medical Records Report^1.3.6.1.4.1.12201.1.1.1.0002|5|several problems. The patient underwent a procedure.||||||F\r";
		
		PipeParser parser = new PipeParser();
		parser.setValidationContext(new ValidationContextImpl());
		ORU_R01 hl7Msg = (ORU_R01) parser.parse(message);
		
		List<ClinicalDocumentGroup> converted = new Converter().convertClinicalDocument(hl7Msg);
		
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.setPrettyPrinting();
		gsonBuilder.disableHtmlEscaping();
		String gson = gsonBuilder.create().toJson(converted);
		
		ourLog.info("GSON: " + gson);
	}
	
	
	/**
	 * This crashed the converter
	 */
	@Test
	public void testBad9008FromSMH() throws EncodingNotSupportedException, HL7Exception, JAXBException {
		
		String message = "MSH|^~\\&|2.16.840.1.113883.3.239.23.5^2.16.840.1.113883.3.239.23.5.101.5|SMH^2.16.840.1.113883.3.239.23.5.100.1|ConnectingGTA|ConnectingGTA|20120622151416-0400||ADT^A01^ADT_A01|00000000018079030|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r\n" + 
				"EVN||20120622151413\r\n" + 
				"PID|1||4003304^^^2.16.840.1.113883.3.239.23.5&2.16.840.1.113883.3.239.23.5.101.5^MR^^^~2898989898^TT^^2.16.840.1.113883.3.239.23.5&2.16.840.1.113883.3.239.23.5.101.5^JHN^^^^CANON&&HL70363~00270021645^^^2.16.840.1.113883.3.239.23.5&2.16.840.1.113883.3.239.23.5.101.5^AN^^^||CGTA-SMH^INPATIENT^^^^^L|CGTA-SMH^^^^^^L|19650512-0400|M|||16 SIMPLE RD^^TORONTO^CANON^M9I8U7^CAN^H||(555)888-7777^PRN^PH^^^555^8887777||ENG^^HL70296|||||||||||||||N\r\n" + 
				"NK1|1|SMITH^TERESA^^^MS^^L|SIS|33 FINCH AVE^^DARTMOUTH^CANON^S8T 9K9^CANADA^H|(402)565-3201^PRN^PH^^^402^5653201\r\n" + 
				"PV1|1|I|2B^286B^2^2.16.840.1.113883.3.239.23.5&2.16.840.1.113883.3.239.23.5.100.1|L|||21892^GRAHAM^ANTHONY^F^^^^^&2.16.840.1.113883.3.239.23.5&2.16.840.1.113883.3.239.23.5.101.5|||15|||||||21892^^ANTHONY^F^^^^^&2.16.840.1.113883.3.239.23.5&2.16.840.1.113883.3.239.23.5.101.5||00270021645^^^2.16.840.1.113883.3.239.23.5&2.16.840.1.113883.3.239.23.5.101.5^AN|||||||||||||||||||||||||201206221513\r\n" + 
				"DG1|1||^UNSTABLE ANGINA";

		System.out.println(message);
		
		PipeParser parser = new PipeParser();
		parser.setValidationContext(new ValidationContextImpl());
		ADT_A01 hl7Msg = (ADT_A01) parser.parse(message);
		
		PatientWithVisits converted = new Converter().convertPatientWithVisits(hl7Msg);
		
	}
	
	
	@Test
	public void testADTMessageWithNoMrn() throws EncodingNotSupportedException, HL7Exception, JAXBException {
		
		String message = "MSH|^~\\&|1.3.6.1.4.1.12201|G^2.16.840.1.113883.3.59.3:947^L|||20111202162100-0500||ADT^A01^ADT_A01|123484|T|2.5|||NE|AL|CAN|8859/1\r\n" + 
				"EVN|A01|201112021621||||201112021621|G^4265^L\r\n" + 
				"PID|1||^^^2.16.840.1.113883.3.59.3:0947&1.3.6.1.4.1.12201.1^MR~9287170261^BL^^^JHN^^^^CANON&&HL70363^20111201||Test^Majaconversion^^^Mrs.^^L~Test^Maj^^^Mrs.^^A||19731230000000-0500|F|||1 Bloor Street ^^Toronto^CANON^L9K 8J7^Can^H|12333|(415)222-3333^PRN^PH||eng^English^HL70296|||||||||||||||N|||201112221537\r\n" + 
				"PD1|||UHN^D^^^^UHN^FI|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI|||||||N^no special privacy^03ZPrvyFlg|N\r\n" + 
				"ROL|||PP^Primary Care Provider^15ZRole|13546^Generic^Physician^Moe^^Dr.^MD^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.1|201111071338||||ET02^Physician^15ZEmpTyp|O^Office|^^^CANON^^Can^B|(416) 340-3391^WPN^PH\r\n" + 
				"ROL|||PP^Primary Care Provider^15ZRole|13546b^Genericb^Physicianb^Moeb^^Dr.^MD^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.1|201111071338||||ET02^Physician^15ZEmpTyp|1^Hospital^15ZOrgTyp|^^^CANON^^Can^B|(416) 340-3388^WPN^PH\r\n" + 
				"NK1|1|Wph^Mom^^^^^L|PAR^Parent^03ZRelshp|82 Buttonwood Avenue^^YORK^CANON^M6M 2J5^Can^H|(416)243-3600^PRN^PH~(416)123-1234^PRN^CP|(416)243-3600^PRN^PH|N^Next-of-Kin^03ZConRol\r\n" + 
				"NK1|2|Wph^Brother^^^^^L|BRO^Brother^03ZRelshp|83 Buttonwood Avenue^^YORK^CANON^M6M 2J5^Can^H|(416)243-3601^PRN^PH|(416)525-2525^PRN^PH|C^Emergency Contact^03ZConRol\r\n" + 
				"PV1|1|I|PMH 15C^413^2^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.3.1|C|||13546a^Generic^Physician^MoeA^^Dr.^MD^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.1|13546b^Generic^Physician^MoeB^^Dr.^MD^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.1||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.1|IP|11110000514^^^2.16.840.1.113883.3.59.3:0947&1.3.6.1.4.1.12201.1^VN||||N|||||||||||||||||||||20111202162100-0500|||||||V\r\n" + 
				"PV2||S^Semi^03ZFinbed|^kfkfkfjcjcjcgcgcclcl^03ZAmitRes|||||||||||||||||||N||AI|Elective||||||N|||||||OTH^Self^03ZBrInBy\r\n" + 
				"DG1|1||06^KFKFKFJCJCJCGCGCCLCL^2.16.840.1.113883.11.19436|KFKFKFJCJCJCGCGCCLCL|201112021621|A|||||||||1||D";
		PipeParser parser = new PipeParser();
		parser.setValidationContext(new ValidationContextImpl());
		ADT_A01 hl7Msg = (ADT_A01) parser.parse(message);
		
		PatientWithVisits converted = new Converter().convertPatientWithVisits(hl7Msg);
		assertEquals(null, converted);
		
		
	}
	
}
