// 	$Id: MissingDeclarationException.java,v 1.5 2006/03/22 14:30:49 dah Exp $
/*
 * @(#)MissingDeclarationException.java
 */

package StratmasClient.TaclanV2;
import StratmasClient.object.type.Declaration;

public class MissingDeclarationException extends SemanticException
{
    /**
     * The scope where the error was detected or null if topmost scope.
     */
    ParsedDeclaration scope;

    /**
     * The declaration missing.
     */
    Declaration declaration;

    /**
     * Extra error information.
     */
    String information;

    /**
     * Creates a new MissingDeclarationException
     * @param declaration the declaration missing.     
     * @param information information on what caused the error     
     */
    public MissingDeclarationException(Declaration declaration, String information)
    {
	this.declaration = declaration;
	this.information = information;
    }

    /**
     * Sets the scope missing the declaration this exception concerns.
     *
     * @param scope the ParsedDeclaration missing this
     * declaration (or null if it is the top scope).
     */
    public void setScopeIfMissing(ParsedDeclaration scope)
    {
	if (this.scope == null) {
	    this.scope = scope;
	}
    }


    /**
     * Returns a string representation of this object.
     */
    public String toString()
    {
	if (this.scope != null) {
	    return this.scope.getPos().toString() + ": '" + this.scope.getType().getName() + 
		"' requires a '" + this.declaration.getName() + 
		"' entry. " + information;
	}
	else {
	    return "Missing a '" + this.declaration.getType().getName() + 
		"' entry. " + information;
	}
    }


    /**
     * Returns a string representation of this object.
     */
    public String getMessage()
    {
	return toString();
    }
}
