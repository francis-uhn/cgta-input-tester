package ca.cgta.input.model.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {})
@XmlRootElement(name = "contributor")
public class Contributor {

	private static final Logger ourLog = LoggerFactory.getLogger(Contributor.class);
	
	@XmlElement(name = "dev_listen_port")
	private List<Integer> myDevListenPort;

	@XmlElement(name = "dev_security_token")
	private String myDevSecurityToken;

	@XmlAttribute(name = "hospital_facility_number")
	private String myHospitalFacilityNumber;

	@XmlElement(name = "hsp_facility_9005")
	private List<Code> myHspFacility;

	@XmlElement(name = "hsp_id_9004")
	private String myHspId9004;

	@XmlElement(name = "management_console_org_id")
	private String myManagementConsoleOrgId;

	@XmlElement(name = "mrn_pool_oid")
	private List<Code> myMrnPoolOids;

	@XmlAttribute(name = "name")
	private String myName;

	@XmlElement(name = "provider_pool_oid")
	private Code myProviderPoolOid;

	@XmlElement(name = "visit_number_pool_oid")
	private List<Code> myVisitNumberPoolOids;

	@XmlElement(name = "hsp_sending_system_9008")
	private List<SendingSystem> mySendingSystem9008;

	@XmlElement(name = "hrm_sending_facility_0362")
	private String myHrmSendingFacility0362;        
        
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object theObj) {
		if (!(theObj instanceof Contributor)) {
			return false;
		}
                return StringUtils.equals(myHspId9004, ((Contributor) theObj).myHspId9004);
	}

	/**
	 * @return the devListenPort
	 */
	public List<Integer> getDevListenPort() {
		if (myDevListenPort == null) {
			myDevListenPort = new ArrayList<Integer>();
		}
		return myDevListenPort;
	}


	/**
	 * @return the devSecurityToken
	 */
	public String getDevSecurityToken() {
		return myDevSecurityToken;
	}


	public String getFacilityNameWithOid(String theHspId) {
		for (Code next : myHspFacility) {
			if (next.getCode().equals(theHspId)) {
				return next.getDescription();
			}
		}

		List<String> set = new ArrayList<String>();
		for (Code next : myHspFacility) {
			set.add(next.getCode());
		}
		
		ourLog.info("Couldn't find facility ID '{}' in allowed values: {}", theHspId, set);
		
	    return null;
    }


	/**
     * @return the hospitalFacilityNumber
     */
    public String getHospitalFacilityNumber() {
    	return myHospitalFacilityNumber;
    }
	
	/**
	 * @return the hspFacility
	 */
	public List<Code> getHspFacility() {
		if (myHspFacility == null) {
			myHspFacility = new ArrayList<Code>();
		}
		return myHspFacility;
	}


	/**
	 * @return the hspId9004
	 */
	public String getHspId9004() {
		return myHspId9004;
	}


	/**
     * @return the managementConsoleOrgId
     */
    public String getManagementConsoleOrgId() {
    	return myManagementConsoleOrgId;
    }


	/**
     * @return the mrnPoolOid
     */
    public List<Code> getMrnPoolOid() {
    	if (myMrnPoolOids == null) {
    		myMrnPoolOids = new ArrayList<Code>();
    	}
    	return myMrnPoolOids;
    }


	/**
	 * @return the name
	 */
	public String getName() {
		return myName;
	}


	/**
     * @return the providerPoolOid
     */
    public String getProviderPoolOid() {
    	return myProviderPoolOid != null ? myProviderPoolOid.getCode() : null;
    }


	/**
	 * @return the sendingSystem
	 */
	public List<SendingSystem> getSendingSystem() {
		if (mySendingSystem9008 == null) {
			mySendingSystem9008 = new ArrayList<SendingSystem>();
		}
		return mySendingSystem9008;
	}


	public SendingSystem getSendingSystem9008WithOid(String theOid) {
		for (SendingSystem next : getSendingSystem()) {
			if (next.getCode().equals(theOid)) {
				return next;
			}
		}

		List<String> set = new ArrayList<String>();
		for (SendingSystem next : getSendingSystem()) {
			set.add(next.getCode());
		}
		
		ourLog.info("Couldn't find sending system ID '{}' in allowed values: {}", theOid, set);
		
		return null;
	}

	/**
	 * @return the HRM Sending Facility ID
	 */	
        public String getHrmSendingFacility() {
		return myHrmSendingFacility0362;
	}
        
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(myHspId9004).toHashCode();
	}


	/**
	 * This should just be a random string that can be provided to the sending system
	 */
	public void setDevSecurityToken(String theDevSecurityToken) {
		myDevSecurityToken = theDevSecurityToken;
	}


	/**
     * @param theHospitalFacilityNumber the hospitalFacilityNumber to set
     */
    public void setHospitalFacilityNumber(String theHospitalFacilityNumber) {
    	myHospitalFacilityNumber = theHospitalFacilityNumber;
    }


	/**
	 * The ID for this HSP as provided by eHO
	 */
	public void setHspId9004AndSubIds(String theHspId9004) {
		myHspId9004 = theHspId9004;
		setProviderPoolOidAndName(theHspId9004 + ".1");
		setVisitNumberPoolOidAndName(theHspId9004 + ".2");
	}


	/**
     * This is a management console mneumonic to identify the sending organization (this should be 
     * all letters with no punctuation or numubers)
     */
    public void setManagementConsoleOrgId(String theManagementConsoleOrgId) {
    	myManagementConsoleOrgId = theManagementConsoleOrgId;
    }

	/**
	 * Set the plain text name for this HSP
	 */
	public void setName(String theName) {
		myName = theName;
	}


	/**
     * @param theProviderPoolOid the providerPoolOid to set
     */
    public void setProviderPoolOidAndName(String theProviderPoolOid) {
    	myProviderPoolOid = new Code(theProviderPoolOid, myName + " Provider IDs");
    }
	
	/**
     * @param theVisitNumberPoolOid the providerPoolOid to set
     */
    public void setVisitNumberPoolOidAndName(String theVisitNumberPoolOid) {
    	getVisitNumberPoolOids().add(new Code(theVisitNumberPoolOid, myName + " Visit/Encounter Number/IDs"));
    }

	public List<Code> getVisitNumberPoolOids() {
		if (myVisitNumberPoolOids == null) {
			myVisitNumberPoolOids = new ArrayList<Code>();
		}
	    return myVisitNumberPoolOids;
    }

     /**
     * @param theHrmSendingFacility the HRM Sending Facility to set
     */
    public void setHrmSendingFacility(String theHrmSendingFacility) {
    	myHrmSendingFacility0362 = theHrmSendingFacility;
    }        
}
