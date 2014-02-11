//         $Id: ImportHandler.java,v 1.1 2005/02/03 10:20:58 dah Exp $
/*
 * @(#)ImportHandler.java
 */

package StratmasClient.TaclanV2;

/**
 * An abstract class specifying necessary operations for an ImportHandler
 * in the taclan language. An ImportHandler is a class converting a
 * importable location into ParsedDeclarations.
 *
 * @version 1, $Date: 2005/02/03 10:20:58 $
 * @author  Daniel Ahlin
*/

public abstract class ImportHandler
{
    /**
     * The location of the import to handle.
     */
    String location;
    

    /**
     * Creates an instance of the ImportHandler.
     *
     * @param location the string pointing out the import target.
     */
    public ImportHandler(String location)
    {
        this.location = location;
    }

    /**
     * Reports whether the class can handle the location.
     */
    public boolean canHandle() throws ImportHandlerException
    {
        return false;
    }

    /**
     * Returns the ParsedDeclaration with the specified reference.
     * @param reference the reference targeted declaration.
     */
    public abstract ParsedDeclaration getParsedDeclaration(ParsedReference reference) throws ImportHandlerException;

    /**
     * Returns a ParsedDeclarationList containing all declarations
     */
    public abstract ParsedDeclarationList getParsedDeclarationList() throws ImportHandlerException;
}


