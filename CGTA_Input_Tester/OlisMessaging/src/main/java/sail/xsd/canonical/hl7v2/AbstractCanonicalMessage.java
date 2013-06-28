package sail.xsd.canonical.hl7v2;

public class AbstractCanonicalMessage {

	private MessageFiltered myMessageFiltered;
	private MessageErrored myMessageErrored;

	public MessageErrored getMessageErrored() {
	    return myMessageErrored;
    }

	/**
     * @param theMessageFiltered the messageFiltered to set
     */
    public void setMessageFiltered(MessageFiltered theMessageFiltered) {
    	myMessageFiltered = theMessageFiltered;
    }

	/**
     * @param theMessageErrored the messageErrored to set
     */
    public void setMessageErrored(MessageErrored theMessageErrored) {
    	myMessageErrored = theMessageErrored;
    }

	public MessageFiltered getMessageFiltered() {
	    return myMessageFiltered;
    }

}
