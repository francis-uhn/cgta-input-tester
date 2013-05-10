package ca.cgta.input.listener;

import java.io.IOException;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sail.wsdl.infrastructure.journalling.InvalidInputException;
import sail.wsdl.infrastructure.journalling.JournallingWebService;
import sail.xsd.canonical.hl7v2.CanonicalHl7V2Message;
import sail.xsd.canonical.hl7v2.MessagePhase;
import sail.xsd.infrastructure.services.journal.JournalMessageRequest;
import ca.cgta.input.converter.Converter;
import ca.cgta.input.converter.Failure;
import ca.cgta.input.converter.FailureCode;
import ca.cgta.input.model.config.Contributor;
import ca.cgta.input.model.config.ContributorConfigFactory;
import ca.cgta.input.model.config.SendingSystem;
import ca.cgta.input.model.inner.MedicationOrder;
import ca.cgta.input.model.outer.ClinicalDocumentGroup;
import ca.cgta.input.model.outer.MedicationOrderWithAdmins;
import ca.cgta.input.model.outer.PatientWithVisits;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Application;
import ca.uhn.hl7v2.app.ApplicationException;
import ca.uhn.hl7v2.app.SimpleServer;
import ca.uhn.hl7v2.llp.MinLowerLayerProtocol;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Varies;
import ca.uhn.hl7v2.model.v25.message.ACK;
import ca.uhn.hl7v2.model.v25.message.ADT_A01;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.message.RAS_O17;
import ca.uhn.hl7v2.model.v25.message.RDE_O11;
import ca.uhn.hl7v2.model.v25.segment.ERR;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.parser.CanonicalModelClassFactory;
import ca.uhn.hl7v2.parser.EncodingNotSupportedException;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.SocketFactory;
import ca.uhn.hl7v2.util.StandardSocketFactory;
import ca.uhn.hl7v2.util.Terser;
import ca.uhn.hl7v2.validation.impl.ValidationContextImpl;
import ca.uhn.sail.integration.AbstractPojo;
import ca.uhn.sail.integration.SailInfrastructureServicesFactory;

public class Listener extends HttpServlet {

	/**
	 * Interface ID
	 */
	public static final String INTERFACE_ID = "CGTAPOP";

	private static final Logger ourLog = LoggerFactory.getLogger(Listener.class);

	private static final long serialVersionUID = 1L;

	private ContributorConfigFactory myContributorConfig;
	private JournallingWebService myJournalSvc;
	private final List<Integer> myServerPorts = new ArrayList<Integer>();
	private final List<SimpleServer> myServers = new ArrayList<SimpleServer>();
	private final boolean myUnitTestMode;
	private PipeParser myParser;


	public Listener() throws JAXBException {
		this(false);
	}


	public Listener(boolean theUnitTestMode) throws JAXBException {
		System.setProperty(Varies.DEFAULT_OBX2_TYPE_PROP, "ST");
		System.setProperty(Varies.INVALID_OBX2_TYPE_PROP, "ST");

		myUnitTestMode = theUnitTestMode;
		myContributorConfig = ContributorConfigFactory.getInstance();

		DefaultHapiContext ctx = new DefaultHapiContext();
		ctx.setValidationContext(new ValidationContextImpl());
		myParser = ctx.getPipeParser();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void destroy() {
		for (int i = 0; i < myServers.size(); i++) {
			SimpleServer next = myServers.get(i);
			int port = myServerPorts.get(i);
			ourLog.info("Stopping server on port {}", port);
			next.stopAndWait();
		}

		myServerPorts.clear();
		myServers.clear();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(ServletConfig theConfig) throws ServletException {

		myJournalSvc = SailInfrastructureServicesFactory.getInstance().getJournallingService();

		for (Contributor contributor : myContributorConfig.getContributorConfig().getContributors()) {
			Object constributorLock = new Object();
			if (myUnitTestMode && !contributor.getHspId9004().equals("1.3.6.1.4.1.12201.999")) {
				continue;
			}

			ourLog.info("Starting server ports for org {}", contributor.getName());

			for (Integer nextPort : contributor.getDevListenPort()) {
				ourLog.info("Starting server on port {} for org {}", nextPort, contributor.getName());

				SocketFactory socketFactory = new MySocketFactory();
				DefaultHapiContext ctx = new DefaultHapiContext();
				ctx.setValidationContext(new ValidationContextImpl());
				ctx.setSocketFactory(socketFactory);
				ctx.setLowerLayerProtocol(new MinLowerLayerProtocol());
				
				// socketFactory, nextPort, new MinLowerLayerProtocol(), myParser
				
				SimpleServer nextServer = new SimpleServer(ctx, nextPort, false);
				MyApplication application = new MyApplication(contributor, nextPort, constributorLock);

				nextServer.registerApplication("*", "*", application);
				nextServer.start();

				myServerPorts.add(nextPort);
				myServers.add(nextServer);
			}
		}

		ourLog.info("Successfully started {} servers", myServers.size());

		if (System.getProperty("sail.env.id") != null) {
			ourLog.info("Initializing SAIL system registry");
			try {
				UploadContributorConfig.uploadContributorConfig();
				ourLog.info("Done initializing SAIL system registry");
			} catch (Exception e) {
				ourLog.info("Failed to initialize SAIL system registry", e);
			}
		}

	}

	public class MyApplication implements Application {

		private static final String PROCESSING_FAILED_OUTCOME = "Processing failed, no data saved (see error codes for more information)";
		private Contributor myContributor;
		private int myPort;
		private Object myContributorLock;


		public MyApplication(Contributor theContributor, int thePort, Object theConstributorLock) {
			Validate.notNull(theContributor);
			Validate.notNull(theConstributorLock);

			myContributor = theContributor;
			myPort = thePort;
			myContributorLock = theConstributorLock;
		}


		public boolean canProcess(Message theArg0) {
			return true;
		}


		private String createHtmlProblemDescription(Converter converter) {
			final StringBuilder val = new StringBuilder();

			val.append("<ul style=\"white-space: wrap !important;\">");
			int index = 1;
			for (final Failure next : converter.getFailures()) {
				val.append("<li>Problem ").append(index);
				val.append("<ul>");
				val.append("<li>Location within Message: ").append(next.getTerserPath()).append("");

				// Error code
				val.append("<li>Error Code: ");
				val.append(next.getFailureCode().name());
				val.append(" <a href=\"http://conftest.connectinggta.ca/cGTA_Conformance_Validator/?showErrorCode=");
				val.append(next.getFailureCode().name());
				val.append("\" target=\"_blank\">(severity " + next.getFailureCode().getSeverity().name() + ", click here for more info)</a>");
				val.append("</li>");

				val.append("<li>").append(next.getMessage()).append("</li>");
				if (StringUtils.isNotBlank(next.getFieldVal())) {
					val.append("<li>Field Value: ").append(next.getFieldVal()).append("</li>");
				}
				val.append("</ul>");
				val.append("</li>");

				index++;
			}
			val.append("</ul>");
			String problemDescription = val.toString();
			return problemDescription;
		}


		private Message doProcess(Message theArg0) throws HL7Exception, ApplicationException {
			String encodedMessage = theArg0.encode();

			Terser terser = new Terser(theArg0);
			String msgType = terser.get("/MSH-9-1");
			String msgTrigger = terser.get("/MSH-9-2");
			MSH msh = (MSH) theArg0.get("MSH");

			ourLog.info("Message is of type " + msgType + "^" + msgTrigger);
			List<Failure> failures;

			try {
				String msgSourceOrg = terser.get("/MSH-3-1");
				String sourceOrg = myContributorConfig.getContributorConfig().getNameOfHspId9004(msgSourceOrg);
				ourLog.info("Sending organization from message is {} - {}", msgSourceOrg, sourceOrg);
			} catch (Exception e) {
				// ignore
			}

			Converter converter;
			try {
				converter = new Converter(true);
			} catch (JAXBException e2) {
				ourLog.error("Failed to convert message: Could not initialize converter", e2);
				throw new HL7Exception(e2);
			}

			String outcome;
			if ("ORU".equals(msgType) && "R01".equals(msgTrigger)) {
				final ORU_R01 oru = (ORU_R01) theArg0;
				final List<ClinicalDocumentGroup> documents = converter.convertClinicalDocument(oru);
				try {
					outcome = Persister.persist(documents);
				} catch (final Exception e1) {
					throw new HL7Exception(e1);
				}
			} else if ("ADT".equals(msgType)) {
				ADT_A01 adt = new ADT_A01();
				adt.setParser(myParser);
				adt.parse(encodedMessage);
				PatientWithVisits pwv = converter.convertPatientWithVisits(adt);
				try {
					if (pwv != null) {
						outcome = Persister.persist(pwv);
					} else {
						outcome = PROCESSING_FAILED_OUTCOME;
					}
				} catch (final Exception e1) {
					throw new HL7Exception(e1);
				}
			} else if ("RDE".equals(msgType) && "O11".equals(msgTrigger)) {
				RDE_O11 rde = new RDE_O11();
				rde.setParser(myParser);
				rde.parse(encodedMessage);
				List<MedicationOrder> medOrders = converter.convertMedicationOrder(rde);
				try {
					outcome = Persister.persistMedicationOrders(medOrders);
				} catch (final Exception e1) {
					throw new HL7Exception(e1);
				}
			} else if ("RAS".equals(msgType) && "O17".equals(msgTrigger)) {
				RAS_O17 ras = new RAS_O17();
				ras.setParser(myParser);
				ras.parse(encodedMessage);
				List<MedicationOrderWithAdmins> medOrdersWithAdmins = converter.convertMedicationAdmin(ras);
				try {
					outcome = Persister.persistMedicationAdmins(medOrdersWithAdmins);
				} catch (final Exception e1) {
					throw new HL7Exception(e1);
				}
			} else {

				/*
				 * Note: If adding processors for additional message types, make
				 * sure to update the description for F098!!
				 */

				converter.validateMsh(msh);
				converter.addFailure("/MSH-9", FailureCode.F098, msgType + "^" + msgTrigger);
				outcome = PROCESSING_FAILED_OUTCOME;

			}

			if (StringUtils.isBlank(outcome)) {
				outcome = PROCESSING_FAILED_OUTCOME;
			}

			// These are determined from the IDs in the message
			SendingSystem sendingSystem = converter.getSendingSystem();
			Contributor contributor = converter.getContributorConfig();

			if (contributor == null) {
				contributor = myContributor;
			} else if (contributor != myContributor) {
				converter.addFailure("MSH-3-1", FailureCode.F112, msh.getMsh3_SendingApplication().getHd1_NamespaceID().getValue());
				contributor = myContributor;
			}

			if (sendingSystem == null || contributor.getSendingSystem9008WithOid(sendingSystem.getCode()) == null) {
				converter.addFailure("MSH-3-2", FailureCode.F113, msh.getMsh3_SendingApplication().getHd2_UniversalID().getValue());
				sendingSystem = contributor.getSendingSystem().get(0);
				ourLog.info("Defaulting to first sending system {} because MSH-3-2 was {}", sendingSystem.getDescription(), msh.getMsh3_SendingApplication().getHd2_UniversalID().getValue());
			}

			ourLog.info("Converted message from {} - {}", contributor.getName(), sendingSystem.getDescription());

			String orgId = sendingSystem.getManagementConsoleOrgId();
			if (StringUtils.isBlank(orgId)) {
				orgId = contributor.getManagementConsoleOrgId();
			}

			final CanonicalHl7V2Message canon = AbstractPojo.createNewCanonicalMessageHl7V2(encodedMessage, orgId, sendingSystem.getManagementConsoleSystemId(), INTERFACE_ID, MessagePhase.INCOMING, Listener.class, java.util.logging.Logger.getLogger(Listener.class.getName()));
			failures = converter.getFailures();
			if (failures.size() > 0) {

				String problemDescription = createHtmlProblemDescription(converter);

				if (problemDescription.length() > 0) {
					canon.setValidationOutcomeDescription(problemDescription);
				}

				ourLog.info("Message has {} failures", converter.getFailures().size());

			} else {

				ourLog.info("Message has no validation failures! Outstanding.");
				canon.setValidationOutcomeDescription("No issues");

			}

			canon.setValidationOutcomeDescription(canon.getValidationOutcomeDescription() + "<br>Outcome: " + outcome);

			// Journal for the source
			ourLog.info("Journalling message");
			final JournalMessageRequest request = new JournalMessageRequest();
			request.setCanonicalHl7V2Message(canon);
			try {
				AbstractPojo.tryToJournalMessage(myJournalSvc, request);
			} catch (final InvalidInputException e) {
				ourLog.error("Failed to send message to journal", e);
				throw new HL7Exception("Failed to process message");
			} catch (final sail.wsdl.infrastructure.journalling.UnexpectedErrorException e) {
				ourLog.error("Failed to send message to dead letter", e);
				throw new HL7Exception("Failed to process message");
			} catch (Exception e) {
				ourLog.error("Failed to journal message. Moving on", e);
				StringWriter w = new StringWriter();
				JAXB.marshal(request, w);
				ourLog.error("Request was: " + w.toString());
			}

			// // Journal again for the destination
			// canon.setDestination(new OutboundInterface());
			// canon.getDestination().setBoxId(canon.getSource().getBoxId());
			// canon.getDestination().setDomainId(canon.getSource().getDomainId());
			// canon.getDestination().setInterfaceDirection("O");
			// canon.getDestination().setOrgId("SIMS");
			// canon.getDestination().setSystemId("CGTA_Interim_CDR");
			// canon.getDestination().setInterfaceId("All");
			// canon.setCurrentMessagePhase(MessagePhase.OUTGOING);
			// try {
			// myJournalSvc.journalMessage(request);
			// } catch (final InvalidInputException e) {
			// ourLog.error("Failed to send message to journal", e);
			// } catch (final
			// sail.wsdl.infrastructure.journalling.UnexpectedErrorException e)
			// {
			// ourLog.error("Failed to send message to dead letter", e);
			// }

			try {
				ACK ack = (ACK) theArg0.generateACK();

				for (final Failure next : converter.getFailures()) {
					ERR err = ack.getERR(ack.getERRReps());
					err.getErr1_ErrorCodeAndLocation(0).getEld1_SegmentID().setValue(next.getTerserPath());
					err.getErr3_HL7ErrorCode().getCwe1_Identifier().setValue(next.getFailureCode().name());
					err.getErr6_ApplicationErrorParameter(0).setValue(next.getFieldVal());
					err.getErr7_DiagnosticInformation().setValue(next.getMessage());
				}

				return ack;
			} catch (final IOException e) {
				throw new HL7Exception(e);
			}
		}


		public Message processMessage(Message theArg0) throws ApplicationException, HL7Exception {
			ourLog.info("Received message of object type {} from port {}" + theArg0.getClass().getName(), myPort);

			Message retVal;

			try {
				synchronized (myContributorLock) {
					ourLog.info("Starting processing");
					retVal = doProcess(theArg0);
				}
			} catch (Throwable e) {
				ourLog.error("Failed to process", e);
				HL7Exception e2 = new HL7Exception("Processing failed due to internal error, please contact your ConnectingGTA site coordinator.");
				try {
					retVal = theArg0.generateACK("AE", e2);
				} catch (IOException e1) {
					throw e2;
				}
			}

			ourLog.info("Done processing message of object type {} from port {}" + theArg0.getClass().getName(), myPort);
			return retVal;

		}

	}

	private final class MyPipeParser extends PipeParser {

		public MyPipeParser() {
			super(new CanonicalModelClassFactory("2.5"));
		}


		/**
		 * {@inheritDoc}
		 */

		@Override
		public String encode(Message theSource) throws HL7Exception {
			String retVal = super.encode(theSource);
			ourLog.info("Response message:\n" + retVal);
			return retVal;
		}


		/**
		 * {@inheritDoc}
		 */
		@Override
		public Message parse(String theMessage) throws HL7Exception, EncodingNotSupportedException {
			ourLog.info("Incoming message:\n" + theMessage);

			char delim = theMessage.charAt(3);
			for (String nextLine : theMessage.split("\\r")) {
				if (!nextLine.matches("[A-Z0-9]{3}.*") || nextLine.charAt(3) != delim) {
					throw new HL7Exception("Message appears to have CR characters (ASCII-13) within segment contents");
				}
			}

			return super.parse(theMessage);
		}

	}

	public class MySocketFactory extends StandardSocketFactory {

		public ServerSocket createServerSocket() throws IOException {
			ServerSocket serverSocket = super.createServerSocket();
			serverSocket.setReuseAddress(true);
			return serverSocket;
		}


		public Socket createSocket() throws IOException {
			return super.createSocket();
		}


		public ServerSocket createTlsServerSocket() throws IOException {
			return super.createTlsServerSocket();
		}


		public Socket createTlsSocket() throws IOException {
			return super.createTlsSocket();
		}

	}

}
