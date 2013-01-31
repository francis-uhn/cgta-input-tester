package sail.xsd.canonical.hl7v2;

public class MessageErrored {

	private String myErrorCode;
	private String myErrorMessage;
	private String myErrorSource;
	private boolean myIsErrored;


	/**
	 * @return the errorCode
	 */
	public String getErrorCode() {
		return myErrorCode;
	}


	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return myErrorMessage;
	}


	/**
     * @return the errorSource
     */
    public String getErrorSource() {
    	return myErrorSource;
    }


	public boolean isIsErrored() {
		return myIsErrored;
	}


	public void setErrorCode(String theString) {
		myErrorCode = theString;
	}


	public void setErrorMessage(String theMessage) {
		myErrorMessage = theMessage;
	}


	public void setErrorSource(String theName) {
		myErrorSource = theName;
    }


	/**
	 * @param theIsErrored
	 *            the isErrored to set
	 */
	public void setIsErrored(boolean theIsErrored) {
		myIsErrored = theIsErrored;
	}

}
