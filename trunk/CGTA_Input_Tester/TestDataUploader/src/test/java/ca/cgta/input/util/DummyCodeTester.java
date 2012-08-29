/*
 * Created on May 1, 2012
 * DummyCodeTester.java
 * 
 */
package ca.cgta.input.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v25.message.ADT_A01;
import ca.uhn.hl7v2.parser.EncodingNotSupportedException;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

/**
 * 
 * @author <a href="mailto:neal.acharya@uhn.on.ca">Neal Acharya</a>
 * @version $Revision:$ updated on $Date:$ by $Author:$
 */
public class DummyCodeTester {
    
    private static DateFormat ourTFormat1 = new SimpleDateFormat("yyyyMMddHHmmssZ");
    private static DateFormat ourTFormat2 = new SimpleDateFormat("yyyyMMddHHmmZ");
    private static DateFormat ourTFormat3 = new SimpleDateFormat("yyyyMMddHHmmss");
    private static DateFormat ourTFormat4 = new SimpleDateFormat("yyyyMMddHHmm");
    private static DateFormat ourTFormat5 = new SimpleDateFormat("yyyyMMddHH");
    private static DateFormat ourTFormat6 = new SimpleDateFormat("yyyyMMdd");
    private static DateFormat ourTFormat7 = new SimpleDateFormat("yyyyMM");
    private static DateFormat ourTFormat8 = new SimpleDateFormat("yyyy");
    

    /**
     * 
     * @param args ...
     * @throws HL7Exception 
     * @throws EncodingNotSupportedException 
     */
    public static void main(String[] args) throws EncodingNotSupportedException, HL7Exception {
        
                
        String message = "MSH|^~\\&|EPR|G^2.16.840.1.113883.3.59.3:947^L|||201112021621||ADT^A01^ADT_A01|123484|T^|2.5^^||||||CAN||||\r" + 
                "EVN|A01|201112021621||||201112021621|G^4265^L\r" + 
                "PID|||7012673^^^UHN^MR^^^^^^^~9287170261^BL^^CANON^JHN^^^^^20111201^^~HN2827^^^UHN^PI^^^^^^^||Test^Majaconversion^^^Mrs.^^L^^^^^201112221537^^~Test^Maj^^^Mrs.^^A^^^^^^^||19731230|F|||1 Bloor Street ^^Toronto^ON^L9K 8J7^Can^H^^^^^^^~|12333|(415)222-3333^PRN^PH^^^^^^^^^~||eng^English^03ZPtlang^^^|||11110000514^^^UHN^VN^^^^^^^~||||||||||||N|||201112221537||||||\r" + 
                "PD1|||UHN^D^^^^UHN^FI^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^^^^^^^^^^^^|||||||N^no special privacy^03ZPrvyFlg^^^|N|||||||||\r" + 
                "ROL||UC|PP^Primary Care Provider^15ZRole^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^^^^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|O^Office^^^^|^^^ON^^Can^^^^^^^^|(416) 340-3391^WPN^PH^^^^^^^^^\r" + 
                "ROL||UC|PP^Primary Care Provider^15ZRole^^^|13546b^Genericb^Physicianb^Moeb^^Dr.^MD^^UHN^L^^^EI^G^2.16.840.1.113883.3.59.3:947^^^^^^^^^^|201111071338||||ET02^Physician^15ZEmpTyp^^^|1^Hospital^15ZOrgTyp^^^|^^^ON^^Can^^^^^^^^|(416) 340-3388^WPN^PH^^^^^^^^^\r" +
                "NK1|1|Wph^Mom^^^^^L^^^^^^^|PAR^Parent^03ZRelshp^^^|82 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^~(416)123-1234^PRN^CP^^^^^^^^^|(416)243-3600^PRN^PH^^^^^^^^^|N^Next-of-Kin^03ZConRol^^^||||||||||||||||||||||||||||||||\r" +
                "NK1|2|Wph^Brother^^^^^L^^^^^^^|BRO^Brother^03ZRelshp^^^|83 Buttonwood Avenue^^YORK^ON^M6M 2J5^Can^H^^^^^^^|(416)243-3601^PRN^PH^^^^^^^^^|(416)525-2525^PRN^PH^^^^^^^^^|C^Emergency Contact^03ZConRol^^^||||||||||||||||||||||||||||||||\r" + 
                "PV1||I|PMH 15C^413^2^G^4265^^^N^P15C 413^P15C 413 2^PMH 15C^1980 2 2^|C||^^^G^4265^^^^^^^  ^|13546a^Generic^Physician^MoeA^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|13546b^Generic^Physician^MoeB^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^||hospServ||||D|||13546c^Generic^Physician^MoeC^^Dr.^MD^^^L^^^EI^^^^^^^^^^^^^|IP^|11110000514^^^UHN^VN^G^4265^^^^^||||N||||||||||||||||G|||||2011-0500|||||||V|\r" + 
                "PV2||S^Semi^03ZFinbed^^^|^kfkfkfjcjcjcgcgcclcl^03ZAmitRes^^^|||||||||||||||||||N||AI|Elective||||||N|||||||OTH^Self^03ZBrInBy^^^|||||||||||\r" + 
                "DG1|1||06^KFKFKFJCJCJCGCGCCLCL^2.16.840.1.113883.11.19436^^^|KFKFKFJCJCJCGCGCCLCL|201112021621|A|||||||||1||D||||\r" + 
                "PR1||||||||||||||||||||\r" + 
                "ZPV|OTH^Self|F^Standard|||N|||13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^^^||Medical Services^General Internal Medicine^GMED|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^4265^G^^^^^^^^^^^^|413||3910||^^^^^|13546^Generic^Physician^Moe^^Dr.^MD^^UHN^L^^^EI^G^4265^^^^^^^^^^|0^1^2|||||||||||N||^^|\r" + 
                "ZWA||||||||active|\r";
        
//        ADT_A01 input = new ADT_A01();
//        input.setParser(PipeParser.getInstanceWithNoValidation());
//        input.parse(message);
        
        PipeParser myParser = new PipeParser();
        myParser.setValidationContext(new NoValidation()); 
        ADT_A01 input = (ADT_A01) myParser.parse(message);
        
//        System.out.println(input.getPV1().getAdmitDateTime().getTs1_Time().getValue());
//        System.out.println(input.getPV1().getAdmitDateTime().getTs1_Time().getValueAsDate());
//        System.out.println(input.getPV1().getAdmitDateTime().getTs1_Time().getValueAsCalendar());
        
        String val = "1972";
        
        System.out.println(validateVariableTsAndAddFailure(null, val));
        
                    

    }
    
    
     private static boolean validateVariableTsAndAddFailure(String theTerserPath, String theValue) {
            
//                if (theValue.matches("^[0-9]{8}[0-9]{4}([0-9]{2}(\\.[0-9]{1,4})?)?[\\-\\+][0-9]{4}$")) {
//                    return true;
//                }
                if (theValue.matches("^[0-9]{4}([0-9]{2}([0-9]{2}([0-9]{2}([0-9]{2}([0-9]{2}(\\.[0-9]{1,4})?)?)?)?)?)?([\\-\\+][0-9]{4})?$")) {
                    return true;
                } 
                return false;                
     }
     
     
    boolean validateTsWithAtLeastMinutePrecisionAndAddFailure(String theTerserPath, String theValue) {
        if (theValue == null || !theValue.matches("^[0-9]{8}[0-9]{4}([0-9]{2})?(\\.[0-9]{1,4})?[\\-\\+][0-9]{4}$")) {
            
            return false;
        } else {
            return true;
        }
    }
         
     
     
     
}

    
