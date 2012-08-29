package ca.cgta.input;

import java.util.HashMap;

import org.codehaus.jackson.annotate.JsonProperty;
import org.ektorp.Attachment;
import org.ektorp.support.CouchDbDocument;
import org.ektorp.util.Assert;


public final class SampleDocWithAttachmentContainer extends CouchDbDocument {

	private static final long serialVersionUID = 1L;
	private SampleDoc document;
	
     @JsonProperty(value="_deleted_conflicts")
     public String[] _deleted_conflicts;

	
	
	public SampleDocWithAttachmentContainer() {
		
	}
	
	public SampleDocWithAttachmentContainer(SampleDoc sampleDoc) {						
		this.document = sampleDoc;			
		this.setRevision(sampleDoc.revision);
	}
			
	public SampleDoc getDocument() {			
		document.revision = this.getRevision();
		return document;
	}

	public void setDocument(SampleDoc document) {
		this.document = document;
	}
	
	
    public void removeAttachment(String id) {
        this.removeAttachment(id);
    }
    
    public void addAttachment(Attachment a) {
        this.addInlineAttachment(a);
    }	
	
	
	
	
}