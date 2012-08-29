package ca.cgta.input.model.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {})
public class SendingSystem extends Code {

	@XmlElement(name = "management_console_org_id")
	private String myManagementConsoleOrgId;
	
	@XmlElement(name = "drug_administration_codesystem_rxa5_9007")
	private List<String> myDrugAdministrationCodeSystemRxa5;

	@XmlElement(name = "drug_component_codesystem_rxc2_9007")
	private List<String> myDrugComponentCodeSystemRxc2;

	@XmlElement(name = "drug_give_codesystem_rxe2_9007")
	private List<String> myDrugGiveCodeSystemRxe2;

	@XmlElement(name = "request_codesystem_obr4_9007")
	private List<String> myRequestCodeSystemSystemObr4;

	@XmlElement(name = "result_codesystem_obx3_9007")
	private List<String> myResultCodeSystemSystemObx3;

	@XmlElement(name = "allergen_codesystem_iam3_9007")
	private List<String> myAllergenCodeSystemIam3;
	
    @XmlElement(name = "management_console_system_id")
	private String myManagementConsoleSystemId;
    
    

	/**
     * @return the allergenCodeSystemIam3
     */
    public List<String> getAllergenCodeSystemIam3() {
    	if (myAllergenCodeSystemIam3 == null) {
    		myAllergenCodeSystemIam3 = new ArrayList<String>();
    	}
    	return myAllergenCodeSystemIam3;
    }


	/**
	 * @return the drugAdministrationCodeSystemRxa5
	 */
	public List<String> getDrugAdministrationCodeSystemRxa5() {
		if (myDrugAdministrationCodeSystemRxa5 == null) {
			myDrugAdministrationCodeSystemRxa5 = new ArrayList<String>();
		}
		return myDrugAdministrationCodeSystemRxa5;
	}


	/**
	 * @return the drugComponentCodeSystemRxc2
	 */
	public List<String> getDrugComponentCodeSystemRxc2() {
		if (myDrugComponentCodeSystemRxc2 == null) {
			myDrugComponentCodeSystemRxc2 = new ArrayList<String>();
		}
		return myDrugComponentCodeSystemRxc2;
	}


	/**
	 * @return the drugGiveCodeSystemRxe2
	 */
	public List<String> getDrugGiveCodeSystemRxe2() {
		if (myDrugGiveCodeSystemRxe2 == null) {
			myDrugGiveCodeSystemRxe2 = new ArrayList<String>();
		}
		return myDrugGiveCodeSystemRxe2;
	}


	/**
	 * @return the requestCodeSystemSystemObr4
	 */
	public List<String> getRequestCodeSystemSystemObr4() {
		if (myRequestCodeSystemSystemObr4 == null) {
			myRequestCodeSystemSystemObr4 = new ArrayList<String>();
		}
		return myRequestCodeSystemSystemObr4;
	}


	/**
	 * @return the requestCodeSystemSystemObx3
	 */
	public List<String> getResultCodeSystemSystemObx3() {
		if (myResultCodeSystemSystemObx3 == null) {
			myResultCodeSystemSystemObx3 = new ArrayList<String>();
		}
		return myResultCodeSystemSystemObx3;
	}

	

	/**
     * @return the managementConsoleId
     */
    public String getManagementConsoleSystemId() {
    	return myManagementConsoleSystemId;
    }


	/**
     * @param theManagementConsoleId the managementConsoleId to set
     */
    public void setManagementConsoleSystemId(String theManagementConsoleId) {
    	myManagementConsoleSystemId = theManagementConsoleId;
    }


    /**
     * @return the managementConsoleId
     */
    public String getManagementConsoleOrgId() {
    	return myManagementConsoleOrgId;
    }


	/**
     * @param theManagementConsoleId the managementConsoleId to set
     */
    public void setManagementConsoleOrgId(String theManagementConsoleId) {
    	myManagementConsoleOrgId = theManagementConsoleId;
    }


}
