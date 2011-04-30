// -*- mode: JDE; c-basic-offset: 2; -*-
// /////////////////////////////////////////////////////////////
// Quick and dirty Node list implementation
//
// Does NOT implement the Interface 
//   org.apache.xalan.xpath.MutableNodeList
//
// Synopsis:
//  Huppaal
// /////////////////////////////////////////////////////////////
// @FILE:    MutableNodeListImpl.java
// @PLACE:   Uppsala; host:eniac
// @FORMAT:  java
// @AUTHOR:  M. Oliver M'o'ller     <omoeller@brics.dk>
// @BEGUN:   Mon Feb 19 15:56:01 2001
// @VERSION: Vanilla-1                  Mon Feb 19 16:07:36 2001
// /////////////////////////////////////////////////////////////
// 

import java.lang.*;
import java.util.Vector;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList; 

//**** from other packages 

//****************************************

/**
 * Implements a list of nodes, that Sun could as well have declared as
 * a Vector but is not.
 * 
 * @author <A HREF="MAILTO:omoeller@brics.dk?subject=MutableNodeListImpl.java%20(Vanilla-1%20Mon%20Feb%2019%2016:03:23%202001)">M. Oliver M&ouml;ller</A>
 * @version Vanilla-1                  Mon Feb 19 16:07:36 2001
 */
public class MutableNodeListImpl
  implements org.w3c.dom.NodeList {

  // //////////////////////////////////////////////////////////////////////
  // ////////////////////////////// FIELDS ////////////////////////////////
  // //////////////////////////////////////////////////////////////////////

  private Vector data;

  // //////////////////////////////////////////////////////////////////////
  // //////////////////////////  CONSTRUCTORS  ////////////////////////////
  // //////////////////////////////////////////////////////////////////////

  /**
   * Default Constructor
   */
  public MutableNodeListImpl(){
    data = new Vector();
  }

  // //////////////////////////////////////////////////////////////////////
  // ///////////////////////////// METHODS  ///////////////////////////////
  // //////////////////////////////////////////////////////////////////////

  public int getLength() {
    return data.size();
  }
  public Node item(int index) {
    return (org.w3c.dom.Node)data.elementAt(index);
  }

  // =================================================================
  // mutable methods needed
  // =================================================================

  /**
   * Insert a node at the end of the list.
   */
  public void addNode(Node n){
    data.addElement(n);
  }

}
