//         $Id: Reference.java,v 1.3 2006/03/31 16:55:51 dah Exp $
/*
 * @(#)Reference.java
 */

package StratmasClient.object.primitive;

import StratmasClient.Debug;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.XMLHelper;

import java.util.Vector;
import org.w3c.dom.Element;

/**
 * A reference is used to reference an identifiable object (the
 * resolve mechanism is scope dependent).
 *
 * @version 1, $Date: 2006/03/31 16:55:51 $
 * @author  Daniel Ahlin
*/

public class Reference
{
    /**
     * The identifiers making up this reference.
     */
    String[] identifiers;

    /**
     * Creates a new reference.
     */
    public Reference()
    {
    }

    /**
     * TEMPORARY Creates a new reference.
     *
     * <p>author  Per Alexius
     *
     */
    public Reference(String [] ids) {
         identifiers = new String[ids.length];
         for (int i = 0; i < ids.length; i++) {
              identifiers[i] = new String(ids[i]);
         }
    }

    /**
     * TEMPORARY Creates a new reference.
     *
     * <p>author  Per Alexius
     *
     */
     public Reference(Reference scope, String id) {
          if (scope == null) {
               identifiers = new String[1];
               identifiers[0] = new String(id);
          }
          else {
               identifiers = new String[scope.identifiers.length + 1];
               identifiers[0] = new String(id);
               System.arraycopy(scope.identifiers, 0, identifiers, 1, scope.identifiers.length);
          }
     }

    /**
     * PERHAPS TEMPORARY Returns the identifier of the referenced object.
     *
     * <p>author  Per Alexius
     *
     */
     public String getIdentifier() {
          return identifiers[0];
     }

    /**
     * Returns a string representation of this object.
     */
    public String toString()
    {
         String ret = "";
         for (int i = identifiers.length - 1; i >= 0; i--) {
              ret += identifiers[i].toString() + (i == 0 ? "" : ":");
         }
         return ret;
    }

    /**
     * Returns object referenced by this reference. Tries to resolv
     * the reference in every scope up to the root.
     *
     * @param scope the scope in which this reference is to be
     * resolved or null if unable to resolv.
     */
    public StratmasObject resolve(StratmasObject scope)
    {
        // Searching upward for matching scope.
        for (StratmasObject currentScope = scope; 
             currentScope != null;
             currentScope = currentScope.getParent()) {
            // Try to resolv in this scope.
            StratmasObject candidate = 
                currentScope.getChild(identifiers[identifiers.length - 1]);
            
            if (candidate == null &&
                currentScope.getIdentifier().equals(identifiers[identifiers.length - 1])) {
                // See if the name of the scope fits, if so resolv from scope.
                candidate = currentScope;
                
            }
            if (candidate != null) {
                // Match parts of reference, going down in the tree,
                // any errore here means an unresolvable reference.
                for (int i = identifiers.length - 2; 
                     i >= 0 && candidate != null; i--) {
                    candidate = candidate.getChild(identifiers[i]);
                }
                return candidate;
            }
            
        }

        // found nothing, give up.
        return null;
    }

    /**
     * Returns object referenced by this reference. Tries to resolv
     * the reference in every scope up to the root.
     *
     * @param scope the scope in which this reference is to be
     * resolved or null if unable to resolv.
     */
    public StratmasObject debugResolve(StratmasObject scope)
    {
        // Searching upward for matching scope.
        Debug.err.println("debugResolv: Resolving " + this.toTaclanV2());
        Debug.err.println("debugResolv: Beginning upward search looking for " + 
                          Identifier.toTaclanV2(identifiers[identifiers.length - 1]));
        for (StratmasObject currentScope = scope; 
             currentScope != null;
             currentScope = currentScope.getParent()) {
            Debug.err.println("debugResolv: " + currentScope.getIdentifier());
            // Try to resolv in this scope.
            StratmasObject candidate = 
                currentScope.getChild(identifiers[identifiers.length - 1]);

            Debug.err.println("debugResolv: candidate = " + candidate);
            
            if (candidate == null &&
                currentScope.getIdentifier().equals(identifiers[identifiers.length - 1])) {
                // See if the name of the scope fits, if so resolv from scope.
                candidate = currentScope;
                
            }
            if (candidate != null) {
                // Match parts of reference, going down in the tree,
                // any errore here means an unresolvable reference.
                for (int i = identifiers.length - 2; 
                     i >= 0 && candidate != null; i--) {
                    candidate = candidate.getChild(identifiers[i]);
                }
                return candidate;
            }
            
        }

        // found nothing, give up.
        return null;
    }

    /**
     * Returns object referenced by this reference
     *
     * <p>author  Per Alexius
     *
     * @return scope The scope for this reference.
     */
    public Reference scope()
    {
         // Check for root Reference
         if (identifiers.length == 1) {
              return null;
         }
         else {
              String [] tmp = new String[identifiers.length - 1];
              
              for (int i = 1; i < identifiers.length; i++) {
                   tmp[i - 1] = identifiers[i].toString();
              }
              return new Reference(tmp);
         }
    }

    /**
     * Creates an XML representation of the body of this object.
     *
     * <p>author  Per Alexius
     *
     * @param b The StringBuffer to write to.
     * @return The StringBuffer b with an XML representation of this
     * object appended to it.
     */
    public StringBuffer bodyXML(StringBuffer b) {
         // Assuming name is at index 0, name's scope at index 1 etc.
         if (identifiers.length == 0) {
              return b;
         }
         else {
              b.append("<name>").append(XMLHelper.encodeSpecialCharacters(identifiers[0].toString())).append("</name>");
              for (int i = 1; i < identifiers.length; i++) {
                   b.append("<scope>").append("<name>").append(XMLHelper.encodeSpecialCharacters(identifiers[i].toString())).append("</name>");
              }
              for (int i = 1; i < identifiers.length; i++) {
                   b.append("</scope>");
              }
         }
         return b;
    }

    /**
     * Checks for equality. Notice that it is guaranteed that the
     * hashcode for two References A and B is equal when A.equals(B)
     * returns true.
     *
     * <p>author  Per Alexius
     *
     * @param r The Reference to compare to.
     * @return true if the References refer to the same object.
     */
     public boolean equals(Reference r) {
         if (identifiers.length != r.identifiers.length) {
             return false;
         }

         for (int i = 0; i < identifiers.length; i++) {
             if (!identifiers[i].equals(r.identifiers[i])) {
                 return false;
             }
         }
         return true;
     }

    /**
     * Checks for equality. Notice that it is guaranteed that the
     * hashcode for two References A and B is equal when A.equals(B)
     * returns true.
     *
     * <p>author  Per Alexius
     *
     * @param o The Object to compare to.
     * @return true if the References refer to the same object.
     */
     public boolean equals(Object o) {
          if (o instanceof Reference) {
               return equals((Reference)o);
          }
          else {
               return false;
          }
     }

     public int hashCode() {
          int ret = 0;
          for (int i = 0; i < identifiers.length; i++) {
               ret += identifiers[i].hashCode();
          }
          return ret;
     }

    /**
     * Tries to create a new reference from the provided
     * string. Returns null iff the supplied string is empty or if any
     * component (i. e. scope) is defined to be empty (e. g. a::b or :).
     *
     * FIXME: should throw suitable excpetion instead of returning
     * null on error.
     *
     * @param str the string to use
     */
    public static Reference parseReference(String str)
    {
        if (str.length() == 0) {
            return null;
        }

        int currentStart = 0; 
        int currentEnd = str.indexOf(':', currentStart);

        Vector res = new Vector();
        for(;currentEnd != -1; currentEnd = str.indexOf(':', currentStart)) {
//             Debug.err.println(currentStart + " to " + currentEnd + " = \"" + 
//                                str.substring(currentStart, currentEnd) + "\"");
            if (currentEnd > 0 && str.charAt(currentEnd - 1) == '\\') {
                // escaped scoper, find next
                continue;                
            }
            else {
                String part = str.substring(currentStart, currentEnd);
                if (part.length() != 0) {
                    res.add(part);
                } else {
                    return null;
                }
                currentStart = currentEnd + 1;
                if (currentStart >= str.length()) {
                    // Means that the string ended with a ':'
                    return null;
                }
            }
        }
        // Add the final component:
        String part = str.substring(currentStart);
        if (part.length() != 0) {
            res.add(part);
        } else {
            return null;
        }

        String[] ids = new String[res.size()];
        
        for (int i = 0; i < res.size(); i++) {
            ids[ids.length - (i + 1)] = (String) res.get(i);
        }
        
        return new Reference(ids);
    }

    /**
     * Returns the length of the reference (i. e. number of
     * components)
     */
    public int getLength()
    {
        return identifiers.length;
    }
    
    
    /**
     * Returns a String holding this object in its Taclan V2
     * representation.
     */
    public String toTaclanV2()
    {
        String ret = "";
        for (int i = identifiers.length - 1; i >= 0; i--) {
            ret += Identifier.toTaclanV2(identifiers[i]) + (i == 0 ? "" : ":");
        }
        return ret;
    }

    /**
     * Clones this object. Notice that the Identifier is NOT
     * cloned. Both the clone and the original object will thus keep a
     * reference to the same Identifier object.
     *
     * <p> author Per Alexius
     *
     * @return A clone of this object.
     */
     public Object clone() {
          String [] tmp = new String[identifiers.length];
          for (int i = 0; i < identifiers.length; i++) {
               tmp[i] = identifiers[i].toString();
          }
          return new Reference(tmp);
     }

    /**
     * Gets a Reference from the element n.
     *
     * @param n The parent Node.
     * @return The newly created Reference.
     */
    public static Reference getReference(Element n) 
    {
        String name = XMLHelper.getString(n, "name");
        Element scope = XMLHelper.getFirstChildByTag(n, "scope");
        if (scope == null) {
            return new Reference(null, name);
        }
        else {
            return new Reference(getReference(scope), name);
        }
    }
}
