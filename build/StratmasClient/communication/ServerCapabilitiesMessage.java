package StratmasClient.communication;


/**
 * Class representing the server capabilities message. The server
 * capabilities message is the message that a client sends to a server
 * in order to find out which process variables the server is able to
 * simulate.
 *
 * @version 1, $Date: 2006/03/22 14:30:49 $
 * @author  Per Alexius
 */
public class ServerCapabilitiesMessage extends StratmasMessage {
    /**
     * Returns a string representation of the type of this message.
     *
     * @return A string representation of the type of this message.
     */
     public String getTypeAsString() {
	  return "ServerCapabilitiesMessage";
     }
}
