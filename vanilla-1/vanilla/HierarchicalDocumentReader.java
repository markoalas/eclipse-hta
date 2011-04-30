// -*- mode: JDE; c-basic-offset: 2; -*-
// /////////////////////////////////////////////////////////////
// Wrapper Class for reading a hierarchical document
// 
// Synopsis:
//  Huppaal
// /////////////////////////////////////////////////////////////
// @TABLE OF CONTENTS:		       [TOCD: 15:45 03 Apr 2001]
//
//  [1] Acessing Elements
//      [1.1] Getting Textual Data
//      [1.2] Accessing sub-elements
//      [1.3] Checking Properties
//  [2] Tracing and Hunt Elements  
//          [2.0.1] Forward: FORKS
//      [2.1] Collecting Target-Guards/Assignments
//  [3] Retrieve Elements
//      [3.1] dealing with hashtables
//  [4] Global Joins of Global Elements (global exit)
//  [5] AUX Services
// ==========================================================
// @FILE:    HierarchicalDocumentReader.java
// @PLACE:   BRICS AArhus; host:harald
// @FORMAT:  java
// @AUTHOR:  M. Oliver M'o'ller     <omoeller@brics.dk>
// @BEGUN:   Thu Nov 16 18:32:59 2000
// @VERSION: Vanilla-1                  Tue Apr  3 16:11:51 2001
// /////////////////////////////////////////////////////////////
// 

import java.lang.*;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;


import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Text;


//**** from other packages 

//****************************************

/**
 * <H1>Wrapper Class</H1>
 * This class is a wrapper around a hierarchical HUppaal document, such 
 * that the grammar might be re-designed with minimal programming effort.
 * 
 * @author <A HREF="MAILTO:omoeller@brics.dk?subject=HierarchicalDocumentReader.java%20(Vanilla-1%20Tue%20Apr%203%2015:54:04%202001)">M. Oliver M&ouml;ller</A>
 * @version Vanilla-1                  Tue Apr  3 16:11:51 2001
 */
public class HierarchicalDocumentReader 
  extends DocumentReader {

  // //////////////////////////////////////////////////////////////////////
  // ////////////////////////////// FIELDS ////////////////////////////////
  // //////////////////////////////////////////////////////////////////////

  /**
   * Determines whether translations uses the &lt;label&gt; construct for
   *    assignments, guards and synchronisations.
   * not.<BR>
   * <BR>
   * <B>From <TT>huppaal-0.4</TT> on, this should 
   * be <TT>true</TT>.</B>
   */
  static final boolean useLabelConstruct = true;
  
  /**
   * Determines whether translations uses the &lt;label&gt; construct also
   * for invariants.
   * <BR>
   * <B>From <TT>huppaal-0.5</TT> on, this should 
   * be <TT>true</TT>.</B>
   */
  static final boolean useLabelForInvariants = true;

  /**
   * Version of the hierarchical Huppaal grammar
   */
  public static final String huppaalDTD = "huppaal-0.6.dtd";
  

  /**
   * original, hierarchical document-root
   */
  private Element oldRoot;


  /**
   * Hashing global element names to dummy component Elements
   */
  private Hashtable wrapAroundInstHashTable;

  // -- AUX ----------------------------------------------------------------
  
  /**
   * Separate Strings (names) for hashing
   */
  private static final String unambigousNameSeparator = "__";

  /**
   * Name of Component IDs that are not present in the original document
   * <BR>
   * <BR>
   * used in <TT>{@link #wrapAroundInstHashTable}</TT>.
   */
  private static final String fakeIDName = "fake__ID_";
  
  /**
   * AUX counter to create unique fake IDs
   */
  private static long fakeIDCounter = 0L;

  /**
   * Spam out debuggin information, if <TT>debug</TT> is true
   */
  static boolean debug = true;
  

  // //////////////////////////////////////////////////////////////////////
  // //////////////////////////  CONSTRUCTORS  ////////////////////////////
  // //////////////////////////////////////////////////////////////////////

  /**
   * Default Constructor
   */
  public HierarchicalDocumentReader(Document theDoc){
    super(theDoc);

    origDoc = theDoc;
    oldRoot = origDoc.getDocumentElement();

    wrapAroundInstHashTable = new Hashtable();
  }

  // //////////////////////////////////////////////////////////////////////
  // ///////////////////////////// METHODS  ///////////////////////////////
  // //////////////////////////////////////////////////////////////////////


  // ===================================================================
  // [1] Acessing Elements
  // ===================================================================

  /**
   * All direct Children of root
   */
  public NodeList getDirectChildNodes(){
    return oldRoot.getChildNodes();
  }
  /**
   * Some direct children of root
   */
  public NodeList getDirectChildNodesWithTag(String tag){
    return oldRoot.getElementsByTagName(tag);
  }

  /**
   * Returns the Element in the original (hierarchical) document, that has
   * the specified name.<BR>
   * Throws Exception, if not found.
   */
  public  Element getTemplateWithName(String name)
    throws Exception {
    
    NodeList templateNodes = getDirectChildNodesWithTag("template");
    
    for(int i=0; i < templateNodes.getLength(); i++){
      if(getElementName((Element)templateNodes.item(i)).equals(name))
	return (Element)templateNodes.item(i);
    }
    
    throw new Exception("ERROR: no template with name \"" + name + 
			"\" found.");
  }
  
  /**
   * Return the (complete) list of defined hierarchical templates
   */
  public NodeList getAllTemplates(){
    return getDirectChildNodesWithTag("template");
  }
  
  /**
   * Return the list of global inits, that refer to a root element
   * that is part of the system.
   */
  public NodeList getAllAliveGlobalInits()
    throws Exception {
    NodeList all = getDirectChildNodesWithTag("globalinit");
    Vector systemParts = getNamesOfAliveSystemParts();
    MutableNodeListImpl result = new MutableNodeListImpl();
    
    for(Enumeration e = systemParts.elements(); e.hasMoreElements(); ){
      boolean found = false;
      String sysPart = (String)e.nextElement();
      for(int i = 0; (i < all.getLength()) && (! found); i++){
	if( sysPart.equals(((Element)all.item(i)).getAttribute("instantiationname"))){
	  result.addNode((Node)all.item(i));
	  found = true;
	}
      }
      if(!found)
	throw new Exception("ERROR: No <globalinit> declared for alive system part \n>>" +
			    sysPart + "<<");
    }
    return result;
  }

  // =============================================
  // [1.1] Getting Textual Data
  // =============================================

  /**
   * Retrieve the texutal <TT>system</TT> data from element tag, if present
   */
  public String getSystemText()
    throws Exception {
    Element system = getTheChildWithLabelIfExists(oldRoot, "system");
    if(null == system)
      return "";
    else 
      return getCdataOfElement(system);
  }
  /**
   * Retrieve the texutal <TT>instantiation</TT> data from element tag, if present
   */
  public String getInstantiationText()
    throws Exception {
    Element instantiation = getTheChildWithLabelIfExists(oldRoot, "instantiation");
    if(null == instantiation)
      return "";
    else 
      return getCdataOfElement(instantiation);
  }
  /**
   * Retrieve the texutal <TT>declaration</TT> data from element tag, if present
   */
  public String getDeclarationText()
    throws Exception {
    Element declaration = getTheChildWithLabelIfExists(oldRoot, "declaration");
    if(null == declaration)
      return "";
    else 
      return getCdataOfElement(declaration);
  }
  
  

  // =============================================
  // [1.2] Accessing sub-elements
  // =============================================

  /**
   * Returns a Vector of exit Elements, that correspond to the exits of the
   * template this component instantiates.
   */
  public Vector getAllExitsOfComponent(Element component)
    throws Exception {
    if(sanityChecks &&
	!((component.getTagName()).equals("component")))
      throw new Exception("ERROR: the element " +
			  component.toString() +
			  "  is not a component.");
    Element template = getTemplateWithName(component.getAttribute("instantiates"));
    return getAllExitsOfTemplate(template);
  }
  /**
   * Returns a Vector of exit Elements, that correspond to the exits of this
   * template.
   */
  public static Vector getAllExitsOfTemplate(Element template)
    throws Exception {
    if(sanityChecks &&
	!((template.getTagName()).equals("template")))
      throw new Exception("ERROR: the element " +
			  template.toString() +
			  "  is not a template.");

    Vector result = new Vector();
    NodeList allExits = getAllChildrenWithLabel(template, "exit");
    for(int i=0; i < allExits.getLength(); i++){
      result.addElement(allExits.item(i));
    }

    return result;
  }

  /**
   * Return a vector with all locations and components.
   */
  public static Vector getAllLocationsAndComponents(Element template)
    throws Exception {
    if(sanityChecks &&
	!((template.getTagName()).equals("template")))
      throw new Exception("ERROR: the element " +
			  template.toString() +
			  "  is not a template.");

    Vector result = new Vector();
    NodeList allLocations = getAllChildrenWithLabel(template, "location");
    for(int i=0; i < allLocations.getLength(); i++){
      result.addElement(allLocations.item(i));
    }
    NodeList allComponents = getAllChildrenWithLabel(template, "component");
    for(int i=0; i < allComponents.getLength(); i++){
      result.addElement(allComponents.item(i));
    }

    return result;
  }
  

  // =============================================
  // [1.3] Checking Properties
  // =============================================
  
  /**
   * Test whether this element is a component.<BR>
   * <BR>
   * Throws Exception, if the element is not a location or component.
   */
  public static boolean isComponent(Element el)
    throws Exception {
    if(sanityChecks &&
       (!((el.getTagName()).equals("component"))) &&
       (!((el.getTagName()).equals("location"))) )
      throw new Exception("ERROR: the element is neither <location> nor <component>:\n " +
			  el.toString());

    return (el.getTagName()).equals("component");
  }

  /**
   *    Returns true, if the element is a default exit or an exitpoint.<BR>
   * Throws exception on type error.
   */
  public static boolean isDefaultExitOrExitpoint(Element exitOrExitpoint)
    throws Exception {
     if(sanityChecks &&
	(!((exitOrExitpoint.getTagName()).equals("exit"))) &&
	(!((exitOrExitpoint.getTagName()).equals("exitpoint"))))
      throw new Exception("ERROR: the element " +
			  exitOrExitpoint.toString() +
			  "  is neither exit nor exitpoint.");
     Element theExit = exitOrExitpoint;
     if((exitOrExitpoint.getTagName()).equals("exitpoint")){
       theExit = (Element)exitOrExitpoint.getParentNode();
     }
     return (theExit.getAttribute("type")).equals("default-exit");
  }

  /**
   * Checks whether this template has a <EM>default exit</EM>.
   */
  public static boolean hasDefaultExit(Element template)
    throws Exception {
    if(sanityChecks &&
	!((template.getTagName()).equals("template")))
      throw new Exception("ERROR: the element " +
			  template.toString() +
			  "  is not a template.");

    Vector allExits = getAllExitsOfTemplate(template);
    for(Enumeration e = allExits.elements(); e.hasMoreElements(); ){
      if(isDefaultExitOrExitpoint((Element)e.nextElement()))
	return true;
    }
    return false;
  }



  /**
   * Returns <TT>true</TT>, if the template is a history 
   * element.<BR>
   * <BR>
   * Trows Exception, if called on a non-template
   */
  public boolean isHistoryTemplate(Element template)
    throws Exception {
    if(sanityChecks &&
	!((template.getTagName()).equals("template")))
      throw new Exception("ERROR: the element " +
			  template.toString() +
			  "  is not a template.");
    NodeList allEntries = getAllChildrenWithLabel(template, "entry");
    for(int i=0; i < allEntries.getLength(); i++){
      if(isHistoryEntry((Element)allEntries.item(i)))
	return true;
    }
    return false;
  }
  /**
   * Returns <TT>true</TT>, if the template is an <EM>AND</EM> 
   * element.<BR>
   * <BR>
   * Trows Exception, if called on a non-template
   */
  public boolean isANDTemplate(Element template)
    throws Exception {
    if(sanityChecks &&
       !((template.getTagName()).equals("template")))
      throw new Exception("ERROR: the element " +
			  template.toString() +
			  "  is not a template.");
    return (template.getAttribute("type")).equals("AND");
  }
  
  /**
   * Returns <TT>true</TT>, if the component is a history 
   * element.<BR>
   * <BR>
   * Trows Exception, if called on a non-component
   */
  public boolean isHistoryComponent(Element component)
    throws Exception {
    if(sanityChecks &&
	!((component.getTagName()).equals("component")))
      throw new Exception("ERROR: the element " +
			  component.toString() +
			  "  is not a component.");
    String templateTextualName = component.getAttribute("instantiates");
    Element template = getTemplateWithName(templateTextualName);
    NodeList allEntries = getAllChildrenWithLabel(template, "entry");
    for(int i=0; i < allEntries.getLength(); i++){
      if(isHistoryEntry((Element)allEntries.item(i)))
	return true;
    }
    return false;
  }

  /**
   * Returns <TT>true</TT>, if the component is isantiation of an
   *  <EM>AND</EM> template.<BR>
   * <BR>
   * Trows Exception, if called on a non-component
   */
  public boolean isANDComponent(Element component)
    throws Exception {
    if(sanityChecks &&
       !((component.getTagName()).equals("component")))
      throw new Exception("ERROR: the element " +
			  component.toString() +
			  "  is not a component.");
    String templateTextualName = component.getAttribute("instantiates");
    Element template = getTemplateWithName(templateTextualName);
    return isANDTemplate(template);
  }
  

  /**
   * Returns <TT>true</TT>, if the entry is a history 
   * element.<BR>
   * <BR>
   * Trows Exception, if called on a non-entry
   */
  public static boolean isHistoryEntry(Element entry)
    throws Exception {
    if(sanityChecks &&
	!((entry.getTagName()).equals("entry")))
      throw new Exception("ERROR: the element " +
			  entry.toString() +
			  "  is not a entry.");
    return (entry.getAttribute("type")).equals("history");
  }
  

  /**
   * Test, whether a node is an Element <EM> and</EM> a target
   */
  public static boolean isTargetElement(Node n){
    return
      (n instanceof Element) &&
      (((Element)n).getTagName()).equals("target");
  }
  /**
   * Test, whether a node is an Element <EM> and</EM> a source
   */
  public static boolean isSourceElement(Node n){
    return
      (n instanceof Element) &&
      (((Element)n).getTagName()).equals("source");
  }
  /**
   * Test, whether a node is an Element <EM> and</EM> a nail
   */
  public static boolean isNailElement(Node n){
    return
      (n instanceof Element) &&
      (((Element)n).getTagName()).equals("nail");
  }
  /**
   * Test, whether this is an assignment Element.<BR>
   * <BR>
   * Depends on {@link #useLabelConstruct}.
   */
  public static boolean isAssignmentElement(Node n){

    if(! (n instanceof Element))
      return false;
    Element el = (Element)n;
    
    boolean result;
    if(useLabelConstruct){
      result = 
	el.getTagName().equals("label") &&
	el.getAttribute("kind").equals("assignment");
    }
    else { // OLD
      result = el.getTagName().equals("assignment");
    }
    return result;
  }
  /**
   * Test, whether this is an guard Element.<BR>
   * <BR>
   * Depends on {@link #useLabelConstruct}.
   */
  public static boolean isGuardElement(Node n){

    if(! (n instanceof Element))
      return false;
    Element el = (Element)n;

    boolean result;
    if(useLabelConstruct){
      result = 
	el.getTagName().equals("label") &&
	el.getAttribute("kind").equals("guard");
    }
    else { // OLD
      result = el.getTagName().equals("guard");
    }
    return result;
  }
  /**
   * Test, whether this is an synchronisation Element.<BR>
   * <BR>
   * Depends on {@link #useLabelConstruct}.
   */
  public static boolean isSynchronisationElement(Node n){

    if(! (n instanceof Element))
      return false;
    Element el = (Element)n;

    boolean result;
    if(useLabelConstruct){
      result = 
	el.getTagName().equals("label") &&
	el.getAttribute("kind").equals("synchronisation");
    }
    else { // OLD
      result = el.getTagName().equals("synchronisation");
    }
    return result;
  }
  /**
   * Test, whether this is an invariant Element.<BR>
   * <BR>
   * Depends on {@link #useLabelConstruct}.
   */
  public static boolean isInvariantElement(Node n){

    if(! (n instanceof Element))
      return false;
    Element el = (Element)n;

    boolean result;
    if(useLabelConstruct){
      result = 
	el.getTagName().equals("label") &&
	el.getAttribute("kind").equals("invariant");
    }
    else { // OLD
      result = el.getTagName().equals("invariant");
    }
    return result;
  }
  
  

  // ===================================================================
  // [2] Tracing and Hunt Elements  
  // ===================================================================

  // ===========================================
  // [2.0.1] Forward: FORKS
  // ===========================================

  /**
   * Map <TT>&lt;entry&gt;</TT> or <TT>&lt;entrypoint&gt;</TT> to the 
   * (single) corresponding <B>fork Element</B>.<BR>
   * Throws exception, if used wrongly or in wrong context.
   *<BR>
   * <BR>
   * Should only be called in AND templates.
   */
  public Element getForkElementOfEntry(Element entry)
    throws Exception {
      
      Element theEntry;
      
    if(entry.getTagName().equals("entry")){
	theEntry = entry;
    } else if(entry.getTagName().equals("entrypoint")){
	theEntry = (Element)entry.getParentNode();
    } else 
      throw new Exception("ERROR: the element \n" +
			  entry.toString() +
			  "\n      is neither <entry> nor <entrypoint>");
    Element connection = getTheChildWithLabel(theEntry, "connection");
    Element target = getTheChildWithLabel(connection, "target");

    return getElementByID(target.getAttribute("ref"));
  }

  /**
   * Map <TT>&lt;entry&gt;</TT> or <TT>&lt;entrypoint&gt;</TT> to the 
   * (single) corresponding <B>target Element</B>.<BR>
   * Throws exception, if used wrongly or in wrong context.
   *<BR>
   */
  public Element getTargetElementOfEntry(Element entry)
    throws Exception {
      
    Element theEntry;
      
    if(entry.getTagName().equals("entry")){
      theEntry = entry;
    } else if(entry.getTagName().equals("entrypoint")){
      theEntry = (Element)entry.getParentNode();
    } else 
      throw new Exception("ERROR: the element \n" +
			  entry.toString() +
			  "\n      is neither <entry> nor <entrypoint>");
    Element connection = getTheChildWithLabel(theEntry, "connection");
    Element target = getTheChildWithLabel(connection, "target");

    return target;
  }
  

  public NodeList getTargetsOfFork(Element fork)
    throws Exception {
    if(sanityChecks &&
       (!(fork.getTagName()).equals("fork")))
      throw new Exception ("ERROR: entry of AND component points to non-fork: \n" +
			   fork.toString());
    // ----------------------------------------------------------
    NodeList connections = getAllChildrenWithLabel(fork, "connection");
    MutableNodeListImpl targets = new MutableNodeListImpl();
    for(int i=0; i < connections.getLength(); i++){
	NodeList localTargets = getAllChildrenWithLabel((Element)connections.item(i), 
							"target");
	for(int j=0; j < localTargets.getLength(); j++){
	    targets.addNode(localTargets.item(j));
	}
    }

    return (NodeList)targets;
  }

  
  // =============================================
  // [2.1] Collecting Target-Guards/Assignments
  // =============================================
  
  /**
   * Traverses the entry-fork starting with this target
   * (which is assumed to point at a component and an entry)<BR>
   * 
   * The guards and assignments present in the <EM>connection</EM>s are 
   * collected textually (Vector of strings), 
   * without any assumption on the order.<BR>
   * <BR>
   * <B>NOTE:</B>The result of this process possibly has to be adjusted
   * to the current instantiation (i.e., names have to be mapped)
   * The information for this can be extraced from the 
   * <TT>context</TT>, which is a list (stack?) of renamings.
   *       <B>!!! NOT IMPLEMENTED YET !!!</B>.
   * 
   * <H2>Invariants</H2>
   * Of the traversed components are also added to the <TT>guards</TT>
   * 
   * <H2>Local Clocks</H2>
   * Might be reset (i.e. added to the assignments.
   * <BLINK>!!! Not implemented yet -- there are no local declarations 
   * !!!</BLINK>
   * 
   */
  public void collectGuardsAndAssignmentsOfTarget(Element target,
						  Vector guards,
						  Vector assignments,
						  Object context)
    throws Exception {
    if(sanityChecks &&
       ( ( !((target.getTagName()).equals("target"))) ||
	 (target.getAttribute("entryref")).equals("")))
      throw new Exception("ERROR: the element " +
			  target.toString() +
			  "  is not a proper target.");

    String componentID = target.getAttribute("ref");
    Element component = getElementByID(componentID);
    Element entry = getEntryATargetPointsTo(target);
    Element connection = getConnectionOfEntry(entry);

    // -- invariants -------------------------------------------------------

    System.out.println(" >>>>>>>>>>>>>>>>>>> " + component.toString());

    Element invariant = getTheChildInvariantIfExists(component);
    if(invariant != null){
      String text = getCdataOfElement(invariant).trim();
      if(debug)
	System.out.println("))))) Propagating invariant:  >" + text + "<");
      // -- !!! here, we have to adjust the text, if parameters are allowed 
      if(text.length() > 0)
	guards.addElement(text.trim());
    } 
    // ---------------------------------------------------------------------
    addGuardsAndAssignmentsOfConnectionInContext(connection,
						 guards,
						 assignments,
						 context);
    // ---------------------------------------------------------------------
    if (isANDComponent(component)){
      // -- AND component: traverse further --------------------------------
      Element nextTarget = getTheChildWithLabel(connection, "target");
      Element fork = getElementByID(nextTarget.getAttribute("ref"));
      if( sanityChecks &&
	  (!(fork.getTagName()).equals("fork")))
	throw new Exception("ERROR: element " +
			    fork.toString() +
			    "  was expected to be a <fork>.");
      NodeList forkConnections = getAllChildrenWithLabel(fork, "connection");
      //!!! here, modify the context (i.e. add new params from THIS tpl).
      for(int i=0; i < forkConnections.getLength(); i++){
	addGuardsAndAssignmentsOfConnectionInContext((Element)forkConnections.item(i),
						     guards,
						     assignments,
						     context);
	Element connectionTarget = getTheChildWithLabel((Element)forkConnections.item(i), "target");
	
	collectGuardsAndAssignmentsOfTarget(connectionTarget,
					    guards,
					    assignments,
					    context);
      }
    }
  }
 
  /**
   * The TexualInstantiaion is handed over for context information
   * (i.e. renaming, that is necessary if parameters are allowed).<BR>
   * <BR>
   * Does not add guards or assignments that contain only whitespaces. <BR>
   * <BR>
   * 
   *
   * <B>can also be applied on transtions</B>
   */
  public void addGuardsAndAssignmentsOfConnectionInContext(Element connection,
							   Vector guards,
							   Vector assignments,
							   Object contextOfIt)
    throws Exception {

    NodeList connAssignments = getAllChildrenThatAreAssignments(connection);
    NodeList connGuards = getAllChildrenThatAreGuards(connection);
    
    // ----------------------------------------------------------
    if(sanityChecks && false &&
       (getTheChildSynchronisationIfExists(connection) != null))
      throw new Exception("FAILURE: the connection " +
			  connection.toString() +
			  "  contains a synchronisation, which is disallowed in " +
			  Flatten.VERSION_NAME); // -------------
    for(int i=0; i < connAssignments.getLength(); i++){
      String text = getCdataOfElement((Element)connAssignments.item(i));
      // -- !!! here, we have to adjust the text, if parameters are allowed 
      if(text.trim().length() > 0)
	assignments.addElement(text.trim());
    }
    for(int i=0; i < connGuards.getLength(); i++){
      String text = getCdataOfElement((Element)connGuards.item(i));
      // -- !!! here, we have to adjust the text, if parameters are allowed 
      if(text.trim().length() > 0)
	guards.addElement(text.trim());
    }
  }

  // =============================================
  //[2.2] Backward: Joins
  // =============================================


  // =================================================================
  // [3] Retrieve Elements
  // =================================================================

  /**
   * Browse the system definition to get the name of instantiations that 
   * are declared to be part of the top-level parallel composition.
   */
  public Vector getNamesOfAliveSystemParts()
    throws Exception {
    Element system = getTheChildWithLabel(oldRoot, "system");
    String sysString = getCdataOfElement(system).trim();
    int from = 7; // "system"
    int to = sysString.indexOf(';');
    if( !(sysString.startsWith("system ")) ||
	(to < 0))
      throw new Exception("ERROR: incorrect system definition: \n" +
			  ">>" + sysString + "<<");
    String parts = sysString.substring(from, to);
    return splitCommaSeperatedString(parts);
  }

  /**
   * Retrieve all child nodes that are assignments.
   */
  private NodeList getAllChildrenThatAreAssignments(Element connection){

    NodeList allChildren = connection.getElementsByTagName("*");
    MutableNodeListImpl result = new MutableNodeListImpl();
    for(int i=0; i < allChildren.getLength(); i++){
      if(isAssignmentElement((Node)allChildren.item(i)))
	result.addNode((Node)allChildren.item(i));
    }
    return result;
  }
  /**
   * Retrieve all child nodes that are guards.
   */
  private NodeList getAllChildrenThatAreGuards(Element connection){

    NodeList allChildren = connection.getElementsByTagName("*");
    MutableNodeListImpl result = new MutableNodeListImpl();
    for(int i=0; i < allChildren.getLength(); i++){
      if(isGuardElement((Node)allChildren.item(i)))
	result.addNode((Node)allChildren.item(i));
    }
    return result;
  }
  /**
   * Retrieve all child nodes that are synchronisations.
   */
  private NodeList getAllChildrenThatAreSynchronisation(Element connection){
    
    NodeList allChildren = connection.getElementsByTagName("*");
    MutableNodeListImpl result = new MutableNodeListImpl();
    for(int i=0; i < allChildren.getLength(); i++){
      if(isSynchronisationElement((Node)allChildren.item(i)))
	result.addNode((Node)allChildren.item(i));
    }
    return result;
  }
  

  /**
   * Returns the (one) &lt;entry&rt; element that a target points to
   */
  private Element getEntryATargetPointsTo(Element target)
    throws Exception {
    if(sanityChecks &&
       ( ( !((target.getTagName()).equals("target"))) ||
	 (target.getAttribute("entryref")).equals("")))
      throw new Exception("ERROR: the element " +
			  target.toString() +
			  "  is not a proper target.");

    Element entry = getElementByID(target.getAttribute("entryref"));
    if((entry.getTagName()).equals("entrypoint")){
      entry = (Element)entry.getParentNode();
    }
    return entry;    
  }
  
  /**
   * Returns s Vector of Elements, containing all the pointing-to 
   * connections.     <BR>
   * <BR>
   * Throws Exception, if the argument is wrong and <TT>{@link 
   *    DocumentReader#sanityChecks} is <EM>true</EM> - or if there are no 
   * ingoing
   * connections.
   */
  public Vector getConnectionsToExitOrExitpoint(Element exitOrExitpoint)
    throws Exception {
    if(sanityChecks &&
       (!((exitOrExitpoint.getTagName()).equals("exit"))) &&
       (!((exitOrExitpoint.getTagName()).equals("exitpoint"))))
      throw new Exception("ERROR: the element " +
			  exitOrExitpoint.toString() +
			  "  is neither exit nor exitpoint.");
    Vector result = new Vector();
    Element theExit = exitOrExitpoint;
    if((exitOrExitpoint.getTagName()).equals("exitpoint"))
      theExit = (Element)exitOrExitpoint.getParentNode();
    
    NodeList theConnections = getAllChildrenWithLabel(theExit, "connection");
    for(int i=0; i < theConnections.getLength(); i++){
      result.addElement(theConnections.item(i));
    }
    if(result.size() == 0)
      throw new Exception("ERROR: the exit \n" +
			    theExit.toString() +
			  "\n is not reachable!");
    return result;
  }

  /**
   * Returns the (one) connection an entry points to
   */
  private Element getConnectionOfEntry(Element entry)
    throws Exception {
    if(sanityChecks &&
       (!(entry.getTagName()).equals("entry")))
      throw new Exception("ERROR: the element " +
			  entry.toString() +
			  "  is not a proper entry.");
    return getTheChildWithLabel(entry, "connection");
  }

  /**
   * Returns the text of a guard, if present and not the empty string, 
   * otherwise, returns 
   * <TT>null</TT>.<BR>
   * <BR>
   * Throws Exception, if not called on transition or connection.
   */
  public String getTextualGuardOfTransitionIfPresent(Element transition)
    throws Exception {
    if( !(transition.getTagName()).equals("transition") &&
	!(transition.getTagName()).equals("connection"))
      throw new Exception("ERROR: the Element \n" +
			  transition.toString() +
			  "\nis neither transition nor connection.");
    Element guard = getTheChildGuardIfExists(transition);
    if(null == guard)
      return null;

    String text = getCdataOfElement(guard);
    if((text.trim()).length() >0 )
      return text.trim();
    else
      return null;    
  }
  /**
   * Returns the text of a assignment, if present and not the empty string, 
   * otherwise, returns 
   * <TT>null</TT>.<BR>
   * <BR>
   * Throws Exception, if not called on transition or connection.
   */
  public String getTextualAssignmentOfTransitionIfPresent(Element transition)
    throws Exception {
    if( !(transition.getTagName()).equals("transition") &&
	!(transition.getTagName()).equals("connection"))
      throw new Exception("ERROR: the Element \n" +
			  transition.toString() +
			  "\nis neither transition nor connection.");
    Element assignment = getTheChildAssignmentIfExists(transition);
    if(null == assignment)
      return null;

    String text = getCdataOfElement(assignment);
    if((text.trim()).length() >0 )
      return text.trim();
    else
      return null;    
  }
  
  /**
   * Retrieve (if existent) the child that is assignment.<BR>
   * <BR>
   * Return <TT>null</TT>, if it does not exist.<BR>
   * <BR>
   * Throws exception, if more than two such children exist.
   */
  public static Element getTheChildAssignmentIfExists(Element connection)
    throws Exception {
    Element result = null;

    NodeList allChildren = connection.getElementsByTagName("*");
    for(int i=0; i < allChildren.getLength(); i++){
      if(isAssignmentElement((Node)allChildren.item(i))){
	if( null == result )
	  result = (Element)allChildren.item(i);
	else
	  throw new Exception("ERROR: more than one assignment child at Element \n" +
			      connection.toString());
      }
    }
    return result;
  }
  /**
   * Retrieve (if existent) the child that is guard.<BR>
   * <BR>
   * Return <TT>null</TT>, if it does not exist.<BR>
   * <BR>
   * Throws exception, if more than two such children exist.
   */
  public static Element getTheChildGuardIfExists(Element connection)
    throws Exception {
    Element result = null;

    NodeList allChildren = connection.getElementsByTagName("*");
    for(int i=0; i < allChildren.getLength(); i++){
      if(isGuardElement((Node)allChildren.item(i))){
	if( null == result )
	  result = (Element)allChildren.item(i);
	else
	  throw new Exception("ERROR: more than one guard child at Element \n" +
			      connection.toString());
      }
    }
    return result;
  }
  /**
   * Retrieve (if existent) the child that is synchronisation.<BR>
   * <BR>
   * Return <TT>null</TT>, if it does not exist.<BR>
   * <BR>
   * Throws exception, if more than two such children exist.
   */
  public static Element getTheChildSynchronisationIfExists(Element connection)
    throws Exception {
    Element result = null;

    NodeList allChildren = connection.getElementsByTagName("*");
    for(int i=0; i < allChildren.getLength(); i++){
      if(isSynchronisationElement((Node)allChildren.item(i))){
	if( null == result )
	  result = (Element)allChildren.item(i);
	else
	  throw new Exception("ERROR: more than one synchronisation child at Element \n" +
			      connection.toString());
      }
    }
    return result;
  }
  /**
   * Retrieve (if existent) the child that is invariant.<BR>
   * <BR>
   * Return <TT>null</TT>, if it does not exist.<BR>
   * <BR>
   * Throws exception, if more than two such children exist.
   */
  public static Element getTheChildInvariantIfExists(Element connection)
    throws Exception {
    Element result = null;

    NodeList allChildren = connection.getElementsByTagName("*");
    for(int i=0; i < allChildren.getLength(); i++){
      if(isInvariantElement((Node)allChildren.item(i))){
	if( null == result )
	  result = (Element)allChildren.item(i);
	else
	  throw new Exception("ERROR: more than one invariant child at Element \n" +
			      connection.toString());
      }
    }
    return result;
  }
  


  // ===============================================
  // [3.1] dealing with hashtables
  // ===============================================

  /**
   * Return the original document as string
   */
  public String docToString(){
    return origDoc.toString();
  }


  // =================================================================
  // [4] Global Joins of Global Elements (global exit)
  // =================================================================

  /**
   * Create the Joins for stopping global elements (direct children of 
   * root);<BR>
   * <BR>
   * 
   */
  public void createJoinsForGlobalExits(TextualInstantiation tinst, Element globalEntry)
    throws Exception {
    
    if(debug)
      System.out.println("!!! Creating joins for global exits of " + tinst.toString() );

    if(sanityChecks &&
       ( !((globalEntry.getTagName()).equals("globalinit"))))
       throw new Exception("ERROR: wrong argument types: " +
			   globalEntry.toString());
       
       NodeList allExitTransitions = new MutableNodeListImpl();
       String canExit = globalEntry.getAttribute("canexit");
       if( (canExit.equals("")) || (canExit.equals("no")) ){

       }
       else if(canExit.equals("all")) {
	 NodeList allExits = getAllChildrenWithLabel(tinst.templateElement, "exit");
	   Element component = wrapComponentAroundGlobalInstantiation(tinst.originalInstantiationName, getElementName(tinst.templateElement));
	   String componentID = getElementID(component);

	 for(int i = 0; i < allExits.getLength(); i++){
	   // -- create fake connection, carrying the source ---------------
	   String exitID = getElementID((Element)allExits.item(i));
	   Element fakeCon = origDoc.createElement("connection");
	   Element fakeSource = origDoc.createElement("source");
	   fakeCon.appendChild(fakeSource);
	   fakeSource.setAttribute("ref", componentID);
	   fakeSource.setAttribute("exitref", exitID);
	   ((MutableNodeListImpl)allExitTransitions).addNode(fakeCon);
	 }
       }
       else if(canExit.equals("specified")) {
	 NodeList allOriginalTransitions = getAllChildrenWithLabel(globalEntry, "connection"); 
	 Element component = wrapComponentAroundGlobalInstantiation(tinst.originalInstantiationName, getElementName(tinst.templateElement));
	 String componentID = getElementID(component);
 
	 for(int i=0; i < allOriginalTransitions.getLength(); i++){
	   String exitID = (getTheChildWithLabel((Element)allOriginalTransitions.item(i), "source")).getAttribute("exitref");
	   Element fakeCon = origDoc.createElement("connection");
	   Element fakeSource = origDoc.createElement("source");
	   fakeCon.appendChild(fakeSource);
	   fakeSource.setAttribute("ref", componentID);
	   fakeSource.setAttribute("exitref", exitID);
	   ((MutableNodeListImpl)allExitTransitions).addNode(fakeCon);
	 }
       }
       else
	 throw new Exception("ERROR: unknown canexit attribute value \"" +
			     canExit + "\"");

       if(allExitTransitions.getLength() > 0){

	 if(debug)
	   System.out.println("!!! found " + allExitTransitions.getLength() + " global exits for" + globalEntry.getAttribute("instantiationname"));


	 for(int j=0; j < allExitTransitions.getLength(); j++){
	   // SHOULD GIVE BACK THE SAME GLOBAL JOIN ALL THE TIME
	   Element connection = (Element)allExitTransitions.item(j);
	   


	   GlobalJoin gj = GlobalJoin.getGlobalJoin(connection, Flatten.rootInstDummy);
	 }
       }
  }
  
  // =================================================================
  // [5] AUX Services
  // =================================================================
  /**
   * Given a string containing a comma-speparated list, split it into a 
   * vector
   * of Strings (without the commata).<BR>
   * 
   * Trims the strings (cuts away whitespaces).
   */
   public static Vector splitCommaSeperatedString(String s){
     Vector result = new Vector();
     int pos;
     s = s.trim();
     
     while( ( pos = s.indexOf(',')) >= 0 ){
       result.addElement((s.substring(0,pos)).trim());
       s = s.substring(pos+1);
     }
     s = s.trim();
     if(s.length() > 0)
       result.addElement(s);
     
     return result;
   }

  /**
   * Returns a (dummy) component Element, that points to a global 
   * instantiation.<BR>
   * 
   * Once constructed, these Elements are stored in a Hashtable, so the 
   * result on the same call will be unique.
   * <BR>
   * <B>Used to make the ComponentMapper work on global Elements</B>
   */
  public Element wrapComponentAroundGlobalInstantiation(String instName, String templateName)
    throws Exception {
    String hashString = instName + unambigousNameSeparator + templateName;

    Object result = wrapAroundInstHashTable.get(hashString);
    if(null == result){
      result = origDoc.createElement("component");
      ((Element)result).setAttribute("instantiates", templateName);
      Element name = origDoc.createElement("name");
      Text cdata = origDoc.createTextNode(instName);
      name.appendChild(cdata);
      fakeIDCounter++;
      ((Element)result).setAttribute("id", fakeIDName + fakeIDCounter);
      ((Element)result).appendChild(name);
      addIDAndElementToHashTable( (fakeIDName + fakeIDCounter),
				  (Element)result);
      wrapAroundInstHashTable.put(hashString, result);
    }

    if(debug)
      System.out.println("&&&&->&&&& wrapComponents: " + hashString + " -> " + ((Element)result).toString());

    return (Element)result;
  }
  // =================================================================
  // AUX
  // =================================================================

  /**
   * Make hash table entries for Elements that are <B>not</B> accessible in 
   * the original document<BR>
   * <BR>
   * Used to retrieve components with fake IDs.
   */
  private void addIDAndElementToHashTable(String ID, Element el)
    throws Exception {
    if(null == hashOriginalIDsToElements)
      memorizeIDsOfOriginalDocument();

    hashOriginalIDsToElements.put(ID, el);
  }

}
