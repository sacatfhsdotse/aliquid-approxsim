//         $Id: StratmasDispatcher.java,v 1.5 2006/03/27 13:36:48 dah Exp $

/*
 * @(#).StratmasDispatcher.java
 */

package StratmasClient.dispatcher;

import StratmasClient.communication.StratmasSocket;
import StratmasClient.communication.ConnectMessage;
import StratmasClient.LSJarXSDResolver;
import StratmasClient.StratmasConstants;

import StratmasClient.Debug;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Random;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;

import java.net.InetSocketAddress;

import  javax.xml.parsers.DocumentBuilderFactory;
import  org.w3c.dom.bootstrap.DOMImplementationRegistry;
import  org.w3c.dom.Document;
import  org.w3c.dom.Element;
import  org.w3c.dom.NodeList;
import  org.w3c.dom.ls.DOMImplementationLS;
import  org.w3c.dom.DOMError;
import  org.w3c.dom.DOMErrorHandler;
import  org.w3c.dom.ls.LSInput;
import  org.w3c.dom.ls.LSOutput;
import  org.w3c.dom.ls.LSParser;
import  org.w3c.dom.ls.LSException;
import org.w3c.dom.ls.LSResourceResolver;

/**
 * StratmasDispatcher represents information about a dispatcher server
 *
 * @version 1, $Date: 2006/03/27 13:36:48 $
 * @author  Daniel Ahlin
*/
public class StratmasDispatcher
{
    /**
     * The hostname of the dispatcher.
     */
    String hostname;

    /**
     * The port on which the dispatcher is listening.
     */
    int port;

    /**
     * XML Schema namespace
     */
    static String XML_SCHEMA_NS = "http://www.w3.org/2001/XMLSchema-instance";

    /**
     * Dispatcher Protocol location
     */
    static String DISPATCHER_PROTOCOL = "dispatcherReply.xsd";

    /**
     * The DomImplementationLS to use.
     */
    private static DOMImplementationLS domImplementationLS = createDomImplementationLS();

    /**
     * The list servers request.
     */
    private static Document listServers = createListServersMessage();

    /**
     * The buffer containing a ready made bytebuffer of the
     * request. (Initialized by createLoadQueryMessage())
     */
    private static byte[] listServersMessageBuffer;

    /**
     * Creates a new server record using the specified hostname and port.
     *
     * @param hostname the hostname of the server.
     * @param port the port on which the server is listening.
     */    
    public StratmasDispatcher(String hostname, int port)
    {
        this.hostname = hostname;
        this.port = port;
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
    public String getHostname()
    {
        return this.hostname;
    }

    /**
     * Returns an instance of a default dispatcher
     */
    public static StratmasDispatcher getDefaultDispatcher() 
    {
            String dispatcherString = System.getProperty("DISPATCHER");
        if (dispatcherString != null && dispatcherString != "") {
            String[] parts = dispatcherString.split(":");
            int port = 4181;
            if (parts.length == 2 && parts[1].matches("\\A\\p{Digit}+\\z")) {
                port = Integer.parseInt(parts[1]);
            }
            return new StratmasDispatcher(parts[0], port);
        }
        
        return null;
    }
    /**
     * Tries to allocate a server from the pool of servers known to the dispatcher.
     *
     * @param retries max number of times to trie to allocate.
     */
    public StratmasSocket allocateServer(int retries)
    {
        Random random = new Random();
        LSInput parserInput = domImplementationLS.createLSInput();

        for (int i = 0; i < retries; i++) {
            Vector prospects = new Vector();
            for (Enumeration e = getServers().elements(); e.hasMoreElements();) {
                StratmasServer server = (StratmasServer) e.nextElement();
                if (!server.isBusy()) {
                    prospects.add(server);
                }
            }
            
            if (prospects.size() != 0) {
                // Randomly pick one:
                int index = random.nextInt(prospects.size());
                StratmasServer prospect = (StratmasServer) prospects.get(index);
                Debug.err.println(i + ": Trying " + prospect.toString());
                // Try to become active client.
                StratmasSocket socket = null;
                try {
                    socket = new StratmasSocket();
                    socket.connect(prospect.getHost(), prospect.getPort());
                    ConnectMessage message = new ConnectMessage();
                    socket.sendMessage(message.toXML());
                    parserInput.setStringData(socket.recvMessage());
                    Document reply = createStratmasParser().parse(parserInput);
                    NodeList list = reply.getDocumentElement().getElementsByTagName("active");
                    if (list.getLength() != 1) {
                        Debug.err.println("Unexpected form of ConnectResponseMessage");
                    } else if (Boolean.valueOf(list.item(0).getFirstChild().getNodeValue()).booleanValue()) {
                        // Hurray, we are active.
                        Debug.err.println("Returning " + prospect.toString());
                        return socket;
                    }
                    
                    socket.close();
                } catch (IOException e) {
                    Debug.err.println(e.getMessage());
                    if (socket != null) {
                        socket.close();
                    }
                } catch (LSException e) {
                    Debug.err.println(e.getMessage());
                    if (socket != null) {
                        socket.close();
                    }
                }
            }
        }

        Debug.err.println("allocateServer failed to allocate server");
        return null;
    }

    /**
     * Creates the list query message
     */
    private static Document createListServersMessage()
    {
        Document document = null;
        if (domImplementationLS == null) {
            domImplementationLS = createDomImplementationLS();
        }
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            document.createEntityReference("xsi");
            Element element = document.createElement("dispatcherRequest");
            element.setAttribute("xmlns:xsi", XML_SCHEMA_NS);
            element.setAttribute("xsi:type", "ListRequest");
            document.appendChild(element);
            LSOutput output = domImplementationLS.createLSOutput();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            output.setByteStream(outputStream);
            document.normalize();
            domImplementationLS.createLSSerializer().write(document, output);

            byte[] array = outputStream.toByteArray();            
            ByteBuffer byteBuffer = ByteBuffer.allocate(4 + array.length);
            byteBuffer.putInt(array.length);
            byteBuffer.put(array);
            byteBuffer.rewind();
            listServersMessageBuffer = byteBuffer.array();
        } catch (javax.xml.parsers.ParserConfigurationException e) {
            System.err.println("Unable to create vital XML-document: " + 
                               e.getMessage());
            System.exit(1);
        }

        return document;
    }

    /**
     * Creates a parser for communication with a StratmasServer.
     */
    public LSParser createStratmasParser()
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

        parser.getDomConfig().setParameter("schema-location", 
                                           StratmasConstants.STRATMAS_PROTOCOL_SCHEMA);
        parser.getDomConfig().setParameter("validate", Boolean.TRUE);
        parser.getDomConfig().setParameter("namespaces", Boolean.TRUE);

        LSResourceResolver prevResolver = 
            (LSResourceResolver) parser.getDomConfig().getParameter("resource-resolver");
        parser.getDomConfig().setParameter("resource-resolver", 
                                           new LSJarXSDResolver(prevResolver));

        return parser;
    }

    /**
     * Creates a parser for communication with a Dispatcher replier..
     */
    public LSParser createDispatcherParser()
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
     * Returns the DomImplementationLS to use.
     */
    private static DOMImplementationLS createDomImplementationLS()
    {
        if (domImplementationLS != null) {
            return domImplementationLS;
        } else {
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
    }
    
    /**
     * Returns a vector with the servers listed on this dispatcher.
     */
    public Vector getServers()
    {
        Vector res = new Vector();
        SocketChannel channel = null;

        try {
            channel = SocketChannel.open(new InetSocketAddress(getHostname(),
                                                               getPort()));
            try {
                channel.socket().getOutputStream().write(listServersMessageBuffer);
                LSInput parserInput = domImplementationLS.createLSInput();

                ByteBuffer header = ByteBuffer.allocate(4);
                for(int r = 0; r < 4; r += channel.read(header));
                header.rewind();
                int length = header.getInt();
                parserInput.setByteStream(channel.socket().getInputStream());
                Document reply = createDispatcherParser().parse(parserInput);
                NodeList servers = reply.getDocumentElement().getElementsByTagName("stratmasServer");
                for (int i = 0; i < servers.getLength(); i++) {
                    res.add(StratmasServer.fromDOMElement((Element) servers.item(i)));
                }
            } catch (org.w3c.dom.ls.LSException e) {
                System.err.println(e.getMessage());
            }
            channel.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            try {
                if (channel != null) {
                    channel.close();
                }
            } catch (IOException ex) {
                System.err.println(e.getMessage());
            }
        }
    
        return res;
    }

}
