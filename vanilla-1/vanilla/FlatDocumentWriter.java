// -*- mode: JDE; c-basic-offset: 2; -*-
// /////////////////////////////////////////////////////////////
// Create the flat Document
// 
// Synopsis:
//  Huppaal
// /////////////////////////////////////////////////////////////
// @TABLE OF CONTENTS:		       [TOCD: 18:28 18 Feb 2001]
//
//      [0.1] Global Elements
//      [0.2] Global Flags influencing the translation
//      [0.3] Accumulative Stuctures  
//      [0.4] Hash Tables
//          [0.4.1] Initialize
//  [1] PUBLIC GROWING of Elements
//      [1.1] Templates and Instantiation
//      [1.2] Modifying Transitions
//          [1.2.1] Modifying special Elements
//      [1.3] Adding Parts
//      [1.4] Adding children at the right place
//      [1.5] Creation of Elements
//  [2] SPECIAL TEMPLATES
//  [3] SEMAPHORE GENERATION
//  [4] RETRIEVE ELEMENTS
// ==========================================================
// @FILE:    FlatDocumentWriter.java
// @PLACE:   BRICS AArhus; host:harald
// @FORMAT:  java
// @AUTHOR:  M. Oliver M'o'ller     <omoeller@brics.dk>
// @BEGUN:   Tue Nov 21 17:43:14 2000
// @VERSION: Vanilla-1                  Mon Apr  9 15:40:40 2001
// /////////////////////////////////////////////////////////////
// 

import DocumentReader;
import HierarchicalDocumentReader;
import DocumentWriter;
import TextualInstantiation;


import java.io.*;

import java.lang.*;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Enumeration;
import java.util.Date;

import java.text.DateFormat;


import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


//**** from other packages 
import org.apache.crimson.tree.XmlDocument;
import org.apache.crimson.tree.XmlDocumentBuilder;
//****************************************

/**
 * This class is used to abstract away from dtd changes when creating
 * the flat document (in <TT>{@link #uppaalDTD}</TT> grammar).
 *
 * <H2>Usage</H2>
All changes and additions to the document are done via method calls to

 The main method is <TT>{@link #complete}</TT>, that 
 * 
 * @author <A HREF="MAILTO:omoeller@brics.dk?subject=FlatDocumentWriter.java%20(Vanilla-1%20Wed%20Apr%204%2012:26:05%202001)">M. Oliver M&ouml;ller</A>
 * @version Vanilla-1                  Mon Apr  9 15:40:40 2001
 */
public class FlatDocumentWriter 
  extends DocumentWriter {

  // //////////////////////////////////////////////////////////////////////
  // ////////////////////////////// FIELDS ////////////////////////////////
  // //////////////////////////////////////////////////////////////////////

  /**
   * Determines whether translations uses the &lt;label&gt; construct or 
   * not.<BR>
   * <BR>
   * <B>From <TT>uppaal-1.4</TT> / <TT>uppaal-3-1-39 </TT> on, this should 
   * be <TT>true</TT>.</B>
   */
  static final boolean useLabelConstruct = true;
  
  /**
   * Determines whether grammar uses the &lt;label&gt; construct also
   * for invariants.
   * <BR>
   * <B>For <TT>uppaal-1.4</TT>, this is <TT>false</TT>.</B>
   */
  static final boolean useLabelForInvariants = false;


  /**
   *  Version of the flat uppaal grammar that is used.
   */ 
  static final String uppaalDTD = "uppaal-1.4.dtd";
 
 
  /**
   *  Used <TT>DTD</TT> for flat Uppaal documents.<BR>
   *  Give the <TT>http://</TT> or <TT>file:/</TT> location
   */ 
  static final String uppaalDTD_URI = "http://www.brics.dk/~omoeller/hta/xml/" + uppaalDTD;
  
  
  /**
   * Module for Layout (Re-)Computations (post-processing)
   */
  Layouter layouter;


  // ===============================================
  // [0.1] Global Elements
  // ===============================================
 
  /**
   * Root of <TT>{@link #doc}</TT>
   */
  private Element root ;

  private Element declaration;
  private Element instantiation;
  private Element system;


  // ===============================================
  // [0.2] Global Flags influencing the translation
  // ===============================================
  

  /**
   * If true, ignore the x/y-information given in the (hierarchical) 
   * templates and use a default-layout.<BR><BR>
   * This is possible, if using only the method <TT>{@link
   * #inventLocation}</TT> for creation of new locations.
   *
   * call <TT>{@link #resetDefaultLayout}</TT> when starting a new template
   */
  public static boolean inventNewGoegraphicalLocations = true;


  /**
   * Keeping track, whether the full-translation is already done
   * <H4><I>(Currently, after a full translation, is is not possible any 
   * more
   * to call a add-method)</I></H4>
   */
  private boolean completed = false;

  // ===============================================
  // [0.3] Accumulative Stuctures  
  // =============================================== 


  /**
   * Templates that are to be instantiated at some point
   */
  protected Stack templates;


  /**
   * Text declaring the flat system.<BR>
   * Accumulates <B>all</B>
   * the timed automata executed in <I>parallel</I>. During construction,
   * they are stored in <TT>{@link #systemComponents}</TT>.
   * 
   */
  private StringBuffer systemText;
  
  /**
   * Store all (instantiated) component of the system by the
   * <I>name String</I>
   */
  protected Stack systemComponents;


  /**
   * A vector of <EM>Strings</EM> that are to be added to 
   * the (global) 
   * instantiation
   * <H3>!! might switch to more sophisticated data structure as content 
   * !!</H3>
   * @see TextualInstantiation
   */
  static Stack slackInstantiations;
  
  /**
   * Text describing the Instantiations of templates
   * used for the system.
   * 
   * <h3>Currently, every template is instantiated exactly <I>once</I>
   * </h3>
   */
  static StringBuffer instantiationText;

  /**
   * Text inserted in the (flat) declaration label.<BR><BR>
   * Contains original global variables plus
   * <I>flattening slack</I> like newly introduced channels etc.
   */
  static StringBuffer declarationText;
  

  /**
   * Store kickoff template Element, once crated...
   */
  public Element kickOffTemplate;

  /**
   * Id of kickoff location, where every direct child of the root is 
   * activated.<BR>
   * <BR>
   * Needed for global exits.
   */
  public String kickOffDoneID;

  // ===============================================
  // [0.4] Hash Tables
  // ===============================================

  /**
   * Store all newly generated (flat) locations.<BR>
   * Requires the use of the methods <TT>{@link #inventLocation}</TT> and 
   * <TT>{@link #changeLocationID}</TT> in order to work properly.
   */
  private static Hashtable hashIDsToNewLocationElements;


  // //////////////////////////////////////////////////////////////////////
  // //////////////////////////  CONSTRUCTORS  ////////////////////////////
  // //////////////////////////////////////////////////////////////////////

  /**
   * Default Constructor
   */
  public FlatDocumentWriter(){
    
    if(debug)
      System.out.println("%%% -- new flat Document: create ------------------------------");
    
    doc.setDoctype("nta",
		   uppaalDTD_URI,
		   ""
		   );

    root     = doc.createElement("nta");
    root.appendChild(doc.createComment(" TRANSLATED by   Flatten   " +
				       
				       Flatten.VERSION +
				       "\n\t          on   " +
				       DateFormat.getDateTimeInstance().format(new Date()) +
				       "\n\t          from " +
				       "a " + HierarchicalDocumentReader.huppaalDTD + " document" +
				       " "));
    
    declaration = doc.createElement("declaration");
    instantiation = doc.createElement("instantiation");
    system = doc.createElement("system");


  // -- Construct the root -----------------------------------
    
    root.appendChild(declaration);
    root.appendChild(instantiation);
    root.appendChild(system);

    
    doc.appendChild(root);
    
    //       root.appendChild(doc.createElement("FAILURE")); // artificial error
  

    // =====================================
    // [0.4.1] Initialize
    // =====================================

    templates = new Stack();
    systemComponents = new Stack();
    slackInstantiations = new Stack();

    declarationText = new StringBuffer();

    hashIDsToNewLocationElements = new Hashtable();

    layouter = new SkewedGridLayouter(this);

    if(debug)
      System.out.println("%%% -- new flat Document: skeleton created. ------------------------");

  }
  
  // //////////////////////////////////////////////////////////////////////
  // ///////////////////////////// METHODS  ///////////////////////////////
  // //////////////////////////////////////////////////////////////////////

  /**
   * <H2>Main Method</H2>
   * Triggers the translation of all added components.
   */
  public void complete()
    throws Exception {

    // ... do the translation, if necessary ... !!!

    ///    !!! correct..
    
    // -- Add Templates ----------------------------------------------------

    while(!templates.empty()){
      root.insertBefore((Element)templates.pop(), instantiation);
    }

    // -- Processing Declarations ------------------------------------------
      
    addTextualContentToElement(declaration, declarationText.toString());


    // -- Processing Instantiations ----------------------------------------
    
    if(debug){
      System.out.println("!! --- Showing Textual Instantiations: ------------------------------ ");
      for(Enumeration e = slackInstantiations.elements(); e.hasMoreElements();){
	System.out.println(" -- " + ((String)e.nextElement()));
      }
    }
    if(debug)
      System.out.println("!! --- Showing Textual Instantiations -------------------------- DONE ");
    StringBuffer instantiationText = new StringBuffer();
    for(Enumeration e = slackInstantiations.elements(); e.hasMoreElements();){
      instantiationText.append(((String)e.nextElement()).toString());
      instantiationText.append(";");
      if(e.hasMoreElements()){
	instantiationText.append("\n");
      }}
    addTextualContentToElement(instantiation, instantiationText.toString());

    // -- Store the collected System ---------------------------------------
    
    systemText = new StringBuffer("system ");
    for(Enumeration e = systemComponents.elements(); e.hasMoreElements();){
      systemText.append((String)e.nextElement());
      if(e.hasMoreElements())
	systemText.append(", ");
    }
    systemText.append(";\n");
    system.appendChild(doc.createTextNode(systemText.toString()));

    // ---------------------------------------------------------------------
    generateEndTemplateSemaphores();
    // ---------------------------------------------------------------------
    completed = true;
  }

  /**
   * Return the (completed) XMLDocument
   */
  public XmlDocument getDoc()
    throws Exception {

    if(!completed)
      complete();
    return doc;
  }



  // ===================================================================
  // [1] PUBLIC GROWING of Elements
  // ===================================================================


  
  /**
   * Append some declaration (purely textual)<BR>
   * <H3>!! This might be more sophisticated at some point</H3>
   */
  public void addDeclarationText(String s)
    throws Exception{
    if(completed)throw new Exception("ERROR: document already completed.");

    if(debug)
      System.out.println("::::::::::::::::::::new declaration: " + s);

    declarationText.append(s);
  }
  
  /**
   * Add just a declaration (e.g. <TT>"chan x"</TT>)
   */
  public void addDeclaration(String s)
    throws Exception{
    if(completed)throw new Exception("ERROR: document already completed.");

    addDeclarationText(s + ";\n");
  }
  
  /**
   * Search declarations (Existing text) for declaration of a channel old
   * (<TT>chan</TT> or <TT>urgent chan</TT>) and declare a channel of the
   * same type, with new name.<BR>
   * <BR>
   * Throws exception, if declaration of the old channel is not found.
   *    !!! <B>BUG:</B> also finds <PRE>/* ...</PRE> commented declarations !!!
   */
  public void copyChannelDeclaration(String oldChanName, String newChanName)
    throws Exception {
    String decSoFar = declarationText.toString();
    
    if(debug)
      System.out.println("## Duplicating channel: " + oldChanName + "   [" + newChanName + "]");

    int i;
    int len = decSoFar.length();
    int patLen = oldChanName.length();
    int lineStart = -1;

    for(i = 1; i+patLen < len; i++){
      if( decSoFar.regionMatches(false,i,oldChanName,0,patLen) &&
	  Character.isWhitespace(decSoFar.charAt(i-1)) &&
	  ( Character.isWhitespace(decSoFar.charAt(i+patLen)) ||
	    decSoFar.charAt(i+patLen) == ';' )  &&
	  notACommentedLine(decSoFar.substring(lineStart = 1+ max(decSoFar.lastIndexOf('\n', i), decSoFar.lastIndexOf('\r', i))))
	  ) {
	;
	addDeclaration(decSoFar.substring(lineStart, i) + " " + newChanName );

	return;
      }
    }
    throw new Exception("ERROR: no declaration of >>" + oldChanName + "<< found in \n" + decSoFar);
  }
  
  /**
   * Check in  String, whether the first line is a commented line
   * (in terms of Uppaal Declarations)<BR>
   * Currently, every line where the first non-whitespace character is a 
   * <TT>'/'</TT> is consiered a comment line.
   * <BR>
   * Assumes that the first line contains characters other than whitespace
   */
  private boolean notACommentedLine(String s){
    int i = 0;
    while(Character.isWhitespace(s.charAt(i)))
      i++;
    
    return ( s.charAt(i) != '/' );
  }
  // ==================================================
  // [1.1] Templates and Instantiation
  // ==================================================
 

  /**
   * Add a new template
   */
  private void addTemplate(Element template)
    throws Exception {
    if(completed)throw new Exception("ERROR: document already completed.");

    if(sanityChecks && 
       (!(template.getTagName()).equals("template")))
       throw new Exception("ERROR: not a <template> Element: \n" +
			   template.toString());
    templates.push(template);
  }


  /**
   * Arrange that a new instantiation is created.<BR>
   * This means, that the template (if not present) is added and
   * the instantiations and the system are updated.
   */
  public void addInstantiation(TextualInstantiation tinst)
    throws Exception {
    if(completed)throw new Exception("ERROR: document already completed.");

    if(debug)
      System.out.println("%%%%% ADDED INSTANTIATION: " + tinst.objectName);

    slackInstantiations.push(tinst.toString());
    systemComponents.push(tinst.objectName);

  }

  // ===============================================
  // [1.2] Modifying Transitions
  // ===============================================

  /**
Change synchronization text of a transtion.<BR>
* <BR>
* 
   */
  protected void changeSyncTextAtTransition(Element transition, String newText)
  throws Exception {
    if( sanityChecks &&
	(!(transition.getTagName()).equals("transition")))
      throw new Exception("ERROR: not a <transition> Element: \n" +
			  transition.toString());
    if(debug)
      System.out.println("///// changing synchronization text in transition " + transition.toString() + " to >>" + newText + "<<");

    Element syncElement = getTheChildThatIsSynchronisationIfExists(transition);
    if(null == syncElement)
      throw new Exception("ERROR: no syncronisatio declared in " + transition.toString());

    while(syncElement.hasChildNodes())
      syncElement.removeChild(syncElement.getFirstChild());

    addTextualContentToElement(syncElement, newText);      	
  }

  /**
   * Add a !-synchronisation (without caring about graphical 
   * information)<BR><BR>
   * <I>Assumes that the argument is a transition</I>
   * <H3>!! Might be augmented with positioning information at 
   * some 
   * point</H3>
   */
  protected void addSendSynchronisationToTransition(String channelName,
						    Element el)
    throws Exception {
    if( sanityChecks &&
	(!(el.getTagName()).equals("transition")))
      throw new Exception("ERROR: not a <transition> Element: \n" +
			  el.toString());

    Element sync = createSynchronisationInheritingCoordinates(el);
    addTextualContentToElement(sync, channelName + "!");
  }
  /**
   * Add a ?-synchronisation (without caring about graphical 
   * information)<BR><BR>
   * <I>Assumes that the argument is a transition</I>
   * <H3>!! Might be augmented with positioning information at some 
   * point</H3>
   */
  protected void addReceiveSynchronisationToTransition(String channelName,
						       Element el)
    throws Exception {
    if( sanityChecks &&
	(!(el.getTagName()).equals("transition")))
      throw new Exception("ERROR: not a <transition> Element: \n" +
			  el.toString());

    Element sync = createSynchronisationInheritingCoordinates(el);
    addTextualContentToElement(sync, channelName + "?");
  }
  /**
   * Add an assigment to a transition (as a child).<BR>
   * Returns the assignment Element.
   */
  protected Element addAssignmentToTransition(Element el)
    throws Exception {
    if( sanityChecks &&
	(!(el.getTagName()).equals("transition")))
      throw new Exception("ERROR: not a <transition> Element: \n" +
			  el.toString());

    Element res = createAssignmentInheritingCoordinates(el);
    el.appendChild(res);

    return res;
  }
  /**
   * Add an assigment to a transition (as a child), including some initial 
   * text<BR>
   * Returns the assignment Element.
   */
  protected Element addAssignmentToTransition(Element el, String assText)
    throws Exception {
    if( sanityChecks &&
	(!(el.getTagName()).equals("transition")))
      throw new Exception("ERROR: not a <transition> Element: \n" +
			  el.toString());

    Element res = addAssignmentToTransition(el);
    addTextualContentToElement(res, assText);

    return res;
  }
  
  /**
   * Add a guard to a transition (as a child).<BR>
   * Returns the guard Element.
   */
  protected Element addGuardToTransition(Element el)
    throws Exception {
    if( sanityChecks &&
	(!(el.getTagName()).equals("transition")))
      throw new Exception("ERROR: not a <transition> Element: \n" +
			  el.toString());

    Element res = createGuardInheritingCoordinates(el);
    el.appendChild(res);

    return res;
  }
  /**
   * Add a guard to a transition (as a child), including some guard text<BR>
   * Returns the guard Element.
   */
  protected Element addGuardToTransition(Element el, String guardText)
    throws Exception {
    if( sanityChecks &&
	(!(el.getTagName()).equals("transition")))
      throw new Exception("ERROR: not a <transition> Element: \n" +
			  el.toString());
    Element result = addGuardToTransition(el);
    addTextualContentToElement(result, guardText);

    return result;
  }
  

  /**
   * Add a synchronisation to a transition (as a child).<BR>
   * Returns the synchronisation Element.
   */
  protected Element addSynchronisationToTransition(Element el)
    throws Exception {
    if( sanityChecks &&
	(!(el.getTagName()).equals("transition")))
      throw new Exception("ERROR: not a <transition> Element: \n" +
			  el.toString());

    Element res = createSynchronisationInheritingCoordinates(el);
    el.appendChild(res);

    return res;
  }
  /**
   * Add a synchronisation to a transition (as a child), including some 
   * initial text.<BR>
   * Returns the synchronisation Element.
   */
  protected Element addSynchronisationToTransition(String syncText, Element el)
    throws Exception {
    if( sanityChecks &&
	(!(el.getTagName()).equals("transition")))
      throw new Exception("ERROR: not a <transition> Element: \n" +
			  el.toString());

    Element result = addSynchronisationToTransition(el);
    addTextualContentToElement(result, syncText);

    return result;
  }
  


  // ========================================
  // [1.2.1] Modifying special Elements
  // ========================================
  
  /**
   * Add a committed element<BR>
   * Throws an exception, if the element is not a location.<BR><BR>
   * <I>(might cause problems if called at the wrong time, for the order 
   * of child nodes matters in the <TT>{@link #uppaalDTD}</TT>)</I>
   */
  protected void makeLocationCommitted(Element el)
    throws Exception {
    if(sanityChecks &&
       (!(el.getNodeName()).equals("location")))
      throw new Exception("ERROR: Element " +
			  el.toString() +
			  " is not a location.");

    if(null == getTheChildWithLabelIfExists(el, "committed")){
      Element committed =  doc.createElement("committed");
      el.appendChild(committed);
    }
  }


  /**
   * <B>NOTE:</B> Only works, if location has been inserted in a template 
   * element alread!
   */
  protected void makeLocationInitial(Element loc)
  throws Exception {
    if(sanityChecks &&
       (!(loc.getNodeName()).equals("location")))
      throw new Exception("ERROR: Element " +
			  loc.toString() +
			  " is not a location.");

    Element template = (Element)loc.getParentNode();
    NodeList inits =  getAllChildrenWithLabel(template, "init");
    if(inits.getLength() == 0){ // -- create init --------------------------
      Element init = doc.createElement("init");
      init.setAttribute("ref", loc.getAttribute("id"));

      NodeList transitions = getAllChildrenWithLabel(template, "transition");
      if(transitions.getLength() > 0){ // -- append before first transition 
	template.insertBefore(init, transitions.item(0));
      }
      else { // -- append at the very end -----------------------
	template.appendChild(init);
      }
    }
    else if (inits.getLength() == 1){ // -- set init -----------------------
      Element init = (Element)inits.item(0);
      init.setAttribute("ref", loc.getAttribute("id"));
    }
    else throw new Exception("ERROR: template " +
			     template.toString() +
			     " has more than one init elements.");
  }

  
  // ===============================================
  // [1.3] Adding Parts
  // ===============================================

  /**
   * Adds some guard text to a transition, i.e. builds the <EM>logical 
   * AND</EM> with its existing guard.<BR>
   * If no guard Element exists jet, one is created.
   */
  public void conjunctTextualGuardToTransition(Element transition, String text)
    throws Exception {
    if ( sanityChecks &&
	 (!(transition.getTagName()).equals("transition")))
      throw new Exception("ERROR: element " +
			  transition.toString() +
			  "  is not a <transition>.");

    Element guard = getTheChildThatIsGuardIfExists(transition);
    if(guard == null){
      addGuardToTransition(transition , text);
    }
    else {
      if((getCdataOfElement(guard).trim()).equals(""))
	appendTextToTextualContentOfElement(guard, text);
      else
	appendTextToTextualContentOfElement(guard, ", " + text);
    }
  }
  /**
   * Adds some assignment text to a transition, i.e. append it to the
   * (possibly empty) textual list of existing assignments.<BR>
   * If no assignement Element exists jet, one is created.
   */
  public void conjunctTextualAssignmentToTransition(Element transition, String text)
    throws Exception {
    if ( sanityChecks &&
	 (!(transition.getTagName()).equals("transition")))
      throw new Exception("ERROR: element " +
			  transition.toString() +
			  "  is not a <transition>.");

    Element assignment = getTheChildThatIsAssignmentIfExists(transition);
    if(assignment == null){
      addAssignmentToTransition(transition , text);
    }
    else {
      if((getCdataOfElement(assignment).trim()).equals(""))
	appendTextToTextualContentOfElement(assignment, text);
      else
	appendTextToTextualContentOfElement(assignment, ", " + text);
    }
  }
  

  // -----------------------------------------------------------------------

  /**
   * Appends some text to the #PCDATA, that is associated with this 
   * element (i.e in between <TT>&lt;element&gt;&lt;/element&gt;</TT>).<BR>
   * 
   * Relies on method "normalize" to join adjacent child text nodes.
   * @see  org.w3c.dom.Node
   */ 
  public void appendTextToTextualContentOfElement(Element el, String text)
    throws Exception {
    Text cdata = doc.createTextNode(text);
    el.appendChild(cdata);
    el.normalize();
  }


  // -----------------------------------------------------------------------

  // =============================================
  // [1.4] Adding children at the right place
  // =============================================

  /**
   * Append a transition to the template as a child at the right place
   */
  public void addTransitionToTemplate(Element transition, Element template){
      template.appendChild(transition);
  }
  
  /**
   * Create a clone of a transition Element and add it in the appropriate 
   * template.<BR>
   * Returns the clone.     <BR>
   * <BR>
   * Throws exception, if attempted to clone something different.
   */
  public Element cloneTransition(Element transition)
    throws Exception {
    if( sanityChecks &&
	(!(transition.getTagName()).equals("transition")))
      throw new Exception("ERROR: not a <transition> Element: \n" +
			  transition.toString());

    Element clone = (Element)transition.cloneNode(true); // deep clone
    (transition.getParentNode()).insertBefore(clone, transition);

    if(debug)
      System.out.println("//////////////// Created clone: " + clone.toString());

    return clone;
  }


  /**
   * Add a source to a transition in the right place.
   */
  public void addSourceToTransition(Element source, Element transition){
    Node first = transition.getFirstChild();
    if(null == first)
      transition.appendChild(source);
    else
      transition.insertBefore(source, first);
  }
  /**
   * Add a target to a transition in the right place.
   */
  public void addTargetToTransition(Element target, Element transition)
    throws Exception {
    Node first = transition.getFirstChild();
    if(null == first)
      transition.appendChild(target);
    else {
      Node firstSource = getFirstChildWithLabelIfExists(transition, "source");
      if(null == firstSource){ // -- up in front ----------------
	transition.insertBefore(target, first);
      }
      else { // -- after source ---------------------------------
	Node second = firstSource.getNextSibling();
	if(null == second)
	  transition.appendChild(target);
	else 
	  transition.insertBefore(target, second);
      }
    }
  }
  
  /**
   * Add a location at the right place.<BR>
   * <BR>
   * Throws Exception, if the second argument is not a template.
   */
  public void addLocationToTemplate(Element loc, Element template)
    throws Exception {
    if(!(template.getTagName()).equals("template"))
      throw new Exception("ERROR: The argument " +
			  template.toString() +
			  "\nis not a template.");

    Element init = getFirstChildWithLabelIfExists(template, "init");
    if(null == init){
      Element transition = getFirstChildWithLabelIfExists(template, "transition");
      if(null == transition)
	template.appendChild(loc);
      else
	template.insertBefore(loc, transition);
    }
    else 
      template.insertBefore(loc, init);
    
  }
  /**
   * Add a parameter element at the right place.<BR>
   * <BR>
   * Throws Exception, if the second argument is not a template or a
   * parameter element exits.
   */
  public void addParameterToTemplate(Element par, Element template)
    throws Exception {
    if(!(template.getTagName()).equals("template"))
      throw new Exception("ERROR: The argument " +
			  template.toString() +
			  "\nis not a template.");

    if(null != getFirstChildWithLabelIfExists(template, "parameter"))
      throw new Exception("ERROR: The template already has a paramter Element:\n " +
			  template.toString());

    Element name = getFirstChildWithLabelIfExists(template, "name");
    if(null == name){ // -- insert at first place -----
      NodeList allChildren = template.getChildNodes();
      if(allChildren.getLength() == 0)
	template.appendChild(par);
      else
	template.insertBefore(par, allChildren.item(0));
    }
    else { // -- insert behind name Element -----------
      Node next = name.getNextSibling();
      template.insertBefore(par, next);
    }
  }
  
  /**
   * Add a name element at the right place.<BR>
   * <BR>
   * Throws Exception, if the second argument is not a template or a
   * name element exits.<BR>
   * <BR>
   * Returns the name Element.
   */
  public Element addNameToTemplate(Element name, Element template)
    throws Exception {
    if(!(template.getTagName()).equals("template"))
      throw new Exception("ERROR: The argument " +
			  template.toString() +
			  "\nis not a template.");

    if(null != getFirstChildWithLabelIfExists(template, "name"))
      throw new Exception("ERROR: The template already has a name Element:\n " +
			  template.toString());
    
    Node first = template.getFirstChild();
    if(null == first)
      template.appendChild(name);
    else
      template.insertBefore(name, first);

    return name;
  }
  

  /**
   * Add an (textual) invariant at the right place.<BR>
   * If an invariant already existsm conjunct it logically.
   * <BR>
   * Throws Exception, if the first argument is not a location.
   */
  public void addTextualInvariantToLocation(Element loc, String invariantText)
    throws Exception {
    if(sanityChecks &&
       !(loc.getTagName()).equals("location"))
      throw new Exception("ERROR: The argument " +
			  loc.toString() +
			  "\nis not a location.");

    Element invariant = getTheChildThatIsInvariantIfExists(loc);
    if(null == invariant){// -- find right place to add --------------------
      Element newInvariant = createInvariant();
      int x = (new Integer(loc.getAttribute("x"))).intValue() - 20;
      int y = (new Integer(loc.getAttribute("y"))).intValue() - 20;
      newInvariant.setAttribute("x", String.valueOf(x));
      newInvariant.setAttribute("y", String.valueOf(y));
      appendTextToTextualContentOfElement(newInvariant, invariantText);
      Element urgent = getFirstChildWithLabelIfExists(loc, "urgent");
      if( null != urgent)
	loc.insertBefore(newInvariant, urgent);
      else {
	Element committed = getFirstChildWithLabelIfExists(loc, "committed");
	if( null != committed)
	  loc.insertBefore(newInvariant, committed);
	else
	  loc.appendChild(newInvariant);
      }
    }
    else { 
      String oldInvariantText = getCdataOfElement(invariant);

      if((oldInvariantText.trim()).length() == 0)
	appendTextToTextualContentOfElement(invariant, invariantText);
      else
	appendTextToTextualContentOfElement(invariant, ", " + invariantText);
    }
  }

  // ==================================================
  // [1.5] Creation of Elements
  // ==================================================
  
  /**
   *  Returns a (fresh) template node and insert it in the list of known templates. <BR>
   * <BR>
   * Throws exception, if already finalized.
   */
  public Element createTemplate()
    throws Exception {
    Element result = doc.createElement("template");
    addTemplate(result);

    return result;
  }

  /**
   *  Returns a (fresh) template node and insert it in the list of known templates. <BR>
   * <BR>
   * Throws exception, if already finalized.
   */
  public Element createTransitionInTemplate(Element template)
    throws Exception {
    if(sanityChecks && 
       (!(template.getTagName()).equals("template")))
      throw new Exception("ERROR: not a <template> Element: \n" +
			  template.toString());

    Element result = doc.createElement("transition");
    addTransitionToTemplate(result, template);

    return result;
  }
  

  /**
   * Create and append a child Element to a transition.<BR>
   * Return this Element
   */
  protected Element createTransitionChildInheritingCoordinates(Element transition, String childType)
    throws Exception {

    Element res = doc.createElement(childType);
    String x = transition.getAttribute("x");
    String y = transition.getAttribute("y");
    if( (x.length() >0 ) &&
	(y.length() >0 )){
      res.setAttribute("x", x);
      res.setAttribute("y", y);
    }
    else
      addCoordinatesToElement(res);

    transition.appendChild(res);

    return res;
  }
  /**
   * Create and append an assignment Element to a transition.<BR>
   * Return this Element
   */
  protected Element createAssignmentInheritingCoordinates(Element transition)
    throws Exception {

    Element res = createAssignment();
    String x = transition.getAttribute("x");
    String y = transition.getAttribute("y");
    if( (x.length() >0 ) &&
	(y.length() >0 )){
      res.setAttribute("x", x);
      res.setAttribute("y", y);
    }
    else
      addCoordinatesToElement(res);
    
    transition.appendChild(res);
    
    return res;
  }
  /**
   * Create and append an guard Element to a transition.<BR>
   * Return this Element
   */
  protected Element createGuardInheritingCoordinates(Element transition)
    throws Exception {

    Element res = createGuard();
    String x = transition.getAttribute("x");
    String y = transition.getAttribute("y");
    if( (x.length() >0 ) &&
	(y.length() >0 )){
      res.setAttribute("x", x);
      res.setAttribute("y", y);
    }
    else
      addCoordinatesToElement(res);
    
    transition.appendChild(res);
    
    return res;
  }
  /**
   * Create and append an synchronisation Element to a transition.<BR>
   * Return this Element
   */
  protected Element createSynchronisationInheritingCoordinates(Element transition)
    throws Exception {

    Element res = createSynchronisation();
    String x = transition.getAttribute("x");
    String y = transition.getAttribute("y");
    if( (x.length() >0 ) &&
	(y.length() >0 )){
      res.setAttribute("x", x);
      res.setAttribute("y", y);
    }
    else
      addCoordinatesToElement(res);
    
    transition.appendChild(res);
    
    return res;
  }
  
  /**
   * Creates an assignment Element.<BR>
   * <BR>
   * From <TT>uppaal-1.4.dtd</TT> on, this is a &lt;label&gt; with 
   * attribute<BR>
   * <BR>
   * <TT>kind="assignment"</TT><BR>
   * <BR>
   * <B>actual choice depends on <TT>{@link #useLabelConstruct}</TT></B>.
   */
  protected Element createAssignment(){
    
    Element result;
    if(useLabelConstruct){
      result = doc.createElement("label");
      result.setAttribute("kind", "assignment");
    }
    else { // OLD: direct 
      result = doc.createElement("assignment");
    }
    
    return result;
  }
  /**
   * Creates a guard Element.<BR>
   * <BR>
   * From <TT>uppaal-1.4.dtd</TT> on, this is a &lt;label&gt; with 
   * attribute<BR>
   * <BR>
   * <TT>kind="guard"</TT><BR>
   * <BR>
   * <B>actual choice depends on <TT>{@link #useLabelConstruct}</TT></B>.
   */
  protected Element createGuard(){
    
    Element result;
    if(useLabelConstruct){
      result = doc.createElement("label");
      result.setAttribute("kind", "guard");
    }
    else { // OLD: direct 
      result = doc.createElement("guard");
    }
    
    return result;
  }
  /**
   * Creates a synchronisation Element.<BR>
   * <BR>
   * From <TT>uppaal-1.4.dtd</TT> on, this is a &lt;label&gt; with 
   * attribute<BR>
   * <BR>
   * <TT>kind="synchronisation"</TT><BR>
   * <BR>
   * <B>actual choice depends on <TT>{@link #useLabelConstruct}</TT></B>.
   */
  protected Element createSynchronisation(){
    
    Element result;
    if(useLabelConstruct){
      result = doc.createElement("label");
      result.setAttribute("kind", "synchronisation");
    }
    else { // OLD: direct 
      result = doc.createElement("synchronisation");
    }
    
    return result;
  }
  /**
   * Creates a invariant Element.<BR>
   * <BR>
   * From some later uppaal versions on, this might be a label with
   * attribute<BR>
   * <BR>
   * <TT>kind="invariant"</TT><BR>
   * <BR>
   * <B>!LABEL!, see <TT>{@link #useLabelForInvariants}</TT></B>.
   */
  protected Element createInvariant(){
    
    Element result;
    if(useLabelForInvariants){
      result = doc.createElement("label");
      result.setAttribute("kind", "invariant");
    }
    else { // OLD: direct 
      result = doc.createElement("invariant");
    }
    
    return result;
  }
  

  /**
   * Create a new declaration element<BR>
   * If the argument is empty, the element will be empty
   * <H3>!! This might be more sophisticated at some point</H3>
   */       
  protected Element createDeclaration(String declarationText){
    Element declaration = doc.createElement("declaration");
    if( declarationText.length() > 0)
      declaration.appendChild(doc.createTextNode(declarationText.toString()));
    
    return declaration;
  }
  /**
   * Create a new parameter element<BR>
   * The argument is a Vector of <EM>Strings</EM> -
   * <H3>!! This might be more sophisticated at some point</H3>
   */       
  protected Element createParameter(Vector parameters){
    Element res = doc.createElement("parameter");
    if(inventNewGoegraphicalLocations){
      addCoordinatesToElement(res,600,0);
    }
    else {
      addCoordinatesToElement(res);
    }
    StringBuffer parameterString = new StringBuffer();
    for(Enumeration e = parameters.elements(); e.hasMoreElements();){
      parameterString.append((String)e.nextElement());
      if(e.hasMoreElements())
	parameterString.append(", ");
    }
    if( parameterString.length() > 0){
      addTextualContentToElement(res, parameterString.toString());
    }
    
    return res;
  }

  /**
   * Create an element particular to the flat hierarchical document
   */
  protected Element createElement(String type)
    throws Exception {
    return doc.createElement(type);
  }

  
  /**
   * Returns a Element that is a name tag with specified content<BR>
   * <H3>!! Could contain optional argument for placement at some point 
   * !!</H3>
   */     
  protected Element createNameElement(String name){
    Element res = doc.createElement("name");
    addCoordinatesToElement(res);
    addTextualContentToElement(res, makeNameSafe(name));
     
    return res;
  }
  /**
   * Returns a basic transition, given source and target id<BR>
   * <H3>!! Might be augmented with coordinates/nails</H3>
   */
  protected Element createTransition(String sourceID, String targetID){
    Element res =  doc.createElement("transition");
    addCoordinatesToElement(res);
    res.appendChild(createSource(sourceID));
    res.appendChild(createTarget(targetID));
    
    if(inventNewGoegraphicalLocations){
      try {
      long middleX = 
	(Long.parseLong(((Element)hashIDsToNewLocationElements.get(sourceID)).getAttribute("x")) +
	 Long.parseLong(((Element)hashIDsToNewLocationElements.get(targetID)).getAttribute("x")) ) / 2L;
      long middleY = 
	(Long.parseLong(((Element)hashIDsToNewLocationElements.get(sourceID)).getAttribute("y")) +
	 Long.parseLong(((Element)hashIDsToNewLocationElements.get(targetID)).getAttribute("y")) ) / 2L;
      res.setAttribute("x", "" + middleX);
      res.setAttribute("y", "" + middleY);

      }
      catch(Exception e) { 
	if(debug)
	  System.out.println("!!! Failed computation of transistion-middle ignored.");
      }
    }

    return res;
  }
  /**
   * Creates a (flat document) source Element and sets the attribute 
   * <TT>ref</TT>.<BR><BR>
   * The attriubte <TT>exitref</TT> remains unset.
   */
  protected Element createSource(String locID){
    Element res = doc.createElement("source");
    res.setAttribute("ref", locID);
    
    return res;
  }
  /**
   * Creates a (flat document) target Element and sets the attribute 
   * <TT>ref</TT>.<BR><BR>
   * The attriubte <TT>exitref</TT> remains unset.
   */
  protected Element createTarget(String locID){
    Element res = doc.createElement("target");
    res.setAttribute("ref", locID);
    
    return res;
  }
  /**
   * Creates a (flat document) target Element and sets the attribute 
   * <TT>ref</TT>.<BR><BR>
   * The attriubte <TT>exitref</TT> remains unset.
   */
  protected Element createTarget(Element locationOrComponent)
    throws Exception {
    if(sanityChecks && 
       (!(locationOrComponent.getTagName()).equals("location")) &&
       (!(locationOrComponent.getTagName()).equals("component")) )
       throw new Exception("ERROR: not a <location> or <component> Element: \n" +
			   locationOrComponent.toString());

    return createTarget(getElementID(locationOrComponent));
  }
  
  
  /**
   * Create a new unique location with a specified name
   * <H3>!! could contain coordinate information at some point !!</H3>
   *    <I>(Exception is never really thrown)</I>
   */
  protected Element inventLocation(String name)
    throws Exception {
    Element res = doc.createElement("location");

    if(inventNewGoegraphicalLocations)
      addDefaultLocationCoordinates(res);
    else
      addCoordinatesToElement(res);

    addIDToElement(res);
    addNameToElement(res, makeNameSafe(name));

    hashIDsToNewLocationElements.put(DocumentReader.getElementID(res), res);

    return res;
  }
  /**
   * Create a new unique location without caring about the name
   *    <I>(Exception is never really thrown)</I>
   */       
  protected Element inventLocation()
    throws Exception {
    return inventLocation(freshLocationName());
  }

  /**
   * Invent a new location and append it to the specified (flat) template.
   */
  public Element inventLocationInTemplate(Element template)
    throws Exception {
    Element result = inventLocation();
    addLocationToTemplate(result, template);
    
    return result;
  }
  /**
   * Invent a new named location and append it to the specified (flat) template.
   */
  public Element inventLocationInTemplate(String name, Element template)
    throws Exception {
    Element result = inventLocation(name);
    addLocationToTemplate(result, template);
    
    return result;
  }
  

  /**
   * Changes the id-attribute of the location, but also updates the 
   * hashtable <TT>{@link #hashIDsToNewLocationElements}</TT>
   */
  protected void changeLocationID(Element el, String newID)
    throws Exception {
    if( sanityChecks &&
	(!(el.getTagName()).equals("location")))
      throw new Exception("ERROR: argument Element not a location: \n" +
			  el.toString());

    if(debug)
      System.out.print("!!! overwriting ID - was: " + el.getAttribute("id"));


    el.setAttribute("id", newID);
    hashIDsToNewLocationElements.put(newID, el);

    if(debug)
      System.out.println(" --> NEW: " + newID);

  }
  
    

  /**
   * Output the (current) flat document to a given file.<BR>
   * Old file contents, if any, are destroyed.
   */
  public void writeToFile(String fileName)
    throws java.io.IOException {
    
    FileWriter fw = new FileWriter(fileName);
    doc.write(fw);
  }
     
  // =============================================
  // Checking Properties
  // =============================================

  /**
   * Test, whether this is an assignment Element.<BR>
   * <BR>
   * Depends on {@link #useLabelConstruct}.
   */
  public static boolean isAssignment(Element el){

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
  public static boolean isGuard(Element el){

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
  public static boolean isSynchronisation(Element el){

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
   * Depends on {@link #useLabelForInvariants}.
   */
  public static boolean isInvariant(Element el){

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
  


  // ======================================================================
  // [2] SPECIAL TEMPLATES
  // ======================================================================
  
  /**
   * Create a dummy automaton trying constantly to synchronize on an 
   * urgent channel
   */
  public void createHurryDummy()
    throws Exception {
    
    resetDefaultLayout();

    String instantiationName = "_global_Hurry_Dummy";
    String channelName = "Hurry"; 
    String locationName = "hurry_Location";
    
    Element hurryTemplate = doc.createElement("template");
    Element hurryParameter = createParameter(new Vector());
    Element hurryLocation = inventLocation(locationName);
    Element hurryTransition = createTransition(DocumentReader.getElementID(hurryLocation),
					       DocumentReader.getElementID(hurryLocation));
    
    addSendSynchronisationToTransition(channelName, hurryTransition);
    
    TextualInstantiation inst = 
      new TextualInstantiation(instantiationName,
			       new Vector(), null, null, null, null );
    
    addNameToElement(hurryTemplate, inst.objectName );
    hurryTemplate.appendChild(hurryParameter);
    hurryTemplate.appendChild(hurryLocation);
    hurryTemplate.appendChild(hurryTransition);

    makeLocationInitial(hurryLocation);
    
    templates.addElement(hurryTemplate);
    addInstantiation(inst);
    addDeclaration("urgent chan  " + channelName);
  }

  /**
   * Create the kickoff-templates, that starts the global process.
   * <BR>
   * The parameter is a vector of strings, i.e., the signals to 
   * enter the global components.
   * <BR>
   * 
   * Returns the name of the instantiation
   */
  public Element createKickTemplate(Vector rootEntrySignals)
    throws Exception {
    
    resetDefaultLayout();

    if(debug)
      System.out.println("** KickTemplate: " + rootEntrySignals.size() +
			 " global component(s).");

    String instantiationName = makeNameSafe("X-global_Kickoff");
    
    kickOffTemplate = doc.createElement("template");
    Element kickParameter = createParameter(new Vector());
    Element lastLocation = inventLocation("X-start");
    Element nextLocation;
    addParameterToTemplate(kickParameter, kickOffTemplate);
    addLocationToTemplate(lastLocation, kickOffTemplate);
    makeLocationInitial(lastLocation);

    for(Enumeration e=rootEntrySignals.elements(); e.hasMoreElements(); ){
      String activator = (String)e.nextElement();
      nextLocation = inventLocation();
      Element kickTransition = createTransition(getElementID(lastLocation),
						getElementID(nextLocation));
      
      addLocationToTemplate(nextLocation, kickOffTemplate);
      addTransitionToTemplate(kickTransition, kickOffTemplate);
      addSendSynchronisationToTransition(activator, kickTransition);
    
      makeLocationCommitted(lastLocation);

      lastLocation = nextLocation;
    }
    
    // -- Enter in lists to be processed finally ----------------------- 
    
    TextualInstantiation inst = 
      new TextualInstantiation(instantiationName,
			       new Vector(), null, null, null, null );
    
    
    addNameToElement(kickOffTemplate, inst.objectName );
    
    templates.addElement(kickOffTemplate); 
    addInstantiation(inst);

    kickOffDoneID = getElementID(lastLocation);
    
    return kickOffTemplate;
  }
  
  // =================================================================
  // [3] Service Functions (dependent on flat dtd conventions)
  // =================================================================

  public void addInformationAboutTranslation(InstantiationTree tree){
    Node comment = doc.createComment("\n        Tree of Instantiations:\n" + tree.showWithOffset(2));
    root.insertBefore(comment, declaration);
  }

  /**
   * Insert a comment line at the end of every template.<BR>
   * <BR>
   * (To be called <EM>after</EM> completion).
   */
  private void generateEndTemplateSemaphores()
    throws Exception {
    
    NodeList allTemplates = getAllChildrenWithLabel(root, "template");

    if(debug)
      System.out.println("... showing " + allTemplates.getLength() + " Templates.");

    for(int i=0; i < allTemplates.getLength(); i++){
      Element template = (Element)allTemplates.item(i);
      String templateName = getElementName(template);

      root.insertBefore(doc.createComment(" ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ "),
			template);
      Node next = template.getNextSibling();

      String endText = " ****** END OF TEMPLATE \"" + templateName + "\"  ";

      if(null == next){
	root.appendChild(doc.createComment(endText));
      }
      else {
	root.insertBefore(doc.createComment(endText),
			  next);
      }
    }
  }

  /**
   * Returns (texualy) a logical <EM>and</EM> of two invariant texts.<BR>
   * (does not manipulate the texts themselves)
   */
  public static String textuallyConjunctInvariants(String inv1, String inv2){
    String result = inv1.trim();
    if(result.length() == 0)
      return inv2;
    if((inv2.trim()).length() == 0)
      return inv1;
    
    return result + ", " + inv2.trim();
  }
  

  /**
   * Browse through all templates (the ones already in the documnent),<BR>
   * and call the Layouter method on them
   */
  public void recomputeAllTemplateLayouts()
    throws Exception {
    
    NodeList allTemplates = getAllChildrenWithLabel(root, "template");
    for(int i=0; i < allTemplates.getLength(); i++){
      Element template = (Element)allTemplates.item(i);

      if(debug)
	System.out.println("... layouting: " + getElementName(template));

      layouter.layoutTemplate(template);
    }
  }
  /**
   * Browse through all transitions (the ones already in the document),<BR>
   * and 
   * <UL>
   *  <LI>remove empty labels</LI>
   *  <LI>sort labels in guard-sync-assignment</LI>
   * </UL>
   */
  public void cleanupTransitionLabels()
    throws Exception {
    
    if(debug)
      System.out.println(">>> CLEANING UP TRANSITION LABELS");

    NodeList allTemplates = getAllChildrenWithLabel(root, "template");
    for(int i=0; i < allTemplates.getLength(); i++){
      Element template = (Element)allTemplates.item(i);

      if(debug)
	System.out.println("... cleaning: " + getElementName(template));

      NodeList allTransitions = getAllChildrenWithLabel(template, "transition");
      for(int j=0; j < allTransitions.getLength(); j++){
	Element transition = (Element)allTransitions.item(j);
	
	NodeList allLabels = getAllChildrenWithLabel(transition, "label");
	Element guardLabel = null;
	Element synchronisationLabel = null;
	Element assignmentLabel = null;
	for(int k=0; k < allLabels.getLength(); k++){
	  Element label = (Element)allLabels.item(k);
	  String kind = (label.getAttribute("kind")).trim();
	  if(kind.equals("guard")){
	    if(null != guardLabel)
	      throw new Exception("ERROR: double guards \n" + guardLabel.toString() + "\n" + label.toString());
	    if((getCdataOfElement(label).trim()).length() >0)
	      guardLabel = label;
	  }
	  else if(kind.equals("synchronisation")){
	    if(null != synchronisationLabel)
	      throw new Exception("ERROR: double synchronisations \n" + synchronisationLabel.toString() + "\n" + label.toString());
	    if((getCdataOfElement(label).trim()).length() >0)
	      synchronisationLabel = label;
	  }
	  else if(kind.equals("assignment")){
	    if(null != assignmentLabel)
	      throw new Exception("ERROR: double assignments \n" + assignmentLabel.toString() + "\n" + label.toString());
	    if((getCdataOfElement(label).trim()).length() >0)
	      assignmentLabel = label;
	  }
	  else
	    throw new Exception("ERROR: unexpected label: \n" + label.toString());
	  transition.removeChild(label);
	}
	if(null != guardLabel)
	  transition.appendChild(guardLabel);
	if(null != synchronisationLabel)
	  transition.appendChild(synchronisationLabel);
	if(null != assignmentLabel)
	  transition.appendChild(assignmentLabel);
      }
    }
  }

  // =================================================================
  // [4] RETRIEVE ELEMENTS
  // =================================================================
  
  /**
   * Returns a <TT>Vector</TT>, where all the (immediate) transitions to a 
   * specific location are stored.<BR>
   * <BR>
   * <B>Should be called only after all transitions are created!</B>
   */
  public Vector getAllTransitionsLeadingToLocation(Element loc)
    throws Exception {
     if(sanityChecks && 
       (!(loc.getTagName()).equals("location")))
       throw new Exception("ERROR: not a <location> Element: \n" +
			   loc.toString());

     Vector result = new Vector();

     String locID = getElementID(loc);
     Element template = (Element)loc.getParentNode();
     
     NodeList allTransitions = getAllChildrenWithLabel(template, 
						       "transition");
     for(int i=0; i < allTransitions.getLength(); i++){
       Element transition = (Element)allTransitions.item(i);
       Element target     = getTheChildWithLabel(transition, "target");
       if(locID.equals(target.getAttribute("ref")))
	 result.addElement(transition);
     }
     
     return result;
  }

  /**
   * Returns a <TT>Vector</TT>, where all the (immediate) transitions <B>from</B> a 
   * specific location are stored.<BR>
   * <BR>
   * <B>Should be called only after all transitions are created!</B>
   */
  public Vector getAllTransitionsStartingAtLocation(Element loc)
    throws Exception {
     if(sanityChecks && 
       (!(loc.getTagName()).equals("location")))
       throw new Exception("ERROR: not a <location> Element: \n" +
			   loc.toString());

     Vector result = new Vector();

     String locID = getElementID(loc);
     Element template = (Element)loc.getParentNode();
     
     NodeList allTransitions = getAllChildrenWithLabel(template, 
						       "transition");
     for(int i=0; i < allTransitions.getLength(); i++){
       Element transition = (Element)allTransitions.item(i);
       Element source     = getTheChildWithLabel(transition, "source");
       if(locID.equals(source.getAttribute("ref")))
	 result.addElement(transition);
     }
     
     return result;
  }
  // -----------------------------------------------------------------------

  /**
   * Return the child label Element, that is an assignment. If it does not 
   * exist,
   * then return <TT>null</TT>.<BR>
   * <BR>
   * If not called on a transition, or it has <EM>more</EM> than one label 
   * of kind assignment, then an exception is thrown.
   */
  public static Element getTheChildThatIsAssignmentIfExists(Element transition)
    throws Exception {
    if(sanityChecks && 
       (!(transition.getTagName()).equals("transition")))
       throw new Exception("ERROR: not a <transition> Element: \n" +
			   transition.toString());

    Element result = null;
    NodeList allChildren = transition.getElementsByTagName("*");
    for(int i=0; i < allChildren.getLength(); i++){
      Node node = (Node)allChildren.item(i);
      if( (node instanceof Element) &&
	  isAssignment((Element)node)){
	if( null == result )
	  result = (Element)node;
	else
	  throw new Exception("ERROR: the transition has more than one assignment label:\n" + 
			      transition.toString());
      }
    }
    return result;
  }
  /**
   * Return the child label Element, that is an guard. If it does not 
   * exist,
   * then return <TT>null</TT>.<BR>
   * <BR>
   * If not called on a transition, or it has <EM>more</EM> than one label 
   * of kind guard, then an exception is thrown.
   */
  public static Element getTheChildThatIsGuardIfExists(Element transition)
    throws Exception {
    if(sanityChecks && 
       (!(transition.getTagName()).equals("transition")))
       throw new Exception("ERROR: not a <transition> Element: \n" +
			   transition.toString());

    Element result = null;
    NodeList allChildren = transition.getElementsByTagName("*");
    for(int i=0; i < allChildren.getLength(); i++){
      Node node = (Node)allChildren.item(i);
      if( (node instanceof Element) &&
	  isGuard((Element)node)){
	if( null == result )
	  result = (Element)node;
	else
	  throw new Exception("ERROR: the transition has more than one guard label:\n" + 
			      transition.toString());
      }
    }
    return result;
  }
  /**
   * Return the child label Element, that is an synchronisation. If it does not 
   * exist,
   * then return <TT>null</TT>.<BR>
   * <BR>
   * If not called on a transition, or it has <EM>more</EM> than one label 
   * of kind synchronisation, then an exception is thrown.
   */									    
  public static Element getTheChildThatIsSynchronisationIfExists(Element transition)
    throws Exception {
    if(sanityChecks && 
       (!(transition.getTagName()).equals("transition")))
       throw new Exception("ERROR: not a <transition> Element: \n" +
			   transition.toString());

    Element result = null;
    NodeList allChildren = transition.getElementsByTagName("*");
    for(int i=0; i < allChildren.getLength(); i++){
      Node node = (Node)allChildren.item(i);
      if( (node instanceof Element) &&
	  isSynchronisation((Element)node)){
	if( null == result )
	  result = (Element)node;
	else
	  throw new Exception("ERROR: the transition has more than one synchronisation label:\n" + 
			      transition.toString());
      }
    }
    return result;
  }
  
  /**
   * Return the child label Element, that is an invariant. If it does not 
   * exist,
   * then return <TT>null</TT>.<BR>
   * <BR>
   * If not called on a location, or it has <EM>more</EM> than one label 
   * of kind invariant, then an exception is thrown.
   */
  public static Element getTheChildThatIsInvariantIfExists(Element location)
    throws Exception {
    if(sanityChecks && 
       (!(location.getTagName()).equals("location")))
       throw new Exception("ERROR: not a <location> Element: \n" +
			   location.toString());

    Element result = null;
    NodeList allChildren = location.getElementsByTagName("*");
    for(int i=0; i < allChildren.getLength(); i++){
      Node node = (Node)allChildren.item(i);
      if( (node instanceof Element) &&
	  isInvariant((Element)node)){
	if( null == result )
	  result = (Element)node;
	else
	  throw new Exception("ERROR: the location has more than one invariant label:\n" + 
			      location.toString());
      }
    }
    return result;
  }
  

}
