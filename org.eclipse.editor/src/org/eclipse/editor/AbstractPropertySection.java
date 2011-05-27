package org.eclipse.editor;

import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.ui.platform.GFPropertySection;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

public abstract class AbstractPropertySection<ObjectType, PropertyType> extends GFPropertySection implements ITabbedPropertyConstants {
	@SuppressWarnings("unchecked")
	protected ObjectType getBusinessObject() {
		PictogramElement pe = getSelectedPictogramElement();

		if (pe != null) {
			return (ObjectType) Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement(pe);
		}

		return null;
	}

	@Override
	public void createControls(Composite parent, TabbedPropertySheetPage tabbedPropertySheetPage) {
		super.createControls(parent, tabbedPropertySheetPage);

		parent.setLayout(new FillLayout());

		TabbedPropertySheetWidgetFactory factory = getWidgetFactory();
		Composite composite = factory.createFlatFormComposite(parent);

		FormData data = new FormData();
		CLabel label = factory.createCLabel(composite, getLabel());
		label.setLayoutData(data);

		createEditElement(factory, composite);
	}

	protected void doInTransaction(final Runnable runnable) {
		TransactionalEditingDomain editingDomain = getDiagramEditor().getEditingDomain();
		editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {
			protected void doExecute() {
				runnable.run();
			}
		});
	}

	protected abstract void createEditElement(TabbedPropertySheetWidgetFactory factory, Composite composite);

	protected abstract PropertyType getValue();

	protected abstract void setValue(PropertyType value);

	protected abstract String getLabel();

}
