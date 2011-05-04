package org.eclipse.editor.features.state;

import org.eclipse.editor.editor.State;
import org.eclipse.editor.features.CheckboxPropertySection;

public class StateCommitedPropertySection extends CheckboxPropertySection<State>{
	@Override
	protected boolean getValue() {
		return getBusinessObject().isCommitted();
	}

	@Override
	protected void setValue(boolean value) {
		getBusinessObject().setCommitted(value);
	}

	@Override
	protected String getLabel() {
		return "Committed:";
	}
}
