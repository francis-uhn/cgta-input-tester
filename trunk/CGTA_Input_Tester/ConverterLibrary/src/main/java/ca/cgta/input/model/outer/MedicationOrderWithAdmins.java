package ca.cgta.input.model.outer;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

import ca.cgta.input.model.inner.MedicationAdmin;
import ca.cgta.input.model.inner.MedicationOrder;

public class MedicationOrderWithAdmins extends AbstractDocument {

	public MedicationOrderWithAdmins() {
	}


	public MedicationOrderWithAdmins(MedicationOrder theOrder) {
		myOrder = theOrder;
	}	
	
	 /** Just an internal flag to show when the database actually saved/updated this record */
    public Date myRecordUpdatedDate;
    public String myRecordUpdatedDateFormatted;
	
    public String myType = "MEDICATION_ORDER";
	public MedicationOrder myOrder;
	public List<MedicationAdmin> myAdmins;
	
    /**
     * Set to false if the MedicationOrder has order identifiers populated but no medication order content, else set to true. 
     * This means that if the medication order exists in the database then we'll only presist the MedicationAdmin data and 
     * leave the existing medication order information intact
     *   
     */
    @JsonIgnore
    public boolean overwriteExistingMedOrder;

    @JsonIgnore
	public String getRawOrderNumberOrUnknown() {
    	if (myOrder == null || myOrder.myPlacerOrderNumber == null || StringUtils.isBlank(myOrder.myPlacerOrderNumber.myId)) {
    		return "(unknown)";
    	}
	    return myOrder.myPlacerOrderNumber.myId;
    } 
    	

}
