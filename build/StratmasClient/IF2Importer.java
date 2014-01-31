package StratmasClient;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Vector;
import java.text.ParseException;
import javax.swing.JOptionPane;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import StratmasClient.communication.XMLHandler;
import StratmasClient.object.type.Declaration;

import StratmasClient.object.primitive.Reference;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasList;
import StratmasClient.object.StratmasObjectFactory;
import StratmasClient.object.StratmasReference;
import StratmasClient.object.SymbolIDCode;
import StratmasClient.object.type.TypeFactory;



/**
 *  A class that handles import of IconFactory2 projects.
 *
 * @version 1, $Date: 2006/09/26 17:26:46 $
 * @author  Per Alexius
 */
public class IF2Importer {
     /**
      * The root object to fetch factions from.  
      */
     private StratmasObject mRootObject;

     /**
      * The faction reference to use for affiliation attribute of
      * imported units.
      */
     private Reference sFaction = null;

     /**
      * Holds the latest error message.
      */
     private String mErrorMessage = null;

     /**
      * True if an error has occurred.
      */
     private boolean mErrorOccurred = false;

     /**
      * The XML parser used to parse IF2 project files
      */
     private DOMParser mParser = new DOMParser();

     /**
      * Default constructor.
      */
     public IF2Importer(StratmasObject root) {
	  if (!(root instanceof StratmasObject)) {
	       throw new AssertionError("Not a Complex root object in IF2Importer.");
	  }
	  mRootObject = (StratmasObject)root.children().nextElement();
	  try {
	       // Turned off for now since IconFactory2 exports broken documents.
	       mParser.setFeature("http://xml.org/sax/features/validation", false);
	  } catch (SAXNotRecognizedException e) {
	       setErrorMessage("Initialization error: Validation option not recognized by parser in IF2Importer. " +
			       "Validation turned off.");
	  } catch (SAXNotSupportedException e) {
	       setErrorMessage("Initialization error: Validation not supported in IF2Importer. Validation turned off.");
	  }
     }

     /**
      * Imports units from an IF2 project. Sets the units' identifier,
      * symbolIDCode and subunits attributes. Asks the user for an
      * affiliation faction and initializes the remaining attributes
      * with their default values.
      *
      * @param fileName The name of the file to import from.
      '
      * @return A StratmasObject representing the unit (hierarchy)
      * imported or null if the file did not contain any importable objects.
      */
     public StratmasObject importFromFile(String fileName) {
	  Vector vec = new Vector();
	  StratmasObject ret = null;
	  try {
	      // InputSource inputSource = new InputSource(new BufferedReader(new FileReader(fileName)));
	      InputSource inputSource = 
		  new InputSource(new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "ISO-8859-1")));
	      inputSource.setEncoding("ISO-8859-1");
	      mParser.parse(inputSource);
	       Element docElem = mParser.getDocument().getDocumentElement();
	       if (!docElem.getTagName().equals("IF2Project")) {
		    setErrorMessage("Root element has tag '" + docElem.getNodeName() + "'. Should be IF2Project'");
		    return null;
	       }
	       
	       Element symbols = XMLHandler.getFirstChildByTag(docElem, "Symbols");

	       // IF2 projects may only contain one root element.
	       ret = handleSymbol(XMLHandler.getFirstChildByTag(symbols, "Symbol"));
	  } catch (SAXException e) {
 	       e.printStackTrace();
	       setErrorMessage("ParseError: " + e.getMessage());
	  } catch (IOException e) {
	       e.printStackTrace();
	       setErrorMessage("IO Error: " + e.getMessage());
	  }
	  sFaction = null;
	  return ret;
     }

     /**
      * Imports units from an IF2 project. Sets the units' identifier,
      * symbolIDCode and subunits attributes.
      *
      * @param fileName The name of the file to import from.
      * @param faction  The affiliation faction.
      *      
      * @return A StratmasObject representing the unit (hierarchy)
      * imported or null if the file did not contain any importable objects.
      */
    public StratmasObject importFromFile(String fileName, Reference faction) {
	Vector vec = new Vector();
	StratmasObject ret = null;	
	sFaction = faction;
	try {
	    // InputSource inputSource = new InputSource(new BufferedReader(new FileReader(fileName)));
	    InputSource inputSource = 
		new InputSource(new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "ISO-8859-1")));
	    inputSource.setEncoding("ISO-8859-1");
	    mParser.parse(inputSource);
	    Element docElem = mParser.getDocument().getDocumentElement();
	    if (!docElem.getTagName().equals("IF2Project")) {
		setErrorMessage("Root element has tag '" + docElem.getNodeName() + "'. Should be IF2Project'");
		return null;
	    }
	    
	    Element symbols = XMLHandler.getFirstChildByTag(docElem, "Symbols");
	    
	    // IF2 projects may only contain one root element.
	    ret = handleSymbol(XMLHandler.getFirstChildByTag(symbols, "Symbol"));
	} catch (SAXException e) {
	    e.printStackTrace();
	    setErrorMessage("ParseError: " + e.getMessage());
	} catch (IOException e) {
	    e.printStackTrace();
	    setErrorMessage("IO Error: " + e.getMessage());
	}
	sFaction = null;
	return ret;
     }




     /**
      * Creates a StratmasObject representing the unit described in a
      * DOMElement (originating from a parsed IF2 project).
      *
      * @param symbol A DOMElement containing data about the symbol.
      *
      * @return The unit created from the DOMElement.
      */
     protected StratmasObject handleSymbol(Element symbol) {
	  String id = new String();
	  Vector subunits = new Vector();
	  String symbolIDCode = XMLHandler.getString(symbol, "symid");

	  // Assure uppercase.
	  symbolIDCode = symbolIDCode.toUpperCase();
	  
	  // Remove "" around the symbolIDCode if there are any.
	  if (symbolIDCode.startsWith("\"") && symbolIDCode.endsWith("\"")) {
	       symbolIDCode = symbolIDCode.substring(1, symbolIDCode.length() - 1);
	  }

	  // Pad string with '-' if it's shorter than 15 chars since
	  // IF2 may save symbol id codes that way and Stratmas
	  // doesn't like it.
	  if (symbolIDCode.length() < 15) {
	      String dashes = "---------------";
	      symbolIDCode += dashes.substring(symbolIDCode.length());
	  }

	  // Handle fields.
	  Element elem = XMLHandler.getFirstChildByTag(symbol, "Fields");
	  if (elem != null) {
	       for (Node child = elem.getFirstChild(); child != null; child = child.getNextSibling()) {
		    if (child.getNodeType() == Node.ELEMENT_NODE &&
			child.getNodeName().equals("field")) {
			 Element field = (Element)child;
			 if (field.getAttribute("ID").equals("T")) {
			      id = field.getAttribute("VALUE");
			 }
		    }
	       }
	  }

	  // Handle children
	  elem = XMLHandler.getFirstChildByTag(symbol, "Children");
	  if (elem != null) {
	       for (Node child = elem.getFirstChild(); child != null; child = child.getNextSibling()) {
		    if (child.getNodeType() == Node.ELEMENT_NODE &&
			child.getNodeName().equals("Symbol")) {
			 StratmasObject unit = handleSymbol((Element)child);
			 if (unit == null) {
			      return null;
			 }
			 else {
			      subunits.add(unit);
			 }
		    }
	       }
	  }
	  return createDefaultImportedUnit(id, symbolIDCode, subunits);
     }

     /**
      * Helper for creating military units with a specified id, symbol
      * id code and set of subunits, where the rest of the attributes
      * are given their default values except for the affiliation
      * faction for which the user is asked..
      *
      * @param id The identifier for the object to be created.
      * @param symCode The symbol id code for the object to be created.
      * @param subunits The subunits of the object to be created.
      *
      * @return The unit created.
      */
     public StratmasObject createDefaultImportedUnit(String id, String symCode, Vector subunits) {
	  // Create new default unit.
	  Declaration unitDec = new Declaration(TypeFactory.getType("MilitaryUnit"), "", 1, 1, false);
	  StratmasObject unit = (StratmasObject)StratmasObjectFactory.defaultCreate(unitDec);

	  if (sFaction == null) {
	       StratmasObject scenario = (StratmasObject)mRootObject.getChild("scenario");
	       if (scenario == null) {
		    setErrorMessage("No scenario in root object");
		    return null;
	       }
	       StratmasList facList = (StratmasList)scenario.getChild("factions");
	       if (facList == null) {
		    setErrorMessage("No faction list in scenario object");
		    return null;
	       }
	       Vector facs = new Vector();
	       for (java.util.Enumeration en = facList.children(); en.hasMoreElements(); ) {
		    facs.add(((StratmasObject)en.nextElement()).getIdentifier());
	       }

	       if (facs.isEmpty()) {
		    setErrorMessage("No factions in Scenario");
		    return null;
	       }
	       else {
		    Object [] values = facs.toArray();
		    StratmasDialog.quitProgressBarDialog(); 
		    Object selectedValue = JOptionPane.showInputDialog(null,
								       "Choose the affiliation faction for the imported units ",
								       "Please choose",
								       JOptionPane.INFORMATION_MESSAGE,
								       null, values, values[0]);
		    
		    if (selectedValue == null) {
			 return null;
		    } 
		    else {
			 StratmasObject chosen = facList.getChild((String)selectedValue);
			 if (chosen == null) {
			      setErrorMessage("Null value chosen in dialog");
			      return null;
			 }
			 else {
			      sFaction = chosen.getReference();
			 }
		    }
	       }
	  }
	  
	  StratmasReference affiliation = (StratmasReference)unit.getChild("affiliation");
	  affiliation.setValue(sFaction, null);

	  // Set identifier, symbolIDCode and subunits.
	  unit.setIdentifier(id);
	  try {
	      ((SymbolIDCode)unit.getChild("symbolIDCode")).valueFromString(symCode, null);
	  } catch (ParseException e) {
	      // No error checking before, ParseException introduced,
	      // ignored no as well.
	  }
	  if (subunits != null && !subunits.isEmpty()) {
	       StratmasObject subunitsList = unit.getChild("subunits");
	       if (subunitsList == null) {
		   unit.add(StratmasObjectFactory.createList(TypeFactory.getType("MilitaryUnit").getSubElement("subunits"), 
							     subunits));
	       }
	       else {
		    subunitsList.add(subunits);
	       }
	  }
	  return unit;
     }

     /**
      * Accessor for the latest error message
      *
      * @return The latest error message
      */
     public String getErrorMessage() {
	  return mErrorMessage;
     }

     /**
      * Mutator for the error message.
      *
      * @param msg The message.
      */
     private void setErrorMessage(String msg) {
	  mErrorOccurred = true;
	  mErrorMessage = msg;
     }

     /**
      * Checks if an error occurred.
      *
      * @return True if an error has occurred, false otherwise.
      */
     public boolean errorOccurred () {
	  return mErrorOccurred;
     }
}
