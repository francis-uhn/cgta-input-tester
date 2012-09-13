package ca.cgta.input.listener;

import java.util.Date;
import java.util.List;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.DbAccessException;
import org.ektorp.DocumentNotFoundException;
import org.ektorp.Revision;
import org.ektorp.UpdateConflictException;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.cgta.input.converter.DateFormatter;
import ca.cgta.input.model.inner.Constants;
import ca.cgta.input.model.inner.Cx;
import ca.cgta.input.model.inner.MedicationOrder;
import ca.cgta.input.model.inner.Patient;
import ca.cgta.input.model.inner.Pl;
import ca.cgta.input.model.inner.Visit;
import ca.cgta.input.model.outer.ClinicalDocumentContainer;
import ca.cgta.input.model.outer.ClinicalDocumentGroup;
import ca.cgta.input.model.outer.MedicationOrderWithAdmins;
import ca.cgta.input.model.outer.MedicationOrderWithAdminsContainer;
import ca.cgta.input.model.outer.PatientWithVisits;
import ca.cgta.input.model.outer.PatientWithVisitsContainer;
import ca.uhn.hl7v2.model.v25.message.MDM_T02;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.ValidationContextImpl;

public class Persister {

	// public static final String ADDRESS = "http://10.7.7.45:5984";
	public static final String ADDRESS = "http://uhnvprx01t.uhn.ca:5984";
	// public static final String ADDRESS = "http://localhost:5984";

	public static final String DB = "cgta_input_test_db";

	private static StdCouchDbConnector ourConnector;
	private static final Logger ourLog = LoggerFactory.getLogger(Persister.class);

	private static boolean ourUnitTestMode;
	public static final String UNIT_TEST_DB = "unit_test";





	public static ClinicalDocumentGroup getClinicalDocument(String id) throws Exception {
		CouchDbConnector connector = getConnector();
		ClinicalDocumentContainer doc = connector.get(ClinicalDocumentContainer.class, id);
		return doc.getDocument();
	}


	public static CouchDbConnector getConnector() throws Exception {
		if (ourConnector == null) {
			HttpClient httpClient = new StdHttpClient.Builder().url(ADDRESS).connectionTimeout(10000).build();

			CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);

			String dbName = ourUnitTestMode ? UNIT_TEST_DB : DB;
			ourConnector = new StdCouchDbConnector(dbName, dbInstance);
		}

		return ourConnector;
	}
	
	
    /**
     * Set the factory to create a unit test mode connector
     */
    public static void setUnitTestMode(boolean theUnitTestMode) {
        ourUnitTestMode = theUnitTestMode;
        ourConnector = null;
    }
    
    
    /**
     * Use this in case you want to specifiy your own connector 
     * with its own conenction settings
     *
     */
    public static void setConnector(StdCouchDbConnector theConnector) {        
        ourConnector = theConnector;
    }
    
    
    
	
    public static String persist(List<ClinicalDocumentGroup> theDocuments) throws Exception {
        ourLog.info("Going to persist {} documents", theDocuments.size());

        StringBuilder outcome = new StringBuilder();
        
        for (ClinicalDocumentGroup next : theDocuments) {
            try {
            	storeClinicalDocument(outcome, next);
            } catch (UpdateConflictException e) {
                ourLog.error("Detected a conflict when storing", e);
            }
        }
        
        return outcome.toString();
    }


    public static String persist(PatientWithVisits thePatientWithVisits) throws Exception {

        Patient patient = thePatientWithVisits.myPatient;

        PatientWithVisitsContainer existing = getOrCreatePatientContainer(patient);

        ourLog.info("Updating document with ID: {}", existing.getId());
        
        StringBuilder outcome = new StringBuilder();
        outcome.append(existing.isNew() ? "Created " : "Updated ");
        
        if ("A01".equals(thePatientWithVisits.myMostRecentEventCode)) {
            updateVisit(existing, thePatientWithVisits);
        	appendOutcomeForVisit(outcome, existing);
            outcome.append("Activate inpatient visit");
        } else if ("A02".equals(thePatientWithVisits.myMostRecentEventCode)) {
            updateVisit(existing, thePatientWithVisits);
        	appendOutcomeForVisit(outcome, existing);
            outcome.append("Transfer visit");
        } else if ("A03".equals(thePatientWithVisits.myMostRecentEventCode)) {
            updateVisit(existing, thePatientWithVisits);
        	appendOutcomeForVisit(outcome, existing);
            outcome.append("Discharge visit");
        } else if ("A04".equals(thePatientWithVisits.myMostRecentEventCode)) {
            updateVisit(existing, thePatientWithVisits);
        	appendOutcomeForVisit(outcome, existing);
            outcome.append("Activate outpatient visit");
        } else if ("A05".equals(thePatientWithVisits.myMostRecentEventCode)) {
            updateVisit(existing, thePatientWithVisits);
            appendOutcomeForVisit(outcome, existing);
            outcome.append("Process pre-admit visit");    
        } else if ("A06".equals(thePatientWithVisits.myMostRecentEventCode)) {
            convertVisit(existing, thePatientWithVisits);
        	appendOutcomeForVisit(outcome, existing);
            outcome.append("Convert outpatient visit to inpatient");
        } else if ("A07".equals(thePatientWithVisits.myMostRecentEventCode)) {
            convertVisit(existing, thePatientWithVisits);
        	appendOutcomeForVisit(outcome, existing);
            outcome.append("Convert inpatient visit to outpatient");
        } else if ("A08".equals(thePatientWithVisits.myMostRecentEventCode)) {
            updateVisit(existing, thePatientWithVisits);
        	appendOutcomeForVisit(outcome, existing);
            outcome.append("Update visit");
        } else if ("A10".equals(thePatientWithVisits.myMostRecentEventCode)) {
            updateVisit(existing, thePatientWithVisits);    
        	appendOutcomeForVisit(outcome, existing);
            outcome.append("Add arrival for recurring outpatient");
        } else if ("A11".equals(thePatientWithVisits.myMostRecentEventCode)) {
            updateVisit(existing, thePatientWithVisits);
        	appendOutcomeForVisit(outcome, existing);
            outcome.append("Cancel admit");
        } else if ("A13".equals(thePatientWithVisits.myMostRecentEventCode)) {
            updateVisit(existing, thePatientWithVisits);
        	appendOutcomeForVisit(outcome, existing);
            outcome.append("Cancel discharge");
        } else if ("A17".equals(thePatientWithVisits.myMostRecentEventCode)) {
            swapLocations(existing, thePatientWithVisits);
        	appendOutcomeForVisit(outcome, existing);
            outcome.append("Perform bedswap with visit " + thePatientWithVisits.getRawVisitNumberOrUnknown());
        } else if ("A23".equals(thePatientWithVisits.myMostRecentEventCode)) {
        	updateVisit(existing, thePatientWithVisits);
        	appendOutcomeForVisit(outcome, existing);
            outcome.append("Delete visit " + thePatientWithVisits.getRawVisitNumberOrUnknown());
        } else if ("A28".equals(thePatientWithVisits.myMostRecentEventCode)) {
            updatePatient(existing, thePatientWithVisits);
        	appendOutcomeForPatient(outcome, existing);
            outcome.append("Add person or patient");
        } else if ("A31".equals(thePatientWithVisits.myMostRecentEventCode)) {
            updatePatient(existing, thePatientWithVisits);
        	appendOutcomeForPatient(outcome, existing);
            outcome.append("Update person or patient");
        } else if ("A37".equals(thePatientWithVisits.myMostRecentEventCode)) {
            unlinkPatient(existing, thePatientWithVisits);
        	appendOutcomeForPatient(outcome, existing);
            outcome.append("Unlink person or patient with MRN " + thePatientWithVisits.getRawMrnOrUnknown());
        } else if ("A40".equals(thePatientWithVisits.myMostRecentEventCode)) {
            mergePatient(existing, thePatientWithVisits);
        	appendOutcomeForPatient(outcome, existing);
            outcome.append("Merge person or patient with MRN " + thePatientWithVisits.getRawMergeMrnOrUnknown());
        } else if ("A42".equals(thePatientWithVisits.myMostRecentEventCode)) {
            mergeVisit(existing, thePatientWithVisits);
            appendOutcomeForVisit(outcome, existing);
            outcome.append("Merge visits for patient with MRN " + thePatientWithVisits.getRawMergeMrnOrUnknown());    
        } else if ("A45".equals(thePatientWithVisits.myMostRecentEventCode)) {
            moveVisit(existing, thePatientWithVisits);
        	appendOutcomeForVisit(outcome, existing);
            outcome.append("Move visit to patient with MRN " + thePatientWithVisits.getRawMergeMrnOrUnknown());
        } else if ("A60".equals(thePatientWithVisits.myMostRecentEventCode)) {
            updateVisit(existing, thePatientWithVisits);
        	appendOutcomeForVisit(outcome, existing);
            outcome.append("Update allergy information");
        }

        // ourLog.info("Updating existing document with id {}", id);
        // connector.update(existing);

        return outcome.toString();
    }


    private static void appendOutcomeForPatient(StringBuilder theOutcome, PatientWithVisitsContainer theExisting) {
    	theOutcome.append(" patient/client ");
    	theOutcome.append(theExisting.getDocument().getRawMrnOrUnknown());
		theOutcome.append(": ");
    }


	private static void appendOutcomeForVisit(StringBuilder theOutcome, PatientWithVisitsContainer theExisting) {
    	theOutcome.append(" visit/encounter ");
    	theOutcome.append(theExisting.getDocument().getRawVisitNumberOrUnknown());
		theOutcome.append(": ");
    }


	public static String persistMedicationAdmins(List<MedicationOrderWithAdmins> theMedOrdersWithAdmins) throws Exception{
        ourLog.info("Going to persist {} medication admins", theMedOrdersWithAdmins.size());
        StringBuilder outcome = new StringBuilder();
        
        for (MedicationOrderWithAdmins nextMedOrderWithAdmins : theMedOrdersWithAdmins) {
            try {
              storeMedicationAdmins(outcome, nextMedOrderWithAdmins);
            } catch (UpdateConflictException e) {
                ourLog.error("Detected a conflict when storing", e);
            }
        }
        
        return outcome.toString();
    }


    public static String persistMedicationOrders(List<MedicationOrder> theMedOrders) throws Exception {
        ourLog.info("Going to persist {} medication orders", theMedOrders.size());
        StringBuilder outcome = new StringBuilder();

        for (MedicationOrder next : theMedOrders) {
            try {
                storeMedicationOrder(outcome, next);
            } catch (UpdateConflictException e) {
                ourLog.error("Detected a conflict when storing", e);
            }
        }
        
        return outcome.toString();
    }


    private static PatientWithVisitsContainer getOrCreatePatientContainer(Patient theHl7Patient) throws Exception {
        String id = "PWV_" + theHl7Patient.toKey();
        PatientWithVisitsContainer retVal;
        try {
			retVal = getConnector().get(PatientWithVisitsContainer.class, id);
            retVal.setNewlyCreated(false);
			return retVal;
        } catch (DocumentNotFoundException e) {
            ourLog.info("Creating new document with id {}", id);
            retVal = new PatientWithVisitsContainer(new PatientWithVisits(theHl7Patient));
            getConnector().create(id, retVal);

            retVal = getConnector().get(PatientWithVisitsContainer.class, id);
            retVal.setNewlyCreated(true);
			return retVal;
        }
    }





	/**
	 * A40 (Merge Patient)
	 */
	private static void mergePatient(PatientWithVisitsContainer theExisting, PatientWithVisits theHl7PatientWithVisits) throws Exception {

		// NOTE: theExisting pertains to the current/toKeep patient in the
		// transaction

		PatientWithVisits existing = theExisting.getDocument();
		Cx firstMrn = existing.getMrn();

		if (theHl7PatientWithVisits.myMergeInfo == null || theHl7PatientWithVisits.myMergeInfo.hasIdWithTypeMr() == false) {
			ourLog.info("Could not merge patient, no second MRN found");
			return;
		}

		// NOTE: secondMrn pertains to the prior/toRemove patient in the
		// transaction

		Cx secondMrn = theHl7PatientWithVisits.myMergeInfo.getMrn();

		String secondId = "PWV_" + secondMrn.toKey();
		PatientWithVisitsContainer secondPatientsWithVisitsContainer;
		try {
			secondPatientsWithVisitsContainer = getConnector().get(PatientWithVisitsContainer.class, secondId);
		} catch (DocumentNotFoundException e) {
			secondPatientsWithVisitsContainer = null;
		}

		if (secondPatientsWithVisitsContainer == null) {

			/*
			 * If there isn't a patient in the DB with the MRN from the MRG
			 * segment, just create a stub patient and add it to the
			 * current/toKeep patient's merged in list
			 */

			if (existing.findMergedInPatientWithVisits(secondMrn) == null) {
				existing.ensureMergedInPatientsWithVisits();
				existing.myMergedInPatientsWithVisits.add(new PatientWithVisits(new Patient(secondMrn)));
			}

			ourLog.warn("Could not merge patient with MRN {} into {} because it did not exist", secondMrn, firstMrn);
			getConnector().update(theExisting);

		} else {
		    
		    PatientWithVisits secondPatient = secondPatientsWithVisitsContainer.getDocument();
		    if (existing.findMergedInPatientWithVisits(secondMrn) == null) {
		        existing.ensureMergedInPatientsWithVisits();
                existing.myMergedInPatientsWithVisits.add(secondPatient);		        
		    }
			
			ourLog.info("Merging patient with MRN {} into {} - Step 1/2", secondMrn, firstMrn);
			getConnector().update(theExisting);

			ourLog.info("Merging patient with MRN {} into {} - Step 2/2", secondMrn, firstMrn);
			getConnector().delete(secondPatientsWithVisitsContainer);

		}
	}
	
	
    /**
     * A42 (Merge Visit)
     */
    private static void mergeVisit(PatientWithVisitsContainer theExisting, PatientWithVisits theHl7PatientWithVisits) throws Exception {

        //NOTE: theExisting pertains to the patient that has the visits which need to be merged in the transaction

        if (theHl7PatientWithVisits.myMergeInfo == null || theHl7PatientWithVisits.myMergeInfo.myMergeVisitId == null) {
            ourLog.warn("Can't merge visits, as the visit information from the MRG segment is missing or incomplete");
            return;
        }
        
        Cx mrn = theExisting.getDocument().getMrn();
        String eventType = theHl7PatientWithVisits.myMostRecentEventCode;        
        Cx visitToKeepId = theHl7PatientWithVisits.myVisits.get(0).myVisitNumber;
        Cx visitToRemoveId = theHl7PatientWithVisits.myMergeInfo.myMergeVisitId;    
        
        //ensure that the visitToKeep (this is the visit identified in the PV1 segment) visit exists. If not then create it
        Visit visitToKeep = theExisting.getDocument().findVisit(visitToKeepId);
        if ( visitToKeep == null ) {
            visitToKeep = theHl7PatientWithVisits.myVisits.get(0);
            visitToKeep.setStatus(eventType);
            theExisting.getDocument().myVisits.add(visitToKeep);            
        }
        
        //remove the visitToRemove (this is the visit identified in the MRG segment) visit.
        theExisting.getDocument().findAndRemoveVisit(visitToRemoveId);
        
        
        ourLog.info("Merging visit with id {} into {} for patient with MRN {}", new Object[] {visitToRemoveId, visitToKeepId, mrn});
        getConnector().update(theExisting);

    }
	
	


	private static void moveVisit(PatientWithVisitsContainer theExisting, PatientWithVisits theHl7PatientWithVisits) throws Exception {

		// NOTE: theExisting pertains to the moveVisitTo patient in the
		// transaction

		if (theHl7PatientWithVisits.myMergeInfo == null || !theHl7PatientWithVisits.myMergeInfo.hasIdWithTypeMr() || theHl7PatientWithVisits.myMergeInfo.myMergeVisitId == null) {
			ourLog.warn("Can't move visit, as the MRG segment is missing or incomplete");
			return;
		}

		// NOTE: secondMrn pertains to the moveVisitFrom patient in the
		// transaction

		Cx secondMrn = theHl7PatientWithVisits.myMergeInfo.getMrn();
		String secondId = "PWV_" + secondMrn.toKey();
		PatientWithVisitsContainer secondPatientsWithVisitsContainer;
		try {
			secondPatientsWithVisitsContainer = getConnector().get(PatientWithVisitsContainer.class, secondId);
		} catch (DocumentNotFoundException e) {
			ourLog.warn("Can't move visit {} because patient {} is unknown", theHl7PatientWithVisits.myMergeInfo.myMergeVisitId, secondMrn);
			return;
		}

		Visit visitToMove = secondPatientsWithVisitsContainer.getDocument().findAndRemoveVisit(theHl7PatientWithVisits.myMergeInfo.myMergeVisitId);
		if (visitToMove == null) {
			ourLog.warn("Can't move visit {} from patient {} because visit ID is unknown", theHl7PatientWithVisits.myMergeInfo.myMergeVisitId, secondMrn);
			return;
		}

		Visit inTheWayVisit = theExisting.getDocument().findAndRemoveVisit(theHl7PatientWithVisits.myMergeInfo.myMergeVisitId);
		if (inTheWayVisit != null) {
			ourLog.warn("Visit {} already exisits for patient {}, so going to overwrite it", theHl7PatientWithVisits.myMergeInfo.myMergeVisitId, theExisting.getDocument().getMrn());
		}

		theExisting.getDocument().ensureVisits();
		theExisting.getDocument().myVisits.add(visitToMove);

		ourLog.warn("Moveing visit {} to patient {} - Step 1/2", theHl7PatientWithVisits.myMergeInfo.myMergeVisitId, theExisting.getDocument().getMrn());
		getConnector().update(theExisting);

		ourLog.warn("Moveing visit {} to patient {} - Step 2/2", theHl7PatientWithVisits.myMergeInfo.myMergeVisitId, theExisting.getDocument().getMrn());
		getConnector().update(secondPatientsWithVisitsContainer);
	}
	
	
    private static void swapLocations(PatientWithVisitsContainer theExisting, PatientWithVisits theHl7PatientWithVisits) throws Exception {

        if (theHl7PatientWithVisits.myBedSwapSecondPatientMrn == null || theHl7PatientWithVisits.myBedSwapSecondPatientVisitNumber == null) {
            ourLog.warn("Can't swap locations, identifiers from the second PID and/or PV1 segments have not been supplied");
            return;
        }

        if (theHl7PatientWithVisits.myBedSwapSecondPatientLocation == null || theHl7PatientWithVisits.myVisits.get(0).myAssignedPatientLocation == null) {
            ourLog.warn("Can't swap locations, one or both locations in the swap have not been supplied in the Hl7 data");
            return;
        }

        // NOTE: theExisting pertains to the patient identified in the first PID
        // segment
        // NOTE: secondMrn pertains to the patient identified in the second PID
        // segment

        Cx secondPatMrn = theHl7PatientWithVisits.myBedSwapSecondPatientMrn;
        String secondId = "PWV_" + secondPatMrn.toKey();
        PatientWithVisitsContainer secondPatientsWithVisitsContainer;
        try {
            secondPatientsWithVisitsContainer = getConnector().get(PatientWithVisitsContainer.class, secondId);
        } catch (DocumentNotFoundException e) {
            ourLog.warn("Can't swap locations because second patient {} is unknown", secondPatMrn);
            return;
        }

        Visit secondPatVisit = secondPatientsWithVisitsContainer.getDocument().findVisit(theHl7PatientWithVisits.myBedSwapSecondPatientVisitNumber);
        if (secondPatVisit == null) {
            ourLog.warn("Can't swap location from visit {} for patient {} because visit ID is unknown", theHl7PatientWithVisits.myBedSwapSecondPatientVisitNumber, secondPatMrn);
            return;
        }

        Visit firstPatVisit = theExisting.getDocument().findVisit(theHl7PatientWithVisits.myVisits.get(0).myVisitNumber);
        if (firstPatVisit == null) {
            ourLog.warn("Can't swap location from visit {} for patient {}, because visit ID is unknown", theHl7PatientWithVisits.myVisits.get(0).myVisitNumber, theExisting.getDocument().getMrn());
            return;
        }

        // get first patient loc from HL7 data
        Pl hl7FirstPatLoc = theHl7PatientWithVisits.myVisits.get(0).myAssignedPatientLocation;
        // get second patient loc from HL7 data
        Pl hl7SecondPatLoc = theHl7PatientWithVisits.myBedSwapSecondPatientLocation;
        // get stored first patient loc
        Pl storedFirstPatLoc = firstPatVisit.myAssignedPatientLocation;
        // get stored second patient loc
        Pl storedSecondPatLoc = secondPatVisit.myAssignedPatientLocation;


        // SWAP Set assignedLoc and priorLoc for firstPatientVisit
        if (!hl7FirstPatLoc.equals(storedFirstPatLoc)) {
            firstPatVisit.myPriorPatientLocation = firstPatVisit.myAssignedPatientLocation;
            firstPatVisit.myAssignedPatientLocation = hl7FirstPatLoc;            
        }

        // SWAP Set assignedLoc and priorLoc for secondPatientVisit
        if (!hl7SecondPatLoc.equals(storedSecondPatLoc)) {
            secondPatVisit.myPriorPatientLocation = secondPatVisit.myAssignedPatientLocation;
            secondPatVisit.myAssignedPatientLocation = hl7SecondPatLoc;
        }

        ourLog.warn("Swaping assigned location {} from visit {} to visit {} - Step 1/2", new Object[] { hl7SecondPatLoc, secondPatVisit.myVisitNumber, firstPatVisit.myVisitNumber });
        getConnector().update(theExisting);

        ourLog.warn("Swaping assigned location {} from visit {} to visit {} - Step 2/2", new Object[] { hl7FirstPatLoc, firstPatVisit.myVisitNumber, secondPatVisit.myVisitNumber });
        getConnector().update(secondPatientsWithVisitsContainer);
    }


    private static void unlinkPatient(PatientWithVisitsContainer theExisting, PatientWithVisits theHl7PatientWithVisits) throws Exception {

        // NOTE: theExisting pertains to the mergedKept patient in the
        // transaction

        PatientWithVisits existing = theExisting.getDocument();
        Cx firstMrn = existing.getMrn();

        // NOTE: secondPatient pertains to the mergedPrior patient in the
        // transaction

        Patient secondPatient = theHl7PatientWithVisits.myUnlinkSecondPatient;
        if (secondPatient == null || !secondPatient.hasIdWithTypeMr()) {
            ourLog.warn("Failed to unlink patient {} as no second patient was found", existing.getMrn());
            return;
        }

        PatientWithVisitsContainer secondPatientContainer = getOrCreatePatientContainer(secondPatient);
        PatientWithVisits secondPatientWithVisits = secondPatientContainer.getDocument();
        Cx secondMrn = secondPatient.getMrn();

        PatientWithVisits patientWithVisitsToMove = existing.findAndRemoveMergedInPatientWithVisits(secondMrn);

        if (patientWithVisitsToMove == null) {
            ourLog.warn("Could not unlink patient {} from {} because it was not found", secondMrn, firstMrn);
            return;
        } else {
            patientWithVisitsToMove.ensureVisits();
            secondPatientWithVisits.ensureVisits();
            List<Visit> visitsToMove = patientWithVisitsToMove.myVisits;
            //NOTE: This is only being done incase the mergedPrior patient some how got recreated since the merge event
            //and has visits attached to it. We should avoid making any duplicate visits in this case.
            for (Visit visit : visitsToMove) {
                Visit inTheWayVisit = theExisting.getDocument().findAndRemoveVisit(visit.myVisitNumber);
                if (inTheWayVisit != null) {
                    ourLog.warn("Visit {} from the mergedKept patient already exisits for the mergedPrior patient, so going to overwrite it", visit.myVisitNumber);                    
                }                                
            }            
            secondPatientWithVisits.myVisits.addAll(patientWithVisitsToMove.myVisits);
        }

        ourLog.info("Unlinking patient {} and patient {} - Step 1/2", firstMrn, secondMrn);
        getConnector().update(theExisting);

        ourLog.info("Unlinking patient {} and patient {} - Step 2/2", firstMrn, secondMrn);
        getConnector().update(secondPatientContainer);

    }
    
    
    private static void convertVisit(PatientWithVisitsContainer theExisting, PatientWithVisits theHl7PatientWithVisits) throws Exception {

        if (theHl7PatientWithVisits.myVisits.size() > 1) {
            throw new IllegalStateException("Should not have more than one visit in the object from the parser");
        }

        Cx mrn = theHl7PatientWithVisits.myPatient.getMrn();
        Cx preConvVisitNumber = theHl7PatientWithVisits.myMergeInfo != null ? theHl7PatientWithVisits.myMergeInfo.myMergeVisitId : null;
        PatientWithVisits existing = theExisting.getDocument();
        String eventType = theHl7PatientWithVisits.myMostRecentEventCode;

        Visit postConvVisit = theHl7PatientWithVisits.myVisits.get(0);
        Cx postConvVisitNumber = postConvVisit.myVisitNumber;

        Visit existingPostConvVisit = existing.findAndRemoveVisit(postConvVisitNumber);
        if (existingPostConvVisit == null) {
            postConvVisit.setStatus(eventType);
            existingPostConvVisit = postConvVisit;
        } else {
            existingPostConvVisit.copyFromHl7(postConvVisit, eventType);
            existingPostConvVisit.setStatus(eventType);
        }

        if (preConvVisitNumber != null) {
            existingPostConvVisit.ensurePreviousVisitNumbers();
            //Do this check to make sure we don't add duplicates
            if (!existingPostConvVisit.myPreviousVisitNumbers.contains(preConvVisitNumber)) {
                existingPostConvVisit.myPreviousVisitNumbers.add(preConvVisitNumber);                
            }
            existing.ensureVisits();
            // need to set the status on the existing's preconv visit to
            // INACTIVE
            Visit existingPreConvVisit = existing.findVisit(preConvVisitNumber);
            if (existingPreConvVisit != null) {
                existingPreConvVisit.myVisitStatus = Constants.INACTIVE_VISIT_STATUS;
            } else {
                // preConvVisit does not exist so lets make a dummy visit, set
                // its status to
                // INACTIVE and then add it
                Visit newV = new Visit();
                newV.myVisitNumber = preConvVisitNumber;
                newV.myVisitStatus = Constants.INACTIVE_VISIT_STATUS;
                existing.myVisits.add(newV);
            }

        }

        existing.myVisits.add(existingPostConvVisit);
        existing.myPatient.copyFromHl7(theHl7PatientWithVisits.myPatient, eventType);

        ourLog.info("Converting visit {} to new visit {}", preConvVisitNumber, postConvVisitNumber);
        getConnector().update(theExisting);

    }   


    private static void updatePatient(PatientWithVisitsContainer theExisting, PatientWithVisits theHl7PatientWithVisits) throws Exception {

        PatientWithVisits existing = theExisting.getDocument();
        Cx mrn = theHl7PatientWithVisits.myPatient.getMrn();
        String eventType = theHl7PatientWithVisits.myMostRecentEventCode;
       
        existing.myPatient.copyFromHl7(theHl7PatientWithVisits.myPatient, eventType);

        ourLog.info("Updating patient with MRN {}", mrn);
        getConnector().update(theExisting);

    }


    private static void updateVisit(PatientWithVisitsContainer theExisting, PatientWithVisits theHl7PatientWithVisits) throws Exception {

        if (theHl7PatientWithVisits.myVisits.size() > 1) {
            throw new IllegalStateException("Should not have more than one visit in the object from the parser");
        }

        PatientWithVisits existing = theExisting.getDocument();
        String eventType = theHl7PatientWithVisits.myMostRecentEventCode;
        Visit newVisit = theHl7PatientWithVisits.myVisits.get(0);

        Visit existingVisit = existing.findAndRemoveVisit(newVisit.myVisitNumber);
        if (existingVisit == null) {
            newVisit.setStatus(eventType);
            if ( eventType.equals("A10") && newVisit.myPatientClassCode.equals("R") ) {
                //this is a case where we know that the visits admit time is also the arrival
                //time for the patient
                newVisit.myArrivalDates.add(newVisit.myAdmitDate);
                newVisit.myFormattedArrivalDates.add(newVisit.myAdmitDateFormatted);
            }
            existingVisit = newVisit;
        } else {
            existingVisit.copyFromHl7(newVisit, eventType);
            existingVisit.setStatus(eventType);
        }

        existing.ensureVisits();
        existing.myVisits.add(existingVisit);
        existing.myPatient.copyFromHl7(theHl7PatientWithVisits.myPatient, eventType);

        ourLog.info("Updating visit for MRN {}", theHl7PatientWithVisits.myPatient.getMrn().myIdNumber);
        getConnector().update(theExisting);
    }	
	

	private static StringBuilder storeClinicalDocument(StringBuilder theOutcome, ClinicalDocumentGroup doc) throws Exception {
		CouchDbConnector connector = getConnector();
		String id = "CDOC_" + doc.myPlacerGroupNumber.toKey();

		try {
			ClinicalDocumentContainer existing = connector.get(ClinicalDocumentContainer.class, id);
			existing.setDocument(doc);

			ourLog.info("Updating existing document with id {}", id);
			connector.update(existing);
			
			theOutcome.append("Updating clinical document record with identifier: ");
			theOutcome.append(doc.getRawDocumentIdNumberOrUnknown());
			theOutcome.append(". ");
			
		} catch (DocumentNotFoundException e) {
			ourLog.info("Creating new document with id {}", id);
			connector.create(id, new ClinicalDocumentContainer(doc));
			
			theOutcome.append("Creating clinical document record with identifier: ");
			theOutcome.append(doc.getRawDocumentIdNumberOrUnknown());
			theOutcome.append(". ");

		} catch (DbAccessException e) {
			
			List<Revision> rev = connector.getRevisions(id);
			String lastRev = rev.get(rev.size() - 1).getRev();
			
			ourLog.info("Deleting revs {} including {}", rev, lastRev);
			connector.delete(id, lastRev);
			
			ourLog.info("DB-Access exception, which probably means a property changed.. Creating new document with id {}", id);
			connector.create(id, new ClinicalDocumentContainer(doc));
			
			theOutcome.append("Recreating clinical document record with identifier: ");
			theOutcome.append(doc.getRawDocumentIdNumberOrUnknown());
			theOutcome.append(". ");

		}
		
		return theOutcome;
	}


	private static String storeMedicationAdmins(StringBuilder theOutcome, MedicationOrderWithAdmins theMedOrderWithAdmins) throws Exception {
		
		CouchDbConnector connector = getConnector();
		String id = toKey(theMedOrderWithAdmins.myOrder);
		
		try {
			MedicationOrderWithAdminsContainer existing = connector.get(MedicationOrderWithAdminsContainer.class, id);
			
			if ( theMedOrderWithAdmins.overwriteExistingMedOrder ) {
                existing.setDocument(theMedOrderWithAdmins);
            } else {
			    existing.getDocument().myAdmins = theMedOrderWithAdmins.myAdmins;
			    existing.getDocument().myRecordUpdatedDate = theMedOrderWithAdmins.myRecordUpdatedDate;
			    existing.getDocument().myRecordUpdatedDateFormatted = theMedOrderWithAdmins.myRecordUpdatedDateFormatted;
			}
			
			theOutcome.append("Added medication administration to order number ");
			theOutcome.append(existing.getDocument().getRawOrderNumberOrUnknown());
			theOutcome.append(". ");
			
			ourLog.info("Updating existing document with id {}", id);
			connector.update(existing);
			
		} catch (DocumentNotFoundException e) {
		    
			ourLog.info("Creating new document with id {}", id);          
			connector.create(id, new MedicationOrderWithAdminsContainer(theMedOrderWithAdmins));

			theOutcome.append("Created medication order number ");
			theOutcome.append(theMedOrderWithAdmins.getRawOrderNumberOrUnknown());
			theOutcome.append(" and added administration information. ");

		}   
		
		return theOutcome.toString();
	}


	private static void storeMedicationOrder(StringBuilder theOutcome, MedicationOrder theOrder) throws Exception {
		CouchDbConnector connector = getConnector();
		String id = toKey(theOrder);
		Date recordUpdatedDate = new Date();
        String recordUpdatedDateFormatted = DateFormatter.formatDateWithGmt(recordUpdatedDate);

		try {
			MedicationOrderWithAdminsContainer existing = connector.get(MedicationOrderWithAdminsContainer.class, id);
			existing.getDocument().myOrder = theOrder;
			existing.getDocument().myRecordUpdatedDate = recordUpdatedDate;
			existing.getDocument().myRecordUpdatedDateFormatted = recordUpdatedDateFormatted;

			ourLog.info("Updating existing document with id {}", id);
			connector.update(existing);

			theOutcome.append("Updated medication order number ");
			theOutcome.append(existing.getDocument().getRawOrderNumberOrUnknown());
			theOutcome.append(". ");

		} catch (DocumentNotFoundException e) {

			ourLog.info("Creating new document with id {}", id);
			MedicationOrderWithAdmins owa =  new MedicationOrderWithAdmins(theOrder);
			owa.myRecordUpdatedDate = recordUpdatedDate;
			owa.myRecordUpdatedDateFormatted = recordUpdatedDateFormatted;
			connector.create(id, new MedicationOrderWithAdminsContainer(owa));
			
			theOutcome.append("Created medication order number ");
			theOutcome.append(owa.getRawOrderNumberOrUnknown());
			theOutcome.append(". ");

		}
		
	}


	private static String toKey(MedicationOrder theOrder) {
	    return "MEDORDER_" + theOrder.myPlacerOrderNumber.toKey();
    }

	
	
    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String message = "MSH|^~\\&|ULTRA|TML|OLIS|OLIS|200905011130||MDM^T02|20169838|T|2.5\r"
                + "EVN||200905011130\r"
                + "PID|||7005728^^^2.16.840.1.113883.3.59.3:0947&QCPR^MR~00000000000^AA^^^JHN^^^^CANON&Ontario&HL70363~A93745H^^^^PPN^^^^CAN&&HL70363||Smith^Joseph^John^Junior^Mr^MD^L~Smith^Joe^^^^^A|Blanche^^^^^^L|19310313|M|||26 RIVINGTON AVE^^Goderich^ON^N7A3Y2^CAN^H~7 WOODPLUMPTON ROAD^^Port Stanley^ON^N0L2A0^CAN^M||1 (416) 340-4800^PRN^PH^^1^416^3404800^^Do not call after 5~^NET^^test@example.com|||||||||||||||||N\r"
                + "PV1|1|I|JS12^123^4^2.16.840.1.113883.3.59.1:4197^^^Q Building^12^JS12-123-4 GIM Inpatient Unit||||38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&2.16.840.1.113883.3.59.3:0947&QCPR|38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&2.16.840.1.113883.3.59.3:0947&QCPR|38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&2.16.840.1.113883.3.59.3:0947&QCPR|GIM|||||||38946^Blake^Donald^Thor^^^^^1.3.6.1.4.1.12201.1.2.1.5&2.16.840.1.113883.3.59.3:0947&QCPR||284675^^^2.16.840.1.113883.3.59.3:0947&QCPR^VN\r"
                + "ORC|1\r" + "OBR|1|||5001^Discharge Summary^1.3.6.1.4.1.12201.1.1.1.0001\r" + "TXA|1|DS||20111228150500-0500|5555^Smith^John^^^^Dr^^1.3.6.1.4.1.12201.1.2.1.5&2.16.840.1.113883.3.59.3:0947|20111228150500-0500|||5555^Smith^John^^^^Dr^^1.3.6.1.4.1.12201.1.2.1.5&2.16.840.1.113883.3.59.3:0947|||12345^2.16.840.1.113883.3.59.3:0947^QCPR|||||F\r"
                + "OBX|1|TX|14009^Medical Records Report^1.3.6.1.4.1.12201.1.1.1.0002|1|Discharge Summary||||||F\r" + "OBX|2|TX|14009^Medical Records Report^1.3.6.1.4.1.12201.1.1.1.0002|2|   Date: 13-Jan-2012||||||F\r" + "OBX|3|TX|14009^Medical Records Report^1.3.6.1.4.1.12201.1.1.1.0002|3|   Patient: Jones, Frank||||||F\r"
                + "OBX|4|TX|14009^Medical Records Report^1.3.6.1.4.1.12201.1.1.1.0002|4|   MRN: 0128376||||||F\r" + "OBX|5|TX|14009^Medical Records Report^1.3.6.1.4.1.12201.1.1.1.0002|5|   DOB: 01-Jan-1900||||||F\r" + "OBX|6|TX|14009^Medical Records Report^1.3.6.1.4.1.12201.1.1.1.0002|6|   Address: 99 FAKE AVE TORONTO ON M1A A1A||||||F\r"
                + "OBX|7|TX|14009^Medical Records Report^1.3.6.1.4.1.12201.1.1.1.0002|7|   Family Physician: Unknown, To Patient||||||F\r" + "OBX|8|TX|14009^Medical Records Report^1.3.6.1.4.1.12201.1.1.1.0002|8|   Referring Physician: Not Found, In Ulticare||||||F\r" + "OBX|9|TX|14009^Medical Records Report^1.3.6.1.4.1.12201.1.1.1.0002|9|   Location: Toronto Western Hospital, FA 9 122 2||||||F\r"
                + "OBX|10|TX|14009^Medical Records Report^1.3.6.1.4.1.12201.1.1.1.0002|10|||||||F\r" + "OBX|11|TX|14009^Medical Records Report^1.3.6.1.4.1.12201.1.1.1.0002|11|Visit Number: 111122222||||||F\r" + "OBX|12|TX|14009^Medical Records Report^1.3.6.1.4.1.12201.1.1.1.0002|12|Your patient Jones, Frank was admitted to Toronto Western Hospital on||||||F\r"
                + "OBX|13|TX|14009^Medical Records Report^1.3.6.1.4.1.12201.1.1.1.0002|13|04-Jan-2012 and discharged to a long term care facility on 12-Jan-2012.||||||F\r" + "OBX|14|ST|14011^Most Responsible Diagnosis^1.3.6.1.4.1.12201.1.1.1.0002|1|- Left Intertrochanteric Hip Fracture||||||F\r" + "OBX|15|ST|14012^Pre-Admission Co-morbidities^1.3.6.1.4.1.12201.1.1.1.0002|1|1. DM type 2||||||F\r"
                + "OBX|16|ST|14012^Pre-Admission Co-morbidities^1.3.6.1.4.1.12201.1.1.1.0002|2|2. CAD||||||F\r" + "OBX|17|ST|14012^Pre-Admission Co-morbidities^1.3.6.1.4.1.12201.1.1.1.0002|3|3. HTN||||||F\r" + "OBX|18|ST|14012^Pre-Admission Co-morbidities^1.3.6.1.4.1.12201.1.1.1.0002|4|4. Osteoporosis||||||F\r"
                + "OBX|19|ST|14012^Pre-Admission Co-morbidities^1.3.6.1.4.1.12201.1.1.1.0002|5|5. Dementia||||||F\r" + "OBX|20|ST|14012^Pre-Admission Co-morbidities^1.3.6.1.4.1.12201.1.1.1.0002|6|6. Aortic Valve Replacement||||||F\r" + "OBX|21|ST|14013^Post-Admission Co-morbidities^1.3.6.1.4.1.12201.1.1.1.0002|1|None||||||F\r"
                + "OBX|23|ST|14014^Additional Diagnoses^1.3.6.1.4.1.12201.1.1.1.0002||None||||||F\r" + "OBX|26|ST|14015^CLINICAL HISTORY^1.3.6.1.4.1.12201.1.1.1.0002||This patient sustained the aforementioned injury from a mechanical fall.||||||F";
        PipeParser parser = new PipeParser();
        parser.setValidationContext(new ValidationContextImpl());
        MDM_T02 hl7Msg = (MDM_T02) parser.parse(message);

        // ClinicalDocument converted = new
        // Converter().convertClinicalDocument(hl7Msg);
        // storeClinicalDocument(converted);

        ClinicalDocumentGroup doc = getClinicalDocument("50011.3.6.1.4.1.12201.1.1.1.0001Discharge Summary777");
        System.out.println(doc.revision);
    }	

}
