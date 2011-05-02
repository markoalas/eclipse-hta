package org.eclipse.editor.features.edge;

import org.eclipse.editor.editor.Edge;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IAddConnectionContext;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.impl.AbstractAddFeature;
import org.eclipse.graphiti.mm.GraphicsAlgorithmContainer;
import org.eclipse.graphiti.mm.algorithms.Polyline;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;
import org.eclipse.graphiti.util.IColorConstant;

public class AddEdgeFeature extends AbstractAddFeature {

	public AddEdgeFeature(IFeatureProvider fp) {
		super(fp);
	}

	public PictogramElement add(IAddContext context) {

		IAddConnectionContext addConContext = (IAddConnectionContext) context;
		Edge addedEdge = (Edge) context.getNewObject();
		IGaService gaService = Graphiti.getGaService();
		IPeCreateService peCreateService = Graphiti.getPeCreateService();

		Connection connection = peCreateService
				.createFreeFormConnection(getDiagram());
		connection.setStart(addConContext.getSourceAnchor());
		connection.setEnd(addConContext.getTargetAnchor());
		Polyline polyline = gaService.createPolyline(connection);
		polyline.setLineWidth(2);
		polyline.setForeground(manageColor(IColorConstant.BLACK));

		ConnectionDecorator cd = peCreateService.createConnectionDecorator(
				connection, false, 1.0, true);
		createArrow(cd);

		link(connection, addedEdge);
		
		return connection;
	}

	public boolean canAdd(IAddContext context) {
		return context instanceof IAddConnectionContext
				&& context.getNewObject() instanceof Edge;
	}

	private Polyline createArrow(GraphicsAlgorithmContainer gaContainer) {

		IGaService gaService = Graphiti.getGaService();
		Polyline polyline = gaService.createPolyline(gaContainer, new int[] {
				-10, 7, 0, 0, -10, -7 });
		polyline.setForeground(manageColor(IColorConstant.BLACK));
		polyline.setLineWidth(2);
		return polyline;
	}
}
