package org.eclipse.editor.features;

import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.collect.Iterables.all;
import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;

import com.google.common.base.Function;

public class AssociateDiagramToEClassFeature extends AbstractCustomFeature {

	public AssociateDiagramToEClassFeature(IFeatureProvider fp) {
		super(fp);
	}

	@Override
	public String getName() {
		return "Associate diagram";
	}

	@Override
	public String getDescription() {
		return "Associate the diagram with this EClass";
	}

	@Override
	public boolean canExecute(ICustomContext context) {
		return all(getBusinessObjects(context), instanceOf(EClass.class));
	}

	public void execute(ICustomContext context) {
		link(getDiagram(), getBusinessObjects(context));
	}

	private Iterable<Object> getBusinessObjects(ICustomContext context) {
		PictogramElement[] pes = context.getPictogramElements();
		Iterable<Object> businessObjects = transform(asList(pes), toBusinessObject());
		return businessObjects;
	}

	private Function<PictogramElement, Object> toBusinessObject() {
		return new Function<PictogramElement, Object>() {
			@Override
			public Object apply(PictogramElement pe) {
				return getBusinessObjectForPictogramElement(pe);
			}
		};
	}
}
