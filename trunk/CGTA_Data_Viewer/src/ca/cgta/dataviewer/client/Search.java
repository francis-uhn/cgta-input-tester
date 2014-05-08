package ca.cgta.dataviewer.client;

import ca.cgta.dataviewer.shared.FieldVerifier;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
//import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;


// import ca.cgta.dataviewer.client.SearchDocumentIDService;
import ca.cgta.dataviewer.client.SearchOIDServiceAsync;

public class Search implements EntryPoint {

	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";
	
	/**
	 * Create a remote service proxy to talk to the server-side Search service.
	 */
	private final SearchDocumentIDServiceAsync searchDocumentIDService = GWT
			.create(SearchDocumentIDService.class);
	
	private final SearchMessageControlIDServiceAsync searchMessageControlIDService = GWT
			.create(SearchMessageControlIDService.class);
	
	private final SearchOIDServiceAsync searchOIDService = GWT
			.create(SearchOIDService.class);
	
	//private VerticalPanel mainPanel = new VerticalPanel();
	//private TextArea outputReport = new TextArea();
	private ScrollPanel mainScrollPanel = new ScrollPanel();
	
	@Override
	public void onModuleLoad() {
		
		
		
		/** Select drop-downs for OID selection **/
		final ListBox lb1 = new ListBox();
		lb1.setName("search1_oid");
		
		lb1.addItem("Please select a hospital", "");
		RootPanel.get("td_search1").add(lb1);
		
		final Button searchButton_DocumentID = new Button("Search by DocumentID");
		
		final TextBox textBox_documentId = new TextBox();
		RootPanel.get("id_search1_id").add(textBox_documentId);
		
		final Label errorLabel1 = new Label();
		RootPanel.get("search1FieldContainer").add(errorLabel1);
		

		
		
		final ListBox lb2 = new ListBox();
		lb2.setName("search2_oid");
		lb2.addItem("Please select a hospital", "");
		RootPanel.get("td_search2").add(lb2);
		
		final Button searchButton_MessageControlID = new Button("Search by MessageControlID");
		
		final TextBox textBox_messageControlID = new TextBox();
		RootPanel.get("id_search2_id").add(textBox_messageControlID);
		
		final Label errorLabel2 = new Label();
		RootPanel.get("search2FieldContainer").add(errorLabel2);
		
		
		//mainPanel.add(outputReport);
		
		
		
		/** Dialog box with close button for displaying async results (right from sample code) **/
		// Create the popup dialog box
		final DialogBox dialogBox = new DialogBox();
		dialogBox.setText("Remote Procedure Call");
		dialogBox.setAnimationEnabled(true);
		final Button closeButton = new Button("Close");
		// We can set the id of a widget by accessing its Element
		closeButton.getElement().setId("closeButton");
		final Label textToServerLabel = new Label();
		final HTML serverResponseLabel = new HTML();
		VerticalPanel dialogVPanel = new VerticalPanel();
		dialogVPanel.addStyleName("dialogVPanel");
		dialogVPanel.add(new HTML("<b>Sending name to the server:</b>"));
		dialogVPanel.add(textToServerLabel);
		dialogVPanel.add(new HTML("<br><b>Server replies:</b>"));
		dialogVPanel.add(serverResponseLabel);
		dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		dialogVPanel.add(closeButton);
		dialogBox.setWidget(dialogVPanel);




		/** Get the OID list from the CSV file on the server, via RPC (rather than just loading a remote file in JS?)
		 */
		
		// Modify this
		String textToServer = "getOIDs";
		
		searchOIDService.searchOIDServer(textToServer,
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
						String[] OIDS = result.split(":");
						for(int x = 0; x<OIDS.length; x++) {
							String[] items = OIDS[x].split("@");
							lb1.addItem(items[1], items[0]);
							lb2.addItem(items[1], items[0]);
						}
					}
				});		
		
		
		// Create a handler for DocumentID search
		class DocumentID_SearchHandler implements ClickHandler /*, KeyUpHandler */ {
			/**
			 * Fired when the user clicks on the sendButton.
			 */
			public void onClick(ClickEvent event) {
				sendNameToServer();
			}
			/**
			 * Send the name from the nameField to the server and wait for a response.
			 */
			private void sendNameToServer() {
				// First, we validate the input.
				errorLabel1.setText("");
				String userOID = lb1.getValue(lb1.getSelectedIndex());
				if (!FieldVerifier.isValidOID(userOID)) {
					errorLabel1.setText("Please enter a valid OID");
					return;
				}
				
				String userDocumentID = textBox_documentId.getText();
				if (!FieldVerifier.isValidName(userDocumentID)) {
					errorLabel1.setText("Please enter a valid Document ID");
					return;
				}
				
				String textToServer = userOID + "|" + userDocumentID;
				
				// Then, we send the input to the server.
				searchButton_DocumentID.setEnabled(false);
				textToServerLabel.setText(textToServer);
				serverResponseLabel.setText("");
				searchDocumentIDService.searchDocumentIDServer(textToServer,
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
								dialogBox.setText("Call HL7Translator");
								serverResponseLabel
										.removeStyleName("serverResponseLabelError");
								serverResponseLabel.setHTML("Report Fetch Success!!");
								//outputReport.setText("Hi cGTAViewer");
								dialogBox.center();
								closeButton.setFocus(true);
								
								// Add data in text area
								//outputReport.setText(result);
								HTMLPanel htmlPanel = new HTMLPanel( result );
								mainScrollPanel.clear();
								mainScrollPanel.add( htmlPanel );
								//RootPanel.get("htmlReport").add(mainScrollPanel);
								RootPanel.get("htmlReportDisplay").add(mainScrollPanel);
								
							}
						});
			}
		}
		
		

		
		// Create a handler for MessageControlID search
		class MessageControlID_SearchHandler implements ClickHandler /*, KeyUpHandler */ {
			/**
			 * Fired when the user clicks on the sendButton.
			 */
			public void onClick(ClickEvent event) {
				sendNameToServer();
			}
			/**
			 * Send the name from the nameField to the server and wait for a response.
			 */
			private void sendNameToServer() {
				// First, we validate the input.
				errorLabel2.setText("");
				String userOID = lb2.getValue(lb2.getSelectedIndex());
				if (!FieldVerifier.isValidOID(userOID)) {
					errorLabel2.setText("Please enter a valid OID");
					return;
				}
				
				String userMessageControlID = textBox_messageControlID.getText();
				if (!FieldVerifier.isValidName(userMessageControlID)) {
					errorLabel2.setText("Please enter a valid Document ID");
					return;
				}
				
				String textToServer = userOID + "|" + userMessageControlID;
				
				// Then, we send the input to the server.
				searchButton_MessageControlID.setEnabled(false);
				textToServerLabel.setText(textToServer);
				serverResponseLabel.setText("");
				searchMessageControlIDService.searchMessageControlIDServer(textToServer,
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
																
								dialogBox.setText("Call HL7Translator");
								serverResponseLabel
										.removeStyleName("serverResponseLabelError");
								serverResponseLabel.setHTML("Report Fetch Success!!");
								
								dialogBox.center();
								closeButton.setFocus(true);
								
								// Add data in text area
								//outputReport.setText(result);
								HTMLPanel htmlPanel = new HTMLPanel( result );
								mainScrollPanel.clear();
								mainScrollPanel.add( htmlPanel );
								//RootPanel.get("htmlReport").add(mainScrollPanel);
								RootPanel.get("htmlReportDisplay").add(mainScrollPanel);
							}
						});
			}
		}
		
		
		
		
		


		DocumentID_SearchHandler handleDocumentID = new DocumentID_SearchHandler();
		searchButton_DocumentID.addClickHandler(handleDocumentID);
		RootPanel.get("search1FieldContainer").add(searchButton_DocumentID);
		
		MessageControlID_SearchHandler handleMessageControlID = new MessageControlID_SearchHandler();
		searchButton_MessageControlID.addClickHandler(handleMessageControlID);
		RootPanel.get("search2FieldContainer").add(searchButton_MessageControlID);
				
		// Add a handler to close the DialogBox
		closeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				dialogBox.hide();
				searchButton_DocumentID.setEnabled(true);
				searchButton_MessageControlID.setEnabled(true);
				//sendButton.setFocus(true);
			}
		});
		
	}
}
