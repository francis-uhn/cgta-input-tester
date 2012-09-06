function(doc) {
	

	var mrnKey;
	var siteId;
	var systemId;
	if (doc.document.myType.toString() == "DOCUMENT"|| doc.document.myType.toString() == "PATIENT_WITH_VISITS" || doc.document.myType.toString() == "MEDICATION_ORDER") {

		
		var patientsArray = null;
        
        if (doc.document.myType.toString() == "MEDICATION_ORDER") {
            patientsArray = doc.document.myOrder.myPatient.myPatientIds;
        } else {
            patientsArray = doc.document.myPatient.myPatientIds;
        }       
        
		
		for (var pNumber in patientsArray) {
			// Medical Record Number
			var patient = patientsArray[pNumber];
			if(patientsArray[pNumber].myIdTypeCode==="MR") {
				mrnKey = patient.myIdNumber;
				siteId = patient.myAssigningAuthorityHspId;
				systemId = patient.myAssigningAuthoritySystemId
			}
		}
	}

	// Patient with visits (each PRO row will be one visit)
	if (doc.document.myType == "PATIENT_WITH_VISITS") {
		
//		// skip visit without myVisits or with out mergeVisits
//		if (!(doc.document.myVisits)) {
//			if(!(doc.document.myMergedInPatientsWithVisits)) {
//				return;
//			}
//		}
		
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
				
				var formattedTime;
				if( visit.myAdmitDateFormatted)
					formattedTime = visit.myAdmitDateFormatted;
				else
					formattedTime = visit.myRecordUpdatedDateFormatted;
				
				// Forming unloaded act object
				var act = {pid: null,
							id: visitKey, 
							name: visitName,
							fullyLoaded: false,
							availabilityTime: formattedTime,
							effectiveTime: formattedTime,							
							statusCode: {id: "COMPLETE", name: "Complete"},
                            statusCodes: [{id: "COMPLETE", name: "Complete"}],
							departmentCode: {},
							procCodes: [{}],
							notes:[],
							observations: [],
							procCode:null,
							visitPid:null,
							resulted: true};
				
				var complexKey = [mrnKey, formattedTime.slice(0,10), "F"];
				emit(complexKey, act);
			}
		}
		
		
		
        function createAllergyAssessment(patient)
        {
                
            var key = "ALLERGY_ASMT___" + siteId + 
                               "___" + systemId + 
                               "___" + mrnKey;
                               
            var allergyAssName = "Allergy Assessment (" + mrnKey + ")";                   
            
            
            // Forming unloaded act object
            var act = {pid: null,
                        id: key, 
                        name: allergyAssName,
                        fullyLoaded: false,
                        availabilityTime: patient.myRecordUpdatedDateFormatted,
                        effectiveTime: patient.myRecordUpdatedDateFormatted,                            
                        statusCode: {id: "COMPLETE", name: "Complete"},
                        statusCodes: [{id: "COMPLETE", name: "Complete"}],
                        departmentCode: {},
                        procCodes: [{}],
                        notes:[],
                        observations: [],
                        procCode:null,
                        visitPid:null,
                        resulted: true};
            
            var complexKey = [mrnKey, patient.myRecordUpdatedDateFormatted.slice(0,10), "F"];
            emit(complexKey, act);                
                
        }   
        
        
        
        function createPatient(patient)
        {                
           var key = "PATIENT___" + siteId + 
                               "___" + systemId + 
                               "___" + mrnKey;
                               
           var patientDemoName = "Patient Demographics (" + mrnKey + ")";
           
            
            // Forming loaded act object
            var act = {pid: null,
                    id: key, 
                    name: patientDemoName,
                    fullyLoaded: false,
                    availabilityTime: patient.myRecordUpdatedDateFormatted,
                    effectiveTime: patient.myRecordUpdatedDateFormatted,
                    statusCode: {id: "COMPLETE", name: "Complete"},
                    statusCodes: [{id: "COMPLETE", name: "Complete"}],
                    departmentCode: {},
                    procCodes: [{}],
                    notes: [{}],
                    observations: [],
                    procCode:null,
                    visitPid:null,
                    resulted: true};

            var complexKey = [mrnKey, patient.myRecordUpdatedDateFormatted.slice(0,10), "F"];
            emit(complexKey, act);
                
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
            createAllergyAssessment(doc.document.myPatient);         
        }        
		
		if (doc.document.myVisits) {
			iterateVisits(doc.document.myVisits);
		}
		
		if(doc.document.myMergedInPatientsWithVisits) {
			iterateMergedVisits(doc.document.myMergedInPatientsWithVisits);
		}
		

	}	
	else if (doc.document.myType.toString() == "DOCUMENT") {
	
		
		var observationsFromDocument = doc.document.mySections;
		var observationArray = new Array();
		
		for (var actNumber in doc.document.mySections)
		{
			
			var documentKey = "DOCUMENT___" + doc.document.mySections[actNumber].mySectionId.myFacilityId + 
					       "___" + doc.document.mySections[actNumber].mySectionId.mySystemId + 
				           "___" + doc.document.mySections[actNumber].mySectionId.myId;
	
			// Forming unloaded act object
		
			
			var formattedTime;
			if(doc.document.mySections[actNumber].myDateFormatted)
			{
				formattedTime = doc.document.mySections[actNumber].myDateFormatted;
			}	
			else
				formattedTime = doc.document.myRecordUpdatedDateFormatted;
			
			var act = {pid: null,
						id: documentKey, 
						name: doc.document.mySections[actNumber].mySectionName,
						fullyLoaded: false,
						availabilityTime: doc.document.myRecordUpdatedDateFormatted,
						effectiveTime: formattedTime,						
						statusCode: {id: "COMPLETE", name: "Complete"},
                        statusCodes: [{id: "COMPLETE", name: "Complete"}],
						departmentCode: {},
						procCodes: [{}],
						notes:[],
						observations: [],
						procCode:null,
						visitPid:null,
						resulted: true};
			
			// Check if myStatusCode exist in the section
			var statusCode;		
			if (doc.document.mySections[actNumber].myStatusCode)
				statusCode = doc.document.mySections[actNumber].myStatusCode;
			else
				statusCode = "X";
				
			var complexKey = [mrnKey, formattedTime.slice(0,10), statusCode];	
			emit( complexKey, act);
		}
	}	
	else if (doc.document.myType.toString() == "MEDICATION_ORDER") {

            var medicationOrderWadmins = doc.document;
            var medicationOrder = medicationOrderWadmins.myOrder;
            
            var medicationOrderKey = "MEDICATION_ORDER___" + medicationOrder.myPlacerOrderNumber.myFacilityId +                
                               "___" + medicationOrder.myPlacerOrderNumber.mySystemId + 
                               "___" + medicationOrder.myPlacerOrderNumber.myId;
            
            var medicationOrderName = medicationOrder.myEncodedOrderGiveCode.myCode + " Medication Order (" + medicationOrder.myPlacerOrderNumber.myId + ")";

    		var formattedTime;
			if(medicationOrder.myEncodedOrderQuantityStartTimeFormatted)
			{
				formattedTime =  medicationOrder.myEncodedOrderQuantityStartTimeFormatted;
			}	
			else
				formattedTime = doc.document.myRecordUpdatedDateFormatted;
			
            // Forming unloaded act object
            var act = {pid: null,
                    id: medicationOrderKey, 
                    name: medicationOrderName,
                    fullyLoaded: false,                        
                    availabilityTime: formattedTime,
                    effectiveTime: formattedTime,
                    statusCode: {id: "COMPLETE", name: "Complete"},
                    statusCodes: [{id: "COMPLETE", name: "Complete"}],
                    departmentCode: {},
                    procCodes: [{}],
                    notes: [],
                    observations: [],
                    procCode:null,
                    visitPid:null,
                    resulted: true};

            var complexKey = [mrnKey, formattedTime.slice(0,10), "F"];
            emit(complexKey, act);
       
        
            
    }
	
	
}