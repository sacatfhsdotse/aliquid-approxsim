// 	$Id: ImportException.java,v 1.2 2005/03/30 22:22:21 dah Exp $
/*
 * @(#)ImportException.java
 */

package StratmasClient.TaclanV2;

/**
 * An object representing an error importing something into TaclanV2
 *
 * @version 1, $Date: 2005/03/30 22:22:21 $
 * @author  Daniel Ahlin
*/

public class ImportException extends SemanticException
{
    /**
     * The incorrect import.
     */
    ParsedImport importation;
    
    /**
     * Additional error information.
     */
    String info;

    /**
     * Creates a new ImportException.
     * @param importation the ParsedImport causing the error.
     * @param info additional error information.
     */
    public ImportException(ParsedImport importation, String info)
    {
	this.importation = importation;
	this.info = info;
    }

    /**
     * Returns a string representation of this object.
     */
    public String getMessage()
    {
	return importation.getPos().toString() + ": " + info;
    }
}
