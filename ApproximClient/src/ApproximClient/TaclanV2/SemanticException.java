// $Id: SemanticException.java,v 1.1 2005/02/03 10:20:59 dah Exp $
/*
 * @(#)SemanticException.java
 */

package ApproxsimClient.TaclanV2;

/**
 * An object representing a semantic error in TaclanV2
 * 
 * @version 1, $Date: 2005/02/03 10:20:59 $
 * @author Daniel Ahlin
 */

public abstract class SemanticException extends Exception {

    /**
	 * 
	 */
    private static final long serialVersionUID = 5373292493538416116L;

    public String toString() {
        return getMessage();
    }

    /**
     * Make declaration the scope of any unclaimed errors
     */
    public void claimUnclaimed(ParsedDeclaration declaration) {}
}
