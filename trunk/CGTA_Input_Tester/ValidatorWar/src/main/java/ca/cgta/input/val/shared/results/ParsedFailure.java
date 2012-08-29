package ca.cgta.input.val.shared.results;

import com.google.gwt.user.client.rpc.IsSerializable;


public class ParsedFailure implements IsSerializable {

	private String myFailureCode;
	private String myMessage;
	private String myTerserPath;
	private String myStepsToResolve;
	private String mySeverityCode;

	public ParsedFailure() {
	}
	
	public ParsedFailure(String theTerserPath, String theFailureCode, String theMessage, String theFieldVal, String theStepsToResolve, String theSeverity) {
		super();
		myTerserPath = theTerserPath.replace("(1)", "");
		myMessage = theMessage;
		myFailureCode = theFailureCode;
		myStepsToResolve = theStepsToResolve;
		mySeverityCode = theSeverity;
	}


	/**
     * @return the severityCode
     */
    public String getSeverityDescription() {
    	return FailureCodeDetails.getSeverityDescription(mySeverityCode);
    }

	/**
     * @return the failureCode
     */
    public String getFailureCode() {
    	return myFailureCode;
    }


	/**
     * @return the stepsToResolve
     */
    public String getStepsToResolve() {
    	return myStepsToResolve;
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
