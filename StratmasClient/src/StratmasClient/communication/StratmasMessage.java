package ApproxsimClient.communication;

import java.lang.StringBuffer;
import javax.swing.event.EventListenerList;
import ApproxsimClient.ApproxsimConstants;

/**
 * Abstract class that is the super class of all messages to be sent between the vlient and the server.
 * 
 * @version 1, $Date: 2005/11/01 09:36:43 $
 * @author Per Alexius
 */
public abstract class ApproxsimMessage extends XMLHelper {
    /**
     * The listeners of this object.
     */
    private EventListenerList eventListenerList = new EventListenerList();

    /**
     * Returns a list of the listeners of this object.
     */
    protected EventListenerList getEventListenerList() {
        return this.eventListenerList;
    }

    /**
     * Adds an event listener for to the eventlistenerlist.
     * 
     * @param listener the listener to add.
     */
    public void addEventListener(ApproxsimMessageListener listener) {
        this.getEventListenerList()
                .add(ApproxsimMessageListener.class, listener);
    }

    /**
     * Removes an event listener for from the eventlistenerlist.
     * 
     * @param listener the listener to remove.
     */
    public void removeEventListener(ApproxsimMessageListener listener) {
        this.getEventListenerList().remove(ApproxsimMessageListener.class,
                                           listener);
    }

    /**
     * Returns the tag
     * 
     * @return The tag e.g 'approxsimMessage'
     */
    public String getTag() {
        return "approxsimMessage";
    }

    /**
     * Creates an XML representation of this object.
     * 
     * @param b The StringBuffer to write to.
     * @return The StringBuffer b with an XML representation of this object appended to it.
     */
    public StringBuffer toXML(StringBuffer b) {
//          b.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        b.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
        b.append("<sp:approxsimMessage xmlns:sp=\"")
                .append(ApproxsimConstants.approxsimNamespace);
        b.append("\" xmlns:xsi=\"").append(ApproxsimConstants.xmlnsNamespace)
                .append("\" ");
        b.append("xsi:type=\"sp:").append(getTypeAsString()).append("\">");
        bodyXML(b);
        b.append("</sp:approxsimMessage>");
        return b;
    }

    /**
     * Fires an event telling that this message has been sent.
     */
    public void fireMessageSent() {
        ApproxsimMessageEvent event = new ApproxsimMessageEvent(this);

        // Guaranteed to return a non-null array
        Object[] listeners = getEventListenerList().getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            ((ApproxsimMessageListener) listeners[i + 1]).messageSent(event);
        }
    }

    /**
     * Fires an event telling that the answer to this message has been received.
     */
    public void fireMessageReceived() {
        ApproxsimMessageEvent event = new ApproxsimMessageEvent(this);

        // Guaranteed to return a non-null array
        Object[] listeners = getEventListenerList().getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            ((ApproxsimMessageListener) listeners[i + 1]).messageReceived(event);
        }
    }

    /**
     * Fires an event telling that the answer to this message has been handled.
     * 
     * @param reply the reply, if any, else null
     */
    public void fireMessageHandled(Object reply) {
        ApproxsimMessageEvent event = new ApproxsimMessageEvent(this);

        // Guaranteed to return a non-null array
        Object[] listeners = getEventListenerList().getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            ((ApproxsimMessageListener) listeners[i + 1]).messageHandled(event,
                                                                        reply);
        }
    }

    /**
     * Fires an event telling that something went wrong during the sending, answer-receiving or answer-handling of this message.
     */
    public void fireErrorOccurred() {
        ApproxsimMessageEvent event = new ApproxsimMessageEvent(this);

        // Guaranteed to return a non-null array
        Object[] listeners = getEventListenerList().getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            ((ApproxsimMessageListener) listeners[i + 1]).errorOccurred(event);
        }
    }
}
