package org.eclipse.editor.features;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.graphiti.examples.common.ExampleUtil;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateContext;
import org.eclipse.graphiti.features.impl.AbstractCreateFeature;
import org.eclipse.graphiti.mm.pictograms.Diagram;

public class CreateConnectorFeature extends AbstractCreateFeature {

	private static final String TITLE = "Create connector";
    private static final String USER_QUESTION = "Enter connector name";

    public CreateConnectorFeature(IFeatureProvider fp) {
        super(fp, "Connector", "Create Connector");
    }

     public boolean canCreate(ICreateContext context) {
        return context.getTargetContainer() instanceof Diagram;
    }

    public Object[] create(ICreateContext context) {
        String newClassName = ExampleUtil.askString(TITLE, USER_QUESTION, "");
        if (newClassName == null || newClassName.trim().length() == 0) {
            return EMPTY;
        }

        EClass newClass = EcoreFactory.eINSTANCE.createEClass();
        getDiagram().eResource().getContents().add(newClass);
        newClass.setName(newClassName);

        addGraphicalRepresentation(context, newClass);

        return new Object[] { newClass };
    }

}
