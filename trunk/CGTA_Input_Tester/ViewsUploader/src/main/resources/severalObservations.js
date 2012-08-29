function(doc)
{
	if(doc.document.mySections && doc.document.mySections.length>1)
		emit(null, doc); 
}