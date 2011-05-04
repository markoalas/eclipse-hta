package org.eclipse.editor.features.subdiagram;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.impl.AbstractAddFeature;
import org.eclipse.graphiti.mm.algorithms.Polyline;
import org.eclipse.graphiti.mm.algorithms.RoundedRectangle;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;
import org.eclipse.graphiti.util.ColorConstant;
import org.eclipse.graphiti.util.IColorConstant;

public class AddSubdiagramFeature extends AbstractAddFeature {

	private static final IColorConstant CLASS_TEXT_FOREGROUND = new ColorConstant(51, 51, 153);
	public static final IColorConstant CLASS_FOREGROUND = new ColorConstant(255, 102, 0);
	public static final IColorConstant CLASS_BACKGROUND = new ColorConstant(255, 204, 153);

	public AddSubdiagramFeature(IFeatureProvider fp) {
		super(fp);
	}

	public boolean canAdd(IAddContext context) {
		return 
			context.getNewObject() instanceof org.eclipse.editor.editor.Diagram && 
			context.getTargetContainer() instanceof Diagram;
	}

	public PictogramElement add(IAddContext context) {
		org.eclipse.editor.editor.Diagram addedSubdiagram = (org.eclipse.editor.editor.Diagram) context.getNewObject();
		Diagram targetDiagram = (Diagram) context.getTargetContainer();

		// CONTAINER SHAPE WITH ROUNDED RECTANGLE
		IPeCreateService peCreateService = Graphiti.getPeCreateService();
		ContainerShape containerShape = peCreateService.createContainerShape(targetDiagram, true);

		int width = Math.max(100, context.getWidth());
		int height = Math.max(100, context.getHeight());

		IGaService gaService = Graphiti.getGaService();

		{
			// create and set graphics algorithm
			RoundedRectangle roundedRectangle = gaService.createRoundedRectangle(containerShape, 5, 5);
			roundedRectangle.setForeground(manageColor(CLASS_FOREGROUND));
			roundedRectangle.setBackground(manageColor(CLASS_BACKGROUND));
			roundedRectangle.setLineWidth(2);
			gaService.setLocationAndSize(roundedRectangle, context.getX(), context.getY(), width, height);

			// if added Class has no resource we add it to the resource
			// of the diagram
			// in a real scenario the business model would have its own resource
			if (addedSubdiagram.eResource() == null) {
				getDiagram().eResource().getContents().add(addedSubdiagram);
			}

			// create link and wire it
			link(containerShape, addedSubdiagram);
		}

		// SHAPE WITH LINE
		{
			// create shape for line
			Shape shape = peCreateService.createShape(containerShape, false);

			// create and set graphics algorithm
			Polyline polyline = gaService.createPolyline(shape, new int[] { 0, 20, width, 20 });
			polyline.setForeground(manageColor(CLASS_FOREGROUND));
			polyline.setLineWidth(2);
		}

		// SHAPE WITH TEXT
		{
			// create shape for text
			Shape shape = peCreateService.createShape(containerShape, false);

			// create and set text graphics algorithm
			Text text = gaService.createDefaultText(shape, addedSubdiagram.getName());
			text.setForeground(manageColor(CLASS_TEXT_FOREGROUND));
			text.setHorizontalAlignment(Orientation.ALIGNMENT_CENTER);
			text.setVerticalAlignment(Orientation.ALIGNMENT_CENTER);
			text.getFont().setBold(true);
			gaService.setLocationAndSize(text, 10, 0, width, 20);

			// create link and wire it
			link(shape, addedSubdiagram);
		}

		layoutPictogramElement(containerShape);

		return containerShape;
	}
}
