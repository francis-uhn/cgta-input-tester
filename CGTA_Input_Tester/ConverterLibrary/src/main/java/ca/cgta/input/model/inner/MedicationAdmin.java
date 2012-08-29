package ca.cgta.input.model.inner;

import java.util.ArrayList;
import java.util.Date;


public class MedicationAdmin {

	public Ei myPlacerOrderNumber;
	public int myAdministrationNumber;
	public Date myStartTime;
	public String myStartTimeFormatted;
	public Date myEndTime;
	public String myEndTimeFormatted;
	public Ce myAdministeredCode;
	public int myAdministeredAmount;
	public Ce myAdministeredUnits;
	public ArrayList<Ce> myAdministrationNotes = new ArrayList<Ce>();
	public String myAdministeredPerTimeUnit;
	public Ce myMedicationRoute;

}
