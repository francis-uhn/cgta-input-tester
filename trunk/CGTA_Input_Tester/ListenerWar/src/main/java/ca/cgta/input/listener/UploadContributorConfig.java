package ca.cgta.input.listener;

import org.apache.commons.lang.StringUtils;

import sail.wsdl.infrastructure.systemregistry.SystemRegistryWebService;
import xsd.sail.infrastructure.services.systemregistry.AddOrUpdateInterfaceRequest;
import xsd.sail.infrastructure.services.systemregistry.AddOrUpdateOrgRequest;
import xsd.sail.infrastructure.services.systemregistry.AddOrUpdateSystemRequest;
import xsd.sail.infrastructure.services.systemregistry.InterfaceInstance;
import xsd.sail.infrastructure.services.systemregistry.InterfaceSystem;
import xsd.sail.infrastructure.services.systemregistry.Org;
import ca.cgta.input.model.config.Contributor;
import ca.cgta.input.model.config.ContributorConfigFactory;
import ca.cgta.input.model.config.SendingSystem;
import ca.uhn.sail.integration.SailInfrastructureServicesFactory;

public class UploadContributorConfig {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(UploadContributorConfig.class);


	/**
	 * Creates entries in SAIL DB for all of the sending systems
	 */
	public static void uploadContributorConfig() throws Exception {

		SystemRegistryWebService sr = SailInfrastructureServicesFactory.getInstance().getSystemRegistryService();

		for (Contributor nextContributor : ContributorConfigFactory.getInstance().getContributorConfig().getContributors()) {

			String orgId = nextContributor.getManagementConsoleOrgId();

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
				ourLog.info("Adding/Updating system: " + nextSendingSystem.getManagementConsoleSystemId());

				if (StringUtils.isNotBlank(nextSendingSystem.getManagementConsoleOrgId())) {
					ourLog.info(" * With org id: " + nextSendingSystem.getManagementConsoleOrgId());
					orgId = nextSendingSystem.getManagementConsoleOrgId();
				} else {
					ourLog.info(" * With org id: " + orgId);
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

}
