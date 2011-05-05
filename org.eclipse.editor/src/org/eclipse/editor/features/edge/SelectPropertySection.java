package org.eclipse.editor.features.edge;

import org.eclipse.editor.editor.Edge;
import org.eclipse.editor.features.TextboxPropertySection;

public class SelectPropertySection extends TextboxPropertySection<Edge> {

	@Override
	protected String getValue() {
		return getBusinessObject().getSelect();
	}

	@Override
	protected void setValue(String value) {
		getBusinessObject().setSelect(value);		
	}

	@Override
	protected String getLabel() {
		return "Select:";
	}

}
