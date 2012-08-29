package ca.cgta.input.model.outer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

import ca.cgta.input.model.inner.Cx;
import ca.cgta.input.model.inner.Mrg;
import ca.cgta.input.model.inner.Patient;
import ca.cgta.input.model.inner.Pl;
import ca.cgta.input.model.inner.Visit;

public class PatientWithVisits extends AbstractDocument {
    
    
	public String myType = "PATIENT_WITH_VISITS";
	public List<PatientWithVisits> myMergedInPatientsWithVisits;
	public Patient myPatient;
	public List<Visit> myVisits;
	
	
	
	
    /**
     * This just comes from the HL7 processor module, it is not stored in the DB
     */
    @JsonIgnore
    public List<String> myAdminHistory;	
	
	
    /**
     * This just comes from the HL7 processor module, it is not stored in the DB
     */
    @JsonIgnore	
	public String myMostRecentEventCode;
	
	
    /**
     * This just comes from the HL7 processor module, it is not stored in the DB
     */
    @JsonIgnore
	public Mrg myMergeInfo;


    /**
	 * This just comes from the HL7 processor module, it is not stored in the DB
	 */
	@JsonIgnore
	public Patient myUnlinkSecondPatient;
	
	
    /**
     * This just comes from the HL7 processor module, it is not stored in the DB
     */
    @JsonIgnore
    public Cx myBedSwapSecondPatientMrn;
	
	
    /**
     * This just comes from the HL7 processor module, it is not stored in the DB
     */
    @JsonIgnore
    public Cx myBedSwapSecondPatientVisitNumber;
    
    
    /**
     * This just comes from the HL7 processor module, it is not stored in the DB
     */
    @JsonIgnore
    public Pl myBedSwapSecondPatientLocation;
    
    

	
	
	
	/**
	 * Constructor
	 */
	public PatientWithVisits() {
		super();
	}

	/**
	 * Constructor
	 */
	public PatientWithVisits(Patient thePatient) {
		myPatient = thePatient;
	}
	
	
    @JsonIgnore
	public Cx getMrn() {
		return myPatient.getMrn();
	}
	
	@JsonIgnore
	public void ensureVisits() {
		if (myVisits == null) {
			myVisits = new ArrayList<Visit>();
		}
	}
	
	
    @JsonIgnore	
    public void ensureMergedInPatientsWithVisits() {
        if (myMergedInPatientsWithVisits == null) {
            myMergedInPatientsWithVisits = new ArrayList<PatientWithVisits>();
        }
    }
	


	
    @JsonIgnore
    public Visit findAndRemoveVisit(Cx theVisitNumber) {
        ensureVisits();
        for (Visit nextVisit : myVisits) {
            if (nextVisit.myVisitNumber.equals(theVisitNumber)) {
                myVisits.remove(nextVisit);
                return nextVisit;
            }
        }

        return null;
    }
    
    
    @JsonIgnore
    public Visit findVisit(Cx theVisitNumber) {
        ensureVisits();
        for (Visit nextVisit : myVisits) {
            if (nextVisit.myVisitNumber.equals(theVisitNumber)) {                
                return nextVisit;
            }
        }
        return null;
    }
    
    
    
    
    @JsonIgnore
    public Visit findAndRemoveMergedInVisit(Cx theVisitNumber) {
        ensureMergedInPatientsWithVisits();
        for (PatientWithVisits nextPatient : myMergedInPatientsWithVisits) {
            nextPatient.ensureVisits();
            for (Visit nextVisit : nextPatient.myVisits) {
                if (nextVisit.myVisitNumber.equals(theVisitNumber)) {
                    nextPatient.myVisits.remove(nextVisit);
                    return nextVisit;
                }
            }
        }
        return null;
    }
    
    
    @JsonIgnore
    public PatientWithVisits findMergedInPatientWithVisits(Cx theMrn) {
        ensureMergedInPatientsWithVisits();
        for (PatientWithVisits next : myMergedInPatientsWithVisits) {
            if (next.myPatient.getMrn().equals(theMrn)) {
                return next;
            }
        }
        return null;
    }
    
    
    @JsonIgnore
    public PatientWithVisits findAndRemoveMergedInPatientWithVisits(Cx theMrn) {
        ensureMergedInPatientsWithVisits();
        for (Iterator<PatientWithVisits> iter = myMergedInPatientsWithVisits.iterator(); iter.hasNext(); ) {
            PatientWithVisits next = iter.next();
            if (next.myPatient.getMrn().equals(theMrn)) {
                iter.remove();
                return next;
            }
        }
        return null;
    }
    
    
    @JsonIgnore
    public void addNewVisits(List <Visit> theVisits) {
        
        if ( theVisits == null || theVisits.isEmpty()  ) {
            return;
        }
        
        ensureVisits();        
        Set vns = new HashSet<Visit>(); 
        for (Visit v : myVisits) {
          vns.add(v.myVisitNumber.myIdNumber);
        }
        
        for (Visit newV : theVisits) {
            if ( !vns.contains(newV.myVisitNumber.myIdNumber) ) {
                myVisits.add(newV);
            }
        }       
        
    }

    @JsonIgnore
	public String getRawVisitNumberOrUnknown() {
		if (myVisits == null || myVisits.size() < 1 || myVisits.get(0).myVisitNumber == null || StringUtils.isBlank(myVisits.get(0).myVisitNumber.myIdNumber)) {
			return "(unknown)";
		} else {
			return (myVisits.get(0).myVisitNumber.myIdNumber);
		}
    }

    @JsonIgnore
	public String getRawMrnOrUnknown() {
		if (getMrn() == null || StringUtils.isBlank(getMrn().myIdNumber)) {
			return "(unknown)";
		}
	    return getMrn().myIdNumber;
    }

    @JsonIgnore
	public String getRawMergeMrnOrUnknown() {
		if (myMergeInfo == null || myMergeInfo.getMrn() == null || StringUtils.isBlank(myMergeInfo.getMrn().myIdTypeCode)) {
			return "(unknown)";
		}
	    return myMergeInfo.getMrn().myIdTypeCode;
    }    
    
	
	


}
