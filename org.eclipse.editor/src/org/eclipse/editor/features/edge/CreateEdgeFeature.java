package org.eclipse.editor.features.edge;

import org.eclipse.editor.editor.Edge;
import org.eclipse.editor.editor.EditorFactory;
import org.eclipse.editor.editor.EndPoint;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateConnectionContext;
import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
import org.eclipse.graphiti.features.impl.AbstractCreateConnectionFeature;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.Connection;

public class CreateEdgeFeature extends AbstractCreateConnectionFeature {

	public CreateEdgeFeature(IFeatureProvider fp) {
		super(fp, "Edge", "Create edge");
	}

	public boolean canCreate(ICreateConnectionContext context) {
		EndPoint source = getEndPoint(context.getSourceAnchor());
		EndPoint target = getEndPoint(context.getTargetAnchor());

		if (source != null && target != null && source != target) {
			return true;
		}

		return false;
	}

	public boolean canStartConnection(ICreateConnectionContext context) {
		if (getEndPoint(context.getSourceAnchor()) != null) {
			return true;
		}

		return false;
	}

	public Connection create(ICreateConnectionContext context) {
		Connection newConnection = null;

		Edge edge = EditorFactory.eINSTANCE.createEdge();
		edge.setStart(getEndPoint(context.getSourceAnchor()));
		edge.setEnd(getEndPoint(context.getTargetAnchor()));
		getDiagram().eResource().getContents().add(edge);

		AddConnectionContext addContext = new AddConnectionContext(context.getSourceAnchor(), context.getTargetAnchor());
		addContext.setNewObject(edge);
		newConnection = (Connection) getFeatureProvider().addIfPossible(addContext);

		return newConnection;
	}

	private EndPoint getEndPoint(Anchor anchor) {
		if (anchor == null) {
			return null;
		}
		
		Object pe = getBusinessObjectForPictogramElement(anchor.getParent());
		if (pe instanceof EndPoint) {
			return (EndPoint)pe;
		}
		
		return null;
	}
}
