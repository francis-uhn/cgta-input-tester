package ca.cgta.input.model.inner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MedicationOrder {

	public Patient myPatient;
	public String myStatusCode;
	public String myStatusName;
	public Ei myPlacerGroupNumber;
	public Ei myPlacerOrderNumber;
	public Visit myVisit;
	public double myEncodedOrderQuantityNumber;
	public String myEncodedOrderQuantityRepeatPattern;
	public String myEncodedOrderQuantityDuration;
	public Date myEncodedOrderQuantityStartTime;
	public String myEncodedOrderQuantityStartTimeFormatted;
	public Date myEncodedOrderQuantityEndTime;
	public String myEncodedOrderQuantityEndTimeFormatted;
	public Ce myEncodedOrderGiveCode;
	public double myEncodedOrderGiveMinimum;
	public double myEncodedOrderGiveMaximum;
	public Ce myEncodedOrderGiveUnits;
	public Ce myEncodedOrderGiveDosageForm;
	public ArrayList<Ce> myEncodedOrderProvidersAdministrationInstructions = new ArrayList<Ce>();
	public List<Note> myNotes = new ArrayList<Note>();
	public ArrayList<Ce> myMedicationRoutes = new ArrayList<Ce>();
	public ArrayList<MedicationComponent> myMedicationComponents = new ArrayList<MedicationComponent>();
   
	
	

	
	
	
}
