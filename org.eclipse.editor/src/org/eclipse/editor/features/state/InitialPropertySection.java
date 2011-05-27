package org.eclipse.editor.features.state;

import org.eclipse.editor.editor.State;
import org.eclipse.editor.features.CheckboxPropertySection;

public class InitialPropertySection extends CheckboxPropertySection<State> {
	@Override
	protected Boolean getValue() {
		return getBusinessObject().isInitial();
	}

	@Override
	protected void setValue(Boolean value) {
		getBusinessObject().setInitial(value);
	}

	@Override
	protected String getLabel() {
		return "Initial:";
	}
	
}
