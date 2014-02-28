package ca.cgta.dataviewer.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>SearchDocumentIDService</code>.
 */
public interface SearchDocumentIDServiceAsync {

	void searchDocumentIDServer(String input, AsyncCallback<String> callback)
			throws IllegalArgumentException;
}
