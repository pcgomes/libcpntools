
/*
* Models SyncTask construct, such as expressions, statements and variables,
* as Coloured Petri ned subnets, in CPN Tools format.
* 
* Author: Pedro de Carvalho Gomes <pedrodcg@csc.kth.se>
* Last update: 2015-07-02
*/

package stave;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import stave.cpntools.*;

public class ExampleCPN extends CPNToolsNetFactory {

   // Default separation between elements - Distance is relative from their centers
   private final int mspacingx = 126;
   private final int mspacingy = 126;

   private TopPage mtoppage;

   private HashSet<Element> mthreadsubtrans;

   // Stores the hierarchy of instance elements, so new substitution transitions are added accordingly.
   Stack<Element> minstance;

   /**
    * Create the basic DOM structure and add the mandatory SyncTask constructs,
    * such as the main page, which contains the Start and End places for threads.
    */
   public ExampleCPN() throws javax.xml.parsers.ParserConfigurationException {
      super();

      // Start the element that stores the instance hierarchy.
      minstance = new Stack<Element>();
      setNextInstance( minstances );

      mthreadsubtrans = new HashSet<Element>();

      // Creates the top page
      mtoppage = new TopPage();
   }

   /**
    * The object representing the top page in the CPN hierarchy
    *
    * @return Object of the top page
    */
   public TopPage topPage() {
      return mtoppage;
   }

   /**
    * Add the declaration for a color set of a bounded integer.
    */
   public void addUnitColset( String pname ) {
      appendElementToGlobbox( createUnitColset( pname ) );
   }

   /**
    * Add the BOOL color set declaration to the DOM
    */
   public void addBoolColset() {
      appendElementToGlobbox( createBoolColset( "BOOL" ) );
   }

   /**
    * Add the declaration for a color set of a bounded integer.
    */
   public void addIntColset( String pname, String plower, String puper) {
      appendElementToGlobbox( createIntColset( pname, plower, puper  ) );
   }

   /**
    *  Generates a new bounded integer type for the given interval, if such doesn't exist yet.
    *
    * @param plbound The (inclusive) lower bound
    * @param pubound The (inclusive) upper bound
    * @return name to reference the generated type.
    */
   public String makeOrGetBoundedIntType( int plbound, int pubound) {
      String lname = new String("INT" + plbound + "_" + pubound);

      NodeList ltypeids = mglobbox.getElementsByTagName( "id" );
      for( int i = 0; i < ltypeids.getLength(); i++) {

	 if ( lname.equals( ltypeids.item(i).getNodeValue() ) ) {
	    // Some other variable with the exact same bound has already been declared.
	    // There's no need to add another declaration. Returning it.
	    return lname;
	 }
      }

      // Create type and return string identifier,
      addIntColset( lname, Integer.toString( plbound ), Integer.toString( pubound ) );       
      return lname;
   }

   /**
    *  Set the instance element to be the working one, i.e., the one accepting new sub-instances.
    */
   public void setNextInstance( Element pnext ) {
      minstance.push( pnext );
   }

   /**
    * Restore the previous instance of the working instance element
    */
   public void restorePrevInstance() {
      if ( ! (minstance.empty() ) ) {
	 minstance.pop();
      }
   }

   /**
    *  Add instance element to the working instance element.
    * 
    * @param lnewinstance Instance element to be added to the DOM.
    */ 
   public void addInstance( Element lnewinstance ) {
      minstance.peek().appendChild(  lnewinstance ); 
   }

   /**
    * Add instance element to the working instance element,
    * and make it the current working instance.
    * 
    * @param lnewinstance Instance element to be added to the DOM.
    */ 
   public void addAndSetNextInstance( Element lnewinstance ) {
      addInstance( lnewinstance );
      setNextInstance( lnewinstance );
   }

   // Base class for all SyncTask nodes
   public static class Node {
      // Must generate uniqids, to create unique names for pages
      // This is a requirement for CPN Tools state-space exploration
      private static long muniqueid=0;

      protected static String getUniqueId() {
	 return Long.toString( muniqueid++);
      }
   }

   /** Creates different subpages for the SyncTask elements */
   public abstract class Page extends Node {

      protected Element mpage;
      // (inport)i
      protected Element mpinport;
      // (inport)o
      protected Element mpoutport;

      /**
       * Set the page name to be displayed in the binder and left menu
       * @param ppagename Page name
       */
      protected Page( String ppagename) {
	 mpage = createPage( ppagename + getUniqueId() );
	 appendElementToCpnet( mpage );
      }

      public Element element() { return mpage; }
      public Element inPort()  { return mpinport; }
      public Element outPort() { return mpoutport; }
      protected Element addInPortPlace( String pname, String ptype, String pinit) {
	 return (Element) mpage.appendChild( createInPortPlace( pname, ptype, pinit ) );
      }
      protected Element addOutPortPlace( String pname, String ptype, String pinit) {
	 return (Element) mpage.appendChild( createOutPortPlace( pname, ptype, pinit ) );
      }
      protected Element addSubstitutionTransition( String pname ) {
	 return (Element) mpage.appendChild( createSubstitutionTransition ( pname ) );
      }
      public void linkSubstitutionTransition( Element pstransition, Element pinsocket, Element poutsocket, Page psubpage ) {
	 connectSubstitutionPage( pstransition, pinsocket, poutsocket, psubpage.element(), psubpage.inPort(), psubpage.outPort() );
      }
      protected Element addInhibitorArc( Element pplace, Element ptrans ) {
	 return (Element) mpage.appendChild( createInhibitorArc( pplace, ptrans ) );
      }
      protected Element addArcPtoT( Element pplace, Element ptrans, String pexpression ) {
	 return (Element) mpage.appendChild( createArcPtoT( pplace, ptrans, pexpression ) );
      }
      protected Element addArcTtoP( Element ptrans, Element pplace, String pexpression ) {
	 return (Element) mpage.appendChild( createArcTtoP( ptrans, pplace, pexpression ) );
      }
      protected Element addPlace(String pname, String ptype, String pinit)  {
	 return (Element) mpage.appendChild( createBasicPlace( pname, ptype, pinit ) );
      }
      // TODO - Check to discontiue
      protected Element addFusionPlace( String pfusionname, String pname, String ptype, String pinit)  {
	 return (Element) mpage.appendChild( createFusionPlace( pfusionname, pname, ptype, pinit ) );
      }
      // Connect all places to a given transition with reflexive arcs (bidirectional, with same expression).
      // Connect a places and a transition with a reflexive arc (place)<-exp->[transition]
      // Notice: in CPN Tools it is represented by two distinct arcs, with opposite orientation.
      protected Collection<Element> addReflexiveArcs( Element ptrans, Collection<Element> pvars ) {
	 HashSet<Element> larcs = new HashSet<Element>();
	 for( Iterator<Element> e = pvars.iterator(); e.hasNext(); ) {
	    Element lplace = e.next();
	    // TODO - Check a better way to get the name
	    String lvarname = findFirstTag( lplace, "text" ).getTextContent();
	    larcs.add( addArcTtoP( ptrans, lplace, lvarname ) );
	    larcs.add( addArcPtoT( lplace, ptrans, lvarname ) );
	 }
	 return larcs;
      }
      protected Element addTransition( String pname ) {
	 return (Element) mpage.appendChild( createBasicTransition( pname ) );
      }
      protected Element addConditionTransition( String pname, String pcondition ) {
	 return (Element) mpage.appendChild( createConditionTransition( pname, pcondition ) );
      }
      protected Element addFusionAndFusionPlace( String pfusionname, String pname, String ptype, String pinit) {

	 // Step1: Create the fusion set that all instances of this variable must participate.
	 Element lfusion = createFusion( pname );
	 appendElementToCpnet( lfusion );

	 // Step2: Create the fusion place, which will also add itself to the fusion set
	 return addFusionPlace( pname, pname, ptype, pinit);
      }

   }

   public class TopPage extends Page {

      /**
       * Create the top page, which contains declaration of inital markings
       * and global variables, represented by places with fusion sets.
       */ 
      private TopPage() {
	 super( "GlobalDeclarations" );

	 // Append the page and instantiate it
	 //appendElementToCpnet( mtoppage );
	 addAndSetNextInstance( createInstanceForPage( mpage ) );

	 // Create start place
	 mpinport = addPlace( "startPlace", "UNIT", "" );
	 LayoutFactory.Place.setLayoutAndPosition( mpinport , 0 , 1*mspacingy );

	 // Create end place
	 mpoutport = addPlace( "endPlace", "UNIT", "" );
	 LayoutFactory.Place.setLayoutAndPosition( mpoutport, 0,  3*mspacingy );
      }

      public void connect( Element pstransition, Page psubpage ) {
	 linkSubstitutionTransition( pstransition, mpinport, mpoutport, psubpage );
      }

      /**
       * Retrieve the reference to the top page
       *
       * @return Reference to top page object
       */
      public Element getTopPage() {
	 return mtoppage.element();
      }

      /**
       * Creaet a substitution transition in the top page.
       *
       * @param pname Name of the thread type being processed
       * @return Substitution transition element. Must be used later for 
       */
      public Element addSubpage( String pname ) {

	 // Create substitution transition and add it to top page.
	 Element lstransition = addSubstitutionTransition( pname );
         // Add in for later positioning
         mthreadsubtrans.add( lstransition );

	 // Create default layout elements for the 
	 LayoutFactory.SubstitutionTransition.setDefaultLayout( lstransition );

	 // (start) -> [[thread]]
	 Element larc = addArcPtoT( mpinport, lstransition, "1`" + pname );
	 // [[thread]] -> (end)
	 larc = addArcTtoP( lstransition, mpoutport, "1`" + pname );

	 return lstransition;
      }

      /**
       * Set the layout of elements in the main page
       */
      public void concludeTopPage() {

	 // Set the layout information
	 LayoutFactory.SubstitutionTransition.distributeHorizontally( mthreadsubtrans, 2*mspacingx , mspacingx );
	 LayoutFactory.Arc.positionAllArcs( this.element() );
      } 
   }

   /** Declare pages that contain a single substitution transition */
   public abstract class PageS1 extends Page {
      // [[s1]]
      protected Element mts1;

      public PageS1( String pname) {
	 super( pname );
	 mts1 =  addSubstitutionTransition( "s1" );
      }
      public Element transS1() { return mts1; }
      public abstract Element insocketS1();
      public abstract Element outsocketS1();
      public void connectS1( Page psubpage ) {
	 linkSubstitutionTransition( transS1(), insocketS1(), outsocketS1(), psubpage );
      }
   }

   /** Declare pages that contain two substitution transition */
   public abstract class PageS1S2 extends PageS1 {
      // [[s2]]
      protected Element mts2;

      public PageS1S2( String pname) {
	 super( pname );
	 mts2 =  addSubstitutionTransition( "s2" );
      }
      public Element transS2() { return mts2; }
      public abstract Element insocketS2();
      public abstract Element outsocketS2();
      public void connectS2( Page psubpage ) {
	 linkSubstitutionTransition( transS2(), insocketS2(), outsocketS2(), psubpage );
      } 
   }

   /** Creates the page for sequential composition */
   public class Composition extends PageS1S2 {

      // (s1s2)
      protected Element mps1s2;

      public Element insocketS1()  { return mpinport;  }
      public Element outsocketS1()  { return mps1s2;  }
      public Element insocketS2()  { return mps1s2;  }
      public Element outsocketS2()  { return mpoutport;  }

      public Composition( String pname) {
	 super( "Composition_" + pname );

	 Element larc;

	 // (inport)i
	 mpinport = addInPortPlace("inport", "UNIT" , "");
	 LayoutFactory.PortPlace.setLayoutAndPosition( mpinport, 0, 0);

	 // [[s1]]
	 LayoutFactory.SubstitutionTransition.setLayoutAndPosition( mts1, mspacingx, 0);

	 // (inport)i -> [[s1]]
	 larc = addArcPtoT( mpinport, mts1, "1`()" );
	 LayoutFactory.Arc.setLayoutAndPosition( larc );

	 // (s1s2)
	 mps1s2 = addPlace( "s1s2", "UNIT", "" );
	 LayoutFactory.Place.setLayoutAndPosition( mps1s2, 2*mspacingx, 0 );

	 // [[s1]] -> (s1s2)
	 larc = addArcTtoP( mts1, mps1s2, "1`()" );
	 LayoutFactory.Arc.setLayoutAndPosition( larc );

	 // [[s2]]
	 LayoutFactory.SubstitutionTransition.setLayoutAndPosition( mts2, 3*mspacingx, 0);

	 // (s1s2) -> [[s2]]
	 larc = addArcPtoT( mps1s2, mts2, "1`()" );
	 LayoutFactory.Arc.setLayoutAndPosition( larc );

	 // (outport)o
	 mpoutport = addOutPortPlace("outport", "UNIT", "");
	 LayoutFactory.PortPlace.setLayoutAndPosition( mpoutport, 4*mspacingx, 0 );

	 // [[s2]] -> (outport)o
	 larc = addArcTtoP( mts2, mpoutport, "1`()" );
	 LayoutFactory.Arc.setLayoutAndPosition( larc );
      }
   }

   /* Creates the page for thread declaration */
   public class Skip extends Page {

      protected Element mtskip;

      public Skip( String pname) {

	 super( "Skip_" + pname );

	 Element larc;

	 // (inport)i
	 mpinport = addInPortPlace("inport", "UNIT", "");
	 LayoutFactory.PortPlace.setLayoutAndPosition( mpinport, 0, 0);

	 // [Skip]
	 mtskip =  addTransition( "Skip" );
	 LayoutFactory.Transition.setLayoutAndPosition( mtskip, 1*mspacingx, 0*mspacingy);

	 // (inport)i -> [Skip]
	 larc = addArcPtoT( mpinport, mtskip, "1`()" );
	 LayoutFactory.Arc.setLayoutAndPosition( larc );

	 // (outport)o
	 mpoutport = addOutPortPlace("outport", "UNIT", "");
	 LayoutFactory.PortPlace.setLayoutAndPosition( mpoutport, 2*mspacingx, 0*mspacingy);

	 // [Skip] -> (outport)o
	 larc = addArcTtoP( mtskip, mpoutport, "1`()" );
	 LayoutFactory.Arc.setLayoutAndPosition( larc );
      }
   }

   public static void main( String[] argv) throws ParserConfigurationException {
      // Build the DOM element
      ExampleCPN mcpnet = new ExampleCPN();

      // Add a UNIT colset
      mcpnet.addUnitColset( "UNIT" );

      // Creates a substitution transition for a sub-process and assign a Skip 
      Element lstransition1 = mcpnet.topPage().addSubpage( "subprocess1" );
      mcpnet.addAndSetNextInstance( mcpnet.createInstanceForSTransition( lstransition1 ) );
      ExampleCPN.Skip lsubpage1 = mcpnet.new Skip( "Skip" );
      mcpnet.topPage().connect( lstransition1, lsubpage1 );
      mcpnet.restorePrevInstance();


      // Creates a substitution transition for a sub-process and assign a Composition, the Skip in both 
      Element lstransition2 = mcpnet.topPage().addSubpage( "subprocess2" );
      mcpnet.addAndSetNextInstance( mcpnet.createInstanceForSTransition( lstransition2 ) );
      ExampleCPN.Composition lsubpage2 = mcpnet.new Composition( "Comp" );
      mcpnet.topPage().connect( lstransition2, lsubpage2 );


      mcpnet.addAndSetNextInstance( mcpnet.createInstanceForSTransition( lsubpage2.transS1() ) );
      ExampleCPN.Skip lskips1 = mcpnet.new Skip( "Skip" );
      lsubpage2.connectS1( lskips1 );
      mcpnet.restorePrevInstance();

      mcpnet.addAndSetNextInstance( mcpnet.createInstanceForSTransition( lsubpage2.transS2()  ) );
      ExampleCPN.Skip lskips2 = mcpnet.new Skip( "Skip" );
      lsubpage2.connectS2( lskips2 );
      mcpnet.restorePrevInstance();

      mcpnet.topPage().concludeTopPage();

      try { 
	 mcpnet.writeDOMtoCpnFile( argv[0] );
      } catch (Exception e) {}
   }
}
