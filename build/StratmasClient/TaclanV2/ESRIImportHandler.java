// 	$Id: ESRIImportHandler.java,v 1.1 2005/02/03 10:20:58 dah Exp $
/*
 * @(#)ESRIImportHandler.java
 */

package StratmasClient.TaclanV2;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * ESRIImportHandler is a class converting a ESRIfileset
 * into ParsedDeclarations.
 *
 * TODO recursion checks.
 *
 * @version 1, $Date: 2005/02/03 10:20:58 $
 * @author  Daniel Ahlin
*/

public class ESRIImportHandler extends ImportHandler
{
    /**
     * The esri shapefile behind this importation.
     */
    ESRIShapefile shapeFile;

    /**
     * Creates an instance of the ESRIImportHandler.
     *
     * @param location the string pointing out the import target.
     */
    public ESRIImportHandler(String location)
    {
	super(location);
    }

    /**
     * Parses location if necessary.
     */
    protected void parse() throws ImportHandlerException
    {
	if (shapeFile == null) {
	    try {
		this.shapeFile = new ESRIShapefile(this.location);
		this.shapeFile.parse();
	    }
	    catch (FileNotFoundException e) {
		throw new ImportHandlerException(this, e.getMessage());
	    } catch (IOException e) {
		throw new ImportHandlerException(this, e.getMessage());
	    } catch (UnsupportedESRIShapeException e) {
		throw new ImportHandlerException(this, e.getMessage());
	    } catch (MalformedESRIRecordException e) {
		throw new ImportHandlerException(this, e.getMessage());
	    }
    	}
    }

    /**
     * Reports whether the class can handle the location.
     */
    public boolean canHandle() throws ImportHandlerException
    {
	this.parse();
	// If we get here its OK.
	return true; 
    }

    /**
     * Returns the ParsedDeclaration with the specified reference.
     * @param reference the reference targeted declaration.
     */
    public ParsedDeclaration getParsedDeclaration(ParsedReference reference) 
	throws ImportHandlerException 
    {
	try {
	    this.parse();
	    return this.shapeFile.getParsedDeclaration(reference);
	} catch (MalformedESRIRecordException e) {
	    throw new ImportHandlerException(this, e.getMessage());
	} catch (UnsupportedESRIShapeException e) {
	    throw new ImportHandlerException(this, e.getMessage());
	}
    }

    /**
     * Returns a ParsedDeclarationList containing all declarations
     */
    public ParsedDeclarationList getParsedDeclarationList() 
	throws ImportHandlerException
    {
	try {
	    this.parse();
	    return this.shapeFile.getParsedDeclarationList();
	} catch (MalformedESRIRecordException e) {
	    throw new ImportHandlerException(this, e.getMessage());
	} catch (UnsupportedESRIShapeException e) {
	    throw new ImportHandlerException(this, e.getMessage());
	}
    }
}


