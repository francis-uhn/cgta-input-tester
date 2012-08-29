package ca.cgta.input.val.server;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.cgta.input.converter.Converter;
import ca.cgta.input.converter.Failure;
import ca.cgta.input.converter.FailureCode;
import ca.cgta.input.model.config.Contributor;
import ca.cgta.input.model.config.ContributorConfig;
import ca.cgta.input.model.config.ContributorConfigFactory;
import ca.cgta.input.model.config.SendingSystem;
import ca.cgta.input.val.client.oidlib.OidBrowserPanel;
import ca.cgta.input.val.client.rpc.ValidatorService;
import ca.cgta.input.val.shared.results.Code;
import ca.cgta.input.val.shared.results.FailureCodeDetails;
import ca.cgta.input.val.shared.results.IStructure;
import ca.cgta.input.val.shared.results.OidLibrary;
import ca.cgta.input.val.shared.results.ParsedComponent;
import ca.cgta.input.val.shared.results.ParsedFailure;
import ca.cgta.input.val.shared.results.ParsedField;
import ca.cgta.input.val.shared.results.ParsedGroup;
import ca.cgta.input.val.shared.results.ParsedMessage;
import ca.cgta.input.val.shared.results.ParsedSegment;
import ca.cgta.input.val.shared.results.ParsedSubComponent;
import ca.cgta.input.val.shared.results.ValidationResult;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Composite;
import ca.uhn.hl7v2.model.Group;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Primitive;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.model.Structure;
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.model.Varies;
import ca.uhn.hl7v2.model.v25.message.ADT_A01;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.preparser.PreParser;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class ValidatorServiceImpl extends RemoteServiceServlet implements ValidatorService {

	private static final String ERR_INTERNAL = "Failed to convert message due to an internal error. This is probably a bug with the validator tool.";
	private static final String ERR_PARSE = "Could not parse message. Please ensure that the message is a valid HL7 message and try again.";
	private final PipeParser myParser = PipeParser.getInstanceWithNoValidation();
	private static final Logger ourLog = LoggerFactory.getLogger(ValidatorServiceImpl.class);


	/**
	 * {@inheritDoc}
	 */
	public synchronized ValidationResult validate(String theMessage) {

		theMessage = theMessage.trim();
		theMessage = theMessage.replace("\r\n", "\n");
		theMessage = theMessage.replace("\n\n", "\n");
		theMessage = theMessage.replace("\n", "\r");

		String[] mshValues;
		try {
			mshValues = PreParser.getFields(theMessage, "MSH-9-1", "MSH-9-2");
		} catch (HL7Exception e) {
			ourLog.info("Failed to get MSH-9-1 or MSH-9-2", e);
			return ValidationResult.getError(ERR_PARSE);
		}

		if (StringUtils.isBlank(mshValues[0]) || StringUtils.isBlank(mshValues[1])) {
			return ValidationResult.getError("Message is missing a mandatory value in MSH-9-1 or MSH-9-2 (Message Type)");
		}

		ourLog.info("Validation message (" + theMessage.length() + " chars) of type " + mshValues[0] + "^" + mshValues[1]);

		Converter converter;
		try {
			converter = new Converter();
		} catch (JAXBException e1) {
			ourLog.info("Failed to convert message", e1);
			return ValidationResult.getError(ERR_INTERNAL);
		}

		Message parsedMessage;

		if ("ADT".equals(mshValues[0])) {

			ADT_A01 adt = new ADT_A01();
			parsedMessage = adt;
			adt.setParser(myParser);
			try {
				adt.parse(theMessage);
			} catch (HL7Exception e) {
				ourLog.info("Failed to parse message", e);
				return ValidationResult.getError(ERR_PARSE);
			}

			try {
				converter.convertPatientWithVisits(adt);
			} catch (Exception e) {
				ourLog.info("Failed to convert message", e);
				return ValidationResult.getError(ERR_INTERNAL);
			}

		} else if ("ORU".equals(mshValues[0])) {

			ORU_R01 oru = new ORU_R01();
			parsedMessage = oru;
			oru.setParser(myParser);
			try {
				oru.parse(theMessage);
			} catch (HL7Exception e) {
				ourLog.info("Failed to parse message", e);
				return ValidationResult.getError(ERR_PARSE);
			}

			try {
				converter.convertClinicalDocument(oru);
			} catch (Exception e) {
				ourLog.info("Failed to convert message", e);
				return ValidationResult.getError(ERR_INTERNAL);
			}

		} else {

			String message = "Unknown message type " + mshValues[0] + "^" + mshValues[1] + ". Please see the ConnectingGTA Input Specification for information on allowed message types";
			ourLog.info(message);
			return ValidationResult.getError(message);

		}

		ParsedGroup convertedParsedMessage;
		try {
			convertedParsedMessage = convertParsedGroup(null, parsedMessage);
		} catch (HL7Exception e) {
			ourLog.info("Failed to convert message", e);
			return ValidationResult.getError(ERR_INTERNAL);
		}

		ValidationResult retVal = new ValidationResult();
		retVal.setParsedMessage(new ParsedMessage(convertedParsedMessage));

		for (Failure next : converter.getFailures()) {

			String fieldVal = next.getFieldVal();
			String message = next.getMessage();
			String failureCode = next.getFailureCode().name();
			String terserPath = next.getTerserPath();
			retVal.addFailure(new ParsedFailure(terserPath, failureCode, message, fieldVal, next.getFailureCode().getStepsToResolve(), next.getFailureCode().getSeverity().name()));
		}

		return retVal;
	}


	private ParsedGroup convertParsedGroup(String theTerserPath, Group theParsedMessage) throws HL7Exception {
		ParsedGroup retVal = new ParsedGroup();

		for (String nextName : theParsedMessage.getNames()) {
			String nextTerserPath = theTerserPath != null ? theTerserPath + "/" + nextName : nextName;

			int repIndex = 1;
			List<IStructure> nextStructureRepList = new ArrayList<IStructure>();
			for (Structure nextRep : theParsedMessage.getAll(nextName)) {

				String nextTerserPathSub = repIndex > 1 ? nextTerserPath + "(" + repIndex + ")" : nextTerserPath;
				if (nextRep instanceof Group) {
					nextStructureRepList.add(convertParsedGroup(nextTerserPathSub, (Group) nextRep));
				} else {
					nextStructureRepList.add(convertParsedSegment(nextTerserPathSub, (Segment) nextRep));
				}

				repIndex++;
			}

			if (nextStructureRepList.size() > 0) {
				retVal.addChild(nextStructureRepList);
			}

		}

		return retVal;
	}


	private ParsedSegment convertParsedSegment(String theNextTerserPathSub, Segment theSegment) throws HL7Exception {
		ParsedSegment retVal = new ParsedSegment();
		retVal.setTerserPath(theNextTerserPathSub);
		retVal.setName(theSegment.getName());

		for (int index = 0; index < theSegment.numFields(); index++) {

			List<ParsedField> nextReps = new ArrayList<ParsedField>();

			Type[] reps = theSegment.getField(index + 1);
			int nextRepIndex = 0;
			for (Type nextRep : reps) {

				String nextTerserPath = theNextTerserPathSub + "-" + (index + 1);
				if (nextRepIndex > 1) {
					nextTerserPath = nextTerserPath + "(" + nextRepIndex + ")";
				}

				ParsedField parsedField = convertParsedField(nextTerserPath, nextRep);
				nextReps.add(parsedField);

				nextRepIndex++;
			}

			retVal.addField(nextReps);

		}

		return retVal;
	}


	private ParsedField convertParsedField(String theTerserPath, Type theField) throws HL7Exception {
		ParsedField retVal = new ParsedField();

		if (theField instanceof Varies) {
			theField = ((Varies) theField).getData();
		}

		if (theField instanceof Primitive) {

			if ("MSH-1".equals(theTerserPath) || "MSH-2".equals(theTerserPath)) {
				retVal.setValueIfLeaf(((Primitive) theField).getValue());
			} else {
				retVal.setValueIfLeaf(((Primitive) theField).encode());
			}

		} else {

			for (Type nextComponent : ((Composite) theField).getComponents()) {
				if (nextComponent instanceof Varies) {
					nextComponent = ((Varies) nextComponent).getData();
				}

				ParsedComponent nextParsedComponent = new ParsedComponent();

				if (nextComponent instanceof Primitive) {
					nextParsedComponent.setValueIfLeaf(((Primitive) nextComponent).encode());
				} else {
					for (Type nextSubComponent : ((Composite) nextComponent).getComponents()) {
						if (nextSubComponent instanceof Varies) {
							nextSubComponent = ((Varies) nextSubComponent).getData();
						}

						ParsedSubComponent nextParsedSubComponent = new ParsedSubComponent();

						if (!(nextSubComponent instanceof Primitive)) {
							nextParsedSubComponent.setValueIfLeaf(((Composite) nextSubComponent).encode());
						} else {
							nextParsedSubComponent.setValueIfLeaf(((Primitive) nextSubComponent).encode());
						}
						nextParsedComponent.addChild(nextParsedSubComponent);
					}
				}
				retVal.addChild(nextParsedComponent);
			}
		}

		return retVal;
	}


	public List<FailureCodeDetails> failureCodes() {
		ArrayList<FailureCodeDetails> list = new ArrayList<FailureCodeDetails>();
		for (FailureCode next : FailureCode.values()) {
			FailureCodeDetails nextDetails = new FailureCodeDetails();
			nextDetails.setCode(next.name());
			nextDetails.setDescription(next.getDesc());
			nextDetails.setHowToResolve(next.getStepsToResolve());
			nextDetails.setSeverity(next.getSeverity().name());
			list.add(nextDetails);
		}
		return list;
	}


	public OidLibrary loadOidLibrary(String theHspId) throws Exception {
		
		ContributorConfig cfg = ContributorConfigFactory.getInstance().getContributorConfig();
		
		OidLibrary retVal = new OidLibrary();
		retVal.setHspIdentifier9004(new ArrayList<Code>());
		retVal.setFacilityIdentifiers9005(new ArrayList<Code>());
		retVal.setCodeSystems9007(new ArrayList<Code>());
		retVal.setSendingSystems9008(new ArrayList<Code>());
		
		retVal.setOtherOids(toCodes(cfg.getOtherOids()));
		retVal.setProvider9001Oids(toCodes(cfg.getProviderId9001()));
		
		for (Contributor nextContributor : cfg.getContributors()) {
			String hspId9004 = nextContributor.getHspId9004();
			Code hspCode = new Code(hspId9004, nextContributor.getName());
			retVal.getHspIdentifier9004().add(hspCode);
			
			hspCode.getMetadata().put(OidBrowserPanel.HSP_METADATA_HOSPITAL_FAC, nextContributor.getHospitalFacilityNumber());
			hspCode.getMetadata().put(OidBrowserPanel.HSP_METADATA_MRNOID, nextContributor.getMrnPoolOid());
			hspCode.getMetadata().put(OidBrowserPanel.HSP_METADATA_PROVIDEROID, nextContributor.getProviderPoolOid());
			
			if (theHspId != null && !theHspId.equals(hspId9004)) {
				continue;
			} 
			
			for (ca.cgta.input.model.config.Code nextFacility : nextContributor.getHspFacility()) {
				retVal.getFacilityIdentifiers9005().add(new Code(nextFacility.getCode(), nextFacility.getDescription(), hspId9004));
			}
			
			for (SendingSystem nextSystem : nextContributor.getSendingSystem()) {
				retVal.getSendingSystems9008().add(new Code(nextSystem.getCode(), nextSystem.getDescription(), hspId9004));
				
				addCodeSystems(retVal.getCodeSystems9007(), nextSystem.getRequestCodeSystemSystemObr4(), hspId9004, "Request Code (OBR-4) for system: " + nextSystem.getDescription());				
				addCodeSystems(retVal.getCodeSystems9007(), nextSystem.getResultCodeSystemSystemObx3(), hspId9004, "Result Code (OBX-3) for system: " + nextSystem.getDescription());				
				addCodeSystems(retVal.getCodeSystems9007(), nextSystem.getAllergenCodeSystemIam3(), hspId9004, "Allergen Code (IAM-3) for system: " + nextSystem.getDescription());				
				addCodeSystems(retVal.getCodeSystems9007(), nextSystem.getDrugAdministrationCodeSystemRxa5(), hspId9004, "Drug Administration Code (RXA-5) for system: " + nextSystem.getDescription());				
				addCodeSystems(retVal.getCodeSystems9007(), nextSystem.getDrugComponentCodeSystemRxc2(), hspId9004, "Drug Component Code (RXC-2) for system: " + nextSystem.getDescription());				
				addCodeSystems(retVal.getCodeSystems9007(), nextSystem.getDrugGiveCodeSystemRxe2(), hspId9004, "Drug Give Code (RXE-2) for system: " + nextSystem.getDescription());				
			}
			
		}
		
	    return retVal;
    }


	private List<Code> toCodes(List<ca.cgta.input.model.config.Code> theOtherOids) {
		ArrayList<Code> retVal = new ArrayList<Code>();
		for (ca.cgta.input.model.config.Code code : theOtherOids) {
			Code newCode = new Code(code.getCode(), code.getDescription());
			retVal.add(newCode);
        }
	    return retVal;
    }


	private void addCodeSystems(List<Code> theDest, List<String> theCodesToUse, String theHspId9004, String theUsage) {
		for (String next : theCodesToUse) {
			theDest.add(new Code(next, "Code System for use in " + theUsage, theHspId9004));
		}
    }

}
