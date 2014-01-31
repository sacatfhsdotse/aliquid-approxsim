package StratmasClient.communication;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import StratmasClient.Client;
import StratmasClient.Debug;
import StratmasClient.object.primitive.Reference;
import StratmasClient.object.Shape;
import StratmasClient.object.StratmasEvent;
import StratmasClient.object.StratmasEventListener;
import StratmasClient.object.primitive.Timestamp;

/**
 * Class used when subscribing to aggregated pv values over a Region.
 *
 * @version 1, $Date: 2006/09/20 08:17:56 $
 * @author  Per Alexius
 */
public class RegionData {
     /** Vector of StratmasEventListeners listening to this object. */
     private Vector      mListeners   = new Vector();

     /** The region this data refers to. */
     private Shape mRegion;

     /**  The id of the subscription for this object or -1 if there is none. */
     private int mSubscriptionId = -1;

     /** The Client to report to when the last listener has been removed. */
     private Client mClient;

     /**
      * Hashtable mapping pv name to Hashtable mapping faction name
      * (excluding the all faction) to pv value.
      */
     private Hashtable   mPV          = new Hashtable();

     /** Simulation time for last update of this object. */
     private Timestamp   mTimestamp;
    
     
     /**
      * Constructs a RegionData object for the specified shape.
      *
      * @param region The region this data should refere to.
      */
     public RegionData(Shape region) {
	  mRegion = region;
     }

     /**
      * Constructs a Subscription for this object.
      *
      * @param reportToWhenDone The Client to report to when the last
      * listener has been removed.
      */
     public Subscription createSubscription(Client reportToWhenDone) {
	  mClient = reportToWhenDone;
	  Subscription sub = new RegionSubscription(this);
	  mSubscriptionId = sub.id();
	  return sub;
     }
    
    /**
     * Creates a subscription object of representing this RegionData.
     */
    public Subscription createSubscription()
    {
	return createSubscription(null);
    }

     /**
      * Accessor for the Shape.
      *
      * @return The Shape this data refers to.
      */
     public Shape getRegion() {
	  return mRegion;
     }
    
     /**
      * Accessor for the simulation time for the last update of this
      * object.
      *
      * @return The simulation time for the last update of this object.
      */
     public Timestamp getTimestamp() {
	  return mTimestamp;
     }
    
     /**
      * Adds a listener to this object. Listeners will be notified
      * when the object has been updated.
      *
      * @param listener The listener to add.
      */
     public synchronized void addListener(StratmasEventListener listener) {
	  mListeners.add(listener);
     }

     /**
      * Removes a listener from this object.
      *
      * @param listener The listener to remove.
      */
     public synchronized void removeListener(StratmasEventListener listener) {
	  mListeners.remove(listener);
	  if (mListeners.isEmpty()) {
	      if (mClient != null) {
		  mClient.unsubscribe(mSubscriptionId);
		  mSubscriptionId = -1;
	      }
	  }
     }
    
    /**
     * Returns true if the subscription for this object exists.
     */
    public boolean subscriptionExists() {
	return mSubscriptionId != -1;
    }

     /**
      * Gets a <b>copy</b> of the Hashtable containing all pv values
      * for all factions.
      *
      * @return A copy of the Hashtable containing all pv values for
      *  all factions.
      */
     public synchronized Hashtable getPV() {
	  Hashtable copy = new Hashtable();
	  for (java.util.Enumeration en = mPV.keys(); en.hasMoreElements(); ) {
	       Object key = en.nextElement();
	       copy.put(key, getPV((String)key));
	  }
	  return copy;
     }

     /**
      * Gets a <b>copy</b> of the Hashtable containing the pv values
      * for all factions for the specified pv.
      *
      * @param pvName The name of the pv.
      * @return A copy of the Hashtable containing the pv values for
      * all factions for the specified pv.
      */
     public synchronized Hashtable getPV(String pvName) {
	  Hashtable toBeCopied = (Hashtable)mPV.get(pvName);
	  if (toBeCopied != null) {
	      Hashtable copy       = new Hashtable();
	      for (java.util.Enumeration en = toBeCopied.keys(); en.hasMoreElements(); ) {
		  Object key = en.nextElement();
		  copy.put(key, toBeCopied.get(key));
	      }
	      return copy;
	  }
	  return null;
     }

     /**
      * Gets a <b>copy</b> of the specified pv for the specified
      * faction. Returns null if the specified pv or the specified
      * faction does not exist.
      *
      * @param pvName The name of the pv.
      * @param factionName The name of the faction.
      * @return A copy of the specified pv for the specified faction.
      */
     public synchronized Double getPV(String pvName, String factionName) {
 	 Hashtable table = (Hashtable) mPV.get(pvName);
	 if (table != null) {
	     Double d = (Double) table.get(factionName);
	     if (d != null) {
		 return new Double(d.doubleValue());
	     } else {
		 return null;
	     }
	 } else {
	     return null;
	 }
     }

     /**
      * Updates this object with the data contained in the Element n.
      *
      * @param n The DOM Element from which to fetch the data.
      * @param t The simulation time for which the data is valid.
      */
     public synchronized void update(Element n, Timestamp t) {
	  mTimestamp = t;
	
	  for (Node child = n.getFirstChild(); child != null; child = child.getNextSibling()) {
	       if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("pv")) {
		    Element elem          = (Element)child;
		    String name           = XMLHandler.getString(elem, "name");
		    Hashtable factionHash = (Hashtable)mPV.get(name);

		    if (factionHash == null) {
			 factionHash = new Hashtable();
			 mPV.put(name, factionHash);
		    }

		    double  value   = XMLHandler.getDouble(elem, "value");
		    Element factionElem = XMLHandler.getFirstChildByTag(elem, "faction");
		    String factionName;
		    if (factionElem == null) {
			 factionName = StratmasClient.StratmasConstants.factionAll;
		    }
		    else {
			 factionName = Reference.getReference(factionElem).getIdentifier();
		    }
		    factionHash.put(factionName, new Double(value));
	       }
	  }
 
	  StratmasEvent event = StratmasEvent.getGeneric(this);
	  for(Iterator it = mListeners.iterator(); it.hasNext(); ) {
	       ((StratmasEventListener)it.next()).eventOccured(event);
	  }
     }
}
