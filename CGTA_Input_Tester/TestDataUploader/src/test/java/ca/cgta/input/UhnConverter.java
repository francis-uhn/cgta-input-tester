package ca.cgta.input;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.cgta.input.converter.Converter;
import ca.cgta.input.model.outer.ClinicalDocumentGroup;
import ca.cgta.input.model.outer.PatientWithVisits;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.model.Structure;
import ca.uhn.hl7v2.model.Varies;
import ca.uhn.hl7v2.model.v25.datatype.DT;
import ca.uhn.hl7v2.model.v25.datatype.ED;
import ca.uhn.hl7v2.model.v25.datatype.PL;
import ca.uhn.hl7v2.model.v25.datatype.ST;
import ca.uhn.hl7v2.model.v25.datatype.TS;
import ca.uhn.hl7v2.model.v25.datatype.XAD;
import ca.uhn.hl7v2.model.v25.datatype.XCN;
import ca.uhn.hl7v2.model.v25.datatype.XPN;
import ca.uhn.hl7v2.model.v25.datatype.XTN;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_OBSERVATION;
import ca.uhn.hl7v2.model.v25.message.ADT_A01;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.segment.DG1;
import ca.uhn.hl7v2.model.v25.segment.EVN;
import ca.uhn.hl7v2.model.v25.segment.IAM;
import ca.uhn.hl7v2.model.v25.segment.MRG;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.model.v25.segment.NK1;
import ca.uhn.hl7v2.model.v25.segment.OBR;
import ca.uhn.hl7v2.model.v25.segment.OBX;
import ca.uhn.hl7v2.model.v25.segment.ORC;
import ca.uhn.hl7v2.model.v25.segment.PID;
import ca.uhn.hl7v2.model.v25.segment.PV1;
import ca.uhn.hl7v2.model.v25.segment.PV2;
import ca.uhn.hl7v2.model.v25.segment.ROL;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Hl7InputStreamMessageStringIterator;
import ca.uhn.hl7v2.util.Terser;

public class UhnConverter {
	public static String ourFacility2Oid = "1.3.6.1.4.1.12201.100.2";
	public static String ourFacility1Oid = "1.3.6.1.4.1.12201.100.1";
	public static String ourResultCodingSystemOid = "1.3.6.1.4.1.12201.102.6";
	public static String ourRequestCodingSystemOid = "1.3.6.1.4.1.12201.102.5";
	public static String ourAllergenCodingSystemOid = "1.3.6.1.4.1.12201.102.1";
	public static String ourSendingSystemOid = "1.3.6.1.4.1.12201.101.1";
	public static String ourHspOid = "1.3.6.1.4.1.12201";
	
	private static final Logger ourLog = LoggerFactory.getLogger(UhnConverter.class);


	public static void main(String[] args) throws Exception {
		System.setProperty(Varies.INVALID_OBX2_TYPE_PROP, "TX");

		{
			InputStream is = UhnConverter.class.getClassLoader().getResourceAsStream("tmpadt.txt");
			Hl7InputStreamMessageStringIterator iter = new Hl7InputStreamMessageStringIterator(is);
			int index = 0;
			while (iter.hasNext()) {
				System.out.println("Converting value " + (index++));

				String next = iter.next();
				PatientWithVisits pwv = convertAdt(next);
				if (pwv == null) {
					continue;
				}

				//Persister.persist(pwv);

			}
		}

		{
			InputStream is = UhnConverter.class.getClassLoader().getResourceAsStream("tmphl7.txt");
			Hl7InputStreamMessageStringIterator iter = new Hl7InputStreamMessageStringIterator(is);
			int index = 0;
			while (iter.hasNext()) {
				System.out.println("Converting value " + (index++));

				String next = iter.next();
				List<ClinicalDocumentGroup> values = convertOru(next);
//				Persister.persist(values);

			}
		}

		
	}


	public static PatientWithVisits convertAdtOrFail(String theInputString) throws Exception {
		PatientWithVisits retVal = convertAdt(theInputString);
		if (retVal == null) {
			throw new Exception("Couldn't convert");
		}
		return retVal;
	}


	public static PatientWithVisits convertAdt(String theInputString) throws Exception {
		if (theInputString.contains("|T|2.2")) {
			return null;
		}

		if (theInputString.contains("EPR|WP") || theInputString.contains("EPR|TRI") || theInputString.contains("EPR|SJR")) {
			return null;
		}
		
		if (theInputString.contains("ADT^A14")) {
			return null;
		}
		if (theInputString.contains("ADT^A27")) {
			return null;
		}
		if (theInputString.contains("ADT^A54")) {
			return null;
		}
		if (theInputString.contains("ADT^A61")) {
			return null;
		}

		ADT_A01 input = new ADT_A01();
		input.setParser(PipeParser.getInstanceWithNoValidation());
		input.parse(theInputString);

		MSH msh = input.getMSH();
		msh.getMsh3_SendingApplication().parse(ourHspOid+"^"+ourSendingSystemOid);
		msh.getMsh4_SendingFacility().parse("");
		msh.getMsh5_ReceivingApplication().parse("cGTA");
		msh.getMsh6_ReceivingFacility().parse("cGTA");
		msh.getMsh7_DateTimeOfMessage().getTs1_Time().setValue(msh.getMsh7_DateTimeOfMessage().getTs1_Time().getValueAsCalendar());

		msh.getMsh15_AcceptAcknowledgmentType().parse("NE");
		msh.getMsh16_ApplicationAcknowledgmentType().parse("AL");
		msh.getMsh17_CountryCode().parse("CAN");
		msh.getMsh18_CharacterSet(0).parse("8859/1");
		msh.getMsh11_ProcessingID().parse("T");

		msh.getMsh8_Security().setValue("2954864636aaa");
		msh.getMsh21_MessageProfileIdentifier(0).getEi1_EntityIdentifier().setValue("CGTA_CDR_INPUT_2_0");

		if (theInputString.contains("ADT^A08")) {
			if (input.getPV1().getVisitNumber().getIDNumber().encode().isEmpty()) {
				return null;
			}
		}
		
		String triggerEvent = msh.getMsh9_MessageType().getMsg2_TriggerEvent().getValue();
		if (StringUtils.isBlank(input.getPID().getPid3_PatientIdentifierList(0).getCx1_IDNumber().getValue())) {
			return null;
		}

		EVN evn = input.getEVN();
		evn.getEvn1_EventTypeCode().parse("");
		evn.getEvn3_DateTimePlannedEvent().parse("");
		evn.getEvn4_EventReasonCode().parse("");
		evn.getEvn5_OperatorID(0).parse("");
		evn.getEvn6_EventOccurred().parse("");
		evn.getEvn7_EventFacility().parse("");
		
		// PID
		PID pid = input.getPID();
		convertPid(pid);

		input.getPD1().parse("PD1");
		
		for (ROL next : input.getROLAll()) {
			convertRol(next);
		}
		while (input.getROL2Reps() > 0) {
			input.removeROL2(0);
		}

		// PV1
		boolean processPv1 = true;
		if (theInputString.contains("ADT^A40")) {
			processPv1 = false;
		}
		if (triggerEvent.equals("A28") || triggerEvent.equals("A31")) {
			processPv1 = false;
		}


		PV1 pv1 = input.getPV1();
		PV2 pv2 = input.getPV2();
		if (processPv1) {
			convertPv1(pv1);
			convertPv2(pv2);
		} else {
			pv1.parse("PV1|");
			pv2.parse("PV2|");
		}

		for (DG1 next : input.getDG1All()) {
			if (next.encode().length() == 5) {
				next.parse("DG1|");
			} else if (StringUtils.isBlank(next.getDg13_DiagnosisCodeDG1().getCe1_Identifier().getValue())){
				next.getDg13_DiagnosisCodeDG1().getCe3_NameOfCodingSystem().setValue("");
				next.getDg14_DiagnosisDescription().parse("");
				next.getDg15_DiagnosisDateTime().parse("");
				next.getDg16_DiagnosisType().parse("");
				next.getDg115_DiagnosisPriority().parse("");
				next.getDg117_DiagnosisClassification().parse("");
			}
		}

		while (input.getPROCEDUREReps() > 0) {
			input.removePROCEDURE(0);
		}
		
		for (NK1 next : input.getNK1All()) {
			if (next.encode().length() == 5) {
				next.parse("NK1|");
			} else {
				if (next.getNk13_Relationship().encode().isEmpty()) {
					next.getNk13_Relationship().parse("OTH^Other^HL70063");
				}
				if (next.getNk14_AddressReps() > 0 && next.getNk14_Address(0).getXad4_StateOrProvince().getValue().equals("ON")) {
					next.getNk14_Address(0).getXad4_StateOrProvince().setValue("CANON");
				}
			}
		}

		if (input.getNonStandardNames().contains("IAM")) {
			for (Structure next : input.getAll("IAM")) {
				IAM iam = (IAM) next;
				if (iam.encode().length() == 5) {
					iam.parse("IAM|");
				} else {

					iam.getIam2_AllergenTypeCode().getCe3_NameOfCodingSystem().setValue("HL70127");
					iam.getIam3_AllergenCodeMnemonicDescription().getCe3_NameOfCodingSystem().setValue(ourAllergenCodingSystemOid);
					iam.getIam9_SensitivityToCausativeAgentCode().parse("");

				}
			}

		}

		if (input.getNonStandardNames().contains("PID2")) {
			PID pid2 = (PID) input.get("PID2");
			convertPid(pid2);
		}

		if (input.getNonStandardNames().contains("PV12")) {
			PV1 pv12 = (PV1) input.get("PV12");
			convertPv1(pv12);
		}

		if (input.getNonStandardNames().contains("MRG")) {
			MRG mrg = (MRG) input.get("MRG");

			while (mrg.getMrg1_PriorPatientIdentifierListReps() > 1) {
				mrg.removeMrg1_PriorPatientIdentifierList(1);
			}
			if (StringUtils.isNotBlank(mrg.getMrg1_PriorPatientIdentifierList(0).getCx1_IDNumber().encode())) {
				mrg.getMrg1_PriorPatientIdentifierList(0).getCx4_AssigningAuthority().getHd1_NamespaceID().setValue(ourHspOid);
				mrg.getMrg1_PriorPatientIdentifierList(0).getCx4_AssigningAuthority().getHd2_UniversalID().setValue(ourSendingSystemOid);
			}

			// MRG-5
			if (StringUtils.isNotBlank(mrg.getMrg5_PriorVisitNumber().encode())) {
				mrg.getMrg5_PriorVisitNumber().getCx4_AssigningAuthority().getHd1_NamespaceID().setValue(ourHspOid);
				mrg.getMrg5_PriorVisitNumber().getCx4_AssigningAuthority().getHd2_UniversalID().setValue(ourSendingSystemOid);
				mrg.getMrg5_PriorVisitNumber().getCx6_AssigningFacility().clear();
				mrg.getMrg5_PriorVisitNumber().getCx7_EffectiveDate().setValue("");
			}

		}
		System.out.println();
		System.out.println(input.encode().replace("\r", "\n"));
		System.out.println();

		Converter c = new Converter(true);
		PatientWithVisits retVal = c.convertPatientWithVisits(input);

		System.out.println();
		System.out.println(input.encode().replace("\r", "\n"));
		System.out.println();

		if (c.hasFailure()) {
			System.out.println();
			System.out.println();
			System.out.println();
			throw new Exception(c.getFailures().toString());
		}

		return retVal;
	}


	private static void convertPv2(PV2 thePv2) throws HL7Exception {
		
		thePv2.getPv22_AccommodationCode().parse("");
		
		thePv2.getPv23_AdmitReason().getCe1_Identifier().setValue(thePv2.getPv23_AdmitReason().getCe2_Text().getValue());
		thePv2.getPv23_AdmitReason().getCe3_NameOfCodingSystem().parse("");

		thePv2.getPv222_VisitProtectionIndicator().parse("");
		thePv2.getPv224_PatientStatusCode().parse("");
		thePv2.getPv225_VisitPriorityCode().parse("");
		thePv2.getPv231_RecurringServiceCode().parse("");
		thePv2.getPv238_ModeOfArrivalCode().parse("");

    }


	private static void convertRol(ROL theRol) throws HL7Exception {
		theRol.getRol2_ActionCode().setValue("");

		
		
		for (int i = 0; i < theRol.getRol4_RolePersonReps(); i++) {
			XCN next = theRol.getRol4_RolePerson(i);
			convertXcn(next);
		}

		theRol.getRol5_RoleBeginDateTime().parse("");
		theRol.getRol9_ProviderType(0).parse("");
		theRol.getRol10_OrganizationUnitType().parse("");

		for (int i = 0; i < theRol.getRol11_OfficeHomeAddressBirthplaceReps(); i++) {
			XAD next = theRol.getRol11_OfficeHomeAddressBirthplace(i);
			next.getAddressType().setValue("B");
			convertXad(next);
		}

		for (int i = 0; i < theRol.getRol12_PhoneReps(); i++) {
			XTN next = theRol.getRol12_Phone(i);
			if (StringUtils.isNotBlank(next.getEmailAddress().getValue())) {
				next.getXtn1_TelephoneNumber().setValue(""); // TODO: create new
															 // rep if phne
															 // number exists
				next.getXtn2_TelecommunicationUseCode().setValue("NET");
				next.getXtn8_Extension().setValue("");
			}
		}

	}


	public static List<ClinicalDocumentGroup> convertOru(String inputString) throws Exception {
		ORU_R01 input = converOruR01(inputString);
		if (input == null) {
			return Collections.emptyList();
		}

		if (input.encode().contains("CBC")) {
			System.out.println();
			System.out.println();
			System.out.println(input.encode().replace("\r", "\n"));
			System.out.println();
			System.out.println();
		}
		
		
		//System.out.println(input.encode());

		Converter converter = new Converter();
		List<ClinicalDocumentGroup> doc = converter.convertClinicalDocument(input);

		if (converter.hasFailure()) {
			System.out.println();
			// System.out.println(input.printStructure());
			System.out.println();
			System.out.println(input.encode().replace("\r", "\n"));
			System.out.println();
			throw new Exception();
		}

		return doc;
	}


	private static ORU_R01 converOruR01(String theInputString) throws HL7Exception, ParseException {
		if (theInputString.contains("EPR|WP") || theInputString.contains("EPR|TRI")) {
			return null;
		}

		ORU_R01 input = new ORU_R01();
		input.setParser(PipeParser.getInstanceWithNoValidation());
		input.parse(theInputString);

		// System.out.println(input.encode());

		if (input.getPATIENT_RESULT().getORDER_OBSERVATIONReps() > 1) {
			System.out.println("** FOund weird message");
			return null;
		}

		if (theInputString.contains("ORD^ORU")) {
			System.out.println("** Found ORD^ORU");
			return null;
		}

		MSH msh = input.getMSH();
		msh.getMsh3_SendingApplication().parse(ourHspOid+"^"+ourSendingSystemOid);
		msh.getMsh7_DateTimeOfMessage().getTs1_Time().setValue(msh.getMsh7_DateTimeOfMessage().getTs1_Time().getValueAsCalendar());

		msh.getMsh11_ProcessingID().parse("T");
		msh.getMsh15_AcceptAcknowledgmentType().parse("NE");
		msh.getMsh16_ApplicationAcknowledgmentType().parse("AL");
		msh.getMsh17_CountryCode().parse("CAN");
		msh.getMsh18_CharacterSet(0).parse("8859/1");

		msh.getMsh8_Security().setValue("2954864636aaa");
		msh.getMsh21_MessageProfileIdentifier(0).getEi1_EntityIdentifier().setValue("CGTA_CDR_INPUT_2_0");

		// PID
		PID pid = input.getPATIENT_RESULT().getPATIENT().getPID();
		convertPid(pid);

		// PV1
		PV1 pv1 = input.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1();
		convertPv1(pv1);

		// TQ1
		while (input.getPATIENT_RESULT().getORDER_OBSERVATION().getTIMING_QTYReps() > 0) {
			input.getPATIENT_RESULT().getORDER_OBSERVATION().removeTIMING_QTY(0);
		}

		// ORC
		ORC orc = input.getPATIENT_RESULT().getORDER_OBSERVATION().getORC();

		// ORC-4
		orc.getOrc4_PlacerGroupNumber().parse(orc.getOrc2_PlacerOrderNumber().encode());
		orc.getOrc4_PlacerGroupNumber().getEi2_NamespaceID().setValue(ourHspOid);
		orc.getOrc4_PlacerGroupNumber().getEi3_UniversalID().setValue(ourSendingSystemOid);
		String orc4 = orc.getOrc4_PlacerGroupNumber().encode();
		orc.clear();
		orc.getOrc4_PlacerGroupNumber().parse(orc4);

		// OBR
		OBR obr = input.getPATIENT_RESULT().getORDER_OBSERVATION().getOBR();

		// OBR-2
		obr.getObr2_PlacerOrderNumber().getEi2_NamespaceID().setValue(ourHspOid);
		obr.getObr2_PlacerOrderNumber().getEi3_UniversalID().setValue(ourSendingSystemOid);

		// OBR-4
		obr.getObr4_UniversalServiceIdentifier().getCe3_NameOfCodingSystem().setValue(ourRequestCodingSystemOid);

		// OBR-7
		if (StringUtils.isNotBlank(obr.getObr7_ObservationDateTime().encode())) {
			obr.getObr7_ObservationDateTime().getTs1_Time().setValue(obr.getObr7_ObservationDateTime().getTime().getValueAsCalendar());
		}

		// OBR-16
		for (int i = 0; i < obr.getObr16_OrderingProviderReps(); i++) {
			convertXcn(obr.getObr16_OrderingProvider(i));
		}

		// OBR-18
		obr.getObr18_PlacerField1().clear();

		// OBR-22
		obr.getObr22_ResultsRptStatusChngDateTime().clear();

		// OBR-25
		String obrStatus = obr.getObr25_ResultStatus().getValue();
		if ("P".equals(obrStatus)) {
			obr.getObr25_ResultStatus().setValue("I");
		}
		if ("R".equals(obrStatus)) {
			obr.getObr25_ResultStatus().setValue("I");
		}

		// OBR-26
		if (StringUtils.isNotBlank(obr.getObr26_ParentResult().encode())) {
			obr.getObr26_ParentResult().getPrl1_ParentObservationIdentifier().getCe2_Text().setValue(ourHspOid);
			obr.getObr26_ParentResult().getPrl1_ParentObservationIdentifier().getCe3_NameOfCodingSystem().setValue(ourSendingSystemOid);
		}

		// OBR-28
		for (int i = 0; i < obr.getObr28_ResultCopiesToReps(); i++) {
			if (obr.getObr28_ResultCopiesTo(i).getXcn1_IDNumber().encode().isEmpty()) {
				obr.removeObr28_ResultCopiesTo(i);
				i--;
			} else {
				convertXcn(obr.getObr28_ResultCopiesTo(i));
			}
		}

		// OBX
		for (int i = 0; i < input.getPATIENT_RESULT().getORDER_OBSERVATION().getOBSERVATIONReps(); i++) {
			ORU_R01_OBSERVATION observation = input.getPATIENT_RESULT().getORDER_OBSERVATION().getOBSERVATION(i);
			OBX obx = observation.getOBX();

			obx.getObx1_SetIDOBX().setValue(Integer.toString(i + 1));

			if (StringUtils.isBlank(obx.getObx3_ObservationIdentifier().getCe1_Identifier().encode()) || StringUtils.isBlank(obx.getObx3_ObservationIdentifier().getCe2_Text().encode())) {
				input.getPATIENT_RESULT().getORDER_OBSERVATION().removeOBSERVATION(i);
				i--;
				continue;
			}

			// OBX-3
			obx.getObx3_ObservationIdentifier().getCe3_NameOfCodingSystem().setValue(ourResultCodingSystemOid);

			if (i > 0) {
				OBX prevObx = input.getPATIENT_RESULT().getORDER_OBSERVATION().getOBSERVATION(i - 1).getOBX();
				String prevId = prevObx.getObx3_ObservationIdentifier().getCe1_Identifier().getValue();
				String newId = obx.getObx3_ObservationIdentifier().getCe1_Identifier().getValue();
				if (ObjectUtils.equals(prevId, newId)) {
					if (prevObx.getObx4_ObservationSubID().getValue() == null) {
						prevObx.getObx4_ObservationSubID().setValue("1");
					}
					obx.getObx4_ObservationSubID().setValue(Integer.toString(Integer.parseInt(prevObx.getObx4_ObservationSubID().getValue()) + 1));
				} else {
					obx.getObx4_ObservationSubID().setValue(null);
				}

			} else {
				obx.getObx4_ObservationSubID().setValue(null);
			}

			String valueType = obx.getObx2_ValueType().getValue();

			if ("WP".equals(valueType)) {
				obx.getObx2_ValueType().setValue("TX");
			}

			if (valueType.equals("DT")) {
				DT dt = (DT) obx.getObx5_ObservationValue(0).getData();
				String value = dt.getValue();
				value = value.substring(value.indexOf(',') + 1);
				Date date = new SimpleDateFormat("dd MMM yyyy").parse(value);
				dt.setValue(new SimpleDateFormat("yyyyMMdd").format(date));
			}

			if (valueType.equals("TS")) {
				TS dt = (TS) obx.getObx5_ObservationValue(0).getData();
				String value = dt.getTs1_Time().getValue();
				value = value.substring(value.indexOf(',') + 1);
				String replaceAll = value.trim().replaceAll(" +", " ");
				if (replaceAll.length() == 11) {
					replaceAll = replaceAll + " 0000";
				}
				Date date = new SimpleDateFormat("dd MMM yyyy HHmm").parse(replaceAll);
				String newValue = new SimpleDateFormat("yyyyMMddHHmm").format(date) + "00";
				dt.getTs1_Time().setValue(newValue);
				dt.getTs1_Time().setValue(dt.getTs1_Time().getValueAsCalendar());
			}

			if (valueType.equals("ED")) {
				ED dt = (ED) obx.getObx5_ObservationValue(0).getData();
				String value = dt.getData().getValue();
				// System.out.println(value);
			}

			String obx8 = obx.getObx8_AbnormalFlags(0).getValue();
			if ("normal".equals(obx8)) {
				obx.getObx8_AbnormalFlags(0).setValue("N");
			} else if ("p".equals(obx8)) {
				// TODO: what is "p"
				obx.getObx8_AbnormalFlags(0).setValue("N");
			} else if ("I".equals(obx8)) {
				// TODO: what is "I"
				obx.getObx8_AbnormalFlags(0).setValue("N");
			}

			String obx11 = obx.getObx11_ObservationResultStatus().getValue();
			if (StringUtils.isBlank(obx11)) {
				obx.getObx11_ObservationResultStatus().setValue(obr.getResultStatus().getValue());
			}
			if ("S".equals(obx11)) {
				// TODO: what's S?
				obx.getObx11_ObservationResultStatus().setValue("F");
			}
			if ("R".equals(obx11)) {
				// TODO: what's R?
				obx.getObx11_ObservationResultStatus().setValue("F");
			}

			String val = obx.getObx14_DateTimeOfTheObservation().getTs1_Time().getValue();
			if (val == null || val.isEmpty()) {

			} else if (val.matches("^[0-9.]+$")) {

				obx.getObx14_DateTimeOfTheObservation().getTs1_Time().setValue(obx.getObx14_DateTimeOfTheObservation().getTs1_Time().getValueAsCalendar());

			} else {
				if (val.matches("^[0-9] .*")) {
					val = "0" + val;
				}
				val = val.replace("  ", " ");
				try {
					Date parse = new SimpleDateFormat("dd MMM yy HHmm").parse(val);
					obx.getObx14_DateTimeOfTheObservation().clear();
					obx.getObx14_DateTimeOfTheObservation().getTs1_Time().setValue(parse);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		}

		Terser t = new Terser(input);

		try {
			Segment zwa = t.getFinder().findSegment("ZWA", 0);
			if (zwa != null) {
				zwa.parse("ZWA|");
			}
		} catch (HL7Exception e) {
		}

		try {
			Segment zwa = t.getFinder().findSegment("SPM", 0);
			if (zwa != null) {
				zwa.parse("ZWA|");
			}
		} catch (HL7Exception e) {
		}

		try {
			Segment zwa = t.getFinder().findSegment("FT1", 0);
			if (zwa != null) {
				zwa.parse("FT1|");
			}
		} catch (HL7Exception e) {
		}

		return input;
	}


	private static void convertPv1(PV1 pv1) throws DataTypeException, HL7Exception {
		// PV1-1
		pv1.getPv11_SetIDPV1().setValue("1");

		// PV1-2
		String ptClass = pv1.getPv12_PatientClass().getValue();
		if ("C".equals(ptClass)) {
			pv1.getPv12_PatientClass().setValue("O");
		} else if ("S".equals(ptClass)) {
			pv1.getPv12_PatientClass().setValue("O");
		}
		convertLocation(pv1.getPv13_AssignedPatientLocation());
		convertLocation(pv1.getPv16_PriorPatientLocation());

		// PV1-7
		for (int i = 0; i < pv1.getPv17_AttendingDoctorReps(); i++) {
			XCN xcn = pv1.getPv17_AttendingDoctor(i);
			if (xcn.getXcn1_IDNumber().encode().isEmpty()) {
				pv1.removePv17_AttendingDoctor(i);
				i--;
			} else {
				convertXcn(xcn);
			}
		}

		// PV1-8
		for (int i = 0; i < pv1.getPv18_ReferringDoctorReps(); i++) {
			XCN xcn = pv1.getPv18_ReferringDoctor(i);
			if (xcn.getXcn1_IDNumber().encode().isEmpty()) {
				pv1.removePv18_ReferringDoctor(i);
				i--;
			} else {
				convertXcn(xcn);
			}
		}

		// PV1-9
		for (int i = 0; i < pv1.getPv19_ConsultingDoctorReps(); i++) {
			XCN xcn = pv1.getPv19_ConsultingDoctor(i);
			String encode = xcn.getXcn1_IDNumber().encode();
			if (encode.isEmpty()) {
				pv1.removePv19_ConsultingDoctor(i);
				i--;
			} else {
				convertXcn(xcn);
				ourLog.debug("PV1-9 is {}", xcn);
			}
		}
		
		pv1.getPv114_AdmitSource().parse("");

		// PV1-17
		for (int i = 0; i < pv1.getPv117_AdmittingDoctorReps(); i++) {
			XCN xcn = pv1.getPv117_AdmittingDoctor(i);
			if (xcn.getXcn1_IDNumber().encode().isEmpty()) {
				pv1.removePv117_AdmittingDoctor(i);
				i--;
			} else {
				convertXcn(xcn);
			}
		}

		pv1.getPv118_PatientType().parse("");

		// PV1-19
		pv1.getPv119_VisitNumber().getCx4_AssigningAuthority().getHd1_NamespaceID().setValue(ourHspOid);
		pv1.getPv119_VisitNumber().getCx4_AssigningAuthority().getHd2_UniversalID().setValue(ourSendingSystemOid);
		pv1.getPv119_VisitNumber().getCx6_AssigningFacility().clear();
		pv1.getPv119_VisitNumber().getCx7_EffectiveDate().clear();

		pv1.getPv123_CreditRating().parse("");
		pv1.getPv136_DischargeDisposition().parse("");

		// PV1-39
		pv1.getPv139_ServicingFacility().clear();

		if (StringUtils.isNotBlank(pv1.getPv144_AdmitDateTime().getTs1_Time().getValue())) {
			pv1.getPv144_AdmitDateTime().getTs1_Time().setValue(pv1.getPv144_AdmitDateTime().getTs1_Time().getValueAsCalendar());
		}

		if (StringUtils.isNotBlank(pv1.getPv145_DischargeDateTime(0).getTs1_Time().getValue())) {
			pv1.getPv145_DischargeDateTime(0).getTs1_Time().setValue(pv1.getPv145_DischargeDateTime(0).getTs1_Time().getValueAsCalendar());
		}

		pv1.getPv151_VisitIndicator().parse("");

	}


	private static void convertPid(PID pid) throws DataTypeException, HL7Exception {
		pid.getPid1_SetIDPID().setValue("1");

		// PID-3
		while (pid.getPid3_PatientIdentifierListReps() > 2) {
			pid.removePid3_PatientIdentifierList(2);
		}

		// PID-3(1)
		pid.getPid3_PatientIdentifierList(0).getCx4_AssigningAuthority().getHd1_NamespaceID().setValue(ourHspOid);
		pid.getPid3_PatientIdentifierList(0).getCx4_AssigningAuthority().getHd2_UniversalID().setValue(ourSendingSystemOid);
		pid.getPid3_PatientIdentifierList(0).getCx7_EffectiveDate().setValue("");
		pid.getPid3_PatientIdentifierList(0).getCx8_ExpirationDate().setValue("");

		// PID-3(2)
		if (StringUtils.isNotBlank(pid.getPid3_PatientIdentifierList(1).getCx1_IDNumber().getValue())) {
			pid.getPid3_PatientIdentifierList(1).getCx9_AssigningJurisdiction().parse(pid.getPid3_PatientIdentifierList(1).getCx4_AssigningAuthority().encode());
			pid.getPid3_PatientIdentifierList(1).getCx9_AssigningJurisdiction().getCwe3_NameOfCodingSystem().setValue("HL70363");
			pid.getPid3_PatientIdentifierList(1).getCx7_EffectiveDate().setValue("");
			pid.getPid3_PatientIdentifierList(1).getCx8_ExpirationDate().setValue("");
			pid.getPid3_PatientIdentifierList(1).getCx4_AssigningAuthority().parse("");
		} else {
			pid.removePid3_PatientIdentifierList(1);
		}

		// PID-5
		for (int i = 0; i < pid.getPid5_PatientNameReps(); i++) {
			XPN next = pid.getPid5_PatientName(i);
			next.getXpn12_EffectiveDate().parse("");
			next.getXpn13_ExpirationDate().parse("");
		}

		if (StringUtils.isNotBlank(pid.getPid7_DateTimeOfBirth().getTs1_Time().getValue())) {
			// pid.getPid7_DateTimeOfBirth().getTs1_Time().setValue(pid.getPid7_DateTimeOfBirth().getTs1_Time().getValue()
			// + "000000-0400");
			pid.getPid7_DateTimeOfBirth().getTs1_Time().setValue(pid.getPid7_DateTimeOfBirth().getTs1_Time().getValueAsCalendar());
		}

		// PID-11
		for (int i = 0; i < pid.getPid11_PatientAddressReps(); i++) {
			XAD next = pid.getPid11_PatientAddress(i);
			convertXad(next);
		}

		pid.getPid12_CountyCode().parse("");
		
		// PID-13
		XTN xpn = pid.getPid13_PhoneNumberHome(0);
		if (StringUtils.isNotBlank(xpn.getEmailAddress().getValue())) {
			XTN newXpn = pid.insertPid13_PhoneNumberHome(1);
			newXpn.getXtn2_TelecommunicationUseCode().setValue("NET");
			newXpn.getXtn4_EmailAddress().setValue(xpn.getXtn4_EmailAddress().getValue());
			xpn.getXtn4_EmailAddress().setValue(null);
		}
		for (int i = 0; i < pid.getPid13_PhoneNumberHomeReps(); i++) {
			xpn = pid.getPid13_PhoneNumberHome(i);
			if (xpn.encode().length() == 0) {
				pid.removePid13_PhoneNumberHome(i);
				i--;
				continue;
			}
			if ("ORN".equals(xpn.getXtn2_TelecommunicationUseCode().getValue())) {
				xpn.getXtn2_TelecommunicationUseCode().setValue("OTH");
			}
		}

		// PID-15
		if (StringUtils.isNotBlank(pid.getPid15_PrimaryLanguage().encode())) {
			pid.getPid15_PrimaryLanguage().getCe3_NameOfCodingSystem().setValue("HL70296");
		}

		// PID-16
		if (StringUtils.isNotBlank(pid.getPid16_MaritalStatus().getCe1_Identifier().getValue())) {
			pid.getPid16_MaritalStatus().getCe3_NameOfCodingSystem().setValue("HL70002");
		}

		// PID-18
		pid.getPid18_PatientAccountNumber().clear();

		// PID-26
		while (pid.getPid26_CitizenshipReps() > 0) {
			pid.removePid26_Citizenship(0);
		}

		if (StringUtils.isNotBlank(pid.getPid29_PatientDeathDateAndTime().encode())) {
			pid.getPid29_PatientDeathDateAndTime().getTs1_Time().setValue(pid.getPid29_PatientDeathDateAndTime().getTs1_Time().getValueAsCalendar());
		}

		pid.getPid33_LastUpdateDateTime().parse("");
	}


	private static void convertXad(XAD next) throws DataTypeException {
		String prov = next.getXad4_StateOrProvince().getValue();
		if ("ON".equals(prov)) {
			next.getXad4_StateOrProvince().setValue("CANON");
		} else if ("AB".equals(prov)) {
			next.getXad4_StateOrProvince().setValue("CANAB");
		} else if ("BC".equals(prov)) {
			next.getXad4_StateOrProvince().setValue("CANBC");
		} else if ("QC".equals(prov)) {
			next.getXad4_StateOrProvince().setValue("CANQC");
		} else if ("NB".equals(prov)) {
			next.getXad4_StateOrProvince().setValue("CANNB");
		} else if ("NL".equals(prov)) {
			next.getXad4_StateOrProvince().setValue("CANNF");
		} else if ("NS".equals(prov)) {
			next.getXad4_StateOrProvince().setValue("CANNS");
		}
	}


	private static void fixTimestamp(DT theDate) {
	}


	private static void convertXcn(XCN xcn) throws HL7Exception {
		xcn.getXcn9_AssigningAuthority().parse("1.3.6.1.4.1.12201.1.2.1.5^"+ourHspOid+"^"+ourSendingSystemOid);
		xcn.getXcn10_NameTypeCode().clear();
		xcn.getXcn11_IdentifierCheckDigit().clear();
		xcn.getXcn12_CheckDigitScheme().clear();
		xcn.getXcn13_IdentifierTypeCode().clear();
		xcn.getXcn14_AssigningFacility().clear();
		xcn.getXcn15_NameRepresentationCode().clear();
		xcn.getXcn16_NameContext().clear();
		xcn.getXcn17_NameValidityRange().clear();
		xcn.getXcn18_NameAssemblyOrder().clear();
		xcn.getXcn19_EffectiveDate().clear();
		xcn.getXcn20_ExpirationDate().clear();
		xcn.getXcn21_ProfessionalSuffix().clear();
		xcn.getXcn22_AssigningJurisdiction().clear();
		xcn.getXcn23_AssigningAgencyOrDepartment().clear();
		xcn.getExtraComponents().getComponent(0);
		xcn.getExtraComponents().getComponent(1);
		ST data = new ST(xcn.getMessage());
		xcn.getExtraComponents().getComponent(2).setData(data);
		data.setValue("");
	}


	private static void convertLocation(PL theLocation) throws DataTypeException {
		if (StringUtils.isBlank(theLocation.getPl1_PointOfCare().getValue())) {
			theLocation.clear();
		}

		String facility = theLocation.getPl4_Facility().getHd1_NamespaceID().getValue();
		String oid = "";
		if ("G".equals(facility)) {
			facility = ourFacility1Oid;
			oid = ourHspOid;
		}
		if ("W".equals(facility)) {
			facility = ourFacility2Oid;
			oid = ourHspOid;
		}

		theLocation.getPl4_Facility().getHd1_NamespaceID().setValue(oid);
		theLocation.getPl4_Facility().getHd2_UniversalID().setValue(facility);

		theLocation.getPl5_LocationStatus().clear();
		theLocation.getPl6_PersonLocationType().clear();
		theLocation.getPl7_Building().clear();
		theLocation.getPl8_Floor().clear();
		theLocation.getPl9_LocationDescription().clear();
		theLocation.getPl10_ComprehensiveLocationIdentifier().clear();
		theLocation.getPl11_AssigningAuthorityForLocation().clear();
		ST st = new ST(theLocation.getMessage());
		theLocation.getExtraComponents().getComponent(0).setData(st);
		st.setValue("");

	}
}
