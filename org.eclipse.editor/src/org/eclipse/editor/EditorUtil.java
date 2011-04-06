package org.eclipse.editor;

import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.graphiti.mm.pictograms.Diagram;

import com.google.common.base.Function;

public class EditorUtil {

	public static Iterable<Diagram> getDiagramsFromResource(Resource resource) {
		return transform(filter(asList(resource.getContents().toArray()), instanceOf(Diagram.class)), castToDiagram());
	}

	private static Function<Object, Diagram> castToDiagram() {
		return new Function<Object, Diagram>() {
			@Override
			public Diagram apply(Object o) {
				return (Diagram) o;
			}

		};
	}

	public static <T> Function<Object, T> cast(Class<T> clazz) {
		return new Function<Object, T>() {
			@SuppressWarnings("unchecked")
			@Override
			public T apply(Object o) {
				return (T) o;
			}
	
		};
	}

}
