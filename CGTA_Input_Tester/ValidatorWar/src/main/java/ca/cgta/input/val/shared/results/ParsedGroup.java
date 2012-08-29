package ca.cgta.input.val.shared.results;

import java.util.ArrayList;
import java.util.List;

public class ParsedGroup implements IStructure {

	private List<List<IStructure>> myChildren = new ArrayList<List<IStructure>>();

	private String myTerserPath;

	public void addChild(List<IStructure> theChild) {
    	assert theChild != null;
    	
		myChildren.add(theChild);
    }

	public List<List<IStructure>> getChildren() {
	    return myChildren;
    }
	
	public String getTerserPath() {
	    return myTerserPath;
    }

    public void setTerserPath(String theTerserPath) {
    	assert theTerserPath != null && theTerserPath.isEmpty() == false;
    	
    	myTerserPath = theTerserPath;
    }

	
}
