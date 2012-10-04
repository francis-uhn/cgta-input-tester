package ca.cgta.input.converter;

import static org.apache.commons.lang.StringUtils.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.cgta.input.model.config.Code;
import ca.cgta.input.model.config.Contributor;
import ca.cgta.input.model.config.ContributorConfigFactory;
import ca.cgta.input.model.config.SendingSystem;
import ca.cgta.input.model.inner.AdverseReaction;
import ca.cgta.input.model.inner.AssociatedParty;
import ca.cgta.input.model.inner.Ce;
import ca.cgta.input.model.inner.ClinicalDocumentData;
import ca.cgta.input.model.inner.ClinicalDocumentSection;
import ca.cgta.input.model.inner.ConfidentialityStatusEnum;
import ca.cgta.input.model.inner.Cx;
import ca.cgta.input.model.inner.Diagnosis;
import ca.cgta.input.model.inner.Ei;
import ca.cgta.input.model.inner.MedicationAdmin;
import ca.cgta.input.model.inner.MedicationComponent;
import ca.cgta.input.model.inner.MedicationOrder;
import ca.cgta.input.model.inner.Mrg;
import ca.cgta.input.model.inner.Note;
import ca.cgta.input.model.inner.Patient;
import ca.cgta.input.model.inner.PersonInRole;
import ca.cgta.input.model.inner.Pl;
import ca.cgta.input.model.inner.Tables;
import ca.cgta.input.model.inner.Visit;
import ca.cgta.input.model.inner.Xad;
import ca.cgta.input.model.inner.Xcn;
import ca.cgta.input.model.inner.Xpn;
import ca.cgta.input.model.inner.Xtn;
import ca.cgta.input.model.outer.ClinicalDocumentGroup;
import ca.cgta.input.model.outer.MedicationOrderWithAdmins;
import ca.cgta.input.model.outer.PatientWithVisits;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.GenericSegment;
import ca.uhn.hl7v2.model.Primitive;
import ca.uhn.hl7v2.model.Structure;
import ca.uhn.hl7v2.model.primitive.AbstractTextPrimitive;
import ca.uhn.hl7v2.model.v25.datatype.CE;
import ca.uhn.hl7v2.model.v25.datatype.CNE;
import ca.uhn.hl7v2.model.v25.datatype.CNN;
import ca.uhn.hl7v2.model.v25.datatype.CX;
import ca.uhn.hl7v2.model.v25.datatype.DT;
import ca.uhn.hl7v2.model.v25.datatype.ED;
import ca.uhn.hl7v2.model.v25.datatype.EI;
import ca.uhn.hl7v2.model.v25.datatype.FT;
import ca.uhn.hl7v2.model.v25.datatype.HD;
import ca.uhn.hl7v2.model.v25.datatype.NDL;
import ca.uhn.hl7v2.model.v25.datatype.NM;
import ca.uhn.hl7v2.model.v25.datatype.PL;
import ca.uhn.hl7v2.model.v25.datatype.PRL;
import ca.uhn.hl7v2.model.v25.datatype.SN;
import ca.uhn.hl7v2.model.v25.datatype.TM;
import ca.uhn.hl7v2.model.v25.datatype.TS;
import ca.uhn.hl7v2.model.v25.datatype.XAD;
import ca.uhn.hl7v2.model.v25.datatype.XCN;
import ca.uhn.hl7v2.model.v25.datatype.XPN;
import ca.uhn.hl7v2.model.v25.datatype.XTN;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_OBSERVATION;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_PATIENT_RESULT;
import ca.uhn.hl7v2.model.v25.group.RAS_O17_ADMINISTRATION;
import ca.uhn.hl7v2.model.v25.group.RAS_O17_ORDER;
import ca.uhn.hl7v2.model.v25.group.RDE_O11_ORDER;
import ca.uhn.hl7v2.model.v25.message.ADT_A01;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.message.RAS_O17;
import ca.uhn.hl7v2.model.v25.message.RDE_O11;
import ca.uhn.hl7v2.model.v25.segment.DG1;
import ca.uhn.hl7v2.model.v25.segment.IAM;
import ca.uhn.hl7v2.model.v25.segment.MRG;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.model.v25.segment.NK1;
import ca.uhn.hl7v2.model.v25.segment.NTE;
import ca.uhn.hl7v2.model.v25.segment.ORC;
import ca.uhn.hl7v2.model.v25.segment.PID;
import ca.uhn.hl7v2.model.v25.segment.PV1;
import ca.uhn.hl7v2.model.v25.segment.PV2;
import ca.uhn.hl7v2.model.v25.segment.ROL;
import ca.uhn.hl7v2.model.v25.segment.RXA;
import ca.uhn.hl7v2.model.v25.segment.RXC;
import ca.uhn.hl7v2.model.v25.segment.RXE;
import ca.uhn.hl7v2.model.v25.segment.RXR;
import ca.uhn.hl7v2.util.Terser;

public class Converter {

    public static final String INPUT_PROFILE_2_0 = "CGTA_CDR_INPUT_2_0";
	private static DateFormat ourTsHourFormat = new SimpleDateFormat("yyyyMMddHH");
    private static DateFormat ourTsLongFormat = new SimpleDateFormat("yyyyMMddHHmmssZ");
	private static DateFormat ourDtFormat = new SimpleDateFormat("yyyyMMdd");
    private static DateFormat ourTsMonthFormat = new SimpleDateFormat("yyyyMM");
    private static DateFormat ourTsYearFormat = new SimpleDateFormat("yyyy");
    	
	private static final Logger ourLog = LoggerFactory.getLogger(Converter.class.getName());

	private HashSet<FailureCode> myFailureCodes = new HashSet<FailureCode>();
	private List<Failure> myFailures = new ArrayList<Failure>();
	private ContributorConfigFactory myAuthorization;
	private Contributor myContributorConfig;
	private boolean myCheckSecurity;
	private SendingSystem mySendingSystem;
	private Boolean myValidatedMsh;
	private boolean myEnforceUnsupported;


	public Converter(boolean theCheckSecurity) throws JAXBException {
		myAuthorization = ContributorConfigFactory.getInstance();
		myCheckSecurity = theCheckSecurity;
	}


	public Converter() throws JAXBException {
		this(false);
	}


	public void addFailure(String theTerserPath, FailureCode theFailureCode, String theFieldVal) {
		if (myFailureCodes.contains(theFailureCode)) {
			ourLog.warn("Not adding message failure because we already have this failure code, in \"" + theTerserPath + "\": " + theFailureCode.getDesc());
		} else {
			myFailureCodes.add(theFailureCode);
			myFailures.add(new Failure(theTerserPath, theFailureCode, theFailureCode.getDesc(), theFieldVal));

			if (isNotBlank(theFieldVal)) {
				ourLog.warn("Message failure " + theFailureCode + " in \"" + theTerserPath + "\": " + theFailureCode.getDesc() + "\n * Field Value: " + theFieldVal);
			} else {
				ourLog.warn("Message failure " + theFailureCode + " in \"" + theTerserPath + "\": " + theFailureCode.getDesc());
			}
		}
	}
	
	/**
	 * If true, generate a failure code if data exists in an unsupported field
	 */
	public void setEnforceUnsupported(boolean theEnforceUnsupported) {
		myEnforceUnsupported = theEnforceUnsupported;
	}
	
	
    public PatientWithVisits convertPatientWithVisits(ADT_A01 theAdt) throws HL7Exception {
        if (!validateMsh(theAdt.getMSH())) {
            return null;
        }

        String triggerEvt = theAdt.getMSH().getMsh9_MessageType().getMsg2_TriggerEvent().getValue();
        Set<String> triggersWithStructureAdtA01 = new HashSet<String>();
        triggersWithStructureAdtA01.add("A01");
        triggersWithStructureAdtA01.add("A02");
        triggersWithStructureAdtA01.add("A03");
        triggersWithStructureAdtA01.add("A04");
        triggersWithStructureAdtA01.add("A05");
        triggersWithStructureAdtA01.add("A06");
        triggersWithStructureAdtA01.add("A07");
        triggersWithStructureAdtA01.add("A08");
        triggersWithStructureAdtA01.add("A10");
        triggersWithStructureAdtA01.add("A11");
        triggersWithStructureAdtA01.add("A13");
        triggersWithStructureAdtA01.add("A28");
        triggersWithStructureAdtA01.add("A31");
        triggersWithStructureAdtA01.add("A60");
        if (triggersWithStructureAdtA01.contains(triggerEvt)) {
        	try {
        		List<String> segments = Arrays.asList(theAdt.getNames());
        		if (!segments.contains("MRG")) {
        			theAdt.addNonstandardSegment("MRG");
        		}
        		if (!segments.contains("IAM")) {
        			theAdt.addNonstandardSegment("IAM");
        		}
        		
//        		RuntimeProfile pp = new ProfileParser(false).parseClasspath("profile_adt_a01.xml");
//        		HL7Exception[] validationResults = new DefaultValidator().validate(theAdt, pp.getMessage());
//        		processProfileValidationResults(validationResults);
        	} catch (Exception e) {
        		throw new HL7Exception(e);
        	}
        }
        
        
        PatientWithVisits retVal = new PatientWithVisits();

        retVal.myMostRecentEventCode = theAdt.getMSH().getMsh9_MessageType().getMsg2_TriggerEvent().getValue();
        if (Tables.lookupHl7Code("0003ADT", retVal.myMostRecentEventCode) == null) {
            addFailure("MSH-9-2", FailureCode.F092, theAdt.getMSH().getMsh9_MessageType().encode());
        }

        retVal.myAdminHistory = new ArrayList<String>();

        retVal.myPatient = convertPid("PID", theAdt.getPID());

        if (!retVal.myPatient.hasIdWithTypeMr()) {
            addFailure("PID-3", FailureCode.F060, null);
            return null;
        }

        // ROL
        retVal.myPatient.myPersonInRoles = new ArrayList<PersonInRole>();
        int index = 1;
        for (ROL next : theAdt.getROLAll()) {
            if (next.encode().length() < 5) {
                continue;
            }

            PersonInRole rol = convertRol("ROL(" + index + ")", next);
            if (rol != null) {
                retVal.myPatient.myPersonInRoles.add(rol);
            }
            index++;
        }
        index = 1;
        for (ROL next : theAdt.getROL2All()) {
            if (next.encode().length() < 5) {
                continue;
            }

            PersonInRole rol = convertRol("ROL2(" + index + ")", next);
            if (rol != null) {
                retVal.myPatient.myPersonInRoles.add(rol);
            }
            index++;
        }

        // NK1
        retVal.myPatient.myAssociatedParties = new ArrayList<AssociatedParty>();
        index = 1;
        for (NK1 next : theAdt.getNK1All()) {
        	if (next.encode().length() <= 4) {
        		continue;
        	}
        	
            AssociatedParty nk1 = convertNk1("NK1(" + index + ")", next);
            if (nk1 != null) {
                retVal.myPatient.myAssociatedParties.add(nk1);
            }
            index++;
        }

        // IAM
        retVal.myPatient.myAdverseReactions = new ArrayList<AdverseReaction>();
        index = 1;
        if (theAdt.getNonStandardNames().contains("IAM")) {
            for (Structure next : theAdt.getAll("IAM")) {
                IAM iam = (IAM) next;
                AdverseReaction adv = convertIam("IAM(" + index + ")", iam);
                if (adv != null) {
                    retVal.myPatient.myAdverseReactions.add(adv);
                }
                index++;
            }
        }
        
        retVal.myVisits = new ArrayList<Visit>();

        boolean processVisit = true;
        boolean processRecordLock = true;
        
        if ("A31".equals(retVal.myMostRecentEventCode)) {
            processVisit = false;
            if (theAdt.getNonStandardNames().contains("ZPD")) {
                GenericSegment zpd = (GenericSegment) theAdt.get("ZPD");                
                retVal.myPatient.myDeactivatePatientIndicator =  Terser.get(zpd, 1, 0, 1, 1);                
            }
        }    
        
        
        if ("A28".equals(retVal.myMostRecentEventCode)) {
            processVisit = false;
        }

        if ("A40".equals(retVal.myMostRecentEventCode)) {
            processVisit = false;
            processRecordLock = false;
        }

        // Unlink person
        if ("A37".equals(retVal.myMostRecentEventCode)) {
            processVisit = false;
            processRecordLock = false;
            PID pid2 = (PID) theAdt.get("PID2");
            retVal.myUnlinkSecondPatient = convertPid("PID2", pid2);
            if (retVal.myUnlinkSecondPatient == null || retVal.myUnlinkSecondPatient.hasIdWithTypeMr() == false) {
                addFailure("PID2-3", FailureCode.F059, null);
            }
        }

        // Swap beds
        if ("A17".equals(retVal.myMostRecentEventCode)) {
            // get second PID mrn
            PID pid2 = (PID) theAdt.get("PID2");
            retVal.myBedSwapSecondPatientMrn = obtainPidMrn("PID2", pid2);
            if (retVal.myBedSwapSecondPatientMrn == null) {
                addFailure("PID2-3", FailureCode.F085, null);
            }
            // get second PV1 visitNumber
            PV1 pv12 = (PV1) theAdt.get("PV12");
            retVal.myBedSwapSecondPatientVisitNumber = obtainPv1VisitNumber("PV12", pv12);
            if (retVal.myBedSwapSecondPatientVisitNumber == null) {
                addFailure("PV12-19", FailureCode.F086, null);
            }
            // get second PV1 location
            retVal.myBedSwapSecondPatientLocation = obtainPv1Loc("PV12", pv12);
            if (retVal.myBedSwapSecondPatientLocation == null) {
                addFailure("PV12-3", FailureCode.F087, null);
            }
        }

        
        if (processVisit) {
            Visit pv1 = convertPv1("PV1", theAdt.getPV1());

            if (pv1.myVisitNumber == null) {
            	addFailure("PV1-19", FailureCode.F128, null);
            }
            
            convertPv2("PV2", theAdt.getPV2(), pv1);

            // DG1
            pv1.myDiagnoses = new ArrayList<Diagnosis>();
            for (DG1 next : theAdt.getDG1All()) {
                Diagnosis dg1 = convertDg1("DG1", next);
                if (dg1 != null) {
                    pv1.myDiagnoses.add(dg1);
                }
            }
            retVal.myVisits.add(pv1);
        }
        
        
        
        if (processRecordLock) {            
            addRecordLockIndicator(theAdt.getPV1(),retVal.myPatient);            
        }
        
        
        

        if (theAdt.getNonStandardNames().contains("MRG")) {
            retVal.myMergeInfo = convertMrg("MRG", (MRG) theAdt.get("MRG"));
        }

        if ("A40".equals(retVal.myMostRecentEventCode)) {
            if (retVal.myMergeInfo == null || retVal.myMergeInfo.hasIdWithTypeMr() == false) {
                addFailure("MRG-1", FailureCode.F063, null);
            }
            if (retVal.myMergeInfo != null && retVal.myMergeInfo.hasMultipleIdWithTypeMr()) {
                addFailure("MRG-1", FailureCode.F064, null);
            }
        }

        if ("A45".equals(retVal.myMostRecentEventCode)) {
            if (retVal.myMergeInfo == null || retVal.myMergeInfo.hasIdWithTypeMr() == false) {
                addFailure("MRG-1", FailureCode.F063, null);
            }
            if (retVal.myMergeInfo != null && retVal.myMergeInfo.hasMultipleIdWithTypeMr()) {
                addFailure("MRG-1", FailureCode.F064, null);
            }

            if (retVal.myMergeInfo != null && retVal.myMergeInfo.myMergeVisitId == null) {
                addFailure("MRG-5", FailureCode.F065, null);
            }
        }

        List<String> inpatientVisitTypes = Arrays.asList("I", "P", "U");
        if ("A01".equals(retVal.myMostRecentEventCode) && !inpatientVisitTypes.contains(retVal.myVisits.get(0).myPatientClassCode)) {
        	addFailure("PV1-2", FailureCode.F129, null);
        }

        List<String> outpatientVisitTypes = Arrays.asList("E", "O", "R", "U");
        if ("A04".equals(retVal.myMostRecentEventCode) && !outpatientVisitTypes.contains(retVal.myVisits.get(0).myPatientClassCode)) {
        	addFailure("PV1-2", FailureCode.F130, null);
        }

        if ("A06".equals(retVal.myMostRecentEventCode) || "A07".equals(retVal.myMostRecentEventCode)) {
            if (retVal.myMergeInfo == null || retVal.myMergeInfo.myMergeVisitId == null) {
                addFailure("MRG-5", FailureCode.F091, null);
            }
        }

        return retVal;
    }	
	
	
	
    private void processProfileValidationResults(HL7Exception[] theValidationResults) {
    	for (HL7Exception nextException : theValidationResults) {
    		if (nextException.getMessage().startsWith("Event type") && nextException.getMessage().contains("doesn't match profile type of")) {
    			continue;
    		}
    		if (nextException.getMessage().startsWith("Message structure") && nextException.getMessage().contains("doesn't match profile type of")) {
    			continue;
    		}
    		
	        ourLog.info("Validation exception: " + nextException);
	        nextException.toString();
        }
    }


	public List<ClinicalDocumentGroup> convertClinicalDocument(ORU_R01 theDocument) throws HL7Exception {

        if (!validateMsh(theDocument.getMSH())) {
            return Collections.emptyList();
        }

        int i = 0;
        Map<String, ClinicalDocumentGroup> placerGroupsToDocuments = new HashMap<String, ClinicalDocumentGroup>();
        for (ORU_R01_PATIENT_RESULT nextPatientResult : theDocument.getPATIENT_RESULTAll()) {
            i++;

            String terserPath = "/PATIENT_RESULT(" + i + ")";

            ourLog.info("Processing {}", terserPath);

            convert(terserPath, nextPatientResult, placerGroupsToDocuments);
        }

        List<ClinicalDocumentGroup> retVal = new ArrayList<ClinicalDocumentGroup>();
        retVal.addAll(placerGroupsToDocuments.values());
        return retVal;
    }
    
    
    
    public List<MedicationOrderWithAdmins> convertMedicationAdmin(RAS_O17 theRas) throws HL7Exception {
        if (!validateMsh(theRas.getMSH())) {
            return Collections.emptyList();
        }

        List<MedicationOrderWithAdmins> retVal = new ArrayList<MedicationOrderWithAdmins>();

        int index = 0;
        for (RAS_O17_ORDER nextOrderObj : theRas.getORDERAll()) {
            index++;

            MedicationOrder nextOrder = new MedicationOrder();
            MedicationOrderWithAdmins owa = new MedicationOrderWithAdmins(nextOrder);
            owa.myRecordUpdatedDate = new Date();
            owa.myRecordUpdatedDateFormatted = DateFormatter.formatDateWithGmt(owa.myRecordUpdatedDate);
                

            if (isBlank(nextOrderObj.getORC().getOrc2_PlacerOrderNumber().getEi1_EntityIdentifier().getValue())) {
                addFailure("ORDER(" + index + ")/ORC-2", FailureCode.F093, null);
                continue;
            }

            nextOrder.myPatient = convertPid("PATIENT/PID", theRas.getPATIENT().getPID());
            nextOrder.myVisit = convertPv1("PATIENT/PATIENT_VISIT/PV1", theRas.getPATIENT().getPATIENT_VISIT().getPV1());

            convertOrcForMedOrder(index, nextOrder, nextOrderObj.getORC());
            if (nextOrder.myPlacerOrderNumber == null) {
                ourLog.warn("Did not find a placer order number, so I am going to skip this order");
                continue;
            }
            
            //obtain optional ENCODING group and convert/extract the data
            //NOTE, if RXE is not present then we may assume that the ENCODING GROUP is empty
            if ( nextOrderObj.getENCODING().getRXE().encode().length() >= 5 ) {
                
                
                convertRxe("ORDER(" + index + ")/ENCODING", nextOrder, nextOrderObj.getENCODING().getRXE());
                
                // Process Pharmacy Routes for Encoding group (optional and repeating)
                convertMedicationRoutes("ORDER(" + index + ")/ENCODING", nextOrderObj.getENCODING().getRXRAll(), nextOrder.myMedicationRoutes);

                // Process Components for Encoding group (optional and repeating)
                convertMedicationComponents("ORDER(" + index + ")/ENCODING", nextOrderObj.getENCODING().getRXCAll(), nextOrder.myMedicationComponents);
                
                //Do a check on a mandatory med order field before setting the overwrite flag 
                if ( nextOrder.myEncodedOrderGiveCode != null ) {
                    owa.overwriteExistingMedOrder = true;                    
                }
                
            }
            else {
                //We don't have any medication order content, so if the med order exists in the database
                //we won't overwrite it
                owa.overwriteExistingMedOrder = false;
            }
                        

            List<MedicationAdmin> admins = new ArrayList<MedicationAdmin>();
            owa.myAdmins = admins;

            int administrationIndex = 0;
            for (RAS_O17_ADMINISTRATION nextAdminGroup : nextOrderObj.getADMINISTRATIONAll()) {
                administrationIndex++;

                int rxaIndex = 0;
                for (RXA nextRxa : nextAdminGroup.getRXAAll()) {
                    rxaIndex++;
                    String rxaTerser = "ORDER(" + index + ")/ADMINISTRATION(" + administrationIndex + ")/RXA(" + rxaIndex + ")";

                    MedicationAdmin nextAdmin = new MedicationAdmin();
                    
                    //set the placer order number 
                    nextAdmin.myPlacerOrderNumber = nextOrder.myPlacerOrderNumber;

                    // RXA-2 (optional field)
                    if (isNotBlank(nextRxa.getRxa2_AdministrationSubIDCounter().getValue())) {                  
                        nextAdmin.myAdministrationNumber = toNumber(rxaTerser + "-2", nextRxa.getRxa2_AdministrationSubIDCounter().getValue());
                    }                   

                    // RXA-3 (start time)
                    if (isBlank(nextRxa.getRxa3_DateTimeStartOfAdministration().getTs1_Time().getValue())) {
                        addFailure(rxaTerser + "-3", FailureCode.F100, null);
                    } else {
                        if (validateTsWithAtLeastMinutePrecisionAndAddFailure(rxaTerser + "-3", nextRxa.getRxa3_DateTimeStartOfAdministration().getTs1_Time().getValue())) {
                        	nextAdmin.myStartTime = nextRxa.getRxa3_DateTimeStartOfAdministration().getTs1_Time().getValueAsDate();
                        	nextAdmin.myStartTimeFormatted = DateFormatter.formatDate(nextRxa.getRxa3_DateTimeStartOfAdministration().getTs1_Time().getValue());
                        }
                    }
                    
                    // RXA-4 (end time) (optional field)
                    if (isNotBlank(nextRxa.getRxa4_DateTimeEndOfAdministration().getTs1_Time().getValue())) {
                        if (validateTsWithAtLeastMinutePrecisionAndAddFailure(rxaTerser + "-4", nextRxa.getRxa4_DateTimeEndOfAdministration().getTs1_Time().getValue())) {
                        	nextAdmin.myEndTime = nextRxa.getRxa4_DateTimeEndOfAdministration().getTs1_Time().getValueAsDate();
                        	nextAdmin.myEndTimeFormatted = DateFormatter.formatDate(nextRxa.getRxa4_DateTimeEndOfAdministration().getTs1_Time().getValue());                        
                        }
                    }

                    
                    //RXA-5 (optional field)
                    if ( isNotBlank(nextRxa.getRxa5_AdministeredCode().encode()) ) {
                        nextAdmin.myAdministeredCode = convertCeWithOptionalIdentifier(rxaTerser + "-5", nextRxa.getRxa5_AdministeredCode());
                        
                        if (isNotBlank(nextAdmin.myAdministeredCode.myCode)  && !mySendingSystem.getDrugAdministrationCodeSystemRxa5().contains(nextAdmin.myAdministeredCode.myCodeSystem)) {
                            addFailure(rxaTerser + "-5-3", FailureCode.F123, nextRxa.getRxa5_AdministeredCode().encode());
                        }
                    }
                    
                    //RXA-6 (optional field)
                    if (isNotBlank(nextRxa.getRxa6_AdministeredAmount().getValue())) {
                        nextAdmin.myAdministeredAmount = toNumber(rxaTerser + "-6", nextRxa.getRxa6_AdministeredAmount().getValue());
                    }

                    // RXA-7 (Units) (optional field)
                    if (isNotBlank(nextRxa.getRxa7_AdministeredUnits().encode())) {
                        nextAdmin.myAdministeredUnits = convertCe(rxaTerser + "-7", nextRxa.getRxa7_AdministeredUnits());
                    }

                    // RXA-9 (Admin Notes)
                    nextAdmin.myAdministrationNotes = new ArrayList<Ce>();
                    for (int i = 0; i < nextRxa.getRxa9_AdministrationNotesReps(); i++) {
                        CE ce = nextRxa.getRxa9_AdministrationNotes(i);
                        if (ce != null && (isNotBlank(ce.getCe1_Identifier().getValue()) || isNotBlank(ce.getCe2_Text().getValue()))) {
                            nextAdmin.myAdministrationNotes.add(convertCeWithOptionalIdentifier(rxaTerser + "-9(" + (i + 1) + ")", ce));                            
                        }
                    }
                    
                    //RXA-12 (optional field)
                    nextAdmin.myAdministeredPerTimeUnit = nextRxa.getRxa12_AdministeredPerTimeUnit().getValue();
                    
                    
                    //NOTE:  Process optional Pharmacy Route for Administration group but add the data to the MedicationAdmin object even though it will be 
                    //the same info added to each MedicationAdmin object created from the Administration group
                    if ( nextAdminGroup.getRXR().encode().length() >= 5 ) {
                        convertMedicationRoute("ORDER(" + index + ")/ADMINISTRATION(" + administrationIndex + ")/RXR", nextAdminGroup.getRXR(), nextAdmin);                        
                    }                    
                    admins.add(nextAdmin);
                }
            }
            retVal.add(owa);
        }
        return retVal;
    }
    


    public List<MedicationOrder> convertMedicationOrder(RDE_O11 theRde) throws HL7Exception {
        if (!validateMsh(theRde.getMSH())) {
            return Collections.emptyList();
        }

        List<MedicationOrder> retVal = new ArrayList<MedicationOrder>();

        int index = 0;
        for (RDE_O11_ORDER nextOrderObj : theRde.getORDERAll()) {
            index++;

            MedicationOrder nextOrder = new MedicationOrder();

            if (isBlank(nextOrderObj.getORC().getOrc2_PlacerOrderNumber().getEi1_EntityIdentifier().getValue())) {
                addFailure("ORDER(" + index + ")/ORC-2", FailureCode.F093, null);
                continue;
            }

            nextOrder.myPatient = convertPid("PATIENT/PID", theRde.getPATIENT().getPID());
            nextOrder.myVisit = convertPv1("PATIENT/PATIENT_VISIT/PV1", theRde.getPATIENT().getPATIENT_VISIT().getPV1());

            convertOrcForMedOrder(index, nextOrder, nextOrderObj.getORC());
            if (nextOrder.myPlacerOrderNumber == null) {
                ourLog.warn("Did not find a placer order number, so I am going to skip this order");
                continue;
            }
            
            //Process encoded order
            convertRxe("ORDER(" + index + ")", nextOrder, nextOrderObj.getRXE());
            
            //Process Notes/Comments for Encoded Order group (optional and repeating)
            convertNotes("ORDER(" + index + ")", nextOrderObj.getNTEAll(), nextOrder.myNotes);
            
            //Process Pharmacy Routes for Encoded Order group (optional and repeating)
            convertMedicationRoutes("ORDER(" + index + ")", nextOrderObj.getRXRAll(), nextOrder.myMedicationRoutes);
            
            //Process Components Encoded Order group (optional and repeating)
            convertMedicationComponents("ORDER(" + index + ")", nextOrderObj.getRXCAll(), nextOrder.myMedicationComponents);                        

            retVal.add(nextOrder);
            
        }

        return retVal;
    }    
	
	
	


	private void convert(String theTerserPath, ORU_R01_PATIENT_RESULT thePatientResult, Map<String, ClinicalDocumentGroup> thePlacerGroupsToDocuments) throws HL7Exception {

		int orderObservationIndex = 0;
		for (ORU_R01_ORDER_OBSERVATION nextOrderObservation : thePatientResult.getORDER_OBSERVATIONAll()) {
			orderObservationIndex++;

			Ei placerGroupNumber = null;
			if (isNotBlank(nextOrderObservation.getORC().getPlacerGroupNumber().getEi1_EntityIdentifier().getValue())) {
				placerGroupNumber = convertEiDocumentNumber(theTerserPath + "/ORDER_OBSERVATION(" + orderObservationIndex + ")/ORC-4", nextOrderObservation.getORC().getPlacerGroupNumber());
			}
			if (placerGroupNumber != null && !placerGroupNumber.isValid()) {
				continue;
			}
			
			Ei placerOrderNumber = convertEiDocumentNumber(theTerserPath + "/ORDER_OBSERVATION(" + orderObservationIndex + ")/OBR-2", nextOrderObservation.getOBR().getObr2_PlacerOrderNumber());
			if (placerOrderNumber == null || !placerOrderNumber.isValid()) {
				continue;
			}

			/*
			 * More than one ORDER_OBSERVATION can be stored in a single group using a placer group number,
			 * but the default behaviour is for the placer order number in OBR-2 to be used as the document ID.
			 * 
			 * This could in theory cause issues if the numbers overlap, which they shouldn't.
			 */
			if (placerGroupNumber == null) {
				placerGroupNumber = placerOrderNumber;
			}
			
			if (thePlacerGroupsToDocuments.containsKey(placerGroupNumber.toKey()) == false) {
				ClinicalDocumentGroup documentGroup = new ClinicalDocumentGroup();
				documentGroup.mySections = new ArrayList<ClinicalDocumentSection>();
				documentGroup.myPlacerGroupNumber = placerGroupNumber;
				documentGroup.myRecordUpdatedDate = new Date();
				documentGroup.myRecordUpdatedDateFormatted = DateFormatter.formatDateWithGmt(documentGroup.myRecordUpdatedDate);
				thePlacerGroupsToDocuments.put(placerGroupNumber.toKey(), documentGroup);
			}

			ClinicalDocumentGroup document = thePlacerGroupsToDocuments.get(placerGroupNumber.toKey());			
			document.myPatient = convertPid(theTerserPath + "/PATIENT/PID", thePatientResult.getPATIENT().getPID());
            
			
            if (thePatientResult.getPATIENT().getVISIT().getPV1().encode().length() >= 5 ) {
                document.myVisit = convertPv1(theTerserPath + "/PATIENT/VISIT/PV1", thePatientResult.getPATIENT().getVISIT().getPV1());
                
                if (thePatientResult.getPATIENT().getVISIT().getPV2().encode().length() >= 5 ) {
                    convertPv2(theTerserPath + "/PATIENT/VISIT/PV2", thePatientResult.getPATIENT().getVISIT().getPV2(), document.myVisit);
                }
                
            }

			ClinicalDocumentSection section = new ClinicalDocumentSection();
			section.myData = new ArrayList<ClinicalDocumentData>();
			section.mySectionId = placerOrderNumber;

			CE usi = nextOrderObservation.getOBR().getObr4_UniversalServiceIdentifier();
			section.mySectionCode = convertCe(theTerserPath + "/ORDER_OBSERVATION(" + orderObservationIndex + ")/OBR-4", usi);

			if (section.mySectionCode == null || isBlank(section.mySectionCode.myText)) {
				addFailure(theTerserPath + "/ORDER_OBSERVATION(" + orderObservationIndex + ")/OBR-4", FailureCode.F011, usi.encode());
			}
			
			if (section.mySectionCode != null && (isBlank(section.mySectionCode.myCodeSystem) || !mySendingSystem.getRequestCodeSystemSystemObr4().contains(section.mySectionCode.myCodeSystem))) {
				addFailure(theTerserPath + "/ORDER_OBSERVATION(" + orderObservationIndex + ")/OBR-4", FailureCode.F012, usi.encode());
			}

			section.mySectionName = usi.getCe2_Text().getValue();

			if (isBlank(section.mySectionName)) {
				addFailure(theTerserPath + "/ORDER_OBSERVATION(" + orderObservationIndex + ")/OBR-4-2", FailureCode.F003, usi.encode());
			}

			// OBR-7
			if (nextOrderObservation.getOBR().getObr7_ObservationDateTime().encode().isEmpty()) {
				addFailure(theTerserPath + "/ORDER_OBSERVATION(" + orderObservationIndex + ")/OBR-7", FailureCode.F004, nextOrderObservation.getOBR().getObr7_ObservationDateTime().encode());
			} else {
				String dt = nextOrderObservation.getOBR().getObr7_ObservationDateTime().getTime().getValue();
				if (validateTsWithAtLeastSecondPrecisionAndAddFailure(theTerserPath + "/ORDER_OBSERVATION(" + orderObservationIndex + ")/OBR-7", dt)) {
					section.myDate = nextOrderObservation.getOBR().getObr7_ObservationDateTime().getTime().getValueAsDate();
					section.myDateFormatted = DateFormatter.formatDate(nextOrderObservation.getOBR().getObr7_ObservationDateTime().getTime().getValue());
				}
			}

			// OBR-8
			if (!nextOrderObservation.getOBR().getObr8_ObservationEndDateTime().encode().isEmpty()) {
				String dt = nextOrderObservation.getOBR().getObr8_ObservationEndDateTime().getTime().getValue();
				if (validateTsWithAtLeastSecondPrecisionAndAddFailure(theTerserPath + "/ORDER_OBSERVATION(" + orderObservationIndex + ")/OBR-8", dt)) {
					section.myEndDate = nextOrderObservation.getOBR().getObr8_ObservationEndDateTime().getTime().getValueAsDate();
					section.myEndDateFormatted = DateFormatter.formatDate(nextOrderObservation.getOBR().getObr8_ObservationEndDateTime().getTime().getValue());
				}
			}

			section.myOrderingProviders = new ArrayList<Xcn>();
			for (int j = 1; j <= nextOrderObservation.getOBR().getObr16_OrderingProviderReps(); j++) {
				section.myOrderingProviders.add(convertXcn(theTerserPath + "/ORDER_OBSERVATION(" + orderObservationIndex + ")/OBR-16(" + j + ")", nextOrderObservation.getOBR().getObr16_OrderingProvider(j - 1)));
			}
			if (section.myOrderingProviders.size() > 10) {
				addFailure(theTerserPath + "/ORDER_OBSERVATION(" + orderObservationIndex + ")/OBR-16", FailureCode.F071, null);
			}

			String resultConfidentiality = nextOrderObservation.getOBR().getObr18_PlacerField1().getValue();
			if (StringUtils.isBlank(resultConfidentiality)) {
				section.myConfidentiality = ConfidentialityStatusEnum.NORMAL;
			} else {
				section.myConfidentiality = ConfidentialityStatusEnum.fromHl7Code(resultConfidentiality);
				if (section.myConfidentiality == null) {
					addFailure(theTerserPath + "/ORDER_OBSERVATION(" + orderObservationIndex + ")/OBR-18", FailureCode.F005, resultConfidentiality);
				}
			}

			// OBR-20
			if (StringUtils.isNotBlank(nextOrderObservation.getOBR().getObr20_FillerField1().getValue())) {
				if (Tables.lookupHl7Code("9006", nextOrderObservation.getOBR().getObr20_FillerField1().getValue()) == null) {
					addFailure(theTerserPath + "/ORDER_OBSERVATION(" + orderObservationIndex + ")/OBR-20", FailureCode.F006, nextOrderObservation.getOBR().getObr20_FillerField1().getValue());
				}
			}

			String resultStatus = nextOrderObservation.getOBR().getObr25_ResultStatus().getValue();
			section.myStatusCode = resultStatus;
			section.myStatus = Tables.lookupHl7Code("0085", section.myStatusCode);
			if (section.myStatus == null) {
				addFailure(theTerserPath + "/ORDER_OBSERVATION(" + orderObservationIndex + ")/OBR-25", FailureCode.F007, resultStatus);
			}

			section.myCopyToProviders = new ArrayList<Xcn>();
			for (int j = 1; j <= nextOrderObservation.getOBR().getObr28_ResultCopiesToReps(); j++) {
				section.myCopyToProviders.add(convertXcn(theTerserPath + "/ORDER_OBSERVATION(" + orderObservationIndex + ")/OBR-28(" + j + ")", nextOrderObservation.getOBR().getObr28_ResultCopiesTo(j - 1)));
			}
			if (section.myCopyToProviders.size() > 10) {
				addFailure(theTerserPath + "/ORDER_OBSERVATION(" + orderObservationIndex + ")/OBR-28", FailureCode.F071, null);
			}

			if (isNotBlank(nextOrderObservation.getOBR().getObr26_ParentResult().encode())) {
				section.myParentSectionId = convertPrlDocumentNumber(theTerserPath + "/ORDER_OBSERVATION(" + orderObservationIndex + ")/OBR-26", nextOrderObservation.getOBR().getObr26_ParentResult());
			}

			if (isNotBlank(nextOrderObservation.getOBR().getObr32_PrincipalResultInterpreter().encode())) {
				section.myPrincipalInterpreter = convertNdl(theTerserPath + "/ORDER_OBSERVATION(" + orderObservationIndex + ")/OBR-32", nextOrderObservation.getOBR().getObr32_PrincipalResultInterpreter());
			}
			
			//Get Order Notes and Comments
			convertNotes(theTerserPath + "/ORDER_OBSERVATION(" + orderObservationIndex + ")", nextOrderObservation.getNTEAll(), section.myNotes);
			
			document.mySections.add(section);

			String appendMode = nextOrderObservation.getOBR().getObr19_PlacerField2().getValue();
			if (isBlank(appendMode)) {
				// Snapshot is the default
				appendMode = "S";
			} else {
				if (Tables.lookupHl7Code("9009", appendMode) == null) {
					addFailure(theTerserPath + "/ORDER_OBSERVATION(" + orderObservationIndex + ")/OBR-19", FailureCode.F127, appendMode);
					appendMode = "S";
				}
			}
			section.myAppendMode = appendMode;
			
			// OBX			
			ClinicalDocumentData prevDocData = null;
			for (int k = 1; k <= nextOrderObservation.getOBSERVATIONReps(); k++) {
				if (nextOrderObservation.getOBSERVATION(k-1).getOBX().encode().length() < 5) {
					continue;
				}
			    
				int setId = toNumber(theTerserPath + "/ORDER_OBSERVATION(" + orderObservationIndex + ")/OBSERVATION(" + k + ")/OBX-1", nextOrderObservation.getOBSERVATION(k - 1).getOBX().getSetIDOBX().getValue());
				if (setId != k) {
					addFailure(theTerserPath + "/ORDER_OBSERVATION(" + orderObservationIndex + ")/OBSERVATION(" + k + ")/OBX-1", FailureCode.F008, nextOrderObservation.getOBSERVATION(k - 1).getOBX().getSetIDOBX().encode());
				}

				ClinicalDocumentData nextDocData = convertObservation(theTerserPath + "/ORDER_OBSERVATION(" + orderObservationIndex + ")/OBSERVATION(" + k + ")", prevDocData, nextOrderObservation.getOBSERVATION(k - 1));
				if (nextDocData != null && nextDocData != prevDocData) {
				    section.myData.add(nextDocData);
                    
				}

				if (nextDocData != null) {
					convertNotes(theTerserPath + "/ORDER_OBSERVATION(" + orderObservationIndex + ")/OBSERVATION(" + k + ")", nextOrderObservation.getOBSERVATION(k - 1).getNTEAll(), nextDocData.myNotes);
				} else if (prevDocData != null) {
					convertNotes(theTerserPath + "/ORDER_OBSERVATION(" + orderObservationIndex + ")/OBSERVATION(" + k + ")", nextOrderObservation.getOBSERVATION(k - 1).getNTEAll(), prevDocData.myNotes);
				} else {
					// This shouldn't happen
					throw new HL7Exception("No repetition of notes");
				}

				prevDocData = nextDocData;
			}
			
			
			//Collapse consecutive ClinicalDocumentData objects with a dataType of ED that have the same code into one ClinicalDocumentData object with the data from their value fields concatonated.  
			//The data in each of these value fields is a separate line from a single electronic document.
			List<ClinicalDocumentData> currentCddList = section.myData;			
			ArrayList<ClinicalDocumentData> revisedCddList = new ArrayList<ClinicalDocumentData>();			
			ClinicalDocumentData prev = null;			
            for (ClinicalDocumentData clinicalDocumentData : currentCddList) {
                ClinicalDocumentData next = clinicalDocumentData;
                if (prev != null && next != null && partitionedEdValueCheck(next, prev)) {
                    ClinicalDocumentData lastRevisedCdd = revisedCddList.get(revisedCddList.size() - 1);
                    lastRevisedCdd.myValue = lastRevisedCdd.myValue + next.myValue;
                    if (lastRevisedCdd.myNotes != null) {
                        lastRevisedCdd.myNotes.addAll(next.myNotes);
                    }
                    else {
                        lastRevisedCdd.myNotes = next.myNotes;
                    }
                }
                else {
                    revisedCddList.add(next);
                }
                prev = next;
            }
			section.myData = revisedCddList;
			
			
			//Ignore processing ZDR  
			
			
		}

	}


	/**
     * 
     * @return ...
     */
    private boolean partitionedEdValueCheck(ClinicalDocumentData theNextCd, ClinicalDocumentData thePrevCd) {
        
        if ( theNextCd.myDataType.equals("ED") && thePrevCd.myDataType.equals("ED") && theNextCd.myCode.equals(thePrevCd.myCode) ) {
          return true;   
        }
        return false;
    }


//    private void convert(String theTerserPath, XTN theXTN) throws HL7Exception {
//
//		String xtn1number = theXTN.getXtn1_TelephoneNumber().getValue();
//		String xtn4email = theXTN.getXtn4_EmailAddress().getValue();
//		if (StringUtils.isNotBlank(xtn1number) && StringUtils.isNotBlank(xtn4email)) {
//			addFailure(theTerserPath, FailureCode.F052, theXTN.encode());
//		}
//
//		if (StringUtils.isNotBlank(theXTN.getXtn1_TelephoneNumber().getValue()) && theXTN.getXtn1_TelephoneNumber().getValue().contains("@")) {
//			addFailure(theTerserPath, FailureCode.F053, theXTN.encode());
//		}
//
//	}


	/**
     * @return the contributorConfig
     */
    public Contributor getContributorConfig() {
    	return myContributorConfig;
    }


	/**
     * @return the sendingSystem
     */
    public SendingSystem getSendingSystem() {
    	return mySendingSystem;
    }


	private Ce convertCe(String theTerserPath, CE theCe) throws HL7Exception {
		if (isBlank(theCe.getCe1_Identifier().getValue())) {
			return null;
		}

		return convertCeAndAllowNoCode(theTerserPath, theCe);
	}

	private Ce convertCeAndAllowNoCode(String theTerserPath, CE theCe) throws HL7Exception {
		Ce retVal = new Ce();
		retVal.myCode = theCe.getCe1_Identifier().getValue();
		retVal.myText = theCe.getCe2_Text().getValue();
		retVal.myCodeSystem = theCe.getCe3_NameOfCodingSystem().getValue();

		return retVal;
	}

	private Ce convertCeWithOptionalIdentifier(String theTerserPath, CE theCe) throws HL7Exception {
		Ce retVal = new Ce();
		retVal.myCode = theCe.getCe1_Identifier().getValue();
		retVal.myText = theCe.getCe2_Text().getValue();
		retVal.myCodeSystem = theCe.getCe3_NameOfCodingSystem().getValue();

		return retVal;
	}



	@SuppressWarnings("unused")
    private Ce convertCneToCe(String theTerserPath, CNE theCne) throws HL7Exception {
		if (isBlank(theCne.getCne1_Identifier().getValue())) {
			return null;
		}

		Ce retVal = new Ce();
		retVal.myCode = theCne.getCne1_Identifier().getValue();
		retVal.myText = theCne.getCne2_Text().getValue();
		retVal.myCodeSystem = theCne.getCne3_NameOfCodingSystem().getValue();

		return retVal;
	}


	private Cx convertCx(String theTerserPath, CX theCx) throws HL7Exception {
		Cx retVal = new Cx();

		retVal.myIdNumber = theCx.getCx1_IDNumber().getValue();
		validateStringLength(theTerserPath + "-1", retVal.myIdNumber, 20);

		retVal.myCheckDigit = theCx.getCx2_CheckDigit().getValue();
		validateStringLength(theTerserPath + "-2", retVal.myCheckDigit, 5);

		retVal.myAssigningAuthorityHspId = theCx.getCx4_AssigningAuthority().getHd1_NamespaceID().getValue();
		validateStringLength(theTerserPath + "-4-1", retVal.myAssigningAuthorityHspId, 50);

		retVal.myAssigningAuthoritySystemId = theCx.getCx4_AssigningAuthority().getHd2_UniversalID().getValue();
		validateStringLength(theTerserPath + "-4-2", retVal.myAssigningAuthoritySystemId, 50);

		retVal.myIdTypeCode = theCx.getCx5_IdentifierTypeCode().getValue();
		retVal.myIdTypeDescription = Tables.lookupHl7Code("0203", retVal.myIdTypeCode);
		if (retVal.myIdTypeDescription == null) {
			addFailure(theTerserPath + "-5", FailureCode.F043, theCx.encode());
		}

		retVal.myEffectiveDate = theCx.getCx7_EffectiveDate().getValue();
		if (isNotBlank(retVal.myEffectiveDate)) {
			validateDtAndAddFailure(theTerserPath + "-7", theCx.getCx7_EffectiveDate().getValue());
		}

		retVal.myExpirationDate = theCx.getCx8_ExpirationDate().getValue();
		if (isNotBlank(retVal.myExpirationDate)) {
			validateDtAndAddFailure(theTerserPath + "-8", theCx.getCx8_ExpirationDate().getValue());
		}

		retVal.myAssigningJurisdictionId = theCx.getCx9_AssigningJurisdiction().getCwe1_Identifier().getValue();
		if (isNotBlank(retVal.myAssigningJurisdictionId)) {
			String assigningJurisdictionText = Tables.lookupHl7Code("0363", retVal.myAssigningJurisdictionId);
			if (assigningJurisdictionText == null) {
				addFailure(theTerserPath + "-9-1", FailureCode.F044, theCx.encode());
			}

			if ("OTHER".equals(retVal.myAssigningJurisdictionId)) {
				retVal.myAssigningJurisdictionText = theCx.getCx9_AssigningJurisdiction().getCwe2_Text().getValue();
				if (isBlank(retVal.myAssigningJurisdictionText)) {
					addFailure(theTerserPath + "-9-2", FailureCode.F045, theCx.encode());
				}
			}

			if (!"HL70363".equals(theCx.getCx9_AssigningJurisdiction().getCwe3_NameOfCodingSystem().getValue())) {
				addFailure(theTerserPath, FailureCode.F046, theCx.encode());
			}
		}

		if (isNotBlank(retVal.myAssigningAuthorityHspId) && isNotBlank(retVal.myAssigningJurisdictionId)) {
			addFailure(theTerserPath, FailureCode.F048, theCx.encode());
		}

		if (isBlank(retVal.myIdTypeCode)) {

			//addFailure(theTerserPath + "-5", FailureCode.F049, theCx.encode());
			// this is caught by F043 above

		} else if (retVal.myIdTypeCode.equals("JHN") || retVal.myIdTypeCode.equals("PPN")) {
			if (isBlank(retVal.myAssigningJurisdictionId)) {
				addFailure(theTerserPath, FailureCode.F050, theCx.encode());
			}
		} else {
			if (isBlank(retVal.myAssigningAuthorityHspId)) {
				addFailure(theTerserPath, FailureCode.F051, theCx.encode());
			}
		}

		// Validate CX.4
		
		if (isNotBlank(retVal.myAssigningAuthorityHspId) && !isNotBlank(retVal.myAssigningAuthoritySystemId)) {
			addFailure(theTerserPath + "-4", FailureCode.F051, theCx.encode());
		} else if (!isNotBlank(retVal.myAssigningAuthorityHspId) && isNotBlank(retVal.myAssigningAuthoritySystemId)) {
			addFailure(theTerserPath + "-4", FailureCode.F051, theCx.encode());
		}

		if (isNotBlank(retVal.myAssigningAuthorityHspId)) {
			if (!myContributorConfig.getHspId9004().equals(retVal.myAssigningAuthorityHspId)) {
				addFailure(theTerserPath + "-4-1", FailureCode.F116, retVal.myAssigningAuthorityHspId);
			}
		}
		
		if (isNotBlank(retVal.myAssigningAuthoritySystemId)) {
			if (myContributorConfig.getSendingSystem9008WithOid(retVal.myAssigningAuthoritySystemId) == null) {
				addFailure(theTerserPath + "-4-2", FailureCode.F117, retVal.myAssigningAuthoritySystemId);
			}
		}
		
		return retVal;
	}


	private Cx convertCxPatient(String theTerserPath, CX theNextRep) throws HL7Exception {
		Cx retVal = convertCx(theTerserPath, theNextRep);

		if ("VN".equals(retVal.myIdTypeCode) || "AN".equals(retVal.myIdTypeCode)) {
			addFailure(theTerserPath + "-5", FailureCode.F041, theNextRep.encode());
		}

		return retVal;
	}


	private Cx convertCxVisit(String theTerserPath, CX theNextRep) throws HL7Exception {
		Cx retVal = convertCx(theTerserPath, theNextRep);

		if (!"VN".equals(retVal.myIdTypeCode) && !"AN".equals(retVal.myIdTypeCode)) {
			addFailure(theTerserPath + "-5", FailureCode.F042, theNextRep.encode());
		}

		return retVal;
	}


	private Diagnosis convertDg1(String theTerserPath, DG1 theDG1) throws HL7Exception {
		if (theDG1.encode().length() < 5) {
			return null;
		}

		Diagnosis retVal = new Diagnosis();

		if (isNotBlank(theDG1.getDg13_DiagnosisCodeDG1().encode())) {
			retVal.myDiagnosis = convertCeWithOptionalIdentifier(theTerserPath + "-3", theDG1.getDg13_DiagnosisCodeDG1());
			
                if (isNotBlank(retVal.myDiagnosis.myCodeSystem) && isBlank(retVal.myDiagnosis.myCode)) {
                    addFailure(theTerserPath + "-3", FailureCode.F082, theDG1.getDg13_DiagnosisCodeDG1().encode());
                }
                
                if (isBlank(retVal.myDiagnosis.myCodeSystem) && isNotBlank(retVal.myDiagnosis.myCode)) {
                    addFailure(theTerserPath + "-3", FailureCode.F080, theDG1.getDg13_DiagnosisCodeDG1().encode());
                }
			
				if (isNotBlank(retVal.myDiagnosis.myCodeSystem) && Tables.lookupHl7Code("0053", retVal.myDiagnosis.myCodeSystem) == null) {
					addFailure(theTerserPath + "-3", FailureCode.F079, theDG1.getDg13_DiagnosisCodeDG1().encode());
				}
			
		} 
//		else {
//			addFailure(theTerserPath + "-3", FailureCode.F078, theDG1.getDg13_DiagnosisCodeDG1().encode());
//		}

		return retVal;
	}


	private Ei convertEiDocumentNumber(String theTerserPath, EI theEi) throws HL7Exception {
		Ei retVal = new Ei();

		retVal.myId = theEi.getEi1_EntityIdentifier().getValue();
		if (isBlank(retVal.myId)) {
			addFailure(theTerserPath + "-1", FailureCode.F020, theEi.encode());
			return null;
		}

		retVal.myFacilityId = theEi.getEi2_NamespaceID().getValue();
		retVal.myFacilityName = lookup9004AndReturnName(retVal.myFacilityId);
		if (isBlank(retVal.myFacilityName)) {
			addFailure(theTerserPath + "-2", FailureCode.F021, theEi.encode());
			return null;
		}

		retVal.mySystemId = theEi.getEi3_UniversalID().getValue();
		if (retVal.mySystemId == null || myContributorConfig.getSendingSystem9008WithOid(retVal.mySystemId) == null) {
			addFailure(theTerserPath + "-3", FailureCode.F022, theEi.encode());
			return null;
		}

		return retVal;
	}

	
	
	
    private void convertRxe(String theTerserPath, MedicationOrder nextOrder, RXE rxe) throws DataTypeException, HL7Exception {
        
        // RXE-1 (Quantity/Timing)
    	/*if (isBlank(rxe.getRxe1_QuantityTiming().getTq1_Quantity().getCq1_Quantity().getValue())) {
            addFailure(theTerserPath + "/RXE-1-1-1", FailureCode.F124, null);
        }
        else {
            nextOrder.myEncodedOrderQuantityNumber = toNumber(theTerserPath + "/RXE-1-1-1", rxe.getRxe1_QuantityTiming().getTq1_Quantity().getCq1_Quantity().getValue());
        }*/	
    	
    	if (isNotBlank(rxe.getRxe1_QuantityTiming().getTq1_Quantity().getCq1_Quantity().getValue())) {
            nextOrder.myEncodedOrderQuantityNumber = toNumber(theTerserPath + "/RXE-1-1-1", rxe.getRxe1_QuantityTiming().getTq1_Quantity().getCq1_Quantity().getValue());
       	}
       
        nextOrder.myEncodedOrderQuantityRepeatPattern = rxe.getRxe1_QuantityTiming().getTq2_Interval().getRi1_RepeatPattern().getValue();
        if (isBlank(nextOrder.myEncodedOrderQuantityRepeatPattern)) {
            addFailure(theTerserPath + "/RXE-1-2-1", FailureCode.F095, null);
        }
        nextOrder.myEncodedOrderQuantityDuration = rxe.getRxe1_QuantityTiming().getTq3_Duration().getValue();
        if (isBlank(nextOrder.myEncodedOrderQuantityRepeatPattern)) {
            addFailure(theTerserPath + "/RXE-1-3", FailureCode.F096, null);
        }
        if (isNotBlank(rxe.getRxe1_QuantityTiming().getTq4_StartDateTime().getTs1_Time().getValue())) {
            if (validateTsWithAtLeastMinutePrecisionAndAddFailure(theTerserPath + "/RXE-1-4", rxe.getRxe1_QuantityTiming().getTq4_StartDateTime().getTs1_Time().getValue())) {
            	nextOrder.myEncodedOrderQuantityStartTime = rxe.getRxe1_QuantityTiming().getTq4_StartDateTime().getTs1_Time().getValueAsDate();
            	nextOrder.myEncodedOrderQuantityStartTimeFormatted = DateFormatter.formatDate(rxe.getRxe1_QuantityTiming().getTq4_StartDateTime().getTs1_Time().getValue());
            }            
        }
        if (isNotBlank(rxe.getRxe1_QuantityTiming().getTq5_EndDateTime().getTs1_Time().getValue())) {
            if (validateTsWithAtLeastMinutePrecisionAndAddFailure(theTerserPath + "/RXE-1-5", rxe.getRxe1_QuantityTiming().getTq5_EndDateTime().getTs1_Time().getValue())) {
            	nextOrder.myEncodedOrderQuantityEndTime = rxe.getRxe1_QuantityTiming().getTq5_EndDateTime().getTs1_Time().getValueAsDate();
                nextOrder.myEncodedOrderQuantityEndTimeFormatted = DateFormatter.formatDate(rxe.getRxe1_QuantityTiming().getTq5_EndDateTime().getTs1_Time().getValue());
            }
        }

        // RXE-2 (Give Code)
        nextOrder.myEncodedOrderGiveCode = convertCe(theTerserPath + "/RXE-2", rxe.getRxe2_GiveCode());
        if (nextOrder.myEncodedOrderGiveCode != null && isNotBlank(nextOrder.myEncodedOrderGiveCode.myCodeSystem) && (!mySendingSystem.getDrugGiveCodeSystemRxe2().contains(nextOrder.myEncodedOrderGiveCode.myCodeSystem)) ) {
            addFailure(theTerserPath + "/RXE-2", FailureCode.F123, rxe.getRxe2_GiveCode().encode());
        } else if (nextOrder.myEncodedOrderGiveCode == null || nextOrder.myEncodedOrderGiveCode.hasBlanks()) {
            addFailure(theTerserPath + "/RXE-2", FailureCode.F097, rxe.getRxe2_GiveCode().encode());
        }

        // RXE-3,4,5 (Minimum, Maxiumum, and Units)
        if (isNotBlank(rxe.getRxe3_GiveAmountMinimum().getValue())) {
            nextOrder.myEncodedOrderGiveMinimum = toNumberDecimal(theTerserPath + "/RXE-3", rxe.getRxe3_GiveAmountMinimum().getValue());
        }
        if (isNotBlank(rxe.getRxe4_GiveAmountMaximum().getValue())) {
            nextOrder.myEncodedOrderGiveMaximum = toNumberDecimal(theTerserPath + "/RXE-4", rxe.getRxe4_GiveAmountMaximum().getValue());
        }
        if (isNotBlank(rxe.getRxe5_GiveUnits().encode())) {
            nextOrder.myEncodedOrderGiveUnits = convertCe(theTerserPath + "/RXE-5", rxe.getRxe5_GiveUnits());
            if ( nextOrder.myEncodedOrderGiveUnits == null || !nextOrder.myEncodedOrderGiveUnits.hasCodeAndText()) {
                addFailure(theTerserPath + "/RXE-5", FailureCode.F125, rxe.getRxe5_GiveUnits().encode());                
            }
            
        }

        // RXE-6 (Give Dosage Form)
        if (isNotBlank(rxe.getRxe6_GiveDosageForm().encode())) {
            nextOrder.myEncodedOrderGiveDosageForm = convertCe(theTerserPath + "/RXE-6", rxe.getRxe6_GiveDosageForm());
            if ( nextOrder.myEncodedOrderGiveDosageForm == null || !nextOrder.myEncodedOrderGiveDosageForm.hasCodeAndText()) {
                addFailure(theTerserPath + "/RXE-6", FailureCode.F126, rxe.getRxe6_GiveDosageForm().encode());                
            }
        }

        // RXE-7 (Provider's Admnistration Instructions)
        nextOrder.myEncodedOrderProvidersAdministrationInstructions = new ArrayList<Ce>();
        for (int i = 0; i < rxe.getRxe7_ProviderSAdministrationInstructionsReps(); i++) {
            Ce next = convertCeWithOptionalIdentifier(theTerserPath + "/RXE-7", rxe.getRxe7_ProviderSAdministrationInstructions(i));
            if (next != null && ( isNotBlank(next.myCode) || isNotBlank(next.myText))) {                
                nextOrder.myEncodedOrderProvidersAdministrationInstructions.add(next);
            }
        }     
        
    }
	
	
	
	

	/**
     * 
     * @param theTerserPath
     * @param theRxrAll
     * @param theNotes ...
     * @throws HL7Exception 
     */
    private void convertMedicationRoutes(String theTerserPath, List<RXR> theRxrAll, List<Ce> theMedicationRoutes) throws HL7Exception {
        
        int index = 0;
        for (RXR next : theRxrAll) {
            if (next.encode().length() < 5) {
                continue;
            }
            
            index++;  
            Ce route = new Ce();
            route.myCode = next.getRxr1_Route().getCe1_Identifier().getValue();
            route.myText = next.getRxr1_Route().getCe2_Text().getValue();
            route.myCodeSystem = next.getRxr1_Route().getCe3_NameOfCodingSystem().getValue();
            
            if ( isBlank(route.myText)) {
                addFailure(theTerserPath + "/RXR(" + index + ")-1-2", FailureCode.F118, next.getRxr1_Route().encode());                                
            }            
            theMedicationRoutes.add(route);
        }    
    }
    
    
    
    /**
     * 
     * @param theTerserPath
     * @param theRxrAll
     * @param theNotes ...
     * @throws HL7Exception 
     */
    private void convertMedicationRoute(String theTerserPath, RXR theRxr, MedicationAdmin theMedAdmin) throws HL7Exception {
        
            Ce route = new Ce();
            route.myCode = theRxr.getRxr1_Route().getCe1_Identifier().getValue();
            route.myText = theRxr.getRxr1_Route().getCe2_Text().getValue();
            route.myCodeSystem = theRxr.getRxr1_Route().getCe3_NameOfCodingSystem().getValue();
            
            if ( isBlank(route.myText)) {
                addFailure(theTerserPath + "/RXR-1-2", FailureCode.F118, theRxr.getRxr1_Route().encode());                                
            }            
            theMedAdmin.myMedicationRoute = route;            
    }    
	

	

	/**
     * 
     * @param theTerserPath
     * @param theRxcAll
     * @param theMedicationComponents ...
	 * @throws HL7Exception 
     */
    private void convertMedicationComponents(String theTerserPath, List<RXC> theRxcAll,
            ArrayList<MedicationComponent> theMedicationComponents) throws HL7Exception {
        
        int index = 0;
        for (RXC next : theRxcAll) {
            if (next.encode().length() < 5) {
                continue;
            }
            
            index++;  
            MedicationComponent mc = new MedicationComponent();
            
            mc.myComponentType = next.getRxc1_RXComponentType().getValue();
            if ( isBlank(mc.myComponentType) || Tables.lookupHl7Code("0166", mc.myComponentType) == null) {
                addFailure(theTerserPath + "/RXC(" + index + ")-1", FailureCode.F119, mc.myComponentType);                                
            }            
            
            mc.myComponentCode = convertCe(theTerserPath + "/RXC(" + index + ")-2", next.getRxc2_ComponentCode());
            if ( mc.myComponentCode == null || mc.myComponentCode.hasBlanks()) {
                addFailure(theTerserPath + "/RXC(" + index + ")-2", FailureCode.F122, next.getRxc2_ComponentCode().encode());                                
            }
            if ( mc.myComponentCode != null && !mySendingSystem.getDrugComponentCodeSystemRxc2().contains(mc.myComponentCode.myCodeSystem)) {
                addFailure(theTerserPath + "/RXC(" + index + ")-2-3", FailureCode.F123, next.getRxc2_ComponentCode().encode());                
            }
            
            
            if ( isBlank(next.getRxc3_ComponentAmount().getValue()) ) {
                addFailure(theTerserPath + "/RXC(" + index + ")-3", FailureCode.F019, null);
            }
            else {
                mc.myComponentAmount = toNumberDecimal(theTerserPath + "/RXC(" + index + ")-3", next.getRxc3_ComponentAmount().getValue());
            }
            
            
            mc.myComponentUnits = convertCe(theTerserPath + "/RXC(" + index + ")-4", next.getRxc4_ComponentUnits());
            if ( mc.myComponentUnits == null || !mc.myComponentUnits.hasCodeAndText()) {
                addFailure(theTerserPath + "/RXC(" + index + ")-4", FailureCode.F120, next.getRxc2_ComponentCode().encode());                                
            }
            
            theMedicationComponents.add(mc);            
            
        }   
        
    }

    

    private Mrg convertMrg(String theTerserPath, MRG theMrg) throws HL7Exception {

		Mrg retVal = new Mrg();
		retVal.myMergePatientIds = new ArrayList<Cx>();

		if (isNotBlank(theMrg.getMrg1_PriorPatientIdentifierList(0).encode())) {
			CX nextRep = theMrg.getMrg1_PriorPatientIdentifierList(0);
			if (isNotBlank(nextRep.getCx1_IDNumber().encode())) {
				retVal.myMergePatientIds.add(convertCxPatient(theTerserPath + "-1", nextRep));
			}
		}

		if (isNotBlank(theMrg.getMrg5_PriorVisitNumber().encode())) {
			CX nextRep = theMrg.getMrg5_PriorVisitNumber();
			if (isNotBlank(nextRep.getCx1_IDNumber().encode())) {
				retVal.myMergeVisitId = (convertCxVisit(theTerserPath + "-5", nextRep));
			}
		}

		return retVal;
	}


	private Xcn convertNdl(String theTerserPath, NDL theNdl) throws HL7Exception {
		Xcn retVal = new Xcn();

		ourLog.info("+++ " + theNdl.encode());
		
		CNN cnn = theNdl.getNdl1_NameOfPerson();
		retVal.myId = cnn.getCnn1_IDNumber().getValue();
		if (isBlank(retVal.myId)) {
			addFailure(theTerserPath + "-1-1", FailureCode.F023, theNdl.encode());
		}

		retVal.myLastName = cnn.getCnn2_FamilyName().getValue();
		retVal.myFirstName = cnn.getCnn3_GivenName().getValue();
		retVal.myMiddleName = cnn.getCnn4_SecondAndFurtherGivenNamesOrInitialsThereof().getValue();

		String authType = cnn.getCnn9_AssigningAuthorityNamespaceID().getValue();
		retVal.myIdType = lookupIdType9001(authType);

		if (retVal.myIdType == null) {
			addFailure(theTerserPath + "-1-9", FailureCode.F024, theNdl.encode());
		}

		if ("1.3.6.1.4.1.12201.1.2.1.5".equals(authType)) {

			retVal.myIdType = "Site Specific ID";
			retVal.myAssigningHspName = lookup9004AndReturnName(cnn.getCnn10_AssigningAuthorityUniversalID().getValue());
			retVal.myAssigningHspSystemId = cnn.getCnn11_AssigningAuthorityUniversalIDType().getValue();
			if (isBlank(retVal.myAssigningHspName)) {
				addFailure(theTerserPath + "-1-10", FailureCode.F025, theNdl.encode());
			}
			if (retVal.myAssigningHspSystemId == null || myContributorConfig.getSendingSystem9008WithOid(retVal.myAssigningHspSystemId) == null) {
				addFailure(theTerserPath + "-1-11", FailureCode.F026, theNdl.encode());
			}

		} else if (retVal.myIdType != null) {

			// Canonical License Number
			if (isNotBlank(cnn.getCnn10_AssigningAuthorityUniversalID().getValue())) {
				addFailure(theTerserPath + "-1-10", FailureCode.F027, theNdl.encode());
			}
			if (isNotBlank(cnn.getCnn11_AssigningAuthorityUniversalIDType().getValue())) {
				addFailure(theTerserPath + "-1-11", FailureCode.F028, theNdl.encode());
			}

		}

		return retVal;
	}


	private AssociatedParty convertNk1(String theTerserPath, NK1 theNk1) throws HL7Exception {
		AssociatedParty retVal = new AssociatedParty();

		retVal.myNames = new ArrayList<Xpn>();

		for (int i = 1; i <= theNk1.getNk12_NKNameReps(); i++) {
			if (isNotBlank(theNk1.getNk12_NKName(i - 1).encode())) {
				Xpn next = convertXpn(theTerserPath + "-2(" + i + ")", theNk1.getNk12_NKName(i - 1));
				if (next != null) {
					retVal.myNames.add(next);
				}
			}
		}

		retVal.myRelationship = convertCe(theTerserPath + "-3", theNk1.getNk13_Relationship());
		if (retVal == null || retVal.myRelationship == null || Tables.lookupHl7Code("0063", retVal.myRelationship.myCode) == null) {
			addFailure(theTerserPath + "-3", FailureCode.F083, theNk1.getNk13_Relationship().encode());
		} else {
			retVal.myRelationshipName = Tables.lookupHl7Code("0063", retVal.myRelationship.myCode);
		}

		retVal.myAddresses = new ArrayList<Xad>();
		for (int i = 0; i < theNk1.getNk14_AddressReps(); i++) {
			if (isBlank(theNk1.getNk14_Address(i).encode())) {
				continue;
			}

			Xad next = convertXad(theTerserPath + "-4(" + (i + 1) + ")", theNk1.getNk14_Address(i));
			if (next != null) {
				retVal.myAddresses.add(next);
			}
		}

		retVal.myContactInformation = new ArrayList<Xtn>();
		for (int i = 0; i < theNk1.getNk15_PhoneNumberReps(); i++) {
			if (isBlank(theNk1.getNk15_PhoneNumber(i).encode())) {
				continue;
			}

			Xtn next = convertXtn(theTerserPath + "-5(" + (i + 1) + ")", theNk1.getNk15_PhoneNumber(i));
			if (next != null) {
				retVal.myContactInformation.add(next);
			}
		}

		return retVal;
	}


	private void convertNotes(String theTerserPath, List<NTE> theNoteSegments, List<Note> theNotes) throws HL7Exception {
//		int index = 0;

		for (NTE next : theNoteSegments) {
			if (next.encode().length() < 5) {
				continue;
			}

//			index++;
//			if (!Integer.toString(index).equals(next.getNte1_SetIDNTE().getValue())) {
//				addFailure(theTerserPath + "/NTE(" + index + ")-1", FailureCode.F054, next.getNte1_SetIDNTE().getValue());
//			}

			for (int i = 0; i < next.getNte3_CommentReps(); i++) {
				FT nextComment = next.getNte3_Comment(i);
				if (StringUtils.isNotBlank(nextComment.getValue())) {

					Note note = new Note();
					note.myName = "Note";
					note.myNoteText = nextComment.getValueAsHtml();
					theNotes.add(note);

				}
			}
		}
	}


	private ClinicalDocumentData convertObservation(String theTerserPath, ClinicalDocumentData thePrevSection, ORU_R01_OBSERVATION theGroup) throws HL7Exception {
		ClinicalDocumentData retVal = new ClinicalDocumentData();

		retVal.myDataType = theGroup.getOBX().getObx2_ValueType().getValue();

		retVal.myCode = convertCe(theTerserPath + "/OBX-3", theGroup.getOBX().getObx3_ObservationIdentifier());
		if (retVal.myCode == null || isBlank(retVal.myCode.myText) || isBlank(retVal.myCode.myCode)) {
			addFailure(theTerserPath + "/OBX-3", FailureCode.F009, theGroup.getOBX().getObx3_ObservationIdentifier().encode());
		}
		if (retVal.myCode != null && !mySendingSystem.getResultCodeSystemSystemObx3().contains((retVal.myCode.myCodeSystem))) {
			addFailure(theTerserPath + "/OBX-3-3", FailureCode.F010, theGroup.getOBX().getObx3_ObservationIdentifier().encode());
		}
		
		if (isNotBlank(theGroup.getOBX().getObx4_ObservationSubID().getValue())) {
			retVal.mySubId = toNumber(theTerserPath + "/OBX-4", theGroup.getOBX().getObx4_ObservationSubID().getValue());
		}

		boolean isTextType = false;
		if ("FT".equals(retVal.myDataType) || "ST".equals(retVal.myDataType) || "TX".equals(retVal.myDataType)) {

			isTextType = true;
			StringBuilder b = new StringBuilder();
			for (int i = 0; i < theGroup.getOBX().getObx5_ObservationValueReps(); i++) {
				Primitive nextRep = (Primitive) theGroup.getOBX().getObx5_ObservationValue(i).getData();
				if (b.length() > 0) {
					b.append("<br>");
				}

				AbstractTextPrimitive text = (AbstractTextPrimitive) nextRep;
				b.append(text.getValueAsHtml());
			}
			retVal.myValue = b.toString();

		} else if ("DT".equals(retVal.myDataType)) {

			validateOnlyOneRep(theTerserPath, theGroup, retVal);
			DT dt = (DT) theGroup.getOBX().getObx5_ObservationValue(0).getData();
			validateDtAndAddFailure(theTerserPath + "/OBX-5", dt.getValue());
			retVal.myValue = DateFormatter.formatDate(dt.getValue());

		} else if ("TS".equals(retVal.myDataType)) {

			validateOnlyOneRep(theTerserPath, theGroup, retVal);
			TS dt = (TS) theGroup.getOBX().getObx5_ObservationValue(0).getData();
			validateTsWithAtLeastSecondPrecisionAndAddFailure(theTerserPath + "/OBX-5", dt.getTs1_Time().getValue());
			retVal.myValue = DateFormatter.formatDate(dt.getTs1_Time().getValue());

		} else if ("NM".equals(retVal.myDataType)) {

			validateOnlyOneRep(theTerserPath, theGroup, retVal);
			NM dt = (NM) theGroup.getOBX().getObx5_ObservationValue(0).getData();
			retVal.myValue = DateFormatter.formatDate(dt.getValue());

		} else if ("TM".equals(retVal.myDataType)) {

			validateOnlyOneRep(theTerserPath, theGroup, retVal);
			TM dt = (TM) theGroup.getOBX().getObx5_ObservationValue(0).getData();
			retVal.myValue = DateFormatter.formatTime(dt.getValue());

		} else if ("SN".equals(retVal.myDataType)) {

			validateOnlyOneRep(theTerserPath, theGroup, retVal);
			SN dt = (SN) theGroup.getOBX().getObx5_ObservationValue(0).getData();
			StringBuilder b = new StringBuilder();
			if (isNotBlank(dt.getSn1_Comparator().getValue())) {
				b.append(dt.getSn1_Comparator().getValue());
			}
			if (isNotBlank(dt.getSn2_Num1().getValue())) {
				b.append(dt.getSn2_Num1().getValue());
			}
			if (isNotBlank(dt.getSn3_SeparatorSuffix().getValue())) {
				b.append(dt.getSn3_SeparatorSuffix().getValue());
			}
			if (isNotBlank(dt.getSn4_Num2().getValue())) {
				b.append(dt.getSn4_Num2().getValue());
			}
			retVal.myValue = b.toString();
			
        } else if ("ED".equals(retVal.myDataType)) {

			validateOnlyOneRep(theTerserPath, theGroup, retVal);
			ED ed = (ED) theGroup.getOBX().getObx5_ObservationValue(0).getData();
			                        
			retVal.myEncapsulatedDataType = ed.getEd2_TypeOfData().getValue();
			if ( retVal.myEncapsulatedDataType == null || Tables.lookupHl7Code("0191", retVal.myEncapsulatedDataType) == null) {
				addFailure(theTerserPath + "-5-2", FailureCode.F104 , retVal.myEncapsulatedDataType);
			}
			
			retVal.myEncapsulatedDataSubType = ed.getEd3_DataSubtype().getValue();
			if ( retVal.myEncapsulatedDataSubType == null || Tables.lookupHl7Code("0291", retVal.myEncapsulatedDataSubType) == null) {
				addFailure(theTerserPath + "-5-3", FailureCode.F105 , retVal.myEncapsulatedDataSubType);
			}
			
			if ( retVal.myEncapsulatedDataSubType != null) {
				retVal.myEncapsulatedDataMimeType = getMimeType(retVal.myEncapsulatedDataSubType);
			}
			            
			retVal.myEncapsulatedDataEncoding = ed.getEd4_Encoding().getValue();
			if ( retVal.myEncapsulatedDataEncoding == null || !retVal.myEncapsulatedDataEncoding.equalsIgnoreCase("Base64")) {
				addFailure(theTerserPath + "-5-4", FailureCode.F106 , retVal.myEncapsulatedDataEncoding);
			}
			
			
			retVal.myValue = ed.getEd5_Data().getValue();
			if ( retVal.myValue == null || retVal.myValue.length() == 0) {
				addFailure(theTerserPath + "-5-5", FailureCode.F107 , null);
			}
            

		} else {

			addFailure(theTerserPath + "/OBX-2", FailureCode.F013, retVal.myDataType);

		}

		if (isNotBlank(theGroup.getOBX().getObx6_Units().encode())) {
			retVal.myUnits = convertCe(theTerserPath + "/OBX-6", theGroup.getOBX().getObx6_Units());
		}

		if (isNotBlank(theGroup.getOBX().getObx7_ReferencesRange().encode())) {
			retVal.myRefRange = theGroup.getOBX().getObx7_ReferencesRange().getValue();
		}

		if (isNotBlank(theGroup.getOBX().getObx8_AbnormalFlags(0).encode())) {
			retVal.myAbnormalFlagCode = theGroup.getOBX().getObx8_AbnormalFlags(0).getValue();
			retVal.myAbnormalFlagName = Tables.lookupHl7Code("0078", retVal.myAbnormalFlagCode);
			if (retVal.myAbnormalFlagName == null) {
				addFailure(theTerserPath + "/OBX-8", FailureCode.F014, theGroup.getOBX().getObx8_AbnormalFlags(0).encode());
			}
		}

		retVal.myDataStatusCode = theGroup.getOBX().getObx11_ObservationResultStatus().getValue();
		retVal.myDataStatus = Tables.lookupHl7Code("0085", retVal.myDataStatusCode);
		if (retVal.myDataStatus == null) {
			addFailure(theTerserPath + "/OBX-11", FailureCode.F015, theGroup.getOBX().getObx11_ObservationResultStatus().encode());
		}

		if (thePrevSection != null && retVal.myCode != null && retVal.myCode.equals(thePrevSection.myCode)) {
//			if (thePrevSection.mySubId != (retVal.mySubId - 1)) {
//				addFailure(theTerserPath, FailureCode.F016, theGroup.getOBX().getObx4_ObservationSubID().encode());
//			}

			if (isTextType) {
				thePrevSection.myValue = thePrevSection.myValue + (isNotEmpty(thePrevSection.myValue) ? "<br>" : "") + retVal.myValue;
				thePrevSection.mySubId = retVal.mySubId;
				return thePrevSection;
			}
		}

		String obx14 = theGroup.getOBX().getObx14_DateTimeOfTheObservation().encode();
		if (isNotBlank(obx14)) {
			if (validateTsWithAtLeastSecondPrecisionAndAddFailure(theTerserPath + "-14", theGroup.getOBX().getObx14_DateTimeOfTheObservation().getTs1_Time().getValue())) {
				retVal.myDateTimeOfObservation = theGroup.getOBX().getObx14_DateTimeOfTheObservation().getTs1_Time().getValueAsDate();
			}
		}		

		return retVal;
	}


	private void convertOrcForMedOrder(int theIndex, MedicationOrder theOrder, ORC theOrc) throws HL7Exception {
		
	    // ORC-2 (Placer Order Number)
		theOrder.myPlacerOrderNumber = convertEiDocumentNumber("ORDER(" + theIndex + ")/ORC-2", theOrc.getOrc2_PlacerOrderNumber());

		// ORC-4 (Placer Group Number)
		if (isNotBlank(theOrc.getOrc4_PlacerGroupNumber().encode())) {
			theOrder.myPlacerGroupNumber = convertEiDocumentNumber("ORDER(" + theIndex + ")/ORC-4", theOrc.getOrc4_PlacerGroupNumber());
		}
		
		//ORC-1
		String orderControl = theOrc.getOrc1_OrderControl().getValue();
		String orderControlName = Tables.lookupHl7Code("0119", orderControl);
		theOrder.myStatusCode = orderControl;
		theOrder.myStatusName = orderControlName;

		if (StringUtils.isBlank(theOrder.myStatusName)) {
			addFailure("ORDER(" + theIndex + ")/ORC-1", FailureCode.F094, orderControl);
		}
	}



	private Patient convertPid(String theTerserPath, PID thePID) throws HL7Exception {
		Patient retVal = new Patient();

//		if (!"1".equals(thePID.getPid1_SetIDPID().getValue())) {
//			addFailure(theTerserPath, FailureCode.F030, thePID.getPid1_SetIDPID().encode());
//		}

		retVal.myPatientIds = new ArrayList<Cx>();
		boolean haveMr = false;
		for (int i = 1; i <= thePID.getPid3_PatientIdentifierListReps(); i++) {
			CX nextRep = thePID.getPid3_PatientIdentifierList(i - 1);
			if (isBlank(nextRep.getCx1_IDNumber().getValue())) {
				continue;
			}

			Cx nextRepConv = convertCxPatient(theTerserPath + "-3(" + i + ")", nextRep);
			if (nextRepConv == null) {
				continue;
			}

			if ("MR".equals(nextRepConv.myIdTypeCode)) {
				if (haveMr) {
					// Make sure we don't have more than one MRN
					addFailure(theTerserPath + "-3(" + i + ")", FailureCode.F072, nextRep.encode());
					continue;
				} else {
					haveMr = true;
				}
			}

			retVal.myPatientIds.add(nextRepConv);
		}
		if (retVal.myPatientIds.size() > 10) {
			addFailure(theTerserPath + "-3", FailureCode.F071, null);
		}

		retVal.myPatientNames = new ArrayList<Xpn>();
		for (int i = 1; i <= thePID.getPid5_PatientNameReps(); i++) {
			XPN nextRep = thePID.getPid5_PatientName(i - 1);
			Xpn nextRepConv = convertXpn(theTerserPath + "-5(" + i + ")", nextRep);
			retVal.myPatientNames.add(nextRepConv);
		}
		if (retVal.myPatientNames.size() > 10) {
			addFailure(theTerserPath + "-5", FailureCode.F071, null);
		}

		if (thePID.getPid6_MotherSMaidenNameReps() > 1) {
			addFailure(theTerserPath + "-6", FailureCode.F031, null);
		}
		for (int i = 1; i <= thePID.getPid6_MotherSMaidenNameReps(); i++) {
			XPN nextRep = thePID.getPid6_MotherSMaidenName(i - 1);
			Xpn nextRepConv = convertXpn(theTerserPath + "-6(" + i + ")", nextRep);
			retVal.myMothersMaidenName = (nextRepConv);
		}

		TS dob = thePID.getPid7_DateTimeOfBirth();
		if (StringUtils.isNotBlank(dob.getTs1_Time().getValue())) {
			if (validateTsWithAnyPrecisionAndAddFailure(theTerserPath + "-7", dob.getTs1_Time().getValue())) {
				retVal.myDateOfBirth = dob.getTs1_Time().getValueAsDate();
			}
			// String dobString = dob.getTs1_Time().getValue();
			// //System.out.println(dobString);
			// if (dobString != null) {
			// dobString = dobString.substring(0, 8);
			// try {
			// retVal.myDateOfBirth = ourDtFormat.parse(dobString);
			// }
			// catch (ParseException e) {
			// addFailure(theTerserPath, FailureCode.F084, dobString);
			// }
			// }
		}

		if (isNotBlank(thePID.getPid8_AdministrativeSex().getValue())) {
			retVal.myAdministrativeSex = thePID.getPid8_AdministrativeSex().getValue();
			if (Tables.lookupHl7Code("0001", retVal.myAdministrativeSex) == null) {
				addFailure(theTerserPath + "-8", FailureCode.F072, retVal.myAdministrativeSex);
			}
		}

		retVal.myPatientAddresses = new ArrayList<Xad>();
		for (int i = 1; i <= thePID.getPid11_PatientAddressReps(); i++) {
			XAD nextRep = thePID.getPid11_PatientAddress(i - 1);
			if (nextRep.encode().isEmpty()) {
				continue;
			}

			Xad nextRepConv = convertXad(theTerserPath + "-11(" + i + ")", nextRep);
			retVal.myPatientAddresses.add(nextRepConv);
		}
		if (retVal.myPatientAddresses.size() > 10) {
			addFailure(theTerserPath + "-10", FailureCode.F071, null);
		}

		retVal.myPhoneNumbers = new ArrayList<Xtn>();
		for (int i = 1; i <= thePID.getPid13_PhoneNumberHomeReps(); i++) {
			XTN nextRep = thePID.getPid13_PhoneNumberHome(i - 1);
			Xtn nextRepConv = convertXtn(theTerserPath + "-13(" + i + ")", nextRep);
			retVal.myPhoneNumbers.add(nextRepConv);
		}
		if (retVal.myPhoneNumbers.size() > 10) {
			addFailure(theTerserPath + "-13", FailureCode.F071, null);
		}

		if (isNotBlank(thePID.getPid15_PrimaryLanguage().encode())) {
			if (!"HL70296".equals(thePID.getPrimaryLanguage().getCe3_NameOfCodingSystem().getValue())) {
				addFailure(theTerserPath, FailureCode.F032, thePID.getPrimaryLanguage().encode());
			}
			retVal.myPrimaryLanguage = convertCe(theTerserPath + "-15", thePID.getPid15_PrimaryLanguage());
		}

		// if (isNotBlank(thePID.getPid17_Religion().encode())) {
		// if
		// (!"HL70002".equals(thePID.getReligion().getCe3_NameOfCodingSystem().getValue()))
		// {
		// addFailure(theTerserPath, "PID-17-3 must be 'HL70002'");
		// }
		// retVal.myReligion = convert(theTerserPath + "-17",
		// thePID.getPid17_Religion());
		// }

		if (isNotBlank(thePID.getPid29_PatientDeathDateAndTime().encode())) {
			if (validateTsWithAtLeastSecondPrecisionAndAddFailure(theTerserPath + "-29", thePID.getPid29_PatientDeathDateAndTime().getTs1_Time().getValue())) {
				retVal.myDeathDateAndTime = thePID.getPid29_PatientDeathDateAndTime().getTs1_Time().getValueAsDate();
				retVal.myDeathDateAndTimeFormatted = DateFormatter.formatDate(thePID.getPid29_PatientDeathDateAndTime().getTs1_Time().getValue());
			}
		}

		String deathInd = thePID.getPid30_PatientDeathIndicator().getValue();
		if (deathInd != null && !"".equals(deathInd) && !"Y".equals(deathInd) && !"N".equals(deathInd)) {
			addFailure(theTerserPath, FailureCode.F033, thePID.getPid30_PatientDeathIndicator().encode());
		}
		retVal.myDeathIndicator = deathInd;

		return retVal;
	}


	private Pl convertPl(String theTerserPath, PL thePl) throws HL7Exception {
		Pl retVal = new Pl();

		retVal.myPointOfCare = thePl.getPointOfCare().getValue();
		validateStringLength(theTerserPath + "-1", retVal.myPointOfCare, 50);

		retVal.myRoom = thePl.getRoom().getValue();
		validateStringLength(theTerserPath + "-2", retVal.myRoom, 50);

		retVal.myBed = thePl.getBed().getValue();
		validateStringLength(theTerserPath + "-3", retVal.myBed, 50);

		retVal.myHspId = thePl.getPl4_Facility().getHd1_NamespaceID().getValue();
		retVal.myHspName = lookup9004AndReturnName(retVal.myHspId);
		if (retVal.myHspName == null) {
			addFailure(theTerserPath + "-4-1", FailureCode.F029, thePl.encode());
		}

		if (StringUtils.isNotBlank(thePl.getFacility().getHd2_UniversalID().getValue())) {
			retVal.myFacilityId = thePl.getFacility().getUniversalID().getValue();
			retVal.myFacilityName = myContributorConfig.getFacilityNameWithOid(retVal.myFacilityId);
		}

		retVal.myBuilding = thePl.getBuilding().getValue();
		validateStringLength(theTerserPath + "-7", retVal.myBuilding, 50);

		retVal.myFloor = thePl.getFloor().getValue();
		validateStringLength(theTerserPath + "-8", retVal.myFloor, 50);

		retVal.myLocationDescription = thePl.getLocationDescription().getValue();
		validateStringLength(theTerserPath + "-9", retVal.myLocationDescription, 50);

		return retVal;
	}

	
	private String lookup9004AndReturnName(String theValue) {
		String retVal = myAuthorization.getContributorConfig().getNameOfHspId9004(theValue);
		
		// TODO: validate security
		
		return retVal;
	}

	private Ei convertPrlDocumentNumber(String theTerserPath, PRL theEi) throws HL7Exception {
		Ei retVal = new Ei();

		retVal.myId = theEi.getPrl1_ParentObservationIdentifier().getCe1_Identifier().getValue();
		if (isBlank(retVal.myId)) {
			addFailure(theTerserPath + "-1-1", FailureCode.F020, theEi.encode());
		}

		retVal.myFacilityName = lookup9004AndReturnName(theEi.getPrl1_ParentObservationIdentifier().getCe2_Text().getValue());
		if (isBlank(retVal.myFacilityName)) {
			addFailure(theTerserPath + "-1-2", FailureCode.F021, theEi.encode());
		}

		retVal.mySystemId = theEi.getPrl1_ParentObservationIdentifier().getCe3_NameOfCodingSystem().getValue();
		if (retVal.mySystemId == null || myContributorConfig.getSendingSystem9008WithOid(retVal.mySystemId) == null) {
			addFailure(theTerserPath + "-1-3", FailureCode.F022, theEi.encode());
		}

		return retVal;
	}
	
	
    private void addRecordLockIndicator(PV1 thePV1, Patient thePatient) throws HL7Exception {
        
        String theTerserPath = "PV1"; 
        
        thePatient.myPatientRequestedRecordLock = thePV1.getPv116_VIPIndicator().getValue();
        if (isNotBlank(thePatient.myPatientRequestedRecordLock)) {
            if (!"Y".equals(thePatient.myPatientRequestedRecordLock) && !"N".equals(thePatient.myPatientRequestedRecordLock)) {
                addFailure(theTerserPath + "-16", FailureCode.F076, thePatient.myPatientRequestedRecordLock);
            }
        }
        
    }
	
	


	private Visit convertPv1(String theTerserPath, PV1 thePV1) throws HL7Exception {
		Visit retVal = new Visit();

		if (!"1".equals(thePV1.getSetIDPV1().getValue())) {
			addFailure(theTerserPath + "-1", FailureCode.F001, thePV1.getSetIDPV1().encode());
		}

		retVal.myPatientClassCode = thePV1.getPv12_PatientClass().getValue();
		if (retVal.myPatientClassCode != null) {
			retVal.myPatientClassName = Tables.lookupHl7Code("0004", thePV1.getPv12_PatientClass().getValue());
			if (retVal.myPatientClassName == null) {
				addFailure(theTerserPath + "-2", FailureCode.F002, retVal.myPatientClassCode);
			}
		}

		// System.out.println(thePV1.getPv13_AssignedPatientLocation().encode());
		if (isNotBlank(thePV1.getPv13_AssignedPatientLocation().encode())) {
			retVal.myAssignedPatientLocation = convertPl(theTerserPath + "-3", thePV1.getPv13_AssignedPatientLocation());
		}

		retVal.myAdmissionType = thePV1.getPv14_AdmissionType().getValue();
		validateStringLength(theTerserPath + "-4", retVal.myAdmissionType, 10);

		if (isNotBlank(thePV1.getPv16_PriorPatientLocation().encode())) {
			retVal.myPriorPatientLocation = convertPl(theTerserPath + "-6", thePV1.getPv16_PriorPatientLocation());
		}

		retVal.myAttendingDoctors = new ArrayList<Xcn>();
		for (int i = 1; i <= thePV1.getPv17_AttendingDoctorReps(); i++) {
		    if("\"\"".equals(thePV1.getPv17_AttendingDoctor(0).getXcn1_IDNumber().getValue())){
                Xcn deleteVal = new Xcn();
                deleteVal.myId = "\"\"";
                retVal.myAttendingDoctors.add(deleteVal);
                break;
            }
			retVal.myAttendingDoctors.add(convertXcn(theTerserPath + "-7(" + i + ")", thePV1.getPv17_AttendingDoctor(i - 1)));
		}
		if (retVal.myAttendingDoctors.size() > 10) {
			addFailure(theTerserPath + "-7", FailureCode.F071, null);
		}

		retVal.myReferringDoctors = new ArrayList<Xcn>();
		for (int i = 1; i <= thePV1.getPv18_ReferringDoctorReps(); i++) {
//		    if("\"\"".equals(thePV1.getPv18_ReferringDoctor(0).getXcn1_IDNumber().getValue())){
//                Xcn deleteVal = new Xcn();
//                deleteVal.myId = "\"\"";
//                retVal.myReferringDoctors.add(deleteVal);
//                break;
//            }
			retVal.myReferringDoctors.add(convertXcn(theTerserPath + "-8(" + i + ")", thePV1.getPv18_ReferringDoctor(i - 1)));
		}
		if (retVal.myReferringDoctors.size() > 10) {
			addFailure(theTerserPath + "-8", FailureCode.F071, null);
		}

		retVal.myConsultingDoctors = new ArrayList<Xcn>();
		for (int i = 1; i <= thePV1.getPv19_ConsultingDoctorReps(); i++) {
//		    if("\"\"".equals(thePV1.getPv19_ConsultingDoctor(0).getXcn1_IDNumber().getValue())){
//                Xcn deleteVal = new Xcn();
//                deleteVal.myId = "\"\"";
//                retVal.myConsultingDoctors.add(deleteVal);
//                break;
//            }
			retVal.myConsultingDoctors.add(convertXcn(theTerserPath + "-9(" + i + ")", thePV1.getPv19_ConsultingDoctor(i - 1)));
		}
		if (retVal.myConsultingDoctors.size() > 10) {
			addFailure(theTerserPath + "-9", FailureCode.F071, null);
		}

		retVal.myHospitalService = thePV1.getPv110_HospitalService().getValue();
		if (isNotBlank(retVal.myHospitalService)) {
			retVal.myHospitalServiceName = retVal.myHospitalService;
		}

//		retVal.myPatientRequestedRecordLock = thePV1.getPv116_VIPIndicator().getValue();
//		if (isNotBlank(retVal.myPatientRequestedRecordLock)) {
//			if (!"Y".equals(retVal.myPatientRequestedRecordLock) && !"N".equals(retVal.myPatientRequestedRecordLock)) {
//				addFailure(theTerserPath + "-16", FailureCode.F076, retVal.myPatientRequestedRecordLock);
//			}
//		}

		retVal.myAdmittingDoctors = new ArrayList<Xcn>();
		for (int i = 1; i <= thePV1.getPv117_AdmittingDoctorReps(); i++) {
//		    if("\"\"".equals(thePV1.getPv117_AdmittingDoctor(0).getXcn1_IDNumber().getValue())){
//		        Xcn deleteVal = new Xcn();
//		        deleteVal.myId = "\"\"";
//		        retVal.myAdmittingDoctors.add(deleteVal);
//		        break;
//		    }
			retVal.myAdmittingDoctors.add(convertXcn(theTerserPath + "-17(" + i + ")", thePV1.getPv117_AdmittingDoctor(i - 1)));
		}
		if (retVal.myAdmittingDoctors.size() > 10) {
			addFailure(theTerserPath + "-17", FailureCode.F071, null);
		}

		if (isNotBlank(thePV1.getPv119_VisitNumber().encode())) {
			retVal.myVisitNumber = convertCxVisit(theTerserPath + "-19", thePV1.getPv119_VisitNumber());
		}

		if (isNotBlank(thePV1.getAdmitDateTime().getTs1_Time().getValue())) {
			if (validateTsWithAtLeastSecondPrecisionAndAddFailure(theTerserPath + "-44", thePV1.getAdmitDateTime().getTs1_Time().getValue())) {
				retVal.myAdmitDate = thePV1.getPv144_AdmitDateTime().getTs1_Time().getValueAsDate();
				retVal.myAdmitDateFormatted = DateFormatter.formatDate(thePV1.getPv144_AdmitDateTime().getTs1_Time().getValue());
			}
		}

		retVal.myDischargeDates = new ArrayList<Date>();
		retVal.myDischargeDatesFormatted = new ArrayList<String>();
		for (int i = 0; i < thePV1.getPv145_DischargeDateTimeReps(); i++) {
			if (StringUtils.isBlank(thePV1.getDischargeDateTime(i).getTs1_Time().getValue())) {
				continue;
			}
			if (validateTsWithAtLeastSecondPrecisionAndAddFailure(theTerserPath + "-45", thePV1.getDischargeDateTime(i).getTs1_Time().getValue())) {
				retVal.myDischargeDates.add(thePV1.getDischargeDateTime(i).getTs1_Time().getValueAsDate());
				retVal.myDischargeDatesFormatted.add(DateFormatter.formatDate(thePV1.getDischargeDateTime(i).getTs1_Time().getValue()));
			}
		}
		if (retVal.myDischargeDates.size() > 10) {
			addFailure(theTerserPath + "-17", FailureCode.F071, null);
		}

		return retVal;
	}


	private void convertPv2(String theTerserPath, PV2 thePV2, Visit theVisit) throws HL7Exception {
		if (isNotBlank(thePV2.getPv23_AdmitReason().encode())) {
			theVisit.myAdmitReasonForEmergencyVisit = convertCe(theTerserPath + "-3", thePV2.getPv23_AdmitReason());
			if (theVisit.myAdmitReasonForEmergencyVisit != null) {
				validateStringLength(theTerserPath + "-3-1", theVisit.myAdmitReasonForEmergencyVisit.myCode, 60);
				validateStringLength(theTerserPath + "-3-2", theVisit.myAdmitReasonForEmergencyVisit.myText, 250);
			}
		}

		if (isNotBlank(thePV2.getPv240_AdmissionLevelOfCareCode().encode())) {
			theVisit.myAdmissionLevelOfCareForEmergencyVisit = convertCe(theTerserPath + "-40", thePV2.getPv240_AdmissionLevelOfCareCode());
			if (theVisit.myAdmissionLevelOfCareForEmergencyVisit != null) {
				validateStringLength(theTerserPath + "-40-1", theVisit.myAdmissionLevelOfCareForEmergencyVisit.myCode, 60);
				validateStringLength(theTerserPath + "-40-2", theVisit.myAdmissionLevelOfCareForEmergencyVisit.myText, 250);
				if (Tables.lookupHl7Code("0432", theVisit.myAdmissionLevelOfCareForEmergencyVisit.myCode) == null) {
					addFailure(theTerserPath + "-40-1", FailureCode.F077, theVisit.myAdmissionLevelOfCareForEmergencyVisit.myCode);
				}
			}
		}
	}


	private PersonInRole convertRol(String theTerserPath, ROL theRol) throws HL7Exception {
		if (theRol.encode().length() < 5) {
			return null;
		}

		PersonInRole retVal = new PersonInRole();

		retVal.myRole = convertCe(theTerserPath + "-3", theRol.getRol3_RoleROL());
		if (retVal.myRole == null || Tables.lookupHl7Code("0443", retVal.myRole.myCode) == null) {
			addFailure(theTerserPath + "-3-1", FailureCode.F081, theRol.getRol3_RoleROL().encode());
		}

		retVal.myPersonNames = new ArrayList<Xcn>();
		for (int i = 0; i < theRol.getRol4_RolePersonReps(); i++) {
			Xcn next = convertXcn(theTerserPath + "-4(" + (i + 1) + ")", theRol.getRol4_RolePerson(i), false);
			if (next != null) {
				retVal.myPersonNames.add(next);
			}
		}
//		if (retVal.myPersonNames.size() == 0) {
//			addFailure(theTerserPath + "-4", FailureCode.F082, null);
//		}

		retVal.myAddresses = new ArrayList<Xad>();
		for (int i = 0; i < theRol.getRol11_OfficeHomeAddressBirthplaceReps(); i++) {
			if (isBlank(theRol.getRol11_OfficeHomeAddressBirthplace(i).encode())) {
				continue;
			}

			Xad next = convertXad(theTerserPath + "-11(" + (i + 1) + ")", theRol.getRol11_OfficeHomeAddressBirthplace(i));
			if (next != null) {
				retVal.myAddresses.add(next);
			}
		}

		retVal.myContactInformation = new ArrayList<Xtn>();
		for (int i = 0; i < theRol.getRol12_PhoneReps(); i++) {
			if (isNotBlank(theRol.getRol12_Phone(i).encode())) {
				Xtn next = convertXtn(theTerserPath + "-12(" + (i + 1) + ")", theRol.getRol12_Phone(i));
				if (next != null) {
					retVal.myContactInformation.add(next);
				}
			}
		}

		return retVal;
	}
	
	
    
    
//2   250 CE  O       0127    00204   Allergen Type Code
//3   250 CE  R           00205   Allergen Code/Mnemonic/Description
//4   250 CE  O       0128    00206   Allergy Severity Code
//5   15  ST  O   Y       00207   Allergy Reaction Code
//6   250 CNE R       0323    01551   Allergy Action Code
//11    8   DT  O           01556   Onset Date
//12   60   ST  O        Onset Text 
//13   19  TS   O       Reported Date/Time
//15   250 CE   O       Relationship to Patient Code    
    
    
    private AdverseReaction convertIam(String theTerserPath, IAM theIAM) throws HL7Exception {
        
        AdverseReaction retVal = new AdverseReaction();
        

        if (isNotBlank(theIAM.getIam2_AllergenTypeCode().encode())) {
            retVal.myAllergenTypeCode = convertCe(theTerserPath + "-2", theIAM.getIam2_AllergenTypeCode());
            if ( retVal.myAllergenTypeCode == null || Tables.lookupHl7Code("0127", retVal.myAllergenTypeCode.myCode) == null) {
            addFailure(theTerserPath + "-2", FailureCode.F088, theIAM.getIam2_AllergenTypeCode().encode());
            }
        }

        
        if (isNotBlank(theIAM.getIam3_AllergenCodeMnemonicDescription().encode())) {
            retVal.myAllergenCode = convertCeAndAllowNoCode(theTerserPath + "-3", theIAM.getIam3_AllergenCodeMnemonicDescription());

            if (isBlank(retVal.myAllergenCode.myText)) {
                addFailure(theTerserPath + "-3-2", FailureCode.F103, theIAM.getIam3_AllergenCodeMnemonicDescription().encode());
            }
            
            if (retVal.myAllergenCode.myCodeSystem == null || !mySendingSystem.getAllergenCodeSystemIam3().contains(retVal.myAllergenCode.myCodeSystem)) {
                ourLog.info("IAM-3-3 code system {} isn't in allowable list: {}", retVal.myAllergenCode.myCodeSystem, mySendingSystem.getAllergenCodeSystemIam3());
            	addFailure(theTerserPath + "-3", FailureCode.F123, theIAM.getIam3_AllergenCodeMnemonicDescription().encode());
            }

        }        
        

        if (isNotBlank(theIAM.getIam4_AllergySeverityCode().encode())) {
            retVal.myAllergySeverityCode = convertCe(theTerserPath + "-4", theIAM.getIam4_AllergySeverityCode());
            if ( retVal.myAllergySeverityCode == null || Tables.lookupHl7Code("0128", retVal.myAllergySeverityCode.myCode) == null) {
            addFailure(theTerserPath + "-4", FailureCode.F089 , theIAM.getIam4_AllergySeverityCode().encode());
            }
        }        
        
        
        retVal.myAllergyReactionCodes = new ArrayList<String>();
        for (int i = 1; i <= theIAM.getIam5_AllergyReactionCodeReps(); i++) {
            if (isNotBlank(theIAM.getIam5_AllergyReactionCode(i - 1).encode())) {
                String next = theIAM.getIam5_AllergyReactionCode(i - 1).getValue();
                if (next != null) {
                    retVal.myAllergyReactionCodes.add(next);
                }
            }
        }        
        if (retVal.myAllergyReactionCodes.size() > 10) {
            addFailure(theTerserPath + "-5", FailureCode.F071, null);
        }
        
        
//        if (isNotBlank(theIAM.getIam9_SensitivityToCausativeAgentCode().encode())) {
//            retVal.mySensitivityToCausativeAgentCode = convertCe(theTerserPath + "-9", theIAM.getIam9_SensitivityToCausativeAgentCode());
//            if (retVal.mySensitivityToCausativeAgentCode == null || Tables.lookupHl7Code("0436", retVal.mySensitivityToCausativeAgentCode.myCode) == null) {
//                addFailure(theTerserPath + "-9", FailureCode.F090, theIAM.getIam9_SensitivityToCausativeAgentCode().encode());
//            }
//        } 
        
        String onsetDateStr = theIAM.getIam11_OnsetDate().getValue();
        if (isNotBlank(onsetDateStr)) {
            if (validateDtAndAddFailure(theTerserPath + "-11", onsetDateStr)) 
            {
                try {
	                retVal.myOnsetDate = ourDtFormat.parse(onsetDateStr);
	                retVal.myOnsetDateFormatted = DateFormatter.formatDate(onsetDateStr);
                } catch (ParseException e) {
                	ourLog.error("Should not happen! " + onsetDateStr);
                	// should not happen
                }
            }
        }
        
                
        if (isNotBlank(theIAM.getIam12_OnsetDateText().getValue())) {
            retVal.myOnsetText = theIAM.getIam12_OnsetDateText().getValue();
            validateStringLength(theTerserPath + "-12", retVal.myOnsetText, 60);
            
        }  
        
        
        String reportedDtTimeStr = theIAM.getIam13_ReportedDateTime().getTs1_Time().getValue();
        if (isNotBlank(reportedDtTimeStr)) {
            validateTsWithAnyPrecisionAndAddFailure(theTerserPath + "-13", reportedDtTimeStr);
            retVal.myReportedDateTime = convertVariableTsToDate(theTerserPath  + "-13", theIAM.getIam13_ReportedDateTime());
            retVal.myReportedDateTimeFormatted = DateFormatter.formatDate(reportedDtTimeStr);
        }
        
        
        if (isNotBlank(theIAM.getIam15_RelationshipToPatientCode().encode())) {
            retVal.myRelationshipToPatient = convertCe(theTerserPath + "-15", theIAM.getIam15_RelationshipToPatientCode());
            if (retVal.myRelationshipToPatient == null || Tables.lookupHl7Code("0063", retVal.myRelationshipToPatient.myCode) == null) {
                addFailure(theTerserPath + "-15", FailureCode.F083, theIAM.getIam15_RelationshipToPatientCode().encode());
            }
        }
        return retVal;
    }
        	




	private Xad convertXad(String theTerserPath, XAD theXad) throws HL7Exception {
		Xad retVal = new Xad();

		retVal.myStreetAddress = theXad.getXad1_StreetAddress().getSad1_StreetOrMailingAddress().getValue();
		validateStringLength(theTerserPath + "-1-1", retVal.myStreetAddress, 50);

		retVal.myStreetAddress2 = theXad.getXad2_OtherDesignation().getValue();
		validateStringLength(theTerserPath + "-2", retVal.myStreetAddress2, 50);

		retVal.myCity = theXad.getXad3_City().getValue();
		validateStringLength(theTerserPath + "-3", retVal.myCity, 80);

		String sop = theXad.getXad4_StateOrProvince().getValue();
		if (StringUtils.isNotBlank(sop)) {
			retVal.myProvince = Tables.lookupHl7Code("9003", sop);
			if (retVal.myProvince == null) {
				addFailure(theTerserPath + "-4", FailureCode.F035, theXad.encode());
			}
		}
		
		retVal.myPostalCode = theXad.getXad5_ZipOrPostalCode().getValue();
		validateStringLength(theTerserPath + "-5", retVal.myPostalCode, 12);

		retVal.myCountry = theXad.getXad6_Country().getValue();
		validateStringLength(theTerserPath + "-6", retVal.myCountry, 50);

		retVal.myAddressType = Tables.lookupHl7Code("0190", theXad.getXad7_AddressType().getValue());

		if (retVal.myAddressType == null) {
			addFailure(theTerserPath + "-7", FailureCode.F037, theXad.encode());
		}
		
       		
	   String effectiveDtTimeStr = theXad.getXad13_EffectiveDate().getTs1_Time().getValue();
        if (isNotBlank(effectiveDtTimeStr)) {
            validateTsWithAnyPrecisionAndAddFailure(theTerserPath + "-13", effectiveDtTimeStr);
            retVal.myEffectiveDate = convertVariableTsToDate(theTerserPath  + "-13", theXad.getXad13_EffectiveDate());
            retVal.myEffectiveDateFormatted = DateFormatter.formatDate(effectiveDtTimeStr);
        }
        
        
       String expirationDtTimeStr = theXad.getXad14_ExpirationDate().getTs1_Time().getValue();
        if (isNotBlank(expirationDtTimeStr)) {
            validateTsWithAnyPrecisionAndAddFailure(theTerserPath + "-14", expirationDtTimeStr);
            retVal.myExpirationDate = convertVariableTsToDate(theTerserPath  + "-14", theXad.getXad14_ExpirationDate());
            retVal.myExpirationDateFormatted = DateFormatter.formatDate(expirationDtTimeStr);
        }   
        
		
		

		return retVal;
	}


	
	private Xcn convertXcn(String theTerserPath, XCN theXcn) throws HL7Exception {
		return convertXcn(theTerserPath, theXcn, true);
	}

	private Xcn convertXcn(String theTerserPath, XCN theXcn, boolean idRequired) throws HL7Exception {
		Xcn retVal = new Xcn();

		retVal.myId = theXcn.getXcn1_IDNumber().getValue();
		if (idRequired && isBlank(retVal.myId)) {
			addFailure(theTerserPath + "-1", FailureCode.F023, theXcn.encode());
		}

		retVal.myLastName = theXcn.getXcn2_FamilyName().getFn1_Surname().getValue();
		retVal.myFirstName = theXcn.getXcn3_GivenName().getValue();
		retVal.myMiddleName = theXcn.getXcn4_SecondAndFurtherGivenNamesOrInitialsThereof().getValue();

		if (idRequired || isNotBlank(retVal.myId)) {
			String authType = theXcn.getXcn9_AssigningAuthority().getHd1_NamespaceID().getValue();
			retVal.myIdType = lookupIdType9001(authType);
	
			if (retVal.myIdType == null) {
				addFailure(theTerserPath + "-9-1", FailureCode.F024, theXcn.encode());
			}
	
			retVal.myAssigningHspName = lookup9004AndReturnName(theXcn.getXcn9_AssigningAuthority().getHd2_UniversalID().getValue());
	
			HD xcn9_AssigningAuthority = theXcn.getXcn9_AssigningAuthority();
			retVal.myAssigningHspSystemId = xcn9_AssigningAuthority.getHd3_UniversalIDType().getValue();
			if ("1.3.6.1.4.1.12201.1.2.1.5".equals(authType)) {
	
				retVal.myIdType = "Site Specific ID";
				if (isBlank(retVal.myAssigningHspName)) {
					addFailure(theTerserPath + "-9-2", FailureCode.F025, theXcn.encode());
				}
				if (retVal.myAssigningHspSystemId == null || myContributorConfig.getSendingSystem9008WithOid(retVal.myAssigningHspSystemId) == null) {
					addFailure(theTerserPath + "-9-2", FailureCode.F026, theXcn.encode());
				}
	
			} else if (retVal.myIdType != null) {
	
				// Canonical License Number
				if (isNotBlank(retVal.myAssigningHspName)) {
					addFailure(theTerserPath + "-9-2", FailureCode.F027, theXcn.encode());
				}
				if (isNotBlank(retVal.myAssigningHspSystemId)) {
					addFailure(theTerserPath + "-9-2", FailureCode.F028, theXcn.encode());
				}
	
			}
		}
		
		return retVal;
	}

	private String lookupIdType9001(String theAuthType) {
		Map<String, Code> toProvider = myAuthorization.getContributorConfig().getProviderId9001ToProvider();
		Code retVal = toProvider.get(theAuthType);
	    return retVal != null ? retVal.getDescription() : null;
    }


	private Xpn convertXpn(String theTerserPath, XPN theXpn) throws HL7Exception {
		Xpn retVal = new Xpn();

		retVal.myLastName = theXpn.getXpn1_FamilyName().getFn1_Surname().getValue();
		validateStringLength(theTerserPath + "-1-1", retVal.myLastName, 50);
		if (isBlank(retVal.myLastName)) {
			addFailure(theTerserPath + "-1-1", FailureCode.F073, null);
		}

		retVal.myFirstName = theXpn.getXpn2_GivenName().getValue();
		validateStringLength(theTerserPath + "-2", retVal.myFirstName, 50);

		retVal.mySecondName = theXpn.getXpn3_SecondAndFurtherGivenNamesOrInitialsThereof().getValue();
		validateStringLength(theTerserPath + "-3", retVal.mySecondName, 50);

		retVal.mySuffix = theXpn.getXpn4_SuffixEgJRorIII().getValue();
		validateStringLength(theTerserPath + "-4", retVal.mySuffix, 20);

		retVal.myPrefix = theXpn.getXpn5_PrefixEgDR().getValue();
		validateStringLength(theTerserPath + "-5", retVal.myPrefix, 20);

		retVal.myDegree = theXpn.getXpn6_DegreeEgMD().getValue();
		validateStringLength(theTerserPath + "-6", retVal.myDegree, 20);

		retVal.myNameType = Tables.lookupHl7Code("0200", theXpn.getXpn7_NameTypeCode().getValue());

		if (retVal.myNameType == null) {
			addFailure(theTerserPath, FailureCode.F034, theXpn.encode());
		}

		return retVal;
	}


	private Xtn convertXtn(String theTerserPath, XTN theXtn) throws HL7Exception {
		Xtn retVal = new Xtn();
		retVal.myPhoneNumber = theXtn.getXtn1_TelephoneNumber().getValue();
		validateStringLength(theTerserPath + "-1", retVal.myPhoneNumber, 25);

		retVal.myPhoneNumberTypeCode = theXtn.getXtn2_TelecommunicationUseCode().getValue();
		retVal.myPhoneNumberType = Tables.lookupHl7Code("0201", retVal.myPhoneNumberTypeCode);
		if (retVal.myPhoneNumberType == null) {
			addFailure(theTerserPath + "-2", FailureCode.F038, theXtn.encode());
		}

		retVal.myPhoneNumberEquipmentTypeCode = theXtn.getXtn3_TelecommunicationEquipmentType().getValue();
		retVal.myPhoneNumberEquipmentType = Tables.lookupHl7Code("0202", retVal.myPhoneNumberEquipmentTypeCode);

		retVal.myEmailAddress = theXtn.getXtn4_EmailAddress().getValue();
		validateStringLength(theTerserPath + "-4", retVal.myEmailAddress, 199);

		retVal.myNumberParts1CountryCode = theXtn.getXtn5_CountryCode().getValue();
		validateStringLength(theTerserPath + "-5", retVal.myNumberParts1CountryCode, 3);
		if (isNotBlank(retVal.myNumberParts1CountryCode) && !retVal.myNumberParts1CountryCode.matches("^[0-9]+$")) {
			addFailure(theTerserPath + "-5", FailureCode.F074, retVal.myNumberParts1CountryCode);
		}

		retVal.myNumberParts2AreaCode = theXtn.getXtn6_AreaCityCode().getValue();
		validateStringLength(theTerserPath + "-6", retVal.myNumberParts2AreaCode, 5);
		if (isNotBlank(retVal.myNumberParts2AreaCode) && !retVal.myNumberParts2AreaCode.matches("^[0-9]+$")) {
			addFailure(theTerserPath + "-6", FailureCode.F074, retVal.myNumberParts2AreaCode);
		}

		retVal.myNumberParts3LocalNumber = theXtn.getXtn7_LocalNumber().getValue();
		validateStringLength(theTerserPath + "-7", retVal.myNumberParts3LocalNumber, 9);
		if (isNotBlank(retVal.myNumberParts3LocalNumber) && !retVal.myNumberParts3LocalNumber.matches("^[0-9]+$")) {
			addFailure(theTerserPath + "-7", FailureCode.F074, retVal.myNumberParts3LocalNumber);
		}

		retVal.myNumberParts4Ext = theXtn.getXtn8_Extension().getValue();
		validateStringLength(theTerserPath + "-8", retVal.myNumberParts4Ext, 5);
		if (isNotBlank(retVal.myNumberParts4Ext) && !retVal.myNumberParts4Ext.matches("^[0-9]+$")) {
			addFailure(theTerserPath + "-8", FailureCode.F074, retVal.myNumberParts4Ext);
		}

		retVal.myAnyText = theXtn.getXtn9_AnyText().getValue();
		validateStringLength(theTerserPath + "-9", retVal.myAnyText, 199);

		if (!"NET".equals(retVal.myPhoneNumberTypeCode)) {
			if (isNotBlank(retVal.myEmailAddress)) {
				addFailure(theTerserPath, FailureCode.F039, theXtn.encode());
			}
		}

		if ("NET".equals(retVal.myPhoneNumberTypeCode)) {
			if (isNotBlank(retVal.myPhoneNumber)) {
				addFailure(theTerserPath, FailureCode.F040, theXtn.encode());
			}
		}

		return retVal;
	}


	public List<Failure> getFailures() {
		return myFailures;
	}


	public boolean hasFailure() {
		return myFailures.size() > 0;
	}


	private Cx obtainPidMrn(String theTerserPath, PID thePID) throws HL7Exception {
		Cx retVal = null;
		boolean haveMr = false;
		for (int i = 1; i <= thePID.getPid3_PatientIdentifierListReps(); i++) {
			CX nextRep = thePID.getPid3_PatientIdentifierList(i - 1);
			if (isBlank(nextRep.getCx1_IDNumber().getValue())) {
				continue;
			}

			Cx nextRepConv = convertCxPatient(theTerserPath + "-3(" + i + ")", nextRep);

			if ("MR".equals(nextRepConv.myIdTypeCode)) {
				if (haveMr) {
					// Make sure we don't have more than one MRN
					addFailure(theTerserPath + "-3(" + i + ")", FailureCode.F072, nextRep.encode());
					continue;
				} else {
					haveMr = true;
					retVal = nextRepConv;
				}
			}
		}

		return retVal;
	}


	private Pl obtainPv1Loc(String theTerserPath, PV1 thePV1) throws HL7Exception {

		Pl retVal = null;

		if (isNotBlank(thePV1.getPv13_AssignedPatientLocation().encode())) {
			retVal = convertPl(theTerserPath + "-3", thePV1.getPv13_AssignedPatientLocation());
		}

		return retVal;
	}


	private Cx obtainPv1VisitNumber(String theTerserPath, PV1 thePV1) throws HL7Exception {

		Cx retVal = null;

		if (isNotBlank(thePV1.getPv119_VisitNumber().encode())) {
			retVal = convertCxVisit(theTerserPath + "-19", thePV1.getPv119_VisitNumber());
		}

		return retVal;
	}


	int toNumber(String theTerserPath, String theValue) {
		if (theValue == null || theValue.isEmpty()) {
			return 0;
		}

		if (!theValue.matches("^[0-9]+$")) {
			addFailure(theTerserPath, FailureCode.F019, theValue);
			return 0;
		}

		return Integer.parseInt(theValue);
	}

    double toNumberDecimal(String theTerserPath, String theValue) {
		if (theValue == null || theValue.isEmpty()) {
			return 0.0;
		}

		if (!theValue.matches("^[0-9.]+$")) {
			addFailure(theTerserPath, FailureCode.F131, theValue);
			return 0.0;
		}

		return Double.parseDouble(theValue);
    }


	private boolean validateDtAndAddFailure(String theTerserPath, String theValue) {
		if (theValue == null || !theValue.matches("^[0-9]{8}$")) {
			addFailure(theTerserPath, FailureCode.F084, theValue);
			return false;
		} else {
			return true;
		}
	}


	public boolean validateMsh(MSH theMsh) {

		// Only validate once
		if (myValidatedMsh != null) {
			return myValidatedMsh;
		}
		
		if (!"|".equals(theMsh.getFieldSeparator().getValue())) {
			addFailure("MSH-1", FailureCode.F066, theMsh.getFieldSeparator().getValue());
		}

		if (!"^~\\&".equals(theMsh.getMsh2_EncodingCharacters().getValue())) {
			addFailure("MSH-2", FailureCode.F066, theMsh.getMsh2_EncodingCharacters().getValue());
		}

		String sendingOrg = theMsh.getMsh3_SendingApplication().getHd1_NamespaceID().getValue();
		myContributorConfig = myAuthorization.getContributorConfig().getHspId9004ToContributor().get(StringUtils.defaultString(sendingOrg));
		if (myContributorConfig == null) {

			addFailure("MSH-3-1", FailureCode.F112, theMsh.getMsh3_SendingApplication().getHd1_NamespaceID().getValue());
			myValidatedMsh = false;
			return false;

		} else {

			String sendingSystemId = theMsh.getMsh3_SendingApplication().getHd2_UniversalID().getValue();
			mySendingSystem = myContributorConfig.getSendingSystem9008WithOid(sendingSystemId);
			if (mySendingSystem == null) {
				addFailure("MSH-3-2", FailureCode.F113, sendingSystemId);
				myValidatedMsh = false;
				return false;
			}

			String securityToken = theMsh.getMsh8_Security().getValue();
			if (myCheckSecurity) {
				if (!myContributorConfig.getDevSecurityToken().equals(securityToken)) {
					addFailure("MSH-8", FailureCode.F114, securityToken);
					myValidatedMsh = false;
					return false;
				}
			}

		}

		validateTsWithAtLeastSecondPrecisionAndAddFailure("MSH-7", theMsh.getMsh7_DateTimeOfMessage().getTs1_Time().getValue());

		String controlId = theMsh.getMsh10_MessageControlID().getValue();
		if (isBlank(controlId)) {
			addFailure("MSH-10", FailureCode.F069, null);
		}

		String processingMode = theMsh.getMsh11_ProcessingID().getPt1_ProcessingID().getValue();
		if (!"T".equals(processingMode)) {
			addFailure("MSH-11-1", FailureCode.F070, processingMode);
		}

		String vid = theMsh.getMsh12_VersionID().getVid1_VersionID().getValue();
		if (!"2.5".equals(vid)) {
			addFailure("MSH-12", FailureCode.F066, vid);
		}

		String aa = theMsh.getMsh15_AcceptAcknowledgmentType().getValue();
		if (!"NE".equals(aa)) {
			addFailure("MSH-15", FailureCode.F066, aa);
		}

		aa = theMsh.getMsh16_ApplicationAcknowledgmentType().getValue();
		if (!"AL".equals(aa)) {
			addFailure("MSH-16", FailureCode.F066, aa);
		}

		String country = theMsh.getMsh17_CountryCode().getValue();
		if (!"CAN".equals(country)) {
			addFailure("MSH-17", FailureCode.F066, country);
		}

		String charset = theMsh.getMsh18_CharacterSet(0).getValue();
		if (!"8859/1".equals(charset)) {
			addFailure("MSH-18", FailureCode.F066, charset);
		}

		String profile = theMsh.getMsh21_MessageProfileIdentifier(0).getEi1_EntityIdentifier().getValue();
		if (!INPUT_PROFILE_2_0.equals(profile)) {
			addFailure("MSH-21(1)-1", FailureCode.F115, profile);
		}

		myValidatedMsh = true;
		return true;
	}


	private void validateOnlyOneRep(String theTerserPath, ORU_R01_OBSERVATION theGroup, ClinicalDocumentData retVal) {
		if (theGroup.getOBX().getObx5_ObservationValueReps() > 1) {
			addFailure(theTerserPath + "/OBX-5", FailureCode.F018, null);
		}
	}


	private void validateStringLength(String theTerserPath, String theString, int theMaxLength) {
		if (theString != null && theString.length() > theMaxLength) {
			addFailure(theTerserPath, FailureCode.F071, theString);
		}
	}


	private boolean validateTsWithAtLeastSecondPrecisionAndAddFailure(String theTerserPath, String theValue) {
		if (theValue == null || !theValue.matches("^[0-9]{8}([0-9]{6})(\\.[0-9]{4})?(-[0-9]{4})?$")) {
			addFailure(theTerserPath, FailureCode.F017, theValue);
			return false;
		} else {
			return true;
		}
	}


	boolean validateTsWithAtLeastMinutePrecisionAndAddFailure(String theTerserPath, String theValue) {
		if (theValue == null || !theValue.matches("^[0-9]{8}[0-9]{4}([0-9]{2})?(\\.[0-9]{1,4})?([\\-\\+][0-9]{4})?$")) {
			addFailure(theTerserPath, FailureCode.F101, theValue);
			return false;
		} else {
			return true;
		}
	}
	
	
	
    private boolean validateTsWithAnyPrecisionAndAddFailure(String theTerserPath, String theValue) {

        if (theValue!= null && theValue.matches("^[0-9]{4}([0-9]{2}([0-9]{2}([0-9]{2}([0-9]{2}([0-9]{2}(\\.[0-9]{1,4})?)?)?)?)?)?([\\-\\+][0-9]{4})?$")) {
            return true;
        }
        else {
            addFailure(theTerserPath, FailureCode.F102, theValue);
            return false;
        }
         
    }
    
    
    /**
     * This method will account for issues when converting low precision ts values into Dates.
     * It seems that the TSComponentOne getValueAsDate() method does not properly convert TS values 
     * with less than hour precision
     * @throws HL7Exception 
     */
    private Date convertVariableTsToDate(String theTerserPath, TS theTs) throws HL7Exception {
        
        if (theTs == null){
            return null;
        }
        
        String tsString = theTs.getTs1_Time().getValue();
        
        if ( tsString == null || tsString.length() == 0 ) {
            return null;            
        }
        
        Date retVal = null;
        
        
        try {
            if ( tsString.length() >= 12  ) {
                retVal = theTs.getTs1_Time().getValueAsDate();            
            }
            else if ( tsString.length() == 10  ) {
                retVal = ourTsHourFormat.parse(tsString);            
            }            
            else if ( tsString.length() == 8  ) {
                retVal = ourDtFormat.parse(tsString);            
            }            
            else if ( tsString.length() == 6  ) {
                retVal = ourTsMonthFormat.parse(tsString);                        
            }            
            else if ( tsString.length() == 4  ) {
                retVal = ourTsYearFormat.parse(tsString);                        
            }
            else {
                addFailure(theTerserPath, FailureCode.F102, tsString);                
            }
        }
        catch (ParseException e) {
            addFailure(theTerserPath, FailureCode.F102, tsString);            
        }
        
        return retVal;
        
         
    }   
    

    
    private String getMimeType(String theDataSubType) {
        
        if ( theDataSubType.equals("HTML") ) {
            return "text/html";
        }
        else if (theDataSubType.equals("RTF")){
            return "text/rtf";
        }
        else if (theDataSubType.equals("TIFF")){
            return "image/tiff";
        }
        else if (theDataSubType.equals("JPEG")){
            return "image/jpeg";
        }
        else if (theDataSubType.equals("GIF")){
            return "image/gif";
        }
        else if (theDataSubType.equals("PNG")){
            return "image/png";
        }
        else if (theDataSubType.equals("WAV")){
            return "audio/wav";
        }
        else if (theDataSubType.equals("MP3")){
            return "audio/mp3";
        }        
        else if (theDataSubType.equals("PDF")){
            return "application/pdf";
        }
        else {
            return theDataSubType; 
        }
       
    }       
	

}
