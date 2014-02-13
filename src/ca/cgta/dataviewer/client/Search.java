package ca.cgta.dataviewer.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

public class Search implements EntryPoint {

	@Override
	public void onModuleLoad() {
		// TODO Auto-generated method stub

		final Button searchButton = new Button("Search");
		final TextBox searchField = new TextBox();
		searchField.setText("Enter text to search");
		final Label errorLabel = new Label();

		// We can add style names to widgets
		searchButton.addStyleName("sendButton");

		// Add the nameField and sendButton to the RootPanel
		// Use RootPanel.get() to get the entire body element
		RootPanel.get("searchFieldContainer").add(searchField);
		RootPanel.get("searchFieldContainer").add(searchButton);
		
	}

}
