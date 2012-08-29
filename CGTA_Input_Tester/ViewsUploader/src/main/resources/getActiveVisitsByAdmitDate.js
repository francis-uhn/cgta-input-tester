function (doc)
{
	if (doc.document.myType.toString() == "PATIENT_WITH_VISITS") {
		
		
		var visits = doc.document.myVisits;
		
		if ( visits == null || visits.length == 0) {
			return;
		}
		
				
		for (var i = 0; i < visits.length; i++) {
			if( visits[i].myVisitStatus == "ACTIVE") {				
				emit(visits[i].myAdmitDate,doc);
			}
		}
		
		
	}
	
}