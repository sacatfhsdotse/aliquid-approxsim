package StratmasClient.communication;


import java.lang.StringBuffer;


/**
 * Abstract class that contains some convenience methods for handling
 * xml creation.
 *
 * @version 1, $Date: 2006/03/22 14:30:50 $
 * @author  Per Alexius
 */
public abstract class XMLHelper {
     /** The line.separator system property. */
     public final static String NL = System.getProperty("line.separator");

    /**
     * Returns a string representation of the type of this object.
     *
     * @return A string representation of the type of this object.
     */
     public abstract String getTypeAsString();

    /**
     * Returns the tag of this object.
     *
     * @return The tag of this object.
     */
     public abstract String getTag();

     /**
      * Creates an XML representation of this object.
      *
      * @return An XML representation of this object.
      */
     public String toXML() {
          return toXML(new StringBuffer()).toString();
     }

     /**
      * Creates an XML representation of this object.
      *
      * @param b The StringBuffer to write to.
      * @return The StringBuffer b with an XML representation of this
      * object appended to it.
      */
     public StringBuffer toXML(StringBuffer b) {
          b.append(NL).append("<").append(getTag());
          b.append(" xsi:type=\"sp:").append(getTypeAsString()).append("\">");
          bodyXML(b);
          b.append(NL).append("</").append(getTag()).append(">");
          return b;
     }

     /**
      * Creates an XML representation of the body of this object.
      *
      * @param b The StringBuffer to write to.
      * @return The StringBuffer b with an XML representation of this
      * object's body appended to it.
      */
     public StringBuffer bodyXML(StringBuffer b) {
          return b;
     }
}
