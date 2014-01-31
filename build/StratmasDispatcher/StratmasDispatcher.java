// 	$Id: StratmasDispatcher.java,v 1.9 2006/08/29 16:25:55 dah Exp $

/*
 * @(#)StratmasDispatcher.java
 */

package StratmasDispatcher;

import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import java.io.IOException;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;

import java.net.InetSocketAddress;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

/**
 * StratmasDispatcher is a minimalistic server maintaining a list of
 * availiable StratmasServers.
 *
 * @version 1, $Date: 2006/08/29 16:25:55 $
 * @author  Daniel Ahlin
*/
public class StratmasDispatcher implements Runnable
{
    /**
     * The default port on which to listen.
     */
    public static int DEFAULT_PORT = 4181;

    /**
     * The port on which to listen.
     */
    int port;

    /**
     * Servers currently being monitored.
     */
    Vector servers;

    /**
     * Creates a new StratmasDispatcher, listening on a specified
     * port.
     *
     * @param port the port on which to listen.
     */
    public StratmasDispatcher(int port)
    {
	this.port = port;
	this.servers = new Vector();
    }

    /** 
     * Creates a new StratmasDispatcher, listening on the default
     * port.
     */
    public StratmasDispatcher()
    {
	this(StratmasDispatcher.DEFAULT_PORT);
    }

    /**
     * Registers a new server to be dispatched.
     *
     * @param server the server to watch.
     */
    public void registerServer(StratmasServer server)
    {
	synchronized(servers) {
	    // Remove any previous registrations of this server. This
	    // prevents a server that stops and starts in between two
	    // samples from having two registrations.
	    int i = getServers().indexOf(server);
	    if (i >= 0) {
		log("Removing stale registration of " + server.toString() + " from pool.");
		StratmasServer oldServer = (StratmasServer) getServers().get(i);
		log("Adding " + server.toString() + " to pool.");
		getServers().setElementAt(server, i);
		// FIXME: This is a ugly way of stopping the monitor thread...
		oldServer.markAsBad();
	    } else {
		log("Adding " + server.toString() + " to pool.");
		getServers().add(server);
	    }
	}
    }

    /**
     * Removes a server
     *
     * @param server the server to remove.
     */
    public void removeServer(StratmasServer server)
    {
	log("Evicting " + server.toString() + " from pool.");
	synchronized(servers) {
	    getServers().remove(server);
	}
    }
	
    /**
     * Starts this dispatcher.
     */
    public void run()
    {
	// Create thread that removes bad nodes from the servers vector.
	Thread evictorThread = new Thread("EvictorThread")
	    {
		/** 
		 * Removes bad servers then sleeps then restarts.
		 */
		public void run() 
		{
		    try {
			while(true) {
			    Vector bad = new Vector();
			    for (Enumeration e = getServers().elements(); 
				 e.hasMoreElements();) {
				StratmasServer server = (StratmasServer) e.nextElement();
				if (server.isBad()) {
				    bad.add(server);
				}
			    }

			    for (Enumeration e = bad.elements(); 
				 e.hasMoreElements();) {
				StratmasServer server = (StratmasServer) e.nextElement();
				removeServer(server);
			    }
			    
			    sleep(1000);
			}
		    } catch (InterruptedException e) {
		        log("evictorThread interupted. No more servers " + 
			    "will be evicted from pool\n");
		    }
		}

	    };
	evictorThread.start();

	try {
	    ServerSocketChannel serverSocketChannel = 
		ServerSocketChannel.open();
	    InetSocketAddress address = new InetSocketAddress(getPort());
	    serverSocketChannel.socket().bind(address);
	
	    while(true) {
		try {
		    SocketChannel socketChannel = serverSocketChannel.accept();
		    (new Thread(new ConnectionHandler(socketChannel, this), "Handler for " + address.toString())).start();
		} catch (IOException e) {
		    log("Error accepting connection: " + e.getMessage());
		}
	    }
	} catch (IOException e) {
	    log("Unable to bind to port: " + e.getMessage());
	    System.exit(1);
	}
    }

    /**
     * Logging method for the dispatcher.
     *
     * @param logMessage the message to log.
     */
    public static void log(String logMessage)
    {
	System.err.println(logMessage);
    }

    /**
     * Returns the port this dispatcher is listening to.
     */
    public int getPort() 
    {
	return this.port;
    }

    /**
     * Adds servers as elements to the provided element.
     *
     * @param element the element to which the servers should be added.
     */
    public void addServersToElement(Element element)
    {
	for (Enumeration e = getServers().elements(); e.hasMoreElements();) {
	    StratmasServer server = (StratmasServer) e.nextElement();
	    if (server.isGood() && ! server.isPending()) {
		element.appendChild(server.toDOMElement(element.getOwnerDocument()));
	    }
	}
    }

    /**
     * Returns the list of servers currently being handled by this dispatcher.
     */
    public Vector getServers()
    {
	return this.servers;
    }

    /**
     * Server starter method.
     *
     * @param args argument to the server
     */
    public static void main(String[] args)
    {

	// Poor mans getopt
	Hashtable options = new Hashtable();
	Vector arguments = new Vector();
	for (int i = 0; i < args.length; i++) {
	    // Is this an option?
	    if (args[i].matches("-.*")) {
		// If more args and the next one does not begin with -, 
		// regard it as a parameter to the present option
		String option = args[i];
		String parameter = "";
		if ((i + 1) < args.length && !args[i + 1].matches("-.*")) {
		    parameter = args[++i];
		}
		options.put(option, parameter);
	    } else {
		arguments.add(args[i]);
	    }
	}

	// Process args:
	String portstr = (String) options.remove("-p");
	int port = StratmasDispatcher.DEFAULT_PORT;
	if (portstr != null) {
	    try {
		port = Integer.parseInt(portstr);
	    } catch (NumberFormatException e) {
		log("Unable to parse portnumber: \"" + portstr + "\"");
		System.exit(1);
	    }
	}

	// If unprocessed args remain, print usage string.
	if (options.size() != 0) {
	    System.err.println("Unregocnised arguments, usage:");
	    System.err.println(" -p portnr");

	    System.exit(1);
	}

	StratmasDispatcher dispatcher = new StratmasDispatcher(port);
	dispatcher.run();
    }
}

