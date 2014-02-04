//         $Id: ParsedFloat.java,v 1.4 2006/03/31 16:55:50 dah Exp $
/*
 * @(#)ParsedFloat.java
 */

package StratmasClient.TaclanV2;
import StratmasClient.object.StratmasDecimal;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.type.Declaration;

/**
 * An object representing the type float of the Taclan language.  As
 * such it contains artefacts of the language. 
 *
 * @version 1, 09/28/04
 * @author  Daniel Ahlin
*/

public class ParsedFloat extends ParsedPrimitive
{   
    /**
     * String containing the declared float.
     */
    String value;

    /**
     *@param pos where the element is declared.
     *@param value the double this ParsedFloat contains.
     */
    public ParsedFloat(SourcePosition pos, String value) throws SemanticException
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
}
