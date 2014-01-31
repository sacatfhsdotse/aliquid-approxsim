// 	$Id: TaclanV2ImportHandler.java,v 1.2 2005/02/12 22:30:40 dah Exp $
/*
 * @(#)TaclanV2ImportHandler.java
 */

package StratmasClient.TaclanV2;
import java.io.FileNotFoundException;

/**
 * TaclanV2ImportHandler is a class converting a Taclan 2 files
 * into ParsedDeclarations.
 *
 * TODO recursion checks.
 *
 * @version 1, $Date: 2005/02/12 22:30:40 $
 * @author  Daniel Ahlin
*/

public class TaclanV2ImportHandler extends ImportHandler
{
    Parser parser = null;
    
    /**
     * Creates an instance of the TaclanV2ImportHandler.
     *
     * @param location the string pointing out the import target.
     */
    public TaclanV2ImportHandler(String location)
    {
	super(location);
    }

    /**
     * Parses location if necessary.
     */
    protected void parse() throws ImportHandlerException
    {
	if (parser == null) {
	    try {
		this.parser = Parser.getParser(this.location);
		this.parser.doParse();
	    }
	    catch (FileNotFoundException e) {
		throw new ImportHandlerException(this, e.getMessage());
		}
	    catch (SemanticException e) {
		throw new ImportHandlerException(this, e.getMessage());
	    }
	    catch (SyntaxException e) {
		throw new ImportHandlerException(this, e.getMessage());
	    }
    	}  
    }
		      
    /**
     * Reports whether the class can handle the location.
     */
    public boolean canHandle() throws ImportHandlerException
    {
	parse();
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
	parse();
	return getParsedDeclarationList().getDeclaration(reference);
    }

    /**
     * Returns a ParsedDeclarationList containing all declarations
     */
    public ParsedDeclarationList getParsedDeclarationList() 
	throws ImportHandlerException {
	
	parse();
	try {
	    return this.parser.getParsedDeclarationList();
	}
	catch (SemanticException e) {
	    throw new ImportHandlerException(this, e.getMessage());
	}
	catch (SyntaxException e) {
	    throw new ImportHandlerException(this, e.getMessage());
	}
    }
}
