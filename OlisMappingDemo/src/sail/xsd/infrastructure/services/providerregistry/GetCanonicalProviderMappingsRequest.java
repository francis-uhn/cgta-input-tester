package sail.xsd.infrastructure.services.providerregistry;

public class GetCanonicalProviderMappingsRequest {

	private String myIdAuthority;
	private String myIdExt;

	public void setIdAuthority(String theAuthority) {
		myIdAuthority = theAuthority;
    }

	public void setIdExt(String theExtension) {
		myIdExt = theExtension;
	    
    }

	/**
     * @return the idAuthority
     */
    public String getIdAuthority() {
    	return myIdAuthority;
    }

	/**
     * @return the idExt
     */
    public String getIdExt() {
    	return myIdExt;
    }

}
