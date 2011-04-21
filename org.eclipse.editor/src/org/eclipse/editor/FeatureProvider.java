package org.eclipse.editor;


import static org.eclipse.editor.Log.withLogging;

import org.apache.log4j.Logger;
import org.eclipse.editor.features.AddConnectorFeature;
import org.eclipse.editor.features.AddEClassFeature;
import org.eclipse.editor.features.AddEReferenceFeature;
import org.eclipse.editor.features.CreateConnectorFeature;
import org.eclipse.editor.features.CreateEReferenceFeature;
import org.eclipse.editor.features.CreateFeature;
import org.eclipse.editor.features.DrillDownFeature;
import org.eclipse.editor.features.LayoutFeature;
import org.eclipse.editor.features.RenameFeature;
import org.eclipse.editor.features.ResizeFeature;
import org.eclipse.editor.features.UpdateFeature;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
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
		if (context.getNewObject() instanceof EClass) {
			if (((EClass) context.getNewObject()).getName().startsWith("CONNECTOR:"))
				return withLogging(IAddFeature.class, new AddConnectorFeature(this));
			return withLogging(IAddFeature.class, new AddEClassFeature(this));
		} else if (context.getNewObject() instanceof EReference) {
			return withLogging(IAddFeature.class, new AddEReferenceFeature(this));
		}

		return super.getAddFeature(context);
	}

	@Override
	public ICreateFeature[] getCreateFeatures() {
		return new ICreateFeature[] { withLogging(ICreateFeature.class, new CreateFeature(this)), withLogging(ICreateFeature.class, new CreateConnectorFeature(this)) };
	}

	@Override
	public ICreateConnectionFeature[] getCreateConnectionFeatures() {
		return new ICreateConnectionFeature[] { withLogging(ICreateConnectionFeature.class, new CreateEReferenceFeature(this)) };
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
			return withLogging(ILayoutFeature.class, new LayoutFeature(this));
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
