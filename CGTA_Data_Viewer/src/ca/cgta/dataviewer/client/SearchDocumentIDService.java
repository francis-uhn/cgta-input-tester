package ca.cgta.dataviewer.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client-side stub for the RPC service.
 */
@RemoteServiceRelativePath("searchDocumentID")
public interface SearchDocumentIDService extends RemoteService {
	String searchDocumentIDServer(String name) throws IllegalArgumentException;
}
