package ca.cgta.input.listener;

import org.apache.commons.lang.StringUtils;

import ca.cgta.input.model.config.AddOrUpdateInterfaceRequest;
import ca.cgta.input.model.config.AddOrUpdateOrgRequest;
import ca.cgta.input.model.config.AddOrUpdateSystemRequest;
import ca.cgta.input.model.config.Contributor;
import ca.cgta.input.model.config.InterfaceInstance;
import ca.cgta.input.model.config.InterfaceSystem;
import ca.cgta.input.model.config.InvalidInputException;
import ca.cgta.input.model.config.Org;
import ca.cgta.input.model.config.SendingSystem;
import ca.cgta.input.model.config.SystemRegistryWebService;
import ca.cgta.input.model.config.UnexpectedErrorException;

public class UploadContributorConfig {

	private void uploadToDev() throws UnexpectedErrorException, InvalidInputException {

		SystemRegistryWebService sr = SailInfrastructureServicesFactory.getInstance().getSystemRegistryService();

		for (Contributor nextContributor : myContributors) {

			String orgId = nextContributor.getManagementConsoleOrgId();
			// LookupAllInterfaceInformationRequest allInterfaceReq = new
			// LookupAllInterfaceInformationRequest();
			// allInterfaceReq.getInterestedInOrg().add(orgId);
			// LookupAllInterfaceInformationResponse allInterface =
			// sr.getAllInterfaceInformation(allInterfaceReq);

			// Add Org if needed
			if (orgId != null) {
				ourLog.info("Adding/Updating org: " + orgId);
				AddOrUpdateOrgRequest aor = new AddOrUpdateOrgRequest();
				Org org = new Org();
				org.setId(orgId);
				org.setName(nextContributor.getName());
				aor.setOrg(org);
				sr.addOrUpdateOrg(aor);
			}

			// Loop through systems
			for (SendingSystem nextSendingSystem : nextContributor.getSendingSystem()) {

				if (StringUtils.isNotBlank(nextSendingSystem.getManagementConsoleOrgId())) {
					orgId = nextSendingSystem.getManagementConsoleOrgId();
				}

				String systemId = nextSendingSystem.getManagementConsoleSystemId();

				AddOrUpdateSystemRequest addOrUpdateSystemRequest = new AddOrUpdateSystemRequest();
				InterfaceSystem system = new InterfaceSystem();
				system.setOrgId(orgId);
				system.setSystemId(systemId);
				system.setContact("?");
				system.setActive(true);
				system.setDescription(nextSendingSystem.getDescription());
				addOrUpdateSystemRequest.setSystem(system);
				sr.addOrUpdateSystem(addOrUpdateSystemRequest);

				AddOrUpdateInterfaceRequest addOrUpdateInterfaceRequest = new AddOrUpdateInterfaceRequest();
				InterfaceInstance iface = new InterfaceInstance();
				iface.setActive(true);
				iface.setDescription("Messages to cGTA CDR");
				iface.setInterfaceDirection("I");
				iface.setInterfaceId(Listener.INTERFACE_ID);
				iface.setJournalToDatabase(true);
				iface.setJournalToDisk(true);
				iface.setOrgId(orgId);
				iface.setSystemId(systemId);
				addOrUpdateInterfaceRequest.setInterface(iface);
				sr.addOrUpdateInterface(addOrUpdateInterfaceRequest);
			}
		}
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
