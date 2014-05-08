package ApproxsimClient.communication;

import java.lang.StringBuffer;
import org.w3c.dom.Element;
import ApproxsimClient.object.primitive.Timestamp;

/**
 * This class represents a subsctiption to a Layer.
 * 
 * @version 1, $Date: 2006/03/22 14:30:49 $
 * @author Per Alexius
 */
public class LayerSubscription extends Subscription {
    /** The LayerData. */
    private LayerData mLD;

    /**
     * Creates a subscription to the specified layer.
     * 
     * @param ld The LayerData.
     */
    public LayerSubscription(LayerData ld) {
        super();
        mLD = ld;
    }

    /**
     * Updates the object this subscription refers to with the contents of the provided dom element.
     * 
     * @param n The dom element to fetch data from.
     * @param t The simulation time for which this update is valid.
     */
    public void update(Element n, Timestamp t) {
        mLD.update(n, t);
    }

    /**
     * Returns a string representation of the type of this object.
     * 
     * @return A string representation of the type of this object.
     */
    public String getTypeAsString() {
        return "LayerSubscription";
    }

    /**
     * Creates an XML representation of the body of this object.
     * 
     * @param b The StringBuffer to write to.
     * @return The StringBuffer b with an XML representation of this object's body appended to it.
     */
    public StringBuffer bodyXML(StringBuffer b) {
        b.append(NL).append("<layer>").append(mLD.getLayer())
                .append("</layer>");
        if (mLD.getFaction() != null) {
            b.append(NL).append("<faction>");
            mLD.getFaction().bodyXML(b);
            b.append(NL).append("</faction>");
        }
        return b;
    }
}
