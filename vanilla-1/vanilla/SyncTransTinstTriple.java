// -*- mode: JDE; c-basic-offset: 2; -*-
// /////////////////////////////////////////////////////////////
// Wrapper
// 
// Synopsis:
//  
// /////////////////////////////////////////////////////////////
// @FILE:    SyncTransTinstTriple.java
// @PLACE:   BRICS AArhus; host:harald
// @FORMAT:  java
// @AUTHOR:  M. Oliver M'o'ller     <omoeller@brics.dk>
// @BEGUN:   Mon Apr  9 11:21:45 2001
// @VERSION: Mon Apr  9 11:30:36 2001
// /////////////////////////////////////////////////////////////
// 


import java.lang.*;

import org.w3c.dom.Element;

//**** from other packages 

//****************************************

/**
 * 
 *
 * @author <A HREF="MAILTO:omoeller@brics.dk?subject=SyncTransTinstTriple.java%20(Mon%20Apr%209%2011:23:49%202001)">M. Oliver M&ouml;ller</A>
 * @version Mon Apr  9 11:30:36 2001
 */
public class SyncTransTinstTriple  {


  // //////////////////////////////////////////////////////////////////////
  // ////////////////////////////// FIELDS ////////////////////////////////
  // //////////////////////////////////////////////////////////////////////

  public String  sync;
  public Element trans;
  public TextualInstantiation tinst;

  // //////////////////////////////////////////////////////////////////////
  // //////////////////////////  CONSTRUCTORS  ////////////////////////////
  // //////////////////////////////////////////////////////////////////////

  /**
   * Default Constructor
   */
  public SyncTransTinstTriple(String theSync, 
			      Element theTrans, 
			      TextualInstantiation theTinst){
    sync = theSync;
    trans = theTrans;
    tinst = theTinst;    
  }

}
