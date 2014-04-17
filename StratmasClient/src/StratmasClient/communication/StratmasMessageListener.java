package StratmasClient.communication;

/**
 * This interface should be implemented by objects that are interested in receiving notifications about what happens with a StratmasMessage.
 * Events are generated when:
 * <p>
 * The message is sent by the client to the server.
 * <p>
 * The answer to the message has been received.
 * <p>
 * The contents of the message has been processed by the XMLHandler
 * <p>
 * Something has gone wrong.
 * 
 * @version 1, $Date: 2005/10/31 09:24:34 $
 * @author Per Alexius
 */
public interface StratmasMessageListener extends java.util.EventListener {
    /**
     * Called when the client has sent the message to the server.
     * 
     * @param e The event that occured.
     */
    public void messageSent(StratmasMessageEvent e);

    /**
     * Called when the client has received the answer to the message from the server..
     * 
     * @param e The event that occured.
     */
    public void messageReceived(StratmasMessageEvent e);

    /**
     * Called when the XMLHandler has processed the data in the answer message received from the server.
     * 
     * @param e The event that occured.
     * @param reply the reply, if any, else null
     */
    public void messageHandled(StratmasMessageEvent e, Object reply);

    /**
     * Called when something has gone wrong during the process of sending the message and receiving and handling the answer.
     * 
     * @param e The event that occured.
     */
    public void errorOccurred(StratmasMessageEvent e);

}
