package javax.jbi.messaging;

import sail.xsd.canonical.hl7v2.CanonicalHl7V2Message;

public class NormalizedMessage {

	private CanonicalHl7V2Message myCanonicalMessage;

	public NormalizedMessage(CanonicalHl7V2Message theCanonicalMessage) {
	    super();
	    myCanonicalMessage = theCanonicalMessage;
    }

	public CanonicalHl7V2Message getCanonicalMessage() {
	    return myCanonicalMessage;
    }

}
