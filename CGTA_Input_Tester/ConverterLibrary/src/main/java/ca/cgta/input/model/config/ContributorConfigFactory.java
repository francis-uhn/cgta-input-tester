package ca.cgta.input.model.config;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;


public class ContributorConfigFactory {

	private static final String CONFIG_RESOURCE = "ca/cgta/input/sending_systems.xml";
	private static ContributorConfigFactory ourInstance;
	private ContributorConfig myContributorConfig;
	
	public static ContributorConfigFactory getInstance() throws JAXBException {
		if (ourInstance == null) {
			ourInstance= new ContributorConfigFactory();
		}
		return ourInstance;
	}
	
	private ContributorConfigFactory() throws JAXBException {
		
		InputStream inputStream = ContributorConfigFactory.class.getClassLoader().getResourceAsStream(CONFIG_RESOURCE);
		if (inputStream == null) {
			throw new JAXBException("Couldn't load resource: " + CONFIG_RESOURCE);
		}
		
		myContributorConfig = (ContributorConfig)JAXBContext.newInstance(ContributorConfig.class).createUnmarshaller().unmarshal(inputStream);
		if (myContributorConfig == null) {
			throw new JAXBException("Failed to parse resource: " + CONFIG_RESOURCE);
		}
		
	}

	/**
     * @return the contributorConfig
     */
    public ContributorConfig getContributorConfig() {
    	return myContributorConfig;
    }
	
}
