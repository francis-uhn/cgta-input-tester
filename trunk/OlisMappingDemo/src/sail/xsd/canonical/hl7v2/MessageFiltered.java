package sail.xsd.canonical.hl7v2;

public class MessageFiltered {

	private boolean myIsFiltered;
	private String myMessage;


	/**
     * @return the message
     */
    public String getMessage() {
    	return myMessage;
    }


	public boolean isIsFiltered() {
		return myIsFiltered;
	}


	/**
	 * @param theIsFiltered
	 *            the isFiltered to set
	 */
	public void setIsFiltered(boolean theIsFiltered) {
		myIsFiltered = theIsFiltered;
	}


	public void setMessage(String theDescription) {
		myMessage = theDescription;
    }

}
