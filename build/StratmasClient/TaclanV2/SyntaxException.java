// 	$Id: SyntaxException.java,v 1.2 2005/03/21 10:14:47 dah Exp $
/*
 * @(#)SyntaxException.java
 */

package StratmasClient.TaclanV2;

/**
 * An object representing a syntactic error in TaclanV2
 *
 * @version 1, $Date: 2005/03/21 10:14:47 $
 * @author  Daniel Ahlin
*/

public class SyntaxException extends Exception
{
    String info;

    public SyntaxException(String info)
    {
	super();
	this.info = info;
    }

    public String toString() 
    {
	return getMessage();
    }

    
    public String getMessage()
    {
	return info;
    }
}
