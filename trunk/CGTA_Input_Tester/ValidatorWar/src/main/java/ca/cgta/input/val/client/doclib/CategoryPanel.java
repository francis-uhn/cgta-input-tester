package ca.cgta.input.val.client.doclib;

import java.io.File;

import ca.cgta.input.val.client.rpc.DocumentLibraryService.LibraryCategory;
import ca.cgta.input.val.client.rpc.DocumentLibraryService.LibraryDocument;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class CategoryPanel extends FlowPanel {	
	private boolean contentIsVisible = false;
	
	private int indentSize = 20;
	
	private VerticalPanel myContentsContainer;
	private Image myFolderIcon = new Image();
	
	public CategoryPanel(LibraryCategory currentDir) {
		 this(currentDir, 0);
	}
		
	public CategoryPanel(final LibraryCategory currentDir, int depth) {
    	
		if (depth > 0) { // do not display the root directory
			FocusPanel directoryWrapper = new FocusPanel();		// needed to handle click events
			
			HorizontalPanel directoryWidget = new HorizontalPanel();
			
			directoryWidget.setSpacing(3);
			directoryWidget.addStyleName("gwt-Anchor");
			directoryWidget.getElement().getStyle().setProperty("paddingLeft", Integer.toString((depth-1) * indentSize) + "px");
			
			directoryWidget.add(myFolderIcon);
			directoryWidget.add(new Label(currentDir.getCategoryName().replace('_', ' ')));
			
			directoryWrapper.add(directoryWidget);
			directoryWrapper.addClickHandler(new ClickHandler() {			
				public void onClick(ClickEvent event) {
					updateVisibility(!contentIsVisible);
				}
			});
			
	        this.add(directoryWrapper);
		}
		
		myContentsContainer = new VerticalPanel();
                
        for (LibraryCategory nextDir : currentDir.getSubcategories()) {
        	// Don't display subtrees that contain no files
        	if (nextDir.hasDocuments())
        		myContentsContainer.add(new CategoryPanel(nextDir, depth + 1));
        }
        
        for (final LibraryDocument nextFile : currentDir.getDocuments()) {
        	// build filepath by climbing up the tree
			final StringBuilder filePath = new StringBuilder();
			filePath.append(nextFile.getBaseName()).append(".").append(nextFile.getExtension());
			
			LibraryCategory nextDir = currentDir;
			while (!nextDir.isRoot()) {
				filePath.insert(0, "/");
				filePath.insert(0, nextDir.getCategoryName());
				nextDir = nextDir.getParent();
			}
        	
        	final HorizontalPanel fileWrapperPanel = new HorizontalPanel();
        	fileWrapperPanel.setSpacing(3);
        	
        	fileWrapperPanel.getElement().getStyle().setProperty("paddingLeft", Integer.toString(depth * indentSize) + "px");
        	
        	final Anchor getFile = new Anchor();
        	getFile.getElement().getStyle().setCursor(Cursor.POINTER);
        	final String fullFileName = nextFile.getBaseName() + "." + nextFile.getExtension();
			getFile.setText(fullFileName);
			
			final String basePath = nextFile.getBasePath().replace('\\', '/');
			
        	getFile.addClickHandler(new ClickHandler() {				
				public void onClick(ClickEvent event) {
					getFile.setEnabled(false);
	                // Passing the organization id as a string via a url parameter
	                String url = GWT.getModuleBaseURL() + "doclib/" + fullFileName + "?getFile=" + filePath.toString();

	                GWT.log("Download URL: " + url);
	                
	                // We're using a hidden frame to do the actual downloading,
	                // since GWT doesn't make it so easy to add a button that
	                // links to a form download
	                Element downloadIframe = RootPanel.get("__download").getElement();
	                DOM.setElementAttribute(downloadIframe, "src", url);
				}
			});
        	
        	fileWrapperPanel.add(new Image(getImagePathForExtension(nextFile.getExtension())));
        	fileWrapperPanel.add(getFile);
        	fileWrapperPanel.add(new Label("(" + optimizeSizeValue(nextFile.getSize()) + ")"));
        	
        	myContentsContainer.add(fileWrapperPanel);
        }
        
        this.add(myContentsContainer);
        
		updateVisibility(depth < 2); // start off with the root and the first level expanded
	}

	private String optimizeSizeValue(long size) {
		String[] units = {" B", " KB", " MB", " GB", " TB"}; // that should be enough... for now
		
		// we don't really need a double here, but that's what format(...) is expecting
		double optimizedSize = (double)size;
		int currentUnitIndex = 0;
		
		while (optimizedSize > 1024 && currentUnitIndex < units.length - 1) {
			optimizedSize /= 1024;
			currentUnitIndex++;
		}
		
		return NumberFormat.getFormat("####.#").format(optimizedSize) + units[currentUnitIndex];
	}
	
	private static String getImagePathForExtension(String theExt) {
		if (theExt.equalsIgnoreCase("doc") || theExt.equalsIgnoreCase("docx"))
			return "images/doclib-file-doc.png";
		if (theExt.equalsIgnoreCase("xls") || theExt.equalsIgnoreCase("xlsx"))
			return "images/doclib-file-xls.png";
		if (theExt.equalsIgnoreCase("ppt") || theExt.equalsIgnoreCase("pptx"))
			return "images/doclib-file-ppt.png";
		if (theExt.equalsIgnoreCase("pdf"))
			return "images/doclib-file-pdf.png";
		
		// generic icon
		return "images/doclib-file.png";
	}

	private void updateVisibility(boolean showContents) {
		contentIsVisible = showContents;
		
		myContentsContainer.setVisible(showContents);

		myFolderIcon.setUrl(showContents ? "images/doclib-folder-open.png" : "images/doclib-folder-closed.png");
	}
}
