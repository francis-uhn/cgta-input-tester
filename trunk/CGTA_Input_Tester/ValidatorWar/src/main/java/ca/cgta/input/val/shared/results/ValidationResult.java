package ca.cgta.input.val.shared.results;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ValidationResult implements IsSerializable {

	private String myErrorMessage;
	private List<ParsedFailure> myFailures = new ArrayList<ParsedFailure>();
	private ParsedMessage myParsedMessage;


	public void addFailure(ParsedFailure theParsedFailure) {
		myFailures.add(theParsedFailure);
	}


	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return myErrorMessage;
	}


	/**
	 * @return the failures
	 */
	public List<ParsedFailure> getFailures() {
		return myFailures;
	}


	/**
	 * @return the parsedMessage
	 */
	public ParsedMessage getParsedMessage() {
		return myParsedMessage;
	}


	public void setParsedMessage(ParsedMessage theParsedMessage) {
		myParsedMessage = theParsedMessage;
	}


	public static ValidationResult getError(String theString) {
		ValidationResult retVal = new ValidationResult();
		retVal.myErrorMessage = theString;
		return retVal;
	}

}
