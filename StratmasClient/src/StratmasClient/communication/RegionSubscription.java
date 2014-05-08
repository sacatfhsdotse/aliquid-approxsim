package ApproxsimClient.communication;

import ApproxsimClient.object.primitive.Timestamp;

/**
 * This class represents a subscription to the aggregate of the process variables for a specified region.
 * 
 * @version 1, $Date: 2006/03/22 14:30:49 $
 * @author Per Alexius
 */
public class RegionSubscription extends Subscription {
    /**
     * Contains data about the region this subscription refers to.
     */
    private RegionData mData;

    /**
     * Creates a subscription to the specified region.
     * 
     * @param data The data for the Region.
     */
    public RegionSubscription(RegionData data) {
        mData = data;
    }

    /**
     * Updates the object this subscription refers to with the contents of the provided dom element.
     * 
     * @param n The dom element to fetch data from.
     * @param t The simulation time for which this update is valid.
     */
    public void update(org.w3c.dom.Element n, Timestamp t) {
        mData.update(n, t);
    }

    /**
     * Returns a string representation of the type of this object.
     * 
     * @return A string representation of the type of this object.
     */
    public String getTypeAsString() {
        return "RegionSubscription";
    }

    /**
     * Creates an XML representation of the body of this object.
     * 
     * @param b The StringBuffer to write to.
     * @return The StringBuffer b with an XML representation of this object's body appended to it.
     */
    public StringBuffer bodyXML(StringBuffer b) {
        b.append(NL).append("<region xsi:type=\"sp:");
        b.append(mData.getRegion().getType().getName()).append("\">");
        mData.getRegion().bodyXML(b);
        b.append(NL).append("</region>");
        return b;
    }

    /**
     * Returns the RegionData of this subscription.
     */
    public RegionData getRegionData() {
        return this.mData;
    }
}
