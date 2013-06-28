package sail.map;

import javax.jbi.messaging.NormalizedMessage;

import sail.xsd.canonical.hl7v2.CanonicalHl7V2Message;

public class Application {

	public static void main(String[] args) throws Exception {

		String message = "MSH|^~\\&|LAB|SCC|OLIS|SMH|201101111537||ORU^R01|00782704|P|2.3.1||||||8859/1\r" +
				"PID|||J4001645||IRONWOOD^BIBIANA^V||19700510|F|||2301-30 BOND ST^^TORONTO^ON^M5B1W8||(416)555-5555|||||00270011851|2000010203AR\r" + "PV1||I|4Q||00270011851||99994||||||||||||||||||||||||||||||||SMH\r" + "ORC|RE|10676743|199|F1110030|||^^^20110111^^R||201101111536|DANGR||99994|4Q^B1\r"
		        + "OBR|1|10676743|199|FEPR^Iron Profile-FE,TIBC,SAT|||201101111536|||DANGR||||201101111536||99994||||TTGEN02||201101111536||BCHEM|F|||40900^40901||||DANGR\r" + "NTE|1||This order was received without a proper requisition.\r" + "NTE|2||This specimen was collecetd by syringe.\r" + "OBX|1|NM|FE^Iron Total||42|umol/L|7-30|H|||F|||201101111536|B|DANGR\r"
		        + "OBX|2|NM|TIBC^TIBC||54|umol/L|42-72||||F|||201101111536|B|DANGR\r" + "NTE|1||This result is invalid\r" + "NTE|2||This specimen was collecetd incorrectly\r" + "OBX|2|ST|TIBC^TIBC||54|umol/L|42-72||||F|||201101111536|B|DANGR\r" + "OBX|2|ST|TIBC^TIBC||54:1|umol/L|42-72||||F|||201101111536|B|DANGR\r" + "OBX|2|ST|TIBC^TIBC||Negative|umol/L|Negative||||F|||201101111536|B|DANGR\r"
		        + "OBX|1|TX|UCLU^Chloride/L||45|mmol/L|||||F|||201105091146|B|GAF\r" + "OBX|1|TX|UCLU^Chloride/L||<45|mmol/L|||||F|||201105091146|B|GAF\r" + "OBX|1|TX|UCLU^Chloride/L||trace|mmol/L|||||F|||201105091146|B|GAF\r" + "OBX|3|NM|SAT^Saturation||0.78||0.20-0.50|H|||F|||201101111536|B|DANGR\r";

		// Create a canonical message object
		CanonicalHl7V2Message canonical = new CanonicalHl7V2Message(message, "TEST", "00782704");
		NormalizedMessage normalized = new NormalizedMessage(canonical);

		// Apply the site specific mapping
		Map_ORU_SMH_Soft_ORUint_ON_OLIS map1 = new Map_ORU_SMH_Soft_ORUint_ON_OLIS();
		map1.receive(normalized);

		// Create a new canonical message object with the updated message
		canonical = map1.getCanonical();
		normalized = new NormalizedMessage(canonical);

		// Apply the general mapping
		SOAP_Map_ORUint_ON_OLIS_ORU_ON_OLIS map2 = new SOAP_Map_ORUint_ON_OLIS_ORU_ON_OLIS();
		map2.map(normalized);

		canonical = map2.getCanonical();
		if (canonical.getMessageErrored() != null) {
			// send to dead letter queue
		} else if (map2.getCanonical().getMessageFiltered() != null) {
			// log and drop (message is filtered and not relevant)
		} else {
			System.out.println("\n\n\n\nOutput:\n" + map2.getCanonical().getRawMessage());
		}
	}

}
