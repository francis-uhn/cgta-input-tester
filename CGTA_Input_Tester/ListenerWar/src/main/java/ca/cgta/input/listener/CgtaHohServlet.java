package ca.cgta.input.listener;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;

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
import ca.uhn.hl7v2.AcknowledgmentCode;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.ApplicationException;
import ca.uhn.hl7v2.hoh.hapi.server.HohServlet;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.message.ACK;
import ca.uhn.hl7v2.model.v25.message.ADT_A01;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.message.RAS_O17;
import ca.uhn.hl7v2.model.v25.message.RDE_O11;
import ca.uhn.hl7v2.model.v25.segment.ERR;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.impl.AppRoutingDataImpl;
import ca.uhn.hl7v2.protocol.impl.ApplicationRouterImpl;
import ca.uhn.hl7v2.util.Terser;
import ca.uhn.hl7v2.validation.impl.ValidationContextImpl;
import ca.uhn.sail.integration.AbstractPojo;
import ca.uhn.sail.integration.SailInfrastructureServicesFactory;

public class CgtaHohServlet extends HohServlet {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(CgtaHohServlet.class);
	private static final long serialVersionUID = 1L;

	private ContributorConfigFactory myContributorConfig;
	private PipeParser myParser;
	private JournallingWebService myJournalSvc;


	public CgtaHohServlet() throws JAXBException {

		myContributorConfig = ContributorConfigFactory.getInstance();
		myJournalSvc = SailInfrastructureServicesFactory.getInstance().getJournallingService();

		DefaultHapiContext ctx = new DefaultHapiContext();
		ctx.setValidationContext(new ValidationContextImpl());
		myParser = ctx.getPipeParser();
		ApplicationRouterImpl router = new ApplicationRouterImpl(myParser);
		router.bindApplication(new AppRoutingDataImpl("*", "*", "*", "*"), new MyApplication());

		setApplicationRouter(router);
	}

	public class MyApplication implements ReceivingApplication {

		private static final String PROCESSING_FAILED_OUTCOME = "Processing failed, no data saved (see error codes for more information)";
		private Contributor myFoundContributor;
		private int myPort;


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

			String outcome = null;
			if ("ORU".equals(msgType) && "R01".equals(msgTrigger)) {
				final ORU_R01 oru = (ORU_R01) theArg0;
				final List<ClinicalDocumentGroup> documents = converter.convertClinicalDocument(oru);
				try {
					outcome = "ORU^R01 message evaluated (containing " + documents.size() + " documents/results)";
//					outcome = Persister.persist(documents);
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
						outcome = "ADT^" + msgTrigger + " message evaluated";
//						outcome = Persister.persist(pwv);
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
					outcome = "RDE^O11 message evaluated (containing " + medOrders.size() + " medication orders)";
//					outcome = Persister.persistMedicationOrders(medOrders);
				} catch (final Exception e1) {
					throw new HL7Exception(e1);
				}
			} else if ("RAS".equals(msgType) && "O17".equals(msgTrigger)) {
				RAS_O17 ras = new RAS_O17();
				ras.setParser(myParser);
				ras.parse(encodedMessage);
				List<MedicationOrderWithAdmins> medOrdersWithAdmins = converter.convertMedicationAdmin(ras);
				try {
					outcome = "RAS^O17 message evaluated (containing administration data for " + medOrdersWithAdmins.size() + " medication orders)";
//					outcome = Persister.persistMedicationAdmins(medOrdersWithAdmins);
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

//			if (contributor == null) {
//				contributor = myContributor;
//			} else if (contributor != myContributor) {
//				converter.addFailure("MSH-3-1", FailureCode.F112, msh.getMsh3_SendingApplication().getHd1_NamespaceID().getValue());
//				contributor = myContributor;
//			}

			if (sendingSystem == null) {
				throw new ApplicationException("Failed to determine sender from message. Please check MSH-3 value");
			}
			
//			if (sendingSystem == null || contributor.getSendingSystem9008WithOid(sendingSystem.getCode()) == null) {
//				converter.addFailure("MSH-3-2", FailureCode.F113, msh.getMsh3_SendingApplication().getHd2_UniversalID().getValue());
//				sendingSystem = contributor.getSendingSystem().get(0);
//				ourLog.info("Defaulting to first sending system {} because MSH-3-2 was {}", sendingSystem.getDescription(), msh.getMsh3_SendingApplication().getHd2_UniversalID().getValue());
//			}

			ourLog.info("Converted message from {} - {}", contributor.getName(), sendingSystem.getDescription());

			String orgId = sendingSystem.getManagementConsoleOrgId();
			if (StringUtils.isBlank(orgId)) {
				orgId = contributor.getManagementConsoleOrgId();
			}

			final CanonicalHl7V2Message canon = AbstractPojo.createNewCanonicalMessageHl7V2(encodedMessage, orgId, sendingSystem.getManagementConsoleSystemId(), Listener.INTERFACE_ID, MessagePhase.INCOMING, Listener.class, java.util.logging.Logger.getLogger(Listener.class.getName()));
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

			if (outcome != null) {
				canon.setValidationOutcomeDescription(canon.getValidationOutcomeDescription() + "<br>Outcome: " + outcome);
			} else {
				canon.setValidationOutcomeDescription(canon.getValidationOutcomeDescription());
			}
			
			// Journal for the source
			ourLog.info("Journalling message");
			final JournalMessageRequest request = new JournalMessageRequest();
			request.setCanonicalHl7V2Message(canon);
			try {
				AbstractPojo.tryToJournalMessage(myJournalSvc, request);
			} catch (InvalidInputException e) {
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


		public Message processMessage(Message theArg0, Map<String, Object> theMetadata) throws HL7Exception {
			ourLog.info("Received message of object type {} from port {}" + theArg0.getClass().getName(), myPort);

			Message retVal;

			try {
				ourLog.info("Starting processing");
				retVal = doProcess(theArg0);
			} catch (Throwable e) {
				ourLog.error("Failed to process", e);
				HL7Exception e2 = new HL7Exception("Processing failed due to internal error, please contact your ConnectingGTA site coordinator.");
				try {
					retVal = theArg0.generateACK(AcknowledgmentCode.AE, e2);
				} catch (IOException e1) {
					throw e2;
				}
			}

			ourLog.info("Done processing message of object type {} from port {}" + theArg0.getClass().getName(), myPort);
			return retVal;

		}

	}
	
	public static void main(String[] args) throws HL7Exception, JAXBException {
		
		String msg = "MSH|^~\\&|1.3.6.1.4.1.12201^1.3.6.1.4.1.12201.101.1|UHN^1.3.6.1.4.1.12201|ConnectingGTA|ConnectingGTA|20130225000100-0500|2954864636aaa|ADT^A11^ADT_A09|142981|T|2.5|||NE|AL|CAN|8859/1|||CGTA_CDR_INPUT_2_0\r" + 
				"EVN||201302250001\r" + 
				"PID|1||7013623^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^MR~HN3651^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^PI||Test^Alexfive^^^^^L||19700105|M|||21 jump street^^NORTH YORK^CANON^M3A 1Y8^Can^H||(416)444-4444^PRN^PH^^1^416^4444444||eng^English^HL70296|||||||||||||||N\r" + 
				"PV1|1|I|^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.100.1|R||^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.100.1||1185^Abrams^Howard^^^Dr.^MD^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1||General Internal Medicine||||||N|13546^Generic^Physician^Moe^^Dr.^MD^^1.3.6.1.4.1.12201.1.2.1.5&1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1||11210000302^^^1.3.6.1.4.1.12201&1.3.6.1.4.1.12201.101.1^VN|||||||||||||||||||||||||20120628105900-0400\r" + 
				"PV2|||^test";
		Message parsed = PipeParser.getInstanceWithNoValidation().parse(msg);
		CgtaHohServlet s = new CgtaHohServlet();
		MyApplication application = s.new MyApplication();
		application.processMessage(parsed, new HashMap<String, Object>());
		
	}
	
	
	
	
	
}
