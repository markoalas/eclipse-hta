package org.eclipse.editor.features.edge;

import org.eclipse.editor.editor.Edge;
import org.eclipse.editor.features.TextboxPropertySection;

public class SyncPropertySection extends TextboxPropertySection<Edge> {

	@Override
	protected String getValue() {
		return getBusinessObject().getSync();
	}

	@Override
	protected void setValue(String value) {
		getBusinessObject().setSync(value);
	}

	@Override
	protected String getLabel() {
		return "Sync:";
	}

}
