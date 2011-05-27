package org.eclipse.editor.features.state;

import org.eclipse.editor.editor.State;
import org.eclipse.editor.features.CheckboxPropertySection;

public class UrgentPropertySection extends CheckboxPropertySection<State> {
	@Override
	protected Boolean getValue() {
		return getBusinessObject().isUrgent();
	}

	@Override
	protected void setValue(Boolean value) {
		getBusinessObject().setUrgent(value);
	}

	@Override
	protected String getLabel() {
		return "Urgent:";
	}
	
}
