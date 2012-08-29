package ca.cgta.input.model.outer;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.ektorp.support.CouchDbDocument;


public final class PatientWithVisitsContainer extends CouchDbDocument {

	private static final long serialVersionUID = 1L;
	
	@JsonProperty(value="_deleted_conflicts")
    public String[] _deleted_conflicts;
	
    private PatientWithVisits document;

	@JsonIgnore
	private boolean myNewlyCreated;
	
	/**
	 * Constructor
	 */
	public PatientWithVisitsContainer() {
		super();
	}

	/**
	 * Constructor
	 */
	public PatientWithVisitsContainer(PatientWithVisits doc) {						
		this.document = doc;			
		this.setRevision(doc.revision);
	}

	public PatientWithVisits getDocument() {			
		document.revision = this.getRevision();
		return document;
	}
	
	/**
     * @return the newlyCreated
     */
    public boolean isNewlyCreated() {
    	return myNewlyCreated;
    }
			
	public void setDocument(PatientWithVisits document) {
		this.document = document;
	}

	/**
     * @param theNewlyCreated the newlyCreated to set
     */
    public void setNewlyCreated(boolean theNewlyCreated) {
    	myNewlyCreated = theNewlyCreated;
    }		
}