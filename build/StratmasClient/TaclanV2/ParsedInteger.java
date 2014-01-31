// 	$Id: ParsedInteger.java,v 1.8 2006/03/31 16:55:50 dah Exp $
/*
 * @(#)ParsedInteger.java
 */

package StratmasClient.TaclanV2;

/**
 * An object representing the type integer of the Taclan language.  As
 * such it contains artefacts of the language. 
 *
 * @version 1, 09/28/04
 * @author  Daniel Ahlin
*/

public class ParsedInteger extends ParsedPrimitive
{   
    /**
     * string containing the declared integer.
     */
    String value;

    /**
     *@param pos where the element is declared.
     *@param value the integer this ParsedInteger contains.
     */
    public ParsedInteger(SourcePosition pos, String value) throws SemanticException
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
