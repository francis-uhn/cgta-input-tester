package ca.cgta.input.model.inner;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonIgnore;


public class Cx {

	public String myAssigningAuthorityHspId;
	public String myAssigningAuthoritySystemId;
	public String myAssigningJurisdictionId;
	public String myAssigningJurisdictionText;
	public String myCheckDigit;
	public String myEffectiveDate;
	public String myExpirationDate;
	public String myIdNumber;
	public String myIdTypeCode;
	public String myIdTypeDescription;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object theObj) {
		if (!(theObj instanceof Cx)) {
			return false;
		}
		
		Cx o = (Cx) theObj;
		
		boolean retVal = ObjectUtils.equals(myIdNumber, o.myIdNumber);
		retVal &= ObjectUtils.equals(myIdTypeCode, o.myIdTypeCode);
		retVal &= ObjectUtils.equals(myAssigningAuthorityHspId, o.myAssigningAuthorityHspId);
		retVal &= ObjectUtils.equals(myAssigningAuthoritySystemId, o.myAssigningAuthoritySystemId);
		retVal &= ObjectUtils.equals(myAssigningJurisdictionId, o.myAssigningJurisdictionId);
		return retVal;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		HashCodeBuilder b = new HashCodeBuilder();
		b.append(myIdNumber);
		b.append(myIdTypeCode);
		b.append(myAssigningAuthorityHspId);
		b.append(myAssigningAuthoritySystemId);
		b.append(myAssigningJurisdictionId);
		return b.toHashCode();		
	}
	
	@JsonIgnore
	public String toKey() {
		if (myAssigningAuthorityHspId == null) {
			throw new IllegalStateException("Missing HSP ID");
		}
		if (myAssigningAuthoritySystemId == null) {
			throw new IllegalStateException("Missing System ID");
		}
		if (myIdNumber == null) {
			throw new IllegalStateException("Missing ID number");
		}
		return this.myAssigningAuthorityHspId + "___" + this.myAssigningAuthoritySystemId + "___" + this.myIdNumber;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder b= new StringBuilder();
		b.append('[');
		b.append("type=").append(myIdTypeCode);
		b.append(", id=").append(myIdNumber);
		
		if (StringUtils.isNotBlank(myAssigningJurisdictionId)) {
			b.append(", jur=").append(myAssigningJurisdictionId);
		}
		
		if (StringUtils.isNotBlank(myAssigningAuthorityHspId)) {
			b.append(", hsp=").append(myAssigningAuthorityHspId);
		}
		
		if (StringUtils.isNotBlank(myAssigningAuthoritySystemId)) {
			b.append(", system=").append(myAssigningAuthoritySystemId);
		}

		b.append(']');
		return b.toString();
	}

	
}
