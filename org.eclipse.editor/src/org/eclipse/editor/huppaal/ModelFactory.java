package org.eclipse.editor.huppaal;

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
	public static Name createName(String value) {
		Name name = new Name();
		name.setvalue(value);
		return name;
	}
	
	public static Location createLocation(Template template, String name) {
		Location location = new Location();
		location.setId(template.getName().getvalue() + "." + name);
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
		exit.setId(template.getName().getvalue() +  "." + name);
		exit.setName(createName(name));
		return exit;
	}

	public static Connection createConnection(Object targetRef) {
		Connection connection = new Connection();
		Target target = new Target();
		target.setRef(targetRef);
		connection.setTarget(target);
		return connection;
	}
	
	public static Transition createTransition(Object sourceRef, Object targetRef) {
		Transition transition = new Transition();
		
		Source source = new Source();
		source.setRef(sourceRef);
		transition.setSource(source);
		
		Target target = new Target();
		target.setRef(targetRef);
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
}
