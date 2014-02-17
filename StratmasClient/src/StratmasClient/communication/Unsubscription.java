package StratmasClient.communication;

import org.w3c.dom.Element;
import StratmasClient.object.primitive.Timestamp;

/**
 * Class representing an unsubscription to an issued subscription.
 *
 * @version 1, $Date: 2006/03/22 14:30:50 $
 * @author  Per Alexius
 */
public class Unsubscription extends Subscription {
    /**
     * Creates an unsubscription to the subscription with the specified
     * id.
     *
     * @param id The id of the subscription to unsubscribe to.
     */
     public Unsubscription(int id) {
          super(id);
     }

    /**
     * Creates an unsubscription to the specified subscription.
     *
     * @param sub The subscription to unsubscribe to.
     */
     public Unsubscription(Subscription sub) {
          super(sub.id());
     }

    /**
     * Returns a string representation of the type of this object
     *
     * @return A string representation of the type of this object.
     */
     public String getTypeAsString() {
          return "Unsubscription";
     }

     /**
      * Updates the object this subscription refers to with the
      * contents of the provided dom element.
      *
      * @param n The dom element to fetch data from.
      * @param t The simulation time for which this update is valid.
      */
    public void update(Element n, Timestamp t)
    {
    }
}
