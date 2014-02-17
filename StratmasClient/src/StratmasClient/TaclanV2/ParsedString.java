//         $Id: ParsedString.java,v 1.7 2006/03/31 16:55:50 dah Exp $
/*
 * @(#)ParsedString.java
 */

package StratmasClient.TaclanV2;

import java.text.ParseException;

import StratmasClient.object.StratmasObjectFactory;
import StratmasClient.object.StratmasSimple;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.type.Declaration;

/**
 * An object representing the type string of the Taclan language.  As
 * such it contains artefacts of the language. 
 *
 * @version 1, 09/28/04
 * @author  Daniel Ahlin
*/

public class ParsedString extends ParsedPrimitive
{
    /**
     * String containing the declared string.
     */
    String value;

    /**
     *@param pos where the element is declared.
     *@param value the string this ParsedString contains.
    */
    public ParsedString(SourcePosition pos, String value) throws SemanticException
    {
        super(pos);
        this.value = value;
    }

    /**
     * Returns a string representation of the value this object holds.
     */
    public String valueToString()
    {
        return value;
    }

    public String getValue()
    {
        return value;
    }

   /**
     * Returns the StratmasObject equivalent this declaration.
     *
     * @param declaration the declaration to use.
     */
    public StratmasObject getStratmasObject(Declaration declaration) 
        throws SemanticException
    {
        String literal = toString();
        // Expand \n and \"
        literal = literal.replaceAll("\\\\n", "\n");
        literal = literal.replaceAll("\\\\\"", "\"");
        
        try {
            StratmasSimple res = (StratmasSimple) StratmasObjectFactory.defaultCreate(declaration);
            res.valueFromString(valueToString());
            return res;
        } catch (ParseException e) {
            throw new ConversionException(this, declaration);
        }
    }
}
