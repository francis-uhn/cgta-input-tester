package ca.cgta.input.model.inner;

import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonIgnore;

public class Mrg {

	public ArrayList<Cx> myMergePatientIds;
	public Cx myMergeVisitId;

	@JsonIgnore
	public boolean hasIdWithTypeMr() {
		if (myMergePatientIds == null) {
			myMergePatientIds = new ArrayList<Cx>();
		}
		for (Cx next : myMergePatientIds) {
			if ("MR".equals(next.myIdTypeCode)) {
				return true;
			}
		}
		return false;
	}

	@JsonIgnore
	public Cx getMrn() {
		if (myMergePatientIds == null) {
			myMergePatientIds = new ArrayList<Cx>();
		}
		for (Cx next : myMergePatientIds) {
			if ("MR".equals(next.myIdTypeCode)) {
				return next;
			}
		}
		return null;
	}

	public boolean hasMultipleIdWithTypeMr() {
		int count = 0;
		for (Cx next : myMergePatientIds) {
			if ("MR".equals(next.myIdTypeCode)) {
				count++;
			}
		}
		return count > 1;
	}

}
