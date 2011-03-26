package org.eclipse.editor.features;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IResizeShapeContext;
import org.eclipse.graphiti.features.impl.DefaultResizeShapeFeature;
import org.eclipse.graphiti.mm.pictograms.Shape;

public class ResizeFeature extends DefaultResizeShapeFeature {
    public ResizeFeature(IFeatureProvider fp) {
        super(fp);
    }

    @Override
    public boolean canResizeShape(IResizeShapeContext context) {
        boolean canResize = super.canResizeShape(context);
        
        if (canResize) {
            Shape shape = context.getShape();

            Object bo = getBusinessObjectForPictogramElement(shape);
            if (bo instanceof EClass) {
                EClass c = (EClass) bo;
                if (c.getName() != null && c.getName().length() == 1) {
                    canResize = false;
                }
            }
        }

        return canResize;
    }
}
