// -*- mode: JDE; c-basic-offset: 2; -*-"
// /////////////////////////////////////////////////////////////
// Storage class for Translation
// 
// Synopsis:
//  Huppaal
// /////////////////////////////////////////////////////////////
// @TABLE OF CONTENTS:		       [TOCD: 18:23 13 Feb 2001]
//
//          [0.0.1] Auxillary
//  [1] Growing the global join
//  [2] Cloning a Join
//  [3] Accessing Elements
//  [4] To String
// ==========================================================
// @FILE:    GlobalJoin.java
// @PLACE:   BRICS AArhus; host:harald
// @FORMAT:  java
// @AUTHOR:  M. Oliver M"oller     <omoeller@brics.dk>
// @BEGUN:   Thu Nov  2 16:43:44 2000
// @VERSION: Vanilla-1                  Sun Apr  1 15:19:20 2001
// /////////////////////////////////////////////////////////////
// 


import java.lang.*;
import java.util.Vector;
import java.util.Stack;
import java.util.Hashtable;
import java.util.Enumeration;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

//**** from other packages 

//****************************************

/**
 * <H2>Global Join</H2>
 * This class is auxillary in the construction of a flat Uppaal document as 
 * done by {@link Flatten}.
 * It stores information about <TT>global joins</TT> which are treated 
 * special in the translations, as they can incorporate 
 * mulit-synchronization.<BR>
 * <BR>
 * The extremal case of a global join is a simple exit of a sub-component.
 * It makes sense to treat it in these lines, for the guards and 
 * assignments of the connectors still have to be collected.<BR>
 * <BR>
 * <H2>Run-to completion steps</H2>
 * As for the semanics of the guards/assigments, in Vanilla-1 we follow the 
 * example of Rhapshody: all guards are conjuncted (logical AND), before 
 * the first step is taken. The assignments are executed without any 
 * pre-determined order (i.e. conflicts are the responsibility of the 
 * modeller)
 * 
 * 
 * <H2>Note</H2>
 * A GlobalJoin grows 'itself' and forks, if it has several possibilities 
 * to continue to grow. It uses many methods from {@link Flatten} to access 
 * the relevant elements.
 * 
 * @see Flatten
 * @see  HierarchicalDocumentReader
 * @author <A HREF="MAILTO:omoeller@brics.dk?subject=GlobalJoin.java%20(Vanilla-1%20Wed%20Mar%2028%2015:13:16%202001)">M. Oliver M&ouml;ller</A>
 * @version Vanilla-1                  Sun Apr  1 15:19:20 2001
 */
public class GlobalJoin 
   implements Cloneable {
    
  // ////////////////////////////////////////
  // ////////  STATIC FIELDS  ///////////////
  // ////////////////////////////////////////

  /**
   * The hierarchical document reader
   */
  private static HierarchicalDocumentReader hdr;

  /**
   * ComponentMapper to help access.<BR>
   * The entries in it are created by Flatten.
   */
  private static ComponentMapper cm;
  
  /**
   * The (global) tree of instantiation, these joins are relative to.<BR>
   * This is not patrticular to one global join.<BR>
   * 
   * Not copied, when cloned.
   */
  public static InstantiationTree iTree;


  /**
   * The (global) Vector, all joins are collected in<BR><BR>
   * This is where the additional join is planted to, if the <TT>{@link 
   * #clone}</TT> method is called.
   */
  public static Vector allGlobalJoins;

  // =================================
  // [0.0.1] Auxillary
  // =================================

  /**
   * To make trigger Variables unique
   */
  public static int triggerVariableCounter = 0;
  
  /**
   * Setting this <IT>true</IT>, slows down but is good for debugging.
   */
  public static boolean sanityChecks = true;
  
  /**
   * For Spamming out information
   */
  public static boolean debug = true;
  
  // ////////////////////////////////////////
  // ////////////// FIELDS //////////////////
  // ////////////////////////////////////////

  /**
   * Contains all the unfinished <B>intermediates nodes</B>, i.e. 
   * {@link TinstElementPair}s.<BR>
   * They are worked off in the while loop {@link #finishGrowth}.
   */
  public Stack growObligations;
  
  /**
   * Name of the counter associated with the global join.<BR>
   * It is also used as a trigger to perform the join completly
   */
  public String triggerVariable;
  
  /**
   * Number of locations that are required to take the join.
   * <BR>
   * <BR>
   * (should be identical to the number of textual instantiation to exit , 
   *  right?)
   */
  public int threshold;

  /**
   * Stack that collects the textual instantiaions, that have to be
   * exited, when the join is taken.<BR>
   * It preserves a <B>partial order</B> in the sense, that the top-most 
   * element in the stack is always at the lowest level and a sub-component 
   * A of
   * a component B is always <EM>higher</EM> in the stack than B.
   */
  public Stack tinstsToExit;
  
  /**
   * A list of transitions, that is taken, after the join has been 
   * transformed.<BR>
   * (i.e. the starting point of these transitions is the root of the join)
   */
  public Vector rootTransitions;

  /**
   * The TextualInstantiation, where the rootTransition is
   * instantiated in.<BR>
   */
  public TextualInstantiation rootTinst;

  /**
   * Vector containing all guards as textual String (with the proper 
   * renaiming), including the one of the root-transition (if present). 
   */
  public Vector allGuards;
  
  /**
   * Vector containing all assignments as textual String (with the proper 
   * renaiming), including the one of the root-transition (if present). 
   */
  public Vector allAssignments;

  /**
   * The (growing) collection of all the (hierarchical) basic 
   * locations, from where the join might start.<BR>
   * 
   * The entries of this Vector are <TT>TinstElementPair</TT>s.
   */
  public Vector allHierarchicalStartLocations;

  /**
   * The (global) Vector, all joins are collected in<BR><BR>
   * This is where the additional join is planted to, if the <TT>{@link 
   * #fork}</TT> method is called.
   

  //  private Stack unprocessedExits;

  //  private Stack processedExits;


  // -- AUXILLARY FIELDS ----------------------------------------

// NOT SURE
//  /**
//Collects the join instantiations that are associated with this object
// /  * ??????????????
//   */
///  private static Hashtable allCoveredJoins = new Hashtable();

  // ////////////////////////////////////////
  // //////////  CONSTRUCTORS  //////////////
  // ////////////////////////////////////////
  
  /**
   * Internal constructor, used for cloning (and of course not setting the
   * static fields again).<BR><BR>
   * Needed also, when <TT>{@link #getGlobalJoin}</TT> did not yield an 
   * existing join.
   * <BR>
   * 
   * Does <B>NOT</B> insert the new join in <TT>{@link 
   * #allGlobalJoins}</TT>.
   */
  private GlobalJoin(){
    // inner-eye-dots
    triggerVariable = inventNewTriggerVariable();

    allGuards = new Vector();
    allAssignments = new Vector();
    allHierarchicalStartLocations = new Vector();
    rootTransitions = new Vector();

    growObligations = new Stack();
    tinstsToExit = new Stack();
  }

  // ////////////////////////////////////////
  // ////////////// METHODS  ////////////////
  // ////////////////////////////////////////

  
  /**
   * Constructor specifying the root transition (in the hierarchical 
   * version)<BR>
   * <BR>
   * Throws Exception, if 
   * HierarchicalDocumentReader.addGuardsAndAssignmentsOfConnectionInContext
   * fails.<BR>
   * <BR>
   * If the second argument is Flatten.rootInstDummy, then this gobal join 
   * is
   *  global exit of a component.
   */
  public static GlobalJoin getGlobalJoin(Element theRootTransition,
					 TextualInstantiation theTinst)
    throws Exception {

    if(null == hdr)
      throw new Exception("ERROR: no static HierarchicalDocumentReader provided for GlobalJoin.");
    if(null == cm)
      throw new Exception("ERROR: no static ComponentMapper provided for GlobalJoin.");


    if(debug)
      System.out.println("===requested global join in " + theTinst.toString() + " via roo transition:\n" + theRootTransition.toString());
    
    GlobalJoin result = null;
    for(Enumeration e= allGlobalJoins.elements(); (null == result) && e.hasMoreElements(); ){
      result = (GlobalJoin)e.nextElement();
      if( ( theTinst != result.rootTinst ) ||
	  ( hdr.getElementByID((hdr.getTheChildWithLabel(theRootTransition, "source")).getAttribute("ref")) != result.getTopmostComponent() ) ||
	  ( hdr.getElementByID((hdr.getTheChildWithLabel(theRootTransition, "source")).getAttribute("exitref")) != result.getTopmostExit()) )
	result = null;
    }


    if(result == null){ // -- create new global join -----------------------
      result = new GlobalJoin();
      result.rootTinst = theTinst;

      result.threshold = 1;

      TextualInstantiation tinstOfComponent = cm.retrieveTextualInstantiation(hdr.getElementByID((hdr.getTheChildWithLabel(theRootTransition, "source")).getAttribute("ref")) , theTinst);

      result.growObligations.push(new TinstElementPair(tinstOfComponent,
						       hdr.getElementByID((hdr.getTheChildWithLabel(theRootTransition, "source")).getAttribute("exitref"))));
      result.tinstsToExit.push(tinstOfComponent);

      allGlobalJoins.addElement(result);
      
      if(debug)
	System.out.println("---------- GLOBAL JOINS: created!  [" + allGlobalJoins.size() + "]");
    }
    else {
      if(debug){
	System.out.println("---------- GLOBAL JOINS: extended!  [" + allGlobalJoins.size() + "] : " + result.rootTransitions.size());
	System.out.println(result.toString());
      }
    }

    result.rootTransitions.addElement(theRootTransition);

    //    hdr.addGuardsAndAssignmentsOfConnectionInContext(rootTransition,
    //						     allGuards,
    //						     allAssignments,
    //						     rootTinst);

    // inner-eye-dots
    //triggerVariable = inventNewTriggerVariable();

    return result;
  }


  /**
   * Required static setups
   */
  public static void init(ComponentMapper theCm, HierarchicalDocumentReader theHdr){
    cm = theCm;
    hdr = theHdr;
  }
  
  //
// NOT SURE WHERE/WHETHER WE NEED THIS
//    /**
//   * Returns <TT>true</TT>, if the join instantiation is part of the global 
//     * join 
//     * described by this object
//     */
//    public boolean coversJoinElement(TextualInstantiation tinst, 
//  				   Element join){
//      return 
//        allCoveredJoins.containsKey(tinst) &&
//        ( allCoveredJoins.get(tinst) == join ); 
//    }
//    /**
//     * Adds an instantiation to the presently covered ones
//     *
//     * <H3>!! maybe this could be calling something recursive !!</H3>
//     */
//    public void addJoinElement(TextualInstantiation tinst, 
//  				   Element join){
//      allCoveredJoins.put(tinst, join);
//    }
  

  // ===================================================================
  // [1] Growing the global join
  // ===================================================================  

  /** 
   * Main method telling the global join to continue to grow,
   * starting with one particular <TT>&lt;exit&gt;   </TT>
   * or <TT>&lt;exitpoint&gt;   </TT>
   * <UL>
   * <LI>the search for possibly different growth stops, when the 
   * <TT>listOfExitPoints</TT> contains only exits of <EM>OR</EM> 
   * templates/components</LI>
   *  <LI>if a grow is completed, the join is defined completely</LI>
   *  <LI>the last thing that is set is the <TT>{@link 
   * #triggerVariable}</TT></LI>
   *  <LI>if at some point in the growing, the join has several options to
   *  continue, it splits itself according to these options, adds all of 
   * the new ones to the Vector <TT>{@link #allGlobalJoins}</TT> and calls 
   * grow() on them</LI>
   * </UL>
   *    <H4>Default-Exits</H4>
   * A default exit is reached by <EM>all</EM> locations in this template 
   * and
   * by <EM>all</EM> components.<BR>
   * In particular, it is assumed that every exit of every component is 
   * connected to the default exit.
   * 
   */
  public void grow(TextualInstantiation tinst,
		   Element exitOrExitpoint)
    throws Exception {

    if(true)
      throw new Exception("ERROR: Unexpected call!");

    if( sanityChecks &&
	(!(exitOrExitpoint.getTagName()).equals("exit")) &&
	(!(exitOrExitpoint.getTagName()).equals("exitpoint"))  )
      throw new Exception("ERROR: the element " +
			  exitOrExitpoint.toString() +
			  "  is neither <exit> nor <exitpoint>.");
    
    growObligations.push(new TinstElementPair(tinst, exitOrExitpoint));

    if(!tinstsToExit.contains(tinst))
      tinstsToExit.push(tinst);

  }

  /**
   * Grow loop
   */
  private void finishGrowth()
    throws Exception {
    
    if(debug)
      System.out.println("===]] FINNISHING GROWTH OF:\n" + this.toString() );

    while( !growObligations.empty() ){
      TinstElementPair tip = (TinstElementPair)growObligations.pop();
      TextualInstantiation tinst = tip.tinst;
      Element exitOrExitpoint = tip.element;
      

      if(debug)
	System.out.println("==] INSPECING EXIT: \n" +
			   exitOrExitpoint.toString());

      // -- CASE 1: Default Exit -------------------------------------------

      if(hdr.isDefaultExitOrExitpoint(exitOrExitpoint)){
	if(tinst.isANDComponent)
	  throw new Exception("ERROR: default-exits not allowed in AND components:\n\t" +
			       tinst.toString());

	Element template = tinst.templateElement;
	System.out.println(template.toString());
	// -- conjunct guards/assignments if any ----------------
	Element theExit = exitOrExitpoint;
	if((exitOrExitpoint.getTagName()).equals("exitpoint"))
	  theExit = (Element)exitOrExitpoint.getParentNode();
	NodeList allConnections = hdr.getAllChildrenWithLabel(theExit, "connection");
	for(int i=0; i < allConnections.getLength(); i++){
	  Element connection = (Element)allConnections.item(i);
	  hdr.addGuardsAndAssignmentsOfConnectionInContext(connection, allGuards, allAssignments, tinst);
	}
	if(allConnections.getLength() > 1)
	  throw new Exception("ERROR: not sure whether 2 connection to one default exit make much sense!");
	// -- new joins for EACH component ----------------------
	NodeList allComponents = hdr.getAllChildrenWithLabel(template,
							     "component");
	if(allComponents.getLength() > 0){// -- split global Join 
	  
	  for(int i=0; i < allComponents.getLength(); i++){
	    Element component = (Element)allComponents.item(i);
	    TextualInstantiation tinstOfSubComponent = 
	      cm.retrieveTextualInstantiation(component, tinst);
	    for(Enumeration e = hdr.getAllExitsOfComponent(component).elements();
		e.hasMoreElements(); ){
	      
	      GlobalJoin further = (GlobalJoin)this.clone();
	      Element subExit = (Element)e.nextElement();
	      further.growObligations.push(new TinstElementPair(tinstOfSubComponent, subExit));
	      further.tinstsToExit.push(tinstOfSubComponent);
	    } // -- went through all exits ------------
	  }} // -- went through all components ---------
	// -- are there basic locations connected ? -----------
	NodeList allLocations = hdr.getAllChildrenWithLabel(template,
							    "location");
	if(allLocations.getLength() > 0){
	  for(int i=0; i < allLocations.getLength(); i++){
	    allHierarchicalStartLocations.addElement(new 
	      TinstElementPair(tinst, (Element)allLocations.item(i)));
	  }
	} 
	else
	  stopGrowingThisOne();
      }// ------------------------------------------------------------------
      else {
	
	Vector connectionsToIt = hdr.getConnectionsToExitOrExitpoint(exitOrExitpoint);
	
	if( sanityChecks &&
	    ( connectionsToIt.size() == 0) )
	  throw new Exception("ERROR: the exit " +
			      exitOrExitpoint.toString() +
			      "\n does not have ingoing edges!");      
      
      
	// -- CASE 2: Non default, AND -------------------------------------
	if(tinst.isANDComponent){ // -- AND: all are joins ---------------------
	  
	  if(debug)
	    System.out.println("==] Checking AND component (all are joins)");
	  
	  for(Enumeration e = connectionsToIt.elements(); e.hasMoreElements(); ){
	    Element connection = (Element)e.nextElement();
	    GlobalJoin further = (GlobalJoin)this.clone();
	    hdr.addGuardsAndAssignmentsOfConnectionInContext(
							     connection,
							     further.allGuards,
							     further.allAssignments,
							     tinst);
	    Element source = hdr.getTheChildWithLabel(connection, "source");
	    Element sourceElement = hdr.getElementByID(source.getAttribute("ref"));
	    
	    if(debug)
	      System.out.println("==] reached Join: \n" +
				 sourceElement.toString());
	    
	    if(sanityChecks &&
	       (!(sourceElement.getTagName()).equals("join")))
	      throw new Exception("ERROR: expected a <join>, found instead:\n" +
				  sourceElement.toString());
	    NodeList backConnections = hdr.getAllChildrenWithLabel(sourceElement,
								   "connection");
	    for(int i=0; i < backConnections.getLength(); i++){
	      Element oneFurtherBack = (Element)backConnections.item(i);
	      hdr.addGuardsAndAssignmentsOfConnectionInContext(
	        oneFurtherBack, 
		further.allGuards,
		further.allAssignments, 
		tinst);
	      Element backSource = hdr.getTheChildWithLabel(oneFurtherBack, "source");
	      Element backComponent = hdr.getElementByID(backSource.getAttribute("ref"));
	      Element backExit = hdr.getElementByID(backSource.getAttribute("exitref"));
	      TextualInstantiation subTinst = cm.retrieveTextualInstantiation(backComponent, tinst); 
	      further.growObligations.push(new TinstElementPair(subTinst, backExit));
	      further.tinstsToExit.push(subTinst);
	    }
	    further.threshold = 
	      threshold + (backConnections.getLength()) - 1;
	    further.finishGrowth();
	  }
	  // -- stop growing this one ---------------------
	  this.stopGrowingThisOne();
	  // ----------------------------------------------
	  
	} // -- CASE 3: non-default, XOR -----------------------------------
	else { // -- XOR: locations and subcomponents-----------------------
	
	  if(debug)
	    System.out.println("==] Checking XOR component (basic locations & deeper)");
	  
	  Stack componentPairs = new Stack();

	  for(Enumeration e = connectionsToIt.elements(); e.hasMoreElements(); ){
	    
	    Element connection = (Element)e.nextElement();
	    hdr.addGuardsAndAssignmentsOfConnectionInContext(
              connection,
	      allGuards,
	      allAssignments,
	      tinst);
	    Element source = hdr.getTheChildWithLabel(connection, "source");
	    Element sourceElement = hdr.getElementByID(source.getAttribute("ref"));
	    
	    if((sourceElement.getTagName()).equals("location")){
	      TinstElementPair theStart = new TinstElementPair(tinst, sourceElement);
	      allHierarchicalStartLocations.addElement(theStart);
	    } 
	    else if((sourceElement.getTagName()).equals("component")){
	      Element subExit = hdr.getElementByID(source.getAttribute("exitref"));
	      componentPairs.push(new ElementElementPair(sourceElement,
							 subExit));
	    }
	    else
	      throw new Exception("ERROR: Found unexpected source of exit in XOR component: \n" +
				  sourceElement.toString());
	    
	  }
	  if(!componentPairs.empty()){
	    // -- split global join on components -----------------
	    // NB: There might be one clone, that is unneccessary (for we could
	    // continue to grow this very object, but what the heck...
	    while(!componentPairs.empty()){
	      ElementElementPair pair = (ElementElementPair)componentPairs.pop();
	      Element component = pair.first;
	      Element subExit   = pair.second;
	      GlobalJoin further = (GlobalJoin)this.clone();

	      TextualInstantiation tinstOfSubComponent = 
		cm.retrieveTextualInstantiation(component, tinst);
	      further.growObligations.push(new TinstElementPair(tinstOfSubComponent, subExit));
	      further.tinstsToExit.push(tinstOfSubComponent);
	      further.allHierarchicalStartLocations.addElement(pair);
	    }
	    stopGrowingThisOne();
	  }
	  // -- done with XOR ----------------------------------------------
	}
      }
    } // -- end of growth-obligations --------------------------------------
  }
  /**
   * Remove from <TT>{link #allGlobalJoins}</TT> and terminate the growth
   * obligations.
   */
  private void stopGrowingThisOne(){
    allGlobalJoins.removeElement(this);
    growObligations = new Stack();
  }

  /**
   * Finish growing <EM>all</EM> global joins.<BR>
   * <BR>
   * That typicaly increases their number.
   */
  public static void growAll()
    throws Exception {
    if(false)
      throw new Exception("STOP");
    boolean changes = true;
    while(changes){
      changes = false;
      for(Enumeration e = allGlobalJoins.elements(); e.hasMoreElements(); ){
	GlobalJoin gj = (GlobalJoin)e.nextElement();
	if(!gj.growObligations.empty()){
	  changes = true;
	  gj.finishGrowth();
	}
      }
    }
  }

  // =================================================================
  // [2] Cloning a Join
  // =================================================================

  /**
   * Returns a copy of this object, i.e.
   * <H2>DEEP copies of fields</H2>
   * <UL>
   *  <LI><TT>growObligations</TT></LI>
   *  <LI><TT>tinstsToExit</TT></LI>
   *  <LI><TT>threshold</TT></LI>
   *  <LI><TT>allGuards</TT></LI>
   *  <LI><TT>allAssignments</TT></LI>
   *  <LI><TT>rootTransitions</TT></LI>
   * </UL>
   * <H2>SHALLOW copies of fields</H2>
   * <UL>
   *  <LI><TT>rootTinst</TT></LI>
   *  <LI><TT>allHierarchialStartLocations [Vector is cloned, but not the contained elements]</TT></LI>
   </UL>
   * <H2>Uncopied (inner-eye-dots of clones)</H2>
   * <UL>
   <LI>triggerVariable [new invented for each gj]</LI>
   * </UL>
   * <TT>Static</TT> fields are of course not cloned.<BR><BR>
   * The clone is <B>automatically</B> inserted in the Vector <TT>{@link 
   * #allGlobalJoins}</TT>.
   */
  public Object clone()
    throws CloneNotSupportedException {
    GlobalJoin res = new GlobalJoin();
    // ???? res.allCoveredJoins = allCoveredJoins;

    // deep cloning:

    res.growObligations = cloneStackOfPointers(growObligations);
    res.threshold = threshold;
    res.tinstsToExit = cloneStackOfPointers(tinstsToExit);
    res.allGuards = cloneVectorOfStrings(allGuards);
    res.allAssignments = cloneVectorOfStrings(allAssignments);
    res.rootTransitions = cloneVectorOfPointers(rootTransitions);
    

    // shallow cloning:
    
    res.rootTinst = rootTinst;
    res.allHierarchicalStartLocations = cloneVectorOfPointers(allHierarchicalStartLocations);


    // ---------------------------------------------------------------------
    if(debug)
      System.out.print("&&&& ADDING global join  - WAS: " + allGlobalJoins.size());
    allGlobalJoins.addElement(res);
    if(debug)
      System.out.println(" - IS: " + allGlobalJoins.size());
    return res;
  }
  
  /**
   * Returns a new <TT>Vector</TT> containing all the strings of the old 
   * one (intentionally, but not necessarily in the same order). <BR>
   * <BR>
   * Throws an CloneNotSupportedException, if one of the contents is not a 
   * String.
   */
  private Vector cloneVectorOfStrings(Vector original)
    throws CloneNotSupportedException {

      Vector result = new Vector();
      
      try {
	  for(Enumeration e = original.elements(); e.hasMoreElements(); ){
	      String nextString = (String)e.nextElement();
	      result.addElement(nextString);
	  }
      } catch (Exception e) {
	  e.printStackTrace();
	  throw new CloneNotSupportedException("ERROR: cloneVectorOfStrings failed");
      }
      return result;
  }
  
  /**
   * Clones a stack of pointers, preserving the order.<BR>
   * <BR>
   * <B>Assumes that a stack is sorted, starting with position 0 as the
   * bottommost element</B>.
   */
  public static Stack cloneStackOfPointers(Stack original){
    Stack result = new Stack();
    for(int i=0; i < original.size(); i++){
      result.push(original.elementAt(i));
    }
    return result;
  }

  /**
   * Returns a new <TT>Vector</TT> containing all (pointers to) 
   *    <EM>objects</EM> of the
   * <TT>Vector</TT> in the same order. <BR>
   * The 'contents' themself are not cloned.
   * <BR>
   */
  private static Vector cloneVectorOfPointers(Vector original){

      Vector result = new Vector();
      
      for(int i=0; i < original.size(); i++){
	result.addElement(original.elementAt(i));
      }
      return result;
  }
  

  /**
   * Make a new Variable name
   */
  private static String inventNewTriggerVariable(){
    triggerVariableCounter++;
    return FlatDocumentWriter.makeNameSafe("triggerVar" + triggerVariableCounter);
  }

  // =================================================================
  // [3] Accessing Elements
  // =================================================================


  /**
   * Returns the (hierarchical) component that is left here, as seen from 
   * the
   * rootTransitions.
   */
  public Element getTopmostComponent()
    throws Exception {

    if(rootTransitions.size() == 0)
      return null;
    else {
      
      Element source = hdr.getTheChildWithLabel((Element)rootTransitions.elementAt(0), "source");
      String componentID = source.getAttribute("ref");
      Element component = hdr.getElementByID(componentID);
      
      return component;
    }
  }
  /**
   * Returns the (only) exit that the global join is allowed to be
   *    specific to, before {@link #finishGrowth} is called.<BR>
   * <BR>
   * Thrwos exception, if it is not exactly one.
   */
  public Element getTopmostExit()
    throws Exception {

    //!!! fix exitPoint ambiguity (in comparison)

    if(growObligations.size() != 1)
      throw new Exception("ERROR: unexpected number of grow oblications (" + 
			  growObligations.size() + " \nin\n" + this.toString());
    
    return ((TinstElementPair)growObligations.elementAt(0)).element;
  }
  

  // =================================================================
  // [4] To String
  // =================================================================

  /**
   * Exhaustively describes this global join
   */
  public String toString(){
    StringBuffer sb = new StringBuffer("====================== GLOBAL JOIN ( " +
				       (allGlobalJoins.indexOf(this) + 1) 
				       + " / " +
				       allGlobalJoins.size() + " ) ======================\n");


    if(null == rootTinst)
      sb.append("ROOT TINST       :  " + "*null: global exit*" + "\n");
    else
      sb.append("ROOT TINST       :  " + rootTinst.toString() + "\n");

    sb.append("TRIGGER VARIABLE :  " + triggerVariable + "\n");
    sb.append("THRESHOLD        :  " + threshold + "\n");
    if(rootTransitions.size() == 0)
      sb.append("ROOT TRANSITIONS :  " + "*none*" + "\n");
    else {
      sb.append("ROOT TRANSITIONS :  \n");
      for(Enumeration e = rootTransitions.elements(); e.hasMoreElements(); ){
	sb.append(((Element)e.nextElement()).toString() + "\n");
	}      
    }
    sb.append("TEXTUAL INSTANTIATIONS TO EXIT [" + tinstsToExit.size() + "] :\n");

    for(int i=0; i < tinstsToExit.size(); i++){
      sb.append("   " + (i+1) + ".  " + ((TextualInstantiation)tinstsToExit.elementAt(i)).toString() + "\n");
    }


    
    sb.append("GUARDS [" + allGuards.size() + "] : \n");
    for(Enumeration e = allGuards.elements(); e.hasMoreElements(); ){
      sb.append((String)e.nextElement() + "\t");
    }
    sb.append("\nASSIGNMENTS [" + allAssignments.size() + "] : \n");
    for(Enumeration e = allAssignments.elements(); e.hasMoreElements(); ){
      sb.append((String)e.nextElement() + "\t");
    }


    sb.append("\nHIERARCHICAL STARTING LOCATIONS [" + allHierarchicalStartLocations.size() + "] : \n");
    for(Enumeration e = allHierarchicalStartLocations.elements(); e.hasMoreElements(); ){
      TinstElementPair tep = (TinstElementPair)e.nextElement();
      sb.append(tep.toString() + "\n");
    }


    
    return sb.toString();    
  }
}
