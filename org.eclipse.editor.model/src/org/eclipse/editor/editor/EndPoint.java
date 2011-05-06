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
 * A representation of the model object '<em><b>End Point</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.editor.editor.EndPoint#getOutgoingEdges <em>Outgoing Edges</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.editor.editor.EditorPackage#getEndPoint()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface EndPoint extends EObject {
	/**
	 * Returns the value of the '<em><b>Outgoing Edges</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.editor.editor.Edge}.
	 * It is bidirectional and its opposite is '{@link org.eclipse.editor.editor.Edge#getStart <em>Start</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Outgoing Edges</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Outgoing Edges</em>' reference list.
	 * @see org.eclipse.editor.editor.EditorPackage#getEndPoint_OutgoingEdges()
	 * @see org.eclipse.editor.editor.Edge#getStart
	 * @model opposite="start"
	 * @generated
	 */
	EList<Edge> getOutgoingEdges();

} // EndPoint
