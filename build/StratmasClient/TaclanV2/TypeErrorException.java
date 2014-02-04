//         $Id: TypeErrorException.java,v 1.3 2005/09/09 17:25:19 dah Exp $
/*
 * @(#)TypeErrorException.java
 */

package StratmasClient.TaclanV2;

public class TypeErrorException extends SemanticException
{
    /**
     * The declaration causing the error.
     */
    ParsedDeclaration declaration;

    /**
     * Extra error information.
     */
    String information;

    /**
     * Creates a new TypeErrorException
     * @param declaration the first declaration of the identifier.
     * @param information information on what caused the error     
     */
    public TypeErrorException(ParsedDeclaration declaration, String information)
    {
        this.declaration = declaration;
        this.information = information;
    }

    /**
     * Returns a string representation of this exception.
     */    
    public String toString()
    {
        return this.declaration.getPos().toString() + " " + information;
    }

    /**
     * Returns a string representation of this exception.
     */
    public String getMessage()
    {
        return toString();
    }

    /**
     * Make declaration the scope of any unclaimed errors
     *
     * @param declaration the declaration thats the scope of the error.
     */
    public void claimUnclaimed(ParsedDeclaration declaration)
    {
        if (declaration == null) {
            this.declaration = declaration;
        }
    }
}
