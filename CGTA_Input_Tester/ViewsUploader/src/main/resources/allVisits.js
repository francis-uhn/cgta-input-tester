function(doc)
{
	if (doc.document.myType.toString() == "PATIENT_WITH_VISITS") {
	/************************************************/
	/** EMIT VISITS                                 */
	/************************************************/
		emit(null, doc);
		 
	}
}