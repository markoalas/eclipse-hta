package org.eclipse.editor.features;

import org.eclipse.editor.AbstractPropertySection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

public abstract class CheckboxPropertySection<T> extends AbstractPropertySection<T>{
	protected Button checkbox;
	protected void createRow(TabbedPropertySheetWidgetFactory factory, Composite composite) {
		checkbox = factory.createButton(composite, "", SWT.CHECK);

		FormData data;
		data = new FormData();
		CLabel invariantLabel = factory.createCLabel(composite, getLabel());
		invariantLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		checkbox.setLayoutData(data);
		checkbox.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if (getValue() != checkbox.getSelection()) {
					doInTransaction(new Runnable() {
						@Override
						public void run() {
							setValue(checkbox.getSelection());
						}
					});
				}
			}
		});
	}

	@Override
	public void refresh() {
		checkbox.setSelection(getValue());
	}
	
	protected abstract boolean getValue();
	protected abstract void setValue(boolean value);
	protected abstract String getLabel();
}
