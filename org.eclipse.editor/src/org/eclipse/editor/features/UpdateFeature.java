package org.eclipse.editor.features;

import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;
import static org.eclipse.editor.EditorUtil.cast;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.editor.editor.Connector;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.AbstractUpdateFeature;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
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
		Object bo = getBusinessObjectForPictogramElement(pictogramElement);
		if (bo instanceof EClass) {
			EClass eClass = (EClass) bo;
			if (eClass.getName().equals("Klass")) {
				EList<EObject> eResourceContents = eClass.eResource().getContents();
				Collection<Diagram> diagrams = getLinkedDiagrams(pictogramElement,
						transform(filter(eResourceContents, instanceOf(Diagram.class)), cast(Diagram.class)));
				if (diagrams.size() > 0) {
					Diagram d = diagrams.iterator().next();

					for (PictogramElement e : d.getChildren()) {
						Object bo1 = getBusinessObjectForPictogramElement(e);
						if (bo1 instanceof Connector) {
							GraphicsAlgorithm graphicsAlgorithm = e.getGraphicsAlgorithm();
							System.out.println(graphicsAlgorithm.getX() + ":" + graphicsAlgorithm.getY());
						}
					}
				}
			}
		}

		boolean updateNameNeeded = ((pictogramName == null && businessName != null) || (pictogramName != null && !pictogramName.equals(businessName)));

		if (updateNameNeeded) {
			return Reason.createTrueReason("Name is out of date");
		} else {
			return Reason.createFalseReason();
		}
	}

	// selle asemel võiks teha getFeatureProvider().getDrillDownFeature() ...
	protected Collection<Diagram> getLinkedDiagrams(PictogramElement pe, Collection<Diagram> allDiagrams) {
		final Collection<Diagram> ret = new HashSet<Diagram>();

		final Object[] businessObjectsForPictogramElement = getAllBusinessObjectsForPictogramElement(pe);

		for (final Diagram d : allDiagrams) {
			final Diagram currentDiagram = getDiagram();
			if (!EcoreUtil.equals(currentDiagram, d)) { // always filter out the
														// current
				// diagram
				final Object[] businessObjectsForDiagram = getAllBusinessObjectsForPictogramElement(d);
				for (int i = 0; i < businessObjectsForDiagram.length; i++) {
					final Object diagramBo = businessObjectsForDiagram[i];
					for (int j = 0; j < businessObjectsForPictogramElement.length; j++) {
						final Object currentBo = businessObjectsForPictogramElement[j];
						if (EcoreUtil.equals((EObject) currentBo, (EObject) diagramBo)) {
							ret.add(d);
						}
					}
				}
			}
		}

		return ret;
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
				GraphicsAlgorithm graphicsAlgorithm = shape.getGraphicsAlgorithm();
				if (graphicsAlgorithm instanceof Text) {
					((Text) graphicsAlgorithm).setValue(newName);
				}
			}
		}
	}

	private String getPictogramName(PictogramElement pictogramElement) {
		if (pictogramElement instanceof ContainerShape) {
			ContainerShape cs = (ContainerShape) pictogramElement;
			for (Shape shape : cs.getChildren()) {
				if (shape.getGraphicsAlgorithm() instanceof Text) {
					Text text = (Text) shape.getGraphicsAlgorithm();
					return text.getValue();
				}
			}
		}
		return null;
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
