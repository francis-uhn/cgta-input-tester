package sail.xsd.infrastructure.services.providerregistry;

import java.util.HashMap;
import java.util.Map;

public class ProviderRegistryWebService {

	private Map<String, String> myCanonProviderMaps = new HashMap<String, String>();
	
	public ProviderRegistryWebService() {
		myCanonProviderMaps.put("2.16.840.1.113883.3.59.1:4083 99994", "1.3.6.1.4.1.12201.1.2.1.1 1000");
		myCanonProviderMaps.put("2.16.840.1.113883.3.59.1:4083 40900", "1.3.6.1.4.1.12201.1.2.1.1 2000");
	}
	
	// "1.3.6.1.4.1.12201.1.2.1.1"
	
	public GetCanonicalProviderMappingsResponse getCanonicalProviderMappings(GetCanonicalProviderMappingsRequest theRequest) {
		String key = theRequest.getIdAuthority() + " " + theRequest.getIdExt();
	    if (myCanonProviderMaps.containsKey(key)) {
	    	String[] bits = myCanonProviderMaps.get(key).split(" ");
	    	
	    	GetCanonicalProviderMappingsResponse resp = new GetCanonicalProviderMappingsResponse();
	    	Provider provider = new Provider();
	    	provider.setIdRoot(bits[0]);
	    	provider.setIdAuthority("ON");
	    	provider.setIdExtension(bits[1]);
	    	
			resp.getTargetProvider().add(provider);
	    	
	    	return resp;
	    }
	    
	    throw new UnsupportedOperationException(key);
    }

	public GetProviderResponse getProvider(GetProviderRequest theProviderLookup) {
		theProviderLookup.getFindIdRoot();
		
		throw new UnsupportedOperationException();
    }

}
