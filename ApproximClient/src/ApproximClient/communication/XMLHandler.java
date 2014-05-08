package ApproxsimClient.communication;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.parsers.IntegratedParserConfiguration;
import org.apache.xerces.parsers.XMLGrammarPreparser;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLGrammarPoolImpl;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.TypeInfo;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ApproxsimClient.Client;
import ApproxsimClient.Debug;
import ApproxsimClient.ProcessVariableDescription;
import ApproxsimClient.object.primitive.Timestamp;
import ApproxsimClient.object.type.TypeFactory;
import ApproxsimClient.object.type.Type;
import ApproxsimClient.ApproxsimConstants;
import ApproxsimClient.LSJarXSDResolver;
import org.apache.xerces.util.XMLResourceIdentifierImpl;

// For printNode only
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;

/**
 * This is the class where all XML from the server is parsed and distributed to its proper place.
 * 
 * @version 1, $Date: 2006/09/11 09:33:42 $
 * @author Per Alexius
 */
public class XMLHandler implements Runnable {
    private static final int sThreshold = 2;

    /** Property identifier: grammar pool. */
    public static final String GRAMMAR_POOL = Constants.XERCES_PROPERTY_PREFIX
            + Constants.XMLGRAMMAR_POOL_PROPERTY;

    /** Namespaces feature id (http://xml.org/sax/features/namespaces). */
    protected static final String NAMESPACES_FEATURE_ID = Constants.SAX_FEATURE_PREFIX
            + Constants.NAMESPACES_FEATURE;

    /** Validation feature id (http://xml.org/sax/features/validation). */
    protected static final String VALIDATION_FEATURE_ID = Constants.SAX_FEATURE_PREFIX
            + Constants.VALIDATION_FEATURE;

    /** Schema validation feature id (http://apache.org/xml/features/validation/schema). */
    protected static final String SCHEMA_VALIDATION_FEATURE_ID = Constants.XERCES_FEATURE_PREFIX
            + Constants.SCHEMA_VALIDATION_FEATURE;

    /** Schema full checking feature id (http://apache.org/xml/features/validation/schema-full-checking). */
    protected static final String SCHEMA_FULL_CHECKING_FEATURE_ID = Constants.XERCES_FEATURE_PREFIX
            + Constants.SCHEMA_FULL_CHECKING;

    /** The client. */
    private Client mClient;

    /** The queue used to store incomming messages. */
    private TSQueue mQueue = new TSQueue();

    /** The DOMParser. */
    protected DOMParser mParser;

    /** The SubscriptionHandler. */
    private SubscriptionHandler mSH;

    /**
     * Constructor. Creates and initializes the DOMParser.
     * 
     * @param client The client.
     * @param grammar The name of the schema file to use for validation.
     */
    public XMLHandler(Client client, String grammar) {
        boolean validate = true;
        mClient = client;

        if (!(grammar == null)) {
            SymbolTable sym = new SymbolTable(2039);
            XMLGrammarPreparser preparser = new XMLGrammarPreparser(sym);
            XMLGrammarPoolImpl grammarPool = new XMLGrammarPoolImpl();
            preparser.registerPreparser(XMLGrammarDescription.XML_SCHEMA, null);
            preparser.setProperty(GRAMMAR_POOL, grammarPool);
            preparser.setFeature(NAMESPACES_FEATURE_ID, true);
            preparser.setFeature(VALIDATION_FEATURE_ID, validate);
            preparser.setFeature(SCHEMA_VALIDATION_FEATURE_ID, validate);
            preparser.setFeature(SCHEMA_FULL_CHECKING_FEATURE_ID, validate);
            try {
                preparser.setEntityResolver(new LSJarXSDResolver(preparser
                        .getEntityResolver()));
                XMLInputSource source = preparser.getEntityResolver()
                        .resolveEntity(new XMLResourceIdentifierImpl(null,
                                               grammar, null, null));
                preparser.preparseGrammar(XMLGrammarDescription.XML_SCHEMA,
                                          source);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
            XMLParserConfiguration parserConfiguration = new IntegratedParserConfiguration(
                    sym, grammarPool);
            parserConfiguration.setEntityResolver(new LSJarXSDResolver(
                    parserConfiguration.getEntityResolver()));
            // Now must reset features, unfortunately:
            parserConfiguration.setFeature(NAMESPACES_FEATURE_ID, true);
            parserConfiguration.setFeature(VALIDATION_FEATURE_ID, validate);
            parserConfiguration.setFeature(SCHEMA_VALIDATION_FEATURE_ID,
                                           validate);
            parserConfiguration.setFeature(SCHEMA_FULL_CHECKING_FEATURE_ID,
                                           validate);

            if (System.getProperty("os.name").matches("Windows.*")) {
                mParser = new DOMParser();
            } else {
                mParser = new DOMParser(parserConfiguration);
            }
        } else {
            Debug.err.println("No grammar!");
        }
    }

    /**
     * Creates an XMLHandler operating independently of any Client, initializes a default DOMParser.
     */
    public XMLHandler() {
        this(null, ApproxsimConstants.APPROXSIM_PROTOCOL_SCHEMA);
    }

    /**
     * Starts a new Thread for this XMLHandler.
     */
    public void start() {
        (new Thread(this, getClass().getName())).start();
    }

    /**
     * Terminates the Thread running this XMLHandler.
     */
    public void kill() {
        mQueue.enqueue(null);
    }

    /**
     * The main loop.
     */
    public void run() {
        QueueEntry qe;
        while (true) {
            qe = (QueueEntry) mQueue.blockingDequeue();
            if (thresholdReached() && mClient != null) {
                mClient.setNotify();
            }
            if (qe == null) {
                break;
            }
            try {
                Object reply = handleDoc(qe.getResponseString());
                if (reply instanceof HandleException) {
                    throw (HandleException) reply;
                }
                qe.getMessage().fireMessageHandled(reply);
            } catch (HandleException e) {
                qe.getMessage().fireErrorOccurred();
            }
        }
    }

    /**
     * Connects this XMLHandler to the specified SubscriptionHandler.
     * 
     * @param sh The SubscriptionHandler to use.
     */
    public void connect(SubscriptionHandler sh) {
        mSH = sh;
    }

    /**
     * Returns the subscription handler of this XMLHandler
     */
    public SubscriptionHandler getSubscriptionHandler() {
        return this.mSH;
    }

    /**
     * Erases all messages in the queue.
     */
    public void reset() {
        mQueue.clear();
    }

    /**
     * Checks if the threshold for the number of incomming messages is reached.
     * 
     * @return True if the threshold is reached, false otherwise.
     */
    public boolean thresholdReached() {
        return mQueue.size() < sThreshold;
    }

    protected void dumpToFile(String filename, String toDump) {
        try {
            java.io.PrintWriter pw = new java.io.PrintWriter(
                    new java.io.FileWriter(filename));
            System.err.println("Dumping to file: " + filename);
            pw.print(toDump);
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Enqueues an xml message for handling.
     * 
     * @param xml The message to enqueue.
     * @param msg The ApproxsimMessage that generated the response now to be handled.
     */
    public void handle(String xml, ApproxsimMessage msg) {
        mQueue.enqueue(new QueueEntry(msg, xml));
    }

    /**
     * Handles the provided xml message.
     * 
     * @param xml The message to handle.
     * @return the result of the handling, if any, else null
     * @throws HandleException on error.
     */
    private Object handleDoc(String xml) throws HandleException {
        Object reply = null;
        try {
            // Parse the Document
            InputSource inputSource = new InputSource(new StringReader(xml));
            inputSource.setEncoding("ISO-8859-1");
            mParser.parse(inputSource);

            Element elem = mParser.getDocument().getDocumentElement();
            if (elem.getNodeName().equals("sp:approxsimMessage")) {
                String type = elem.getAttribute("xsi:type");
                // ConnectResponseMessage
                if (type.equals("sp:ConnectResponseMessage")) {
                    Debug.err.println("ConnectResponseMessage");
                    handleConnectResponseMsg(elem);
                }
                // ServerCapabilitiesResponseMessage
                else if (type.equals("sp:ServerCapabilitiesResponseMessage")) {
                    Debug.err.println("ServerCapabilitiesResponseMessage");
                    reply = handleServerCapabilitiesResponseMsg(elem);
                }
                // GetGridResponseMessage
                else if (type.equals("sp:GetGridResponseMessage")) {
                    Debug.err.println("GetGridResponseMessage");
                    handleGetGridResponseMsg(elem);
                }
                // InitializationResponseMessage
                else if (type.equals("sp:InitializationResponseMessage")) {
                    Debug.err.println("InitializationResponseMessage");
                    handleInitializationResponseMsg(elem);
                }
                // StatusMessage
                else if (type.equals("sp:StatusMessage")) {
                    Debug.err.println("StatusMessage");
                    reply = handleStatusMsg(elem);
                }
                // SubscribedDataMessage
                else if (type.equals("sp:SubscribedDataMessage")) {
                    Debug.err.println("SubscribedDataMessage");
                    Timestamp timestamp = handleSubscribedDataMsg(elem);
                    if (mClient != null) {
                        mClient.notifyHandledSubs(timestamp);
                    }
                    reply = timestamp;
                }
                // UpdateMessage
                else if (type.equals("sp:UpdateClientMessage")) {
                    Debug.err.println("UpdateClientMessage");
                    Timestamp timestamp = handleUpdateMsg(elem);
                    if (mClient != null) {
                        mClient.notifyHandledSubs(timestamp);
                    }
                    reply = timestamp;
                } else {
                    Debug.err.println("Unknown message type'" + type + "'");
                    throw new HandleException();
                }
            } else {
                System.err.println("Root element not 'sp:approxsimMessage'");
                System.exit(1);
                // throw new HandleException();
            }
        } catch (SAXException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            dumpToFile("DISCARDED_MESSAGE.tmp", xml);
            throw new HandleException();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            dumpToFile("DISCARDED_MESSAGE.tmp", xml);
            throw new HandleException();
        }

        return reply;
    }

    /**
     * Handles a ConnectResponseMessage.
     * 
     * @param The message to handle.
     */
    private void handleConnectResponseMsg(Element elem) {
        boolean active = getBoolean(elem, "active");
        Debug.err.println((active ? "active" : "passive"));
        if (mClient != null) {
            mClient.setActiveClient(active);
            mClient.updateStatus(new Hashtable<String, Vector<String>>(),
                                 "ConnectResponseMessage");
        }
    }

    /**
     * Handles an GetGridResponseMessage.
     * 
     * @param The message to handle.
     */
    private void handleGetGridResponseMsg(Element elem) {
        GridData gd = new GridData(getFirstChildByTag(elem, "gridData"));
        if (mClient != null) {
            mClient.setGrid(gd);

//               // FIXME - This is ugly...
//               mClient.updateStatus(new Hashtable(), "GetGridMessage");
        }
    }

    /**
     * Handles an InitializationResponseMessage.
     * 
     * @param The message to handle.
     */
    private void handleInitializationResponseMsg(Element elem) {
        GridData gd = new GridData(getFirstChildByTag(elem, "gridData"));
        if (mClient != null) {
            mClient.setGrid(gd);

            // FIXME - This is ugly...
            mClient.updateStatus(new Hashtable<String, Vector<String>>(),
                                 "InitializationMessage");
        }
    }

    /**
     * Handles a StatusMessage.
     * 
     * @param n The message to handle.
     */
    private HandleException handleStatusMsg(Element n) {
        Hashtable<String, Vector<String>> errs = new Hashtable<String, Vector<String>>();
        errs.put("fatal", new Vector<String>());
        errs.put("general", new Vector<String>());
        errs.put("warning", new Vector<String>());
        StringBuffer buffer = new StringBuffer();

        String type = getString(n, "type");

        for (Node child = n.getFirstChild(); child != null; child = child
                .getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) child;
                if (elem.getTagName().equals("error")) {
                    Vector<String> v = errs.get(getString(elem, "type"));
                    String err = getString(elem, "description");
                    v.add(err);
                    buffer.append(" " + err);
                }
            }
        }
        //
        if (mClient != null) {
            mClient.updateStatus(errs, type);
        }

        if (buffer.length() > 0) {
            final String errString = buffer.toString();
            return new HandleException() {
                /**
					 * 
					 */
                private static final long serialVersionUID = -8195832072394647380L;

                public String getMessage() {
                    return super.getMessage() + " - " + errString;
                }
            };
        } else {
            return null;
        }
    }

    /**
     * Handles a ServerCapabilitiesMessage.
     * 
     * @param n message to handle.
     */
    private Vector<ProcessVariableDescription> handleServerCapabilitiesResponseMsg(
            Element n) {
        Vector<ProcessVariableDescription> pvs = new Vector<ProcessVariableDescription>();
        Element nn = getFirstChildByTag(n, "processVariables");
        for (Node child = nn.getFirstChild(); child != null; child = child
                .getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) child;
                if (elem.getTagName().equals("pv")) {
                    Element range = XMLHandler
                            .getFirstChildByTag(elem, "range");
                    pvs.add(new ProcessVariableDescription(XMLHandler
                            .getString(elem, "name"), XMLHandler
                            .getString(elem, "category"), XMLHandler
                            .getBoolean(elem, "factions"), XMLHandler
                            .getDouble(range, "min"), XMLHandler
                            .getDouble(range, "max")));
                }
            }
        }
        if (mClient != null) {
            mClient.setProcessVariables(pvs);
            mClient.updateStatus(new Hashtable<String, Vector<String>>(),
                                 "ServerCapabilitiesMessage");
        }
        return pvs;
    }

    /**
     * Handles a SubscribedDataMessage.
     * 
     * @param n The message to handle.
     */
    private Timestamp handleSubscribedDataMsg(Element n) {
        Timestamp t = new Timestamp(
                getLong(getFirstChildByTag(n, "simulationTime"), "value"));
        Debug.err.println("Current time: " + t
                + ". Now handling subscriptions...");
        for (Node child = n.getFirstChild(); child != null; child = child
                .getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE
                    && child.getNodeName().equals("subscribedData")) {
                mSH.handleSubscribedData((Element) child, t);
            }
        }
        return t;
    }

    /**
     * Handles an UpdateMessage.
     * 
     * @param n The message to handle.
     */
    private Timestamp handleUpdateMsg(Element n) {
        String tsStr = getString(getFirstChildByTag(n, "simulationTime"),
                                 "value");
        Timestamp t;
        try {
            t = new Timestamp(Timestamp.parseDateTime(tsStr).getTime());
        } catch (java.text.ParseException e) {
            throw new AssertionError("Failed to extract a dateTime from '"
                    + tsStr + "'.");
        }
        Debug.err.println("Current time: " + t + ". Now handling updates...");
        Element elem = getFirstChildByTag(n, "update");
        if (elem != null) {
            mClient.getRootObject().update(elem, t);
        }
        for (Iterator<Element> it = getChildElementsByTag(n, "subscribedData")
                .iterator(); it.hasNext();) {
            mSH.handleSubscribedData(it.next(), t);
        }

        return t;
    }

/// Helpers

    /**
     * Gets the type of ApproxsimObject represented by the provided dom element. Casts the provided Element to an ElementImpl to avoid the
     * problem that the getSchemaTypeInfo method does not exist in 1.4.2. This is not a perfect solution but rather an acceptable hack until
     * 1.5 becomed default version. If the getSchemaTypeInfo method fails the xsi:type attribute is checked. This will occur when validation
     * is switched off, which it often is for performance reasons.
     * 
     * @param element The dom element to get the type for.
     * @return The type of ApproxsimObject the element represents.
     */
    public static Type getType(Element element) {
        TypeInfo typeInfo = ((org.apache.xerces.dom.ElementImpl) element)
                .getSchemaTypeInfo();
        Type ret = TypeFactory.getType(typeInfo.getTypeName(),
                                       typeInfo.getTypeNamespace());
        if (ret == null) {
            ret = TypeFactory.getType(removeNamespace(element
                    .getAttribute("xsi:type")));
            if (ret == null) {
                throw new AssertionError(
                        "getType() failed for element "
                                + element.getTagName()
                                + ". This may indicate that validation is switched off "
                                + "and that the xsi:type attribute is missing.");
            }
        }
        return ret;
    }

    /**
     * Gets the type of ApproxsimObject represented by the provided dom element. First check the xsi:type attribute and if it doesn't exist
     * use the 1.5 specific method getSchemaTypeInfo method. Since Xerces supports this method it should be possible to call it even though
     * 1.4.2 is used.
     * 
     * @param element The dom element to get the type for.
     * @return The type of ApproxsimObject the element represents.
     */
    public static Type getXsiType(Element element) {
        boolean checkSchemaTypeInfo = false;
        Type ret = null;
        String xsiType = element.getAttribute("xsi:type");
        if (!xsiType.equals("")) {
            ret = TypeFactory.getType(removeNamespace(xsiType));
            if (ret == null) {
                checkSchemaTypeInfo = true;
            }
        } else {
            checkSchemaTypeInfo = true;
        }
        if (checkSchemaTypeInfo) {
            try {
                Method getSchemaTypeInfo = element.getClass()
                        .getMethod("getSchemaTypeInfo", (Class<?>[]) null);
                TypeInfo typeInfo = (TypeInfo) getSchemaTypeInfo
                        .invoke(element, (Object[]) null);
                ret = TypeFactory.getType(typeInfo.getTypeName(),
                                          typeInfo.getTypeNamespace());
                if (ret == null) {
                    throw new AssertionError("Can't create Type object for '"
                            + typeInfo.getTypeName() + "' in namespace '"
                            + typeInfo.getTypeNamespace() + "'");
                }
            } catch (IllegalAccessException e) {
                throw new AssertionError(e.toString());
            } catch (InvocationTargetException e) {
                throw new AssertionError(e.toString());
            } catch (ExceptionInInitializerError e) {
                throw new AssertionError(e.toString());
            } catch (NoSuchMethodException e) {
                throw new AssertionError(e.toString());
            }
        }
        return ret;
    }

    /**
     * Gets the first child Element of n that has a tag matching 'tag'.
     * 
     * @param n The Node from which to get the Element
     * @return The first child Element of n that has a tag matching 'tag' or null if there was no such Element.
     */
    public static Element getFirstChildByTag(Node n, String tag) {
        for (Node child = n.getFirstChild(); child != null; child = child
                .getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) child;
                if (tag.equals(elem.getTagName())) {
                    return elem;
                }
            }
        }
        return null;
    }

    /**
     * Gets child Elements of n that has a tag matching 'tag'.
     * 
     * @param n The Node from which to get the Element
     * @param tag The tag.
     * @return A Vector containing all elements with the specified tag.
     */
    public static Vector<Element> getChildElementsByTag(Node n, String tag) {
        Vector<Element> ret = new Vector<Element>();
        for (Node child = n.getFirstChild(); child != null; child = child
                .getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) child;
                if (tag.equals(elem.getTagName())) {
                    ret.add(elem);
                }
            }
        }
        return ret;
    }

    /**
     * Gets the String contained in the first TEXT_NODE child of n. For example, if we know that n has an element with tag TAG, this method
     * returns the value VALUE between the tags '<TAG>VALUE</TAG>'.
     * 
     * @param n The Node from which to get the String
     * @return The String contained in the first TEXT_NODE child of n.
     */
    public static String getString(Node n) {
        for (Node child = n.getFirstChild(); child != null; child = child
                .getNextSibling()) {
            if (child.getNodeType() == Node.TEXT_NODE) {
                return child.getNodeValue();
            }
        }
        return null;
    }

    /**
     * Gets the value of an element of type xsd:string with tag 'tag' that is a child of n.
     * 
     * @param n The parent Node.
     * @return The value of the string.
     */
    public static String getString(Node n, String tag) {
        Element elem = getFirstChildByTag(n, tag);
        if (elem == null) {
            System.err.println("No element with tag '" + tag
                    + "' in Node with name " + n.getNodeName());
            return "";
        } else {
            return getString(elem);
        }
    }

    /**
     * Gets the value of an element of type xsd:boolean with tag 'tag' that is a child of n.
     * 
     * @param n The parent Node.
     * @return The value of the bool.
     */
    public static boolean getBoolean(Node n, String tag) {
        String val = getString(n, tag);
        return (val.equalsIgnoreCase("true") || val.equals("1"));
    }

    /**
     * Gets the value of an element of type xsd:double with tag 'tag' that is a child of n.
     * 
     * @param n The parent Node.
     * @return The value of the xsd:double as a double.
     */
    public static double getDouble(Node n, String tag) {
        String tmp = getString(n, tag);
        if (tmp.trim().endsWith("INF")) {
            tmp = tmp.replaceAll("INF", "Infinity");
        }
        return Double.parseDouble(tmp);
    }

    /**
     * Gets the value of an element of type xsd:integer with tag 'tag' that is a child of n.
     * 
     * @param n The parent Node.
     * @return The value of the xsd:integer as a long
     */
    public static long getLong(Node n, String tag) {
        String tmp = getString(n, tag);
        return Long.parseLong(tmp);
    }

    /**
     * Gets the value of an element of type xsd:integer with tag 'tag' that is a child of n.
     * 
     * @param n The parent Node.
     * @return The value of the xsd:integer as a long
     */
    public static int getInt(Node n, String tag) {
        String tmp = getString(n, tag);
        return Integer.parseInt(tmp);
    }

    /**
     * Gets an Identifier from the element n.
     * 
     * @param n The Element to get the object from.
     * @return The newly created Identifier
     */
    public static String getIdentifier(Element n) {
        String id = n.getAttribute("identifier");
        if (id.equals("")) {
            id = n.getTagName();
        }
        return id;
    }

    /**
     * Removes the namespace from the provided String, that is, removes all characters from the beginning of the String to (and including)
     * the last occurence of the ':' character.
     * 
     * @param s The String to remove the namespace from.
     * @return A copy of the String without namespace.
     */
    public static String removeNamespace(String s) {
        return s.substring(s.lastIndexOf(":") + 1);
    }

    /**
     * Encodes XML special characters.
     * 
     * @param s The String in which to encode the special characters.
     * @return A new String with special characters encoded.
     */
    public static String encodeSpecialCharacters(String s) {
        String res = s;
        res = res.replaceAll("&", "&amp;");
        res = res.replaceAll("<", "&lt;");
        res = res.replaceAll(">", "&gt;");
        res = res.replaceAll("'", "&apos;");
        res = res.replaceAll("\"", "&quot;");
        return res;
    }

    public static void printNode(Node n) {
        try {
            System.setProperty(DOMImplementationRegistry.PROPERTY,
                               "org.apache.xerces.dom.DOMImplementationSourceImpl");
            DOMImplementationRegistry registry = DOMImplementationRegistry
                    .newInstance();
            DOMImplementationLS domImplementationLS = (DOMImplementationLS) registry
                    .getDOMImplementation("LS");
            System.err.println(domImplementationLS.createLSSerializer()
                    .writeToString(n));
        } catch (ClassNotFoundException e) {
            System.err.println("Unable to find DOM Parser: " + e.getMessage());
            System.exit(1);
        } catch (InstantiationException e) {
            System.err.println("Unable to find DOM Parser: " + e.getMessage());
            System.exit(1);
        } catch (IllegalAccessException e) {
            System.err.println("Unable to find DOM Parser: " + e.getMessage());
            System.exit(1);
        }
    }
}

/**
 * Simple class for grouping the message and its response together before enqueuing them in the XMLHandlers queue.
 * 
 * @version 1, $Date: 2006/09/11 09:33:42 $
 * @author Per Alexius
 */
class QueueEntry {
    public ApproxsimMessage mMessage;
    public String mResponseMessageString;

    /**
     * Constructor
     * 
     * @param msg The approxsim message.
     * @param reMsgStr The response message xml string.
     */
    public QueueEntry(ApproxsimMessage msg, String reMsgStr) {
        mMessage = msg;
        mResponseMessageString = reMsgStr;
    }

    /**
     * Gets the approxsim message
     * 
     * @return The approxsim message.
     */
    public ApproxsimMessage getMessage() {
        return mMessage;
    }

    /**
     * Gets the response message xml string.
     * 
     * @return The response message xml string.
     */
    public String getResponseString() {
        return mResponseMessageString;
    }
}

/**
 * Simple exception used to signify errors between the different handle-levels
 */
class HandleException extends Exception {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1234433267375406352L;

    /**
     * Creates a new HandleException
     */
    public HandleException() {}
}
