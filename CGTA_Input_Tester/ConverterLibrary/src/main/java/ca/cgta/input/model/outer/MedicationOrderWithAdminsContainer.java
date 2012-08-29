package ca.cgta.input.model.outer;

import org.codehaus.jackson.annotate.JsonProperty;
import org.ektorp.support.CouchDbDocument;


public final class MedicationOrderWithAdminsContainer extends CouchDbDocument {

	private static final long serialVersionUID = 1L;
	private MedicationOrderWithAdmins document;
	
     @JsonProperty(value="_deleted_conflicts")
     public String[] _deleted_conflicts;

	
	
	public MedicationOrderWithAdminsContainer() {
		
	}
	
	public MedicationOrderWithAdminsContainer(MedicationOrderWithAdmins doc) {						
		this.document = doc;			
		this.setRevision(doc.revision);
	}
			
	public MedicationOrderWithAdmins getDocument() {			
		document.revision = this.getRevision();
		return document;
	}

	public void setDocument(MedicationOrderWithAdmins document) {
		this.document = document;
	}		
}