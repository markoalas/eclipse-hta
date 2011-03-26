package org.eclipse.editor.features;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.graphiti.examples.common.ExampleUtil;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;

public class RenameFeature extends AbstractCustomFeature {

	public RenameFeature(IFeatureProvider fp) {
		super(fp);
	}
	
	@Override
	public String getName() {
		return "Rename EClass";
	}

	@Override
	public String getDescription() {
		return "Set new name for EClass";
	}

	@Override
	public boolean canExecute(ICustomContext context) {
		return getBusinessObject(context) != null;
	}

	private EClass getBusinessObject(ICustomContext context) {
		PictogramElement[] pes = context.getPictogramElements();
		if (pes != null && pes.length == 1) {
			Object bo = getBusinessObjectForPictogramElement(pes[0]);
			if (bo instanceof EClass) {
				return (EClass)bo;
			}
		}
		
		return null;
	}

	@Override
	public void execute(ICustomContext context) {
		EClass bo = getBusinessObject(context);
		String currentName = bo.getName();
		String newName = ExampleUtil.askString(getName(), getDescription(), currentName);
		if (newName != null) {
			bo.setName(newName);
		}
	}
}
