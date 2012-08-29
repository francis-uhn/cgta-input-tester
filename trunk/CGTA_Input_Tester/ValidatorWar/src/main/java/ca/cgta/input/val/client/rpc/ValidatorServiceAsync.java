package ca.cgta.input.val.client.rpc;

import ca.cgta.input.val.shared.results.FailureCodeDetails;
import ca.cgta.input.val.shared.results.OidLibrary;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public interface ValidatorServiceAsync
{

    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see ca.cgta.input.val.client.rpc.ValidatorService
     */
    void validate( java.lang.String p0, AsyncCallback<ca.cgta.input.val.shared.results.ValidationResult> callback );


    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see ca.cgta.input.val.client.rpc.ValidatorService
     */
    void failureCodes( AsyncCallback<java.util.List<FailureCodeDetails>> callback );


    /**
     * Utility class to get the RPC Async interface from client-side code
     */
    public static final class Util 
    { 
        private static ValidatorServiceAsync instance;

        public static final ValidatorServiceAsync getInstance()
        {
            if ( instance == null )
            {
                instance = (ValidatorServiceAsync) GWT.create( ValidatorService.class );
                ServiceDefTarget target = (ServiceDefTarget) instance;
                target.setServiceEntryPoint( GWT.getModuleBaseURL() + "ValidatorService" );
            }
            return instance;
        }

        private Util()
        {
            // Utility class should not be instanciated
        }
    }


	void loadOidLibrary(String theHspId, AsyncCallback<OidLibrary> callback);
}
