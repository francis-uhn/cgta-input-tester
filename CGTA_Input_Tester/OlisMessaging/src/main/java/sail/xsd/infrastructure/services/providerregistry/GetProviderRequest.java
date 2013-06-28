package sail.xsd.infrastructure.services.providerregistry;

public class GetProviderRequest {

	private String myFindIdAuthority;
	private String myFindIdExt;
	private String myFindIdRoot;
	private ProviderLookupMapTo myMapTo;

	/**
     * @return the findIdAuthority
     */
    public String getFindIdAuthority() {
    	return myFindIdAuthority;
    }

	/**
     * @return the findIdExt
     */
    public String getFindIdExt() {
    	return myFindIdExt;
    }

	/**
     * @return the findIdRoot
     */
    public String getFindIdRoot() {
    	return myFindIdRoot;
    }

	public ProviderLookupMapTo getMapTo() {
	    return myMapTo;
    }


	public void setFindIdAuthority(String theAuthority) {
		myFindIdAuthority = theAuthority;
	}

	public void setFindIdExt(String theExtension) {
	    myFindIdExt = theExtension;
    }

	public void setFindIdRoot(String theRoot) {
		myFindIdRoot = theRoot;
    }

	public void setMapTo(ProviderLookupMapTo theProviderLookupMapTo) {
	    myMapTo = theProviderLookupMapTo;
    }

}
