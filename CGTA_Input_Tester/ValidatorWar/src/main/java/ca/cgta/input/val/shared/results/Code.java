package ca.cgta.input.val.shared.results;

import java.io.Serializable;
import java.util.HashMap;

public class Code implements Serializable {

	private static final long serialVersionUID = 1L;

	private String myCode;
	private String myDescription;
	private String myHspId;
	private String mySendingSystem;
	private String myUsage;
	private HashMap<String, String> myMetadata;

	public Code() {
	}


	public Code(String theCode, String theDescription) {
		myCode = theCode;
		myDescription = theDescription;
	}


	public Code(String theCode, String theDescription, String theHspId) {
		myCode = theCode;
		myDescription = theDescription;
		myHspId = theHspId;
    }


	/**
     * @return the metadata
     */
    public HashMap<String, String> getMetadata() {
    	if (myMetadata == null) {
    		myMetadata = new HashMap<String, String>();
    	}
    	return myMetadata;
    }


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
     * @return the hspId
     */
    public String getHspId() {
    	return myHspId;
    }


	/**
	 * @return the sendingSystem
	 */
	public String getSendingSystem() {
		return mySendingSystem;
	}


	/**
	 * @return the usage
	 */
	public String getUsage() {
		return myUsage;
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
     * @param theHspId the hspId to set
     */
    public void setHspId(String theHspId) {
    	myHspId = theHspId;
    }


	/**
	 * @param theSendingSystem
	 *            the sendingSystem to set
	 */
	public void setSendingSystem(String theSendingSystem) {
		mySendingSystem = theSendingSystem;
	}


	/**
	 * @param theUsage
	 *            the usage to set
	 */
	public void setUsage(String theUsage) {
		myUsage = theUsage;
	}

}
