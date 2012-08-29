package ca.cgta.input.model.outer;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

import ca.cgta.input.model.inner.ClinicalDocumentSection;
import ca.cgta.input.model.inner.Ei;
import ca.cgta.input.model.inner.Patient;
import ca.cgta.input.model.inner.Visit;

public class ClinicalDocumentGroup extends AbstractDocument {

	/** Just an internal flag to show when the database actually saved this record */
	public Date myRecordUpdatedDate;
	public String myRecordUpdatedDateFormatted;
	public String myType = "DOCUMENT";
	public Patient myPatient;
	public List<ClinicalDocumentSection> mySections;
	public Ei myPlacerGroupNumber;
	public Visit myVisit;

	@JsonIgnore
	public String getRawDocumentIdNumberOrUnknown() {
		if (myPlacerGroupNumber == null || StringUtils.isBlank(myPlacerGroupNumber.myId)) {
			return "(unknown)";
		}
	    return myPlacerGroupNumber.myId;
    }
	
}
