// -*- mode: JDE; c-basic-offset: 2; -*-
// /////////////////////////////////////////////////////////////
// 
// 
// Synopsis:
//  
// /////////////////////////////////////////////////////////////
// @TABLE OF CONTENTS:		       [TOCD: 13:47 19 Mar 2001]
//
//  [1] Simple Reading
//  [2] Access
//          [2.0.1] Really auxillary aux
// ==========================================================
// @FILE:    DocumentReader.java
// @PLACE:   BRICS AArhus; host:newton
// @FORMAT:  java
// @AUTHOR:  M. Oliver M'o'ller     <omoeller@brics.dk>
// @BEGUN:   Fri Nov 17 17:43:48 2000
// @VERSION: Vanilla-1                  Mon Mar 19 14:41:37 2001
// /////////////////////////////////////////////////////////////
// 


import java.lang.*;

import java.util.Vector;
import java.util.Hashtable;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

//**** from other packages 

//****************************************

/**
 * <H2>Accessor Class</H2>
 * Nice wrapper class to access documents with<BR>
 * Contains only static methods that are not specific to the document in 
 * question.
 * 
 * 
 * 
 * @author <A HREF="MAILTO:omoeller@brics.dk?subject=DocumentReader.java%20(Vanilla-1%20Mon%20Mar%2019%2014:41:27%202001)">M. Oliver M&ouml;ller</A>
 * @version Vanilla-1                  Mon Mar 19 14:41:37 2001
 */
public class DocumentReader  {

  // //////////////////////////////////////////////////////////////////////
  // ////////////////////////////// FIELDS ////////////////////////////////
  // //////////////////////////////////////////////////////////////////////

  /**
   * Document it wraps around
   */
  protected Document origDoc;

  // -- Access -------------------------------------------------------------

  /**
   * Store once for every translation
   */
  protected Hashtable hashOriginalIDsToElements;


  /**
   * Setting this <IT>true</IT>, slows down but is good for debugging.
   */
  public static final boolean sanityChecks = true;


  // //////////////////////////////////////////////////////////////////////
  // //////////////////////////  CONSTRUCTORS  ////////////////////////////
  // //////////////////////////////////////////////////////////////////////


  /**
   * Constructor with hand-over of document
   */
  public DocumentReader(Document theDoc){
    origDoc = theDoc;

  }

  // //////////////////////////////////////////////////////////////////////
  // ///////////////////////////// METHODS  ///////////////////////////////
  // //////////////////////////////////////////////////////////////////////


  // =================================================================
  // [1] Simple Reading
  // =================================================================
    
  
  /**
   * Read the <TT>id</TT> of a given element<BR>        
   * Throw exception, if it does not exist
   */
  public static String getElementID(Element el)
    throws Exception {
    
    if(el.hasAttribute("id")){
      return el.getAttribute("id");
    }
    else {
      throw new Exception("ERROR: Element " +
			  el.toString() +
			  " does not have ID attribute.");
    }
    
  }
  /**
   * Read the contens of <TT>name</TT> child of a given element<BR>       
   * Throw exception, if it does not exist
   */
  public static String getElementName(Element el)
    throws Exception {
    
    NodeList nameChildren = getAllChildrenWithLabel(el, "name");
    if(nameChildren.getLength() == 1){
      return (((Text)((Element)nameChildren.item(0)).getFirstChild())).getData();
    }
    else {
      throw new Exception("ERROR: in " +
			  el.toString() +
			  ": unexpected number of <NAME> children ("+
			  nameChildren.getLength() + ")");
    }
    
  }
  /**
   * If the Element is a tag containing CDATA, then this data is returned.
   * If no children exist or cdata is empty, then the empty string is 
   * returned.<BR><BR>
   * Otherwise, an exception is thrown.
   */
  public static String getCdataOfElement(Element el)
    throws Exception {
    if (null == el.getFirstChild())
      return "";
    String cdata = ((Text)el.getFirstChild()).getData();
    if(null == cdata)
      return "";
    else
      return cdata;
  }
  /**
   * Return all child nodes that have a specific label
   */
  public static NodeList getAllChildrenWithLabel(Element el, String label){
    NodeList childNodes = el.getChildNodes();
    MutableNodeListImpl res = new MutableNodeListImpl();
    for(int i = 0; i < childNodes.getLength(); i++){
      Node node = childNodes.item(i);
      if( (node instanceof Element) &&
	  (((Element)node).getTagName()).equals(label) ){
	res.addNode(node);
      }
    }
    
    return res;
  }
  /**
   * Get first child that has the designated label.<BR>
   * Throw exception, if no such child exists
   */      
  public static Element getFirstChildWithLabel(Element el, String label)
    throws Exception {
    NodeList childNodes = el.getChildNodes();
    for(int i = 0; i < childNodes.getLength(); i++){
      Node node = childNodes.item(i);
      if( (node instanceof Element) &&
	  (((Element)node).getTagName()).equals(label) ){
	return (Element)node;
      }
    }
    throw new Exception("ERROR: no child element with label >>" +
			label +
			"<< found in \n" +
			el.toString());
  }
  /**
   * Get first child that has the designated label.<BR>
   * Returns <TT>null</TT>, if no such child exists
   */      
  public static Element getFirstChildWithLabelIfExists(Element el, String label)
    throws Exception {
    NodeList childNodes = el.getChildNodes();
    for(int i = 0; i < childNodes.getLength(); i++){
      Node node = childNodes.item(i);
      if( (node instanceof Element) &&
	  (((Element)node).getTagName()).equals(label) ){
	return (Element)node;
      }
    }
    return null;
  }
  

   /**
   * Get the child that has the designated label.<BR>
   * Throw exception, if none or more than one such child exists.
   */      
  public static Element getTheChildWithLabel(Element el, String label)
    throws Exception {
    NodeList childNodes = el.getChildNodes();
    Element result = null;
    for(int i = 0; i < childNodes.getLength(); i++){
      Node node = childNodes.item(i);
      if( (node instanceof Element) &&
	  (((Element)node).getTagName()).equals(label) ){
	if(result == null){
	  result = (Element)node;
	}
	else 
	  throw new Exception("ERROR: the element " +
			      el.toString() +
			      "  has more than one child with label " + label);
      }
    }
    if (result == null){
      throw new Exception("ERROR: no child element with label >>" +
			  label +
			  "<< found in \n" +
			  el.toString());
    }
    else { 
      return result;
    }
  }
  /**
   * Get the child that has the designated label.<BR>
   * Throw exception, if more than one such child exists.
   * Returns <TT>null</TT>, if none exists
   */      
  public static Element getTheChildWithLabelIfExists(Element el, String label)
    throws Exception {
    NodeList childNodes = el.getChildNodes();
    Element result = null;
    for(int i = 0; i < childNodes.getLength(); i++){
      Node node = childNodes.item(i);
      if( (node instanceof Element) &&
	  (((Element)node).getTagName()).equals(label) ){
	if(result == null){
	  result = (Element)node;
	}
	else 
	  throw new Exception("ERROR: the element " +
			      el.toString() +
			      "  has more than one child with label " + label);
      }
    }
    return result;
  }

  /**
   * Read x coordinate (as long).<BR>
   * 
   * Return "0L" if not present
   */
  public long getXCoordinate(Element e){
    String xs = e.getAttribute("x").trim();

    return new Long(xs).longValue();
  }
  /**
   * Read y coordinate (as long).<BR>
   * 
   * Return "0L" if not present
   */
  public long getYCoordinate(Element e){
    String ys = e.getAttribute("y").trim();

    return new Long(ys).longValue();
  }
  
  

  // =================================================================
  // [2] Access (by Hashing)
  // =================================================================

  /**
   * Return the Element-Node in the original document that 
   * is augmented with the specific id<BR>
   * Throw excpetion, if it does not exist.<BR><BR>
   * <H2>Note: There should be a <TT>org.w3c.dom.Document</TT> method 
   * doing this (<TT>getElementByID</TT>)
   * for us, but it seems to be buggy (see bugreport in 
   * <TT>./Bug</TT>)</H2>
   * 
   * @see org.w3c.dom.Document
   */
  public Element getElementByID(String id)
    throws Exception {
    
    if(null == hashOriginalIDsToElements)
      memorizeIDsOfOriginalDocument();

    Object res = hashOriginalIDsToElements.get(id);

    if (null == res){
      memorizeIDsOfOriginalDocument();
      res = hashOriginalIDsToElements.get(id);
      
      if(null == res)
	throw new Exception("ERROR: ID \"" + id + "\" not found in original document.");
    }
    
    return (Element)res;
  }

  /**
   * Enter IDs in the hashtable <TT>{@link #hashOriginalIDsToElements}</TT><BR>
   * <BR>
   * Throws exception, if document is not set properly
   */
  public void memorizeIDsOfOriginalDocument()
    throws Exception {

    if(null == origDoc)
      throw new Exception("ERROR: document is (still) null");

    hashOriginalIDsToElements = new Hashtable();

    NodeList allNodes = origDoc.getElementsByTagName("*");
    for(int i=0; i < allNodes.getLength(); i++){
      Node node = allNodes.item(i);
      if( ( node instanceof Element) &&
	  ( ((Element)node).hasAttribute("id")) ){
	hashOriginalIDsToElements.put(((Element)node).getAttribute("id"),
				      node);
      }
    }
  }


  
  // ========================================
  // [2.0.1] Really auxillary aux
  // ========================================
   
   /**
    * the lesser of two integers
    */
   public static int min(int a, int b){
     if (a < b)
       return a;
     else
       return b;
   }
   /**
    * the larger of two longs
    */
   public static long max(long a, long b){
     if (a > b)
       return a;
     else
       return b;
   }
  /**
    * the larger of two integers
    */
   public static int max(int a, int b){
     if (a > b)
       return a;
     else
       return b;
   }
  
  /**
   * the larger of three integers
   */
  public static int max3(int a, int b, int c){
    return max(a,max(b,c));
  }
  /**
   * the larger of four integers
   */
  public static int max4(int a, int b, int c, int d){
    return max(max(a,b),max(c,d));
  }
  /**
   * the larger of five integers
   */
  public static int max5(int a, int b, int c, int d, int e){
    return max(max(max(a,b),max(c,d)),e);
  }
  
}

