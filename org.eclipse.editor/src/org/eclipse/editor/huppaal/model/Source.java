//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.05.06 at 11:49:37 AM EEST 
//


package org.eclipse.editor.huppaal.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "source")
public class Source {

    @XmlAttribute(required = true)
    @XmlIDREF
    protected Object ref;
    @XmlAttribute
    @XmlIDREF
    protected Object exitref;

    /**
     * Gets the value of the ref property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getRef() {
        return ref;
    }

    /**
     * Sets the value of the ref property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setRef(Object value) {
        this.ref = value;
    }

    /**
     * Gets the value of the exitref property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getExitref() {
        return exitref;
    }

    /**
     * Sets the value of the exitref property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setExitref(Object value) {
        this.exitref = value;
    }

}
