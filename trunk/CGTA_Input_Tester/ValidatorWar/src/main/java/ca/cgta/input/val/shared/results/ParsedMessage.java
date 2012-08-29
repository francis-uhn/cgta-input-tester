package ca.cgta.input.val.shared.results;

import java.util.List;

public class ParsedMessage extends ParsedGroup {

	private char myComponentSeparator;
	private char myEscapeChar;
	private String myFieldSeparator;
	private char myRepetitionSeparator;
	private char mySubComponentSeparator;

	public ParsedMessage() {
    }

	/** Copy constructor */
	public ParsedMessage(ParsedGroup theGroup) {
		setTerserPath(theGroup.getTerserPath());
		for (List<IStructure> nextChild : theGroup.getChildren()) {
			addChild(nextChild);
		}
		
		ParsedSegment mshSegment = (ParsedSegment) theGroup.getChildren().get(0).get(0);
		myFieldSeparator = mshSegment.getFieldRepetitions().get(0).get(0).getValueIfLeaf();
		myComponentSeparator = mshSegment.getFieldRepetitions().get(1).get(0).getValueIfLeaf().charAt(0);
		myRepetitionSeparator = mshSegment.getFieldRepetitions().get(1).get(0).getValueIfLeaf().charAt(1);
		myEscapeChar = mshSegment.getFieldRepetitions().get(1).get(0).getValueIfLeaf().charAt(2);
		mySubComponentSeparator = mshSegment.getFieldRepetitions().get(1).get(0).getValueIfLeaf().charAt(3);

		
    }

	/**
     * @return the componentSeparator
     */
    public char getComponentSeparator() {
    	return myComponentSeparator;
    }

	/**
     * @return the escapeChar
     */
    public char getEscapeChar() {
    	return myEscapeChar;
    }

	/**
     * @return the fieldSeparator
     */
    public String getFieldSeparator() {
    	return myFieldSeparator;
    }

	/**
     * @return the repetitionSeparator
     */
    public char getRepetitionSeparator() {
    	return myRepetitionSeparator;
    }

	/**
     * @return the subComponentSeparator
     */
    public char getSubComponentSeparator() {
    	return mySubComponentSeparator;
    }

	
}
