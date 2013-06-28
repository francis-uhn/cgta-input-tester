package sail.xsd.canonical.hl7v2;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CanonicalHl7V2Message extends AbstractCanonicalMessage {

	private String myEnvironment;
	private String myRawMessage;
	private OutboundInterface myOutboundInterface;
	private Destination myDestination;
	private String myMessageControlID;
	private ArrayList<IntermediateMessageEntry> myIntermediateMessage;


	public CanonicalHl7V2Message() {
	}


	/**
	 * 
	 * @param theMessage
	 *            The raw HL7 message (ER7 encoded)
	 * @param theEnvironment
	 *            Should be "PROD" for production or "DEV" any other time
	 * @param theMessageControlID
	 *            The MSH-10 value
	 */
	public CanonicalHl7V2Message(String theMessage, String theEnvironment, String theMessageControlID) {
		myRawMessage = theMessage;
		myEnvironment = theEnvironment;
		myMessageControlID = theMessageControlID;
	}


	public List<Identifier> getAlternateIDs() {
		return new ArrayList<Identifier>();
	}


	public String getEnvironment() {
		return myEnvironment;
	}


	public List<MessagePathEntry> getMessagePath() {
		return new ArrayList<MessagePathEntry>();
	}


	public String getRawMessage() {
		return myRawMessage;
	}


	/**
	 * @param theRawMessage
	 *            the rawMessage to set
	 */
	public void setRawMessage(String theRawMessage) {
		myRawMessage = theRawMessage;
	}


	public OutboundInterface getDestination() {
		return myOutboundInterface;
	}


	public void setDestination(OutboundInterface theOutboundInterface) {
		myOutboundInterface = theOutboundInterface;
	}


	/**
	 * @param theOutboundInterface
	 *            the outboundInterface to set
	 */
	public void setOutboundInterface(OutboundInterface theOutboundInterface) {
		myOutboundInterface = theOutboundInterface;
	}


	public List<IntermediateMessageEntry> getIntermediateMessage() {
		if (myIntermediateMessage == null) {
			myIntermediateMessage = new ArrayList<IntermediateMessageEntry>();
		}
		return myIntermediateMessage;
	}


	public String getMessageControlID() {
		return myMessageControlID;
	}

}
