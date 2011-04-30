// -*- mode: JDE; c-basic-offset: 2; -*-
// /////////////////////////////////////////////////////////////
// 
// 
// Synopsis:
//  Huppaal -- used in translation to flat structure
//  (huppaal.?.dtd  -> uppaal.dtd)
// /////////////////////////////////////////////////////////////
// @FILE:    TextualInstantiation.java
// @PLACE:   BRICS AArhus; host:newton
// @FORMAT:  java
// @AUTHOR:  M. Oliver M"oller     <omoeller@brics.dk>
// @BEGUN:   Thu Oct 26 16:32:17 2000
// @VERSION: Vanilla-1                  Fri Mar 23 11:37:31 2001
// /////////////////////////////////////////////////////////////
// 

import java.lang.*;
import java.util.Vector;
import java.util.Enumeration;


import org.w3c.dom.Element;

//**** from other packages 
 
//****************************************

/**
 * Stores information about textual instantiations.
 * <H2>Missing</H2>
 * Probably will have to store information about parameters at some point
 *
 * @see Flatten
 * @author <A HREF="MAILTO:omoeller@brics.dk?subject=TextualInstantiation.java%20(Vanilla-1%20Mon%20Feb%2019%2015:26:37%202001)">M. Oliver M&ouml;ller</A>
 * @VERSION Vanilla-1                  Fri Mar 23 11:37:31 2001
 */
public class TextualInstantiation  {

  // ////////////////////////////////////////
  // ////////////// FIELDS //////////////////
  // ////////////////////////////////////////
  
  /**
   * Counter to provide unique object names
   *
   */
  static long objectCounter = 0L;
   
  /**
   * The name of the incarnated object
   */
  public String objectName = null;

  /**
   * Object name in the original hierarchical version<BR>
   * <EM>Only relevant for global elements</EM>
   */
  public String originalInstantiationName = null;

  /**
   * List of parameters
   * <H3>!! in Vanilla-1 always empty !!</H3>
   */
  public Vector parameters;
  
  /**
   * The (hierarchical old) Element node this object instantiates
   */
  public Element templateElement;

  /**
   * The (hierarchical old) component, where this object instanted
   */
  public Element componentElement;
  

  /**
   * The channel the translation of this template depends on.<BR>
   * The translation of this part of the system remains in a special state 
   * <TT>_idle_(number)</TT>, until the signal <TT>[activator]!</TT> is 
   * sent.
   *
   * <H3><B>!! OUTDATED !!</B></H3>
   */
  public String activator;


  /**
   * The signal that is issued, if the component becomes idle.<BR>
   * This is <B>NOT</B> specific to any particular exit, but only to
   * the TextualInstantiation itself.<BR>
   * <BR>
   */
  public String exitSignal;

  /**
   * Set to <TT>true</TT>, if the exit point of this instantiaion are 
   * connected to a 
   * <EM>join</EM><BR>
   * Note that an <EM>arbitrary</EM> amount of global joins can be related 
   * to this particular instantiation.
   * Default is <TT>false</TT><BR>
   */
  public boolean isANDComponent = false;

  
  /**
   * Pointer to the (flat) template Element this textual instantiation 
   * was translated to.<BR><BR>
   * Used for the global joins, i.e. to augment this transition with the 
   * encoding of the join.<BR>
   */
  public Element translationOfTemplateElement;
  
  /**
   * Pointer to the direct father.
   * Is null, if this textual instantiation is global.
   */
  public TextualInstantiation father;
  
  
  /**
   * Contains (texutally conjunct) the invariants inherited by
   *    ancestors; they are assumed to be parameter-corrected allready.<BR>
   * Is initialized on call of {@link #setup}.<BR>
   * <BR>
   * <B>ASSUMPTION:</B> ALL ANCHESTORS ARE SETUP <EM>BEFORE</EM> THEIR 
   * DESCENDANDTS.
   */
  public String inheritedInvariant = null;

  // -- AUX ----------------------------------------------------------------
  
  
  /**
   * Spam out debuggin information, if <TT>debug</TT> is true
   */
  static boolean debug = true;
 
  // -----------------------------------------------------------------------
  /**
   * Auxillary Variable, to make exit signals unique.
   */
  private static int exitSignalCounter = 0;

  // ////////////////////////////////////////
  // //////////  CONSTRUCTORS  //////////////
  // ////////////////////////////////////////
  
  /**
   * Default Constructor
   * <UL>
   *  <LI><TT>objectPrefix</TT> gives a hint, in how to name the object 
   * such that the origin is derivable</LI>
   *  <LI>The vector gives the list of parameters</LI>
   *  <LI><TT>fatherPrefix</TT> is the textual prefix of the father 
   * instantiation<BR> it is used for unique naming</LI>   
   * <LI><TT>templateElement</TT> is the node in the hierarchical document 
   * that is instanciated<BR>
   * (for the kicker, this is <TT>null</TT>)</LI>
   * </UL>
   * <B>NOTE:</B>Equivalently, you can call the empty constructor and
   * later call <TT>{@link #setup}</TT> with the same arguments.
   */
  public TextualInstantiation(String objectPrefix, 
			      Vector v,
			      TextualInstantiation fatherPointer,
			      Element tplElement,
			      Element theComponent,
			      Element theTranslation)
    throws Exception {
  
    originalInstantiationName = objectPrefix;

    inventExitSignal();
    setup(objectPrefix, v, fatherPointer, tplElement, theComponent, theTranslation);
  }
  
  /**
   * Empty Constructor; use <TT>{@link #setup}</TT> to fill with meaning.
   */
  public TextualInstantiation(){
    inventExitSignal();
  }

  // ////////////////////////////////////////
  // ////////////// METHODS  ////////////////
  // ////////////////////////////////////////


  /**
   * Is assumed to be called exactly once.
   */
  public void setup(String objectPrefix, 
		    Vector v,
		    TextualInstantiation fatherPointer,
		    Element tplElement,
		    Element theComponent,
		    Element theTranslation)
    throws Exception {

    if(objectName != null)
      throw new Exception("ERROR - setup of " + objectName + 
			  " called more than once!");
    

    String fatherPrefix = "";
    if (fatherPointer != null)
      fatherPrefix = fatherPointer.objectName;
    
    objectName = DocumentWriter.makeNameSafe(fatherPrefix + "X-" + getFreshObjectName(objectPrefix));
    parameters = v;
    templateElement = tplElement;
    componentElement = theComponent;

    father = fatherPointer;
    
    if ( (null != parameters ) &&
	 ( parameters.size() != 0)  )
      throw new Exception("ERROR: in Vanilla-1, templates cannot have parameters.");
    // -- invariants -------------------------------------------------------
    
    if(fatherPointer == null)
      inheritedInvariant = "";
    else 
      inheritedInvariant = fatherPointer.inheritedInvariant;
    
    if( null != componentElement ){ // -- real NON-Overhead template --------
      
      Element invariant = HierarchicalDocumentReader.getTheChildWithLabelIfExists(componentElement, "invariant");
      if( null != invariant){
	String invariantText = DocumentReader.getCdataOfElement(invariant).trim();
	// !!! now adjust according to parameters !!
	inheritedInvariant = FlatDocumentWriter.textuallyConjunctInvariants(inheritedInvariant, invariantText);

	if(debug)
	  System.out.println("/////////// new invariant at " + this.toString() +
			     " :\n\t" + inheritedInvariant);

      }
    }

    // ---------------------------------------------------------------------
    translationOfTemplateElement = theTranslation;
  }

  
  /**
   * Set the field <TT> {@link #activator}</TT>
   */
  public void setActivator(String act){
    activator = act;
  }
  
  /**
   * Returns the instantiation text <BR><BR>
   * 
   *    <TT>objectName := templateName(parameters)</TT>
   */
  public String toString(){
    if(null == objectName)
      return "**unspecified**";
    else {
      StringBuffer res = new StringBuffer(objectName + " := " + objectName );
      if(null != parameters){
	res.append("(");
	for(Enumeration e = parameters.elements(); e.hasMoreElements(); ){
	  res.append((String)e.nextElement());
	  if(e.hasMoreElements())
	    res.append(", ");
	}
	res.append(")");
      }
      return res.toString();
    }
  }
    

  // ========================================
  // Auxillary
  // ========================================  

  /**
   * uniqueName, due to containment of non-user string "--"
   */
  private static String getFreshObjectName(String objectPrefix){
    objectCounter++;
    return DocumentWriter.makeNameSafe(objectPrefix + "--" + objectCounter);
  }

  /**
   * Set the exit signal.<BR>
   * (Unfortunately, this cannot be dependent on the NAME of the textual
   * instantiation, for it has to be present, before the setup is called).  
   *     
   */
  private void inventExitSignal(){
    exitSignalCounter++;
    exitSignal = FlatDocumentWriter.makeSignalSafe(("exitSignal_NR_" + exitSignalCounter));
  }

}
