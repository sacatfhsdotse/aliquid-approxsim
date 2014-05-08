// $Id: CollectedErrorsException.java,v 1.2 2005/09/09 17:25:19 dah Exp $
/*
 * @(#)CollectedErrorsException.java
 */

package ApproxsimClient.TaclanV2;

import java.util.Vector;
import java.util.Enumeration;

/**
 * An object representing a set of semantic errors in the Taclan V2 language.
 * 
 * @version 1, $Date: 2005/09/09 17:25:19 $
 * @author Daniel Ahlin
 */

public class CollectedErrorsException extends SemanticException {
    /**
	 * 
	 */
    private static final long serialVersionUID = 4347020574636698597L;
    /**
     * The errors.
     */
    Vector errors;

    /**
     * Creates a new CollectedErrorsException
     * 
     * @param errors the errors of this exception.
     */
    public CollectedErrorsException(Vector errors) {
        this.errors = errors;
    }

    /**
     * Creates a new CollectedErrorsException
     */
    public CollectedErrorsException() {
        this(new Vector());
    }

    /**
     * Adds an error to the exception
     * 
     * @param error the error to add.
     */
    public void add(Exception error) {
        this.errors.add(error);
    }

    public String getMessage() {
        StringBuffer buf = new StringBuffer();
        for (Enumeration es = this.errors.elements(); es.hasMoreElements();) {
            Exception e = (Exception) es.nextElement();
            if (e instanceof CollectedErrorsException) {
                buf.append(e.getMessage());
            } else {
                buf.append(e.getMessage() + "\n");
            }
        }

        return buf.toString();
    }

    /**
     * Make declaration the scope of any unclaimed errors
     * 
     * @param declaration the declaration thats the scope of the error.
     */
    public void claimUnclaimed(ParsedDeclaration declaration) {
        for (Enumeration es = this.errors.elements(); es.hasMoreElements();) {
            Object e = es.nextElement();
            if (e instanceof SemanticException) {
                ((SemanticException) e).claimUnclaimed(declaration);
            }
        }
    }
}
