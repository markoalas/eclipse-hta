// -*- mode: JDE; c-basic-offset: 2; -*-
// /////////////////////////////////////////////////////////////
// Component Mapper:  Component * Tinst -> Tinst
// 
// Synopsis:
//  Huppaal
// /////////////////////////////////////////////////////////////
// @FILE:    ComponentMapper.java
// @PLACE:   BRICS AArhus; host:harald
// @FORMAT:  java
// @AUTHOR:  M. Oliver M'o'ller     <omoeller@brics.dk>
// @BEGUN:   Sat Feb  3 20:33:16 2001
// @VERSION: Vanilla-1                  Tue Apr  3 15:33:48 2001
// /////////////////////////////////////////////////////////////
// 


import java.lang.*;

import java.util.Hashtable;

import org.w3c.dom.Element;

//**** from other packages 

//****************************************

/**
 * <H2>Service Class</H2>
 * This class enables a mapping of hierarchical component elements
 * and the TextualInstantiation they are declared in to the 
 * TextualInstantiation they are instantiated with.
 * <BR>
 * This mapping can be retrieved lateron, which is neccessary for the 
 * translation
 * of global joins.
 *
 * @author <A HREF="MAILTO:omoeller@brics.dk?subject=ComponentMapper.java%20(Vanilla-1%20Tue%20Apr%203%2014:22:47%202001)">M. Oliver M&ouml;ller</A>
 * @version Vanilla-1                  Tue Apr  3 15:33:48 2001
 */
public class ComponentMapper  {

  // //////////////////////////////////////////////////////////////////////
  // ////////////////////////////// FIELDS ////////////////////////////////
  // //////////////////////////////////////////////////////////////////////


  /**
   * Maps (basic) hierarchical components to another hashtable, that
   * TextualInstantiation to TextualInstantiation, namely those
   * that correspond to the instantiation of the component in this context.
   * 
   * @see GlobalJoin
   */
  private Hashtable hashHierarchicalComponentsToHashtable;

  // ===================================
  // Auxillary
  // ===================================

  /**
   * Spam out debuggin information, if <TT>debug</TT> is true
   */
  static boolean debug = true;

  // //////////////////////////////////////////////////////////////////////
  // //////////////////////////  CONSTRUCTORS  ////////////////////////////
  // //////////////////////////////////////////////////////////////////////

  /**
   * Default Constructor
   */
  public ComponentMapper(){
    
    hashHierarchicalComponentsToHashtable = new Hashtable();
  }

  // //////////////////////////////////////////////////////////////////////
  // ///////////////////////////// METHODS  ///////////////////////////////
  // //////////////////////////////////////////////////////////////////////

  

  /**
   * Get the TextualInstantiation
   * according to the mapping<BR>
   * <BR>
   * <TT> Component * TextualInstantiation  ---> 
   * TextualInstantiation</TT><BR>
   * <BR>
   * The first TextualInstantiation is the <TT>declaringContext</TT>,
   * the result TextualInstantiation is the (sub-)declared one.<BR>
   * <BR>
   * If the TextualInstantiation is not entered yet, it is created with
   *    the empty constructor.
   * 
   */ 
  public TextualInstantiation  retrieveTextualInstantiation(Element component,
							    TextualInstantiation tinst)
    throws Exception {
    
    if(debug)
      System.out.println("$$$$ ComponentMapper Retrieving: \n" + component.toString() + "\n" + tinst.toString());

    Object hashed = hashHierarchicalComponentsToHashtable.get(component);
    Object mem;
    if( (null == hashed) ||
	(null == (mem = ((Hashtable)hashed).get(tinst)))  ){
      TextualInstantiation result = new TextualInstantiation();
      enterTextualInstantiation(component, tinst, result);
    
      if(debug)
	System.out.println("-a-> " + result.toString());

      return result;
    }
    else {
      if(debug)
	System.out.println("-b-> " +  ((TextualInstantiation)mem).toString());

      return (TextualInstantiation)mem;
    }
  }
  /**
   * Create an entry in
   * <BR>
   * <TT> Component * TextualInstantiation  ---> 
   * TextualInstantiation</TT><BR>
   * <BR>
   * The first TextualInstantiation is the <TT>declaringContext</TT>,
   * the result TextualInstantiation is the (sub-)declared one.<BR>
   * <BR>
   */ 
  public void  enterTextualInstantiation(Element component, TextualInstantiation tinst, TextualInstantiation subTinst)
    throws Exception {

    if(debug)
      System.out.println("$$$$ ComponentMapper entry: \n" + component.toString() + "\nx " + tinst.toString() + "\n-> " + subTinst.toString());

    
    Object hashed = hashHierarchicalComponentsToHashtable.get(component);
    if(null == hashed){
      Hashtable subHash = new Hashtable();
      hashHierarchicalComponentsToHashtable.put(component, subHash);
      subHash.put(tinst, subTinst);
    }
    else { 
      ((Hashtable)hashed).put(tinst, subTinst);
    }
  }
}
