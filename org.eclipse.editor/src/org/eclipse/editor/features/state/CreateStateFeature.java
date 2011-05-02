package org.eclipse.editor.features.state;

import org.eclipse.editor.editor.EditorFactory;
import org.eclipse.editor.editor.State;
import org.eclipse.graphiti.examples.common.ExampleUtil;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateContext;
import org.eclipse.graphiti.features.impl.AbstractCreateFeature;
import org.eclipse.graphiti.mm.pictograms.Diagram;

public class CreateStateFeature extends AbstractCreateFeature {
    private static final String TITLE = "Create state";
    private static final String USER_QUESTION = "Enter new state name";

    public CreateStateFeature(IFeatureProvider fp) {
        super(fp, "State", "Create state");
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

        State newState = EditorFactory.eINSTANCE.createState();
        getDiagram().eResource().getContents().add(newState);
        newState.setName(name);
        addGraphicalRepresentation(context, newState);

        return new Object[] { newState };
    }
}
