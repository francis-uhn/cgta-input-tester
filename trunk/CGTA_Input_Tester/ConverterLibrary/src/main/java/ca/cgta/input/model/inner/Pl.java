package ca.cgta.input.model.inner;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonIgnore;

import ca.uhn.se.commons.util.ObjectUtil;

public class Pl {

	public String myPointOfCare;
	public String myRoom;
	public String myBed;
	public String myFacilityId;
	public String myFacilityName;
	public String myBuilding;
	public String myFloor;
	public String myLocationDescription;
	public String myHspId;
	public String myHspName;

	
	
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object theObj) {
        if (!(theObj instanceof Pl)) {
            return false;
        }
        
        Pl o = (Pl) theObj;
        
        boolean retVal = ObjectUtil.equals(myPointOfCare, o.myPointOfCare);
        retVal &= ObjectUtil.equals(myRoom, o.myRoom);
        retVal &= ObjectUtil.equals(myBed, o.myBed);
        retVal &= ObjectUtil.equals(myFacilityId, o.myFacilityId);
        retVal &= ObjectUtil.equals(myHspId, o.myHspId);
        return retVal;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        HashCodeBuilder b = new HashCodeBuilder();
        b.append(myPointOfCare);
        b.append(myRoom);
        b.append(myBed);
        b.append(myFacilityId);
        b.append(myHspId);
        return b.toHashCode();      
    }
    

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder b= new StringBuilder();
        b.append('[');
        b.append("poc=").append(myPointOfCare);
        b.append(", room=").append(myRoom);
        b.append(", bed=").append(myBed);
        b.append(", facId=").append(myFacilityId);
        b.append(", hspId=").append(myHspId);
        b.append(']');
        return b.toString();
    }
	
	
	
	
}
