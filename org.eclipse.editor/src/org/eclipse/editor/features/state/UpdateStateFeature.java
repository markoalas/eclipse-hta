package org.eclipse.editor.features.state;

import org.eclipse.editor.EditorUtil;
import org.eclipse.editor.editor.State;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.AbstractUpdateFeature;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;

public class UpdateStateFeature extends AbstractUpdateFeature {

	public UpdateStateFeature(IFeatureProvider fp) {
		super(fp);
	}
	
	@Override
	public boolean canUpdate(IUpdateContext context) {
		if (!(context.getPictogramElement() instanceof ContainerShape)) {
			return false;
		}
		
		Object bo = getBusinessObjectForPictogramElement(context.getPictogramElement());
		return  bo instanceof State;
	}

	@Override
	public IReason updateNeeded(IUpdateContext context) {
		
		PictogramElement pictogramElement = context.getPictogramElement();
		
		State state = (State)getBusinessObjectForPictogramElement(pictogramElement);
		String pictogramName = getNameElement(pictogramElement).getValue();
		
		if (!EditorUtil.nvl(state.getName()).equals(EditorUtil.nvl(pictogramName))) {
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
		State state = (State)getBusinessObjectForPictogramElement(pictogramElement);
		getNameElement(pictogramElement).setValue(state.getName());
		return true;
	}
}
