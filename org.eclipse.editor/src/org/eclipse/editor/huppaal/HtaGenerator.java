package org.eclipse.editor.huppaal;

import static org.eclipse.editor.huppaal.ModelFactory.createComponent;
import static org.eclipse.editor.huppaal.ModelFactory.createConnectionFrom;
import static org.eclipse.editor.huppaal.ModelFactory.createConnectionTo;
import static org.eclipse.editor.huppaal.ModelFactory.createEntry;
import static org.eclipse.editor.huppaal.ModelFactory.createExit;
import static org.eclipse.editor.huppaal.ModelFactory.createGlobalinit;
import static org.eclipse.editor.huppaal.ModelFactory.createLabel;
import static org.eclipse.editor.huppaal.ModelFactory.createLocation;
import static org.eclipse.editor.huppaal.ModelFactory.createTemplate;
import static org.eclipse.editor.huppaal.ModelFactory.createTransition;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Stack;

import org.eclipse.editor.EditorUtil;
import org.eclipse.editor.editor.Connector;
import org.eclipse.editor.editor.Edge;
import org.eclipse.editor.editor.State;
import org.eclipse.editor.huppaal.model.Committed;
import org.eclipse.editor.huppaal.model.Component;
import org.eclipse.editor.huppaal.model.Entry;
import org.eclipse.editor.huppaal.model.Exit;
import org.eclipse.editor.huppaal.model.Hta;
import org.eclipse.editor.huppaal.model.Location;
import org.eclipse.editor.huppaal.model.Template;
import org.eclipse.editor.huppaal.model.Transition;
import org.eclipse.editor.huppaal.model.Urgent;
import org.eclipse.emf.ecore.EObject;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;

public class HtaGenerator {
	private Map<String, GeneratedObject> visitedLocations = Maps.newHashMap();
	private Map<String, Template> templates = Maps.newHashMap();
	private Map<Template, Component> components = Maps.newHashMap();
	private Hta hta;
	
	public Hta generateModel(EObject... objects) {
		hta = new Hta();

		Template template = createTemplate("Template");
		hta.getTemplate().add(template);

		State s = findInitialState(objects);

		Entry entry = createEntry(template, "ENTRY");
		Stack<Template> templateStack = new Stack<Template>();
		templateStack.push(template);
		GeneratedObject startLocation = generateFor(templateStack, s);
		entry.getConnection().add(createConnectionTo(startLocation));
		template.getEntry().add(entry);
		template.getExit().add(createExit(template, "EXIT"));

		hta.setInstantiation("template := Template();");
		hta.setSystem("system template;");

		hta.getGlobalinit().add(createGlobalinit("template", entry));
		return hta;
	}

	private GeneratedObject generateFor(Stack<Template> templates, EObject root) {
		if (root instanceof State) {
			return generateForState(templates, (State) root);

		} else if (root instanceof Connector) {
			return generateForConnector(templates, (Connector) root);
		}

		throw new IllegalArgumentException("Unknown EObject type: " + root.getClass().getSimpleName());
	}

	private GeneratedObject generateForState(Stack<Template> templates, State state) {
		Template template = templates.peek();
		
		String mapKey = "state." + template.getName().getvalue() + "." + state.getName();
		if (visitedLocations.containsKey(mapKey)) {
			return visitedLocations.get(mapKey);
		}

		Location location = createLocation(template, state.getName());
		template.getLocation().add(location);

		GeneratedObject ret = new GeneratedObject(location);
		visitedLocations.put(mapKey, ret);

		location.getLabel().add(createLabel("invariant", state.getInvariant()));
		if (state.isUrgent()) {
			location.setUrgent(new Urgent());
		}
		if (state.isCommitted()) {
			location.setCommitted(new Committed());
		}

		for (Edge e : state.getOutgoingEdges()) {
			createTransitionForEdge(templates, location, e);
		}

		return ret;
	}

	private GeneratedObject generateForConnector(Stack<Template> templates, Connector connector) {
		Template template = templates.peek(); 
		String templateName = connector.getDiagram().getName();
		Template subTemplate = getCachedTemplate(templateName);

		String mapKey = "connector." + subTemplate.getName().getvalue() + "." + connector.getName();
		if (visitedLocations.containsKey(mapKey)) {
			return visitedLocations.get(mapKey);
		}

		if (!subTemplate.equals(template)) { // ENTRY
			
			Component component = getCachedComponent(template, subTemplate);
			
			Entry entry = createEntry(subTemplate, "ENTRY");
			subTemplate.getEntry().add(entry);

			GeneratedObject ret = new GeneratedObject(component, entry);
			visitedLocations.put(mapKey, ret);

			templates.push(subTemplate);
			for (Edge e : connector.getOutgoingEdges()) {
				entry.getConnection().add(createConnectionTo(generateFor(templates, e.getEnd())));
			}

			templates.pop();
			return ret;

		} else { // EXIT
			final Exit exit = createExit(subTemplate, "EXIT");
			subTemplate.getExit().add(exit);
			final Component component = components.get(template);

			GeneratedObject ret = new GeneratedObject(exit) {
				@Override
				public Transition connectionFrom(GeneratedObject o, Template t) {
					exit.getConnection().add(createConnectionFrom(o));
					return null;
				}
			};
			visitedLocations.put(mapKey, ret);

			Template currentTemplate = templates.pop();
			for (Edge e : connector.getOutgoingEdges()) {
				GeneratedObject gen = generateFor(templates, e.getEnd());
				gen.connectionFrom(new GeneratedObject(component, exit), templates.peek());
			}

			templates.push(currentTemplate);
			return ret;
		}
	}

	private Component getCachedComponent(Template template, Template subTemplate) {
		if (!components.containsKey(subTemplate)) {
			Component component = createComponent(template, subTemplate);
			template.getComponent().add(component);
			components.put(subTemplate, component);
		}
		
		Component component = components.get(subTemplate);
		return component;
	}

	private Template getCachedTemplate(String templateName) {
		if (!templates.containsKey(templateName)) {
			Template t = createTemplate(templateName);
			templates.put(templateName, t);
			hta.getTemplate().add(t);
		}

		Template subTemplate = templates.get(templateName);
		return subTemplate;
	}

	private Transition createTransitionForEdge(Stack<Template> templates, Location location, Edge edge) {
		GeneratedObject generatedObject = generateFor(templates, edge.getEnd());
		Transition transition = generatedObject.connectionFrom(new GeneratedObject(location), templates.peek());

		if (transition != null) {
			transition.getLabel().add(createLabel("guard", edge.getGuard()));

			if (!EditorUtil.isEmpty(edge.getSelect())) {
				transition.getLabel().add(createLabel("select", edge.getSelect()));
			}
			transition.getLabel().add(createLabel("assignment", edge.getUpdate()));
			transition.getLabel().add(createLabel("synchronisation", edge.getSync()));
			if (!EditorUtil.isEmpty(edge.getComments())) {
				transition.getLabel().add(createLabel("comments", edge.getComments()));
			}
		}

		return transition;
	}

	private State findInitialState(EObject[] objects) {
		Collection<EObject> initialStates = Collections2.filter(Arrays.asList(objects), new Predicate<EObject>() {
			@Override
			public boolean apply(EObject obj) {
				return obj instanceof State && ((State) obj).isInitial();
			}
		});

		if (initialStates.size() != 1) {
			throw new ValidationException("Model must have exactly one initial state.");
		}

		return (State) initialStates.iterator().next();
	}

	public static class GeneratedObject {
		private Object object;
		private Entry entry;
		private Exit exit;

		public GeneratedObject(Object object) {
			this.object = object;
		}

		public GeneratedObject(Object obj, Entry entry) {
			this.object = obj;
			this.entry = entry;
		}

		public GeneratedObject(Object object, Exit exit) {
			this.object = object;
			this.exit = exit;
		}

		public Object getTarget() {
			return object;
		}

		public Entry getEntry() {
			return entry;
		}

		public Exit getExit() {
			return exit;
		}

		public Transition connectionFrom(GeneratedObject o, Template t) {
			Transition transition = createTransition(o, GeneratedObject.this);
			t.getTransition().add(transition);
			return transition;
		}
	}

}
