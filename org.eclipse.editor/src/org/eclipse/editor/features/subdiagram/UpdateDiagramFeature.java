package org.eclipse.editor.features.subdiagram;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Iterables.find;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static org.eclipse.editor.EditorUtil.cast;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.editor.Log;
import org.eclipse.editor.editor.Connector;
import org.eclipse.emf.common.util.EList;
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
import org.eclipse.graphiti.mm.pictograms.BoxRelativeAnchor;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

public class UpdateDiagramFeature extends AbstractUpdateFeature {
	private static final Logger log = Log.getLogger();
	
	public UpdateDiagramFeature(IFeatureProvider fp) {
		super(fp);
	}

	@Override
	public boolean canUpdate(IUpdateContext context) {
		Object bo = getBusinessObjectForPictogramElement(context.getPictogramElement());
		return bo instanceof org.eclipse.editor.editor.Diagram;
	}

	@Override
	public IReason updateNeeded(IUpdateContext context) {
		PictogramElement pictogramElement = context.getPictogramElement();

		IReason nameReason = getNameUpdateReason(pictogramElement);
		if (nameReason.toBoolean()) {
			return nameReason;
		}

		IReason connectorsReason = getConnectorsUpdateReason(pictogramElement);
		if (connectorsReason.toBoolean()) {
			return connectorsReason;
		}

		return Reason.createFalseReason();
	}

	private IReason getNameUpdateReason(PictogramElement pictogramElement) {
		String pictogramName = getPictogramName(pictogramElement);
		String businessName = getBusinessName(pictogramElement);

		boolean updateNameNeeded = (pictogramName == null && businessName != null) || (pictogramName != null && !pictogramName.equals(businessName));
		if (updateNameNeeded) {
			return Reason.createTrueReason("Name is out of date");
		} else {
			return Reason.createFalseReason();
		}
	}

	/**
	 * TODO Return true only if the connectors have changed after last update.
	 */
	private IReason getConnectorsUpdateReason(PictogramElement pictogramElement) {
		Iterable<PictogramElement> connectors = getConnectorElementsForDiagram(pictogramElement);

		if (connectors.iterator().hasNext()) {
			return Reason.createTrueReason("Connectors are out of date");
		} else {
			return Reason.createFalseReason();
		}
	}

	private String getPictogramName(PictogramElement pictogramElement) {
		try {
			ContainerShape cs = (ContainerShape) pictogramElement;
			Text text = (Text) Iterables.find(cs.getChildren(), instanceOf(Text.class));
			return text.getValue();
		} catch (Exception e) {
			return null;
		}
	}

	private Iterable<PictogramElement> getConnectorElementsForDiagram(PictogramElement pictogramElement) {
		return filter(getAllElementsForDiagram(pictogramElement), isLinkedToConnector());
	}

	private Predicate<PictogramElement> isLinkedToConnector() {
		return new Predicate<PictogramElement>() {
			@Override
			public boolean apply(PictogramElement e) {
				return getBusinessObjectForPictogramElement(e) instanceof Connector;
			}
		};
	}

	private List<PictogramElement> getAllElementsForDiagram(PictogramElement pictogramElement) {

		Object businessObject = getBusinessObjectForPictogramElement(pictogramElement);
		checkArgument(businessObject instanceof org.eclipse.editor.editor.Diagram, "pictogramElement must be liked a Diagram business object.");

		org.eclipse.editor.editor.Diagram diagram = (org.eclipse.editor.editor.Diagram) businessObject;
		EList<EObject> eResourceContents = diagram.eResource().getContents();

		Diagram d = getLinkedDiagram(pictogramElement, transform(filter(eResourceContents, instanceOf(Diagram.class)), cast(Diagram.class)));

		List<PictogramElement> connectors = Lists.newArrayList();
		for (PictogramElement e : d.getChildren()) {
			connectors.add(e);
		}

		return connectors;
	}

	private Map<Connector, BoxRelativeAnchor> getAnchorsForSubdiagram(ContainerShape cs) {
		Map<Connector, BoxRelativeAnchor> anchors = Maps.newHashMap();
		@SuppressWarnings({ "rawtypes", "unchecked" })
		Collection<BoxRelativeAnchor> relativeAnchors = (Collection) filter(cs.getAnchors(), instanceOf(BoxRelativeAnchor.class));

		for (BoxRelativeAnchor a : relativeAnchors) {
			Connector connector = (Connector) Iterables.find(a.getLink().getBusinessObjects(), instanceOf(Connector.class));
			anchors.put(connector, a);
		}

		return anchors;
	}

	private Diagram getLinkedDiagram(PictogramElement pe, Collection<Diagram> allDiagrams) {
		Object[] businessObjectsForPictogramElement = getAllBusinessObjectsForPictogramElement(pe);
		Diagram currentDiagram = getDiagram();

		return find(allDiagrams, and(notEqual(currentDiagram), containsAnyOf(businessObjectsForPictogramElement)));
	}

	private Predicate<Diagram> containsAnyOf(final Object[] businessObjects) {
		return new Predicate<Diagram>() {
			@Override
			public boolean apply(Diagram d) {
				Object[] businessObjectsForDiagram = getAllBusinessObjectsForPictogramElement(d);

				for (Object diagramBo : businessObjectsForDiagram) {
					for (Object currentBo : businessObjects) {
						if (EcoreUtil.equals((EObject) currentBo, (EObject) diagramBo)) {
							return true;
						}
					}
				}

				return false;
			}
		};
	}

	private Predicate<Diagram> notEqual(final Diagram currentDiagram) {
		return new Predicate<Diagram>() {
			@Override
			public boolean apply(Diagram d) {
				return !EcoreUtil.equals(currentDiagram, d);
			}
		};
	}

	@Override
	public boolean update(IUpdateContext context) {
		try {
			ContainerShape pictogramElement = (ContainerShape) context.getPictogramElement();
			updateName(pictogramElement);
			updateConnectors(pictogramElement);
			return true;
		} catch (Exception e) {
			log.error("Unable to update diagram: " + e.getMessage());
			return false;
		}
	}

	private void updateName(ContainerShape pictogramElement) {
		ContainerShape cs = (ContainerShape) pictogramElement;
		for (Shape shape : cs.getChildren()) {
			GraphicsAlgorithm graphicsAlgorithm = shape.getGraphicsAlgorithm();
			if (graphicsAlgorithm instanceof Text) {
				((Text) graphicsAlgorithm).setValue(getBusinessName(pictogramElement));
			}
		}
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

	private void updateConnectors(ContainerShape containerShape) {
		Map<Connector, BoxRelativeAnchor> anchorsForSubdiagram = getAnchorsForSubdiagram(containerShape);

		Iterable<PictogramElement> connectorElements = getConnectorElementsForDiagram(containerShape);
		double maxX = xOrdering().max(connectorElements).getGraphicsAlgorithm().getX();
		double maxY = max(yOrdering().max(getAllElementsForDiagram(containerShape)).getGraphicsAlgorithm().getY(), 300);

		for (PictogramElement connectorElement : connectorElements) {
			Connector connector = (Connector) getBusinessObjectForPictogramElement(connectorElement);

			GraphicsAlgorithm ga = connectorElement.getGraphicsAlgorithm();
			double normalizedX = min(round(min(ga.getX(), maxX) / maxX), 0.9);
			double normalizedY = min(max(min(ga.getY(), maxY) / maxY, 0.1), 0.9);

			BoxRelativeAnchor anchor = findAnchorForConnector(anchorsForSubdiagram, connector);
			if (anchor == null) {
				anchor = createAnchor(containerShape, connector);
			}

			anchor.setRelativeWidth(normalizedX);
			anchor.setRelativeHeight(normalizedY);
		}
	}

	private Ordering<PictogramElement> yOrdering() {
		return new Ordering<PictogramElement>() {
			@Override
			public int compare(PictogramElement arg0, PictogramElement arg1) {
				return Ints.compare(arg0.getGraphicsAlgorithm().getY(), arg1.getGraphicsAlgorithm().getY());
			}
		};
	}

	private Ordering<PictogramElement> xOrdering() {
		return new Ordering<PictogramElement>() {
			@Override
			public int compare(PictogramElement arg0, PictogramElement arg1) {
				return Ints.compare(arg0.getGraphicsAlgorithm().getX(), arg1.getGraphicsAlgorithm().getX());
			}
		};
	}

	private BoxRelativeAnchor findAnchorForConnector(Map<Connector, BoxRelativeAnchor> anchorsForSubdiagram, Connector connector) {
		for (Map.Entry<Connector, BoxRelativeAnchor> e : anchorsForSubdiagram.entrySet()) {
			if (EcoreUtil.equals(e.getKey(), connector)) {
				return e.getValue();
			}
		}

		return null;
	}

	private BoxRelativeAnchor createAnchor(ContainerShape containerShape, Connector connector) {
		IPeCreateService peCreateService = Graphiti.getPeCreateService();
		IGaService gaService = Graphiti.getGaService();
		BoxRelativeAnchor boxAnchor = peCreateService.createBoxRelativeAnchor(containerShape);

		Rectangle rectangle = gaService.createRectangle(boxAnchor);
		rectangle.setFilled(true);

		int w = 12;
		gaService.setSize(rectangle, w, w);
		link(boxAnchor, connector);
		rectangle.setForeground(manageColor(AddSubdiagramFeature.CLASS_FOREGROUND));
		rectangle.setBackground(manageColor(AddSubdiagramFeature.CLASS_BACKGROUND));

		return boxAnchor;
	}
}
