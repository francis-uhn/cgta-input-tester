package ca.cgta.input.val.client.oidlib;

import java.util.HashMap;
import java.util.List;

import ca.cgta.input.val.client.BaseLayoutPanel;
import ca.cgta.input.val.client.LoadingSpinner;
import ca.cgta.input.val.client.rpc.ValidatorService;
import ca.cgta.input.val.client.rpc.ValidatorServiceAsync;
import ca.cgta.input.val.shared.results.Code;
import ca.cgta.input.val.shared.results.OidLibrary;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;

public class OidBrowserPanel extends BaseLayoutPanel {

	public static final String HSP_METADATA_MRNOID = "MRNOID";
	public static final String HSP_METADATA_VISITOID = "VISITOID";
	public static final String HSP_METADATA_HOSPITAL_FAC = "FAC";
	public static final String HSP_METADATA_PROVIDEROID = "PROV";
	
	private FlexTable myHSPGrid;
	private boolean myInitialized;
	private LoadingSpinner myLoadingSpinner;
	private FlowPanel myPanel;
	private OidLibrary myResult;


	private ValidatorServiceAsync myValidatorSvc = GWT.create(ValidatorService.class);
	private HashMap<String, String> myCodeToHsp = new HashMap<String, String>();
	private FlexTable myFacilitiesGrid;
	private FlexTable myCodeSystemsGrid;
	private FlexTable mySendingSystemsGrid;
	private FlexTable myOtherGrid;
	private FlexTable myProvidersGrid;
	private HTML myJumpLabel;


	/**
	 * Constructor
	 */
	public OidBrowserPanel() {

		myPanel = new FlowPanel();
		this.add(new ScrollPanel(myPanel));

		HTML header = new HTML("<h1>OID Browser</h1>" + "<p>This page contains HSP-specific OID values which are used in " +
				"several HL7 tables within the ConnectingGTA Input Specification (HL7 Version 2.5). " +
				"HL7 interfaces which are transmitting data to ConnectingGTA must use values which are " +
				"drawn from these tables, and which are appropropriate for the specific HSP that" +
				" is sending data.</p>");
		myPanel.add(header);

		myLoadingSpinner = new LoadingSpinner("Loading OIDs...");
		myLoadingSpinner.show();
		myPanel.add(myLoadingSpinner);

		loadLibrary();
	}


	private void initPage() {
		if (myInitialized) {
			return;
		}

		myInitialized = true;

		StringBuilder b = new StringBuilder();
		b.append("Jump to:<br/>");
		b.append("<ul>");
		b.append("<li><a href=\"#OID_TBL9004\">Table 9004 - HSP Identifiers</a></li>");
		b.append("<li><a href=\"#OID_TBL9005\">Table 9005 - Facilities</a></li>");
		b.append("<li><a href=\"#OID_TBL9007\">Table 9007 - Code Systems</a></li>");
		b.append("<li><a href=\"#OID_TBL9008\">Table 9008 - Source Systems</a></li>");
		b.append("<li><a href=\"#OID_TBL9001\">Table 9001 - Provider OIDs</a></li>");
		b.append("<li><a href=\"#OID_TBLOTHER\">Other OIDs</a></li>");
		b.append("</ul>");
		myJumpLabel = new HTML(b.toString());
		myPanel.add(myJumpLabel);
		
		myPanel.add(new HTML("<a name=\"OID_TBL9004\"/>" + "<h2>Table 9004 - HSP Identifiers</h2>"));
		myHSPGrid = new FlexTable();
		myHSPGrid.addStyleName("errorCodesGrid");
		myPanel.add(myHSPGrid);

		myPanel.add(new HTML("<a name=\"OID_TBL9005\"/>" + "<h2>Table 9005 - Facilities</h2>"));
		myFacilitiesGrid = new FlexTable();
		myFacilitiesGrid.addStyleName("errorCodesGrid");
		myPanel.add(myFacilitiesGrid);
		
		myPanel.add(new HTML("<a name=\"OID_TBL9007\"/>" + "<h2>Table 9007 - Code Systems</h2>"));
		myCodeSystemsGrid = new FlexTable();
		myCodeSystemsGrid.addStyleName("errorCodesGrid");
		myPanel.add(myCodeSystemsGrid);

		myPanel.add(new HTML("<a name=\"OID_TBL9008\"/>" + "<h2>Table 9008 - Source Systems</h2>"));
		mySendingSystemsGrid = new FlexTable();
		mySendingSystemsGrid.addStyleName("errorCodesGrid");
		myPanel.add(mySendingSystemsGrid);

		myPanel.add(new HTML("<a name=\"OID_TBL9001\"/>" + "<h2>Table 9001 - Provider OIDs</h2>"));
		myProvidersGrid = new FlexTable();
		myProvidersGrid.addStyleName("errorCodesGrid");
		myPanel.add(myProvidersGrid);

		myPanel.add(new HTML("<a name=\"OID_TBLOTHER\"/>" + "<h2>Other OIDs</h2>"));
		myOtherGrid = new FlexTable();
		myOtherGrid.addStyleName("errorCodesGrid");
		myPanel.add(myOtherGrid);
		
	}


	private void loadLibrary() {

		AsyncCallback<OidLibrary> callback = new AsyncCallback<OidLibrary>() {

			public void onFailure(Throwable theCaught) {
				myLoadingSpinner.showMessage(theCaught.getMessage(), false);
			}


			public void onSuccess(OidLibrary theResult) {
				myLoadingSpinner.hide();
				initPage();
				myResult = theResult;
				redrawTables();
			}

		};
		myValidatorSvc.loadOidLibrary(null, callback);

	}


	private void redrawTables() {
		redrawTable(myResult.getHspIdentifier9004(), myHSPGrid, true);
		redrawTable(myResult.getFacilityIdentifiers9005(), myFacilitiesGrid, false);
		redrawTable(myResult.getCodeSystems9007(), myCodeSystemsGrid, false);
		redrawTable(myResult.getSendingSystems9008(), mySendingSystemsGrid, false);
		redrawTable(myResult.getProvider9001Oids(), myProvidersGrid, false);
		redrawTable(myResult.getOtherOids(), myOtherGrid, false);
	}


	private void redrawTable(List<Code> codes, FlexTable grid, boolean isHsp) {
	    grid.clear();
		int col = 0;
		if (!isHsp) {
			grid.setText(0, col++, "HSP");
		}
		grid.setText(0, col++, "Code");
		grid.setText(0, col++, "Description");
		
		if (isHsp) {
			grid.setText(0, col++, "MOH Facility");
			grid.setText(0, col++, "MRN OID");
			grid.setText(0, col++, "Visit/Encounter OID");
			grid.setText(0, col++, "Provider OID");
		}
		
		int row = 1;
		for (Code next : codes) {
			
			col = 0;
			if (!isHsp) {
				grid.setText(row, col++, myCodeToHsp.get(next.getHspId()));
			}
			grid.setText(row, col++, next.getCode());
			grid.setText(row, col++, next.getDescription());
			if (isHsp) {
				grid.setText(row, col++, defaultString(next.getMetadata().get(HSP_METADATA_HOSPITAL_FAC)));
				grid.setText(row, col++, defaultString(next.getMetadata().get(HSP_METADATA_MRNOID)));
				grid.setText(row, col++, defaultString(next.getMetadata().get(HSP_METADATA_VISITOID)));
				grid.setText(row, col++, defaultString(next.getMetadata().get(HSP_METADATA_PROVIDEROID)));
			}
			
			myCodeToHsp.put(next.getCode(), next.getDescription());
			
			row++;
		}
    }


	private String defaultString(String theString) {
	    return theString != null ? theString : "";
    }

}
