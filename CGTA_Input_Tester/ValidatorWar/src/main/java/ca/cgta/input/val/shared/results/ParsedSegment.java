package ca.cgta.input.val.shared.results;

import java.util.ArrayList;
import java.util.List;

public class ParsedSegment implements IStructure {

	private List<List<ParsedField>> myFieldRepetitions = new ArrayList<List<ParsedField>>();

	private String myName;
	private String myTerserPath;

	public void addField(List<ParsedField> theRepetitions) {
    	assert theRepetitions != null;
    	myFieldRepetitions.add(theRepetitions);
    }

	/**
     * @return the fieldRepetitions
     */
    public List<List<ParsedField>> getFieldRepetitions() {
    	return myFieldRepetitions;
    }

	/**
     * @return the name
     */
    public String getName() {
    	return myName;
    }

	public String getTerserPath() {
	    return myTerserPath;
    }

    /**
     * @param theName the name to set
     */
    public void setName(String theName) {
    	myName = theName;
    }

    public void setTerserPath(String theTerserPath) {
    	assert theTerserPath != null && theTerserPath.isEmpty() == false;
    	myTerserPath = theTerserPath;
    }
    
}
