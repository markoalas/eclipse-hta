// -*- mode: JDE; c-basic-offset: 2; -*-
// /////////////////////////////////////////////////////////////
// Create some XMl documents
// 
// Synopsis:
//  Huppaal: Flattening HTAs
// /////////////////////////////////////////////////////////////
// @TABLE OF CONTENTS:		       [TOCD: 10:15 13 Feb 2001]
//
//      [0.1] Other AUX    
//      [0.2] Counters
//      [0.3] Global Elements
//          [0.3.1] AUX for fresh names
//      [0.4] Weeding Out Illegal Characters
//      [0.5] DEFAULT LAYOUT
//      [0.6] AUX for adding attributes
//      [0.7] Creation of Elements
// ==========================================================
// @FILE:    DocumentWriter.java
// @PLACE:   BRICS AArhus; host:newton
// @FORMAT:  java
// @AUTHOR:  M. Oliver M'o'ller     <omoeller@brics.dk>
// @BEGUN:   Tue Nov 21 17:39:23 2000
// @VERSION: Vanilla-1                  Mon Mar 19 13:12:52 2001
// /////////////////////////////////////////////////////////////
// 

import DocumentReader;

import java.lang.*;

import java.util.Vector;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

//**** from other packages 
import org.apache.crimson.tree.XmlDocument;
import org.apache.crimson.tree.XmlDocumentBuilder;
//****************************************

/**
 * <H1>Constructor Class</H1>
 * 
 * This class serves as a wrapper do construct XML documents.<BR>
 * <BR>
 * Though a lot of variations of this theme exist, these are the ones 
 * specific to Uppaal Documents.<BR>
 * <H2>This class contains</H2>
 * <UL>
 *  <LI>Separator Chars</LI>
 *  <LI>Graphical Layout Routines</LI>
 *  <LI>Fresh Name Mechanisms</LI>
 *  <LI>Auxillary Mechnisms for adding attributes to elements</LI>
 * </UL>
 * <BR>
 * It inherits the general accessors from {@link DocumentReader}.
 * 
 * @see DocumentReader
 * @author <A HREF="MAILTO:omoeller@brics.dk?subject=DocumentWriter.java%20(Vanilla-1%20Mon%20Mar%2019%2013:12:15%202001)">M. Oliver M&ouml;ller</A>
 * @version Vanilla-1                  Mon Mar 19 13:12:52 2001
 */
public class DocumentWriter 
  extends DocumentReader {

  // //////////////////////////////////////////////////////////////////////
  // ////////////////////////////// FIELDS ////////////////////////////////
  // //////////////////////////////////////////////////////////////////////

  /**
   * Separator sequence used in Uppaal channel names
   */
  static final String CHANNEL_SEPARATOR = "X";

  
  /**
   * Separator that can be used everywhere
   */
  static final char SAFE_SEPARATOR_CHAR = 'I';

  /**
   * Separator that can be used in &lt;name&gt; elements
   */
  static final char NAME_SEPARATOR_CHAR = '_';

  // ==================================================
  // [0.1] Other AUX    
  // ==================================================
 
  
  /**
   * Spam out debuggin information, if <TT>debug</TT> is true
   */
  public boolean debug = true;
  

  /**
   * Do (possibly time-consuming) sanity checks, if true
   */
  static boolean sanityChecks = true;

 
  
  // ===============================================
  // [0.2] Counters
  // ===============================================

  /**
   * Counter to make introduced location names unique
   */
  static long locationNameCount = 0L;
  
  /**
   * Counter to make introduced channel names unique
   */
  static long synchronisationNameCount = 0L;
  
  /**
   * Counter to make introduced ID names unique
   */
  static long idNameCount = 0L;
  
  
  /**
   * Counter to make introduced template names unique
   */
  static long templateNameCount = 0L;

  // ===============================================
  // [0.3] Global Elements
  // ===============================================

  /**
   * XML document where the written version is constructed
   */
  protected XmlDocument doc;

  // --  Parameters for layout ------------------------
  
  
  /**
   * x-Distance from upper left corner
   */
  public long globalGeoXOffset = 40L;
  /**
   * y-Distance from upper left corner
   */
  public long globalGeoYOffset = 40L;

  public long geoXOffset = 500L;
  public long geoYOffset = 500L;


  public static long locNameXOffset = 12L;
  public static long locNameYOffset = 12L;

  /**
   * Parameter for default layout computatation
   */
  public long geoCount = 0L;

  // --------------------------------------------------
 
  // //////////////////////////////////////////////////////////////////////
  // //////////////////////////  CONSTRUCTORS  ////////////////////////////
  // //////////////////////////////////////////////////////////////////////

  /**
   * Default Constructor:
   * Creates the (global) document
   */
  public DocumentWriter(){
    super(null);

    XmlDocumentBuilder xmlDocBuilder = new XmlDocumentBuilder();
    doc = xmlDocBuilder.createDocument();
    origDoc = doc;
  }

  // //////////////////////////////////////////////////////////////////////
  // ///////////////////////////// METHODS  ///////////////////////////////
  // //////////////////////////////////////////////////////////////////////


  // ========================================
  // [0.3.1] AUX for fresh names
  // ========================================
  
  /**
   * Return a new, unique template name
   */
  protected static String freshTemplateName(){
    templateNameCount++;
    return ".TMP." + templateNameCount;
  }
  /**
   * Return a new, unique location name
   */
  protected static String freshLocationName()
    throws Exception {
    locationNameCount++;
    
    return "L." + locationNameCount;
  }
  /**
   * Return a new, unique channel name (without !/?)
   */
  protected static String freshSynchronisationName(){
    synchronisationNameCount++;
    return "sync" + CHANNEL_SEPARATOR + synchronisationNameCount;
  }
  /**
   * Return a unique ID string<BR>
   * <H3>!! might enter the ID in some list at some point !!</H3>
   */
  protected static String inventFreshID(){
    idNameCount++;
    return makeIDSafe("X-ID-" + idNameCount);
  }
  
  // ===============================================
  // [0.4] Weeding Out Illegal Characters
  // ===============================================
  

  /**
   * Transform the name (cdata content of a <name> Element) to a safe 
   * version, i.e. one that is accepted by Uppaal
   */
  public static String makeNameSafe(String name){
    String res = name.replace('-', NAME_SEPARATOR_CHAR);
    res = res.replace('_', NAME_SEPARATOR_CHAR);
    res = res.replace('.', NAME_SEPARATOR_CHAR);
    res = res.replace(' ', NAME_SEPARATOR_CHAR);
    

    // AUX to restrict name length
    StringBuffer buf = new StringBuffer();
    for( int i = 0; i < res.length(); i++){
      char c = res.charAt(i);
      if( (c != 'X' ) &&
	  (c != 'X' ) &&
	  (c != 'X' ) &&
	  (c != 'X' ) &&
	  (c != '_' ))
	buf.append(c);
    }
    res = buf.toString();

    if(res.charAt(0) == '_')
      res = "X" + res;

    return res;
  }
  /**
   * Transform the signal (i.e. a channel name) to a safe 
   * version, i.e. one that is accepted by Uppaal
   */
  public static String makeSignalSafe(String name){
    String res = name.replace('-', NAME_SEPARATOR_CHAR);
    res = res.replace('_', NAME_SEPARATOR_CHAR);
    res = res.replace('.', NAME_SEPARATOR_CHAR);
    res = res.replace(' ', NAME_SEPARATOR_CHAR);

    // AUX to restrict signal length
    StringBuffer buf = new StringBuffer();
    for( int i = 0; i < res.length(); i++){
      char c = res.charAt(i);
      if( (c != 'a' ) &&
	  (c != 'e' ) &&
	  (c != 'i' ) &&
	  (c != 'o' ) &&
	  (c != 'X' ) &&
	  (c != 'n' ) &&
	  (c != '_' ) &&
	  (c != 'u' ))
	buf.append(c);
    }
    res = buf.toString();


    if(res.charAt(0) == '_')
      res = "X" + res;

    return res;
  }
  
  /**
   * Transform the ID (Attribute value) to a safe 
   * version, i.e. one that is accepted by Uppaal
   */
  public static String makeIDSafe(String name){
    String res = name.replace('-', NAME_SEPARATOR_CHAR);

    return res;
  }
  


  // ===============================================
  // [0.5] DEFAULT LAYOUT
  // ===============================================   

  /**
   * Start coordinate-counting anew
   */
  protected void resetDefaultLayout(){
    geoCount = 0L;
  }
  protected void addDefaultLocationCoordinates(Element location)
    throws Exception {
    if(sanityChecks){
      if(!((location.getTagName()).equals("location")))
	throw new Exception("ERROR: tried to add location coordinates to non-location\n" +
			    location.toString());
    }

    long diag = 0L;
    long size = 1L; // size of THIS diagonal
    long count = 0L;
    while(geoCount >= (size + count)){
      diag++;
      count = count + size;
      size++; 
    }
    long position = geoCount - count;
    long x = (diag - position) * geoXOffset;
    long y = position          * geoYOffset;

    String xCoordinate = "" + (globalGeoXOffset + x);
    String yCoordinate = "" + (globalGeoYOffset + y);

    location.setAttribute("x", xCoordinate);
    location.setAttribute("y", yCoordinate);

    geoCount++;
  }


  public void testGeoCordinates(long upto){
    long ii = 0L;
    Element el = doc.createElement("location");
    
    resetDefaultLayout();
    
    while( ii < upto ){
      try {
	addDefaultLocationCoordinates(el);
      }
      catch (Exception e) {
	System.out.println("Oooops, something went wrong...");
	e.printStackTrace();
	System.exit(1);
      }
      System.out.println(ii + ".\t\t" +
			 el.getAttribute("x") + "\t\t" +
			 el.getAttribute("y"));
      
			 
      
      ii++;
    }
  }
  // -----------------------------------------------------------------------
  // ========================================    
  // [0.6] AUX for adding attributes
  // ========================================  
  
  /**
   * Set x and y to some value.<BR>
   * <H3>!! Can be the default layout at some point !!</H3>
   * (Currently, everything is "0")
   */
  protected static void addCoordinatesToElement(Element el){
    el.setAttribute("x","0");
    el.setAttribute("y","0");
  }
  /**
   * Set x and y to specified values.<BR>
   */
  protected static void addCoordinatesToElement(Element el, long x, long y){
    el.setAttribute("x","" + x);
    el.setAttribute("y","" + y);
  }
  /**
   * Invent a fresh unique ID for this element.<BR>
   * <H3>!! Could take an optional argument, hinting at the origin</H3>
    */
  protected static void addIDToElement(Element el){
    el.setAttribute("id",inventFreshID());
   }
  /**
   * Insert a child node containing a supplided name<BR>
   *      by default, add coordinates next to el; can be changed 
   * afterwards if required.<BR>
   * <BR>
   * <B>Relies on the fact, that a name element is always the first child.</B><BR>
   * <BR>
   * Returns name Element.
   */       
  protected Element addNameToElement(Element el, String name){
    Element nameNode = createNameElement(name);
    
    long x = 0L;
    long y = 0L;
    
    try {
       x = max(Long.parseLong(el.getAttribute("x")) + locNameXOffset,
	       0L);
       y = max(Long.parseLong(el.getAttribute("y")) + locNameYOffset,
	       0L);
    } catch (Exception e) {
      // on fail: assume UNSET coordinates, i.e. set to (0,0)
      x = 0L;
      y = 0L;
    }
    
    nameNode.setAttribute("x", "" + x);
    nameNode.setAttribute("y", "" + y);
    
    Node first = el.getFirstChild();
    if(null == first)
      el.appendChild(nameNode);
    else
      el.insertBefore(nameNode, first);

    return nameNode;
  }
  
  
  /**
   * Add textual content to an Element
   */
  protected  void addTextualContentToElement(Element el, String text){
    Text cdata = doc.createTextNode(text);
    el.appendChild(cdata);
  }
  

  /**
   * Append comment text as last child of a Node
   */
  public void appendComment(Node node, String text){
    node.appendChild(doc.createComment(text));
  }


  // ==================================================
  // [0.7] Creation of Elements
  // ==================================================
   /**
   * Returns a Element that is a name tag with specified content<BR>
   * <H3>!! Could contain optional argument for placement at some point 
   * !!</H3>
   */     
  private Element createNameElement(String name){
    Element res = doc.createElement("name");
    addCoordinatesToElement(res);
    addTextualContentToElement(res, makeNameSafe(name));
     
    return res;
  }

}
