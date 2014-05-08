package ApproxsimClient.communication;

/**
 * Class representing the reset message. The reset message is the message that a client sends to a server in order to reset a simulation.
 * 
 * @version 1, $Date: 2006/03/22 14:30:49 $
 * @author Per Alexius
 */
public class ResetMessage extends ApproxsimMessage {
    /**
     * Returns a string representation of the type of this message.
     * 
     * @return A string representation of the type of this message.
     */
    public String getTypeAsString() {
        return "ResetMessage";
    }
}
