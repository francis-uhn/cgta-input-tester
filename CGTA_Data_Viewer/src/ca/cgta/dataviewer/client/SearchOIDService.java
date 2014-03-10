package ca.cgta.dataviewer.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client-side stub for the RPC service.
 */
@RemoteServiceRelativePath("searchOID")
public interface SearchOIDService extends RemoteService {
	String searchOIDServer(String name) throws IllegalArgumentException;
}
