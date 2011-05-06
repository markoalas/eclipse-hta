package org.eclipse.editor.huppaal;

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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

import org.apache.log4j.Logger;
import org.eclipse.editor.Log;
import org.eclipse.editor.editor.Edge;
import org.eclipse.editor.editor.State;
import org.eclipse.editor.huppaal.model.Entry;
import org.eclipse.editor.huppaal.model.Hta;
import org.eclipse.editor.huppaal.model.Location;
import org.eclipse.editor.huppaal.model.Template;
import org.eclipse.editor.huppaal.model.Transition;
import org.eclipse.emf.ecore.EObject;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;

public class XmlSerializer {
	private static Logger log = Log.getLogger();

	public void toXml(Writer sw, EObject... objects) {
		try {
			Hta hta = generateModel(objects);
			writeHta(hta, sw);
		} catch (Exception e) {
			log.error("Unable to serialize to XML: " + e.getMessage(), e);
		}
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

	protected Hta generateModel(EObject... objects) {
		Hta hta = new Hta();

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
	
	private Map<String, Location> visitedLocations = Maps.newHashMap();

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
			location.getLabel().add(createLabel("initial", String.valueOf(state.isInitial())));
			location.getLabel().add(createLabel("committed", String.valueOf(state.isCommitted())));
			location.getLabel().add(createLabel("urgent", String.valueOf(state.isUrgent())));

			for (Edge e : state.getOutgoingEdges()) {
				Transition transition = createTransition(location, generateFor(template, e.getEnd()));
				transition.getLabel().add(createLabel("guard", e.getGuard()));
				transition.getLabel().add(createLabel("select", e.getSelect()));
				transition.getLabel().add(createLabel("update", e.getUpdate()));
				transition.getLabel().add(createLabel("sync", e.getSync()));
				// TODO comment or comments?
				transition.getLabel().add(createLabel("comment", e.getComments()));

				template.getTransition().add(transition);
			}

			return location;
		}

		throw new IllegalArgumentException("Unknown EObject type");
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
