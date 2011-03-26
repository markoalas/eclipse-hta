package org.eclipse.editor.features;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.AbstractUpdateFeature;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;

public class UpdateFeature extends AbstractUpdateFeature {

	public UpdateFeature(IFeatureProvider fp) {
		super(fp);
	}

	@Override
	public boolean canUpdate(IUpdateContext context) {
		Object bo = getBusinessObjectForPictogramElement(context.getPictogramElement());
		return bo instanceof EClass;
	}

	@Override
	public IReason updateNeeded(IUpdateContext context) {
		PictogramElement pictogramElement = context.getPictogramElement();

		String pictogramName = getPictogramName(pictogramElement);
		String businessName = getBusinessName(pictogramElement);

		boolean updateNameNeeded = ((pictogramName == null && businessName != null) || (pictogramName != null && !pictogramName.equals(businessName)));

		if (updateNameNeeded) {
			return Reason.createTrueReason("Name is out of date");
		} else {
			return Reason.createFalseReason();
		}
	}

	@Override
	public boolean update(IUpdateContext context) {
		PictogramElement pictogramElement = context.getPictogramElement();
		String businessName = getBusinessName(pictogramElement);

		setPictogramName(pictogramElement, businessName);

		return true;
	}

	private void setPictogramName(PictogramElement pictogramElement, String newName) {
		if (pictogramElement instanceof ContainerShape) {
			ContainerShape cs = (ContainerShape) pictogramElement;
			for (Shape shape : cs.getChildren()) {
				if (shape.getGraphicsAlgorithm() instanceof Text) {
					Text text = (Text) shape.getGraphicsAlgorithm();
					text.setValue(newName);
				}
			}
		}
	}

	private String getPictogramName(PictogramElement pictogramElement) {
		String pictogramName = null;
		if (pictogramElement instanceof ContainerShape) {
			ContainerShape cs = (ContainerShape) pictogramElement;
			for (Shape shape : cs.getChildren()) {
				if (shape.getGraphicsAlgorithm() instanceof Text) {
					Text text = (Text) shape.getGraphicsAlgorithm();
					pictogramName = text.getValue();
				}
			}
		}
		return pictogramName;
	}

	private String getBusinessName(PictogramElement pictogramElement) {
		String businessName = null;
		Object bo = getBusinessObjectForPictogramElement(pictogramElement);
		if (bo instanceof EClass) {
			EClass eClass = (EClass) bo;
			businessName = eClass.getName();
		}
		return businessName;
	}
}
