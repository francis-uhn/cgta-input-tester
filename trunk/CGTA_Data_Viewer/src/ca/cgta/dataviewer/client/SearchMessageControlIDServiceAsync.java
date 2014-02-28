package ca.cgta.dataviewer.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>SearchMessageControlID</code>.
 */
public interface SearchMessageControlIDServiceAsync {
	void searchMessageControlIDServer(String input, AsyncCallback<String> callback)
			throws IllegalArgumentException;
}
