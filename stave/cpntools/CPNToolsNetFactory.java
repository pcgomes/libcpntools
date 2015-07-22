/*
* Helper class that creates new DOM elements for  CPN Tools elements,
* such as places, transitions, arcs, subpages and fusion sets.
* Author: Pedro de Carvalho Gomes <pedrodcg@csc.kth.se>
* Last update: 2015-07-08
* TODO : 
* - Clean the ID setting. Currently is very risk-prone and indexing useless things.
*/

package stave.cpntools;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CPNToolsNetFactory extends BaseCPNCommunication {

   private long muniqid = 10;

   /**
   * Generates sequentially identifiers for the DOM elements.
   * @return unique identifier.
   */
   private String getUniqueId() {
      muniqid++;
      return new String( "ID" + muniqid);
   }

   // Main object that stores the DOM
   private Document mdocument;

   /**
   * The DOM object containing the CPN Tools information.
   * @return DOM object
   */
   public Document getDOM() {
      return mdocument;
   }

   /**
   * Output the internal DOM to a file.
   *
   * @param pfilename Location of the file.
   */
   public void writeDOMtoCpnFile(String pfilename)  throws FileNotFoundException, TransformerConfigurationException, TransformerException {
      outputDOMtoFile(mdocument, pfilename);
   }

   /* Begin: methods that keep track of important DOM sections */

   protected final Element mcpnet;
   protected final Element mglobbox;
   protected final Element minstances;

   /**
   * Append an element to the cpnet section, which is the document's main section.
   * @param pelement Element to append at cpnet section.
   */
   public void appendElementToCpnet( Element pelement ) {
      mcpnet.appendChild( pelement );
   }

   /**
   * Prepend an element to the cpnet section, which is the document's main section.
   * @param pelement Element to append at cpnet section.
   */
   public void prependElementToCpnet( Element pelement ) {
      mcpnet.insertBefore( pelement, mcpnet.getFirstChild());
   }

   /**
   * Append an element to the globbox section, which contains the color set definitions.
   * @param pelement Element to append at globbox section.
   */
   public void appendElementToGlobbox(Element pelement) {
      mglobbox.appendChild( pelement);
   }

   /**
   * Prepend an element to the globbox section, which contains the color set definitions.
   * @param pelement Element to append at globbox section.
   */
   public void prependElementToGlobbox(Element pelement) {
      mglobbox.insertBefore( pelement, mcpnet.getFirstChild());
   }

   /**
   * Append an element to the instances section, which contains the instantiations of pages.
   * @param pelement Element to append at globbox section.
   */
   public void appendElementToInstances(Element pinstance) {
      minstances.appendChild( pinstance);
   }

   /**
   * Helper method that searches for the first descending occurance of an element with a given tag.
   *
   * @param proot root element to start the search.
   * @param ptag Tag of the node you are looking for.
   * @return reference to top-most element with a given tag. Null if empty.
   */
   protected Node findFirstTag(Element proot, String ptag) {
      org.w3c.dom.NodeList ltaglist = proot.getElementsByTagName(ptag);
      if (ltaglist.getLength() > 0) return ltaglist.item(0);

      return null;
   }

   /**
   * Helper method that searches for the first descending occurance of an element in the DOM with a given tag.
   *
   * @param ptag Tag of the node you are looking for.
   * @return reference to top-most element with a given tag. Null if empty.
   */
   protected Node findFirstTag(String ptag) {
      org.w3c.dom.NodeList ltaglist = mdocument.getElementsByTagName(ptag);
      if (ltaglist.getLength() > 0) return ltaglist.item(0);

      return null;
   }

   /**
   * Search the DOM for an object by its "supposedly" unique id.
   *
   * @return reference to the object with given id.
   */
   public Element findElementId(String pid) {
      return mdocument.getElementById( pid);
   }

   /* End: methods that keep track of important DOM sections */

   /**
   * Constructs the basic DOM for CPN Tools file.
   */
   public CPNToolsNetFactory() throws ParserConfigurationException {

      // Create factory, to generate a builder, to finally create the DOM.
      DocumentBuilderFactory lfactory =  DocumentBuilderFactory.newInstance();
      DocumentBuilder lbuilder = lfactory.newDocumentBuilder();

      mdocument = lbuilder.newDocument();
      mdocument.setXmlVersion( "1.0" );
      //Create doc type
      DOMImplementation limplementation = mdocument.getImplementation();
      DocumentType ldoctype = limplementation.createDocumentType( "workspaceElements", "-//CPN//DTD CPNXML 1.0//EN", "http://cpntools.org/DTD/6/cpn.dtd" );
      mdocument.appendChild( ldoctype );

      // Add root element
      Element lroot = mdocument.createElement( "workspaceElements" );
      mdocument.appendChild( lroot);

      // Add default elements
      // 1 -generator
      Element lgenerator = mdocument.createElement( "generator" );
      lgenerator.setAttribute( "tool","CPN Tools" );
      lgenerator.setAttribute( "version","4.0.1" );
      lgenerator.setAttribute( "format","6" );
      lroot.appendChild( lgenerator);

      // 2 - cpnet
      Element lcpnet = mdocument.createElement( "cpnet" );
      mcpnet = lcpnet;
      lroot.appendChild( lcpnet );

      // 2-A <globbox/>
      Element lglobbox = mdocument.createElement( "globbox" );
      mglobbox = lglobbox;
      //mglobbox.appendChild( createUnitColset( "UNIT" ) );
      lcpnet.appendChild( lglobbox );
      // 2-B <instances/>
      minstances = mdocument.createElement( "instances" );
      lcpnet.appendChild( minstances );
      // 2-C <options>
      lcpnet.appendChild( mdocument.createElement( "options" ) );
      // 2-D <binders/>
      lcpnet.appendChild( mdocument.createElement( "binders" ) );
      // 2-E <monitorblock name="Monitors"/>
      Element lmonitorblock = mdocument.createElement( "monitorblock" );
      lmonitorblock.setAttribute( "name","Monitors" );
      lcpnet.appendChild(lmonitorblock);

   }

   /** 
   * Make a pagem element.
   *
   * @param pname Page's name. Used for graphical purposes.
   * @param pid The page's (suposedly) unique id.
   * @return DOM element representing a page.
   */
   public Element createPage(String pname, String pid) {
      Element lpage = mdocument.createElement( "page" );
      lpage.setAttribute( "id", pid );
      lpage.setIdAttribute( "id", true );

      Element lpageattr = mdocument.createElement( "pageattr" );
      lpageattr.setAttribute( "name", pname);

      lpage.appendChild(lpageattr);
      lpage.appendChild( mdocument.createElement( "constraints" ) );

      return lpage;
   }

   /** 
   * Make a pagem element where the id generated automatically.
   *
   * @param pname Page's name. Used for graphical purposes.
   * @return DOM element representing a page.
   */
   public Element createPage( String pname ) {
      return createPage( pname, getUniqueId() );
   }

   /** 
   * Search for a page using its presumably unique Id.
   * If none if found, it creates a new page with name equals to the id.
   *
   * @param pid Page identifier, which is assumed to be unique.
   * @return DOM element of the found page.
   */
   public Element findPageById( String pid ) {
      NodeList lpagelist = mcpnet.getElementsByTagName( "page" );
      for (int i = 0; i < lpagelist.getLength(); i++ ) {
         Element lpage = (Element) lpagelist.item( i );

         if (pid.equals( lpage.getAttribute( "id" ) ) ) return lpage;
      }

      return createPage(pid,pid);
   }

   /**
   * Creates an instance element, instantiating a page.
   * 
   * This is the case for different pages not used in hierarchy.
   *
   * @param ppageid Page instantiated by this declaration, used to instantiate the defined pages.
   * @return DOM element instance, which must be added to the corresponding \<instances\>
   */
   public Element createInstanceForPage(String ppageid) {
      Element linstance = mdocument.createElement( "instance" );
      linstance.setAttribute( "id", getUniqueId() );
      linstance.setIdAttribute( "id", true );
      linstance.setAttribute( "page", ppageid );

      return linstance;
   }

   /**
   * Creates an instance element, instantiating a page.
   * 
   * This is the case for different pages not used in hierarchy.
   * @param ppage Page element instantiated by this declaration, used to instantiate the defined pages.
   * @return DOM element instance, which must be added to the corresponding \<instances\>
   */
   public Element createInstanceForPage( Element ppage ) {
      return createInstanceForPage( ppage.getAttribute( "id" ) );
   }

   /**
   * Creates an instance element, instantiating a substitution transition.
   * 
   * This is the case for different pages not used in hierarchy.
   *
   * @param ppageid Page instantiated by this declaration, used to instantiate the defined pages.
   * @return DOM element instance, which must be added to the corresponding @{code <instance>}
   */
   public Element createInstanceForSTransition(String ppageid) {
      Element linstance = mdocument.createElement( "instance" );
      linstance.setAttribute( "id", getUniqueId() );
      linstance.setIdAttribute( "id", true );
      linstance.setAttribute( "trans", ppageid );

      return linstance;
   }

   /**
   * Creates an instance element, instantiating a substitution transition.
   * 
   * This is the case for different pages not used in hierarchy.
   *
   * @param ppage Page element instantiated by this declaration, used to instantiate the defined pages.
   * @return DOM element instance, which must be added to the corresponding @{code <instance>}
   */
   public Element createInstanceForSTransition( Element ppage ) {
      return createInstanceForSTransition( ppage.getAttribute( "id" ) );
   }

   /**
   * Fusion elements group diferent instantiations of the same place
   *
   * @param pfusionname Text representing the fusion set in the menu.
   * @return DOM element that prepresents the fusion set
   */
   public Element createFusion(String pfusionname) {
      Element lfusion = mdocument.createElement( "fusion" );
      lfusion.setAttribute( "id", getUniqueId() );
      lfusion.setIdAttribute( "id", true );
      lfusion.setAttribute( "name", pfusionname );

      return lfusion;
   }

   /**
   * 
   * Creates the fusion_elm, which is the actual reference to which places are part of the fusion set.
   * @return DOM element fusion_elem, which must be added to the respective fusion element.
   */
   public Element createFusion_elm(String pidref) {
      Element fusion = mdocument.createElement( "fusion_elm" );
      fusion.setAttribute( "idref", pidref );

      return fusion;
   }

   /**
   * Creates a new color set element, with the common structure to all sets.
   *
   * @param pname color set name
   * @return new incomplete color set element.
   */
   private Element createBasicColset( String pname) {
      //1<color>
      Element lcolset = mdocument.createElement( "color" );
      lcolset.setAttribute( "id", getUniqueId() );
      lcolset.setIdAttribute( "id", true );

      //1.A<id></id>
      Element lid =  mdocument.createElement( "id" );
      lid.setTextContent(pname);
      lcolset.appendChild(lid);

      return lcolset;
   }

   /** 
   * Creates a new singleton color set.
   * 
   * @param pname Color set name. Used both for graphical representation and referencing.
   * @return DOM element representing the color set.
   */
   protected Element createUnitColset( String pname) {
      //1<color>
      Element lcolset = createBasicColset( pname);

      //1.A<unit/>
      Element lunit  = mdocument.createElement("unit");
      //1.B<layout>
      Element llayout = mdocument.createElement("layout");
      llayout.setTextContent("colset " + pname + " = unit;" );

      lcolset.appendChild(lunit);
      lcolset.appendChild(llayout);
      return lcolset;
   }

   /** 
   * Creates a new color set with standard boolean values false and true.
   * 
   * @param pname Color set name. Used both for graphical representation and referencing.
   * @return DOM element representing the color set.
   */
   protected Element createBoolColset( String pname) {
      //1<color>
      Element lcolset = createBasicColset( pname);

      //1.A<bool/>
      Element lunit  = mdocument.createElement("bool");
      //1.B<layout>
      Element llayout = mdocument.createElement("layout");
      llayout.setTextContent("colset " + pname + " = bool;" );

      lcolset.appendChild(lunit);
      lcolset.appendChild(llayout);

      return lcolset;
   }
   
   /** 
   * Creates a new color set of bounded integers
   * 
   * @param pname Color set name. Used both for graphical representation and referencing.
   * @param plbound The set domain's (inclusive) lower value
   * @param pubound The set domain's (inclusive) upper value
   * @return DOM element representing the color set.
   */
   protected Element createIntColset( String pname, String plbound, String pubound) {

      //1<color>
      Element lcolset = createBasicColset( pname );

      //1.A<int/>
      Element lint  = mdocument.createElement( "int" );
      lcolset.appendChild( lint );
      //1.A.1<with>
      Element lwith  = mdocument.createElement( "with" );
      lint.appendChild( lwith );
      //1.A.1.A<ml>: lower bound
      Element lmllower  = mdocument.createElement( "ml" );
      lwith.appendChild( lmllower );
      lmllower.setTextContent( plbound );
      //1.A.1.B<ml>: upper bound
      Element lmlupper  = mdocument.createElement( "ml" );
      lwith.appendChild( lmlupper );
      lmlupper.setTextContent( pubound );

      //1.B<layout>
      Element llayout = mdocument.createElement( "layout" );
      lcolset.appendChild( llayout );
      llayout.setTextContent( "colset " + pname + " =  with " + plbound + ".." + pubound + ";" );

      return lcolset;
   }

   /** 
   * Creates a new color set from an enumeration.
   * 
   * @param pname Color set name. Used both for graphical representation and referencing.
   * @param pitems The elements of the color set.
   * @return DOM element representing the color set.
   */
   protected Element createEnumColset( String pname, Collection<String> pitems) throws BadCPNDefinitionException {

      // CPN Tools does not allow creation of empty sets.
      if (pitems.isEmpty()) throw new BadCPNDefinitionException("Cannot create the empty ENUM color set " + pname + ".");

      //1<color>
      Element lcolset = createBasicColset( pname );

      //1.A<enum/>
      Element lenum  = mdocument.createElement( "enum" );
      lcolset.appendChild( lenum );

      //1.A.1<id>: Set items
      String llayoutlist = new String();
      boolean isFirst = true;
      for( Iterator<String> literator = pitems.iterator(); literator.hasNext(); ) {
         Element lid  = mdocument.createElement( "id" );
         lenum.appendChild( lid );

         String litem = literator.next();
         lid.setTextContent( litem );
         
         // Create part of declaration for the layout below
         if (isFirst) isFirst = false;
         else llayoutlist += " | ";
 
         llayoutlist  += litem;
      }

      //1.B<layout>
      Element llayout = mdocument.createElement( "layout" );
      lcolset.appendChild( llayout );
      llayout.setTextContent( "colset " + pname + " =  with " + llayoutlist + ";" );

      return lcolset;
   }

   /** 
   * Creates the DOM element for an color set defined by cartesian product (S1 x S2 x S3...).
   * Obs: The declaration of the sets must preceed this one in the DOM.
   * 
   * @param pname Color set name. Used both for graphical representation and referencing.
   * @param psets Ordered list of sets defining the product
   * @return DOM element representing the color set.
   */
   protected Element createProductColset( String pname, List<String> psets) throws BadCPNDefinitionException {

      // CPN Tools does not allows declaration with arity smaller than 2
      if (psets.size() < 2) throw new BadCPNDefinitionException( "Tried to create an product color set with less than two sets." );

      //1<color>
      Element lcolset = createBasicColset( pname );

      //1.A<product/>
      Element lproduct  = mdocument.createElement( "product" );
      lcolset.appendChild( lproduct );

      //1.A.1<id>: Define the set domains 
      String llayoutlist = new String();
      boolean isFirst = true;
      for( Iterator<String> literator = psets.iterator(); literator.hasNext(); ) {
         Element lid  = mdocument.createElement( "id" );
         lproduct.appendChild( lid );

         String litem = literator.next();
         lid.setTextContent( litem );
         
         // Create part of declaration for the layout below
         if (isFirst) isFirst = false;
         else llayoutlist += " * ";
 
         llayoutlist  += litem;
      }

      //1.B<layout>
      Element llayout = mdocument.createElement( "layout" );
      lcolset.appendChild( llayout );
      llayout.setTextContent( "colset " + pname + " =  product with " + llayoutlist + ";" );

      return lcolset;
   }

   /** 
   * Creates the DOM element for an color set defined by cartesian product of two sets.
   * Obs: The declaration of the two sets must preceed this one in the DOM.
   * 
   * @param pname Color set name. Used both for graphical representation and referencing.
   * @param pset1 Domain of the first element.
   * @param pset2 Domain of the second element.
   * @return DOM element representing the color set.
   */
   protected Element createPairColset( String pname, String pset1, String pset2 ) throws BadCPNDefinitionException {
      java.util.Vector<String> lcollection = new java.util.Vector<String>();
      lcollection.add(pset1);
      lcollection.add(pset2);

      return createProductColset( pname, lcollection);
   }

   /** 
   * Creates the declaration of an single ML variable
   * 
   * @param pname name of the variable to be declared
   * @param variable type
   * @return DOM element representing the color set.
   */
   protected Element createVarDecl( String pname, String ptype) {
      HashSet<String> lvar = new HashSet<String>();
      lvar.add( pname );
      return createVarDeclList( lvar, ptype );
   }

   /** 
   * Creates the declaration of a list of variables from the same type
   * 
   * @param plist Structure containing all variables to be declared
   * @param variable type
   * @return DOM element representing the color set.
   */
   protected Element createVarDeclList( Collection<String> plist, String ptype) {

      if (plist.isEmpty()) throw new BadCPNDefinitionException("Cannot create empty var declaration of type " + ptype + ".");

      //1<var>
      Element lvar = mdocument.createElement( "var" );
      lvar.setAttribute( "id", getUniqueId() );
      lvar.setIdAttribute( "id", true );

      //1.A<type></type>
      Element ltype =  mdocument.createElement( "type" );
      lvar.appendChild( ltype );

      //1.A.1<id></id>
      Element lid1 =  mdocument.createElement( "id" );
      ltype.appendChild( lid1 );
      lid1.setTextContent( ptype );

      String lltext = new String();
      for( Iterator<String> i = plist.iterator(); i.hasNext(); ) {
         String lname = i.next();

	 //1.B<id></id>
	 Element lid2 =  mdocument.createElement( "id" );
	 lvar.appendChild( lid2 );
	 lid2.setTextContent( lname );

         lltext += ", " + lname ;
      }
      lltext = lltext.substring(1);
 
      //1.C<layout>
      Element llayout = mdocument.createElement("layout");
      lvar.appendChild(llayout);
      llayout.setTextContent("var" + lltext + ": " + ptype + ";" );

      return lvar;
   }


   /**
   * Creates a basic transition, without layout information.
   *
   * @param pname Text to be displayed in the transition.
   * @return DOM element representing the transition.
   */
   public Element createBasicTransition(String pname) {
      Element ltrans = mdocument.createElement( "trans" );
      ltrans.setAttribute( "id", getUniqueId() );
      ltrans.setIdAttribute( "id", true );
      ltrans.setAttribute( "explicit", "false" );

      Element ltext = mdocument.createElement( "text" );
      ltext.setTextContent(pname);
      ltrans.insertBefore( ltext, ltrans.getFirstChild() );

      return ltrans;
   }

   /**
   * Creates a transition containing a condition.
   *
   * @param pname Text to be displayed in the substitution transition.
   * @param pcondition Condition necessary to trigger the transition, specified as ML expression.
   * @return DOM element representing the substitution transition.
   */
   public Element createConditionTransition( String pname, String pcondition ) {
      // First create the ordinary transition
      Element ltrans = createBasicTransition( pname );

      Element lcond = mdocument.createElement( "cond" );
      ltrans.appendChild( lcond );
      lcond.setAttribute( "id", getUniqueId() );

      Element ltext = mdocument.createElement( "text" );
      lcond.appendChild( ltext );
      ltext.setAttribute( "tool", "CPN Tools" );
      ltext.setAttribute( "version", "4.0.1" );
      ltext.setTextContent( pcondition );

      return ltrans;
   }

   /**
   * Creates a substitute transition without references to its subpage.
   *
   * @param pname Text to be displayed in the substitution transition.
   * @return DOM element representing the substitution transition.
   */
   public Element createSubstitutionTransition( String pname ) {
      // First create the ordinary transition
      Element ltrans = createBasicTransition(pname);

      Element lsubs = mdocument.createElement( "subst" );
      ltrans.appendChild(lsubs);

      Element lsubpageinfo = mdocument.createElement( "subpageinfo" );
      lsubpageinfo.setAttribute( "id", getUniqueId() );
      lsubpageinfo.setIdAttribute( "id", true );
      lsubpageinfo.setAttribute( "name", pname );
      lsubs.insertBefore( lsubpageinfo, lsubs.getFirstChild() );

      return ltrans;
   }

   /**
   * Set the in-port/in-socket and out-port/out-socket pairs, and instantiate subpage.
   *
   * @param pstranstion Element of the substitution transition.
   * @param ppageid. The identifier of the actual page that the transition represents.
   * @param pinport Id of the in-port place at the subpage.
   * @param pinsocket Id of the in-socket place at the superpage.
   * @param poutport Id of the out-port place at the subpage.
   * @param poutsocket Id of the out-socket place at the superpage.
   * @return DOM element representing the substitution transition with the socket/port set.
   */
   public void connectSubstitutionPage( Element pstransition, String pinsocket, String poutsocket, String psubpageid, String pinport, String poutport ) {

      NodeList lsubslist = pstransition.getElementsByTagName( "subst" );

      if ( lsubslist.getLength() > 0 ) {
          Element lsubs = (Element) lsubslist.item( 0 );

          lsubs.setAttribute( "portsock", "(" + pinport + "," + pinsocket + ")(" + poutport + "," + poutsocket + ")" );
          lsubs.setAttribute( "subpage", psubpageid );
      }
   }

   /**
   * Set the in-port/in-socket and out-port/out-socket pairs, and instantiate subpage.
   *
   * @param pstranstion Element of the substitution transition.
   * @param pinsocket Place element of the in-socket place at the superpage.
   * @param poutsocket Place elelement of the out-socket place at the superpage.
   * @param psubpage Element of the subpage represented by the substitution transition.
   * @param pinport Place element of the subpage's in-port.
   * @param poutport Place element of the subpage's out-port.
   * @return DOM element representing the substitution transition with the socket/port set.
   */
   public void connectSubstitutionPage( Element pstransition, Element pinsocket, Element poutsocket, Element psubpage, Element pinport, Element poutport ) {

      String linsocket = pinsocket.getAttribute( "id" );
      String loutsocket = poutsocket.getAttribute( "id" );
      String lsubpageid = psubpage.getAttribute( "id" );
      String linport = pinport.getAttribute( "id" );
      String loutport = poutport.getAttribute( "id" );

      // If any parameter is null, do not perform the connection
      if ( (linsocket==null)  || (loutsocket==null) || (lsubpageid==null)|| (linport == null) || (loutport == null) )
         throw new BadCPNDefinitionException("connectSubstitutionPage: Bad null parameter");

      connectSubstitutionPage( pstransition, linsocket, loutsocket, lsubpageid, linport, loutport );
   }

   /**
   * Finds the in-port of a subpage, or null if not found.
   *
   * @param ppage A page element, which may be a subpage.
   * @return Id of the first in-port found in this page. Return null if not found.
   */
   public String getInPortPlaceId( Element ppage ) {
      NodeList lplacelist = ppage.getElementsByTagName( "place" );

      for (int i = 0; i < lplacelist.getLength(); i++ ) {
         Element lplace = (Element) lplacelist.item( i );

         NodeList lportlist = lplace.getElementsByTagName( "port" );

         // Check if the first (should be the only) element is of type IN
         if ( (lportlist.getLength() > 0 ) ) {
            
            Element lport = (Element) lportlist.item(0);
            if ( ( lport.getAttribute( "type" ) ).equals("In") ) {
               
               return lplace.getAttribute( "id" );
            }
         }
      }

      return null;
   }

   /**
   * Finds the Out-port of a subpage, or null if not found.
   *
   * @param ppage A page element, which may be a subpage.
   * @return Id of the first out-port found in this page. Return null if not found.
   */
   public String getOutPortPlaceId( Element ppage ) {
      NodeList lplacelist = ppage.getElementsByTagName( "place" );

      for (int i = 0; i < lplacelist.getLength(); i++ ) {
         Element lplace = (Element) lplacelist.item( i );

         NodeList lportlist = lplace.getElementsByTagName( "port" );

         // Check if the first (should be the only) element is of type IN
         if ( (lportlist.getLength() > 0 ) ) {
            
            Element lport = (Element) lportlist.item(0);
            if ( ( lport.getAttribute( "type" ) ).equals("Out") ) {
               return lplace.getAttribute( "id" );
            }
         }
      }


      return null;
   }

   /**
   * Creates a place with minimum information: name, type, initial marking and Id. No graphical information is present.
   * @param pname text describig the place.
   * @param ptype color set of the present place.
   * @param pinit text containing initial marking.
   * @return a basic place, without layout information.
   */
   public Element createBasicPlace(String pname, String ptype, String pinit) {

      // Create place object: 0<place id="?">
      Element lplace = mdocument.createElement( "place" );
      lplace.setAttribute( "id", getUniqueId() );
      lplace.setIdAttribute( "id", true );

      // Add the layout definitions
      //LayoutFactory.Place.setDefaultLayout( lplace);

      // Set name shown inside the box: <text>?</text>
      Element ltext = mdocument.createElement( "text" );
      ltext.setTextContent(pname);
      lplace.appendChild(ltext);

      // Create the type: 1<type id="?">
      Element ltype = mdocument.createElement( "type" );
      //LayoutFactory.Type.setDefaultLayout(ltype);
      ltype.setAttribute( "id", getUniqueId() );
      ltype.setIdAttribute( "id", true );
      // Set the type: 2<text tool="CPN Tools" version="4.0.1">?
      Element ltypetext = mdocument.createElement( "text" );
      ltypetext.setAttribute( "tool", "CPN Tools" );
      ltypetext.setAttribute( "version", "4.0.1" );
      ltypetext.setTextContent(ptype);
      ltype.appendChild(ltypetext);
      lplace.appendChild(ltype);

      // Create initial marking
      Element linitmark = mdocument.createElement( "initmark" );
      //LayoutFactory.Initmark.setDefaultLayout(linitmark);
      Element lmarktext = mdocument.createElement( "text" );
      lmarktext.setAttribute( "tool", "CPN Tools" );
      lmarktext.setAttribute( "version", "4.0.1" );
      lmarktext.setTextContent( pinit );
      linitmark.appendChild( lmarktext );
      lplace.appendChild( linitmark );

      return lplace;
   }

   /**
   * Creates a fusion place, set locally the information and add it to the fusion set.
   *
   * @param pfusionname Name of the fusion set to be added to.
   * @param pname text describig the place.
   * @param ptype color set of the present place.
   * @param pinit text containing initial marking.
   * @return an in-port place.
   */
   public Element createFusionPlace( String pfusionname, String pname, String ptype, String pinit) {

      Element lplace = createBasicPlace( pname, ptype, pinit);

      String lplaceid = lplace.getAttribute( "id" );

      Element lfusioninfo = mdocument.createElement( "fusioninfo" );
      lfusioninfo.setAttribute( "id", getUniqueId() );
      lfusioninfo.setIdAttribute( "id", true );
      lfusioninfo.setAttribute( "name", pfusionname );
      lplace.appendChild(lfusioninfo);
     
      NodeList lfusionlist = mcpnet.getElementsByTagName("fusion");
      for ( int i = 0; i < lfusionlist.getLength(); i++ ) {
         Element lfusion = (Element) lfusionlist.item( i );
         //System.out.println("Fusion set: " + lfusion.getAttribute( "name" ) + "| Local: " + pfusionname );

         // Found the element representing the fusion set. Add the place's ID.
         if ( pfusionname.equals( lfusion.getAttribute( "name" ) ) ) {
            lfusion.appendChild( createFusion_elm( lplaceid ) ); 
            break;
         }
      }

      return lplace;
   }

   /**
   * Creates a fusion place, set locally the information and add it to the fusion set. Set an empty initmark.
   *
   * @param pfusionname Name of the fusion set to be added to.
   * @param pname text describig the place.
   * @param ptype color set of the present place.
   * @return an in-port place.
   */
   public Element createFusionPlace( String pfusionname, String pname, String ptype) {
      return createFusionPlace( pfusionname, pname, ptype, "");
   }

   /**
   * Creates an in-port place with no graphical information
   *
   * @param pname text describig the place.
   * @param ptype color set of the present place.
   * @param pinit text containing initial marking.
   * @return an in-port place.
   */

   public Element createInPortPlace(String pname, String ptype, String pinit) {
      Element lplace = createBasicPlace( pname, ptype, pinit);
      
      // Create the <port> element.
      Element lport = mdocument.createElement("port");
      lplace.appendChild(lport);
      lport.setAttribute( "id", getUniqueId() );
      lport.setIdAttribute( "id", true );
      lport.setAttribute( "type", "In" );

      return lplace;
   }

   /**
   * Creates an out-port place with no graphical information
   * @param pname text describig the place.
   * @param ptype color set of the present place.
   * @param pinit text containing initial marking.
   * @return an in-port place.
   */
   public Element createOutPortPlace(String pname, String ptype, String pinit) {
      Element lplace = createBasicPlace( pname, ptype, pinit);
      
      // Create the <port> element.
      Element lport = mdocument.createElement("port");
      lport.setAttribute( "id", getUniqueId() );
      lport.setIdAttribute( "id", true );
      lport.setAttribute( "type", "Out" );

      lplace.appendChild(lport);

      return lplace;
   }

   /**
   * Create an arc element without orientation.
   *
   * @param pplaceid Identifier of the the adjacent place
   * @param ptransid Identifier of the the adjacent transiton
   * @param pexpression Arc expression
   * @return Arc element with souce being the 
   */
   private Element createBasicArc( String pplaceid, String ptransid, String pexpression) {

      Element larc =  mdocument.createElement( "arc" );
      larc.setAttribute( "id", getUniqueId() );
      larc.setIdAttribute( "id", true );
      larc.setAttribute( "order", "1" );

      // Set the adjacent place
      Element lplaceend =  mdocument.createElement( "placeend" );
      larc.appendChild(lplaceend);
      lplaceend.setAttribute( "idref", pplaceid );

      // Set the adjacent transition
      Element ltransend =  mdocument.createElement( "transend" );
      larc.appendChild(ltransend);
      ltransend.setAttribute( "idref", ptransid );

      // Set the annotation, containing the arc expression
      Element lannot =  mdocument.createElement( "annot" );
      larc.appendChild( lannot );
      lannot.setAttribute( "id", getUniqueId() );
      lannot.setIdAttribute( "id", true );

      Element ltext = mdocument.createElement( "text" );
      lannot.appendChild( ltext );
      ltext.setAttribute( "tool", "CPN Tools" );
      ltext.setAttribute( "version", "4.0.1" );
      ltext.setTextContent( pexpression );

      return larc;
   }

   /**
   * Create an arc element without orientation with default expression.
   *
   * @param pplaceid Identifier of the the adjacent place
   * @param ptransid Identifier of the the adjacent transiton
   * @return Arc element with souce being the 
   */
   private Element createBasicArc( String pplaceid, String ptransid) {
      return createBasicArc( pplaceid, ptransid, "1`()");
   }

   /**
   * Create an arc element having a place as source, and transition as destination.
   *
   * @param pplaceid Identifier of the the adjacent place
   * @param ptransid Identifier of the the adjacent transiton
   * @param pexpression Arc expression
   * @return Arc element with Plate-to-Transition orientation
   */
   public Element createArcPtoT( String pplaceid, String ptransid, String pexpression) {
      Element larc = createBasicArc( pplaceid, ptransid, pexpression );
      larc.setAttribute( "orientation", "PtoT" );

      return larc;
   }

   /**
   * Create an arc element having a place as source, and transition as destination.
   *
   * @param pplace Element of the adjacent place
   * @param ptrans Element of the adjacent transiton
   * @param pexpression Arc expression
   * @return Arc element with Plate-to-Transition orientation
   */
   public Element createArcPtoT( Element pplace, Element ptrans, String pexpression) {
      return createArcPtoT( pplace.getAttribute("id"), ptrans.getAttribute("id"), pexpression );
   }

   /**
   * Create an arc element having a transition as source, and a place as destination.
   *
   * @param ptrans Element of the adjacent transiton
   * @param pplace Element of the adjacent place
   * @param pexpression Arc expression
   * @return Arc element with Plate-to-Transition orientation
   */
   public Element createArcTtoP( String ptransid, String pplaceid, String pexpression) {
      Element larc = createBasicArc( pplaceid, ptransid, pexpression );
      larc.setAttribute( "orientation", "TtoP" );

      return larc;
   }

   /**
   * Create an arc element having a transition as source, and a place as destination.
   *
   * @param ptransid Identifier of the the adjacent transiton
   * @param pplaceid Identifier of the the adjacent place
   * @param pexpression Arc expression
   * @return Arc element with Plate-to-Transition orientation
   */
   public Element createArcTtoP( Element ptrans, Element pplace, String pexpression ) {
      return createArcTtoP( ptrans.getAttribute("id"), pplace.getAttribute("id"), pexpression );
   }

   /**
   * Create an inhibitor arc
   *
   * @param pplaceid Identifier of the the adjacent place
   * @param ptransid Identifier of the the adjacent transiton
   * @return Inhibitor arc element
   */
   public Element createInhibitorArc( String pplaceid, String ptransid) {
      Element larc = createBasicArc( pplaceid, ptransid, "");
      larc.setAttribute( "orientation", "Inhibitor" );

      return larc;
   }

   /**
   * Create an inhibitor arc
   *
   * @param pplace Element of the adjacent place
   * @param ptrans Element of the adjacent transiton
   * @return Inhibitor arc element
   */
   public Element createInhibitorArc( Element pplace, Element ptrans) {
      return createInhibitorArc( pplace.getAttribute("id"), ptrans.getAttribute("id") );
   }

   /**
   * Receives a mapping from element types and quantity and produce a string representing its marking in CPN tools format.
   *
   * @param Map between element type and quantity
   * @return Text in CPN tools format representing the marking.
   */
   public static String createMarkingText( Hashtable<String,Integer> pmarking) {
      String ltext = new String();

      // Return empty string if mapping is empty
      if (pmarking.isEmpty() ) {
         return ltext;
      } 

      Enumeration<String> e = pmarking.keys();

      String lelement = e.nextElement();
      int lamount = pmarking.get( lelement );

      // First element must *not* contain the separator ++ 
      // Elements with non-positive amount are ignored
      if ( lamount > 0 ) ltext += lamount + "`" + lelement;

      while ( e.hasMoreElements() ) {
         lelement = e.nextElement();
         lamount = pmarking.get( lelement );

         // Append "++" separator for all valid elements
         if ( lamount > 0 ) ltext += "++" + lamount + "`" + lelement;
      }

      return ltext;
   }
}
