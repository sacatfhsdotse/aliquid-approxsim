// 	$Id: StratmasServer.java,v 1.6 2005/10/07 12:59:15 dah Exp $

/*
 * @(#).StratmasServer.java
 */

package StratmasClient.dispatcher;

import java.util.Vector;

import  org.w3c.dom.Element;
import  org.w3c.dom.Document;
import  org.w3c.dom.NodeList;

/**
 * StratmasServer represent information about a simulation server
 *
 * @version 1, $Date: 2005/10/07 12:59:15 $
 * @author  Daniel Ahlin
*/
public class StratmasServer
{
    /**
     * The hostname of the server.
     */
    String host;

    /**
     * The port on which the server is listening.
     */
    int port;

    /**
     * The simulations running on this server.
     */
    Vector simulations;

    /**
     * Whether the server has an active client or not.
     */
    boolean hasActiveClient = false;

    /**
     * Whether the server status is presently pending.
     */
    boolean isPending = false;

    /**
     * Creates a new server record using the specified host and port.
     *
     * @param host the hostname of the server.
     * @param port the port on which the server is listening.
     * @param simulations the simulations running on this server.
     */    
    public StratmasServer(String host, int port, Vector simulations)
    {
	this.host = host;
	this.port = port;
	this.simulations = simulations;
    }

    /**
     * Returns the port of this server
     */
    public int getPort()
    {
	return this.port;
    }

    /**
     * Returns the hostname of this server
     */
    public String getHost()
    {
	return this.host;
    }

    /**
     * Returns the reported simulations of this host.
     */
    public Vector getSimulations()
    {
	return this.simulations;
    }

    /**
     * Returns true if the allocated server has an active connection.
     */
    public boolean isBusy()
    {
	return hasActiveClient() || getSimulations().size() > 0;
    }

    /**
     * Mark this server as having an active client or not
     *
     * @param flag true if this server has an active client, else false.
     */
    public void setHasActiveClient(boolean flag)
    {
	this.hasActiveClient = flag;
    }

    /**
     * Returns true if this server have an active client, else false.
     */
    public boolean hasActiveClient()
    {
	return this.hasActiveClient;
    }

    /**
     * Returns true if the status of the server is currently pending.
     */
    public boolean isPending()
    {
	return  this.isPending;
    }

    /**
     * Mark this server as pending.
     *
     * @param flag true if this server is pending, else false.
     */
    public void setIsPending(boolean flag)
    {
	this.isPending = flag;
    }

    /**
     * Creates a server entry from an XML representation of this object.
     *
     * @param element the element representing the server.
     */
    public static StratmasServer fromDOMElement(Element element)
    {
	String host = element.getElementsByTagName("host").item(0).getFirstChild().getNodeValue();
	int port = Integer.parseInt(element.getElementsByTagName("port").item(0).getFirstChild().getNodeValue());
	boolean active = Boolean.valueOf(element.getElementsByTagName("hasActiveClient").item(0).getFirstChild().getNodeValue()).booleanValue();
	boolean isPending = Boolean.valueOf(element.getElementsByTagName("isPending").item(0).getFirstChild().getNodeValue()).booleanValue();
	
	Vector res = new Vector();
	NodeList sims = element.getElementsByTagName("simulation");
	for (int i = 0; i < sims.getLength(); i++) {
	    res.add(sims.item(i).getFirstChild().getNodeValue());
	}
	
	StratmasServer server = new StratmasServer(host, port, res);
	server.setHasActiveClient(active);
	server.setIsPending(isPending);

	return server;
    }

    /**
     * Returns a string representation of this server.
     */
    public String toString() 
    {
	String status = isBusy() ? "busy" : "available";
	if (isPending()) {
	    status += " (pending)";
	}
	
	return getHost() + ":" + getPort() + " - " + status; 
    }
}
