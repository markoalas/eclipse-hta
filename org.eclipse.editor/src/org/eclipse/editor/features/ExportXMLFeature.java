package org.eclipse.editor.features;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
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
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

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

		List<EObject> states = Lists.newArrayList();
		for (PictogramElement pe : getDiagram().getChildren()) {
			for (Object bo : getAllBusinessObjectsForPictogramElement(pe)) {
				if (bo instanceof State) {
					states.add((State) bo);
				}
			}
		}

		log.info("Exporting to file: " + fileName);
		XmlSerializer xmlSerializer = new XmlSerializer();
		try {
			FileOutputStream fos = new FileOutputStream(fileName);
			try {
				xmlSerializer.toXml(new OutputStreamWriter(fos), states.toArray(new EObject[] {}));
			} finally {
				fos.close();
			}
			log.info("Export done");
		} catch (Exception e) {
			log.error("Unable to export: " + e.getMessage(), e);
		}
		
	}

	private String askFileName() {
		Shell s = new Shell(Display.getCurrent());

		FileDialog fileDialog = new FileDialog(s, SWT.SAVE);
		fileDialog.setText("Save As...");
		fileDialog.setOverwrite(true);
		fileDialog.setFilterExtensions(new String[] { "*.xml", "*.*" });

		return fileDialog.open();
	}

}
