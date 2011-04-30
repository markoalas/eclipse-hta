// -*- mode: JDE; c-basic-offset: 2; -*-
// /////////////////////////////////////////////////////////////
// Wrapper for a Pair
// 
// Synopsis:
//  Huppaal
// /////////////////////////////////////////////////////////////
// @FILE:    TinstElementPair.java
// @PLACE:   Uppsala; host:eniac
// @FORMAT:  java
// @AUTHOR:  M. Oliver M'o'ller     <omoeller@brics.dk>
// @BEGUN:   Sat Feb  3 19:34:00 2001
// @VERSION: Vanilla-1                  Tue Feb 13 11:46:22 2001
// /////////////////////////////////////////////////////////////
// 

import java.lang.*;

import org.w3c.dom.Element;

//**** from other packages 

//****************************************

/**
 * Wrapper Class for a pair describing an explicit element.<BR>
 * Usually used for Exits and Locations.
 *
 * @author <A HREF="MAILTO:omoeller@brics.dk?subject=TinstElementPair.java%20(Vanilla-1%20Tue%20Feb%206%2016:19:55%202001)">M. Oliver M&ouml;ller</A>
 * @version Vanilla-1                  Tue Feb 13 11:46:22 2001
 */
public class TinstElementPair  {

  // //////////////////////////////////////////////////////////////////////
  // ////////////////////////////// FIELDS ////////////////////////////////
  // //////////////////////////////////////////////////////////////////////

  /**
   * The textual instantiation the element belongs to
   */
   public  final TextualInstantiation tinst;
  
  /**
   * The element (as defined in the template).
   */
   public  final Element element;

  // //////////////////////////////////////////////////////////////////////
  // //////////////////////////  CONSTRUCTORS  ////////////////////////////
  // //////////////////////////////////////////////////////////////////////

  /**
   * Default Constructor
   */
  public TinstElementPair(TextualInstantiation theTinst,
		       Element theElement){
      tinst = theTinst;
      element  = theElement;
  }

  // //////////////////////////////////////////////////////////////////////
  // ///////////////////////////// METHODS  ///////////////////////////////
  // //////////////////////////////////////////////////////////////////////

  public String toString(){
    return 
      "TEXUTAL INSTANTIATION IN " + tinst.toString() + " - AT:\n" +
      element.toString();
  }

}
