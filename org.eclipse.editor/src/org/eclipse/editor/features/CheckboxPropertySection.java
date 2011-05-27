package org.eclipse.editor.features;

import org.eclipse.editor.AbstractPropertySection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

public abstract class CheckboxPropertySection<T> extends AbstractPropertySection<T, Boolean> {
	protected Button checkbox;
	protected void createEditElement(TabbedPropertySheetWidgetFactory factory, Composite composite) {
		checkbox = factory.createButton(composite, "", SWT.CHECK);

		FormData data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		checkbox.setLayoutData(data);
		checkbox.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(SelectionEvent _) {
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
}
