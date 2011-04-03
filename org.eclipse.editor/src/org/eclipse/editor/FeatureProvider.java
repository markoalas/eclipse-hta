package org.eclipse.editor;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Iterables.toArray;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.editor.features.AddEClassFeature;
import org.eclipse.editor.features.AddEReferenceFeature;
import org.eclipse.editor.features.AssociateDiagramToEClassFeature;
import org.eclipse.editor.features.CreateEReferenceFeature;
import org.eclipse.editor.features.CreateFeature;
import org.eclipse.editor.features.DrillDownFeature;
import org.eclipse.editor.features.LayoutFeature;
import org.eclipse.editor.features.RenameFeature;
import org.eclipse.editor.features.ResizeFeature;
import org.eclipse.editor.features.UpdateFeature;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.ICreateConnectionFeature;
import org.eclipse.graphiti.features.ICreateFeature;
import org.eclipse.graphiti.features.IFeature;
import org.eclipse.graphiti.features.ILayoutFeature;
import org.eclipse.graphiti.features.IResizeShapeFeature;
import org.eclipse.graphiti.features.IUpdateFeature;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.context.ILayoutContext;
import org.eclipse.graphiti.features.context.IPictogramElementContext;
import org.eclipse.graphiti.features.context.IResizeShapeContext;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.custom.ICustomFeature;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.ui.features.DefaultFeatureProvider;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class FeatureProvider extends DefaultFeatureProvider {
	public FeatureProvider(IDiagramTypeProvider dtp) {
		super(dtp);
	}

	@Override
	public IAddFeature getAddFeature(IAddContext context) {
		if (context.getNewObject() instanceof EClass) {
			return new AddEClassFeature(this);
		} else if (context.getNewObject() instanceof EReference) {
			return new AddEReferenceFeature(this);
		}

		return super.getAddFeature(context);
	}

	@Override
	public ICreateFeature[] getCreateFeatures() {
		return new ICreateFeature[] { new CreateFeature(this) };
	}

	@Override
	public ICreateConnectionFeature[] getCreateConnectionFeatures() {
		return new ICreateConnectionFeature[] { new CreateEReferenceFeature(this) };
	}

	@Override
	public IFeature[] getDragAndDropFeatures(IPictogramElementContext context) {
		return getCreateConnectionFeatures();
	}

	@Override
	public IResizeShapeFeature getResizeShapeFeature(IResizeShapeContext context) {
		if (getBusinessObjectForPictogramElement(context.getShape()) instanceof EClass) {
			return new ResizeFeature(this);
		}
		return super.getResizeShapeFeature(context);
	}

	@Override
	public ILayoutFeature getLayoutFeature(ILayoutContext context) {
		if (getBusinessObjectForPictogramElement(context.getPictogramElement()) instanceof EClass) {
			return new LayoutFeature(this);
		}
		return super.getLayoutFeature(context);
	}

	@Override
	public IUpdateFeature getUpdateFeature(IUpdateContext context) {
		if (getBusinessObjectForPictogramElement(context.getPictogramElement()) instanceof EClass) {
			return new UpdateFeature(this);
		}
		return super.getUpdateFeature(context);
	}

	@Override
	public ICustomFeature[] getCustomFeatures(ICustomContext context) {
		return new ICustomFeature[] { new RenameFeature(this), new DrillDownFeature(this), new AssociateDiagramToEClassFeature(this) };
	}

	HashMap<String, Set<String>> linkMap = newHashMap();

	@Override
	public void link(PictogramElement pictogramElement, Object[] businessObjects) {
		if (linkMap.containsKey(pictogramElement) == false) {
			linkMap.put(pictogramElement.eResource().getURI().toString(), new HashSet<String>());
		}
		
		if (businessObjects[0] instanceof EClass) {
			linkMap.get(pictogramElement).addAll(Lists.transform(asList(businessObjects), new Function<Object, String>() {
				@Override
				public String apply(Object bo) {
					return ((EClass)bo).getEPackage().getNsURI().toString();
				}
			}));
		}

		super.link(pictogramElement, businessObjects);
	}

	@Override
	public Object getBusinessObjectForPictogramElement(PictogramElement pictogramElement) {
		if (linkMap.containsKey(pictogramElement)) {
			return getFirst(linkMap.get(pictogramElement), null);
		}

		return super.getBusinessObjectForPictogramElement(pictogramElement);
	}

	@Override
	public PictogramElement[] getAllPictogramElementsForBusinessObject(final Object businessObject) {
		Iterable<PictogramElement> fromMap = transform(filter(linkMap.entrySet(), containsObject(businessObject)), toPictogramElements());
		if (!isEmpty(fromMap)) {
			return toArray(fromMap, PictogramElement.class);
		}

		return super.getAllPictogramElementsForBusinessObject(businessObject);
	}

	private Function<? super Entry<String, Set<String>>, PictogramElement> toPictogramElements() {
		return new Function<Entry<String, Set<String>>, PictogramElement>() {
			@Override
			public PictogramElement apply(Entry<String, Set<String>> entry) {
				//return (PictogramElement)new ResourceSetImpl().getResource(URI.createURI(entry.getKey()), true).getEObject(arg0);
			}
		};
	}

	private Predicate<Entry<String, Set<String>>> containsObject(final Object o) {
		return new Predicate<Map.Entry<String, Set<String>>>() {
			@Override
			public boolean apply(Entry<String, Set<String>> mapEntry) {
				return mapEntry.getValue().contains(o);
			}
		};
	}
}
