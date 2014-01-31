// 	$Id: ConnectionHandler.java,v 1.4 2005/10/07 12:57:21 dah Exp $

/*
 * @(#)ConnectionHandler.java
 */

package StratmasDispatcher;

import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;
import java.net.InetSocketAddress;

import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSParserFilter;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.ls.LSResourceResolver;
import org.w3c.dom.ls.LSException;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * A class used to handle connections made to the dispatcher.
 *
 * @version 1, $Date: 2005/10/07 12:57:21 $
 * @author  Daniel Ahlin
*/	
public class ConnectionHandler implements Runnable
{
    /**
     * The socket making up the connection
     */
    SocketChannel socketChannel;
    
    /**
     * XML Schema namespace
     */
    static String XML_SCHEMA_NS = "http://www.w3.org/2001/XMLSchema-instance";

    /**
     * StratmasProtocol location
     */
    static String DISPATCHER_PROTOCOL = "dispatcherRequest.xsd";

    /**
     * The dispatcher to which the handler is associated.
     */
    StratmasDispatcher dispatcher;

    /**
     * The DomImplementationLS to use.
     */
    private static DOMImplementationLS domImplementationLS = createDomImplementationLS();

    /**
     * The handlers for the different element types.
     */
    static Hashtable elementHandlers = createElementHandlers();

    /**
     * The default handler (for requests with no registred handler).
     */
    static ElementHandler defaultElementHandler = new ElementHandler()
	{
	    /**
	     * Handles an unjknown request by logging it and being very quiet.
	     *
	     * @param request the element to handle
	     * @param reply the reply which will be sent back.
	     * @param handler the connection handler this handler belongs to.
	     */
	    public boolean handle(Element request, Element reply, ConnectionHandler handler)
	    {
		StratmasDispatcher.log("Unknown element type: " + 
				       request.getAttribute("xsi:type"));
		return false;
	    }
	};

    /**
     * Creates a new ConnectionHandler
     *
     * @param socketChannel the socket making up the connection
     * @param dispatcher the dispatcher to which the handler is associated.
     */
    public ConnectionHandler(SocketChannel socketChannel, StratmasDispatcher dispatcher)
    {
	this.socketChannel = socketChannel;
	this.dispatcher = dispatcher;
    }

    /**
     * Returns the DomImplementationLS to use.
     */
    private static DOMImplementationLS createDomImplementationLS()
    {
	try {
	    System.setProperty(DOMImplementationRegistry.PROPERTY,
			       "org.apache.xerces.dom.DOMImplementationSourceImpl");
	    DOMImplementationRegistry registry = 
		DOMImplementationRegistry.newInstance();
	    return (DOMImplementationLS)registry.getDOMImplementation("LS");
	} catch (ClassNotFoundException e) {
	    System.err.println("Unable to find DOM Parser: " + e.getMessage());
	    System.exit(1);
	    return null;
	} catch (InstantiationException e) {
	    System.err.println("Unable to find DOM Parser: " + e.getMessage());
	    System.exit(1);
	    return null;
	} catch (IllegalAccessException e) {
	    System.err.println("Unable to find DOM Parser: " + e.getMessage());
	    System.exit(1);
	    return null;
	}

    }

    /**
     * Creates a parser for communication with a Dispatcher requestor..
     */
    public LSParser createParser()
    {
	LSParser parser = 
	    domImplementationLS.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, 
					       "http://www.w3.org/2001/XMLSchema");
	parser.getDomConfig().setParameter("error-handler", new DOMErrorHandler() 
	    {
		/**
		 * This method is called on the error handler when an error occurs.
		 *
		 * @param error the error;
		 */
		public boolean handleError(DOMError error)
		{
		    throw new LSException(LSException.PARSE_ERR, 
					  error.getMessage());

		}
	    });

	parser.getDomConfig().setParameter("schema-location", DISPATCHER_PROTOCOL);
	parser.getDomConfig().setParameter("validate", Boolean.TRUE);
	parser.getDomConfig().setParameter("namespaces", Boolean.TRUE);

	LSResourceResolver prevResolver = 
	    (LSResourceResolver) parser.getDomConfig().getParameter("resource-resolver");
	parser.getDomConfig().setParameter("resource-resolver", 
					   new LSJarXSDResolver(prevResolver));

	return parser;
    }

    /**
     * Returns the socket of this connection
     */
    public SocketChannel getSocketChannel()
    {
	return this.socketChannel;
    }

    /**
     * Returns the dispatcher of this connection
     */
    public StratmasDispatcher getDispatcher()
    {
	return this.dispatcher;
    }

    /**
     * The method doing the work, either registering a new server or
     * returning information of availiable servers.
     */
    public void run()
    {
	LSInput parserInput = domImplementationLS.createLSInput();
	LSOutput replyOutput = domImplementationLS.createLSOutput();
	try {	    
	    try {
		// Read length:
		ByteBuffer header = ByteBuffer.allocate(4);
		for(int r = 0; r < 4; r += getSocketChannel().read(header));
		header.rewind();
		int length = header.getInt();
		ByteBuffer messageBuf = ByteBuffer.allocate(length);
		for(int r = 0; r < length; r += getSocketChannel().read(messageBuf));
		ByteArrayInputStream byteStream = new ByteArrayInputStream(messageBuf.array());
		parserInput.setByteStream(byteStream);		
		Document reply = handle(createParser().parse(parserInput).getDocumentElement());
		if (reply != null && reply.getDocumentElement() != null) {
		    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		    replyOutput.setByteStream(outputStream);
		    reply.normalize();
		    domImplementationLS.createLSSerializer().write(reply, replyOutput);
		    byte[] array = outputStream.toByteArray();
		    ByteBuffer byteBuffer = ByteBuffer.allocate(4 + array.length);
		    byteBuffer.putInt(array.length);
		    byteBuffer.put(array);
		    byteBuffer.rewind();
		    for(int w = 0; w < 4 + array.length; w += getSocketChannel().write(byteBuffer));
		}
	    } catch (org.w3c.dom.ls.LSException e) {
		StratmasDispatcher.log("Parse error recieving from " + 
				       getSocketChannel().socket().getRemoteSocketAddress() +
				       ": " + e.getMessage());
	    }
	    getSocketChannel().close();
	} catch (IOException e) {
	    StratmasDispatcher.log("Error recieiving from " + 
				   getSocketChannel().socket().getRemoteSocketAddress() + 
				   ":" + e.getMessage());
	    try {
		getSocketChannel().close();
	    } catch (IOException ex) {
		StratmasDispatcher.log(ex.getMessage() + "\nFatal error, exiting...");
		System.exit(1);
	    }
	}	
    }

    /**
     * Creates the table mapping element types to handlers
     */
    private static Hashtable createElementHandlers()
    {
	Hashtable res = new Hashtable();

	res.put("RegistrationRequest", new ElementHandler()
	    {
		/**
		 * Handles a registration request by registering the
		 * server described in the provided element.
		 *
		 * @param request the request to handle
		 * @param reply the reply which will be sent back.
		 * @param handler the connection handler this handler belongs to.
		 */
		public boolean handle(Element request, Element reply, ConnectionHandler handler)
		{
		    handler.getDispatcher().registerServer(StratmasServer.fromDOMElement((Element) request.getElementsByTagName("stratmasServer").item(0)));
		    return false;
		}
	    });
	res.put("ListRequest", new ElementHandler()
	    {
		/**
		 * Handles a list request by doing nothing the
		 * server described in the provided element.
		 *
		 * @param request the request to handle
		 * @param reply the reply which will be sent back.
		 * @param handler the connection handler this handler belongs to.
		 */
		public boolean handle(Element request, Element reply, ConnectionHandler handler)
		{		    
		    reply.setAttribute("xsi:type", "ListReply");
		    handler.getDispatcher().addServersToElement(reply);
		    return true;
		}
	    });

	return res;
    }

    /**
     * Performs tasks depending on the type of the provided element
     *
     * @param request the element to handle.
     *
     * @return a document to send to the requestor
     */
    Document handle(Element request)
    {
	StratmasDispatcher.log(request.getAttribute("xsi:type") + " from " +
			       getSocketChannel().socket().getRemoteSocketAddress());
	ElementHandler handler = 
	    (ElementHandler) elementHandlers.get(request.getAttribute("xsi:type"));
	if (handler == null) {
	    handler = defaultElementHandler;
	}

	Document reply = null;
	try {
	    reply = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
	    reply.createEntityReference("xsi");
	    Element element = reply.createElement("dispatcherReply");
	    element.setAttribute("xmlns:xsi", XML_SCHEMA_NS);
	    element.setAttribute("xsi:type", "DispatcherReply");
	    reply.appendChild(element);
	    boolean sendReply = handler.handle(request, reply.getDocumentElement(), this);
	    if (!sendReply) {
		reply = null;
	    }
	} catch (javax.xml.parsers.ParserConfigurationException e) {
	    System.err.println(e.getMessage());
	}

	return reply;
    }
}

/**
 * Class encapsulating common handler tasks
 */
abstract class ElementHandler
{
    /**
     * Handles request contained in the provided element, the returned element 
     * is sent back to the requestor. Returns true if the reply is to be sent.
     *
     * @param request the request to handle
     * @param reply the reply which will be sent back.
     * @param handler the connection handler this handler belongs to.
     *
     * @return the reply to the requestor, or null if none provided.
     */
    abstract public boolean handle(Element request, Element reply, ConnectionHandler handler);
}
