package org.eclipse.editor.features.state;

import org.eclipse.editor.editor.State;
import org.eclipse.editor.features.TextboxPropertySection;

public class StateInvariantPropertySection extends TextboxPropertySection<State> {

	@Override
	protected String getValue() {
		return getBusinessObject().getInvariant();
	}

	@Override
	protected void setValue(String value) {
		getBusinessObject().setInvariant(value);
	}

	@Override
	protected String getLabel() {
		return "Invariant:";
	}

}
