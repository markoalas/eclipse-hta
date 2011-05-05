package org.eclipse.editor.features.connector;

import org.eclipse.editor.editor.Connector;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.ui.platform.AbstractPropertySectionFilter;

public class PropertySectionFilter extends AbstractPropertySectionFilter {
	@Override
	protected boolean accept(PictogramElement pe) {
		return Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement(pe) instanceof Connector;
	}
}
