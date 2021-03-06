// -*- mode: JDE; c-basic-offset: 2; -*-
// /////////////////////////////////////////////////////////////
// Change Layout of Templates to something readable
// 
// Synopsis:
//  Huppaal
// /////////////////////////////////////////////////////////////
// @FILE:    Layouter.java
// @PLACE:   BRICS AArhus; host:newton
// @FORMAT:  java
// @AUTHOR:  M. Oliver M'o'ller     <omoeller@brics.dk>
// @BEGUN:   Mon Mar 19 13:11:45 2001
// @VERSION: Mon Mar 19 13:17:21 2001
// /////////////////////////////////////////////////////////////
// 


import org.w3c.dom.Element;

//**** from other packages 

//****************************************

/**
 * Interface for Layout modules
 *
 * @author <A HREF="MAILTO:omoeller@brics.dk?subject=Layouter.java%20(Mon%20Mar%2019%2013:17:14%202001)">M. Oliver M&ouml;ller</A>
 * @version Mon Mar 19 13:17:21 2001
 */
public interface Layouter  {

  // //////////////////////////////////////////////////////////////////////
  // ////////////////////////////// FIELDS ////////////////////////////////
  // //////////////////////////////////////////////////////////////////////


  // //////////////////////////////////////////////////////////////////////
  // ///////////////////////////// METHODS  ///////////////////////////////
  // //////////////////////////////////////////////////////////////////////

  public void layoutTemplate(Element template) throws Exception;

}
