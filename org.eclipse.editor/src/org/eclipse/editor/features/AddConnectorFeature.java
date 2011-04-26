package org.eclipse.editor.features;

import org.eclipse.editor.editor.Connector;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.impl.AbstractAddFeature;
import org.eclipse.graphiti.mm.algorithms.Rectangle;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
import org.eclipse.graphiti.mm.pictograms.BoxRelativeAnchor;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;
import org.eclipse.graphiti.util.ColorConstant;
import org.eclipse.graphiti.util.IColorConstant;

public class AddConnectorFeature extends AbstractAddFeature implements IAddFeature {

	private static final IColorConstant CLASS_TEXT_FOREGROUND = new ColorConstant(51, 51, 153);
	private static final IColorConstant CLASS_FOREGROUND = new ColorConstant(255, 102, 0);
	private static final IColorConstant CLASS_BACKGROUND = new ColorConstant(255, 255, 255);

	public AddConnectorFeature(IFeatureProvider fp) {
		super(fp);
	}

	public boolean canAdd(IAddContext context) {
		return context.getNewObject() instanceof Connector && context.getTargetContainer() instanceof Diagram;
	}

	public PictogramElement add(IAddContext context) {
		Connector connector = (Connector) context.getNewObject();
		Diagram targetDiagram = (Diagram) context.getTargetContainer();

		IPeCreateService peCreateService = Graphiti.getPeCreateService();
		IGaService gaService = Graphiti.getGaService();
		
		ContainerShape containerShape = peCreateService.createContainerShape(targetDiagram, true);
		link(containerShape, connector);

		int width = 50;
		int height = 40;
		
		Rectangle invisibleRect = gaService.createInvisibleRectangle(containerShape);
		gaService.setLocationAndSize(invisibleRect, context.getX(), context.getY(), width, height);

		if (connector.eResource() == null) {
			getDiagram().eResource().getContents().add(connector);
		}

		Shape shape = peCreateService.createShape(containerShape, false);
		createLabel(gaService, width, connector.getName(), shape);
		link(shape, connector);

		createAnchor(peCreateService, gaService, containerShape);

		layoutPictogramElement(containerShape);

		return containerShape;
	}

	private void createLabel(IGaService gaService, int width, String name, Shape shape) {
		Text text = gaService.createDefaultText(shape, name);
		text.setForeground(manageColor(CLASS_TEXT_FOREGROUND));
		text.setHorizontalAlignment(Orientation.ALIGNMENT_CENTER);
		text.setVerticalAlignment(Orientation.ALIGNMENT_TOP);
		text.getFont().setBold(true);
		gaService.setLocationAndSize(text, 0, 0, width, 20);
	}

	private void createAnchor(IPeCreateService peCreateService, IGaService gaService, ContainerShape containerShape) {
		BoxRelativeAnchor boxAnchor = peCreateService.createBoxRelativeAnchor(containerShape);
		boxAnchor.setRelativeWidth(0.5);
		boxAnchor.setRelativeHeight(1);

		Rectangle rectangle = gaService.createRectangle(boxAnchor);
		rectangle.setFilled(true);

		int w = 20;
		gaService.setLocationAndSize(rectangle, -w/2, -w, w, w);
		rectangle.setForeground(manageColor(CLASS_FOREGROUND));
		rectangle.setBackground(manageColor(CLASS_BACKGROUND));
	}

}
