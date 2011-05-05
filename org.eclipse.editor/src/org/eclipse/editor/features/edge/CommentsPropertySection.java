package org.eclipse.editor.features.edge;

import org.eclipse.editor.editor.Edge;
import org.eclipse.editor.features.TextboxPropertySection;

public class CommentsPropertySection extends TextboxPropertySection<Edge>{

	@Override
	protected String getValue() {
		return getBusinessObject().getComments();
	}

	@Override
	protected void setValue(String value) {
		getBusinessObject().setComments(value);
	}

	@Override
	protected String getLabel() {
		return "Comments:";
	}

}
