package org.eclipse.editor.features.state;

import org.eclipse.editor.editor.State;
import org.eclipse.editor.features.CheckboxPropertySection;

public class CommitedPropertySection extends CheckboxPropertySection<State>{
	@Override
	protected Boolean getValue() {
		return getBusinessObject().isCommitted();
	}

	@Override
	protected void setValue(Boolean value) {
		getBusinessObject().setCommitted(value);
	}

	@Override
	protected String getLabel() {
		return "Committed:";
	}
}
