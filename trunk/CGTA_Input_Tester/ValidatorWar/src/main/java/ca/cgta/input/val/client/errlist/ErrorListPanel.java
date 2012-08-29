package ca.cgta.input.val.client.errlist;

import java.util.List;

import ca.cgta.input.val.client.BaseLayoutPanel;
import ca.cgta.input.val.client.LoadingSpinner;
import ca.cgta.input.val.client.Messages;
import ca.cgta.input.val.client.ValidatorEntryPoint;
import ca.cgta.input.val.client.rpc.ValidatorService;
import ca.cgta.input.val.client.rpc.ValidatorServiceAsync;
import ca.cgta.input.val.shared.results.FailureCodeDetails;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.ScrollPanel;

public class ErrorListPanel extends BaseLayoutPanel {

	private static List<FailureCodeDetails> ourResult;
	private final Messages messages = GWT.create(Messages.class);
	private FlexTable myCodesGrid;
	private HorizontalPanel myLinksPanel;
	private LoadingSpinner myLoadingSpinner;
	private boolean myRedrawing;
	private String mySelectedErrorCode;
	private RadioButton myShowAllRadioButton;
	
	private ListBox myShowOneListBox;
	private RadioButton myShowOneRadioButton;
	/**
	 * Create a remote service proxy to talk to the server-side Greeting
	 * service.
	 */
	private final ValidatorServiceAsync myValService = GWT.create(ValidatorService.class);
	
	/**
	 * Constructor
	 */
	public ErrorListPanel() {
		final FlowPanel flowPanel = new FlowPanel();
		add(new ScrollPanel(flowPanel));

		HTML htmlNewHtml = new HTML(messages.errorCodesWelcomeMessage(), true);
		flowPanel.add(htmlNewHtml);

		myLoadingSpinner = new LoadingSpinner();
		myLoadingSpinner.show();
		flowPanel.add(myLoadingSpinner);

		myLinksPanel = new HorizontalPanel();
		myLinksPanel.setVisible(false);
		flowPanel.add(myLinksPanel);

		myLinksPanel.add(new HTML("<b>Show</b> "));

		myShowAllRadioButton = new RadioButton("show_group", "All Codes");
		myShowAllRadioButton.addValueChangeHandler(new MyShowButtonValueChangeHandler());
		myLinksPanel.add(myShowAllRadioButton);

		myShowOneRadioButton = new RadioButton("show_group", "Code: ");
		myShowOneRadioButton.addValueChangeHandler(new MyShowButtonValueChangeHandler());
		myLinksPanel.add(myShowOneRadioButton);

		myShowOneListBox = new ListBox(false);
		myShowOneListBox.addChangeHandler(new MyShowOneChangeHandler());
		myLinksPanel.add(myShowOneListBox);

		myCodesGrid = new FlexTable();
		myCodesGrid.setVisible(false);
		myCodesGrid.addStyleName("errorCodesGrid");
		flowPanel.add(myCodesGrid);

		mySelectedErrorCode = ValidatorEntryPoint.getAndClearErrorCodeParameter();

		if (ourResult != null) {
			redrawTable();
		} else {
			myValService.failureCodes(new MyLoadFailureCodesAsyncHandler());
		}

	}

	private void redrawTable() {
		myLoadingSpinner.hide();
		myLinksPanel.setVisible(true);
		myCodesGrid.setVisible(true);

		myRedrawing = true;
		myCodesGrid.removeAllRows();

		myCodesGrid.setText(0, 0, "Code");
		myCodesGrid.setText(0, 1, "Severity");
		myCodesGrid.setText(0, 2, "Description");
		myCodesGrid.setText(0, 3, "How to Resolve");

		myShowOneListBox.clear();
		myShowOneListBox.addItem("Select...");
		myShowOneListBox.setSelectedIndex(0);

		if (mySelectedErrorCode != null) {
			boolean found = false;
			for (FailureCodeDetails next : ourResult) {
				if (next.getCode().equals(mySelectedErrorCode)) {
					found = true;
					break;
				}
			}
			if (!found) {
				mySelectedErrorCode = null;
			}
		}

		int row = 1;
		int index = 1;
		for (FailureCodeDetails next : ourResult) {

			if (mySelectedErrorCode == null || mySelectedErrorCode.equals(next.getCode())) {
				myCodesGrid.setText(row, 0, next.getCode());
				myCodesGrid.setText(row, 1, FailureCodeDetails.getSeverityDescription(next.getSeverity()));
				myCodesGrid.setText(row, 2, next.getDescription());

				if (next.getHowToResolve() != null) {
					myCodesGrid.setHTML(row, 3, next.getHowToResolve());
				} else {
					myCodesGrid.setHTML(row, 3, "&nbsp;");
				}
				
				for (int i = 0; i < 4; i++) {
					myCodesGrid.getFlexCellFormatter().setVerticalAlignment(row, i, HasVerticalAlignment.ALIGN_TOP);
				}

				row++;
			}

			myShowOneListBox.addItem(next.getCode());
			if (next.getCode().equals(mySelectedErrorCode)) {
				myShowOneListBox.setSelectedIndex(index);
			}

			index++;
		}

		if (myShowOneListBox.getSelectedIndex() == 0) {
			myShowAllRadioButton.setValue(true);
			myShowOneRadioButton.setValue(false);
		} else {
			myShowAllRadioButton.setValue(false);
			myShowOneRadioButton.setValue(true);
		}

		myRedrawing = false;
	}


	protected void respondToShowButtonClick() {
		if (myRedrawing) {
			return;
		}
		
		if (myShowOneListBox.getSelectedIndex() > 0 && myShowOneRadioButton.getValue() == true) {
			mySelectedErrorCode = myShowOneListBox.getItemText(myShowOneListBox.getSelectedIndex());
		} else {
			mySelectedErrorCode = null;
		}
		
		redrawTable();
		
    }


	private final class MyLoadFailureCodesAsyncHandler implements AsyncCallback<List<FailureCodeDetails>> {
	    public void onFailure(Throwable theCaught) {
	    	// TODO: handle
	    }


	    public void onSuccess(List<FailureCodeDetails> theResult) {
	    	ourResult = theResult;
	    	redrawTable();
	    }
    }


	private final class MyShowButtonValueChangeHandler implements ValueChangeHandler<Boolean> {
	    public void onValueChange(ValueChangeEvent<Boolean> theEvent) {
	    	if (myShowOneRadioButton.getValue() == true && myShowOneListBox.getSelectedIndex() == -1) {
	    		myShowOneListBox.setSelectedIndex(1);
	    	}
	    	
	    	respondToShowButtonClick();
	    }
    }


	public class MyShowOneChangeHandler implements ChangeHandler {

	    public void onChange(ChangeEvent theEvent) {
	    	if (myShowOneListBox.getSelectedIndex() == 0) {
	    		myShowAllRadioButton.setValue(true, true);
	    	} else {
	    		myShowOneRadioButton.setValue(true, true);
	    	}
	    }

    }

}
