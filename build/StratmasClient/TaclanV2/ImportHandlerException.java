//         $Id: ImportHandlerException.java,v 1.3 2005/07/14 10:01:14 dah Exp $
/*
 * @(#)ImportHandlerException.java
 */

package StratmasClient.TaclanV2;
import java.util.Vector;
import java.util.Enumeration;

/**
 * An object representing and error in a handler when importing
 * something into Taclan
 *
 * @version 1, $Date: 2005/07/14 10:01:14 $
 * @author  Daniel Ahlin
*/

public class ImportHandlerException extends Exception
{
    /**
     * The handler causing the exception.
     */
    ImportHandler handler;
    
    /**
     * Additional error information.
     */
    String info;

    /**
     * Creates a new ImportHandlerException.
     * @param handler the handler causing the exception.
     * @param info a vector containing the error messages from the
     * different handlers.
     */
    public ImportHandlerException(ImportHandler handler, String info)
    {
        this.handler = handler;
        this.info = info;
    }

    /**
     * Returns a detailed error message.
     */
    public String getMessage()
    {
        return handler.getClass().getName() + ": " + info;
    }


    /**
     * Returns a string representation of this object.
     */
    public String toString()
    {
        return getMessage();
    }

}
