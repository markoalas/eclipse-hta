/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.eclipse.editor.editor.impl;

import java.util.Collection;

import org.eclipse.editor.editor.Connector;
import org.eclipse.editor.editor.Diagram;
import org.eclipse.editor.editor.Edge;
import org.eclipse.editor.editor.EditorPackage;
import org.eclipse.editor.editor.State;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.EObjectWithInverseResolvingEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Diagram</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.editor.editor.impl.DiagramImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.editor.editor.impl.DiagramImpl#getConnectors <em>Connectors</em>}</li>
 *   <li>{@link org.eclipse.editor.editor.impl.DiagramImpl#getStates <em>States</em>}</li>
 *   <li>{@link org.eclipse.editor.editor.impl.DiagramImpl#getSubdiagrams <em>Subdiagrams</em>}</li>
 *   <li>{@link org.eclipse.editor.editor.impl.DiagramImpl#getEdges <em>Edges</em>}</li>
 *   <li>{@link org.eclipse.editor.editor.impl.DiagramImpl#isIsParallel <em>Is Parallel</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class DiagramImpl extends EObjectImpl implements Diagram {
	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

	/**
	 * The cached value of the '{@link #getConnectors() <em>Connectors</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getConnectors()
	 * @generated
	 * @ordered
	 */
	protected EList<Connector> connectors;

	/**
	 * The cached value of the '{@link #getStates() <em>States</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStates()
	 * @generated
	 * @ordered
	 */
	protected EList<State> states;

	/**
	 * The cached value of the '{@link #getSubdiagrams() <em>Subdiagrams</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSubdiagrams()
	 * @generated
	 * @ordered
	 */
	protected EList<Diagram> subdiagrams;

	/**
	 * The cached value of the '{@link #getEdges() <em>Edges</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEdges()
	 * @generated
	 * @ordered
	 */
	protected EList<Edge> edges;

	/**
	 * The default value of the '{@link #isIsParallel() <em>Is Parallel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIsParallel()
	 * @generated
	 * @ordered
	 */
	protected static final boolean IS_PARALLEL_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isIsParallel() <em>Is Parallel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIsParallel()
	 * @generated
	 * @ordered
	 */
	protected boolean isParallel = IS_PARALLEL_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DiagramImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return EditorPackage.Literals.DIAGRAM;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, EditorPackage.DIAGRAM__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Connector> getConnectors() {
		if (connectors == null) {
			connectors = new EObjectContainmentWithInverseEList<Connector>(Connector.class, this, EditorPackage.DIAGRAM__CONNECTORS, EditorPackage.CONNECTOR__DIAGRAM);
		}
		return connectors;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<State> getStates() {
		if (states == null) {
			states = new EObjectContainmentEList<State>(State.class, this, EditorPackage.DIAGRAM__STATES);
		}
		return states;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Diagram> getSubdiagrams() {
		if (subdiagrams == null) {
			subdiagrams = new EObjectContainmentEList<Diagram>(Diagram.class, this, EditorPackage.DIAGRAM__SUBDIAGRAMS);
		}
		return subdiagrams;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Edge> getEdges() {
		if (edges == null) {
			edges = new EObjectContainmentEList<Edge>(Edge.class, this, EditorPackage.DIAGRAM__EDGES);
		}
		return edges;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isIsParallel() {
		return isParallel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setIsParallel(boolean newIsParallel) {
		boolean oldIsParallel = isParallel;
		isParallel = newIsParallel;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, EditorPackage.DIAGRAM__IS_PARALLEL, oldIsParallel, isParallel));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case EditorPackage.DIAGRAM__CONNECTORS:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getConnectors()).basicAdd(otherEnd, msgs);
		}
		return super.eInverseAdd(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case EditorPackage.DIAGRAM__CONNECTORS:
				return ((InternalEList<?>)getConnectors()).basicRemove(otherEnd, msgs);
			case EditorPackage.DIAGRAM__STATES:
				return ((InternalEList<?>)getStates()).basicRemove(otherEnd, msgs);
			case EditorPackage.DIAGRAM__SUBDIAGRAMS:
				return ((InternalEList<?>)getSubdiagrams()).basicRemove(otherEnd, msgs);
			case EditorPackage.DIAGRAM__EDGES:
				return ((InternalEList<?>)getEdges()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case EditorPackage.DIAGRAM__NAME:
				return getName();
			case EditorPackage.DIAGRAM__CONNECTORS:
				return getConnectors();
			case EditorPackage.DIAGRAM__STATES:
				return getStates();
			case EditorPackage.DIAGRAM__SUBDIAGRAMS:
				return getSubdiagrams();
			case EditorPackage.DIAGRAM__EDGES:
				return getEdges();
			case EditorPackage.DIAGRAM__IS_PARALLEL:
				return isIsParallel();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case EditorPackage.DIAGRAM__NAME:
				setName((String)newValue);
				return;
			case EditorPackage.DIAGRAM__CONNECTORS:
				getConnectors().clear();
				getConnectors().addAll((Collection<? extends Connector>)newValue);
				return;
			case EditorPackage.DIAGRAM__STATES:
				getStates().clear();
				getStates().addAll((Collection<? extends State>)newValue);
				return;
			case EditorPackage.DIAGRAM__SUBDIAGRAMS:
				getSubdiagrams().clear();
				getSubdiagrams().addAll((Collection<? extends Diagram>)newValue);
				return;
			case EditorPackage.DIAGRAM__EDGES:
				getEdges().clear();
				getEdges().addAll((Collection<? extends Edge>)newValue);
				return;
			case EditorPackage.DIAGRAM__IS_PARALLEL:
				setIsParallel((Boolean)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case EditorPackage.DIAGRAM__NAME:
				setName(NAME_EDEFAULT);
				return;
			case EditorPackage.DIAGRAM__CONNECTORS:
				getConnectors().clear();
				return;
			case EditorPackage.DIAGRAM__STATES:
				getStates().clear();
				return;
			case EditorPackage.DIAGRAM__SUBDIAGRAMS:
				getSubdiagrams().clear();
				return;
			case EditorPackage.DIAGRAM__EDGES:
				getEdges().clear();
				return;
			case EditorPackage.DIAGRAM__IS_PARALLEL:
				setIsParallel(IS_PARALLEL_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case EditorPackage.DIAGRAM__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case EditorPackage.DIAGRAM__CONNECTORS:
				return connectors != null && !connectors.isEmpty();
			case EditorPackage.DIAGRAM__STATES:
				return states != null && !states.isEmpty();
			case EditorPackage.DIAGRAM__SUBDIAGRAMS:
				return subdiagrams != null && !subdiagrams.isEmpty();
			case EditorPackage.DIAGRAM__EDGES:
				return edges != null && !edges.isEmpty();
			case EditorPackage.DIAGRAM__IS_PARALLEL:
				return isParallel != IS_PARALLEL_EDEFAULT;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (Name: ");
		result.append(name);
		result.append(", IsParallel: ");
		result.append(isParallel);
		result.append(')');
		return result.toString();
	}

} //DiagramImpl
