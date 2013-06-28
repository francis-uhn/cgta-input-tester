package ca.uhn.sail.integration;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.StringUtils;
import org.hl7.cts.types.GetCodeSystemsByIdRequest;
import org.hl7.cts.types.GetCodeSystemsByIdResponse;

import sail.wsdl.infrastructure.location.SecurityFailureException;
import sail.wsdl.infrastructure.terminology.InvalidInputException;
import sail.wsdl.infrastructure.terminology.TerminologyWebService;
import sail.wsdl.infrastructure.terminology.UnexpectedErrorException;
import sail.xsd.canonical.hl7v2.AbstractCanonicalMessage;
import sail.xsd.canonical.hl7v2.CanonicalHl7V2Message;
import sail.xsd.canonical.hl7v2.MessageErrored;
import sail.xsd.canonical.hl7v2.MessagePathEntry;
import sail.xsd.canonical.hl7v2.MessagePhase;
import sail.xsd.infrastructure.services.location.Facility;
import sail.xsd.infrastructure.services.location.LookupFacilityRequest;
import sail.xsd.infrastructure.services.location.LookupFacilityResponse;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v251.message.ACK;

/**
 * Contains utility methods for dealing with canonical messages
 */
public class CanonicalMessageUtils {

    private static final Logger logger = Logger.getLogger(CanonicalMessageUtils.class.getName());

    /** Non instantiable */
    private CanonicalMessageUtils() {
        // nothing
    }


    /**
     * Accepts an input string, and returns a canonical message object which has
     * been produced by parsing the input string.
     * 
     * @throws JAXBException
     *             If the message can not be parsed for any reason
     */
    public static CanonicalHl7V2Message unmarshallCanonicalHl7V2Message(String theInput) throws JAXBException {
        Source source = new StreamSource(new StringReader(theInput));
        JAXBContext canonicalHl7V2MessageContext = AbstractIntegration.getCanonicalMessageContext();
        Unmarshaller unmarshaller = canonicalHl7V2MessageContext.createUnmarshaller();
        JAXBElement<CanonicalHl7V2Message> retVal = unmarshaller.unmarshal(source, CanonicalHl7V2Message.class);
        return retVal.getValue();
    }




    /**
     * Accepts a canonical message object, and marshalls it into an XML string
     * representation
     * 
     * @throws JAXBException
     *             If the message can not be parsed for any reason
     */
    public static String marshallCanonicalHl7V2Message(CanonicalHl7V2Message theInput) throws JAXBException {
        JAXBContext canonicalHl7V2MessageContext = AbstractIntegration.getCanonicalMessageContext();
        Marshaller unmarshaller = canonicalHl7V2MessageContext.createMarshaller();
        unmarshaller.setProperty(Marshaller.JAXB_ENCODING, "US-ASCII");

        Writer stringWriter = new StringWriter();
        unmarshaller.marshal(theInput, stringWriter);
        String retVal = stringWriter.toString();

        retVal = retVal.replace("\r", "&#13;");
        retVal = retVal.replace("\n", "&#10;");

        return retVal;
    }


    /**
     * Adds a new entry to the message path (the chain of processors listed as
     * having processed a particular message) for a canonical message
     * 
     * @param theInput
     *            The canonical message
     * @param theComponent
     *            The component currently processing. Typically will be set to
     *            <code>this</code>
     * @param theMessagePhase
     *            The current message phases associated with processing
     */
    public static void startNewMessagePathEntry(CanonicalHl7V2Message theInput, Class<?> theComponent, MessagePhase theMessagePhase) {
        MessagePathEntry messagePathEntry = new MessagePathEntry();
        messagePathEntry.setComponentName(theComponent.getName());
        messagePathEntry.setStartTimestamp(System.currentTimeMillis());
        messagePathEntry.setPhase(theMessagePhase);
        theInput.getMessagePath().add(messagePathEntry);
    }


    /**
     * Completes the final message path entry associated with a canonical
     * message by assigning it an end timestamp.
     * 
     * @param theInput
     *            The canonical message
     */
    public static void completeCurrentMessagePathEntry(CanonicalHl7V2Message theInput) {
        MessagePathEntry messagePathEntry = theInput.getMessagePath().get(theInput.getMessagePath().size() - 1);
        messagePathEntry.setEndTimestamp(System.currentTimeMillis());
    }


    /**
     * Returns <code>true</code> if the message is <b>not</b> marked as being
     * skipped (meaning that it was filtered within the ESB) or errored (meaning
     * that some component in the ESB was unable to process it)
     * 
     * @param theMessage
     *            The canonical message
     */
    public static boolean isMessageOkToTransmit(AbstractCanonicalMessage theMessage) {
        if (theMessage.getMessageErrored() != null && theMessage.getMessageErrored().isIsErrored()) {
            return false;
        }
        if (theMessage.getMessageFiltered() != null && theMessage.getMessageFiltered().isIsFiltered()) {
            return false;
        }
        return true;
    }


    /**
     * Creates a {@link MessageErrored} object describing an invalid message
     * (meaning a message containing data that is not valid for processing) for
     * use in adding to a canonical message object within the ESB.
     * 
     * @param theSourceComponent
     *            The component which should be marked as the error's source
     * @param theMessage
     *            The text description of the problem
     * @param theSegment
     *            The segment responsible for the problem (e.g. "PID")
     * @param theFieldNum
     *            The field index responsible, or <code>null</code> if none
     * @param theComponentNum
     *            The component index responsible, or <code>null</code> if none
     * @return The errored element
     */
    public static MessageErrored createMessageErroredForV2InvalidMessage(Class<?> theSourceComponent, String theMessage, String theSegment, String theFieldNum, String theComponentNum) {
        if (theSourceComponent == null) {
            throw new IllegalArgumentException("Missing source component");
        }
        if (StringUtils.isBlank(theMessage)) {
            throw new IllegalArgumentException("Message is blank");
        }
        if (StringUtils.isBlank(theSegment)) {
            throw new IllegalArgumentException("Segment is blank");
        }

        StringBuilder code = new StringBuilder();
        code.append("INVALID_MESSAGE.V2.");
        code.append(theSegment);
        if (StringUtils.isNotBlank(theFieldNum)) {
            code.append("-");
            code.append(theFieldNum);
        }
        if (StringUtils.isNotBlank(theComponentNum)) {
            code.append("-");
            code.append(theComponentNum);
        }

        MessageErrored retVal = new MessageErrored();
        retVal.setErrorMessage(theMessage);
        retVal.setErrorCode(code.toString());
        retVal.setIsErrored(true);
        retVal.setErrorSource(theSourceComponent.getName());

        return retVal;
    }


    /**
     * Creates a {@link MessageErrored} object for NAK HL7v2 response for
     * use in adding to a canonical message object within the ESB.
     * 
     * @param theSourceComponent
     *            The component which should be marked as the error's source
     * @param theMessage
     *            The text description of the problem
     * @param theSegment
     *            The segment responsible for the problem (e.g. "PID")
     * @param theFieldNum
     *            The field index responsible, or <code>null</code> if none
     * @param theComponentNum
     *            The component index responsible, or <code>null</code> if none
     * @return The errored element
     */
    public static MessageErrored createMessageErroredForV2Nak(Class<?> theSourceComponent, ACK theAckMessage) {
        if (theSourceComponent == null) {
            throw new IllegalArgumentException("Missing source component");
        }
        if (theAckMessage == null) {
            throw new IllegalArgumentException("Missing ACK message");
        }

        StringBuilder code = new StringBuilder();
        code.append("INVALID_MESSAGE.V2");

        MessageErrored retVal = new MessageErrored();
        try {
            retVal.setErrorMessage(theAckMessage.getERR().getErr3_HL7ErrorCode().encode());
        } catch (HL7Exception ex) {
            retVal.setErrorMessage("");
        }
        retVal.setErrorCode("NAK.V2." + theAckMessage.getMSA().getAcknowledgmentCode().getValue());
        retVal.setIsErrored(true);
        retVal.setErrorSource(theSourceComponent.getName());

        return retVal;
    }


    /**
     * Creates a {@link MessageErrored} object describing an incomplete message
     * (meaning that the message was missing data which is required) for use in
     * adding to a canonical message object within the ESB.
     * 
     * @param theSourceComponent
     *            The component which should be marked as the error's source
     * @param theMessage
     *            The text description of the problem
     * @return The errored element
     */
    public static MessageErrored createMessageErroredForV2IncompleteMessage(Class<?> theSourceComponent, String theMessage, String theSegment, String theFieldNum, String theComponentNum) {
        if (theSourceComponent == null) {
            throw new IllegalArgumentException("Missing source component");
        }
        if (StringUtils.isBlank(theMessage)) {
            throw new IllegalArgumentException("Message is blank");
        }
        if (StringUtils.isBlank(theSegment)) {
            throw new IllegalArgumentException("Segment is blank");
        }

        StringBuilder code = new StringBuilder();
        code.append("INCOMPLETE_MESSAGE.V2.");
        code.append(theSegment);
        if (StringUtils.isNotBlank(theFieldNum)) {
            code.append("-");
            code.append(theFieldNum);
        }
        if (StringUtils.isNotBlank(theComponentNum)) {
            code.append("-");
            code.append(theComponentNum);
        }

        MessageErrored retVal = new MessageErrored();
        retVal.setErrorMessage(theMessage);
        retVal.setErrorCode(code.toString());
        retVal.setIsErrored(true);
        retVal.setErrorSource(theSourceComponent.getName());

        return retVal;
    }


    /**
     * Creates a {@link MessageErrored} object describing a mapping error
     * (meaning that a nomenclature/terminology code could not be mapped) for
     * use in adding to a canonical message object within the ESB.
     * 
     * @param theSourceComponent
     *            The component which should be marked as the error's source
     * @param theMessage
     *            The text description of the problem
     * @return The errored element
     */
    public static MessageErrored createMessageErroredForV2MappingError(Class<?> theSourceComponent, String theMessage, String theSegment, String theFieldNum, String theComponentNum) {
        if (theSourceComponent == null) {
            throw new IllegalArgumentException("Missing source component");
        }
        if (StringUtils.isBlank(theMessage)) {
            throw new IllegalArgumentException("Message is blank");
        }
        String string = createMessageErroredForV2MappingErrorCode(theSegment, theFieldNum, theComponentNum);

        MessageErrored retVal = new MessageErrored();
        retVal.setErrorMessage(theMessage);
        retVal.setErrorCode(string);
        retVal.setIsErrored(true);
        retVal.setErrorSource(theSourceComponent.getName());

        return retVal;
    }


    /**
     * Creates a {@link MessageErrored} object describing an error which
     * is of unknown origin (perhaps generated as a result of a generic catch all)
     *
     * @param theSourceComponent
     *            The component which should be marked as the error's source
     * @param theMessage
     *            The text description of the problem
     * @return The errored element
     */
    public static MessageErrored createMessageErroredForUnknownError(Class<?> theSourceComponent, String theMessage) {
        if (theSourceComponent == null) {
            throw new IllegalArgumentException("Missing source component");
        }
        if (StringUtils.isBlank(theMessage)) {
            throw new IllegalArgumentException("Message is blank");
        }

        MessageErrored retVal = new MessageErrored();
        retVal.setErrorMessage(theMessage);
        retVal.setErrorCode("UNKNOWN");
        retVal.setIsErrored(true);
        retVal.setErrorSource(theSourceComponent.getName());

        return retVal;
    }

    /**
     * Creates the error code for
     * {@link #createMessageErroredForV2MappingError(Class, String, String, String, String)}
     */
    public static String createMessageErroredForV2MappingErrorCode(String theSegment, String theFieldNum, String theComponentNum) {
        if (StringUtils.isBlank(theSegment)) {
            throw new IllegalArgumentException("Segment is blank");
        }

        StringBuilder code = new StringBuilder();
        code.append("NM_MAPPING_ERROR.V2.");
        code.append(theSegment);
        if (StringUtils.isNotBlank(theFieldNum)) {
            code.append("-");
            code.append(theFieldNum);
        }
        if (StringUtils.isNotBlank(theComponentNum)) {
            code.append("-");
            code.append(theComponentNum);
        }
        String string = code.toString();
        return string;
    }

    /**
     * Creates the error code for
     * {@link #createMessageErroredForV2MappingError(Class, String, String, String, String)}
     */
    public static String createMessageErroredForV2ProviderMappingErrorMessage(String theIdRoot, String theIdAuth, String theIdExt) {
        StringBuffer buffer = new StringBuffer();

        buffer.append("Unable to map provider with ID[");
        buffer.append(theIdExt);
        buffer.append( "] for site [" );
        buffer.append(theIdAuth);
        buffer.append("]");

        return buffer.toString();
    }

    /**
     * Creates a {@link MessageErrored} object describing an error which
     * is of unknown origin (perhaps generated as a result of a generic catch all)
     *
     * @param theSourceComponent
     *            The component which should be marked as the error's source
     * @param theMessage
     *            The text description of the problem
     * @return The errored element
     */
    public static MessageErrored createMessageErroredForV2ProviderMappingError(Class<?> theSourceComponent,String theSegment, String theFieldNum, String theComponentNum, String theIdRoot, String theIdAuth, String theIdExt ) {
        if (theSourceComponent == null) {
            throw new IllegalArgumentException("Missing source component");
        }

        MessageErrored retVal = new MessageErrored();
        retVal.setErrorCode(createMessageErroredForV2MappingErrorCode(theSegment, theFieldNum, theComponentNum));
        retVal.setErrorMessage(createMessageErroredForV2ProviderMappingErrorMessage(theIdRoot, theIdAuth, theIdExt));
        retVal.setIsErrored(true);
        retVal.setErrorSource(theSourceComponent.getName());

        return retVal;
    }


    /**
     * Creates the error code for
     * {@link #createMessageErroredForV2MappingError(Class, String, String, String, String)}
     */
    public static String createMessageErroredForV2ProviderMappingErrorCode(String theSegment, String theFieldNum, String theComponentNum) {
        if (StringUtils.isBlank(theSegment)) {
            throw new IllegalArgumentException("Segment is blank");
        }

        StringBuilder code = new StringBuilder();
        code.append("PR_MAPPING_ERROR.V2.");
        code.append(theSegment);
        if (StringUtils.isNotBlank(theFieldNum)) {
            code.append("-");
            code.append(theFieldNum);
        }
        if (StringUtils.isNotBlank(theComponentNum)) {
            code.append("-");
            code.append(theComponentNum);
        }
        String string = code.toString();
        return string;
    }



    /**
     * Creates the error message for
     * {@link #createMessageErroredForV2MappingError(Class, String, String, String, String)}
     */
    public static String createMessageErroredForV2MappingErrorMessage(TerminologyWebService theTerminologyWebService, String theCode, String theSourceCodeSystem, String theTargetCodeSystem) throws InvalidInputException, UnexpectedErrorException {
        
        GetCodeSystemsByIdRequest request = new GetCodeSystemsByIdRequest();
        request.getId().add(theSourceCodeSystem);
        request.getId().add(theTargetCodeSystem);
        
        GetCodeSystemsByIdResponse response = theTerminologyWebService.getCodeSystemsById(request);
        
        StringBuilder retVal = new StringBuilder();
        retVal.append("Unable to map CODE[");
        retVal.append(theCode);
        retVal.append("] from CODESYSTEM[");
        retVal.append(theSourceCodeSystem);
        retVal.append(" (");
        retVal.append(response.getCodeSystem().get(0).getName());
        retVal.append("] to CODESYSTEM[");
        retVal.append(theTargetCodeSystem);
        retVal.append(" (");
        retVal.append(response.getCodeSystem().get(1).getName());
        retVal.append("]");
        
        return retVal.toString();
    }


    public static void main(String[] args) throws Exception {

        String in = "MSH|^~\\&|UCD|TRI|SCC|MSH|201106010956||ORM^O01|6605356|P|2.3\rPID|1||3156308||MORRIS^LEONARD||19280121|M|||5 PROUDBANK MILLWAY^^NORTH YORK^^M2L 1P3|||||||T41112000256|3843466776NF^MPV1|1|I|QU5S^4^2||||T137659^TAZKARJI^MOHAMMAD^BACHIR||||||||||T137659^TAZKARJI^MOHAMMAD^BACHIR|||||||||||||||||||||||||||201105251338\rORC|NW|25461979:171535598|||SC||||20110601074500|||T137659^TAZKARJI^MOHAMMAD^BACHIR\rOBR|1|25461979:171535598||CLPL^CHLORIDE PLASMA|||201106010745||||O|||201106010745|772|T137659^TAZKARJI^MOHAMMAD^BACHIR\r";

        CanonicalHl7V2Message canon = new CanonicalHl7V2Message();
        canon.setRawMessage(in);

        String out = marshallCanonicalHl7V2Message(canon);

        System.out.println(out);

    }

}
