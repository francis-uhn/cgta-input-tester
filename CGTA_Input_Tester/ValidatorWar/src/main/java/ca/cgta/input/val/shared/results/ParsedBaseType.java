package ca.cgta.input.val.shared.results;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ParsedBaseType<T> implements IsSerializable {

	private List<T> myChildren = new ArrayList<T>();
	private String myValueIfLeaf;


	/**
	 * @param theChildren
	 *            the children to set
	 */
	public void addChild(T theChild) {
		myChildren.add(theChild);
	}


	/**
	 * @return the children
	 */
	public List<T> getChildren() {
		return myChildren;
	}


	/**
	 * @return the valueIfLeaf
	 */
	public String getValueIfLeaf() {
		return myValueIfLeaf;
	}


	/**
	 * @param theValueIfLeaf
	 *            the valueIfLeaf to set
	 */
	public void setValueIfLeaf(String theValueIfLeaf) {
		myValueIfLeaf = theValueIfLeaf;
	}

}
