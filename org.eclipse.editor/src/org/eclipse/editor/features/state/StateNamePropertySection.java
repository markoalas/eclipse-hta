package org.eclipse.editor.features.state;

import org.eclipse.editor.editor.State;
import org.eclipse.editor.features.TextboxPropertySection;

public class StateNamePropertySection extends TextboxPropertySection<State> {
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
