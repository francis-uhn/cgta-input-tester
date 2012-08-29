package ca.cgta.input.val.client.rpc;

import java.util.List;

import ca.cgta.input.val.shared.results.FailureCodeDetails;
import ca.cgta.input.val.shared.results.OidLibrary;
import ca.cgta.input.val.shared.results.ValidationResult;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("val")
public interface ValidatorService extends RemoteService {
  
	ValidationResult validate(String theMessage);

	List<FailureCodeDetails> failureCodes();
	
	OidLibrary loadOidLibrary(String theHspId) throws Exception;
	
}
