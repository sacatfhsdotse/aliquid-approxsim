// 	$Id: UnitImportFilter.java,v 1.2 2006/09/25 10:27:39 alexius Exp $
/*
 * @(#) UnitImportFilter.java
 */

package StratmasClient;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.IOException;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Iterator;

import java.text.ParseException;

import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasObjectFactory;
import StratmasClient.object.StratmasSimple;
import StratmasClient.object.SymbolIDCode;
import StratmasClient.object.primitive.Reference;

import StratmasClient.object.type.Type;
import StratmasClient.object.type.TypeFactory;

import StratmasClient.object.type.Declaration;

/**
 * UnitImportFilter uses information from a table to add sensible
 * variables. 
 *
 * @version 1, $Date: 2006/09/25 10:27:39 $
 * @author  Daniel Ahlin
*/
public class UnitImportFilter
{
    /**
     * Position for symbol id code
     */
    final static int APP6A_ECHELON = 0;
    /**
     * Position for symbol id code
     */
    final static int APP6A_COUNTRY_CODE = 1;
    /**
     * Position for symbol id code
     */
    final static int APP6A_F1 =  2;
    /**
     * Position for symbol id code
     */
    final static int APP6A_F2 =  3;
    /**
     * Position for symbol id code
     */
    final static int APP6A_F3 =  4;
    /**
     * Position for symbol id code
     */
    final static int APP6A_F4 =  5;
    /**
     * Position for symbol id code
     */
    final static int APP6A_F5 =  6;
    /**
     * Position for symbol id code
     */
    final static int APP6A_F6 = 7;

    /**
     * Delimiter expression
     */
    String delimiter = ",";

    /**
     * Headers of file
     */
    String[] headers = new String[0];
    
    /**
     * In Table
     */
    String[][] inTable = new String[0][0];

    /**
     * Out Table
     */
    String[][] outTable = new String[0][0];
    
    /**
     * Out Table headers
     */
    String[] outTableHeaders = new String[0];

    /**
     * Out Table targets 
     */
    Reference[] outTableTargets = new Reference[0];

    /**
     * Creates a new Import filter given a csv stream.
     *
     * @param stream stream to get table data from.
     */
    public UnitImportFilter(InputStream stream) throws IOException, 
						       ParseException
    {
	Vector warnings = importFilter(new InputStreamReader(stream));
	for (Iterator it = warnings.iterator(); it.hasNext();) {
	    Debug.err.println("UnitImport error: " + it.next());
	}
    }

    /**
     * Imports the filter.
     *
     * @param reader reader to use.
     * @return warnings
     */
    private Vector importFilter(Reader reader) throws IOException, 
						      ParseException
    {
	LineNumberReader lineReader = new LineNumberReader(reader);
	Vector warnings = new Vector();
	
	String headerString = lineReader.readLine();
	
	String[] headers = headerString.split(this.delimiter);
	// array used when sorting columns into in and out set.
	// a > 0: in column at index a - 1
	// a == 0: ignore column a
	// a < 0 : out column (will be at index (-a) - 1);
	int outColumnCounter = 0;
	int inColumnCounter = 0;
	int[] columnSorter = new int[headers.length];
	Vector ovh = new Vector();
	for (int j = 0; j < headers.length; j++) {
	    int a = classifyColumn(headers[j]);
	    if (a > 0) {
		columnSorter[j] = a;
		inColumnCounter = inColumnCounter > a ? inColumnCounter : a;
	    } else if (a < 0) {
		columnSorter[j] = -(1 + outColumnCounter++);
		ovh.add(headers[j]);
	    } else {
		columnSorter[j] = a;
	    }
	}
	   
	this.outTableHeaders = (String[]) ovh.toArray(outTableHeaders);
	this.outTableTargets = new Reference[outTableHeaders.length];
	for (int j = 0; j < outTableHeaders.length; j++) {
	    Reference ref = resolveHeaderTarget(outTableHeaders[j]);
	    if (ref == null) {
		warnings.add("Unable to match column \"" + outTableHeaders[j] 
			     + "\" to target"); 
	    }
	    outTableTargets[j] = ref;
	}

	Vector iv = new Vector();
	Vector ov = new Vector();
	
	for (String line = lineReader.readLine(); line != null; 
	     line = lineReader.readLine()) {
	    String[] fields = line.split(this.delimiter);
	    if (fields.length != headers.length) {
		throw new ParseException("Wrong number of fields " + 
					 fields.length + " (should be) " + 
					 headers.length, 
					 lineReader.getLineNumber());
	    } else {
		// inColumnCounter start at 1, hence no +1
		String[] inFields = new String[inColumnCounter];
		// outColumnCounter is postincremented above, hence no +1
		String[] outFields = new String[outColumnCounter];
		for (int j = 0; j < fields.length; j++) {
		    String field = fields[j];
		    if (field == "" || field.length() == 0 ||
			field.matches("\\s+") || field.matches("\\*+")) {
			field = null;
		    }

		    if (columnSorter[j] > 0) {
			inFields[columnSorter[j] - 1] = field;
		    } else if (columnSorter[j] < 0) {
			outFields[(-columnSorter[j]) - 1] = field;
		    }
		    // Ignore 0-fields
		}
		iv.add(inFields);
		ov.add(outFields);
	    }
	}

	this.inTable = (String[][]) iv.toArray(inTable);
	this.outTable = (String[][]) ov.toArray(outTable);

	return warnings;
    }
    
    /**
     * Classifies a header as either input, output or non
     * interesting.
     *
     * ret >= 0: input and position in input
     * ret == -1 not interesting.
     * ret == -2 output
     * @param header the header to classify.
     */
    private int classifyColumn(String header)
    {
        if (header == null || header == "" || 
	    header.length() == 0 || header.matches("\\s+")) {
	    return 0;
	} else if (header.matches("Function ID 1.*")) {
	    return APP6A_F1 + 1;
	} else if (header.matches("Function ID 2.*")) {
	    return APP6A_F2 + 1;
	} else if (header.matches("Function ID 3.*")) {
	    return APP6A_F3 + 1;
	} else if (header.matches("Function ID 4.*")) {
	    return APP6A_F4 + 1;
	} else if (header.matches("Function ID 5.*")) {
	    return APP6A_F5 + 1;
	} else if (header.matches("Function ID 6.*")) {
	    return APP6A_F6 + 1;
	} else if (header.matches("Echelon.*")) {
	    return APP6A_ECHELON + 1;
	} else if (header.matches("Country Code.*")) {
	    return APP6A_COUNTRY_CODE + 1;
	} else {
	    return -2;
	}
    }

    /**
     *  Finds appropriate target for output.
     *
     * @param header header
     *
     * @return reference if mapping succesful, else null.
     */
    private Reference resolveHeaderTarget(String header) 
    {
	Reference res = null;
	
	if (header.matches("Radius.*")) {
	    res = new Reference(new String[] {"radius", "location"});
	} else if (header.matches("Personnel.*")) {
	    res = new Reference(new String[] {"personnel"});
	} else if (header.matches("Strength Factor.*")) {
	    res = new Reference(new String[] {"strengthFactor"});
	} else if (header.matches("Attack Factor.*")) {
	    res = new Reference(new String[] {"attackFactor"});
	} else if (header.matches("Defense Factor.*")) {
	    res = new Reference(new String[] {"defenseFactor"});
	} else if (header.matches("Maximum Velocity.*")) {
	    res = new Reference(new String[] {"maxVelocity"});
	} else if (header.matches("Withdraw Threshold.*")) {
	    res = new Reference(new String[] {"WithdrawThreshold"});
	} else if (header.matches("Distribution.*")) {
	    res = new Reference(new String[] {"deployment"});
	} else if (header.matches("Sigma.*")) {
	    res = new Reference(new String[] {"sigmaMeters", "deployment"});
	}

	return res;
    }


    /**
     * Sets values of the provided unit to match its symbolIDCode. If
     * the unit lacks a symbolIDCode nothing is done.
     *
     * @param unit the unit being completed
     *
     * @return vector with errors
     */
    public Vector apply(StratmasObject unit)
    {
	Vector warnings = new Vector();

	// Get symbolIDCode
	StratmasObject candidate = unit.getChild("symbolIDCode");
	if (candidate == null ||
	    !(candidate instanceof SymbolIDCode)) {
	    warnings.add("No symbolIDCode in " + unit.getIdentifier());
	    return warnings;
	}
	String[] matchFields = createMatchFields((SymbolIDCode) candidate);

	// Find first matching row in table. (maybe we should cache this...)
	for (int i = 0; i < inTable.length; i++) {
	    boolean match = true;
	    for (int j = 0; j < matchFields.length; j++) {
		// Match iff
		// 1. equality or
		// 2. matchField only contain dashes '--' or
		// 3. matchField is null;

		if (!(inTable[i][j] == null ||
		      inTable[i][j].equals(matchFields[j]))) {
		    // Stop at first mismatch.
		    match = false;
		    break;
		}
	    }
	    // Take first match
	    if (match) {
		applyLine(unit, i, warnings);
		break;
	    }
	}
	
	return warnings;
    }

    /**
     * Tries to apply the settings of the given line to the provided unit.
     *
     * @param unit the unit to apply changed to
     * @param lineIndex the values to apply to unit.
     * @param warnings to add warnings to.
     *
     * @param returns vector with warnings.
     */
    public void applyLine(StratmasObject unit, int lineIndex, Vector warnings) 
    {
	String[] line = outTable[lineIndex];

	for (int j = 0; j < outTableHeaders.length; j++) {
	    // If no value is set or no target, continue:
	    if (line[j] == null || outTableTargets[j] == null) {
		continue;
	    }
	    boolean doAdd = false;

	    StratmasObject child = outTableTargets[j].resolve(unit);

	    if (child == null) {
		Declaration declaration = 
		    unit.getType().getSubElement(outTableHeaders[j]);
		if (declaration == null) {
		    warnings.add("Unable to apply \"" + outTableHeaders[j] + 
				 "\" no such element in " + 
				 unit.getType().getName());
		} else {
		    child = 
			StratmasObjectFactory.create(declaration.getType());
		    doAdd = true;
		}
	    }

	    if (child instanceof StratmasSimple) {
		try {
		    ((StratmasSimple) child).valueFromString(line[j]);
		} catch (ParseException e) {
		    warnings.add("" + e.getErrorOffset() + ": " + 
				 e.getMessage());
		    doAdd = false;
		}
	    } else {
		// Check if requested type can substitute target
		Type requestedType = TypeFactory.getType(line[j]);
		if (requestedType == null ||
		    !requestedType.canSubstitute(unit.getType().getSubElement(child.getIdentifier()).getType())) {
		    warnings.add(line[j] + " can not serve as an " + 
				 child.getIdentifier());
		    doAdd = false;
		} else {
		    StratmasObject newChild = 
			StratmasObjectFactory.create(requestedType);
		    newChild.setIdentifier(child.getIdentifier());
		    unit.add(newChild);
		    doAdd = false;
		}
	    }

	    if (doAdd) {
		unit.add(child);
	    }
	}
    }

    
    /**
     * Splits a SymbolIDCode into fields used for matching in the in table
     *
     * @param id the code to split.
     */
    private String[] createMatchFields(SymbolIDCode id)
    {
	String str = id.valueToString();
	
	// Known max, fix this.
	String[] matchFields = new String[APP6A_F6 + 1];
	
	matchFields[APP6A_F1] = str.substring(4, 5);
	matchFields[APP6A_F2] = str.substring(5, 6);
	matchFields[APP6A_F3] = str.substring(6, 7);
	matchFields[APP6A_F4] = str.substring(7, 8);
	matchFields[APP6A_F5] = str.substring(8, 9);
	matchFields[APP6A_F6] = str.substring(9, 10);
	matchFields[APP6A_ECHELON] = str.substring(11, 12);
	matchFields[APP6A_COUNTRY_CODE] = str.substring(12, 14);

	return matchFields;
    }

    /**
     * Produces a string representing the mapping:
     */
    public String toString()
    {
	StringBuffer buf = new StringBuffer();

	buf.append("In table:\n");
	for (int i = 0; i < inTable.length; i++) {
	    buf.append(" | " + i + " | ");
	    for (int j = 0; j < inTable[i].length; j++) {
		buf.append(inTable[i][j] + " | ");
	    }
	    buf.append("\n");
	}

	buf.append("\nOut table:\n |  Nr | ");
	for (int j = 0; j < outTableHeaders.length; j++) {
	    buf.append(outTableHeaders[j] + " | ");
	}
	buf.append("\n");
	for (int i = 0; i < outTable.length; i++) {
	    buf.append(" | " + i + " | ");
	    for (int j = 0; j < outTable[i].length; j++) {
		buf.append(outTable[i][j] + " | ");
	    }
	    buf.append("\n");
	}

	return buf.toString();
    }
    
}
