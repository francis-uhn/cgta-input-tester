package ca.cgta.input.model.inner;

public enum ConfidentialityStatusEnum {

	NORMAL,
	RESTRICTED,
	VERY_RESTRICTED,
	TABOO;
	
	public static ConfidentialityStatusEnum fromHl7Code(String theCode) {
		if ("N".equals(theCode)) {
			return NORMAL;
		}
		if ("R".equals(theCode)) {
			return RESTRICTED;
		}
		if ("V".equals(theCode)) {
			return VERY_RESTRICTED;
		}
		if ("T".equals(theCode)) {
			return TABOO;
		}
		return null;
	}
	
}
