// -*- mode: JDE;  c-basic-offset: 2; -*-
// /////////////////////////////////////////////////////////////
// Tree Structure
// 
// Synopsis:
//  
// /////////////////////////////////////////////////////////////
// @FILE:    InstantiationTree.java
// @PLACE:   BRICS AArhus; host:harald
// @FORMAT:  java
// @AUTHOR:  M. Oliver M"oller     <omoeller@brics.dk>
// @BEGUN:   Tue Nov  7 11:42:55 2000
// @VERSION: Vanilla-1                  Mon Apr  9 10:50:37 2001
// /////////////////////////////////////////////////////////////
// 

import TextualInstantiation;

import java.lang.*;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Text;

import java.util.Vector;
import java.util.Enumeration;

//**** from other packages 

//****************************************

/**
 * This Datat Type implements a tree structure, where every
 * Node correspons to a {@link TextualInstantiation}. Leaves are
 * Objects with a empty <TT>{@link #children}</TT> Vector.
 *
 * @author <A HREF="MAILTO:omoeller@brics.dk?subject=InstantiationTree.java%20(Vanilla-1%20Fri%20Mar%2023%2011:48:01%202001)">M. Oliver M&ouml;ller</A>
 * @version Vanilla-1                  Mon Apr  9 10:50:37 2001
 */
public class InstantiationTree  {

  // //////////////////////////////////////////////////////////////////////
  // ////////////////////////////// FIELDS ////////////////////////////////
  // //////////////////////////////////////////////////////////////////////
  
  public TextualInstantiation content;
  
  
  /**
   * All child instantiatino trees.
   */
  public Vector children;
  
  
  // =====================================
  // AUX
  // =====================================
  
  /**
   * Space indentations when displaying children
   */       
  public static int offsetFactor = 4;
  
  public static final String spaces = "                                                                                                                                                                                                        ";
  
  // //////////////////////////////////////////////////////////////////////
  // //////////////////////////  CONSTRUCTORS  ////////////////////////////
  // //////////////////////////////////////////////////////////////////////
  
  /**
   * Default Constructor
   */
  public InstantiationTree(TextualInstantiation tinst){
    content = tinst;
    children = new Vector();
  }
  
  // //////////////////////////////////////////////////////////////////////
  // ///////////////////////////// METHODS  ///////////////////////////////
  // //////////////////////////////////////////////////////////////////////
  
  /**
   * Add a child
   */
  public void addChild(InstantiationTree instt){
    children.addElement(instt);
  }
  
  /** 
   * Return enumeration of the children
   */
  public Enumeration enumChildren(){
    return children.elements();
  }
  
  
  /**
   * Returns the node of the tree that contains the specific {@link 
   * TextualInstantiation}.<BR><BR>
   * 
   * Returns <TT>null</TT>, if it is not found.
   */
  public InstantiationTree findNodeWithContent(TextualInstantiation tinst){
    if(tinst == content)
      return this;
    for(Enumeration e = this.enumChildren(); e.hasMoreElements();){
      Object res = ((InstantiationTree)e.nextElement()).findNodeWithContent(tinst);
      if(null != res)
	return (InstantiationTree)res;
    }
    return null;
  }
  /**
   * Return a string representing the (sub-)tree, with <TT>{@link 
   * #offsetFactor}</TT>*<TT>offset</TT> spaces before the name
   */
  public String showWithOffset(int offset){
    StringBuffer buf = new StringBuffer();
    buf.append(spaces.substring(0,offset*offsetFactor) +
	       content.objectName);
    if(content.isANDComponent)
      buf.append(" [AND]");
    else
      buf.append(" [XOR]");
    buf.append("\n");
    for(Enumeration e = this.enumChildren(); e.hasMoreElements();){
      buf.append(((InstantiationTree)e.nextElement()).showWithOffset(offset + 1));
    }
    return buf.toString();
  }
  
  
  // ===================================================================
  // GLOBAL JOINS
  // ===================================================================
  
  /**
   * Compute the global joins starting at this component or at a 
   * component below.
   * 
   */
  public Vector computeGlobalJoins(){
    Vector allJoins = new Vector();
    
    collectGlobalJoinsStartingHERE(allJoins);
    collectGlobalJoinsStartingBelowHere(allJoins);
    
    return allJoins;
  }
  /**
   * Adds the global joins starting in a <EM>descendand</EM> to the 
   * argument Vector
   */
  private void collectGlobalJoinsStartingBelowHere(Vector allJoins){
    for(Enumeration e = this.enumChildren(); e.hasMoreElements(); ){
      InstantiationTree intr = (InstantiationTree)e.nextElement();
      intr.collectGlobalJoinsStartingHERE(allJoins);
      intr.collectGlobalJoinsStartingBelowHere(allJoins);
    }
  }
  /**
   * Add the global joins, that have their <EM>root transition</EM> in 
   * the textual instantiation.
   * If this component is not an OR component, nothing is done.
   * 
   */
  private void collectGlobalJoinsStartingHERE(Vector allJoins){
    if(this.content.isANDComponent)
      return;
    
    Element template = this.content.templateElement;
    NodeList allComponents = DocumentReader.getAllChildrenWithLabel(template,
								    "component");
    
    
  }
  
}
