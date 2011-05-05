package org.eclipse.editor.features.connector;

import org.eclipse.editor.editor.Connector;
import org.eclipse.editor.features.TextboxPropertySection;

public class NamePropertySection extends TextboxPropertySection<Connector> {
	@Override
	protected String getValue() {
		return getBusinessObject().getName();
	}

	@Override
	protected void setValue(String value) {
		getBusinessObject().setName(value);
	}

	@Override
	protected String getLabel() {
		return "Name:";
	}
}