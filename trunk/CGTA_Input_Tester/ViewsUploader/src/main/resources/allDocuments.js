function(doc)
{
	if (doc.document.myType.toString() == "DOCUMENT") {
	/************************************************/
	/**              EMIT DOCUMENTS                 */
	/************************************************/
		emit(null, doc); 
	}
}