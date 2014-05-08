// $Id: ParsedPrimitive.java,v 1.4 2006/03/31 16:55:50 dah Exp $
/*
 * @(#)ParsedPrimitive.java
 */

package ApproxsimClient.TaclanV2;

import java.text.ParseException;

import ApproxsimClient.object.ApproxsimObjectFactory;
import ApproxsimClient.object.ApproxsimObject;
import ApproxsimClient.object.ApproxsimSimple;
import ApproxsimClient.object.type.TypeInformation;
import ApproxsimClient.object.type.Declaration;

/**
 * An object representing a declaration of a primitive type in the Taclan V2 language. As a parsed construct it contains artefacts of the
 * language, e. g. references to the source files
 * 
 * @version 1, $Date: 2006/03/31 16:55:50 $
 * @author Daniel Ahlin
 */

public abstract class ParsedPrimitive extends ParsedDeclaration {
    public static ParsedIdentifier primitiveType = null;

    /**
     * @param pos where the declaration were made.
     */
    public ParsedPrimitive(SourcePosition pos) throws SemanticException {
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
            TypeInformation typeInformation) throws SemanticException {

    }

    /**
     * Returns a string representation of this object.
     */
    public String toString() {
        return "'" + this.getIdentifier().getName() + "' = " + valueToString();
    }

    /**
     * Returns a string representation of the value this object holds.
     */
    public abstract String valueToString();

    /**
     * Returns the ApproxsimObject equivalent this declaration.
     * 
     * @param declaration the declaration to use.
     */
    public ApproxsimObject getApproxsimObject(Declaration declaration)
            throws SemanticException {
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
