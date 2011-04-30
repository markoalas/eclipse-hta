// -*- mode: JDE; c-basic-offset: 2; -*-
// /////////////////////////////////////////////////////////////
// Simple Layout Module (for Templates)
//
//  Change Layout of Templates to something readable
//  by using default grid and moving labels.
// 
// Synopsis:
//  Huppaal
// /////////////////////////////////////////////////////////////
// @FILE:    SimpleLayouter.java
// @PLACE:   BRICS AArhus; host:newton
// @FORMAT:  java
// @AUTHOR:  M. Oliver M'o'ller     <omoeller@brics.dk>
// @BEGUN:   Mon Mar 19 13:16:19 2001
// @VERSION: Mon Mar 19 16:11:52 2001
// /////////////////////////////////////////////////////////////
// 


import DocumentReader;

import org.w3c.dom.Element;
import org.w3c.dom.Text;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;


//**** from other packages 

//****************************************

/**
 * 
 *
 * @author <A HREF="MAILTO:omoeller@brics.dk?subject=SimpleLayouter.java%20(Mon%20Mar%2019%2014:38:59%202001)">M. Oliver M&ouml;ller</A>
 * @version Mon Mar 19 16:11:52 2001
 */
public class SimpleLayouter
    implements Layouter {

  // //////////////////////////////////////////////////////////////////////
  // ////////////////////////////// FIELDS ////////////////////////////////
  // //////////////////////////////////////////////////////////////////////

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


  // -- location labels -------------------------------

  public static long locNameXOffset = 12L;
  public static long locNameYOffset = 12L;

  // -- transition labels -----------------------------

  public static long assignmentXOffset = 5L;
  public static long assignmentYOffset = 5L;

  public static double assignmentXShift = 0.5;
  public static double assignmentYShift = 0.5;


  public static long guardXOffset = -5L;
  public static long guardYOffset = -5L;

  public static double guardXShift = -0.7;
  public static double guardYShift = -0.7;


  public static long synchronisationXOffset = 10L;
  public static long synchronisationYOffset = 10L;

  public static double synchronisationXShift = -0.1;
  public static double synchronisationYShift = -0.1;


  // --------------------------------------------------


  /**
   * Parameter for default layout computatation
   */
  private long geoCount = 0L;


  // -- AUX -----------------------------------------------------

  private FlatDocumentWriter fdw;

  public static boolean sanityChecks = true;

  public static boolean debug = true;

  

  // //////////////////////////////////////////////////////////////////////
  // //////////////////////////  CONSTRUCTORS  ////////////////////////////
  // //////////////////////////////////////////////////////////////////////

  /**
   * Default Constructor<BR>
   * Needs the flat document writer to access elements by ID (fast)
   */
  public SimpleLayouter(FlatDocumentWriter theFdw){
    fdw = theFdw;
  }

  // //////////////////////////////////////////////////////////////////////
  // ///////////////////////////// METHODS  ///////////////////////////////
  // //////////////////////////////////////////////////////////////////////

  public void layoutTemplate(Element template) 
    throws Exception {
    geoCount = 0L;
    
    // -- Browse Locations -------------------------------------------------

    NodeList locs =  fdw.getAllChildrenWithLabel(template, "location");
    for(int i=0; i < locs.getLength(); i++){
      Element loc = (Element)locs.item(i);
      addDefaultLocationCoordinates(loc);
    }

    // -- Recompute Transitions (remove nails) --------
    
    NodeList transs = fdw.getAllChildrenWithLabel(template, "transition");
    for(int i=0; i < transs.getLength(); i++){
      Element trans = (Element)transs.item(i);
      adjustTransition(trans);

    }


  }

  // -- Modifications ------------------------------------------------------

  private void addDefaultLocationCoordinates(Element location)
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

  /**
   * <H3>Adjust a transition:</H3>
   * <UL>
   *  <LI>remove nails</LI>
   *  <LI>center coordinates</LI>
   *  <LI>move labels to default points</LI>
   * </UL>
   */
  private void adjustTransition(Element trans)
    throws Exception {

    // -- remove nails (if any) --------------------------------------------

    NodeList nails = fdw.getAllChildrenWithLabel(trans, "nails");
    for(int j=0 ; j < nails.getLength(); j++){
      Element nail = (Element)nails.item(j);
      trans.removeChild(nail);
    }

    // -- center coordinates -----------------------------------------------

    Element source = fdw.getTheChildWithLabel(trans, "source");
    Element target = fdw.getTheChildWithLabel(trans, "target");
    Element sourceLoc = fdw.getElementByID(source.getAttribute("ref"));
    Element targetLoc = fdw.getElementByID(target.getAttribute("ref"));

    long sourceX = fdw.getXCoordinate(sourceLoc);
    long sourceY = fdw.getYCoordinate(sourceLoc);
    long targetX = fdw.getXCoordinate(targetLoc);
    long targetY = fdw.getYCoordinate(targetLoc);

    long meanX = (sourceX + targetX)/2L;
    long meanY = (sourceY + targetY)/2L;
    
    trans.setAttribute("x", "" + meanX);
    trans.setAttribute("y", "" + meanY);

    NodeList labels = fdw.getAllChildrenWithLabel(trans, "label");
    for(int j=0 ; j < labels.getLength(); j++){
      Element label = (Element)labels.item(j);
      long x;
      long y;
      if(fdw.isAssignment(label)){
	if(debug)
	  System.out.println("============== ASSIGMENT");
	x = meanX + assignmentXOffset + (long)(((double)(targetX - meanX))* assignmentXShift);
	y = meanY + assignmentYOffset + (long)(((double)(targetY - meanY))* assignmentYShift);
      } 
      else if(fdw.isGuard(label)){
	if(debug)
	  System.out.println("============== GUARD");

	x = meanX + guardXOffset + (long)(((double)(targetX - meanX))* guardXShift);
	y = meanY + guardYOffset + (long)(((double)(targetY - meanY))* guardYShift);
      } 
      else if(fdw.isSynchronisation(label)){
	if(debug)
	  System.out.println("============== SYNCHRONISATION");

	x = meanX + synchronisationXOffset + (long)(((double)(targetX - meanX))* synchronisationXShift);
	y = meanY + synchronisationYOffset + (long)(((double)(targetY - meanY))* synchronisationYShift);
      } 
      else { // ?? unknow label ??
	x = meanX;
	y = meanY;
      }
	
      label.setAttribute("x", "" + x);
      label.setAttribute("y", "" + y);
    } // -- done with child labels ------------------------------


  }

}
