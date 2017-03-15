package stave.cpntools;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import org.w3c.dom.Document;

/**
 * Base class for CPN Tools manipulation.
 * Contains the exceptions for error, and methods to debug
 */
public class BaseCPNCommunication {

   protected static boolean mwarnings = true;
   protected static boolean mdebug = true;

   BaseCPNCommunication() {
   }

   public static void setWarning(boolean lwarn) {
      mwarnings = lwarn;
   }

   public static boolean onWarning() {
      return mwarnings;
   }

   public static void setDebug( boolean ldebug) {
      mdebug = ldebug;
   }

   public static boolean onDebug() {
      return mdebug;
   }

   protected static void warning(String larg) {
      if (mwarnings) System.err.println("Warning - " + larg);
   }

   protected static void warning(String larg, Exception e) {
      warning( larg + "Message: " + e.getMessage());
   }

   protected static void warning(Exception e) {
      warning( e.getMessage());
   }

   protected static void debug(String larg) {
      if (mdebug) System.err.println("Debug: - " + larg);
   }

   protected static void debug(String larg, Exception e) {
      debug( larg + "Message(Exception): " + e.getMessage());
   }
 
   /* The following exception is to be used only while generating CPN tools file */

   class BadCPNDefinitionException extends RuntimeException {
      BadCPNDefinitionException(String lmessage) {
         super(lmessage);
      }
   }

   // Output the internal DOM as an XML file.
   public void outputDOMtoFile(Document ldocument, String pfilename) throws FileNotFoundException, TransformerConfigurationException, TransformerException {
      
      DOMSource ldomsource = new DOMSource(ldocument);
      // Create the object that flushes to the writer
      StreamResult loutput = new StreamResult( new PrintWriter(pfilename) );

      TransformerFactory lfactory = TransformerFactory.newInstance();
      // Default indentation is zero. Must set to the desired amount.
      lfactory.setAttribute( "indent-number", new Integer(2) );

      Transformer ltransformer = lfactory.newTransformer();

      //ltransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no" );
      ltransformer.setOutputProperty(OutputKeys.INDENT, "yes" );
      //ltransformer.setOutputProperty(OutputKeys.ENCODING, "iso-8859-1" );
      //ltransformer.setOutputProperty(OutputKeys.STANDALONE, "no" );
      if (ldocument.getDoctype() != null) {
         ltransformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, ldocument.getDoctype().getPublicId() );
         ltransformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, ldocument.getDoctype().getSystemId() );
      }

      ltransformer.transform(ldomsource, loutput);
   }
}
