package sail.wsdl.infrastructure.terminology;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.hl7.cts.types.CD;
import org.hl7.cts.types.GetCodeSystemsByIdRequest;
import org.hl7.cts.types.GetCodeSystemsByIdResponse;
import org.hl7.cts.types.TranslateCodeSimpleRequest;
import org.hl7.cts.types.TranslateCodeSimpleResponse;
import org.hl7.cts.types.ValidateCodeSimpleRequest;
import org.hl7.cts.types.ValidateCodeSimpleResponse;

public class TerminologyWebService {

	private MultiMap myCodeToCodes = new MultiHashMap();
	private Map<String, String> myCodeSystemIdToName = new HashMap<String, String>();


	public TerminologyWebService() {
		addMapping("1.3.6.1.4.1.12201.1.1.1.38", "FE", "1.3.6.1.4.1.12201.1.1.1.3", "X001");
		addMapping("1.3.6.1.4.1.12201.1.1.1.38", "TIBC", "1.3.6.1.4.1.12201.1.1.1.3", "X002");
		addMapping("1.3.6.1.4.1.12201.1.1.1.38", "UCLU", "1.3.6.1.4.1.12201.1.1.1.3", "X003");
		addMapping("1.3.6.1.4.1.12201.1.1.1.38", "SAT", "1.3.6.1.4.1.12201.1.1.1.3", "X002");

		addMapping("1.3.6.1.4.1.12201.1.1.1.37","FEPR","1.3.6.1.4.1.12201.1.1.1.1","Y001");
		addMapping("1.3.6.1.4.1.12201.1.1.1.37","FEPR","1.3.6.1.4.1.12201.1.1.1.2","Z001");
	}


	private void addMapping(String theFromCodeSystem, String theFromCode, String theToCodeSystem, String theToCode) {
	    String fromKey = theFromCodeSystem + " " + theFromCode;
	    String toKey = theToCodeSystem + " " + theToCode;
	    
	    myCodeToCodes.put(fromKey, toKey);
    }


	public ValidateCodeSimpleResponse validateCodeSimple(ValidateCodeSimpleRequest theRequest) {
		String key = theRequest.getCodeSystemId() + " " + theRequest.getCodeId();

		ValidateCodeSimpleResponse retVal = new ValidateCodeSimpleResponse();
		retVal.setValidates(myCodeToCodes.containsKey(key));
		return retVal;
	}


	public GetCodeSystemsByIdResponse getCodeSystemsById(GetCodeSystemsByIdRequest theRequest) {
		throw new UnsupportedOperationException();
	}


	public TranslateCodeSimpleResponse translateCodeSimple(TranslateCodeSimpleRequest theVcr) {
		String key = theVcr.getCodeSystemId() + " " + theVcr.getCodeId();

		if (key.contains(".100 ")) {
			TranslateCodeSimpleResponse resp = new TranslateCodeSimpleResponse();
			return resp;
		}
		
		List<String> translation = (List<String>) myCodeToCodes.get(key);
		if (translation == null || translation.size() == 0) {
			throw new NullPointerException(key);
		}

		for (String next : translation) {
			String toCs = next.split(" ")[0];
			if (!toCs.equals(theVcr.getToCodeSystemId())) {
				continue;
			}
			
			TranslateCodeSimpleResponse resp = new TranslateCodeSimpleResponse();

			CD e = new CD();
			e.setCodeSystem(next.split(" ")[0]);
			e.setCode(next.split(" ")[1]);

			resp.getCode().add(e);

			return resp;
		}

		throw new NullPointerException(theVcr.getCodeSystemId() + " / " + theVcr.getCodeId() + "/" + theVcr.getToCodeSystemId());
	}

}
