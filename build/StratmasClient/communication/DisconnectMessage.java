package StratmasClient.communication;


/**
 * Class representing the disconnect message. The disconnect message
 * is the message that a client sends to a server in order to
 * terminate a session.
 *
 * @version 1, $Date: 2006/03/22 14:30:49 $
 * @author  Per Alexius
 */
public class DisconnectMessage extends StratmasMessage {
    /**
     * Returns a string representation of the type of this message.
     *
     * @return A string representation of the type of this message.
     */
     public String getTypeAsString() {
          return "DisconnectMessage";
     }
}
