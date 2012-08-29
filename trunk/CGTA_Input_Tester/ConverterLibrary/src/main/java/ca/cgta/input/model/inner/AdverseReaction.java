package ca.cgta.input.model.inner;

import java.util.Date;
import java.util.ArrayList;

public class AdverseReaction {

	public Ce myAllergenTypeCode;
	public Ce myAllergenCode;
	public Ce myAllergySeverityCode;
	public ArrayList<String> myAllergyReactionCodes;
//	public Ce mySensitivityToCausativeAgentCode;
	public Date myOnsetDate;
    public String myOnsetDateFormatted;
	public String myOnsetText;
    public Date myReportedDateTime;
    public String myReportedDateTimeFormatted;
    public Ce myRelationshipToPatient;
    
	
	
//2   250 CE  O       0127    00204   Allergen Type Code
//3   250 CE  R           00205   Allergen Code/Mnemonic/Description
//4   250 CE  O       0128    00206   Allergy Severity Code
//5   15  ST  O   Y       00207   Allergy Reaction Code
//11    8   DT  O           01556   Onset Date  
//12   60   ST  O        Onset Text 
//13   19  TS   O       Reported Date/Time
//15   250 CE   O       Relationship to Patient Code    
    	

	
	

}
