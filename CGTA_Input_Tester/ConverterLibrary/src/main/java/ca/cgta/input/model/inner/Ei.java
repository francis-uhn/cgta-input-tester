package ca.cgta.input.model.inner;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

public class Ei {

	public String myId;
	public String myFacilityId;
	public String myFacilityName;
	public String mySystemId;

	@JsonIgnore
	public boolean isValid() {
	    return StringUtils.isNotBlank(myId) && StringUtils.isNotBlank(myFacilityId) && StringUtils.isNotBlank(mySystemId);
    }
	
	public void setValid(boolean theValid) {
		// ignore this, it's just needed to allow CouchDB to deserialize
	}

	/**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object theObj) {
    	if (!(theObj instanceof Ei)) {
    		return false;
    	}
    	
    	Ei ei = (Ei)theObj;
    	return ObjectUtils.equals(myId, ei.myId) && ObjectUtils.equals(myFacilityId, ei.myFacilityId) && ObjectUtils.equals(mySystemId, ei.mySystemId);
    }

	@JsonIgnore
	public String toKey() {
		if (myFacilityId == null) {
			throw new IllegalStateException("Missing facility ID");
		}
		if (mySystemId == null) {
			throw new IllegalStateException("Missing System ID");
		}
		if (myId == null) {
			throw new IllegalStateException("Missing ID number");
		}
		return myFacilityId + "___" + mySystemId + "___" + myId;
	}
	
	@JsonIgnore
	public static Ei fromKey(String theString) {
		String[] parts = theString.split("___");
		
		Ei retVal = new Ei();
		retVal.myFacilityId = parts[0];
		retVal.mySystemId = parts[1];
		retVal.myId = parts[2];
		
		return retVal;
	}
}
