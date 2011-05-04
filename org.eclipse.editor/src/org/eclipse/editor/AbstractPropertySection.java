package org.eclipse.editor;

import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.ui.platform.GFPropertySection;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

public abstract class AbstractPropertySection<T> extends GFPropertySection implements ITabbedPropertyConstants {
	@SuppressWarnings("unchecked")
	protected T getBusinessObject() {
		PictogramElement pe = getSelectedPictogramElement();

		if (pe != null) {
			return (T) Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement(pe);
		}

		return null;
	}

	@Override
	public void createControls(Composite parent, TabbedPropertySheetPage tabbedPropertySheetPage) {
		super.createControls(parent, tabbedPropertySheetPage);

		parent.setLayout(new FillLayout());

		TabbedPropertySheetWidgetFactory factory = getWidgetFactory();
		Composite composite = factory.createFlatFormComposite(parent);

		createRow(factory, composite);
	}

	protected void doInTransaction(final Runnable runnable) {
		TransactionalEditingDomain editingDomain = getDiagramEditor().getEditingDomain();
		editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {
			protected void doExecute() {
				runnable.run();
			}
		});
	}

	protected abstract void createRow(TabbedPropertySheetWidgetFactory factory, Composite composite);
}
