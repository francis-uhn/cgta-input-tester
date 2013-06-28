package sail.xsd.infrastructure.services.providerregistry;

public class GetProviderResponse {

	private Provider myProviderIfFound;

	public void setFoundMapping(boolean theB) {
	    // TODO Auto-generated method stub
	    
    }

	public void setProviderIfFound(sail.xsd.infrastructure.services.providerregistry.Provider theProvider) {
	    myProviderIfFound = theProvider;
    }

	public Provider getProviderIfFound() {
	    return myProviderIfFound;
    }

}
