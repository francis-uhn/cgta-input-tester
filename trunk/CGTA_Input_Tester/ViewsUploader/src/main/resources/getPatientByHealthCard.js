/**************************************************************************/
/** view to make available patient search by Health Card Number/last name
/** test CDR as PLS
/** work started: Sep 27 2012
/** author: Alex Naumov
/**************************************************************************/

function(doc)
{
	var mrnKey;
	var siteId;
	var healthCardNum;
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
			var idTypeCode = patientsArray[pNumber].myIdTypeCode;
			// Medical Record Number
			if(idTypeCode==="MR") {
				mrnKey = patientsArray[pNumber].myIdNumber;
				siteId = patientsArray[pNumber].myAssigningAuthorityHspId;
			}
			else if (idTypeCode==="JHN") {
				healthCardNum = patientsArray[pNumber].myIdNumber;
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
		if(!dob){
		 dob = "1800-01-01T05:00:00.000+0000";
		}	
		
		function capitaliseFirstLetter(string)
		{
		    return string.charAt(0).toUpperCase() + string.slice(1);
		}
		
		
		patient = {healthCardNum:healthCardNum,
				mrn:mrnKey,
				firstName: capitaliseFirstLetter(legalName.myFirstName.toLowerCase()),
				lastName: capitaliseFirstLetter(legalName.myLastName.toLowerCase()),
				gender: myPatient.myAdministrativeSex,
				// Sites sending us different daylight saving time
				// this was causing duplicate patients in patient search. now it is hardcoded
				dateOfBirth: ""+dob.replace("T"," ").slice(0,10)+" 05:00", 
				site: siteId};
		
		var complexKey = [patient.healthCardNum, patient.lastName, patient.mrn, patient.firstName, patient.gender, patient.dateOfBirth, patient.site];
		
		// Check for Deactivate flag it is related only for PWV
		if(doc.document.myType.toString() == "PATIENT_WITH_VISITS")
			if(myPatient.myDeactivatePatientIndicator && myPatient.myDeactivatePatientIndicator === 'Y')
				complexKey.push("Y");
		
		emit(complexKey, patient);
	}
}