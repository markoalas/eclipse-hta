package org.eclipse.editor.features;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.editor.EditorUtil;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.ui.features.AbstractDrillDownFeature;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class DrillDownFeature extends AbstractDrillDownFeature {

	private String newFileContents = "<?xml version=\"1.0\" encoding=\"ASCII\"?>"
			+ "<pi:Diagram xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:al=\"http://eclipse.org/graphiti/mm/algorithms\" xmlns:pi=\"http://eclipse.org/graphiti/mm/pictograms\" visible=\"true\" gridUnit=\"10\" diagramTypeId=\"org.eclipse.editor.diagramType\" name=\"omg\" snapToGrid=\"true\" showGuides=\"true\">"
			+ "  <graphicsAlgorithm xsi:type=\"al:Rectangle\" background=\"//@colors.1\" foreground=\"//@colors.0\" width=\"1000\" height=\"1000\"/>"
			+ "  <colors red=\"227\" green=\"238\" blue=\"249\"/>" + "  <colors red=\"255\" green=\"255\" blue=\"255\"/>" + "</pi:Diagram>";

	public DrillDownFeature(IFeatureProvider fp) {
		super(fp);
	}

	@Override
	public String getName() {
		return "Open subdiagram";
	}

	@Override
	public String getDescription() {
		return "Open the subdiagram associated with this EClass";
	}

	@Override
	public boolean canExecute(ICustomContext context) {
		PictogramElement[] pes = context.getPictogramElements();

		if (pes != null && pes.length == 1) {
			Object bo = getBusinessObjectForPictogramElement(pes[0]);
			if (bo instanceof EClass) {
				// then forward to super-implementation, which checks if
				// this EClass is associated with other diagrams
				return true;
			}
		}

		return false;
	}
	
	private EClass getBusinessObject(ICustomContext context) {
		PictogramElement[] pes = context.getPictogramElements();

		if (pes != null && pes.length == 1) {
			Object bo = getBusinessObjectForPictogramElement(pes[0]);
			if (bo instanceof EClass) {
				// then forward to super-implementation, which checks if
				// this EClass is associated with other diagrams
				return (EClass)bo;
			}
		}

		return null;
	}

	@Override
	protected Collection<Diagram> getDiagrams() {
		Collection<Diagram> result = Collections.emptyList();
		Resource resource = getDiagram().eResource();
		URI uri = resource.getURI();
		URI uriTrimmed = uri.trimFragment();
		if (uriTrimmed.isPlatformResource()) {
			String platformString = uriTrimmed.toPlatformString(true);
			IResource fileResource = ResourcesPlugin.getWorkspace().getRoot().findMember(platformString);

			if (fileResource != null) {
				IProject project = fileResource.getProject();
				result = EditorUtil.getDiagrams(project);
			}
		}

		return result;
	}

	@Override
	public void execute(ICustomContext context) {
		if (super.canExecute(context)) {
			super.execute(context);
		} else {
			createNewDiagramAndOpenIt(context);
		}
	}

	private void createNewDiagramAndOpenIt(ICustomContext context) {
		try {
			EClass businessObject = getBusinessObject(context);
			IFile file = createDiagramFile(businessObject.getName());
			ResourceSet rSet = new ResourceSetImpl();
			Diagram diagram = EditorUtil.getDiagramFromFile(file, rSet);
			link(diagram, businessObject);
			openEditor(file);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private IFile createDiagramFile(String diagramFileName) throws CoreException {
		Resource eResource = getDiagram().eResource();

		String platformString = eResource.getURI().toPlatformString(true);

		IResource fileResource = ResourcesPlugin.getWorkspace().getRoot().findMember(platformString);
		IProject project = fileResource.getProject();

		IFile file;
		do {
			file = project.getFile("/src/diagrams/" + diagramFileName + "-subdiagram.diagram");
			diagramFileName += "-1";
		} while (file.exists());
		
		file.create(new ByteArrayInputStream(newFileContents.getBytes()), true, null);
		
		return file;
	}

	protected void openEditor(IFile file) throws PartInitException {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IDE.openEditor(page, file);
	}
}
