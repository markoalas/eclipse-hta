package org.eclipse.editor;

import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Iterables.transform;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.graphiti.mm.pictograms.Diagram;

import com.google.common.base.Function;

public class EditorUtil {

	public static Iterable<Diagram> getDiagrams(IProject p) {
		final ResourceSet rSet = new ResourceSetImpl();

		return filter(
				transform(getDiagramFiles(p), getDiagramFromFile(rSet)), 
				notNull());
	}

	private static Function<IFile, Diagram> getDiagramFromFile(final ResourceSet rSet) {
		return new Function<IFile, Diagram>() {
			@Override
			public Diagram apply(IFile file) {
				return getDiagramFromFile(file, rSet);
			}
		};
	}

	private static List<IFile> getDiagramFiles(IContainer folder) {
		List<IFile> ret = new ArrayList<IFile>();

		try {
			IResource[] members = folder.members();

			for (IResource resource : members) {
				if (resource instanceof IContainer) {
					ret.addAll(getDiagramFiles((IContainer) resource));
				} else if (resource instanceof IFile) {
					IFile file = (IFile) resource;
					if (file.getName().endsWith(".diagram")) {
						ret.add(file);
					}
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public static Diagram getDiagramFromFile(IFile file, ResourceSet resourceSet) {
		URI resourceURI = getFileURI(file, resourceSet);

		try {
			Resource resource = resourceSet.getResource(resourceURI, true);

			if (resource != null) {
				EList<EObject> contents = resource.getContents();
				return (Diagram) find(contents, instanceOf(Diagram.class), null);
			}
		} catch (WrappedException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static URI getFileURI(IFile file, ResourceSet resourceSet) {
		final String pathName = file.getFullPath().toString();
		URI resourceURI = URI.createFileURI(pathName);
		resourceURI = resourceSet.getURIConverter().normalize(resourceURI);
		return resourceURI;
	}
}
