// $Id: ParsedString.java,v 1.7 2006/03/31 16:55:50 dah Exp $
/*
 * @(#)ParsedString.java
 */

package ApproxsimClient.TaclanV2;

import java.text.ParseException;

import ApproxsimClient.object.ApproxsimObjectFactory;
import ApproxsimClient.object.ApproxsimSimple;
import ApproxsimClient.object.ApproxsimObject;
import ApproxsimClient.object.type.Declaration;

/**
 * An object representing the type string of the Taclan language. As such it contains artefacts of the language.
 * 
 * @version 1, 09/28/04
 * @author Daniel Ahlin
 */

public class ParsedString extends ParsedPrimitive {
    /**
     * String containing the declared string.
     */
    String value;

    /**
     * @param pos where the element is declared.
     * @param value the string this ParsedString contains.
     */
    public ParsedString(SourcePosition pos, String value)
            throws SemanticException {
        super(pos);
        this.value = value;
    }

    /**
     * Returns a string representation of the value this object holds.
     */
    public String valueToString() {
        return value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Returns the ApproxsimObject equivalent this declaration.
     * 
     * @param declaration the declaration to use.
     */
    public ApproxsimObject getApproxsimObject(Declaration declaration)
            throws SemanticException {
        String literal = toString();
        // Expand \n and \"
        literal = literal.replaceAll("\\\\n", "\n");
        literal = literal.replaceAll("\\\\\"", "\"");

        try {
            ApproxsimSimple res = (ApproxsimSimple) ApproxsimObjectFactory
                    .defaultCreate(declaration);
            res.valueFromString(valueToString());
            return res;
        } catch (ParseException e) {
            throw new ConversionException(this, declaration);
        }
    }
}
