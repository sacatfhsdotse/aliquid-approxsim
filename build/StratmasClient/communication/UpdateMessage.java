package StratmasClient.communication;


import java.lang.StringBuffer;
import java.util.Iterator;
import java.util.Vector;
import StratmasClient.object.StratmasObject;


/**
 * This class represents an update message. The update message is sent
 * to the server when a user has changed an object during a simulation.
 *
 * @version 1, $Date: 2006/05/15 12:24:33 $
 * @author  Per Alexius
 */
public class UpdateMessage extends StratmasMessage {
     /** Constant for defining add message. */
     public static final String ADD = "ServerUpdateAdd";

     /** Constant for defining remove message. */
     public static final String REMOVE = "ServerUpdateRemove";

     /** Constant for defining replace message. */
     public static final String REPLACE = "ServerUpdateReplace";

     /** Constant for defining update message. */
     public static final String MODIFY = "ServerUpdateModify";

     /** Contains all StratmasObejcts to be updated. */
     private Vector mUpdates = new Vector();

     /**
      * Adds an updated StratmasObejct to this message.
      *
      * @param u The update object
      */
     public void addUpdate(Update u) {
          mUpdates.add(u);
     }

    /**
     * Returns a string representation of the type of this message.
     *
     * @return A string representation of the type of this message.
     */
     public String getTypeAsString() {
          return "UpdateServerMessage";
     }

     /**
      * Creates an XML representation of the body of this object.
      *
      * @param b The StringBuffer to write to.
      * @return The StringBuffer b with an XML representation of this
      * object's body appended to it.
      */
     public StringBuffer bodyXML(StringBuffer b) {
          for (Iterator i = mUpdates.iterator(); i.hasNext(); ) {
               b.append(((Update)i.next()).toXML());
          }
          return b;
     }
}
