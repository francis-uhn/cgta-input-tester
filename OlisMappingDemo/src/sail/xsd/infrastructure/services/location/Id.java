package sail.xsd.infrastructure.services.location;

public class Id {

	private String myAuthority;
	private String myExt;
	private String myRoot;

	/**
     * @return the authority
     */
    public String getAuthority() {
    	return myAuthority;
    }


	/**
	 * @return the ext
	 */
	public String getExt() {
		return myExt;
	}

	/**
     * @return the root
     */
    public String getRoot() {
    	return myRoot;
    }

	public void setAuthority(String theNamespace) {
	    myAuthority = theNamespace;
    }

	public void setExt(String theString) {
		myExt = theString;
    }

	public void setRoot(String theString) {
		myRoot = theString;
    }


}
