package org.eclipse.editor.huppaal;

import org.eclipse.editor.huppaal.HtaGenerator.GeneratedObject;
import org.eclipse.editor.huppaal.model.Component;
import org.eclipse.editor.huppaal.model.Connection;
import org.eclipse.editor.huppaal.model.Entry;
import org.eclipse.editor.huppaal.model.Exit;
import org.eclipse.editor.huppaal.model.Globalinit;
import org.eclipse.editor.huppaal.model.Label;
import org.eclipse.editor.huppaal.model.Location;
import org.eclipse.editor.huppaal.model.Name;
import org.eclipse.editor.huppaal.model.Source;
import org.eclipse.editor.huppaal.model.Target;
import org.eclipse.editor.huppaal.model.Template;
import org.eclipse.editor.huppaal.model.Transition;

public class ModelFactory {
	private static int idCounter = 0;
	
	public static Name createName(String value) {
		Name name = new Name();
		name.setvalue(value);
		return name;
	}
	
	public static Location createLocation(Template template, String name) {
		Location location = new Location();
		location.setId(template.getName().getvalue() + "." + name + "." + (idCounter++));
		location.setName(createName(name));
		return location;
	}
	
	public static Entry createEntry(Template template, String name) {
		Entry entry = new Entry();
		entry.setId(template.getName().getvalue() +  "." + name);
		entry.setName(createName(name));
		return entry;
	}

	public static Exit createExit(Template template, String name) {
		Exit exit = new Exit();
		exit.setId(template.getName().getvalue() +  "." + name + (idCounter++));
		exit.setName(createName(name));
		return exit;
	}

	public static Connection createConnectionTo(GeneratedObject targetRef) {
		Connection connection = new Connection();
		Target target = new Target();
		target.setRef(targetRef.getTarget());
		target.setEntryref(targetRef.getEntry());
		connection.setTarget(target);
		return connection;
	}

	public static Connection createConnectionFrom(GeneratedObject sourceRef) {
		Connection connection = new Connection();
		Source source = new Source();
		source.setRef(sourceRef.getTarget());
		source.setExitref(sourceRef.getExit());
		connection.setSource(source);
		return connection;
	}
	
	public static Transition createTransition(GeneratedObject sourceRef, GeneratedObject targetRef) {
		Transition transition = new Transition();
		
		Source source = new Source();
		source.setRef(sourceRef.getTarget());
		source.setExitref(sourceRef.getExit());
		transition.setSource(source);
		
		Target target = new Target();
		target.setRef(targetRef.getTarget());
		target.setEntryref(targetRef.getEntry());
		transition.setTarget(target);
		
		return transition;
	}
	
	public static Template createTemplate(String name) {
		Template template = new Template();
		template.setType("XOR");
		template.setName(createName(name));
		
		return template;
	}
	
	public static Globalinit createGlobalinit(String instantiationname, Object ref) {
		Globalinit globalinit = new Globalinit();
		globalinit.setInstantiationname(instantiationname);
		globalinit.setRef(ref);
		return globalinit;
	}
	
	public static Label createLabel(String kind, String value) {
		Label label = new Label();
		label.setKind(kind);
		label.setvalue(value);
		return label;
	}
	
	public static Component createComponent(Template template, Template subTemplate) {
		Component c = new Component();
		c.setInstantiates(subTemplate.getName().getvalue());
		c.setId(template.getName().getvalue() + "." + (idCounter++));
		c.setName(createName("N" + (idCounter++)));
		return c;
	}


}
