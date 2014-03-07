//         $Id: ParsedBoolean.java,v 1.3 2006/03/31 16:55:50 dah Exp $
/*
 * @(#)ParsedBoolean.java
 */

package StratmasClient.TaclanV2;

/**
 * An object representing the type boolean of the Taclan language.  As
 * such it contains artefacts of the language. 
 *
 * @version 1, 09/28/04
 * @author  Daniel Ahlin
*/

public class ParsedBoolean extends ParsedPrimitive
{   
    /**
     * boolean containing the value of the declared boolean.
     */
    boolean value;

    /**
     *@param pos where the element is declared.
     *@param value the boolean this ParsedBoolean contains.
     */
    public ParsedBoolean(SourcePosition pos, boolean value) throws SemanticException
    {
        super(pos);
        this.value = value;
    }

    /**
     * Returns a string representation of the value this object holds.
     */
    public String valueToString()
    {
        if (value) {
            return "true";
        } else {
            return "false";
        }
    }
}
