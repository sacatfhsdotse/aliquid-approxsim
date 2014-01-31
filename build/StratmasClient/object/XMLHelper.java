// $Id: XMLHelper.java,v 1.1 2006/03/31 16:55:51 dah Exp $

package StratmasClient.object;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.TypeInfo;

import java.util.Vector;

import StratmasClient.object.type.TypeFactory;
import StratmasClient.object.type.Type;

/**
 * XML helper functions.
 *
 * @version 1, $Date: 2006/03/31 16:55:51 $
 * @author Per Alexius, Daniel Ahlin
 */
public class XMLHelper
{
    /**
     * Gets the type of StratmasObject represented by the provided
     * dom element. Casts the provided Element to an ElementImpl to
     * avoid the problem that the getSchemaTypeInfo method does not
     * exist in 1.4.2. This is not a perfect solution but rather an
     * acceptable hack until 1.5 becomed default version. If the
     * getSchemaTypeInfo method fails the xsi:type attribute is
     * checked. This will occur when validation is switched off,
     * which it often is for performance reasons.
     *
     * @param element The dom element to get the type for.
     * @return The type of StratmasObject the element represents.
     */
    public static Type getType(Element element)
    {
	TypeInfo typeInfo = 
	    ((org.apache.xerces.dom.ElementImpl)element).getSchemaTypeInfo();
	Type ret = TypeFactory.getType(typeInfo.getTypeName(), 
				       typeInfo.getTypeNamespace());
	if (ret == null) {
	    String[] nsType = element.getAttribute("xsi:type").split(":");
	    if (nsType.length == 1) {
		ret = TypeFactory.getType(nsType[0]);
	    } else {
		ret = TypeFactory.getType(nsType[1]);
	    }

	    if (ret == null) {
		throw new AssertionError("getType() failed for element " + 
					 element.getTagName() + 
					 ". This may indicate that " + 
					 "validation is switched off " + 
					 "and that the xsi:type attribute " + 
					 "is missing.");
	    }
	}
	return ret;
    }

    /**
     * Gets the first child Element of n that has a tag matching 'tag'.
     *
     * @param n The Node from which to get the Element
     * @return The first child Element of n that has a tag matching
     * 'tag' or null if there was no such Element.
     */
    public static Element getFirstChildByTag(Node n, String tag)
    {
	for (Node child = n.getFirstChild(); 
	     child != null; 
	     child = child.getNextSibling()) {
	    if (child.getNodeType() == Node.ELEMENT_NODE) {
		Element elem = (Element)child;
		if (tag.equals(elem.getTagName())) {
		    return elem;
		}
	    }
	}
	return null;
    }

    /**
     * Gets child Elements of n that has a tag matching 'tag'.
     *
     * @param n The Node from which to get the Element
     * @param tag The tag.
     * @return A Vector containing all elements with the specified
     * tag.
     */
    public static Vector getChildElementsByTag(Node n, String tag)
    {
	Vector ret = new Vector();
	for (Node child = n.getFirstChild(); child != null; 
	     child = child.getNextSibling()) {
	    if (child.getNodeType() == Node.ELEMENT_NODE) {
		Element elem = (Element)child;
		if (tag.equals(elem.getTagName())) {
		    ret.add(elem);
		    }
	    }
	}
	return ret;
    }

    /**
     * Gets the String contained in the first TEXT_NODE child of
     * n. For example, if we know that n has an element with tag TAG,
     * this method returns the value VALUE between the tags
     * '<TAG>VALUE</TAG>'.
     *
     * @param n The Node from which to get the String
     * @return The String contained in the first TEXT_NODE child of n.
     */
    public static String getString(Node n)
    {
	for (Node child = n.getFirstChild(); 
	     child != null; child = child.getNextSibling()) {
	    if (child.getNodeType() == Node.TEXT_NODE) {
		return child.getNodeValue();
	    }
	}
	return null;
    }
     
    /**
     * Gets the value of an element of type xsd:string with tag 'tag'
     * that is a child of n.
     *
      * @param n The parent Node.
      * @return The value of the string.
      */
    public static String getString(Node n, String tag) 
    {
	Element elem = getFirstChildByTag(n, tag);
	if (elem == null) {
	    return null;
	} else {
	    return getString(elem);
	}
    }
     
    /**
     * Gets the value of an element of type xsd:boolean with tag 'tag'
     * that is a child of n.
     *
     * @param n The parent Node.
     * @return The value of the bool.
     */
    public static boolean getBoolean(Node n, String tag) 
    {
	String val = getString(n, tag);
	return (val.equalsIgnoreCase("true") || val.equals("1"));
    }

    /**
     * Gets the value of an element of type xsd:double with tag 'tag'
     * that is a child of n.
     *
     * @param n The parent Node.
     * @return The value of the xsd:double as a double.
     */
    public static double getDouble(Node n, String tag) 
    {
	String tmp = getString(n, tag);
	if (tmp.equals("INF")) {
	    tmp = "Infinity";
	}	  
	return Double.parseDouble(tmp);
    }

    /**
     * Gets the value of an element of type xsd:integer with tag
     * 'tag' that is a child of n.
     *
     * @param n The parent Node.
     * @return The value of the xsd:integer as a long
     */
    public static long getLong(Node n, String tag)
    {
	String tmp = getString(n, tag);
	return Long.parseLong(tmp);
    }
    
    /**
     * Encodes XML special characters.
     *
     * @param s The String in which to encode the special characters.
     * @return A new String with special characters encoded.
     */
    public static String encodeSpecialCharacters(String s)
    {
	String res = s;
	res = res.replaceAll("&", "&amp;");
	res = res.replaceAll("<", "&lt;");
	res = res.replaceAll(">", "&gt;");
	res = res.replaceAll("'", "&apos;");
	res = res.replaceAll("\"", "&quot;");
	return res;
    }
}
