//         $Id: ConversionException.java,v 1.3 2006/03/22 14:30:49 dah Exp $
/*
 * @(#)ConversionException.java
 */

package StratmasClient.TaclanV2;
import StratmasClient.object.type.Declaration;

public class ConversionException extends SemanticException
{
    /**
     * The erroneous primitive.
     */
    ParsedPrimitive primitive;

    /**
     * The declaration the primitive failed to comply with.
     */
    Declaration declaration;

    /**
     * Creates a new IdConflictException
     * @param primitive the erroneous primitive.
     * @param declaration the declaration the primitive failed to comply with.
     */
    public ConversionException(ParsedPrimitive primitive, Declaration declaration)
    {
        this.primitive = primitive;
        this.declaration = declaration;        
    }

    public String toString()
    {
        return primitive.getPos().toString() + ": Unable to construct a " + declaration.getType().getName() + " from \"" + primitive.valueToString() + "\"";
    }
}
