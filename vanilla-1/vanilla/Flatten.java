// -*- mode: JDE;  c-basic-offset: 2; -*-
// /////////////////////////////////////////////////////////////
// Flatten hierarchical Uppaal definitions
//
// !!: Things to look at.
// !!!: Things to work on.
// !t!: Things that are tricky/likely to be buggy
// 
// Synopsis:
//  Huppaal
// /////////////////////////////////////////////////////////////
// @TABLE OF CONTENTS:		       [TOCD: 14:12 09 Apr 2001]
//
//      [0.1] Global Flags influencing the translation
//          [0.1.1] Lists
//          [0.1.2] Strings
//          [0.1.3] AUX fields for names
//      [0.2] Other AUX    
//  [1] MAIN METHOD (STATIC)
//      [1.1] Now translate it
//          [1.1.1] Hashtables
//          [1.1.2] Lists & String Init
//      [1.2] Create elements      
//          [1.2.1] Declaration (of global variables)
//          [1.2.2] Retrieve the used Templates
//      [1.3] Phase I: Process the necessary instantiations          
//      [1.4] PHASE II
//      [1.5] PHASE III
//      [1.6] Run Layouter (postprocess)
//      [1.7] Add Information about Translation
//      [1.8] IN THIS CASE: COLLECT ALL GUARDS/ASSIGNMENTS
//      [1.9] NOW: Insert Transitions 
//      [1.10] IN THIS CASE: Compute Global Join
//      [1.11] IN THIS CASE: COLLECT ALL GUARDS/ASSIGNMENTS
//      [1.12] NOW: Insert Transitions (leftover from entries)
//  [2] Deal with global Joins
//      [2.1] Transforming hierarchical Elements
//          [2.1.1] MAPs that require some Hashtables
//  [3] Entry and Exit Locations (plus Joins)
//          [3.0.1] AUX for list handling
//      [3.1] Helpers: Document traversal
// ==========================================================
// @FILE:    Flatten.java
// @PLACE:   BRICS AArhus; host:harald
// @FORMAT:  java
// @AUTHOR:  M. Oliver M'o'ller     <omoeller@brics.dk>
// @BEGUN:   Thu Oct 19 10:43:08 2000
// @VERSION: Vanilla-1                  Mon Apr  9 14:28:09 2001
// /////////////////////////////////////////////////////////////
// 


import TextualInstantiation;
import InstantiationTree;
import DocumentReader;
import HierarchicalDocumentReader;
import GlobalJoin;

import java.io.*;

import org.xml.sax.*;

import javax.xml.parsers.SAXParserFactory;  
import javax.xml.parsers.ParserConfigurationException;  
import javax.xml.parsers.SAXParser;  

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Text;
//import org.w3c.dom.CDATASection;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.AttributesImpl;
//import org.xml.sax.helpers.ParserFactory;

import org.xml.sax.InputSource;

import java.util.Vector;
import java.util.Stack;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Enumeration;


//**** from internal API packages 
import org.apache.crimson.tree.XmlDocument;
//****************************************

/**
 * <H3>Flattening &nbsp;<TT>Huppaal</TT>&nbsp; Definitions</H3> 
 * This is a prototype for flattening a hierarchical timed automata 
 * definition
 * (according to DOM <TT>{@link 
 * HierarchicalDocumentReader#huppaalDTD}</TT>)<BR>
 * The optained description  has 
 * the same 
 * behaviour 
 * (modulo some construction slack) as the hierarchical one.<BR>
 * 
 * <H2>For a detailled description see <TT>hu.tex</TT></H2>
 * 
 * <H4>Jargon</H4>
 * <UL>
 *   <LI><B>slack</B>: overhead introduced by the translation</LI>
 *   <LI><B>create</B>: do what you are told</LI>
 *   <LI><B>invent</B>: create and <EM>add</EM> some unique value</LI>
 *   <LI><B>copy</B>:   mirror without changes</LI>
 *   <LI><B>transform</B>:   mirror <EM>with</EM> changes</LI>
 *   <LI><B>migrate</B>:   mirror <EM>with</EM> changes and append to some 
 *       argument</LI>
 *   <LI><B>map</B>:   transformation of Strings</LI>
 * </UL>
 * 
 * <H4>To be sorted out</H4>
 * <UL>
 *   <LI>translation of TCTL formulas?</LI>
 *   <LI>relationship between original and newly introduced names</LI>
 *   <LI>modularity of templates (currently: one new flat template per 
 * instantiation of a hierarchical one)</LI>
 *   <LI>translation of <B>traces</B> back and forth</LI>
 * </UL>
 * 
 * <H3>Known Problems</H3>
 * <UL>
 *  <LI>all variables are global (the translations of global joins works 
 * like this at the moment</LI>
 *  <LI>name clashes are not detected</LI>
 *  <LI>order of assignments in global joins might be strange</LI>
 *  <LI>geometrical Layout has to be fixed</LI>
 * </UL>
 * 
 * @see TextualInstantiation GlobalJoin
 * @author <A HREF="MAILTO:omoeller@brics.dk?subject=Flatten.java%20(Vanilla-1%20Mon%20Apr%209%2014:24:26%202001)">M. Oliver M&ouml;ller</A>
 * @version Vanilla-1                  Mon Apr  9 14:28:09 2001
 */
public class Flatten extends DefaultHandler {
  
  // ////////////////////////////////////////
  // ////////////// FIELDS //////////////////
  // ////////////////////////////////////////
  
  /**
   * Name of this version (should be global for project)
   */
  public static final String VERSION_NAME = "Vanilla-1";
  
  /**
   * Date of last changes
   */
  public static final String VERSION_DATE = "Mon Apr  9 14:28:08 2001";
  
  /**
   * String describing the version of this API
   */
  public static final String VERSION = VERSION_NAME + " " + VERSION_DATE;
  
    
  // ===============================================
  // [0.1] Global Flags influencing the translation
  // ===============================================
  
    
  /**
   * Factor for indenting
   */
  static int factor = 2;
  /**
   * Additional Indentation for attributes
   */
  static int attIndent = 4;
  
  static String blanks = "                                                                                ";

  /**
   * The way to access the hierarchical document
   */
  static HierarchicalDocumentReader hdr;

  /**
   * The way to create the flat document
   */
  private static FlatDocumentWriter fdw;
  
  /**
   * An entity that translates basic locations and keeps track of them
   */
  private static LocationMapper lm;
    
  /**
   * An entity that translates pairs (hierarchial component, textual 
   * instantiation) to the textual instantiation of this compenent.
   */
  private static ComponentMapper cm;
  
  /**
   * Registration of hand-shake communication for Phase III processing
   */
  private static ChannelRegistry cr;

  /**
   * The document that the <TT>{@link #fdw}</TT> is writing
   */
  private static XmlDocument flatDoc;
  
  /**
   * Textual instantiation corresponding to the root.
   */
  protected static TextualInstantiation rootInstDummy;

  /**
   * Name of variable blocking joins, while other joins are executed
   */
  // !obsolete!  static String gjBLOCK = "BLOCK_GLOBAL_JOIN_STARTS";

  /**
   * The tree the system is mapped to
   */
  static InstantiationTree globalInstantiationTree;

  
  static org.w3c.dom.Element instantiation;
  static org.w3c.dom.Element system;
  
  static org.w3c.dom.Element template;
  static org.w3c.dom.Element location;
  static org.w3c.dom.Element name;
  static org.w3c.dom.Element transition;
  static org.w3c.dom.Element source;
  static org.w3c.dom.Element target;
  static org.w3c.dom.Element guard;
  static org.w3c.dom.Element synchronisation;
  static org.w3c.dom.Element assignment;
  
  // =========================
  // [0.1.1] Lists
  // =========================
  
  static Vector collectedInstantiations; //!!!! surplus, see slackInstantion

  // =========================
  // [0.1.2] Strings
  // =========================
  
  /**
   * Prefix that makes the currently translated  Template unique
   */
  static String currentTemplatePrefix = "";
  
  
  /**
   * Main stack that contains the instantiations of templates to be
   * processed
   * 
   * <h3>In Vanilla-1, every template is instantiated exactly <I>once</I>
   * </h3>
   */
  static Stack instantiationsToBeTranslated ;
  
 
  
  // ========================================
  // [0.1.3] AUX fields for names
  // ========================================
  
  /**
   * Counter to make introduced locations for components unique
   */
  static long componentCount = 0L;
  
  /**
   * Counter to make hashing of elements unique
   */
  static long elementCount = 0L;

  /**
   * Hashtable mapping (original, hierarchical) template names to
   * the corresponding Element nodes, see {@link #memorizeTemplate}
   */
  private static Hashtable hashTemplateNamesToElements;
  

  /**
   * Hashtable storing the IDs the tranlation of elements corresponds
   * to ?t?
   */
  private static Hashtable hashElementsToIDs;
  
  /**
   * Hashtable mapping instantiation/componentID to the
   * {@link TextualInstantiation} it corresponds to.
   */
  private static Hashtable hashInstComponentToTInst;
  
  // ==================================================
  // [0.2] Other AUX    
  // ==================================================
  
  /**
   * Output stream writer
   */
  static private Writer  out;
  
  /**
   * Spam out debuggin information, if <TT>debug</TT> is true
   */
  static boolean debug = true;
  
  /**
   * Do (possibly time-consuming) sanity checks, if true
   */
  static boolean sanityChecks = true;



  // ////////////////////////////////////////
  // //////////  CONSTRUCTORS  //////////////
  // ////////////////////////////////////////
  
  /**
Default Constructor: missing (only static)
   */
  //    public Flatten(){
  //  }
  
  // ////////////////////////////////////////
  // ////////////// METHODS  ////////////////
  // ////////////////////////////////////////
  
  // =================================================================
  // [1] MAIN METHOD (STATIC)
  // =================================================================
  
  public static void main (String argv [])
  {
    if (argv.length != 1) {
      System.err.println ("Usage: cmd filename");
      System.exit (1);
    }
    
    
    try {
      
      DocumentBuilderFactory domFactory = 
	DocumentBuilderFactory.newInstance();
      domFactory.setNamespaceAware(true);
      domFactory.setValidating(true);
      
      
      if(debug)
	System.out.println("** Document Builder Factory validating: "
			   + domFactory.isValidating());
      if(debug)
	System.out.println("** Document Builder Factory namespace aware: "
			   + domFactory.isNamespaceAware());
      
      DocumentBuilder dom = domFactory.newDocumentBuilder();
      dom.setErrorHandler(null); // default error handler

      Document doc = dom.parse(new File(argv[0]));
      
      /*
	String tagName = argv[0];
	NodeList nl = doc.getElementsByTagName(tagName);
	if(debug)
	System.out.println("Document contains " + nl.getLength() +
	" elements with tag " + tagName +
	":");
	for(int i=0; i< nl.getLength();i++){
	Node n = nl.item(i);
	if(debug)
	System.out.println("++ Node ++\n" +
	"NAME:    " + n.getNodeName() + "\n" +
	"VALUE:   " + n.getNodeValue() + "\n" +
	"CHILDREN:" + (n.getChildNodes()).getLength()
	);
	}
      */
      traverse(doc,"hta");
      
      // -- ------------------------------
      if(debug)
	System.out.println("** Document Builder DONE.");
      // -- ------------------------------
      if(false)return;
      
      if(false){
	
	// Use the validating parser
	SAXParserFactory factory = SAXParserFactory.newInstance();
	factory.setValidating(true);
	if(debug)
	  System.out.println("** Parser Factory validating: "
			     + factory.isValidating());
	
	//        factory.setNamespaceAware(true); // trows exception..
	if(debug)
	  System.out.println("** Parser Factory namespace-aware: "
			     + factory.isNamespaceAware());
	//!o!** begin of modification **** Wed Sep 13 16:11:14 2000 *****
	
	// Set up output stream
	out = new OutputStreamWriter (System.out, "UTF8");
	
	// Parse the input
	SAXParser saxParser = factory.newSAXParser();
	saxParser.parse( new File(argv [0]), new Flatten() );
      }
      
    } catch (SAXParseException spe) {
      // Error generated by the parser
      System.out.println ("\n** Parsing error" 
			  + ", line " + spe.getLineNumber ()
			  + ", uri " + spe.getSystemId ());
      System.out.println("   " + spe.getMessage() );
      
      // Use the contained exception, if any
      Exception  x = spe;
      if (spe.getException() != null)
	x = spe.getException();
      x.printStackTrace();
      
    } catch (SAXException sxe) {
      // Error generated by this application
      // (or a parser-initialization error)
      Exception  x = sxe;
      if (sxe.getException() != null)
	x = sxe.getException();
      x.printStackTrace();
      
    } catch (ParserConfigurationException pce) {
      // Parser with specified options can't be built
      pce.printStackTrace();
      
    } catch (IOException ioe) {
      // I/O error
      ioe.printStackTrace();
    } catch (Exception e) {
      // some other error
      e.printStackTrace();
    }
    
    // ===============================================
    // [1.1] Now translate it
    // ===============================================

    try{

      DocumentBuilderFactory domFactory = 
	DocumentBuilderFactory.newInstance();
      domFactory.setNamespaceAware(true);
      domFactory.setValidating(true);
    
      DocumentBuilder dom = domFactory.newDocumentBuilder();
 
      Document theDoc = dom.parse(new File(argv[0]));
      XmlDocument translatedDoc = constructFlatDoc(new HierarchicalDocumentReader(theDoc));
      
      // -------------------------------------------------------------
      //      if(debug)System.out.println(flatDoc.toString());
      //  traverse(flatDoc,"nta");
      
      if(debug)
	System.out.println("%%% %% new flat Document: show %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
      
      // translatedDoc.write(System.out);  // -- write to stdout
      
      String fileName = "output.xml";
      FileWriter fw = new FileWriter(fileName);
      translatedDoc.write(fw);


      // -- Display Instantiation Tree -------------------------------------
      if(debug){
	System.out.println("%%% %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
	System.out.println("%%% Show Instantiation Tree");
	System.out.println("%%% %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
	System.out.println(globalInstantiationTree.showWithOffset(0));
      }

      // -- Parse it for validation ----------------------------------------
      
      if(debug){
	System.out.println("%%% %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
	System.out.println("%%% Parse flat document");
	System.out.println("%%% %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
      }
      
      Document validateDoc = dom.parse(new File("output.xml"));
      
      if(debug)
	System.out.println("%%% Validation: OK.\n");


    } catch (Exception e) {
      // some other error
      e.printStackTrace();
    }

    // ---------------------------------------------------------------------
    //    testGeoCordinates(1000);
    // ------------------------------------------------------------- THE END
    System.exit (0);
  }
  
  /**    
   * <H1>Main Method</H1>
   * Create an nta document that is a translation of the original one<BR>
   * Uses auxillary global fields, like <TT>{@link #hdr}</TT> or 
   * <TT>{@link #flatDoc}</TT>.
   * 
   * Constructs the <TT>{@link #globalInstantiationTree}</TT>, which is 
   * used to compute the global joins.
   * 
   */
  public static XmlDocument constructFlatDoc(HierarchicalDocumentReader reader)
    throws Exception {

    hdr = reader;
    fdw = new FlatDocumentWriter();
    flatDoc = fdw.doc;
    
    lm = new LocationMapper(hdr, fdw);
    cm = new ComponentMapper();

    GlobalJoin.init(cm, hdr);
    
    rootInstDummy = new TextualInstantiation("ROOT", null, null, null, null, null);
    rootInstDummy.isANDComponent = true;
    globalInstantiationTree = new InstantiationTree(rootInstDummy);
    
    cr = new ChannelRegistry(globalInstantiationTree);
    
    System.out.println("%%% %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
    System.out.println("%%% Construct flat document");
    System.out.println("%%% use Vanilla-1 method");
    System.out.println("%%% %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
  
    // ==============================================================
    
    // ======================
    // [1.1.1] Hashtables
    // ======================

    hashElementsToIDs = new Hashtable();
    hashTemplateNamesToElements = new Hashtable();
    hashInstComponentToTInst = new Hashtable();
    
    // =====================================
    // [1.1.2] Lists & String Init
    // =====================================

    instantiationsToBeTranslated = new Stack();
    
    GlobalJoin.allGlobalJoins = new Vector();

    Vector rootEntrySignals = new Vector();
    Vector rootExitSignals = new Vector();
    
    NodeList templateNodes = hdr.getAllTemplates();
    for(int i = 0; i < templateNodes.getLength(); i++){
      memorizeTemplate((Element)templateNodes.item(i));
    }
  


    // ==================================================
    // [1.2] Create elements      
    // ==================================================
    
    NodeList directChildList = hdr.getDirectChildNodes();
    int directChildListLength = directChildList.getLength();
    
    
    String oldSystemText = ""; // fail if uninitiated
    String oldInstantiationText = ""; 
    
    // ========================================
    // [1.2.1] Declaration (of global variables)
    // ========================================

    oldSystemText = hdr.getSystemText().trim();
    oldInstantiationText = hdr.getInstantiationText().trim();
    
    fdw.addDeclarationText(hdr.getDeclarationText().trim());
    fdw.addDeclarationText("\n\n// ----- Translation Slack ----- \n");


    
    // ========================================
    // [1.2.2] Retrieve the used Templates
    // ========================================
    
    Vector listOfUsedTemplates = new Vector();
    
    if(!oldSystemText.startsWith("system ")){
      throw new Exception("System Tag contains strange text: " + oldSystemText);
    }
    
    collectedInstantiations = new Vector();
    Vector usedInstantiations = hdr.splitCommaSeperatedString(oldSystemText.substring(oldSystemText.indexOf(' ')+1,oldSystemText.indexOf(';')));
    
    // -- Get templates name(s) corresponding to usedInstantiations --

    NodeList rootEntryPoints = hdr.getAllAliveGlobalInits();
    
    for(Enumeration e = usedInstantiations.elements(); e.hasMoreElements();){
      String name = ((String)e.nextElement()).trim();
      
      int pos = DocumentReader.max5(
	  oldInstantiationText.indexOf(name + " ",0),
	  oldInstantiationText.indexOf(name + ",",0),
	  oldInstantiationText.indexOf(name + "\t",0),
	  oldInstantiationText.indexOf(name + "\n",0),
	  oldInstantiationText.indexOf(name + ":",0));
      
      if(pos < 0)
	throw new Exception("OldInstantiation >>" + name + "<< not found.");
      
      int posDef = oldInstantiationText.indexOf(":=",pos-1)+2;
      while(java.lang.Character.isWhitespace(oldInstantiationText.charAt(posDef)))
	posDef++;
      int posEnd = oldInstantiationText.indexOf('(',posDef);
      String templateName = oldInstantiationText.substring(posDef,posEnd).trim();
      int parStart = posEnd+1;
      int parEnd = oldInstantiationText.indexOf(')',parStart);
      String parameterString = oldInstantiationText.substring(parStart,parEnd).trim();
      
      Element templateElement = 
	(Element)hashTemplateNamesToElements.get(templateName);
      
      if( null == templateElement )
	throw new Exception("ERROR: instanciated template \"" +
			    templateName +
			    "\" not found.");
      
      System.out.println("ZZ " + parameterString);
      
      TextualInstantiation tinst = 
	new TextualInstantiation(name,
				 hdr.splitCommaSeperatedString(parameterString),
				 null,
				 templateElement,
				 null,
				 fdw.createTemplate());
      tinst.isANDComponent = (templateElement.getAttribute("type")).equals("AND");
      cm.enterTextualInstantiation(hdr.wrapComponentAroundGlobalInstantiation(name, templateName),
				   rootInstDummy,
				   tinst);
      // ----------------------------------------------
      globalInstantiationTree.addChild(new InstantiationTree(tinst));
      // -- get global entry point ------------------------------
    
      Element rootEntry = null;
      for(int i =0; (null == rootEntry) && ( i< rootEntryPoints.getLength()); i++){
	if(name.equals(((Element)rootEntryPoints.item(i)).getAttribute("instantiationname")))
	  rootEntry = (Element)rootEntryPoints.item(i);
      }
      if(null == rootEntry)
	throw new Exception("ERROR: not globalinit declared for used instantiation: \n\t" + tinst.toString());
      
      String rootEntryID = (rootEntry).getAttribute("ref");
      Element rootEntryPoint = hdr.getElementByID(rootEntryID);
      String activator = getSignal(tinst.objectName, rootEntryPoint);
      
      tinst.setActivator(activator);
	
      // fdw.addDeclaration("urgent chan  " + rootActivator ); declared on entry
      
      if(debug)
	System.out.println("$$  adding to instantiationsToBeTranslated stack: " + tinst.toString());

      rootEntrySignals.addElement(activator); 

      // ----------------------------------------------
      instantiationsToBeTranslated.push(tinst);
      // ----------------------------------------------

      // -- adjust global exits -----------------------
      hdr.createJoinsForGlobalExits(tinst, rootEntry);
    }
    
    // ==================================================
    // [1.3] Phase I: Process the necessary instantiations          
    // ==================================================
    
    if(debug)
      System.out.println("%% === PHASE I ================================================ \n" +
			 "%% --- Creating Templates: ------------------------------ ");
    
    while(!instantiationsToBeTranslated.empty()){
      TextualInstantiation tinst = (TextualInstantiation)instantiationsToBeTranslated.pop();
      translateTemplateInstantiation(tinst);
      fdw.addInstantiation(tinst);
    }
    
    if(debug)
      System.out.println("%% --- Creating Templates -------------------------- DONE");

    Element kickoffTemplateElement = fdw.createKickTemplate(rootEntrySignals);
    // fdw.createHurryDummy();

    // ---------------------------------------------------------------------

    // ===========================================
    // [1.4] PHASE II
    // ===========================================

    if(debug)
      System.out.println("%% === PHASE II =============================================== \n" +
			 "%% --- Growing all Global Joins: ------------------------------ ");

    GlobalJoin.growAll();


    if(debug)
      System.out.println("%% --- Processing Global Joins: ------------------------------ ");
    
    // !obsolete! fdw.addDeclaration("int[0,1] " + gjBLOCK);

    lm.allEntriesAreCreated = true;

    for(Enumeration e = GlobalJoin.allGlobalJoins.elements();
	e.hasMoreElements(); ){
      processGlobalJoin((GlobalJoin)e.nextElement());
    }
    
    if(debug)
      System.out.println("%% --- Processing Global Joins: ------------------------- DONE");
    
    // ---------------------------------------------------------------------

    // ===========================================
    // [1.5] PHASE III
    // ===========================================

    if(debug)
      System.out.println("%% === PHASE III ============================================== \n" +
			 "%% duplicating channels, if necessary");
    
    while(!cr.possibleConflicts.empty()){
      SyncTransTinstTriple conflict = (SyncTransTinstTriple)cr.possibleConflicts.pop();
      String sync = conflict.sync;
      Element trans = conflict.trans;
      TextualInstantiation tinst = conflict.tinst; // tinst of sub-component
      if(cr.occursInOrBelow(cr.matchingSync(sync), tinst)){
	String newChan = cr.chanOfSync(sync) + cr.newPostfix();
	fdw.copyChannelDeclaration(cr.chanOfSync(sync), newChan);
	cr.unregisterSync(sync, trans, tinst.father);
	String changedSync =  newChan + cr.typeOfSync(sync);
	fdw.changeSyncTextAtTransition(trans, changedSync);
	cr.registerSync(changedSync, trans, tinst.father);
	
	for(Enumeration e = cr.enumTinstTransitionsWithSyncOutsideTinst(cr.matchingSync(sync), tinst); e.hasMoreElements(); ){
	  TinstElementPair tip = (TinstElementPair)e.nextElement();
	  TextualInstantiation matchTinst = tip.tinst;
	  Element matchTransition = tip.element;
	  Element clone = fdw.cloneTransition(matchTransition);
	  fdw.changeSyncTextAtTransition(clone, cr.matchingSync(changedSync));
	  cr.registerSync(cr.matchingSync(changedSync), clone, matchTinst);
	  // -- check whether this is in the conflict stack -----
	  cr.addConflictsForNewSync(sync, changedSync);
	}
      }

    }
    if(debug)
      System.out.println("%% --- Phase III processing ----------------------------- DONE");
    

    // ===========================================
    // [1.6] Run Layouter (postprocess)
    // ===========================================

    if(debug)
      System.out.println("%% --- Recomputing Layout: ----------------------------------------------------- ");

    fdw.complete();

    fdw.cleanupTransitionLabels();

    fdw.recomputeAllTemplateLayouts();

    if(debug)
      System.out.println("%% --- Recomputing Layout: ----------------------------------------------------- DONE");

    // ===========================================
    // [1.7] Add Information about Translation
    // ===========================================

    fdw.addInformationAboutTranslation(globalInstantiationTree);

    return fdw.getDoc();
  }
  
  // //////////////////////////////////////////////////////////////////////
  // Methods for flat Document Creation
  // //////////////////////////////////////////////////////////////////////
  // ======================================================================
 
  
  /**
   * Return the node with specified template name
   */
  public static Element getNodeOfTemplate(String s)
    throws Exception {
    
    if(debug)
      System.out.println("$$$ Searching for template >>" + s + "<<");
    
    NodeList allTemplates = hdr.getAllTemplates();
    int n = allTemplates.getLength();
    
    if(debug)
      System.out.println("$$$ found " + n + " templates.");
    
    for(int i=0; i < n; i++){
      NodeList nameNodes = ((Element)(allTemplates.item(i))).getElementsByTagName("name");
      String nameOfTemplate = ((Text)((nameNodes.item(0)).getFirstChild())).getData();
      
      if(debug)
	System.out.println("$$$ " + i + ". " + nameOfTemplate);
      
      if( nameOfTemplate.equals(s) )
	return (Element)allTemplates.item(i);
    }
    throw new Exception("Template >>" + s + "<< not found.");
  }
  /**
   * Return the first child element that is of the given kind.<BR>
   * Throws an Exception, if no such child exists.
   */
  private static Element getFirstChildThatIsElement(Element el, String tagName)
    throws Exception{
    NodeList directChildNodes = el.getChildNodes();
    for(int i=0; i < directChildNodes.getLength(); i++){
      Node child = directChildNodes.item(i);
      if( (child instanceof Element) &&
	  (((Element)child).getTagName()).equals(tagName))
	return (Element)child;
    }
    throw new Exception("ERROR: Element " +
			el.toString() +
			" does not contain a child element <" +
			tagName + 
			">");
    
  }
  
  
  /**
   * <H2>Translates a instantiation of a hierarchical template to a flat 
   * one</H2>
   * <H3>Assumes:</H3>
   * <UL>
   *   <LI>Every component will be translated (somewhere else)</LI>
   *   <LI>The activator of a component is <TT>"_activate_"</TT> + 
   *       <TT>ID</TT></LI>
   *   <LI>the activator has been declared as a channel</LI>
   * </UL>
   * Add the template to <TT>{@link #fdw}</TT> via the method
   * <TT>{@link FlatDocumentWriter#addTemplate}</TT>.
   * Returns the textual name of the generated <B>instantiation</B>.
   * 
   * 
   * <H4>Creates a <I>flat</I> template from a hierarchical one</H4>
   * <UL>
   *   <LI>the result will have no <TT>name</TT></LI>
   *   <LI>the result has always an parameter element, possibly it is 
   *       empty</LI>
   *   <LI>there is an additional initial location ("_idle")</LI>
   *   <LI><BLINK>!!! entry points</BLINK></LI>
   *   <LI><BLINK>!!! exit points</BLINK></LI>
   *   <LI><EM>components</EM> are translated into ordinary
   *       locations</LI>
   *   <LI>transitions <B>to</B> <EM>components</EM> are augmented 
   *       with the appropriate ?-synchronisation</LI>
   *   <LI>transitions <B>from</B> <EM>components</EM> are augmented 
   *       with the appropriate ?-synchronisation</LI>
   * </UL>
   * 
   * <I>Constructs the <TT>{@link #globalInstantiationTree}</TT> as it goes 
   * along</I><BR><BR>
   * <B>USES GLOBAL FIELD <TT>{@link #currentTemplatePrefix}</TT></B>
   * 
   */
  private static String translateTemplateInstantiation(TextualInstantiation tinst)
    throws Exception {
    
    if(debug)
      System.out.println("----------------- transforming template " + DocumentReader.getElementName(tinst.templateElement));
    
    // -- exit signal ------------------------------------------------------
    fdw.addDeclaration("chan  " + tinst.exitSignal );
    // ---------------------------------------------------------------------
    if((tinst.templateElement.getAttribute("type")).equals("AND"))
      return translateANDTemplateInstantiation(tinst);
    else
      return translateXORTemplateInstantiation(tinst);
  }
  /**
   * <H2>Translates a instantiation of a hierarchical AND template to a flat 
   * (xor) template instantiation</H2>
   *
   * (called from translateTemplateInstantiation)
   */
  private static String translateXORTemplateInstantiation(TextualInstantiation tinst)
      throws Exception {


    //System.out.println("------------------------------------------------------------" + tinst.toString());

    
    String instantiationName = tinst.objectName;
    Element originalTemplate = tinst.templateElement;
    boolean isANDComponent = tinst.isANDComponent;
    
    currentTemplatePrefix = tinst.objectName; // used for mapID
    
    fdw.resetDefaultLayout();
    
    // ---------------------------------------------------------------------

    // boolean isANDTemplate = false;


    InstantiationTree positionInInstantiationTree = globalInstantiationTree.findNodeWithContent(tinst);
    Stack connectorTransistions = new Stack();
    
    Element aTemplate = tinst.translationOfTemplateElement;
    Element nameNode = fdw.createNameElement(instantiationName);
    Element aParameter = fdw.createParameter(new Vector());
    Element aDeclaration = fdw.createDeclaration("");
    Element aTransition;
    
    aTemplate.appendChild(nameNode);
    aTemplate.appendChild(aParameter);
    aTemplate.appendChild(aDeclaration);
    
    System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n" + globalInstantiationTree.toString());

    // -- Ordinary Locations -----------------------------------------------
    aTemplate.appendChild(flatDoc.createComment("ordinary locations"));
    
    NodeList locationNodes = DocumentReader.getAllChildrenWithLabel(originalTemplate,
								    "location");
    for(int i=0; i < locationNodes.getLength(); i++){
      fdw.addLocationToTemplate(transformLocationToFlat(tinst, (Element)locationNodes.item(i)),
				aTemplate);

    }
    // -- Compontents --------------------------------------------------
    
    aTemplate.appendChild(flatDoc.createComment("now the (former) components"));
    
    NodeList componentNodes = DocumentReader.getAllChildrenWithLabel(originalTemplate,
								     "component");
    
    // -- Push on Stack -------------------------------
    for(int i=0; i < componentNodes.getLength(); i++){
      Element component = (Element)componentNodes.item(i);
      String templateName = component.getAttribute("instantiates");
      Element subTemplate = (Element)hashTemplateNamesToElements.get(templateName);
      if( null == subTemplate )
	throw new Exception("ERROR: instanciated template \"" +
			    subTemplate +
			    "\" not found.");
      
      TextualInstantiation cpt = 
	cm.retrieveTextualInstantiation(component, tinst);
      cpt.setup(DocumentReader.getElementName(component),
		new Vector(),
		tinst,
		subTemplate,
		component,
		fdw.createTemplate());
      // ----------------------------------------------
      positionInInstantiationTree.addChild(new InstantiationTree(cpt));
      
      cpt.isANDComponent = (cpt.templateElement.getAttribute("type")).equals("AND");
      
      instantiationsToBeTranslated.push(cpt);
      
      memorizeInstantionAndComponentIDToTextualInstantiation(instantiationName, DocumentReader.getElementID(component), cpt);
      // ----------------------------------------------
      Element subComponentActiveLoc =
	lm.migrateOrdinaryLocation(component, tinst);
      fdw.addLocationToTemplate(subComponentActiveLoc, aTemplate);

      // -- translate all entries of Component (in parent) -----------------
      String componentID = hdr.getElementID(component);
      NodeList allEntries = hdr.getAllChildrenWithLabel(subTemplate, "entry");
      for(int j=0; j < allEntries.getLength(); j++){
	Element entry = (Element)allEntries.item(j);
	String entryref = hdr.getElementID(entry);
	Element newEntryLoc = lm.mapTargetIDEntryrefToFlatLocation(componentID, entryref,tinst); 
	fdw.makeLocationCommitted(newEntryLoc);
	Element newEntryTransition =
	  fdw.createTransitionInTemplate(aTemplate);
	fdw.addSourceToTransition(fdw.createSource(hdr.getElementID(newEntryLoc)),
				  newEntryTransition);
	fdw.addTargetToTransition(fdw.createTarget(lm.mapHierachicalLocationToFlatOne(component, tinst)),
				  newEntryTransition);

	String sync = 	getSignal(cpt.objectName, entry);
	fdw.addSendSynchronisationToTransition(sync,
					       newEntryTransition);
	// ??? fwd.addLocationToTemplate(newEntryLoc, aTemplate);
	fdw.addTransitionToTemplate(newEntryTransition, aTemplate);
      }
    }
    // -- Special Location: Idle -------------------------------------------
    
    aTemplate.appendChild(flatDoc.createComment("special location: idle"));
    
    Element idleLocation = lm.mapTextualInstantiationToIdleLocation(tinst);
    String  idleLocationID = DocumentReader.getElementID(idleLocation);
    
    // -- For every entry add transitions on stack -------------------------
    // -- (by conventions, sync channels are declared on entry) ------------
    
    Stack entryTransitions = new Stack();
    
    NodeList entryNodes = hdr.getAllChildrenWithLabel(originalTemplate, 
						      "entry");
    
    // -- translate entries as transitions from IDLE -----------------------
    

    if(debug)
      System.out.println("ALL " + entryNodes.getLength() + " ENTRIES of " + tinst.toString() + "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

    for(int i=0; i < entryNodes.getLength(); i++){
      Element entry = (Element)entryNodes.item(i);
      String signal = getSignal(instantiationName, entry);
      fdw.addDeclaration("chan  " + signal );
      // --
      Element target  =   hdr.getTargetElementOfEntry(entry);
      Element targetTemplateElement =  hdr.getElementByID(target.getAttribute("ref"));
      String targetID = hdr.getElementID(targetTemplateElement);

      Vector guards = new Vector(); // vector of strings
      Vector assignments = new Vector(); // vector of strings
      

      if(debug)
	System.out.println("########################################################################################################################ENTRY: " + entry.toString());

      if(hdr.isComponent(targetTemplateElement)){
	if(debug)
	  System.out.println("-------------------------------------------------- non-basic entry:\n" + target.toString() + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + targetID  );
	targetID = fdw.getElementID(lm.mapTargetToFlatLocation(target, tinst));
	// =======================================================
	// [1.8] IN THIS CASE: COLLECT ALL GUARDS/ASSIGNMENTS
	// =======================================================
	hdr.collectGuardsAndAssignmentsOfTarget(target, guards, assignments,
						null); 
	fdw.appendComment(aTemplate, "entry of sub-component [" + hdr.getElementName(targetTemplateElement) + "]");
      }
      else {
	targetID = mapID(tinst, targetID); 
      }
      if(debug)
	System.out.println("^^ adding transition from " + 
			   DocumentReader.getElementID(idleLocation) +
			   " to " +
			   targetID);
      
      aTransition = fdw.createTransition(idleLocationID,
					 targetID);
      fdw.addReceiveSynchronisationToTransition(signal, aTransition);
      for(Enumeration e=guards.elements(); e.hasMoreElements(); ){
	fdw.conjunctTextualGuardToTransition(aTransition, (String)e.nextElement());
      }
      for(Enumeration e=assignments.elements(); e.hasMoreElements(); ){
	fdw.conjunctTextualAssignmentToTransition(aTransition, (String)e.nextElement()); 
      }
      
      entryTransitions.push(aTransition);
    }
    // ===========================================
    // [1.9] NOW: Insert Transitions 
    // ===========================================
    
    // -- leftover from entries --------------------------------------------
    
    aTemplate.appendChild(flatDoc.createComment("entry pseudo-transitions (OR component)"));
    
    while(!entryTransitions.empty())
      fdw.addTransitionToTemplate((Element)(entryTransitions.pop()), aTemplate);
    
    // -- ordinary transitions --------------------------------------------
    
    aTemplate.appendChild(flatDoc.createComment("ordinary transitions"));
    NodeList origTransitions = DocumentReader.getAllChildrenWithLabel(originalTemplate,
								      "transition");
    
    for(int i=0 ; i < origTransitions.getLength(); i++){
      Element transition = (Element)origTransitions.item(i);
      
      Element source = DocumentReader.getFirstChildWithLabel(transition, "source");
      Element target = DocumentReader.getFirstChildWithLabel(transition, "target");
      String sourceID = source.getAttribute("ref");
      String targetID = target.getAttribute("ref");
      Element sourceTemplateElement =  hdr.getElementByID(sourceID);
      Element targetTemplateElement =  hdr.getElementByID(targetID);

      if((sourceTemplateElement.getTagName()).equals("component")){
	// !wrong!sourceID = mapSourceToID(instantiationName, source);
	sourceID = fdw.getElementID(lm.mapHierachicalLocationToFlatOne(sourceTemplateElement, tinst));
	// =======================================================
	// [1.10] IN THIS CASE: Compute Global Join
	// =======================================================
	
	TextualInstantiation subTinst = 
	  cm.retrieveTextualInstantiation(sourceTemplateElement, tinst);
	
	GlobalJoin gj = GlobalJoin.getGlobalJoin(transition, tinst);
	//	gj.grow(subTinst, 
	//	hdr.getElementByID(source.getAttribute("exitref")) );
	
	/// !!! DON'T ADD TRANSITION YET !!!

      }
      else {
	sourceID = instantiationName +  sourceID;
	
	Vector guards = new Vector(); // vector of strings
	Vector assignments = new Vector(); // vector of strings
	
	if((targetTemplateElement.getTagName()).equals("component")){
	  targetID = fdw.getElementID(lm.mapTargetToFlatLocation(target, tinst));
	  // =======================================================
	  // [1.11] IN THIS CASE: COLLECT ALL GUARDS/ASSIGNMENTS
	  // =======================================================
	  hdr.collectGuardsAndAssignmentsOfTarget(target, guards, assignments, null); 
	  aTemplate.appendChild(flatDoc.createComment("entry of sub-component [" +
						      hdr.getElementName(targetTemplateElement) +
						      "]"));
	}
	else {
	  targetID = instantiationName + targetID;
	}
      
	Element translatedTransition = fdw.createTransition(sourceID, targetID);
	migrateTransitionChildren(transition, 
				  tinst,
				  translatedTransition);
	for(Enumeration e=guards.elements(); e.hasMoreElements(); ){
	  fdw.conjunctTextualGuardToTransition(translatedTransition, (String)e.nextElement());
	}
	for(Enumeration e=assignments.elements(); e.hasMoreElements(); ){
	  fdw.conjunctTextualAssignmentToTransition(translatedTransition, (String)e.nextElement()); 
	}
	fdw.addTransitionToTemplate(translatedTransition, aTemplate);
      }
    }
    // ---------------------------------------------------------------------
    // -- exit transitions -------------------------------------------------
    // ---------------------------------------------------------------------
    fdw.appendComment(aTemplate,"exit transitions"); // OBSOLETE??

    if(hdr.hasDefaultExit(tinst.templateElement)){
      for(Enumeration e = hdr.getAllLocationsAndComponents(tinst.templateElement).elements(); e.hasMoreElements(); ){
	Element sourceTemplateElement = (Element)e.nextElement();
	
	System.out.println("!!!!!!!!!!!!!!!!!!!!!! " + sourceTemplateElement.toString());
	
	
	String sourceID = hdr.getElementID(sourceTemplateElement);
	if((sourceTemplateElement.getTagName()).equals("component")){
	  
	  
	  sourceID = fdw.getElementID(lm.mapHierachicalLocationToFlatOne(sourceTemplateElement, tinst));
	  //mapSourceToID(instantiationName, sourceTemplateElement);
	}
	else {
	  sourceID = instantiationName +  sourceID;
	}
	Element transition = fdw.createTransition(sourceID, idleLocationID);
	fdw.addReceiveSynchronisationToTransition(tinst.exitSignal, transition);
	fdw.addTransitionToTemplate(transition, aTemplate);
      }
    }
    else { // -- treat each exit separately --------------------------------
      
      NodeList allExits = hdr.getAllChildrenWithLabel(originalTemplate,
						      "exit");
      for(int i=0; i < allExits.getLength(); i++){
	
	NodeList connections = 
	  hdr.getAllChildrenWithLabel(((Element)allExits.item(i)),
				      "connection");
	
	for(int j=0; j < connections.getLength(); j++){
	  Element connection = (Element)connections.item(j);
	  Element source = hdr.getTheChildWithLabel(connection, "source");
	  String sourceID = source.getAttribute("ref");
	  Element sourceTemplateElement =  hdr.getElementByID(sourceID);
	  if((sourceTemplateElement.getTagName()).equals("component")){
	    sourceID = mapSourceToID(instantiationName, source);
	  }
	  else {
	    sourceID = instantiationName +  sourceID;
	  }
	  Element transition = fdw.createTransition(sourceID, idleLocationID);
	  fdw.addReceiveSynchronisationToTransition(tinst.exitSignal, transition);
	  fdw.addTransitionToTemplate(transition, aTemplate);
	}
      }
    }
    // -----------------------------------------------------------------
    fdw.makeLocationInitial(idleLocation);
    // ---------------------------------------------------------------------

    return tinst.objectName;
    }


  /**
   * <H2>Translates a instantiation of a hierarchical AND template to a flat 
   * (xor) template instantiation</H2>
   *
   * (called from translateTemplateInstantiation)
   */
  private static String translateANDTemplateInstantiation(TextualInstantiation tinst)
    throws Exception {

    String instantiationName = tinst.objectName;
    Element originalTemplate = tinst.templateElement;

    currentTemplatePrefix = tinst.objectName; // used for mapID
    
    fdw.resetDefaultLayout();

    // ---------------------------------------------------------------------
    
    InstantiationTree positionInInstantiationTree = globalInstantiationTree.findNodeWithContent(tinst);
    Stack connectorTransistions = new Stack();
    
    Element aTemplate = tinst.translationOfTemplateElement;
    fdw.addNameToElement(aTemplate, instantiationName);
    Element aParameter = fdw.createParameter(new Vector());
    Element aDeclaration = fdw.createDeclaration("");
    Element aTransition;
    
    aTemplate.appendChild(aParameter);
    aTemplate.appendChild(aDeclaration);
    
    // -- Auxillary Locations ----------------------------------------------

    Element idleLocation   = lm.mapTextualInstantiationToIdleLocation(tinst);
    Element activeLocation = lm.mapTextualInstantiationToActiveLocation(tinst);

    fdw.addLocationToTemplate(idleLocation, aTemplate);
    fdw.addLocationToTemplate(activeLocation, aTemplate);
    
    String idleLocationID = fdw.getElementID(idleLocation);
    String activeLocationID = fdw.getElementID(activeLocation);

    // -- Parallel Compontents ---------------------------------------------
    
    aTemplate.appendChild(flatDoc.createComment("parallel components"));
    
    NodeList componentNodes = DocumentReader.getAllChildrenWithLabel(originalTemplate,
								     "component");
    
    // -- Push on Stack -------------------------------
    for(int i=0; i < componentNodes.getLength(); i++){
      Element component = (Element)componentNodes.item(i);
      String templateName = component.getAttribute("instantiates");
      TextualInstantiation cpt = 
	cm.retrieveTextualInstantiation(component, tinst);
      cpt.setup(DocumentReader.getElementName(component),
		new Vector(),
		tinst,
		(Element)hashTemplateNamesToElements.get(templateName),
		component,
		fdw.createTemplate());
      // ----------------------------------------------
      positionInInstantiationTree.addChild(new InstantiationTree(cpt));

      cpt.isANDComponent = (cpt.templateElement.getAttribute("type")).equals("AND");
      
      instantiationsToBeTranslated.push(cpt);
      
      memorizeInstantionAndComponentIDToTextualInstantiation(instantiationName, DocumentReader.getElementID(component), cpt);
     
    }

    // -- translate entries and attached forks -----------------------------
      
    if(debug)
	System.out.println("+++ translating forks...");

    Stack entryTransitions = new Stack();
    NodeList entryNodes = DocumentReader.getAllChildrenWithLabel(originalTemplate, 
								 "entry");
    
    aTemplate.appendChild(flatDoc.createComment("translation of entries + forks"));
    
    for(int i=0; i < entryNodes.getLength(); i++){
      Element entry = (Element)entryNodes.item(i);
      fdw.addDeclaration("chan  " + getSignal(instantiationName, entry) );
      // --
      Element fork = hdr.getForkElementOfEntry(entry);
      NodeList targets = hdr.getTargetsOfFork(fork);
      String lastID = idleLocationID;
      String nextSignal = getSignal(instantiationName, entry);

      aTemplate.appendChild(flatDoc.createComment("entry " + 
						  (i+1) + " : " +
						  hdr.getElementName(entry)));
      if(debug)
	System.out.println("------> entry " + (i+1) + " : " +hdr.getElementName(entry));


      for(int j=0; j < targets.getLength(); j++){
	  
	Element target = (Element)targets.item(j);
	
	Element loc = fdw.inventLocation(instantiationName + "-" + DocumentReader.getElementID(entry) + "-fork-" + (j+1) + "-" ); 
	if(debug)
	  System.out.println("..... forking: " + (j+1));
	fdw.makeLocationCommitted(loc);
	fdw.addLocationToTemplate(loc, aTemplate);
	// --  put transitions on stack ---------------
	Element transistionLastToLoc = fdw.createTransition(lastID,hdr.getElementID(loc));
	lastID = hdr.getElementID(loc);
	
	if(0 == j){
	  fdw.addReceiveSynchronisationToTransition(nextSignal,
						    transistionLastToLoc); 
	}
	else {
	  fdw.addSendSynchronisationToTransition(nextSignal,
						 transistionLastToLoc);
	}

	nextSignal = getSignal(recallInstantionAndComponentIDToTextualInstantiation(instantiationName, target.getAttribute("ref")).objectName,
			       hdr.getElementByID(((Element)targets.item(j)).getAttribute("entryref")));
	
	entryTransitions.push(transistionLastToLoc);
      }
      Element transistionLastToActive = fdw.createTransition(lastID, 
							     activeLocationID);
      fdw.addSendSynchronisationToTransition(nextSignal,
					     transistionLastToActive);
      entryTransitions.push(transistionLastToActive);
    }


    // -- (by conventions, sync channels are declared on entry) ------------



    // ===========================================
    // [1.12] NOW: Insert Transitions (leftover from entries)
    // ===========================================
    
    aTemplate.appendChild(flatDoc.createComment("entry pseudo-transitions (AND component -> forks)"));
    
    while(!entryTransitions.empty())
      fdw.addTransitionToTemplate((Element)(entryTransitions.pop()), aTemplate);
    
    // -- process the exit points (to declare the signals) ---------------
    
    NodeList exitNodes = DocumentReader.getAllChildrenWithLabel(originalTemplate, 
								"exit");
    
    for(int i=0; i < exitNodes.getLength(); i++){
      Element exit = (Element)exitNodes.item(i);
      String signal = getSignal(instantiationName, exit);
      fdw.addDeclaration("chan  " + signal );
    }

    fdw.makeLocationInitial(idleLocation);

    // -- the (only) exit transition ---------------------------------------
    
    aTemplate.appendChild(flatDoc.createComment("the exit transitions"));
    Element transition = fdw.createTransition(activeLocationID, 
					      idleLocationID);
    fdw.addReceiveSynchronisationToTransition(tinst.exitSignal,
					      transition);
    fdw.addTransitionToTemplate(transition, aTemplate);

    // ---------------------------------------------------------------------
    
    return tinst.objectName;
  }
  

  // =================================================================
  // [2] Deal with global Joins
  // =================================================================

  /**
   * <H3>Processing Global Joins</H3>
   * For every global join, do
   * <UL>
   *   <LI>push the guard of the outgoing transition inside</LI>
   *   <LI>declare sync channels<BR>
   *   if required, make the synchronization channels urgent</LI>
   *   <LI>for the locations associated with this global join:
   *     <UL>
   *       <LI>to ingoing transitions, add increment to 
   * <TT>counterVariable</TT></LI>
   *       <LI>to outgoing transitions (that are <EM>not</EM> modelling 
   * the join),
   *       add decrement to <TT>counterVariable</TT></LI>
   *       <LI>to join-transition, add reset of 
   * <TT>counterVariable</TT></LI>
   *    </UL></LI>
   * </UL>
   * 
   * <H2>Structure of the translation</H2>
   * The translation is basically a chain of committed locations, that 
   * synchronizes by sending exit signals to the corresponding 
   * sub-components. This is done in the translation of the 
   * sub-component, where the root transition of the global join starts 
   * <BR>
   * 
   * The first transition contains 
   * <UL>
   * <LI>all the guards (in particular the one that 
   * states, whether it can be taken) </LI>
   * <LI>all the assignment</LI>
   * <LI>the synchronisation of the root transition (if any)</LI>
   * </UL>
   * 
   * 
   * <B>NOTE:</B> Uses {@see ChannelRegistry} to enter possible Phase III 
   * conflicts
   * 
   */
  private static void processGlobalJoin(GlobalJoin gj)
    throws Exception {
    System.out.print(gj.toString());
    System.out.println("---------------------------------------------------------------------- PROCESSING");

    // -- basic parameters ------------------------------------------

    Element topComponent = gj.getTopmostComponent();
    TextualInstantiation topTinst = 
      (TextualInstantiation)gj.tinstsToExit.elementAt(0);
 
    Element gjTemplate;
    Element nextTransition;
    String sourceID;
    String lastTargetID;

    boolean globalExit = (gj.rootTinst == rootInstDummy);

    if(globalExit){ // -- global join ----------------------------

      if(debug)
	System.out.println("&&&& Global exit: Join");

      gjTemplate = fdw.kickOffTemplate;
      //      nextTransition; //??? = fdw.createTransitionInTemplate(gjTemplate);
      sourceID = fdw.kickOffDoneID;      
      lastTargetID = fdw.kickOffDoneID;      

    }
    else {
       gjTemplate = gj.rootTinst.translationOfTemplateElement;

       // ---------------------------------------------
       if(sanityChecks &&
	  (topTinst.templateElement != hdr.getTemplateWithName(topComponent.getAttribute("instantiates"))))
	 throw new Exception("ERROR!\n" +
			     "the templates of the component and textual instantiation do not match: \n" +
			     topTinst.templateElement.toString() + "\n\n" +
			     (hdr.getTemplateWithName(topComponent.getAttribute("instantiates"))).toString());
       // ------------------------------------------------------------------
    }
    fdw.appendComment(gjTemplate, "inserting next global join [" +
		      gj.rootTransitions.size() + " root transitions]");
    
    // -- ADD COUNTER ------------------------------------------------------

    fdw.addDeclaration("int  " + gj.triggerVariable);

    HashSet incrementSet = new HashSet();
    HashSet decrementSet = new HashSet();

    for(Enumeration e = gj.allHierarchicalStartLocations.elements(); 
	e.hasMoreElements(); ){
      TinstElementPair pair = (TinstElementPair)e.nextElement();
      Element hierLoc = pair.element;
      TextualInstantiation hierTinst = pair.tinst;
      Element flatLoc = lm.mapHierachicalLocationToFlatOne(hierLoc, 
							   hierTinst);
      // -- ingoing -----------------------------------
      Vector transitions = fdw.getAllTransitionsLeadingToLocation(flatLoc);
      for(Enumeration plug = transitions.elements(); plug.hasMoreElements(); ){
	Object transition = plug.nextElement();
	if(decrementSet.contains(transition))
	  decrementSet.remove(transition);
	else
	  incrementSet.add(transition);
      }
      // -- outgoing ----------------------------------
      transitions = fdw.getAllTransitionsStartingAtLocation(flatLoc);
      for(Enumeration plug = transitions.elements(); plug.hasMoreElements(); ){
	Object transition = plug.nextElement();
	if(incrementSet.contains(transition))
	  incrementSet.remove(transition);
	else
	  decrementSet.add(transition);
      }
    }
    String increment = 
      gj.triggerVariable + " := " + gj.triggerVariable + " + 1 ";
    String decrement = 
	gj.triggerVariable + " := " + gj.triggerVariable + " - 1 ";
    // -- increments --------------------------------------------
    for(Iterator i = incrementSet.iterator(); i.hasNext(); ){
      fdw.conjunctTextualAssignmentToTransition((Element)i.next(),
						increment);
    }
    // -- decrements --------------------------------------------
    for(Iterator i = decrementSet.iterator(); i.hasNext(); ){
      fdw.conjunctTextualAssignmentToTransition((Element)i.next(),
						decrement);
    }
    // -- BROWSE THROUGH ALL ROOT TRANSITIONS ------------------------------

    for(Enumeration f = gj.rootTransitions.elements(); f.hasMoreElements(); ){
      Element connection = (Element)f.nextElement();
      
      nextTransition = fdw.createTransitionInTemplate(gjTemplate);
      
      Vector inAssign = new Vector();
      Vector inGuard = new Vector();
      
      // -- SPECIAL: if global join enters a component ----------------------
      if((!globalExit) &&
	 hdr.isComponent(hdr.getElementByID(hdr.getTheChildWithLabel(connection, "target").getAttribute("ref"))) ){
	hdr.collectGuardsAndAssignmentsOfTarget(hdr.getTheChildWithLabel(connection, "target"),
						inGuard, inAssign, gj.rootTinst);
      }
      // -- COLLECT GUARDS --------------------------------------
      String guardText = hdr.getTextualGuardOfTransitionIfPresent(connection);
      if(null != guardText)
	fdw.conjunctTextualGuardToTransition(nextTransition,
					     guardText);
      
      for(Enumeration e = gj.allGuards.elements(); e.hasMoreElements(); ){
	fdw.conjunctTextualGuardToTransition(nextTransition,
					     (String)e.nextElement());
      }
      //!obsolete! fdw.conjunctTextualGuardToTransition(nextTransition, gjBLOCK + " == 0");
      // -- threshold ---------------------------------
      fdw.conjunctTextualGuardToTransition(nextTransition,
					   gj.triggerVariable + 
					   " == " +
					   gj.threshold);
      for(Enumeration e = inGuard.elements(); e.hasMoreElements(); ){
	fdw.conjunctTextualGuardToTransition(nextTransition,
					     (String)e.nextElement());
      }
      // -- SYNC (if present and non-empty) -----------
      
      Element sync = hdr.getTheChildSynchronisationIfExists(connection);
      if( null != sync){
	String syncText = (hdr.getCdataOfElement(sync)).trim();
	if(syncText.length() > 0){
	  Element newSync = 
	    addRegisteredSynchronisationToTransition(syncText, nextTransition, gj.rootTinst);
	  // -- Entry for Phase III -------------------
	  cr.addConflict(syncText, nextTransition, topTinst);
	}
      }
      else {
	fdw.appendComment(nextTransition, "STRANGE: no sync found in root transition of global join");
      }
      // -- assignments -----------------------------------------
      String assignmentText = hdr.getTextualAssignmentOfTransitionIfPresent(connection); 
      if(null != assignmentText)
	fdw.conjunctTextualAssignmentToTransition(nextTransition, assignmentText);
      for(Enumeration e = gj.allAssignments.elements(); e.hasMoreElements(); ){
	fdw.conjunctTextualAssignmentToTransition(nextTransition,
						  (String)e.nextElement());
      }
      //!obsolete! fdw.conjunctTextualAssignmentToTransition(nextTransition, gjBLOCK + " :=  1");
      for(Enumeration e = inAssign.elements(); e.hasMoreElements(); ){
	  fdw.conjunctTextualAssignmentToTransition(nextTransition,
						    (String)e.nextElement());
      }
      // --------------------------------------------------------------------
      if(globalExit){
	sourceID = fdw.kickOffDoneID;    
	lastTargetID = fdw.kickOffDoneID;  
      }
      else {
	Element activeSubcomponentLocation = 
	  lm.mapHierachicalLocationToFlatOne(hdr.getElementByID(hdr.getTheChildWithLabel(connection, "source").getAttribute("ref")),
					    gj.rootTinst);
	 sourceID = fdw.getElementID(activeSubcomponentLocation);
	 
	 Element lastTargetLocation = 
	   lm.mapTargetToFlatLocation(hdr.getTheChildWithLabel(connection, "target"), 
				      gj.rootTinst);
	 lastTargetID = fdw.getElementID(lastTargetLocation);
     }
       
     
     // -- backwards travers subs ------------------------------
     Stack tinstsToExit = GlobalJoin.cloneStackOfPointers(gj.tinstsToExit);
     while(!tinstsToExit.empty()){
       TextualInstantiation tinst = (TextualInstantiation)tinstsToExit.pop();
       
	 Element loc = fdw.inventLocation();
	 String locID = fdw.getElementID(loc);
	 fdw.makeLocationCommitted(loc);
	 
	 fdw.addSourceToTransition(fdw.createSource(sourceID), nextTransition);
	 fdw.addTargetToTransition(fdw.createTarget(locID),    nextTransition);
	 
	 fdw.addLocationToTemplate(loc, gjTemplate);
	 fdw.addTransitionToTemplate(nextTransition, gjTemplate);
	 
	 // ------------------------------------------------------
	 nextTransition = fdw.createTransitionInTemplate(gjTemplate);
	 sourceID = locID;
	 fdw.addSendSynchronisationToTransition(tinst.exitSignal, nextTransition);
     }
     fdw.addSourceToTransition(fdw.createSource(sourceID),     nextTransition);
     fdw.addTargetToTransition(fdw.createTarget(lastTargetID), nextTransition);
     
     //!obsolete! fdw.conjunctTextualAssignmentToTransition(nextTransition, gjBLOCK + " :=  0");
     
       // --------------------------------------------------------
    }
  }
  /**
   * Browse the children of the original transition, transform them and 
   * add them to the new transition (in the specific instantiation)
   */
  private static void migrateTransitionChildren(Element origTransition,
						TextualInstantiation tinst,
						Element newTransition)
    throws Exception {
    
    Element el;

    NodeList childNodes = origTransition.getChildNodes();
    for(int i = 0; i < childNodes.getLength(); i++){
      Node node = childNodes.item(i);
      if( (node instanceof Element) ){
	if(hdr.isSourceElement(node)){        // ignore
	} else if(hdr.isTargetElement(node)){ // ignore
	} else if(hdr.isGuardElement(node)){
	  el = fdw.addGuardToTransition(newTransition,
					mapGuardText(DocumentReader.getCdataOfElement((Element)node)));
	} else if(hdr.isSynchronisationElement(node)){
	  el = addRegisteredSynchronisationToTransition(mapSynchronisationText(DocumentReader.getCdataOfElement((Element)node)),
							newTransition,
							tinst);
	  
	} else if(hdr.isAssignmentElement(node)){
	  el = fdw.addAssignmentToTransition(newTransition,
					     mapAssignmentText(DocumentReader.getCdataOfElement((Element)node)));
	} else if(hdr.isNailElement(node)){
	  newTransition.appendChild(mapNail((Element)node));
	} else {
	  throw new Exception("ERROR: transition child " +
			      node.toString() +
			      "unexpected.");
	}
      }
    }
  }

  // ==================================================
  // [2.1] Transforming hierarchical Elements
  // ==================================================
  /**
   * Maps an ID in a way, such that
   * <UL>
   *   <LI>the output is <EM>deterministic</EM></LI>
   *   <LI>the outcome is different for different inputs</LI>
   *   <LI>the outcome is different from flat locations</LI>
   * </UL>
   * Used in translation of locations &ampl; components.<BR><BR>
   */
  private static String mapID(TextualInstantiation tinst, String ID){
    return tinst.objectName + ID;
  }
  /**
   * Maps an <tt>name</tt> in a way, such that
   * <UL>
   *   <LI>the output is <EM>deterministic</EM></LI>
   *   <LI>the outcome is different for different inputs</LI>
   *   <LI>the outcome is different from flat locations</LI>
   * </UL>
   * Used in translation of locations &ampl; components.<BR><BR>
   */
  private static String mapName(String name){
    return fdw.makeNameSafe("X-flat." + name);
  }
  /**
   * Maps an (textual) <tt>invariant</tt> to a flat location.<BR>
   * Used in translation of locations and components.<BR><BR>
   * <H3>!!! Might be necessary to translate the variable names !!!</H3>
   */
  public static String mapInvariantText(String inv){
    return inv;
  }
  /**
   * Maps a  <tt>synchronisation</tt> text of a hierarchical to a flat 
   * location.<BR>
   * Used in translation of locations &ampl; components.<BR><BR>
   * <H3>!!! Might be necessary to translate the variable names !!!</H3>
   */
  public static String mapSynchronisationText(String syncText)
    throws Exception {

    return syncText;
  }
  /**
   * Maps a  <tt>assignment</tt> text of a hierarchical to a flat 
   * location.<BR>
   * Used in translation of locations &ampl; components.<BR><BR>
   * <H3>!!! Might be necessary to translate the variable names !!!</H3>
   */
  public static String mapAssignmentText(String text)
    throws Exception {

    return text;
  }
  /**
   * Maps a  <tt>guard</tt> text of a hierarchical to a flat 
   * location.<BR>
   * Used in translation of locations &ampl; components.<BR><BR>
   * <H3>!!! Might be necessary to translate the variable names !!!</H3>
   */
  public static String mapGuardText(String text)
    throws Exception {

    return text;
  }

  /**
   * Maps a  <tt>nail</tt> of a hierarchical to a flat 
   * location.<BR>
   * Used in translation of locations &ampl; components.<BR><BR>
   */
  public static Element mapNail(Element nail)
    throws Exception {
    Element res = flatDoc.createElement("nail");
    res.setAttribute("x", nail.getAttribute("x"));
    res.setAttribute("y", nail.getAttribute("y"));
    
    return res;
  }
  
  
  
  
  
  
  // ========================================
  // [2.1.1] MAPs that require some Hashtables
  // ========================================
  
  /**
   *      <B>CHECK THAT THIS IS USED CONSISTENTLY WITH INSTANTIATIONS OF
   *      ENTRIES/EXITS</B>
   */
  public static String mapSourceToID(String instantiationName,
				     Element source)
    throws Exception {

    System.out.println(instantiationName + " : " + source);
    
    if((source.getTagName()).equals("source")){
      
      String refID = source.getAttribute("ref");
      String exitrefID = source.getAttribute("exitref");
      
      return getActiveSubcomponentID(recallInstantionAndComponentIDToTextualInstantiation(instantiationName, refID));      
    }
    
    throw new Exception("ERROR: the element \n" +
			source.toString() +
			"\n      is not a <source>.");
  }
  /**
   *      <B>CHECK THAT THIS IS USED CONSISTENTLY WITH INSTANTIATIONS OF
   *      ENTRIES/EXITS</B>
   */
  public static String mapTargetToID(String instantiationName,
				     Element target)
    throws Exception {
    
    if((target.getTagName()).equals("target")){
      
      String refID = target.getAttribute("ref");
      String entryrefID = target.getAttribute("entryref");
      
      return getActiveSubcomponentID(recallInstantionAndComponentIDToTextualInstantiation(instantiationName, refID));
    }
    
    throw new Exception("ERROR: the element \n" +
			target.toString() +
			"\n      is not a <target>.");
  }
  
  /**
   * Compute the ID of the translation of an <EM>entry</EM>.<BR>
   * A transition in the flat version goes to this ID.
   */
  public static String mapEntryToID(Element entry)
    throws Exception {
    return "X-enter" + mapElementToID(entry);
  }
  
  /**
   * Compute the ID of the translation of an <EM>exit</EM>.<BR>
   * A transition in the flat version goes to this ID.
   */
  public static String mapExitToID(Element exit)
    throws Exception {
    return "X-leave" + mapElementToID(exit);
  }
  public static String mapComponentToActiveID(Element compt)
    throws Exception {
    return "X-active" + mapElementToID(compt);
  }
  
  /**
   * Get/invent the ID an component maps to<BR><BR>
   * <B>USES <TT>{@link #elementCount}</TT></B>
   */
  private static String mapElementToID(Element el)
    throws Exception {
    
    if(hashElementsToIDs.containsKey(el)){
      return (String)hashElementsToIDs.get(el);
    }
    else { // make a new entry
      String res =  componentCount + "." + DocumentReader.getElementID(el);
      hashElementsToIDs.put(el, res);
      return res;
    }
  }
  
  /**
   * The signal this entry/exit corresponds to (without !/?)<BR>
   * If the component in question is not a enty/exit, an exception 
   * is thrown.
   */
  private static String getSignal(String instantiationName,
				  Element entryOrExit)
    throws Exception {
    
    if((entryOrExit.getTagName()).equals("entry")){
      return fdw.makeSignalSafe(DocumentReader.getElementID(entryOrExit) + fdw.CHANNEL_SEPARATOR + "enter" + fdw.CHANNEL_SEPARATOR + instantiationName);
    }
    if((entryOrExit.getTagName()).equals("exit")){
      return  fdw.makeSignalSafe(DocumentReader.getElementID(entryOrExit) + fdw.CHANNEL_SEPARATOR + "leave" + fdw.CHANNEL_SEPARATOR + instantiationName);
    }
    
    
    throw new Exception("ERROR: the element \n" +
			entryOrExit.toString() +
			"\n      is neither entry nor exit.");
  }
  /**
   *      Returns a flat location that corresponds to the hierarchical 
   *      one, but
   *      <UL>
   *        <LI>name is altered</LI>
   *        <LI>IDs are mapped</LI>
   *        <LI>Coordinates are copied</LI>
   *      </UL>
   * <H2>Takes care of</H2>
   * <UL>
   *  <LI>invariants</LI>
   *  <LI>committed location propagation</LI>
   *  <LI>synchronisation</LI>
   * </UL>
   */
  private static Element transformLocationToFlat(TextualInstantiation tinst,
						 Element loc)
    throws Exception {
    Element res = lm.migrateOrdinaryLocation(loc, tinst);
    fdw.changeLocationID(res, mapID(tinst, hdr.getElementID(loc)));

    if(!fdw.inventNewGoegraphicalLocations){
      res.setAttribute("x", loc.getAttribute("x"));
      res.setAttribute("y", loc.getAttribute("y"));
    }

    NodeList childNodes = loc.getChildNodes();
    MutableNodeListImpl newChildren = new MutableNodeListImpl();

    for(int i=0; i < childNodes.getLength(); i++){
      Node node = childNodes.item(i);
      Element el = transformSimpleNode(node);
      if ( el != null )
	newChildren.addNode(el);
    }
    
    for(int i=0; i < newChildren.getLength(); i++){
      res.appendChild(newChildren.item(i));
    }
    
    // -- invariant: add inherited (if any) --------------------------------

    if( tinst.inheritedInvariant.length() > 0){
      fdw.addTextualInvariantToLocation(res, tinst.inheritedInvariant);
    }

    return res;
  }
  /**
   *      Transforms the following simple elements to a flat element
   *      <UL>
   *        <LI>name</LI>
   *        <LI>invariant</LI>
   *        <LI>urgent</LI>
   *        <LI>committed</LI>
   *      </UL>
   *      (does alter names/ids/etc. in the canonical way)<BR><BR>
   *      
   *      <H2>Will <EM>swallow</EM> (i.e. return <TT>null</TT> on the 
   * following:</H2>
   *      <UL>
   *        <LI>parameter (since if it occurs in components, THOSE are 
   * translated to locations</LI>
   *      </UL>
   *      <HR>
   *      Returns <TT>null</TT>, if the node is not an Element.<BR>
   *      Throws exception if the way to deal with it is unknown.
   */
  private static Element transformSimpleNode(Node node)
    throws Exception{
    
    if(node instanceof Element){
      Element el = (Element)node;
      String tagName = el.getTagName();
      Element res;
      
      if(hdr.isInvariantElement(node)){
	 res = fdw.createInvariant();
	 fdw.addTextualContentToElement(res, mapInvariantText(DocumentReader.getCdataOfElement(el)));
	 res.setAttribute("x", el.getAttribute("x"));
	 res.setAttribute("y", el.getAttribute("y"));
	 return res;
       }
       if(tagName.equals("urgent")){
	 res = flatDoc.createElement("urgent");
	 return res;
       }
       if(tagName.equals("committed")){
	 res = flatDoc.createElement("committed");
	 return res;
       }
       // -- Nodes to swallow -----------------------------------------
       if(tagName.equals("parameter")){
	 return null;
       }
       if(tagName.equals("name")){ // ignore !!
	 return null;
       }
       
     }
     else
       return null;
     
     throw new Exception("ERROR: do not know how to transform this element: " + node.toString());
     
   }
 
  // =================================================================
  // Registed Synchronizations (for Phase III)
  // =================================================================


  /**
   * Add synchronization and provide registration in {@see 
   * ChannelRegistry}<BR>
   * <BR>
   * Returns the synchronisation Element<BR>
   * <BR>
   *    (only necessary for original (hierarchical) synchronizations)
   */
  private static Element addRegisteredSynchronisationToTransition(
                String syncText,
		Element transition,
		TextualInstantiation tinst)
    throws Exception {
    Element result = fdw.addSynchronisationToTransition(syncText, transition);
    cr.registerSync(syncText, transition, tinst);

    return result;
  }
  /**
   * Add SEND synchronization and provide registration in {@see 
   * ChannelRegistry}<BR>
   * <BR>
   * Returns the synchronisation Element<BR>
   * <BR>
   *    (only necessary for original (hierarchical) synchronizations)<BR>
   * <BR>
   * ??? necessary ???
   */
  private static Element addRegisteredSendSynchronisationToTransition(String syncText,
							       Element transition,
							       TextualInstantiation tinst)
    throws Exception {
    return addRegisteredSynchronisationToTransition(syncText + "!", transition, tinst);
  }
  /**
   * Add RECEIVE synchronization and provide registration in {@see 
   * ChannelRegistry}<BR>
   * <BR>
   * Returns the synchronisation Element<BR>
   * <BR>
   *    (only necessary for original (hierarchical) synchronizations)
   * <BR>
   * 
   * ??? necessary ???
   */
  private static Element addRegisteredReceiveSynchronisationToTransition(String syncText,
							       Element transition,
							       TextualInstantiation tinst)
    throws Exception {
    return addRegisteredSynchronisationToTransition(syncText + "?", transition, tinst);
  }
  


  // ===================================================================
  // [3] Entry and Exit Locations (plus Joins)
  // ===================================================================
  
  /**
   * Takes the instantiation of the <EM>component</EM> as an argument
   * and returns the (flat) <TT>location-ID</TT>, that corresponds to
   * the situation, that this component is <EM>active</EM>, i.e. the
   * locations lives in the translation of the instantiation of the 
   * <B>father</B> component of this one.<BR>
   * <BR>
   * It is unique, since this version introduces new templates for 
   * everything.
   */
  private static String getActiveSubcomponentID(TextualInstantiation cpt)
    throws Exception {
    
    return DocumentReader.getElementID(lm.mapTextualInstantiationToActiveLocation(cpt));
    }
    

   // ========================================
   // [3.0.1] AUX for list handling
   // ========================================
   
  
   /**
    * Remember the connection between (original, hierarchical)
    * templates and names, accesible then in
    * <TT>{@link #hashTemplateNamesToElements}</TT><BR><BR>
    * Throws exception, if two templates have the same name
    */
   public static void memorizeTemplate(Element template)
     throws Exception {
     String name = DocumentReader.getElementName(template);
     if( hashTemplateNamesToElements.containsKey(name) )
       throw new Exception("ERROR: double template name \"" +
			   name + "\"");
     hashTemplateNamesToElements.put(name, template);
   }

   /** 
    * Remember the mapping from instantiation + componentID to the 
    * TextualInstantiaion this component corresponds to.<BR><BR>
    * 
    * Necessary for computing the target/source-ID of non-elementary 
    * transitions
    */
    public static void memorizeInstantionAndComponentIDToTextualInstantiation(String instantiationName, String componentID, TextualInstantiation tinst){

	hashInstComponentToTInst.put(instantiationName + ".$$." + componentID,
				     tinst);
    }
 
    /**
     * Counterpart to <TT>{@link 
     * #memorizeInstantionAndComponentIDToTextualInstantiation}</TT>.<BR>
     * Maps back to the TextualInstantiation
     */
    public static TextualInstantiation recallInstantionAndComponentIDToTextualInstantiation(String instantiationName, String componentID){
	return (TextualInstantiation)hashInstComponentToTInst.get(instantiationName + ".$$." + componentID);
    }

 
  // ===========================================================
  // [3.1] Helpers: Document traversal
  // ===========================================================
  
  private static void traverseNode(Node n,int o){
    
    int type = n.getNodeType();
    int nextOffset = o + factor;
    
    switch(type){
    case Node.ELEMENT_NODE : {
      NamedNodeMap atts = n.getAttributes();
      if ((atts == null) ||
	  (atts.getLength() == 0)){
	System.out.print(blanks.substring(0,o) +
			 "<" + n.getNodeName() + ">");
	System.out.println(); // ??
	nextOffset = o + (n.getNodeName()).length();
      }
      else { // -- show attributes
	System.out.println(blanks.substring(0,o) +
			   "<" + n.getNodeName());
	Node a;
	for(int i = 0; i < atts.getLength(); i++){
	  a = atts.item(i);
	  System.out.println(blanks.substring(0,o + attIndent) +
			     a.getNodeName() + 
			     " = \"" +
			     a.getNodeValue() +
			     "\"");
	}
	System.out.println(blanks.substring(0,o + attIndent) +
			   ">");
	nextOffset = o + attIndent + 1;
      }
      NodeList children = n.getChildNodes();
      for(int i = 0; i < children.getLength(); i++){
	traverseNode(children.item(i),nextOffset);
      }
      System.out.println(blanks.substring(0,o) +
			 "</" + n.getNodeName() + ">");
      break;}
    case Node.TEXT_NODE : {
      if(n.getNodeValue().trim().length() >0)
	System.out.println(blanks.substring(0,nextOffset) +
			   n.getNodeValue().trim());
      break;
      
    }
    case Node.CDATA_SECTION_NODE : {
      System.out.println(blanks.substring(0,nextOffset) +
			 "[CDATA SECION]");
      break;
    }
    case Node.PROCESSING_INSTRUCTION_NODE : {
      System.out.println(blanks.substring(0,nextOffset) +
			 "[PROCESSING INSTRUCTION]");
      break;
    }
    case Node.ENTITY_REFERENCE_NODE : {
      System.out.println(blanks.substring(0,nextOffset) +
			 "&" + n.getNodeName() + ";");
      break;
    }
    case Node.COMMENT_NODE : {
      System.out.println(//blanks.substring(0,nextOffset) +
			 "<!-- " + n.getNodeValue() + 
			 "-->");
      break;
    }
    default: {
      System.out.println("Unknown NODE TYPE: Nr." + type);}
    }
  }
  
  /**
   * Traverse and spam...
   */
  private static void traverse(Document doc,String root)
    throws Exception {
    NodeList nl = doc.getElementsByTagName(root);
    if (nl.getLength() != 1){
      throw new Exception("Error: " + nl.getLength() + " tags named " +
			  root);
    }
    traverseNode(nl.item(0),0);
    System.out.println("**** Traversal done.");
  }

}
