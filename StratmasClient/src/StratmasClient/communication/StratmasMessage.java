package StratmasClient.communication;

import java.lang.StringBuffer;
import javax.swing.event.EventListenerList;
import StratmasClient.StratmasConstants;

/**
 * Abstract class that is the super class of all messages to be sent between the vlient and the server.
 * 
 * @version 1, $Date: 2005/11/01 09:36:43 $
 * @author Per Alexius
 */
public abstract class StratmasMessage extends XMLHelper {
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
    public void addEventListener(StratmasMessageListener listener) {
        this.getEventListenerList()
                .add(StratmasMessageListener.class, listener);
    }

    /**
     * Removes an event listener for from the eventlistenerlist.
     * 
     * @param listener the listener to remove.
     */
    public void removeEventListener(StratmasMessageListener listener) {
        this.getEventListenerList().remove(StratmasMessageListener.class,
                                           listener);
    }

    /**
     * Returns the tag
     * 
     * @return The tag e.g 'stratmasMessage'
     */
    public String getTag() {
        return "stratmasMessage";
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
        b.append("<sp:stratmasMessage xmlns:sp=\"")
                .append(StratmasConstants.stratmasNamespace);
        b.append("\" xmlns:xsi=\"").append(StratmasConstants.xmlnsNamespace)
                .append("\" ");
        b.append("xsi:type=\"sp:").append(getTypeAsString()).append("\">");
        bodyXML(b);
        b.append("</sp:stratmasMessage>");
        return b;
    }

    /**
     * Fires an event telling that this message has been sent.
     */
    public void fireMessageSent() {
        StratmasMessageEvent event = new StratmasMessageEvent(this);

        // Guaranteed to return a non-null array
        Object[] listeners = getEventListenerList().getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            ((StratmasMessageListener) listeners[i + 1]).messageSent(event);
        }
    }

    /**
     * Fires an event telling that the answer to this message has been received.
     */
    public void fireMessageReceived() {
        StratmasMessageEvent event = new StratmasMessageEvent(this);

        // Guaranteed to return a non-null array
        Object[] listeners = getEventListenerList().getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            ((StratmasMessageListener) listeners[i + 1]).messageReceived(event);
        }
    }

    /**
     * Fires an event telling that the answer to this message has been handled.
     * 
     * @param reply the reply, if any, else null
     */
    public void fireMessageHandled(Object reply) {
        StratmasMessageEvent event = new StratmasMessageEvent(this);

        // Guaranteed to return a non-null array
        Object[] listeners = getEventListenerList().getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            ((StratmasMessageListener) listeners[i + 1]).messageHandled(event,
                                                                        reply);
        }
    }

    /**
     * Fires an event telling that something went wrong during the sending, answer-receiving or answer-handling of this message.
     */
    public void fireErrorOccurred() {
        StratmasMessageEvent event = new StratmasMessageEvent(this);

        // Guaranteed to return a non-null array
        Object[] listeners = getEventListenerList().getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            ((StratmasMessageListener) listeners[i + 1]).errorOccurred(event);
        }
    }
}
