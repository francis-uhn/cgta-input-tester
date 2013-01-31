package sail.map;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.sail.integration.AbstractPojoMapping;
import hapi.on.olis.oru_r01.group.ORU_R01_OBSERVATION;
import hapi.on.olis.oru_r01.group.ORU_R01_ORDER_OBSERVATION;
import hapi.on.olis.oru_r01.message.ORU_R01;
import hapi.on.olis.oru_r01.segment.OBX;
import hapi.on.olis.oru_r01.segment.ZBX;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.transform.Source;
import org.glassfish.openesb.pojose.api.annotation.Provider;
import org.glassfish.openesb.pojose.api.annotation.Resource;
import org.glassfish.openesb.pojose.api.annotation.Operation;
import org.glassfish.openesb.pojose.api.res.Context;
import java.util.logging.Logger;
import javax.jbi.messaging.NormalizedMessage;
import org.apache.commons.lang.StringUtils;
import sail.xsd.canonical.hl7v2.CanonicalHl7V2Message;
import ca.uhn.sail.integration.CanonicalMessageUtils;
import org.hl7.cts.types.ValidateCodeSimpleRequest;
import org.hl7.cts.types.ValidateCodeSimpleResponse;
import org.glassfish.openesb.pojose.api.Consumer;
import org.glassfish.openesb.pojose.api.annotation.ConsumerEndpoint;
import org.hl7.cts.types.CD;
import org.hl7.cts.types.TranslateCodeSimpleRequest;
import org.hl7.cts.types.TranslateCodeSimpleResponse;
import sail.xsd.canonical.hl7v2.Identifier;
import sail.xsd.canonical.hl7v2.IdentifierType;
import static org.apache.commons.lang.StringUtils.isEmpty;

/**
 *Update History:
 * 1 - 2011-11-23 - Marco Pagura - Change the Hash Map Code for the SubId in OBX-4.  Comment in the process OBX Method line 992
 * 2 - 2012-10-29 - Marco Pagura - Modified the code for patien Given Name.   Factored in null pointers.
 *
 * Production Deployment History:
 * 2011-09-28 - Marco Pagura - Initial Deployment
 * 2011-11-23 - Marco Pagura - Update Deployment
 * 2012-10-29 - Marco Pagura - Update Deployment
 */
@Provider(name = "Map_ORU_SMH_Soft_ORUint_ON_OLIS", interfaceQN = "{http://j2ee.netbeans.org/wsdl/prj_Map_ORU_SMH_Soft_ORUint_ON_OLIS_Pojo/Map_ORU_SMH_Soft_ORUint_ON_OLIS}JMSInPortType", serviceQN = "{http://j2ee.netbeans.org/wsdl/prj_Map_ORU_SMH_Soft_ORUint_ON_OLIS_Pojo/Map_ORU_SMH_Soft_ORUint_ON_OLIS}JMSInPortTypeService")
public class Map_ORU_SMH_Soft_ORUint_ON_OLIS extends AbstractPojoMapping {

    private static HashMap<String, String> performingLaboratory = new HashMap<String, String>();
    private static Set<String> testRequestCode = new HashSet<String>();

    static {
        performingLaboratory.put("TAMIR02", "2.16.840.1.113883.3.59.1:4265");
        //performingLaboratory.put("TBANT01", "2");
        performingLaboratory.put("TC1CB05", "2.16.840.1.113883.3.59.1:4182");
        performingLaboratory.put("TCHEO01", "2.16.840.1.113883.3.59.1:4232");
        performingLaboratory.put("TESTO05", "2.16.840.1.113883.3.59.1:4137");
        performingLaboratory.put("TFEWT03", "2.16.840.1.113883.3.59.1:4267");
        performingLaboratory.put("THAMI01", "2.16.840.1.113883.3.59.1:4180");
        performingLaboratory.put("THAMN02", "2.16.840.1.113883.3.59.1:4180");
        performingLaboratory.put("THLAB01", "2.16.840.1.113883.3.59.1:4261");
        performingLaboratory.put("THLAN08", "2.16.840.1.113883.3.59.1:4261");
        performingLaboratory.put("THLAS01", "2.16.840.1.113883.3.59.1:4261");
        performingLaboratory.put("TKING01", "2.16.840.1.113883.3.59.1:4122");
        performingLaboratory.put("TLOND02", "2.16.840.1.113883.3.59.1:4182");
        performingLaboratory.put("TLONS02", "2.16.840.1.113883.3.59.1:4185");
        performingLaboratory.put("TMCMA02", "2.16.840.1.113883.3.59.1:4137");
        performingLaboratory.put("TMDS01", "2.16.840.1.113883.3.59.1:5687");
        performingLaboratory.put("TMDSL01", "2.16.840.1.113883.3.59.1:5687");
        performingLaboratory.put("TMSHA05", "2.16.840.1.113883.3.59.1:4194");
        performingLaboratory.put("TMSIN02", "2.16.840.1.113883.3.59.1:4194");
        performingLaboratory.put("TNORC06", "2.16.840.1.113883.3.59.1:4182");
        performingLaboratory.put("TNTEL12", "2.16.840.1.113883.3.59.1:4194");
        performingLaboratory.put("TNYOR01", "2.16.840.1.113883.3.59.1:4047");
        performingLaboratory.put("TOTTA02", "2.16.840.1.113883.3.59.1:4249");
        performingLaboratory.put("TPEEL01", "2.16.840.1.113883.3.59.1:4111");
        performingLaboratory.put("TPLEA11", "2.16.840.1.113883.3.59.1:4182");
        performingLaboratory.put("TREFC03", "2.16.840.1.113883.3.59.1:4137");
        performingLaboratory.put("TRVHS01", "2.16.840.1.113883.3.59.1:4179");
        performingLaboratory.put("TSICK02", "2.16.840.1.113883.3.59.1:4159");
        performingLaboratory.put("TSJCA07", "2.16.840.1.113883.3.59.1:4145");
        performingLaboratory.put("TSJOE01", "2.16.840.1.113883.3.59.1:4128");
        performingLaboratory.put("TSTJH01", "2.16.840.1.113883.3.59.1:4037");
        performingLaboratory.put("TSTJL02", "2.16.840.1.113883.3.59.1:4128");
        performingLaboratory.put("TSTJO03", "2.16.840.1.113883.3.59.1:4145");
        performingLaboratory.put("TSUNY01", "2.16.840.1.113883.3.59.1:4267");
        performingLaboratory.put("TTGEN02", "2.16.840.1.113883.3.59.1:4204");
        performingLaboratory.put("TTGH102", "2.16.840.1.113883.3.59.1:4204");
        performingLaboratory.put("TTGH106", "2.16.840.1.113883.3.59.1:4204");
        performingLaboratory.put("TTGH107", "2.16.840.1.113883.3.59.1:4204");
        performingLaboratory.put("TTMLS01", "2.16.840.1.113883.3.59.1:4265");
        performingLaboratory.put("TAHEN01", "2");
        performingLaboratory.put("TAHEN01", "2");
        performingLaboratory.put("TAHEN01", "2");
        performingLaboratory.put("TAHEN01", "2");
        performingLaboratory.put("TAHEN01", "2");
        performingLaboratory.put("TAHEN01", "2");
        performingLaboratory.put("TAHEN01", "2");
        performingLaboratory.put("TAHEN01", "2");
        performingLaboratory.put("TAHEN01", "2");
        performingLaboratory.put("TAHEN01", "2");
        performingLaboratory.put("TAHEN01", "2");
        performingLaboratory.put("TAHEN01", "2");
        performingLaboratory.put("TAHEN01", "2");
        performingLaboratory.put("TAHEN01", "2");
        performingLaboratory.put("TAHEN01", "2");
        performingLaboratory.put("TAHEN01", "2");
        performingLaboratory.put("TAHEN01", "2");
        performingLaboratory.put("TAHEN01", "2");
        performingLaboratory.put("TAHEN01", "2");
        performingLaboratory.put("TAHEN01", "2");
        performingLaboratory.put("TAHEN01", "2");
        performingLaboratory.put("TAHEN01", "2");
        performingLaboratory.put("TAHEN01", "2");
        testRequestCode.add("SCAN");
        testRequestCode.add("ADIFF");
        testRequestCode.add("BUAB");
        testRequestCode.add("CHS");
        testRequestCode.add("ADAM");
        testRequestCode.add("RDF");
        testRequestCode.add("PNHF");
        testRequestCode.add("FII");
        testRequestCode.add("FV");
        testRequestCode.add("FVII");
        testRequestCode.add("FVIII");
        testRequestCode.add("FIX");
        testRequestCode.add("FX");
        testRequestCode.add("FXI");
        testRequestCode.add("FXII");
        testRequestCode.add("IN2");
        testRequestCode.add("IN5");
        testRequestCode.add("IN7");
        testRequestCode.add("IN8");
        testRequestCode.add("IN9");
        testRequestCode.add("IN10");
        testRequestCode.add("IN11");
        testRequestCode.add("IN12");
        testRequestCode.add("JAK2");
        testRequestCode.add("FLDS");
        testRequestCode.add("DF");
        testRequestCode.add("PT50");
        testRequestCode.add("PTT50");
        testRequestCode.add("CD4P");
        testRequestCode.add("TBNKP");
        testRequestCode.add("OLIG");
        testRequestCode.add("ACA");
        testRequestCode.add("CP1");
        testRequestCode.add("CP2");
        testRequestCode.add("CP3");
        testRequestCode.add("CP4");
        testRequestCode.add("CP5");
        testRequestCode.add("CP7");
        testRequestCode.add("CP8");
        testRequestCode.add("CP9");
        testRequestCode.add("CP11");
        testRequestCode.add("CP12");
        testRequestCode.add("CP13");
        testRequestCode.add("CP14");
        testRequestCode.add("CP15");
        testRequestCode.add("CP16");
        testRequestCode.add("CP18");
        testRequestCode.add("UDRSC");
        testRequestCode.add("ULIT1");
        testRequestCode.add("ULIT2");
        testRequestCode.add("ULITN");
        testRequestCode.add("INT");
        //added 2/22/12
        testRequestCode.add("CALPN");
        testRequestCode.add("HBAB");
        testRequestCode.add("HBAG");
        testRequestCode.add("HIVL");
        testRequestCode.add("HBCT");
        testRequestCode.add("RUB");
        testRequestCode.add("HIVAB");
        testRequestCode.add("S75G");
        testRequestCode.add("MRSAM");
        testRequestCode.add("MRSAP");
        testRequestCode.add("MSPT");
        testRequestCode.add("RAST");
        testRequestCode.add("MISCS");
        testRequestCode.add("DCO2");
        testRequestCode.add("HBEAG");
        testRequestCode.add("HBEAB");
        testRequestCode.add("IGGSC");
        testRequestCode.add("CHRK");
        testRequestCode.add("DFAE");
        testRequestCode.add("CDIFP");
        testRequestCode.add("TXR42");
        testRequestCode.add("HTLV");
        testRequestCode.add("VAZI");
        //added 2/27/12
        testRequestCode.add("FEFAT");
        testRequestCode.add("CORN");
    }

    /**
     * Constructor
     */
    public Map_ORU_SMH_Soft_ORUint_ON_OLIS() {
    }
    private static int requestCount = 0;

    /**
     * POJO Operation
     *
     * @param input input of type NormalizedMessage input
     */
    @Operation
    public void receive(NormalizedMessage theNormalizedMessage) throws Exception {
        CanonicalHl7V2Message input = unmarshallCanonicalHl7V2Message(theNormalizedMessage);
        ORU_R01 olisOru = new ORU_R01();
        hapi.smh.soft.oru.message.ORU_R01 smhOru = new hapi.smh.soft.oru.message.ORU_R01();
        boolean unmarshalled = unmarshallHapiMessage(input, smhOru);


        String laboratoryFacilityOID = "";
        String hospitalOID;
        String msh7 = smhOru.getMSH().getMsh7_DateTimeOfMessage().getTime().getValue();
        String messageControlId = smhOru.getMSH().getMsh10_MessageControlID().getValue();
        String processingId;
        String sendingApplication;
        String universalServiceId = smhOru.getPATIENT_RESULT().getORDER_OBSERVATION(0).getOBR().getObr4_UniversalServiceIdentifier().getIdentifier().getValue();
        String resultStatus = smhOru.getPATIENT_RESULT().getORDER_OBSERVATION(0).getOBR().getObr25_ResultStatus().getValue();
        String receivingApplication = smhOru.getMSH().getMsh5_ReceivingApplication().getNamespaceID().getValue();
        
        String observationDateTime = smhOru.getPATIENT_RESULT().getORDER_OBSERVATION(0).getOBR().getObr7_ObservationDateTime().getTime().getValue();
        String specimenReceiveDateTime = smhOru.getPATIENT_RESULT().getORDER_OBSERVATION(0).getOBR().getObr14_SpecimenReceivedDateTime().getTime().getValue();
        
        //Environment Selection
        if (!"PROD".equals(input.getEnvironment())) {
            sendingApplication = "CN=LIS.MICHO2.PRODTEST, OU=Applications, OU=eHealthUsers, OU=Subscribers, DC=subscribers, DC=ssh";
            laboratoryFacilityOID = "2.16.840.1.113883.3.59.1:4083";
            hospitalOID = "2.16.840.1.113883.3.59.1:4083";
            processingId = "C";
        } else {
            sendingApplication = "CN=LIS.MICHO2, OU=Applications, OU=eHealthUsers, OU=Subscribers, DC=subscribers, DC=ssh";
            laboratoryFacilityOID = "2.16.840.1.113883.3.59.1:4083";
            hospitalOID = "2.16.840.1.113883.3.59.3:0852";
            processingId = "P";
        }

        //Terminology Validation
        String requestCode = universalServiceId;
        String requestCodeSystem = "";
        if (!"PIMS".equals(receivingApplication)) {
            requestCodeSystem = "1.3.6.1.4.1.12201.1.1.1.37";
        } else {
            requestCodeSystem = "1.3.6.1.4.1.12201.1.1.1.80";
        }
        ValidateCodeSimpleRequest request = new ValidateCodeSimpleRequest();

        request.setCodeId(requestCode);
        request.setCodeSystemId(requestCodeSystem);

        ValidateCodeSimpleResponse response = getTerminologyService().validateCodeSimple(request);

        boolean returnValue = response.isValidates();

        //Validating for Panels in the Terminology Table.
        if (!returnValue) {
            request.setCodeSystemId(requestCodeSystem + ".100");
            response = getTerminologyService().validateCodeSimple(request);

            returnValue = response.isValidates();
        }

        logger.info(" Terminology Response: " + returnValue);
        // If we are allowing for unknown codes to be passed in for mapping, then validate the code first

        // end of Terminology Validation


        //Start of the Translation
        if (!unmarshalled) {
            sendToDeadLetterAndForward(input, "INVALID_MESSAGE.UNMARSHAL", "An exception occurred during unmarshalling a message. Message was likely invalid in some way.");
            return;
        } else if (isEmpty(msh7)) {
            sendToDeadLetterAndForward(input, "INVALID_MESSAGE.V2.MSH-7", "MSH-7 is null.");
            return;
        } else if (isEmpty(observationDateTime) || isEmpty(specimenReceiveDateTime)) {
            skipAndForward(input, "OBR.7 or OBR.14 is missing; filtering for Wave 1");
            return;
            //        } else if (!returnValue) {
//            //Filter method
//            skipAndForward(input, "Test Request Code is not Mapped in the Hub OLIS terminology table  " + requestCodeSystem + ":" + requestCode);
//            return;
        } else if (testRequestCode.contains(universalServiceId) == true) {
            //Filter method
            skipAndForward(input, "Test Request Code is filtered for Wave 1");
            return;
        }
        else if (processMessage(input, smhOru, olisOru, laboratoryFacilityOID, hospitalOID, sendingApplication, processingId)) {
//            if ("PIMS".equals(receivingApplication) && ("PROD".equals(input.getEnvironment()))) {
//                if (!isSent()) {
//                    requestCount++;
//                    if (requestCount > 0) {
//                        skipAndForward(input, "Not sending more pathology messages.");
//                    }
//                }
//            }  
            marshallIntermediateAndForward(input, olisOru, "ON", "OLIS", "ORU");
        } else {
            logger.info("+++ Not going to forward message as it should already have been forwarded");
        }

    }





























































	private boolean processMessage(CanonicalHl7V2Message input, hapi.smh.soft.oru.message.ORU_R01 inputOru, ORU_R01 outputOru, String laboratoryFacilityOID, String hospitalOID, String sendingApplication, String processingId)
            throws Exception {
        String messageControlId = inputOru.getMSH().getMsh10_MessageControlID().getValue();
        logger.info("+++ Beginning mapping from SMH intermediate to OLIS format " + messageControlId);
        long startTime = System.currentTimeMillis();
        String receivingApplication = inputOru.getMSH().getMsh5_ReceivingApplication().getNamespaceID().getValue();
        String universalServiceId = inputOru.getPATIENT_RESULT().getORDER_OBSERVATION(0).getOBR().getObr4_UniversalServiceIdentifier().getIdentifier().getValue();

        String nextOrderId = "";

        //Extract IDs from message
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        String callingMethod = stackTraceElements[2].getMethodName();

        if (!"main".equals(callingMethod)) {
            for (int i = 0; i < inputOru.getPATIENT_RESULT().getORDER_OBSERVATIONReps(); i++) {
                Identifier identifier = new Identifier();
                identifier.setType(IdentifierType.ORDER);
                if ("PIMS".equals(receivingApplication)) {
                    nextOrderId = inputOru.getPATIENT_RESULT().getORDER_OBSERVATION(i).getOBR().getObr3_FillerOrderNumber().getEi1_EntityIdentifier().getValue();
                } else {
                    nextOrderId = inputOru.getPATIENT_RESULT().getORDER_OBSERVATION(i).getORC().getOrc4_PlacerGroupNumber().getEi1_EntityIdentifier().getValue();                    
                }
                identifier.setValue(nextOrderId);
                input.getAlternateIDs().add(identifier);
            }
        }

        if (!processMSH(inputOru, outputOru, sendingApplication, processingId)) {
            logger.info("+++ Leaving Pojo MSH Error");
            return false;
        }
        if (!processPID(input, inputOru, outputOru, hospitalOID)) {
            logger.info("+++ Leaving Pojo PID Error");
            return false;
        }

        if (!processZPD(input, inputOru, outputOru)) {
            logger.info("+++ Leaving Pojo ZPD Error");
            return false;
        }
        if (!processPV1(input, inputOru, outputOru, hospitalOID)) {
            logger.info("+++ Leaving Pojo PV1 Error");
            return false;
        }
        if ("PIMS".equals(receivingApplication)) {
            if (!mappingPathology(input, inputOru, outputOru, laboratoryFacilityOID, hospitalOID)) {
                logger.info("+++ Leaving Pojo mappingPathology Error");
                return false;
            }
        } else {
            if (!processORC(input, inputOru, outputOru, laboratoryFacilityOID)) {
                logger.info("+++ Leaving Pojo ORC Error");
                return false;
            }

            if (!processOBR(input, inputOru, outputOru, laboratoryFacilityOID, hospitalOID)) {
                logger.info("+++ Leaving Pojo OBR Error");
                return false;
            }

            if (!processZBR(input, inputOru, outputOru, laboratoryFacilityOID)) {
                logger.info("+++ Leaving Pojo ZBR Error");
                return false;
            }

            if (!processOrderNotes(input, inputOru, outputOru)) {
                logger.info("+++ Leaving Pojo NTE Error");
                return false;
            }
        }

        if (!processOBX(input, inputOru, outputOru)) {
            logger.info("+++ Leaving Pojo OBX Error");
            return false;
        }

        if (!processZBX(input, inputOru, outputOru)) {
            logger.info("+++ Leaving Pojo ZBX Error");
            return false;
        }
        if (!processObservationNotes(input, inputOru, outputOru)) {
            logger.info("+++ Leaving Pojo NTE Error");
            return false;
        }

        if (!processBLG(input, outputOru)) {
            logger.info("+++ Leaving Pojo BLG Error");
            return false;
        }

        //Extract IDs from message
//        for (int i = 0; i < outputOru.getPATIENT_RESULT().getORDER_OBSERVATIONReps(); i++) {
//            Identifier identifier = new Identifier();
//            identifier.setType(IdentifierType.ORDER);
//
//            nextOrderId = inputOru.getPATIENT_RESULT().getORDER_OBSERVATION(i).getORC().getOrc4_PlacerGroupNumber().getEi1_EntityIdentifier().getValue();
//            identifier.setValue(nextOrderId);
//            input.getAlternateIDs().add(identifier);
//        }

        long processingTime = System.currentTimeMillis() - startTime;
        logger.info("+++ Finished mapping from SMH intermediate to OLIS format in " + processingTime + " ms.");
        return true;
    }

    public boolean processMSH(hapi.smh.soft.oru.message.ORU_R01 input, ORU_R01 output, String sendingApplication, String processingId)
            throws Exception {
        // ++++++++++++++++++++Entering MSH+++++++++++++++++++++++++++++++++++++++++++


        String msh7 = "";

        msh7 = input.getMSH().getMsh7_DateTimeOfMessage().getTime().getValue();

        output.getMSH().getMsh1_FieldSeparator().setValue(input.getMSH().getMsh1_FieldSeparator().getValue());
        output.getMSH().getMsh2_EncodingCharacters().setValue(input.getMSH().getMsh2_EncodingCharacters().getValue());
        output.getMSH().getMsh3_SendingApplication().getNamespaceID().setValue("");
        output.getMSH().getMsh3_SendingApplication().getUniversalID().setValue(sendingApplication);
        output.getMSH().getMsh3_SendingApplication().getUniversalIDType().setValue("X500");
        output.getMSH().getMsh4_SendingFacility().getNamespaceID().setValue("StMcGTA");
        output.getMSH().getMsh5_ReceivingApplication().getNamespaceID().setValue("");
        output.getMSH().getMsh5_ReceivingApplication().getUniversalID().setValue("OLIS");
        output.getMSH().getMsh5_ReceivingApplication().getUniversalIDType().setValue("X500");
        output.getMSH().getMsh6_ReceivingFacility().parse("");
        output.getMSH().getMsh7_DateTimeOfMessage().getTime().setValue(msh7);
        output.getMSH().getMsh9_MessageType().getMessageCode().setValue("ORU");
        output.getMSH().getMsh9_MessageType().getTriggerEvent().setValue("R01");
        output.getMSH().getMsh9_MessageType().getMessageStructure().setValue("ORU_R01");
        output.getMSH().getMsh10_MessageControlID().setValue(input.getMSH().getMsh10_MessageControlID().getValue());
        output.getMSH().getMsh11_ProcessingID().getProcessingID().setValue(processingId);
        output.getMSH().getMsh12_VersionID().getVersionID().setValue("2.3.1");
        output.getMSH().getMsh18_CharacterSet(0).setValue("8859/1");

        // ++++++++++++++++++++Leaving MSH+++++++++++++++++++++++++++++++++++++++++++
        return true;
    }

    public boolean processPID(CanonicalHl7V2Message theInput, hapi.smh.soft.oru.message.ORU_R01 input, ORU_R01 output, String hospitalOID)
            throws Exception {
        // ++++++++++++++++++++Entering PID++++++++++++++++++++++++++++++++++++++++++

        String hcn = "";
        String hcnvc = "";
        String mrn = "";
        String pid19 = "";
        String receivingApplication = input.getMSH().getMsh5_ReceivingApplication().getNamespaceID().getValue();

        //Variables for Pathology Health Card Number
        String pathHealthCardId = "";
        String pathIdentifierTypeCode = "";
        String pathHealthCardVersionCode = "";
        String pathAssginAuthority = "";

        //Variables for Pathology  MRN
        String pathMRN = "";
        String patientIdentifierListAssigningFacilityUniversalId = "";

        if ("PIMS".equals(receivingApplication)) {
            /*Looping through PID-3, Checking for HCN
             * Copath is sending out multiple Patient ID in PID-3.  The Health Card Number may or may not be one of the Patient IDs.
             * Copath will send the the IdentifierTypeCode of "JHN" for the Health Card Number.
             */

            for (int patientIndex = 0; patientIndex < input.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierListReps(); patientIndex += 1) {
                if ("JHN".equals(input.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(patientIndex).getIdentifierTypeCode().getValue())) {
                    if (!isEmpty(input.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(patientIndex).getIDNumber().getValue())) {
                        pathHealthCardId = input.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(patientIndex).getIDNumber().getValue();
                        pathIdentifierTypeCode = input.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(patientIndex).getIdentifierTypeCode().getValue();
                        pathHealthCardVersionCode = input.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(patientIndex).getIDNumber().getExtraComponents().getComponent(0).getData().encode();
                        pathAssginAuthority = input.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(patientIndex).getIDNumber().getExtraComponents().getComponent(1).getData().encode();
                    }
                }
                if ("CMR".equals(input.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(patientIndex).getIdentifierTypeCode().getValue())) {
                    if (!isEmpty(input.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(patientIndex).getIDNumber().getValue())) {
                        pathMRN = input.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(0).getIDNumber().getValue();
                        patientIdentifierListAssigningFacilityUniversalId = input.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(patientIndex).getAssigningFacility().getUniversalID().getValue();
                    } else {
                        logger.fine("No Patient ID");
                    }
                }

            }
        } else {
            pid19 = input.getPATIENT_RESULT().getPATIENT().getPID().getPid19_SSNNumberPatient(0).getIdentifier().getValue();

            if (!isEmpty(pid19)) {
                if (pid19.length() < 10) {
                    logger.info("Ignoring HCN under 10 digits long");
                    logger.fine("HCN was: " + pid19);
                } else {
                    hcn = pid19.substring(0, 10);
                    if (pid19.length() > 10) {
                        if (pid19.length() == 11) {
                            hcnvc = pid19.substring(10, 11);
                        } else if (pid19.length() == 12) {
                            hcnvc = pid19.substring(10, 12);
                        }
                    } else {
                        logger.fine("No Version Code Available");
                    }
                }
            } else {
                logger.fine("No Health Card");
            }
            mrn = input.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(0).getIDNumber().getValue();
        }

        output.getPATIENT_RESULT().getPATIENT().getPID().getPid1_SetIDPID().setValue("1");
     
        if (!isEmpty(hcn)) {
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(0).getIDNumber().setValue(hcn);
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(0).getIdentifierTypeCode().setValue("JHN");
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(0).getAssigningJurisdiction().getCwe1_Identifier().setValue("ON");
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(0).getAssigningJurisdiction().getCwe2_Text().setValue("Ontario");
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(0).getAssigningJurisdiction().getCwe3_NameOfCodingSystem().setValue("HL70347");
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(1).getIDNumber().setValue(mrn);
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(1).getIdentifierTypeCode().setValue("MR");
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(1).getAssigningAuthority().getUniversalID().setValue(hospitalOID);
        } else if (!isEmpty(pathHealthCardId)) {
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(0).getIDNumber().setValue(pathHealthCardId);
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(0).getIdentifierTypeCode().setValue(pathIdentifierTypeCode);
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(0).getAssigningJurisdiction().getCwe1_Identifier().setValue(pathAssginAuthority);
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(0).getAssigningJurisdiction().getCwe2_Text().setValue("Ontario");
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(0).getAssigningJurisdiction().getCwe3_NameOfCodingSystem().setValue("HL70347");
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(1).getIDNumber().setValue(pathMRN);
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(1).getIdentifierTypeCode().setValue("MR");
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(1).getAssigningAuthority().getUniversalID().setValue(hospitalOID);
        } else if (!isEmpty(mrn)) {
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(0).getIDNumber().setValue(mrn);
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(0).getIdentifierTypeCode().setValue("MR");
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(0).getAssigningAuthority().getUniversalID().setValue(hospitalOID);
        } else if (!isEmpty(pathMRN)) {
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(0).getIDNumber().setValue(pathMRN);
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(0).getIdentifierTypeCode().setValue("MR");
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(0).getAssigningAuthority().getUniversalID().setValue(hospitalOID);
        } else {
            sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.PID-3", "Message contains neither an OHIP nor a MRN");
        }

        if (!isEmpty(hcnvc)) {
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(0).getExtraComponents().getComponent(0).parse(hcnvc);
        } else if (!isEmpty(pathHealthCardVersionCode)) {
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(0).getExtraComponents().getComponent(0).parse(pathHealthCardVersionCode);
        }
        output.getPATIENT_RESULT().getPATIENT().getPID().getPid5_PatientName().getFamilyName().getSurname().setValue(input.getPATIENT_RESULT().getPATIENT().getPID().getPid5_PatientName().getFamilyName().getSurname().getValue().toUpperCase());
        
        
        
        output.getPATIENT_RESULT().getPATIENT().getPID().getPid5_PatientName().getGivenName().parse(input.getPATIENT_RESULT().getPATIENT().getPID().getPid5_PatientName().getGivenName().encode().toUpperCase());
        String mName = input.getPATIENT_RESULT().getPATIENT().getPID().getPid5_PatientName().getSecondAndFurtherGivenNamesOrInitialsThereof().getValue();
        if (!isEmpty(mName)) {
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid5_PatientName().getSecondAndFurtherGivenNamesOrInitialsThereof().setValue(mName);
        }
        output.getPATIENT_RESULT().getPATIENT().getPID().getPid5_PatientName().getNameTypeCode().setValue("U");
        String gender = input.getPATIENT_RESULT().getPATIENT().getPID().getPid8_AdministrativeSex().getValue();
        if (!isEmpty(gender)) {
            if ("M".equals(input.getPATIENT_RESULT().getPATIENT().getPID().getPid8_AdministrativeSex().getValue()) || "F".equals(input.getPATIENT_RESULT().getPATIENT().getPID().getPid8_AdministrativeSex().getValue())) {
                output.getPATIENT_RESULT().getPATIENT().getPID().getPid8_AdministrativeSex().setValue(gender);
            } else {
                output.getPATIENT_RESULT().getPATIENT().getPID().getPid8_AdministrativeSex().setValue("U");
            }
        } else {
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid8_AdministrativeSex().setValue("U");
        }
        String dob = input.getPATIENT_RESULT().getPATIENT().getPID().getPid7_DateTimeOfBirth().getTime().getValue();
        if (!isEmpty(dob)) {
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid7_DateTimeOfBirth().getTime().setValue(dob);
        } else {
            logger.fine("NO DOB");
        }
        // Multiple Addresses, OLIS needs only one from Ontario

        String zipOrPostalCode = input.getPATIENT_RESULT().getPATIENT().getPID().getPid11_PatientAddress(0).getZipOrPostalCode().getValue();
        String province = input.getPATIENT_RESULT().getPATIENT().getPID().getPid11_PatientAddress(0).getStateOrProvince().getValue();
        String streetAddress = input.getPATIENT_RESULT().getPATIENT().getPID().getPid11_PatientAddress(0).getStreetAddress().getStreetOrMailingAddress().getValue();

        Set<String> provinceCheck = new HashSet();

        provinceCheck.add("AB");
        provinceCheck.add("BC");
        provinceCheck.add("MB");
        provinceCheck.add("NB");
        provinceCheck.add("NL");
        provinceCheck.add("NT");
        provinceCheck.add("NS");
        provinceCheck.add("NU");
        provinceCheck.add("ON");
        provinceCheck.add("PE");
        provinceCheck.add("QC");
        provinceCheck.add("SK");
        provinceCheck.add("YT");

        if (!isEmpty(streetAddress)) {
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid11_PatientAddress(0).parse(input.getPATIENT_RESULT().getPATIENT().getPID().getPid11_PatientAddress(0).encode());
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid11_PatientAddress(0).getAddressType().setValue("H");
            if (!isEmpty(zipOrPostalCode)) {
                if (zipOrPostalCode.length() < 6) {
                    sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.PID-11.5", "Invalid Postal Code");
                } else {
                    if (zipOrPostalCode.length() < 7) {
                        StringBuffer bufferPostalCode = new StringBuffer();
                        bufferPostalCode.append(zipOrPostalCode);
                        bufferPostalCode.insert(3, " ");
                        String postalCode = bufferPostalCode.toString();
                        output.getPATIENT_RESULT().getPATIENT().getPID().getPid11_PatientAddress(0).getZipOrPostalCode().setValue(postalCode);
                    } else if (zipOrPostalCode.contains("-")) {
                        zipOrPostalCode = zipOrPostalCode.replace("-", " ");
                        output.getPATIENT_RESULT().getPATIENT().getPID().getPid11_PatientAddress(0).getZipOrPostalCode().setValue(zipOrPostalCode);
                    } else {
                        output.getPATIENT_RESULT().getPATIENT().getPID().getPid11_PatientAddress(0).getZipOrPostalCode().setValue(zipOrPostalCode);
                    }
                }
            }
            if (provinceCheck.contains(province)) {
                output.getPATIENT_RESULT().getPATIENT().getPID().getPid11_PatientAddress(0).getCountry().setValue("CAN");
            }

        } else {
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid11_PatientAddress(0).parse("");
        }

        String homePhoneNumber = input.getPATIENT_RESULT().getPATIENT().getPID().getPid13_PhoneNumberHome(0).getTelephoneNumber().getValue();
        String workPhoneNumber = input.getPATIENT_RESULT().getPATIENT().getPID().getPid14_PhoneNumberBusiness(0).getTelephoneNumber().getValue();
        if (!isEmpty(homePhoneNumber)) {
            homePhoneNumber = formatTelephone(homePhoneNumber);
            homePhoneNumber = homePhoneNumber.trim();
            if (homePhoneNumber.length() < 9) {
                output.getPATIENT_RESULT().getPATIENT().getPID().getPid13_PhoneNumberHome(0).parse("");
            } else {
                output.getPATIENT_RESULT().getPATIENT().getPID().getPid13_PhoneNumberHome(0).getTelephoneNumber().setValue("");
                output.getPATIENT_RESULT().getPATIENT().getPID().getPid13_PhoneNumberHome(0).getTelecommunicationUseCode().setValue("PRN");
                output.getPATIENT_RESULT().getPATIENT().getPID().getPid13_PhoneNumberHome(0).getTelecommunicationEquipmentType().setValue("PH");
                //output.getPATIENT_RESULT().getPATIENT().getPID().getPid13_PhoneNumberHome(0).getCountryCode().setValue("1");
                output.getPATIENT_RESULT().getPATIENT().getPID().getPid13_PhoneNumberHome(0).getAreaCityCode().setValue(homePhoneNumber.substring(0, 3));
                int numberEnd = homePhoneNumber.length();
                if (numberEnd > 10) {
                    numberEnd = 10;
                }
                output.getPATIENT_RESULT().getPATIENT().getPID().getPid13_PhoneNumberHome(0).getLocalNumber().setValue(homePhoneNumber.substring(3, numberEnd));
                if (homePhoneNumber.length() > 10) {
                    output.getPATIENT_RESULT().getPATIENT().getPID().getPid13_PhoneNumberHome(0).getExtension().setValue(homePhoneNumber.substring(11));
                }
            }
        }

        if (!isEmpty(workPhoneNumber)) {
            workPhoneNumber = formatTelephone(workPhoneNumber);
            workPhoneNumber = workPhoneNumber.trim();
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid14_PhoneNumberBusiness(0).getTelephoneNumber().setValue("");
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid14_PhoneNumberBusiness(0).getTelecommunicationUseCode().setValue("WPN");
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid14_PhoneNumberBusiness(0).getTelecommunicationEquipmentType().setValue("PH");
            //output.getPATIENT_RESULT().getPATIENT().getPID().getPid14_PhoneNumberBusiness(0).getCountryCode().setValue("1");
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid14_PhoneNumberBusiness(0).getAreaCityCode().setValue(workPhoneNumber.substring(0, 3));
            int numberEnd = workPhoneNumber.length();
            if (numberEnd > 10) {
                numberEnd = 10;
            }
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid14_PhoneNumberBusiness(0).getLocalNumber().setValue(workPhoneNumber.substring(3, numberEnd));
            if (workPhoneNumber.length() > 10) {
                output.getPATIENT_RESULT().getPATIENT().getPID().getPid14_PhoneNumberBusiness(0).getExtension().setValue(workPhoneNumber.substring(11));
            }
        }


        String deathTime = input.getPATIENT_RESULT().getPATIENT().getPID().getPid29_PatientDeathDateAndTime().getTime().getValue();
        if (!isEmpty(deathTime)) {
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid29_PatientDeathDateAndTime().getTime().setValue(deathTime);
        }
        if ("Y".equals(input.getPATIENT_RESULT().getPATIENT().getPID().getPid30_PatientDeathIndicator().getValue())) {
            output.getPATIENT_RESULT().getPATIENT().getPID().getPid30_PatientDeathIndicator().setValue(input.getPATIENT_RESULT().getPATIENT().getPID().getPid30_PatientDeathIndicator().getValue());
        }

        // ++++++++++++++++++++Leaving PID+++++++++++++++++++++++++++++++++++++++++++
        return true;
    }

    public boolean processZPD(CanonicalHl7V2Message thInput, hapi.smh.soft.oru.message.ORU_R01 input, ORU_R01 output) throws Exception {

        // ++++++++++++++++++++Entering ZPD++++++++++++++++++++++++++++++++++++++++++
        String hcn = input.getPATIENT_RESULT().getPATIENT().getPID().getPid19_SSNNumberPatient(0).getIdentifier().getValue();
        String receivingApplication = input.getMSH().getMsh5_ReceivingApplication().getNamespaceID().getValue();
        String pathHealthCardNumberId = "";
        for (int patientIndex = 0; patientIndex < input.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierListReps(); patientIndex += 1) {
            if ("JHN".equals(input.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(patientIndex).getIdentifierTypeCode().getValue())) {
                if (!isEmpty(input.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(patientIndex).getIDNumber().getValue())) {
                    pathHealthCardNumberId = input.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList(patientIndex).getIDNumber().getValue();

                }
            }
        }
        if ("PIMS".equals(receivingApplication)) {
            if (isEmpty(pathHealthCardNumberId)) {
                output.getPATIENT_RESULT().getPATIENT().getZPD().getZpd2_PatientIdentificationVerifiedFlag().setValue("Y");
            }
        } else {
            if (isEmpty(hcn)) {
                output.getPATIENT_RESULT().getPATIENT().getZPD().getZpd2_PatientIdentificationVerifiedFlag().setValue("Y");
            }
        }
        // ++++++++++++++++++++Leaving ZPD++++++++++++++++++++++++++++++++++++++++++
        return true;
    }

	public boolean processPV1(CanonicalHl7V2Message theInput, hapi.smh.soft.oru.message.ORU_R01 input, ORU_R01 output, String hospitalOID)
            throws Exception {
        // ++++++++++++++++++++Entering PV1+++++++++++++++++++++++++++++++++++++++++++

        String pointOfCare = "";
        String attendingPhysicianId = "";
        String admittingPhysicianId = "";
        String patientClass;
        String receivingApplication = input.getMSH().getMsh5_ReceivingApplication().getNamespaceID().getValue();

        output.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv11_SetIDPV1().setValue("1");

        patientClass = input.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv12_PatientClass().getValue();
        if (!"I".equals(patientClass) && !"E".equals(patientClass)) {
            output.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv12_PatientClass().setValue("0");
        } else {
            output.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv12_PatientClass().setValue(patientClass);
        }

        pointOfCare = input.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv13_AssignedPatientLocation().getPointOfCare().getValue();

        logger.fine("+++ Location length: " + pointOfCare);
        if (isEmpty(pointOfCare)) {
            logger.fine("+++ PV1[3] is null +++");
        } else if (pointOfCare.length() > 30) {
            output.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv13_AssignedPatientLocation().getPointOfCare().setValue(pointOfCare.substring(0, 25));
        } else {
            output.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv13_AssignedPatientLocation().getPointOfCare().setValue(pointOfCare);
        }


        //Populating the Attending and Admitting Physician Fields
        attendingPhysicianId = input.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv17_AttendingDoctor().getIDNumber().getValue();
        admittingPhysicianId = input.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv117_AdmittingDoctor().getIDNumber().getValue();
        logger.fine("+++AdmitPhysician: " + admittingPhysicianId);


        if (!isEmpty(attendingPhysicianId)) {
            output.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv17_AttendingDoctor().getIDNumber().setValue(attendingPhysicianId);
            output.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv17_AttendingDoctor().getFamilyName().getSurname().setValue(input.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv17_AttendingDoctor().getFamilyName().getSurname().getValue());
            output.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv17_AttendingDoctor().getGivenName().setValue(input.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv17_AttendingDoctor().getGivenName().getValue());
            if ("PIMS".equals(receivingApplication)) {
                output.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv17_AttendingDoctor().getIdentifierTypeCode().setValue("MDL");
            } else {
                output.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv17_AttendingDoctor().getIdentifierTypeCode().setValue("EI");
            }
            if ("PIMS".equals(receivingApplication)) {
                output.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv17_AttendingDoctor().getAssigningAuthority().getNamespaceID().setValue("ON");
            } else {
                output.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv17_AttendingDoctor().getAssigningAuthority().getNamespaceID().setValue(hospitalOID);
            }

            output.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv17_AttendingDoctor().getNameTypeCode().setValue("");
            output.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv17_AttendingDoctor().getSuffixEgJRorIII().setValue("");

        }

        if (!isEmpty(admittingPhysicianId)) {
            output.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv117_AdmittingDoctor().getIDNumber().setValue(admittingPhysicianId);
            output.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv117_AdmittingDoctor().getFamilyName().getSurname().setValue(input.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv117_AdmittingDoctor().getFamilyName().getSurname().getValue());
            output.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv117_AdmittingDoctor().getGivenName().setValue(input.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv117_AdmittingDoctor().getGivenName().getValue());
            if ("PIMS".equals(receivingApplication)) {
                output.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv117_AdmittingDoctor().getIdentifierTypeCode().setValue("MDL");
            } else {
                output.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv117_AdmittingDoctor().getIdentifierTypeCode().setValue("EI");
            }
            if ("PIMS".equals(receivingApplication)) {
                output.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv117_AdmittingDoctor().getAssigningAuthority().getNamespaceID().setValue("ON");
            } else {
                output.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv117_AdmittingDoctor().getAssigningAuthority().getNamespaceID().setValue(hospitalOID);
            }

            output.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv117_AdmittingDoctor().getNameTypeCode().setValue("");
            output.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1().getPv117_AdmittingDoctor().getSuffixEgJRorIII().setValue("");

        }



        // ++++++++++++++++++++Leaving PV1+++++++++++++++++++++++++++++++++++++++++++
        return true;
    }

    public boolean processORC(CanonicalHl7V2Message theInput, hapi.smh.soft.oru.message.ORU_R01 input, ORU_R01 output, String laboratoryFacilityOID)
            throws Exception {
        // ++++++++++++++++++++Entering ORC+++++++++++++++++++++++++++++++++++++++++++
        String sourceSystem = "ON_OLIS";

        for (int indexOrderObservation = 0; indexOrderObservation < input.getPATIENT_RESULT().getORDER_OBSERVATIONReps(); indexOrderObservation += 1) {
            String placerOrderNumber = input.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getORC().getOrc2_PlacerOrderNumber().getEntityIdentifier().getValue();
            String placerGroupNumber = input.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getORC().getOrc4_PlacerGroupNumber().getEntityIdentifier().getValue();
            String fillerOrderNumber = input.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getORC().getOrc3_FillerOrderNumber().getEntityIdentifier().getValue();

            output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getORC().getOrc1_OrderControl().setValue("");

            //Populating of ORC-4
            if (!isEmpty(placerGroupNumber)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getORC().getOrc4_PlacerGroupNumber().getEntityIdentifier().setValue(placerGroupNumber);
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getORC().getOrc4_PlacerGroupNumber().getNamespaceID().setValue("ON_OLIS");
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getORC().getOrc4_PlacerGroupNumber().getUniversalID().setValue(laboratoryFacilityOID);
            } else if (!isEmpty(placerOrderNumber)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getORC().getOrc4_PlacerGroupNumber().getEntityIdentifier().setValue(placerOrderNumber);
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getORC().getOrc4_PlacerGroupNumber().getNamespaceID().setValue("ON_OLIS");
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getORC().getOrc4_PlacerGroupNumber().getUniversalID().setValue(laboratoryFacilityOID);
            } else if (!isEmpty(fillerOrderNumber)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getORC().getOrc4_PlacerGroupNumber().getEntityIdentifier().setValue(fillerOrderNumber);
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getORC().getOrc4_PlacerGroupNumber().getNamespaceID().setValue("ON_OLIS");
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getORC().getOrc4_PlacerGroupNumber().getUniversalID().setValue(laboratoryFacilityOID);
            } else {
                sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.ORC-4.1", "No placer group number");
            }


            String dateTiemOfTransaction = input.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getORC().getOrc9_DateTimeOfTransaction().getTime().getValue();
            logger.info("+++ ORC9: " + dateTiemOfTransaction);
            if (!isEmpty(dateTiemOfTransaction)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getORC().getOrc9_DateTimeOfTransaction().getTime().setValue(dateTiemOfTransaction);
            } else {
                sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.ORC-9.1", "No date/time of transaction provided");
            }

            output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getORC().getOrc21_OrderingFacilityName().getAssigningAuthority().getNamespaceID().setValue(sourceSystem);
            output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getORC().getOrc21_OrderingFacilityName().getAssigningAuthority().getUniversalID().setValue(laboratoryFacilityOID);

        }


        // ++++++++++++++++++++Leaving ORC+++++++++++++++++++++++++++++++++++++++++++
        return true;
    }

    public boolean processOBR(CanonicalHl7V2Message theInput, hapi.smh.soft.oru.message.ORU_R01 input, ORU_R01 output, String laboratoryFacilityOID, String hospitalOID)
            throws Exception {
        // ++++++++++++++++++++Entering OBR+++++++++++++++++++++++++++++++++++++++++++

        int obrSetId = 0;

        for (int indexOrderObservation = 0; indexOrderObservation < input.getPATIENT_RESULT().getORDER_OBSERVATIONReps(); indexOrderObservation += 1) {
            obrSetId = obrSetId + 1;
            String universalServiceId = input.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr4_UniversalServiceIdentifier().getIdentifier().getValue();
            String placerOrderNumber = input.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr2_PlacerOrderNumber().getEntityIdentifier().getValue();
            String fillerOrderNumber = input.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr3_FillerOrderNumber().getEntityIdentifier().getValue();
            String placerGroupNumber = input.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getORC().getOrc4_PlacerGroupNumber().getEntityIdentifier().getValue();
            String priority = input.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr27_QuantityTiming().getTq6_Priority().getValue();



            output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr1_SetIDOBR().setValue(Integer.toString(obrSetId));

            if (!isEmpty(placerOrderNumber)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr2_PlacerOrderNumber().getEntityIdentifier().setValue(placerOrderNumber);
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr2_PlacerOrderNumber().getNamespaceID().setValue("ON_OLIS");
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr2_PlacerOrderNumber().getUniversalID().setValue(laboratoryFacilityOID);
            } else if (!isEmpty(placerGroupNumber)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr2_PlacerOrderNumber().getEntityIdentifier().setValue(placerGroupNumber + universalServiceId);
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr2_PlacerOrderNumber().getNamespaceID().setValue("ON_OLIS");
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr2_PlacerOrderNumber().getUniversalID().setValue(laboratoryFacilityOID);
            } else if (!isEmpty(fillerOrderNumber)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr2_PlacerOrderNumber().getEntityIdentifier().setValue(fillerOrderNumber);
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr2_PlacerOrderNumber().getNamespaceID().setValue("ON_OLIS");
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr2_PlacerOrderNumber().getUniversalID().setValue(laboratoryFacilityOID);
            } else {
                sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.OBR-2.1", "Missing Placer Order ID");
            }

            if (!isEmpty(fillerOrderNumber)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr3_FillerOrderNumber().getEntityIdentifier().setValue(fillerOrderNumber);
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr3_FillerOrderNumber().getNamespaceID().setValue("ON_OLIS");
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr3_FillerOrderNumber().getUniversalID().setValue(laboratoryFacilityOID);
            } else if (!isEmpty(placerGroupNumber)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr3_FillerOrderNumber().getEntityIdentifier().setValue(placerGroupNumber + universalServiceId);
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr3_FillerOrderNumber().getNamespaceID().setValue("ON_OLIS");
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr3_FillerOrderNumber().getUniversalID().setValue(laboratoryFacilityOID);
            } else {
                sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.OBR-3.1", "Missing Filler Order ID");
            }

            if (!isEmpty(universalServiceId)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr4_UniversalServiceIdentifier().getIdentifier().setValue(universalServiceId);
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr4_UniversalServiceIdentifier().getText().setValue(input.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr4_UniversalServiceIdentifier().getText().getValue());
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr4_UniversalServiceIdentifier().getNameOfCodingSystem().setValue("1.3.6.1.4.1.12201.1.1.1.37");


            } else {
                sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.OBR-4.1", "No Universal Service Identifier - Code");
            }
            // reverse the data in OBR-7 and OBR-14
            output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr7_ObservationDateTime().getTime().setValue(input.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr14_SpecimenReceivedDateTime().getTime().getValue());
            output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr14_SpecimenReceivedDateTime().getTime().setValue(input.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr7_ObservationDateTime().getTime().getValue());
            String specimenReceivedDateTime = input.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr14_SpecimenReceivedDateTime().getTime().getValue();

            String orderProviderId = input.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr16_OrderingProvider().getIDNumber().getValue();
            if (!isEmpty(orderProviderId)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr16_OrderingProvider().getIDNumber().setValue(orderProviderId);
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr16_OrderingProvider().getFamilyName().getSurname().setValue(input.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr16_OrderingProvider().getFamilyName().getSurname().getValue());
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr16_OrderingProvider().getGivenName().setValue(input.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr16_OrderingProvider().getGivenName().getValue());
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr16_OrderingProvider().getIdentifierTypeCode().setValue("EI");
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr16_OrderingProvider().getAssigningAuthority().getNamespaceID().setValue(hospitalOID);
            } else {
                sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.OBR-16.1", "No ordering provider");
            }

            String performingLabSpecimenId = input.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr20_PerformingLabUserReadableSpecimenIdentifier().getCe1_Identifier().getValue();

            if (!isEmpty(performingLabSpecimenId)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr20_PerformingLabUserReadableSpecimenIdentifier().getCe1_Identifier().setValue(performingLabSpecimenId);
            }



            //Looping through the OBX Segments to capture the value of "C" in OBX-11
            String observationResultStatus;
            int countObservationSegment = input.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBSERVATIONReps();
            int indexObservation = 0;
            do {
                observationResultStatus = input.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBSERVATION(indexObservation).getOBX().getObx11_ObservationResultStatus().getValue();
                indexObservation++;
            } while (!"C".equals(observationResultStatus) && !"W".equals(observationResultStatus) && countObservationSegment > indexObservation);

            String resultStatus = input.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr25_ResultStatus().getValue();

            //Populate OBR-25 with either Observation Result Flag or OBR-25 value.
            if (isEmpty(resultStatus)) {
                sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.OBR-25", "Missing Result Status");
            } else if (!"C".equals(observationResultStatus) && !"W".equals(observationResultStatus)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr25_ResultStatus().setValue(resultStatus);
            } else if ("W".equals(observationResultStatus)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr25_ResultStatus().setValue("C");
            } else {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr25_ResultStatus().setValue(observationResultStatus);
            }

            String quantityTimingStartTime = input.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr27_QuantityTiming().getStartDateTime().getTime().getValue();
            output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr27_QuantityTiming().getQuantity().getQuantity().setValue("1");
            if (!isEmpty(quantityTimingStartTime)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr27_QuantityTiming().getStartDateTime().getTime().setValue(quantityTimingStartTime);
            } else {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr27_QuantityTiming().getStartDateTime().getTime().setValue(specimenReceivedDateTime);
            }

            if (!isEmpty(priority)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr27_QuantityTiming().getPriority().setValue(priority);
            } else {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr27_QuantityTiming().getPriority().setValue("R");
            }


            //Looping through OBR-28 to remove Practitioner that don't have an ID
            for (int resultCopiedIndex = 0; resultCopiedIndex < input.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr28_ResultCopiesToReps(); resultCopiedIndex++) {
                String resultCopiesToIdNumber = input.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr28_ResultCopiesTo(resultCopiedIndex).getIDNumber().getValue();
                if (isEmpty(resultCopiesToIdNumber)) {
                    input.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().removeObr28_ResultCopiesTo(resultCopiedIndex);
                    resultCopiedIndex--;
                }

            }

            for (int resultCopiedIndex = 0; resultCopiedIndex < input.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr28_ResultCopiesToReps(); resultCopiedIndex++) {
                String resultCopiesToIdNumber = input.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr28_ResultCopiesTo(resultCopiedIndex).getIDNumber().getValue();

                if (!isEmpty(resultCopiesToIdNumber)) {
                    output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr28_ResultCopiesTo(resultCopiedIndex).getIDNumber().setValue(resultCopiesToIdNumber);
                    output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr28_ResultCopiesTo(resultCopiedIndex).getIdentifierTypeCode().setValue("EI");
                    output.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBR().getObr28_ResultCopiesTo(resultCopiedIndex).getAssigningAuthority().getNamespaceID().setValue(hospitalOID);
                } else {
                    logger.fine("+++ No Result Copies To Physician ID ");
                }
            }

        }

        // ++++++++++++++++++++Leaving OBR+++++++++++++++++++++++++++++++++++++++++++
        return true;
    }

    public boolean processZBR(CanonicalHl7V2Message theInput, hapi.smh.soft.oru.message.ORU_R01 input, ORU_R01 output, String laboratoryFacilityOID)
            throws Exception {
        // ++++++++++++++++++++Entering ZBR++++++++++++++++++++++++++++++++++++++++++
        String sourceSystem = "ON_OLIS";
        int zbrIndex = 0;
        for (int orderIndex = 0; orderIndex < input.getPATIENT_RESULT().getORDER_OBSERVATIONReps(); orderIndex += 1) {
            zbrIndex = zbrIndex + 1;
            String performingLabUserReadableSpecimenIdentifier = input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr20_PerformingLabUserReadableSpecimenIdentifier().getCe1_Identifier().getValue();
            String sortKey = Integer.toString(zbrIndex);
            sortKey = StringUtils.leftPad(sortKey, 4, "0");
            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getZBR().getZbr2_TestRequestPlacer().getOrganizationName().setValue(laboratoryFacilityOID);
            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getZBR().getZbr2_TestRequestPlacer().getAssigningAuthority().getNamespaceID().setValue(sourceSystem);
            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getZBR().getZbr2_TestRequestPlacer().getAssigningAuthority().getUniversalID().setValue(laboratoryFacilityOID);
            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getZBR().getZbr3_SpecimenCollector().getOrganizationName().setValue(laboratoryFacilityOID);
            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getZBR().getZbr3_SpecimenCollector().getAssigningAuthority().getNamespaceID().setValue(sourceSystem);
            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getZBR().getZbr3_SpecimenCollector().getAssigningAuthority().getUniversalID().setValue(laboratoryFacilityOID);
            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getZBR().getZbr4_ReportingLaboratory().getOrganizationName().setValue(laboratoryFacilityOID);
            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getZBR().getZbr4_ReportingLaboratory().getAssigningAuthority().getNamespaceID().setValue(sourceSystem);
            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getZBR().getZbr4_ReportingLaboratory().getAssigningAuthority().getUniversalID().setValue(laboratoryFacilityOID);

            if (!isEmpty(performingLabUserReadableSpecimenIdentifier)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getZBR().getZbr6_PerformingLaboratory().getOrganizationName().setValue(performingLaboratorySearch(performingLabUserReadableSpecimenIdentifier));
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getZBR().getZbr6_PerformingLaboratory().getAssigningAuthority().getNamespaceID().setValue(sourceSystem);
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getZBR().getZbr6_PerformingLaboratory().getAssigningAuthority().getUniversalID().setValue(performingLaboratorySearch(performingLabUserReadableSpecimenIdentifier));
            } else {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getZBR().getZbr6_PerformingLaboratory().getOrganizationName().setValue(laboratoryFacilityOID);
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getZBR().getZbr6_PerformingLaboratory().getAssigningAuthority().getNamespaceID().setValue(sourceSystem);
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getZBR().getZbr6_PerformingLaboratory().getAssigningAuthority().getUniversalID().setValue(laboratoryFacilityOID);
            }

            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getZBR().getZbr11_TestRequestSortKey().setValue(sortKey);
        }
        // ++++++++++++++++++++Leaving ZBR+++++++++++++++++++++++++++++++++++++++++++
        return true;
    }

    public boolean processOrderNotes(CanonicalHl7V2Message theInput, hapi.smh.soft.oru.message.ORU_R01 input, ORU_R01 output)
            throws Exception {
        // ++++++++++++++++++++Entering NTE+++++++++++++++++++++++++++++++++++++++++++
        int nteSetId = 0;
        StringBuffer buffer = new StringBuffer();
        String commentNote = "";

        int nteCount = input.getPATIENT_RESULT().getORDER_OBSERVATION(0).getORDER_NOTEReps();

        if (nteCount > 0) {
            for (int i1 = 0; i1 < input.getPATIENT_RESULT().getORDER_OBSERVATION(0).getORDER_NOTEReps(); i1 += 1) {

                nteSetId = nteSetId + 1;

                buffer.append(input.getPATIENT_RESULT().getORDER_OBSERVATION(0).getORDER_NOTE(i1).getNTE().getComment(0).getValue());
                buffer.append("\\.br\\");
            }
            commentNote = buffer.toString();
            output.getPATIENT_RESULT().getORDER_OBSERVATION(0).getORDER_NOTE(0).getNTE().getSetIDNTE().setValue("1");
            output.getPATIENT_RESULT().getORDER_OBSERVATION(0).getORDER_NOTE(0).getNTE().getComment(0).setValue(commentNote);
            buffer.delete(0, buffer.length());
        }



        // ++++++++++++++++++++Leaving NTE+++++++++++++++++++++++++++++++++++++++++++
        return true;
    }

    public boolean processOBX(CanonicalHl7V2Message theInput, hapi.smh.soft.oru.message.ORU_R01 input, ORU_R01 output)
            throws Exception {
        // ++++++++++++++++++++Entering OBX+++++++++++++++++++++++++++++++++++++++++++
        String pathologistLname = "";
        String pathologistFname = "";
        String cytotechLname = "";
        String cytotechFname = "";
        String receivingApplication = input.getMSH().getMsh5_ReceivingApplication().getNamespaceID().getValue();


        for (int orderIndex = 0; orderIndex < input.getPATIENT_RESULT().getORDER_OBSERVATIONReps(); orderIndex += 1) {
            int obxSetId = 0;

            //Pathology Module.  Retrieving Pathologist Name and will create an new OBX Segment.

            String pathologistId = input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr32_PrincipalResultInterpreter().getNameOfPerson().getIDNumber().getValue();
            pathologistLname = input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr32_PrincipalResultInterpreter().getStartDateTime().getTime().getValue();

            if (!isEmpty(pathologistId)) {

                pathologistLname = input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr32_PrincipalResultInterpreter().getStartDateTime().getTime().getValue();
                pathologistFname = input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr32_PrincipalResultInterpreter().getEndDateTime().getTime().getValue();

            } else if (!isEmpty(pathologistLname)) {
                pathologistLname = input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr32_PrincipalResultInterpreter().getStartDateTime().getTime().getValue();
                pathologistFname = input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr32_PrincipalResultInterpreter().getEndDateTime().getTime().getValue();
            } else {
                logger.fine(" No Pathologists Reported ");
            }

            //Retrieve Cytotechonologist Name
            String universalServiceId = input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr4_UniversalServiceIdentifier().getIdentifier().getValue();
            String cytotechnologist = input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr34_Technician(0).getNdl1_NameOfPerson().getFamilyName().getValue();

            if (!isEmpty(cytotechnologist)) {
                cytotechLname = input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr34_Technician(0).getNdl1_NameOfPerson().getFamilyName().getValue();
                cytotechFname = input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr34_Technician(0).getNdl1_NameOfPerson().getGivenName().getValue();
            }

            for (int observationIndex = 0; observationIndex < input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATIONReps(); observationIndex += 1) {
                obxSetId = obxSetId + 1;
                String dataType = input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx2_ValueType().getValue();
                String observationIdentifier = input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx3_ObservationIdentifier().getIdentifier().getValue();
                String observationValue = input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx5_ObservationValue(0).encode();


                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx1_SetIDOBX().setValue(Integer.toString(obxSetId));

                if (observationValue != null && observationValue.matches(".*[a-zA-Z].*") && "NM".equals(dataType)) {
                    //The code below is to change the Value Type from NM to ST
                    output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx2_ValueType().setValue("ST");
                } else if (!isEmpty(observationValue) && observationValue.matches(".*[a-zA-Z].*") && "ST".equals(dataType)) {
                    //The code below keeps the Value Type of ST
                    output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx2_ValueType().setValue("ST");
                } else if (!isEmpty(observationValue) && observationValue.matches(".*[a-zA-Z].*") && "TX".equals(dataType)) {
                    //The code below keeps the Value Type of ST
                    output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx2_ValueType().setValue("ST");
                } else if (!isEmpty(observationValue) && observationValue.matches(".*[^0-9. ].*") && "ST".equals(dataType)) {
                    //The code below is to change the Value Type from ST to SN
                    output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx2_ValueType().setValue("SN");
                } else if (!isEmpty(observationValue) && observationValue.matches(".*[^0-9. ].*") && "TX".equals(dataType)) {
                    //The code below is to change the Value Type from ST to SN
                    output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx2_ValueType().setValue("SN");
                } else if (!isEmpty(observationValue) && observationValue.matches(".*[0-9].*") && "ST".equals(dataType)) {
                    //The code below is to change the Value Type from ST to NM
                    output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx2_ValueType().setValue("NM");
                } else if (!isEmpty(observationValue) && observationValue.matches(".*[0-9].*") && "TX".equals(dataType)) {
                    //The code below is to change the Value Type from ST to NM
                    output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx2_ValueType().setValue("NM");
                } else if (observationValue != null && observationValue.startsWith("-") && "NM".equals(dataType)) {
                    //The code below keeps the Value Type of NM
                    output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx2_ValueType().setValue("NM");
                } else if (observationValue != null && observationValue.matches(".*[^0-9. ].*") && "NM".equals(dataType)) {
                    //The code below is to change the Value Type from NM to SN
                    output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx2_ValueType().setValue("SN");
                } else {
                    output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx2_ValueType().setValue(dataType);
                }


                if (!isEmpty(observationIdentifier)) {
                    output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx3_ObservationIdentifier().getIdentifier().setValue(observationIdentifier);
                    output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx3_ObservationIdentifier().getText().setValue(input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx3_ObservationIdentifier().getText().getValue());
                    if (!"PIMS".equals(receivingApplication)) {
                        output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx3_ObservationIdentifier().getNameOfCodingSystem().setValue("1.3.6.1.4.1.12201.1.1.1.38");
                    } else {
                        output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx3_ObservationIdentifier().getNameOfCodingSystem().setValue("1.3.6.1.4.1.12201.1.1.1.81");
                    }
                } else {
                    sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.OBX-3.1", "Missing Observation Identifier");
                    return false;
                }

                // The hash map is created to log all the OBX segments that repeat.  The Hash map will be used to populate
                //OBX-4 SubId field.

                Map<String, Integer> observationId2Count = new HashMap<String, Integer>();
                Map<String, String> malariaObservationId = new HashMap<String, String>();
                malariaObservationId.put("MALB", "1");
                malariaObservationId.put("MALS", "2");
                malariaObservationId.put("MALV", "3");
                //malariaObservationId.put("SOMAL", "4");



                for (int observationIndex1 = 0; observationIndex1 < output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATIONReps(); observationIndex1++) {


                    String mapobservationIdentifier = output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex1).getOBX().getObx3_ObservationIdentifier().getCe1_Identifier().getValue();
                    String nameOfCodingSystem = output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex1).getOBX().getObx3_ObservationIdentifier().getCe3_NameOfCodingSystem().getValue();

                    //There are local result codes that are mapped to the same OLIS LOINC Code.
                    //Calling the Terminology Service to retrieve the OLIS LOINC Code.
                    //OLIS LOINC Code will be add to the Hash Map so the Sub-ID is incremented.

                    CD mappedCode = lookupTerminologySingleCode(mapobservationIdentifier, nameOfCodingSystem);
                    String retVal = "";
                    if (mappedCode == null) {
                        String errorCode = CanonicalMessageUtils.createMessageErroredForV2MappingErrorCode("OBX", "3", null);
                        String errorMessage = CanonicalMessageUtils.createMessageErroredForV2MappingErrorMessage(getTerminologyService(), mapobservationIdentifier, nameOfCodingSystem, "1.3.6.1.4.1.12201.1.1.1.3");
                        sendToDeadLetterAndForward(theInput, errorCode, errorMessage);
                        return false;
                    } else {
                        retVal = mappedCode.getCode();
                    }

                    if (!observationId2Count.containsKey(retVal)) {
                        observationId2Count.put(retVal, 0);
                    }

                    int count = observationId2Count.get(retVal);

                    count = count + 1;

                    output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex1).getOBX().getObx4_ObservationSubID().setValue(Integer.toString(count));

                    observationId2Count.put(retVal, count);

                }
                // Remove the SubId Number if there is only one Observation Identifier.  If there are
                // more than one Observation Identifier in the message then SubId is populated.
                //Malaria Tests will have subId populated from the HashMap above called malariaObservationId
                for (int observationIndex1 = 0; observationIndex1 < output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATIONReps(); observationIndex1++) {

                    String mapobservationIdentifier = output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex1).getOBX().getObx3_ObservationIdentifier().getCe1_Identifier().getValue();
                    String nameOfCodingSystem = output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex1).getOBX().getObx3_ObservationIdentifier().getCe3_NameOfCodingSystem().getValue();

                    CD mappedCode = lookupTerminologySingleCode(mapobservationIdentifier, nameOfCodingSystem);
                    String retVal = mappedCode.getCode();

                    int countmap = observationId2Count.get(retVal);

                    String subId = output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex1).getOBX().getObx4_ObservationSubID().getValue();
                    if (malariaObservationId.containsKey(mapobservationIdentifier) == true) {
                        output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex1).getOBX().getObx4_ObservationSubID().setValue(malariaObservationId.get(mapobservationIdentifier));
                    } else {
//                      commenting out the part that blanks out the subID if there is only one result for an observation identifier. 
//                      We need this to accommodate SMH BB work flow, and there is no harm in including it for all modalities
//                      so we are doing this for all results.                        
//                        if (observationId2Count.get(retVal).intValue() > 1) {
                            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex1).getOBX().getObx4_ObservationSubID().setValue(subId);
//                        } else {
//                            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex1).getOBX().getObx4_ObservationSubID().setValue("");
//                        }
                    }

                }

                // OBX-5 field might contain the '&' in the value.  The code below will ensure that all the data is not missed when sending to OLIS
                StringBuilder buffer = new StringBuilder();
                String observationValueBuffer = "";
                int countObservationValueComponents = 0;
                countObservationValueComponents = input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx5_ObservationValue(0).getExtraComponents().numComponents();

                if (countObservationValueComponents > 0) {
                    for (int observationValueIndex = 0; observationValueIndex < input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx5_ObservationValue(0).getExtraComponents().numComponents(); observationValueIndex++) {
                        buffer.append(input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx5_ObservationValue(0).getExtraComponents().getComponent(observationValueIndex).encode());
                        buffer.append(" ");
                    }
                    observationValueBuffer = buffer.toString();

                }
                int ph = observationValue.indexOf("&");
                String observationCombinedValue = "";
                if (ph == -1) {
                    logger.fine("No Report Indicator");
                    //System.out.println("No Report Indicator");
                } else {
                    observationCombinedValue = observationValue.substring(0, ph) + observationValueBuffer;

                }



                //The code below will separate the OBX-5 value into components in the OBX-5 field
                if (countObservationValueComponents > 0) {
                    if (observationValue != null && observationValue.startsWith("-") && "NM".equals(dataType)) {
                        output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx5_ObservationValue(0).parse(observationValue);
                    } else if (observationValue != null && observationValue.matches(".*[^0-9. ].*") && "NM".equals(dataType)) {
                        output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx5_ObservationValue(0).parse(observationValue(observationValue));
                    } else if (!isEmpty(observationValue) && observationValue.matches(".*[a-zA-Z].*") && "ST".equals(dataType)) {
                        output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx5_ObservationValue(0).parse(observationValue);
                    } else if (!isEmpty(observationValue) && observationValue.matches(".*[a-zA-Z].*") && "TX".equals(dataType)) {
                        output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx5_ObservationValue(0).parse(observationValue);
                    } else if (observationValue != null && observationValue.matches(".*[^0-9. ].*") && "ST".equals(dataType)) {
                        output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx5_ObservationValue(0).parse(observationValue(observationValue));
                    } else if (observationValue != null && observationValue.matches(".*[^0-9. ].*") && "TX".equals(dataType)) {
                        output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx5_ObservationValue(0).parse(observationValue(observationValue));
                    } else {
                        output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx5_ObservationValue(0).parse(observationValue);
                    }
                } else {
                    if (observationValue != null && observationValue.startsWith("-") && "NM".equals(dataType)) {
                        output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx5_ObservationValue(0).parse(observationValue);
                    } else if (observationValue != null && observationValue.matches(".*[^0-9. ].*") && "NM".equals(dataType)) {
                        output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx5_ObservationValue(0).parse(observationValue(observationValue));
                    } else if (!isEmpty(observationValue) && observationValue.matches(".*[a-zA-Z].*") && "ST".equals(dataType)) {
                        output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx5_ObservationValue(0).parse(observationValue);
                    } else if (!isEmpty(observationValue) && observationValue.matches(".*[a-zA-Z].*") && "TX".equals(dataType)) {
                        output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx5_ObservationValue(0).parse(observationValue);
                    } else if (observationValue != null && observationValue.matches(".*[^0-9. ].*") && "ST".equals(dataType)) {
                        output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx5_ObservationValue(0).parse(observationValue(observationValue));
                    } else if (observationValue != null && observationValue.matches(".*[^0-9. ].*") && "TX".equals(dataType)) {
                        output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx5_ObservationValue(0).parse(observationValue(observationValue));
                    } else {
                        output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx5_ObservationValue(0).parse(observationValue);
                    }
                }

                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx6_Units().getIdentifier().setValue(input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx6_Units().getIdentifier().getValue());
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx7_ReferencesRange().setValue(input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx7_ReferencesRange().getValue());
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx8_AbnormalFlags(0).setValue(input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx8_AbnormalFlags(0).getValue());
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx11_ObservationResultStatus().setValue(input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx11_ObservationResultStatus().getValue());
                //output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx14_DateTimeOfTheObservation().getTime().setValue(input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx14_DateTimeOfTheObservation().getTime().getValue());

            }

            //Insert OBX Segment based on the Pathologist in OBR-32
            int count = output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATIONReps();

            //Build of new OBX segments for Pathologist and Cytotechnoloigst

            String sortKey = Integer.toString(count + 1);
            sortKey = StringUtils.leftPad(sortKey, 4, '0');

            if (isEmpty(pathologistLname)) {
                logger.fine("+++No Pathologist Name+++");
            } else {
                ORU_R01_OBSERVATION OBXGroup = output.getPATIENT_RESULT().getORDER_OBSERVATION(0).getOBSERVATION(count);

                OBXGroup.getOBX().getObx1_SetIDOBX().setValue(Integer.toString(count + 1));
                OBXGroup.getOBX().getObx2_ValueType().setValue("ST");
                OBXGroup.getOBX().getObx3_ObservationIdentifier().getIdentifier().setValue("PN");
                OBXGroup.getOBX().getObx3_ObservationIdentifier().getText().setValue("Pathologist Name");
                OBXGroup.getOBX().getObx3_ObservationIdentifier().getNameOfCodingSystem().setValue("1.3.6.1.4.1.12201.1.1.1.81");
                OBXGroup.getOBX().getObx5_ObservationValue(0).parse(pathologistLname + ", " + pathologistFname);
                OBXGroup.getOBX().getObx11_ObservationResultStatus().setValue("F");
                OBXGroup.getZBX().getZbx1_TestResultReleaseDateTime().getTime().setValue(input.getMSH().getMsh7_DateTimeOfMessage().getTs1_Time().getValue());
                OBXGroup.getZBX().getTestResultSortKey().setValue(sortKey);

            }
            if (!"33717-0".equals(universalServiceId)) {
                logger.fine("+++ Not a GYN Procedure No Tech Name+++");
            } else {

                if (!isEmpty(pathologistLname) && !isEmpty(cytotechLname)) {
                    ORU_R01_OBSERVATION OBXGroup1 = output.getPATIENT_RESULT().getORDER_OBSERVATION(0).getOBSERVATION(count + 1);

                    OBXGroup1.getOBX().getObx1_SetIDOBX().setValue(Integer.toString(count + 2));
                    OBXGroup1.getOBX().getObx2_ValueType().setValue("ST");
                    OBXGroup1.getOBX().getObx3_ObservationIdentifier().getIdentifier().setValue("CN");
                    OBXGroup1.getOBX().getObx3_ObservationIdentifier().getText().setValue("Cytotechnologist Name");
                    OBXGroup1.getOBX().getObx3_ObservationIdentifier().getNameOfCodingSystem().setValue("1.3.6.1.4.1.12201.1.1.1.81");
                    OBXGroup1.getOBX().getObx5_ObservationValue(0).parse(cytotechLname + ", " + cytotechFname);
                    OBXGroup1.getOBX().getObx11_ObservationResultStatus().setValue("F");
                    OBXGroup1.getZBX().getZbx1_TestResultReleaseDateTime().getTime().setValue(input.getMSH().getMsh7_DateTimeOfMessage().getTs1_Time().getValue());
                    
                    sortKey = Integer.toString(count + 2);
                    sortKey = StringUtils.leftPad(sortKey, 4, '0');
                    
                    OBXGroup1.getZBX().getTestResultSortKey().setValue(sortKey);
                } else if (!isEmpty(cytotechLname)) {
                    ORU_R01_OBSERVATION OBXGroup1 = output.getPATIENT_RESULT().getORDER_OBSERVATION(0).getOBSERVATION(count);

                    OBXGroup1.getOBX().getObx1_SetIDOBX().setValue(Integer.toString(count + 1));
                    OBXGroup1.getOBX().getObx2_ValueType().setValue("ST");
                    OBXGroup1.getOBX().getObx3_ObservationIdentifier().getIdentifier().setValue("CN");
                    OBXGroup1.getOBX().getObx3_ObservationIdentifier().getNameOfCodingSystem().setValue("1.3.6.1.4.1.12201.1.1.1.81");
                    OBXGroup1.getOBX().getObx5_ObservationValue(0).parse(cytotechLname + ", " + cytotechFname);
                    OBXGroup1.getOBX().getObx11_ObservationResultStatus().setValue("F");
                    OBXGroup1.getZBX().getZbx1_TestResultReleaseDateTime().getTime().setValue(input.getMSH().getMsh7_DateTimeOfMessage().getTs1_Time().getValue());
                    OBXGroup1.getZBX().getTestResultSortKey().setValue(sortKey);

                } else {
                    logger.fine("+++ GYN Procedure No Tech Name+++");
                }
            }

        }

        // ++++++++++++++++++++Leaving OBX+++++++++++++++++++++++++++++++++++++++++++
        return true;
    }

    public boolean processZBX(CanonicalHl7V2Message theInput, hapi.smh.soft.oru.message.ORU_R01 input, ORU_R01 output)
            throws Exception {
        // ++++++++++++++++++++Entering NTE+++++++++++++++++++++++++++++++++++++++++++


        for (int orderIndex = 0; orderIndex < input.getPATIENT_RESULT().getORDER_OBSERVATIONReps(); orderIndex += 1) {

            int zbxSetId = 0;
            for (int observationIndex = 0; observationIndex < input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATIONReps(); observationIndex += 1) {
                String observationIdentifier = input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getOBX().getObx3_ObservationIdentifier().getIdentifier().getValue();
                zbxSetId = zbxSetId + 1;
                String sortKey = Integer.toString(zbxSetId);
                sortKey = StringUtils.leftPad(sortKey, 4, "0");
                String testResultReleaseDateTime = input.getMSH().getMsh7_DateTimeOfMessage().getTime().getValue();

                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getZBX().getZbx1_TestResultReleaseDateTime().getTime().setValue(testResultReleaseDateTime);


                if ("REVW".equals(observationIdentifier)) {
                    output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getZBX().getZbx2_TestResultSortKey().setValue("9999" + zbxSetId);
                } else {
                    output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBSERVATION(observationIndex).getZBX().getZbx2_TestResultSortKey().setValue(sortKey);
                }

            }



        }


        // ++++++++++++++++++++Leaving NTE+++++++++++++++++++++++++++++++++++++++++++
        return true;
    }

    public boolean processObservationNotes(CanonicalHl7V2Message theInput, hapi.smh.soft.oru.message.ORU_R01 input, ORU_R01 output)
            throws Exception {
        // ++++++++++++++++++++Entering NTE+++++++++++++++++++++++++++++++++++++++++++
        int nteSetId = 0;
        StringBuffer buffer = new StringBuffer();
        String commentNote = "";
        for (int i1 = 0; i1 < input.getPATIENT_RESULT().getORDER_OBSERVATION(0).getOBSERVATIONReps(); i1 += 1) {
            int nteCount = input.getPATIENT_RESULT().getORDER_OBSERVATION(0).getOBSERVATION(i1).getOBSERVATION_NOTEReps();

            if (nteCount > 0) {
                for (int i2 = 0; i2 < input.getPATIENT_RESULT().getORDER_OBSERVATION(0).getOBSERVATION(i1).getOBSERVATION_NOTEReps(); i2 += 1) {
                    //If the repeating NTE segments are greater than 5 then
                    //the Hub will concatentate the NTE segments into 1 NTE segment.

                    nteSetId = nteSetId + 1;

                    buffer.append(input.getPATIENT_RESULT().getORDER_OBSERVATION(0).getOBSERVATION(i1).getOBSERVATION_NOTE(i2).getNTE().getComment(0).getValue());
                    buffer.append("\\.br\\");
                    commentNote = buffer.toString();
                }

                output.getPATIENT_RESULT().getORDER_OBSERVATION(0).getOBSERVATION(i1).getOBSERVATION_NOTE(0).getNTE().getSetIDNTE().setValue("1");
                output.getPATIENT_RESULT().getORDER_OBSERVATION(0).getOBSERVATION(i1).getOBSERVATION_NOTE(0).getNTE().getComment(0).setValue(commentNote);

                buffer.delete(0, buffer.length());

            }



        }


        // ++++++++++++++++++++Leaving NTE+++++++++++++++++++++++++++++++++++++++++++
        return true;
    }

    public boolean processBLG(CanonicalHl7V2Message theInput, ORU_R01 output)
            throws Exception {
        // ++++++++++++++++++++Entering BLG++++++++++++++++++++++++++++++++++++++++++
        for (int orderIndex = 0; orderIndex < output.getPATIENT_RESULT().getORDER_OBSERVATIONReps(); orderIndex += 1) {
            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getBLG().getBlg3_AccountID().getIDNumber().setValue("MOHLTC");
        }
        // ++++++++++++++++++++Leaving BLG+++++++++++++++++++++++++++++++++++++++++++
        return true;
    }
    // Logger
    private static final Logger logger = Logger.getLogger(Map_ORU_SMH_Soft_ORUint_ON_OLIS.class.getName());
    // POJO Context
    @Resource
    private Context jbiCtx;

    @Override
    protected void forward(Source theOutput) throws Exception {
        sepSOAP_Map_ORUint_ON_OLIS_ORU_ON_OLIS.sendSynchInOut(theOutput, Consumer.MessageObjectType.String);
    }

    public boolean mappingPathology(CanonicalHl7V2Message theInput, hapi.smh.soft.oru.message.ORU_R01 input, ORU_R01 output, String laboratoryFacilityOID, String hospitalOID)
            throws Exception {
        // ++++++++++++++++++++Entering Pathology+++++++++++++++++++++++++++++++++++++++++++
        logger.info("Entering Pathology Mapping");

        String sourceSystem = "ON_OLIS";
        String specimenReceiveTime = "";
        int obrSetId = 0;
        for (int indexOrderObservation = 0; indexOrderObservation < input.getPATIENT_RESULT().getORDER_OBSERVATIONReps(); indexOrderObservation += 1) {
            //Copath send 2 OBR Segments.  The first OBR Segment does not have any OBX Segments.  The code will look for OBX Segments.  If OBX segment is null
            // The code will remove the OBR Segment.
            if (input.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBSERVATIONReps() == 0) {
                input.getPATIENT_RESULT().removeORDER_OBSERVATION(indexOrderObservation);
                logger.fine("Removed the OBR Segment with No OBX Segments");
                indexOrderObservation--;
                continue;
            }
            
            // Copath interface now has the ability to separate the Synoptic Results from the standard Pathology Results.
            // Copath will now send out two OBR segments for the Synoptic Reporting.  OLIS at the present time does not want the Synoptic Result in the message
            // Coapth will send the Synoptic Result as the first OBR segment.  Synoptic Results can be identified with the ValueType of CWE.
            // The code will remove the Synoptic OBR semgent from the message.
            specimenReceiveTime = input.getPATIENT_RESULT().getORDER_OBSERVATION(0).getOBR().getObr14_SpecimenReceivedDateTime().getTime().getValue();

            for (int observationIndex2 = 0; observationIndex2 < input.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBSERVATIONReps(); observationIndex2++) {
                if ("CWE".equals(input.getPATIENT_RESULT().getORDER_OBSERVATION(indexOrderObservation).getOBSERVATION(observationIndex2).getOBX().getObx2_ValueType().getValue())) {
                    input.getPATIENT_RESULT().removeORDER_OBSERVATION(indexOrderObservation);
                    indexOrderObservation--;
                    break;
                }
            }
        }

        int zbrIndex = 0;
        for (int orderIndex = 0; orderIndex < input.getPATIENT_RESULT().getORDER_OBSERVATIONReps(); orderIndex += 1) {
            String placerOrderNumber = input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr2_PlacerOrderNumber().getEntityIdentifier().getValue();
            String fillerOrderNumber = input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr3_FillerOrderNumber().getEntityIdentifier().getValue();
            String dateTimeOfMessage = input.getMSH().getMsh7_DateTimeOfMessage().getTime().getValue();
            String universalServiceId = input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr4_UniversalServiceIdentifier().getIdentifier().getValue();
            String dateTimeOfTransaction = input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getORC().getOrc9_DateTimeOfTransaction().getTime().getValue();
            String requestDateTime = input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr6_RequestedDateTime(0).getTime().getValue();
            String observationDateTime = input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr7_ObservationDateTime().getTime().getValue();

            if (!isEmpty(placerOrderNumber)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getORC().getOrc4_PlacerGroupNumber().getEntityIdentifier().setValue(placerOrderNumber);
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getORC().getOrc4_PlacerGroupNumber().getNamespaceID().setValue("ON_OLIS");
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getORC().getOrc4_PlacerGroupNumber().getUniversalID().setValue(laboratoryFacilityOID);
            } else if (!isEmpty(fillerOrderNumber)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getORC().getOrc4_PlacerGroupNumber().getEntityIdentifier().setValue(fillerOrderNumber);
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getORC().getOrc4_PlacerGroupNumber().getNamespaceID().setValue("ON_OLIS");
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getORC().getOrc4_PlacerGroupNumber().getUniversalID().setValue(laboratoryFacilityOID);
            }

            if (!isEmpty(dateTimeOfTransaction)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getORC().getOrc9_DateTimeOfTransaction().getTime().setValue(dateTimeOfTransaction);
            } else if (!isEmpty(observationDateTime)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getORC().getOrc9_DateTimeOfTransaction().getTime().setValue(observationDateTime);
            } else {
                sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.ORC-9", "No date/time of transaction provided");
            }

            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getORC().getOrc21_OrderingFacilityName().getAssigningAuthority().getNamespaceID().setValue(sourceSystem);
            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getORC().getOrc21_OrderingFacilityName().getAssigningAuthority().getUniversalID().setValue(laboratoryFacilityOID);

            //OBR Mappings
            obrSetId = obrSetId + 1;
            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr1_SetIDOBR().setValue(Integer.toString(obrSetId));
            if (!isEmpty(placerOrderNumber)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr2_PlacerOrderNumber().getEntityIdentifier().setValue(placerOrderNumber);
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr2_PlacerOrderNumber().getNamespaceID().setValue("ON_OLIS");
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr2_PlacerOrderNumber().getUniversalID().setValue(laboratoryFacilityOID);
            } else if (!isEmpty(fillerOrderNumber)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr2_PlacerOrderNumber().getEntityIdentifier().setValue(fillerOrderNumber);
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr2_PlacerOrderNumber().getNamespaceID().setValue("ON_OLIS");
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr2_PlacerOrderNumber().getUniversalID().setValue(laboratoryFacilityOID);
            } else {
                sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.OBR-2.1", "Missing Placer Order ID");
            }

            if (!isEmpty(fillerOrderNumber)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr3_FillerOrderNumber().getEntityIdentifier().setValue(fillerOrderNumber);
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr3_FillerOrderNumber().getNamespaceID().setValue("ON_OLIS");
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr3_FillerOrderNumber().getUniversalID().setValue(laboratoryFacilityOID);
            } else {
                sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.OBR-3.1", "Missing Filler Order ID");
            }

            if (!isEmpty(universalServiceId)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr4_UniversalServiceIdentifier().getIdentifier().setValue(universalServiceId);
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr4_UniversalServiceIdentifier().getText().setValue(input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr4_UniversalServiceIdentifier().getText().getValue());
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr4_UniversalServiceIdentifier().getNameOfCodingSystem().setValue("1.3.6.1.4.1.12201.1.1.1.80");
            } else {
                sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.OBR-4.1", "No Universal Service Identifier - Code");
            }



            if (!isEmpty(observationDateTime)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr7_ObservationDateTime().getTime().setValue(observationDateTime);
            } else {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr7_ObservationDateTime().getTime().setValue(specimenReceiveTime);
            }

            if (!isEmpty(specimenReceiveTime)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr14_SpecimenReceivedDateTime().getTs1_Time().setValue(specimenReceiveTime);
            } else {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr14_SpecimenReceivedDateTime().getTs1_Time().setValue(observationDateTime);
            }

            String orderProviderId = input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr16_OrderingProvider().getIDNumber().getValue();
            if (!isEmpty(orderProviderId)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr16_OrderingProvider().getIDNumber().setValue(orderProviderId);
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr16_OrderingProvider().getFamilyName().getSurname().setValue(input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr16_OrderingProvider().getFamilyName().getSurname().getValue());
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr16_OrderingProvider().getGivenName().setValue(input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr16_OrderingProvider().getGivenName().getValue());
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr16_OrderingProvider().getIdentifierTypeCode().setValue("MDL");
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr16_OrderingProvider().getAssigningAuthority().getNamespaceID().setValue("ON");
            } else {
                sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.OBR-16.1", "No ordering provider");
            }

            String performingLabSpecimenId = input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr20_PerformingLabUserReadableSpecimenIdentifier().getCe1_Identifier().getValue();

            if (!isEmpty(performingLabSpecimenId)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr20_PerformingLabUserReadableSpecimenIdentifier().getCe1_Identifier().setValue(performingLabSpecimenId);
            }
            
            String resultStatus = input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr25_ResultStatus().getValue();

            if (!isEmpty(resultStatus)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr25_ResultStatus().setValue(resultStatus);
            } else {
                sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.OBR-25", "Missing Result Status");
            }

            //Quantity and Timing
            String priority = input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr27_QuantityTiming().getQuantity().getQuantity().getValue();

            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr27_QuantityTiming().getQuantity().getQuantity().setValue("1");

            if (!isEmpty(observationDateTime)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr27_QuantityTiming().getStartDateTime().getTime().setValue(observationDateTime);
            } else if (!isEmpty(specimenReceiveTime)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr27_QuantityTiming().getStartDateTime().getTime().setValue(specimenReceiveTime);
            }
            if (!isEmpty(priority)) {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr27_QuantityTiming().getPriority().setValue(priority);
            } else {
                output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr27_QuantityTiming().getPriority().setValue("R");
            }


            //Looping through OBR-28 to remove Practitioner that don't have an ID
            for (int resultCopiedIndex = 0; resultCopiedIndex < input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr28_ResultCopiesToReps(); resultCopiedIndex++) {
                String resultCopiesToIdNumber = input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr28_ResultCopiesTo(resultCopiedIndex).getIDNumber().getValue();
                if (isEmpty(resultCopiesToIdNumber)) {
                    input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().removeObr28_ResultCopiesTo(resultCopiedIndex);
                    resultCopiedIndex--;
                }

            }
            
            for (int resultCopiedIndex = 0; resultCopiedIndex < input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr28_ResultCopiesToReps(); resultCopiedIndex++) {
                String resultCopiesToIdNumber = input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr28_ResultCopiesTo(resultCopiedIndex).getIDNumber().getValue();

                if (!isEmpty(resultCopiesToIdNumber)) {
                    output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr28_ResultCopiesTo(resultCopiedIndex).getIDNumber().setValue(resultCopiesToIdNumber);
                    output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr28_ResultCopiesTo(resultCopiedIndex).getFamilyName().getSurname().setValue(input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr28_ResultCopiesTo(resultCopiedIndex).getFamilyName().getSurname().getValue());
                    output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr28_ResultCopiesTo(resultCopiedIndex).getGivenName().setValue(input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr28_ResultCopiesTo(resultCopiedIndex).getGivenName().getValue());
                    //changed so CC doctors for path will use the practitioner mapping service, as SMH is sending the internal ID.
                    output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr28_ResultCopiesTo(resultCopiedIndex).getIdentifierTypeCode().setValue("EI");
                    output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr28_ResultCopiesTo(resultCopiedIndex).getAssigningAuthority().getNamespaceID().setValue(hospitalOID);
                } else {
                    logger.fine("+++ No Result Copies To Physician ID ");
                }

            }

            String diagnosticServSecId = input.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getOBR().getObr24_DiagnosticServSectID().getValue();

            zbrIndex = zbrIndex + 1;
            String sortKey = Integer.toString(zbrIndex);
            sortKey =
                    StringUtils.leftPad(sortKey, 4, "0");
            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getZBR().getZbr2_TestRequestPlacer().getOrganizationName().setValue(laboratoryFacilityOID);
            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getZBR().getZbr2_TestRequestPlacer().getAssigningAuthority().getNamespaceID().setValue(sourceSystem);
            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getZBR().getZbr2_TestRequestPlacer().getAssigningAuthority().getUniversalID().setValue(laboratoryFacilityOID);
            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getZBR().getZbr3_SpecimenCollector().getOrganizationName().setValue(laboratoryFacilityOID);
            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getZBR().getZbr3_SpecimenCollector().getAssigningAuthority().getNamespaceID().setValue(sourceSystem);
            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getZBR().getZbr3_SpecimenCollector().getAssigningAuthority().getUniversalID().setValue(laboratoryFacilityOID);
            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getZBR().getZbr4_ReportingLaboratory().getOrganizationName().setValue(laboratoryFacilityOID);
            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getZBR().getZbr4_ReportingLaboratory().getAssigningAuthority().getNamespaceID().setValue(sourceSystem);
            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getZBR().getZbr4_ReportingLaboratory().getAssigningAuthority().getUniversalID().setValue(laboratoryFacilityOID);
            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getZBR().getZbr6_PerformingLaboratory().getOrganizationName().setValue(laboratoryFacilityOID);
            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getZBR().getZbr6_PerformingLaboratory().getAssigningAuthority().getNamespaceID().setValue(sourceSystem);
            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getZBR().getZbr6_PerformingLaboratory().getAssigningAuthority().getUniversalID().setValue(laboratoryFacilityOID);
            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getZBR().getZbr6_PerformingLaboratory().getOrganizationName().setValue(laboratoryFacilityOID);
            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getZBR().getZbr6_PerformingLaboratory().getAssigningAuthority().getNamespaceID().setValue(sourceSystem);
            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getZBR().getZbr6_PerformingLaboratory().getAssigningAuthority().getUniversalID().setValue(laboratoryFacilityOID);
            output.getPATIENT_RESULT().getORDER_OBSERVATION(orderIndex).getZBR().getZbr11_TestRequestSortKey().setValue(sortKey);

        }






        // ++++++++++++++++++++Leaving Pathology+++++++++++++++++++++++++++++++++++++++++++
        return true;
    }

    public static String formatTelephone(String unformatted) {
        unformatted = unformatted.replaceAll("-", "");
        // unformatted = unformatted.replaceFirst( "\\)", "^" );
        unformatted = unformatted.replaceAll("\\(", "");
        unformatted = unformatted.replaceAll("\\)", "");
        unformatted = unformatted.replaceAll(" ", "");
        unformatted = unformatted.replaceAll("X", " ");
        return unformatted;
    }

    public CD lookupTerminologySingleCode(String trmCode, String trmNamingofCoding) throws Exception {

        TranslateCodeSimpleRequest vcr = new TranslateCodeSimpleRequest();
        vcr.setCodeId(trmCode);
        vcr.setCodeSystemId(trmNamingofCoding);
        vcr.setToCodeSystemId("1.3.6.1.4.1.12201.1.1.1.3");
        TranslateCodeSimpleResponse translatedCode = getTerminologyService().translateCodeSimple(vcr);

        List<org.hl7.cts.types.CD> retVal = translatedCode.getCode();
        if (retVal.size() == 0) {
            return null;
        }

        return retVal.get(0);
    }

    public static String performingLaboratorySearch(String performingLaboratorySearch) {
        performingLaboratory.containsKey(performingLaboratorySearch);
        performingLaboratorySearch = performingLaboratory.get(performingLaboratorySearch);
        logger.info("PFL: " + performingLaboratorySearch);
        return performingLaboratorySearch;
    }

    public static String replaceEncodedCharacters(String encodedCharacters) {
        if (encodedCharacters == null) {
            return null;
        }

        encodedCharacters = encodedCharacters.replaceAll("&", "");
        encodedCharacters = encodedCharacters.replaceAll("\\^", " ");
        // encodedCharacters = encodedCharacters.replaceAll( "\\", " " );
        encodedCharacters = encodedCharacters.replaceAll("\\~", "");
        encodedCharacters = encodedCharacters.replaceAll("HL70396", "");
        return encodedCharacters;
    }

    public static String observationValue(String observationvalue) {

        StringBuilder bufferComparator = new StringBuilder();
        bufferComparator.append(observationvalue);


        if (observationvalue.startsWith("<=")) {
            bufferComparator.insert(2, '^');
        } else if (observationvalue.startsWith("<")) {
            bufferComparator.insert(1, '^');
        } else if (observationvalue.startsWith(">=")) {
            bufferComparator.insert(2, '^');
        } else if (observationvalue.startsWith(">")) {
            bufferComparator.insert(1, '^');
        }

        observationvalue = bufferComparator.toString();

        StringBuilder bufferSeparator = new StringBuilder();
        bufferSeparator.append(observationvalue);

        if (observationvalue.contains(":")) {
            int colonIndex = observationvalue.indexOf(":");

            bufferSeparator.insert(0, '^');
            bufferSeparator.insert(colonIndex + 1, '^');
            bufferSeparator.insert(colonIndex + 3, '^');
        } else if (observationvalue.contains("-")) {
            int minusIndex = observationvalue.indexOf("-");
            bufferSeparator.insert(0, '^');
            bufferSeparator.insert(minusIndex + 1, '^');
            bufferSeparator.insert(minusIndex + 3, '^');
        } else if (observationvalue.contains("+")) {
            int plusIndex = observationvalue.indexOf("+");
            bufferSeparator.insert(0, '^');
            bufferSeparator.insert(plusIndex + 1, '^');
            bufferSeparator.insert(plusIndex + 3, '^');
        }
        observationvalue = bufferSeparator.toString();

        return observationvalue;
    }

    public static void main(String[] args) throws HL7Exception, Exception {

        String biochemMessageText =
                //Biochemistry
                "MSH|^~\\&|LAB|SCC||SMH|201004211412||ORU^R01|84812667|P|2.2\r" +
                "PID|||4001647||RHODODENDRON^PAUL^R||19581030|M|||NFA^ST MIKE'S DETOX^TORONTO^ON^m5g1x4||(416)555-5555|||||00270011877|2000010179B\r" +
                "PV1||I|16CN||00270011455||30329||||||||||||||||||||||||||||||||SMH\r" +
                "ORC|RE|10633753|2|E2190001|||^^^201004191500^^T||201004191255|HIS||16164|3B^B1\r" +
                "NTE|||2||\r" +
                "OBR|1|10633753|2|LYT4^Digoxin|||201004191300|||GAF||||201004191300||16164||||||201004191301||BCHEM|F||^^^201004191500^^T|12345~~MHDOC||||GAF\r" +
                "NTE|||3||\r" +
                "NTE|||4||\r" +
                "OBX|1|NM|DIG^Digoxin||2.5 |nmol/L|1.0-2.6||||F|||201004191301|B|GAF\r" +
//                "OBX|1|NM|DIG^Digoxin||3.0+4.0|nmol/L|1.0-2.6||||C|||201004191301|B|GAF\r" +
                "NTE|||2||\r";

//                "MSH|^~\\&|LAB|SCC|OLIS|SMH|201101111537||ORU^R01|00782704|P|2.3.1||||||8859/1\r" +
//                "PID|||J4001645||IRONWOOD^BIBIANA^V||19700510|F|||2301-30 BOND ST^^TORONTO^ON^M5B1W8||(416)555-5555|||||00270011851|2000010203AR\r" +
//                "PV1||I|4Q||00270011851||99994||||||||||||||||||||||||||||||||SMH\r" +
//                "ORC|RE|10676743|199|F1110030|||^^^20110111^^R||201101111536|DANGR||99994|4Q^B1\r" +
//                "OBR|1|10676743|199|FEPR^Iron Profile-FE,TIBC,SAT|||201101111536|||DANGR||||201101111536||99994||||TTGEN02||201101111536||BCHEM|F|||40900^40901||||DANGR\r" +
//                "NTE|1||This order was received without a proper requisition.\r" +
//                "NTE|2||This specimen was collecetd by syringe.\r" +
//                "OBX|1|NM|FE^Iron Total||42|umol/L|7-30|H|||F|||201101111536|B|DANGR\r" +
//                "OBX|2|NM|TIBC^TIBC||54|umol/L|42-72||||F|||201101111536|B|DANGR\r" +
//                "NTE|1||This result is invalid\r" +
//                "NTE|2||This specimen was collecetd incorrectly\r" +
//                "OBX|2|ST|TIBC^TIBC||54|umol/L|42-72||||F|||201101111536|B|DANGR\r" +
//                "OBX|2|ST|TIBC^TIBC||54:1|umol/L|42-72||||F|||201101111536|B|DANGR\r" +
//                "OBX|2|ST|TIBC^TIBC||Negative|umol/L|Negative||||F|||201101111536|B|DANGR\r" +
//                "OBX|1|TX|UCLU^Chloride/L||45|mmol/L|||||F|||201105091146|B|GAF\r" +
//                "OBX|1|TX|UCLU^Chloride/L||<45|mmol/L|||||F|||201105091146|B|GAF\r" +
//                "OBX|1|TX|UCLU^Chloride/L||trace|mmol/L|||||F|||201105091146|B|GAF\r" +
//                "OBX|3|NM|SAT^Saturation||0.78||0.20-0.50|H|||F|||201101111536|B|DANGR\r";

//                "MSH|^~\\&|LAB|SCC|OLIS|SMH|201105091148||ORU^R01|00838955|P|2.3.1||||||8859/1\r" +
//                "PID|||4001647||RHODODENDRON^PAUL^R||19581030|M|||2611-30 BOND ST^^TORONTO^ON^M5B1W8||(416)555-5555|||||00270011877|2000010179\r" +
//                "PV1||I|4Q||00270011877||99994||||||||||||||||||||||||||||||||SMH\r" +
//                "ORC|RE||482|F5090044|||^^^20110509^^R||201105091146|GAF||99994|4Q^B1\r" +
//                "OBR|1||482|24CL^Chloride 24h Urine|||201105091146|||AUTOV||||201105091146||99994||||||201105091146||BCHEM|F||^^^20110509^^R|||||GAF\r" +
//                "OBX|1|TX|UCLU^Chloride/L||45|mmol/L|||||F|||201105091146|B|GAF\r" +
//                "OBX|2|NM|24UCL^Chloride/d||72|mmol/d|110-250|L|||F|||201105091146|B|GAF\r" +
//                "ORC|RE||482|F5090044|||^^^20110509^^R||201105091146|GAF||99994|4Q^B1\r" +"
//                "OBR|2||482|UVOL^Volume|||201105091146|||AUTOV||||201105091146||99994||||||201105091146||BCHEM|F||^^^20110509^^R|||||GAF\r" +
//                "OBX|1|NM|UVOL^Volume||1.600|L|0.800-1.800||||F|||201105091146|B|GAF\r";
        //Haematology
        String haemMessageText =
                "MSH|^~\\&|LAB|SCC|OLIS|SMH|201105161459||ORU^R01|00842550|P|2.3.1||||||8859/1\r" +
                "PID|||4002039||T-PSSUITET^HARRIS^J||19401201|M|||2 POPE ROAD.^^MISSISSAUGA^ON^M5B1W8||(905)123-4567|||||00700002215|2222222222H\r" +
                "PV1||O|LIT||00700002215||90248||||||||||||||||||||||||||||||||SMH\r" +
                "ORC|RE||7|F5160025|||^^^20110516^^R||201105161457|GAF||90248|LIT^B1\r" +
                "OBR|1||7|CLOT^PT/INR/PTT|||201105161457|||GAF||||201105211457||90248||||N||201105161457||BHAEM|F||^^^20110516^^R|||||GAF\r" +
                "NTE|1||Patient on Coumadin and Heparin\r" +
                "OBX|1|NM|RPT^PT||13.0|s|10.0-13.0||||F|||201105161457|B|GAF\r" +
                "OBX|2|NM|RINR^INR||0.90||0.90-1.20||||F|||201105161457|B|GAF\r" +
                "OBX|3|NM|RPTT^APTT||23.0|s|24.0-37.0|L|||F|||201105161457|B|GAF\r" +
                "OBX|4|TX|COM^Comments|4|Specimen was collected by syringe||||||F|||201105161457\r";

//                "MSH|^~\\&|LAB|SCC||SMH|201012090810||ORU^R01|00771060|P|2.2\r" +
//                "PID|||J4001645||IRONWOOD^BIBIANA^V||19700510|F|||2301-30 BOND ST^^TORONTO^ON^M5B1W8||(416)555-5555|||||00270011851|2000010203\r" +
//                "PV1||I|4Q||00270011851||99994||||||||||||||||||||||||||||||||SMH\r" +
//                "ORC|RE|10671008|154|F0090000|||^^^20101209^^R||201012090800|CAJ||99994|4Q^B1\r" +
//                "OBR|1|10671008|154|CBC^CBC|||201012090801|||CAJ||||201012090801||99994||||||201012090808||BHAEM|F||^^^20101209^^R|||||CAJ\r" +
//                "OBX|1|NM|IWBCR^WBC||10.00|E9/L|4.00-11.00||||F|||201012090808|B|CAJ\r" +
//                "OBX|2|NM|RBC^RBC||4.50|E12/L|3.80-5.20||||F|||201012090808|B|CAJ\r" +
//                "OBX|3|NM|HGB^HGB||145|g/L|115-155||||F|||201012090808|B|CAJ\r" +
//                "OBX|4|NM|HCT^HCT||0.450|L/L|0.345-0.450||||F|||201012090808|B|CAJ\r" +
//                "OBX|5|NM|MCV^MCV||87.0|fL|82.0-97.0||||F|||201012090808|B|CAJ\r" +
//                "OBX|6|NM|MCH^MCH||31.0|pg|27.0-32.0||||F|||201012090808|B|CAJ\r" +
//                "OBX|7|NM|MCHC^MCHC||330|g/L|320-360||||F|||201012090808|B|CAJ\r" +
//                "OBX|8|NM|RDW^RDW||14.0|%|11.0-15.0||||F|||201012090808|B|CAJ\r" +
//                "OBX|9|NM|PLT^PLT||140|E9/L|140-400||||F|||201012090808|B|CAJ\r" +
//                "OBX|10|NM|MPV^MPV||11.0|fL|7.0-11.0||||F|||201012090808|B|CAJ\r" +
//                "OBX|11|NM|ANEUT^Abs.Neutrophils||8.000|E9/L|2.000-6.300|H|||F|||201012090808|B|CAJ\r" +
//                "OBX|12|NM|ALYMP^Abs.Lymphocytes||2.000|E9/L|1.000-3.200||||F|||201012090808|B|CAJ\r" +
//                "OBX|13|NM|AMONO^Abs.Monocytes||1.000|E9/L|0.200-0.800|H|||F|||201012090808|B|CAJ\r" +
//                "OBX|14|NM|AEOS^Abs.Eosinophils||0.005|E9/L|0.040-0.400|L|||F|||201012090808|B|CAJ\r" +
//                "OBX|15|NM|ABASO^Abs.Basophils||0.001|E9/L|0.000-0.100||||F|||201012090808|B|CAJ\r";

        String haemPanelMessageText =
                "MSH|^~\\&|LAB|SCC||SMH|201012161414||ORU^R01|00774235|P|2.2\r" +
                "PID|||J4001647||RHODODENDRON^PAUL^R||19581030|M|||2611-30 BOND ST^^TORONTO^ON^M5B1W8||(416)555-5555|||||00270011877|2000010179\r" +
                "PV1||I|4Q||00270011877||99994||||||||||||||||||||||||||||||||SMH\r" +
                "ORC|RE|10671496|119|F0160002|||^^^20101216^^R||201012161057|CAJ||99994|4Q^B1\r" +
                "OBR|1|10671496|119|DIFA^Diff and Smear|||201012161057|||CAJ||||201012161057||99994||||||201012161059||BHAEM|F||^^^20101216^^R|||||AUTOV\r" +
                "OBX|1|ST|SMUDG^Smudge Cells||Few||||||F|||201012161059|B|CAJ\r" +
                "OBX|2|ST|ROUL^Rouleaux||Moderate||||||F|||201012161059|B|CAJ\r" +
                "OBX|3|ST|SIC^Sickle Cells||Moderate||||||F|||201012161059|B|CAJ\r" +
                "OBX|4|ST|SPHER^Spherocytes||Few||||||F|||201012161059|B|CAJ\r" +
                "OBX|5|ST|STIP^Stippling||Slight||||||F|||201012161059|B|CAJ\r" +
                "OBX|6|ST|STOM^Stomatocytes||Few||||||F|||201012161059|B|CAJ\r" +
                "OBX|7|ST|TARG^Targets||Few||||||F|||201012161059|B|CAJ\r" +
                "OBX|8|ST|TEAR^Teardrops||Few||||||F|||201012161059|B|CAJ\r" +
                "OBX|9|ST|PLTCL^Platelet Clumps||Few||||||F|||201012161059|B|CAJ\r" +
                "OBX|10|ST|GPLT^Giant Platelets||Few||||||F|||201012161059|B|CAJ\r" +
                "OBX|11|ST|HYSEG^Hyperseg. Neut||Occ||||||F|||201012161059|B|CAJ\r" +
                "OBX|12|ST|TGRAN^Toxic granulation||Moderate||||||F|||201012161059|B|CAJ\r" +
                "OBX|13|ST|TVAC^Toxic vacuolation||Slight||||||F|||201012161059|B|CAJ\r" +
                "OBX|1|TX|REVW^Blood Film Interpretation|1|Query Acute Myeloid Leukemia||||||F|||201109121509\r" +
                "OBX|1|TX|MALB^Blood Film Interpretation|1|Query Acute Myeloid Leukemia||||||F|||201109121509\r" +
                "OBX|1|TX|MALS^Blood Film Interpretation|1|Query Acute Myeloid Leukemia||||||F|||201109121509\r" +
                "OBX|1|TX|MALV^Blood Film Interpretation|1|Query Acute Myeloid Leukemia||||||F|||201109121509\r" +
                "OBX|2|TX|REVW^Blood Film Interpretation|2|Reviewed by Dr. Smith||||||F|||201109121509\r" +
                "OBX|14|ST|DOBOD^Dohle bodies||Occ||||||F|||201012161059|B|CAJ\r";

//                "MSH|^~\\&|LAB|SCC|OLIS|SMH|201103250834||ORU^R01|00821674|P|2.3.1||||||8859/1\r" +
//                "PID|||J4001647||RHODODENDRON^PAUL^R||19581030|M|||2611-30 BOND ST^^TORONTO^ON^M5B1W8||(416)555-5555|||||00270011877|2000010179\r" +
//                "PV1||I|4Q||00270011877||99994||||||||||||||||||||||||||||||||SMH\r" +
//                "ORC|RE||364|F3250018|||^^^20110325^^R||201103250830|CAJ||99994|4Q^B1\r" +
//                "OBR|1||364|SICKG^SICK|||201103250830|||CAJ||||201103250830||99994||||||201103250830|||P||^^^20110325^^R|||||CAJ\r" +
//                "OBX|1|NM|ANEUT^Abs.Neutrophils||7.000|E9/L|2.000-6.300|H|||F|||201103250830|B|CAJ\r" +
//                "OBX|2|NM|ALYMP^Abs.Lymphocytes||3.000|E9/L|1.000-3.200||||F|||201103250830|B|CAJ\r" +
//                "OBX|3|NM|AMONO^Abs.Monocytes||1.000|E9/L|0.200-0.800|H|||F|||201103250830|B|CAJ\r" +
//                "OBX|4|NM|AEOS^Abs.Eosinophils||0.200|E9/L|0.040-0.400||||F|||201103250830|B|CAJ\r" +
//                "OBX|5|NM|ABASO^Abs.Basophils||0.100|E9/L|0.000-0.100||||F|||201103250830|B|CAJ\r" +
//                "OBX|6|TX|ADIFF^AutoDiff||Man.check||||||F|||201103250830|B|CAJ\r" +
//                "OBX|7|TX|ELLIP^Elliptocytes||Few||||||F|||201103250830|B|CAJ\r" +
//                "OBX|8|TX|FRAG^Fragments||Few||||||F|||201103250830|B|CAJ\r" +
//                "OBX|9|TX|HJB^Howell Jolly||Moderate||||||F|||201103250830|B|CAJ\r";


        //Blood Bank
        String bloodBankMessageText =
                "MSH|^~\\&|LAB|SCC|HL7|SMH|201010280922||ORU^R01|00753964|P|2.2\r" +
                "PID|||J4001527||T-PEPPERMINTPATTY^PRCTWENTY^T||19820816|F|||3210-30 BOND ST^^TORONTO^ON^M0M0M0||(416)555-555|||||00304010366\r" +
                "PV1|1|I|4Q^401Q^2||00270011885||99994||||||||||||InternalVisitID||||||||||||||||||||SMH|||||20100423|\r" +
                "ORC|NW|||E8260040|||^^^20101026^^R||201010261245|ALA||99994\r" +
                "OBR|1||E8260040|ABORH^ABO & Rh Group|||201010261244|||ALA||||201010261244||99994||||||201010280919||BB|F||^^^20101026^^R|||||ALA\r" +
                "OBX|1|TX|ABORH^ABO & Rh Group||LAB IDENTIFIER: E8260040||||||F\r" +
                "OBX|3|TX|ABSC^ABO & Rh Group||POSITIVE||||||F\r" +
                "OBX|4|TX|ABORH^ABO & Rh Group||||||||F\r" +
                "OBX|5|TX|ABORH^ABO & Rh Group||ABO & Rh Group      O POS                    OC/28/10 09:19||||||F|||201010261244\r";
//Pathoology
        String pathMessageText =
                //Non Synoptic Message

                //                "MSH|^~\\&|PATHLAB_LIS|ST MICHAELS HOSPITAL^1444^MOH|PIMS|CCO|20110321113800|18A1C1641255206DDD817E72450A7A2C661DCA3A|ORU^R01|616|D|2.5\r" +
                //                "PID|1||4001578^^^^CMR^St. Michaels Hospital&1444&MOH~4001578^^^^MRN^&1444&MOH~2222222222&GK&ON^^^^JHN||T-TESTPATIENTE^ROSE||19700810|F|||2204-30 BOND ST^^SCARBOROUGH^ON^M0M 0M0^2||(416)555-5555|||||||||||||||||N\r" +
                //                "PV1|1||16 NORTH SURGERY/GASTRO^M082-02||||21655^MCCLEARY^PAUL||||||||||21655^MCCLEARY^PAUL|I|00270011463^^^1444|1010\r" +
                //                "ORC|RE||S11-4^CoPathPlus||CM||||201101071540|STC^STREUTKER^CATHERINE||50534^MCCLEARY^PAUL\r" +
                //                "OBR|2||S11-4|11529-5^Surgical Pathology Study Report^LN^P^Pathology^L|||201101070000|||50534^MCCLEARY^PAUL||||201101071540|PAT&&Right hemicolectomy|50534^MCCLEARY^PAUL||||N||201103210943||SUR|F|||||||^Yip^Drake\r" +
                //                "OBX|1|FT|22633-2^Path report.site of origin^LN||Right hemicolectomy||||||F||1^1\r" +
                //                "OBX|2|FT|22634-0^Path report.gross description^LN||The specimen consists of a right hemicolectomy specimen measuring 50 cm in length.  A tumour is present in the cecum. ||||||F\r" +
                //                "OBX|3|FT|22636-5^Path report.relevant Hx^LN|| Clinical History goes here ||||||F\r" +
                //                "OBX|4|FT|22637-3^Path report.final diagnosis^LN|| COLON, HEMICOLECTOMY (RIGHT): - ADENOCARCINOMA OF COLON (SEE SYNOPTIC REPORT) ||||||F\r" +
                //                "OBX|5|FT|22638-1^Path report.comments^LN|| Comment if any goes here ||||||F\r" +
                //                "OBX|6|FT|22635-7^Path report.microscopic observation^LN|| Right hemicolectomy:         Specimen:                            Terminal ileum                                      Cecum                                      Ascending colon Procedure:                           Right hemicolectomy Specimen Length:                     50 cm Tumour Site:                         Cecum Tumour Size:                         Greatest dimension: 3.0 cm Macroscopic Tumour Perforation:      Not identified Macroscopic Intactness of Mesorectum:                                       Not applicable Histologic Type:                     Adenocarcinoma Histologic Grade:                    High-grade (poorly differentiated to undifferentiated) Intratumoral Lymphocytic Response:      Mild to moderate (0-2 per high-power [X400] field) Peritumour Lymphocytic Response:      Mild to moderate Tumour Subtype and Differentiation:      High histologic grade (poorly differentiated) Microscopic Tumour Extension:        Tumour invades through the muscularis propria into the subserosal adipose tissue or the nonperitonealized pericolic or perirectal soft tissues but does not extend to the serosal surface Margins:                             Proximal margin uninvolved by invasive carcinoma                                      Distal margin uninvolved by invasive carcinoma                                      Circumferential (Radial) or Mesenteric Margin not applicable                                      Distance of invasive carcinoma from closest margin: 3.0 cm Treatment Effect:                    No prior treatment Lymphatic (Small Vessel) Invasion (L):                                       Present Tumour Budding at Invasive Front:      Present, extensive Perineural Invasion:                 Not identified Tumour Deposits:                     Not identified Pathologic Staging (pTNM):       Primary Tumor (pT):                  pT3:  Tumour invades through the muscularis propria into the subserosa or the nonperitonealized pericolic or perirectal soft tissues Regional Lymph Nodes (pN):           pN1b: Metastasis in 2 to 3 regional lymph nodes                                      Number of nodes examined: 20                                      Number of nodes involved: 2 Distant Metastasis (pM):             Not applicable Ancillary Studies:                   MLH1 - Intact nuclear positivity, tumour cells                                      MSH2 - Loss of nuclear positivity, tumor cells                                      MSH6 - Intact nuclear positivity, tumour cells                                      PMS2 - Intact nuclear positivity, tumour cells --------------------------------------------------------    ||||||F\r";

                //Synoptic Message
                //                "MSH|^~\\&|PATHLAB_LIS|ST MICHAELS HOSPITAL^1444^MOH|PIMS|CCO|20110321113800|18A1C1641255206DDD817E72450A7A2C661DCA3A|ORU^R01|616|D|2.5\r" +
                //                "PID|1||4001578^^^^CMR^St. Michaels Hospital&1444&MOH~4001578^^^^MRN^&1444&MOH~2222222222&GK&ON^^^^JHN||T-TESTPATIENTE^ROSE||19700810|F|||2204-30 BOND ST^^SCARBOROUGH^ON^M0M 0M0^2||(416)555-5555|||||||||||||||||N\r" +
                //                "PV1|1||16 NORTH SURGERY/GASTRO^M082-02||||21655^MCCLEARY^PAUL||||||||||21655^MCCLEARY^PAUL|I|00270011463^^^1444|1010\r" +
                //                "ORC|RE||S11-4^CoPathPlus||CM||||201101071540|STC^STREUTKER^CATHERINE||50534^MCCLEARY^PAUL\r" +
                //                "OBR|1||S11-4|11529-5^Surgical Pathology Study Report^LN^P^Pathology^L|||201101070000|||21655^MCCLEARY^PAUL||||201101071540||21655^MCCLEARY^PAUL||||N||201103210943||SUR|F|||||||^Yip^Drake|68169^STREUTKER^CATHERINE\r" +
                //                "OBX|1|CWE|VERSION^Template Version Identifier^L||16.1000043:1.0^DY-CC10 COLON AND RECTUM: Resection, Including Transanal Disk Excision of Rectal Neoplasms -2010||||||F\r" +
                //                "OBX|2|CWE|1414.1000043^Colon-Ancillary Studies^CAPECC||15482.1000043^MLH1^CAPECC||||||F\r" +
                //                "OBX|3|CWE|15483.1000043^Colon-Ancillary Studies^CAPECC||15447.1000043^MLH1 Intact nuclear positivity, tumor cells^CAPECC||||||F\r" +
                //                "OBX|4|CWE|1414.1000043^Colon-Ancillary Studies^CAPECC||15411.1000043^MSH2^CAPECC||||||F\r" +
                //                "OBX|5|CWE|15412.1000043^Colon-Ancillary Studies^CAPECC||15452.1000043^MSH2 Loss of nuclear positivity, tumor cells^CAPECC||||||F\r" +
                //                "OBX|6|CWE|1414.1000043^Colon-Ancillary Studies^CAPECC||15456.1000043^MSH6^CAPECC||||||F\r" +
                //                "OBX|7|CWE|15457.1000043^Colon-Ancillary Studies^CAPECC||15458.1000043^MSH6 Intact nuclear positivity, tumor cells^CAPECC||||||F\r" +
                //                "OBX|8|CWE|1414.1000043^Colon-Ancillary Studies^CAPECC||15463.1000043^PMS2^CAPECC||||||F\r" +
                //                "OBX|9|CWE|15466.1000043^Colon-Ancillary Studies^CAPECC||15475.1000043^PMS2 Intact nuclear positivity, tumor cells^CAPECC||||||F\r" +
                //                "OBX|10|CWE|1499.1000043^Colon-Primary Tumor (pT)^CAPECC^384625004^pT category^SCT||15491.1000043^pN1b: Metastasis in 2 to 3 regional lymph nodes^CAPECC||||||F\r" +
                //                "OBX|11|CWE|1506.1000043^Colon-Primary Tumor (pT)^CAPECC^384625004^pT category^SCT||2332.1000043^Not applicable^CAPECC||||||F\r" +
                //                "OBX|12|CWE|15383.1000043^Colon-Intratumoral Lymphocytic Response (tumor-infiltrating lymphocytes)^CAPECC||15416.1000043^Mild to moderate^CAPECC||||||F\r" +
                //                "OBX|13|CWE|15497.1000043^Colon-Specimen^CAPECC||15498.1000043^Terminal ileum^CAPECC||||||F\r" +
                //                "OBX|14|CWE|15497.1000043^Colon-Specimen^CAPECC||15499.1000043^Cecum^CAPECC||||||F\r" +
                //                "OBX|15|CWE|15497.1000043^Colon-Specimen^CAPECC||15501.1000043^Ascending colon^CAPECC||||||F\r" +
                //                "OBX|16|CWE|15358.1000043^Colon-Macroscopic Tumor Perforation^CAPECC||15371.1000043^Not identified^CAPECC||||||F\r" +
                //                "OBX|17|CWE|15384.1000043^Colon-Peritumor Lymphocytic Response (Crohn-like response)^CAPECC||15477.1000043^Mild to moderate^CAPECC||||||F\r" +
                //                "OBX|18|CWE|15385.1000043^Colon-Tumor Subtype and Differentiation^CAPECC||15419.1000043^High histologic grade (poorly differentiated)^CAPECC||||||F\r" +
                //                "OBX|19|CWE|15386.1000043^Colon-Microscopic Tumor Extension^CAPECC||15424.1000043^Tumor invades through the muscularis propria into the subserosal adipose tissue or the nonperitoneal^CAPECC||||||F\r" +
                //                "OBX|20|CWE|15387.1000043^Colon-Treatment Effect^CAPECC||15405.1000043^No prior treatment^CAPECC||||||F\r" +
                //                "OBR|2||S11-4|11529-5^Surgical Pathology Study Report^LN^P^Pathology^L|||201101070000|||50534^MCCLEARY^PAUL||||201101071540|PAT&&Right hemicolectomy|50534^MCCLEARY^PAUL||||N||201103210943||SUR|F|||||||^Yip^Drake\r" +
                //                "OBX|21|CWE|15388.1000043^Colon-Tumor Deposits^CAPECC||15406.1000043^Tumor Deposits Not identified^CAPECC||||||F\r" +
                //                "OBX|22|CWE|1480.1000043^Colon-Histologic Grade^CAPECC^371469007^histologic grade^SCT||1484.1000043^High-grade (poorly differentiated to undifferentiated)^CAPECC^395530002^high grade (poorly differentiated to undifferentiated)^SCT||||||F\r" +
                //                "OBX|23|CWE|1416.1000043^Colon-Procedure^CAPECC||1425.1000043^Right hemicolectomy^CAPECC^122648004^specimen from colon obtained by right hemicolectomy^SCT||||||F\r" +
                //                "OBX|24|CWE|1487.1000043^Colon-Primary Tumor (pT)^CAPECC^384625004^pT category^SCT||15487.1000043^pT3: Tumor invades through the muscularis propria into the subserosa or the nonperitonealized perico^CAPECC^395707006^pT3: Tumor invades through the muscularis propria into the subserosa or into non-peritonealized pericolic or perirectal tissues (colon/rectum)^SCT||||||F\r" +
                //                "OBX|25|CWE|1544.1000043^Colon-Lymphatic (Small Vessel) Invasion^CAPECC||1546.1000043^Present^CAPECC^74139005^L1 stage^SCT||||||F\r" +
                //                "OBX|26|CWE|1558.1000043^Colon-Perineural Invasion^CAPECC^371513001^status of perineural invasion by tumor^SCT||1559.1000043^Perineural invasion absent^CAPECC^370051000^perineural invasion by tumor absent^SCT||||||F\r" +
                //                "OBX|27|CWE|1463.1000043^Colon-Histologic Type^CAPECC^371441004^histologic type^SCT||1464.1000043^Adenocarcinoma^CAPECC^35917007^adenocarcinoma^SCT||||||F\r" +
                //                "OBX|28|CWE|1452.1000043^Colon-Tumor Size Greatest Dimension^CAPECC^33728-7|1454|1454.1000043^Greatest dimension: ______ cm^CAPECC^371479009^tumor size, largest dimension^SCT|cm^centimeter^ISO+|||||F\r" +
                //                "OBX|29|CWE|1452.1000043^Colon-Tumor Size Greatest Dimension^CAPECC^33728-7|1454|^3.0^^371479009^tumor size, largest dimension^SCT|cm^centimeter^ISO+|||||F\r" +
                //                "OBX|30|CWE|1504.1000043^Colon-Number of Nodes Examined^CAPECC^21894-1||^20^^372309006^number of regional lymph nodes examined^SCT||||||F\r" +
                //                "OBX|31|CWE|1505.1000043^Colon-Number of Nodes Involved^CAPECC^21893-3||^2^^372308003^number of regional lymph nodes involved^SCT||||||F\r" +
                //                "OBX|32|CWE|1434.1000043^Colon-Specimen Length^CAPECC||^50^^384606002^length of specimen^SCT|cm^centimeter^ISO+|||||F\r" +
                //                "OBX|33|CWE|1513.1000043^Colon-Proximal Margin^CAPECC^33734-5||1514.1000043^Proximal margin uninvolved by invasive carcinoma^CAPECC^384614008^surgical proximal margin uninvolved by malignant neoplasm^SCT||||||F\r" +
                //                "OBX|34|CWE|1527.1000043^Colon-Circumferential Radial Margin^CAPECC^33736-0||1528.1000043^Circumferential (radial) margin - Not applicable^CAPECC^384619003^surgical circumferential margin involvement by tumor not applicable^SCT||||||F\r" +
                //                "OBX|35|CWE|1521.1000043^Colon-Distal Margin^CAPECC^33735-2||1522.1000043^Distal margin uninvolved by invasive carcinoma^CAPECC^384623006^surgical distal margin uninvolved by malignant neoplasm^SCT||||||F\r" +
                //                "OBX|36|CWE|1537.1000043^Colon-Distance to Closest Uninvolved Margin^CAPECC^33737-8||^3.0^^384891002^distance of malignant neoplasm from closest margin^SCT|cm^centimeter^ISO+|||||F\r" +
                //                "OBX|37|CWE|1457.1000043^Colon-Intactness of Mesorectum^CAPECC^408655002^status of intactness of mesorectal specimen^SCT||1458.1000043^Not applicable^CAPECC^408656001^intactness of mesorectal specimen not applicable^SCT||||||F\r" +
                //                "OBX|38|CWE|1435.1000043^Colon-Tumor Site^CAPECC^371480007^tumor site^SCT||1436.1000043^Cecum^CAPECC^32713005^cecum structure^SCT||||||F\r" +
                //                "OBR|2||S11-4|TEST11529-5^Surgical Pathology Study Report^LN^P^Pathology^L|||201101070000|||50534^MCCLEARY^PAUL||||201101071540|PAT&&Right hemicolectomy|50534^MCCLEARY^PAUL||||N||201103210943||SUR|F|||||||^Yip^Drake||&Bell&Valerie\r" +
                //                "OBX|1|FT|22633-2^Path report.site of origin^LN||Right hemicolectomy||||||F||1^1\r" +
                //                "OBX|2|FT|22634-0^Path report.gross description^LN||The specimen consists of a right hemicolectomy specimen measuring 50 cm in length.  A tumour is present in the cecum. ||||||F\r" +
                //                "OBX|3|FT|22636-5^Path report.relevant Hx^LN|| Clinical History goes here ||||||F\r" +
                //                "OBX|4|FT|22637-3^Path report.final diagnosis^LN|| COLON, HEMICOLECTOMY (RIGHT): - ADENOCARCINOMA OF COLON (SEE SYNOPTIC REPORT) ||||||F\r" +
                //                "OBX|5|FT|22638-1^Path report.comments^LN|| Comment if any goes here ||||||F\r" +
                //                "OBX|6|FT|22635-7^Path report.microscopic observation^LN|| Right hemicolectomy:         Specimen:                            Terminal ileum                                      Cecum                                      Ascending colon Procedure:                           Right hemicolectomy Specimen Length:                     50 cm Tumour Site:                         Cecum Tumour Size:                         Greatest dimension: 3.0 cm Macroscopic Tumour Perforation:      Not identified Macroscopic Intactness of Mesorectum:                                       Not applicable Histologic Type:                     Adenocarcinoma Histologic Grade:                    High-grade (poorly differentiated to undifferentiated) Intratumoral Lymphocytic Response:      Mild to moderate (0-2 per high-power [X400] field) Peritumour Lymphocytic Response:      Mild to moderate Tumour Subtype and Differentiation:      High histologic grade (poorly differentiated) Microscopic Tumour Extension:        Tumour invades through the muscularis propria into the subserosal adipose tissue or the nonperitonealized pericolic or perirectal soft tissues but does not extend to the serosal surface Margins:                             Proximal margin uninvolved by invasive carcinoma                                      Distal margin uninvolved by invasive carcinoma                                      Circumferential (Radial) or Mesenteric Margin not applicable                                      Distance of invasive carcinoma from closest margin: 3.0 cm Treatment Effect:                    No prior treatment Lymphatic (Small Vessel) Invasion (L):                                       Present Tumour Budding at Invasive Front:      Present, extensive Perineural Invasion:                 Not identified Tumour Deposits:                     Not identified Pathologic Staging (pTNM):       Primary Tumor (pT):                  pT3:  Tumour invades through the muscularis propria into the subserosa or the nonperitonealized pericolic or perirectal soft tissues Regional Lymph Nodes (pN):           pN1b: Metastasis in 2 to 3 regional lymph nodes                                      Number of nodes examined: 20                                      Number of nodes involved: 2 Distant Metastasis (pM):             Not applicable Ancillary Studies:                   MLH1 - Intact nuclear positivity, tumour cells                                      MSH2 - Loss of nuclear positivity, tumor cells                                      MSH6 - Intact nuclear positivity, tumour cells                                      PMS2 - Intact nuclear positivity, tumour cells --------------------------------------------------------    ||||||F\r";

                "MSH|^~\\&|PATHLAB_LIS|ST MICHAELS HOSPITAL^1444^MOH|PIMS|CCO|20110829114000|18A1C1641255206DDD817E72450A7A2C661DCA3A|ORU^R01|1177|D|2.5\r" +
                "PID|1||437563^^^^CMR^St. Michael's Hospital&amp;1444&amp;MOH~437563^^^^MRN^&amp;1444&amp;MOH||RAO^^A||19701229|F||||||||||||||||||||||N\r" +
//                "PID|1||4001645^^^^CMR^St. Michaels Hospital&1444&MOH~4001645^^^^MRN^&1444&MOH~2000010203&S&ON^^^^JHN||IRONWOOD^BIBIANA^V||19700510|F|||2301-30 BOND ST^^TORONTO^ON^M5B 1W8^220||(416)555-5555|||||||||||||||||N\r" +
                "PV1|1||4 QUEEN PALLIATIVE CARE UNIT^415Q-1|||||||||||||||I|00270011851^^^1444|1010\r" +
                "ORC|RE||C11-24^CoPathPlus||CM||||201108261444|YID^Yip^Drake||99994^BLAKE^DONALD|||||||||21|22||24\r" +
                "OBR|1||S11-4|11529-5^Surgical Pathology Study Report^LN^P^Pathology^L|||201101070000|||21655^MCCLEARY^PAUL||||201101071540||21655^MCCLEARY^PAUL||||N||201103210943||SUR|F|||||||^Yip^Drake|68169^STREUTKER^CATHERINE\r" +
                "OBR|2||S11-4|11529-5^Surgical Pathology Study Report^LN^P^Pathology^L|||201101070000|||21655^MCCLEARY^PAUL||||201101071540||21655^MCCLEARY^PAUL||||N||201103210943||SUR|F|||^BLAKE^DONALD~921379^BLAKE^DONALD||||^Yip^Drake|68169^STREUTKER^CATHERINE\r" +
                "OBX|1|CWE|VERSION^Template Version Identifier^L||16.1000043:1.0^DY-CC10 COLON AND RECTUM: Resection, Including Transanal Disk Excision of Rectal Neoplasms -2010||||||F\r" +
                "OBX|2|CWE|1414.1000043^Colon-Ancillary Studies^CAPECC||15482.1000043^MLH1^CAPECC||||||F\r" +
                "OBR|3||C11-24|11529-5^Study Report: Cytology.Cvx/Vag^LN^CG^Cytology (gyn)^L|||201108261444|||921379^BLAKE^DONALD||||201108261444|PAT&&Thin Prep - Pap Smear|921379^BLAKE^DONALD||||N||201108291140||GYN|F|||921379^BLAKE^DONALD||||12345^Yip^Drake||&Bell&Valerie\r" +
                "OBR|4||C11-24|33717-0^Study Report: Cytology.Cvx/Vag^LN^CG^Cytology (gyn)^L|||201108261444|||921379^BLAKE^DONALD||||201108261444|PAT&&Thin Prep - Pap Smear|921379^BLAKE^DONALD||||N||201108291140||GYN|F|||777777777^BLAKE^DONALD||||12345^Yipppp^Drake||&Bell&Valerie\r" +
                "OBX|1|FT|22633-2^Path report.site of origin^LN||Thin Prep - Pap Smear||||||F||1^A\r" +
                "OBX|2|FT|22637-3^Path report.final diagnosis^LN||SPECIMEN ADEQUACY Satisfactory for evaluation.  Transformation zone component not identified.  DIAGNOSIS Atypical squamous cells of undetermined significance (ASCUS). Fungal organisms morphologically consistent with Candida spp.    ||||||F\r" +
                "OBR|5||S11-4|11529-5^Surgical Pathology Study Report^LN^P^Pathology^L|||201101070000|||21655^MCCLEARY^PAUL||||201101071540||21655^MCCLEARY^PAUL||||N||201103210943||SUR|F|||||||^Yip^Drake|68169^STREUTKER^CATHERINE\r";



        String invalidMessageText =
                "MSH|^~\\&|LAB|SCC|OLIS|SMH|201108181413||ORU^R01|00871374|P|2.3.1||||||8859/1\r" +
                "PID|||4001649||HONEYSUCKLE^DANITA^W||19920120|F|||1916-30 BOND ST^^TORONTO^ON^M5B1W8||(416)555-5555|||||00270011893|2000010245AW\r" +
                "PV1||I|4Q||00270011893||99994||||||||||||||||||||||||||||||||SMH\r" +
                "ORC|RE|10698293|272|F8180018|||^^^20110818^^R||201108181335|DANGR||99994|4Q^B1\r" +
                "OBR|1|10698293|272|LYT4^Lytes|||201108181335|||DANGR||||201108181335||99994||||||201108181411||BCHEM|F|||||||DANGR\r" +
                "OBX|1|NM|NA^Sodium||136 |mmol/L|135-145||||W|||201108181411|B|DANGR\r" +
                "OBX|2|NM|K^Potassium||4.5 |mmol/L|3.5-5.0||||W|||201108181411|B|DANGR\r" +
                "OBX|3|NM|CL^Chloride||102 |mmol/L|96-106||||W|||201108181411|B|DANGR\r" +
                "OBX|4|NM|CO2^Total CO2||26 |mmol/L|22-30||||W|||201108181411|B|DANGR\r";



        ORU_R01 olisOru = new ORU_R01();
        hapi.smh.soft.oru.message.ORU_R01 smhOru = new hapi.smh.soft.oru.message.ORU_R01();
        smhOru.parse(pathMessageText);
        String laboratoryFacilityOID = "2.16.840.1.113883.3.59.1:4083";
        String hosptialOID = "2.16.840.1.113883.3.59.3:0852";
        String processingId = "T";
        String resultStatus = smhOru.getPATIENT_RESULT().getORDER_OBSERVATION(0).getOBR().getObr25_ResultStatus().getValue();
        String sendingApplication = "CN=LIS.MICHO2.PRODTEST, OU=Applications, OU=eHealthUsers, OU=Subscribers, DC=subscribers, DC=ssh";
        String universalServiceId = smhOru.getPATIENT_RESULT().getORDER_OBSERVATION(0).getOBR().getObr4_UniversalServiceIdentifier().getIdentifier().getValue();

        if (!"F".equals(resultStatus) && !"C".equals(resultStatus) && !"P".equals(resultStatus)) {
            //Filter method
            logger.info("Preliminary Result OBR-25 was \"" + resultStatus + "\"");
            return;
//        } else if (testRequestCode.contains(universalServiceId) == false) {
//            logger.info("Not a Top Order");
        } else if (new Map_ORU_SMH_Soft_ORUint_ON_OLIS().processMessage(null, smhOru, olisOru, laboratoryFacilityOID, hosptialOID, sendingApplication, processingId)) {
            String marshalledMessage = olisOru.encode();
            System.out.println(marshalledMessage.replaceAll("\\r", "\r\n"));
        } else {
            logger.info("+++ Not going to forward message as it should already have been forwarded");
        }

    }
    @ConsumerEndpoint(serviceQN = "{http://j2ee.netbeans.org/wsdl/prj_Map_ORUint_ON_OLIS_ORU_ON_OLIS_Pojo/newpojobinding}epSOAP_Map_ORUint_ON_OLIS_ORU_ON_OLIService", interfaceQN = "{http://j2ee.netbeans.org/wsdl/prj_Map_ORUint_ON_OLIS_ORU_ON_OLIS_Pojo/newpojobinding}SOAP_Map_ORUint_ON_OLIS_ORU_ON_OLIS", name = "epSOAP_Map_ORUint_ON_OLIS_ORU_ON_OLI", operationQN = "{http://j2ee.netbeans.org/wsdl/prj_Map_ORUint_ON_OLIS_ORU_ON_OLIS_Pojo/newpojobinding}map", inMessageTypeQN = "{http://j2ee.netbeans.org/wsdl/prj_Map_ORUint_ON_OLIS_ORU_ON_OLIS_Pojo/newpojobinding}mapRequest")
    private Consumer sepSOAP_Map_ORUint_ON_OLIS_ORU_ON_OLIS;
}
