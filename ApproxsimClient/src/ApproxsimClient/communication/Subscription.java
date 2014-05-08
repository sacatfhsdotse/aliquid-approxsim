package ApproxsimClient.communication;

import java.lang.StringBuffer;
import org.w3c.dom.Element;
import ApproxsimClient.object.primitive.Timestamp;

/**
 * Abstract class that is the base class of all types of subscriptions.
 * 
 * @version 1, $Date: 2006/03/22 14:30:50 $
 * @author Per Alexius
 */
public abstract class Subscription extends XMLHelper {
    /** Keeps track of last provided subscription id. */
    private static int smCurrentId = 0;

    /** The id for this subscription. */
    private int mId;

    /**
     * Creates a Subscription with a unique id.
     */
    protected Subscription() {
        mId = newId();
    }

    /**
     * Creates a Subscription with the specified id.
     * 
     * @param id The id for the Subscription to be created.
     */
    protected Subscription(int id) {
        mId = id;
    }

    /**
     * Accessor for the id.
     * 
     * @return The id of this Subscription.
     */
    public int id() {
        return mId;
    }

    /**
     * Generates a new id. Id.s are generated starting with 1 and increasing with 1 for each subscription created.
     * 
     * @return The newly generated id.
     */
    private synchronized int newId() {
        return smCurrentId++;
    }

    /**
     * Updates the object this subscription refers to with the contents of the provided dom element.
     * 
     * @param n The dom element to fetch data from.
     * @param t The simulation time for which this update is valid.
     */
    abstract public void update(Element n, Timestamp t);

    /**
     * Returns the tag to be used when producing an xml representation of this subscription.
     * 
     * @return The tag to be used when producing an xml representation of this subscription.
     */
    public String getTag() {
        return "subscription";
    }

    /**
     * Creates an XML representation of this object.
     * 
     * @param b The StringBuffer to write to.
     * @return The StringBuffer b with an XML representation of this object appended to it.
     */
    public StringBuffer toXML(StringBuffer b) {
        b.append(NL).append("<").append(getTag());
        b.append(" xsi:type=\"sp:").append(getTypeAsString());
        b.append("\" id=\"").append(mId).append("\">");
        bodyXML(b);
        b.append("</").append(getTag()).append(">");
        return b;
    }
}
