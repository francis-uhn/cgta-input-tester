package ca.cgta.input.val.shared.results;

import java.io.Serializable;
import java.util.List;

public class OidLibrary implements Serializable {

    private static final long serialVersionUID = 1L;
    
	private List<Code> myCodeSystems9007;
	private List<Code> myFacilityIdentifiers9005;
	private List<Code> myHspIdentifier9004;
	private List<Code> myOtherOids;

	private List<Code> myProvider9001Oids;

	private List<Code> mySendingSystems9008;


	/**
	 * @return the codeSystems9007
	 */
	public List<Code> getCodeSystems9007() {
		return myCodeSystems9007;
	}


	/**
	 * @return the facilityIdentifiers9005
	 */
	public List<Code> getFacilityIdentifiers9005() {
		return myFacilityIdentifiers9005;
	}


	/**
	 * @return the hspIdentifier9004
	 */
	public List<Code> getHspIdentifier9004() {
		return myHspIdentifier9004;
	}


	/**
     * @return the otherOids
     */
    public List<Code> getOtherOids() {
    	return myOtherOids;
    }


	/**
     * @return the provider9001Oids
     */
    public List<Code> getProvider9001Oids() {
    	return myProvider9001Oids;
    }


	/**
	 * @return the sendingSystems9008
	 */
	public List<Code> getSendingSystems9008() {
		return mySendingSystems9008;
	}


	/**
	 * @param theCodeSystems9007
	 *            the codeSystems9007 to set
	 */
	public void setCodeSystems9007(List<Code> theCodeSystems9007) {
		myCodeSystems9007 = theCodeSystems9007;
	}


	/**
	 * @param theFacilityIdentifiers9005
	 *            the facilityIdentifiers9005 to set
	 */
	public void setFacilityIdentifiers9005(List<Code> theFacilityIdentifiers9005) {
		myFacilityIdentifiers9005 = theFacilityIdentifiers9005;
	}


	/**
	 * @param theHspIdentifier9004
	 *            the hspIdentifier9004 to set
	 */
	public void setHspIdentifier9004(List<Code> theHspIdentifier9004) {
		myHspIdentifier9004 = theHspIdentifier9004;
	}


	public void setOtherOids(List<Code> theOtherOids) {
		myOtherOids = theOtherOids;
    }


	public void setProvider9001Oids(List<Code> theCodes) {
		myProvider9001Oids = theCodes;
    }


	/**
	 * @param theSendingSystems9008
	 *            the sendingSystems9008 to set
	 */
	public void setSendingSystems9008(List<Code> theSendingSystems9008) {
		mySendingSystems9008 = theSendingSystems9008;
	}

}
