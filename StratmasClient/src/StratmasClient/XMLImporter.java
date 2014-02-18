package StratmasClient;

import StratmasClient.communication.XMLHandler;
import StratmasClient.object.Point;
import StratmasClient.object.StratmasList;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasObjectFactory;
import StratmasClient.object.StratmasReference;
import StratmasClient.object.StratmasSimple;
import StratmasClient.object.primitive.Reference;
import StratmasClient.object.type.Declaration;
import StratmasClient.object.type.Type;
import StratmasClient.object.type.TypeFactory;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.EntityResolver2;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * This class handles the creation of StratmasObejcts from xml.
 *
 * (Acknowledgement: Some comments in this file copied and or adapted
 * from the Xerces library.)
 *
 * @version 1, $Date: 2006/09/18 09:45:25 $
 * @author  Per Alexius
 */
public class XMLImporter {
     private static XMLReader parser = null;

     public static void main(String[] args) {
          if (args.length != 1) {
               System.out.println("Usage:  java XMLImporter <instance file>");
               System.exit(0);
          }

          try {
               StratmasObject so;
               so = saxParseFromFile(args[0]);
               so = saxParseFromFile(args[0]);
               System.err.println("Created " + so.getIdentifier() + ", " + so.getType().getName());
            } catch (IOException e) {
                 System.err.println(e.getMessage());
            } catch (ExceptionCollection e) {
                  System.err.println(e.getMessage());
                for (Enumeration en = e.getExceptions().elements(); en.hasMoreElements(); ) {
                     System.err.println("==============================");
                     System.err.println(((Exception)en.nextElement()).getMessage());
                }
          }
     }

     public static StratmasObject saxParseFromFile(String instanceFile) throws IOException, ExceptionCollection {
          InputSource source = new InputSource(new BufferedInputStream(new FileInputStream(instanceFile)));
          source.setSystemId(instanceFile);
          return saxParse(source);
     }

     public static StratmasObject saxParseFromString(String instanceDoc) throws IOException, ExceptionCollection {
          return saxParse(new InputSource(new StringReader(instanceDoc)));
     }

     public static StratmasObject saxParse(InputSource source) throws IOException, ExceptionCollection {
          StratmasObject ret = null;
          SAXDocumentHandler handler = null;

          try {
               long start = System.currentTimeMillis();

               if (parser == null) {
                    createParser();
               }
               
                parser.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation",
                                   StratmasConstants.stratmasNamespace + " " +
                                   StratmasConstants.STRATMAS_SIMULATION_SCHEMA);

               handler = new SAXDocumentHandler();
               parser.setContentHandler(handler);
               parser.setEntityResolver(handler);
               parser.setErrorHandler(handler);

               parser.parse(source);

               long duration = System.currentTimeMillis() - start;
               Debug.err.println("SAX took " + duration + " ms");

               if (!handler.getExceptions().isEmpty()) {
                    throw new ExceptionCollection(handler.getExceptions());
               }

               ret = handler.getCreatedObject();               
          } catch (SAXException e) {
               Vector v;
               if (handler == null) {
                    v = new Vector();
               }
               else {
                    v = handler.getExceptions();
                    v.add(e);
               }
               throw new ExceptionCollection(v);
           } finally {
              // Remove parsers reference to handler (because handler
              // have references to stratmasObject's it creates.)
              if (handler != null) {
                  handler.nullify();
                  if (parser != null) {
                      // This don't accomplish much, but we try
                      // anyway.
                      parser.setContentHandler(null);
                      parser.setEntityResolver(null);
                      parser.setErrorHandler(null);
                  }
                  handler = null;
              }
          }
          return ret;
     }

     /**
      * Creates a parser.
      */
     private static void createParser() throws SAXException {
          parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
          parser.setFeature("http://xml.org/sax/features/validation", true);
          parser.setFeature("http://xml.org/sax/features/namespaces", true);
          parser.setFeature("http://apache.org/xml/features/validation/schema", true);
          parser.setFeature("http://apache.org/xml/features/xinclude", true); 
          //parser.setProperty("http://apache.org/xml/properties/validation/schema/root-type-definition", "foo"); 
     }

}

class ExceptionCollection extends Exception {
     private Vector exceptions;
     public ExceptionCollection(Vector v) {
          super("Collection of " + v.size() + " exceptions.");
          exceptions = v;
     }
     public Vector getExceptions() {
          return exceptions;
     }
}


/**
 * A place holder object for complex StratmasObjects used when
 * creating StratmasObjects from an xml document.
 *
 * @version 1, $Date: 2006/09/18 09:45:25 $
 * @author  Per Alexius
 */
class SOPlaceHolder {
     /**
      * The idenfifier of the object this placeholder refers to.
      */
     private String identifier;

     /**
      * The type of the object this placeholder refers to.
      */
     private Type type;

     /**
      * The parent of this place holder or null if there is no parent.
      */
     private SOPlaceHolder parent = null;

     /**
      * A Hashtable mapping a child's name to its StratmasObject.
      */
     private Hashtable objects = new Hashtable();

     /**
      * Creates a placeholder for an object of the specified type.
      *
      * @param identifier The idenfifier of the object this
      * placeholder refers to.
      * @param type The type of the object this placeholder refers to.
      * @param parent The parent of this place holder.
      */
     public SOPlaceHolder(String identifier, Type type, SOPlaceHolder parent) {
          this.identifier = identifier;
          this.type = type;
          this.parent = parent;

          // Create StratmasLists for all lists in our Type.
          for (Enumeration en = type.getSubElements().elements(); en.hasMoreElements(); ) {
               Declaration dec = (Declaration)en.nextElement();
               if (dec.isList()) {
                    objects.put(dec.getName(), StratmasObjectFactory.vectorCreateList(dec).getStratmasObject(new Vector()));
               }
          }
     }

     /**
      * Accessor for the identifier.
      *
      * @return The idenfifier of the object this placeholder refers to.
      */
     public String getIdentifier() {
          return identifier;
     }

     /**
      * Accessor for the parent.
      *
      * @return The parent of this placeholder.
      */
     public SOPlaceHolder getParent() {
          return parent;
     }

     /**
      * Accessor for the type.
      *
      * @return The type of the object this placeholder refers to.
      */
     public Type getType() {
          return type;
     }

     /**
      * Adds a created StratmasObject to this placeholder. Notice that
      * in order to be able to put list elements in the correct lists
      * the identifier of the provided StratmasObject must be the tag
      * of the dom element it was created from and newIdentifier must
      * contain the value of the identifier attribute.
      *
      * @param o The StratmasObject to add.
      * @param newIdentifier The value of the identifier attribute in
      * the dom element the StratmasObject was created from.
      */
     public void addObject(StratmasObject o, String newIdentifier) {
          Declaration dec = getType().getSubElement(o.getIdentifier());
          if (dec.isList()) {
               StratmasList list = (StratmasList)objects.get(o.getIdentifier());
               o.setIdentifier(newIdentifier);
               list.add(o);
          }
          else {
               objects.put(o.getIdentifier(), o);
          }
     }

     /**
      * Creates the StratmasObject that this object is placeholder for.
      *
      * @param tag The tag of the dom element this Object was is
      * created from.
      * @return The newly created StratmasObject.
      */
     public StratmasObject createStratmasObject(String tag) throws IncompleteVectorConstructException {
          Vector parts = new Vector();
          for (Enumeration en = type.getSubElements().elements(); en.hasMoreElements(); ) {
               StratmasObject o = (StratmasObject)objects.get(((Declaration)en.nextElement()).getName());
               if (o != null) {
                    parts.add(o);
               }
          }

          Declaration dec;
          if(getParent() == null) {
               dec = new Declaration(getType(), tag, 0, -1, true);
          }
          else {
               dec = getParent().getType().getSubElement(tag).clone(getType());
          }
          StratmasObject so = StratmasObjectFactory.vectorCreate(dec).getStratmasObject(parts);
          if (so == null) {
               throw new IncompleteVectorConstructException("Error when creating object of type " + 
                                                            dec.getType().getName() + " with tag '" + tag + "'.");
          }
          else {
               return so;
          }
     }
}


/**
 * The DocumentHandler that when attached to an XMLReader creates
 * StratmasObjects from the parsed xml. It also handles import from
 * ESRI .shp files.
 *
 * @version 1, $Date: 2006/09/18 09:45:25 $
 * @author  Per Alexius
 */
class SAXDocumentHandler extends DefaultHandler implements EntityResolver2 {
     /**
      * Non-fatal exceptions that occurs during parsing are collected
      * in this vector.
      */
     Vector exceptions = new Vector();

     /**
      * Flag marking whether the current element is imported from an
      * external entity or not.
      */
     private boolean imported = false;

     /**
      * This flag indicates if we should care about characters in the
      * characters() method.
      */
     private boolean readChars = false;

     /**
      * The number of characters currently in the chars array. 
      */
     private int currentLength = 0;

     /**
      * The current max length of the chars array.
      */
     private int allocatedLength = 1024;

     /**
      * The array in which characters are stored between consecutive
      * calls to the characters method that belongs to the same simple
      * type.
      */
     private char [] chars;

     /**
      * The Vector in which identifiers are stored while parsing a
      * Reference.
      */
     private Vector refVec = new Vector();

     /**
      * When parsing a Reference refDepth keeps track of the current
      * depth i.e. the recursion depth. Necessary since a Reference
      * may contain other complete Reference objects and it is only
      * the top Reference that should be created as a StratmasObject.
      */
     private int refDepth = 0;

     /**
      * The placeholder for the StratmasComplex currently beeing
      * constructed.
      */
     private SOPlaceHolder currentPlaceHolder = null;

     /**
      * A stack on which we push the type of the element currently
      * beeing created.
      */
     private Stack typeStack = new Stack();

     /**
      * The identifer of the StratmasObject currently beeing created.
      */
     private String currentIdentifier = null;

     /**
      * The StratmasSimple currently beeing created.
      */
     private StratmasObject currentObject = null;

     /**
      * The StratmasObject created from the parsed document.
      */
     private StratmasObject createdObject = null;

     /**
      * The locator, if the parser sets any.
      */
     private Locator locator = null;

     /**
      * Default constructor.
      */
     public SAXDocumentHandler() {
          chars = new char[allocatedLength];
     }

     /**
      * Accessor for the StratmasObject created from the parsed
      * document.
      *
      * @return The StratmasObject created from the parsed document.
      */
     public StratmasObject getCreatedObject() {
          return createdObject;
     }

     public Vector getExceptions() {
          return exceptions;
     }

     /**
      * Receive an object for locating the origin of SAX document
      * events.
      *
      * @param locator The locator.
      */
     public void setDocumentLocator(Locator locator) {
          this.locator = locator;
     }

     /**
      * Receive notification of character data.
      *
      * <p>The Parser will call this method to report each chunk of
      * character data.  SAX parsers may return all contiguous character
      * data in a single chunk, or they may split it into several
      * chunks; however, all of the characters in any single event
      * must come from the same external entity so that the Locator
      * provides useful information.</p>
      *
      * <p>The application must not attempt to read from the array
      * outside of the specified range.</p>
      *
      * <p>Individual characters may consist of more than one Java
      * <code>char</code> value.  There are two important cases where this
      * happens, because characters can't be represented in just sixteen bits.
      * In one case, characters are represented in a <em>Surrogate Pair</em>,
      * using two special Unicode values. Such characters are in the so-called
      * "Astral Planes", with a code point above U+FFFF.  A second case involves
      * composite characters, such as a base character combining with one or
      * more accent characters. </p>
      *
      * <p> Your code should not assume that algorithms using
      * <code>char</code>-at-a-time idioms will be working in character
      * units; in some cases they will split characters.  This is relevant
      * wherever XML permits arbitrary characters, such as attribute values,
      * processing instruction data, and comments as well as in data reported
      * from this method.  It's also generally relevant whenever Java code
      * manipulates internationalized text; the issue isn't unique to XML.</p>
      *
      * <p>Note that some parsers will report whitespace in element
      * content using the {@link #ignorableWhitespace ignorableWhitespace}
      * method rather than this one (validating parsers <em>must</em> 
      * do so).</p>
      *
      * @param ch the characters from the XML document
      * @param start the start position in the array
      * @param length the number of characters to read from the array
      * @throws org.xml.sax.SAXException any SAX exception, possibly
      *            wrapping another exception
      * @see #ignorableWhitespace 
      * @see org.xml.sax.Locator
      */
     public void characters(char[] ch, int start, int length) throws SAXException {
          if (readChars == true) {
               // Check if the allocated char array is large enough.
               if (currentLength + length > allocatedLength) {
                    allocatedLength = currentLength + length;
                    char [] tmp = new char[allocatedLength];
                    System.arraycopy(chars, 0, tmp, 0, currentLength);
                    chars = tmp;
                    Debug.err.println("SAX Document handler reallocating char array.");
               }
               // Sorry, we have to copy chars to our own array since
               // there seems to be no guarantee how the 'ch' char
               // array will look like the next time.
               System.arraycopy(ch, start, chars, currentLength, length);
               currentLength += length;
          }
     }

     /**
      * Receive notification of the beginning of an element.
      *
      * <p>The Parser will invoke this method at the beginning of every
      * element in the XML document; there will be a corresponding
      * {@link #endElement endElement} event for every startElement event
      * (even when the element is empty). All of the element's content will be
      * reported, in order, before the corresponding endElement
      * event.</p>
      *
      * <p>This event allows up to three name components for each
      * element:</p>
      *
      * <ol>
      * <li>the Namespace URI;</li>
      * <li>the local name; and</li>
      * <li>the qualified (prefixed) name.</li>
      * </ol>
      *
      * <p>Any or all of these may be provided, depending on the
      * values of the <var>http://xml.org/sax/features/namespaces</var>
      * and the <var>http://xml.org/sax/features/namespace-prefixes</var>
      * properties:</p>
      *
      * <ul>
      * <li>the Namespace URI and local name are required when 
      * the namespaces property is <var>true</var> (the default), and are
      * optional when the namespaces property is <var>false</var> (if one is
      * specified, both must be);</li>
      * <li>the qualified name is required when the namespace-prefixes property
      * is <var>true</var>, and is optional when the namespace-prefixes property
      * is <var>false</var> (the default).</li>
      * </ul>
      *
      * <p>Note that the attribute list provided will contain only
      * attributes with explicit values (specified or defaulted):
      * #IMPLIED attributes will be omitted.  The attribute list
      * will contain attributes used for Namespace declarations
      * (xmlns* attributes) only if the
      * <code>http://xml.org/sax/features/namespace-prefixes</code>
      * property is true (it is false by default, and support for a 
      * true value is optional).</p>
      *
      * <p>Like {@link #characters characters()}, attribute values may have
      * characters that need more than one <code>char</code> value.  </p>
      *
      * @param uri the Namespace URI, or the empty string if the
      *        element has no Namespace URI or if Namespace
      *        processing is not being performed
      * @param localName the local name (without prefix), or the
      *        empty string if Namespace processing is not being
      *        performed
      * @param qName the qualified name (with prefix), or the
      *        empty string if qualified names are not available
      * @param atts the attributes attached to the element.  If
      *        there are no attributes, it shall be an empty
      *        Attributes object.  The value of this object after
      *        startElement returns is undefined
      * @throws org.xml.sax.SAXException any SAX exception, possibly
      *            wrapping another exception
      * @see #endElement
      * @see org.xml.sax.Attributes
      * @see org.xml.sax.helpers.AttributesImpl
      */
     public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
          if (!exceptions.isEmpty()) {
               return;
          }
          if (imported == true) {
               imported = false;
          }

          Type type = null;

          // First check the xsi:type attribute.
          String typeStr = attributes.getValue("xsi:type");
          if (typeStr != null) {
               type = TypeFactory.getType(typeStr.split(":")[1]);
          }

          // If no xsi:type then get type from tag and parent's type.
          if (type == null && currentPlaceHolder != null) {
               Declaration dec = ((Type)typeStack.peek()).getSubElement(localName);
               if (dec != null) {
                    type = dec.getType();
               }
          }

          if (type == null) {
               throw new AssertionError("Can't create Type for element with tag '" + localName + "'");
          }
          else if (!type.getNamespace().equals(StratmasConstants.xsdNamespace) &&
                   !type.canSubstitute("Identifier", StratmasConstants.stratmasNamespace)) {
               // Shouldn't care about currentIdentifier if we're
               // currently parsing a Reference.
               if (refDepth == 0) {
                    currentIdentifier = attributes.getValue("identifier");
                    if (currentIdentifier == null || currentIdentifier.equals("")) {
                         currentIdentifier = localName;
                    }
               }
               
               if (type.canSubstitute("Reference", StratmasConstants.stratmasNamespace)) {
                    // Just increase depth. StratmasReferences are
                    // created in endElement in order to avoid a
                    // refDepth check here.
                    refDepth++;
               }
               else if (type.getName().equals("Point") ||
                        (type.canSubstitute("SimpleType", StratmasConstants.stratmasNamespace) &&
                         !type.getName().equals("SymbolIDCode"))) {
                    // Points and simple types except for Reference
                    // and SymbolIDCode may be created here.
                   Declaration decToCreateFrom = ((Type)typeStack.peek()).getSubElement(localName).clone(type);
                   currentObject = StratmasObjectFactory.defaultCreate(decToCreateFrom);
               }
               else if (type.canSubstitute("ComplexType", StratmasConstants.stratmasNamespace) ||
                        type.canSubstitute("Shape", StratmasConstants.stratmasNamespace) || 
                        type.getName().equals("SymbolIDCode") ||
                        type.getName().equals("Root")) {
                    // Create a place holder for complex types and SymbolIDCodes.
                    currentPlaceHolder = new SOPlaceHolder(currentIdentifier, type, currentPlaceHolder);
               }
               // Keep track of the Type of object we're currently creating.
               typeStack.push(type);
          }
          // Should be only anySimpleType descendants here...
          else {
               // Simple type descendant means we must read characters.
               readChars = true;
          }
     }

     /**
      * Receive notification of the end of an element.
      *
      * <p>The SAX parser will invoke this method at the end of every
      * element in the XML document; there will be a corresponding
      * {@link #startElement startElement} event for every endElement 
      * event (even when the element is empty).</p>
      *
      * <p>For information on the names, see startElement.</p>
      *
      * @param uri the Namespace URI, or the empty string if the
      *        element has no Namespace URI or if Namespace
      *        processing is not being performed
      * @param localName the local name (without prefix), or the
      *        empty string if Namespace processing is not being
      *        performed
      * @param qName the qualified XML name (with prefix), or the
      *        empty string if qualified names are not available
      * @throws org.xml.sax.SAXException any SAX exception, possibly
      *            wrapping another exception
      */
     public void endElement(String uri, String localName, String qName) throws SAXException {
          if (!exceptions.isEmpty()) {
               return;
          }
          if (readChars == true) {
               // Here the element we're closing is a anySimpleType descendant.
               Type type = (Type)typeStack.peek();
               if (type.getName().equals("Point")) {
                    // Have to give Point special treatment.
                    if (localName.equals("lat")) {
                         ((Point)currentObject).setLat(Double.parseDouble(new String(chars, 0, currentLength)), null);
                    }
                    else if (localName.equals("lon")) {
                         ((Point)currentObject).setLon(Double.parseDouble(new String(chars, 0, currentLength)), null);
                    }
               }
               else if (type.canSubstitute("Reference", StratmasConstants.stratmasNamespace)) {
                    // Collect the newly parsed Identifier in the reference Vector.
                    refVec.add(new String(chars, 0, currentLength));
               }       
               else if (type.canSubstitute("SimpleType", StratmasConstants.stratmasNamespace) &&
                        !type.getName().equals("SymbolIDCode")) {
                    // Set value of simple type.
                   try {
                       ((StratmasSimple)currentObject).valueFromString(new String(chars, 0, currentLength), null);
                   } catch (ParseException e) {
                       throw new SAXException(e);
                   }
               }
               
               readChars = false;
               currentLength = 0;
          }
          else {
               Type type = (Type)typeStack.pop();
               if (type.canSubstitute("Reference", StratmasConstants.stratmasNamespace)) {
                    refDepth--;
                    if (refDepth == 0) {
                         // Here we're closing a top Reference element
                         // so we must create a StratmasReference from
                         // the identifiers in the reference Vector.
                         String [] ids = new String[refVec.size()];
                         ids = (String[])refVec.toArray(ids);
                         Declaration dec = ((Type)typeStack.peek()).getSubElement(localName);
                         StratmasReference so = (StratmasReference)StratmasObjectFactory.defaultCreate(dec);
                         so.setValue(new Reference(ids), null);
                         refVec.removeAllElements();
                         currentPlaceHolder.addObject(so, currentIdentifier);                         
                         currentIdentifier = currentPlaceHolder.getIdentifier();
                    }
               }
               else if (type.getName().equals("Point") ||
                        (type.canSubstitute("SimpleType", StratmasConstants.stratmasNamespace) && 
                         !type.getName().equals("SymbolIDCode"))) {
                    // Points and SimpleTypes except for References
                    // and SymbolIDCodes are already created so simply
                    // add them to their parent-to-be placeholder.
                    currentPlaceHolder.addObject(currentObject, currentIdentifier);
                    currentIdentifier = currentPlaceHolder.getIdentifier();
               }
               else if (currentPlaceHolder.getParent() == null) {
                    // Now we're going to create the last object.
                    StratmasObject so = null;
                    try {
                         so = currentPlaceHolder.createStratmasObject(localName);
                    } catch (IncompleteVectorConstructException e) {
                         String msg = "";
                         if (locator != null) {
                              msg = "Line: " + locator.getLineNumber() + ", column: " + locator.getColumnNumber() + ": ";
                         }
                         throw new SAXException(msg + e.getMessage());
                    }
                    so.setIdentifier(currentIdentifier);

                    if (!type.getName().equals("Root")) {
                        // The document has a root element that isn't
                        // of type Root so we must create the Root
                        // type object explicitly.
                        StratmasObject docRoot = so;
                        so = StratmasObjectFactory.create(TypeFactory.getType("Root"));
                        so.getChild("identifiables").add(docRoot);
                    }

                    createdObject = so;
               }
               else if (!type.canSubstitute("anySimpleType", StratmasConstants.xsdNamespace)) {
                    // Here we're closing a complex element so let's
                    // create a StratmasComplex from the current
                    // placeholder.
                    StratmasObject so = null;
                    try {
                         so = currentPlaceHolder.createStratmasObject(localName);
                    } catch (IncompleteVectorConstructException e) {
                         String msg = "";
                         if (locator != null) {
                              msg = "Line: " + locator.getLineNumber() + ", column: " + locator.getColumnNumber() + ": ";
                         }
                         throw new SAXException(msg + e.getMessage());
                    }
                    currentPlaceHolder = currentPlaceHolder.getParent();
                    currentPlaceHolder.addObject(so, currentIdentifier);
                    currentIdentifier = currentPlaceHolder.getIdentifier();
               }
          }
     }

     /**
      * Default implementation to fulfil interface contract.
      *
      * @param name Identifies the document root element.  This name
      *        comes from a DOCTYPE declaration (where available) or from the
      *        actual root element.
      * @param baseURI The document's base URI, serving as an
      *        additional hint for selecting the external subset.  This is
      *        always an absolute URI, unless it is null because the
      *        XMLReader was given an InputSource without one.
      *
      * @return An InputSource object describing the new external
      *        subset to be used by the parser, or null to indicate that no
      *        external subset is provided.
      */
     public InputSource getExternalSubset(String name, String baseURI) throws SAXException, IOException {
          return null;
     }

     /**
      * Resolves references to ESRI .shp files and schemas possibly
      * located in a jar-file.
      *
      * If the referenced entity is a file ending with '.shp' a stream
      * to the ESRI file that produces XML from the binary data is
      * opened and returned to the application.
      *
      * For both .shp files and .xml files the import flag is set to
      * true in order to allow the application to collect information
      * about which elements that were imported from external sources.
      *
      * @param name Identifies the external entity being resolved.
      *        Either "[dtd]" for the external subset, or a name starting
      *        with "%" to indicate a parameter entity, or else the name of a
      *        general entity.  This is never null when invoked by a SAX2
      *        parser.
      * @param publicId The public identifier of the external entity
      *        being referenced (normalized as required by the XML
      *        specification), or null if none was supplied.
      * @param baseURI The URI with respect to which relative
      *        systemIDs are interpreted.  This is always an absolute URI,
      *        unless it is null (likely because the XMLReader was given an
      *        InputSource without one).  This URI is defined by the XML
      *        specification to be the one associated with the "&lt;"
      *        starting the relevant declaration.
      * @param systemId The system identifier of the external entity
      *        being referenced; either a relative or absolute URI.  This is
      *        never null when invoked by a SAX2 parser; only declared
      *        entities, and any external subset, are resolved by such
      *        parsers.
      *
      * @return An InputSource object describing the new input source
      *        to be used by the parser.  Returning null directs the parser
      *        to resolve the system ID against the base URI and open a
      *        connection to resulting URI.
      *
      * @exception SAXException Any SAX exception, possibly wrapping
      *        another exception.
      * @exception IOException Probably indicating a failure to create
      *        a new InputStream or Reader, or an illegal URL.
      */
     public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId)
          throws SAXException, IOException {
          InputSource ret = null;

          InputStream stream = XMLImporter.class.getResourceAsStream(StratmasConstants.JAR_SCHEMA_LOCATION + systemId);
          if (stream != null) {
               ret = new InputSource(stream);
               ret.setPublicId(publicId);
               ret.setSystemId(systemId);
               return ret;
          }

          if (systemId != null) {
               if (systemId.toLowerCase().matches(".*\\.shp(\\?.*)?")) {
                    imported = true;
                    try {
                         URL url = findFile(baseURI, systemId);
                         if (url != null) {
                              ret = new InputSource(new ESRIInputStream(url, null));
                              ret.setPublicId(publicId);
                              ret.setSystemId(systemId);
                              ret.setEncoding("ISO-8859-1");
                         }
                    } catch (IOException e) {
                         ret = null;
                    }
               }
               else if (systemId.matches(".*\\.xml(\\?.*)?")) {
                    imported = true;
               }
          }
          return ret;
     }

     /**
      * Tries to locate a file from the given systemId and
      * baseUri. First checks if the systemId is an absolute path and
      * second if it is relative to the folder containing the file
      * pointed to by baseURI.
      *
      * @param baseURI The base URI.
      * @param systemId The system id.
      * @return An URL to the file or null if no file was found.
      */
     private URL findFile(String baseURI, String systemId) {          
          final String enc = "UTF-8"; // Encoding to decode to.
          URL ret = null;
          try {
               URL url = new URL(systemId);
               File file = new File(URLDecoder.decode(url.getPath(), enc));
               // Check for absolute path.
               if (file.isAbsolute() && file.exists()) {
                    ret = url;
               }
               else if (!file.isAbsolute()) {
                    // Try relative the same directory as the
                    // 'importing' file lives in.
                    URL baseURL = new URL(baseURI);
                    File baseFile = new File(URLDecoder.decode(baseURL.getPath(), enc)).getParentFile();
                    String absPath = baseFile.getPath() + System.getProperty("file.separator") +
                         URLDecoder.decode(url.getPath(), enc);
                    file = new File(absPath);
                    if (file.exists()) {
                         String newURL = file.toURI().toURL().toString();
                         if (url.getQuery() != null) {
                             newURL += "?" + url.getQuery();
                         }
                         ret = new URL(newURL);
                    }
                    else {
                         // Perhaps try relative cwd here...
                    }
               }
          } catch (MalformedURLException e) {
          } catch (UnsupportedEncodingException e) {
           }
          return ret;
     }

     /**
      * Handles warnings from the parser. Stores the given exception
      * and lets the parser continue unless the total number of
      * exceptions is to large.
      *
      * @param exception The exception found by the parser.
      * @throws SAXException Throws the last exception when to many
      * exceptions has occurred.
      */
     public void warning(SAXParseException exception) throws SAXException {
          SAXParseException e = addLineAndColumnInfoToMessage(exception);
          System.err.println("Warning: " + e);
          if (exceptions.size() > 98) {
               throw e;
          }
          else {
               exceptions.add(e);
          }
     }

     /**
      * Handles errors from the parser. Stores the given exception and
      * lets the parser continue unless the total number of exceptions
      * is to large.
      *
      * @param exception The exception found by the parser.
      * @throws SAXException Throws the last exception when to many
      * exceptions has occurred.
      */
     public void error(SAXParseException exception) throws SAXException {
          SAXParseException e = addLineAndColumnInfoToMessage(exception);
          System.err.println("Error: " + e);
          if (exceptions.size() > 98) {
               throw e;
          }
          else {
               exceptions.add(e);
          }
     }

     /**
      * Handles fatal errors from the parser. Rethrows the exception.
      *
      * @param exception The exception found by the parser.
      * @throws SAXException Throws the exception found by the parser.
      */
     public void fatalError(SAXParseException exception) throws SAXException {
          SAXParseException e = addLineAndColumnInfoToMessage(exception);
          System.err.println("Fatal: " + e);
          throw e;
     }

     /**
      * Adds line and column number information to the beginning of
      * the message of the given exception.
      *
      * @param e The exception to add line and column number
      * information to.
      * @return A new exception with line and column number
      * information added to the message.
      */
     private SAXParseException addLineAndColumnInfoToMessage(SAXParseException e) {
          String msg = "Line: " + e.getLineNumber() + ", column: " + e.getColumnNumber() + ": ";
          return new SAXParseException(msg + e.getMessage(),
                                       e.getPublicId(),
                                       e.getSystemId(),
                                       e.getLineNumber(),
                                       e.getColumnNumber());
     }

    /**
     * Nullifies references this object currently has to any
     * StratmasObject.
     */ 
    public void nullify() 
    {
        currentPlaceHolder = null;
        currentObject = null;
        createdObject = null;
    }
}

/**
 * Exception thrown when SOPlaceHolder gets null from the vector
 * constructor.
 *
 * @version 1, $Date: 2006/09/18 09:45:25 $
 * @author  Per Alexius
 */
class IncompleteVectorConstructException extends Exception {
     /**
      * Creates an exception with the specified message.
      *
      * @param msg The message.
      */
     public IncompleteVectorConstructException(String msg) {
          super(msg);
     }
}
