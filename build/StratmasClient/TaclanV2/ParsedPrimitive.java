// 	$Id: ParsedPrimitive.java,v 1.4 2006/03/31 16:55:50 dah Exp $
/*
 * @(#)ParsedPrimitive.java
 */

package StratmasClient.TaclanV2;

import java.text.ParseException;

import StratmasClient.object.StratmasObjectFactory;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasSimple;
import StratmasClient.object.type.TypeInformation;
import StratmasClient.object.type.Declaration;
import StratmasClient.object.type.Type;

/**
 * An object representing a declaration of a primitive type in the
 * Taclan V2 language. As a parsed construct it contains artefacts of
 * the language, e. g. references to the source files
 *
 * @version 1, $Date: 2006/03/31 16:55:50 $
 * @author  Daniel Ahlin
*/

public abstract class ParsedPrimitive extends ParsedDeclaration
{
    public static ParsedIdentifier primitiveType = null;

    /**
     *@param pos where the declaration were made.
     */
    public ParsedPrimitive(SourcePosition pos)
	throws SemanticException
    {
	super(pos, primitiveType, ParsedIdentifier.getAnonymous(), 
	      ParsedDeclarationList.getEmpty());
    }

    /**
     * Performs type checking on immidiates (i. e. instances) using the supplied TypeInformation 
     *
     * @param definedDeclaration the Declaration this ParsedDeclaration is checked against.
     * @param typeInformation the TypeInformation to use.
     */
    public void typeCheckImmidiates(Declaration definedDeclaration, 
				    TypeInformation typeInformation) 
	throws SemanticException
    {
	
    }

    /**
     * Returns a string representation of this object.
     */
    public String toString()
    {
	return "'" + this.getIdentifier().getName() + "' = " + valueToString();
    }

    /**
     * Returns a string representation of the value this object holds.
     */
    public abstract String valueToString();
    
    /**
     * Returns the StratmasObject equivalent this declaration.
     *
     * @param declaration the declaration to use.
     */
    public StratmasObject getStratmasObject(Declaration declaration) throws SemanticException
    {
	try {
	    StratmasSimple res = (StratmasSimple) StratmasObjectFactory.defaultCreate(declaration);
	    res.valueFromString(valueToString());
	    return res;
	} catch (ParseException e) {
	    throw new ConversionException(this, declaration);
	}
    }
}
