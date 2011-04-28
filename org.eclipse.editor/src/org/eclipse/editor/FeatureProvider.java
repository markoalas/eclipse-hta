package org.eclipse.editor;

import static org.eclipse.editor.Log.withLogging;

import org.eclipse.editor.editor.Connector;
import org.eclipse.editor.editor.Edge;
import org.eclipse.editor.editor.State;
import org.eclipse.editor.features.AddConnectorFeature;
import org.eclipse.editor.features.AddStateFeature;
import org.eclipse.editor.features.AddEdgeFeature;
import org.eclipse.editor.features.AddSubdiagramFeature;
import org.eclipse.editor.features.CreateConnectorFeature;
import org.eclipse.editor.features.CreateEdgeFeature;
import org.eclipse.editor.features.CreateStateFeature;
import org.eclipse.editor.features.CreateSubdiagramFeature;
import org.eclipse.editor.features.DrillDownFeature;
import org.eclipse.editor.features.LayoutSubdiagramFeature;
import org.eclipse.editor.features.RenameFeature;
import org.eclipse.editor.features.ResizeFeature;
import org.eclipse.editor.features.UpdateFeature;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.ICreateConnectionFeature;
import org.eclipse.graphiti.features.ICreateFeature;
import org.eclipse.graphiti.features.IFeature;
import org.eclipse.graphiti.features.ILayoutFeature;
import org.eclipse.graphiti.features.IResizeShapeFeature;
import org.eclipse.graphiti.features.IUpdateFeature;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.context.ILayoutContext;
import org.eclipse.graphiti.features.context.IPictogramElementContext;
import org.eclipse.graphiti.features.context.IResizeShapeContext;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.custom.ICustomFeature;
import org.eclipse.graphiti.ui.features.DefaultFeatureProvider;

public class FeatureProvider extends DefaultFeatureProvider {
	public FeatureProvider(IDiagramTypeProvider dtp) {
		super(dtp);
	}

	@Override
	public IAddFeature getAddFeature(IAddContext context) {
		if (context.getNewObject() instanceof State) {
			return withLogging(IAddFeature.class, new AddStateFeature(this));
		} else if (context.getNewObject() instanceof Edge) {
			return withLogging(IAddFeature.class, new AddEdgeFeature(this));
		} else if (context.getNewObject() instanceof org.eclipse.editor.editor.Diagram) {
			return withLogging(IAddFeature.class, new AddSubdiagramFeature(this));
		} else if (context.getNewObject() instanceof Connector) {
			return withLogging(IAddFeature.class, new AddConnectorFeature(this));
		}

		return super.getAddFeature(context);
	}

	@Override
	public ICreateFeature[] getCreateFeatures() {
		return new ICreateFeature[] { withLogging(ICreateFeature.class, new CreateStateFeature(this)),
				withLogging(ICreateFeature.class, new CreateConnectorFeature(this)), withLogging(ICreateFeature.class, new CreateSubdiagramFeature(this)) };
	}

	@Override
	public ICreateConnectionFeature[] getCreateConnectionFeatures() {
		return new ICreateConnectionFeature[] { withLogging(ICreateConnectionFeature.class, new CreateEdgeFeature(this)) };
	}

	@Override
	public IFeature[] getDragAndDropFeatures(IPictogramElementContext context) {
		return getCreateConnectionFeatures();
	}

	@Override
	public IResizeShapeFeature getResizeShapeFeature(IResizeShapeContext context) {
		if (getBusinessObjectForPictogramElement(context.getShape()) instanceof EClass) {
			return withLogging(IResizeShapeFeature.class, new ResizeFeature(this));
		}
		return super.getResizeShapeFeature(context);
	}

	@Override
	public ILayoutFeature getLayoutFeature(ILayoutContext context) {
		if (getBusinessObjectForPictogramElement(context.getPictogramElement()) instanceof EClass) {
			return withLogging(ILayoutFeature.class, new LayoutSubdiagramFeature(this));
		}
		return super.getLayoutFeature(context);
	}

	@Override
	public IUpdateFeature getUpdateFeature(IUpdateContext context) {
		if (getBusinessObjectForPictogramElement(context.getPictogramElement()) instanceof EClass) {
			return withLogging(IUpdateFeature.class, new UpdateFeature(this));
		}
		return super.getUpdateFeature(context);
	}

	@Override
	public ICustomFeature[] getCustomFeatures(ICustomContext context) {
		return new ICustomFeature[] { withLogging(ICustomFeature.class, new RenameFeature(this)), withLogging(ICustomFeature.class, new DrillDownFeature(this)) };
	}
}
