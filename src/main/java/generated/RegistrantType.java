//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.04.07 at 07:43:44 PM CDT 
//


package generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for registrantType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="registrantType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="RegistrantID" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="RegistrantName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="GeneralDescription" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="Address" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="RegistrantCountry" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="RegistrantPPBCountry" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "registrantType")
public class RegistrantType {

    @XmlAttribute(name = "RegistrantID")
    protected Long registrantID;
    @XmlAttribute(name = "RegistrantName")
    protected String registrantName;
    @XmlAttribute(name = "GeneralDescription")
    protected String generalDescription;
    @XmlAttribute(name = "Address")
    protected String address;
    @XmlAttribute(name = "RegistrantCountry")
    protected String registrantCountry;
    @XmlAttribute(name = "RegistrantPPBCountry")
    protected String registrantPPBCountry;

    /**
     * Gets the value of the registrantID property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getRegistrantID() {
        return registrantID;
    }

    /**
     * Sets the value of the registrantID property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setRegistrantID(Long value) {
        this.registrantID = value;
    }

    /**
     * Gets the value of the registrantName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRegistrantName() {
        return registrantName;
    }

    /**
     * Sets the value of the registrantName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRegistrantName(String value) {
        this.registrantName = value;
    }

    /**
     * Gets the value of the generalDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGeneralDescription() {
        return generalDescription;
    }

    /**
     * Sets the value of the generalDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGeneralDescription(String value) {
        this.generalDescription = value;
    }

    /**
     * Gets the value of the address property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the value of the address property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAddress(String value) {
        this.address = value;
    }

    /**
     * Gets the value of the registrantCountry property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRegistrantCountry() {
        return registrantCountry;
    }

    /**
     * Sets the value of the registrantCountry property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRegistrantCountry(String value) {
        this.registrantCountry = value;
    }

    /**
     * Gets the value of the registrantPPBCountry property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRegistrantPPBCountry() {
        return registrantPPBCountry;
    }

    /**
     * Sets the value of the registrantPPBCountry property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRegistrantPPBCountry(String value) {
        this.registrantPPBCountry = value;
    }

}
