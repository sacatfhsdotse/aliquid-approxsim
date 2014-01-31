// 	$Id: StratmasServer.java,v 1.8 2006/09/05 14:38:48 dah Exp $

/*
 * @(#)StratmasServer.java
 */

package StratmasDispatcher;

import java.util.Vector;
import java.util.Enumeration;

import  org.w3c.dom.Element;
import  org.w3c.dom.NodeList;
import  org.w3c.dom.Document;

/**
 * The information associated with a Stratmas server.
 *
 * @version 1, $Date: 2006/09/05 14:38:48 $
 * @author  Daniel Ahlin
*/

public class StratmasServer
{
    /**
     * The host where the server is running.
     */ 
    public String host;

    /**
     * The port on which the server is listening.
     */ 
    public int port;
    
    /**
     * Whether this server is appropriate to use.
     */
    public boolean good = false;

    /**
     * Whether the status of this host is currently pending.
     */
    public boolean pending = true;

    /**
     * Whether this Server should be used no more. This is a
     * nonrevocable state. After setting this to true, the monitoring
     * thread will stop.
     */
    public boolean bad = false;

    /**
     * The list of refererences of simulation running on this host.
     */
    Vector simulations;

    /**
     * Whether the server has an active client or not.
     */
    boolean hasActiveClient = false;

    /**
     * Creates a new StratmasServer record.
     *
     * @param host the host where the server is running.
     * @param port the port on which the server is listening.
     */
    public StratmasServer(String host, int port)
    {
	this.host = host;
	this.port = port;
	Thread monitorThread = new Thread(new ServerMonitor(this), "ServerMonitor: " + toString());
	monitorThread.start();
	setSimulations(new Vector());
    }

    /**
     * Equal for this class means host==host && port == port.
     *
     * @param o object to compare against.
     */
    public boolean equals(Object o)
    {
	if (o instanceof StratmasServer) {
	    StratmasServer so = (StratmasServer) o;
	    return getHost().equals(so.getHost()) &&
		getPort() == so.getPort();
	} else {
	    return false;
	}	
    }
    
    /**
     * Returns the hashcode for this object. This is lazily
     * constructed by taking the hashcode of this.toString().
     */
    public int hashCode()
    {
	return toString().hashCode();
    }

    /**
     * Returns the host of this server.
     */
    public String getHost()
    {
	return this.host;
    }

    /**
     * Returns the port of this server.
     */
    public int getPort()
    {
	return this.port;
    }

    /**
     * Returns the simulations running on this server last sample.
     */
    public Vector getSimulations()
    {
	return this.simulations;
    }

    /**
     * Sets the simulations running on this server last sample.
     */
    protected void setSimulations(Vector v)
    {
	this.simulations = v;
    }

    /**
     * Irreversible mark this server as bad.
     */
    protected void markAsBad()
    {
	this.bad = true;
    }

    /**
     * Returns true if this server is marked as bad.
     */
    public boolean isBad()
    {
	return this.bad;
    }

    /**
     * Returns true if this server is marked as good.
     */
    public boolean isGood()
    {
	return this.good;
    }

    /**
     * Returns true if this server is marked as pending.
     */
    public boolean isPending()
    {
	return this.pending;
    }

    /**
     * Mark this server as pending (or not).
     *
     * @param flag true if this server is pending.
     */
    public void setPending(boolean flag)
    {
	this.pending = flag;
    }

    /**
     * Mark this server as good (or not).
     *
     * @param flag true if this server is good.
     */
    public void setGood(boolean flag)
    {
	this.good = flag;
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
     * Creates a DOM element representing this server.
     * 
     * @param document the document where the node will be inserted.
     */
    public Element toDOMElement(Document document)
    {
	Element element = document.createElement("stratmasServer");
	Element hostElement = document.createElement("host");
	hostElement.appendChild(document.createTextNode(getHost()));
	Element portElement = document.createElement("port");
	portElement.appendChild(document.createTextNode(Integer.toString(getPort())));

	element.appendChild(hostElement);
	element.appendChild(portElement);

	Element hasActiveClientElement = document.createElement("hasActiveClient");
	hasActiveClientElement.appendChild(document.createTextNode(Boolean.toString(hasActiveClient())));
	element.appendChild(hasActiveClientElement);

	Element isPendingElement = document.createElement("isPending");
	isPendingElement.appendChild(document.createTextNode(Boolean.toString(isPending())));
	element.appendChild(isPendingElement);

	for (Enumeration e = getSimulations().elements(); e.hasMoreElements();) {
	    String id = (String) e.nextElement();
	    Element simElement = document.createElement("simulation");
	    simElement.appendChild(document.createTextNode(id));
	    element.appendChild(simElement);
	}

	return element;
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
	boolean hasActive = (Boolean.valueOf(element.getElementsByTagName("port").item(0).getFirstChild().getNodeValue())).booleanValue();
	
	StratmasServer res = new StratmasServer(host, port);
	if (res != null) {
	    res.setHasActiveClient(hasActive);
	}

	return res;
    }

    /**
     * Returns a string representation of this server.
     */
    public String toString()
    {
	return getHost() + ":" + getPort();
    }

    /**
     * Updates this server from values in supplied DOM Element
     *
     * @param element the element
     * @return true if update was succesful, else false.
     */
    public boolean udateFromLoadQueryResponse(Element element)
    {
	Element activeElement = (Element) element.getElementsByTagName("hasActiveClient").item(0);
	if (activeElement != null) {
	    setHasActiveClient(Boolean.valueOf(activeElement.getFirstChild().getNodeValue()).booleanValue());
	} else {
	    return false;
	}

	NodeList simulations = element.getElementsByTagName("simulation");
	Vector sims = new Vector();
	for (int i = 0; i < simulations.getLength(); i++) {
	    sims.add(simulations.item(i).getFirstChild().getNodeValue());
	}		    		    
	setSimulations(sims);		    

	return true;
    }
}
