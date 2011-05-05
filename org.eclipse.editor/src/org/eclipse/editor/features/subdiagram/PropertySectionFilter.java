package org.eclipse.editor.features.subdiagram;

import org.eclipse.editor.editor.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.ui.platform.AbstractPropertySectionFilter;

public class PropertySectionFilter extends AbstractPropertySectionFilter {
	@Override
	protected boolean accept(PictogramElement pe) {
		return Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement(pe) instanceof Diagram;
	}
}
