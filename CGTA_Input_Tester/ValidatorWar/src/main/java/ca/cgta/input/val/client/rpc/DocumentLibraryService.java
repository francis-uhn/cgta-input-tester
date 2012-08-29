package ca.cgta.input.val.client.rpc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * @author t21703uhn
 */
@RemoteServiceRelativePath("doclib")
public interface DocumentLibraryService extends RemoteService {
	
	/**
	 * @return The root of the document library as a LibraryCategory
	 */
	LibraryCategory getLibraryTree() throws Exception;
	

	
	public class LibraryCategory implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private List<LibraryCategory> childCategories = new ArrayList<LibraryCategory>();
		private List<LibraryDocument> childDocuments = new ArrayList<LibraryDocument>();
		private String myName;
		private LibraryCategory myParent;
		
		public LibraryCategory() {
            this(null);
        }
		
		public LibraryCategory(String theDirName) {
			myName = theDirName;
		}
		
		public void addCategory(LibraryCategory theCategory) {
			theCategory.setParent(this);
			childCategories.add(theCategory);
		}
		
		public void addCatorogies(List<LibraryCategory> theCategories) {
			for (LibraryCategory nextCategory : theCategories)
				addCategory(nextCategory);
		}
		
		public boolean addDocument(LibraryDocument theDocument) {
			return childDocuments.add(theDocument);
		}
		
		public void addDocuments(List<LibraryDocument> theDocuments) {
			for (LibraryDocument nextDoc : theDocuments)
				addDocument(nextDoc);
		}
		
		public List<LibraryCategory> getSubcategories() {
			return childCategories;
		}
		
		public List<LibraryDocument> getDocuments() {
			return childDocuments;
		}
		
		public String getCategoryName() {
			return myName;
		}

		public LibraryCategory getParent() {
			return myParent;
		}
		
		public boolean hasDocuments() {
			if (childDocuments.size() > 0)
				return true;
			
			for (LibraryCategory nextCategory : childCategories) {
				if (nextCategory.hasDocuments()) {
					return true;
				}
			}
			
			return false;
		}
		
		public boolean isRoot() {
			return myParent == null;
		}

		public void setParent(LibraryCategory theParent) {
			myParent = theParent;
		}
	}
	
	public class LibraryDocument implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private String myBaseName;
		private String myExt;
		private String myBasePath;
		private long mySize;
		
        public LibraryDocument() {
            // GWT needs all it's serializable classes to have a default empty constructor.
        }
		
		public LibraryDocument(String baseName, String ext, String theBasePath, long size) {
			myBaseName = baseName;
			myExt = ext;
			myBasePath = theBasePath;
			mySize = size;
		}
		
		/**
         * @return the basePath
         */
        public String getBasePath() {
        	return myBasePath;
        }

		public String getBaseName() {
			return myBaseName;
		}
		
		public String getExtension() {
			return myExt;
		}
		
		public long getSize() {
			return mySize;
		}
	}
}
