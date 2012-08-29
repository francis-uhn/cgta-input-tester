package ca.cgta.input.model.inner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

public class ClinicalDocumentSection {

	public ConfidentialityStatusEnum myConfidentiality;
	public ArrayList<Xcn> myCopyToProviders;
	public List<ClinicalDocumentData> myData;
	public Date myDate;
	public String myDateFormatted;
	public Date myEndDate;
	public String myEndDateFormatted;
	public ArrayList<Xcn> myOrderingProviders;
	public Ei myParentSectionId;
	public Xcn myPrincipalInterpreter;
	public Ce mySectionCode;
	public Ei mySectionId;
	public String mySectionName;
	public String myStatusCode = "";
	public List<Note> myNotes = new ArrayList<Note>();
	public String myStatus;
    
	
	@JsonIgnore
	public String myAppendMode;

	@JsonIgnore
	public void mergeIn(ClinicalDocumentSection theSection) {
	    myConfidentiality = theSection.myConfidentiality;
	    myCopyToProviders = theSection.myCopyToProviders;
	    myDate = theSection.myDate;
	    myDateFormatted = theSection.myDateFormatted;
	    myEndDate = theSection.myEndDate;
	    myEndDateFormatted = theSection.myEndDateFormatted;
	    myOrderingProviders = theSection.myOrderingProviders;
	    myParentSectionId = theSection.myParentSectionId;
	    myPrincipalInterpreter = theSection.myPrincipalInterpreter;
	    mySectionCode = theSection.mySectionCode;
	    mySectionId = theSection.mySectionId;
	    mySectionName = theSection.mySectionName;
	    myStatusCode = theSection.myStatusCode;
	    myNotes = theSection.myNotes;
	    myStatus = theSection.myStatus;
	    
	    if (theSection.myAppendMode.equals("S")) {
	    	myData = theSection.myData;
	    } else if (theSection.myAppendMode.equals("A")) {
	    	myData = new ArrayList<ClinicalDocumentData>(myData);
	    	myData.addAll(theSection.myData);
	    } else {
	    	throw new IllegalArgumentException("Unknown append mode: " + theSection.myAppendMode);
	    }
	    	
	    
    }

}
