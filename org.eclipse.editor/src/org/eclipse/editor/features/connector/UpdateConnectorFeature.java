package org.eclipse.editor.features.connector;

import static org.eclipse.editor.EditorUtil.nvl;

import org.eclipse.editor.editor.Connector;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.AbstractUpdateFeature;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;

public class UpdateConnectorFeature extends AbstractUpdateFeature {

	public UpdateConnectorFeature(IFeatureProvider fp) {
		super(fp);
	}
	
	@Override
	public boolean canUpdate(IUpdateContext context) {
		if (!(context.getPictogramElement() instanceof ContainerShape)) {
			return false;
		}
		
		Object bo = getBusinessObjectForPictogramElement(context.getPictogramElement());
		return bo instanceof Connector;
	}

	@Override
	public IReason updateNeeded(IUpdateContext context) {
		if (!(context.getPictogramElement() instanceof ContainerShape)) {
			return Reason.createFalseReason();
		}
		
		PictogramElement pictogramElement = context.getPictogramElement();
		
		Connector connector = (Connector)getBusinessObjectForPictogramElement(pictogramElement);
		String pictogramName = getNameElement(pictogramElement).getValue();
		
		if (!nvl(connector.getName()).equals(nvl(pictogramName))) {
			return Reason.createTrueReason("Name is out of date.");
		}

		return Reason.createFalseReason();
	}
	
	private Text getNameElement(PictogramElement pictogramElement) {
		ContainerShape cs = (ContainerShape) pictogramElement;
		return ((org.eclipse.graphiti.mm.algorithms.Text)cs.getChildren().get(0).getGraphicsAlgorithm());
	}


	@Override
	public boolean update(IUpdateContext context) {
		PictogramElement pictogramElement = context.getPictogramElement();
		Connector connector = (Connector)getBusinessObjectForPictogramElement(pictogramElement);
		getNameElement(pictogramElement).setValue(connector.getName());
		
		return true;
	}
}