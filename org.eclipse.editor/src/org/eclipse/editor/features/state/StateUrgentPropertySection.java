package org.eclipse.editor.features.state;

import org.eclipse.editor.editor.State;
import org.eclipse.editor.features.CheckboxPropertySection;

public class StateUrgentPropertySection extends CheckboxPropertySection<State> {
	@Override
	protected boolean getValue() {
		return getBusinessObject().isUrgent();
	}

	@Override
	protected void setValue(boolean value) {
		getBusinessObject().setUrgent(value);
	}

	@Override
	protected String getLabel() {
		return "Urgent:";
	}
	
}
