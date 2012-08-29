function (doc) {

    
    var mrnKey;
    var siteId;
    var systemId;
    
    
    function nullConvert(value){
    	
    	if( !value || value === "" || value.length === 0 ){    		
    		return "NULL"    		
    	}
    	else {
    		return value    	
    	}
    }
    
    function getNoteObj(name, value) {
    	return {name: name.toString(), note: value.toString()}	
    }
    function pushNoteObj(name,value,array)
    {
    	array.push(getNoteObj(name,value));
    }
        
    if (doc.document.myType.toString() == "DOCUMENT"|| doc.document.myType.toString() == "PATIENT_WITH_VISITS" || doc.document.myType.toString() == "MEDICATION_ORDER") {
        
        var patientIdArray = null;
        
        if (doc.document.myType.toString() == "MEDICATION_ORDER") {
            patientIdArray = doc.document.myOrder.myPatient.myPatientIds;
        } else {
            patientIdArray = doc.document.myPatient.myPatientIds;
        }       
        
        
        for (var pNumber in patientIdArray) {
            // Medical Record Number
            var patientId = patientIdArray[pNumber];
            if(patientIdArray[pNumber].myIdTypeCode==="MR") {
                mrnKey = patientId.myIdNumber;
                siteId = patientId.myAssigningAuthorityHspId;
                systemId = patientId.myAssigningAuthoritySystemId
            }
        }
    }

    if (doc.document.myType.toString() == "PATIENT_WITH_VISITS") {

        /** ********************************************* */
        /** EMIT VISITS */
        /** ********************************************* */
    	
        	var myPatient = doc.document.myPatient;
            
            function iterateVisits(myVisits)
            {
                for (var nextVisitIdx in myVisits) {
    
                    var visit = myVisits[nextVisitIdx];
            
                    var visitKey = "VISIT___" + siteId + 
                                       "___" + systemId + 
                                       "___" + mrnKey +
                                       "___" + visit.myVisitNumber.myAssigningAuthorityHspId + 
                                       "___" + visit.myVisitNumber.myAssigningAuthoritySystemId + 
                                       "___" + visit.myVisitNumber.myIdNumber;
                    
                    var visitName = visit.myPatientClassName + " Visit (" + visit.myVisitNumber.myIdNumber + ")";
                    // Add notes containing details of the visit
                    var notesArray = new Array();
                    
                    
                    notesArray.push({name: "Visit Number", note: nullConvert(visit.myVisitNumber.myIdNumber)});              
                    notesArray.push({name: "Visit Status", note: nullConvert(visit.myVisitStatus)});
    
    
                    // Admit date
                    notesArray.push({name: "Visit Start Date", note: nullConvert(visit.myAdmitDateFormatted)});         
                                        
                    // Discharge Date
                    if (visit.myDischargeDatesFormatted && visit.myDischargeDatesFormatted.length > 0) {
                        notesArray.push({name: "Visit End Date", note: nullConvert(visit.myDischargeDatesFormatted[visit.myDischargeDates.length - 1])}); 
                    }
                    else {
                        notesArray.push({name: "Visit End Date", note: "NULL"});                     
                    }
                        
    
                    // Function to right down doctors from array
                    function writeDownDoctors(doctorArray, type)
                    {                        
                        var noteText = "";
                        for(var docNum=0; docNum < doctorArray.length; docNum++)
                        {
                            var doctor = doctorArray[docNum];                            
                            noteText += "<b>Last name: </b>" + nullConvert(doctor.myLastName) + "<br><b>First Name: </b>"+ nullConvert(doctor.myFirstName);                            
                            noteText += "<br><b>Middle Name: </b>"+ nullConvert(doctor.myMiddleName);                            
                            noteText += "<br><b>Id: </b>"+ nullConvert(doctor.myId);
                            noteText += "<br><b>Id Type: </b>"+ nullConvert(doctor.myIdType);
                            noteText += "<br><b>AssigningHspName: </b>" + nullConvert(doctor.myAssigningHspName);
                            if ( docNum <  doctorArray.length - 1 ) {
                            	noteText += "<br><br>";
                            }
                        }
                        notesArray.push({name: type, note: noteText});
                    }
                    
                    
                    // if(visit.myPatientClassCode)
                    notesArray.push({name: "Patient Class Code", note: nullConvert(visit.myPatientClassCode)});
                    notesArray.push({name: "Admission Type", note: nullConvert(visit.myAdmissionType)});
                    notesArray.push({name: "Patient Class Name",note: nullConvert(visit.myPatientClassName)});
                        
                    // add doctors to notes
                    if(visit.myAdmittingDoctors && visit.myAdmittingDoctors.length > 0){
                        writeDownDoctors(visit.myAdmittingDoctors, "Admitting Doctors");
                    }
                    else {
                        notesArray.push({name: "Admitting Doctors", note: "NULL"});                     
                    }
                    
                    if(visit.myAttendingDoctors && visit.myAttendingDoctors.length > 0){
                        writeDownDoctors(visit.myAttendingDoctors, "Attending Doctors");
                    }
                    else {
                        notesArray.push({name: "Admitting Doctors", note: "NULL"});                     
                    }
                                        
                    if(visit.myReferringDoctors && visit.myReferringDoctors.length > 0){
                        writeDownDoctors(visit.myReferringDoctors, "Referring Doctors"); 
                    }
                    else {
                        notesArray.push({name: "Referring Doctors", note: "NULL"});                     
                    }
                    
                    if(visit.myConsultingDoctors && visit.myConsultingDoctors.length > 0){
                        writeDownDoctors(visit.myConsultingDoctors, "Consulting Doctors");
                    }
                    else {
                        notesArray.push({name: "Consulting Doctors", note: "NULL"});                     
                    }
    
                    // PriorPatientLocation
                    if(visit.myPriorPatientLocation)
                    {
                        var tempNote = "";
                        tempNote += "<b>Point Of Care: </b>" + nullConvert(visit.myPriorPatientLocation.myPointOfCare);
                        tempNote += "<br> <b>Room: </b>" + nullConvert(visit.myPriorPatientLocation.myRoom);
                        tempNote += "<br> <b>Bed: </b>" + nullConvert(visit.myPriorPatientLocation.myBed);
                        tempNote += "<br> <b>Facility Name: </b>" + nullConvert(visit.myPriorPatientLocation.myFacilityName);
                        tempNote += "<br> <b>Hsp Name: </b>" + nullConvert(visit.myPriorPatientLocation.myHspName);
                        notesArray.push({name: "Prior Patient Location", note: tempNote});                    
                    }
                    else {
                        notesArray.push({name: "Prior Patient Location", note: "NULL"});                     
                    }
                    
                    // AssignedPatientLocation
                    if(visit.myAssignedPatientLocation)
                    {    
                      var tempNote = "";
                      tempNote += "<b>Point Of Care: </b>" + nullConvert(visit.myAssignedPatientLocation.myPointOfCare);
                      tempNote += "<br> <b>Room: </b>" + nullConvert(visit.myAssignedPatientLocation.myRoom);
                      tempNote += "<br> <b>Bed: </b>" + nullConvert(visit.myAssignedPatientLocation.myBed);
                      tempNote += "<br> <b>Facility Name: </b>" + nullConvert(visit.myAssignedPatientLocation.myFacilityName);
                      tempNote += "<br> <b>Hsp Name: </b>" + nullConvert(visit.myAssignedPatientLocation.myHspName);
                      notesArray.push({name: "Assigned Patient Location", note: tempNote});    
                     }
                     else {
                        notesArray.push({name: "Assigned Patient Location", note: "NULL"});                     
                    }
                     
            
                    // Other fields
                    if ( visit.myDiagnoses && visit.myDiagnoses.length > 0 ) {                    	
                    	var noteText = "";                    	
                        for(var index=0; index < visit.myDiagnoses.length; index++)
                        {
                            var dg = visit.myDiagnoses[index];                            
                            noteText += "<b>Diagnosis Code: </b>" + nullConvert(dg.myDiagnosis.myCode);
                            noteText += "<br> <b>Diagnosis Text: </b>" + nullConvert(dg.myDiagnosis.myText);
                            if ( index < visit.myDiagnoses.length - 1 ) {
                            	noteText += "<br><br>";
                            }
                        }
                        notesArray.push({name: "Diagnoses", note: noteText});
                    }
                    else {
                        notesArray.push({name: "Diagnoses", note: "NULL"});                     
                    }
    
    
                    // Other fields
                    if ( visit.myPreviousVisitNumbers && visit.myPreviousVisitNumbers.length > 0 ) {
                        var noteText = "";
                        for(var index=0; index < visit.myPreviousVisitNumbers.length; index++) {   
                            noteText += nullConvert(visit.myPreviousVisitNumbers[index].myIdNumber);
                            if (index < (visit.myPreviousVisitNumbers.length - 1)) {
                                noteText += ", ";                           
                            }                         
                        } 
                        notesArray.push({name: "Previous Visit Numbers", note: noteText});
                    }
                    else {
                        notesArray.push({name: "Previous Visit Numbers", note: "NULL"});                     
                    }
                    
                    
                    if ( visit.myAdmissionLevelOfCareForEmergencyVisit ) {
                        notesArray.push({name: "Admission Level Of Care For Emerg Visit Code", note: nullConvert(visit.myAdmissionLevelOfCareForEmergencyVisit.myCode)});
                        notesArray.push({name: "Admission Level Of Care For Emerg Visit Text", note: nullConvert(visit.myAdmissionLevelOfCareForEmergencyVisit.myText)});                                        
                    }
                    else {
                        notesArray.push({name: "Admission Level Of Care For Emerg Visit Code and Text", note: "NULL"});                     
                    }
                    
                    
                    if ( visit.myAdmitReasonForEmergencyVisit ) {
                        notesArray.push({name: "Admit Reason For Emergency Visit Code", note: nullConvert(visit.myAdmitReasonForEmergencyVisit.myCode)});
                        notesArray.push({name: "Admit Reason For Emergency Visit Text", note: nullConvert(visit.myAdmitReasonForEmergencyVisit.myText)});                                        
                    }
                    else {
                        notesArray.push({name: "Admit Reason For Emergency Visit Code and Text", note: "NULL"});                     
                    }
                    
                    
                    notesArray.push({name: "Hospital Service Code", note: nullConvert(visit.myHospitalService)});
                    notesArray.push({name: "Hospital Service Name", note: nullConvert(visit.myHospitalServiceName)});
                    notesArray.push({name: "Patient Requested Record Lock", note: nullConvert(visit.myPatientRequestedRecordLock)});
                    
                    
                    // reoccuring outpatient visit arrival times
                    if ( visit.myFormattedArrivalDates && visit.myFormattedArrivalDates.length > 0 ) {
                        var noteText = "";
                        for(var j=0; j < visit.myFormattedArrivalDates.length; j++) {                           
                            noteText += nullConvert(visit.myFormattedArrivalDates[j]);
                            if ( j < visit.myFormattedArrivalDates.length - 1 ) {
                            	noteText += "<br>";                            	
                            }
                        }                        
                        notesArray.push({name: "Reoccurring Oupatient Arrival Dates", note: noteText});
                    }
                    else {
                        notesArray.push({name: "Reoccurring Oupatient Arrival Dates", note: "NULL"});                     
                    }              
                    
                    
                    
                    // Null check for DOB
                    var dob;
                    if(myPatient.myDateOfBirth)
                    	dob = myPatient.myDateOfBirth.replace(/T.*/, "");
                    else
                    	dob = "NULL";
                    	
                    //Null check for myAdministrativeSex                    	
                    var sex = nullConvert(myPatient.myAdministrativeSex);
                                        
                    // Add some header info as act level notes
                    var actNotesArray = new Array();
                    var ptDemo = "Last Name: " + myPatient.myPatientNames[0].myLastName + ", First Name: " + myPatient.myPatientNames[0].myFirstName + "<br>" +
                    "DOB: " + dob + ", Gender: " + sex;
                     actNotesArray.push({name: "Patient", note: ptDemo});   
                     
                     
                    
                    
                    // Forming loaded act object
                    var act = {pid: null,
                            id: visitKey, 
                            name: visitName,
                            fullyLoaded: true,
                            availabilityTime: visit.myAdmitDateFormatted,
                            effectiveTime: visit.myAdmitDateFormatted,
                            statusCode: {id: "COMPLETE", name: "Complete"},
                            statusCodes: [{id: "COMPLETE", name: "Complete"}],
                            departmentCode: {},
                            procCodes: [{}],
                            notes: actNotesArray,
                            observations: [{valueType:"ST", notes:notesArray}],
                            procCode:null,
                            visitPid:null,
                            resulted: true};
    
                    emit(visitKey, act);
                }
            }
            
 
            
            function createAllergyAssessment(patient, adverseReactions)
           	{                
               var key = "ALLERGY_ASMT___" + siteId + 
                                   "___" + systemId + 
                                   "___" + mrnKey;
                                   
                var notesArray = new Array();
                
                if ( adverseReactions.length == 0 ) {
                	return;
                }
                
                var allergyAssName = "Allergy Assessment (" + mrnKey + ")";
                
                var noteText = "";
                
                for(var index=0; index < adverseReactions.length; index++) {
                	
                	var ar = adverseReactions[index];
                	
                	if ( ar.myAllergenTypeCode ) {                            
                        noteText += "<b>Allergen Type Code: </b>" + nullConvert(ar.myAllergenTypeCode.myCode);
                        noteText += "<br><b>Allergen Type Text: </b>" + nullConvert(ar.myAllergenTypeCode.myText);                        
                	}
                	else {
                		noteText += "<b>Allergen Type Code and Text: </b>NULL";                		
                	}
                	
                	if ( ar.myAllergenCode ) {                            
                        noteText += "<br><b>Allergen Mnemonic/Description Code: </b>" + nullConvert(ar.myAllergenCode.myCode);
                        noteText += "<br><b>Allergen Mnemonic/Description Text: </b>" + nullConvert(ar.myAllergenCode.myText);
                    }
                    else {
                        noteText += "<br><b>Allergen Mnemonic/Description Code and Text: </b>NULL";                     
                    }
                    
                    
                    if ( ar.myAllergySeverityCode ) {                            
                        noteText += "<br><b>Allergen Severity Code: </b>" + nullConvert(ar.myAllergySeverityCode.myCode);
                        noteText += "<br><b>Allergen Severity Text: </b>" + nullConvert(ar.myAllergySeverityCode.myText);
                    }
                    else {
                        noteText += "<br><b>Allergen Severity Code and Text: </b>NULL";                     
                    }
                    
                    
                    if ( ar.myAllergyReactionCodes && ar.myAllergyReactionCodes.length > 0 ) {
                        var reactions = "";
                        for(var j=0; j < ar.myAllergyReactionCodes.length; j++) {                        	
                            reactions += "<br>" + nullConvert(ar.myAllergyReactionCodes[j]);
                        }
                        noteText += "<br><b>Allergy Reactions: </b>" + reactions;
                    }
                    else {
                        noteText += "<br><b>Allergy Reactions: </b>NULL";                     
                    }
                    
                                    
                    noteText += "<br><b>Onset Date: </b>" + nullConvert(ar.myOnsetDateFormatted);         
                    noteText += "<br><b>Onset Text: </b>" + nullConvert(ar.myOnsetText);
                    noteText += "<br><b>Reported Date: </b>" + nullConvert(ar.myReportedDateTimeFormatted);         
 
                    
                    if ( ar.myRelationshipToPatient ) {                            
                        noteText += "<br><b>Relationship to Patient Code: </b>" + nullConvert(ar.myRelationshipToPatient.myCode);
                        noteText += "<br><b>Relationship to Patient Text: </b>" + nullConvert(ar.myRelationshipToPatient.myText);
                    }
                    else {
                        noteText += "<br><b>Relationship to Patient Code and Text: </b>NULL";                     
                    }
                    
                    if (index < (adverseReactions.length - 1)) {                                                   
                        noteText += "<br><br>";                                                        
                    }
                }
                
                notesArray.push({name: "Adverse Reactions", note: nullConvert(noteText)});
                
                
                // Null check for DOB
                var dob;
                if(patient.myDateOfBirth)
                    dob = patient.myDateOfBirth.replace(/T.*/, "");
                else
                    dob = "NULL";
                    
                 //Null check for myAdministrativeSex                       
                var sex = nullConvert(patient.myAdministrativeSex);    
                    
                
                // Add some header info as act level notes
                var actNotesArray = new Array();
                var ptDemo = "Last Name: " + patient.myPatientNames[0].myLastName + ", First Name: " + patient.myPatientNames[0].myFirstName + "<br>" +
                "DOB: " + dob + ", Gender: " + sex;
                 actNotesArray.push({name: "Patient", note: ptDemo});   
                 
                
                // Forming loaded act object
                var act = {pid: null,
                        id: key, 
                        name: allergyAssName,
                        fullyLoaded: true,
                        availabilityTime: patient.myRecordUpdatedDateFormatted,
                        effectiveTime: patient.myRecordUpdatedDateFormatted,
                        statusCode: {id: "COMPLETE", name: "Complete"},
                        statusCodes: [{id: "COMPLETE", name: "Complete"}],
                        departmentCode: {},
                        procCodes: [{}],
                        notes: actNotesArray,
                        observations: [{valueType:"ST", notes:notesArray}],
                        procCode:null,
                        visitPid:null,
                        resulted: true};
    
                emit(key, act);            
            }
        
        
        
        
            function createPatient(patient)
            {                
               var key = "PATIENT___" + siteId + 
                                   "___" + systemId + 
                                   "___" + mrnKey;
                                   
                var notesArray = new Array();
                
                var patientDemoName = "Patient Demographics (" + mrnKey + ")";
                
                                
                //print all patient ids (number, idType, idTypeDesc)                
                if ( patient.myPatientIds && patient.myPatientIds.length > 0 ) {
                	var noteText = "";                	
                    for(var index=0; index < patient.myPatientIds.length; index++)
                    {
                        var id = patient.myPatientIds[index];                        
                        noteText += "<b>Id Number: </b>" + nullConvert(id.myIdNumber);
                        noteText += "<br><b>Id Check Digit: </b>" + nullConvert(id.myCheckDigit);
                        noteText += "<br><b>Id Type Code: </b>" + nullConvert(id.myIdTypeCode);
                        noteText += "<br><b>Id Type Description: </b>" + nullConvert(id.myIdTypeDescription);
                        noteText += "<br><b>Id Assigning Jurisdiction Id: </b>" + nullConvert(id.myAssigningJurisdictionId);                        
                        noteText += "<br><b>Id Assigning Jurisdiction Text: </b>" + nullConvert(id.myAssigningJurisdictionText);
                        if ( index < patient.myPatientIds.length - 1 ) {
                            noteText += "<br><br>";
                        }
                    }
                    notesArray.push({name: "Patient Ids", note: noteText});
                }
                else {
                    notesArray.push({name: "Patient Ids", note: "NULL"});                     
                }
                
                //function to create names string from a names array
                //print all name fields
                function createNamesString(names)
                {
                    var noteText = "";	
                    for(var index=0; index < names.length; index++)                
                    {
                        var name = names[index];                        
                        noteText += "<b>Last Name: </b>" + nullConvert(name.myLastName);
                        noteText += "<br><b>First Name: </b>" + nullConvert(name.myFirstName);
                        noteText += "<br><b>Second Name: </b>" + nullConvert(name.mySecondName);
                        noteText += "<br><b>Name Prefix: </b>" + nullConvert(name.myPrefix);
                        noteText += "<br><b>Name Suffix: </b>" + nullConvert(name.mySuffix);
                        noteText += "<br><b>Name Degree: </b>" + nullConvert(name.myDegree);
                        noteText += "<br><b>Name Type: </b>" + nullConvert(name.myNameType);
                        if ( index < names.length - 1 ) {
                            noteText += "<br><br>";
                        }
                    }
                    return noteText
                }                    
                
                //print all patient names
                if ( patient.myPatientNames && patient.myPatientNames.length > 0 ) {
                    notesArray.push({name: "Patient Names", note: createNamesString(patient.myPatientNames)});
                }
                else {
                    notesArray.push({name: "Patient Names", note: "NULL"});                     
                }
                
                //print mother's maiden name (main fields)
                if ( patient.myMothersMaidenName ) {
                	var name = patient.myMothersMaidenName;
                	var noteText = "<b>Last Name: </b>" + nullConvert(name.myLastName);                    
                    noteText += "<br><b>Name Type: </b>" + nullConvert(name.myNameType);
                    notesArray.push({name: "Mother's Maiden Name", note: noteText});
                }
                else {
                    notesArray.push({name: "Mothers Maiden Name", note: "NULL"});                     
                }                                    
                
                //print DOB                
                if(patient.myDateOfBirth){
                    dob = patient.myDateOfBirth.replace(/T.*/, "");
                    notesArray.push({name: "Date of Birth", note: dob});
                }    
                else {
                    notesArray.push({name: "Date of Birth", note: "NULL"});
                }   
                    
                //print Gender 
                notesArray.push({name: "Administrative Sex", note: nullConvert(patient.myAdministrativeSex)});
                                
                //function to create addresses string from a address array
                //print (myStreetAddress, myStreetAddress, myCity, myProvince, myPostalCode, myCountry, myAddressType, myEffectiveDateFormatted, myExpirationDateFormatted)
                function createAddressesString(addresses)
                {
                    var noteText = "";  
                    for(var index=0; index < addresses.length; index++)                
                    {
                        var address = addresses[index];                        
                        noteText += "<b>Address Street: </b>" + nullConvert(address.myStreetAddress);
                        noteText += "<br><b>Address Street2: </b>" + nullConvert(address.myStreetAddress2);
                        noteText += "<br><b>Address City: </b>" + nullConvert(address.myCity);
                        noteText += "<br><b>Address Province/State: </b>" + nullConvert(address.myProvince);                        
                        noteText += "<br><b>Address Country: </b>" + nullConvert(address.myCountry);
                        noteText += "<br><b>Address Postal Code: </b>" + nullConvert(address.myPostalCode);
                        noteText += "<br><b>Address Type: </b>" + nullConvert(address.myAddressType);
                        noteText += "<br><b>Address Effective Date: </b>" + nullConvert(address.myEffectiveDateFormatted);
                        noteText += "<br><b>Address Expiration Date: </b>" + nullConvert(address.myExpirationDateFormatted);
                        if ( index < addresses.length - 1 ) {
                            noteText += "<br><br>";
                        }
                    }
                    return noteText
                }
                    
                //print all addresses                
                if ( patient.myPatientAddresses && patient.myPatientAddresses.length > 0 ) {
                    notesArray.push({name: "Patient Addresses", note: createAddressesString(patient.myPatientAddresses)});
                }
                else {
                    notesArray.push({name: "Patient Addresses", note: "NULL"});                     
                }
                
                //print primary lang                
                if(patient.myPrimaryLanguage){
                	notesArray.push({name: "Primary Language Code", note: nullConvert(patient.myPrimaryLanguage.myCode)});                    
                    notesArray.push({name: "Primary Language Text", note: nullConvert(patient.myPrimaryLanguage.myText)});
                }    
                else {
                    notesArray.push({name: "Primary Language", note: "NULL"});
                }
                                
                //print death date
                notesArray.push({name: "Death Date and Time", note: nullConvert(patient.myDeathDateAndTimeFormatted)});
                
                
                //function to create phone numbers string from a phone numbers array
                //print (myPhoneNumber, myPhoneNumberType, myNumberParts1CountryCode, myNumberParts2AreaCode, myNumberParts3LocalNumber, myNumberParts4Ext,myEmailAddress, myPhoneNumberTypeCode)
                function createPhoneNumbersString(phoneNumbers)
                {
                    var noteText = "";  
                    for(var index=0; index < phoneNumbers.length; index++)                
                    {
                        var phone = phoneNumbers[index];                        
                        noteText += "<b>Phone Number: </b>" + nullConvert(phone.myPhoneNumber);
                        noteText += "<br><b>Phone Number Type: </b>" + nullConvert(phone.myPhoneNumberType);
                        noteText += "<br><b>Phone Number Country Code: </b>" + nullConvert(phone.myNumberParts1CountryCode);
                        noteText += "<br><b>Phone Number Area Code: </b>" + nullConvert(phone.myNumberParts2AreaCode);                        
                        noteText += "<br><b>Phone Number Local Number: </b>" + nullConvert(phone.myNumberParts3LocalNumber);
                        noteText += "<br><b>Phone Number Ext: </b>" + nullConvert(phone.myNumberParts4Ext);
                        noteText += "<br><b>Email Address: </b>" + nullConvert(phone.myEmailAddress);
                        noteText += "<br><b>Any Text: </b>" + nullConvert(phone.myAnyText);                        
                        if ( index < phoneNumbers.length - 1 ) {
                            noteText += "<br><br>";
                        }
                    }
                    return noteText
                }
                                
                
                if ( patient.myPhoneNumbers && patient.myPhoneNumbers.length > 0 ) {                    
                    notesArray.push({name: "Patient Phone Numbers", note: createPhoneNumbersString(patient.myPhoneNumbers)});
                }
                else {
                    notesArray.push({name: "Patient Phone Numbers", note: "NULL"});                     
                }
                
                
                //function to create provider names string from a names array
                //print all name fields
                function createProviderNamesString(pnames)
                {
                    var noteText = "";  
                    for(var index=0; index < pnames.length; index++)                
                    {
                        var pname = pnames[index];
                        noteText += "<b>Id: </b>" + nullConvert(pname.myId);
                        noteText += "<br><b>Id Type: </b>" + nullConvert(pname.myIdType);
                        noteText += "<br><b>Last Name: </b>" + nullConvert(pname.myLastName);
                        noteText += "<br><b>First Name: </b>" + nullConvert(pname.myFirstName);
                        noteText += "<br><b>Middle Name: </b>" + nullConvert(pname.myMiddleName);
                        noteText += "<br><b>Name Prefix: </b>" + nullConvert(pname.myPrefix);
                        noteText += "<br><b>Name Suffix: </b>" + nullConvert(pname.mySuffix);
                        noteText += "<br><b>Name Degree: </b>" + nullConvert(pname.myDegree);                                                
                        noteText += "<br><b>Hospital Name: </b>" + nullConvert(pname.myAssigningHspName);
                        if ( index < pnames.length - 1 ) {
                            noteText += "<br><br>";
                        }
                    }
                    return noteText
                }   
                
                
                
                //print all personInRoles (for each print roleType, names, addresses, contact numbers)
                if ( patient.myPersonInRoles && patient.myPersonInRoles.length > 0 ) {                 	 
                 	var noteText = "";  
                    for(var index=0; index < patient.myPersonInRoles.length; index++)                
                    {
                        var role = patient.myPersonInRoles[index];
                        
                        if(role.myRole){
                        	noteText += "<b>Role Code: </b>" + nullConvert(role.myRole.myCode);
                        	noteText += "<br><b>Role Text: </b>" + nullConvert(role.myRole.myText);
                        	noteText += "<br>";
                        }
                        else{
                        	noteText += "<b>Role: </b>NULL";
                        	noteText += "<br>";
                        }                        
                        
                        //print all names
                        if ( role.myPersonNames && role.myPersonNames.length > 0 ) {                        	
                        	noteText += "<br><b>Provider Names: </b>";
                        	noteText += "<br>" + nullConvert(createProviderNamesString(role.myPersonNames));
                        	noteText += "<br>";
                        }
                        else {
                            noteText += "<br><b>Names: </b>NULL";
                            noteText += "<br>";
                        }
                        
                        //print all addresses
                        if ( role.myAddresses && role.myAddresses.length > 0 ) {                            
                            noteText += "<br><b>Addresses: </b>";
                            noteText += "<br>" + nullConvert(createAddressesString(role.myAddresses));
                            noteText += "<br>";
                        }
                        else {
                            noteText += "<br><b>Addresses: </b>NULL";
                            noteText += "<br>";
                        }
                        
                        //print all contact numbers
                        if ( role.myContactInformation && role.myContactInformation.length > 0 ) {                            
                            noteText += "<br><b>Contact Info: </b>";
                            noteText += "<br>" + nullConvert(createPhoneNumbersString(role.myContactInformation));
                            noteText += "<br>";
                        }
                        else {
                            noteText += "<br><b>Contact Info: </b>NULL";
                            noteText += "<br>";
                        }                        
                        
                        if ( index < patient.myPersonInRoles.length - 1 ) {
                            noteText += "<br><br>";
                        }
                    }                 	
                    notesArray.push({name: "Person Level Providers", note: noteText});
                }
                else {
                    notesArray.push({name: "Person Level Providers", note: "NULL"});                     
                }
                
                                
                //print all associated parties (for each print relationship, names, addresses, contact numbers)
                if ( patient.myAssociatedParties && patient.myAssociatedParties.length > 0 ) {                      
                    var noteText = "";  
                    for(var index=0; index < patient.myAssociatedParties.length; index++)                
                    {
                        var party = patient.myAssociatedParties[index];
                        
                        noteText += "<b>Relationship Name: </b>" + nullConvert(party.myRelationshipName);
                        noteText += "<br>";
                        
                        //print all names
                        if ( party.myNames && party.myNames.length > 0 ) {                            
                            noteText += "<br><b>Names: </b>";
                            noteText += "<br>" + nullConvert(createNamesString(party.myNames));
                            noteText += "<br>";
                        }
                        else {
                            noteText += "<br><b>Names: </b>NULL";
                            noteText += "<br>";
                        }
                        
                        //print all addresses
                        if ( party.myAddresses && party.myAddresses.length > 0 ) {                            
                            noteText += "<br><b>Addresses: </b>";
                            noteText += "<br>" + nullConvert(createAddressesString(party.myAddresses));
                            noteText += "<br>";
                        }
                        else {
                            noteText += "<br><b>Addresses: </b>NULL";
                            noteText += "<br>";
                        }
                        
                        //print all contact numbers
                        if ( party.myContactInformation && party.myContactInformation.length > 0 ) {                            
                            noteText += "<br><b>Contact Info: </b>";
                            noteText += "<br>" + nullConvert(createPhoneNumbersString(party.myContactInformation));
                            noteText += "<br>";
                        }
                        else {
                            noteText += "<br><b>Contact Info: </b>NULL";
                            noteText += "<br>";
                        }                        
                        
                        if ( index < patient.myAssociatedParties.length - 1 ) {
                            noteText += "<br><br>";
                        }
                    }                   
                    notesArray.push({name: "Associated Parties", note: noteText});
                }
                else {
                    notesArray.push({name: "Associated Parties", note: "NULL"});                     
                }
                
                
                 
                
                // Forming loaded act object
                var act = {pid: null,
                        id: key, 
                        name: patientDemoName,
                        fullyLoaded: true,
                        availabilityTime: patient.myRecordUpdatedDateFormatted,
                        effectiveTime: patient.myRecordUpdatedDateFormatted,
                        statusCode: {id: "COMPLETE", name: "Complete"},
                        statusCodes: [{id: "COMPLETE", name: "Complete"}],
                        departmentCode: {},
                        procCodes: [{}],
                        notes: [{}],
                        observations: [{valueType:"ST", notes:notesArray}],
                        procCode:null,
                        visitPid:null,
                        resulted: true};
    
                emit(key, act);            
             }        
        
        
                
        

        function iterateMergedVisits(MergedInPatientsWithVisits)
        {
            for(var mergeVisitsIndx in MergedInPatientsWithVisits)
            {
                var mergedVisit = MergedInPatientsWithVisits[mergeVisitsIndx];
                
                if(mergedVisit.myVisits)
                    iterateVisits(mergedVisit.myVisits);
                    
                if(mergedVisit.myMergedInPatientsWithVisits)
                    iterateMergedVisits(mergedVisit.myMergedInPatientsWithVisits);
            }
        }
                      
        
        if (doc.document.myPatient) {
            createPatient(doc.document.myPatient);         
        }        
                
        if (doc.document.myPatient && doc.document.myPatient.myAdverseReactions && doc.document.myPatient.myAdverseReactions.length > 0) {
        	createAllergyAssessment(doc.document.myPatient, doc.document.myPatient.myAdverseReactions);        	
        }  
                
        if (doc.document.myVisits){ 
            iterateVisits(doc.document.myVisits);
        }     
        
        if(doc.document.myMergedInPatientsWithVisits){
            iterateMergedVisits(doc.document.myMergedInPatientsWithVisits);
        }    
            
            
    
    } else  if (doc.document.myType.toString() == "MEDICATION_ORDER") {

        
        /** ********************************************* */
        /** EMIT MEDICATION ORDER */
        /** ********************************************* */
        

                var medicationOrderWadmins = doc.document;
                var myPatient = medicationOrderWadmins.myOrder.myPatient;
                var medicationOrder = medicationOrderWadmins.myOrder;
                var medicationAdmins = medicationOrderWadmins.myAdmins;
                
                
                
                if (!medicationOrder) {
                    return;
                }
                
                if (!myPatient) {
                    return;
                }
                        
                
                var medicationOrderKey = "MEDICATION_ORDER___" + medicationOrder.myPlacerOrderNumber.myFacilityId +                
                                   "___" + medicationOrder.myPlacerOrderNumber.mySystemId + 
                                   "___" + medicationOrder.myPlacerOrderNumber.myId;
                
                // Add notes containing details of the medication order and its
                // medication administrations
                var notesArray = new Array();

                
                notesArray.push({name: "Order Status Code", note: nullConvert(medicationOrder.myStatusCode)});
                notesArray.push({name: "Order Status Name", note: nullConvert(medicationOrder.myStatusName)});
                notesArray.push({name: "Order Quantity Number", note: nullConvert(medicationOrder.myEncodedOrderQuantityNumber)});
                notesArray.push({name: "Order Quantity Repeat Pattern", note: nullConvert(medicationOrder.myEncodedOrderQuantityRepeatPattern)});
                notesArray.push({name: "Order Quantity Duration", note: nullConvert(medicationOrder.myEncodedOrderQuantityDuration)});
                                
                var startDate = medicationOrder.myEncodedOrderQuantityStartTimeFormatted;
                notesArray.push({name: "Order Quantity Start Time", note: nullConvert(startDate)});         
                
                var endDate = medicationOrder.myEncodedOrderQuantityEndTimeFormatted;
                notesArray.push({name: "Order Quantity End Time", note: nullConvert(endDate)});         
                
                if ( medicationOrder.myEncodedOrderGiveCode ) {
                    notesArray.push({name: "Order Give Code", note: nullConvert(medicationOrder.myEncodedOrderGiveCode.myCode)});
                    notesArray.push({name: "Order Give Text", note: nullConvert(medicationOrder.myEncodedOrderGiveCode.myText)});                                        
                }
                else {
                    notesArray.push({name: "Order Give Code and Text", note: "NULL"});                     
                }
                
                notesArray.push({name: "Order Give Min", note: nullConvert(medicationOrder.myEncodedOrderGiveMinimum)});
                notesArray.push({name: "Order Give Max", note: nullConvert(medicationOrder.myEncodedOrderGiveMaximum)});
                
                
                if ( medicationOrder.myEncodedOrderGiveUnits ) {
                    notesArray.push({name: "Order Give Units Code", note: nullConvert(medicationOrder.myEncodedOrderGiveUnits.myCode)});
                    notesArray.push({name: "Order Give Units Text", note: nullConvert(medicationOrder.myEncodedOrderGiveUnits.myText)});                                        
                }
                else {
                    notesArray.push({name: "Order Give Units Code and Text", note: "NULL"});                     
                }
                
                
                if ( medicationOrder.myEncodedOrderGiveDosageForm ) {
                    notesArray.push({name: "Order Give Dosage Form Code", note: nullConvert(medicationOrder.myEncodedOrderGiveDosageForm.myCode)});
                    notesArray.push({name: "Order Give Dosage Form Text", note: nullConvert(medicationOrder.myEncodedOrderGiveDosageForm.myText)});                                        
                }
                else {
                    notesArray.push({name: "Order Give Dosage From Code and Text", note: "NULL"});                     
                }
                
                
                
                if ( medicationOrder.myEncodedOrderProvidersAdministrationInstructions && medicationOrder.myEncodedOrderProvidersAdministrationInstructions.length > 0 ) {
                    var noteText = "";
                    for(var index=0; index < medicationOrder.myEncodedOrderProvidersAdministrationInstructions.length; index++)
                    {
                        var instruct = medicationOrder.myEncodedOrderProvidersAdministrationInstructions[index];
                        noteText += "<b>Administration Instruction Code: </b>" + nullConvert(instruct.myCode);
                        noteText += "<br> <b>Administration Instruction Text:  </b>" + nullConvert(instruct.myText);
                        if (index < (medicationOrder.myEncodedOrderProvidersAdministrationInstructions.length - 1)) {
                            noteText += "<br><br>";                                                       
                        }                        
                    }
                    notesArray.push({name: "Order Provider Administration Instructions", note: noteText});
                }
                else {
                    notesArray.push({name: "Order Provider Administration Instructions", note: "NULL"});                     
                }
                
                
                if ( medicationOrder.myNotes && medicationOrder.myNotes.length > 0 ) {
                    var noteText = "";
                    for(var index=0; index < medicationOrder.myNotes.length; index++)                    
                    {   
                        noteText += nullConvert(medicationOrder.myNotes[index].myNoteText);                        
                        if (index < (medicationOrder.myNotes.length - 1)) {                            
                            noteText += "<br>";
                        }                         
                    } 
                    notesArray.push({name: "Medication Order Notes", note: noteText});
                }
                else {
                    notesArray.push({name: "Medication Order Notes", note: "NULL"});                     
                }
                
                
               if ( medicationOrder.myMedicationRoutes && medicationOrder.myMedicationRoutes.length > 0 ) {
                    var noteText = "";
                    for(var index=0; index < medicationOrder.myMedicationRoutes.length; index++)
                    {
                        var route = medicationOrder.myMedicationRoutes[index];
                        noteText += "<b>Route Code: </b>" + nullConvert(route.myCode);
                        noteText += "<br> <b>Route Text:  </b>" + nullConvert(route.myText);
                        if (index < (medicationOrder.myMedicationRoutes.length - 1)) {                                                   
                            noteText += "<br><br>";                                                        
                        }                       
                    }
                    notesArray.push({name: "Medication Routes", note: noteText});
                }
                else {
                    notesArray.push({name: "Medication Routes", note: "NULL"});                     
                }
                
                
               if ( medicationOrder.myMedicationComponents && medicationOrder.myMedicationComponents.length > 0 ) {
                    var noteText = "";
                    for(var index=0; index < medicationOrder.myMedicationComponents.length; index++)
                    {
                        var component = medicationOrder.myMedicationComponents[index];
                        noteText += "<b>Component Type: </b>" + nullConvert(component.myComponentType);
                        if ( component.myComponentCode ) {
                            noteText += "<br> <b>Component Code:  </b>" + nullConvert(component.myComponentCode.myCode);
                            noteText += "<br> <b>Component Text:  </b>" + nullConvert(component.myComponentCode.myText);                            
                        }                     
                        noteText += "<br> <b>Component Amount:  </b>" + nullConvert(component.myComponentAmount);
                        if ( component.myComponentUnits ) {
                            noteText += "<br> <b>Component Units Code:  </b>" + nullConvert(component.myComponentUnits.myCode);
                            noteText += "<br> <b>Component Units Text:  </b>" + nullConvert(component.myComponentUnits.myText);
                        }
                        if (index < (medicationOrder.myMedicationComponents.length - 1)) {                                                   
                            noteText += "<br><br>";                                                        
                        }                        
                    }
                    notesArray.push({name: "Medication Components", note: noteText});
                }
                else {
                    notesArray.push({name: "Medication Components", note: "NULL"});                     
                }
                
                
                
                if ( medicationAdmins && medicationAdmins.length > 0 ) {
                    var noteText = "";
                    for(var index=0; index < medicationAdmins.length; index++)
                    {
                        var admin = medicationAdmins[index];
                        noteText += "<b>Administration Number: </b>" + nullConvert(admin.myAdministrationNumber);                        
                        
                        var startDate = admin.myStartTimeFormatted;
                        noteText += "<br><b>Administration Start Time: </b>" + nullConvert(startDate);                           
                        
                        var endDate = admin.myEndTimeFormatted;
                        noteText += "<br><b>Administration End Time: </b>" + nullConvert(endDate);                          
                        
                        if ( admin.myAdministeredCode ) {
                            noteText += "<br><b>Administerred Code: </b>" + nullConvert(admin.myAdministeredCode.myCode);
                            noteText += "<br><b>Administerred Text: </b>" + nullConvert(admin.myAdministeredCode.myText);
                        }
                        else {
                            noteText += "<br><b>Administerred Code and Text: </b>NULL";
                        }
                        
                        noteText += "<br><b>Administerred Amount: </b>" + nullConvert(admin.myAdministeredAmount);
                        
                        if ( admin.myAdministeredUnits ) {
                            noteText += "<br><b>Administerred Units Code: </b>" + nullConvert(admin.myAdministeredUnits.myCode);
                            noteText += "<br><b>Administerred Units Text: </b>" + nullConvert(admin.myAdministeredUnits.myText);
                        }
                        else {
                            noteText += "<br><b>Administerred Units Code and Text: </b>NULL";
                        }
                        
                        
                        if ( admin.myAdministrationNotes && admin.myAdministrationNotes.length > 0 ) {
                            var adminNote = "";
                            for(var j=0; j < admin.myAdministrationNotes.length; j++)
                            {
                                var note = admin.myAdministrationNotes[j];                                
                                adminNote += "<br>";
                                adminNote += "<b>Admin Note Code: </b>" + nullConvert(note.myCode);
                                adminNote += "<br> <b>Admin Note Text:  </b>" + nullConvert(note.myText);
                                                       
                            }
                            noteText += "<br><b>Administration Notes: </b>" + adminNote;
                        }
                        else {
                            noteText += "<br><b>Administration Notes: </b>NULL";
                        }
                                                
                        noteText += "<br><b>Administerred Per Time Unit: </b>" + nullConvert(admin.myAdministeredPerTimeUnit);
                        
                        if ( admin.myMedicationRoute ) {
                            noteText += "<br><b>Administerred Medication Route Code: </b>" + nullConvert(admin.myMedicationRoute.myCode);
                            noteText += "<br><b>Administerred Medication Route Text: </b>" + nullConvert(admin.myMedicationRoute.myText);
                        }
                        else {
                            noteText += "<br><b>Administerred Medication Route Code and Text: </b>NULL";
                        }
                         
                        
                        if (index < (medicationAdmins.length - 1)) {                                                   
                            noteText += "<br><br>";                                                        
                        }                        
                    }                    
                    notesArray.push({name: "Medication Administrations", note: nullConvert(noteText)});
                }
                else {
                    notesArray.push({name: "Medication Administrations", note: "NULL"});                     
                }
                
                
                
                // Null check for DOB
                var dob;
                if(myPatient.myDateOfBirth)
                	dob = myPatient.myDateOfBirth.replace(/T.*/, "");
                else
                	dob = "NULL";
                	
                //Null check for myAdministrativeSex
                var sex = nullConvert(myPatient.myAdministrativeSex);
                

                // Add some header info as act level notes
                var actNotesArray = new Array();
                var ptDemo = "Last Name: " + myPatient.myPatientNames[0].myLastName + ", First Name: " + myPatient.myPatientNames[0].myFirstName + "<br>" +
                "DOB: " + dob + ", Gender: " + sex;
                 actNotesArray.push({name: "Patient", note: ptDemo});
                
         		var effectiveTime;
    			if(medicationOrder.myEncodedOrderQuantityStartTimeFormatted)
    			{
    				effectiveTime =  medicationOrder.myEncodedOrderQuantityStartTimeFormatted;
    			}	
    			else{
    				effectiveTime = doc.document.myRecordUpdatedDateFormatted;
    			}
    			
                
                // Forming loaded act object
                var act = {pid: null,
                        id: medicationOrderKey, 
                        name: medicationOrder.myEncodedOrderGiveCode.myText,
                        fullyLoaded: true,                        
                        availabilityTime: doc.document.myRecordUpdatedDateFormatted,
                        effectiveTime: effectiveTime,
                        statusCode: {id: "COMPLETE", name: "Complete"},
                        statusCodes: [{id: "COMPLETE", name: "Complete"}],
                        departmentCode: {},
                        procCodes: [{}],
                        notes: actNotesArray,
                        observations: [{valueType:"ST", notes:notesArray}],
                        procCode:null,
                        visitPid:null,
                        resulted: true};

                emit(medicationOrderKey, act);           
                
            
            
    } else if (doc.document.myType.toString() == "DOCUMENT") {

        /** ********************************************* */
        /** EMIT DOCUMENTS */
        /** ********************************************* */
    
        if (!(doc.document.mySections)) {
            return;
        }
        
        // Loop through each section and create one act entry for each
        for (var actNumber in doc.document.mySections)
        {
            var observationsFromDocument = doc.document.mySections[actNumber].myData;
    
            // Break out observations
            var observationArray = new Array();
            for(var obsNumber in observationsFromDocument)
            {
                var observation =  observationsFromDocument[obsNumber];
                
                var statusCodeObj;
                
                switch(observation.myDataStatusCode)
                {
                    case "F":
                        statusCodeObj = {id: "FINAL", name: "Final"};
                        break;
                    case "C":
                        statusCodeObj = {id: "CORRECTED", name: "Corrected"};
                        break;
                    case "I":
                        statusCodeObj = {id: "INPROGRESS", name: "Inprogress"};
                        break;
                    case "W":
                        statusCodeObj = {id: "CANCELED", name: "Canceled"};
                        break;
                     default: statusCodeObj = {id: "UNKNOWN", name: "Unknown"};
                      break;
                }
                
                var typeObj = {type: observation.myDataType,
                        numValue: null,
                        textValue: null};
                
                var invalidNumericVar = false;
          
                if (typeObj.type === "ED")
				{
					// what for do I need refRange ????
					refRangeType="ST";
					
					var dataType = observation.myEncapsulatedDataMimeType.split("/");
					var typeOfData = dataType[0];
					var dataFormat = dataType[1];
					
					var edKey = ""+doc._id+"_"+actNumber+"_"+obsNumber;
					typeObj.textValue = "else;/cgta-tester-ed-servlet/ed_servlet?key="+edKey;
		
				}
                else
                {
                    typeObj.textValue = observation.myValue;
                    refRangeType="ST";
                    if(typeObj.type.indexOf("<")!= -1)
                    {
                        typeObj.type = "HTML";
                    }
                }
                
                // Break out Observation Level Notes
                var obsNotesArray = new Array();
                var notes = observation.myNotes;
                for (var obsNoteIdx in notes) {
                    var nextNote = notes[obsNoteIdx];
                    obsNotesArray.push(
                            {
                                name: nullConvert(nextNote.myName),
                                // TODO is this the right name?
                                note: nullConvert(nextNote.myNoteText)
                            });     
                }
    
                var unitsValue = "";
                if (observation.myUnits) {
                    unitsValue = observation.myUnits.myText;
                }
                
                if (typeObj.type === "ED")
                {
                	var time ="";
                	
                	if(observation.myDateFormatted)
                		time += nullConvert(observation.myDateFormatted);
                	else
                		time += nullConvert(doc.document.myRecordUpdatedDateFormatted);
                	
                	
                	observationArray.push({name: "Look for ED below",valueType: "NM"})
	                observationArray.push(
	                        {
	                            name: observation.myCode.myText,
	                            availabilityTime: time,
	                            valueType: observation.myDataType,
	                            numValue: typeObj.numValue,
	                            textValue: typeObj.textValue,
	                            units: unitsValue,
	                            refRangeType: typeObj.type,
	                            refRange: observation.myRefRange,
	                            statusCode: statusCodeObj,
	                            statusCodes: [statusCodeObj],
	                            
	                            interpretation: {
	                                id: observation.myAbnormalFlagName
	                            },
	                            interpretationCodes: [{
	                                id: observation.myAbnormalFlagCode
	                            }],
	                            notes: obsNotesArray,
	                            invalidNumeric: invalidNumericVar
	                        }
	                    );
                }
                else
                {
                	var tempObsNotes = new Array();
                		
                	var observationName = "Name not supplied";
                	if (observation.myCode)
                	{
                		if(observation.myCode.myText)
                			observationName = nullConvert(observation.myCode.myText);
                		else if(observation.myCode.myCode)
                			observationName = nullConvert(observation.myCode.myCode);
                	}
                	
                	
                	var observationText = "";
                	
                	
                	var obsDateTime;
        			if(observation.myDateTimeOfObservation)
        			{
        				obsDateTime = nullConvert(observation.myDateTimeOfObservation);
        			}	
        			else
        				obsDateTime = doc.document.myRecordUpdatedDateFormatted;
                	
        			//DateTimeOfObservation
        			observationText += "<b>Last Update Time: </b>"+obsDateTime.replace(/T.*/, "").slice(0,10)+"<br>";

                	// VALUE
                	observationText += "<b>Value: </b>"+nullConvert(observation.myValue)+"<br>";
                	
                	// UNITS
                	if(observation.myUnits)
                	{
                		observationText += "<b>Units Text: </b>"+nullConvert(observation.myUnits.myText)+"<br>";
                		observationText += "<b>Units Code: </b>"+nullConvert(observation.myUnits.myCode)+"<br>";
                	}
                	else
                		observationText += "<b>Units: </b>NULL<br>";
                	
                	// REF RANGE
                	observationText += "<b>Ref Range: </b>"+nullConvert(observation.myRefRange)+"<br>";
                	
                	// myAbnormalFlagName
                	observationText += "<b>Abnormal Flag Name: </b>"+nullConvert(observation.myAbnormalFlagName)+"<br>";
                	
                	//myAbnormalFlagCode
                	observationText += "<b>Abnormal Flag Code: </b>"+nullConvert(observation.myAbnormalFlagCode)+"<br>";
              
              	
                	//myDataStatus
                	if(observation.myDataStatus)
                		observationText += "<b>Data Status: </b>"+nullConvert(observation.myDataStatus)+"<br>";
                	else
                		observationText += "<b>Data Status: </b>NULL<br>";
                	
                	// Observation has notes
                	if(obsNotesArray.length > 0)
                	{
                		for(var obsN in obsNotesArray)
                		{
                			var note = obsNotesArray[obsN];
                			observationText +=  "<b>"+note.name+": </b> "+note.note+"<br>"
                		}
                	}
                	
                	pushNoteObj(observationName, observationText,tempObsNotes);
                	
                	observationArray.push({valueType:"ST", notes:tempObsNotes} );
                }
                
            }
            
            	var myPatient = doc.document.myPatient;
                // Null check for DOB
                var dob;
                if(myPatient.myDateOfBirth)
                	dob = myPatient.myDateOfBirth.replace(/T.*/, "");
                else
                	dob = "NULL";
                	
                //Null check for myAdministrativeSex                	
                var sex = nullConvert(myPatient.myAdministrativeSex);
                
                // Add some header info as act level notes
                var actNotesArray = new Array();
                var ptDemo = "Last Name: " + myPatient.myPatientNames[0].myLastName + ", First Name: " + myPatient.myPatientNames[0].myFirstName + "<br>" +
                "DOB: " + dob + ", Gender: " + sex;
                 actNotesArray.push({name: "Patient", note: ptDemo});               
   
                       
            // Break out Act Level Notes
            var actNotes = doc.document.mySections[actNumber].myNotes;
            for (var actNoteIdx in actNotes) {
                var nextNote = actNotes[actNoteIdx];
                actNotesArray.push(
                        {
                            name: nullConvert(nextNote.myName),
                            // TODO is this the right name?
                            note: nullConvert(nextNote.myNoteText)
                        });         
            }
            
            var documentKey = "DOCUMENT___" + doc.document.mySections[actNumber].mySectionId.myFacilityId + 
                              "___" + doc.document.mySections[actNumber].mySectionId.mySystemId + 
                              "___" + doc.document.mySections[actNumber].mySectionId.myId;
    
            
        	var effectiveTime;
			if(doc.document.mySections[actNumber].myDateFormatted)
			{
				effectiveTime = doc.document.mySections[actNumber].myDateFormatted;
			}	
			else
				effectiveTime = doc.document.myRecordUpdatedDateFormatted;
            
			  // Forming loaded act object
            var act = {pid: null,
                        id: documentKey, 
                        name: doc.document.mySections[actNumber].mySectionName,
                        fullyLoaded: false,
                        availabilityTime: doc.document.myRecordUpdatedDateFormatted,
                        effectiveTime: effectiveTime,
                        statusCode: {id: "COMPLETE", name: "Complete"},
                        statusCodes: [{id: "COMPLETE", name: "Complete"}],
                        departmentCode: {},
                        procCodes: [{}],
                        notes: actNotesArray,
                        observations: observationArray,
                        procCode:{pid:8888888,id: "XZ",name:"X Z",domainId:"Some",conceptType:"someCode"},
                        visitPid:9999999,
                        resulted: true};
    
            emit( documentKey, act);    
    
         } 

    }

}