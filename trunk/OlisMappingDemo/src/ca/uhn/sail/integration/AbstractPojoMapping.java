package ca.uhn.sail.integration;

import hapi.on.olis.oru_r01.message.ORU_R01;

import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.Source;

import sail.wsdl.infrastructure.terminology.TerminologyWebService;
import sail.xsd.canonical.hl7v2.CanonicalHl7V2Message;
import sail.xsd.canonical.hl7v2.MessageErrored;
import sail.xsd.canonical.hl7v2.MessageFiltered;
import sail.xsd.infrastructure.services.location.LocationRegistryWebService;
import sail.xsd.infrastructure.services.providerregistry.ProviderRegistryWebService;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;

public class AbstractPojoMapping {

	private CanonicalHl7V2Message myCanon;
	protected void forward(CanonicalHl7V2Message theInput) {
		// this doesn't need to do anything execept exist :)
    }
    
	protected void forward(Source theOutput) throws Exception {
		// this doesn't need to do anything execept exist :)
    }

    /**
     * @return the canon
     */
    public CanonicalHl7V2Message getCanonical() {
    	return myCanon;
    }

    
	protected HcvWebService getHcvService() {
	    throw new UnsupportedOperationException();
    }

	protected LocationRegistryWebService getLocationRegistryService() {
	    return new LocationRegistryWebService();
    }

    protected ProviderRegistryWebService getProviderRegistryService() {
	    return new ProviderRegistryWebService();
    }

	protected TerminologyWebService getTerminologyService() {
	    return new TerminologyWebService();
    }
	
	
	protected void marshallIntermediateAndForward(CanonicalHl7V2Message theInput, ORU_R01 theFinalMessage, String theString, String theString2, String theString3) throws HL7Exception {
		myCanon = theInput;
		myCanon.setRawMessage(theFinalMessage.encode());
    }


    protected void sendToDeadLetterAndForward(CanonicalHl7V2Message theInput, MessageErrored theError) {
		myCanon = theInput;
		myCanon.setMessageErrored(theError);
		theError.setIsErrored(true);
    }

	protected void sendToDeadLetterAndForward(CanonicalHl7V2Message theInput, String theCode, String theDescription) {
		myCanon = theInput;
		myCanon.setMessageErrored(new MessageErrored());
		myCanon.getMessageErrored().setIsErrored(true);
		myCanon.getMessageErrored().setErrorCode(theCode);
		myCanon.getMessageErrored().setErrorMessage(theDescription);
    }

	protected void skipAndForward(CanonicalHl7V2Message theInput, String theDescription) {
		myCanon = theInput;
		myCanon.setMessageFiltered(new MessageFiltered());
		myCanon.getMessageFiltered().setIsFiltered(true);
		myCanon.getMessageFiltered().setMessage(theDescription);
    }

	protected boolean skipMessage(CanonicalHl7V2Message theInput) {
	    return theInput.getMessageFiltered() != null || theInput.getMessageErrored() != null;
    }

    protected CanonicalHl7V2Message unmarshallCanonicalHl7V2Message(NormalizedMessage theNormalizedMessage) {
	    return theNormalizedMessage.getCanonicalMessage();
    }

    protected boolean unmarshallHapiMessage(CanonicalHl7V2Message theInput, Message theMessage) throws HL7Exception {
		theMessage.setParser(PipeParser.getInstanceWithNoValidation());
		theMessage.parse(theInput.getRawMessage());
	    return true;
    }

}
