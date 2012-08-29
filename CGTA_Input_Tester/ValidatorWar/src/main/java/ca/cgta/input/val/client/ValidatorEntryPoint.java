package ca.cgta.input.val.client;

import ca.cgta.input.val.client.doclib.DocumentLibraryPanel;
import ca.cgta.input.val.client.errlist.ErrorListPanel;
import ca.cgta.input.val.client.oidlib.OidBrowserPanel;
import ca.cgta.input.val.client.rpc.ValidatorService;
import ca.cgta.input.val.client.rpc.ValidatorServiceAsync;
import ca.cgta.input.val.client.validator.QueryPanel;
import ca.cgta.input.val.client.validator.ValidationResultsPanel;
import ca.cgta.input.val.shared.results.ValidationResult;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootLayoutPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class ValidatorEntryPoint implements EntryPoint {
	public static final String PARAM_SHOW_ERROR_CODE = "showErrorCode";
	public static final String TOKEN_SAMPLE_MESSAGE = "SAM";
	public static final String TOKEN_DOCUMENT_LIBRARY = "DOC";
	public static final String TOKEN_ERROR_CODES = "ERR";
	public static final String TOKEN_VALIDATION_RESULTS = "RES";
	public static final String TOKEN_OID_EXPLORER = "OID";

	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while " + "attempting to contact the server. Please check your network " + "connection and try again.";

	private final Messages messages = GWT.create(Messages.class);

	private QueryPanel myQueryPanel;
	private ValidationResultsPanel myResultsPanel;
	private RootLayoutPanel myRootPanel;


	/**
	 * Create a remote service proxy to talk to the server-side Greeting
	 * service.
	 */
	private final ValidatorServiceAsync myValService = GWT.create(ValidatorService.class);
	private String myPreviousToken;
	private static String ourErrorCodeParam;


	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {

		// Add the nameField and sendButton to the RootPanel
		// Use RootPanel.get() to get the entire body element
		//RootPanel rootPanel = RootPanel.get("outerContainer");
		myRootPanel = RootLayoutPanel.get();
        myRootPanel.getElement().setInnerHTML("");

        ourErrorCodeParam = Location.getParameter(PARAM_SHOW_ERROR_CODE);
        if (ourErrorCodeParam != null && ourErrorCodeParam.trim().length() > 0) {
        	History.newItem(TOKEN_ERROR_CODES, false);
        }
        
        History.addValueChangeHandler(new ValueChangeHandler<String>() {
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				updateViewBasedOnHistory();
			}
		});
        History.fireCurrentHistoryState();
        
	}
	
	private void updateViewBasedOnHistory() {
		String token = History.getToken();
		if (token != null) {
			token = token.replaceAll("_.*", "");
		}

		if (myPreviousToken != null && myPreviousToken.equals(token)) {
			return;
		}

		myRootPanel.clear();

		if (TOKEN_VALIDATION_RESULTS.equals(token) && myValidationResult != null) {
			myResultsPanel = new ValidationResultsPanel(myValidationResult);
			myRootPanel.add(myResultsPanel);
		} else if (TOKEN_ERROR_CODES.equals(token)) {
			myRootPanel.add(new ErrorListPanel());
		} else if (TOKEN_DOCUMENT_LIBRARY.equals(token)) {
			myRootPanel.add(new DocumentLibraryPanel());
		} else if (TOKEN_OID_EXPLORER.equals(token)) {
			myRootPanel.add(new OidBrowserPanel());
		} else {
			myQueryPanel = new QueryPanel(this);
			myRootPanel.add(myQueryPanel);
		}
		
		if (TOKEN_SAMPLE_MESSAGE.equals(token)) {
			myQueryPanel.useSampleMessage();
		}

		myPreviousToken = token;
		
	}

	private ValidationResult myValidationResult;

	/**
	 * Actually validate a message
	 */
	public void validate(String theMessageText) {
		
		AsyncCallback<ValidationResult> callback = new AsyncCallback<ValidationResult>() {
			




			public void onFailure(Throwable theCaught) {
				GWT.log("Failed to validate", theCaught);
				Window.alert("Failed to validate because of an internal error (this is probably a bug): " + theCaught.getMessage());
			}
			
			
			public void onSuccess(ValidationResult theResult) {
				if (theResult.getErrorMessage() != null && theResult.getErrorMessage().length() > 0) {
					if (myQueryPanel != null) {
						myQueryPanel.setErrorMessage(theResult.getErrorMessage());
					}
				} else {
					myQueryPanel = null;
					myValidationResult = theResult;
					History.newItem(TOKEN_VALIDATION_RESULTS, true);
				}
			}
		};
		myValService.validate(theMessageText, callback);
		
    }
	
	/**
	 * Return the URL parameter for error code and clear it so it only shows up once per page load
	 */
	public static String getAndClearErrorCodeParameter() {
		String retVal = ourErrorCodeParam;
		ourErrorCodeParam = null;
		return retVal;
	}
}
