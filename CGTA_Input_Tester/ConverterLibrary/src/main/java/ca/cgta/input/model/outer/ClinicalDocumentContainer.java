package ca.cgta.input.model.outer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;
import org.ektorp.support.CouchDbDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.cgta.input.model.inner.ClinicalDocumentSection;


public final class ClinicalDocumentContainer extends CouchDbDocument {

	private static final long serialVersionUID = 1L;
	private static final Logger ourLog = LoggerFactory.getLogger(ClinicalDocumentContainer.class);
	
	private ClinicalDocumentGroup document;
	
	 @JsonProperty(value="_deleted_conflicts")
     public String[] _deleted_conflicts;


	public ClinicalDocumentContainer() {
		
	}
	
	public ClinicalDocumentContainer(ClinicalDocumentGroup doc) {						
		this.document = doc;			
		this.setRevision(doc.revision);
	}
			
	public ClinicalDocumentGroup getDocument() {			
		document.revision = this.getRevision();
		return document;
	}

	public void setDocument(ClinicalDocumentGroup document) {
		if (this.document == null) {
			
			this.document = document;
			
		} else {
			
			List<ClinicalDocumentSection> newSections = new LinkedList<ClinicalDocumentSection>(document.mySections);
			this.document.mySections = new LinkedList<ClinicalDocumentSection>(this.document.mySections);

			for (ClinicalDocumentSection nextExistingSection : this.document.mySections) {
				for (Iterator<ClinicalDocumentSection> newSectionIterator = newSections.iterator(); newSectionIterator.hasNext(); ) {
					ClinicalDocumentSection nextNewSection = newSectionIterator.next();
					if (nextExistingSection.mySectionId.equals(nextNewSection.mySectionId)) {
						ourLog.info("Merging section {} contents into document id {}", nextNewSection.mySectionId.toKey(), this.document.myPlacerGroupNumber.toKey());
						
						newSectionIterator.remove();
						nextExistingSection.mergeIn(nextNewSection);
						
					}
				}
			}

			for (Iterator<ClinicalDocumentSection> newSectionIterator = newSections.iterator(); newSectionIterator.hasNext(); ) {
				ClinicalDocumentSection nextNewSection = newSectionIterator.next();
				ourLog.info("Adding section {} contents into document id {}", nextNewSection.mySectionId.toKey(), this.document.myPlacerGroupNumber.toKey());
				this.document.mySections.add(nextNewSection);
			}

		}
		
	}		
}