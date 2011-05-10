package org.eclipse.editor.huppaal;

import static org.eclipse.editor.huppaal.ModelFactory.createComponent;
import static org.eclipse.editor.huppaal.ModelFactory.createConnection;
import static org.eclipse.editor.huppaal.ModelFactory.createEntry;
import static org.eclipse.editor.huppaal.ModelFactory.createExit;
import static org.eclipse.editor.huppaal.ModelFactory.createGlobalinit;
import static org.eclipse.editor.huppaal.ModelFactory.createLabel;
import static org.eclipse.editor.huppaal.ModelFactory.createLocation;
import static org.eclipse.editor.huppaal.ModelFactory.createTemplate;
import static org.eclipse.editor.huppaal.ModelFactory.createTransition;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Stack;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

import org.eclipse.editor.EditorUtil;
import org.eclipse.editor.editor.Connector;
import org.eclipse.editor.editor.Edge;
import org.eclipse.editor.editor.State;
import org.eclipse.editor.huppaal.model.Committed;
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

public class XmlSerializer {
	public void toXml(Writer sw, EObject... objects) throws PropertyException, JAXBException {
		Hta hta = generateModel(objects);
		writeHta(hta, sw);
	}

	private void writeHta(Hta hta, Writer writer) throws JAXBException, PropertyException {
		JAXBContext context = JAXBContext.newInstance(hta.getClass());
		Marshaller marshaller = context.createMarshaller();
		try {
			writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE hta SYSTEM \"huppaal-0.6.dtd\">\n");
		} catch (IOException e) {
		}
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		marshaller.marshal(hta, writer);
	}
	
	private Hta hta;

	protected Hta generateModel(EObject... objects) {
		hta = new Hta();

		Template template = createTemplate("Template");
		hta.getTemplate().add(template);

		State s = findInitialState(objects);

		Entry entry = createEntry(template, "ENTRY");
		Object startLocation = generateFor(template, s);
		entry.getConnection().add(createConnection(startLocation));
		template.getEntry().add(entry);
		template.getExit().add(createExit(template, "EXIT"));

		hta.setInstantiation("template := Template();");
		hta.setSystem("system template;");

		hta.getGlobalinit().add(createGlobalinit("template", entry));
		return hta;
	}

	// connectorisse tagasi minek annab errori?
	private Map<String, Location> visitedLocations = Maps.newHashMap();
	private Map<String, Template> templates = Maps.newHashMap();
	private Stack<Template> templatesStack = new Stack<Template>(); 
	
	private Object generateFor(Template template, EObject root) {
		if (root instanceof State) {
			State state = (State) root;
			String mapKey = template.getName().getvalue() + "." + state.getName();
			if (visitedLocations.containsKey(mapKey)) {
				return visitedLocations.get(mapKey);
			}

			Location location = createLocation(template, state.getName());
			template.getLocation().add(location);
			visitedLocations.put(mapKey, location);

			location.getLabel().add(createLabel("invariant", state.getInvariant()));
			if (state.isUrgent()) {
				location.setUrgent(new Urgent());
			}
			if (state.isCommitted()) {
				location.setCommitted(new Committed());
			}

			for (Edge e : state.getOutgoingEdges()) {
				Transition transition = createTransitionForEdge(template, location, e);
				template.getTransition().add(transition);
			}

			return location;
		} else if (root instanceof Connector) {
			Connector connector = (Connector)root;
			
			String templateName = connector.getDiagram().getName();
			if (!templates.containsKey(templateName)) {
				Template t = createTemplate(templateName);
				templates.put(templateName, t);
				hta.getTemplate().add(t);
			}
			
			Template subTemplate = templates.get(templateName);
			if (!templateName.equals(template.getName().getvalue())) {
				templatesStack.push(template);
				
				template.getComponent().add(createComponent(template, subTemplate));
				
				Entry entry = createEntry(subTemplate, "ENTRY");
				subTemplate.getEntry().add(entry);
			
				for (Edge e : connector.getOutgoingEdges()) {
					entry.getConnection().add(createConnection(generateFor(subTemplate, e.getEnd())));
				}
				
				return entry;
			}
			else {
				Exit exit = createExit(subTemplate, "EXIT");
				subTemplate.getExit().add(exit);
				Template upperTemplate = templatesStack.pop();
				for (Edge e : connector.getOutgoingEdges()) {
					upperTemplate.getTransition().add(createTransition(exit, generateFor(upperTemplate, e.getEnd())));
				}
				
				return exit;
			}
		}

		throw new IllegalArgumentException("Unknown EObject type: " + root.getClass().getSimpleName());
	}

	private Transition createTransitionForEdge(Template template, Location location, Edge e) {
		Transition transition = createTransition(location, generateFor(template, e.getEnd()));
		transition.getLabel().add(createLabel("guard", e.getGuard()));

		if (!EditorUtil.isEmpty(e.getSelect())) {
			transition.getLabel().add(createLabel("select", e.getSelect()));
		}
		transition.getLabel().add(createLabel("assignment", e.getUpdate()));
		transition.getLabel().add(createLabel("synchronisation", e.getSync()));
		if (!EditorUtil.isEmpty(e.getComments())) {
			transition.getLabel().add(createLabel("comments", e.getComments()));
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
}
