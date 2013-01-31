package sail.xsd.infrastructure.services.providerregistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GetCanonicalProviderMappingsResponse {

	private List<Provider> myTargetProviders;

	public List<Provider> getTargetProvider() {
		if (myTargetProviders == null) {
			myTargetProviders = new ArrayList<Provider>();
		}
	    return myTargetProviders;
    }

}
