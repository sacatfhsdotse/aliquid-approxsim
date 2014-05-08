package ApproxsimClient.communication;

import java.lang.StringBuffer;
import org.w3c.dom.Element;
import ApproxsimClient.Debug;
import ApproxsimClient.object.primitive.Reference;
import ApproxsimClient.object.ApproxsimEvent;
import ApproxsimClient.object.ApproxsimEventListener;
import ApproxsimClient.object.ApproxsimObject;
import ApproxsimClient.object.primitive.Timestamp;

/**
 * This class represents the type of subscription that should be used in order to obtain updates of a ApproxsimObject from the server.
 * 
 * @version 1, $Date: 2006/03/22 14:30:50 $
 * @author Per Alexius
 */
public class ApproxsimObjectSubscription extends Subscription implements
        ApproxsimEventListener {
    /** The ApproxsimObject this subscription refers to. */
    private ApproxsimObject mObject = null;

    /** The ApproxsimObject this subscription refers to. */
    private Reference mReference = null;

    /**
     * The SubscriptionHandler to use if we must deregister this subscription in case the element subscribed to is removed.
     */
    private SubscriptionHandler mSH = null;

    /**
     * Constructor used by ApproxsimObjects when issuing a subscription to themselves.
     * 
     * @param obj The ApproxsimObject this subscription refers to.
     * @param ref The Reference to where in the simulation tree the object refered to by this
     */
    public ApproxsimObjectSubscription(ApproxsimObject obj, Reference ref) {
        mObject = obj;
        mObject.addEventListener(this);
        mReference = ref;
    }

    /**
     * Annihilates this subscription. Called by the SubscriptionHandler when it shuts down or resets itself and when the object this
     * subscription refers to gets removed.
     */
    void annihilate() {
        mObject.removeEventListener(this);
        mObject = null;
        mReference = null;
        if (mSH != null) {
            mSH.regSubscription(new Unsubscription(this));
            mSH = null;
        }
    }

    /**
     * Accessor for the ApproxsimObject this subscription refers to.
     * 
     * @return The ApproxsimObject this subscription refers to.
     */
    public ApproxsimObject object() {
        return mObject;
    }

    /**
     * Sets the subscription handler of this class. Called by the SubscriptionHandler when the subscription is registered so that the
     * subscription may unsubscribe to itself in case the ApproxsimObject refered to is removed.
     * 
     * @param handler The SubscriptionHandler.
     */
    void setSubscriptionHandler(SubscriptionHandler sh) {
        mSH = sh;
    }

    /**
     * Updates this object with the data contained in the Element n.
     * 
     * @param n The DOM Element from which to fetch the data.
     * @param t The simulation time for which the data is valid.
     */
    public void update(Element n, Timestamp t) {
        Element elem = XMLHandler.getFirstChildByTag(n, "update");
        if (elem.getAttribute("xsi:type").equals("sp:UpdateModify")) {
            elem = XMLHandler.getFirstChildByTag(elem, "newValue");
        }
        mObject.update(elem, t);
    }

    /**
     * Handles events. On an objectCreatedEvent it adds the server created object to a list in the object this subscription refers to. This
     * happens when the Initializer created for a server-created list element reports that the object to be added has been created. On a
     * removed event it posts an unsubscription to itself.
     * 
     * @param event The event.
     */
    public void eventOccured(ApproxsimEvent event) {
        if (event.isRemoved()) {
            annihilate();
        } else if (event.isReplaced()) {
            Debug.err.print("Replacing " + mObject.getType().getName());
            mObject.removeEventListener(this);
            mObject = (ApproxsimObject) event.getArgument();
            mObject.addEventListener(this);
            Debug.err.println(" with " + mObject.getType().getName()
                    + " in ApproxsimObjectSubscription");
        }
    }

    /**
     * Returns a string representation of the type of this object.
     * 
     * @return A string representation of the type of this object.
     */
    public String getTypeAsString() {
        return "ApproxsimObjectSubscription";
    }

    /**
     * Creates an XML representation of the body of this object.
     * 
     * @param b The StringBuffer to write to.
     * @return The StringBuffer b with an XML representation of this object's body appended to it.
     */
    public StringBuffer bodyXML(StringBuffer b) {
        b.append(NL).append("<reference>");
        mReference.bodyXML(b);
        b.append(NL).append("</reference>");
        return b;
    }
}
