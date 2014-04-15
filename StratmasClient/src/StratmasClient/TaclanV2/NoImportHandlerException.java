//         $Id: NoImportHandlerException.java,v 1.2 2005/03/30 22:22:23 dah Exp $
/*
 * @(#)NoImportHandlerException.java
 */

package StratmasClient.TaclanV2;
import java.util.Vector;
import java.util.Enumeration;

/**
 * An object representing the incapability of importing a format into
 * TaclanV2
 *
 * @version 1, $Date: 2005/03/30 22:22:23 $
 * @author  Daniel Ahlin
*/

public class NoImportHandlerException extends Exception
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 2992857371086675738L;

	/**
     * The location of the import.
     */
    String location;
    
    /**
     * Additional error information.
     */
    Vector errors;

    /**
     * Creates a new NoImportHandlerException.
     * @param location the location of the import.
     * @param errors a vector containing the error messages from the
     * different handlers.
     */
    public NoImportHandlerException(String location, Vector errors)
    {
        this.location = location;
        this.errors = errors;
    }

    /**
     * Returns a message detailing the cause of the exception.
     */
    public String getMessage()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("No handler is capable of handling \"" + 
                   this.location + "\n Details as follows:\n");
        for (Enumeration es = this.errors.elements(); es.hasMoreElements();) {
            ImportHandlerException e = (ImportHandlerException) es.nextElement();
            buf.append(e.getMessage() + "\n");
        }

        return buf.toString();
    }

    /**
     * Returns a string representation of this object.
     */
    public String toString()
    {
        return getMessage();
    }

}
