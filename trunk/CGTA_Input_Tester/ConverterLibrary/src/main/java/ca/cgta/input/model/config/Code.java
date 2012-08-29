package ca.cgta.input.model.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.HashCodeBuilder;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {})
public class Code {

	public Code() {
	}


	public Code(String theCode, String theDescription) {
		setCode(theCode);
		setDescription(theDescription);
	}

	@XmlAttribute(name = "code")
	private String myCode;

	@XmlAttribute(name = "description")
	private String myDescription;


	/**
	 * @return the code
	 */
	public String getCode() {
		return myCode;
	}


	/**
	 * @param theCode
	 *            the code to set
	 */
	public void setCode(String theCode) {
		myCode = theCode;
	}


	/**
	 * @return the description
	 */
	public String getDescription() {
		return myDescription;
	}


	/**
	 * @param theDescription
	 *            the description to set
	 */
	public void setDescription(String theDescription) {
		myDescription = theDescription;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(getCode()).toHashCode();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object theObj) {
		if (!getClass().equals(theObj.getClass())) {
			return false;
		}
		return getCode().equals(((Code) theObj).getCode());
	}

}
