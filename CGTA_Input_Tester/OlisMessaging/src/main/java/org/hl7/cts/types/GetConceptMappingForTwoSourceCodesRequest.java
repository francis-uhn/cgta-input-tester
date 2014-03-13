//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-833 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.11.13 at 04:02:04 PM EST 
//


package org.hl7.cts.types;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 				Container for a request to find a code which is mapped to by two separate
 * 				codes.
 *             
 * 
 * <p>Java class for GetConceptMappingForTwoSourceCodesRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetConceptMappingForTwoSourceCodesRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="code_1" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="code_system_1" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="code_2" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="code_system_2" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="target_code_system" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetConceptMappingForTwoSourceCodesRequest", propOrder = {
    "code1",
    "codeSystem1",
    "code2",
    "codeSystem2",
    "targetCodeSystem"
})
public class GetConceptMappingForTwoSourceCodesRequest
    implements Serializable
{

    private final static long serialVersionUID = 12343L;
    @XmlElement(name = "code_1", required = true)
    protected String code1;
    @XmlElement(name = "code_system_1", required = true)
    protected String codeSystem1;
    @XmlElement(name = "code_2", required = true)
    protected String code2;
    @XmlElement(name = "code_system_2", required = true)
    protected String codeSystem2;
    @XmlElement(name = "target_code_system", required = true)
    protected String targetCodeSystem;

    /**
     * Gets the value of the code1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCode1() {
        return code1;
    }

    /**
     * Sets the value of the code1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCode1(String value) {
        this.code1 = value;
    }

    public boolean isSetCode1() {
        return (this.code1 != null);
    }

    /**
     * Gets the value of the codeSystem1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCodeSystem1() {
        return codeSystem1;
    }

    /**
     * Sets the value of the codeSystem1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCodeSystem1(String value) {
        this.codeSystem1 = value;
    }

    public boolean isSetCodeSystem1() {
        return (this.codeSystem1 != null);
    }

    /**
     * Gets the value of the code2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCode2() {
        return code2;
    }

    /**
     * Sets the value of the code2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCode2(String value) {
        this.code2 = value;
    }

    public boolean isSetCode2() {
        return (this.code2 != null);
    }

    /**
     * Gets the value of the codeSystem2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCodeSystem2() {
        return codeSystem2;
    }

    /**
     * Sets the value of the codeSystem2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCodeSystem2(String value) {
        this.codeSystem2 = value;
    }

    public boolean isSetCodeSystem2() {
        return (this.codeSystem2 != null);
    }

    /**
     * Gets the value of the targetCodeSystem property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetCodeSystem() {
        return targetCodeSystem;
    }

    /**
     * Sets the value of the targetCodeSystem property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetCodeSystem(String value) {
        this.targetCodeSystem = value;
    }

    public boolean isSetTargetCodeSystem() {
        return (this.targetCodeSystem!= null);
    }

}