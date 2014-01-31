package StratmasClient.communication;


import java.util.Enumeration;
import java.util.Hashtable;
import StratmasClient.Debug;
import StratmasClient.object.primitive.Timestamp;


/**
 * This class handles subscriptions created by differentother
 * objets. It bundles them up in SubscriptionMessages and passes them
 * on to the ServerConnection.
 *
 * @version 1, $Date: 2006/10/19 09:10:17 $
 * @author  Per Alexius
 */
public class SubscriptionHandler implements Runnable {
     /** Maximum number of subscriptions per message. */
     private static final int sMaxNumSubsPerMsg = 400;

     /** 
      * The time to wait after receiving a subscription before
      * sending a subscription message.
      */
     private static final long sDelayms = 100;

     /** Set to true if we should quit. */
     private boolean mQuit = false;

     /** The queue in which incomming subscriptions are stored. */
     private TSQueue mQueue = new TSQueue();  

     /** Maps subscription id to the subscription itself. */
     private Hashtable mSubs = new Hashtable();

     /** The ServerConnection to use. */
     private ServerConnection mSC = null;

     /**
      * Creates a new SubscriptionHandler.
      */
     public SubscriptionHandler() {
     }

     /**
      * Creates a new SubscriptionHandler and connects it to the
      * provided ServerConnection.
      * 
      * @param serverConnection the connection to use for this subscriptionHandler.
      */
     public SubscriptionHandler(ServerConnection serverConnection) {
	  connect(serverConnection);
     }

     /**
      * Connects this SubscriptionHandler to the specified
      * ServerConnection.
      *
      * @param sc The ServerConnection to use.
      */
     public void connect(ServerConnection sc) {
	  disconnectSubscriptionsFromObjects();
	  mSubs.clear();
	  mQueue.clear();
	  mSC = sc;
	  sc.getXMLHandler().connect(this);
     }

     /**
      * Starts a new Thread for this SubscriptionHandler.
      */
     public void start() {
	  (new Thread(this, getClass().getName())).start();
     }

     /**
      * Terminates the Thread running this SubscriptionHandler.
      */
     public void kill() {
	  mQuit = true;
	  mQueue.enqueue(null);
     }

     /**
      * Register a new subscription.
      *
      * @param sub The Subscription to register.
      */
     public void regSubscription(Subscription sub) {
	  if (sub instanceof Unsubscription) {
	       Object o = mSubs.remove(new Integer(sub.id()));
	       // If the subscription the unsubscription refers to
	       // does not exist we don't have to forward it to the
	       // server.
	       if (o != null) {
		    mQueue.enqueue(sub);
	       }
	  }
	  else if (sub instanceof StratmasObjectSubscription) {
	       StratmasObjectSubscription sosub = (StratmasObjectSubscription)sub;
	       sosub.setSubscriptionHandler(this);
	       mSubs.put(new Integer(sub.id()), sub);
	       mQueue.enqueue(sub);
	  }
	  else {
	       mSubs.put(new Integer(sub.id()), sub);
	       mQueue.enqueue(sub);
	  }
     }

     /**
      * Register a new subscription. Blocks until subscription confirmed.
      *
      * @param sub The Subscription to register.
      */
     public void blockingRegSubscription(Subscription sub) throws ServerException {
	  if (sub instanceof Unsubscription) {
	       Object o = mSubs.remove(new Integer(sub.id()));
	  }
	  else if (sub instanceof StratmasObjectSubscription) {
	       StratmasObjectSubscription sosub = (StratmasObjectSubscription)sub;
	       sosub.setSubscriptionHandler(this);
	       mSubs.put(new Integer(sub.id()), sub);
	  }
	  else {
	       mSubs.put(new Integer(sub.id()), sub);
	  }

	  SubscriptionMessage message = new SubscriptionMessage(1);
	  message.addSubscription(sub);
	  
	  mSC.blockingSend(message);
     }

     /**
      * Make Subscriptions listening to StratmasObjects stop listen.
      */
     private void disconnectSubscriptionsFromObjects() {
	  for (Enumeration en = mSubs.elements(); en.hasMoreElements(); ) {
	       Subscription sub = (Subscription)en.nextElement();
	       if (sub instanceof StratmasObjectSubscription) {
		    ((StratmasObjectSubscription)sub).annihilate();
	       }
	  }
     }

     /**
      * The main loop.
      */
     public void run() {
	  Subscription sub;
	  SubscriptionMessage msg = new SubscriptionMessage(sMaxNumSubsPerMsg);
	  long firstDequeueTime = 0;

	  while (!mQuit) {
	       if (msg.isEmpty()) {
		    sub = (Subscription)mQueue.blockingDequeue();
		    if (mQuit) {
			 break;
		    }
		    msg.addSubscription(sub);
		    SubscriptionCounter.updateNrOfMessInSendingQueue2(msg.size());
		    firstDequeueTime = System.currentTimeMillis();
	       }
	       else {
		    while (System.currentTimeMillis() - firstDequeueTime < sDelayms &&
			   msg.size() < sMaxNumSubsPerMsg &&
			   !mQuit) {
			 sub = (Subscription)mQueue.dequeue();
			 if (sub != null) {
			      msg.addSubscription(sub);
			 }
			 SubscriptionCounter.updateNrOfMessInSendingQueue2(msg.size());
		    }
		    if (mQuit) {
			 break;
		    }

		    // Send
		    if (mSC != null) {
			 mSC.send(msg);
			 msg = new SubscriptionMessage(sMaxNumSubsPerMsg);
		    }
		    else {
			 System.err.println("No ServerConnection in SubscriptionHandler!");
			 mQuit = true;
		    }
	       }
	       SubscriptionCounter.updateNrOfMessInSendingQueue2(msg.size());  
	  }
	  disconnectSubscriptionsFromObjects();
     }

     /**
      * Handle the data contained in the provided Element.
      *
      * @param n The Element that contains the data to be handled.
      * @param t The simulation time for which the data is valid.
      */
     public void handleSubscribedData(org.w3c.dom.Element n, Timestamp t) {
	  Integer id = new Integer(n.getAttribute("id"));
	  Subscription sub = (Subscription)mSubs.get(id);
	  if (sub != null) {
	       if (sub instanceof StratmasObjectSubscription) {
		    StratmasObjectSubscription s = (StratmasObjectSubscription)sub;
		    Debug.err.println("handling sub for " + s.object().getReference());
	       }
	       sub.update(n, t);
	  }
	  else {
	       Debug.err.println("Can't find subscription of type '" + n.getAttribute("xsi:type") + "' with id " + id);
	  }
     }
}
