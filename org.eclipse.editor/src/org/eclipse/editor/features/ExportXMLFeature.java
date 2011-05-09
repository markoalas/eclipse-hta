package org.eclipse.editor.features;

import java.io.FileWriter;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.editor.Log;
import org.eclipse.editor.editor.State;
import org.eclipse.editor.huppaal.XmlSerializer;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.google.common.collect.Lists;

public class ExportXMLFeature extends AbstractCustomFeature {
	private static Logger log = Log.getLogger();

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
		String fileName = askFileName();
		if (fileName == null) {
			log.info("User cancelled.");
			return;
		}

		exportToFile(fileName);
	}

	private void exportToFile(String fileName) {
		log.info("Exporting to file: " + fileName);

		List<EObject> states = getStatesForCurrentDiagram();

		FileWriter fw = null;
		try {
			fw = new FileWriter(fileName);
			XmlSerializer xmlSerializer = new XmlSerializer();
			xmlSerializer.toXml(fw, states.toArray(new EObject[states.size()]));
			
			log.info("Export done");
		} catch (Exception e) {
			log.error("Unable to export: " + e.getMessage(), e);
			showErrorDialog(e);
		} finally {
			try {
				fw.close();
			} catch (Exception e) {
			}
		}
	}

	private void showErrorDialog(Exception e) {
		MessageDialog messageDialog = new MessageDialog(getShell(), "Error", null, e.getMessage(), 0, new String[] { IDialogConstants.OK_LABEL }, 0);
		messageDialog.open();
	}

	private List<EObject> getStatesForCurrentDiagram() {
		List<EObject> states = Lists.newArrayList();
		for (PictogramElement pe : getDiagram().getChildren()) {
			for (Object bo : getAllBusinessObjectsForPictogramElement(pe)) {
				if (bo instanceof State) {
					states.add((State) bo);
				}
			}
		}
		return states;
	}

	private String askFileName() {
		FileDialog fileDialog = new FileDialog(getShell(), SWT.SAVE);
		fileDialog.setText("Save As...");
		fileDialog.setOverwrite(true);
		fileDialog.setFilterExtensions(new String[] { "*.xml", "*.*" });

		return fileDialog.open();
	}

	private Shell getShell() {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		return shell;
	}

}
