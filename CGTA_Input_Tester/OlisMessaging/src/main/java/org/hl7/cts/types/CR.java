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
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CR complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CR">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{urn://cts.hl7.org/types}CV" minOccurs="0"/>
 *         &lt;element name="value" type="{urn://cts.hl7.org/types}CV" minOccurs="0"/>
 *         &lt;element name="inverted" type="{urn://cts.hl7.org/types}BL" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CR", propOrder = {
    "name",
    "value",
    "inverted"
})
public class CR
    implements Serializable
{

    private final static long serialVersionUID = 12343L;
    protected CV name;
    protected CV value;
    protected BL inverted;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link CV }
     *     
     */
    public CV getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link CV }
     *     
     */
    public void setName(CV value) {
        this.name = value;
    }

    public boolean isSetName() {
        return (this.name!= null);
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link CV }
     *     
     */
    public CV getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link CV }
     *     
     */
    public void setValue(CV value) {
        this.value = value;
    }

    public boolean isSetValue() {
        return (this.value!= null);
    }

    /**
     * Gets the value of the inverted property.
     * 
     * @return
     *     possible object is
     *     {@link BL }
     *     
     */
    public BL getInverted() {
        return inverted;
    }

    /**
     * Sets the value of the inverted property.
     * 
     * @param value
     *     allowed object is
     *     {@link BL }
     *     
     */
    public void setInverted(BL value) {
        this.inverted = value;
    }

    public boolean isSetInverted() {
        return (this.inverted!= null);
    }

}
