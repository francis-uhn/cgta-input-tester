package ca.cgta.input.converter;

import static ca.cgta.input.converter.FailureCode.Severity.*;
import ca.cgta.input.model.inner.Tables;

public enum FailureCode {
	
	F001("Set ID must be '1'", 
			"To resolve this issue, simply hardcode the value \"1\" in this field.", LOW), // -
	
	F002("Invalid patient class code in PV1-2. Values must be drawn from table 0004",
			"PV1-2 must contain a valid value from Table 0004. It may be neccesary to map locally " +
			"defined values to the values that ConnectingGTA expects. Valid values are: " + Tables.getHl7TableAsHtml("0004"), MED), // -
			
	F003("Missing universal service identifier text in OBR-4-2",
			"This field identifies an individual document or result type (e.g. \"Chest XRay\", or \"Discharge Summary\"). " +
			"The component in OBR-4-2 specifically should have a textual description or title for the document or result.", HIGH), // -
	
	F004("Missing observation date/time in OBR-7", "This field contains the most clinically relevant time for " +
			"the document or result. See the ConnectingGTA Input Specification for details about what time is " +
			"most appropriate for specific types of results.", MED), // -
	
	F005("Invalid result confidentiality code. Values must be drawn from table 0272",
			"OBR-18 must contain a valid value from Table 0272. It may be neccesary to map locally " +
			"defined values to the values that ConnectingGTA expects. Valid values are: " + Tables.getHl7TableAsHtml("0272"), MED), //-
	
	F006("Invalid HRM category. Values must be drawn from table 9006"), // -
	
	F007("Invalid result status code in OBR-25. Values must be drawn from table 0085",
			"OBR-25 must contain a valid value from Table 0085. It may be neccesary to map locally " +
			"defined values to the values that ConnectingGTA expects. Valid values are: " + Tables.getHl7TableAsHtml("0085"), MED), //-
	
	F008("OBX Set ID must be numeric and must reflect the sequence of OBX",
			"In the first OBX segment plase a \"1\", in the second place a \"2\", etc.", LOW), // -
	
	F009("Missing OBX observation identifier code or text in OBX-3-1 or OBX-3-2",
			"Every OBX segment must have a value within OBX-3-1 and OBX-3-2 which indicates " +
			"what the content in OBX-5 is. For example if the document or result contains multiple " +
			"discrete fields (each in a new OBX segment) then OBX-3 should identify which discrete " +
			"field is being represented in this OBX repetition. If a series of OBX segments contain " +
			"the complete text of a report, than each OBX-3-1 value should be the same. See the " +
			"ConnectingGTA Input Specification (HL7 Version 2.5) for more information.", MED), // -
	
	
	F010("Missing or invalid OBX observation identifier coding system. Values must be drawn from table 9007",
			"Your HSP will be assigned a value by ConnectingGTA which must be used in this position. " +
			"This value is unique to your site and sending system. If your site has multiple sending " +
			"systems, you may have specific allowable values for this field.", HIGH), // -
			
	F011("Missing or incomplete OBR universal service identifier (OBR-4).", "OBR-4 must contain both a code (OBR-4-1) and a " +
			"text description (OBR-4-2) which identify the type of document or result contained within this " +
			"ORDER_OBSERVATION group.", HIGH), // -
	
	F012("Missing or invalid OBR universal service identifier coding system (OBR-4-3). Values must be drawn from table 9007", "Your HSP will be " +
			"assigned a code system ID which should be hardcoded in this position. This code system ID identifies the \"source\" of the codes " +
			"in OBR-4-1.", HIGH), // -
	
	F013("Missing or invalid Observation Type in OBR-2. Valid data types must be drawn from Table 0125", "OBR-2 indicates the datatype which " +
			"is placed in OBX-5 (e.g. if OBX-2 is \"ST\", then OBX-5 is a String data type). Valid values are: " + Tables.getHl7TableAsHtml("0125"), MED), // -
	
	F014("Invalid OBX abnormal flag in OBX-8. Values must be drawn from table 0078", "OBX-8 must contain a value which is drawn from " +
			"Table 0078. If other values are used in the sending system, than these must be mapped in the interface engine to " +
			"equivalent values from Table 0078. Value values are: " + Tables.getHl7TableAsHtml("0078"), LOW), // -
	
	F015("Invalid OBX observation status in OBX-11. Values must be drawn from table 0085", "OBX-11 must contain a value which is drawn from " +
			"Table 0085. If other values are used in the sending system, than these must be mapped in the interface engine to " +
			"equivalent values from Table 0085. Value values are: " + Tables.getHl7TableAsHtml("0085"), LOW), // -
	
//	F016("When successive OBX segments have the same observation code (OBX-3), they must have a sequentially incremented sub-id (OBX-4)"), // -
	
	F017("Timestamp (TS) values must be of the format YYYYMMDDHHMMSS[.SSSS]-ZZZZ", "Values in this field must follow the " +
			"standard HL7 format and must include precision up to the second. In the event that this level of precision is " +
			"not captured in the sending system, it may make sense to pad the time with zeros.", LOW), // -
	
	F018("OBX-5 may not have more than one repetition for non-textual data types", "There are no currently known use " +
			"cases for transmitting multiple elements of non-textual data (e.g. numbers, dates) in a single " +
			"observation segment within an HL7 message. If your message contains a new use case which requires this, " +
			"please discuss with your site coordinator.", MED), // -
	
	F019("This field must contain a numeric integer, but text was found or it was blank", "This field must contain a whole number. In other words," +
			"it must only contain characters 0-9.", MED), // -
	
	F020("Missing Entity Identifier ID (EI-1)", "EI-1 is the actual ID or number of the document/result/order/etc. An appropriate value " +
			"must be placed in this location. Note that this number must be unique and must never be reused (other than to update an " +
			"existing document/result/order/etc).", MED), // -
	
	F021("Missing/Invalid Entity Identifier HSP ID (EI-2 / Namespace ID). Values must be drawn from Table 9004", "Your HSP will be assigned a specific " +
			"value within Table 9004 which uniquely identifies it. This value must be hardcoded in EI-2.", MED), // -
	
	F022("Invalid Entity Identifier System ID (EI-3 / Universal ID). Values must be drawn from Table 9008", "Your HSP will be assigned a specific " +
			"value within Table 9008 which uniquely identifies each sending system which is generating identifiers. This value must be hardcoded in EI-3. " +
			"For example: If EI-1 contains the ID 12345, EI-2 must contain the ID of the HSP which assigned that number, and EI-3 must contain the " +
			"ID of the system at that HSP which assigned that number", MED), // -
	
	F023("Missing provider ID number in NDL-1-1", "NDL-1-1 must contain the actual identifier for a given provider. This may be either a local " +
			"HSP assigned identifier or a provincial license number. See the ConnectingGTA Input Specification for more details.", MED), // -
	
	F024("Missing or invalid assigning authority ID type. Values must be drawn from table 9001", "This component identifies what type of " +
			"ID is being transmitted in this field. For example, 'MR' indicates that this repetition of this field is transmitting a " +
			"Medical Record Number. See the ConnectingGTA Input Specification for more information. Value values for this field are: " + 
			Tables.getHl7TableAsHtml("9001"), HIGH), // -
	
	F025("Invalid assigning authority HSP universal ID. Values must be drawn from table 9004 when XCN-9-2 is '1.3.6.1.4.1.12201.1.2.1.5'",
			"When transmitting a provider identifier which is a locally (HSP) assigned identifier, XCN-9-2 must be set " +
			"to '1.3.6.1.4.1.12201.1.2.1.5', and this component must be populated with an identifier from Table 9004 which " +
			"identifies the HSP which assigned this ID. " + Constants.SEE_HERE_HTML +
			"for a list of allowable values in Table 9004", HIGH), // -
	
	F026("Invalid assigning authority HSP system ID.",
			"When transmitting a provider identifier which is a locally (HSP) assigned identifier, XCN-9-2 must be set " +
			"to '1.3.6.1.4.1.12201.1.2.1.5', and this component must be populated with an identifier from Table 9008 which " +
			"identifies the system which assigned this ID. See <a href=\"http://conftest.connectinggta.ca/cGTA_Conformance_Validator/#OID\">here</a> " +
			"for a list of allowable values in Table 9008", HIGH), // -
	
	F027("Must not provide a universal ID for provincial provider identifier", "This component must be empty if this field is transmitting a " +
			"provincial provider identifier", LOW), // -
	
	F028("Must not provide a universal ID type for provincial provider identifier", "This component must be empty if this field is transmitting a " +
			"provincial provider identifier", LOW), // -
			
	F029("Invalid facility ID (PL.4.1). Values must be drawn from table 9004", "Locations in PV1-3 and PV1-6 must include an OID which identifies the " +
			"facility in which the Visit or Encounter Location is located. " + Constants.SEE_HERE_HTML + " for a list of valid values in " +
			"Table 9004", MED), // -
			
//	F030("PID Set ID (PID-1) must be '1'"), // -
	
	F031("PID-6 (mother's maiden name) may have at most only one repetition", LOW), // -
	
	F032("PID-15-3 must contain the exact text 'HL70296'", LOW), // -
	
	F033("PID-30 must be blank, Y, or N", "This field must contain one of the following values: (empty), \"Y\", or \"N\". It may " +
			"be neccesary to map local values to one of these values if different flags are used in the sending " +
			"system", MED), // -
	
	F034("Name type code is invalid. Values must be drawn from table 0200", "Name type code indicates the type of " +
			"name contained within this field. The most common value for this component is \"L\". Valid values are: " +
			Tables.getHl7TableAsHtml("0200"), LOW), // -
	
	F035("Invalid province code. Values must be drawn from table 9003", "ConnectingGTA requires a specific format for province codes. It may be " +
			"neccesary to create interface engine mappings to locally defined codes. Valid values are: " + Tables.getHl7TableAsHtml("9003"), LOW), // -
	
	F036("Province (XAD.4) of Ontario must be represented as CANON", "ConnectingGTA has detected a code which appears to represent Ontario, but " +
			"the valid code in Table 9004 for Ontario is \"CANON\". It may be " +
			"neccesary to create interface engine mappings to locally defined codes. Valid values are: " + Tables.getHl7TableAsHtml("9003"), LOW), // -
	
	F037("Invalid address type. Values must be drawn from Table 0190", "XAD.7 identifies the type of address being identified. If this is not tracked in the " +
			"sending system, it may be appropriate to hard code this to a specific value. The most common value for this field is 'H'. Valid values are: "
			+ Tables.getHl7TableAsHtml("0190"), LOW), // -
	
	F038("Invalid telecommunications use code. Values must be drawn from table 0201", "XTN.2 identifies the primary use for this " +
			"phone number or email address. If this is not captured in the sending system, it may be appropriate to hard code this to " +
			"an appropriate value. The most common value for this component is 'PRN'. Valid values are: " + Tables.getHl7TableAsHtml("0201"), LOW), // -
	
	F039("Email address must only be provided in the case of equipment type 'NET'", "If email address is being captured, it should be transmitted in " +
			"a separate repetition of this field with XTN.2 set to 'NET' and XTN.3 set to 'Internet'. In this case, XTN.1 must be blank.", LOW), // -
			
	F040("Phone number must not be provided in the case of equipment type 'NET'", "If email address is being captured, it should be transmitted in " +
			"a separate repetition of this field with XTN.2 set to 'NET' and XTN.3 set to 'Internet'. In this case, XTN.1 must be blank.", LOW), // -
			
	F041("Visit/Encounter Identifier type must not be used in PID", "Repetitions of this field must not be included with CX.5 values which identify " +
			"visit or encounter numbers", LOW), // -
	
	F042("Visit/Encounter Identifier type must be used in PV1", "Repetitions of this field must not be included with CX.5 values which identify " +
			"person or patient numbers", LOW), // -
			
	F043("Missing or invalid ID Type Code. Values must be drawn from table 0203", "CX.5 indivates the type of ID which is being transmitted " +
			"in this field. For a patient identifier (i.e. within PID-3) this would often be 'MR' for a medical record number, or 'JHN' for " +
			"a provincial health number. For a visit/encounter number (i.e. within PV1-19) this would often be 'VN' for visit number. Valid " +
			"values are " + Tables.getHl7TableAsHtml("0203"), MED), // -
	
	F044("Invalid CX assigning jurisdiction. Values must be drawn from Table 0363", "ConnectingGTA requires specific values which identify " +
			"the assigning jurisdiction for a jurisdictional health number or passport number. For example, for an OHIP number, the " +
			"assigning jurisdiction would be 'CANON'. If other identifiers are used, it may be neccesary to build a mapping table in " +
			"your interface engine. If you are capturing OHIP numbers in a specific field but not other types of provincial " +
			"identifiers, it may be appropriate to simply hardcode 'CANON'. If a coded value is not available for this field, it is " +
			"also acceptable to use the code 'OTHER' and place the non-coded text in CX.9.2. " +
			"Valid values are: " + Tables.getHl7TableAsHtml("0363"), MED), // -
	
	F045("No text description provided for an assigning jurisdiction of 'OTHER'", "When CX.9.1 (assigning jurisdiction) is populated with " +
			"the string 'OTHER', CX.9.2 must contain a textual description (which may be free text) of the assigning jurisdiction. ", MED), // -
	
	F046("CX.9.3 must be 'HL70363' if CX.9 is populated", 
			"Hardcode CX.9.3 with the string 'HL70363' in the event that CX.9 has content.", LOW), // -
	
//	F047("CX-4-1 must be populated if CX-4-2 is populated", "Consult the ConnectingGTA Input Specification (HL7 v2.5) for information on " +
//			"the correct format for the CX datatype in this position.", MED), // -
	
	F048("IDs must have EITHER an assigning authority or jurisdiction (CX.4 / CX.9), not both. " +
			"If this identifier is a provincial/national number such as a health card number, only CX.9 (assigning jurisdiction) should be populated. " +
			"If this identifier is a site-assigned number such as an MRN or Visit Number, only CX.4 (assigning authority) should be populated.", 
			"Consult the ConnectingGTA Input Specification (HL7 v2.5) for information on " +
			"the correct format for the CX datatype in this position.", MED), // -
			
//	F049("Missing CX.5 ID type code", ""), // -
	
	F050("IDs of type JHN and PPN must have a jurisdiction ID (CX.9)", "The JHN (Jurisdictional Health Number / Provincial Health Card Number) and " +
			"PPN (Passport Number) ID types must report the assigning jurisdiction which assigned the number. For example, if CX.1 represents " +
			"an OHIP number, CX.9.1 should contain 'CANON'. Values must be drawn from Table 0363. Valid values are: " + 
			Tables.getHl7TableAsHtml("0203"), MED), // -
	
	F051("IDs of this type must have an assigning authority ID (CX.4)", 
			"For HSP-specific identifiers (such as MRNs and Visit Numbers), CX.4.1 must identify the HSP which assigned the ID, and " +
			"CX.4.2 must identify the system.", MED), // -
	
//	F052("XTN should not have both a telephone number and an email address"), // -
//	F053("Email address should be placed in XTN-4"), // -
	
//	F054("NTE Set ID (NTE.1) must be a sequential number indicating the repetition number. For the first NTE, this would be '1'."), // -
	
//	F055("Visit must have a visit/account number"), // -
	
//	F056("Missing or invalid Trigger Event code"), // -
	
//	F057("MRG-1 may not have more than one repetition", "ADT merge messages may not be used to merge more than one source patient into the " +
//			"destination patient, therefore MRG-1 must contain a single identifier which identifies the source", MED), // -
	
//	F058("Invalid admission level of care code. Must be a CTAS value 1-5.", 
//			"Valid values for this field are: " + Tables.getHl7TableAsHtml("0432"), LOW), // -
	
	F059("Unlink Patient (ADT^A37) must contain an identifier of type MR in the second PID segment", "The ADT^A37 message " +
			"is used to unlink (unmerge) two patients. It must contain a second PID segment which identifies the " +
			"second patient to unlink", HIGH), // -
	
	F060("ADT messages must have an organization specific MRN (PID-3 with ID type code 'MR')", "All ADT messages must contain " +
			"a locally defined identifier (in addition to any provincial identifiers such as OHIP numbers) which identify " +
			"the patient/subject of the message", HIGH), // -
	
	F061("PID segment contains more than one primary MRN (PID-3 with ID type code 'MR')", "Each individual patient " +
			"record must have exactly one primary HSP-specific identifier, which is identified with the type code 'MR'. If " +
			"multiple IDs are assigned at a site, one must be designated as primary. Any additional identifiers may use " +
			"the type code of 'MRS' (secondary ID).", HIGH), // -
	
	F062("ADT^A06 and ADT^A07 messages must have a value for MRG-5 indicating the previous visit number which is being subsumed", MED), // -
	
	F063("ADT^A40 and ADT^A45 messages must have a merge-MRN (MRG-1 with ID type code of 'MR')", MED), // -

	F064("MRG-1 may not have more than one repetition", "ADT merge messages may not be used to merge more than one source patient into the " +
			"destination patient, therefore MRG-1 must contain a single identifier which identifies the source", MED), // -
	
	F065("ADT^A45 messages must have a visit number to move (MRG-5)", MED), // -
	
	F066("This field has a required fixed value which was not provided. See the ConnectingGTA Input Specification for more details", "" +
			"Consult the ConnectingGTA Input Specification for the required value in this field, and hard code it in your interface.", LOW), // -
	
	F067("The value at this position exceeds the maximum allowed length. See the ConnectingGTA Input Specification for more details", MED), // -,
	
//	F068("Not authorized: MSH-3-1 must contain an identifier which identifies the sending organization. This identifier is provided by ConnectingGTA"), // -
	
	F069("Message does not have a control ID", HIGH), // -
	
	F070("Message Processing Mode must be 'T' for non production use", LOW), // -
	
	F071("Maximum allowable cardinality exceeded for this position. See the ConnectingGTA Input Specification for more details", MED), // -
	
	F072("Invalid Administrative Sex. Values must be drawn from Table 0001", "Valid values are: " + Tables.getHl7TableAsHtml("0001"), HIGH), // -,
	
	F073("XPN.1.1 (Client/Patient Last Name) must be populated", HIGH), // -
	
	F074("XTN.5, XTN.6, XTN.7 and XTN.8 must be numeric with no letters or other characters", "Phone number components do not need to include spacer characters. For example, if the full phone number in XTN.1 is \"(416) 555-1234\", XTN.7 should contain \"5551234\".", LOW), // -
	
	// F075("PV1-10 (Hospital Service) must be drawn from Table 0069"), //-
	
	F076("PV1-16 (VIP Flag) must be blank, 'Y' or 'N'", "Consult your site coordinator for more information on privacy flags in ConnectingGTA", HIGH), // -
	
	F077("PV2-40-1 (Admission Level of Care Code for Emergency Visit) must be drawn from Table 0432", "Values must be " +
			"a value drawn from the Canadian Triage Acuity Score. Valid values for this field are: " + 
			Tables.getHl7TableAsHtml("0432"), LOW), // -
	
//	F078("DG1-3 (Diagnosis) must be populated if the DG1 segment is present"), // -
			
	F079("DG1-3-3 (Diagnosis Name of Code System) must be drawn from Table 0053 if (and only if) diagnosis codes are " +
			"being drawn from a standardized vocabulary such as SNOMED CT. CE.3 must be blank.", "If a non-standard set of " +
			"diagnosis codes are used, or if freetext diganosis codes are being used CE.2 must be populated but CE.1 and CE.3 must be blank. " +
			"Valid values are: " + Tables.getHl7TableAsHtml("0053"), LOW), // -
	
	F080("DG1-3-3 (Diagnosis Name of Code System) must be populated if DG1-3-1 (Diagnosis Code) is populated", "If a non-standard set of " +
			"diagnosis codes are used, or if freetext diganosis codes are being used CE.2 must be populated but CE.1 and CE.3 must be blank. ", LOW), // -
	
	F081("ROL-3-1 (Role Code) is missing or invalid. Values must be drawn from Table 0443", "Valid values are: " + Tables.getHl7TableAsHtml("0443"), MED), // -
	
	
    F082("DG1-3-1 (Diagnosis Code) must be populated if DG1-3-3 (Diagnosis Name of Coding System) is populated", "If a non-standard set of " +
            "diagnosis codes are used, or if freetext diganosis codes are being used CE.2 must be populated but CE.1 and CE.3 must be blank. ", LOW), // -
	
//	F082("ROL (Role) segment is missing mandatory ROL-4 (Role Person Name)"), // -
	
	F083("Either Associated Party Relationship text or code must be supplied. If a code is supplied then the value must be drawn from Table 0063 and its description" +
			" will overwrite any text value that has been supplied", "Valid values are: " + Tables.getHl7TableAsHtml("0063"), MED), // -
	
	F084("Date (DT) values must be of the format YYYYMMDD", "If sending system captures more precision than year-month-day, it is " +
			"acceptable to trim the value in this field to only the first 8 characters.", MED), // -	
			
	F085("Swap Patient (ADT^A17) must contain an identifier of type MR in the second PID segment", HIGH), // -
	
	F086("Swap Patient (ADT^A17) must contain a visit number in the second PV1 segment", HIGH), // -
	
	F087("Swap Patient (ADT^A17) must contain a location in the second PV1 segment", HIGH), // -
	
	F088("IAM-2 Allergen Type Code is missing or invalid. Values must be drawn from Table 0127", 
			"This field indicates the type of allergy being transmitted. If the sending system uses different codes, " +
			"it may be neccesary to perform a mapping in the interface engine for this component. If the sending system only captures drug " +
			"allergies, it may be appropriate to hard code this component to 'DA' (Drug Allergy). If the sending system does not capture " +
			"the allergy type, it may be appropriate to hard code this component to 'MA' (Miscellaneous Allergy). " +
			"Valid values for this field are: " + Tables.getHl7TableAsHtml("0127"), LOW), // -
	
	F089("IAM-4 Allergy Severity Code is missing or invalid. Values must be drawn from Table 0128", "This field indicates " +
			"the severity of the allergy. If the sending system uses different codes, it may be neccesary to " +
			"perform a mapping in the interface engine for this component. If the sending system does not capture " +
			"allergy severity, it may be appropriate to hard code this component to 'U' (Unknown). Valid values for " +
			"this field are: " + Tables.getHl7TableAsHtml("0128"), LOW), // -
	
//	F090("IAM-9 Sensitivity to Allergen Code is missing or invalid. Values must be drawn from Table 0436"), // -
	
	F091("ADT^A06 and A07 messages must have a preconversion visit number (MRG-5)", 
			"If a visit is being converted from one visit type to another, MRG-5 must be populated with " +
			"the preconversion visit number", MED), // -
	
	F092("Missing or invalid message trigger code (MSH-9-2)", "The ConnectingGTA Input Specification defines a specific set of " +
			"ADT transactions which are supported. The trigger code in this message is not currently supported. There are two " +
			"possible resolutions to this problem: If the trigger represents an event which is not a workflow step which is " +
			"tracked by ConnectingGTA (i.e. \"Leave of Absence\" triggers) this message may be filtered from the ConnectingGTA " +
			"data feed. If the trigger represents an event which is covered by another trigger (e.g. a physician assignment modification), " +
			"it may be appropriate to simply remap the trigger to an appropriate message type (e.g. ADT^A08) and transmit it. " +
			"Valid ADT triggers are: " + Tables.getHl7TableAsHtml("0003ADT"), MED), // -
	
	F093("Medication orders must have an identifier in ORC-2 (Filler Order Number)", "All medication events must contain a unique number " +
			"which identifies the order number associated with the event (either an order itself, or an administration).", HIGH), //- 
	
	F094("Missing or invalid value for ORC-1 (Order Control) for a medication order", HIGH), //-
	
	F095("RXE segment is missing medication repeat pattern (RXE-1 TQ.2 RI.1)", LOW), //- 
	
	F096("RXE segment is missing medication repeat duration (RXE-1 TQ.3)", LOW), //- 
	
	F097("Missing or invalid medication Give Code (RXE-2). See the ConnectingGTA Input Specification for more details", HIGH), //-
	
	F098("Invalid message event type or trigger code (MSH-9)", "The ConnectingGTA Input Specification lists the specific message types " +
			"which may be transmitted to ConnectingGTA. The message provided does not match one of these message types. Please see " +
			"the ConnectingGTA Input Specification for more detail.", HIGH), //-
	
//	F099("Missing RX administration number (RXA-2)"), //-
	
	F100("Missing or invalid RX administration start time (RXA-3)", HIGH), //-
	
	F101("TS (Timestamp) value is not in correct format. This field must contain at precision at least up to the minute level. " +
			"Acceptable format: YYYYMMDDHHMM[SS[.m[m[m[m]]]][+/-ZZZZ]", MED), //-
	
	F102("TS (Timestamp) value is not in correct format for this particular field. " +
			"Acceptable format: YYYY[MM[DD[HH[MM[SS[.m[m[m[m]]]]]]]]][+/-ZZZZ]", MED), //-
			
    F103("IAM-3.2 Allergen Description is missing", HIGH), // -
    
    F104("EncapsulatedData Type id is missing or invalid. Values must be drawn from Table 0191", 
    		"Valid values for this component are: " + Tables.getHl7TableAsHtml("0191"), MED), //-
    
    F105("EncapsulatedData SubType id is missing or invalid. Values must be drawn from Table 0291",
    		"Valid values for this component are: " + Tables.getHl7TableAsHtml("0291"), MED), //-
    
    F106("EncapsulatedData encoding must be set to Base64", 
    		"This component must be hard coded to the text \"Base64\". This also means that the contents of " +
    		"the encapsulated data must be encoded using Base 64 encoding. See the ConnectingGTA Input " +
    		"Specification for more information.", MED), //-
    
    F107("EncapsulatedData observation value type specified but no observation value has been provided", MED), //-
    
	F112("Not authorized: MSH-3-1 must contain an identifier which identifies the sending HSP. This identifier must be drawn from Table 9004, and is provided by ConnectingGTA", 
			Constants.SEE_HERE_HTML + "for a list of acceptable values in Table 9004",
			HIGH), // -
	
	F113("Not authorized: MSH-3-2 must contain an identifier which identifies the sending application. This identifier must be drawn from Table 9008 and is provided by ConnectingGTA", 
			Constants.SEE_HERE_HTML + "for a list of acceptable values in Table 9008",
			HIGH), // -
	
	F114("Not authorized: MSH-8 must contain a security token which is provided by ConnectingGTA for the sending HSP", 
			"Contact your site coordinator to request your HSP-specific security token",
			HIGH), // -
	
	F115("MSH-21 must contain an identifier which identifies the version of the specification being transmitted. " +
			"See the ConnectingGTA Input Specification for more details",
			"As of the current release of the ConnectingGTA Input Specification, the only valid value for this " +
			"field is \"" + Converter.INPUT_PROFILE_2_0 + "\"",
			HIGH), // -
	
	F116("CX-4-1 must be set to the identifier assigned to your HSP in table 9004.", 
			"If an identifier such as an MRN or Visit Number is being transmitted, CX-4-1 must identify the " +
			"HSP which assigned that identifier. Correct values for this component are assigned by ConnectingGTA." + 
			Constants.SEE_HERE_HTML + "for a list of acceptable values in Table 9004", HIGH), //-
			
	F117("CX-4-2 must be set to the identifier assigned to your sending system in table 9008.", 
			"If an identifier such as an MRN or Visit Number is being transmitted, CX-4-1 must identify the " +
			"system which assigned that identifier. Correct values for this component are assigned by ConnectingGTA." +
			Constants.SEE_HERE_HTML + "for a list of acceptable values in Table 9008", HIGH), //-
					
	F118("Medication route (RXR-1) must include a textual description of the route in CE.2", MED), //-
	
	F119("Medication Component Type (RXC-1) is missing or invalid. Values must be drawn from Table 0166", 
			"Valid values in this field are: " + Tables.getHl7TableAsHtml("0166"), MED), //-
	
	F120("Missing Code (CE.1) and/or Textual Description (CE.2) for Component Code (RXC-2)", HIGH), //-
	
//	F121("code (CE-1) and code system (CE-3) must be supplied"), //-
	
	F122("Code (CE-1), Code text (CE-2), and Code System (CE-3) must be supplied for Component Code (RXC-2)", HIGH), //-
	
	F123 ("Missing or invalid coding system identifier. Values must be drawn from table 9007", "Your HSP will be assigned a value by ConnectingGTA which must be used in this position. " +
          "This value is unique to your site and sending system. If your site has multiple sending systems, you may have specific allowable values for this field.", HIGH),
          
    F124("Quantity value (TQ-1-1) must be supplied for Encoded Order Quantity", HIGH), //-
	
	F125("Missing Code (CE.1) and/or Textual Description (CE.2) for Encoded Order Give Units (RXE-5)", HIGH), //-
	
	F126("Missing Code (CE.1) and/or Textual Description (CE.2) for Encoded Order Give Dosage Form (RXE-6)", HIGH), //-
	
	F127("Append mode value is invalid. This field must either be blank or contain a value drawn from Table 9009.", "Append mode may be used if " +
			"the sending system transmits updates to clinical documents and results by transmitting only new text which is to be " +
			"appended to the existing contents. See the ConnectingGTA Input Specification (HL7 Version 2.5) for more informaion. Valid " +
			"values in this field are: " + Tables.getHl7TableAsHtml("9009"), MED), //-

	F128("No visit/encounter number found in message (PV1-19) and it is required for this type of message", "PV1-19 must contain a number which uniquely " +
			"identifies the visit or encounter in this message", HIGH), //-
	
	F129("Trigger A01 should only be used to activate an inpatient visit (PV1-2 = I/P/U)", "Ambulatory/Outpatient/Recurring Outpatient visits " +
			"should be activated using trigger A04", LOW), //-

	F130("Trigger A04 should only be used to activate an outpatient/ambulatory/emergency visit (PV1-2 = E/O/R/U)", "Inpatient visits " +
			"should be activated using trigger A01", LOW), //-
	
	F131("This field must contain a number (whole integer or decimal), but text was found or it was blank", "This field must contain a whole or decimal number. In other words," +
			"it must only contain characters 0-9 or '.'", MED), // -

			;
	
	private String myDesc;
	private String myStepsToResolve;
	private Severity mySeverity;


	FailureCode(String theDescription) {
		myDesc = theDescription;
	}

	FailureCode(String theDescription, Severity theSeverity) {
		myDesc = theDescription;
		mySeverity = theSeverity;
	}

	FailureCode(String theDescription, String theStepsToResolve, Severity theSeverity) {
		myDesc = theDescription;
		myStepsToResolve = theStepsToResolve;
		mySeverity = theSeverity;
	}

	/**
     * @return the severity
     */
    public Severity getSeverity() {
    	return mySeverity != null ? mySeverity : Severity.MED;
    }

	/**
     * @return the stepsToResolve
     */
    public String getStepsToResolve() {
    	return myStepsToResolve;
    }

	/**
	 * @return the desc
	 */
	public String getDesc() {
		return myDesc;
	}
	
	
	public enum Severity{
		LOW, MED, HIGH
	}
}


class Constants
{
	static final String SEE_HERE_HTML = "See <a href=\"http://conftest.connectinggta.ca/cGTA_Conformance_Validator/#OID\">here</a> ";
	
}