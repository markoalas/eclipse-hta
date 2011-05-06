package org.eclipse.editor.huppaal;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.eclipse.editor.Log;
import org.eclipse.emf.ecore.EObject;

public class XmlSerializer {
	private static Logger log = Log.getLogger();
	
	public String toXml(EObject... objects) {
		StringWriter sw = new StringWriter();
		
		try {
//			HuppaalDocument item = new HuppaalDocument();
//			item.declaration = "int i;";
//			JAXBContext context = JAXBContext.newInstance(item.getClass());
//			Marshaller marshaller = context.createMarshaller();
//			marshaller.setProperty("com.sun.xml.internal.bind.xmlHeaders", "<!DOCTYPE hta SYSTEM \"huppaal-0.6.dtd\">");
//			
//			marshaller.marshal(item, sw);
		
		
//			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//			DocumentBuilder db = dbf.newDocumentBuilder();
//			
//			DOMImplementation impl = db.getDOMImplementation();
//			impl.createDocument(null, "HUPPAAL", null);
			
			
		} catch (Exception e) {
			log.error("Unable to serialize to XML: " + e.getMessage(), e);
		}
		
		return sw.toString();
	}
}
