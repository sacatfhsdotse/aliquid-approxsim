package StratmasClient.communication;


import java.lang.StringBuffer;


/**
 * Class representing the SetPropertyMessage. The SetPropertyMessage
 * is used to set different properties for the server.
 *
 * @version 1, $Date: 2006/03/22 14:30:49 $
 * @author  Per Alexius
 */
public class SetPropertyMessage extends StratmasMessage {
     /** The name of the property. */
     private String mProperty;
     /** The value of the property. */
     private String mValue;

    /**
     * Creates a set property message.
     *
     * @param property The name of the property to set.
     * @param value The value of the property.
     */
     public SetPropertyMessage(String property, String value) {
          mProperty = property;
          mValue = value;
     }

    /**
     * Returns a string representation of the type of this message.
     *
     * @return A string representation of the type of this message.
     */
     public String getTypeAsString() {
          return "SetPropertyMessage";
     }

     /**
      * Creates an XML representation of the body of this object.
      *
      * @param b The StringBuffer to write to.
      * @return The StringBuffer b with an XML representation of this
      * object's body appended to it.
      */
     public StringBuffer bodyXML(StringBuffer b) {
          b.append(NL).append("<property>").append(mProperty).append("</property>");
          b.append(NL).append("<value>").append(mValue).append("</value>");
          return b;
     }
}
