package sail.xsd.infrastructure.services.location;

public class LookupFacilityResponse {

	private Facility myMappedFacility;
	private Facility myFacility;

	public Facility getMappedFacility() {
		return myMappedFacility;
    }

	/**
     * @param theMappedFacility the mappedFacility to set
     */
    public void setMappedFacility(Facility theMappedFacility) {
    	myMappedFacility = theMappedFacility;
    }

	public Facility getFacility() {
	    return myFacility;
    }

	public void setFacility(Facility theFacility) {
	    myFacility = theFacility;
    }

}
