package org.eclipse.editor.features.state;

import org.eclipse.editor.editor.State;
import org.eclipse.editor.features.CheckboxPropertySection;

public class StateInitialPropertySection extends CheckboxPropertySection<State> {
	@Override
	protected boolean getValue() {
		return getBusinessObject().isInitial();
	}

	@Override
	protected void setValue(boolean value) {
		getBusinessObject().setInitial(value);
	}

	@Override
	protected String getLabel() {
		return "Initial:";
	}
	
}
