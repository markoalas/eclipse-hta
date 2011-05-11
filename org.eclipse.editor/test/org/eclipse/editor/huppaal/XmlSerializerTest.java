package org.eclipse.editor.huppaal;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;

import org.eclipse.editor.huppaal.model.Hta;
import org.junit.Before;
import org.junit.Test;

public class XmlSerializerTest {
	XmlSerializer xmlSerializer;

	@Before
	public void before() {
		xmlSerializer = new XmlSerializer();
	}

	@Test
	public void toXml() throws Exception {
		StringWriter sw = new StringWriter();
		xmlSerializer.toXml(new Hta(), sw);
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE hta SYSTEM \"huppaal-0.6.dtd\">\n<hta/>", sw.toString());
	}
}
