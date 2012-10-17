package ca.cgta.input.model.inner;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;


public class Ce {

	public String myCode = "";
	public String myText = "";
	public String myCodeSystem = "";


	/**
	 * {@inheritDoc}
	 */

	@Override
	public int hashCode() {
		return 1;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object theObj) {
//		return (theObj instanceof Ce) && myCode.equals(((Ce) theObj).myCode) && myCodeSystem.equals(((Ce) theObj).myCodeSystem);
	    
	    if (!(theObj instanceof Ce)){
	        return false;
	    }
	    
	    boolean sameCode = (StringUtils.isBlank(myCode))?(StringUtils.isBlank(((Ce) theObj).myCode)): myCode.equals(((Ce) theObj).myCode); 
	    
	    boolean sameCodeSys = (StringUtils.isBlank(myCodeSystem))?(StringUtils.isBlank(((Ce) theObj).myCodeSystem)): myCodeSystem.equals(((Ce) theObj).myCodeSystem);
	    
	    return (sameCode && sameCodeSys);
		
	}


	@JsonIgnore
	public boolean hasBlanks() {
	    return StringUtils.isBlank(myCode) || StringUtils.isBlank(myCodeSystem) || StringUtils.isBlank(myText);
    }


	@JsonIgnore
	public boolean hasCodeAndText() {
	    return StringUtils.isNotBlank(myCode) && StringUtils.isNotBlank(myText);
    }

}
