package ca.cgta.input.val.client.doclib;

import ca.cgta.input.val.client.BaseLayoutPanel;
import ca.cgta.input.val.client.LoadingSpinner;
import ca.cgta.input.val.client.rpc.DocumentLibraryService;
import ca.cgta.input.val.client.rpc.DocumentLibraryService.LibraryCategory;
import ca.cgta.input.val.client.rpc.DocumentLibraryServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class DocumentLibraryPanel extends BaseLayoutPanel {

	private VerticalPanel myActiveState;
	private LoadingSpinner myLoadingState;
	private LibraryCategory myRoot;


	/**
	 * Constructor
	 */
	public DocumentLibraryPanel() {

		FlowPanel fp = new FlowPanel();
		this.add(fp);
		
		HTML header = new HTML("<h1>Document Library</h1>" +
				"<p>Welcome to the document library. Click on the links below to download documents used for " +
				"ConnectingGTA CDR population.</p>");
		fp.add(header);
		
		// initialize panels and widgets
		myActiveState = new VerticalPanel();
		
		myLoadingState = new LoadingSpinner("Loading Document Library...");

		// style
		myActiveState.setSpacing(10);

		fp.add(myLoadingState);
		fp.add(myActiveState);

		loadLibrary();
	}



	private void loadLibrary() {
		myActiveState.setVisible(false);
		myLoadingState.show();

		// get the document library
		DocumentLibraryServiceAsync createService = GWT.create(DocumentLibraryService.class);
		createService.getLibraryTree(new AsyncCallback<LibraryCategory>() {

			public void onFailure(Throwable caught) {
				myActiveState.clear();

				myActiveState.add(new Label("Unable to load document library."));
				GWT.log("Unable to load document library.", caught);

				myLoadingState.hide();
				myActiveState.setVisible(true);
			}


			public void onSuccess(LibraryCategory result) {
				myRoot = result;

				myActiveState.clear();

				if (myRoot.hasDocuments()) {
					myActiveState.add(new CategoryPanel(myRoot));
				} else {
					myActiveState.add(new Label("The Document Library does not contain any files."));
					GWT.log("The Document Library does not contain any files.");
				}

				myLoadingState.hide();
				myLoadingState.setVisible(false);
				myActiveState.setVisible(true);
			}
		});
	}
}
