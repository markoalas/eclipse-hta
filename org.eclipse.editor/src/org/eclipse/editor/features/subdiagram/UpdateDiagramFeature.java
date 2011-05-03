package org.eclipse.editor.features.subdiagram;

import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static org.eclipse.editor.EditorUtil.cast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.editor.Log;
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
import org.eclipse.graphiti.mm.algorithms.Rectangle;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.BoxRelativeAnchor;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;

public class UpdateDiagramFeature extends AbstractUpdateFeature {
	private static final Logger log = Log.getLogger();

	public UpdateDiagramFeature(IFeatureProvider fp) {
		super(fp);
	}

	@Override
	public boolean canUpdate(IUpdateContext context) {
		Object bo = getBusinessObjectForPictogramElement(context
				.getPictogramElement());
		return bo instanceof org.eclipse.editor.editor.Diagram;
	}

	@Override
	public IReason updateNeeded(IUpdateContext context) {
		PictogramElement pictogramElement = context.getPictogramElement();

		String pictogramName = getPictogramName(pictogramElement);
		String businessName = getBusinessName(pictogramElement);
		List<PictogramElement> connectors = getAllConnectorElements(pictogramElement);

		boolean updateNameNeeded = ((pictogramName == null && businessName != null) || (pictogramName != null && !pictogramName
				.equals(businessName)));
		if (updateNameNeeded) {
			return Reason.createTrueReason("Name is out of date");
		} else if (connectors.size() > 0) {
			// TODO figure out if the connectors have changed and only then
			// return true
			return Reason.createTrueReason("Connectors need updating");
		} else {
			return Reason.createFalseReason();
		}
	}

	private List<PictogramElement> getAllConnectorElements(
			PictogramElement pictogramElement) {
		List<PictogramElement> connectors = new ArrayList<PictogramElement>();

		Object businessObject = getBusinessObjectForPictogramElement(pictogramElement);
		if (businessObject instanceof org.eclipse.editor.editor.Diagram) {
			org.eclipse.editor.editor.Diagram diagram = (org.eclipse.editor.editor.Diagram) businessObject;
			EList<EObject> eResourceContents = diagram.eResource()
					.getContents();
			
			Collection<Diagram> diagrams = getLinkedDiagrams(
					pictogramElement,
					transform(
							filter(eResourceContents, instanceOf(Diagram.class)),
							cast(Diagram.class)));
			
			if (diagrams.size() > 0) {
				Diagram d = diagrams.iterator().next();

				for (PictogramElement e : d.getChildren()) {
					Object child = getBusinessObjectForPictogramElement(e);
					if (child instanceof Connector) {
						connectors.add(e);
					}
				}
			}
		}
		return connectors;
	}

	private List<Anchor> getAllAnchorsForSubdiagram(ContainerShape cs) {
		List<PictogramElement> connectors = new ArrayList<PictogramElement>();

		Object businessObject = getBusinessObjectForPictogramElement(cs);
		if (businessObject instanceof org.eclipse.editor.editor.Diagram) {
			org.eclipse.editor.editor.Diagram diagram = (org.eclipse.editor.editor.Diagram) businessObject;
		}
		return null;
	}

	// TODO this is common code with DrillDownFeature
	protected Collection<Diagram> getLinkedDiagrams(PictogramElement pe,
			Collection<Diagram> allDiagrams) {
		final Collection<Diagram> ret = new HashSet<Diagram>();

		final Object[] businessObjectsForPictogramElement = getAllBusinessObjectsForPictogramElement(pe);

		for (final Diagram d : allDiagrams) {
			final Diagram currentDiagram = getDiagram();
			if (!EcoreUtil.equals(currentDiagram, d)) {
				final Object[] businessObjectsForDiagram = getAllBusinessObjectsForPictogramElement(d);
				for (int i = 0; i < businessObjectsForDiagram.length; i++) {
					final Object diagramBo = businessObjectsForDiagram[i];
					for (int j = 0; j < businessObjectsForPictogramElement.length; j++) {
						final Object currentBo = businessObjectsForPictogramElement[j];
						if (EcoreUtil.equals((EObject) currentBo,
								(EObject) diagramBo)) {
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
		updateConnectors(pictogramElement);

		return true;
	}

	private void setPictogramName(PictogramElement pictogramElement,
			String newName) {
		if (pictogramElement instanceof ContainerShape) {
			ContainerShape cs = (ContainerShape) pictogramElement;
			for (Shape shape : cs.getChildren()) {
				GraphicsAlgorithm graphicsAlgorithm = shape
						.getGraphicsAlgorithm();
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
		if (bo instanceof org.eclipse.editor.editor.Diagram) {
			org.eclipse.editor.editor.Diagram eClass = (org.eclipse.editor.editor.Diagram) bo;
			businessName = eClass.getName();
		}
		return businessName;
	}

	private void updateConnectors(PictogramElement diagramElement) {
		try {
			ContainerShape containerShape = (ContainerShape) diagramElement
					.getGraphicsAlgorithm().eContainer();
			containerShape.getAnchors().clear();
			System.out.println("clearing");
			List<PictogramElement> connectorElements = getAllConnectorElements(diagramElement);

			for (PictogramElement connectorElement : connectorElements) {
				Connector connector = (Connector) getBusinessObjectForPictogramElement(connectorElement);

				GraphicsAlgorithm ga = connectorElement.getGraphicsAlgorithm();

				double normalizedX = min(round(min(ga.getX(), 600.0) / 600), 0.9);
				double normalizedY = min(ga.getY(), 300.0) / 300;

				IPeCreateService peCreateService = Graphiti
						.getPeCreateService();
				IGaService gaService = Graphiti.getGaService();

				createAnchor(peCreateService, containerShape, gaService,
						normalizedX, normalizedY, connector);
			}
		} catch (Exception e) {
			// TODO better solution for finding containerShape
			log.error(e.getMessage(), e);
		}
	}

	private void createAnchor(IPeCreateService peCreateService,
			ContainerShape containerShape, IGaService gaService, double x,
			double y, Connector connector) {
		
		BoxRelativeAnchor boxAnchor = peCreateService
				.createBoxRelativeAnchor(containerShape);
		boxAnchor.setRelativeWidth(x);
		boxAnchor.setRelativeHeight(y);

		System.out.println("creating for " + connector.getName() + " " + x + ":" + y);
		
		Rectangle rectangle = gaService.createRectangle(boxAnchor);
		rectangle.setFilled(true);

		int w = 12;
		gaService.setSize(rectangle, w, w);
		link(boxAnchor, connector);
		rectangle
				.setForeground(manageColor(AddSubdiagramFeature.CLASS_FOREGROUND));
		rectangle
				.setBackground(manageColor(AddSubdiagramFeature.CLASS_BACKGROUND));
	}
}
