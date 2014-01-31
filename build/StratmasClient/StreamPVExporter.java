// 	$Id: StreamPVExporter.java,v 1.5 2006/03/22 14:30:42 dah Exp $
/*
 * @(#)StreamPVExporter.java
 */

package StratmasClient;

import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Enumeration;

import StratmasClient.object.primitive.Reference;
import StratmasClient.object.Shape;
import StratmasClient.object.StratmasEvent;
import StratmasClient.object.StratmasEventListener;
import StratmasClient.object.primitive.Timestamp;


import StratmasClient.communication.RegionData;
import StratmasClient.communication.Subscription;
import StratmasClient.communication.SubscriptionHandler;

/**
 * Subscribes to the aggreagate of all process variables in a region
 * and prints them to a stream.
 *
 * @version 1, $Date: 2006/03/22 14:30:42 $
 * @author  Daniel Ahlin
*/

public class StreamPVExporter implements StratmasEventListener
{
    /**
     * The field delimiter.
     */
    String delimiter = ";";

    /**
     * A reference to the shape this export is for.
     */
    Reference shapeReference;

    /**
     * The stream to which the data is supposed to be written.
     */
    OutputStreamWriter stream;

     Subscription mSubscription;
     SubscriptionHandler mSH;

    /**
     * Creates a new StreamPVExporter for the specified shape.
     *
     * @param shape the shape to get the aggregate for.
     */
    public StreamPVExporter (OutputStreamWriter stream, 
			     SubscriptionHandler handler, 
			     Shape shape)
    {
	this.stream = stream;
	this.shapeReference = shape.getReference();
	this.mSH = handler;
	RegionData regionData = new RegionData(shape);
	regionData.addListener(this);
	mSubscription = regionData.createSubscription();
	mSH.regSubscription(mSubscription);
    }

    /**
     * Unsubscribes to the subscription held by this object.
     */
     public void kill() {
	  mSH.regSubscription(new StratmasClient.communication.Unsubscription(mSubscription));
     }


    /**
     * Method called when new data is availiable
     *
     * @param event the event.
     */
    public synchronized void eventOccured(StratmasEvent event)
    {
	if (event.getSource() instanceof RegionData) {
	    writeValues(((RegionData) event.getSource()).getTimestamp(), 
			((RegionData) event.getSource()).getPV());
	}
    }

    /**
     * Method writing the recieved values to the stream.
     *
     * @param timestamp the time to prefix with.
     * @param factions the hashtable holding the data, per faction.
     */
    public void writeValues(Timestamp timestamp, Hashtable factions)
    {
	String prefix = timestamp.toString() + delimiter + 
	    shapeReference.toTaclanV2();
	
	for (Enumeration fs = factions.keys(); fs.hasMoreElements();) {
	    Object faction = fs.nextElement();
	    Hashtable pVariables = (Hashtable) factions.get(faction);
	    for (Enumeration pVs = pVariables.keys(); pVs.hasMoreElements();) {
		Object pVariable = pVs.nextElement();
		Double value = (Double) pVariables.get(pVariable);
		try {
		    this.stream.write(prefix + delimiter + faction + delimiter + pVariable + delimiter + value + "\n");
 		} catch (IOException e) {
 		    System.err.println(e.getMessage());
 		}
	    }
	}
    }
}
