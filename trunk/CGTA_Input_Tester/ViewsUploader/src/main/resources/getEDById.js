//***********************************************************
// Go over all documents and emit ED with KEY: docID + #myData
//***********************************************************
function(doc)
{
	if (doc.document.myType.toString() == "DOCUMENT") {
	/************************************************/
	/**              EMIT DOCUMENTS                 */
	/************************************************/
		if (!(doc.document.mySections)) {
			return;
		}
		
		for (var actNumber in doc.document.mySections)
		{
			var observationsFromDocument = doc.document.mySections[actNumber].myData;
			// Break out observations
			for(var obsNumber in observationsFromDocument)
			{
				var observation =  observationsFromDocument[obsNumber];
				
				// Observation with Embeded Data
				if(observation.myDataType ==="ED")
				{
					var fileName = observation.myEncapsulatedDataMimeType.split("/");
                	if (observation.myCode)
                	{
                		if(observation.myCode.myText)
                			fileName[0] = observation.myCode.myText;
                		else if(observation.myCode.myCode)
                			fileName[0] = observation.myCode.myCode;
                	}
                	
					var key = ""+doc._id+"_"+actNumber+"_"+obsNumber;
					emit(key, { type: observation.myEncapsulatedDataMimeType, 
								name: ""+fileName[0]+"."+fileName[1], 
								base64: observation.myValue});
				}
				else
					continue;
			}
		}
	}
}