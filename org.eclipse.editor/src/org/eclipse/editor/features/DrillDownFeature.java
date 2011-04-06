package org.eclipse.editor.features;

import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Iterables.find;
import static java.util.Arrays.asList;
import static org.eclipse.editor.EditorUtil.cast;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.editor.Log;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IPeService;
import org.eclipse.graphiti.ui.editor.DiagramEditor;
import org.eclipse.graphiti.ui.editor.DiagramEditorInput;
import org.eclipse.graphiti.ui.features.AbstractDrillDownFeature;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.google.common.base.Function;

public class DrillDownFeature extends AbstractDrillDownFeature {
	private static Logger log = Log.getLogger();

	public DrillDownFeature(IFeatureProvider fp) {
		super(fp);
	}

	@Override
	public String getName() {
		return "Open subdiagram";
	}

	@Override
	public String getDescription() {
		return "Open the subdiagram associated with this item";
	}

	@Override
	public boolean canExecute(ICustomContext context) {
		return getBusinessObject(context) != null;
	}

	private EClass getBusinessObject(ICustomContext context) {
		List<PictogramElement> pes = asList(context.getPictogramElements());
		if (pes.size() != 1)
			return null;

		return (EClass) find(transform(pes, toBusinessObject()), instanceOf(EClass.class), null);
	}

	private Function<PictogramElement, Object> toBusinessObject() {
		return new Function<PictogramElement, Object>() {
			@Override
			public Object apply(PictogramElement pe) {
				return getBusinessObjectForPictogramElement(pe);
			}
		};
	}

	@Override
	protected Collection<Diagram> getDiagrams() {
		Collection<EObject> contents = getDiagram().eResource().getContents();
		return transform(filter(contents, instanceOf(Diagram.class)), cast(Diagram.class));
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
			Diagram newDiagram = createNewDiagram(businessObject.getName() + "-sub");
			openDiagramEditor(newDiagram, getDiagramEditor().getEditingDomain(), getFeatureProvider().getDiagramTypeProvider().getProviderId(), false);
			link(newDiagram, businessObject);
		} catch (Exception e) {
			log.error("Unable to create new diagram: " + e.getMessage(), e);
		}
	}

	private Diagram createNewDiagram(String name) throws CoreException {
		IFeatureProvider featureProvider = getFeatureProvider();
		Diagram currentDiagram = featureProvider.getDiagramTypeProvider().getDiagram();
		IPeService peService = Graphiti.getPeService();
		Diagram newDiagram = peService.createDiagram(currentDiagram.getDiagramTypeId(), name, currentDiagram.isSnapToGrid());
		currentDiagram.eResource().getContents().add(newDiagram);

		return newDiagram;
	}

	public void openDiagramEditor(Diagram diagram, TransactionalEditingDomain domain, String providerId, boolean disposeEditingDomain) {
		try {
			DiagramEditorInput diagramEditorInput = DiagramEditorInput.createEditorInput(diagram, domain, providerId, disposeEditingDomain);
			IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IDE.openEditor(workbenchPage, diagramEditorInput, DiagramEditor.DIAGRAM_EDITOR_ID);
		} catch (Exception e) {
			log.error("Unable to open editor: " + e.getMessage(), e);
		}
	}
}
