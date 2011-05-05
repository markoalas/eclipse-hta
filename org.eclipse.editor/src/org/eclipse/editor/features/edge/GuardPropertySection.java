package org.eclipse.editor.features.edge;

import org.eclipse.editor.editor.Edge;
import org.eclipse.editor.features.TextboxPropertySection;

public class GuardPropertySection extends TextboxPropertySection<Edge> {

	@Override
	protected String getValue() {
		return getBusinessObject().getGuard();
	}

	@Override
	protected void setValue(String value) {
		getBusinessObject().setGuard(value);
	}

	@Override
	protected String getLabel() {
		return "Guard:";
	}

}
