package ca.cgta.input.model.inner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
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
	public ArrayList<Xcn> myConsultingDoctors;
	public ArrayList<Date> myDischargeDates;
	public ArrayList<Diagnosis> myDiagnoses;
	public ArrayList<String> myDischargeDatesFormatted;
	public String myHospitalService;
	public String myHospitalServiceName;

	public String myPatientClassCode;
	public String myAdmissionType;

	public String myPatientClassName;	
	
	public Pl myPriorPatientLocation;
	public ArrayList<Xcn> myReferringDoctors;
	public Cx myVisitNumber;
	/** Just an internal flag to show when the database actually saved/updated this record */
    public Date myRecordUpdatedDate;
    public String myRecordUpdatedDateFormatted;
    
    //Only used for visits with AdmissionType of R 
    public List<Date> myArrivalDates = new ArrayList<Date>();
    public List<String> myFormattedArrivalDates = new ArrayList<String>();
    
    
    //These fields can have hl7null ("") as a value in the first component
    public ArrayList<Xcn> myAttendingDoctors;
	
	
	//These fields are not set using data extracted from an HL7 message
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
		myPriorPatientLocation = theHl7MsgVisit.myPriorPatientLocation;
		myReferringDoctors = theHl7MsgVisit.myReferringDoctors;
		myVisitNumber = theHl7MsgVisit.myVisitNumber;
		
	}
	
	
    @JsonIgnore
    //may need in the future
    private void copySuppliedValuesFrom(Visit theHl7MsgVisit) {
        myAdmissionLevelOfCareForEmergencyVisit = (theHl7MsgVisit.myAdmissionLevelOfCareForEmergencyVisit != null) ? theHl7MsgVisit.myAdmissionLevelOfCareForEmergencyVisit : myAdmissionLevelOfCareForEmergencyVisit;
        myAdmitDate = (theHl7MsgVisit.myAdmitDate != null) ? theHl7MsgVisit.myAdmitDate : myAdmitDate;
        myAdmitDateFormatted = (StringUtils.isNotBlank(theHl7MsgVisit.myAdmitDateFormatted)) ? theHl7MsgVisit.myAdmitDateFormatted : myAdmitDateFormatted;
        myAdmitReasonForEmergencyVisit = (theHl7MsgVisit.myAdmitReasonForEmergencyVisit != null) ? theHl7MsgVisit.myAdmitReasonForEmergencyVisit : myAdmitReasonForEmergencyVisit;
        myAdmittingDoctors = (theHl7MsgVisit.myAdmittingDoctors != null && theHl7MsgVisit.myAdmittingDoctors.size()!= 0) ? theHl7MsgVisit.myAdmittingDoctors : myAdmittingDoctors;
        myAssignedPatientLocation = (theHl7MsgVisit.myAssignedPatientLocation != null) ? theHl7MsgVisit.myAssignedPatientLocation : myAssignedPatientLocation;
        myAttendingDoctors = (theHl7MsgVisit.myAttendingDoctors != null && theHl7MsgVisit.myAttendingDoctors.size()!= 0) ? theHl7MsgVisit.myAttendingDoctors : myAttendingDoctors;
        myConsultingDoctors = (theHl7MsgVisit.myConsultingDoctors != null && theHl7MsgVisit.myConsultingDoctors.size()!= 0) ? theHl7MsgVisit.myConsultingDoctors : myConsultingDoctors;
        myDischargeDates = (theHl7MsgVisit.myDischargeDates != null && theHl7MsgVisit.myDischargeDates.size()!= 0) ? theHl7MsgVisit.myDischargeDates : myDischargeDates;
        myDiagnoses = (theHl7MsgVisit.myDiagnoses != null && theHl7MsgVisit.myDiagnoses.size()!= 0) ? theHl7MsgVisit.myDiagnoses : myDiagnoses;
        myDischargeDatesFormatted = (theHl7MsgVisit.myDischargeDatesFormatted != null && theHl7MsgVisit.myDischargeDatesFormatted.size()!= 0) ? theHl7MsgVisit.myDischargeDatesFormatted : myDischargeDatesFormatted;
        myHospitalService = (StringUtils.isNotBlank(theHl7MsgVisit.myHospitalService)) ? theHl7MsgVisit.myHospitalService : myHospitalService;
        myHospitalServiceName = (StringUtils.isNotBlank(theHl7MsgVisit.myHospitalServiceName)) ? theHl7MsgVisit.myHospitalServiceName : myHospitalServiceName;
        myPatientClassCode = (StringUtils.isNotBlank(theHl7MsgVisit.myPatientClassCode)) ? theHl7MsgVisit.myPatientClassCode : myPatientClassCode;
        myAdmissionType = (StringUtils.isNotBlank(theHl7MsgVisit.myAdmissionType)) ? theHl7MsgVisit.myAdmissionType : myAdmissionType;
        myPatientClassName = (StringUtils.isNotBlank(theHl7MsgVisit.myPatientClassName)) ? theHl7MsgVisit.myPatientClassName : myPatientClassName;        
        myPriorPatientLocation = (theHl7MsgVisit.myPriorPatientLocation != null) ? theHl7MsgVisit.myPriorPatientLocation : myPriorPatientLocation;
        myReferringDoctors = (theHl7MsgVisit.myReferringDoctors != null && theHl7MsgVisit.myReferringDoctors.size()!= 0) ? theHl7MsgVisit.myReferringDoctors : myReferringDoctors;
        myVisitNumber = (theHl7MsgVisit.myVisitNumber != null) ? theHl7MsgVisit.myVisitNumber : myVisitNumber;
        
    }
    
    
    
    @JsonIgnore
    private ArrayList<Xcn> processHl7NullableField(ArrayList<Xcn> storedField, ArrayList<Xcn> hl7MsgField){
        
        if (hl7MsgField == null || hl7MsgField.size() == 0) {
            return storedField;
        }
        
        if ("\"\"".equals(hl7MsgField.get(0).myId)) {
            return null;
        }
        
        return hl7MsgField;        
        
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
        
        if ( theEventType.equals("A01") || theEventType.equals("A04") || theEventType.equals("A05") || theEventType.equals("A06") 
                || theEventType.equals("A07") || theEventType.equals("A08")) {
            this.copyFrom(theHl7MsgVisit);
            setRecordUpdatedTime();
            
        }
        

        if ( theEventType.equals("A10") && theHl7MsgVisit.myPatientClassCode.equals("R")) {
            myArrivalDates.add(theHl7MsgVisit.myAdmitDate);            
            myFormattedArrivalDates.add(theHl7MsgVisit.myAdmitDateFormatted);
            setRecordUpdatedTime();
            
        }
        
        
        if ( theEventType.equals("A02")) {
            myPriorPatientLocation = theHl7MsgVisit.myPriorPatientLocation;
            myAssignedPatientLocation = theHl7MsgVisit.myAssignedPatientLocation;           
            setRecordUpdatedTime(); 
        }
        
        
        if ( theEventType.equals("A03")) {
            
            myAttendingDoctors = processHl7NullableField(myAttendingDoctors,theHl7MsgVisit.myAttendingDoctors);
            
            if ( theHl7MsgVisit.myPriorPatientLocation != null ) {
                myPriorPatientLocation = theHl7MsgVisit.myPriorPatientLocation;                
            }
            if ( theHl7MsgVisit.myAssignedPatientLocation != null ) {
                myAssignedPatientLocation = theHl7MsgVisit.myAssignedPatientLocation;                
            }
            myDischargeDates = theHl7MsgVisit.myDischargeDates;
            myDischargeDatesFormatted = theHl7MsgVisit.myDischargeDatesFormatted;
            
            setRecordUpdatedTime(); 
        }
        
        
        
        
        if ( theEventType.equals("A13") && (theHl7MsgVisit.myAssignedPatientLocation != null || theHl7MsgVisit.myPriorPatientLocation != null)) {            
            
            if ( theHl7MsgVisit.myAssignedPatientLocation != null) {
                myAssignedPatientLocation = theHl7MsgVisit.myAssignedPatientLocation;
            }
            if ( theHl7MsgVisit.myPriorPatientLocation != null) {                
                myPriorPatientLocation = theHl7MsgVisit.myPriorPatientLocation;
            }                
            setRecordUpdatedTime();
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
            this.myDischargeDatesFormatted = null;
            setRecordUpdatedTime(); 
            return;
        }
        
        if ( theEventType.equals("A03")) {
            this.myVisitStatus = Constants.INACTIVE_VISIT_STATUS;
            setRecordUpdatedTime(); 
            return;
        }
        
        if ( theEventType.equals("A05")) {            
            this.myVisitStatus = Constants.PREADMIT_VISIT_STATUS;
            this.myDischargeDates = null;         
            this.myDischargeDatesFormatted = null;
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
    
    
    
    /**
     *  Use this method to clear hl7Nullable fields 
     *  that have been set to hl7Null ("") for a given
     *  event type
     */
    @JsonIgnore
    public void clearHl7Nulls(String theEventType){
        
        if (theEventType.equals("A03")) {
            if (myAttendingDoctors != null && myAttendingDoctors.size() != 0
                    && "\"\"".equals(myAttendingDoctors.get(0).myId)) {
                myAttendingDoctors = null;
            }
        }
        
    }
        
    
    
	
	
	
	@JsonIgnore
	public void ensurePreviousVisitNumbers() {
		if (myPreviousVisitNumbers == null) {
			myPreviousVisitNumbers = new ArrayList<Cx>();
		}
	}
}
