package ca.cgta.input.val.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabBar;

public class BaseLayoutPanel extends DockLayoutPanel {
	private final Messages messages = GWT.create(Messages.class);
	private Hyperlink myErrorCodesLink;
	private Hyperlink myValidatorLink;
	private Hyperlink myDocLibLink;
	private Hyperlink myOidLibLink;


	public BaseLayoutPanel() {
		super(Unit.PX);

		// "ConnectingGTA Conformance Tester"
		FlowPanel topPanel2 = new FlowPanel();
		topPanel2.setStyleName("layoutTitleRow");
		addNorth(topPanel2, 30);

		Label lblConnectinggtaHlvInput = new Label("ConnectingGTA Conformance Tester");
		topPanel2.add(lblConnectinggtaHlvInput);

		FlowPanel linksTopPanel = new FlowPanel();
		linksTopPanel.setStyleName("containerPanel");
		addNorth(linksTopPanel, 25);

		// Page links
		myValidatorLink = new Hyperlink("Message Validator", "");
		myValidatorLink.addStyleName("topHyperlink");
		linksTopPanel.add(myValidatorLink);

		myErrorCodesLink = new Hyperlink("Error Code List", ValidatorEntryPoint.TOKEN_ERROR_CODES);
		myErrorCodesLink.addStyleName("topHyperlink");
		linksTopPanel.add(myErrorCodesLink);

		myDocLibLink = new Hyperlink("Document Library", ValidatorEntryPoint.TOKEN_DOCUMENT_LIBRARY);
		myDocLibLink.addStyleName("topHyperlink");
		linksTopPanel.add(myDocLibLink);
		
		myOidLibLink = new Hyperlink("OID Explorer", ValidatorEntryPoint.TOKEN_OID_EXPLORER);
		myOidLibLink.addStyleName("topHyperlink");
		linksTopPanel.add(myOidLibLink);

		//		TabBar topTabBar = new TabBar();
//		topTabBar.addTab("Message Validator");
//		topTabBar.addTab("Error Code List");
//		topTabBar.addTab("Documents");
//		addNorth(topTabBar, 50);
		
		// Footer
		FlowPanel bottomPanel = new FlowPanel();
		bottomPanel.setStyleName("footerMessageStyleNoSpace");
		addSouth(bottomPanel, 20);

		Label lblNewLabel = new Label(messages.containerFooterMesssage());
		bottomPanel.add(lblNewLabel);

		com.google.gwt.user.client.History.addValueChangeHandler(new ValueChangeHandler<String>() {
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				updateLinks();
			}
		});
		updateLinks();

	}


	private void updateLinks() {
		String token = com.google.gwt.user.client.History.getToken();
		if (token != null) {
			token = token.replaceAll("_.*", "");
		}

		myErrorCodesLink.removeStyleName("topHyperlinkSelected");
		myValidatorLink.removeStyleName("topHyperlinkSelected");
		myDocLibLink.removeStyleName("topHyperlinkSelected");
		myOidLibLink.removeStyleName("topHyperlinkSelected");

		if (ValidatorEntryPoint.TOKEN_ERROR_CODES.equals(token)) {
			myErrorCodesLink.addStyleName("topHyperlinkSelected");
		} else if (ValidatorEntryPoint.TOKEN_DOCUMENT_LIBRARY.equals(token)) {
			myDocLibLink.addStyleName("topHyperlinkSelected");
		} else if (ValidatorEntryPoint.TOKEN_OID_EXPLORER.equals(token)) {
			myOidLibLink.addStyleName("topHyperlinkSelected");
		} else {
			myValidatorLink.addStyleName("topHyperlinkSelected");
		}

	}

}
