package org.eclipse.editor.features;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;

public class ExportXMLFeature extends AbstractCustomFeature {
	public ExportXMLFeature(IFeatureProvider fp) {
		super(fp);
	}
	
	@Override
	public String getName() {
		return "Export as Huppaal XML";
	}

	@Override
	public String getDescription() {
		return "Export the diagram as Huppaal XML";
	}
	
	@Override
	public boolean canExecute(ICustomContext context) {
		return true;
	}

	@Override
	public void execute(ICustomContext context) {
		System.out.println("jou!");
	}

}
