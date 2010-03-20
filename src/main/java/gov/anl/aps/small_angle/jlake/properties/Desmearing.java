//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.05.01 at 01:42:34 PM CDT 
//


package gov.anl.aps.small_angle.jlake.properties;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *         &lt;element name="dataFile" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="extrapolation_form">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="flat background"/>
 *               &lt;enumeration value="linear"/>
 *               &lt;enumeration value="power law"/>
 *               &lt;enumeration value="power law + flat"/>
 *               &lt;enumeration value="Porod law"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="x_start_extrapolation_evaluation" type="{jlake-desmearing}floatUnitType"/>
 *         &lt;element name="iterations" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="iterative_weight_method">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="1.0"/>
 *               &lt;enumeration value="CorrectedI / SmearedI"/>
 *               &lt;enumeration value="2*SQRT(ChiSqr(0) / ChiSqr(i))"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="version" use="required" type="{http://www.w3.org/2001/XMLSchema}string" fixed="1.0" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "dataFile",
    "slitLength",
    "extrapolationForm",
    "xStartExtrapolationEvaluation",
    "iterations",
    "iterativeWeightMethod"
})
@XmlRootElement(name = "desmearing")
public class Desmearing {

    @XmlElement(required = true)
    protected String dataFile;
    @XmlElement(name = "extrapolation_form", required = true, defaultValue = "flat background")
    protected String extrapolationForm;
    @XmlElement(name = "x_start_extrapolation_evaluation", required = true)
    protected FloatUnitType xStartExtrapolationEvaluation;
    @XmlElement(defaultValue = "10000")
    protected int iterations;
    @XmlElement(name = "iterative_weight_method", required = true, defaultValue = "CorrectedI / SmearedI")
    protected String iterativeWeightMethod;
    @XmlAttribute(required = true)
    protected String version;

    /**
     * Gets the value of the dataFile property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataFile() {
        return dataFile;
    }

    /**
     * Sets the value of the dataFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataFile(String value) {
        this.dataFile = value;
    }

    /**
     * Gets the value of the extrapolationForm property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExtrapolationForm() {
        return extrapolationForm;
    }

    /**
     * Sets the value of the extrapolationForm property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExtrapolationForm(String value) {
        this.extrapolationForm = value;
    }

    /**
     * Gets the value of the xStartExtrapolationEvaluation property.
     * 
     * @return
     *     possible object is
     *     {@link FloatUnitType }
     *     
     */
    public FloatUnitType getXStartExtrapolationEvaluation() {
        return xStartExtrapolationEvaluation;
    }

    /**
     * Sets the value of the xStartExtrapolationEvaluation property.
     * 
     * @param value
     *     allowed object is
     *     {@link FloatUnitType }
     *     
     */
    public void setXStartExtrapolationEvaluation(FloatUnitType value) {
        this.xStartExtrapolationEvaluation = value;
    }

    /**
     * Gets the value of the iterations property.
     * 
     */
    public int getIterations() {
        return iterations;
    }

    /**
     * Sets the value of the iterations property.
     * 
     */
    public void setIterations(int value) {
        this.iterations = value;
    }

    /**
     * Gets the value of the iterativeWeightMethod property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIterativeWeightMethod() {
        return iterativeWeightMethod;
    }

    /**
     * Sets the value of the iterativeWeightMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIterativeWeightMethod(String value) {
        this.iterativeWeightMethod = value;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        if (version == null) {
            return "1.0";
        } else {
            return version;
        }
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

}