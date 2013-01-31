/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sail.map;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Group;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.model.Structure;
import ca.uhn.hl7v2.model.v251.datatype.XAD;
import ca.uhn.hl7v2.parser.EncodingCharacters;
import ca.uhn.hl7v2.parser.Escape;
import ca.uhn.sail.integration.AbstractPojoMapping;
import ca.uhn.sail.integration.CanonicalMessageUtils;
import ca.uhn.sail.integration.SailInfrastructureServicesFactory;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.Source;
import org.glassfish.openesb.pojose.api.Consumer;
import org.glassfish.openesb.pojose.api.annotation.ConsumerEndpoint;
import hapi.on.olis.oru_r01.group.ORU_R01_PATIENT_RESULT;
import hapi.on.olis.oru_r01.message.ORU_R01;
import hapi.on.olis.oru_r01.segment.PID;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;
import org.hl7.cts.types.TranslateCodeSimpleRequest;
import org.hl7.cts.types.TranslateCodeSimpleResponse;
import sail.xsd.canonical.hl7v2.CanonicalHl7V2Message;
import sail.xsd.infrastructure.services.hcv.HCVRequest;
import sail.xsd.infrastructure.services.hcv.HCVResponse;
import sail.xsd.infrastructure.services.location.Id;
import sail.xsd.infrastructure.services.location.LookupFacilityResponse;
import sail.xsd.infrastructure.services.providerregistry.GetCanonicalProviderMappingsResponse;
import sail.xsd.infrastructure.services.providerregistry.GetProviderResponse;
import sail.xsd.infrastructure.services.providerregistry.ProviderLookupMapTo;
import org.glassfish.openesb.pojose.api.annotation.Provider;
import org.glassfish.openesb.pojose.api.annotation.Resource;
import org.glassfish.openesb.pojose.api.annotation.Operation;
import org.glassfish.openesb.pojose.api.res.Context;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.hl7.cts.types.CD;
import sail.xsd.canonical.hl7v2.IntermediateMessageEntry;
import sail.xsd.canonical.hl7v2.MessageErrored;
import sail.xsd.canonical.hl7v2.OutboundInterface;
import sail.xsd.infrastructure.services.location.Facility;
import sail.xsd.infrastructure.services.location.LookupFacilityRequest;
import sail.xsd.infrastructure.services.providerregistry.GetCanonicalProviderMappingsRequest;
import sail.xsd.infrastructure.services.providerregistry.GetProviderRequest;
import static org.apache.commons.lang.StringUtils.*;

/**
 *
 * @author t3903uhn
 *
 * Update History:
 * 1 - 2011-12-15 - James Agnew - Send message to dead letter queue if PID-3 has a type of JHN but there is no actual ID or ordering provider ID
 * 2 - 2012-05-16 - Diederik Muylwyk - HCV testing is disabled for Ultra RefIn results
 * 3 - 2012-05-25 - Diederik Muylwyk - Fixed "HCV testing is disabled for Ultra RefIn results" to remove ORC-13 flag for non-JHN identifiers
 * 4 - 2012-06-19 - Diederik Muylwyk - HCV testing is disabled for non-Ontario health cards; identifier preserved
 * 5 - 2012-06-25 - Marco Pagura - added a new method of PatientNote.  New NTE-ZNT aftert the ZPD segment.
 * 6 - 2012-07-27 - Diederik Muylwyk - Added the synchronized keyword to declaration for method getUTC(...) to make it thread-safe
 *
 * Development Deployment History
 * 2012-03-29 - Diederik Muylwyk - Previously deployed to DEV
 * 2012-05-16 - Diederik Muylwyk - Redeployed to DEV
 * 2012-05-25 - Diederik Muylwyk - Redeployed to DEV
 * 2012-06-19 - Diederik Muylwyk - Redeployed to DEV
 * 2012-06-25 - Marco Pagura - Redeployed to DEV
 * 2012-07-27 - Diederik Muylwyk - Redeployed to DEV
 *
 * Production Deployment History
 * 2011-03-29 - Diederik Muylwyk - Deployed to PROD
 * 2012-05-16 - Diederik Muylwyk - Redeployed to PROD
 * 2012-05-25 - Diederik Muylwyk - Redeployed to PROD
 * 2012-10-02 - Diederik Muylwyk - Redeployed to PROD
 */
@Provider(name = "newpojobinding", interfaceQN = "{http://j2ee.netbeans.org/wsdl/prj_Map_ORUint_ON_OLIS_ORU_ON_OLIS_Pojo/newpojobinding}SOAP_Map_ORUint_ON_OLIS_ORU_ON_OLIS", serviceQN = "{http://j2ee.netbeans.org/wsdl/prj_Map_ORUint_ON_OLIS_ORU_ON_OLIS_Pojo/newpojobinding}SOAP_Map_ORUint_ON_OLIS_ORU_ON_OLISService")
public class SOAP_Map_ORUint_ON_OLIS_ORU_ON_OLIS extends AbstractPojoMapping {

    public static final String SUCCESS_RESPONSE = "<SuccessResponse xmlns=\"http://j2ee.netbeans.org/wsdl/prj_Map_ORUint_ON_OLIS_ORU_ON_OLIS_Pojo/Map_ORUint_ON_OLIS_ORU_ON_OLIS\"/>";
    private static final Logger logger = Logger.getLogger(SOAP_Map_ORUint_ON_OLIS_ORU_ON_OLIS.class.getName());
    private static final String JHN = "JHN";
    private static final String MR = "MR";
    private static Set<String> myValidPayors = new HashSet<String>();
    private static Map<String, String> myProviderClassifierToIdRoot = new HashMap<String, String>();
    private static Map<String, String> myIdRootToProviderClassifier = new HashMap<String, String>();
    private static Map<String, String> myIdAuthorityToProviderClassifier = new HashMap<String, String>();
    private static Map<String, String> myHcvResponseCodeToDescription = new HashMap<String, String>();
    private static SimpleDateFormat mySimpleDateFormat8;    // use only when synchronized
    private static SimpleDateFormat mySimpleDateFormat12;   // use only when synchronized
    private static SimpleDateFormat mySimpleDateFormat14;   // use only when synchronized
    private static SimpleDateFormat mySimpleDateFormat19;   // use only when synchronized
    private static final String HCV_RESPONSE_CODE_FOR_PAHN;
    private static final Set<String> myHcvCodesToPass = new HashSet<String>();

    static {
        // Set up payors
        myValidPayors.add("WSIB");
        myValidPayors.add("MOHLTC");
        myValidPayors.add("SELF");
        myValidPayors.add("3RDPARTY");

        // Set up provider classifier/OID mappings
        myProviderClassifierToIdRoot.put("MDL", "1.3.6.1.4.1.12201.1.2.1.1");
        myProviderClassifierToIdRoot.put("DDSL", "1.3.6.1.4.1.12201.1.2.1.2");
        myProviderClassifierToIdRoot.put("NPL", "1.3.6.1.4.1.12201.1.2.1.3");
        myProviderClassifierToIdRoot.put("ML", "1.3.6.1.4.1.12201.1.2.1.4");
        myProviderClassifierToIdRoot.put("EI", "1.3.6.1.4.1.12201.1.2.1.5");

        myIdRootToProviderClassifier.put("1.3.6.1.4.1.12201.1.2.1.1", "MDL");
        myIdRootToProviderClassifier.put("1.3.6.1.4.1.12201.1.2.1.2", "DDSL");
        myIdRootToProviderClassifier.put("1.3.6.1.4.1.12201.1.2.1.3", "NPL");
        myIdRootToProviderClassifier.put("1.3.6.1.4.1.12201.1.2.1.4", "ML");
        myIdRootToProviderClassifier.put("1.3.6.1.4.1.12201.1.2.1.5", "EI");

        myIdAuthorityToProviderClassifier.put("AB", "Alberta");
        myIdAuthorityToProviderClassifier.put("BC", "British Columbia");
        myIdAuthorityToProviderClassifier.put("MB", "Manitoba");
        myIdAuthorityToProviderClassifier.put("NB", "New Brunswick");
        myIdAuthorityToProviderClassifier.put("NL", "Newfoundland and Labrador");
        myIdAuthorityToProviderClassifier.put("NT", "Northwest Territories");
        myIdAuthorityToProviderClassifier.put("NS", "Nova Scotia");
        myIdAuthorityToProviderClassifier.put("NU", "Nunavut");
        myIdAuthorityToProviderClassifier.put("ON", "Ontario");
        myIdAuthorityToProviderClassifier.put("PE", "Prince Edward Island");
        myIdAuthorityToProviderClassifier.put("QC", "Quebec");
        myIdAuthorityToProviderClassifier.put("SK", "Saskatchwan");
        myIdAuthorityToProviderClassifier.put("YT", "Yukon");

//        myProviderIdRootToClassifier.put("MDL", "1.3.6.1.4.1.12201.1.2.1.1");
//        myProviderIdRootToClassifier.put("DDSL", "1.3.6.1.4.1.12201.1.2.1.2");
//        myProviderIdRootToClassifier.put("NPL", "1.3.6.1.4.1.12201.1.2.1.3");
//        myProviderIdRootToClassifier.put("ML", "1.3.6.1.4.1.12201.1.2.1.4");
//        myProviderIdRootToClassifier.put("EI", "1.3.6.1.4.1.12201.1.2.1.5");

        mySimpleDateFormat8 = new SimpleDateFormat("yyyyMMdd");
        mySimpleDateFormat12 = new SimpleDateFormat("yyyyMMddHHmm");
        mySimpleDateFormat14 = new SimpleDateFormat("yyyyMMddHHmmss");
        mySimpleDateFormat19 = new SimpleDateFormat("yyyyMMddHHmmssZ");
        TimeZone est = TimeZone.getTimeZone("America/New_York");
        GregorianCalendar gc = new GregorianCalendar(est);
        mySimpleDateFormat8.setCalendar(gc);
        mySimpleDateFormat12.setCalendar(gc);
        mySimpleDateFormat14.setCalendar(gc);
        mySimpleDateFormat19.setCalendar(gc);

        myHcvResponseCodeToDescription.put("5", "Incorrect Health Number Format.");
        myHcvResponseCodeToDescription.put("10", "Health Number Does Not Exist.");
        myHcvResponseCodeToDescription.put("15", "Pre-Assigned Health Number For Newborn.");
        myHcvResponseCodeToDescription.put("20", "Eligibility Does Not Exist For This Health Number.");
        myHcvResponseCodeToDescription.put("25", "Unknown Card/Invalid Stripe.");
        myHcvResponseCodeToDescription.put("50", "Card Passed Validation.");
        myHcvResponseCodeToDescription.put("51", "Card Passed Validation.");
        myHcvResponseCodeToDescription.put("52", "Card Passed Validation. Card Holder Must Contact Ministry To Maintain Coverage.");
        myHcvResponseCodeToDescription.put("53", "Card Passed Validation- Card Is Expired. Card Holder Must Contact Ministry to Maintain Coverage.");
        myHcvResponseCodeToDescription.put("54", "Card Passed Validation. Card Is Future Dated.");
        myHcvResponseCodeToDescription.put("55", "Card Passed Validation. No Valid Address For Card Holder.");
        myHcvResponseCodeToDescription.put("60", "Expired Card. This Card No Longer Valid.");
        myHcvResponseCodeToDescription.put("65", "Incorrect Version Code. Card Holder Is Eligible But Card Is Not Valid.");
        myHcvResponseCodeToDescription.put("70", "Stolen Card.  Card Has Been Reported Stolen.");
        myHcvResponseCodeToDescription.put("75", "Cancelled or Voided Card.");
        myHcvResponseCodeToDescription.put("80", "Damaged Card. Replacement Has Been Issued.");
        myHcvResponseCodeToDescription.put("83", "Lost Card. Card Has Been Reported Lost.");

        HCV_RESPONSE_CODE_FOR_PAHN = "15";

        myHcvCodesToPass.add("20");
        myHcvCodesToPass.add("50");
        myHcvCodesToPass.add("51");
        myHcvCodesToPass.add("52");
        myHcvCodesToPass.add("53");
        myHcvCodesToPass.add("54");
        myHcvCodesToPass.add("55");
        myHcvCodesToPass.add("60");
        myHcvCodesToPass.add("65");
        myHcvCodesToPass.add("75");

        // MSH specific HCV codes indicating that the ministry connection is down
        myHcvCodesToPass.add("90");
        myHcvCodesToPass.add("99");
    }

    /**
     * Constructor
     */
    public SOAP_Map_ORUint_ON_OLIS_ORU_ON_OLIS() {
    }

    /**
     * POJO Operation
     *
     * @param input input of type String input
     * @return String
     */
    @Operation(outMessageTypeQN = "{http://j2ee.netbeans.org/wsdl/prj_Map_ORUint_ON_OLIS_ORU_ON_OLIS_Pojo/newpojobinding}mapResponse")
    public String map(NormalizedMessage theNormalizedMessage) throws Exception {
        CanonicalHl7V2Message input = unmarshallCanonicalHl7V2Message(theNormalizedMessage);

        if (input.getDestination() == null) {
            input.setDestination(new OutboundInterface());
        }
        input.getDestination().setOrgId("ON");
        input.getDestination().setSystemId("OLIS");
        input.getDestination().setInterfaceId("ORU");
        input.getDestination().setInterfaceDirection("O");
        input.getDestination().setBoxId((SailInfrastructureServicesFactory.getEngineBoxId()));
        input.getDestination().setDomainId((SailInfrastructureServicesFactory.getEngineDomainId()));

        // Fix up any CR issues
        String rawMessage = input.getRawMessage();
        rawMessage = rawMessage.replaceAll("\\r?\\n", "\r");
        input.setRawMessage(rawMessage);

        input.getIntermediateMessage().add(new IntermediateMessageEntry());
        input.getIntermediateMessage().get(0).setDestinationComponentName(getClass().getName());
        input.getIntermediateMessage().get(0).setRawIntermediateMessage(rawMessage);

        if (skipMessage(input)) {
            logger.info("+++ Skipping message because it is marked for skipping");
            forward(input);
            return SUCCESS_RESPONSE;
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("+++ Message incoming is " + input.getRawMessage());
        }

        processMessage(input);

        return SUCCESS_RESPONSE;
    }


	// POJO Context
    @Resource
    private Context jbiCtx;

    public Facility lookupFacility(String theNamespace, String theId) throws Exception {

        if (logger.isLoggable(Level.INFO)) {
            logger.info("+++Looking up facility: " + theNamespace + "/" + theId);
        }

        String[] idParts = theId.split("\\:");

        LookupFacilityRequest request = new LookupFacilityRequest();
        request.setId(new Id());
        request.getId().setRoot(idParts[0]);
        request.getId().setAuthority(theNamespace);
        request.getId().setExt(idParts[1]);

        boolean map = false;
        if (!"ON_OLIS".equals(theNamespace)) {
            request.setMapToAuthority("ON_OLIS");
            map = true;
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Looking up and mapping facility: " + theId);
            }
        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Looking up facility: " + theId);
            }
        }

        LookupFacilityResponse response = getLocationRegistryService().lookupFacility(request);

        Facility retVal;
        if (map) {
            retVal = response.getMappedFacility();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Mapped facility was: " + retVal.getId().getRoot() + "/" + retVal.getId().getAuthority() + "/" + retVal.getId().getExt());
            }
        } else {
            retVal = response.getFacility();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Facility was: " + retVal.getId().getRoot() + "/" + retVal.getId().getAuthority() + "/" + retVal.getId().getExt());
            }
        }
        return retVal;

    }

	public GetProviderResponse lookupProvider(String theClassifier, String theAuthority, String theExtension, String theMapToClassifier)
            throws Exception {
        String root = (String) myProviderClassifierToIdRoot.get(theClassifier);
        if (root == null) {
            throw new Exception("Unknown ID classifier: " + theClassifier);
        }

        String mapToRoot = null;
        GetProviderRequest providerLookup = new GetProviderRequest();

        String message;
        GetCanonicalProviderMappingsResponse response;
        if (theMapToClassifier != null && !theClassifier.equals(theMapToClassifier)) {
            mapToRoot = (String) myProviderClassifierToIdRoot.get(theMapToClassifier);
            if (mapToRoot == null) {
                throw new Exception("Unknown ID classifier for map-to: " + theClassifier);
            }

            if (mapToRoot.equals(root)) {
                message = "Looking up provider " + root + "/" + theAuthority + "/" + theExtension;
                logger.fine(message);
                providerLookup.setFindIdRoot(root);
                providerLookup.setFindIdAuthority(theAuthority);
                providerLookup.setFindIdExt(theExtension);
            } else {
                message = "Looking up provider " + root + "/" + theAuthority + "/" + theExtension + " and mapping to " + mapToRoot + "/ON";
                logger.fine(message);
                providerLookup.setFindIdRoot(root);
                providerLookup.setFindIdAuthority(theAuthority);
                providerLookup.setFindIdExt(theExtension);
                providerLookup.setMapTo(new ProviderLookupMapTo());
                providerLookup.getMapTo().setIdRoot(mapToRoot);
                providerLookup.getMapTo().setIdAuthority("ON");

                GetCanonicalProviderMappingsRequest request = new GetCanonicalProviderMappingsRequest();
                request.setIdAuthority(theAuthority);
                request.setIdExt(theExtension);
                response = getProviderRegistryService().getCanonicalProviderMappings(request);

                GetProviderResponse getresponse = new GetProviderResponse();
                if (response.getTargetProvider().size() > 0) {
                    getresponse.setFoundMapping(true);
                    getresponse.setProviderIfFound(new sail.xsd.infrastructure.services.providerregistry.Provider());
                    getresponse.getProviderIfFound().setFirstName(response.getTargetProvider().get(0).getFirstName());
                    getresponse.getProviderIfFound().setLastName(response.getTargetProvider().get(0).getLastName());
                    getresponse.getProviderIfFound().setMiddleName(response.getTargetProvider().get(0).getMiddleName());
                    getresponse.getProviderIfFound().setFirstName(response.getTargetProvider().get(0).getFirstName());
                    getresponse.getProviderIfFound().setIdAuthority(response.getTargetProvider().get(0).getIdAuthority());
                    getresponse.getProviderIfFound().setIdExtension(response.getTargetProvider().get(0).getIdExtension());
                    getresponse.getProviderIfFound().setIdRoot(response.getTargetProvider().get(0).getIdRoot());

                }
                return getresponse;
            }

        } else {

            message = "Looking up provider " + root + "/" + theAuthority + "/" + theExtension;
            logger.fine(message);
            providerLookup.setFindIdRoot(root);
            providerLookup.setFindIdAuthority(theAuthority);
            providerLookup.setFindIdExt(theExtension);

        }

        GetProviderResponse retVal = getProviderRegistryService().getProvider(providerLookup);

        if (retVal.getProviderIfFound() != null) {
            logger.fine("+++ Looked up and found provider " + retVal.getProviderIfFound().getIdExtension());
        } else {
            logger.info("+++ No provider found - Request was " + message);
        }

        return retVal;

    }

	public CD lookupTerminologySingle(String theVocabularyDomain, String theSourceCode, String theSourceCodeSystem, String theTargetCodeSystem)
            throws Exception {

        List<CD> retVal = lookupTerminologyMultiple(theVocabularyDomain, theSourceCode, theSourceCodeSystem, theTargetCodeSystem, true);

        if (retVal.size() > 1) {
            throw new Exception("More than one mapping from [" + theSourceCode + "/" + theSourceCodeSystem + "] to [" + theTargetCodeSystem + "]");
        }

        if (retVal.isEmpty()) {
            return null;
        }

        return retVal.get(0);
    }

    public List<CD> lookupTerminologyMultiple(String theVocabularyDomain, String theSourceCode, String theSourceCodeSystem, String theTargetCodeSystem)
            throws Exception {
        return lookupTerminologyMultiple(theVocabularyDomain, theSourceCode, theSourceCodeSystem, theTargetCodeSystem, false);
    }

    public List<CD> lookupTerminologyMultiple(String theVocabularyDomain, String theSourceCode, String theSourceCodeSystem, String theTargetCodeSystem, boolean theAllowUnknownCodes)
            throws Exception {

        // Map the code
        TranslateCodeSimpleRequest vcr = new TranslateCodeSimpleRequest();
        vcr.setCodeId(theSourceCode);
        vcr.setCodeSystemId(theSourceCodeSystem);
        vcr.setToCodeSystemId(theTargetCodeSystem);
        TranslateCodeSimpleResponse translatedCode = getTerminologyService().translateCodeSimple(vcr);

        // If we are allowing for unknown codes to be passed in for mapping, then validate the code first
        if (!theAllowUnknownCodes && translatedCode.getError() != null && translatedCode.getError().isUnknownCode() == true) {
            throw new Exception("Unknown code: " + theSourceCode + "/" + theSourceCodeSystem);
        }

        List<CD> retVal = translatedCode.getCode();
        return retVal;

    }

    public boolean breakOutPanels(CanonicalHl7V2Message theInput, hapi.on.olis.oru_r01.message.ORU_R01 message)
            throws Exception {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("+++ Message before panel breakout: " + message.encode().replace("\r", "\r\n"));
        }

        boolean foundPanels = false;
        for (int i2 = 0; i2 < message.getPATIENT_RESULT().getORDER_OBSERVATIONReps(); i2++) {
            message.getPATIENT_RESULT().getORDER_OBSERVATION(i2).getOBR().getObr1_SetIDOBR().setValue(Integer.toString(i2 + 1));
            // For each OBR (ORDER_OBSERVATION group), check if w ehave a panel definition
            String universalServiceIdentifier = message.getPATIENT_RESULT().getORDER_OBSERVATION(i2).getOBR().getObr4_UniversalServiceIdentifier().getIdentifier().getValue();
            String codeSystem = message.getPATIENT_RESULT().getORDER_OBSERVATION(i2).getOBR().getObr4_UniversalServiceIdentifier().getNameOfCodingSystem().getValue();

            if (isEmpty(universalServiceIdentifier)) {
                sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.OBR-4-1", "No Universal Service Itendifier - Code");
                return false;
            }
            if (isEmpty(codeSystem)) {
                sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.OBR-4-3", "No Universal Service Itendifier - Coding System");
                return false;
            }

            List<CD> mappings = lookupTerminologyMultiple("OlisPanels", universalServiceIdentifier, codeSystem + ".100", codeSystem + ".101", true);

            if (mappings.isEmpty()) {
                // Not a panel
                continue;
            }

            foundPanels = true;
            logger.info("+++Group is a panel with " + mappings.size() + " children");

            hapi.on.olis.oru_r01.group.ORU_R01_ORDER_OBSERVATION removedOrderObservation = message.getPATIENT_RESULT().removeORDER_OBSERVATION(i2);
            HashSet<String> potentialMappings = new HashSet<String>();
            for (int childIndex = 0; childIndex < mappings.size(); childIndex++) {

                String nextChildUsi = mappings.get(childIndex).getCode();

                List<CD> nextChildObservationIdentifiers = lookupTerminologyMultiple("OlisPanels", nextChildUsi, codeSystem + ".101", codeSystem + ".102", true);

                for (CD next : nextChildObservationIdentifiers) {
                    potentialMappings.add(next.getCode());
                }

                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("PotentialMappings " + potentialMappings);
                }

            }


            for (int mappingIndex = 0, childIndex = 0; mappingIndex < mappings.size(); mappingIndex++, childIndex++) {
                String nextChildUsi = mappings.get(mappingIndex).getCode();
                String nextChildUsiName = mappings.get(mappingIndex).getDisplayName();

                List<CD> nextChildObservationIdentifiers = lookupTerminologyMultiple("OlisPanels", nextChildUsi, codeSystem + ".101", codeSystem + ".102", true);


                boolean found = false;
                for (CD next : nextChildObservationIdentifiers) {
                    String nextChildChildUsi = next.getCode();

                    for (int observationIndex = 0; observationIndex < removedOrderObservation.getOBSERVATIONReps(); observationIndex++) {
                        String observationIdentifier = removedOrderObservation.getOBSERVATION(observationIndex).getOBX().getObx3_ObservationIdentifier().getIdentifier().getValue();

                        if (!potentialMappings.contains(observationIdentifier)) {

                            System.out.println(removedOrderObservation.getOBSERVATION(observationIndex).getOBX().encode());

                            sendToDeadLetterAndForward(theInput, "INCORRECT_MESSASGE.V2.OBR-4-1", "Test Request Mapping Does not Exist: " + observationIdentifier + " for Panel: " + universalServiceIdentifier);
                            return false;
                        }
                        if (nextChildChildUsi.equals(observationIdentifier)) {
                            found = true;
                            break;
                        }

                    }
                }
                if (found == false) {
                    childIndex--;
                    continue;
                }


                // Build new ORC
                hapi.on.olis.oru_r01.group.ORU_R01_ORDER_OBSERVATION newOrderObservation = message.getPATIENT_RESULT().insertORDER_OBSERVATION(i2 + childIndex);
                hapi.on.olis.oru_r01.segment.ORC newOrc = newOrderObservation.getORC();
                hapi.on.olis.oru_r01.segment.ORC oldOrc = removedOrderObservation.getORC();
                newOrc.parse(oldOrc.encode());


                // Build new OBR
                //PlacerOrderNumber and FillerOrderNumber must be incremented to pass in OLIS.
                hapi.on.olis.oru_r01.segment.OBR newObr = newOrderObservation.getOBR();
                hapi.on.olis.oru_r01.segment.OBR oldObr = removedOrderObservation.getOBR();
                newObr.getSetIDOBR().setValue(Integer.toString(mappingIndex + 1));
                newObr.getObr2_PlacerOrderNumber().parse(oldObr.getObr2_PlacerOrderNumber().encode());
                newObr.getObr2_PlacerOrderNumber().getEntityIdentifier().setValue(oldObr.getObr2_PlacerOrderNumber().getEntityIdentifier().getValue() + "-" + Integer.toString(mappingIndex + 1));
                newObr.getObr3_FillerOrderNumber().parse(oldObr.getObr3_FillerOrderNumber().encode());
                newObr.getObr3_FillerOrderNumber().getEntityIdentifier().setValue(oldObr.getObr3_FillerOrderNumber().getEntityIdentifier().getValue() + "-" + Integer.toString(mappingIndex + 1));
                newObr.getObr4_UniversalServiceIdentifier().getIdentifier().setValue(nextChildUsi);
                newObr.getObr4_UniversalServiceIdentifier().getText().setValue(nextChildUsiName);
                newObr.getObr4_UniversalServiceIdentifier().getNameOfCodingSystem().setValue(oldObr.getObr4_UniversalServiceIdentifier().getNameOfCodingSystem().getValue());
                newObr.getObr7_ObservationDateTime().parse(oldObr.getObr7_ObservationDateTime().encode());
                newObr.getObr14_SpecimenReceivedDateTime().parse(oldObr.getObr14_SpecimenReceivedDateTime().encode());
                newObr.getObr16_OrderingProvider().parse(oldObr.getObr16_OrderingProvider().encode());
                newObr.getObr20_PerformingLabUserReadableSpecimenIdentifier().parse(oldObr.getObr20_PerformingLabUserReadableSpecimenIdentifier().encode());
                newObr.getObr25_ResultStatus().parse(oldObr.getObr25_ResultStatus().encode());
                newObr.getObr27_QuantityTiming().parse(oldObr.getObr27_QuantityTiming().encode());
                for (int physicianIndex = 0; physicianIndex < oldObr.getObr28_ResultCopiesToReps(); physicianIndex++) {
                    newObr.getObr28_ResultCopiesTo(physicianIndex).parse(oldObr.getObr28_ResultCopiesTo(physicianIndex).encode());
                }
                // Copy ZBR Segment
                hapi.on.olis.oru_r01.segment.ZBR newZbr = newOrderObservation.getZBR();
                hapi.on.olis.oru_r01.segment.ZBR oldZbr = removedOrderObservation.getZBR();
                newZbr.parse(oldZbr.encode());
                String sortKey = Integer.toString(mappingIndex + 1);
                sortKey = StringUtils.leftPad(sortKey, 4, "0");

                //Copy NTE Order Segment


                hapi.on.olis.oru_r01.segment.NTE oldNte = removedOrderObservation.getORDER_NOTE(0).getNTE();
                String nteComment = oldNte.getNte3_Comment(0).getValue();
                logger.info("NTEComment: " + nteComment);
                if (!isEmpty(nteComment)) {
                    hapi.on.olis.oru_r01.segment.NTE newNte = newOrderObservation.getORDER_NOTE(0).getNTE();
                    newNte.getNte1_SetIDNTE().setValue("1");
                    newNte.getNte2_SourceOfComment().setValue("L");
                    newNte.getNte3_Comment(0).setValue(oldNte.getNte3_Comment(0).getValue());
                    newNte.getNte4_CommentType().getIdentifier().setValue("RE");
                    newNte.getNte4_CommentType().getText().setValue("Remark");
                    newNte.getNte4_CommentType().getNameOfCodingSystem().setValue("HL70364");

                    // ZNT
                    String ORC4_3 = message.getPATIENT_RESULT().getORDER_OBSERVATION(i2).getORC().getOrc4_PlacerGroupNumber().getUniversalID().getValue();
                    hapi.on.olis.oru_r01.segment.ZNT newZnt = newOrderObservation.getORDER_NOTE(0).getZNT();
                    logger.info("Start of ZNT segment");
                    newZnt.getZnt1_SourceOrganization().getUniversalID().setValue(ORC4_3);
                    newZnt.getZnt1_SourceOrganization().getUniversalIDType().setValue("ISO");
                } else {

                    logger.fine("No NTE Segment");
                }

                // Copy BLG segment
                hapi.on.olis.oru_r01.segment.BLG newBlg = newOrderObservation.getBLG();
                hapi.on.olis.oru_r01.segment.BLG oldBlg = removedOrderObservation.getBLG();
                newBlg.parse(oldBlg.encode());

                // Copy OBX-NTE Pairs
                for (int childObxId = 0; childObxId < nextChildObservationIdentifiers.size(); childObxId++) {
                    String nextChildObservationIdentifier = nextChildObservationIdentifiers.get(childObxId).getCode();
                    logger.fine("+++ Looking for OBX-4 of " + nextChildObservationIdentifier);
                    for (int oldObxIndex = 0; oldObxIndex < removedOrderObservation.getOBSERVATIONReps(); oldObxIndex++) {
                        String identifier = removedOrderObservation.getOBSERVATION(oldObxIndex).getOBX().getObx3_ObservationIdentifier().getIdentifier().getValue();
                        int observationCount = newOrderObservation.getOBSERVATIONReps();

                        if (nextChildObservationIdentifier.equals(identifier)) {
                            newOrderObservation.getOBSERVATION(observationCount).getOBX().parse(removedOrderObservation.getOBSERVATION(oldObxIndex).getOBX().encode());
                            newOrderObservation.getOBSERVATION(observationCount).getOBX().getObx1_SetIDOBX().parse(Integer.toString(observationCount + 1));

                            //Copy ZBX Segments
                            sortKey = Integer.toString(observationCount + 1);

                            sortKey = Integer.toString(observationCount + 1);
                            sortKey = StringUtils.leftPad(sortKey, 4, "0");
                            newOrderObservation.getOBSERVATION(observationCount).getZBX().getTestResultReleaseDateTime().getTime().parse(getUTC(removedOrderObservation.getOBSERVATION(oldObxIndex).getZBX().getTestResultReleaseDateTime().getTime().getValue()));
                            newOrderObservation.getOBSERVATION(observationCount).getZBX().getTestResultSortKey().parse(sortKey);

                            // Copy Notes
                            for (int nteIndex = 0; nteIndex < removedOrderObservation.getOBSERVATION(oldObxIndex).getOBSERVATION_NOTEReps(); nteIndex++) {
                                newOrderObservation.getOBSERVATION(observationCount).getOBSERVATION_NOTE(nteIndex).getNTE().parse(removedOrderObservation.getOBSERVATION(oldObxIndex).getOBSERVATION_NOTE(nteIndex).getNTE().encode());
                                newOrderObservation.getOBSERVATION(observationCount).getOBSERVATION_NOTE(nteIndex).getZNT().parse(removedOrderObservation.getOBSERVATION(oldObxIndex).getOBSERVATION_NOTE(nteIndex).getZNT().encode());
                            }
                        }
                    }
                }
            }
        }
        for (int i3 = 0; i3 < message.getPATIENT_RESULT().getORDER_OBSERVATIONReps(); i3++) {

            message.getPATIENT_RESULT().getORDER_OBSERVATION(i3).getOBR().getObr1_SetIDOBR().setValue(Integer.toString(i3 + 1));

            //Sort for the Test Request Sort Key in ZBR for Panels
            //Sites will send the modality and test request code in the ZBR-11.  Certain sites will send the sort ID in the field.
            //The code will distinguish between an alpha and a numeric.
            String sortKey = Integer.toString(i3 + 1);
            sortKey = StringUtils.leftPad(sortKey, 4, "0");
            String testRequestSortKey = message.getPATIENT_RESULT().getORDER_OBSERVATION(i3).getZBR().getTestRequestSortKey().getValue();
            String universalServiceIdentifier = message.getPATIENT_RESULT().getORDER_OBSERVATION(i3).getOBR().getObr4_UniversalServiceIdentifier().getCe1_Identifier().getValue();
            if (foundPanels == true) {
                if (!isEmpty(testRequestSortKey)) {
                    if (testRequestSortKey.matches(".*[a-zA-Z].*")) {
                        message.getPATIENT_RESULT().getORDER_OBSERVATION(i3).getZBR().getTestRequestSortKey().setValue(testRequestSortKey + "_" + universalServiceIdentifier);
                    } else {
                        message.getPATIENT_RESULT().getORDER_OBSERVATION(i3).getZBR().getTestRequestSortKey().setValue(sortKey + "_" + universalServiceIdentifier);
                    }
                } else {
                    message.getPATIENT_RESULT().getORDER_OBSERVATION(i3).getZBR().getTestRequestSortKey().setValue(sortKey);
                }
            }
        }

        if (logger.isLoggable(Level.FINER) && foundPanels) {
            logger.finer("+++ Message is now: " + message.encode().replace("\r", "\r\n"));
        }

        return true;
    }

    private static synchronized String getUTC(String s) {
        String s1 = "";
        if (s == null || s.length() == 0) {
            return "";
        }

        String trimmedString = s.trim();
        if (trimmedString.length() == 8 || trimmedString.length() == 12 || trimmedString.length() == 14) {
            SimpleDateFormat sdf;
            if (trimmedString.length() == 8) {
                sdf = mySimpleDateFormat8;
            } else if (trimmedString.length() == 12) {
                sdf = mySimpleDateFormat12;
            } else if (trimmedString.length() == 14) {
                sdf = mySimpleDateFormat14;
            } else if (trimmedString.length() == 19) {
                return trimmedString;
            } else {
                sdf = mySimpleDateFormat19;
            }

            Date date = null;
            try {
                date = sdf.parse(s);
            } catch (ParseException e) {
                logger.info("Failed to parse date string \"" + s + "\" because of error: " + e.getMessage());
                return s;
            }
            s1 = mySimpleDateFormat19.format(date).toString();
        } else {
            return s;
        }
        return s1;
    }

    public static String removeChar(String s) {
        if (s != null && !s.equals("")) {
            if (s.charAt(s.length() - 2) == '-') {
                s = s.replaceFirst("-", "");
                char c = s.charAt(s.length() - 1);
                s = s.substring(0, s.length() - 2) + c;
            } else {
                return s;
            }
        } else {
            return s;
        }
        return s;
    }

    public boolean processMSH(ORU_R01 theMessage)
            throws Exception {
        // ++++++++++++++++++++Entering MSH+++++++++++++++++++++++++++++++++++++++++++
        String MSH3 = theMessage.getMSH().getMsh3_SendingApplication().getNamespaceID().getValue();

        logger.fine("+++Entering MSH segment+++");

        //theMessage.getMSH().getMsh3_SendingApplication().getNamespaceID().setValue("");
        //theMessage.getMSH().getMsh3_SendingApplication().getUniversalID().setValue("CN=LIS.UniHea.ProdTest, OU=Applications, OU=UniHea, OU=Hospitals, OU=Subscribers, DC=subscribers, DC=ssh");
        //theMessage.getMSH().getMsh3_SendingApplication().getUniversalIDType().setValue("X500");
        if (!isEmpty(theMessage.getMSH().getMsh7_DateTimeOfMessage().getTime().getValue())) {
            theMessage.getMSH().getMsh7_DateTimeOfMessage().getTime().setValue(getUTC(theMessage.getMSH().getMsh7_DateTimeOfMessage().getTime().getValue()));
        }
        //theMessage.getMSH().getMsh12_VersionID().getVersionID().setValue("2.3.1");

        logger.fine("+++Leaving MSH segment+++");

        return true;
        // ++++++++++++++++++++Leaving MSH+++++++++++++++++++++++++++++++++++++++++++
    }

    private String printStructure(Structure theStructure, int theLevel) throws HL7Exception {

        StringBuilder retVal = new StringBuilder();
        for (int i = 0; i < theLevel; i++) {
            retVal.append("   ");
        }
        retVal.append(theStructure.getName()).append("\r\n");

        if (theStructure instanceof Group) {
            Group group = (Group) theStructure;
            for (int i = 0; i < group.getNames().length; i++) {
                retVal.append(printStructure(group.get(group.getNames()[i]), theLevel + 1));
            }
        } else if (theStructure instanceof Segment) {
            Segment segment = (Segment) theStructure;
            retVal.append(segment.encode()).append("\r\n");
        }

        return retVal.toString();
    }

    /**
     * Removes the repetition of PID-3 at the given index (starts at 0) and shifts every repetition
     * afterwards forward. So if PID-3(1) is being deleted, PID-3(2) becomes PID-3(1).
     */
    /*
    private void removePid3PatientIdentifier( int theIndex )
    {
    // Loop from the current index to the end and copy each rep forwards. This overwrites the
    // rep at theIndex with the rep after it
    for (int j1 = theIndex; j1 < OlisORU.getPATIENT_RESULT().getGroupPatient().getPID().countPid3PatientIdentifierList() - 1; j1++) {
    OlisORU.getPATIENT_RESULT().getGroupPatient().getPID().setPid3PatientIdentifierList( j1, OlisORU.getPATIENT_RESULT().getGroupPatient().getPID().getPid3PatientIdentifierList( j1 + 1 ) );
    }
    ;
    // Clear the last rep, since it was copied to the spot before it.
    OlisORU.getPATIENT_RESULT().getGroupPatient().getPID().removePid3PatientIdentifierList( OlisORU.getPATIENT_RESULT().getGroupPatient().getPID().countPid3PatientIdentifierList() - 1 );
    }
     *
     */
    public boolean processPID(CanonicalHl7V2Message theInput, ORU_R01 theMessage)
            throws Exception {
        // ++++++++++++++++++++Entering PID+++++++++++++++++++++++++++++++++++++++++++
        // PID-3: Patient ID List
        // First we loop through all IDs to see what kind of IDs we have in the message
        boolean haveOHIP = false;
        boolean haveMRN = false;
        
        Set<String> jurisdictionCheck = new HashSet();
        jurisdictionCheck.add("AB");
        jurisdictionCheck.add("BC");
        jurisdictionCheck.add("MB");
        jurisdictionCheck.add("NB");
        jurisdictionCheck.add("NL");
        jurisdictionCheck.add("NT");
        jurisdictionCheck.add("NS");
        jurisdictionCheck.add("NU");
        jurisdictionCheck.add("ON");
        jurisdictionCheck.add("PE");
        jurisdictionCheck.add("QC");
        jurisdictionCheck.add("SK");
        jurisdictionCheck.add("YT");
        
        PID pid = theMessage.getPATIENT_RESULT().getPATIENT().getPID();

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("+++ Entering PID");
            logger.fine("+++ Message: " + theMessage.encode());
            logger.fine("+++ Message Segments: " + Arrays.asList(theMessage.getNames()));
            logger.fine("+++ PID Segment: " + pid.encode());
            logger.fine("+++ Count: " + pid.getPid3_PatientIdentifierListReps());
        }

        for (int i1 = 0; i1 < pid.getPid3_PatientIdentifierList().length; i1++) {

            String identifierType = pid.getPid3_PatientIdentifierList(i1).getIdentifierTypeCode().getValue();
            String identifierNumber = pid.getPid3_PatientIdentifierList(i1).getIDNumber().getValue();
            logger.info("+++ PID-3[" + (i1 + 1) + "] has identifier type: " + identifierType);

            if (JHN.equals(identifierType) && !isEmpty(identifierNumber)) {
                String jurisdiction = pid.getPid3_PatientIdentifierList(i1).getAssigningJurisdiction().getIdentifier().getValue();
                if (isEmpty(jurisdiction)) {
                    sendToDeadLetterAndForward(theInput, "INVALID_MESSAGE.V2.PID-3(" + (i1 + 1) + ")-9", "Missing jurisdiction for PID-3 with type JHN");
                    return false;
                } else if (jurisdictionCheck.contains(jurisdiction)) {
                    haveOHIP = true;
                }
            } else if (MR.equals(identifierType) && !isEmpty(identifierNumber)) {
                haveMRN = true;
            }
        }

        if (!haveOHIP && !haveMRN) {
            sendToDeadLetterAndForward(theInput, "INVALID_MESSAGE.V2.PID-3", "Message contains neither an OHIP nor a MRN");
            return false;
        }

        // Checking for Ultra RefIn results
        boolean flagForUltraRefIn = false;
        for (int iOrc = 0; iOrc < theMessage.getPATIENT_RESULT().getORDER_OBSERVATIONReps(); iOrc++) {
            String orc13_1ForRefIn = StringUtils.defaultIfEmpty(theMessage.getPATIENT_RESULT().getORDER_OBSERVATION(iOrc).getORC().getOrc13_EntererSLocation().getPl1_PointOfCare().getValue(), "");
            if (!orc13_1ForRefIn.isEmpty() && orc13_1ForRefIn.startsWith("Ultra_RefIn_")) {
                theMessage.getPATIENT_RESULT().getORDER_OBSERVATION(iOrc).getORC().getOrc13_EntererSLocation().getPl1_PointOfCare().setValue("");
                flagForUltraRefIn = true;
            }
        }

        Facility fif;

        for (int i1 = 0; i1 < pid.getPid3_PatientIdentifierList().length; i1++) {
            String identifierType = pid.getPid3_PatientIdentifierList(i1).getIdentifierTypeCode().getValue();
            String identifierNumber = pid.getPid3_PatientIdentifierList(i1).getIDNumber().getValue();
            String jurisdiction = pid.getPid3_PatientIdentifierList(i1).getAssigningJurisdiction().getIdentifier().getValue();
            String authority = pid.getPid3_PatientIdentifierList(i1).getAssigningAuthority().getUniversalID().getValue();
            if (!isEmpty(authority) && !isEmpty(jurisdiction)) {
                sendToDeadLetterAndForward(theInput, "INVALID_MESSAGE.V2.PID-3[" + (i1 + 1) + "]4,9", "PID-3 has both assigning authority and jurisdiction");
                return false;
            }
            if (JHN.equals(identifierType) && !isEmpty(identifierNumber)) {
                /*
                If this is a JHN (Jurisdictional Health Number) and it's for Ontario, validate the demographics and enrich
                 */
                if (authority != null && authority.length() > 0) {
                    sendToDeadLetterAndForward(theInput, "INVALID_MESSAGE.V2.PID-3[" + (i1 + 1) + "]-4", "Should not provide an assigning authority");
                    return false;
                }
                if (jurisdiction == null || jurisdiction.length() == 0) {
                    sendToDeadLetterAndForward(theInput, "INVALID_MESSAGE.V2.PID-3[" + (i1 + 1) + "]-9", "Missing jurisdiction");
                    return false;
                }

                if (flagForUltraRefIn) {
                    // HCV testing is disabled for Ultra RefIn results
                    logger.info("Not performing HCV validation; this is an Ultra RefIn result message");

                    continue;
                }

                if (!"ON".equals(jurisdiction)) {
                    // HCV testing is disabled for non-Ontario health card numbers
                    logger.info("Not performing HCV validation for jurisdiction: " + jurisdiction);
                    continue;
                }

                // Call HCV for validation
                String sendingApplication = theMessage.getMSH().getMsh3_SendingApplication().encode();

                if (!"DEV".equals(theInput.getEnvironment())) {
                    if (!sendingApplication.contains("UniHea")) {
                        // HCV testing is disabled for everyone except UHN
                        logger.info("Not performing HCV validation for site: " + sendingApplication);
                        continue;
                    }
                } else {
                    logger.fine("HCV Validation for Dev");
                }

                String versionCode;
                if (pid.getPid3_PatientIdentifierList(i1).getExtraComponents().numComponents() > 0) {
                    versionCode = pid.getPid3_PatientIdentifierList(i1).getExtraComponents().getComponent(0).encode();
                } else {
                    versionCode = pid.getPid3_PatientIdentifierList(i1).getCheckDigit().getValue();
                }

                String healthCardNumber = pid.getPid3_PatientIdentifierList(i1).getIDNumber().getValue();
                if (healthCardNumber == null || healthCardNumber.trim().length() == 0) {
                    sendToDeadLetterAndForward(theInput, "INVALID_MESSAGE.V2.PID-3[" + (i1 + 1) + "]-3", "No health card number supplied");
                    return false;
                }

                String requestingUserId = theMessage.getPATIENT_RESULT().getORDER_OBSERVATION().getOBR().getObr16_OrderingProvider().getXcn1_IDNumber().getValue();
                if (requestingUserId == null || requestingUserId.trim().length() == 0) {
                    sendToDeadLetterAndForward(theInput, "INVALID_MESSAGE.V2.OBR-16-1", "Missing Ordering Provider ID");
                    return false;
                }

                HCVRequest hcvRequest = new HCVRequest();
                hcvRequest.setHealthCardNumber(healthCardNumber);
                hcvRequest.setVersionCode(versionCode);
                hcvRequest.setRequestingUserId(requestingUserId);

                logger.info("+++Calling HCV");
                HCVResponse hcvResponse = getHcvService().lookupHcv(hcvRequest);

                String resCode = hcvResponse.getResponseCode();
                logger.info("+++Response code from HCV is : " + resCode + " - " + myHcvResponseCodeToDescription.get(resCode));

                if (resCode.equals(HCV_RESPONSE_CODE_FOR_PAHN)) {

                    logger.info("HCV is a pre assigned health number, skipping validation");

                } else if (myHcvCodesToPass.contains(resCode)) {

                    String HCV_Lname = hcvResponse.getLastName();
                    String HCV_Fname = hcvResponse.getFirstName();
                    String HCV_Mname = hcvResponse.getMiddleName();
                    String HCV_DOB = hcvResponse.getDateOfBirth();
                    String HCV_Gender = hcvResponse.getGender();

                    String PID5_1 = pid.getPid5_PatientName().getFamilyName().getSurname().getValue().toUpperCase();
                    if (isEmpty(PID5_1)) {
                        sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.PID-5-1", "Missing last name");
                        return false;
                    }
                    boolean lastNameMatches = PID5_1.equalsIgnoreCase(HCV_Lname);
                    String dob = pid.getPid7_DateTimeOfBirth().getTime().getValue();
                    if (isEmpty(dob)) {
                        sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.PID-7", "Missing date of birth");
                        return false;
                    }
                    boolean dobMatches = dob.equals(HCV_DOB);
                    String sex = pid.getPid8_AdministrativeSex().getValue();
                    if (isEmpty(sex)) {
                        sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.PID-8", "Missing administrative sex");
                        return false;
                    }
                    boolean sexMatches = sex.equalsIgnoreCase(HCV_Gender);
                    if (lastNameMatches && dobMatches && sexMatches) {
                        pid.getPid3_PatientIdentifierList(i1).parse("");
                        pid.getPid3_PatientIdentifierList(i1).getIDNumber().setValue(hcvResponse.getHealthCardNumber());
                        pid.getPid3_PatientIdentifierList(i1).getIdentifierTypeCode().setValue("JHN");
                        pid.getPid3_PatientIdentifierList(i1).getAssigningJurisdiction().getIdentifier().setValue("ON");
                        pid.getPid3_PatientIdentifierList(i1).getAssigningJurisdiction().getText().setValue("Ontario");
                        pid.getPid3_PatientIdentifierList(i1).getAssigningJurisdiction().getNameOfCodingSystem().setValue("HL70347");

                        if (!isEmpty(hcvResponse.getVersionCode())) {
                            // TODO: missing version code?
                            // theMessage.getPATIENT_RESULT().getPATIENT().getPID().getPid3_PatientIdentifierList( i1 ).getVersionCode( wsdl_HCVSvc.getHCVMSHTransactionsSoap().getHCVValidate_A().getOutput().getParameters().getHCVValidateAResponse().getHCVValidateAResult().getVersionCode() );
                            pid.getPid3_PatientIdentifierList(i1).getExtraComponents().getComponent(0).parse(versionCode);
                            pid.getPid3_PatientIdentifierList(i1).getCheckDigit().setValue("");
                        } else {
                            pid.getPid3_PatientIdentifierList(i1).getExtraComponents().getComponent(0).parse("");
                            pid.getPid3_PatientIdentifierList(i1).getCheckDigit().setValue("");
                        }

                        // Update first name based on response from HCV
                        boolean updatedFirstName = false;
                        if (!StringUtils.equalsIgnoreCase(pid.getPid5_PatientName().getGivenName().getValue(), HCV_Fname)) {
                            pid.getPid5_PatientName().getGivenName().setValue(HCV_Fname);
                            updatedFirstName = true;
                        }

                        // Update last name based on response from HCV
                        boolean updatedMiddleName = false;
                        if (!StringUtils.equalsIgnoreCase(pid.getPid5_PatientName().getSecondAndFurtherGivenNamesOrInitialsThereof().getValue(), HCV_Mname)) {
                            pid.getPid5_PatientName().getSecondAndFurtherGivenNamesOrInitialsThereof().setValue(HCV_Mname);
                            updatedMiddleName = true;
                        }

                        // Log whether or not we made any updates
                        if (updatedFirstName && updatedMiddleName) {
                            logger.info("OLIS HCV: Updating MIDDLE and GIVEN name on message with ID " + theInput.getMessageControlID());
                        } else if (updatedFirstName) {
                            logger.info("OLIS HCV: Updating GIVEN name on message with ID " + theInput.getMessageControlID());
                        } else if (updatedMiddleName) {
                            logger.info("OLIS HCV: Updating MIDDLE name on message with ID " + theInput.getMessageControlID());
                        } else {
                            logger.info("OLIS HCV: Not updating MIDDLE or GIVEN name on message with ID " + theInput.getMessageControlID());
                        }

                        // ++++++++++++++++++++Entering ADT A04 Message++++++++++++++++++++++++++++++++++++++++++
                        // ++++++++++++++++++++Leaving ADT A04 Message+++++++++++++++++++++++++++++++++++++++++++


                    } else if (!lastNameMatches) {
                        String rejectionReason = "Patient demographic mismatch: Last name does not match HCV";
                        logger.info("OLIS HCV: " + rejectionReason);
                        sendToDeadLetterAndForward(theInput, "INCORRECT_MESSAGE.V2.PID", rejectionReason);
                        return false;
                    } else if (!dobMatches) {
                        String rejectionReason = "Patient demographic mismatch: DOB does not match HCV";
                        logger.info("OLIS HCV: " + rejectionReason);
                        sendToDeadLetterAndForward(theInput, "INCORRECT_MESSAGE.V2.PID", rejectionReason);
                        return false;
                    } else if (!sexMatches) {
                        String rejectionReason = "Patient demographic mismatch: Sex does not match HCV";
                        logger.info("OLIS HCV: " + rejectionReason);
                        sendToDeadLetterAndForward(theInput, "INCORRECT_MESSAGE.V2.PID", rejectionReason);
                        return false;
                    }

                } else {

                    String rejectionReason = "HCV responded with code " + resCode + " - " + myHcvResponseCodeToDescription.get(resCode);
                    logger.info("OLIS HCV: " + rejectionReason);
                    sendToDeadLetterAndForward(theInput, "HCV_REJECT." + resCode, rejectionReason);
                    return false;
                }

            } else if (MR.equals(identifierType) && !isEmpty(identifierNumber)) {

                // Remove MRN if an OHIP was present
                if (haveOHIP) {
                    // Remove this ID since we already have an OHIP number.
                    // See the method being called for an explanation of how it works
                    pid.removePid3_PatientIdentifierList(i1);
                    i1--;
                    continue;
                }

                /*
                If this is an "MR" (Medical Record) ID, make sure that we are sending a valid assigning authority
                 */
                if (jurisdiction != null && jurisdiction.length() > 0) {
                    sendToDeadLetterAndForward(theInput, "INVALID_MESSAGE.V2.PID-3[" + (i1 + 1) + "].9", "MR (MRN) IDs should not have a jurisdiction");
                    return false;
                }
                if (authority == null || authority.length() == 0) {
                    sendToDeadLetterAndForward(theInput, "INVALID_MESSAGE.V2.PID-4[" + (i1 + 1) + "]-4-2", "Missing assigning authority universal ID");
                    return false;
                }
                String namespace = pid.getPid3_PatientIdentifierList(i1).getAssigningAuthority().getNamespaceID().getValue();
                if (isEmpty(namespace)) {
                    namespace = "ON_OLIS";
                }

                fif = lookupFacility(namespace, authority);
                if (fif == null) {
                    sendToDeadLetterAndForward(theInput, "INVALID_MESSAGE.V2.PID-3[" + (i1 + 1) + "]-4", "Invalid assigning authority: " + namespace + "/" + authority);
                    return false;
                }

                String newRoot = fif.getId().getRoot() + ":" + fif.getId().getExt();

                pid.getPid3_PatientIdentifierList(i1).getAssigningAuthority().getUniversalID().setValue(newRoot);
                pid.getPid3_PatientIdentifierList(i1).getAssigningAuthority().getNamespaceID().setValue("");
                pid.getPid3_PatientIdentifierList(i1).getAssigningAuthority().getUniversalIDType().setValue("ISO");

            } else if (isEmpty(identifierType)) {
                sendToDeadLetterAndForward(theInput, "INVALID_MESSAGE.V2.PID-3[" + (i1 + 1) + "]-5", "Missing identifier type");
                return false;
            } else {
                // Remove this ID since we only want MRN and OHIP
                // See the method being called for an explanation of how it works
                pid.removePid3_PatientIdentifierList(i1);
                i1--;
                continue;
            }
        }
        // End of PID-3 validation

        // Clean up PID-4
        for (int i = 0; i < pid.getPid4_AlternatePatientIDPID().length; i++) {
            pid.getPid4_AlternatePatientIDPID(i).parse("");
        }


        // Force PID5-1 into caps
        String PID5_1 = pid.getPid5_PatientName().getFamilyName().getSurname().getValue().toUpperCase();
        pid.getPid5_PatientName().getFamilyName().getSurname().setValue(PID5_1);


        // Patient Death Date and Time
        String patientDeathDate = pid.getPid29_PatientDeathDateAndTime().getTime().getValue();

        if (!isEmpty(patientDeathDate)) {
            pid.getPid29_PatientDeathDateAndTime().getTime().setValue(getUTC(patientDeathDate));
        } else {
            logger.fine("+++No Patient Death Date and Time");
        }

        // Validate PID-11
        for (int i = 0; i < pid.getPid11_PatientAddressReps(); i++) {
            XAD nextAddress = pid.getPid11_PatientAddress(i);
            if (isEmpty(nextAddress.getXad1_StreetAddress().encode())) {
                sendToDeadLetterAndForward(theInput, CanonicalMessageUtils.createMessageErroredForV2IncompleteMessage(getClass(), "Address-Street is required", "PID", "11", "1"));
            }
            if (isEmpty(nextAddress.getXad3_City().encode())) {
                sendToDeadLetterAndForward(theInput, CanonicalMessageUtils.createMessageErroredForV2IncompleteMessage(getClass(), "Address-City is required", "PID", "11", "3"));
            }
            if (isEmpty(nextAddress.getXad6_Country().encode())) {
                sendToDeadLetterAndForward(theInput, CanonicalMessageUtils.createMessageErroredForV2IncompleteMessage(getClass(), "Address-Country is required", "PID", "11", "6"));
            }
            if (isEmpty(nextAddress.getXad7_AddressType().encode())) {
                sendToDeadLetterAndForward(theInput, CanonicalMessageUtils.createMessageErroredForV2IncompleteMessage(getClass(), "Address-Type is required", "PID", "11", "7"));
            }
        }

        return true;
        // ++++++++++++++++++++Leaving PID+++++++++++++++++++++++++++++++++++++++++++
    }



	public boolean processPatientNote(CanonicalHl7V2Message theInput, ORU_R01 thePatientNote)
            throws Exception {
        // ++++++++++++++++++++Entering PatientNTE+++++++++++++++++++++++++++++++++++++++++++
        // NTE
        hapi.on.olis.oru_r01.segment.NTE theNte = thePatientNote.getPATIENT_RESULT().getPATIENT().getNTE();
        String nteComment = theNte.getNte3_Comment(0).getValue();

        if (!isEmpty(nteComment)) {
            theNte.getNte2_SourceOfComment().setValue("P");
            theNte.getNte4_CommentType().getIdentifier().setValue("RE");
            theNte.getNte4_CommentType().getText().setValue("Remark");
            theNte.getNte4_CommentType().getNameOfCodingSystem().setValue("HL70364");

            // ZNT
            String ORC4_3 = thePatientNote.getPATIENT_RESULT().getORDER_OBSERVATION(0).getORC().getOrc4_PlacerGroupNumber().getUniversalID().getValue();
            hapi.on.olis.oru_r01.segment.ZNT theZnt = thePatientNote.getPATIENT_RESULT().getPATIENT().getZNT();
            logger.info("Start of Group Order ZNT segment");
            theZnt.getZnt1_SourceOrganization().getUniversalID().setValue(ORC4_3);
            theZnt.getZnt1_SourceOrganization().getUniversalIDType().setValue("ISO");
        } else {
            logger.info("No Group Order Note");
        }

        return true;
        // ++++++++++++++++++++Leaving PatientNTE+++++++++++++++++++++++++++++++++++++++++++
    }

    public boolean processPV1(CanonicalHl7V2Message theInput, ORU_R01_PATIENT_RESULT thePatientResult)
            throws Exception {
        // ++++++++++++++++++++Entering PV1++++++++++++++++++++++++++++++++++++++++++
        logger.fine("+++Entering PV1 Segment+++");
        String PV1_7 = "";
        String PV1_17 = "";

        PV1_7 = thePatientResult.getPATIENT().getVISIT().getPV1().getPv17_AttendingDoctor().getIDNumber().getValue();
        PV1_17 = thePatientResult.getPATIENT().getVISIT().getPV1().getPv117_AdmittingDoctor().getIDNumber().getValue();
        if (!isEmpty(PV1_7)) {
            String identifierType = thePatientResult.getPATIENT().getVISIT().getPV1().getPv17_AttendingDoctor().getIdentifierTypeCode().getValue();
            if (isEmpty(identifierType)) {
                sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.PV1-7-13", "Missing identifier type for ID");
                return false;
            }
            String authority = thePatientResult.getPATIENT().getVISIT().getPV1().getPv17_AttendingDoctor().getAssigningJurisdiction().getIdentifier().getValue();
            if (isEmpty(authority)) {
                authority = thePatientResult.getPATIENT().getVISIT().getPV1().getPv17_AttendingDoctor().getAssigningAuthority().getNamespaceID().getValue();
            }
            if (isEmpty(authority)) {
                sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.PV1-7-9", "PV1-7 requires either assigning authority or jurisdiction");
                return false;
            }

            String mapClassifier = "";

            if ("ON".equals(authority)) {
                mapClassifier = identifierType;
            } else {
                mapClassifier = "MDL";
            }
            GetProviderResponse pif = lookupProvider(identifierType, authority, PV1_7, mapClassifier);
            if (pif.getProviderIfFound() != null) {
                String attendingDoctorId = pif.getProviderIfFound().getIdExtension();
                String attendingDoctorFName = pif.getProviderIfFound().getFirstName();
                String attendingDoctorLName = pif.getProviderIfFound().getLastName();
                String attendingDoctorSName = pif.getProviderIfFound().getMiddleName();
                String attendingDoctorIdRoot = pif.getProviderIfFound().getIdRoot();
                String attendingAssigningJurisdiction = pif.getProviderIfFound().getIdAuthority();

                thePatientResult.getPATIENT().getVISIT().getPV1().getPv17_AttendingDoctor().getIDNumber().setValue(attendingDoctorId);
                thePatientResult.getPATIENT().getVISIT().getPV1().getPv17_AttendingDoctor().getFamilyName().getSurname().setValue(attendingDoctorLName);
                thePatientResult.getPATIENT().getVISIT().getPV1().getPv17_AttendingDoctor().getGivenName().setValue(attendingDoctorFName);
                thePatientResult.getPATIENT().getVISIT().getPV1().getPv17_AttendingDoctor().getSecondAndFurtherGivenNamesOrInitialsThereof().setValue(attendingDoctorSName);
                thePatientResult.getPATIENT().getVISIT().getPV1().getPv17_AttendingDoctor().getAssigningAuthority().getNamespaceID().setValue("");
                thePatientResult.getPATIENT().getVISIT().getPV1().getPv17_AttendingDoctor().getIdentifierTypeCode().setValue(myIdRootToProviderClassifier.get(attendingDoctorIdRoot));
                thePatientResult.getPATIENT().getVISIT().getPV1().getPv17_AttendingDoctor().getAssigningJurisdiction().getIdentifier().setValue(attendingAssigningJurisdiction);
                thePatientResult.getPATIENT().getVISIT().getPV1().getPv17_AttendingDoctor().getAssigningJurisdiction().getText().setValue(myIdAuthorityToProviderClassifier.get(attendingAssigningJurisdiction));
                thePatientResult.getPATIENT().getVISIT().getPV1().getPv17_AttendingDoctor().getAssigningJurisdiction().getNameOfCodingSystem().setValue("HL70347");
            } else {
                sendToDeadLetterAndForward(theInput, CanonicalMessageUtils.createMessageErroredForV2ProviderMappingError(getClass(), "PV1", "7", null, myProviderClassifierToIdRoot.get(identifierType), authority, PV1_7));
                return false;
            }
        }

        if (!isEmpty(PV1_17)) {
            String identifierType = thePatientResult.getPATIENT().getVISIT().getPV1().getPv117_AdmittingDoctor().getIdentifierTypeCode().getValue();
            if (isEmpty(identifierType)) {
                sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.PV1-17-13", "Missing identifier type for ID");
                return false;
            }
            String authority = thePatientResult.getPATIENT().getVISIT().getPV1().getPv117_AdmittingDoctor().getAssigningJurisdiction().getIdentifier().getValue();
            if (isEmpty(authority)) {
                authority = thePatientResult.getPATIENT().getVISIT().getPV1().getPv117_AdmittingDoctor().getAssigningAuthority().getNamespaceID().getValue();
            }
            if (isEmpty(authority)) {
                sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.PV1-17-9", "PV1-17 requires either assigning authority or jurisdiction");
                return false;
            }
            String mapClassifier = "";

            if ("ON".equals(authority)) {
                mapClassifier = identifierType;
            } else {
                mapClassifier = "MDL";
            }

            GetProviderResponse pif = lookupProvider(identifierType, authority, PV1_17, mapClassifier);
            if (pif.getProviderIfFound() != null) {
                String admittingDoctorId = pif.getProviderIfFound().getIdExtension();
                String admittingDoctorFName = pif.getProviderIfFound().getFirstName();
                String admittingDoctorLName = pif.getProviderIfFound().getLastName();
                String admittingDoctorSName = pif.getProviderIfFound().getMiddleName();
                String admittingDoctorIdRoot = pif.getProviderIfFound().getIdRoot();
                String admittingAssigningJurisdiction = pif.getProviderIfFound().getIdAuthority();

                thePatientResult.getPATIENT().getVISIT().getPV1().getPv117_AdmittingDoctor().getIDNumber().setValue(admittingDoctorId);
                thePatientResult.getPATIENT().getVISIT().getPV1().getPv117_AdmittingDoctor().getFamilyName().getSurname().setValue(admittingDoctorLName);
                thePatientResult.getPATIENT().getVISIT().getPV1().getPv117_AdmittingDoctor().getGivenName().setValue(admittingDoctorFName);
                thePatientResult.getPATIENT().getVISIT().getPV1().getPv117_AdmittingDoctor().getSecondAndFurtherGivenNamesOrInitialsThereof().setValue(admittingDoctorSName);
                thePatientResult.getPATIENT().getVISIT().getPV1().getPv117_AdmittingDoctor().getAssigningAuthority().getNamespaceID().setValue("");
                thePatientResult.getPATIENT().getVISIT().getPV1().getPv117_AdmittingDoctor().getIdentifierTypeCode().setValue(myIdRootToProviderClassifier.get(admittingDoctorIdRoot));
                thePatientResult.getPATIENT().getVISIT().getPV1().getPv117_AdmittingDoctor().getAssigningJurisdiction().getIdentifier().setValue(admittingAssigningJurisdiction);
                thePatientResult.getPATIENT().getVISIT().getPV1().getPv117_AdmittingDoctor().getAssigningJurisdiction().getText().setValue(myIdAuthorityToProviderClassifier.get(admittingAssigningJurisdiction));
                thePatientResult.getPATIENT().getVISIT().getPV1().getPv117_AdmittingDoctor().getAssigningJurisdiction().getNameOfCodingSystem().setValue("HL70347");
            } else {
                sendToDeadLetterAndForward(theInput, CanonicalMessageUtils.createMessageErroredForV2ProviderMappingError(getClass(), "PV1", "17", null, myProviderClassifierToIdRoot.get(identifierType), authority, PV1_17));
            }
        }


        String PV1_2 = thePatientResult.getPATIENT().getVISIT().getPV1().getPv12_PatientClass().getValue();
        if ("E".equals(PV1_2) || "I".equals(PV1_2) || "O".equals(PV1_2) || "P".equals(PV1_2)) {
            // We're OK
        } else {
            logger.info("Replacing patient class " + PV1_2 + " with value \"O\"");
            thePatientResult.getPATIENT().getVISIT().getPV1().getPv12_PatientClass().setValue("O");
        }


        // ++++++++++++++++++++Leaving PV1++++++++++++++++++++++++++++++++++++++++++
        return true;
    }

    private boolean processMessage(CanonicalHl7V2Message input, ORU_R01 OlisORU) throws Exception {
        logger.fine("+++ Going to beak out panel tests");
        if (!breakOutPanels(input, OlisORU)) {
            logger.fine("+++ Leaving breakOutPanels");
            return true;
        }

        logger.fine("+++ Going to process MSH");
        if (!processMSH(OlisORU)) {
            logger.fine("+++ Leaving MSH");
            return true;
        }

        logger.fine("+++ Going to process PID");
        if (!processPID(input, OlisORU)) {
            logger.fine("+++ Leaving PID");
            return true;
        }

        logger.fine("+++ Going to process PatientNote");
        if (!processPatientNote(input, OlisORU)) {
            logger.fine("+++ Leaving PatientNote");
            return true;
        }

        logger.fine("+++ Going to process PV1");
        if (!processPV1(input, OlisORU.getPATIENT_RESULT())) {
            logger.fine("+++ Leaving PV1");
            return true;
        }

        for (int i1 = 0; i1 < OlisORU.getPATIENT_RESULT().getORDER_OBSERVATIONReps(); i1 += 1) {
            hapi.on.olis.oru_r01.group.ORU_R01_ORDER_OBSERVATION orderObservation = OlisORU.getPATIENT_RESULT().getORDER_OBSERVATION(i1);
            hapi.on.olis.oru_r01.group.ORU_R01_ORDER_OBSERVATION firstOrderObservation = OlisORU.getPATIENT_RESULT().getORDER_OBSERVATION(0);
            if (!processGroupOrderObservation(input, OlisORU, orderObservation, firstOrderObservation)) {
                logger.fine("+++ Leaving GroupOrderObservation");
                return true;
            }
        }
        return false;
    }

    private boolean processGroupOrderObservation(CanonicalHl7V2Message theInput, hapi.on.olis.oru_r01.message.ORU_R01 theMessage, hapi.on.olis.oru_r01.group.ORU_R01_ORDER_OBSERVATION theGroupOrderObservation, hapi.on.olis.oru_r01.group.ORU_R01_ORDER_OBSERVATION theFirstGroupOrderObservation)
            throws Exception {

        logger.fine("+++ Going to process ORC");
        if (!processORC(theInput, theGroupOrderObservation.getORC())) {
            return false;
        }

        logger.fine("+++ Going to process OBR-ZBR");
        if (!processOBR(theInput, theGroupOrderObservation.getOBR(), theFirstGroupOrderObservation.getOBR())) {
            return false;
        }
        if (!processZBR(theInput, theGroupOrderObservation.getZBR())) {
            return false;
        }

        for (int i = 0; i < theGroupOrderObservation.getORDER_NOTEReps(); i++) {
            logger.fine("+++ Going to process Order Note");
            if (!processGroupOrderNote(theGroupOrderObservation.getORC(), theGroupOrderObservation.getORDER_NOTE(i))) {
                return false;
            }
        }
        for (int i = 0; i < theGroupOrderObservation.getOBSERVATIONReps(); i++) {
            logger.fine("+++ Going to process Observation");
            if (!processGroupObservation(theInput, theMessage, theGroupOrderObservation.getORC(), theGroupOrderObservation.getOBSERVATION(i))) {
                return false;
            }
        }

        logger.fine("+++ Going to process BLG");
        if (!processBLG(theInput, theGroupOrderObservation.getBLG())) {
            return false;
        }
        return true;
    }

    public boolean processBLG(CanonicalHl7V2Message theInput, hapi.on.olis.oru_r01.segment.BLG theBlg)
            throws Exception {
        String idNumber = theBlg.getBlg3_AccountID().getIDNumber().getValue();
        if (isEmpty(idNumber) || !myValidPayors.contains(idNumber)) {
            sendToDeadLetterAndForward(theInput, "INCORRECT_MESSAGE.V2.BLG-3-1", "Unknown billing ID, valid values: " + myValidPayors + " - Found: " + idNumber);
            return false;
        }
        return true;
    }

    public boolean processGroupObservationNote(hapi.on.olis.oru_r01.segment.ORC theOrc, hapi.on.olis.oru_r01.group.ORU_R01_OBSERVATION_NOTE theGroupObservationNote)
            throws Exception {

        logger.fine("+++ Processing OBX Note");

        // NTE
        hapi.on.olis.oru_r01.segment.NTE theNte = theGroupObservationNote.getNTE();
        theNte.getNte2_SourceOfComment().setValue("L");
        theNte.getNte4_CommentType().getIdentifier().setValue("RE");
        theNte.getNte4_CommentType().getText().setValue("Remark");
        theNte.getNte4_CommentType().getNameOfCodingSystem().setValue("HL70364");

        // ZNT
        hapi.on.olis.oru_r01.segment.ZNT theZnt = theGroupObservationNote.getZNT();
        String ORC4_3 = theOrc.getOrc4_PlacerGroupNumber().getUniversalID().getValue();
        theZnt.getZnt1_SourceOrganization().getUniversalID().setValue(ORC4_3);
        theZnt.getZnt1_SourceOrganization().getUniversalIDType().setValue("ISO");
        // TODO:
        // theZnt.getZnt1_SourceOrganization().getHD().setN341UniversalId( ORC4_3 );
        // theZnt.getZnt1_SourceOrganization().getHD().setN343UniversalIdType( "ISO" );

        return true;
    }

    public boolean processGroupOrderNote(hapi.on.olis.oru_r01.segment.ORC theOrc, hapi.on.olis.oru_r01.group.ORU_R01_ORDER_NOTE theGroupOrderNote)
            throws Exception {

        // NTE
        hapi.on.olis.oru_r01.segment.NTE theNte = theGroupOrderNote.getNTE();
        String nteComment = theNte.getNte3_Comment(0).getValue();

        if (!isEmpty(nteComment)) {
            theNte.getNte2_SourceOfComment().setValue("L");
            theNte.getNte4_CommentType().getIdentifier().setValue("RE");
            theNte.getNte4_CommentType().getText().setValue("Remark");
            theNte.getNte4_CommentType().getNameOfCodingSystem().setValue("HL70364");

            // ZNT
            String ORC4_3 = theOrc.getOrc4_PlacerGroupNumber().getUniversalID().getValue();
            hapi.on.olis.oru_r01.segment.ZNT theZnt = theGroupOrderNote.getZNT();
            logger.info("Start of Group Order ZNT segment");
            theZnt.getZnt1_SourceOrganization().getUniversalID().setValue(ORC4_3);
            theZnt.getZnt1_SourceOrganization().getUniversalIDType().setValue("ISO");
        } else {
            logger.info("No Group Order Note");
        }
        return true;
    }

    public boolean processGroupObservation(CanonicalHl7V2Message theInput, hapi.on.olis.oru_r01.message.ORU_R01 theMessage, hapi.on.olis.oru_r01.segment.ORC theOrc, hapi.on.olis.oru_r01.group.ORU_R01_OBSERVATION theGroupObservation)
            throws Exception {
        hapi.on.olis.oru_r01.segment.OBX theObx = theGroupObservation.getOBX();
        String valueType = theObx.getObx2_ValueType().getValue();
        String resultStatus = theObx.getObx11_ObservationResultStatus().getValue();
        if (isEmpty(valueType) && (!"X".equals(resultStatus) && !"N".equals(resultStatus))) {
            sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.OBX-2", "OBX-2 Value Type");
            return false;
        }

        // OBX-3 Map Observation Identifier
        String OBX3_1 = theObx.getObx3_ObservationIdentifier().getIdentifier().getValue();
        String OBX3_3 = theObx.getObx3_ObservationIdentifier().getNameOfCodingSystem().getValue();
        String mappedObservationIdentifier = OBX3_1;
        if (!"HL79902".equals(OBX3_3)) {
            if (isEmpty(OBX3_1) || isEmpty(OBX3_3)) {
                sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.OBX-3", "Missing terminology ID or coding system");
            }

            logger.fine("+++ OBX looking up terminology for " + OBX3_1);
            CD mappedCode = lookupTerminologySingle("OlisRequestCodes", OBX3_1, OBX3_3, "1.3.6.1.4.1.12201.1.1.1.3");
            logger.fine("+++ OBX found terminology: " + mappedCode);

            if (mappedCode == null) {
                String errorCode = CanonicalMessageUtils.createMessageErroredForV2MappingErrorCode("OBX", "3", null);
                String errorMessage = CanonicalMessageUtils.createMessageErroredForV2MappingErrorMessage(getTerminologyService(), OBX3_1, OBX3_3, "1.3.6.1.4.1.12201.1.1.1.3");
                sendToDeadLetterAndForward(theInput, errorCode, errorMessage);
                return false;
            }

            mappedObservationIdentifier = mappedCode.getCode();
            theObx.getObx3_ObservationIdentifier().getIdentifier().setValue(mappedObservationIdentifier);
            theObx.getObx3_ObservationIdentifier().getText().setValue(mappedCode.getDisplayName());
            theObx.getObx3_ObservationIdentifier().getNameOfCodingSystem().setValue("HL79902");
        }

        // If the value is a CE (ie. OBX-2 says that OBX-5 is a CE), we might have to do terminology mapping
        if ("CE".equals(valueType)) {
            String[] observationValueComponents = theObx.getObx5_ObservationValue(0).encode().split("\\^");
            if (observationValueComponents.length >= 3) {
                if (observationValueComponents[2].indexOf("1.3.6.1.4.1.12201.1") == 0) {

                    logger.fine("+++ OBX looking up terminology for " + observationValueComponents[0]);
                    CD mappedCode = lookupTerminologySingle("OlisRequestCodes", observationValueComponents[0], observationValueComponents[2], "1.3.6.1.4.1.12201.1.1.1.3");
                    logger.fine("+++ OBX found terminology: " + mappedCode);

                    if (mappedCode == null) {
                        String errorCode = CanonicalMessageUtils.createMessageErroredForV2MappingErrorCode("OBX", "5", null);
                        String errorMessage = CanonicalMessageUtils.createMessageErroredForV2MappingErrorMessage(getTerminologyService(), observationValueComponents[0], observationValueComponents[2], "1.3.6.1.4.1.12201.1.1.1.3");
                        sendToDeadLetterAndForward(theInput, errorCode, errorMessage);
                        return false;
                    }

                    String newValue = mappedCode.getCode() + "^" + mappedCode.getDisplayName() + "^" + "HL79905";
                    theObx.getObx5_ObservationValue(0).parse(newValue);
                }
            }
        }

        theObx.getObx14_DateTimeOfTheObservation().getTime().parse("");
        hapi.on.olis.oru_r01.segment.ZBX theZbx = theGroupObservation.getZBX();
        theZbx.getZbx1_TestResultReleaseDateTime().getTime().setValue(getUTC(theZbx.getZbx1_TestResultReleaseDateTime().getTime().getValue()));

        for (int i = 0; i < theGroupObservation.getOBSERVATION_NOTEReps(); i++) {
            if (!processGroupObservationNote(theOrc, theGroupObservation.getOBSERVATION_NOTE(i))) {
                return false;
            }
        }

        // Check if this OBX's observation identifier is on OLIS's list of
        // codes marked as "anciliary". If so, they need to be treated slightly
        // differently: The code is not considered an "OLIS" code even though
        // it is still LOINC, so the name of coding system must by "LN". In addition,
        // the result status should be "Z" to indicate that the result does not
        // come from the lab
        //DISABLE THE CODE BLEOW SINCE OLIS DOES NOT REQUIRE IT ANYMORE
//        ValidateCodeSimpleRequest validateRequest = new ValidateCodeSimpleRequest();
//        validateRequest.setCodeId(mappedObservationIdentifier);
//        validateRequest.setCodeSystemId("1.3.6.1.4.1.12201.1.1.1.43");
//        ValidateCodeSimpleResponse validateResponse = getTerminologyService().validateCodeSimple(validateRequest);
//        if (validateResponse.isValidates()) {
//            theObx.getObservationIdentifier().getNameOfCodingSystem().setValue("LN");
//            theObx.getObservationResultStatus().setValue("Z");
//        }

        return true;
    }

    public boolean processORC(CanonicalHl7V2Message theInput, hapi.on.olis.oru_r01.segment.ORC theOrc)
            throws Exception {
        // If ORC-21 is present, look up the facility in the facility registry
        String performingLabFacilityId = theOrc.getOrc21_OrderingFacilityName().getAssigningAuthority().getUniversalID().getValue();
        String performingLabFacilityIdNamespace = theOrc.getOrc21_OrderingFacilityName().getAssigningAuthority().getNamespaceID().getValue();
        if (isEmpty(performingLabFacilityIdNamespace)) {
            performingLabFacilityIdNamespace = "ON_OLIS";
        }

        Facility orderingFacility = null;
        if (performingLabFacilityId != null && performingLabFacilityId.length() > 0) {
            orderingFacility = lookupFacility(performingLabFacilityIdNamespace, performingLabFacilityId);
            if (orderingFacility == null) {
                sendToDeadLetterAndForward(theInput, "INVALID_MESSAGE.V2.ORC-21", "Value " + performingLabFacilityId + " was not found in the facility registry");
                return false;
            }

            if (orderingFacility.getAddress() == null) {
                throw new Exception("Address is null, this should not happen");
            }

            String facilityAddress1 = orderingFacility.getAddress().getAddress1();
            String facilityAddress2 = orderingFacility.getAddress().getAddress2();

            theOrc.getOrc21_OrderingFacilityName().getOrganizationName().setValue(orderingFacility.getOrgName());
            theOrc.getOrc21_OrderingFacilityName().getAssigningAuthority().getUniversalIDType().setValue("ISO");
            theOrc.getOrc21_OrderingFacilityName().getAssigningAuthority().getUniversalID().setValue(orderingFacility.getId().getRoot() + ":" + orderingFacility.getId().getExt());
            theOrc.getOrc21_OrderingFacilityName().getAssigningAuthority().getNamespaceID().setValue("");
            theOrc.getOrc22_OrderingFacilityAddress().getStreetAddress().getStreetOrMailingAddress().setValue(facilityAddress1);
            theOrc.getOrc22_OrderingFacilityAddress().getOtherDesignation().setValue(facilityAddress2);
            theOrc.getOrc22_OrderingFacilityAddress().getCity().setValue(orderingFacility.getAddress().getCity());
            theOrc.getOrc22_OrderingFacilityAddress().getStateOrProvince().setValue(orderingFacility.getAddress().getProvince());
            theOrc.getOrc22_OrderingFacilityAddress().getZipOrPostalCode().setValue(orderingFacility.getAddress().getPostalCode());
            theOrc.getOrc22_OrderingFacilityAddress().getCountry().setValue("CAN");
            theOrc.getOrc22_OrderingFacilityAddress().getAddressType().setValue("B");
        }

        // ORC-4 - Validate the value
        // ORC-4-1 - Entity Identifier
        if (isEmpty(theOrc.getOrc4_PlacerGroupNumber().getEntityIdentifier().getValue())) {
            sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.ORC-4-1", "No placer group number");
            return false;
        }
        // ORC-4-3 Universal ID (facility)
        if (isEmpty(theOrc.getOrc4_PlacerGroupNumber().getUniversalID().getValue())) {
            if (orderingFacility != null) {
                theOrc.getOrc4_PlacerGroupNumber().getUniversalID().setValue(orderingFacility.getId().getRoot() + ":" + orderingFacility.getId().getExt());
            } else {
                sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.ORC-4-3", "No universal ID for placer group number");
                return false;
            }
        } else {
            /*
            If ORC-4 is present look up the facility in the facility registry. If the namespace
            is present but isn't ON_OLIS, we are also doing mapping
             */
            String facilityId = theOrc.getOrc4_PlacerGroupNumber().getUniversalID().getValue();
            String facilityNamespace = theOrc.getOrc4_PlacerGroupNumber().getNamespaceID().getValue();
            if (isEmpty(facilityNamespace)) {
                facilityNamespace = "ON_OLIS";
            }

            Facility fif = lookupFacility(facilityNamespace, facilityId);
            if (fif != null) {
                String mappedFacilityId = fif.getId().getRoot() + ":" + fif.getId().getExt();
                theOrc.getOrc4_PlacerGroupNumber().getUniversalID().setValue(mappedFacilityId);
            } else {
                sendToDeadLetterAndForward(theInput, "INVALID_MESSAGE.V2.ORC-4-3", "Value " + facilityId + " was not found in the facility registry");
                return false;
            }
        }
        theOrc.getOrc4_PlacerGroupNumber().getNamespaceID().setValue("");
        theOrc.getOrc4_PlacerGroupNumber().getUniversalIDType().setValue("ISO");

        // ORC-9 - Make sure we have enough chars
        String ORC9 = theOrc.getOrc9_DateTimeOfTransaction().getTime().getValue();
        if (isEmpty(ORC9)) {
            sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.ORC-9", "No date/time of transaction provided");
            return false;
        }
        theOrc.getOrc9_DateTimeOfTransaction().getTime().setValue(getUTC(ORC9));

        // ORC-10
        theOrc.getOrc1_OrderControl().setValue("");

        // ORC-21 is mapped at the start of this method

        return true;
    }

    public boolean processOBR(CanonicalHl7V2Message theInput, hapi.on.olis.oru_r01.segment.OBR theObr, hapi.on.olis.oru_r01.segment.OBR theFirstObr)
            throws Exception {
        String OBR_16 = theObr.getObr16_OrderingProvider().getIDNumber().getValue();
        if (isEmpty(OBR_16)) {
            sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.OBR-16", "No ordering provider");
            return false;
        }


        String identifierType = theObr.getObr16_OrderingProvider().getIdentifierTypeCode().getValue();
        String authority = theObr.getObr16_OrderingProvider().getAssigningJurisdiction().getIdentifier().getValue();
        if (isEmpty(authority)) {
            authority = theObr.getObr16_OrderingProvider().getAssigningAuthority().getNamespaceID().getValue();
        }
        if (isEmpty(authority)) {
            sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.OBR-16-9", "OBR-16-9 requires either assigning authority or jurisdiction");
            return false;
        }
        if (isEmpty(identifierType)) {
            sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.OBR-16-13", "Missing identifier type for ID");
            return false;
        }
        logger.fine("+++Parameters of OBR-16: " + OBR_16 + " " + identifierType + " " + authority);

        String mapClassifier = "";

        if ("ON".equals(authority)) {
            mapClassifier = identifierType;
        } else {
            mapClassifier = "MDL";
        }
        GetProviderResponse pif = lookupProvider(identifierType, authority, OBR_16, mapClassifier);
        if (pif.getProviderIfFound() == null) {
            sendToDeadLetterAndForward(theInput, CanonicalMessageUtils.createMessageErroredForV2ProviderMappingError(getClass(), "OBR", "16", null, myProviderClassifierToIdRoot.get(identifierType), authority, OBR_16));
            return false;
        }
        String orderingProviderId = pif.getProviderIfFound().getIdExtension();
        String orderingProviderLname = pif.getProviderIfFound().getLastName();
        String orderingProviderFname = pif.getProviderIfFound().getFirstName();
        String orderingProviderMname = pif.getProviderIfFound().getMiddleName();
        String orderingProviderIdRoot = pif.getProviderIfFound().getIdRoot();
        String orderingProviderIdAuthority = pif.getProviderIfFound().getIdAuthority();


        theObr.getObr16_OrderingProvider().getIDNumber().setValue(orderingProviderId);
        theObr.getObr16_OrderingProvider().getFamilyName().getSurname().setValue(orderingProviderLname);
        theObr.getObr16_OrderingProvider().getGivenName().setValue(orderingProviderFname);
        theObr.getObr16_OrderingProvider().getXcn4_SecondAndFurtherGivenNamesOrInitialsThereof().setValue(orderingProviderMname);
        theObr.getObr16_OrderingProvider().getAssigningAuthority().getNamespaceID().setValue("");
        theObr.getObr16_OrderingProvider().getIdentifierTypeCode().setValue(myIdRootToProviderClassifier.get(orderingProviderIdRoot));
        theObr.getObr16_OrderingProvider().getSourceTable().setValue("");
        theObr.getObr16_OrderingProvider().getAssigningJurisdiction().getIdentifier().setValue(orderingProviderIdAuthority);
        theObr.getObr16_OrderingProvider().getAssigningJurisdiction().getText().setValue(myIdAuthorityToProviderClassifier.get(orderingProviderIdAuthority));
        theObr.getObr16_OrderingProvider().getAssigningJurisdiction().getNameOfCodingSystem().setValue("HL70347");

        // OBR-2-1 - Validate it is present
        if (isEmpty(theObr.getObr2_PlacerOrderNumber().getEi1_EntityIdentifier().getValue())) {
            sendToDeadLetterAndForward(theInput, CanonicalMessageUtils.createMessageErroredForV2IncompleteMessage(getClass(), "Filler order number is required", "OBR", "2", "1"));
        }

        // OBR-2 Placer Number - Validate/translate universal ID
        String facilityRoot = theObr.getObr2_PlacerOrderNumber().getUniversalID().getValue();
        if (isEmpty(facilityRoot)) {
            sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSASGE.V2.OBR-2-3", "Missing universal ID for placer number");
            return false;
        }
        String facilityNamespace = theObr.getObr2_PlacerOrderNumber().getNamespaceID().getValue();
        if (!isEmpty(facilityNamespace) && !"ON_OLIS".equals(facilityNamespace)) {
            Facility fif = lookupFacility(facilityNamespace, facilityRoot);
            if (fif == null) {
                sendToDeadLetterAndForward(theInput, "INCORRECT_MESSASGE.V2.OBR-2-3", "Invalid universal ID: " + facilityRoot + " for namespace " + facilityNamespace);
                return false;
            }
            theObr.getObr2_PlacerOrderNumber().getUniversalID().setValue(fif.getId().getRoot() + ":" + fif.getId().getExt());
        }
        theObr.getObr2_PlacerOrderNumber().getNamespaceID().setValue("");
        theObr.getObr2_PlacerOrderNumber().getUniversalIDType().setValue("ISO");

        // OBR-3 - Validate it is present
        if (isEmpty(theObr.getObr3_FillerOrderNumber().getEi1_EntityIdentifier().getValue())) {

            // If no filler order number is present (OBR-3-1), make sure we
            // don't have any other fields populated in OBR-3
            theObr.getObr3_FillerOrderNumber().parse("");

        } else {

            // OBR-3 Filler Order Number - Validate/translate universal ID
            facilityRoot = theObr.getObr3_FillerOrderNumber().getUniversalID().getValue();
            if (isEmpty(facilityRoot)) {
                sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSASGE.V2.OBR-3-3", "Missing universal ID for placer number");
                return false;
            }
            facilityNamespace = theObr.getObr3_FillerOrderNumber().getNamespaceID().getValue();
            if (!isEmpty(facilityNamespace) && !"ON_OLIS".equals(facilityNamespace)) {
                Facility fif = lookupFacility(facilityNamespace, facilityRoot);
                if (fif == null) {
                    sendToDeadLetterAndForward(theInput, "INCORRECT_MESSASGE.V2.OBR-3-3", "Invalid universal ID: " + facilityRoot + " for namespace " + facilityNamespace);
                    return false;
                }
                theObr.getObr3_FillerOrderNumber().getUniversalID().setValue(fif.getId().getRoot() + ":" + fif.getId().getExt());
            }
            theObr.getObr3_FillerOrderNumber().getNamespaceID().setValue("");
            theObr.getObr3_FillerOrderNumber().getUniversalIDType().setValue("ISO");

        }

        // Map OBR terminology
        String OBR4_1 = theObr.getObr4_UniversalServiceIdentifier().getIdentifier().getValue();
        String OBR4_3 = theObr.getObr4_UniversalServiceIdentifier().getNameOfCodingSystem().getValue();
        
        String OBR15_1_1 = theObr.getObr15_SpecimenSource().getSps1_SpecimenSourceNameOrCode().getCwe1_Identifier().getValue();
        String OBR15_1_3 = theObr.getObr15_SpecimenSource().getSps1_SpecimenSourceNameOrCode().getCwe3_NameOfCodingSystem().getValue();
        if (isEmpty(OBR4_1)) {
            sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.OBR-4-1", "No Universal Service Identifier - Code");
            return false;
        }
        if (isEmpty(OBR4_3)) {
            sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.OBR-4-3", "No Universal Service Identifier - Coding System");
            return false;
        }
        if (!"HL79901".equals(OBR4_3)) {
            CD mappedCode = lookupTerminologySingle("OlisRequestCodes", OBR4_1, OBR4_3, "1.3.6.1.4.1.12201.1.1.1.1");
            if (mappedCode == null) {
                String errorCode = CanonicalMessageUtils.createMessageErroredForV2MappingErrorCode("OBR", "4", null);
                String errorMessage = CanonicalMessageUtils.createMessageErroredForV2MappingErrorMessage(getTerminologyService(), OBR4_1, OBR4_3, "1.3.6.1.4.1.12201.1.1.1.1");
                sendToDeadLetterAndForward(theInput, errorCode, errorMessage);
                return false;
            }

            theObr.getObr4_UniversalServiceIdentifier().getIdentifier().setValue(mappedCode.getCode());
            theObr.getObr4_UniversalServiceIdentifier().getText().setValue(mappedCode.getDisplayName());
            theObr.getObr4_UniversalServiceIdentifier().getNameOfCodingSystem().setValue("HL79901");

            // Map specimen codes to OLIS

            if (!isEmpty(OBR15_1_1) && !isEmpty(OBR15_1_3)) {
                mappedCode = lookupTerminologySingle("OlisSpecimenCodes", OBR15_1_1, OBR15_1_3, "1.3.6.1.4.1.12201.1.1.1.2");
                if (mappedCode == null) {
                    String errorCode = CanonicalMessageUtils.createMessageErroredForV2MappingErrorCode("OBR", "15", null);
                    String errorMessage = CanonicalMessageUtils.createMessageErroredForV2MappingErrorMessage(getTerminologyService(), OBR15_1_1, OBR15_1_3, "1.3.6.1.4.1.12201.1.1.1.2");
                    sendToDeadLetterAndForward(theInput, errorCode, errorMessage);
                    return false;
                }
            } else {
                mappedCode = lookupTerminologySingle("OlisRequestCodes", OBR4_1, OBR4_3, "1.3.6.1.4.1.12201.1.1.1.2");
                if (mappedCode == null) {
                    String errorCode = CanonicalMessageUtils.createMessageErroredForV2MappingErrorCode("OBR", "4", null);
                    String errorMessage = CanonicalMessageUtils.createMessageErroredForV2MappingErrorMessage(getTerminologyService(), OBR4_1, OBR4_3, "1.3.6.1.4.1.12201.1.1.1.2");
                    sendToDeadLetterAndForward(theInput, errorCode, errorMessage);
                    return false;
                }
            }
                if ((!isEmpty(OBR15_1_1))) {
                    String OBR15_5_2 = theObr.getObr15_SpecimenSource().getSiteModifier().getText().getValue();
                    theObr.getObr15_SpecimenSource().getSiteModifier().getText().setValue(OBR15_5_2);
                }
                theObr.getObr15_SpecimenSource().getSpecimenSourceNameOrCode().getIdentifier().setValue(mappedCode.getCode());
                theObr.getObr15_SpecimenSource().getSpecimenSourceNameOrCode().getText().setValue(mappedCode.getDisplayName());
                theObr.getObr15_SpecimenSource().getSpecimenSourceNameOrCode().getNameOfCodingSystem().setValue("HL70070");
        } else {

            /* This is a special case. If we have broken up a single ORDER_OBSERVATION for Micro to create a group
             * of ORDER_OBSERVATIONS, then we may not yet know the specimen source before now for the automatically
             * generated ORDER_OBSERVATION group containing this specific OBR segment. In that case, we copy the
             * specimen source from the first ORDER_OBSERVATION if we have one.
             */
            if ("TR10695-5".equals(OBR4_1)) {
                if (isEmpty(theObr.getObr15_SpecimenSource().getSpecimenSourceNameOrCode().getIdentifier().getValue())) {
                    String specimenSourceCode = theFirstObr.getObr15_SpecimenSource().getSpecimenSourceNameOrCode().getIdentifier().getValue();
                    String specimenSourceText = theFirstObr.getObr15_SpecimenSource().getSpecimenSourceNameOrCode().getText().getValue();
                    String specimenSourceCodeSystem = theFirstObr.getObr15_SpecimenSource().getSpecimenSourceNameOrCode().getNameOfCodingSystem().getValue();
                    if (!isEmpty(specimenSourceCode)) {
                        theObr.getObr15_SpecimenSource().getSpecimenSourceNameOrCode().getIdentifier().setValue(specimenSourceCode);
                        theObr.getObr15_SpecimenSource().getSpecimenSourceNameOrCode().getText().setValue(specimenSourceText);
                        theObr.getObr15_SpecimenSource().getSpecimenSourceNameOrCode().getNameOfCodingSystem().setValue(specimenSourceCodeSystem);
                    }
                }
            }

            // Mapping of the TR11561-8^Antibiotic Sensitivity

            /* This is a special case. If we have broken up a single ORDER_OBSERVATION for Blood Bank to create a group
             * of ORDER_OBSERVATIONS, then we may not yet know the specimen source before now for the automatically
             * generated ORDER_OBSERVATION group containing this specific OBR segment. In that case, we copy the
             * specimen source from the first ORDER_OBSERVATION if we have one.
             */
            if ("TR11561-8".equals(OBR4_1)) {
                if (isEmpty(theObr.getObr15_SpecimenSource().getSpecimenSourceNameOrCode().getIdentifier().getValue())) {
                    String specimenSourceCode = theFirstObr.getObr15_SpecimenSource().getSpecimenSourceNameOrCode().getIdentifier().getValue();
                    String specimenSourceText = theFirstObr.getObr15_SpecimenSource().getSpecimenSourceNameOrCode().getText().getValue();
                    String specimenSourceCodeSystem = theFirstObr.getObr15_SpecimenSource().getSpecimenSourceNameOrCode().getNameOfCodingSystem().getValue();
                    if (!isEmpty(specimenSourceCode)) {
                        theObr.getObr15_SpecimenSource().getSpecimenSourceNameOrCode().getIdentifier().setValue(specimenSourceCode);
                        theObr.getObr15_SpecimenSource().getSpecimenSourceNameOrCode().getText().setValue(specimenSourceText);
                        theObr.getObr15_SpecimenSource().getSpecimenSourceNameOrCode().getNameOfCodingSystem().setValue(specimenSourceCodeSystem);
                    }
                }
            } else if ("TR11561-8".equals(OBR4_1)) {
                if (isEmpty(theObr.getObr15_SpecimenSource().getSpecimenSourceNameOrCode().getIdentifier().getValue())) {
                    String specimenSourceCode = theFirstObr.getObr15_SpecimenSource().getSpecimenSourceNameOrCode().getIdentifier().getValue();
                    String specimenSourceText = theFirstObr.getObr15_SpecimenSource().getSpecimenSourceNameOrCode().getText().getValue();
                    String specimenSourceCodeSystem = theFirstObr.getObr15_SpecimenSource().getSpecimenSourceNameOrCode().getNameOfCodingSystem().getValue();
                    if (!isEmpty(specimenSourceCode)) {
                        theObr.getObr15_SpecimenSource().getSpecimenSourceNameOrCode().getIdentifier().setValue(specimenSourceCode);
                        theObr.getObr15_SpecimenSource().getSpecimenSourceNameOrCode().getText().setValue(specimenSourceText);
                        theObr.getObr15_SpecimenSource().getSpecimenSourceNameOrCode().getNameOfCodingSystem().setValue(specimenSourceCodeSystem);
                    }
                }
            }

            // Mapping of the TR11561-8^Antibody Screen
            /*
            CD mappedCode = lookupTerminologySingle( "OlisRequestCodes", OBR4_1, OBR4_3, "1.3.6.1.4.1.12201.1.1.1.1" );
            theObr.getObr15_SpecimenSource().getSpecimenSourceNameOrCode().getIdentifier().setValue( mappedCode.getCode() );
            theObr.getObr15_SpecimenSource().getSpecimenSourceNameOrCode().getText().setValue( mappedCode.getDisplayName() );
            theObr.getObr15_SpecimenSource().getSpecimenSourceNameOrCode().getNameOfCodingSystem().setValue( "HL70070" );
             */
        }
        // Map for OBR-26 Parent Result
        String microParentResult = theObr.getObr4_UniversalServiceIdentifier().getIdentifier().getValue();

        // OBR-7
        if (!isEmpty(theObr.getObr7_ObservationDateTime().getTime().getValue())) {
            theObr.getObr7_ObservationDateTime().getTime().setValue(getUTC(theObr.getObr7_ObservationDateTime().getTime().getValue()));
            if (!isEmpty(theObr.getObr14_SpecimenReceivedDateTime().getTime().getValue())) {
                theObr.getObr14_SpecimenReceivedDateTime().getTime().setValue(getUTC(theObr.getObr14_SpecimenReceivedDateTime().getTime().getValue()));
            }
        } else {
            theObr.getObr14_SpecimenReceivedDateTime().getTime().setValue("");
        }

        theObr.getObr18_ReferringLabUserReadableSpecimenIdentifier().parse("");
        theObr.getObr19_ReferringLabSpecimenBarCodeNumber().parse("");
        //theObr.getObr20_PerformingLabUserReadableSpecimenIdentifier().parse("");
        theObr.getObr21_FillerField2(0).parse("");

        // Map Parent Result for Micro 
        String OBR26_1 = theObr.getObr26_ParentResult().getParentObservationIdentifier().getIdentifier().getValue();
        String OBR26_3 = theObr.getObr26_ParentResult().getParentObservationIdentifier().getNameOfCodingSystem().getValue();
        if ("TR10695-5".equals(microParentResult)) {
            if (isEmpty(OBR26_1)) {
                sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.OBR-26-1", "No Parent Result - Code");
                return false;
            }
            if (isEmpty(OBR4_3)) {
                sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.OBR-26-3", "No Parent Result - Coding System");
                return false;
            }
            if (!"HL79902".equals(OBR26_3)) {
                CD mappedCode = lookupTerminologySingle("OlisResultCodes", OBR26_1, OBR26_3, "1.3.6.1.4.1.12201.1.1.1.3");
                if (mappedCode == null) {
                    String errorCode = CanonicalMessageUtils.createMessageErroredForV2MappingErrorCode("OBR", "26", null);
                    String errorMessage = CanonicalMessageUtils.createMessageErroredForV2MappingErrorMessage(getTerminologyService(), OBR26_1, OBR26_3, "1.3.6.1.4.1.12201.1.1.1.3");
                    sendToDeadLetterAndForward(theInput, errorCode, errorMessage);
                    return false;
                }
                theObr.getObr26_ParentResult().getParentObservationIdentifier().getIdentifier().setValue(mappedCode.getCode());
                theObr.getObr26_ParentResult().getParentObservationIdentifier().getText().setValue(mappedCode.getDisplayName());
                theObr.getObr26_ParentResult().getParentObservationIdentifier().getNameOfCodingSystem().setValue("HL79902");
                theObr.getObr26_ParentResult().getParentObservationSubIdentifier().setValue(theObr.getObr26_ParentResult().getParentObservationSubIdentifier().getValue());
            }
        } else {
            logger.fine("Non Positive Micro Result or Non Blood Group Screening Blood Bank Result");
        }

        theObr.getObr27_QuantityTiming().getStartDateTime().getTime().setValue(getUTC(theObr.getObr27_QuantityTiming().getStartDateTime().getTime().getValue()));

        // OBR28 Result Copies To
        for (int i1 = 0; i1 < theObr.getObr28_ResultCopiesTo().length; i1 += 1) {
            if (!isEmpty(theObr.getObr28_ResultCopiesTo(i1).getIDNumber().getValue()) && !("\"\"").equals(theObr.getObr28_ResultCopiesTo(i1).getIDNumber().getValue()) ) {
                String Obr_28 = theObr.getObr28_ResultCopiesTo(i1).getIDNumber().getValue();
                String obr28IdentifierType = theObr.getObr28_ResultCopiesTo(i1).getIdentifierTypeCode().getValue();
                String obr28Authority = theObr.getObr28_ResultCopiesTo(i1).getAssigningJurisdiction().getIdentifier().getValue();
                if (isEmpty(Obr_28)) {
                    sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.OBR-28", "No ResultCopiesTo provider");
                    return false;
                }
                if (isEmpty(obr28IdentifierType)) {
                    sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.OBR-28-13", "Missing identifier type for ID");
                    return false;
                }
                if (isEmpty(obr28Authority)) {
                    obr28Authority = theObr.getObr28_ResultCopiesTo(i1).getAssigningAuthority().getNamespaceID().getValue();
                }
                if (isEmpty(obr28Authority)) {
                    sendToDeadLetterAndForward(theInput, "INCOMPLETE_MESSAGE.V2.OBR-28-9", "OBR-28-9 requires either assigning authority or jurisdiction");
                    return false;
                }
             
                pif = lookupProvider(obr28IdentifierType, obr28Authority, Obr_28, mapClassifier);
                if (pif.getProviderIfFound() == null) {
                    sendToDeadLetterAndForward(theInput, CanonicalMessageUtils.createMessageErroredForV2ProviderMappingError(getClass(), "OBR", "28", null, myProviderClassifierToIdRoot.get(identifierType), authority, Obr_28));
                    return false;
                }
                String resultCopiesProviderId = pif.getProviderIfFound().getIdExtension();
                String resultCopiesProviderLname = pif.getProviderIfFound().getLastName();
                String resultCopiesProviderFname = pif.getProviderIfFound().getFirstName();
                String resultCopiesProviderMname = pif.getProviderIfFound().getMiddleName();
                String resultCopiesProviderIdRoot = pif.getProviderIfFound().getIdRoot();
                String resultCopiesProviderIdAuthority = pif.getProviderIfFound().getIdAuthority();


                theObr.getObr28_ResultCopiesTo(i1).getIDNumber().setValue(resultCopiesProviderId);
                theObr.getObr28_ResultCopiesTo(i1).getFamilyName().getSurname().setValue(resultCopiesProviderLname);
                theObr.getObr28_ResultCopiesTo(i1).getGivenName().setValue(resultCopiesProviderFname);
                theObr.getObr28_ResultCopiesTo(i1).getSecondAndFurtherGivenNamesOrInitialsThereof().setValue(resultCopiesProviderMname);
                theObr.getObr28_ResultCopiesTo(i1).getIdentifierTypeCode().setValue(myIdRootToProviderClassifier.get(resultCopiesProviderIdRoot));
                theObr.getObr28_ResultCopiesTo(i1).getAssigningAuthority().getNamespaceID().setValue("");
                theObr.getObr28_ResultCopiesTo(i1).getAssigningJurisdiction().getIdentifier().setValue(resultCopiesProviderIdAuthority);
                theObr.getObr28_ResultCopiesTo(i1).getAssigningJurisdiction().getText().setValue(myIdAuthorityToProviderClassifier.get(resultCopiesProviderIdAuthority));
                theObr.getObr28_ResultCopiesTo(i1).getAssigningJurisdiction().getNameOfCodingSystem().setValue("HL70347");
            } else {
                logger.fine("+++ OBR-28 is null ");
            }
        }
        return true;
    }

    public boolean processZBR(CanonicalHl7V2Message theInput, hapi.on.olis.oru_r01.segment.ZBR theZbr)
            throws Exception {

        // ZBR-2 - Test Request Placer
        String id = theZbr.getZbr2_TestRequestPlacer().getAssigningAuthority().getUniversalID().getValue();
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("ZBR-2 UniversalID:" + id);
        }
        if (!isEmpty(id)) {
            String namespace = theZbr.getZbr2_TestRequestPlacer().getAssigningAuthority().getNamespaceID().getValue();
            if (isEmpty(namespace)) {
                namespace = "ON_OLIS";
            }
            Facility fif = lookupFacility(namespace, id);
            if (fif == null) {
                sendToDeadLetterAndForward(theInput, "INCORRECT_MESSAGE.V2.ZBR-2", "Unknown facility with ID " + id + " and namespace " + namespace);
                return false;
            } else {
                theZbr.getZbr2_TestRequestPlacer().getOrganizationName().setValue(fif.getOrgName());
                theZbr.getZbr2_TestRequestPlacer().getAssigningAuthority().getUniversalID().setValue(fif.getId().getRoot() + ":" + fif.getId().getExt());
                theZbr.getZbr2_TestRequestPlacer().getAssigningAuthority().getNamespaceID().setValue("");
                theZbr.getZbr2_TestRequestPlacer().getAssigningAuthority().getUniversalIDType().setValue("ISO");
            }
        }

        // ZBR-3 - Specimen Collector
        id = theZbr.getZbr3_SpecimenCollector().getAssigningAuthority().getUniversalID().getValue();
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("ZBR-3 UniversalID:" + id);
        }
        if (!isEmpty(id)) {
            String namespace = theZbr.getZbr3_SpecimenCollector().getAssigningAuthority().getNamespaceID().getValue();
            if (isEmpty(namespace)) {
                namespace = "ON_OLIS";
            }

            Facility fif = lookupFacility(namespace, id);
            if (fif == null) {
                sendToDeadLetterAndForward(theInput, "INCORRECT_MESSAGE.V2.ZBR-3", "Unknown facility with ID " + id + " and namespace " + namespace);
                return false;
            }
            theZbr.getZbr3_SpecimenCollector().getOrganizationName().setValue(fif.getOrgName());
            theZbr.getZbr3_SpecimenCollector().getAssigningAuthority().getUniversalID().setValue(fif.getId().getRoot() + ":" + fif.getId().getExt());
            theZbr.getZbr3_SpecimenCollector().getAssigningAuthority().getNamespaceID().setValue("");
            theZbr.getZbr3_SpecimenCollector().getAssigningAuthority().getUniversalIDType().setValue("ISO");
        }

        id = theZbr.getZbr4_ReportingLaboratory().getAssigningAuthority().getUniversalID().getValue();
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("ZBR-4 UniversalID:" + id);
        }
        if (!isEmpty(id)) {
            String namespace = theZbr.getZbr4_ReportingLaboratory().getAssigningAuthority().getNamespaceID().getValue();
            if (isEmpty(namespace)) {
                namespace = "ON_OLIS";
            }
            Facility fif = lookupFacility(namespace, id);
            if (fif == null) {
                sendToDeadLetterAndForward(theInput, "INCORRECT_MESSAGE.V2.ZBR-4", "Unknown facility with ID " + id + " and namespace " + namespace);
                return false;
            }
            theZbr.getZbr4_ReportingLaboratory().getOrganizationName().setValue(fif.getOrgName());
            theZbr.getZbr4_ReportingLaboratory().getAssigningAuthority().getUniversalID().setValue(fif.getId().getRoot() + ":" + fif.getId().getExt());
            theZbr.getZbr4_ReportingLaboratory().getAssigningAuthority().getNamespaceID().setValue("");
            theZbr.getZbr4_ReportingLaboratory().getAssigningAuthority().getUniversalIDType().setValue("ISO");
            theZbr.getZbr5_ReportingLaboratoryAddress().getAddressType().setValue("B");
            theZbr.getZbr5_ReportingLaboratoryAddress().getCountry().setValue("CAN");
            theZbr.getZbr5_ReportingLaboratoryAddress().getStreetAddress().getStreetOrMailingAddress().setValue(fif.getAddress().getAddress1());
            theZbr.getZbr5_ReportingLaboratoryAddress().getOtherDesignation().setValue(fif.getAddress().getAddress2());
            theZbr.getZbr5_ReportingLaboratoryAddress().getCity().setValue(fif.getAddress().getCity());
            theZbr.getZbr5_ReportingLaboratoryAddress().getStateOrProvince().setValue(fif.getAddress().getProvince());
            theZbr.getZbr5_ReportingLaboratoryAddress().getZipOrPostalCode().setValue(fif.getAddress().getPostalCode());
        }

        // ZBR-6, ZBR-7 - Performing Laboratory and Performing Laboratory Address
        id = theZbr.getZbr6_PerformingLaboratory().getAssigningAuthority().getUniversalID().getValue();
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("ZBR-6 UniversalID:" + id);
        }
        if (!isEmpty(id)) {
            String namespace = theZbr.getZbr6_PerformingLaboratory().getAssigningAuthority().getNamespaceID().getValue();
            if (isEmpty(namespace)) {
                namespace = "ON_OLIS";
            }
            Facility fif = lookupFacility(namespace, id);
            if (fif == null) {
                sendToDeadLetterAndForward(theInput, "INCORRECT_MESSAGE.V2.ZBR-6", "Unknown facility with ID " + id + " and namespace " + namespace);
                return false;
            }

            theZbr.getZbr6_PerformingLaboratory().getOrganizationName().setValue(fif.getOrgName());
            theZbr.getZbr6_PerformingLaboratory().getAssigningAuthority().getUniversalID().setValue(fif.getId().getRoot() + ":" + fif.getId().getExt());
            theZbr.getZbr6_PerformingLaboratory().getAssigningAuthority().getNamespaceID().setValue("");
            theZbr.getZbr6_PerformingLaboratory().getAssigningAuthority().getUniversalIDType().setValue("ISO");
            theZbr.getZbr7_PerformingLaboratoryAddress().getAddressType().setValue("B");
            theZbr.getZbr7_PerformingLaboratoryAddress().getCountry().setValue("CAN");
            theZbr.getZbr7_PerformingLaboratoryAddress().getStreetAddress().getStreetOrMailingAddress().setValue(fif.getAddress().getAddress1());
            theZbr.getZbr7_PerformingLaboratoryAddress().getOtherDesignation().setValue(fif.getAddress().getAddress2());
            theZbr.getZbr7_PerformingLaboratoryAddress().getCity().setValue(fif.getAddress().getCity());
            theZbr.getZbr7_PerformingLaboratoryAddress().getStateOrProvince().setValue(fif.getAddress().getProvince());
            theZbr.getZbr7_PerformingLaboratoryAddress().getZipOrPostalCode().setValue(fif.getAddress().getPostalCode());

        }

        // ZBR-8 - Destination Laboratory
        id = theZbr.getZbr8_DestinationLaboratory().getAssigningAuthority().getUniversalID().getValue();
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("ZBR-8 UniversalID:" + id);
        }
        if (!isEmpty(id)) {
            String namespace = theZbr.getZbr8_DestinationLaboratory().getAssigningAuthority().getNamespaceID().getValue();
            if (isEmpty(namespace)) {
                namespace = "ON_OLIS";
            }

            Facility fif = lookupFacility(namespace, id);
            if (fif == null) {
                sendToDeadLetterAndForward(theInput, "INCORRECT_MESSAGE.V2.ZBR-8", "Unknown facility with ID " + id + " and namespace " + namespace);
                return false;
            }
            theZbr.getZbr8_DestinationLaboratory().getOrganizationName().setValue(fif.getOrgName());
            theZbr.getZbr8_DestinationLaboratory().getAssigningAuthority().getUniversalID().setValue(fif.getId().getRoot() + ":" + fif.getId().getExt());
            theZbr.getZbr8_DestinationLaboratory().getAssigningAuthority().getNamespaceID().setValue("");
            theZbr.getZbr8_DestinationLaboratory().getAssigningAuthority().getUniversalIDType().setValue("ISO");
        }

        return true;
    }

    @Override
    public void forward(Source theOutput) throws Exception {
        sepJMSOutPortType.sendSynchInOnly(theOutput);
    }

    private void processMessage(CanonicalHl7V2Message input) throws HL7Exception, Exception {
        logger.info("+++ Beginning mapping message with ID '" + input.getMessageControlID() + "' from intermediate to OLIS format ");
        long startTime = System.currentTimeMillis();

        ORU_R01 OlisORU = new hapi.on.olis.oru_r01.message.ORU_R01();
        boolean unmarshalled = unmarshallHapiMessage(input, OlisORU);
        if (!unmarshalled) {
            return;
        }

        processMessage(input, OlisORU);

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("+++ Mapped message is: " + input.getRawMessage().replaceAll("\\r", "\n"));
        }

        marshallIntermediateAndForward(input, OlisORU, "ON", "OLIS", "ORU");
        long processingTime = System.currentTimeMillis() - startTime;

        logger.info("+++ Finished mapping from intermediate to OLIS format in " + processingTime + " ms.");
    }
    @ConsumerEndpoint(serviceQN = "{http://j2ee.netbeans.org/wsdl/prj_Map_ORUint_ON_OLIS_ORU_ON_OLIS_Pojo/toJMS}epJMSOutPortTypService", interfaceQN = "{http://j2ee.netbeans.org/wsdl/prj_Map_ORUint_ON_OLIS_ORU_ON_OLIS_Pojo/toJMS}JMSOutPortType", name = "epJMSOutPortTyp", operationQN = "{http://j2ee.netbeans.org/wsdl/prj_Map_ORUint_ON_OLIS_ORU_ON_OLIS_Pojo/toJMS}JMSOutOperation", inMessageTypeQN = "{http://j2ee.netbeans.org/wsdl/prj_Map_ORUint_ON_OLIS_ORU_ON_OLIS_Pojo/toJMS}JMSInputMessage")
    private Consumer sepJMSOutPortType;

    public static void main(String[] args) throws HL7Exception, Exception {
        logger.setLevel(Level.FINEST);
        Logger.getLogger("").getHandlers()[0].setLevel(Level.FINEST);

        String implMessage =
                //"MSH|^~\\&|^CN=LIS.NORYO2.PRODTEST, OU=Applications, OU=eHealthUsers, OU=Subscribers, DC=subscribers, DC=ssh^X500|NYGcGTA|^OLIS^X500||20110713150549||ORU^R01^ORU_R01|20111940000077|T|2.3.1||||||8859/1\r" +
                //"PID|1||2000010351^^^^JHN^^^^ON&Ontario&HL70347||HEMLOCK^NICOLA^^^^^U||19540516|F|||26 MAPLE DR^^PETERBOROUGH^ON^H6T 5G3^CAN^H\r" +
                "MSH|^~\\&|^CN=LIS.SUNWO3.PRODTEST, OU=Applications, OU=eHealthUsers, OU=Subscribers, DC=subscribers, DC=ssh^X500|SUNcGTA|^OLIS^X500||20120229155601||ORU^R01^ORU_R01|20120229035601|C|2.3.1||||||8859/1\r" +
                "PID|1||2222222223^^^^JHN^^^^ON&Ontario&HL70347^^CC||ASPEN^ALFREDO^POPULUS^^^^U||19940516|M|||15 BEET WAY^^TORONTO^ON^M9K 2E3^CAN^H||^PRN^PH^^^416^4455220\r" +
                "NTE|1||Testing PatientNote\r" +
                "PV1|1|I|5N||||00010^BLAKE^DONALD^^^^^^2.16.840.1.113883.3.59.1:4047^^^^EI||||||||||00010^BLAKE^DONALD^^^^^^2.16.840.1.113883.3.59.1:4047^^^^EI\r" +
                "ORC||||W3659223L65^ON_OLIS^2.16.840.1.113883.3.59.1:4047|||||20110713150549||||||||||||^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4047\r" +
                "OBR|1|131391923^ON_OLIS^2.16.840.1.113883.3.59.1:4047|131391923^ON_OLIS^2.16.840.1.113883.3.59.1:4047|RAM^URINE R^1.3.6.1.4.1.12201.1.1.1.33|||201107131000|||||||201107131300||00010^BLAKE^DONALD^^^^^^2.16.840.1.113883.3.59.1:4047^^^^EI||||W3659|W3659223L65||||F||1^^^201107131300^^R|00015^TAKAHAMA^HALLIE^^^^^^2.16.840.1.113883.3.59.1:4047^^^^EI\r" +
                "ZBR||2.16.840.1.113883.3.59.1:4047^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4047|2.16.840.1.113883.3.59.1:4047^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4047|2.16.840.1.113883.3.59.1:4047^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4047||2.16.840.1.113883.3.59.1:4047^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4047|||||RM_RAM\r" +
                "OBX|1|SN|LEUK^URINE LEUK^1.3.6.1.4.1.12201.1.1.1.34||^3^+||NEG|A|||F\r" +
                "ZBX|20110713150549|0001\r" +
                "OBX|2|NM|UPH^URINE PH^1.3.6.1.4.1.12201.1.1.1.34||7.5||5.0-7.5||||F\r" +
                "ZBX|20110713150549|0002\r" +
                "OBX|3|ST|UPR^URINE PROTEIN^1.3.6.1.4.1.12201.1.1.1.34||NEG||NEG||||F\r" +
                "ZBX|20110713150549|0003\r" +
                "OBX|4|ST|UGL^URINE GLUCOSE^1.3.6.1.4.1.12201.1.1.1.34||NEG||NEG||||F\r" +
                "ZBX|20110713150549|0004\r" +
                "OBX|5|ST|UKET^URINE KETONES^1.3.6.1.4.1.12201.1.1.1.34||NEG||||||F\r" +
                "ZBX|20110713150549|0005\r" +
                "OBX|6|NM|UROB^URINE UROBILINOGEN^1.3.6.1.4.1.12201.1.1.1.34||3.2|UMOL/L|3.2-17.0||||F\r" +
                "ZBX|20110713150549|0006\r" +
                "OBX|7|ST|UBIL^URINE BILIRUBIN^1.3.6.1.4.1.12201.1.1.1.34||NEG||NEG||||F\r" +
                "ZBX|20110713150549|0007\r" +
                "OBX|8|NM|UBL^URINE BLOOD^1.3.6.1.4.1.12201.1.1.1.34||10||NEG||||F\r" +
                "ZBX|20110713150549|0008\r" +
                "OBX|9|ST|NITR^NITRITES^1.3.6.1.4.1.12201.1.1.1.34||NEG||NEG||||F\r" +
                "ZBX|20110713150549|0009\r" +
                "OBX|10|NM|SPG^SPECIFIC GRAVITY^1.3.6.1.4.1.12201.1.1.1.34||1.013||1.005-1.035||||F\r" +
                "ZBX|20110713150549|0010\r" +
                "OBX|11|SN|URBC^URINE RBC'S^1.3.6.1.4.1.12201.1.1.1.34||^1^-^3|/HPF|<5||||F\r" +
                "ZBX|20110713150549|0011\r" +
                "OBX|12|SN|UWBC^URINE WBC'S^1.3.6.1.4.1.12201.1.1.1.34||^3^-^5|/HPF|<8||||F\r" +
                "ZBX|20110713150549|0012\r" +
                "OBX|13|ST|CAST^CASTS^1.3.6.1.4.1.12201.1.1.1.34||NONE|/LPF|||||F\r" +
                "ZBX|20110713150549|0013\r" +
                "OBX|14|ST|EPI^EPITHELIAL CELLS^1.3.6.1.4.1.12201.1.1.1.34||NONE|/HPF|||||F\r" +
                "ZBX|20110713150549|0014\r" +
                "OBX|15|ST|BCT^BACTERIA^1.3.6.1.4.1.12201.1.1.1.34||NONE|/HPF|||||F\r" +
                "ZBX|20110713150549|0015\r" +
                "BLG|||MOHLTC\r";

        String implMicroMessage =
                "MSH|^~\\&|^CN=LIS.NORYO2.PRODTEST, OU=Applications, OU=eHealthUsers, OU=Subscribers, DC=subscribers, DC=ssh^X500|NYGcGTA|^OLIS^X500||20110901150522||ORU^R01^ORU_R01|20112440000029|T|2.3.1||||||8859/1\r" +
                "PID|1||2000010328^^^^JHN^^^^ON^^BE||FIR^OLE^CONCOLOR^^^^U||19700910|M|||45 MAIN ST^^UXBRIDGE^ON^J7H 6T5^CAN^H||^PRN^PH^^1^905^7896541\r" +
                "PV1|1|I|4W||||||||||||||\r" +
                "ORC||||H5391225L3^ON_OLIS^2.16.840.1.113883.3.59.1:4047|||||20110901150522||||||||||||^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4047\r" +
                "OBR|1|131617501^ON_OLIS^2.16.840.1.113883.3.59.1:4047|131617501^ON_OLIS^2.16.840.1.113883.3.59.1:4047|WNDC^WOUND CULTURE^1.3.6.1.4.1.12201.1.1.1.33|||201109010700|||||||201109011050||00010^BLAKE^DONALD^^^^^^2.16.840.1.113883.3.59.1:4047^^^^EI||||H5391|||||F||1^^^201109011050^^R|00015^TAKAHAMA^HALLIE^^^^^^2.16.840.1.113883.3.59.1:4047^^^^EI\r" +
                "ZBR||2.16.840.1.113883.3.59.1:4047^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4047|2.16.840.1.113883.3.59.1:4047^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4047|2.16.840.1.113883.3.59.1:4047^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4047||2.16.840.1.113883.3.59.1:4047^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4047|||||MA_WNDC\r" +
                "OBX|1|TX|SDES^WOUND CULTURE^1.3.6.1.4.1.12201.1.1.1.34||SPECIMEN DESCRIPTION: WOUND||||||F\r" +
                "ZBX|20110901150522|0001\r" +
                "OBX|2|TX|SREQ^WOUND CULTURE^1.3.6.1.4.1.12201.1.1.1.34||ADDITIONAL INFO.: ABDOMEN||||||F\r" +
                "ZBX|20110901150522|0002\r" +
                "OBX|3|TX|GS^WOUND CULTURE^1.3.6.1.4.1.12201.1.1.1.34|1|GRAM SMEAR: MODERATE PUS CELLS||||||F\r" +
                "ZBX|20110901150522|0003\r" +
                "OBX|4|TX|GS^WOUND CULTURE^1.3.6.1.4.1.12201.1.1.1.34|2|GRAM SMEAR: FEW EPITHELIAL CELLS||||||F\r" +
                "ZBX|20110901150522|0004\r" +
                "OBX|5|TX|GS^WOUND CULTURE^1.3.6.1.4.1.12201.1.1.1.34|3|GRAM SMEAR: MODERATE GRAM NEGATIVE BACILLI||||||F\r" +
                "ZBX|20110901150522|0005\r" +
                "OBX|6|TX|GS^WOUND CULTURE^1.3.6.1.4.1.12201.1.1.1.34|4|GRAM SMEAR: MODERATE GRAM POSITIVE COCCI||||||F\r" +
                "ZBX|20110901150522|0006\r" +
                "OBX|7|TX|GS^WOUND CULTURE^1.3.6.1.4.1.12201.1.1.1.34|5|GRAM SMEAR: TESTING PERFORMED AT THE SHARED HOSPITAL LAB.   555 FINCH AVE. W  TORONTO, ONTARIO||||||F\r" +
                "ZBX|20110901150522|0007\r" +
                "OBX|8|TX|GS^WOUND CULTURE^1.3.6.1.4.1.12201.1.1.1.34|6|GRAM SMEAR:  M2R 1N5||||||F\r" +
                "ZBX|20110901150522|0008\r" +
                "OBX|9|TX|CULT^WOUND CULTURE^1.3.6.1.4.1.12201.1.1.1.34|1|CULTURE: MODERATE GROWTH OF ESCHERICHIA COLI||||||F\r" +
                "ZBX|20110901150522|0009\r" +
                "OBX|10|TX|CULT^WOUND CULTURE^1.3.6.1.4.1.12201.1.1.1.34|2|CULTURE: MODERATE GROWTH OF GROUP B STREPTOCOCCUS,EMPIRICALLY SENSITIVE TO PENICILLIN.||||||F\r" +
                "ZBX|20110901150522|0010\r" +
                "OBX|11|TX|CULT^WOUND CULTURE^1.3.6.1.4.1.12201.1.1.1.34|3|CULTURE: TESTING PERFORMED AT THE SHARED HOSPITAL LAB.   555 FINCH AVE. W  TORONTO, ONTARIO||||||F\r" +
                "ZBX|20110901150522|0011\r" +
                "OBX|12|TX|CULT^WOUND CULTURE^1.3.6.1.4.1.12201.1.1.1.34|4|CULTURE:  M2R 1N5||||||F\r" +
                "ZBX|20110901150522|0012\r" +
                "OBX|13|TX|RPT^WOUND CULTURE^1.3.6.1.4.1.12201.1.1.1.34||REPORT STATUS: FINAL 01/Sep/2011||||||F\r" +
                "ZBX|20110901150522|0013\r" +
                "OBX|14|CE|ORGANISM^ZZ00^1.3.6.1.4.1.12201.1.1.1.34||ECOL^^1.3.6.1.4.1.12201.1.1.1.75||||||F\r" +
                "ZBX|20110901150522|0014\r" +
                "BLG|||MOHLTC\r" +
                "ORC||||H5391225L3^ON_OLIS^2.16.840.1.113883.3.59.1:4047|||||20110901150522||||||||||||^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4047\r" +
                "OBR|2|131617501SEN2^ON_OLIS^2.16.840.1.113883.3.59.1:4047|131617501SEN2^ON_OLIS^2.16.840.1.113883.3.59.1:4047|MIC^METHOD MIC^1.3.6.1.4.1.12201.1.1.1.33|||201109010700|||||||201109011050||00010^BLAKE^DONALD^^^^^^2.16.840.1.113883.3.59.1:4047^^^^EI||||H5391|||||F|ORGANISM&&1.3.6.1.4.1.12201.1.1.1.34^1|1^^^201109011050^^R|00015^TAKAHAMA^HALLIE^^^^^^2.16.840.1.113883.3.59.1:4047^^^^EI|131617501&&2.16.840.1.113883.3.59.1:4047&ISO^131617501&&2.16.840.1.113883.3.59.1:4047&ISO\r" +
                "ZBR||2.16.840.1.113883.3.59.1:4047^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4047|2.16.840.1.113883.3.59.1:4047^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4047|2.16.840.1.113883.3.59.1:4047^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4047||2.16.840.1.113883.3.59.1:4047^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4047|||||MA_MIC\r" +
                "OBX|1|SN|AM^AMPICILLIN^1.3.6.1.4.1.12201.1.1.1.76||>=^32|||R|||F\r" +
                "ZBX|20110901150522|0001\r" +
                "OBX|2|SN|CFZ^CEFAZOLIN^1.3.6.1.4.1.12201.1.1.1.76||<=^4|||S|||F\r" +
                "ZBX|20110901150522|0002\r" +
                "OBX|3|SN|GM^GENTAMICIN^1.3.6.1.4.1.12201.1.1.1.76||<=^1|||S|||F\r" +
                "ZBX|20110901150522|0003\r" +
                "OBX|4|SN|TS^COTRIMOXAZOLE^1.3.6.1.4.1.12201.1.1.1.76||<=^10|||S|||F\r" +
                "ZBX|20110901150522|0004\r" +
                "BLG|||MOHLTC\r";




        String implPanelMessage =
                "MSH|^~\\&|^CN=LIS.NORYO2.PRODTEST, OU=Applications, OU=eHealthUsers, OU=Subscribers, DC=subscribers, DC=ssh^X500|NYGcGTA|^OLIS^X500||20111007131013||ORU^R01^ORU_R01|20112800000089|T|2.3.1||||||8859/1\r" +
                "PID|1||2000010013^^^^JHN^^^^ON&Ontario&HL70347^^AA||ASH^GREEN^LYNN^^^^U||19701210|F|||200 MAIN ST^^TORONTO^ON^N7U 8I8^CAN^H\r" +
                "PV1|1|I|6SE1||||00010^BLAKE^DONALD^^^^^^2.16.840.1.113883.3.59.1:4047^^^^EI||||||||||00010^BLAKE^DONALD^^^^^^2.16.840.1.113883.3.59.1:4047^^^^EI\r" +
                "ORC||||W79363226L23^ON_OLIS^2.16.840.1.113883.3.59.1:4047|||||20111007131013||||||||||||^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4047\r" +
                "OBR|1|131766356^ON_OLIS^2.16.840.1.113883.3.59.1:4047|131766356^ON_OLIS^2.16.840.1.113883.3.59.1:4047|ENZ^CARDIAC ENZYMES^1.3.6.1.4.1.12201.1.1.1.33|||201110070600|||||||201110070832||00010^BLAKE^DONALD^^^^^^2.16.840.1.113883.3.59.1:4047^^^^EI||||W79363|W79363226L23||||F||1^^^201110070832^^R|00015^TAKAHAMA^HALLIE^^^^^^2.16.840.1.113883.3.59.1:4047^^^^EI\r" +
                "ZBR||2.16.840.1.113883.3.59.1:4047^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4047|2.16.840.1.113883.3.59.1:4047^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4047|2.16.840.1.113883.3.59.1:4047^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4047||2.16.840.1.113883.3.59.1:4047^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4047|||CD_ENZ||0001\r" +
                "OBX|1|NM|CK^CK^1.3.6.1.4.1.12201.1.1.1.34||7|U/L|6-170||||F\r" +
                "ZBX|20111007131013|0001\r" +
                "OBX|2|SN|MTROT^TROPONIN T^1.3.6.1.4.1.12201.1.1.1.34||<^0.003|UG/L|<0.031||||F\r" +
                "ZBX|20111007131013|0002\r" +
                "NTE|1||99TH PERCENTILE OF NORMAL = 0.014 ug/L\\.br\\SUGGESTED ACUTE MI CUTOFF > OR = 0.1 ug/L\\.br\\\r" +
                "BLG|||MOHLTC\r";

        String biochemSMHMessage =
                "MSH|^~\\&|^CN=LIS.MICHO2.PRODTEST, OU=Applications, OU=eHealthUsers, OU=Subscribers, DC=subscribers, DC=ssh^X500|StMcGTA|^OLIS^X500||201004211412||ORU^R01^ORU_R01|00676420|T|2.3.1||||||8859/1\r" +
                "PID|1||2000015822^^^^JHN^^^^ON&Ontario&HL70347^^GN~4003549^^^&2.16.840.1.113883.3.59.1:4083^MR||AAFMSILVER^RINGO^R^^^^U||19481030|M|||99 NICKLE RD^^TORONTO^ON^M9I 8U7^CAN^H||^PRN^PH^^^555^7778888\r" +
                "PV1|1|I|16CN||||99994^^^^^^^^2.16.840.1.113883.3.59.1:4083^^^^EI\r" +
                "ORC||||E2190001^ON_OLIS^2.16.840.1.113883.3.59.1:4083|||||201004191255||||||||||||^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4083\r" +
                "OBR|1|10633753^ON_OLIS^2.16.840.1.113883.3.59.1:4083|2^ON_OLIS^2.16.840.1.113883.3.59.1:4083|ALB^Digoxin^1.3.6.1.4.1.12201.1.1.1.37|||201004191300|||||||201004191300||99994^^^^^^^^2.16.840.1.113883.3.59.1:4083^^^^EI|||||||||C||1^^^201004191500^^T|99994^^^^^^^^2.16.840.1.113883.3.59.1:4083^^^^EI~99994^^^^^^^^2.16.840.1.113883.3.59.1:4083^^^^EI\r" +
                "ZBR||2.16.840.1.113883.3.59.1:4083^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4083|2.16.840.1.113883.3.59.1:4083^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4083|2.16.840.1.113883.3.59.1:4083^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4083||2.16.840.1.113883.3.59.1:4083^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4083|||||0001\r" +
                "OBX|1|SN|ALB^Digoxin^1.3.6.1.4.1.12201.1.1.1.38|2|^3.0^+^4.0|nmol/L|1.0-2.6||||C\r" +
                "ZBX|201004211412|0001\r" +
                "OBX|2||ALB^Digoxin^1.3.6.1.4.1.12201.1.1.1.38|2|||||||N\r" +
                "ZBX|201004211412|0002\r" +
                "NTE|1||2\\.br\\\r" +
                "BLG|||MOHLTC";
        
        String pathMessage =
                "MSH|^~\\&|^CN=LIS.SOUREG.PRODTEST, OU=Applications, OU=eHealthUsers, OU=Subscribers, DC=subscribers, DC=ssh^X500|SolcGTA|^OLIS^X500||20110216120000||ORU^R01^ORU_R01|1317|T|2.3.1||||||8859/1\r" + 
		"PID|1||1000323823^^^^JHN^^^^ON&Ontario&HL70347^^IV||STORM^SUSAN^S^^^^U||19870218|F|||||^PRN^PH^^^905^5896541\r" + 
//		"PID|1||1000323823^^^^JHN^^^^ON&Ontario&HL70347^^IV||STORM^SUSAN^S^^^^U||19870218|F|||123 ANYWHERE AVENUE^^NEWMARKET^ON^L3Y 2P9^CAN^H||^PRN^PH^^^905^5896541\r" + 
                "PV1|1|O|Medicine General||||4061^Roberts^David^^^^^^ON^^^^MDL||||||||||4061^Roberts^David^^^^^^ON^^^^MDL\r" + 
		"ORC||||YS11-33^ON_OLIS^2.16.840.1.113883.3.59.1:4061|||||201102150000||||||||||||^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4061\r" + 
		"OBR|1|YS11-33^ON_OLIS^2.16.840.1.113883.3.59.1:4061|YS11-33^ON_OLIS^2.16.840.1.113883.3.59.1:4061|11529-5^Surgical Pathology Study Report ^1.3.6.1.4.1.12201.1.1.1.82|||201102150000|||||||201102161127|TISSUE^^^modifier|4061^Roberts^David^^^^^^ON^^^^MDL||||N|||||F||1^^^201102150000^^R|4061^Roberts^David^^^^^^ON^^^^MDL\r" + 
		"ZBR||2.16.840.1.113883.3.59.1:4061^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4061|2.16.840.1.113883.3.59.1:4061^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4061|2.16.840.1.113883.3.59.1:4061^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4061||2.16.840.1.113883.3.59.1:4061^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4061|||||0001\r" + 
//		"OBX|1|FT|22633-2^Path report.site of origin^1.3.6.1.4.1.12201.1.1.1.83||A. Salivary gland, biopsy||||||F\r" + 
//		"ZBX|20110216120000|0001\r" + 
//		"OBX|2|FT|22634-0^Path report.gross description^1.3.6.1.4.1.12201.1.1.1.83|1| Salivary gland from mandible, received in formalin. Submitted in toto ||||||F\r" + 
//		"ZBX|20110216120000|0002\r" + 
//		"OBX|3|FT|22636-5^Path report.relevant Hx^1.3.6.1.4.1.12201.1.1.1.83||Bump on jaw ||||||F\r" + 
//		"ZBX|20110216120000|0003\r" + 
//		"OBX|4|FT|22637-3^Path report.final diagnosis^1.3.6.1.4.1.12201.1.1.1.83||Salivary gland, biopsy:   -EXCISIONAL BIOPSY -SINGLE FOCUS OF ADENOID CYSTIC CARCINOMA  ||||||F\r" + 
//		"ZBX|20110216120000|0004\r" + 
//		"OBX|5|FT|22635-7^Path report.microscopic observation^1.3.6.1.4.1.12201.1.1.1.83|| Salivary gland, biopsy:       Specimen:                            Submandibular gland Received:                            In formalin Procedure:                           Excisional biopsy Specimen Size:                       Greatest dimension:  0.75 cm                                      Additional dimension:  0.02 cm                                      Additional dimension:  0.02 cm Specimen Laterality:                 Right Tumor Site:                          Submandibular gland Tumor Focality:                      Single focus Tumor Size:                          Greatest dimension: 0.01 cm Histologic Type:                     Adenoid cystic carcinoma Histologic Grade:                    Not applicable Margins:                             Cannot be assessed Lymph-Vascular Invasion:             Not identified Perineural Invasion:                 Not identified Lymph Nodes, Extranodal Extension:      Not identified Pathologic Staging (pTNM):       TNM Descriptors:                     Not applicable Primary Tumor (pT):                  pTX: Cannot be assessed Regional Lymph Nodes (pN):           pNX:  Regional lymph nodes cannot be assessed                                      Number of regional lymph nodes examined: 2                                      Number of regional lymph nodes involved: 0 Distant Metastasis (pM):             Not applicable Pathologic Staging is based on AJCC/UICC TNM, 7th Edition. --------------------------------------------------------  ||||||F\r" + 
//		"ZBX|20110216120000|0005\r" + 
//		"OBX|6|FT|22634-0^Path report.gross description^1.3.6.1.4.1.12201.1.1.1.83|2|\\.br\\6 slides, needle rinse in cytolyte received.\\.br\\\\.br\\Count Material Prepared\\.br\\ 0 \\.in +5\\\\.ti +5\\ ThinPrep Slide(s)\\.br\\ 6 \\.ti +5\\ Smear(s)\\.br\\ 2 \\.ti +5\\ Cytospin Slide(s)\\.br\\ 1 \\.ti +5\\ Cell Block(s)\\.br\\ \\.br\\\\.ti -5\\\\.in -5\\||||||F\r" + 
//		"ZBX|20110216120000|0006\r" + 
		"OBX|7|ST|PN^Pathologist Name^1.3.6.1.4.1.12201.1.1.1.83||Pathologist, Lab||||||F\r" + 
		"ZBX|20110216120000|0007\r" + 
		"BLG|||MOHLTC";

        String sjhcMicroMessage =
            "MSH|^~\\&|^CN=LIS.JOSHE13.CST, OU=Applications, OU=eHealthUsers, OU=Subscribers, DC=subscribers, DC=ssh^X500|SJHCcGTA|^OLIS^X500||201204161354||ORU^R01^ORU_R01|00030904|C|2.3.1||||||8859/1\r" +
            "PID|1||^^^^JHN^^^^ON&Ontario&HL70347||SCC INTERFACE^MALEADULT^TWO^^^^U||19680823|M\r" +
            "ZPD||Y\r" +
            "NTE|1||SCHIZO DISORDER.\\.br\\SCHIZO DISORDER OLIS TESTING\r" +
            "PV1|1|O|4G||||204917^KARANICOLAS^STAVROS^^^^^^2.16.840.1.113883.3.59.3:0898^^^^EI||||||||||204917^KARANICOLAS^STAVROS^^^^^^2.16.840.1.113883.3.59.3:0898^^^^EI\r" +
            "ORC||||G0160036^ON_OLIS^2.16.840.1.113883.3.59.1:4145|||||201204161343||||||||||||^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4145\r" +
            "OBR|1|G0160036CXDWD^ON_OLIS^2.16.840.1.113883.3.59.1:4145|G0160036CXDWD^ON_OLIS^2.16.840.1.113883.3.59.1:4145|CXDWD^Culture Deep Wound^1.3.6.1.4.1.12201.1.1.1.106|||201204161344|||||||201204161344|ABSC&&1.3.6.1.4.1.12201.1.1.1.132^^^^&Abdominal|204917^^^^^^^^2.16.840.1.113883.3.59.3:0898^^^^EI|||||||||P||1^^^20120416^^R|204917^KARANICOLAS^STAVROS^^^^^^2.16.840.1.113883.3.59.3:0898^^^^EI\r" +
            "ZBR||2.16.840.1.113883.3.59.1:4145^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4145|2.16.840.1.113883.3.59.1:4145^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4145|2.16.840.1.113883.3.59.1:4145^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4145||2.16.840.1.113883.3.59.1:4145^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4145|||||CXDWD\r" +
            "OBX|1|TX|GRMST^Direct Gram Stain^1.3.6.1.4.1.12201.1.1.1.107|1|Moderate Polymorphonuclear leucocytes seen. Moderate Gram\\.br\\positive cocci. Moderate Gram negative bacilli||||||F\r" +
            "ZBX|201204161354|0001\r" +
            "OBX|2|CE|ORG^Culture Deep Wound^1.3.6.1.4.1.12201.1.1.1.107|1|Staphylococcus aureus^Staphylococcus aureus^1.3.6.1.4.1.12201.1.1.1.130||||||P\r" +
            "ZBX|201204161354|0002\r" +
            "OBX|3|TX|CXDWD^Culture Deep Wound^1.3.6.1.4.1.12201.1.1.1.107|1|Moderate growth||||||P\r" +
            "ZBX|201204161354|0003\r" +
            "BLG|||MOHLTC\r" +
            "ORC||||G0160036^ON_OLIS^2.16.840.1.113883.3.59.1:4145|||||201204161343||||||||||||^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4145\r" +
            "OBR|2|G0160036CXDWDS1^ON_OLIS^2.16.840.1.113883.3.59.1:4145|G0160036CXDWDS1^ON_OLIS^2.16.840.1.113883.3.59.1:4145|MIC^Culture Deep Wound^1.3.6.1.4.1.12201.1.1.1.106|||201204161344|||||||201204161344|ABSC&&1.3.6.1.4.1.12201.1.1.1.132^^^^&Abdominal|204917^^^^^^^^2.16.840.1.113883.3.59.3:0898^^^^EI|||||||||P|ORG&&1.3.6.1.4.1.12201.1.1.1.107^1|1^^^20120416^^R|204917^KARANICOLAS^STAVROS^^^^^^2.16.840.1.113883.3.59.3:0898^^^^EI|G0160036CXDWD&&2.16.840.1.113883.3.59.1:4145&ISO^G0160036CXDWD&&2.16.840.1.113883.3.59.1:4145&ISO\r" +
            "ZBR||2.16.840.1.113883.3.59.1:4145^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4145|2.16.840.1.113883.3.59.1:4145^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4145|2.16.840.1.113883.3.59.1:4145^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4145||2.16.840.1.113883.3.59.1:4145^^^^^ON_OLIS&2.16.840.1.113883.3.59.1:4145|||||MIC\r" +
            "OBX|1|ST|e^Erythromycin^1.3.6.1.4.1.12201.1.1.1.131|1|SUSCEPTIBLE|mcg/mL||S|||P\r" +
            "ZBX|201204161354|0001\r" +
            "OBX|2|ST|ox^Oxacillin^1.3.6.1.4.1.12201.1.1.1.131|1|SUSCEPTIBLE|mcg/mL||S|||P\r" +
            "ZBX|201204161354|0002\r" +
            "OBX|3||sxt^Trimethoprim/Sulfa^1.3.6.1.4.1.12201.1.1.1.131|1|SUSCEPTIBLE|mcg/mL||S|||P\r" +
            "ZBX|201204161354|0003\r" +
            "BLG|||MOHLTC";

        CanonicalHl7V2Message canonical = new CanonicalHl7V2Message();
        canonical.setRawMessage(biochemSMHMessage);

        ORU_R01 hapiMsg = new ORU_R01();
        hapiMsg.parse(biochemSMHMessage);

        new SOAP_Map_ORUint_ON_OLIS_ORU_ON_OLIS().processMessage(canonical, hapiMsg);

        logger.info("Message is:\r\n" + hapiMsg.encode().replace("\r", "\r\n"));

        String actual = Escape.escape("GLUCOSE^1H POST 75 G GLUCOSE PO:SCNC:PT:SER/PLAS:QN", EncodingCharacters.getInstance(hapiMsg));
        logger.info("Actual is: " + actual);

        logger.info(getUTC("20100915180200"));

    }
}
