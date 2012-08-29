package ca.cgta.input.val.shared.results;

import java.io.Serializable;

public class FailureCodeDetails implements Serializable {

    private static final long serialVersionUID = 1L;
    
	private String myCode;
	private String myDescription;
	private String myHowToResolve;

	private String mySeverity;

	/**
	 * @return the code
	 */
	public String getCode() {
		return myCode;
	}


	/**
	 * @return the description
	 */
	public String getDescription() {
		return myDescription;
	}


	/**
     * @return the howToResolve
     */
    public String getHowToResolve() {
    	return myHowToResolve;
    }


	/**
	 * @param theCode
	 *            the code to set
	 */
	public void setCode(String theCode) {
		myCode = theCode;
	}


	/**
	 * @param theDescription
	 *            the description to set
	 */
	public void setDescription(String theDescription) {
		myDescription = theDescription;
	}


	/**
     * @param theHowToResolve the howToResolve to set
     */
    public void setHowToResolve(String theHowToResolve) {
    	myHowToResolve = theHowToResolve;
    }


	public void setSeverity(String theName) {
		mySeverity = theName;
    }


	/**
     * @return the severity
     */
    public String getSeverity() {
    	return mySeverity;
    }


	public static String getSeverityDescription(String theSeverity) {
		if ("LOW".equals(theSeverity)) {
			return "Low";
		}
		if ("HIGH".equals(theSeverity)) {
			return "High";
		}
		if ("MED".equals(theSeverity)) {
			return "Medium";
		}
	    return theSeverity;
    }
	
}
