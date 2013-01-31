package sail.xsd.infrastructure.services.providerregistry;

public class Provider {

	private String myFirstName;
	private String myIdAuthority;
	private String myIdExtension;
	private String myIdRoot;
	private String myLastName;
	private String myMiddleName;


	public String getFirstName() {
		return myFirstName;
	}


	public String getIdAuthority() {
		return myIdAuthority;
	}


	public String getIdExtension() {
		return myIdExtension;
	}


	public String getIdRoot() {
		return myIdRoot;
	}


	public String getLastName() {
		return myLastName;
	}


	public String getMiddleName() {
		return myMiddleName;
	}


	/**
	 * @param theFirstName
	 *            the firstName to set
	 */
	public void setFirstName(String theFirstName) {
		myFirstName = theFirstName;
	}


	/**
	 * @param theIdAuthority
	 *            the idAuthority to set
	 */
	public void setIdAuthority(String theIdAuthority) {
		myIdAuthority = theIdAuthority;
	}


	/**
	 * @param theIdExtension
	 *            the idExtension to set
	 */
	public void setIdExtension(String theIdExtension) {
		myIdExtension = theIdExtension;
	}


	/**
	 * @param theIdRoot
	 *            the idRoot to set
	 */
	public void setIdRoot(String theIdRoot) {
		myIdRoot = theIdRoot;
	}


	/**
	 * @param theLastName
	 *            the lastName to set
	 */
	public void setLastName(String theLastName) {
		myLastName = theLastName;
	}


	/**
	 * @param theMiddleName
	 *            the middleName to set
	 */
	public void setMiddleName(String theMiddleName) {
		myMiddleName = theMiddleName;
	}

}
