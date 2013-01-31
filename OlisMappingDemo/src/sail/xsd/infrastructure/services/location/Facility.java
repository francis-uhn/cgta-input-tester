package sail.xsd.infrastructure.services.location;

public class Facility {

	private Id myId;
	private Address myAddress;

	public Id getId() {
		return myId;
    }

	/**
     * @param theId the id to set
     */
    public void setId(Id theId) {
    	myId = theId;
    }

	public Address getAddress() {
	    return myAddress;
    }

	/**
     * @param theAddress the address to set
     */
    public void setAddress(Address theAddress) {
    	myAddress = theAddress;
    }

	public String getOrgName() {
	    // TODO Auto-generated method stub
	    return null;
    }

}
