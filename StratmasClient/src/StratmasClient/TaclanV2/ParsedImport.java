//         $Id: ParsedImport.java,v 1.4 2005/03/30 22:22:24 dah Exp $
/*
 * @(#)ParsedImport.java
 */

package StratmasClient.TaclanV2;
import java.util.Enumeration;
import java.util.Vector;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * An object representing an importation in the Taclan V2 language.
 * It is responsible to deliver a ParsedDeclaration or
 * ParsedDeclarationList based on the resource given in the
 * constructor.
 *
 * @version 1, $Date: 2005/03/30 22:22:24 $
 * @author  Daniel Ahlin
*/

public class ParsedImport extends ParsedObject
{
    /**
     * The factory creating handler objects.
     */
    static ImportHandlerFactory handlerFactory = new ImportHandlerFactory();
    
    /**
     * The location of the importation.
     */
    String location;

    /**
     *@param pos where the declaration were made.
     *@param location the location of the importation
    */
    public ParsedImport(SourcePosition pos, String location)
        throws SemanticException
    {
        super(pos);
        this.location = location;
    }
    
    /**
     * Returns the ParsedDeclaration with the specified reference.
     * @param reference the reference targeted declaration.
     */
    public ParsedDeclaration getParsedDeclaration(ParsedReference reference) throws ImportException
    {
        ImportHandler handler = getImportHandler();
        try {
            return handler.getParsedDeclaration(reference);
        } 
        catch (ImportHandlerException e) {
            throw new ImportException(this, e.getMessage());
        }
    }

    /**
     * Returns a ParsedDeclarationList containing all declarations
     */
    public ParsedDeclarationList getParsedDeclarationList() throws ImportException
    {
        ImportHandler handler = getImportHandler();
        try {
            return handler.getParsedDeclarationList();
        } 
        catch (ImportHandlerException e) {
            throw new ImportException(this, e.getMessage());
        }
    }

    protected ImportHandler getImportHandler() throws ImportException
    {
        try {
            return this.handlerFactory.getHandler(this.location);
        } 
        catch (NoImportHandlerException e) {
            // Befor giving up, try once more with the location made
            // relative to the source files location (if possible);
            try {
                String relativePath = getRelativePath();
                if (relativePath != null) {
                    return this.handlerFactory.getHandler(relativePath);
                } else {
                    // Did not work, giving up. Throw first error.
                    throw new ImportException(this, e.getMessage());
                }
            }
            catch (NoImportHandlerException e2) {
                // Did not work, giving up. Throw first error.
                throw new ImportException(this, e.getMessage());
            }
        }
    }

    /**
     * Tries to make and return the location of the target for the
     * import relative to the location where the import statement was
     * made. Returns null on failure.
     */
    protected String getRelativePath()
    {
        if (getPos() == null || getPos().getSource() == null)  {
            return null;
        }

        String sourceLocation = getPos().getSource();
        File pwd = new File(sourceLocation).getAbsoluteFile().getParentFile();
        if (pwd != null) {
            return new File(pwd, this.location).getPath();
        } else {
            return null;
        }
    }
}

/**
 * A factory used to handle different import formats.
 *
 * @version 1, $Date: 2005/03/30 22:22:24 $
 * @author  Daniel Ahlin
*/

class ImportHandlerFactory
{
    static Class[] handlers = null;

    public ImportHandlerFactory()
    {
        createHandlers();
    }

    synchronized protected void createHandlers()
    {
        if (handlers == null) {
            try {
                handlers = new Class[] {
                    Class.forName(TaclanV2ImportHandler.class.getName()), 
                    Class.forName(ESRIImportHandler.class.getName())};
            } 
            catch (ClassNotFoundException e) {
                // This means that classes present at compile time has
                // been removed.
                throw new AssertionError(e.getMessage());
            }
        }
    }

    /**
     * Returns the first availiable handler stating that it is capable
     * of handling the location.
     */    
    public ImportHandler getHandler(String location) throws NoImportHandlerException
    {
        Vector errors = new Vector();
        for (int i = 0; i < handlers.length; i++) {
            Class handler = this.handlers[i];
            try {
                Constructor constructor = 
                    handler.getConstructor(new Class[] {location.getClass()});
                ImportHandler handlerInstance = 
                    (ImportHandler) constructor.newInstance(((Object[]) new String[] {location}));
                if (handlerInstance.canHandle()) {
                    return handlerInstance;
                }
            }
            catch (InstantiationException e) {
                // This means that a abstract class or an interface
                // has been registred as a handler. Since, at present,
                // the handlers are static, this means an
                // implementation error. Hence the assertion.
                throw new AssertionError(e);
            }
            catch (IllegalAccessException e) {
                // This should not happen in this code.
                throw new AssertionError(e);
            }
            catch (NoSuchMethodException e) {
                // This should not happen since this is checked at
                // compile time and in createHandlers();
                throw new AssertionError(e);
            }
            catch (InvocationTargetException e) {
                // Apart from runtime exceptions this can only be an
                // ImportHandlerException.
                Throwable cause = e.getCause();
                if (cause instanceof ImportHandlerException) {
                    errors.add(0, cause);
                } 
                else if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } 
                else {
                    // Not conforming to constraints.
                    throw new AssertionError(cause);
                }
            }
            catch (ImportHandlerException e) {
                errors.add(0, e);
            }            
        }
        // Being here indicates that no handler could handle the location.
        throw new NoImportHandlerException(location, errors);        
    }
}
