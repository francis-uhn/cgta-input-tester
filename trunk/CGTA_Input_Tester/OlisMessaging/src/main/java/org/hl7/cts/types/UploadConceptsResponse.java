//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-833 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.11.13 at 04:02:04 PM EST 
//


package org.hl7.cts.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="message" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="value_set" type="{urn://cts.hl7.org/types}ValueSet" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="concepts" type="{urn://cts.hl7.org/types}ConceptChangeCount" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="conceptMappings" type="{urn://cts.hl7.org/types}ConceptMappingChangeCount" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="warnings" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "message",
    "valueSet",
    "concepts",
    "conceptMappings",
    "warnings"
})
@XmlRootElement(name = "UploadConceptsResponse")
public class UploadConceptsResponse
    implements Serializable
{

    private final static long serialVersionUID = 12343L;
    @XmlElement(required = true)
    protected String message;
    @XmlElement(name = "value_set")
    protected List<ValueSet> valueSet;
    protected List<ConceptChangeCount> concepts;
    protected List<ConceptMappingChangeCount> conceptMappings;
    protected List<String> warnings;

    /**
     * Gets the value of the message property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the value of the message property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessage(String value) {
        this.message = value;
    }

    public boolean isSetMessage() {
        return (this.message!= null);
    }

    /**
     * Gets the value of the valueSet property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the valueSet property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getValueSet().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ValueSet }
     * 
     * 
     */
    public List<ValueSet> getValueSet() {
        if (valueSet == null) {
            valueSet = new ArrayList<ValueSet>();
        }
        return this.valueSet;
    }

    public boolean isSetValueSet() {
        return ((this.valueSet!= null)&&(!this.valueSet.isEmpty()));
    }

    public void unsetValueSet() {
        this.valueSet = null;
    }

    /**
     * Gets the value of the concepts property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the concepts property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConcepts().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ConceptChangeCount }
     * 
     * 
     */
    public List<ConceptChangeCount> getConcepts() {
        if (concepts == null) {
            concepts = new ArrayList<ConceptChangeCount>();
        }
        return this.concepts;
    }

    public boolean isSetConcepts() {
        return ((this.concepts!= null)&&(!this.concepts.isEmpty()));
    }

    public void unsetConcepts() {
        this.concepts = null;
    }

    /**
     * Gets the value of the conceptMappings property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the conceptMappings property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConceptMappings().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ConceptMappingChangeCount }
     * 
     * 
     */
    public List<ConceptMappingChangeCount> getConceptMappings() {
        if (conceptMappings == null) {
            conceptMappings = new ArrayList<ConceptMappingChangeCount>();
        }
        return this.conceptMappings;
    }

    public boolean isSetConceptMappings() {
        return ((this.conceptMappings!= null)&&(!this.conceptMappings.isEmpty()));
    }

    public void unsetConceptMappings() {
        this.conceptMappings = null;
    }

    /**
     * Gets the value of the warnings property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the warnings property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWarnings().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getWarnings() {
        if (warnings == null) {
            warnings = new ArrayList<String>();
        }
        return this.warnings;
    }

    public boolean isSetWarnings() {
        return ((this.warnings!= null)&&(!this.warnings.isEmpty()));
    }

    public void unsetWarnings() {
        this.warnings = null;
    }

}
