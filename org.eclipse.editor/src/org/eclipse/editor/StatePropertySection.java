package org.eclipse.editor;

import static org.eclipse.editor.EditorUtil.coalesce;

import java.util.Collection;

import org.eclipse.editor.editor.State;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.ui.platform.GFPropertySection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

public class StatePropertySection extends GFPropertySection implements ITabbedPropertyConstants {
	private Text nameText;
	private Text invariantText;
	private Button initialCheckbox;

	@Override
	public void createControls(Composite parent, TabbedPropertySheetPage tabbedPropertySheetPage) {
		super.createControls(parent, tabbedPropertySheetPage);

		parent.setLayout(new FillLayout());

		TabbedPropertySheetWidgetFactory factory = getWidgetFactory();
		Composite composite = factory.createFlatFormComposite(parent);

		createNameRow(factory, composite);
		// createInvariantRow(factory, composite);
		// createInitialRow(factory, composite);
		// createUrgentRow(factory, composite);
		// createCommittedRow(factory, composite);
	}

	private void createNameRow(TabbedPropertySheetWidgetFactory factory, Composite composite) {
		nameText = factory.createText(composite, "");

		FormData data;
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, VSPACE);
		nameText.setLayoutData(data);

		nameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				if (!getStateObject().getName().equals(nameText.getText())) {
					TransactionalEditingDomain editingDomain = getDiagramEditor().getEditingDomain();
					editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {
						protected void doExecute() {
							getStateObject().setName(nameText.getText());
						}
					});
				}
			}
		});

		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(nameText, -HSPACE);
		data.top = new FormAttachment(nameText, 0, SWT.CENTER);

		CLabel nameLabel = factory.createCLabel(composite, "Name:");
		nameLabel.setLayoutData(data);
	}

	private void createInvariantRow(TabbedPropertySheetWidgetFactory factory, Composite composite) {
		invariantText = factory.createText(composite, "");

		FormData data;
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(invariantText, -HSPACE);
		data.top = new FormAttachment(invariantText, 0, SWT.CENTER);

		CLabel invariantLabel = factory.createCLabel(composite, "Invariant:");
		invariantLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(20, VSPACE);

		invariantText.setLayoutData(data);
	}

	private void createInitialRow(TabbedPropertySheetWidgetFactory factory, Composite composite) {
		initialCheckbox = factory.createButton(composite, "", SWT.CHECK);

		FormData data;
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(initialCheckbox, -HSPACE);
		data.top = new FormAttachment(initialCheckbox, 0, SWT.CENTER);

		CLabel invariantLabel = factory.createCLabel(composite, "Initial:");
		invariantLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(50, VSPACE);

		initialCheckbox.setLayoutData(data);
	}

	private void createUrgentRow(TabbedPropertySheetWidgetFactory factory, Composite composite) {
		initialCheckbox = factory.createButton(composite, "", SWT.CHECK);

		FormData data;
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(initialCheckbox, -HSPACE);
		data.top = new FormAttachment(initialCheckbox, 0, SWT.CENTER);

		CLabel invariantLabel = factory.createCLabel(composite, "Initial:");
		invariantLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(50, VSPACE);

		initialCheckbox.setLayoutData(data);
	}

	private void createCommittedRow(TabbedPropertySheetWidgetFactory factory, Composite composite) {
		initialCheckbox = factory.createButton(composite, "", SWT.CHECK);

		FormData data;
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(initialCheckbox, -HSPACE);
		data.top = new FormAttachment(initialCheckbox, 0, SWT.CENTER);

		CLabel invariantLabel = factory.createCLabel(composite, "Initial:");
		invariantLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(50, VSPACE);

		initialCheckbox.setLayoutData(data);
	}

	@Override
	public void refresh() {
		State state = getStateObject();

		nameText.setText(coalesce(state.getName(), ""));
		// invariantText.setText(coalesce(state.getInvariant(), ""));
		// initialCheckbox.setData(state.isInitial());
	}

	private State getStateObject() {
		PictogramElement pe = getSelectedPictogramElement();
		State state = null;
		if (pe != null) {
			Object bo = Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement(pe);

			if (bo != null)
				state = (State) bo;
		}

		return state;
	}
}
