// $Id: UnresolvedReferenceException.java,v 1.1 2005/02/03 10:20:59 dah Exp $
/*
 * @(#)UnresolvedReferenceException.java
 */

package StratmasClient.TaclanV2;

public class UnresolvedReferenceException extends SemanticException {
    /**
	 * 
	 */
    private static final long serialVersionUID = 9099580719425446802L;

    /**
     * The reference causing the error.
     */
    ParsedReference reference;

    /**
     * The declaration where the reference stopped resolving.
     */
    ParsedDeclaration lastResolved;

    /**
     * Creates a new UnresolvedReferenceException
     * 
     * @param reference the reference causing the error.
     * @param lastResolved the declaration where the reference stopped resolving.
     */
    public UnresolvedReferenceException(ParsedReference reference,
            ParsedDeclaration lastResolved) {
        this.reference = reference;
        this.lastResolved = lastResolved;
    }

    public String toString() {
        return reference.getPos().toString() + ": Unable to resolv + "
                + this.reference.toString();
    }
}
