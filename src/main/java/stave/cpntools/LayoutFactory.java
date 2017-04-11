/**
 * Helper class that creates new DOM elements for CPN Tools elements,
 * such as places, transitions, arcs and subpages,
 *
 * @author Pedro de Carvalho Gomes <pedrodcg@csc.kth.se>
 *
 * TODO: Switch to dynamic dispatching, especially for setPosition
 */

package stave.cpntools;

import java.util.Collection;
import java.util.Iterator;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class LayoutFactory {

    /**
     * This class generates 1-dimension coordiantes spreading elements evenly.
     */
    public static class Spacer {

        // space between elements
        private int mspace = 126;
        // next element position
        private int mcurrent = 0;
        // translation of the coordinates w.r.t to center of the axis
        private int mtranslation = 0;

        protected Spacer(int pspace, int pinit) {
            mspace = pspace;
            mcurrent = pinit;
        }

        protected Spacer(int pspace, int pinit, int ptranslation) {
            mspace = pspace;
            mcurrent = pinit;
            mtranslation = ptranslation;
        }

        /**
         * Returns the next
         *
         * @return next uni-dimensional coordinate
         */
        public int getNext() {
            // Return the previous value, but before calculate the next one.
            int lold = mcurrent;

            if (mcurrent <= 0) {
                // Increase at negative values
                mcurrent = ((-1) * (mcurrent - mspace)) + mtranslation;
            } else {
                mcurrent = ((-1) * mcurrent) + mtranslation;
            }

            return lold;
        }
    }

    static abstract class DOMElement {

        // Create the common tags for different types
        static void setFillattr(Element pelement, String pcolour, String ppattern, String pfilled) {
            Element lfillattr = pelement.getOwnerDocument().createElement("fillattr");
            lfillattr.setAttribute("colour", pcolour);
            lfillattr.setAttribute("pattern", ppattern);
            lfillattr.setAttribute("filled", pfilled);
            pelement.insertBefore(lfillattr, pelement.getFirstChild());
        }

        static void setLineattr(Element pelement, String pcolour, String pthick, String ptype) {
            Element llineattr = pelement.getOwnerDocument().createElement("lineattr");
            llineattr.setAttribute("colour", pcolour);
            llineattr.setAttribute("thick", pthick);
            llineattr.setAttribute("type", ptype);
            pelement.insertBefore(llineattr, pelement.getFirstChild());
        }

        static void setTextattr(Element pelement, String pcolour, String pbold) {
            Element ltextattr = pelement.getOwnerDocument().createElement("textattr");
            ltextattr.setAttribute("colour", pcolour);
            ltextattr.setAttribute("bold", pbold);
            pelement.insertBefore(ltextattr, pelement.getFirstChild());
        }

        static void setPosattr(Element pelement, String px, String py) {
            Element lposattr = pelement.getOwnerDocument().createElement("posattr");
            lposattr.setAttribute("x", px);
            lposattr.setAttribute("y", py);
            pelement.insertBefore(lposattr, pelement.getFirstChild());
        }

        /**
         *  This method is overwritten if an element's coordiantion must be propagated to subelements.
         */
        static void setPosition(Element pelement, int px, int py) {
            setPosattr(pelement, Integer.toString(px), Integer.toString(py));
        }
    }

   /* Define one class with default values for each type of element */

    public static class Place extends DOMElement {

        /* Place elements' standard attributes */
        final static String fillattr_colour = "White";
        final static String fillattr_pattern = "";
        final static String fillattr_filled = "false";
        final static String lineattr_colour = "Black";
        final static String lineattr_thick = "1";
        final static String lineattr_type = "Solid";
        final static String textattr_colour = "Black";
        final static String textattr_bold = "false";
        final static String ellipse_w = "60.000000";
        final static String ellipse_h = "40.000000";
        final static String token_x = "-10.000000";
        final static String token_y = "0.000000";
        final static String marking_x = "0.000000";
        final static String marking_y = "0.000000";
        final static String marking_hidden = "false";
        final static String snap_id = "0";
        // Replaced '.' with last "_"
        final static String snap_anchor_horizontal = "0";
        // Replaced '.' with last "-"
        final static String snap_anchor_vertical = "0";

        static void setDefaultLayout(Element pplace) {
            // Set the common tags
            setFillattr(pplace, fillattr_colour, fillattr_pattern, fillattr_filled);
            setLineattr(pplace, lineattr_colour, lineattr_thick, lineattr_type);
            setTextattr(pplace, textattr_colour, textattr_bold);

            // Set the specific tags:
            // - shape
            Element lellipse = pplace.getOwnerDocument().createElement("ellipse");
            lellipse.setAttribute("w", ellipse_w);
            lellipse.setAttribute("h", ellipse_h);
            pplace.insertBefore(lellipse, pplace.getFirstChild());
            // - token type position
            Element ltoken = pplace.getOwnerDocument().createElement("token");
            ltoken.setAttribute("x", token_x);
            ltoken.setAttribute("y", token_y);
            pplace.insertBefore(ltoken, pplace.getFirstChild());
            // - marking position
            Element lmarking = pplace.getOwnerDocument().createElement("marking");
            lmarking.setAttribute("x", marking_x);
            lmarking.setAttribute("y", marking_y);
            pplace.insertBefore(lmarking, pplace.getFirstChild());

            // - type
            NodeList ltype = pplace.getElementsByTagName("type");
            Type.setDefaultLayout((Element) ltype.item(0));

            // - initmark
            NodeList linitmark = pplace.getElementsByTagName("initmark");
            Initmark.setDefaultLayout((Element) linitmark.item(0));
        }

        // Set the place's position
        static void setPosition(Element pplace, int px, int py) {

            setPosattr(pplace, Integer.toString(px), Integer.toString(py));

            // Find the type element, and set the position relative to place's
            NodeList ltype = pplace.getElementsByTagName("type");
            if (ltype.getLength() > 0) {
                Type.setPosition((Element) ltype.item(0), px + 50, py - 25);
            }

            // Find the initmark element, and set the position relative to place's
            NodeList linitmark = pplace.getElementsByTagName("initmark");
            if (linitmark.getLength() > 0) {
                Initmark.setPosition((Element) linitmark.item(0), px + 50, py + 25);
            }
        }

        public static void setLayoutAndPosition(Element pelement, int px, int py) {
            setDefaultLayout(pelement);
            setPosition(pelement, px, py);
        }

        /**
         * Take a list of elements and distribute them horizontally with equal spacing, starting from 0.
         * If the number of elements is odd, the first element is placed centrally;
         * otherwise the elements are places simmetrically to the axis.
         *
         * @param plist DOM elements to be distributed
         * @param py Fixed coordinate in the Y-axis
         * @param pspace Distance between two elements
         */
        public static void distributeHorizontally(Collection<Element> plist, int py, int pspace) {
            // Check if number of elements is even or odd
            int lfirst = ((plist.size() & 1) == 0) ? (pspace / 2) : 0;

            Spacer lspacer = new Spacer(pspace, lfirst);

            for (Iterator<Element> i = plist.iterator(); i.hasNext(); ) {
                setPosition(i.next(), lspacer.getNext(), py);
            }
        }
    }

    public static class FusionPlace extends Place {

        /* Place elements' standard attributes */
        final static String fillattr_colour = "White";
        final static String fillattr_pattern = "Solid";
        final static String fillattr_filled = "false";
        final static String lineattr_colour = "Black";
        final static String lineattr_thick = "0";
        final static String lineattr_type = "Solid";
        final static String textattr_colour = "Black";
        final static String textattr_bold = "false";

        static void setDefaultLayout(Element pplace) {
            Place.setDefaultLayout(pplace);

            // Add layout for the port tag.
            NodeList lportlist = pplace.getElementsByTagName("fusioninfo");
            if (lportlist.getLength() > 0) {
                Element lport = (Element) lportlist.item(0);
                setFillattr(lport, fillattr_colour, fillattr_pattern, fillattr_filled);
                setLineattr(lport, lineattr_colour, lineattr_thick, lineattr_type);
                setTextattr(lport, textattr_colour, textattr_bold);
            }
        }

        /**
         * Set the position of arcs in a given collection.
         *
         * @param ppage Distance between two elements
         */
        public static void setDefaultLayout(Iterable<Element> plist) {
            for (Iterator<Element> i = plist.iterator(); i.hasNext(); ) {
                Element pelement = i.next();
                setDefaultLayout(pelement);
            }
        }

        static void setPosition(Element pplace, int px, int py) {
            Place.setPosition(pplace, px, py);

            // Add layout for the fusion tag.
            NodeList lfusionlist = pplace.getElementsByTagName("fusioninfo");
            if (lfusionlist.getLength() > 0) {
                Element lfusion = (Element) lfusionlist.item(0);
                setPosattr(lfusion, Integer.toString(px), Integer.toString(py - 20));
            }
        }

        public static void setLayoutAndPosition(Element pelement, int px, int py) {
            setDefaultLayout(pelement);
            setPosition(pelement, px, py);
        }

        /**
         * Take a list of elements and distribute them horizontally with equal spacing, starting from 0.
         * If the number of elements is odd, the first element is placed centrally;
         * otherwise the elements are places simmetrically to the axis.
         *
         * @param plist DOM elements to be distributed
         * @param py Fixed coordinate in the Y-axis
         * @param pspace Distance between two elements
         */
        public static void distributeHorizontally(Collection<Element> plist, int py, int pspace) {
            // Check if number of elements is even or odd
            int lfirst = ((plist.size() & 1) == 0) ? (pspace / 2) : 0;

            Spacer lspacer = new Spacer(pspace, lfirst);

            for (Iterator<Element> i = plist.iterator(); i.hasNext(); ) {
                setPosition(i.next(), lspacer.getNext(), py);
            }
        }
    }

    public static class PortPlace extends Place {

        /* Place elements' standard attributes */
        final static String fillattr_colour = "White";
        final static String fillattr_pattern = "Solid";
        final static String fillattr_filled = "false";
        final static String lineattr_colour = "Black";
        final static String lineattr_thick = "0";
        final static String lineattr_type = "Solid";
        final static String textattr_colour = "Black";
        final static String textattr_bold = "false";

        public static void setDefaultLayout(Element pplace) {
            Place.setDefaultLayout(pplace);

            // Add layout for the port tag.
            NodeList lportlist = pplace.getElementsByTagName("port");
            if (lportlist.getLength() > 0) {
                Element lport = (Element) lportlist.item(0);
                setFillattr(lport, fillattr_colour, fillattr_pattern, fillattr_filled);
                setLineattr(lport, lineattr_colour, lineattr_thick, lineattr_type);
                setTextattr(lport, textattr_colour, textattr_bold);
            }
        }

        public static void setPosition(Element pplace, int px, int py) {
            Place.setPosition(pplace, px, py);

            // Add layout for the port tag.
            NodeList lportlist = pplace.getElementsByTagName("port");
            if (lportlist.getLength() > 0) {
                Element lport = (Element) lportlist.item(0);
                setPosattr(lport, Integer.toString(px - 25), Integer.toString(py - 20));
            }
        }

        public static void setLayoutAndPosition(Element pelement, int px, int py) {
            setDefaultLayout(pelement);
            setPosition(pelement, px, py);
        }

        /**
         * Take a list of elements and distribute them horizontally with equal spacing, starting from 0.
         * If the number of elements is odd, the first element is placed centrally;
         * otherwise the elements are places simmetrically to the axis.
         *
         * @param plist DOM elements to be distributed
         * @param py Fixed coordinate in the Y-axis
         * @param pspace Distance between two elements
         */
        public static void distributeHorizontally(Collection<Element> plist, int py, int pspace) {
            // Check if number of elements is even or odd
            int lfirst = ((plist.size() & 1) == 0) ? (pspace / 2) : 0;

            Spacer lspacer = new Spacer(pspace, lfirst);

            for (Iterator<Element> i = plist.iterator(); i.hasNext(); ) {
                setPosition(i.next(), lspacer.getNext(), py);
            }
        }
    }

    static class Type extends DOMElement {

        /* Place elements' standard attributes */
        final static String fillattr_colour = "White";
        final static String fillattr_pattern = "Solid";
        final static String fillattr_filled = "false";
        final static String lineattr_colour = "Black";
        final static String lineattr_thick = "0";
        final static String lineattr_type = "Solid";
        final static String textattr_colour = "Black";
        final static String textattr_bold = "false";

        public static void setDefaultLayout(Element pplace) {
            // Set the common tags
            setFillattr(pplace, fillattr_colour, fillattr_pattern, fillattr_filled);
            setLineattr(pplace, lineattr_colour, lineattr_thick, lineattr_type);
            setTextattr(pplace, textattr_colour, textattr_bold);
        }
    }

    static class Initmark extends DOMElement {

        /* Place elements' standard attributes */
        final static String fillattr_colour = "White";
        final static String fillattr_pattern = "Solid";
        final static String fillattr_filled = "false";
        final static String lineattr_colour = "Black";
        final static String lineattr_thick = "0";
        final static String lineattr_type = "Solid";
        final static String textattr_colour = "Black";
        final static String textattr_bold = "false";

        public static void setDefaultLayout(Element pplace) {
            // Set the common tags
            setFillattr(pplace, fillattr_colour, fillattr_pattern, fillattr_filled);
            setLineattr(pplace, lineattr_colour, lineattr_thick, lineattr_type);
            setTextattr(pplace, textattr_colour, textattr_bold);
        }
    }

    public static class Transition extends DOMElement {

        /* Transition elements' standard attributes */
        final static String fillattr_colour = "White";
        final static String fillattr_pattern = "";
        final static String fillattr_filled = "false";
        final static String lineattr_colour = "Black";
        final static String lineattr_thick = "1";
        final static String lineattr_type = "Solid";
        final static String textattr_colour = "Black";
        final static String textattr_bold = "false";
        final static String box_w = "60.000000";
        final static String box_h = "40.000000";

        public static void setDefaultLayout(Element ptrans) {
            // Set the common tags
            setFillattr(ptrans, fillattr_colour, fillattr_pattern, fillattr_filled);
            setLineattr(ptrans, lineattr_colour, lineattr_thick, lineattr_type);
            setTextattr(ptrans, textattr_colour, textattr_bold);

            // Set the specific tags:
            // - shape
            Element lbox = ptrans.getOwnerDocument().createElement("box");
            lbox.setAttribute("w", box_w);
            lbox.setAttribute("h", box_h);
            ptrans.insertBefore(lbox, ptrans.getFirstChild());
        }

        // Set the place's position
        public static void setPosition(Element ptrans, int px, int py) {
            setPosattr(ptrans, Integer.toString(px), Integer.toString(py));
        }

        public static void setLayoutAndPosition(Element pelement, int px, int py) {
            setDefaultLayout(pelement);
            setPosition(pelement, px, py);
        }

        /**
         * Take a list of elements and distribute them horizontally with equal spacing,
         * starting from 0. If the number of elements is odd, the first element is placed centrally;
         * otherwise the elements are places simmetrically to the axis.
         *
         * @param plist DOM elements to be distributed
         * @param py Fixed coordinate in the Y-axis
         * @param pspace Distance between two elements
         */
        public static void distributeHorizontally(Collection<Element> plist, int py, int pspace) {
            // Check if number of elements is even or odd
            int lfirst = ((plist.size() & 1) == 0) ? (pspace / 2) : 0;

            Spacer lspacer = new Spacer(pspace, lfirst);

            for (Iterator<Element> i = plist.iterator(); i.hasNext(); ) {
                setPosition(i.next(), lspacer.getNext(), py);
            }
        }
    }

    public static class ConditionTransition extends Transition {

        /* Substution transition elements' standard attributes */
        final static String fillattr_colour = "White";
        final static String fillattr_pattern = "solid";
        final static String fillattr_filled = "false";
        final static String lineattr_colour = "Black";
        final static String lineattr_thick = "0";
        final static String lineattr_type = "Solid";
        final static String textattr_colour = "Black";
        final static String textattr_bold = "false";

        public static void setDefaultLayout(Element ptrans) {
            Transition.setDefaultLayout(ptrans);

            // Add layout for the subpageinfo.
            NodeList lsubpageinfolist = ptrans.getElementsByTagName("subpageinfo");
            if (lsubpageinfolist.getLength() > 0) {

                Element lsubpageinfo = (Element) lsubpageinfolist.item(0);

                setFillattr(lsubpageinfo, fillattr_colour, fillattr_pattern, fillattr_filled);
                setLineattr(lsubpageinfo, lineattr_colour, lineattr_thick, lineattr_type);
                setTextattr(lsubpageinfo, textattr_colour, textattr_bold);
            }
        }

        // Set the place's position
        public static void setPosition(Element ptrans, int px, int py) {
            Transition.setPosition(ptrans, px, py);

            // Add layout for the subpageinfo.
            NodeList lsubpageinfolist = ptrans.getElementsByTagName("cond");
            if (lsubpageinfolist.getLength() > 0) {

                Element lsubpageinfo = (Element) lsubpageinfolist.item(0);
                setPosattr(lsubpageinfo, Integer.toString(px), Integer.toString(py + 25));

            }
        }

        public static void setLayoutAndPosition(Element pelement, int px, int py) {
            setDefaultLayout(pelement);
            setPosition(pelement, px, py);
        }

        /**
         * Take a list of elements and distribute them horizontally with equal spacing, starting from 0.
         * If the number of elements is odd, the first element is placed centrally;
         * otherwise the elements are places simmetrically to the axis.
         *
         * @param plist DOM elements to be distributed
         * @param py Fixed coordinate in the Y-axis
         * @param pspace Distance between two elements
         */
        public static void distributeHorizontally(Collection<Element> plist, int py, int pspace) {
            // Check if number of elements is even or odd
            int lfirst = ((plist.size() & 1) == 0) ? (pspace / 2) : 0;

            Spacer lspacer = new Spacer(pspace, lfirst);

            for (Iterator<Element> i = plist.iterator(); i.hasNext(); ) {
                setPosition(i.next(), lspacer.getNext(), py);
            }
        }
    }

    public static class SubstitutionTransition extends Transition {

        /* Substution transition elements' standard attributes */
        final static String fillattr_colour = "White";
        final static String fillattr_pattern = "solid";
        final static String fillattr_filled = "false";
        final static String lineattr_colour = "Black";
        final static String lineattr_thick = "0";
        final static String lineattr_type = "Solid";
        final static String textattr_colour = "Black";
        final static String textattr_bold = "false";

        public static void setDefaultLayout(Element ptrans) {
            Transition.setDefaultLayout(ptrans);

            // Add layout for the subpageinfo.
            NodeList lsubpageinfolist = ptrans.getElementsByTagName("subpageinfo");
            if (lsubpageinfolist.getLength() > 0) {

                Element lsubpageinfo = (Element) lsubpageinfolist.item(0);

                setFillattr(lsubpageinfo, fillattr_colour, fillattr_pattern, fillattr_filled);
                setLineattr(lsubpageinfo, lineattr_colour, lineattr_thick, lineattr_type);
                setTextattr(lsubpageinfo, textattr_colour, textattr_bold);
            }
        }

        // Set the place's position
        public static void setPosition(Element ptrans, int px, int py) {
            Transition.setPosition(ptrans, px, py);

            // Add layout for the subpageinfo.
            NodeList lsubpageinfolist = ptrans.getElementsByTagName("subpageinfo");
            if (lsubpageinfolist.getLength() > 0) {

                Element lsubpageinfo = (Element) lsubpageinfolist.item(0);
                setPosattr(lsubpageinfo, Integer.toString(px), Integer.toString(py - 25));

            }
        }

        public static void setLayoutAndPosition(Element pelement, int px, int py) {
            setDefaultLayout(pelement);
            setPosition(pelement, px, py);
        }

        /**
         * Take a list of elements and distribute them horizontally with equal spacing, starting from 0.
         * If the number of elements is odd, the first element is placed centrally;
         * otherwise the elements are places simmetrically to the axis.
         *
         * @param plist DOM elements to be distributed
         * @param py Fixed coordinate in the Y-axis
         * @param pspace Distance between two elements
         */
        public static void distributeHorizontally(Collection<Element> plist, int py, int pspace) {
            // Check if number of elements is even or odd
            int lfirst = ((plist.size() & 1) == 0) ? (pspace / 2) : 0;

            Spacer lspacer = new Spacer(pspace, lfirst);

            for (Iterator<Element> i = plist.iterator(); i.hasNext(); ) {
                setPosition(i.next(), lspacer.getNext(), py);
            }
        }
    }

    public static class Arc extends DOMElement {

        /* Arc elements' standard attributes */
        final static String fillattr_colour = "White";
        final static String fillattr_pattern = "";
        final static String fillattr_filled = "false";
        final static String lineattr_colour = "Black";
        final static String lineattr_thick = "1";
        final static String lineattr_type = "Solid";
        final static String textattr_colour = "Black";
        final static String textattr_bold = "false";

        final static String arrowattr_headsize = "1.200000";
        final static String arrowattr_currentcyckle = "2";

        public static void setDefaultLayout(Element parc) {
            // Set the common tags
            setFillattr(parc, fillattr_colour, fillattr_pattern, fillattr_filled);
            setLineattr(parc, lineattr_colour, lineattr_thick, lineattr_type);
            setTextattr(parc, textattr_colour, textattr_bold);

            // Set the specific tags:
            // - posattr: its always zero
            setPosattr(parc, "0", "0");
            // - arrowattr
            Element larrowattr = parc.getOwnerDocument().createElement("arrowattr");
            larrowattr.setAttribute("headsize", arrowattr_headsize);
            larrowattr.setAttribute("currentcyckle", arrowattr_currentcyckle);
            parc.insertBefore(larrowattr, parc.getFirstChild());
        }

        // Set the annotation's position, not the arc itself
        public static void setPosition(Element parc) {

            // Find positions of its respective transition and place
            NodeList lplacelist = parc.getElementsByTagName("placeend");
            NodeList ltranslist = parc.getElementsByTagName("transend");

            // Both should ends should be found. Otherwise exit.
            if (lplacelist.getLength() < 1 || ltranslist.getLength() < 1) {
                return;
            }

            // Fetch the place's position
            String lplaceid = ((Element) lplacelist.item(0)).getAttribute("idref");
            String ltransid = ((Element) ltranslist.item(0)).getAttribute("idref");

            Element lplace = parc.getOwnerDocument().getElementById(lplaceid);
            Element ltrans = parc.getOwnerDocument().getElementById(ltransid);

            // Null denotes that such attribute was not set.
            if (lplace == null || ltrans == null) {
                return;
            }

            // Get the posattr element
            NodeList lplaceposattrlist = lplace.getElementsByTagName("posattr");
            NodeList ltransposattrlist = ltrans.getElementsByTagName("posattr");

            // Both should ends should be found. Otherwise exit.
            if (lplaceposattrlist.getLength() < 1 || ltransposattrlist.getLength() < 1) {
                return;
            }

            // calculate medium
            int lx = (Integer.parseInt(((Element) lplaceposattrlist.item(0)).getAttribute("x")) +
                    Integer.parseInt(((Element) ltransposattrlist.item(0)).getAttribute("x"))) / 2;
            // calculate medium
            int ly = (Integer.parseInt(((Element) lplaceposattrlist.item(0)).getAttribute("y")) +
                    Integer.parseInt(((Element) ltransposattrlist.item(0)).getAttribute("y"))) / 2;

            // Add layout for the subpageinfo.
            NodeList lannotlist = parc.getElementsByTagName("annot");
            if (lannotlist.getLength() > 0) {

                Element lannot = (Element) lannotlist.item(0);

                setPosattr(lannot, Integer.toString(lx), Integer.toString(ly));
            }
        }

        public static void setLayoutAndPosition(Element pelement) {
            setDefaultLayout(pelement);
            setPosition(pelement);
        }

        /**
         * Set the position of arcs in a given collection.
         *
         * @param ppage Distance between two elements
         */
        public static void setLayoutAndPosition(Iterable<Element> plist) {
            for (Iterator<Element> i = plist.iterator(); i.hasNext(); ) {
                Element pelement = i.next();
                setDefaultLayout(pelement);
                setPosition(pelement);
            }
        }

        /**
         * Set the position of arcs in a whole page.
         *
         * @param ppage Distance between two elements
         */
        public static void positionAllArcs(Element ppage) {

            NodeList larclist = ppage.getElementsByTagName("arc");

            for (int i = 0; i < larclist.getLength(); i++) {
                setPosition((Element) larclist.item(i));
            }
        }

    }

    public static class InhibitorArc extends Arc {

        /* Arc elements' standard attributes */
        final static String fillattr_colour = "White";
        final static String fillattr_pattern = "";
        final static String fillattr_filled = "false";
        final static String lineattr_colour = "Black";
        final static String lineattr_thick = "1";
        final static String lineattr_type = "Solid";
        final static String textattr_colour = "Black";
        final static String textattr_bold = "false";

        final static String arrowattr_headsize = "1.200000";
        final static String arrowattr_currentcyckle = "2";

        public static void setDefaultLayout(Element parc) {
            // Set the common tags
            setFillattr(parc, fillattr_colour, fillattr_pattern, fillattr_filled);
            setLineattr(parc, lineattr_colour, lineattr_thick, lineattr_type);
            setTextattr(parc, textattr_colour, textattr_bold);

            // Set the specific tags:
            // - posattr: its always zero
            setPosattr(parc, "0", "0");
            // - arrowattr
            Element larrowattr = parc.getOwnerDocument().createElement("arrowattr");
            larrowattr.setAttribute("headsize", arrowattr_headsize);
            larrowattr.setAttribute("currentcyckle", arrowattr_currentcyckle);
            parc.insertBefore(larrowattr, parc.getFirstChild());
        }

        // Set the annotation's position, not the arc itself
        public static void setPosition(Element parc) {

            // Find positions of its respective transition and place
            NodeList lplacelist = parc.getElementsByTagName("placeend");
            NodeList ltranslist = parc.getElementsByTagName("transend");

            // Both should ends should be found. Otherwise exit.
            if (lplacelist.getLength() < 1 || ltranslist.getLength() < 1) {
                return;
            }

            // Fetch the place's position
            String lplaceid = ((Element) lplacelist.item(0)).getAttribute("idref");
            String ltransid = ((Element) ltranslist.item(0)).getAttribute("idref");

            Element lplace = parc.getOwnerDocument().getElementById(lplaceid);
            Element ltrans = parc.getOwnerDocument().getElementById(ltransid);

            // Null denotes that such attribute was not set.
            if (lplace == null || ltrans == null) {
                return;
            }

            // Get the posattr element
            NodeList lplaceposattrlist = lplace.getElementsByTagName("posattr");
            NodeList ltransposattrlist = ltrans.getElementsByTagName("posattr");

            // Both should ends should be found. Otherwise exit.
            if (lplaceposattrlist.getLength() < 1 || ltransposattrlist.getLength() < 1) {
                return;
            }

            // calculate medium
            int lx = (Integer.parseInt(((Element) lplaceposattrlist.item(0)).getAttribute("x")) +
                    Integer.parseInt(((Element) ltransposattrlist.item(0)).getAttribute("x"))) / 2;
            // calculate medium
            int ly = (Integer.parseInt(((Element) lplaceposattrlist.item(0)).getAttribute("y")) +
                    Integer.parseInt(((Element) ltransposattrlist.item(0)).getAttribute("y"))) / 2;

            // Add layout for the subpageinfo.
            NodeList lannotlist = parc.getElementsByTagName("annot");
            if (lannotlist.getLength() > 0) {

                Element lannot = (Element) lannotlist.item(0);

                setPosattr(lannot, Integer.toString(lx), Integer.toString(ly));
            }
        }

        public static void setLayoutAndPosition(Element pelement) {
            setDefaultLayout(pelement);
            setPosition(pelement);
        }

        /**
         * Set the position of arcs in a whole page.
         *
         * @param ppage Distance between two elements
         */
        public static void positionAllArcs(Element ppage) {

            NodeList larclist = ppage.getElementsByTagName("arc");

            for (int i = 0; i < larclist.getLength(); i++) {
                setPosition((Element) larclist.item(i));
            }
        }
    }
}
