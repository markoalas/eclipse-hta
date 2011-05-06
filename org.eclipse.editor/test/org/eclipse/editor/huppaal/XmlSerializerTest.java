package org.eclipse.editor.huppaal;

import static org.eclipse.editor.EditorUtil.nvl;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.StringWriter;
import java.util.List;

import org.eclipse.editor.editor.Edge;
import org.eclipse.editor.editor.EditorFactory;
import org.eclipse.editor.editor.State;
import org.eclipse.editor.huppaal.model.Entry;
import org.eclipse.editor.huppaal.model.Globalinit;
import org.eclipse.editor.huppaal.model.Hta;
import org.eclipse.editor.huppaal.model.Label;
import org.eclipse.editor.huppaal.model.Location;
import org.eclipse.editor.huppaal.model.Template;
import org.eclipse.editor.huppaal.model.Transition;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class XmlSerializerTest {
	XmlSerializer xmlSerializer;

	@Before
	public void before() {
		xmlSerializer = new XmlSerializer();
	}

	//@Test
	public void toXml() throws Exception {
		StringWriter sw = new StringWriter();
		xmlSerializer.toXml(sw);
		System.out.println(sw);
	}

	@Test
	public void emptyDocument() throws Exception {
		try {
			xmlSerializer.generateModel();
			fail();
		} catch (Exception e) {
			assertEquals("Model must have exactly one initial state.", e.getMessage());
		}
	}
	
	@Test
	public void onlyInitialState() throws Exception {
		State state = EditorFactory.eINSTANCE.createState();
		state.setInitial(true);
		state.setName("A");
		state.setInvariant("i < 10");
		state.setUrgent(true);
		state.setCommitted(true);
		
		Hta hta = xmlSerializer.generateModel(state);
		
		assertEquals("", nvl(hta.getDeclaration()));
		assertEquals(1, hta.getTemplate().size());
		
		Location l = hta.getTemplate().get(0).getLocation().get(0);
		assertEquals("Template.A", l.getId());
		assertEquals("A", l.getName().getvalue());
		assertEquals("i < 10", findByKind(l.getLabel(), "invariant").getvalue());
		assertEquals("true", findByKind(l.getLabel(), "initial").getvalue());
		assertEquals("true", findByKind(l.getLabel(), "urgent").getvalue());
		assertEquals("true", findByKind(l.getLabel(), "committed").getvalue());
		
		assertEquals("template := Template();", hta.getInstantiation());
		assertEquals("system template;", hta.getSystem());
		List<Globalinit> globalinits = hta.getGlobalinit();
		assertEquals(1, globalinits.size());
		assertEquals("template", globalinits.get(0).getInstantiationname());
		assertEquals("Template.ENTRY", ((Entry)globalinits.get(0).getRef()).getId());
	}

	@Test
	public void twoStatesAndAnEdge() throws Exception {
		State stateA = createState("A");
		stateA.setInitial(true);
		State stateB = createState("B");
		Edge edge = createEdge(stateA, stateB);
		edge.setGuard("a == 0");
		edge.setSelect("select");
		edge.setSync("s?");
		edge.setUpdate("a = 1");
		edge.setComments("comment");
		
		Hta hta = xmlSerializer.generateModel(stateA, stateB);
		Template template = hta.getTemplate().get(0);
		
		List<Location> locations = template.getLocation();
		assertEquals(2, locations.size());
		assertEquals("A", locations.get(0).getName().getvalue());
		assertEquals("B", locations.get(1).getName().getvalue());
		
		List<Transition> transitions = template.getTransition();
		assertEquals(1, transitions.size());
		Transition t = transitions.get(0);
		assertEquals(locations.get(0), t.getSource().getRef());
		assertEquals(locations.get(1), t.getTarget().getRef());
		assertEquals("a == 0", findByKind(t.getLabel(), "guard").getvalue());
		assertEquals("select", findByKind(t.getLabel(), "select").getvalue());
		assertEquals("s?", findByKind(t.getLabel(), "sync").getvalue());
		assertEquals("a = 1", findByKind(t.getLabel(), "update").getvalue());
		assertEquals("comment", findByKind(t.getLabel(), "comment").getvalue());
	}
	
	private Label findByKind(Iterable<Label> labels, final String kind) {
		return Iterables.find(labels, new Predicate<Label>() {
			@Override
			public boolean apply(Label label) {
				return label.getKind().equals(kind);
			}
		});
	}
	
	@Test
	public void twoOutgoingEdges() throws Exception {
		State stateA = createState("A");
		stateA.setInitial(true);
		State stateB = createState("B");
		State stateC = createState("C");
		createEdge(stateA, stateB);
		createEdge(stateA, stateC);
		
		Hta hta = xmlSerializer.generateModel(stateA, stateB);
		Template template = hta.getTemplate().get(0);
		
		List<Location> locations = template.getLocation();
		assertEquals(3, locations.size());
		
		List<Transition> transitions = template.getTransition();
		assertEquals(2, transitions.size());
		assertTrue(hasTransition(transitions, locations.get(0), locations.get(1)));
		assertTrue(hasTransition(transitions, locations.get(0), locations.get(2)));
	}
	
	@Test
	public void cyclic() throws Exception {
		State stateA = createState("A");
		stateA.setInitial(true);
		State stateB = createState("B");
		createEdge(stateA, stateB);
		createEdge(stateB, stateA);
		
		Hta hta = xmlSerializer.generateModel(stateA, stateB);
		Template template = hta.getTemplate().get(0);
		
		List<Location> locations = template.getLocation();
		assertEquals(2, locations.size());
		
		List<Transition> transitions = template.getTransition();
		assertEquals(2, transitions.size());
		assertTrue(hasTransition(transitions, locations.get(0), locations.get(1)));
		assertTrue(hasTransition(transitions, locations.get(1), locations.get(0)));
	}
	
	private boolean hasTransition(Iterable<Transition> transitions, final Object from, final Object to) {
		return Iterables.any(transitions, new Predicate<Transition>() {
			@Override
			public boolean apply(Transition t) {
				return t.getSource().getRef().equals(from) && t.getTarget().getRef().equals(to);
			}
		});
	}

	private State createState(String value) {
		State state = EditorFactory.eINSTANCE.createState();
		state.setName(value);
		return state;
	}
	
	private Edge createEdge(State start, State end) {
		Edge edge = EditorFactory.eINSTANCE.createEdge();
		edge.setStart(start);
		edge.setEnd(end);
		return edge;
	}

}
