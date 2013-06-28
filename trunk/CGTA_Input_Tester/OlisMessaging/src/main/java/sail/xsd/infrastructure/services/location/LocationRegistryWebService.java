package sail.xsd.infrastructure.services.location;

public class LocationRegistryWebService {

	public LookupFacilityResponse lookupFacility(LookupFacilityRequest theRequest) {
		Id id = theRequest.getId();
		
		LookupFacilityResponse response = new LookupFacilityResponse();
		response.setMappedFacility(new Facility());
		response.setFacility(response.getMappedFacility());
		
		response.getFacility().setId(id);
		Address addr = new Address();
		
		response.getFacility().setAddress(addr);
		
		return response;
    }

}
