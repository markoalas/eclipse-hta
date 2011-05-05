package org.eclipse.editor.features.edge;

import org.eclipse.editor.editor.Edge;
import org.eclipse.editor.features.TextboxPropertySection;

public class UpdatePropertySection extends TextboxPropertySection<Edge> {

	@Override
	protected String getValue() {
		return getBusinessObject().getUpdate();
	}

	@Override
	protected void setValue(String value) {
		getBusinessObject().setUpdate(value);
	}

	@Override
	protected String getLabel() {
		return "Update:";
	}

}
