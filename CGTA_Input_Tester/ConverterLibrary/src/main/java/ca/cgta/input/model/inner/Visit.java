package ca.cgta.input.model.inner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.cgta.input.converter.DateFormatter;

public class Visit {

	// TODO: visit status
	
	/** Presenting Triage Code (0-5) for EP visit */
	public Ce myAdmissionLevelOfCareForEmergencyVisit;
	public Date myAdmitDate;
	public String myAdmitDateFormatted;
	public Ce myAdmitReasonForEmergencyVisit;
	public ArrayList<Xcn> myAdmittingDoctors;
	public Pl myAssignedPatientLocation;
	public ArrayList<Xcn> myAttendingDoctors;
	public ArrayList<Xcn> myConsultingDoctors;
	public ArrayList<Date> myDischargeDates;
	public ArrayList<Diagnosis> myDiagnoses;
	public ArrayList<String> myDischargeDatesFormatted;
	public String myHospitalService;
	public String myHospitalServiceName;

	public String myPatientClassCode;
	public String myAdmissionType;

	public String myPatientClassName;
	public String myPatientRequestedRecordLock;
	
	public Pl myPriorPatientLocation;
	public ArrayList<Xcn> myReferringDoctors;
	public Cx myVisitNumber;
	/** Just an internal flag to show when the database actually saved/updated this record */
    public Date myRecordUpdatedDate;
    public String myRecordUpdatedDateFormatted;
    
    //Only used for visits with AdmissionType of R 
    public List<Date> myArrivalDates = new ArrayList<Date>();
    public List<String> myFormattedArrivalDates = new ArrayList<String>();
	
  
	
	
	//NOTE: These fields are not set using data extracted from an HL7 message
    public List<Cx> myPreviousVisitNumbers;	
	public String myVisitStatus;
	
	
	private static final Logger ourLog = LoggerFactory.getLogger(Visit.class);
	
    /**
     * Constructor
     */
    public Visit() {        
        setRecordUpdatedTime();   
    }	
	

	@JsonIgnore
	private void copyFrom(Visit theHl7MsgVisit) {
	    myAdmissionLevelOfCareForEmergencyVisit = theHl7MsgVisit.myAdmissionLevelOfCareForEmergencyVisit;
	    myAdmitDate = theHl7MsgVisit.myAdmitDate;
	    myAdmitDateFormatted = theHl7MsgVisit.myAdmitDateFormatted;
	    myAdmitReasonForEmergencyVisit = theHl7MsgVisit.myAdmissionLevelOfCareForEmergencyVisit;
        myAdmittingDoctors = theHl7MsgVisit.myAdmittingDoctors;
        myAssignedPatientLocation = theHl7MsgVisit.myAssignedPatientLocation;
        myAttendingDoctors = theHl7MsgVisit.myAttendingDoctors;
        myConsultingDoctors = theHl7MsgVisit.myConsultingDoctors;
        myDischargeDates = theHl7MsgVisit.myDischargeDates;
        myDiagnoses = theHl7MsgVisit.myDiagnoses;
        myDischargeDatesFormatted = theHl7MsgVisit.myDischargeDatesFormatted;
        myHospitalService = theHl7MsgVisit.myHospitalService;
        myHospitalServiceName = theHl7MsgVisit.myHospitalServiceName;
		myPatientClassCode = theHl7MsgVisit.myPatientClassCode;
		myAdmissionType = theHl7MsgVisit.myAdmissionType;
		myPatientClassName = theHl7MsgVisit.myPatientClassName;
		myPatientRequestedRecordLock = theHl7MsgVisit.myPatientRequestedRecordLock;
		myPriorPatientLocation = theHl7MsgVisit.myPriorPatientLocation;
		myReferringDoctors = theHl7MsgVisit.myReferringDoctors;
		myVisitNumber = theHl7MsgVisit.myVisitNumber;
		
	}
	
	
    @JsonIgnore  
    private void setRecordUpdatedTime() {
        myRecordUpdatedDate = new Date();
        myRecordUpdatedDateFormatted = DateFormatter.formatDateWithGmt(myRecordUpdatedDate);        
        
    }	
	
	
	
    @JsonIgnore
    public void copyFromHl7(Visit theHl7MsgVisit, String theEventType) {
        
        if ( theEventType == null || theEventType.equals("")) {
            throw new IllegalArgumentException("Eventype must be supplied");
        }
        
        if ( theEventType.equals("A01") || theEventType.equals("A04") || theEventType.equals("A08")) {
            this.copyFrom(theHl7MsgVisit);
            setRecordUpdatedTime();
            
        }
        

        if ( theEventType.equals("A10") && theHl7MsgVisit.myPatientClassCode.equals("R")) {
            myArrivalDates.add(theHl7MsgVisit.myAdmitDate);
            myFormattedArrivalDates.add(theHl7MsgVisit.myAdmitDateFormatted);
            setRecordUpdatedTime();
            
        }
        
        
        if ( theEventType.equals("A02") || theEventType.equals("A03")) {
            myPriorPatientLocation = theHl7MsgVisit.myPriorPatientLocation;
            myAssignedPatientLocation = theHl7MsgVisit.myAssignedPatientLocation;
            if (theEventType.equals("A03")) {
                myDischargeDates = theHl7MsgVisit.myDischargeDates;
                myDischargeDatesFormatted = theHl7MsgVisit.myDischargeDatesFormatted;
            }
            setRecordUpdatedTime(); 
        }
        
        
        if ( theEventType.equals("A13")) {
            if ( myAssignedPatientLocation == null && theHl7MsgVisit.myAssignedPatientLocation != null ) {
                myAssignedPatientLocation = theHl7MsgVisit.myAssignedPatientLocation;   
                setRecordUpdatedTime(); 
            }
        }        
       
        
    }
    	
	
    @JsonIgnore
    public void setStatus(String theEventType) {
        
        if ( theEventType == null || theEventType.equals("")) {
            throw new IllegalArgumentException("Eventype must be supplied");
        }
        
        if ( theEventType.equals("A01") || theEventType.equals("A04") || theEventType.equals("A06") 
                || theEventType.equals("A07") || theEventType.equals("A13")) {
            this.myVisitStatus = Constants.ACTIVE_VISIT_STATUS;
            this.myDischargeDates = null;
            setRecordUpdatedTime(); 
            return;
        }
        
        if ( theEventType.equals("A03")) {
            this.myVisitStatus = Constants.INACTIVE_VISIT_STATUS;
            setRecordUpdatedTime(); 
            return;
        }
        
        
        if ( theEventType.equals("A11")) {
            this.myVisitStatus = Constants.CANCELLED_VISIT_STATUS;
            setRecordUpdatedTime(); 
            return;
        }
        
        if ( theEventType.equals("A23")) {
            this.myVisitStatus = Constants.DELETED_VISIT_STATUS;
            setRecordUpdatedTime(); 
            return;
        }
        
        if (this.myVisitStatus == null) {
            this.myVisitStatus = Constants.UNDEF_VISIT_STATUS;  
            setRecordUpdatedTime(); 
        }
                
    }    
    
    
	
	
	
	@JsonIgnore
	public void ensurePreviousVisitNumbers() {
		if (myPreviousVisitNumbers == null) {
			myPreviousVisitNumbers = new ArrayList<Cx>();
		}
	}
}
