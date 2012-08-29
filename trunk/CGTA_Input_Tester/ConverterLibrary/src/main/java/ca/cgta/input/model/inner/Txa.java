package ca.cgta.input.model.inner;

import java.util.ArrayList;
import java.util.Date;

public class Txa {

	public String myDocumentTypeCode;
	public String myDocumentTypeName;
	public Date myEffectiveTime;
	public ArrayList<Xcn> myPrimaryActivityProviders;
	public Date myOriginationTime;
	public Date myTranscriptionTime;
	public ArrayList<Date> myEditTimes;
	public ArrayList<Xcn> myOriginators;
	public ArrayList<Xcn> myAuthenticators;
	public ArrayList<Xcn> myTranscriptionists;
	public Ei myUniqueDocumentNumber;
	public Ei myParentDocumentNumber;
	public ArrayList<Ei> myPlacerOrderNumbers;
	public Ei myFillerOrderNumber;
	public String myDocumentCompletionStatus;
	public String myDocumentConfidentialityStatus;
	public ArrayList<Ppn> myAuthenticationPersonTimestamps;

}
