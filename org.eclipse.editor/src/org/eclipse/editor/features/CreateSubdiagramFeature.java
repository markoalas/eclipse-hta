package org.eclipse.editor.features;

import org.eclipse.editor.editor.EditorFactory;
import org.eclipse.graphiti.examples.common.ExampleUtil;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateContext;
import org.eclipse.graphiti.features.impl.AbstractCreateFeature;
import org.eclipse.graphiti.mm.pictograms.Diagram;

public class CreateSubdiagramFeature extends AbstractCreateFeature {

	private static final String TITLE = "Create subdiagram";
    private static final String USER_QUESTION = "Enter new subdiagram name";

    public CreateSubdiagramFeature(IFeatureProvider fp) {
        super(fp, "Subdiagram", "Create subdiagram");
    }

     public boolean canCreate(ICreateContext context) {
        return context.getTargetContainer() instanceof Diagram;
    }

    public Object[] create(ICreateContext context) {
        String name = ExampleUtil.askString(TITLE, USER_QUESTION, "");
        if (name == null || name.trim().length() == 0) {
        	System.out.println("No name!");
            return EMPTY;
        }

        org.eclipse.editor.editor.Diagram diagram = EditorFactory.eINSTANCE.createDiagram();
        getDiagram().eResource().getContents().add(diagram);
        diagram.setName(name);
        addGraphicalRepresentation(context, diagram);

        return new Object[] { diagram };
    }

}
