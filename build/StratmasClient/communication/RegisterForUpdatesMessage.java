package StratmasClient.communication;


/**
 * Class representing the RegisterForUpdates message. The
 * RegisterForUpdates message is the message that a client sends to a
 * server in order to register for updates of the StratmasObjects in a
 * simulation.
 *
 * @version 1, $Date: 2006/03/22 14:30:49 $
 * @author  Per Alexius
 */
public class RegisterForUpdatesMessage extends StratmasMessage {
     private boolean mRegister;
     
     /**
      * Constructor
      *
      * @param register True if we want to register - false if we want
      * to deregister.
      */
     public RegisterForUpdatesMessage(boolean register) {
	  mRegister = register;
     }

    /**
     * Returns a string representation of the type of this message.
     *
     * @return A string representation of the type of this message.
     */
     public String getTypeAsString() {
	  return "RegisterForUpdatesMessage";
     }

     /**
      * Creates an XML representation of the body of this object.
      *
      * @param b The StringBuffer to write to.
      * @return The StringBuffer b with an XML representation of this
      * object's body appended to it.
      */
     public StringBuffer bodyXML(StringBuffer b) {
	  b.append(NL).append("<register>").append(mRegister).append("</register>");
	  return b;
     }
}
