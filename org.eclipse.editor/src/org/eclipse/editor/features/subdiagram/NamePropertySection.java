package org.eclipse.editor.features.subdiagram;

import org.eclipse.editor.editor.Diagram;
import org.eclipse.editor.features.TextboxPropertySection;

public class NamePropertySection extends TextboxPropertySection<Diagram> {
	@Override
	protected String getValue() {
		Diagram businessObject = getBusinessObject();
		if (businessObject == null) {
			// TODO for some reason
			// GraphitiInternal.getEmfService().isObjectAlive(pictogramElement)
			// returns false sometimes and that means no business object for us
			// :(
			return "";
		}
		return businessObject.getName();
	}

	@Override
	protected void setValue(String value) {
		getBusinessObject().setName(value);
	}

	@Override
	protected String getLabel() {
		return "Name:";
	}
}