package ApproxsimClient.communication;

import java.lang.StringBuffer;

/**
 * Class representing the connect message. The connect message is the first message that a client sends to a server in order to establish a
 * connection.
 * 
 * @version 1, $Date: 2006/03/22 14:30:49 $
 * @author Per Alexius
 */
public class ConnectMessage extends ApproxsimMessage {
    /**
     * Returns a string representation of the type of this message.
     * 
     * @return A string representation of the type of this message.
     */
    public String getTypeAsString() {
        return "ConnectMessage";
    }

    /**
     * Creates an XML representation of the body of this object.
     * 
     * @param b The StringBuffer to write to.
     * @return The StringBuffer b with an XML representation of this object's body appended to it.
     */
    public StringBuffer bodyXML(StringBuffer b) {
        b.append(NL).append("<bigEndian>true</bigEndian>");
        return b;
    }
}
