package ca.cgta.dataviewer.client;

import javax.servlet.http.HttpSession;

import ca.cgta.dataviewer.shared.FieldVerifier;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dev.util.msg.Message;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class CGTA_DataViewer implements EntryPoint {
	
	static final int VIEW_LOGIN = 1;
	static final int VIEW_DEFAULT = VIEW_LOGIN;
	static final int VIEW_SEARCH = 2;
	static final int VIEW_LOGOUT = 3;
	static final int VIEW_NONE = 4;		// For debugging and testing purposes - ensure a blank page to start.
	
	
	static final TextBox nameField = new TextBox();			// Login box -> User Name
	static final DialogBox dialogBox = new DialogBox();		// AJAX Server Error status box
	static final HTML serverResponseLabel = new HTML();		// AJAX Server Error response box -> Description label
	static final Button closeButton = new Button("Close");	// AJAX Server Error response box -> Close button
	static final Button logoutButton = new Button("Logout");
	
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";
	
	private static final String NOT_LOGGED_IN_TEXT = 
			"Session Information - User not logged in.";
	Label SessionInformationLabel = 
			new HTML(NOT_LOGGED_IN_TEXT); // With the 'Logout' button, a label indicating session status
	
	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
	private final GreetingServiceAsync greetingService = GWT
			.create(GreetingService.class);
	private final SecurityServiceAsync securityService = GWT
			.create(SecurityService.class);

	public static void displayTransition(int viewSelection) {
		switch(viewSelection) {
			case VIEW_LOGIN:
				DOM.getElementById("cgta_viewer_login").getStyle().setDisplay(Display.BLOCK);
				DOM.getElementById("cgta_viewer_grid").getStyle().setDisplay(Display.NONE);
				Document.get().getElementById("cgta_viewer_login").scrollIntoView();
				// Focus the cursor on the name field when the app loads
				nameField.setFocus(true);
				nameField.selectAll();
				break;
			case VIEW_SEARCH:	// They must be logged in for this SEARCH page to be displayed
				DOM.getElementById("cgta_viewer_login").getStyle().setDisplay(Display.NONE);
				DOM.getElementById("cgta_viewer_grid").getStyle().setDisplay(Display.BLOCK);
				Document.get().getElementById("cgta_viewer_grid").scrollIntoView();
				DOM.getElementById("viewerSessionContainer").getStyle().setDisplay(Display.BLOCK);
				break;
			case VIEW_NONE:
				DOM.getElementById("cgta_viewer_login").getStyle().setDisplay(Display.NONE);
				DOM.getElementById("cgta_viewer_grid").getStyle().setDisplay(Display.NONE);
				
				break;
			default:
				// Do nothing if an invalid view is selected
				return;
		}
	}
	
	public static void displayTransition() {
		displayTransition(VIEW_DEFAULT);
	}
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		/** Session Box **/
		
        RootPanel.get("viewerSessionContainer").add(SessionInformationLabel);
        RootPanel.get("viewerSessionContainer").add(logoutButton);
        
        DOM.getElementById("viewerSessionContainer").getStyle().setDisplay(Display.NONE);
        
		/** Login Box **/
		final Button sendButton = new Button("Login");
		//	final TextBox nameField = new TextBox();
		nameField.setText("");		// No more default username
		nameField.getElement().setId("txtUserName");   // Set the Javascript ID of an already created item
		
		final Label errorLabel = new Label();

		final TextBox passwordField = new PasswordTextBox();
		passwordField.setText("");

		// We can add style names to widgets
		sendButton.addStyleName("sendButton");

		// Add the nameField and sendButton to the RootPanel
		// Use RootPanel.get() to get the entire body element
		RootPanel.get("nameFieldContainer").add(nameField);
		RootPanel.get("nameFieldContainer").add(passwordField);
		RootPanel.get("sendButtonContainer").add(sendButton);
		RootPanel.get("errorLabelContainer").add(errorLabel);

		

		// Create the popup dialog box
		//	final DialogBox dialogBox = new DialogBox();
		dialogBox.setText("Remote Procedure Call");
		dialogBox.setAnimationEnabled(true);
		//	final Button closeButton = new Button("Close");
		// We can set the id of a widget by accessing its Element
		closeButton.getElement().setId("closeButton");
		final Label textToServerLabel = new Label();
		//	final HTML serverResponseLabel = new HTML();
		VerticalPanel dialogVPanel = new VerticalPanel();
		dialogVPanel.addStyleName("dialogVPanel");
		dialogVPanel.add(new HTML("<b>Sending name to the server</b>"));
		dialogVPanel.add(textToServerLabel);
		dialogVPanel.add(new HTML("<br><b>Server replies:</b>"));
		dialogVPanel.add(serverResponseLabel);
		dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		dialogVPanel.add(closeButton);
		dialogBox.setWidget(dialogVPanel);

		// Add a handler to close the DialogBox
		closeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				dialogBox.hide();
				sendButton.setEnabled(true);
				sendButton.setFocus(true);
			}
		});
		
		// Add a logout handler
		logoutButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				CGTA_DataViewer.displayTransition(CGTA_DataViewer.VIEW_LOGOUT);
				
				serverResponseLabel.setText("");
				securityService.logoutServer(
					new AsyncCallback<String>() {
						
						public void onFailure(Throwable caught) {
							// Show the RPC error message to the user
							dialogBox
									.setText("Remote Procedure Call - Failure");
							serverResponseLabel
									.addStyleName("serverResponseLabelError");
							serverResponseLabel.setHTML(SERVER_ERROR);
							dialogBox.center();
							closeButton.setFocus(true);
						}
						
						public void onSuccess(String result) {
							if((!(null == result)) && (result.indexOf("OGGED_OUT") > 0)) {
								/** Go to the Login Page once we've been confirmed to be logged out **/
								displayTransition(VIEW_LOGIN);
								DOM.getElementById("viewerSessionContainer").getStyle().setDisplay(Display.NONE);
								SessionInformationLabel.setText("Logged out. " + NOT_LOGGED_IN_TEXT);
							} else {
								// Display the content of the error message directly from the server
								errorLabel.setText(result);
								SessionInformationLabel.setText("An Error occurred logging you out.");
								// TODO: Why would a logout fail at the server side?
								/*
								dialogBox.setText("Remote Procedure Call - Logout Failed");
								serverResponseLabel
										.removeStyleName("serverResponseLabelError");
								serverResponseLabel.setHTML(result);
								dialogBox.center();
								closeButton.setFocus(true);
								*/
							}
						}
					});
			}
		});
		
		
		
		// Create a handler for the sendButton and nameField
		class MyHandler implements ClickHandler, KeyUpHandler {
			/**
			 * Fired when the user clicks on the sendButton.
			 */
			public void onClick(ClickEvent event) {
				submitLoginCredentials();
				// sendNameToServer();
			}

			/**
			 * Fired when the user types in the nameField.
			 */
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					submitLoginCredentials();
					// sendNameToServer();
				}
			}
			
			/**
			 * Send the username and password to the server for verification
			 */
			private void submitLoginCredentials() {
				// Input validation
				errorLabel.setText("");
				String userName = nameField.getText();
				String errorText = FieldVerifier.isValidUserName(userName);
				if (errorText != null) {
					errorLabel.setText(errorText);
					return;
				}
				
				String userPassword = passwordField.getText();
				errorText = FieldVerifier.isValidPassword(userPassword);
				if (errorText != null) {
					errorLabel.setText(errorText);
					return;
				}
				
				// Then, we send the input to the server.
				sendButton.setEnabled(false);
				
				String textToServer = "LOGIN:" + userName + '\n' + userPassword;
				
				// Clear the password from the field as soon as they have entered it:
				passwordField.setText("");
				
				textToServerLabel.setText(textToServer);
				serverResponseLabel.setText("");
				securityService.loginServer(userName, userPassword,
					new AsyncCallback<String>() {
						
						public void onFailure(Throwable caught) {
							// Show the RPC error message to the user
							dialogBox
									.setText("Remote Procedure Call - Failure");
							serverResponseLabel
									.addStyleName("serverResponseLabelError");
							serverResponseLabel.setHTML(SERVER_ERROR);
							textToServerLabel.setText("");
							SessionInformationLabel.setText(NOT_LOGGED_IN_TEXT);	// Was a previous login-session actually destroyed?
							dialogBox.center();
							closeButton.setFocus(true);
						}
						
						public void onSuccess(String result) {
							sendButton.setEnabled(true);	 // Re-Enable the button
							if(result.indexOf("OGIN_OK") > 0) {	// LOGIN_OK is at 'zero',  which might not be distinguished from false
								/** Hide the login box, show the search panel **/
								displayTransition(VIEW_SEARCH);
								/** Update the user name in the Session Information line **/
								SessionInformationLabel.setText("Logged In as " + nameField.getText());
							} else {
								// Display the content of the error message directly from the server
								errorLabel.setText(result.indexOf("LOGIN_OK") + "--" + result);
								SessionInformationLabel.setText(NOT_LOGGED_IN_TEXT);
								/*
								dialogBox.setText("Remote Procedure Call - Login Failed");
								serverResponseLabel
										.removeStyleName("serverResponseLabelError");
								serverResponseLabel.setHTML(result);
								dialogBox.center();
								closeButton.setFocus(true);
								*/
							}
							
						}
					});
			}
			
			
			/**
			 * Send the name from the nameField to the server and wait for a response.
			 */
			private void sendNameToServer() {
				// First, we validate the input.
				errorLabel.setText("");
				String textToServer = nameField.getText();
				if (!FieldVerifier.isValidName(textToServer)) {
					errorLabel.setText("Please enter at least four characters");
					return;
				}

				// Then, we send the input to the server.
				sendButton.setEnabled(false);
				textToServerLabel.setText(textToServer);
				serverResponseLabel.setText("");
				greetingService.greetServer(textToServer,
						new AsyncCallback<String>() {
							public void onFailure(Throwable caught) {
								// Show the RPC error message to the user
								dialogBox
										.setText("Remote Procedure Call - Failure");
								serverResponseLabel
										.addStyleName("serverResponseLabelError");
								textToServerLabel.setText("Login Details");
								serverResponseLabel.setHTML(SERVER_ERROR);
								dialogBox.center();
								closeButton.setFocus(true);
							}

							public void onSuccess(String result) {
								
								if(result.indexOf("LOGIN_OK") > 0) {
									/** Hide the login box, show the search panel **/
									DOM.getElementById("cgta_viewer_login").getStyle().setDisplay(Display.NONE);
									DOM.getElementById("cgta_viewer_grid").getStyle().setDisplay(Display.BLOCK);
									Document.get().getElementById("cgta_viewer_grid").scrollIntoView();
								} else {
									
									dialogBox.setText("Remote Procedure Call - Login Failed");
									serverResponseLabel
											.removeStyleName("serverResponseLabelError");
									serverResponseLabel.setHTML(result);
									dialogBox.center();
									closeButton.setFocus(true);
									
								}
								
							}
						});
			}
		}

		// Add a handler to send the name to the server
		MyHandler handler = new MyHandler();
		sendButton.addClickHandler(handler);
		nameField.addKeyUpHandler(handler);
		
		// Is the user logged in?  Initiate a callback		
		
		
		
		// Hide the 'loading' box, and display the correct view:
		DOM.getElementById("cgta_viewer_loading").getStyle().setDisplay(Display.NONE);
		updateLoggedInUser();
		//	displayTransition(VIEW_SEARCH);
		
	}
	
	public void updateLoggedInUser() {
		securityService.userAccountServer(
				new AsyncCallback<String>() {
					
					public void onFailure(Throwable caught) {
						// Show the RPC error message to the user
						dialogBox
								.setText("Remote Procedure Call - Failure");
						serverResponseLabel
								.addStyleName("serverResponseLabelError");
						serverResponseLabel.setHTML(SERVER_ERROR);
						dialogBox.center();
						closeButton.setFocus(true);
					}

					public void onSuccess(String result) {
						/**
						dialogBox
							.setText("Remote Procedure Call - SUCCESS");
						serverResponseLabel
								.addStyleName("serverResponseLabelError");
						serverResponseLabel.setHTML("Result:" + result);
						dialogBox.center();
						closeButton.setFocus(true);
						*/
						if(result == null) {
							// Display the content of the error message directly from the server
							SessionInformationLabel.setText(NOT_LOGGED_IN_TEXT);
							displayTransition(VIEW_LOGIN);
						} else {
							/** Hide the login box, show the search panel **/
							SessionInformationLabel.setText("Logged In as " + result);
							displayTransition(VIEW_SEARCH);
						}
					}
				});
	}
	
}
