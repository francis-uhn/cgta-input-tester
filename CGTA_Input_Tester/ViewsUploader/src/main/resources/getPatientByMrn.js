function (doc)
{
	var mrnKey;
	var siteId;
	if (doc.document.myType.toString() == "DOCUMENT"||doc.document.myType.toString() == "PATIENT_WITH_VISITS"||doc.document.myType.toString() == "MEDICATION_ORDER") {
		
		
		var myPatient;
		if(doc.document.myType.toString() == "MEDICATION_ORDER")
		{
			var medicationOrderWadmins = doc.document;
			myPatient = medicationOrderWadmins.myOrder.myPatient;
		}
		else
			myPatient = doc.document.myPatient;

		var patientsArray = myPatient.myPatientIds;
		for (var pNumber in patientsArray) {
			// Medical Record Number
			if(patientsArray[pNumber].myIdTypeCode==="MR") {
				mrnKey = patientsArray[pNumber].myIdNumber;
				siteId = patientsArray[pNumber].myAssigningAuthorityHspId;
			}
		}
		// iterate over patient Names, use only leagl name
		var patientNamesArray = myPatient.myPatientNames;
		var legalName;
		
		for(var patientNameNumber in patientNamesArray) {
			if( "Legal Name" === patientNamesArray[patientNameNumber].myNameType) {
				legalName = patientNamesArray[patientNameNumber];
			}
		}
		var patient;
		
		var dob = myPatient.myDateOfBirth;
		if(!dob)
			dob = "";
		
		function capitaliseFirstLetter(string)
		{
		    return string.charAt(0).toUpperCase() + string.slice(1);
		}
		
		patient = {mrn:mrnKey,
				firstName: capitaliseFirstLetter(legalName.myFirstName.toLowerCase()),
				lastName: capitaliseFirstLetter(legalName.myLastName.toLowerCase()),
				gender: myPatient.myAdministrativeSex,
				// Sites sending us different daylight saving time
				// this was causing duplicate patients in patient search. now it is hardcoded
				dateOfBirth: ""+dob.replace("T"," ").slice(0,10)+" 05:00", 
				site: siteId};
		
		var complexKey = [patient.mrn, patient.site, patient.firstName, patient.lastName, patient.gender,patient.dateOfBirth];
		
		// Check for Deactivate flag it is related only for PWV
		if(doc.document.myType.toString() == "PATIENT_WITH_VISITS")
			if(myPatient.myDeactivatePatientIndicator && myPatient.myDeactivatePatientIndicator === 'Y')
				complexKey.push("Y");
		
		emit(complexKey, patient);
	}
}