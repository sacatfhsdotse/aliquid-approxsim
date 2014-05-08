// $Id: DefaultApproxsimMessageListener.java,v 1.4 2005/11/14 15:46:13 alexius Exp $

package ApproxsimClient.communication;

/**
 * This class provides a default implementation of the ApproxsimMessageListener interface.
 * 
 * @version 1, $Date: 2005/11/14 15:46:13 $
 * @author Per Alexius
 */
public class DefaultApproxsimMessageListener implements ApproxsimMessageListener {
    /**
     * Called when the client has sent the message to the server.
     * 
     * @param e The event that occured.
     */
    public void messageSent(ApproxsimMessageEvent e) {}

    /**
     * Called when the client has received the answer to the message from the server..
     * 
     * @param e The event that occured.
     */
    public void messageReceived(ApproxsimMessageEvent e) {}

    /**
     * Called when the XMLHandler has processed the data in the answer message received from the server.
     * 
     * @param e The event that occured.
     * @param reply the reply, if any, else null
     */
    public void messageHandled(ApproxsimMessageEvent e, Object reply) {}

    /**
     * Called when something has gone wrong during the process of sending the message and receiving and handling the answer.
     * 
     * @param e The event that occured.
     */
    public void errorOccurred(ApproxsimMessageEvent e) {}
}
