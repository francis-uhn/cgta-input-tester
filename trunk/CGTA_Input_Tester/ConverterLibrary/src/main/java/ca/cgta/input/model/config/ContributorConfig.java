package ca.cgta.input.model.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.cgta.input.model.config.Contributor.HspType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {})
@XmlRootElement(name = "contributor_config")
public class ContributorConfig {

	private static final Logger ourLog = LoggerFactory.getLogger(ContributorConfig.class);

	@XmlElement(name = "contributor")
	private List<Contributor> myContributors;

	@XmlTransient
	private HashMap<String, Contributor> myHospitalFacilityNumberToContributor;

	@XmlTransient
	private Map<String, Contributor> myHspIdToContributor = new HashMap<String, Contributor>();
        
        @XmlTransient
	private Map<String, Contributor> myHrmIdToContributor = new HashMap<String, Contributor>();        

	@XmlTransient
	private HashMap<String, Contributor> myMrnPoolIdToContributor;

	@XmlElement(name = "other_oid")
	private List<Code> myOtherOids;

	@XmlElement(name = "provider_oid_9001")
	private List<Code> myProviderId9001;

	@XmlTransient
	private Map<String, Code> myProviderId9001ToProvider;

	@XmlTransient
	private Map<String, Contributor> myProviderPoolIdToContributor;


	/**
	 * @return the contributors
	 */
	public List<Contributor> getContributors() {
		if (myContributors == null) {
			myContributors = new ArrayList<Contributor>();
		}
		return myContributors;
	}


	/**
	 * @return the hospitalFacilityNumberToContributor
	 */
	public HashMap<String, Contributor> getHospitalFacilityNumberToContributor() {
		if (myHospitalFacilityNumberToContributor == null) {
			myHospitalFacilityNumberToContributor = new HashMap<String, Contributor>();
			for (Contributor next : myContributors) {
				if (StringUtils.isNotBlank(next.getHospitalFacilityNumber())) {
					myHospitalFacilityNumberToContributor.put(next.getHospitalFacilityNumber(), next);
				}
			}
		}
		return myHospitalFacilityNumberToContributor;
	}


	public Map<String, Contributor> getHspId9004ToContributor() {
		if (myHspIdToContributor.isEmpty()) {
			for (Contributor next : myContributors) {
				myHspIdToContributor.put(next.getHspId9004(), next);
			}
		}
		return myHspIdToContributor;
	}
        
        public Map<String, Contributor> getHrmId0362ToContributor() {
		if (myHrmIdToContributor.isEmpty()) {
			for (Contributor next : myContributors) {
				myHrmIdToContributor.put(next.getHrmSendingFacility(), next);
			}
		}
		return myHrmIdToContributor;
	}


	/**
	 * @return the providerPoolIdToContributor
	 */
	public Map<String, Contributor> getMrnPoolIdToContributor() {
		if (myMrnPoolIdToContributor == null) {
			myMrnPoolIdToContributor = new HashMap<String, Contributor>();
			for (Contributor next : myContributors) {
				for (Code nextCode : next.getMrnPoolOid()) {
					myMrnPoolIdToContributor.put(nextCode.getCode(), next);
				}
			}
		}
		return myMrnPoolIdToContributor;
	}


	public String getNameOfHspId9004(String theValue) {
		for (Contributor next : myContributors) {
			if (next.getHspId9004().equals(theValue)) {
				return next.getName();
			}
		}

		List<String> set = new ArrayList<String>();
		for (Contributor next : myContributors) {
			set.add(next.getHspId9004());
		}

		ourLog.info("Couldn't find HSP 9004 ID '{}' in allowed values: {}", theValue, set);

		return null;
	}


	/**
	 * @return the otherOids
	 */
	public List<Code> getOtherOids() {
		if (myOtherOids == null) {
			myOtherOids = new ArrayList<Code>();
		}
		return myOtherOids;
	}


	/**
	 * @return the providerId9001
	 */
	public List<Code> getProviderId9001() {
		if (myProviderId9001 == null) {
			myProviderId9001 = new ArrayList<Code>();
		}
		return myProviderId9001;
	}


	/**
	 * @return the providerId9001
	 */
	public Map<String, Code> getProviderId9001ToProvider() {
		if (myProviderId9001ToProvider == null) {
			myProviderId9001ToProvider = new HashMap<String, Code>();
			for (Code next : getProviderId9001()) {
				myProviderId9001ToProvider.put(next.getCode(), next);
			}
		}
		return myProviderId9001ToProvider;
	}


	/**
	 * @return the providerPoolIdToContributor
	 */
	public Map<String, Contributor> getProviderPoolIdToContributor() {
		if (myProviderPoolIdToContributor == null) {
			myProviderPoolIdToContributor = new HashMap<String, Contributor>();
			for (Contributor next : myContributors) {
				if (next.getProviderPoolOid() != null) {
					myProviderPoolIdToContributor.put(next.getProviderPoolOid(), next);
				}
			}
		}
		return myProviderPoolIdToContributor;
	}


	public void validate() throws ValidationException {
		Set<String> codeSystems = new HashSet<String>();
		Set<String> contributorIds = new HashSet<String>();
		Set<String> facilityIds = new HashSet<String>();
		Set<String> sendingSystemIds = new HashSet<String>();
		Set<String> hospitalFacilityIds = new HashSet<String>();
		Set<String> mrnIds = new HashSet<String>();
		Set<String> visitIds = new HashSet<String>();
		Set<String> otherIds = new HashSet<String>();
		// Note: if you add another map, add it to the global check at the
		// bottom of this method!!

		Set<Integer> listenPorts = new HashSet<Integer>();

		checkForDuplicate(otherIds, getOtherOids());

		for (Contributor nextContributor : myContributors) {
			checkForDuplicate(contributorIds, nextContributor.getHspId9004());

			if (StringUtils.isNotBlank(nextContributor.getHospitalFacilityNumber())) {
				checkForDuplicate(hospitalFacilityIds, nextContributor.getHospitalFacilityNumber());
			}

			checkForDuplicate(mrnIds, nextContributor.getMrnPoolOid());
			checkForDuplicate(visitIds, nextContributor.getVisitNumberPoolOids());

			for (Integer next : nextContributor.getDevListenPort()) {
				if (listenPorts.contains(next)) {
					throw new ValidationException("Duplicate listen port: " + next);
				} else {
					listenPorts.add(next);
				}
			}

			for (Code nextFacility : nextContributor.getHspFacility()) {
				checkForDuplicate(facilityIds, nextFacility.getCode());
				checkForStartsWith(nextFacility.getCode(), nextContributor.getHspId9004() + ".100.");
			}

			int ssIndex = 0;
			for (SendingSystem nextSendingSystem : nextContributor.getSendingSystem()) {
				ssIndex++;

				checkNotBlank(nextSendingSystem.getCode(), "Sending system code for sending system " + ssIndex + " in org " + nextContributor.getHspId9004());
				checkNotBlank(nextSendingSystem.getDescription(), "Sending system description");

				if (StringUtils.isBlank(nextContributor.getManagementConsoleOrgId())) {
					checkNotBlank(nextSendingSystem.getManagementConsoleOrgId(), nextSendingSystem.getDescription() + " doesn't have a management console ORG ID for either itself or for it's HSP definition: " + nextContributor.getHspId9004());
					if (nextSendingSystem.getManagementConsoleOrgId() != null && nextSendingSystem.getManagementConsoleOrgId().contains("/")) {
						throw new ValidationException("Illegal character in: " + nextSendingSystem.getManagementConsoleOrgId());
					}
				} else if (nextContributor.getManagementConsoleOrgId().contains("/")) {
					throw new ValidationException("Illegal character in: " + nextContributor.getManagementConsoleOrgId());
				}

				checkNotBlank(nextSendingSystem.getManagementConsoleSystemId(), "management_console_system_id for " + nextSendingSystem.getDescription());

				if (nextSendingSystem.getManagementConsoleSystemId() != null && nextSendingSystem.getManagementConsoleSystemId().contains("/")) {
					throw new ValidationException("Illegal character in: " + nextSendingSystem.getManagementConsoleSystemId());
				}

				checkForDuplicate(sendingSystemIds, nextSendingSystem.getCode());
				checkForStartsWith(nextSendingSystem.getCode(), nextContributor.getHspId9004() + ".101.");

				for (String nextCode : nextSendingSystem.getAllergenCodeSystemIam3()) {
					checkForDuplicate(codeSystems, nextCode);
					checkForStartsWith(nextCode, nextContributor.getHspId9004() + ".102.");
				}
				for (String nextCode : nextSendingSystem.getDrugAdministrationCodeSystemRxa5()) {
					checkForDuplicate(codeSystems, nextCode);
					checkForStartsWith(nextCode, nextContributor.getHspId9004() + ".102.");
				}
				for (String nextCode : nextSendingSystem.getDrugComponentCodeSystemRxc2()) {
					checkForDuplicate(codeSystems, nextCode);
					checkForStartsWith(nextCode, nextContributor.getHspId9004() + ".102.");
				}
				for (String nextCode : nextSendingSystem.getDrugGiveCodeSystemRxe2()) {
					checkForDuplicate(codeSystems, nextCode);
					checkForStartsWith(nextCode, nextContributor.getHspId9004() + ".102.");
				}
				for (String nextCode : nextSendingSystem.getRequestCodeSystemSystemObr4()) {
					checkForDuplicate(codeSystems, nextCode);
					checkForStartsWith(nextCode, nextContributor.getHspId9004() + ".102.");
				}
				for (String nextCode : nextSendingSystem.getResultCodeSystemSystemObx3()) {
					checkForDuplicate(codeSystems, nextCode);
					checkForStartsWith(nextCode, nextContributor.getHspId9004() + ".102.");
				}

			} // sending system

			if ("2.16.840.1.113883.3.239.23.1".equals(nextContributor.getHspId9004())) {
				// OACCAC has no facilities
			}else if (nextContributor.getSendingSystem().size() > 0 && nextContributor.getHspFacility().size() == 0) {
				throw new ValidationException("Contributor \"" + nextContributor.getName() + "\" does not have any facilities defined");
			}

		} // contributor

		Set<String> allOids = new HashSet<String>();
		checkForDuplicate(allOids, codeSystems);
		checkForDuplicate(allOids, contributorIds);
		checkForDuplicate(allOids, facilityIds);
		checkForDuplicate(allOids, sendingSystemIds);
		checkForDuplicate(allOids, hospitalFacilityIds);
		checkForDuplicate(allOids, mrnIds);
		checkForDuplicate(allOids, visitIds);
		checkForDuplicate(allOids, otherIds);

	}


	private static void addProviderType(ContributorConfig theCfg, String theCode, String theDescription) {
		theCfg.getProviderId9001().add(new Code(theCode, theDescription));
	}


	private static void checkForDuplicate(Set<String> theExistingIds, List<Code> theCodes) throws ValidationException {
		for (Code code : theCodes) {
			checkForDuplicate(theExistingIds, code.getCode());
		}
	}


	private static void checkForDuplicate(Set<String> theAllOids, Set<String> theNextSubOidSet) throws ValidationException {
		for (String next : theNextSubOidSet) {
			checkForDuplicate(theAllOids, next);
		}
	}


	private static void checkForDuplicate(Set<String> theIdsMap, String theId) throws ValidationException {
		if (theIdsMap.contains(theId) && !theId.endsWith("XXXX")) {
			throw new ValidationException("Duplicate ID: " + theId);
		} else {
			theIdsMap.add(theId);
		}
	}


	private static void checkForStartsWith(String theCode, String theStartsWith) throws ValidationException {
		if (!theCode.startsWith(theStartsWith)) {
			throw new ValidationException("Code \"" + theCode + "\" must start with \"" + theStartsWith + "\"");
		}
	}


	private static void checkNotBlank(String theId, String theDescription) throws ValidationException {
		if (StringUtils.isBlank(theId)) {
			throw new ValidationException("Missing ID in " + theDescription);
		}
	}


	/**
	 * Modify this method in order to add HSPs or assign OIDs.
	 * 
	 * When modifications have been made, run the main method to validate and
	 * store the changes.
	 */
	public static void main(String[] args) throws JAXBException, ValidationException, FileNotFoundException, IOException {

		ContributorConfig cfg = new ContributorConfig();
		cfg.getOtherOids().add(new Code("2.16.840.1.113883.3.239.22.1", "ConnectingGTA HIAL (System) - Also Root OID for HIAL derived OIDs"));
		cfg.getOtherOids().add(new Code("2.16.840.1.113883.3.239.22.2", "ConnectingGTA CDR (System)"));
		cfg.getOtherOids().add(new Code("2.16.840.1.113883.3.239.22.3", "UHN eMPI eCID (Client ID Namespace)"));
		cfg.getOtherOids().add(new Code("2.16.840.1.113883.3.239.22.4", "UHN Provider Registry ePID (Provider ID Namespace)"));
		cfg.getOtherOids().add(new Code("2.16.840.1.113883.3.239.22.5", "(Reserved)")); // This was labelled "Unafiiliated".. What was it for?
		cfg.getOtherOids().add(new Code("2.16.840.1.113883.3.239.22.6", "Base OID for CDR Input Specification HL7 v2 Tables"));
		//cfg.getOtherOids().add(new Code("2.16.840.1.113883.3.239.22.7", "UHN CR"));
		cfg.getOtherOids().add(new Code("2.16.840.1.113883.3.239.22.8", "UHN PR"));
		cfg.getOtherOids().add(new Code("2.16.840.1.113883.3.239.22.1.2", "HIAL LDAP"));
		cfg.getOtherOids().add(new Code("2.16.840.1.113883.3.239.22.9", "HRM"));
		cfg.getOtherOids().add(new Code("2.16.840.1.113883.3.239.22.10", "CGTA_CONSENT_MANAGEMENT_REGISTRY"));
		cfg.getOtherOids().add(new Code("2.16.840.1.113883.3.239.22.11", "HIPAAT"));
		cfg.getOtherOids().add(new Code("2.16.840.1.113883.3.239.22.12", "ConnectingGTA ID ROOT"));

		
		addProviderType(cfg, "1.3.6.1.4.1.12201.1.2.1.5", "Site Specific Provider ID (Deprocated)");
		addProviderType(cfg, "2.16.840.1.113883.3.239.13.6", "College of Audiologist and Speech-Language Pathologists of Ontario Licence Number");
		addProviderType(cfg, "2.16.840.1.113883.3.239.13.17", "College of Chiropodists of Ontario Licence Number");
		addProviderType(cfg, "2.16.840.1.113883.4.394", "College of Chiropractors of Ontario Licence Number");
		addProviderType(cfg, "2.16.840.1.113883.3.239.13.7", "College of Dental Hygienists of Ontario Licence Number");
		addProviderType(cfg, "2.16.840.1.113883.3.239.13.52", "Royal College of Dental Surgeons of Ontario Licence Number");
		addProviderType(cfg, "2.16.840.1.113883.3.239.13.10", "College of Dental Technologists of Ontario Licence Number");
		addProviderType(cfg, "2.16.840.1.113883.3.239.13.8", "College of Denturists of Ontario Licence Number");
		addProviderType(cfg, "2.16.840.1.113883.3.239.13.9", "College of Dieticians of Ontario Licence Number");
		addProviderType(cfg, "2.16.840.1.113883.3.239.13.14", "College of Massage Therapist of Ontario Licence Number");
		addProviderType(cfg, "2.16.840.1.113883.3.239.13.11", "College of Medical Laboratory Technologists of Ontario");
		addProviderType(cfg, "2.16.840.1.113883.3.239.13.13", "College of Radiation Technologists of Ontario Licence Number");
		addProviderType(cfg, "2.16.840.1.113883.3.239.13.12", "College of Midwives of Ontario Licence Number");
		addProviderType(cfg, "2.16.840.1.113883.3.239.13.4", "Board of Regents, Board of Directors of Drugless Therapy-Naturopathy Licence Number");
		addProviderType(cfg, "2.16.840.1.113883.3.239.13.15", "College of Nurses of Ontario Licence Number");
		addProviderType(cfg, "2.16.840.1.113883.3.239.13.18", "College of Occupational Therapists of Ontario Licence Number");
		addProviderType(cfg, "2.16.840.1.113883.4.390", "College of Opticians of Ontario Licence Number");
		addProviderType(cfg, "2.16.840.1.113883.4.395", "College of Optometrists of Ontario Licence Number");
		addProviderType(cfg, "2.16.840.1.113883.3.239.13.43", "Ontario College of Pharmacists Licence Number");
		addProviderType(cfg, "2.16.840.1.113883.4.347", "College of Physicians and Surgeons of Ontario Licence Number");
		addProviderType(cfg, "2.16.840.1.113883.4.388", "College of Physiotherapists of Ontario Licence Number");
		addProviderType(cfg, "2.16.840.1.113883.3.239.13.71", "College of Psychologists of Ontario Licence Number");
		addProviderType(cfg, "2.16.840.1.113883.3.239.13.19", "College of Respiratory Therapists in Ontario Licence Number");
		addProviderType(cfg, "2.16.840.1.113883.3.239.13.44", "Ontario College of Social Workers and Social Service Workers  Licence Number");
		addProviderType(cfg, "2.16.840.1.113883.3.239.13.20", "College of Traditional Chinese Medicine Practitioners and Acupuncturists of Ontario Licence Number");
		addProviderType(cfg, "2.16.840.1.113883.3.239.13.54", "Unregulated - Transitional Council of the College of Homeopaths Licence Number");
		addProviderType(cfg, "2.16.840.1.113883.3.239.13.55", "Unregulated - Transitional council for the College of Kinesiologists of Ontario Licence Number");
		addProviderType(cfg, "2.16.840.1.113883.3.239.13.56", "Unregulated - Transitional Council of the College of Psychotherapists Licence Number");

		// .1 - Provider ID pool
		// .2 - Visit/Encounter number pool
		// .100.x - Facilities
		// .101.x - Systems
		// .102.x - Code Systems

		// *******************************************************
		// Fake Testing Site
		// *******************************************************
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);

			contributor.setName("Testing Organization");
			contributor.setDevSecurityToken("383525aaa");
			contributor.setHspId9004AndSubIds("1.3.6.1.4.1.12201.999");
			contributor.getDevListenPort().add(13999);
			

			contributor.getHspFacility().add(new Code("1.3.6.1.4.1.12201.999.100.1", "Testing Hospital - North Campus"));
			contributor.getHspFacility().add(new Code("1.3.6.1.4.1.12201.999.100.2", "Testing Hospital - South Campus"));

			SendingSystem epr = new SendingSystem();
			epr.setCode("1.3.6.1.4.1.12201.999.101.1");
			epr.setDescription("Testing Medical Record Software");
			epr.setManagementConsoleOrgId("Test");
			epr.setManagementConsoleSystemId("TestMRS");
			epr.getAllergenCodeSystemIam3().add("1.3.6.1.4.1.12201.999.102.1");
			epr.getDrugAdministrationCodeSystemRxa5().add("1.3.6.1.4.1.12201.999.102.2");
			epr.getDrugComponentCodeSystemRxc2().add("1.3.6.1.4.1.12201.999.102.3");
			epr.getDrugGiveCodeSystemRxe2().add("1.3.6.1.4.1.12201.999.102.4");
			epr.getRequestCodeSystemSystemObr4().add("1.3.6.1.4.1.12201.999.102.5");
			epr.getResultCodeSystemSystemObx3().add("1.3.6.1.4.1.12201.999.102.6");

			contributor.getSendingSystem().add(epr);
		}

		// *******************************************************
		// UHN
		// *******************************************************
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);

			contributor.setName("University Health Network");
			contributor.setAddressLine1("R. Fraser Elliott Building, 1st Floor");
			contributor.setAddressLine2("190 Elizabeth St.");
			contributor.setCity("Toronto");
			contributor.setProvince("ON"); 
			contributor.setPostalCode("M5G 2C4");
			contributor.setHspType(HspType.HOSPITAL);
			
			contributor.setDevSecurityToken("2954864636aaa");
			contributor.setHspId9004AndSubIds("1.3.6.1.4.1.12201");
			contributor.setHospitalFacilityNumber("0947");
			contributor.getMrnPoolOid().add(new Code("2.16.840.1.113883.3.239.18.148", contributor.getName() + " MRNs"));
			contributor.getDevListenPort().add(14018);

			contributor.getHspFacility().add(new Code("1.3.6.1.4.1.12201.100.1", "Toronto General Hospital"));
			contributor.getHspFacility().add(new Code("1.3.6.1.4.1.12201.100.2", "Toronto Western Hospital"));
			contributor.getHspFacility().add(new Code("1.3.6.1.4.1.12201.100.3", "Princess Margaret Hospital"));
			contributor.getHspFacility().add(new Code("1.3.6.1.4.1.12201.100.4", "Toronto Rehab Institute"));

			SendingSystem epr = new SendingSystem();
			epr.setCode("1.3.6.1.4.1.12201.101.1");
			epr.setDescription("Quadramed CPR HIS");
			epr.setManagementConsoleOrgId("SIMS");
			epr.setManagementConsoleSystemId("EPR");
			epr.getAllergenCodeSystemIam3().add("1.3.6.1.4.1.12201.102.1");
			epr.getDrugAdministrationCodeSystemRxa5().add("1.3.6.1.4.1.12201.102.2");
			epr.getDrugComponentCodeSystemRxc2().add("1.3.6.1.4.1.12201.102.3");
			epr.getDrugGiveCodeSystemRxe2().add("1.3.6.1.4.1.12201.102.4");
			epr.getRequestCodeSystemSystemObr4().add("1.3.6.1.4.1.12201.102.5");
			epr.getResultCodeSystemSystemObx3().add("1.3.6.1.4.1.12201.102.6"); // for result codes
			epr.getResultCodeSystemSystemObx3().add("1.3.6.1.4.1.12201.102.13"); // for assessment codes
			contributor.getSendingSystem().add(epr);

			SendingSystem caps = new SendingSystem();
			caps.setCode("1.3.6.1.4.1.12201.101.2");
			caps.setDescription("CAPS (Cardiology)");
			caps.setManagementConsoleOrgId("SIMS");
			caps.setManagementConsoleSystemId("CAPS");
			caps.getRequestCodeSystemSystemObr4().add("1.3.6.1.4.1.12201.102.7");
			caps.getResultCodeSystemSystemObx3().add("1.3.6.1.4.1.12201.102.8");
			contributor.getSendingSystem().add(caps);

			SendingSystem hpf = new SendingSystem();
			hpf.setCode("1.3.6.1.4.1.12201.101.3");
			hpf.setDescription("HPF (Scanned Documents)");
			hpf.setManagementConsoleOrgId("SIMS");
			hpf.setManagementConsoleSystemId("HPF");
			hpf.getRequestCodeSystemSystemObr4().add("1.3.6.1.4.1.12201.102.9");
			hpf.getResultCodeSystemSystemObx3().add("1.3.6.1.4.1.12201.102.10");
			contributor.getSendingSystem().add(hpf);

			// MUSE
			SendingSystem muse = new SendingSystem();
			muse.setCode("1.3.6.1.4.1.12201.101.4");
			muse.setDescription("MUSE");
			muse.setManagementConsoleOrgId("SIMS");
			muse.setManagementConsoleSystemId("MUSE");
			muse.getRequestCodeSystemSystemObr4().add("1.3.6.1.4.1.12201.102.11");
			muse.getResultCodeSystemSystemObx3().add("1.3.6.1.4.1.12201.102.12");
			contributor.getSendingSystem().add(muse);

			SendingSystem centricity = new SendingSystem();
			centricity.setCode("1.3.6.1.4.1.12201.101.5");
			centricity.setDescription("Centricity Pharmacy");
			centricity.setManagementConsoleOrgId("SIMS");
			centricity.setManagementConsoleSystemId("RxTFC");
			centricity.getDrugGiveCodeSystemRxe2().add("1.3.6.1.4.1.12201.102.14");
			
		}

		// *******************************************************
		// Women's College Hospital
		// *******************************************************
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);

			contributor.setName("Women's College Hospital");
			contributor.setDevSecurityToken("323452erwettw");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.57");
			contributor.getMrnPoolOid().add(new Code("2.16.840.1.113883.3.239.18.157", contributor.getName() + " MRNs"));
			contributor.getDevListenPort().add(14048);
			contributor.getHspFacility().add(new Code("2.16.840.1.113883.3.239.23.57.100.1", "Women's College Hospital"));

			SendingSystem epic = new SendingSystem();
			epic.setCode("2.16.840.1.113883.3.239.23.57.101.1");
			epic.setDescription("EPIC");
			epic.setManagementConsoleOrgId("WCH");
			epic.setManagementConsoleSystemId("EPIC");
			contributor.getSendingSystem().add(epic);
		}
		// *******************************************************
		// NYGH
		// *******************************************************
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);

			contributor.setName("North York General Hospital");
			contributor.setDevSecurityToken("38272347521144bbb");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.8");
			contributor.getMrnPoolOid().add(new Code("2.16.840.1.113883.3.239.18.115", contributor.getName() + " MRNs"));
			contributor.setHospitalFacilityNumber("0632");
			contributor.getDevListenPort().add(14010);
			contributor.setManagementConsoleOrgId("NYGH");

			// Should be the value from table 9004 with ".100.x" at the end
			contributor.getHspFacility().add(new Code("2.16.840.1.113883.3.239.23.8.100.1", "North York General Hospital"));

			// Cerner
			SendingSystem cerner = new SendingSystem();

			// Should be the value from table 9004 with ".101.x" at the end
			cerner.setCode("2.16.840.1.113883.3.239.23.8.101.1");
			cerner.setDescription("Cerner HIS");
			cerner.setManagementConsoleSystemId("Cerner");

			// Should be the value from table 9004 with ".102.x" at the end
			cerner.getAllergenCodeSystemIam3().add("2.16.840.1.113883.3.239.23.8.102.1");
			cerner.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.8.102.2");
			cerner.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.8.102.3");

			contributor.getSendingSystem().add(cerner);

			// Dictaphone
			SendingSystem dictaphone = new SendingSystem();

			// Should be the value from table 9004 with ".101.x" at the end
			dictaphone.setCode("2.16.840.1.113883.3.239.23.8.101.2");
			dictaphone.setDescription("Dictaphone");
			dictaphone.setManagementConsoleSystemId("Dictaphone");

			// Should be the value from table 9004 with ".102.x" at the end
			dictaphone.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.8.102.4");
			dictaphone.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.8.102.5");

			contributor.getSendingSystem().add(dictaphone);

		}

		// Individual CCACs
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);
			contributor.setName("Central West CCAC");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.1.100.1");
		}
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);
			contributor.setName("Mississauga Halton CCAC");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.1.100.2");
		}
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);
			contributor.setName("Toronto Central CCAC");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.1.100.3");
		}
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);
			contributor.setName("Central CCAC");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.1.100.4");
		}
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);
			contributor.setName("Central East CCAC");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.1.100.5");
		}
		
		// *******************************************************
		// OACCAC
		// *******************************************************
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);

			contributor.getDevListenPort().add(14001);
			contributor.getDevListenPort().add(14002);
			contributor.getDevListenPort().add(14003);
			contributor.getDevListenPort().add(14004);
			contributor.getDevListenPort().add(14005);
			contributor.getDevListenPort().add(14006);

			contributor.setName("Ontario Association of Community Care Access Centres (OACCAC)");
			contributor.setDevSecurityToken("3498749864ccc");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.1");
			contributor.setManagementConsoleOrgId("OACCAC");

			// CHRIS - DEV testing environment
			SendingSystem chris = new SendingSystem();
			// Should be the value from table 9008 with ".101.x" at the end
			chris.setCode("2.16.840.1.113883.3.239.23.1.101.1");
			chris.setDescription("CHRIS");
			chris.setManagementConsoleSystemId("CHRIS");
			// Should be the value from table 9007 with ".102.x" at the end
			chris.getAllergenCodeSystemIam3().add("2.16.840.1.113883.3.239.23.1.102.1");
			chris.getDrugAdministrationCodeSystemRxa5().add("2.16.840.1.113883.3.239.23.1.102.2");
			chris.getDrugComponentCodeSystemRxc2().add("2.16.840.1.113883.3.239.23.1.102.3");
			chris.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.1.102.4");
			chris.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.1.102.5");
			contributor.getSendingSystem().add(chris);

			// PCC
			SendingSystem pcc = new SendingSystem();
			// Should be the value from table 9008 with ".101.x" at the end
			pcc.setCode("2.16.840.1.113883.3.239.23.1.101.2");
			pcc.setDescription("Point Click Care");
			pcc.setManagementConsoleSystemId("PCC");
			// Should be the value from table 9007 with ".102.x" at the end
			pcc.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.1.102.6");
			pcc.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.1.102.7");
			contributor.getSendingSystem().add(pcc);

			// Strata
			SendingSystem strata = new SendingSystem();
			// Should be the value from table 9008 with ".101.x" at the end
			strata.setCode("2.16.840.1.113883.3.239.23.1.101.3");
			strata.setDescription("Strata Health Pathways");
			strata.setManagementConsoleSystemId("Strata");
			// Should be the value from table 9007 with ".102.x" at the end
			strata.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.1.102.8");
			strata.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.1.102.9");
			contributor.getSendingSystem().add(strata);

		}
		// *******************************************************
		// OACCAC-QA
		// *******************************************************
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);

			contributor.getDevListenPort().add(14039);
			contributor.getDevListenPort().add(14040);
			contributor.getDevListenPort().add(14041);

			contributor.setName("Ontario Association of Community Care Access Centres (OACCAC) - QA");
			contributor.setDevSecurityToken("2385472197mmm");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.22.7");
			contributor.setManagementConsoleOrgId("OACCAC_QA");

			// Should be the value from table 9005 with ".100.x" at the end
			contributor.getHspFacility().add(new Code("2.16.840.1.113883.3.239.22.7.100.1", "Central West CCAC"));
			contributor.getHspFacility().add(new Code("2.16.840.1.113883.3.239.22.7.100.2", "Mississauga Halton CCAC"));
			contributor.getHspFacility().add(new Code("2.16.840.1.113883.3.239.22.7.100.3", "Toronto Central CCAC"));
			contributor.getHspFacility().add(new Code("2.16.840.1.113883.3.239.22.7.100.4", "Central CCAC"));
			contributor.getHspFacility().add(new Code("2.16.840.1.113883.3.239.22.7.100.5", "Central East CCAC"));
			
			// CHRIS-QA testing environment
			SendingSystem chris = new SendingSystem();
			// Should be the value from table 9008 with ".101.x" at the end
			chris.setCode("2.16.840.1.113883.3.239.22.7.101.1");
			chris.setDescription("CHRIS");
			chris.setManagementConsoleSystemId("CHRIS");
			// Should be the value from table 9007 with ".102.x" at the end
			chris.getAllergenCodeSystemIam3().add("2.16.840.1.113883.3.239.22.7.102.1");
			chris.getDrugAdministrationCodeSystemRxa5().add("2.16.840.1.113883.3.239.22.7.102.2");
			chris.getDrugComponentCodeSystemRxc2().add("2.16.840.1.113883.3.239.22.7.102.3");
			chris.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.22.7.102.4");
			chris.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.22.7.102.5");
			contributor.getSendingSystem().add(chris);
		}

		// *******************************************************
		// Lakeridge
		// *******************************************************
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);

			contributor.getDevListenPort().add(14011);

			contributor.setName("Lakeridge Health");
			contributor.setDevSecurityToken("3498742963125ddd");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.2");
			contributor.setHospitalFacilityNumber("0952");
			contributor.getMrnPoolOid().add(new Code("2.16.840.1.113883.3.239.18.90", "Lakeridge Port Perry MRNs"));
			contributor.getMrnPoolOid().add(new Code("2.16.840.1.113883.3.239.18.91", "Lakeridge Whitby MRNs"));
			contributor.getMrnPoolOid().add(new Code("2.16.840.1.113883.3.239.18.92", "Lakeridge Bowmanville MRNs"));
			contributor.getMrnPoolOid().add(new Code("2.16.840.1.113883.3.239.18.93", "Lakeridge Oshawa MRNs"));

			contributor.setManagementConsoleOrgId("LHC");
			contributor.getHspFacility().add(new Code("2.16.840.1.113883.3.239.23.2.100.1", "Lakeridge Health Oshawa"));
			contributor.getHspFacility().add(new Code("2.16.840.1.113883.3.239.23.2.100.2", "Lakeridge Health Bowmanville"));
			contributor.getHspFacility().add(new Code("2.16.840.1.113883.3.239.23.2.100.3", "Lakeridge Health Port Perry"));
			contributor.getHspFacility().add(new Code("2.16.840.1.113883.3.239.23.2.100.4", "Lakeridge Whitby"));

			// MEDITECH Magic
			SendingSystem meditech_m = new SendingSystem();
			// Should be the value from table 9008 with ".101.x" at the end
			meditech_m.setCode("2.16.840.1.113883.3.239.23.2.101.1");
			meditech_m.setDescription("MEDITECH Magic");
			meditech_m.setManagementConsoleSystemId("MeditechMagic");
			// Should be the value from table 9007 with ".102.x" at the end
			meditech_m.getAllergenCodeSystemIam3().add("2.16.840.1.113883.3.239.23.2.102.1");
			meditech_m.getDrugAdministrationCodeSystemRxa5().add("2.16.840.1.113883.3.239.23.2.102.2");
			meditech_m.getDrugComponentCodeSystemRxc2().add("2.16.840.1.113883.3.239.23.2.102.3");
			meditech_m.getDrugGiveCodeSystemRxe2().add("2.16.840.1.113883.3.239.23.2.102.8");

			meditech_m.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.2.102.4");
			meditech_m.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.2.102.5");
			contributor.getSendingSystem().add(meditech_m);

			// MEDITECH Client/Server
			SendingSystem meditech_cs = new SendingSystem();
			// Should be the value from table 9008 with ".101.x" at the end
			meditech_cs.setCode("2.16.840.1.113883.3.239.23.2.101.2");
			meditech_cs.setDescription("MEDITECH Client/Server");
			meditech_cs.setManagementConsoleSystemId("MeditechCS");
			meditech_cs.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.2.102.6");
			meditech_cs.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.2.102.7");
			contributor.getSendingSystem().add(meditech_cs);

			// OnBase
			SendingSystem onbase = new SendingSystem();
			// Should be the value from table 9008 with ".101.x" at the end
			onbase.setCode("2.16.840.1.113883.3.239.23.2.101.3");
			onbase.setDescription("Onbase");
			onbase.setManagementConsoleSystemId("Onbase");
			onbase.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.2.102.9");
			onbase.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.2.102.10");
			contributor.getSendingSystem().add(onbase);

		}
		// *******************************************************
		// Scarborough
		// *******************************************************
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);

			contributor.getDevListenPort().add(14013);

			contributor.setName("The Scarborough Hospital");
			contributor.setDevSecurityToken("38731785345ddd");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.3");
			contributor.getMrnPoolOid().add(new Code("2.16.840.1.113883.3.239.18.135", contributor.getName() + " MRNs"));
			contributor.setHospitalFacilityNumber("0960");
			contributor.setManagementConsoleOrgId("TSH");
			contributor.getHspFacility().add(new Code("2.16.840.1.113883.3.239.23.3.100.1", "The Scarborough Hospital - Birchmount Campus"));
			contributor.getHspFacility().add(new Code("2.16.840.1.113883.3.239.23.3.100.2", "The Scarborough Hospital - General Campus"));

			// MEDITECH Magic
			SendingSystem meditech_m = new SendingSystem();
			// Should be the value from table 9008 with ".101.x" at the end
			meditech_m.setCode("2.16.840.1.113883.3.239.23.3.101.1");
			meditech_m.setDescription("MEDITECH Magic");
			meditech_m.setManagementConsoleSystemId("MeditechMagic");
			// Should be the value from table 9007 with ".102.x" at the end
			meditech_m.getAllergenCodeSystemIam3().add("2.16.840.1.113883.3.239.23.3.102.1");
			meditech_m.getDrugAdministrationCodeSystemRxa5().add("2.16.840.1.113883.3.239.23.3.102.2");
			meditech_m.getDrugComponentCodeSystemRxc2().add("2.16.840.1.113883.3.239.23.3.102.3");
			meditech_m.getDrugGiveCodeSystemRxe2().add("2.16.840.1.113883.3.239.23.3.102.12");

			meditech_m.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.3.102.4");
			meditech_m.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.3.102.5");
			contributor.getSendingSystem().add(meditech_m);

			// Accentus
			SendingSystem accentus = new SendingSystem();
			// Should be the value from table 9008 with ".101.x" at the end
			accentus.setCode("2.16.840.1.113883.3.239.23.3.101.2");
			accentus.setDescription("Accentus");
			accentus.setManagementConsoleSystemId("Accentus");
			accentus.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.3.102.6");
			accentus.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.3.102.7");
			contributor.getSendingSystem().add(accentus);

			// OnBase
			SendingSystem onbase = new SendingSystem();
			// Should be the value from table 9008 with ".101.x" at the end
			onbase.setCode("2.16.840.1.113883.3.239.23.3.101.3");
			onbase.setDescription("Onbase");
			onbase.setManagementConsoleSystemId("Onbase");
			onbase.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.3.102.8");
			onbase.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.3.102.9");
			contributor.getSendingSystem().add(onbase);

			// Powerscribe
			SendingSystem powerscribe = new SendingSystem();
			// Should be the value from table 9008 with ".101.x" at the end
			powerscribe.setCode("2.16.840.1.113883.3.239.23.3.101.4");
			powerscribe.setDescription("Powerscribe");
			powerscribe.setManagementConsoleSystemId("Powerscribe");
			powerscribe.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.3.102.10");
			powerscribe.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.3.102.11");
			contributor.getSendingSystem().add(powerscribe);

		}
		// *******************************************************
		// Rouge Valley
		// *******************************************************
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);

			contributor.getDevListenPort().add(14012);
			contributor.getDevListenPort().add(14035);

			contributor.setName("The Rouge Valley Health System");
			contributor.setDevSecurityToken("32986342953eee");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.4");
			contributor.getMrnPoolOid().add(new Code("2.16.840.1.113883.3.239.18.131", "Rouge Valley Ajax MRNs"));
			contributor.getMrnPoolOid().add(new Code("2.16.840.1.113883.3.239.18.132", "Rouge Valley Centenary MRNs"));

			contributor.setHospitalFacilityNumber("0954");
			contributor.setManagementConsoleOrgId("RVHS");
			contributor.getHspFacility().add(new Code("2.16.840.1.113883.3.239.23.4.100.1", "Rouge Valley Health System - Ajax and Pickering"));
			contributor.getHspFacility().add(new Code("2.16.840.1.113883.3.239.23.4.100.2", "Rouge Valley Health System - Centenary Site"));

			// MEDITECH Magic
			SendingSystem meditech_m = new SendingSystem();
			// Should be the value from table 9008 with ".101.x" at the end
			meditech_m.setCode("2.16.840.1.113883.3.239.23.4.101.1");
			meditech_m.setDescription("MEDITECH Magic");
			meditech_m.setManagementConsoleSystemId("MeditechMagic");
			// Should be the value from table 9007 with ".102.x" at the end
			meditech_m.getAllergenCodeSystemIam3().add("2.16.840.1.113883.3.239.23.4.102.1");
			meditech_m.getDrugAdministrationCodeSystemRxa5().add("2.16.840.1.113883.3.239.23.4.102.2");
			meditech_m.getDrugComponentCodeSystemRxc2().add("2.16.840.1.113883.3.239.23.4.102.3");
			meditech_m.getDrugGiveCodeSystemRxe2().add("2.16.840.1.113883.3.239.23.4.102.8");

			meditech_m.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.4.102.4");
			meditech_m.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.4.102.5");
			contributor.getSendingSystem().add(meditech_m);

			// MEDITECH Client/Server
			SendingSystem meditech_cs = new SendingSystem();
			// Should be the value from table 9008 with ".101.x" at the end
			meditech_cs.setCode("2.16.840.1.113883.3.239.23.4.101.2");
			meditech_cs.setDescription("MEDITECH Client/Server");
			meditech_cs.setManagementConsoleSystemId("MeditechCS");
			meditech_cs.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.4.102.6");
			meditech_cs.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.4.102.7");
			contributor.getSendingSystem().add(meditech_cs);

			
			// CKM
			SendingSystem ckm = new SendingSystem();
			// Should be the value from table 9008 with ".101.x" at the end
			ckm.setCode("2.16.840.1.113883.3.239.23.4.101.3");
			ckm.setDescription("CKM");
			ckm.setManagementConsoleSystemId("CKM");
			ckm.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.4.102.9");
			ckm.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.4.102.10");
			contributor.getSendingSystem().add(ckm);

			// CVIS
			SendingSystem cvis = new SendingSystem();
			// Should be the value from table 9008 with ".101.x" at the end
			cvis.setCode("2.16.840.1.113883.3.239.23.4.101.4");
			cvis.setDescription("Cardio Vascular Information System");
			cvis.setManagementConsoleSystemId("CVIS");
			cvis.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.4.102.11");
			cvis.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.4.102.12");
			contributor.getSendingSystem().add(cvis);


		}
		// *******************************************************
		// Saint Michael's Hospital
		// *******************************************************
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);

			contributor.getDevListenPort().add(14019);

			contributor.setName("Saint Michael's Hospital");
			contributor.setDevSecurityToken("1238973875fff");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.5");
			contributor.getMrnPoolOid().add(new Code("2.16.840.1.113883.3.239.18.142", contributor.getName() + " MRNs"));
			contributor.setHospitalFacilityNumber("0852");
			contributor.setManagementConsoleOrgId("SMH");

			// Should be the value from table 9005 with ".100.x" at the end
			contributor.getHspFacility().add(new Code("2.16.840.1.113883.3.239.23.5.100.1", "St. Michael's Hospital"));

			// Soarian
			SendingSystem soarian = new SendingSystem();
			// Should be the value from table 9008 with ".101.x" at the end
			soarian.setCode("2.16.840.1.113883.3.239.23.5.101.1");
			soarian.setDescription("Soarian Clinicals");
			soarian.setManagementConsoleSystemId("Soarian");
			// Should be the value from table 9007 with ".102.x" at the end
			soarian.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.5.102.1");
			soarian.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.5.102.2");
			soarian.getAllergenCodeSystemIam3().add("2.16.840.1.113883.3.239.23.5.102.9");
			soarian.getDrugAdministrationCodeSystemRxa5().add("2.16.840.1.113883.3.239.23.5.102.17");
			soarian.getDrugComponentCodeSystemRxc2().add("2.16.840.1.113883.3.239.23.5.102.18");
			soarian.getDrugGiveCodeSystemRxe2().add("2.16.840.1.113883.3.239.23.5.102.19");
			contributor.getSendingSystem().add(soarian);

			// Sovera
			SendingSystem sovera = new SendingSystem();
			// Should be the value from table 9008 with ".101.x" at the end
			sovera.setCode("2.16.840.1.113883.3.239.23.5.101.2");
			sovera.setDescription("Sovera");
			sovera.setManagementConsoleSystemId("Sovera");
			// Should be the value from table 9007 with ".102.x" at the end
			sovera.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.5.102.3");
			sovera.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.5.102.4");
			contributor.getSendingSystem().add(sovera);

			// Syngo
			SendingSystem syngo = new SendingSystem();
			// Should be the value from table 9008 with ".101.x" at the end
			syngo.setCode("2.16.840.1.113883.3.239.23.5.101.3");
			syngo.setDescription("Syngo RIS");
			syngo.setManagementConsoleSystemId("Syngo");
			// Should be the value from table 9007 with ".102.x" at the end
			syngo.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.5.102.5");
			syngo.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.5.102.6");
			contributor.getSendingSystem().add(syngo);

			// Softmed
			SendingSystem softmed = new SendingSystem();
			// Should be the value from table 9008 with ".101.x" at the end
			softmed.setCode("2.16.840.1.113883.3.239.23.5.101.4");
			softmed.setDescription("SoftMed");
			softmed.setManagementConsoleSystemId("SoftMed");
			// Should be the value from table 9007 with ".102.x" at the end
			softmed.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.5.102.7");
			softmed.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.5.102.8");
			softmed.getDrugAdministrationCodeSystemRxa5().add("2.16.840.1.113883.3.239.23.5.102.10");
			softmed.getDrugComponentCodeSystemRxc2().add("2.16.840.1.113883.3.239.23.5.102.11");
			softmed.getDrugGiveCodeSystemRxe2().add("2.16.840.1.113883.3.239.23.5.102.12");
			contributor.getSendingSystem().add(softmed);

			// MS4
			SendingSystem ms4 = new SendingSystem();
			// Should be the value from table 9008 with ".101.x" at the end
			ms4.setCode("2.16.840.1.113883.3.239.23.5.101.5");
			ms4.setDescription("MS4");
			ms4.setManagementConsoleSystemId("MS4");
			contributor.getSendingSystem().add(ms4);

			// MUSE
			SendingSystem muse = new SendingSystem();
			// Should be the value from table 9008 with ".101.x" at the end
			muse.setCode("2.16.840.1.113883.3.239.23.5.101.6");
			muse.setDescription("MUSE");
			muse.setManagementConsoleSystemId("MUSE");
			// Should be the value from table 9007 with ".102.x" at the end
			muse.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.5.102.13");
			muse.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.5.102.14");
			contributor.getSendingSystem().add(muse);

			// XCelera
			SendingSystem xcelera = new SendingSystem();
			// Should be the value from table 9008 with ".101.x" at the end
			xcelera.setCode("2.16.840.1.113883.3.239.23.5.101.7");
			xcelera.setDescription("XCelera");
			xcelera.setManagementConsoleSystemId("XCelera");
			// Should be the value from table 9007 with ".102.x" at the end
			xcelera.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.5.102.15");
			xcelera.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.5.102.16");
			contributor.getSendingSystem().add(xcelera);

			// Pharmacy
			SendingSystem pharmacy = new SendingSystem();
			// Should be the value from table 9008 with ".101.x" at the end
			pharmacy.setCode("2.16.840.1.113883.3.239.23.5.101.8");
			pharmacy.setDescription("Pharmacy");
			pharmacy.setManagementConsoleSystemId("Pharmacy");
			// Should be the value from table 9007 with ".102.x" at the end
			pharmacy.getAllergenCodeSystemIam3().add("2.16.840.1.113883.3.239.23.5.102.20");
			pharmacy.getDrugAdministrationCodeSystemRxa5().add("2.16.840.1.113883.3.239.23.5.102.21");
			pharmacy.getDrugComponentCodeSystemRxc2().add("2.16.840.1.113883.3.239.23.5.102.22");
			pharmacy.getDrugGiveCodeSystemRxe2().add("2.16.840.1.113883.3.239.23.5.102.23");
			contributor.getSendingSystem().add(pharmacy);

		}
		// *******************************************************
		// MSH
		// *******************************************************
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);

			contributor.getDevListenPort().add(14017);

			contributor.setName("Mount Sinai Hospital Joseph and Wolf Lebovic Health Complex");
			contributor.setDevSecurityToken("35364554ggg");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.6");
			contributor.getMrnPoolOid().add(new Code("2.16.840.1.113883.3.239.18.97", contributor.getName() + " MRNs"));
			contributor.setHospitalFacilityNumber("0842");
			contributor.setManagementConsoleOrgId("MSH");
			contributor.getHspFacility().add(new Code("2.16.840.1.113883.3.239.23.6.100.1", "Mount Sinai Hospital Joseph and Wolf Lebovic Health Complex"));

			// Cerner
			SendingSystem cerner = new SendingSystem();
			// Should be the value from table 9004 with ".101.x" at the end
			cerner.setCode("2.16.840.1.113883.3.239.23.6.101.1");
			cerner.setDescription("Cerner HIS");
			cerner.setManagementConsoleSystemId("Cerner");
			// Should be the value from table 9004 with ".102.x" at the end
			cerner.getAllergenCodeSystemIam3().add("2.16.840.1.113883.3.239.23.6.102.1");
			cerner.getDrugAdministrationCodeSystemRxa5().add("2.16.840.1.113883.3.239.23.6.102.2");
			cerner.getDrugComponentCodeSystemRxc2().add("2.16.840.1.113883.3.239.23.6.102.3");
			cerner.getDrugGiveCodeSystemRxe2().add("2.16.840.1.113883.3.239.23.6.102.10");
			cerner.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.6.102.4");
			cerner.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.6.102.5");
			contributor.getSendingSystem().add(cerner);

			// Dictaphone
			SendingSystem dictaphone = new SendingSystem();
			// Should be the value from table 9004 with ".101.x" at the end
			dictaphone.setCode("2.16.840.1.113883.3.239.23.6.101.2");
			dictaphone.setDescription("Dictaphone");
			dictaphone.setManagementConsoleSystemId("Dictaphone");
			// Should be the value from table 9004 with ".102.x" at the end
			dictaphone.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.6.102.6");
			dictaphone.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.6.102.7");
			contributor.getSendingSystem().add(dictaphone);

			// EFilm Medical
			SendingSystem efilm = new SendingSystem();
			// Should be the value from table 9004 with ".101.x" at the end
			efilm.setCode("2.16.840.1.113883.3.239.23.6.101.3");
			efilm.setDescription("eFilm Medical");
			efilm.setManagementConsoleSystemId("eFilm Medical");
			// Should be the value from table 9004 with ".102.x" at the end
			efilm.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.6.102.8");
			efilm.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.6.102.9");
			contributor.getSendingSystem().add(efilm);

		}
		// *******************************************************
		// Sunnybrook
		// *******************************************************
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);

			contributor.getDevListenPort().add(14020);
			contributor.getDevListenPort().add(14021);
			contributor.getDevListenPort().add(14022);
			contributor.getDevListenPort().add(14023);
			contributor.getDevListenPort().add(14024);
			contributor.getDevListenPort().add(14025);
			contributor.getDevListenPort().add(14026);
			contributor.getDevListenPort().add(14027);
			contributor.getDevListenPort().add(14028);
			contributor.getDevListenPort().add(14029);
			contributor.getDevListenPort().add(14030);
			contributor.getDevListenPort().add(14031);
			contributor.getDevListenPort().add(14032);
			contributor.getDevListenPort().add(14033);
			contributor.getDevListenPort().add(14034);

			contributor.setName("Sunnybrook Health Sciences Centre");
			contributor.setManagementConsoleOrgId("SHSC");
			contributor.setDevSecurityToken("23498643698hhh");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.7");
			contributor.setHospitalFacilityNumber("0857");
			contributor.getMrnPoolOid().add(new Code("2.16.840.1.113883.3.239.18.144", contributor.getName() + " MRNs"));

			contributor.getHspFacility().add(new Code("2.16.840.1.113883.3.239.23.7.100.1", "Sunnybrook Health Sciences Centre"));

			// PCS/ADS
			SendingSystem pcs_ads = new SendingSystem();
			// Should be the value from table 9004 with ".101.x" at the end
			pcs_ads.setCode("2.16.840.1.113883.3.239.23.7.101.11");
			pcs_ads.setDescription("PCS/ADS");
			pcs_ads.setManagementConsoleSystemId("PCS_ADS");
			// Should be the value from table 9004 with ".102.x" at the end
			contributor.getSendingSystem().add(pcs_ads);

			// WoRx
			SendingSystem worx = new SendingSystem();
			// Should be the value from table 9004 with ".101.x" at the end
			worx.setCode("2.16.840.1.113883.3.239.23.7.101.1");
			worx.setDescription("WoRx");
			worx.setManagementConsoleSystemId("WoRx");
			// Should be the value from table 9004 with ".102.x" at the end
			worx.getDrugAdministrationCodeSystemRxa5().add("2.16.840.1.113883.3.239.23.7.102.2");
			worx.getDrugComponentCodeSystemRxc2().add("2.16.840.1.113883.3.239.23.7.102.3");
			worx.getDrugGiveCodeSystemRxe2().add("2.16.840.1.113883.3.239.23.7.102.4");
			worx.getAllergenCodeSystemIam3().add("2.16.840.1.113883.3.239.23.7.102.1");
			contributor.getSendingSystem().add(worx);

			// Dictaphone
			SendingSystem dictaphone = new SendingSystem();
			// Should be the value from table 9004 with ".101.x" at the end
			dictaphone.setCode("2.16.840.1.113883.3.239.23.7.101.2");
			dictaphone.setDescription("Dictaphone");
			dictaphone.setManagementConsoleSystemId("Dictaphone");
			// Should be the value from table 9004 with ".102.x" at the end
			dictaphone.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.7.102.5");
			dictaphone.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.7.102.6");
			contributor.getSendingSystem().add(dictaphone);

			// Viasys
			SendingSystem viasys = new SendingSystem();
			// Should be the value from table 9004 with ".101.x" at the end
			viasys.setCode("2.16.840.1.113883.3.239.23.7.101.3");
			viasys.setDescription("Viasys");
			viasys.setManagementConsoleSystemId("Viasys");
			// Should be the value from table 9004 with ".102.x" at the end
			viasys.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.7.102.7");
			viasys.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.7.102.8");
			contributor.getSendingSystem().add(viasys);

			// EDIS
			SendingSystem edis = new SendingSystem();
			// Should be the value from table 9004 with ".101.x" at the end
			edis.setCode("2.16.840.1.113883.3.239.23.7.101.4");
			edis.setDescription("EDIS");
			edis.setManagementConsoleSystemId("EDIS");
			// Should be the value from table 9004 with ".102.x" at the end
			edis.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.7.102.9");
			edis.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.7.102.10");
			contributor.getSendingSystem().add(edis);

			// Sovera
			SendingSystem sovera = new SendingSystem();
			// Should be the value from table 9004 with ".101.x" at the end
			sovera.setCode("2.16.840.1.113883.3.239.23.7.101.5");
			sovera.setDescription("Sovera");
			sovera.setManagementConsoleSystemId("Sovera");
			// Should be the value from table 9004 with ".102.x" at the end
			sovera.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.7.102.11");
			sovera.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.7.102.12");
			contributor.getSendingSystem().add(sovera);

			// Tracemaster ECG
			SendingSystem tracemaster = new SendingSystem();
			// Should be the value from table 9004 with ".101.x" at the end
			tracemaster.setCode("2.16.840.1.113883.3.239.23.7.101.6");
			tracemaster.setDescription("Tracemaster ECG");
			tracemaster.setManagementConsoleSystemId("TracemasterECG");
			// Should be the value from table 9004 with ".102.x" at the end
			tracemaster.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.7.102.13");
			tracemaster.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.7.102.14");
			contributor.getSendingSystem().add(tracemaster);

			// XCelera
			SendingSystem xcelera = new SendingSystem();
			// Should be the value from table 9004 with ".101.x" at the end
			xcelera.setCode("2.16.840.1.113883.3.239.23.7.101.7");
			xcelera.setDescription("XCelera");
			xcelera.setManagementConsoleSystemId("XCelera");
			// Should be the value from table 9004 with ".102.x" at the end
			xcelera.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.7.102.15");
			xcelera.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.7.102.16");
			contributor.getSendingSystem().add(xcelera);

			// AGFA Radiology
			SendingSystem agfa = new SendingSystem();
			// Should be the value from table 9004 with ".101.x" at the end
			agfa.setCode("2.16.840.1.113883.3.239.23.7.101.8");
			agfa.setDescription("AGFA Radiology");
			agfa.setManagementConsoleSystemId("AGFARadiology");
			// Should be the value from table 9004 with ".102.x" at the end
			agfa.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.7.102.17");
			agfa.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.7.102.18");
			contributor.getSendingSystem().add(agfa);

			// eDischarge
			SendingSystem edischarge = new SendingSystem();
			// Should be the value from table 9004 with ".101.x" at the end
			edischarge.setCode("2.16.840.1.113883.3.239.23.7.101.9");
			edischarge.setDescription("eDischarge");
			edischarge.setManagementConsoleSystemId("eDischarge");
			// Should be the value from table 9004 with ".102.x" at the end
			edischarge.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.7.102.19");
			edischarge.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.7.102.20");
			contributor.getSendingSystem().add(edischarge);

			// ICNET
			SendingSystem icnet = new SendingSystem();
			// Should be the value from table 9004 with ".101.x" at the end
			icnet.setCode("2.16.840.1.113883.3.239.23.7.101.10");
			icnet.setDescription("ICNet");
			icnet.setManagementConsoleSystemId("ICNet");
			// Should be the value from table 9004 with ".102.x" at the end
			icnet.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.7.102.21");
			icnet.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.7.102.22");
			contributor.getSendingSystem().add(icnet);

		}
		// *******************************************************
		// Credit Valley Hospital
		// *******************************************************
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);

			contributor.getDevListenPort().add(14015);

			contributor.setName("Credit Valley Hospital");
			contributor.setDevSecurityToken("340975709474jjj");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.9");
			contributor.setHospitalFacilityNumber("0731");
			contributor.setManagementConsoleOrgId("CVH");
			contributor.getMrnPoolOid().add(new Code("2.16.840.1.113883.3.239.18.26", contributor.getName() + " MRNs"));

			contributor.getHspFacility().add(new Code("2.16.840.1.113883.3.239.23.9.100.1", "Credit Valley Hospital and Trillium Health Centre -- Credit Valley Site"));

			// Meditech
			SendingSystem meditech = new SendingSystem();
			// Should be the value from table 9004 with ".101.x" at the end
			meditech.setCode("2.16.840.1.113883.3.239.23.9.101.1");
			meditech.setDescription("Meditech");
			meditech.setManagementConsoleSystemId("Meditech");

			// Should be the value from table 9004 with ".102.x" at the end
			meditech.getAllergenCodeSystemIam3().add("2.16.840.1.113883.3.239.23.9.102.1");
			meditech.getDrugAdministrationCodeSystemRxa5().add("2.16.840.1.113883.3.239.23.9.102.2");
			meditech.getDrugComponentCodeSystemRxc2().add("2.16.840.1.113883.3.239.23.9.102.3");
			meditech.getDrugGiveCodeSystemRxe2().add("2.16.840.1.113883.3.239.23.9.102.22");
			meditech.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.9.102.4");
			meditech.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.9.102.5");
			contributor.getSendingSystem().add(meditech);

			// MUSE
			SendingSystem muse = new SendingSystem();
			// Should be the value from table 9004 with ".101.x" at the end
			muse.setCode("2.16.840.1.113883.3.239.23.9.101.2");
			muse.setDescription("MUSE");
			muse.setManagementConsoleSystemId("MUSE");
			// Should be the value from table 9004 with ".102.x" at the end
			muse.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.9.102.6");
			muse.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.9.102.7");
			contributor.getSendingSystem().add(muse);

			// Heartlab
			SendingSystem heartlab = new SendingSystem();
			// Should be the value from table 9004 with ".101.x" at the end
			heartlab.setCode("2.16.840.1.113883.3.239.23.9.101.3");
			heartlab.setDescription("Heartlab");
			heartlab.setManagementConsoleSystemId("Heartlab");
			// Should be the value from table 9004 with ".102.x" at the end
			heartlab.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.9.102.8");
			heartlab.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.9.102.9");
			contributor.getSendingSystem().add(heartlab);

			// AGFA
			SendingSystem agfa = new SendingSystem();
			// Should be the value from table 9004 with ".101.x" at the end
			agfa.setCode("2.16.840.1.113883.3.239.23.9.101.4");
			agfa.setDescription("AGFA");
			agfa.setManagementConsoleSystemId("AGFA");
			// Should be the value from table 9004 with ".102.x" at the end
			agfa.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.9.102.10");
			agfa.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.9.102.11");
			contributor.getSendingSystem().add(agfa);

			// Varian
			SendingSystem varian = new SendingSystem();
			// Should be the value from table 9004 with ".101.x" at the end
			varian.setCode("2.16.840.1.113883.3.239.23.9.101.5");
			varian.setDescription("Varian");
			varian.setManagementConsoleSystemId("Varian");
			// Should be the value from table 9004 with ".102.x" at the end
			varian.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.9.102.12");
			varian.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.9.102.13");
			contributor.getSendingSystem().add(varian);

			// Endoworks
			SendingSystem endoworks = new SendingSystem();
			// Should be the value from table 9004 with ".101.x" at the end
			endoworks.setCode("2.16.840.1.113883.3.239.23.9.101.6");
			endoworks.setDescription("Endoworks");
			endoworks.setManagementConsoleSystemId("Endoworks");
			// Should be the value from table 9004 with ".102.x" at the end
			endoworks.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.9.102.14");
			endoworks.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.9.102.15");
			contributor.getSendingSystem().add(endoworks);

		}
		// *******************************************************
		// Trillium
		// *******************************************************
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);

			contributor.getDevListenPort().add(14016);
			contributor.getDevListenPort().add(14036);
			contributor.getDevListenPort().add(14037);
			contributor.getDevListenPort().add(14038);

			contributor.setName("Trillium Health Centre");
			contributor.setDevSecurityToken("2387527832459kkk");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.10");
			contributor.setManagementConsoleOrgId("THC");
			contributor.setHospitalFacilityNumber("0975");
			contributor.getMrnPoolOid().add(new Code("2.16.840.1.113883.3.239.18.146", "Trillium Mississauga MRNs"));
			contributor.getMrnPoolOid().add(new Code("2.16.840.1.113883.3.239.18.147", "Trillium Queensway MRNs"));

			contributor.getHspFacility().add(new Code("2.16.840.1.113883.3.239.23.10.100.1", "Credit Valley Hospital and Trillium Health Centre -- Mississauga Site"));
			contributor.getHspFacility().add(new Code("2.16.840.1.113883.3.239.23.10.100.2", "Credit Valley Hospital and Trillium Health Centre -- West Toronto Site"));

			// Meditech
			SendingSystem meditech = new SendingSystem();
			// Should be the value from table 9004 with ".101.x" at the end
			meditech.setCode("2.16.840.1.113883.3.239.23.10.101.1");
			meditech.setDescription("Meditech");
			meditech.setManagementConsoleSystemId("Meditech");

			// Should be the value from table 9004 with ".102.x" at the end
			meditech.getAllergenCodeSystemIam3().add("2.16.840.1.113883.3.239.23.10.102.1");
			meditech.getDrugAdministrationCodeSystemRxa5().add("2.16.840.1.113883.3.239.23.10.102.2");
			meditech.getDrugComponentCodeSystemRxc2().add("2.16.840.1.113883.3.239.23.10.102.3");
			meditech.getDrugGiveCodeSystemRxe2().add("2.16.840.1.113883.3.239.23.10.102.12");
			meditech.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.10.102.4");
			meditech.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.10.102.5");
			contributor.getSendingSystem().add(meditech);

			// Medquist
			SendingSystem medquist = new SendingSystem();
			// Should be the value from table 9004 with ".101.x" at the end
			medquist.setCode("2.16.840.1.113883.3.239.23.10.101.2");
			medquist.setDescription("Medquist");
			medquist.setManagementConsoleSystemId("Medquist");
			// Should be the value from table 9004 with ".102.x" at the end
			medquist.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.10.102.6");
			medquist.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.10.102.7");
			contributor.getSendingSystem().add(medquist);

			// MUSE
			SendingSystem muse = new SendingSystem();
			// Should be the value from table 9004 with ".101.x" at the end
			muse.setCode("2.16.840.1.113883.3.239.23.10.101.3");
			muse.setDescription("MUSE");
			muse.setManagementConsoleSystemId("MUSE");
			// Should be the value from table 9004 with ".102.x" at the end
			muse.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.10.102.8");
			muse.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.10.102.9");
			contributor.getSendingSystem().add(muse);

			// VMax
			SendingSystem vmax = new SendingSystem();
			// Should be the value from table 9004 with ".101.x" at the end
			vmax.setCode("2.16.840.1.113883.3.239.23.10.101.4");
			vmax.setDescription("VMax");
			vmax.setManagementConsoleSystemId("VMax");
			// Should be the value from table 9004 with ".102.x" at the end
			vmax.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.10.102.10");
			vmax.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.10.102.11");
			contributor.getSendingSystem().add(vmax);

		}
		// *******************************************************
		// William Osler
		// *******************************************************
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);

			contributor.getDevListenPort().add(14014);

			contributor.setName("William Osler Health Centre");
			contributor.setManagementConsoleOrgId("WOHC");
			contributor.setDevSecurityToken("3240956904lll");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.11");
			contributor.setHospitalFacilityNumber("0951");
			contributor.getMrnPoolOid().add(new Code("2.16.840.1.113883.3.239.18.150", contributor.getName() + " MRNs"));
            contributor.setHrmSendingFacility("4052");

			contributor.getHspFacility().add(new Code("2.16.840.1.113883.3.239.23.11.100.1", "Etobicoke General Hospital"));
			contributor.getHspFacility().add(new Code("2.16.840.1.113883.3.239.23.11.100.2", "Peel Memorial Centre"));
			contributor.getHspFacility().add(new Code("2.16.840.1.113883.3.239.23.11.100.3", "Brampton Civic Hospital"));
			contributor.getHspFacility().add(new Code("2.16.840.1.113883.3.239.23.11.100.4", "Withdrawal Management Centre"));

			// Meditech
			SendingSystem meditech = new SendingSystem();
			// Should be the value from table 9004 with ".101.x" at the end
			meditech.setCode("2.16.840.1.113883.3.239.23.11.101.1");
			meditech.setDescription("Meditech");
			meditech.setManagementConsoleSystemId("Meditech");

			// Should be the value from table 9004 with ".102.x" at the end
			meditech.getAllergenCodeSystemIam3().add("2.16.840.1.113883.3.239.23.11.102.1");
			meditech.getDrugAdministrationCodeSystemRxa5().add("2.16.840.1.113883.3.239.23.11.102.2");
			meditech.getDrugComponentCodeSystemRxc2().add("2.16.840.1.113883.3.239.23.11.102.3");
			meditech.getDrugGiveCodeSystemRxe2().add("2.16.840.1.113883.3.239.23.11.102.10");
			meditech.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.11.102.4");
			meditech.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.11.102.5");
			contributor.getSendingSystem().add(meditech);

			// Syngo
			SendingSystem syngo = new SendingSystem();
			// Should be the value from table 9004 with ".101.x" at the end
			syngo.setCode("2.16.840.1.113883.3.239.23.11.101.2");
			syngo.setDescription("Syngo");
			syngo.setManagementConsoleSystemId("Syngo");
			// Should be the value from table 9004 with ".102.x" at the end
			syngo.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.11.102.6");
			syngo.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.11.102.7");
			contributor.getSendingSystem().add(syngo);

			// Pulsecheck
			SendingSystem pulsecheck = new SendingSystem();
			// Should be the value from table 9004 with ".101.x" at the end
			pulsecheck.setCode("2.16.840.1.113883.3.239.23.11.101.3");
			pulsecheck.setDescription("Pulsecheck");
			pulsecheck.setManagementConsoleSystemId("Pulsecheck");
			// Should be the value from table 9004 with ".102.x" at the end
			pulsecheck.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.11.102.8");
			pulsecheck.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.11.102.9");
			contributor.getSendingSystem().add(pulsecheck);

		}
		// *******************************************************
		// South Lake (Southlake)
		// *******************************************************
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);

			// contributor.getDevListenPort().add(14014);

			contributor.setName("South Lake Regional Hospital");
			contributor.setDevSecurityToken("");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.59");
			contributor.setHospitalFacilityNumber("0736");
		}
		// *******************************************************
		// Baycrest Centre for Geriatric Care
		// *******************************************************
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);

			// contributor.getDevListenPort().add(14014);

			contributor.setName("Baycrest Centre for Geriatric Care");
			contributor.setDevSecurityToken("");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.13");
			contributor.setHospitalFacilityNumber("0827");
                        contributor.getHspFacility().add(new Code("2.16.840.1.113883.3.239.23.13.100.1", "Baycrest Centre for Geriatric Care"));
                        
                        SendingSystem meditech = new SendingSystem();
			meditech.setCode("2.16.840.1.113883.3.239.23.13.101.1");
			meditech.setDescription("Meditech");
			meditech.setManagementConsoleOrgId("Baycrest");
			meditech.setManagementConsoleSystemId("Meditech");
			contributor.getSendingSystem().add(meditech);

		}
		// *******************************************************
		// Campbellford Memorial Hospital
		// *******************************************************
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);

			// contributor.getDevListenPort().add(14014);

			contributor.setName("Campbellford Memorial Hospital");
			contributor.setDevSecurityToken("");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.14");
			contributor.setHospitalFacilityNumber("0624");

		}
		// *******************************************************
		// Centre for Addiction and Mental Health
		// *******************************************************
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);

			// contributor.getDevListenPort().add(14014);

			contributor.setName("Centre for Addiction and Mental Health");
			contributor.setDevSecurityToken("");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.15");
			contributor.setHospitalFacilityNumber("0948");
		}
		// *******************************************************
		// Halton Healthcare Services Corporation
		// *******************************************************
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);

			// contributor.getDevListenPort().add(14014);

			contributor.setName("Halton Healthcare Services Corporation");
			contributor.setDevSecurityToken("");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.16");
			contributor.setHospitalFacilityNumber("0950");

		}
		// *******************************************************
		// Headwaters Health Care Centre
		// *******************************************************
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);

			// contributor.getDevListenPort().add(14014);

			contributor.setName("Headwaters Health Care Centre");
			contributor.setDevSecurityToken("");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.17");
			contributor.setHospitalFacilityNumber("0916");

		}
		// *******************************************************
		// Hospital for Sick Children
		// *******************************************************
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);

			// contributor.getDevListenPort().add(14014);

			contributor.setName("Hospital for Sick Children");
			contributor.setDevSecurityToken("");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.18");
            contributor.setHrmSendingFacility("3969");
			contributor.setHospitalFacilityNumber("0837");

		}
		// *******************************************************
		// Humber River Regional Hospital
		// *******************************************************
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);

			// contributor.getDevListenPort().add(14014);

			contributor.setName("Humber River Regional Hospital");
			contributor.setDevSecurityToken("");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.19");
			contributor.setHospitalFacilityNumber("0941");

		}
		// *******************************************************
		// Northumberland Hills Hospital
		// *******************************************************
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);

			// contributor.getDevListenPort().add(14014);

			contributor.setName("Northumberland Hills Hospital");
			contributor.setDevSecurityToken("");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.20");
			contributor.setHospitalFacilityNumber("0940");

		}
		// *******************************************************
		// Peterborough Regional Health Centre
		// *******************************************************
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);

			// contributor.getDevListenPort().add(14014);

			contributor.setName("Peterborough Regional Health Centre");
			contributor.setDevSecurityToken("");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.21");
			contributor.setHospitalFacilityNumber("0771");
		}
		// *******************************************************
		// Ross Memorial Hospital
		// *******************************************************
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);

			// contributor.getDevListenPort().add(14014);

			contributor.setName("Ross Memorial Hospital");
			contributor.setDevSecurityToken("");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.22");
			contributor.setHospitalFacilityNumber("0707");
		}
		// *******************************************************
		// St Joseph's Health Centre (Toronto)
		// *******************************************************
		{
            Contributor contributor = new Contributor();
            cfg.getContributors().add(contributor);

            // Anthony, is the last port number 14044 (for TEGH?)
            contributor.getDevListenPort().add(14045);
            contributor.getDevListenPort().add(14046);
            contributor.getDevListenPort().add(14047);

            contributor.setName("St Joseph's Health Centre (Toronto)");
            contributor.setDevSecurityToken("1957395332368kkk");
            contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.23");
            // what is this?
            contributor.setManagementConsoleOrgId("SJHC");
            // what is this? the MOHLTC number?
            contributor.setHospitalFacilityNumber("0898");
            contributor.getMrnPoolOid().add(new Code("2.16.840.1.113883.3.239.18.140", contributor.getName() + " MRNs"));
            contributor.getHspFacility().add(new Code("2.16.840.1.113883.3.239.23.23.100.1", "St Joseph's Health Centre"));
            contributor.setHrmSendingFacility("4056");


            // Crescendo
            SendingSystem crescendo = new SendingSystem();
            // Should be the value from table 9004 with ".101.x" at the end
            crescendo.setCode("2.16.840.1.113883.3.239.23.23.101.1");
            crescendo.setDescription("Crescendo");
            crescendo.setManagementConsoleSystemId("Crescendo");
			contributor.getSendingSystem().add(crescendo);
            
            // Should be the value from table 9004 with ".102.x" at the end
            // HRM does not currently do terminology mapping, these are here as placeholders
            // crescendo.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.23.102.1");
            // crescendo.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.23.102.2");
            // contributor.getSendingSystem().add(crescendo);
            
            // Sectra
            SendingSystem sectra = new SendingSystem();
            // Should be the value from table 9004 with ".101.x" at the end
            sectra.setCode("2.16.840.1.113883.3.239.23.23.101.2");
            sectra.setDescription("Sectra");
            sectra.setManagementConsoleSystemId("Sectra");
			contributor.getSendingSystem().add(sectra);
           
            // Should be the value from table 9004 with ".102.x" at the end
            // HRM does not currently do terminology mapping, these are here as placeholders
            // sectra.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.23.102.3");
            // sectra.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.23.102.4");
            // contributor.getSendingSystem().add(sectra);
		}
		// *******************************************************
		// The Stevenson Memorial Hospital
		// *******************************************************
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);

			// contributor.getDevListenPort().add(14014);

			contributor.setName("The Stevenson Memorial Hospital");
			contributor.setDevSecurityToken("");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.24");
			contributor.setHospitalFacilityNumber("0596");
		}
		// *******************************************************
		// The Toronto East General Hospital
		// *******************************************************
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);

			contributor.getDevListenPort().add(14042);
			contributor.getDevListenPort().add(14043);
			contributor.getDevListenPort().add(14044);

			contributor.setName("The Toronto East General Hospital");
			contributor.setDevSecurityToken("24591738492ppp");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.25");
            contributor.setManagementConsoleOrgId("TEGH");
			contributor.getMrnPoolOid().add(new Code("2.16.840.1.113883.3.239.18.145", contributor.getName() + " MRNs"));
			contributor.getHspFacility().add(new Code("2.16.840.1.113883.3.239.23.25.100.1", "The Toronto East General Hospital"));
			contributor.setHospitalFacilityNumber("0858");
                        contributor.setHrmSendingFacility("4209");
			
			// Cerner
			SendingSystem cerner = new SendingSystem();

			// Should be the value from table 9004 with ".101.x" at the end
			cerner.setCode("2.16.840.1.113883.3.239.23.25.101.1");
			cerner.setDescription("Cerner HIS");
			cerner.setManagementConsoleSystemId("Cerner");

			// Should be the value from table 9004 with ".102.x" at the end
			//cerner.getRequestCodeSystemSystemObr4().add("2.16.840.1.113883.3.239.23.25.102.1");
			//cerner.getResultCodeSystemSystemObx3().add("2.16.840.1.113883.3.239.23.25.102.2");

			contributor.getSendingSystem().add(cerner);

		}
		
		
		
		// *******************************************************
		// York Central Hospital
		// *******************************************************
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);

			// contributor.getDevListenPort().add(14014);

			contributor.setName("York Central Hospital");
			contributor.setDevSecurityToken("");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.26");
			contributor.setHospitalFacilityNumber("0701");
		}
                
                // *******************************************************
		// West Park Health Centre
		// *******************************************************
		{
			Contributor contributor = new Contributor();
			cfg.getContributors().add(contributor);

			// contributor.getDevListenPort().add(14014);

			contributor.setName("West Park Health Centre");
			contributor.setDevSecurityToken("");
			contributor.setHspId9004AndSubIds("2.16.840.1.113883.3.239.23.58");
			contributor.setHospitalFacilityNumber("0613");

		}

		StringWriter w = new StringWriter();
		Marshaller marshaller = JAXBContext.newInstance(ContributorConfig.class).createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(cfg, w);

		System.out.println(w.toString());
		System.out.flush();

		cfg.validate();

		//removed ConverterLibrary folder from file path for use with Netbeans.
                File cfgFile = new File("ConverterLibrary/src/main/resources/ca/cgta/input/sending_systems.xml");
//                File cfgFile = new File("src/main/resources/ca/cgta/input/sending_systems.xml");
		if (!cfgFile.exists()) {
			throw new ValidationException("Could not find file " + cfgFile.getAbsolutePath());
		}

		String existing = IOUtils.toString(new FileReader(cfgFile));
		if (existing.equals(w.toString())) {
			ourLog.info("Existing config file matches, so I don't need to update it");
		} else {
			FileWriter fw = new FileWriter(cfgFile, false);
			fw.append(w.toString());
			fw.close();
		}

		cfg.getProviderPoolIdToContributor();

	}
}
