package ca.cgta.input.model.inner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Tables {

	private static Map<String, Map<String, String>> ourTables = new HashMap<String, Map<String, String>>();

	static {
		{
			HashMap<String, String> tbl = new HashMap<String, String>();
			ourTables.put("0001", tbl);
			tbl.put("F", "Female");
			tbl.put("M", "Male");
			tbl.put("O", "Other");
			tbl.put("U", "Unknown");
		}
		{
			HashMap<String, String> tbl = new HashMap<String, String>();
			ourTables.put("0003ADT", tbl);
			tbl.put("A01", "Activate an inpatient visit");
			tbl.put("A02", "Transfer a patient");
			tbl.put("A03", "Discharge/End visit");
			tbl.put("A04", "Activate outpatient/emergency visit");
			tbl.put("A05", "Pre-admit a patient");
			tbl.put("A06", "Convert OP to IP");
			tbl.put("A07", "Convert IP to OP");
			tbl.put("A08", "Update visit information");
			tbl.put("A10", "Patient arrival");
			tbl.put("A11", "Cancel Admit");
			tbl.put("A13", "Cancel discharge");
			tbl.put("A17", "Swap patients");
			tbl.put("A23", "Delete a Patient Record");
			tbl.put("A28", "Add a person or patient");
			tbl.put("A31", "Update person or patient");
			tbl.put("A37", "Unlink person or patient");
			tbl.put("A40", "Merge patient information");
			tbl.put("A42", "Merge visit information");
			tbl.put("A45", "Move visit information");
			tbl.put("A60", "Update Adverse Reaction");
		}
		{
			HashMap<String, String> tbl0004 = new HashMap<String, String>();
			ourTables.put("0004", tbl0004);
			tbl0004.put("C", "CCAC Client");
			tbl0004.put("E", "Emergency");
			tbl0004.put("I", "Inpatient");
			tbl0004.put("O", "Outpatient");
			tbl0004.put("P", "Preadmit");
			tbl0004.put("R", "Recurring Outpatient");
			tbl0004.put("U", "Unknown/Other");
		}
		{
			HashMap<String, String> tbl = new HashMap<String, String>();
			ourTables.put("0053", tbl);
			tbl.put("2.16.840.1.113883.11.19436", "ICD-10-CA");
			tbl.put("2.16.840.1.113883.6.96", "SNOMED CT");
		}
		{
			HashMap<String, String> tbl = new HashMap<String, String>();
			ourTables.put("0063", tbl);
			tbl.put("SEL", "Self");
			tbl.put("SPO", "Spouse");
			tbl.put("DOM", "Life partner");
			tbl.put("CHD", "Child");
			tbl.put("GCH", "Grandchild");
			tbl.put("NCH", "Natural child");
			tbl.put("SCH", "Stepchild");
			tbl.put("FCH", "Foster child");
			tbl.put("DEP", "Handicapped dependent");
			tbl.put("WRD", "Ward of court");
			tbl.put("PAR", "Parent");
			tbl.put("MTH", "Mother");
			tbl.put("FTH", "Father");
			tbl.put("CGV", "Care giver");
			tbl.put("GRD", "Guardian");
			tbl.put("GRP", "Grandparent");
			tbl.put("EXF", "Extended family");
			tbl.put("SIB", "Sibling");
			tbl.put("BRO", "Brother");
			tbl.put("SIS", "Sister");
			tbl.put("FND", "Friend");
			tbl.put("OAD", "Other adult");
			tbl.put("EME", "Employee");
			tbl.put("EMR", "Employer");
			tbl.put("ASC", "Associate");
			tbl.put("EMC", "Emergency contact");
			tbl.put("OWN", "Owner");
			tbl.put("TRA", "Trainer");
			tbl.put("MGR", "Manager");
			tbl.put("NON", "None");
			tbl.put("STM", "Step Mother");
			tbl.put("STF", "Step Father");
			tbl.put("STP", "Step Parent");
			tbl.put("FOM", "Foster Mother");
			tbl.put("FOF", "Foster Father");
			tbl.put("FOP", "Foster Parent");

			tbl.put("UNK", "Unknown");
			tbl.put("OTH", "Other");

		}
		{
			HashMap<String, String> tbl = new HashMap<String, String>();
			ourTables.put("0069", tbl);
			tbl.put("CAR", "Cardiac Service");
			tbl.put("MED", "Medical Service");
			tbl.put("PUL", "Pulmonary Service");
			tbl.put("SUR", "Surgical Service");
			tbl.put("URO", "Urology Service");
		}
		{
			HashMap<String, String> tbl = new HashMap<String, String>();
			ourTables.put("0078", tbl);
			tbl.put("L", "Low");
			tbl.put("H", "High");
			tbl.put("LL", "Critical Low");
			tbl.put("HH", "Critical High");
			tbl.put("N", "Normal");
			tbl.put("A", "Abnormal");
			tbl.put("S", "Suseptible");
			tbl.put("R", "Resistant");
		}
		{
			HashMap<String, String> tbl = new HashMap<String, String>();
			ourTables.put("0085", tbl);
			tbl.put("F", "Final");
			tbl.put("C", "Corrected");
			tbl.put("W", "Withdrawn");
			tbl.put("I", "Incomplete / In Progress");
			tbl.put("D", "Draft");
			tbl.put("P", "Preliminary");
		}
		{
			HashMap<String, String> tbl = new HashMap<String, String>();
			ourTables.put("0119", tbl);
			tbl.put("NW", "New Order placed in ordering system");
			tbl.put("CA", "Order cancelled");
			tbl.put("OK", "Order verified");
			tbl.put("OR", "Order replaced (changed/modified)");
			tbl.put("OD", "Order discontinued");
			tbl.put("HD", "Order held");
			tbl.put("RL", "Order released (after being held)");
		}
		{
			HashMap<String, String> tbl = new HashMap<String, String>();
			ourTables.put("0125", tbl);
			tbl.put("DT", "Date");
			tbl.put("ED", "Encapsulated Data");
			tbl.put("FT", "Formatted Text (Display)");
			tbl.put("NM", "Numeric");
			tbl.put("SN", "Structured Numeric");
			tbl.put("ST", "String Data");
			tbl.put("TM", "Time");
			tbl.put("TS", "Time Stamp (Date & Time)");
			tbl.put("TX", "Text Data (Display)");
		}
        {
            HashMap<String, String> tbl = new HashMap<String, String>();
            ourTables.put("0127", tbl);
            tbl.put("AA", "Animal Allergy");
            tbl.put("DA", "Drug allergy");
            tbl.put("EA", "Environmental Allergy");
            tbl.put("FA", "Food allergy");
            tbl.put("LA", "Pollen Allergy");
            tbl.put("MA", "Miscellaneous allergy");
            tbl.put("MC", "Miscellaneous contraindication");
            tbl.put("PA", "Plant Allergy");
        }
        
        {
            HashMap<String, String> tbl = new HashMap<String, String>();
            ourTables.put("0128", tbl);
            tbl.put("MI", "Mild");
            tbl.put("MO", "Moderate");
            tbl.put("SV", "Severe");
            tbl.put("U", "Unknown");
        }
        
        
        {
            HashMap<String, String> tbl = new HashMap<String, String>();
            ourTables.put("0166", tbl);
            tbl.put("B", "Base");
            tbl.put("A", "Additive");
        }
        
       

		{
			HashMap<String, String> tbl = new HashMap<String, String>();
			ourTables.put("0190", tbl);
			tbl.put("H", "Home");
			tbl.put("C", "Current (If different from home)");
			tbl.put("B", "Business / Work");
			tbl.put("M", "Mailing");
			tbl.put("U", "Other / Unknown");
		}
        {
            HashMap<String, String> tbl = new HashMap<String, String>();
            ourTables.put("0191", tbl);
            tbl.put("SI", "Scanned Image");
            tbl.put("NS", "Non-scanned Image");
            tbl.put("SD", "Scanned Document");
            tbl.put("TEXT", "Machine readable text document");
            tbl.put("IM", "Image Data");
            tbl.put("AU", "Audio");
        }
		{
			HashMap<String, String> tbl = new HashMap<String, String>();
			ourTables.put("0200", tbl);
			tbl.put("L", "Legal Name");
			tbl.put("A", "Alias Name");
			tbl.put("B", "Name at Birth");
			tbl.put("M", "Maiden Name");
			tbl.put("U", "Unspecified");
		}
		{
			HashMap<String, String> tbl0201 = new HashMap<String, String>();
			ourTables.put("0201", tbl0201);
			tbl0201.put("PRN", "Primary Residence Number");
			tbl0201.put("HRN", "Home Residence Number");
			tbl0201.put("WPN", "Work Number");
			tbl0201.put("EMP", "Emergency Number");
			tbl0201.put("NET", "Email Address");
			tbl0201.put("OTH", "Other/Unknown Number");
		}
		{
			HashMap<String, String> tbl0202 = new HashMap<String, String>();
			ourTables.put("0202", tbl0202);
			tbl0202.put("PH", "Phone");
			tbl0202.put("FX", "Fax");
			tbl0202.put("CP", "Mobile");
			tbl0202.put("BP", "Pager/Beeper");
		}
		{
			HashMap<String, String> tbl0203 = new HashMap<String, String>();
			ourTables.put("0203", tbl0203);
			tbl0203.put("DL", "Drivers License");
			tbl0203.put("JHN", "Provincial Health Number");
			tbl0203.put("MR", "Medical Record Number");
			tbl0203.put("MRS", "Secondary or Ancillary Medical Record Number");
			tbl0203.put("MRT", "Temporary Medical Record Number");
			tbl0203.put("PI", "Patient or person identifier (non-MRN)");
			tbl0203.put("PPN", "Passport Number");
			tbl0203.put("AN", "Account Number");
			tbl0203.put("VN", "Visit Number");
		}
		{
			HashMap<String, String> tbl0270 = new HashMap<String, String>();
			ourTables.put("0270", tbl0270);
			tbl0270.put("TBD", "Need to provide these!");
		}
		{
			HashMap<String, String> tbl0271 = new HashMap<String, String>();
			ourTables.put("0271", tbl0271);
			tbl0271.put("F", "Final");
			tbl0271.put("C", "Corrected");
			tbl0271.put("W", "Withdrawn");
			tbl0271.put("I", "Incomplete / In Progress");
			tbl0271.put("P", "Preliminary");
			tbl0271.put("D", "Draft");
		}
		{
			HashMap<String, String> tbl0272 = new HashMap<String, String>();
			ourTables.put("0272", tbl0272);
			tbl0272.put("N", "Normal confidentiality rules");
			tbl0272.put("R", "Restricted access, e.g. only to providers having a current care relationship to the patient.");
			tbl0272.put("V", "Very restricted access as declared by the Privacy Officer of the record holder.");
			tbl0272.put("T", "Information not to be disclosed or discussed with patient except through physician assigned to patient in this case.");

		}
		
        {
            HashMap<String, String> tbl = new HashMap<String, String>();
            ourTables.put("0291", tbl);
            tbl.put("TIFF", "TIFF Image");
            tbl.put("JPEG", "JPEG Image");
            tbl.put("GIF", "GIF Image");
            tbl.put("PNG", "PNG Image");
            tbl.put("HTML", "HTML Document");
            tbl.put("RTF", "RTF Document");
            tbl.put("PDF", "PDF Document");
            tbl.put("WAV", "WAV Sound File");
            tbl.put("MP3", "MP3 Sound File");        

        }
		
        {
            HashMap<String, String> tbl = new HashMap<String, String>();
            ourTables.put("0323", tbl);
            tbl.put("A", "Add/Insert");
            tbl.put("D", "Delete");
            tbl.put("U", "Update");
            tbl.put("X", "No change");
        }
		
		{
			HashMap<String, String> tbl0432 = new HashMap<String, String>();
			ourTables.put("0432", tbl0432);
			tbl0432.put("1", "CTAS 1 - Resuscitation");
			tbl0432.put("2", "CTAS 2 - Emergent");
			tbl0432.put("3", "CTAS 3 - Urgent");
			tbl0432.put("4", "CTAS 4 - Less Urgent");
			tbl0432.put("5", "CTAS 5 - Non Urgent");
		}
        {
            HashMap<String, String> tbl0436 = new HashMap<String, String>();
            ourTables.put("0436", tbl0436);
            tbl0436.put("AD", "Adverse Reaction (Not otherwise classified)"); 
            tbl0436.put("AL", "Allergy"); 
            tbl0436.put("CT", "Contraindication");    
            tbl0436.put("IN", "Intolerance"); 

        }
		{
			HashMap<String, String> tbl = new HashMap<String, String>();
			ourTables.put("0443", tbl);
			tbl.put("PP", "Primary Care Provider");
		}
		
		
//		{
//			Map<String, String> idTypes = new HashMap<String, String>();
//			ourTables.put("9001", idTypes);
//			idTypes.put("2.16.840.1.113883.4.347", "Ontario Doctor License Number (CPSO)");
//			idTypes.put("2.16.840.1.113883.3.239.13.52", "Ontario Dentist License Number");
//			idTypes.put("2.16.840.1.113883.3.239.13.15", "Ontario Nurse Practitioner License Number");
//			idTypes.put("2.16.840.1.113883.3.239.13.12", "Ontario Midwife License Number");
//			idTypes.put("1.3.6.1.4.1.12201.1.2.1.5", "Locally Assigned Identifier at/by a specific HSP");
//
//		}
		{
			Map<String, String> tbl = new HashMap<String, String>();
			ourTables.put("9006", tbl);
			tbl.put("MR", "Medical Records");
			tbl.put("DI", "Diagnostic Imaging");
		}
		{
			HashMap<String, String> tbl0363 = new HashMap<String, String>();
			ourTables.put("0363", tbl0363);
			tbl0363.put("CAN", "Canada");
			tbl0363.put("CANAB", "Alberta");
			tbl0363.put("CANBC", "British Columbia");
			tbl0363.put("CANMB", "Manitoba");
			tbl0363.put("CANNB", "New Brunswick");
			tbl0363.put("CANNF", "Newfoundland");
			tbl0363.put("CANNS", "Nova Scotia");
			tbl0363.put("CANNT", "Northwest Territories");
			tbl0363.put("CANNU", "Nunavut");
			tbl0363.put("CANON", "Ontario");
			tbl0363.put("CANPE", "Prince Edward Island");
			tbl0363.put("CANQC", "Quebec");
			tbl0363.put("CANSK", "Saskatchewan");
			tbl0363.put("CANYT", "Yukon Territories");
			tbl0363.put("US", "United States");
			tbl0363.put("USAL", "Alabama");
			tbl0363.put("USAK", "Alaska");
			tbl0363.put("USAZ", "Arizona");
			tbl0363.put("USAR", "Arkansas");
			tbl0363.put("USCA", "California");
			tbl0363.put("USCO", "Colorado");
			tbl0363.put("USCT", "Connecticut");
			tbl0363.put("USDE", "Delaware");
			tbl0363.put("USFL", "Florida");
			tbl0363.put("USGA", "Georgia");
			tbl0363.put("USHI", "Hawaii");
			tbl0363.put("USID", "Idaho");
			tbl0363.put("USIL", "Illinois");
			tbl0363.put("USIN", "Indiana");
			tbl0363.put("USIA", "Iowa");
			tbl0363.put("USKS", "Kansas");
			tbl0363.put("USKY", "Kentucky");
			tbl0363.put("USLA", "Louisiana");
			tbl0363.put("USME", "Maine");
			tbl0363.put("USMD", "Maryland");
			tbl0363.put("USMA", "Massachusetts");
			tbl0363.put("USMI", "Michigan");
			tbl0363.put("USMN", "Minnesota");
			tbl0363.put("USMS", "Mississippi");
			tbl0363.put("USMO", "Missouri");
			tbl0363.put("USMT", "Montana");
			tbl0363.put("USNE", "Nebraska");
			tbl0363.put("USNV", "Nevada");
			tbl0363.put("USNH", "New Hampshire");
			tbl0363.put("USNJ", "New Jersey");
			tbl0363.put("USNM", "New Mexico");
			tbl0363.put("USNY", "New York");
			tbl0363.put("USNC", "North Carolina");
			tbl0363.put("USND", "North Dakota");
			tbl0363.put("USOH", "Ohio");
			tbl0363.put("USOK", "Oklahoma");
			tbl0363.put("USOR", "Oregon");
			tbl0363.put("USPA", "Pennsylvania");
			tbl0363.put("USRI", "Rhode Island");
			tbl0363.put("USSC", "South Carolina");
			tbl0363.put("USSD", "South Dakota");
			tbl0363.put("USTN", "Tennessee");
			tbl0363.put("USTX", "Texas");
			tbl0363.put("USUT", "Utah");
			tbl0363.put("USVT", "Vermont");
			tbl0363.put("USVA", "Virginia");
			tbl0363.put("USWA", "Washington");
			tbl0363.put("USWV", "West Virginia");
			tbl0363.put("USWI", "Wisconsin");
			tbl0363.put("USWY", "Wyoming");
			tbl0363.put("USDC", "Washington DC");
			tbl0363.put("OTHER", "Other (Text description must be provided in CX.2)");
		}
		{
			HashMap<String, String> tbl9003 = new HashMap<String, String>();
			ourTables.put("9003", tbl9003);
			tbl9003.put("CANAB", "Alberta");
			tbl9003.put("CANBC", "British Columbia");
			tbl9003.put("CANMB", "Manitoba");
			tbl9003.put("CANNB", "New Brunswick");
			tbl9003.put("CANNF", "Newfoundland and Labrador");
			tbl9003.put("CANNS", "Nova Scotia");
			tbl9003.put("CANNT", "Northwest Territories");
			tbl9003.put("CANNU", "Nunavut");
			tbl9003.put("CANON", "Ontario");
			tbl9003.put("CANPE", "Prince Edward Island");
			tbl9003.put("CANQC", "Quebec");
			tbl9003.put("CANSK", "Saskatchewan");
			tbl9003.put("CANYT", "Yukon Territories");
			tbl9003.put("USAL", "Alabama");
			tbl9003.put("USAK", "Alaska");
			tbl9003.put("USAZ", "Arizona");
			tbl9003.put("USAR", "Arkansas");
			tbl9003.put("USCA", "California");
			tbl9003.put("USCO", "Colorado");
			tbl9003.put("USCT", "Connecticut");
			tbl9003.put("USDE", "Delaware");
			tbl9003.put("USFL", "Florida");
			tbl9003.put("USGA", "Georgia");
			tbl9003.put("USHI", "Hawaii");
			tbl9003.put("USID", "Idaho");
			tbl9003.put("USIL", "Illinois");
			tbl9003.put("USIN", "Indiana");
			tbl9003.put("USIA", "Iowa");
			tbl9003.put("USKS", "Kansas");
			tbl9003.put("USKY", "Kentucky");
			tbl9003.put("USLA", "Louisiana");
			tbl9003.put("USME", "Maine");
			tbl9003.put("USMD", "Maryland");
			tbl9003.put("USMA", "Massachusetts");
			tbl9003.put("USMI", "Michigan");
			tbl9003.put("USMN", "Minnesota");
			tbl9003.put("USMS", "Mississippi");
			tbl9003.put("USMO", "Missouri");
			tbl9003.put("USMT", "Montana");
			tbl9003.put("USNE", "Nebraska");
			tbl9003.put("USNV", "Nevada");
			tbl9003.put("USNH", "New Hampshire");
			tbl9003.put("USNJ", "New Jersey");
			tbl9003.put("USNM", "New Mexico");
			tbl9003.put("USNY", "New York");
			tbl9003.put("USNC", "North Carolina");
			tbl9003.put("USND", "North Dakota");
			tbl9003.put("USOH", "Ohio");
			tbl9003.put("USOK", "Oklahoma");
			tbl9003.put("USOR", "Oregon");
			tbl9003.put("USPA", "Pennsylvania");
			tbl9003.put("USRI", "Rhode Island");
			tbl9003.put("USSC", "South Carolina");
			tbl9003.put("USSD", "South Dakota");
			tbl9003.put("USTN", "Tennessee");
			tbl9003.put("USTX", "Texas");
			tbl9003.put("USUT", "Utah");
			tbl9003.put("USVT", "Vermont");
			tbl9003.put("USVA", "Virginia");
			tbl9003.put("USWA", "Washington");
			tbl9003.put("USWV", "West Virginia");
			tbl9003.put("USWI", "Wisconsin");
			tbl9003.put("USWY", "Wyoming");
			tbl9003.put("USDC", "Washington DC");
		}
        {
            HashMap<String, String> tbl = new HashMap<String, String>();
            ourTables.put("9009", tbl);
            tbl.put("S", "Snapshot Mode (Contents will replace all existing contents within the current ORDER_OBSERVATION group). This is the default behaviour.");
            tbl.put("A", "Append Mode (Contents will be added at the end of any existing contents)");
        }

	}

	public static String getHl7TableAsHtml(String theTableNumber) {
		Map<String, String> table = ourTables.get(theTableNumber);
		if (table == null) {
			return "<b>ERROR: Unknown table " + theTableNumber + "</b>";
		}
		
		ArrayList<String> keys = new ArrayList<String>(table.keySet());
		Collections.sort(keys);
		
		StringBuilder b = new StringBuilder();
		b.append("<table class=\"errorCodesCodeTable\">");
		
		b.append("<tr><td>Code</td><td>Description</td></tr>");
		
		for (String key : keys) {
	        b.append("<tr><td>").append(key).append("</td><td>").append(table.get(key)).append("</td></tr>");
        }
		
		b.append("</table>");
		
		return b.toString();
	}
	

	public static String lookupHl7Code(String theTableNumber, String theCode) {
		if (theCode == null) {
			return null;
		}
		if (ourTables.containsKey(theTableNumber) == false) {
			throw new IllegalArgumentException("Unknown table: " + theTableNumber);
		}
		return ourTables.get(theTableNumber).get(theCode);
	}

}
