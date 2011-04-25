package org.eclipse.editor.features;

import org.eclipse.editor.editor.Connector;
import org.eclipse.editor.editor.EditorFactory;
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
        String newConnectorName = ExampleUtil.askString(TITLE, USER_QUESTION, "");
        if (newConnectorName == null || newConnectorName.trim().length() == 0) {
            return EMPTY;
        }

        Connector newConnector = EditorFactory.eINSTANCE.createConnector();
        getDiagram().eResource().getContents().add(newConnector);
        newConnector.setName(newConnectorName);
        
        addGraphicalRepresentation(context, newConnector);

        return new Object[] { newConnector };
    }
}
