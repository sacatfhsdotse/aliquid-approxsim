package StratmasClient.communication;


import java.lang.StringBuffer;
import java.util.Iterator;
import java.util.Vector;


/**
 * This class represents the subscription message. This type of
 * message is sent to the server in order to subscribe to updates for
 * StratmasObjects.
 *
 * @version 1, $Date: 2006/03/22 14:30:50 $
 * @author  Per Alexius
 */
public class SubscriptionMessage extends StratmasMessage {
     /** Contains all subscriptions. */
     private Vector mSubscriptions;

     /**
      * Creates a subscription message with the specified initial
      * storage space.
      *
      * @param initialSize The initial storage space (in number of
      * subscriptions).
      */
     public SubscriptionMessage(int initialSize) {
          mSubscriptions = new Vector(initialSize);
     }

     /**
      * Adds a subscription to this message.
      *
      * @param sub The subscription to be added.
      */
     public void addSubscription(Subscription sub) {
          mSubscriptions.add(sub);
     }

     /**
      * Removes all subscriptions from this message.
      */
     public void clear() {
          mSubscriptions.clear();
     }

     /**
      * Checks if this message is empty.
      *
      * @return true if this message does not contain any
      * subscriptions, false otherwise.
      */
     public boolean isEmpty() {
          return mSubscriptions.isEmpty();
     }

     /**
      * Returns the number of subscriptions currently in this message.
      *
      * @return The number of subscriptions currently in this message.
      */
     public int size() {
          return mSubscriptions.size();
     }

    /**
     * Returns a string representation of the type of this object.
     *
     * @return A string representation of the type of this object.
     */
     public String getTypeAsString() {
          return "SubscriptionMessage";
     }

     /**
      * Creates an XML representation of the body of this object.
      *
      * @param b The StringBuffer to write to.
      * @return The StringBuffer b with an XML representation of this
      * object's body appended to it.
      */
     public StringBuffer bodyXML(StringBuffer b) {
          for (Iterator i = mSubscriptions.iterator(); i.hasNext(); ) {
               Subscription sub = (Subscription)i.next();
               if (!sub.getTypeAsString().equals("Unsubscription")) {
                    b.append(NL);
               }
               sub.toXML(b);
          }
          return b;
     }
}
