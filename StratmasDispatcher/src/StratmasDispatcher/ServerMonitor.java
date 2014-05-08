//         $Id: ServerMonitor.java,v 1.8 2006/03/24 11:36:10 dah Exp $

/*
 * @(#)ServerMonitor.java
 */

package ApproxsimDispatcher;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

import java.util.Vector;
import java.util.Timer;
import java.util.TimerTask;


import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;

import java.net.InetSocketAddress;

import  javax.xml.parsers.DocumentBuilderFactory;
import  org.w3c.dom.bootstrap.DOMImplementationRegistry;
import  org.w3c.dom.DOMImplementation;
import  org.w3c.dom.Document;
import  org.w3c.dom.NodeList;
import  org.w3c.dom.ls.DOMImplementationLS;
import  org.w3c.dom.DOMError;
import  org.w3c.dom.DOMErrorHandler;
import  org.w3c.dom.ls.LSInput;
import  org.w3c.dom.ls.LSOutput;
import  org.w3c.dom.ls.LSParser;
import  org.w3c.dom.ls.LSResourceResolver;
import  org.w3c.dom.ls.LSException;
import  org.w3c.dom.TypeInfo;
import  org.w3c.dom.Element;


/**
 * The class performing the monitoring of individual servers.
 *
 * @version 1, $Date: 2006/03/24 11:36:10 $
 * @author  Daniel Ahlin
*/
public class ServerMonitor implements Runnable
{
    /**
     * The server to monitor
     */
    ApproxsimServer server;

    /**
     * ApproxsimProtocol namespace
     */
    static String APPROXSIM_PROTOCOL_NS = "http://pdc.kth.se/approxsimNamespace";

    /**
     * ApproxsimProtocol location
     */
    static String APPROXSIM_PROTOCOL = "approxsimProtocol.xsd";

    /**
     * XML Schema namespace
     */
    static String XML_SCHEMA_NS = "http://www.w3.org/2001/XMLSchema-instance";

    /**
     * The sample rate of this monitor in milliseconds.
     */
    long sampleRate = 5000;

    /**
     * The DomImplementationLS to use.
     */
    private static DOMImplementationLS domImplementationLS = createDomImplementationLS();

    /**
     * The status request.
     */
    private static Document loadQueryMessage = createLoadQueryMessage();

    /**
     * The buffer containing a ready made bytebuffer of the
     * request. (Initialized by createLoadQueryMessage())
     */
    private static byte[] loadQueryMessageBuffer;

    /**
     * Creates a monitor for the specified server.
     *
     * @param server the server to monitor.
     */
    public ServerMonitor(ApproxsimServer server)
    {
        this.server = server;
    }
    
    /**
     * Returns the server this monitor watches.
     */
    public ApproxsimServer getServer()
    {
        return this.server;
    }

    /**
     * Returns the sampleRate in milliseconds.
     */
    public long getSampleRate()
    {
        return this.sampleRate;
    }

    /**
     * Creates the load query message
     */
    private static Document createLoadQueryMessage()
    {
        Document document = null;
        if (domImplementationLS == null) {
            domImplementationLS = createDomImplementationLS();
        }
        try {

            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            document.createEntityReference("xsi");
            Element element = document.createElementNS(APPROXSIM_PROTOCOL_NS, "approxsimMessage");
            element.setPrefix("sp");
            element.setAttribute("xmlns:xsi", XML_SCHEMA_NS);
            element.setAttribute("xsi:type", "sp:LoadQueryMessage");
            document.appendChild(element);
            LSOutput output = domImplementationLS.createLSOutput();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            output.setByteStream(outputStream);
            document.normalize();
            domImplementationLS.createLSSerializer().write(document, output);
            byte[] array = outputStream.toByteArray();            
            ByteBuffer byteBuffer = ByteBuffer.allocate(16 + array.length);
            byteBuffer.putLong(array.length);
            byteBuffer.putLong(0l);
            byteBuffer.put(array);
            loadQueryMessageBuffer = byteBuffer.array();
        } catch (javax.xml.parsers.ParserConfigurationException e) {
            System.err.println("Unable to create vital XML-document: " + 
                               e.getMessage());
            System.exit(1);
        }

        return document;
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
     * Creates a parser for communication with a ApproxsimServer.
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

        parser.getDomConfig().setParameter("schema-location", APPROXSIM_PROTOCOL);
        parser.getDomConfig().setParameter("validate", Boolean.TRUE);
        parser.getDomConfig().setParameter("namespaces", Boolean.TRUE);

        LSResourceResolver prevResolver = 
            (LSResourceResolver) parser.getDomConfig().getParameter("resource-resolver");
        parser.getDomConfig().setParameter("resource-resolver", 
                                           new LSJarXSDResolver(prevResolver));

        return parser;

    }
    
    /**
     * The run method of this server, basically a sleep-check-sleep
     * loop. This function will return iff getServer().isBad() == true
     */
    public void run()
    {
        // Timer used to ensure that io is performed within a reasonable time.
        Timer timer = new Timer();

        try {
            while(!getServer().isBad()) {
                getServer().setPending(true);
                
                // Don't let this take more than 4 * samplerate
                // seconds (where 4 is extremely arbitrary).
                timer = new Timer();
                timer.schedule(new TimerTask() 
                    {
                        public void run()
                        {
                            downCheck("Timed out opening connection.");
                        }
                    }, 4 * getSampleRate());
                
                final SocketChannel channel = 
                    SocketChannel.open(new InetSocketAddress(getServer().getHost(),
                                                             getServer().getPort()));
                timer.cancel();
                // Set up new timer which will provoke a quicker exit
                // by closing the channel.
                timer = new Timer();
                timer.schedule(new TimerTask() 
                    {
                        public void run()
                        {
                            try {
                                channel.close();
                            } catch (IOException e) {
                                downCheck(e.getMessage());
                            }
                            downCheck("Timed out requesting status.");
                        }
                    }, 4 * getSampleRate());
                
                channel.socket().getOutputStream().write(loadQueryMessageBuffer);
                LSInput parserInput = domImplementationLS.createLSInput();
                ByteBuffer header = ByteBuffer.allocate(16);                
                for(int r = 0; r < 16; r += channel.read(header));
                header.rewind();
                long length = header.getLong();
                ByteBuffer messageBuf = ByteBuffer.allocate((int) length);
                for(int r = 0; r < length; r += channel.read(messageBuf));
                ByteArrayInputStream byteStream = new ByteArrayInputStream(messageBuf.array());
                parserInput.setByteStream(byteStream);
                Document reply = createParser().parse(parserInput);
                channel.close();

                timer.cancel();
                String type = reply.getDocumentElement().getAttributeNS(XML_SCHEMA_NS,
                                                                        "type");
//                Wait for DOM3
//                 TypeInfo typeInfo = reply.getDocumentElement().getSchemaTypeInfo();
//                 if (typeInfo.isDerivedFrom(APPROXSIM_PROTOCOL_NS,
//                                            "LoadQueryResponseMessage",
//                                            0)) {
                if (type != null && type.equals("sp:LoadQueryResponseMessage")) {
                    if (getServer().udateFromLoadQueryResponse(reply.getDocumentElement())) {
                        getServer().setGood(true);
                        getServer().setPending(false);
                    } else {
                        throw new RuntimeException("Malformed LoadQueryResponseMessage "  + 
                                                   "accepted by XML parser");
                    }
//                 } else if (typeInfo.isDerivedFrom(APPROXSIM_PROTOCOL_NS,
//                                                   "StatusMessage",
//                                                   0)) {
                } else if (type != null && type.equals("sp:StatusMessage")) {
                    // Collect any errors:
                    StringBuffer errorDescriptions = new StringBuffer();
                    NodeList errors = 
                        reply.getDocumentElement().getElementsByTagName("error");
                    for (int i = 0; i < errors.getLength(); i++) {
                        errorDescriptions.append("\"" + ((Element) errors.item(i)).getElementsByTagName("description").item(0).getFirstChild().getNodeValue().toString() + "\" ");
                    }
                    downCheck("Unexpected status message from server: " + 
                              reply.getDocumentElement().getElementsByTagName("type").item(0).getFirstChild().getNodeValue() + ": " + errorDescriptions.toString());
                } else {
                    //downCheck("Unexpected reply from server: " + typeInfo.getTypeName());
                    downCheck("Unexpected reply type from server: " + type);
                }

                try {
                    Thread.currentThread().sleep(getSampleRate());
                } catch (InterruptedException e) {
                    // Don't care (faster samplerate not really a problem.)
                }
            }
        } catch (LSException e) {
            downCheck(e.getMessage());
            timer.cancel();
        } catch (IOException e) {
            downCheck(e.getMessage());
            timer.cancel();
        } catch (RuntimeException e) {
            e.printStackTrace();
            timer.cancel();
            downCheck("Error in monitor: " + e.getMessage());
        }
    }

    /**
     * Causes a server to be downchecked.
     *
     * @param message string describing the reason of downchecking.
     */
    void downCheck(String message)
    {
        ApproxsimDispatcher.log("Downchecking " + 
                               getServer().getHost() + ":" + 
                               getServer().getPort() + " due to: " + 
                               message);
        getServer().setGood(false);
        getServer().setPending(false);
        getServer().markAsBad();
    }
}
