package ca.cgta.input.val.client.validator;

import ca.cgta.input.val.client.BaseLayoutPanel;
import ca.cgta.input.val.client.Messages;
import ca.cgta.input.val.client.ValidatorEntryPoint;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextArea;

public class QueryPanel extends BaseLayoutPanel {

	private final Messages messages = GWT.create(Messages.class);
	private ValidatorEntryPoint myEntryPoint;
	private Label myErrorLabel;
	private PushButton myValidateNowButton;
	private TextArea myTextArea;

	public QueryPanel(ValidatorEntryPoint theEntryPoint) {
		FlowPanel flowPanel = new FlowPanel();
		add(flowPanel);
		
		myEntryPoint = theEntryPoint;
		
		HTML htmlNewHtml = new HTML(messages.queryPanelWelcomeMessage(), true);
		htmlNewHtml.addStyleName("validatorFormContent");
		flowPanel.add(htmlNewHtml);
		
		myTextArea = new TextArea();
		myTextArea.addStyleName("validatorFormContent");
		myTextArea.setVisibleLines(10);
		flowPanel.add(myTextArea);
		myTextArea.setWidth("90%");
		myTextArea.addKeyPressHandler(new KeyPressHandler() {
			public void onKeyPress(KeyPressEvent theEvent) {
				setErrorMessage("");
			}
		});
		
		myErrorLabel = new Label("");
		myErrorLabel.setStyleName("errorLabel");
		myErrorLabel.addStyleName("validatorFormContent");
		flowPanel.add(myErrorLabel);
		
		myValidateNowButton = new PushButton(messages.pshbtnValidateNow_text());
		myValidateNowButton.addStyleName("validatorFormContent");
		flowPanel.add(myValidateNowButton);
		myValidateNowButton.setWidth("100px");
		
		myValidateNowButton.addClickHandler(new ClickHandler() {
			
			public void onClick(ClickEvent theEvent) {
				String messageText = myTextArea.getText();
				if (messageText == null || messageText.isEmpty()) {
					Window.alert("Please enter a message in the text box above");
					return;
				}
				
				myEntryPoint.validate(messageText);
				myValidateNowButton.setHTML("<img src=\"images/spinner.gif\"/>" + messages.buttonWorking());
				
			}
		});
		
	}

	
	public void setErrorMessage(String theErrorMessage) {
		GWT.log("Setting error message to: " + theErrorMessage);
		myErrorLabel.setText(theErrorMessage);
		myValidateNowButton.setText(messages.pshbtnValidateNow_text());
	}


	public void useSampleMessage() {
		myTextArea.setValue("MSH|^~\\&|1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1||||201203190000||ADT^A03^ADT_A03|127639|T^|2.5^^||||||CAN||||\nEVN|A03|201203190000||||201203190000|W^4266^L\nPID|||7013043^^^UHN^MR^^^^^^^~^^^^JHN^^^^^^^~HN3171^^^UHN^PI^^^^^^^||EDoc^Twh^^^^^L^^^^^201203151442^^~||19101010|M|||200 Bay st^^NORTH YORK^ON^M3A 2S8^Can^H^^^^^^^~|1811|(416)666-9898^PRN^PH^^^^^^^^^~||fra^French^03ZPtlang^^^|||11140000656^^^UHN^VN^^^^^^^~||||||||||||N|||201203151442||||||\nPD1|||UHN^D^^^^UHN^FI^^^^^|17374^Lafebvre^P^^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\nROL||UC|PP^Primary Care Provider^15ZRole^^^|17374^Lafebvre^P^^^Dr.^MD^^UHN^L^^^^^^^^^^^^^^^|200306191834||||ET62^Community Physician^15ZEmpTyp^^^|O^Office^^^^|41 Ramsey Lake Rd.^^Sudbury^ON^P3E 5J1^Can^B^^^^^^^|(705) 522-6237^WPN^PH^^^^^^^^^\nPV1||C|2002-KNP-Eye Clinic^^^W^4266^^^C^^^EyeClinic^1442^|||^^^W^4266^^^^^^^  ^|3648^Rubin^Barry^B.^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^298539|||||||A|||3648^Rubin^Barry^B.^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^298539|CP^|11140000656^^^UHN^VN^W^4266^^^^^|||||||||||||||||1|||W|||||201203081019|201203082359||||||V|\nPV2|||^^^^^|||||||||||||||||||N||DC|||||||N||||||||||||||||||\nROL||UC|AT^Attending Physician^15ZRole^^^|3648^Rubin^Barry^B.^^Dr.^MD^^UHN^L^^^EI^W^2.16.840.1.113883.3.59.3:947^^^^^^^^^^|200907241009||||ET02^Physician^15ZEmpTyp^^^|1^Hospital^15ZOrgTyp^^^|E 5-302^^Toronto^ON^M5G 2C4^CAN^B^UHN-TGH^^^^^^|^^^^^^^^^^\nROL||UC|AD^Admitting Physician^15ZRole^^^|3648^Rubin^Barry^B.^^Dr.^MD^^UHN^L^^^EI^W^2.16.840.1.113883.3.59.3:947^^^^^^^^^^|200907241009||||ET02^Physician^15ZEmpTyp^^^|1^Hospital^^^^|E 5-302^^Toronto^ON^M5G 2C4^CAN^B^UHN-TGH^^^^^^|^^^^^^^143645^^^^\nDG1|1||||||||||||||||||||\nPR1||||||||||||||||||||\nZPV|^|^|||N|||3648^Rubin^Barry^B.^^Dr.^MD^^UHN^L^^^EI^W^4266^^^^^^^^^^^298539^|^^^^^^^^UHN^^^^EI^W^4266^^^^^^^^^^^^||^^|3648^Rubin^Barry^B.^^Dr.^MD^^UHN^L^^^EI^4266^W^^^^^^^^^^^298539^|||||^^^^^|17374^Lafebvre^P^^^Dr.^MD^^UHN^L^^^EI^W^4266^^^^^^^^^^|0^1^2|||||||||||N||^^|");
    }
}
