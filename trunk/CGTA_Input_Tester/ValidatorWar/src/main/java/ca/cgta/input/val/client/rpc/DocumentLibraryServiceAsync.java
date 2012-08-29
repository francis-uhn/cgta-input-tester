package ca.cgta.input.val.client.rpc;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public interface DocumentLibraryServiceAsync
{

    /**
     * GWT-RPC service  asynchronous (client-side) interface
     * @see ca.cgta.input.val.client.rpc.DocumentLibraryService
     */
    void getLibraryTree( AsyncCallback<ca.cgta.input.val.client.rpc.DocumentLibraryService.LibraryCategory> callback );


    /**
     * Utility class to get the RPC Async interface from client-side code
     */
    public static final class Util 
    { 
        private static DocumentLibraryServiceAsync instance;

        public static final DocumentLibraryServiceAsync getInstance()
        {
            if ( instance == null )
            {
                instance = (DocumentLibraryServiceAsync) GWT.create( DocumentLibraryService.class );
                ServiceDefTarget target = (ServiceDefTarget) instance;
                target.setServiceEntryPoint( GWT.getModuleBaseURL() + "DocumentLibraryService" );
            }
            return instance;
        }

        private Util()
        {
            // Utility class should not be instanciated
        }
    }
}
