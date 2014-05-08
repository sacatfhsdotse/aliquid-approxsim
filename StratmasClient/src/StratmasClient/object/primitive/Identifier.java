// $Id: Identifier.java,v 1.2 2006/03/31 16:55:51 dah Exp $
/*
 * @(#)Identifier.java
 */

package StratmasClient.object.primitive;

import org.w3c.dom.Element;

/**
 * An identifier used to identify an identifiable object within its scope.
 * 
 * @version 1, $Date: 2006/03/31 16:55:51 $
 * @author Daniel Ahlin
 */

public class Identifier {
    /**
     * Stop creation of identifiers
     */
    private Identifier() {}

    /**
     * Creates a new anonymous identifier.
     */
    public static String getAnonymous() {
        return "";
    }

    /**
     * Returns true if this identifier is anonymous.
     */
    public static boolean isAnonymous(String string) {
        return string.equals("");
    }

    /**
     * Returns a String holding this object in its Taclan V2 representation.
     */
    public static String toTaclanV2(String identifier) {
        if (identifier.matches("[A-Za-z_][A-Za-z_0-9]*")) {
            // Wordlike identifier - check for keywords.
            if (identifier.equals("import") || identifier.equals("from")
                    || identifier.equals("as") || identifier.equals("true")
                    || identifier.equals("false")) {
                return "'" + identifier + "'";
            } else {
                return identifier;
            }
        } else {
            // Wash a bit.
            String wash = identifier;
            wash = wash.replaceAll("\\\\", "\\\\"); // replaceAll(regex, string)
            wash = wash.replaceAll("\n", "\\\\n");
            wash = wash.replaceAll("'", "\\\\'");

            return "'" + wash + "'";
        }
    }

    /**
     * Gets an Identifier from the element n.
     * 
     * @param n The Element to get the object from.
     * @return The newly created Identifier
     */
    public static String getIdentifier(Element n) {
        String id = n.getAttribute("identifier");
        if (id.equals("")) {
            id = n.getTagName();
        }
        return id;
    }
}
