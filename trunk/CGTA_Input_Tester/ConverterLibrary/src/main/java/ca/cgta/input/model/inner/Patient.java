package ca.cgta.input.model.inner;

import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.cgta.input.converter.DateFormatter;

public class Patient {

	public String myDeathIndicator;
	public ArrayList<Cx> myPatientIds;
	public ArrayList<Xpn> myPatientNames;
	public Xpn myMothersMaidenName;
	public Date myDateOfBirth;
	public String myAdministrativeSex;
	public ArrayList<Xad> myPatientAddresses;
	public Ce myPrimaryLanguage;
	public Date myDeathDateAndTime;
	public String myDeathDateAndTimeFormatted;
	public ArrayList<Xtn> myPhoneNumbers;
    public ArrayList<PersonInRole> myPersonInRoles;
    public ArrayList<AssociatedParty> myAssociatedParties;
    public ArrayList<AdverseReaction> myAdverseReactions;
    /** Just an internal flag to show when the database actually saved/updated this record */
    public Date myRecordUpdatedDate;
    public String myRecordUpdatedDateFormatted;
    public String myDeactivatePatientIndicator;     
    
    
    

    
    private static final Logger ourLog = LoggerFactory.getLogger(Patient.class);
	
	/**
	 * Constructor
	 */
	public Patient() {
		super();
		setRecordUpdatedTime();   
	}
	
	/**
	 * Constructor
	 */
	public Patient(Cx theId) {
		ensurePatientIds();
		myPatientIds.add(theId);
		setRecordUpdatedTime();   
	}

	@JsonIgnore
	private void ensurePatientIds() {
		if (myPatientIds == null) {
			myPatientIds = new ArrayList<Cx>();
		}
	}

	@JsonIgnore
	private void copyDemographicsFrom(Patient theHl7MsgPatient) {
		myDeathIndicator = theHl7MsgPatient.myDeathIndicator;
		myPatientIds = theHl7MsgPatient.myPatientIds;
		myPatientNames = theHl7MsgPatient.myPatientNames;
		myMothersMaidenName = theHl7MsgPatient.myMothersMaidenName;
		myDateOfBirth = theHl7MsgPatient.myDateOfBirth;
		myAdministrativeSex = theHl7MsgPatient.myAdministrativeSex;
		myPatientAddresses = theHl7MsgPatient.myPatientAddresses;
		myPrimaryLanguage = theHl7MsgPatient.myPrimaryLanguage;
		myDeathDateAndTime = theHl7MsgPatient.myDeathDateAndTime;
		myDeathDateAndTimeFormatted = theHl7MsgPatient.myDeathDateAndTimeFormatted;
		myPhoneNumbers = theHl7MsgPatient.myPhoneNumbers;
		myPersonInRoles = theHl7MsgPatient.myPersonInRoles;
		myAssociatedParties = theHl7MsgPatient.myAssociatedParties;			
		
	}
		


    @JsonIgnore
    private void copyAllergiesFrom(Patient theHl7MsgPatient) {
        myAdverseReactions = theHl7MsgPatient.myAdverseReactions;
    }
	
	
	
    @JsonIgnore  
    private void setRecordUpdatedTime() {
        myRecordUpdatedDate = new Date();
        myRecordUpdatedDateFormatted = DateFormatter.formatDateWithGmt(myRecordUpdatedDate);        
        
    }

    
    @JsonIgnore
    public void copyFromHl7(Patient theHl7MsgPatient, String theEventType) {
        
        if ( theEventType == null || theEventType.equals("")) {
            throw new IllegalArgumentException("Eventype must be supplied");
        }  
        
        if ( theEventType.equals("A01") || theEventType.equals("A04") || 
                theEventType.equals("A08") || theEventType.equals("A28") || theEventType.equals("A31")) {
            
            this.copyDemographicsFrom(theHl7MsgPatient); 
            
            if (theEventType.equals("A31") && theHl7MsgPatient.myDeactivatePatientIndicator != null && !theHl7MsgPatient.myDeactivatePatientIndicator.equals("")) {
                this.myDeactivatePatientIndicator = theHl7MsgPatient.myDeactivatePatientIndicator;                                                
            }   
            
            this.setRecordUpdatedTime();
        }
        
        
        if ( theEventType.equals("A60")) {
            this.copyAllergiesFrom(theHl7MsgPatient);
            this.setRecordUpdatedTime();
        } 
        
    }
	
	

	@JsonIgnore
	public String toKey() {
		for (Cx next : myPatientIds) {
			if ("MR".equals(next.myIdTypeCode)) {
				return next.toKey();
			}
		}
		
		return null;
	}

	@JsonIgnore
	public boolean hasIdWithTypeMr() {
		if (myPatientIds == null) {
			myPatientIds = new ArrayList<Cx>();
		}
		for (Cx next : myPatientIds) {
			if ("MR".equals(next.myIdTypeCode) && StringUtils.isNotBlank(next.myIdNumber)) {
				return true;
			}
		}
		return false;
	}

	@JsonIgnore
	public Cx getMrn() {
		if (myPatientIds == null) {
			myPatientIds = new ArrayList<Cx>();
		}
		for (Cx next : myPatientIds) {
			if ("MR".equals(next.myIdTypeCode)) {
				return next;
			}
		}
		return null;
	}
	
	

}
