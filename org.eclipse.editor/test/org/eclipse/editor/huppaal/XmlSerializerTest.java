package org.eclipse.editor.huppaal;

import static org.junit.Assert.*;

import java.io.StringWriter;
import java.util.Arrays;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.eclipse.editor.editor.EditorFactory;
import org.eclipse.editor.editor.State;
import org.eclipse.editor.huppaal.XmlSerializer;
import org.eclipse.editor.huppaal.model.Hta;
import org.eclipse.editor.huppaal.model.Name;
import org.eclipse.editor.huppaal.model.Template;
import org.junit.Test;

public class XmlSerializerTest {
	@Test
	public void jaxb() throws Exception {
		Hta hta = new Hta();
		hta.setDeclaration("int i;");
		Template template = new Template();
		Name name = new Name();
		name.setvalue("Subtemplate 1");
		template.setName(name);
		template.setDeclaration("int j;");
		
		hta.getTemplate().add(template);
		
//		Template template2 = new Template();
//		template2.name = "Subtemplate2";
//		template2.declaration = "int k;";
//		
//		item.templates.add(template2);
		
		JAXBContext context = JAXBContext.newInstance(hta.getClass());
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty("com.sun.xml.internal.bind.xmlHeaders", "<!DOCTYPE hta SYSTEM \"huppaal-0.6.dtd\">");
		StringWriter sw = new StringWriter();
		marshaller.marshal(hta, sw);
		
		System.out.println(sw.toString());
	}
}
