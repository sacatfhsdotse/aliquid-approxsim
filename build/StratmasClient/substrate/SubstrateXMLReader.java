package StratmasClient.substrate;

import java.util.Vector;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSException;
import org.w3c.dom.ls.LSResourceResolver;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

import StratmasClient.ProcessVariableDescription;
import StratmasClient.StratmasDialog;
import StratmasClient.LSJarXSDResolver;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.Shape;
import StratmasClient.object.Circle;
import StratmasClient.object.Polygon;
import StratmasClient.object.StratmasObjectFactory;
import StratmasClient.object.primitive.Reference;
import StratmasClient.object.type.Type;
import StratmasClient.object.type.TypeFactory;
import StratmasClient.communication.XMLHandler;

/**
 * This class is used to read XML files needed for SubstrateEditor.
 */
public class SubstrateXMLReader {
    /**
     * XML Schema namespace.
     */
    private static String XML_SCHEMA_NS = "http://www.w3.org/2001/XMLSchema-instance";
    /**
     * Protocol location for SubstrateEditor.
     */
    private static String SUBSTRATE_PROTOCOL = "substrateXML_IO.xsd";
    /**
     * The DomImplementationLS to use.
     */
    private static DOMImplementationLS domImplementationLS = createDomImplementationLS();
    /**
     * Reference to the editor.
     */
    private SubstrateEditor substrateEditor;
    
    /**
     * Creates an XML reader.
     */
    public SubstrateXMLReader(SubstrateEditor substrateEditor) {
	this.substrateEditor = substrateEditor;
    }
    
    /**
     * Creates a parser for SubstrateEditor.
     */
    public static LSParser createParser(){
	LSParser parser = domImplementationLS.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, "http://www.w3.org/2001/XMLSchema");
	parser.getDomConfig().setParameter("error-handler", new DOMErrorHandler() {
		/** This method is called on the error handler when an error occurs. */
		public boolean handleError(DOMError error) {
		    throw new LSException(LSException.PARSE_ERR, error.getMessage());
		}
	    });
	parser.getDomConfig().setParameter("schema-location", SUBSTRATE_PROTOCOL);
	parser.getDomConfig().setParameter("validate", Boolean.TRUE);
	parser.getDomConfig().setParameter("namespaces", Boolean.TRUE);
	
	LSResourceResolver prevResolver = (LSResourceResolver) parser.getDomConfig().getParameter("resource-resolver");
	parser.getDomConfig().setParameter("resource-resolver", new LSJarXSDResolver(prevResolver));
	
	return parser;
    }
    
    /**
     * Creates a document from an xml-file.
     *
     * @param filename name of an xml-file.
     */
    public static Document createDocument(String filename) {
	try {
	    LSInput parserInput = domImplementationLS.createLSInput();
	    parserInput.setByteStream(new BufferedInputStream(new FileInputStream(filename)));
	    return createParser().parse(parserInput);
	} catch (FileNotFoundException e) {
	    StratmasDialog.showErrorMessageDialog(null, "File '" + filename + "' not found.", "File Not Found"); 
	}
        catch (IOException e) {
	    StratmasDialog.showErrorMessageDialog(null, "Error while reading the input file", "IO Error"); 
	} 
	catch (LSException e) {
	    StratmasDialog.showErrorMessageDialog(null, "Error while parsing the input file", "Parsing Error");    
	}
	return null;
    }

    /**
     * Returns the DomImplementationLS to use.
     */
    private static DOMImplementationLS createDomImplementationLS() {
	if (domImplementationLS != null) {
	    return domImplementationLS;
	} 
	else {
	    try {
		System.setProperty(DOMImplementationRegistry.PROPERTY, "org.apache.xerces.dom.DOMImplementationSourceImpl");
		DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
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
     * Reads a file with process variables.
     */
    public static Vector getProcessVariables(String filename) {
	Vector pvs = new Vector();
	try {
	    Document doc = createDocument(filename);
	    NodeList list = doc.getDocumentElement().getElementsByTagName("pv");
	    for (int i = 0; i < list.getLength(); i++) {
		Node node = list.item(i);
		if (node.getNodeType() == Node.ELEMENT_NODE) {
		    pvs.add(getProcessVariable((Element)node));
		}
	    }
	}
	catch (RuntimeException e) {
	    System.err.println("Unable to read process variables! "); 
	} 
	return pvs;
    }
    
    /**
     * Reads a file with process variables, factions and the assigned shape values.		    
     */
    public Vector getShapeValuesForProcessVariables(String filename) {
	Vector pvs = new Vector();
	try {
	    Document doc = createDocument(filename);
	    NodeList list = doc.getDocumentElement().getElementsByTagName("pviv");
	    for (int i = 0; i < list.getLength(); i++) {
		Node node = list.item(i);
		if (node.getNodeType() == Node.ELEMENT_NODE) {
		    pvs.add(getProcessVariableInitialValues((Element)node));
		}
	    }
	}
	catch (NullPointerException e) {
	    System.err.println("Unable to read shape values! "); 
	}
	return pvs;
    }
    
    /**
     * Returns a process variable, a faction and an ordered list of shape values from an Element.
     */
    public ProcessVariableInitialValues getProcessVariableInitialValues(Element element) {
	// get process variable
	ProcessVariableDescription processVariable = getPV(element);
	if (processVariable == null) { 
	    processVariable = getProcessVariable(XMLHandler.getFirstChildByTag(element, "pv"));
	    substrateEditor.getProcessVariableHandler().importProcessVariable(processVariable);
	}
	// get faction
	Element elem = XMLHandler.getFirstChildByTag(element, "faction");
	StratmasObject faction = (elem == null)? null : getFaction(elem);
	// get shapes 
	Vector orderedShapeList = getOrderedShapeList(XMLHandler.getChildElementsByTag(element, "regions"));

	return new ProcessVariableInitialValues(processVariable, faction, orderedShapeList);
    }
    
    /**
     * Returns a process variable from an Element. 
     */
    public static ProcessVariableDescription getProcessVariable(Element element) {
	Element range = XMLHandler.getFirstChildByTag(element, "range");
	return new ProcessVariableDescription(XMLHandler.getString(element, "name"),
					      XMLHandler.getString(element, "category"),
					      XMLHandler.getBoolean(element, "factions"),
					      XMLHandler.getDouble(range, "min"),
					      XMLHandler.getDouble(range, "max"));
    }
    
    /**
     * Returns a process variable from the substrate editor.
     */
    private ProcessVariableDescription getPV(Element element) {
	Element ele = XMLHandler.getFirstChildByTag(element, "pv");
	String pvName = XMLHandler.getString(ele, "name");
	ProcessVariableDescription pvd = substrateEditor.getProcessVariableHandler().get(pvName);
	return pvd;
    }
    
    /**
     * Returns a faction from an Element. 
     */
    public StratmasObject getFaction(Element element) {
	String identifier = element.getAttribute("identifier");
	StratmasObject faction = substrateEditor.getFactionHandler().getFaction(identifier);
	if (faction != null) {
	    return faction;
	} 
	else {
	    StratmasObject newFaction = StratmasObjectFactory.create(TypeFactory.getType(element));
	    newFaction.setIdentifier(identifier);
	    return newFaction;
	}
    }
    
    /**
     * Returns an ordered list of ShapeValuePair objects. 
     */
    public Vector getOrderedShapeList(Vector elements) {
	Vector shapeList = new Vector();
	for (int i = 0; i < elements.size(); i++) {
	    shapeList.add(getShapeValuePair((Element)elements.get(i)));
	}
	return shapeList;
    }
    
    /** 
     * Returns a shape and the assigned value.
     */
    public ShapeValuePair getShapeValuePair(Element element) {
	String type = getType(element);
	// get value
	double value = XMLHandler.getDouble(element, "value");
	// get shape reference
	if (type.equals("ESRIRegion")) {
	    Element elem = XMLHandler.getFirstChildByTag(element, "reference");
	    Reference shapeRef = Reference.getReference(elem);
	    Shape shape = substrateEditor.getESRIShape(shapeRef);
	    if (shape != null) {
		return new ShapeValuePair(shape, value, true);
	    }
	}
	// get shape
	else if (type.equals("CreatedRegion")) {
	    Shape shape = getShape(element);
	    if (shape != null) {
		return new ShapeValuePair(shape, value, false);
	    }
	}
	return null;
    }
    
    /**
     * Returns a shape.
     */
    public Shape getShape(Element element) {
	Element elem = XMLHandler.getFirstChildByTag(element, "shape");
	String type = getType(elem);
	if (type.equals("Circle")) {
	    return (Circle)StratmasObjectFactory.domCreate(elem);
	}
	else if (type.equals("Polygon")) {
	    return (Polygon)StratmasObjectFactory.domCreate(elem);
	}
	return null;
    }
    
    /**
     * Returns the type of the element.
     */
    private String getType(Element element) {
	return element.getAttribute("xsi:type").split(":")[1];
    }
   
}
