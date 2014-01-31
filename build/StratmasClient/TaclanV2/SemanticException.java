// 	$Id: SemanticException.java,v 1.1 2005/02/03 10:20:59 dah Exp $
/*
 * @(#)SemanticException.java
 */

package StratmasClient.TaclanV2;

/**
 * An object representing a semantic error in TaclanV2
 *
 * @version 1, $Date: 2005/02/03 10:20:59 $
 * @author  Daniel Ahlin
*/

public abstract class SemanticException extends Exception
{

    public String toString() 
    {
	return getMessage();
    }

    /**
     * Make declaration the scope of any unclaimed errors
     */
    public void claimUnclaimed(ParsedDeclaration declaration)
    {
    }
}
