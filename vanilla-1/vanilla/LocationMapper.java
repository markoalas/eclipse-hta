// -*- mode: JDE; c-basic-offset: 2; -*-
// /////////////////////////////////////////////////////////////
// Servics Class to retrieve translations of hierarchical
// locations
//
// Synopsis:
//  Huppaal
// /////////////////////////////////////////////////////////////
// @FILE:    LocationMapper.java
// @PLACE:   BRICS AArhus; host:harald
// @FORMAT:  java
// @AUTHOR:  M. Oliver M'o'ller     <omoeller@brics.dk>
// @BEGUN:   Wed Jan 31 11:52:28 2001
// @VERSION: Vanilla-1                  Mon Apr  9 10:04:40 2001
// /////////////////////////////////////////////////////////////
// 

import java.lang.*;

import java.util.Hashtable;

import org.w3c.dom.Element;

//**** from other packages 

//****************************************

/**
 * <H2>Service Class</H2>
 * This class enables a mapping of hierarchical locations or components 
 * (i.e. their instantition) to flat ones.<BR>
 * <BR>
 * This mapping can be retrieved lateron, which is neccessary for the 
 * translation
 * of global joins.
 * 
 * @see GlobalJoin
 * @author <A HREF="MAILTO:omoeller@brics.dk?subject=LocationMapper.java%20(Vanilla-1%20Mon%20Apr%202%2012:18:56%202001)">M. Oliver M&ouml;ller</A>
 * @version Vanilla-1                  Mon Apr  9 10:04:40 2001
 */
public class LocationMapper  {

  // //////////////////////////////////////////////////////////////////////
  // ////////////////////////////// FIELDS ////////////////////////////////
  // //////////////////////////////////////////////////////////////////////

  /**
   * Relevant reader (necessary to follow ID references)
   */
  private HierarchicalDocumentReader hdr;

  /**
   * Relevant writer (necessary to create object)
   */
  private FlatDocumentWriter fdw;
  

  /**
   * Maps (basic) hierarchical locations to another hashtable, that
   * maps textual instantiations to Elements, namely the Element this
   * particular instantion of a location was translated to.<BR>
   * 
   * @see GlobalJoin
   */
  private Hashtable hashHierarchicalLocationsToHashtable;


  /**
   * Maps textual instantions to their <EM>active</EM> locations.<BR>
   * <BR>
   * In Vanilla-1, there is only one unique active location per AND
   * Component.
   * 
   * @see GlobalJoin
   */
  private Hashtable hashTextualInstantitionsToActiveLocation;
  

  /**
   * Maps textual instantions to their <EM>idle</EM> locations.<BR>
   * <BR>
   * In Vanilla-1, there is only one unique active location per 
   * Component.
   * 
   * @see GlobalJoin
   */
  private Hashtable hashTextualInstantitionsToIdleLocation;

  /**
   * Separates IDs of element and contiains characters that are disallowed 
   * for
   * XML IDs, thus no concatations of different IDs with this separator can 
   * give a clash
   */
  private static final String unambigousIDSeparator = "_.-ö-._";

  /**
   * Maps concatentations of separators to another hashtable, that maps 
   * TextualInstantiations to (flat) locations.<BR>
   * Used to map entries of hierarchical components.
   */
  private Hashtable hashIDConcatenationsToFlatLocations;
  
  /**
   * Spam out debuggin information, if <TT>debug</TT> is true
   */
  static boolean debug = true;

  /**
   * Do (possibly time-consuming) sanity checks, if true
   */
  static boolean sanityChecks = true;


  /// DEBUGGING
  static boolean allEntriesAreCreated = false;
  
  // //////////////////////////////////////////////////////////////////////
  // //////////////////////////  CONSTRUCTORS  ////////////////////////////
  // //////////////////////////////////////////////////////////////////////

  /**
   * Default Constructor
   */
  public LocationMapper(HierarchicalDocumentReader theHdr,
			FlatDocumentWriter theFdw){
    hdr = theHdr;
    fdw = theFdw;
    
    hashHierarchicalLocationsToHashtable = new Hashtable();
    hashTextualInstantitionsToActiveLocation = new Hashtable();
    hashTextualInstantitionsToIdleLocation = new Hashtable();
    hashIDConcatenationsToFlatLocations = new Hashtable();
    
  }

  // //////////////////////////////////////////////////////////////////////
  // ///////////////////////////// METHODS  ///////////////////////////////
  // //////////////////////////////////////////////////////////////////////

  /**
   * Take an ordinary location and return a flattened one.<BR>
   * <BR>
   * Does <B>NOT</B> migrate all the children (this is job of Flatten, in 
   * particular of the methos <TT>transformLocationToFlat</TT>) and 
   * the <TT>ID</TT> is up to a change later.<BR><BR>
   * 
   * However, it records a mapping <BR>
   * <TT>{Location, Component} * TextualInstantiation -> 
   * Location</TT><BR><BR>
   * 
   * that is needed in the translations of global joins.
   * <BR>
   * <BR>
   * In the case, there is a component, it maps to the flat location, 
   * in the father of the instantiation of this component, that 
   * corresponds to the situation that the component is active.
   * 
   * @see GlobalJoin
   * @see Flatten
   * 
   * 
   */
  public Element migrateOrdinaryLocation(Element loc, 
					 TextualInstantiation tinst)
    throws Exception {
    Element res = fdw.inventLocation(DocumentReader.getElementName(loc));
    
    Hashtable theHashTable;
    Object hashed = hashHierarchicalLocationsToHashtable.get(loc);
    if(null == hashed){ // -- create new hashtable entry --------
      theHashTable = new Hashtable();
      hashHierarchicalLocationsToHashtable.put(loc, theHashTable);
    }
    else { // -- use existing one -------------------------------
      theHashTable = (Hashtable)hashed;
    }
    theHashTable.put(tinst, res);
      
    if(debug)
      System.out.println("``` NEW migrate Hashtable entry:\n\t" +
			 loc.toString() + "\n\t" +
			 tinst.toString());
    
    return res;
  }

  /**
   * <H2>for instantiations of AND templates</H2>
   * Retrieve the location Element (in some template, that is instantiatied
   * exactly once), that corresponds to the instantiated component to be 
   * active.<BR>
   * <BR>
   * If it does not exist yet, invent it.<BR>
   * <BR>
   * If not called on an AND instantiation, an exception is thrown.
   */
  public Element mapTextualInstantiationToActiveLocation(TextualInstantiation tinst)
    throws Exception {
    
    if (!tinst.isANDComponent) 
      throw new Exception("ERROR: only AND components have active locations.\n     " +
			  tinst.toString() + "\nis instantiation of an XOR template.");

    Element res;

    Object hashed = hashTextualInstantitionsToActiveLocation.get(tinst);
    if(null == hashed){
      res = fdw.inventLocationInTemplate("ACTIVE", tinst.translationOfTemplateElement);
      hashTextualInstantitionsToActiveLocation.put(tinst, res);
    } 
    else
      res = (Element)hashed;

    return res;
  }

  /**
   * <H2>for instantiations of XOR/AND templates</H2>
   * Retrieve the location Element (in some template, that is instantiatied
   * exactly once), that corresponds to the instantiated component to be 
   * idle.<BR><BR>
   * <BR>
   * If it does not exist yet, invent it.<BR>
   * <BR>
   */
  public Element mapTextualInstantiationToIdleLocation(TextualInstantiation tinst)
    throws Exception {

    Element res;

    Object hashed = hashTextualInstantitionsToIdleLocation.get(tinst);
    if(null == hashed){
      res = fdw.inventLocationInTemplate("IDLE", tinst.translationOfTemplateElement );
      hashTextualInstantitionsToIdleLocation.put(tinst, res);
    } 
    else
      res = (Element)hashed;

    return res;
  }
  

/**
 * Computes the mapping<BR>
 * <BR>
 * 
 * <TT>{Locations, Components} * TextualInstantiation -> 
 * Location</TT><BR><BR>
 * 
 * <BR>
 * 
 * Throws an exception, if there is no entry.
 */
  public Element mapHierachicalLocationToFlatOne(Element loc,
						 TextualInstantiation tinst)
    throws Exception {
    
    Object hashed = hashHierarchicalLocationsToHashtable.get(loc);
    if(null == hashed)
      throw new Exception("ERROR: no hash entry for " +
			  loc.toString() +
			  "in hashHierarchicalLocationsToHashtable.");
    Hashtable tinstToElement = (Hashtable)hashed;
    
    Object result = tinstToElement.get(tinst);
    if(null == result)
      throw new Exception("ERROR: no hash entry for pair \n" +
			  "\t( " + loc.toString() +
			  ",\t " + tinst.toString() + ")\n" +
			  "in LocationMapper.");

    return (Element)result;
  }
  /**
   * Map a (hierarchical) target to the flat location it points to.<BR>
   * (If it does not exist yet, invent a new (flat) location for it).<BR>
   * <BR>
   * If the target point to a basic location, it behaves like
   * @{link #mapHierachicalLocationToFlatOne}.<BR>
   * <BR>
   * Calls <TT>{@link mapTargetIDEntryrefToFlatLocation}</TT> to compute 
   * unique location.<BR> 
   * 
   * <B>NOTE</B>This does depend on the <EM>identity</EM> of the target,
   * but only on the attributes.
   */
  public Element mapTargetToFlatLocation(Element target, TextualInstantiation tinst)
    throws Exception {
    if( sanityChecks &&
	!(target.getTagName()).equals("target"))
      throw new Exception("ERROR: the following Element is not a <target>:\n" +
			  target.toString());
    
    String ref = target.getAttribute("ref").trim();
    String entryref = target.getAttribute("entryref").trim();

    return mapTargetIDEntryrefToFlatLocation(ref, entryref, tinst);
  }

  /**
   * Map ID of Element a connection points to and (possibly) the ID of the
   *    entry to a coresponding flat location.<BR>
   * (If it does not exist yet, invent a new (flat) location for it).<BR>
   * <BR>
   * If the target point to a basic location, it behaves like
   * @{link #mapHierachicalLocationToFlatOne}.<BR>
   * <BR>
   */
  public Element mapTargetIDEntryrefToFlatLocation(String elementID, 
						   String entryID, 
						   TextualInstantiation tinst)
    throws Exception {
    
    if(debug)
      System.out.println("********************************************************************************************************************************************************************************************************" + 
			 elementID + "//" + entryID + "//" + tinst.toString() );

    if(entryID.equals(""))
      return mapHierachicalLocationToFlatOne(hdr.getElementByID(elementID), tinst);

    String hashString = elementID + unambigousIDSeparator + entryID;

    Object hashed = hashIDConcatenationsToFlatLocations.get(hashString);

    if(null == hashed){

      // -- DEBUG ----------------------------------------------------------
      if(debug && allEntriesAreCreated)
	throw new Exception("ERROR! This target points to an entry, that does not exist but should:\n" + elementID + "/" + entryID);
      // -------------------------------------------------------------------

      Hashtable newHashTable = new Hashtable();
      hashIDConcatenationsToFlatLocations.put(hashString, newHashTable);
      
      Element result = fdw.inventLocationInTemplate(tinst.translationOfTemplateElement);    
      newHashTable.put(tinst, result);

	if(debug)
	  System.out.println("((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((( LocationMapper: New Creation on " + hashString);

      
      return result;
    }
    else {
      Object result = ((Hashtable)hashed).get(tinst);
      if(null == result){

	// -- DEBUG --------------------------------------------------------
	if(debug && allEntriesAreCreated)
	  throw new Exception("ERROR! This target points to an entry, that does not exist but should:\n" + elementID + "/" + entryID);
	// -----------------------------------------------------------------

	if(debug)
	  System.out.println("((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((( LocationMapper: New Creation on " + hashString);

	result = fdw.inventLocationInTemplate(tinst.translationOfTemplateElement);
	((Hashtable)hashed).put(tinst, result);
      }
      return (Element)result;
    }
  }
}



