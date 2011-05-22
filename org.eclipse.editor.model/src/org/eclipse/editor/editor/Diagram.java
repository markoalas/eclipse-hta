/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.eclipse.editor.editor;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Diagram</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.editor.editor.Diagram#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.editor.editor.Diagram#getConnectors <em>Connectors</em>}</li>
 *   <li>{@link org.eclipse.editor.editor.Diagram#getStates <em>States</em>}</li>
 *   <li>{@link org.eclipse.editor.editor.Diagram#getSubdiagrams <em>Subdiagrams</em>}</li>
 *   <li>{@link org.eclipse.editor.editor.Diagram#getEdges <em>Edges</em>}</li>
 *   <li>{@link org.eclipse.editor.editor.Diagram#isIsParallel <em>Is Parallel</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.editor.editor.EditorPackage#getDiagram()
 * @model
 * @generated
 */
public interface Diagram extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.editor.editor.EditorPackage#getDiagram_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.editor.editor.Diagram#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Connectors</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.editor.editor.Connector}.
	 * It is bidirectional and its opposite is '{@link org.eclipse.editor.editor.Connector#getDiagram <em>Diagram</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Connectors</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Connectors</em>' containment reference list.
	 * @see org.eclipse.editor.editor.EditorPackage#getDiagram_Connectors()
	 * @see org.eclipse.editor.editor.Connector#getDiagram
	 * @model opposite="diagram" containment="true"
	 * @generated
	 */
	EList<Connector> getConnectors();

	/**
	 * Returns the value of the '<em><b>States</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.editor.editor.State}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>States</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>States</em>' containment reference list.
	 * @see org.eclipse.editor.editor.EditorPackage#getDiagram_States()
	 * @model containment="true"
	 * @generated
	 */
	EList<State> getStates();

	/**
	 * Returns the value of the '<em><b>Subdiagrams</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.editor.editor.Diagram}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Subdiagrams</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Subdiagrams</em>' containment reference list.
	 * @see org.eclipse.editor.editor.EditorPackage#getDiagram_Subdiagrams()
	 * @model containment="true"
	 * @generated
	 */
	EList<Diagram> getSubdiagrams();

	/**
	 * Returns the value of the '<em><b>Edges</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.editor.editor.Edge}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Edges</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Edges</em>' containment reference list.
	 * @see org.eclipse.editor.editor.EditorPackage#getDiagram_Edges()
	 * @model containment="true"
	 * @generated
	 */
	EList<Edge> getEdges();

	/**
	 * Returns the value of the '<em><b>Is Parallel</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Is Parallel</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Is Parallel</em>' attribute.
	 * @see #setIsParallel(boolean)
	 * @see org.eclipse.editor.editor.EditorPackage#getDiagram_IsParallel()
	 * @model
	 * @generated
	 */
	boolean isIsParallel();

	/**
	 * Sets the value of the '{@link org.eclipse.editor.editor.Diagram#isIsParallel <em>Is Parallel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Is Parallel</em>' attribute.
	 * @see #isIsParallel()
	 * @generated
	 */
	void setIsParallel(boolean value);

} // Diagram
