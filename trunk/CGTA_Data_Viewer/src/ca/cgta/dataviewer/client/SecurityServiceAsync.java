package ca.cgta.dataviewer.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>SecurityService</code>.
 */
public interface SecurityServiceAsync {
	// String userAccountServer() throws IllegalArgumentException;
	// String loginServer(String username, String password);
	
	/*
	void greetServer(String input, AsyncCallback<String> callback)
			throws IllegalArgumentException;
	*/
	
	void userAccountServer(AsyncCallback<String> callback);
	void loginServer(String username, String password, AsyncCallback<String> callback);
	void logoutServer(AsyncCallback<String> callback);
}
