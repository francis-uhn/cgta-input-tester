package ca.cgta.input.converter;


public class Failure {

	private FailureCode myFailureCode;
	private String myFieldVal;
	private String myMessage;
	private String myTerserPath;


	public Failure(String theTerserPath, FailureCode theFailureCode, String theMessage, String theFieldVal) {
		super();
		myTerserPath = theTerserPath;
		myMessage = theMessage;
		myFailureCode = theFailureCode;
		myFieldVal = theFieldVal;
	}


	/**
     * @return the failureCode
     */
    public FailureCode getFailureCode() {
    	return myFailureCode;
    }


	/**
     * {@inheritDoc}
     */
    
    @Override
    public String toString() {
	    return "Failure[" + myFailureCode + " at " + myTerserPath + ": " + myMessage + " (field value is " + myFieldVal + ")]";
    }


	/**
     * @return the fieldVal
     */
    public String getFieldVal() {
    	return myFieldVal;
    }


	/**
	 * @return the message
	 */
	public String getMessage() {
		return myMessage;
	}


	/**
	 * @return the terserPath
	 */
	public String getTerserPath() {
		return myTerserPath;
	}

}
