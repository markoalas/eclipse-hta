// -*- mode: JDE; c-basic-offset: 2; -*-
// /////////////////////////////////////////////////////////////
// Channel Manager
//
//  for Phase III of translation
// 
// Synopsis:
//  Huppaal
// /////////////////////////////////////////////////////////////
// @FILE:    ChannelRegistry.java
// @PLACE:   BRICS AArhus; host:harald
// @FORMAT:  java
// @AUTHOR:  M. Oliver M'o'ller     <omoeller@brics.dk>
// @BEGUN:   Mon Apr  9 09:59:25 2001
// @VERSION:                  Vanilla-1 Mon Apr  9 13:44:10 2001
// /////////////////////////////////////////////////////////////
// 

import java.lang.*;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Stack;


import org.w3c.dom.Element;

//**** from other packages 

//****************************************

/**
 * This class stores information about cannel communication, that is needed
 * for  <TT>Phase III</TT> of translation, i.e., duplication of channels if 
 * necessary. <BR>
 * <BR>
 * Conventionally, 
 * <UL>
 *  <LI> a <TT>sync</TT> is always the channel name <EM>plus</EM>
 * <TT>!</TT> or <TT>?</TT></LI>
 *  <LI> a <TT>chan</TT> is only the channel name
 * </UL>
 * 
 * 
 * @author <A HREF="MAILTO:omoeller@brics.dk?subject=ChannelRegistry.java%20(Vanilla-1%20Mon%20Apr%209%2011:43:58%202001)">M. Oliver M&ouml;ller</A>
 * @version Vanilla-1 Mon Apr  9 13:44:10 2001
 */
public class ChannelRegistry  {

  // //////////////////////////////////////////////////////////////////////
  // ////////////////////////////// FIELDS ////////////////////////////////
  // //////////////////////////////////////////////////////////////////////

  /**
   * (completed) tree of textual instantiations<BR>
   * Needed to compute father/child replation
   */
  private InstantiationTree rootOfInstTree;

  /**
   * Maps Textual instantiations to hashtables, that contain
   * the syncs occuring in <EM>this</EM> particular tinst as keys
   * and Vectors of transitions as elements
   */
  private Hashtable mapTinstToHashtable;


  /**
   * Stack of synchronisations that might cause conflicts.<BR>
   * Use method <TT>{@link #addConflict}</TT> to access it.
   */
  protected Stack possibleConflicts;

  // ===================================
  // AUX
  // ===================================

  /**
   * Postfix for channel copies
   * <B>NOTE:</B>!!! Should contain some chars that the user cannot use !!!
   */

  private static final String uniquePostfix = "XCR";

  /**
   * Counter used for @{link newPostfix}
   */
  private static long channelCopyCounter = 0L;

  // //////////////////////////////////////////////////////////////////////
  // //////////////////////////  CONSTRUCTORS  ////////////////////////////
  // //////////////////////////////////////////////////////////////////////

  /**
   * Default Constructor: Needs references
   */
  public ChannelRegistry(InstantiationTree theInstTree){
    rootOfInstTree = theInstTree;

    mapTinstToHashtable = new Hashtable();
    possibleConflicts = new Stack();
  }

  // //////////////////////////////////////////////////////////////////////
  // ///////////////////////////// METHODS  ///////////////////////////////
  // //////////////////////////////////////////////////////////////////////

  /**
   * Checks, whether there exist an entry for <TT>sync</TT> in the
   * textual instantiation or in any descendand of it
   */
  public boolean occursInOrBelow(String sync, TextualInstantiation tinst){

    if (occursIn(sync, tinst))
      return true;
    
    for(Enumeration e = (rootOfInstTree.findNodeWithContent(tinst)).enumChildren(); e.hasMoreElements(); ){
      
      if(occursIn(sync, ((InstantiationTree)e.nextElement()).content))
	return true;
    }
    
    return false;
  }

  /**
   * Regiser a sync occuring on a specific transition 
   * in a specific texual instantiation<BR>
   * <BR>
   * (I.e., create appropriate hashtable entries)
   */
  public void registerSync(String sync, Element transition, TextualInstantiation tinst){
    Object hashed = mapTinstToHashtable.get(tinst);
    if(null == hashed){
      Hashtable newHash = new Hashtable();
      Vector transitions = new Vector();
      transitions.addElement(transition);
      newHash.put(sync, transitions);
      mapTinstToHashtable.put(tinst, newHash);
    }
    else {
      Object transitions = ((Hashtable)hashed).get(sync);
      if(null == transitions){
	Vector newVec = new Vector();
	newVec.addElement(transition);
	((Hashtable)hashed).put(sync, newVec);
      }
      else 
	((Vector)transitions).addElement(transition);
    }
  }
  /**
   * <B>UN</B>Regiser a sync occuring on a specific transition 
   * in a specific texual instantiation, i.e., remove from database
   * <BR>
   * (Needed if the synchronization changes)
   *<BR>
   * Throws exception, if there was no registration entry.
   */
  public void unregisterSync(String sync, Element transition, TextualInstantiation tinst)
    throws Exception {

    Object hashed = mapTinstToHashtable.get(tinst);
    if(null == hashed)
      throw new Exception("ERROR: trying to unregister " + sync + ", " + transition.toString() + ", " + tinst.toString() + "\n -- no registration entry found." );
    
    Object transitions = ((Hashtable)hashed).get(sync);
    if(null == transitions)
      throw new Exception("ERROR: trying to unregister " + sync + ", " + transition.toString() + ", " + tinst.toString() + "\n -- no registration entry found." );
    
    Vector theVector = ((Vector)transitions);
    
    if(!theVector.removeElement(transition))
      throw new Exception("ERROR: trying to unregister " + sync + ", " + transition.toString() + ", " + tinst.toString() + "\n -- no registration entry found." );
  }
  
   
  /**
   * Returns an enumeration of <EM>all</EM> (flat) transtions, that carry 
   * the 
   * specified sync and reside outside the specific tinst.<BR>
   * (possibly the empty enumeration).<BR>
   * <BR>
   * Only works properly, if all transitions were <EM>registered</EM> with  
   * <TT>{@link #registerSync}</TT>.
   */
  public Enumeration enumTransitionsWithSyncOusideTinst(String sync, TextualInstantiation tinst){
    Vector result = new Vector();
    for(Enumeration e = enumTinstsOutsideTinst(tinst); e.hasMoreElements();){
      result.addAll(allTransitionsWithSyncInTinst(sync,
						  (TextualInstantiation)e.nextElement()));
    }
    
    return result.elements();
  }
  
  /**
   * Adds the synchronization from a global join, that might cause a 
   * conflict.<BR>
   * <BR>
   * Note that the TextualInstantiaton handed over is
   * <EM>not</EM> where the transition lives in, but the tinst of the 
   * component, where it originates from.
   */
  public void addConflict(String sync, 
			  Element transition,
			  TextualInstantiation tinst){
    possibleConflicts.push(new SyncTransTinstTriple(sync, transition, tinst));
  }
  
  /**
   * Go through the conflicts;<BR>
   * <BR>
   * If there was a conflict entry for <TT>oldSync</TT>,
   * add a new conflict entry for  <TT>newSync</TT>
   * <BR>
   * <BR>
   * (Necessary, see ChannelManager Handnotes)
   */
  public void addConflictsForNewSync(String oldSync,
				     String newSync){
    for(Enumeration e = possibleConflicts.elements(); e.hasMoreElements(); ){
      SyncTransTinstTriple sttt = (SyncTransTinstTriple)e.nextElement();
      if(oldSync.equals(sttt.sync))
	possibleConflicts.push(new
	  SyncTransTinstTriple(newSync, sttt.trans, sttt.tinst));
    }
  }

  // =================================================================
  // Help Services
  // =================================================================

  /**
   * Return matching sync (handshake)<BR>
   * Throw Exception, if the input is not a <TT>sync</TT>.
   */
  public static String matchingSync(String sync)
    throws Exception {
    int firstShriek = sync.indexOf('!');
    int firstQuery  = sync.indexOf('?');
    if( ( (firstShriek < 0) && (firstQuery < 0)) ||
	( (firstShriek >= 0) && (firstQuery >= 0)) ||
	( max(firstShriek, firstQuery) != (sync.length() -1) ) )
      throw new Exception("ERROR: not a valid sync: >>" + sync + "<<");

    if(firstQuery > firstShriek)
      return sync.substring(0, sync.length() -1) + "!";
    else
      return sync.substring(0, sync.length() -1) + "?";
  }
  /**
   * Return channel of sync (handshake)<BR>
   * Throw Exception, if the input is not a <TT>sync</TT>.
   */
  public static String chanOfSync(String sync)
    throws Exception {
    int firstShriek = sync.indexOf('!');
    int firstQuery  = sync.indexOf('?');
    if( ( (firstShriek < 0) && (firstQuery < 0)) ||
	( (firstShriek >= 0) && (firstQuery >= 0)) ||
	( max(firstShriek, firstQuery) != (sync.length() -1) ) )
      throw new Exception("ERROR: not a valid sync: >>" + sync + "<<");
    
    return sync.substring(0, sync.length() -1);
  }

  /**
   * Return 'type' of sync (handshake), i.e. the string "!" or "?"<BR>
   * Throw Exception, if the input is not a <TT>sync</TT>.
   */
  public static String typeOfSync(String sync)
    throws Exception {
    int firstShriek = sync.indexOf('!');
    int firstQuery  = sync.indexOf('?');
    if( ( (firstShriek < 0) && (firstQuery < 0)) ||
	( (firstShriek >= 0) && (firstQuery >= 0)) ||
	( max(firstShriek, firstQuery) != (sync.length() -1) ) )
      throw new Exception("ERROR: not a valid sync: >>" + sync + "<<");
    
    return sync.substring(sync.length() -1);
  }
  

  /**
   * MAXimum of two integers.
   */
  public static int max(int a, int b){
    if(a > b)
      return a;
    else
      return b;
  }

  /**
   * New (uniquifying) postfix to keep track of channel copies
   */
  public static String newPostfix(){
    channelCopyCounter++;
    return uniquePostfix + channelCopyCounter;
  }
  
  // =================================================================
  // Auxillary Methods (internal)
  // =================================================================
  
  /**
   * Check Hashtable for entry
   */
  private boolean occursIn(String sync, TextualInstantiation tinst){

    Object hashed = mapTinstToHashtable.get(tinst);

    if(null == hashed)
      return false;
    else 
      return ((Hashtable)hashed).containsKey(sync);
  }

  /**
   * Enumerate textual Instantiations (including root) outside a specific 
   * one,
   * i.e., above and incomparable to it.
   */
  private Enumeration enumTinstsOutsideTinst(TextualInstantiation tinst){
    Stack treeNodes = new Stack();
    treeNodes.push(rootOfInstTree);
    Vector result = new Vector();

    while(!treeNodes.empty()){
      InstantiationTree it = (InstantiationTree)treeNodes.pop();
      if(tinst != it.content){
	result.addElement(it.content);
	treeNodes.addAll(it.children);
      }
    }

    return result.elements();
  }

  /**
   * Return vector of entires, of empty vector if there are none.
   */
  private Vector allTransitionsWithSyncInTinst(String sync,
					       TextualInstantiation tinst){
    Object hashed = mapTinstToHashtable.get(tinst);
    
    if(null == hashed)
      return new Vector();
    else {
      Object vec = ((Hashtable)hashed).get(sync);
      if(null == vec)
	return new Vector();
      else
	return (Vector)vec;
    }
  }
  /**
   * The transitions OUTSIDE a tinst
   * (those have to be cloned in phase III)
   * Return enumeration of TinstElementPairs
   */
  public Enumeration enumTinstTransitionsWithSyncOutsideTinst(String sync,
							       TextualInstantiation tinst){
    Vector result = new Vector();
    
    for(Enumeration e = enumTinstsOutsideTinst(tinst); e.hasMoreElements(); ){
      TextualInstantiation transTinst = (TextualInstantiation)e.nextElement();
      for(Enumeration f = allTransitionsWithSyncInTinst(sync, transTinst ).elements(); f.hasMoreElements(); ){
	result.addElement(new TinstElementPair(transTinst,
					       (Element)f.nextElement()));
      }
    }
    return result.elements();
  }
}
