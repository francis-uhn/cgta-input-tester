package ca.cgta.input.model.inner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ClinicalDocumentData {

	public String myDataType = "";
	public Ce myCode = new Ce();
	public int mySubId = 0;
	public String myValue = "";
	public Ce myUnits = new Ce();
	public String myRefRange = "";
	public String myAbnormalFlagCode = "";
	public String myDataStatus = "";
	public String myDataStatusCode = "";
	public Object myAbnormalFlagName = "";
	public Date myDateTimeOfObservation;
	public List<Note> myNotes = new ArrayList<Note>();
	public String myEncapsulatedDataType = "";
	public String myEncapsulatedDataSubType = "";
	public String myEncapsulatedDataMimeType = "";
	public String myEncapsulatedDataEncoding = "";
   
	
	
	
	
	
//ED.2    cGTA    R   20  ID  0191 Type of Data
//ED.3    cGTA    R   20  ID  0291 Data Subtype
//ED.4    cGTA    R   20  ID       Encoding    Base64 (Fixed Value)
//ED.5    cGTA    R   *   ST       Encoded Data    
	
	

	
	
	
	
	
	
	

}
