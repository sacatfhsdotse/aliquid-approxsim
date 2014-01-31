// 	$Id: ServerException.java,v 1.1 2005/10/28 18:47:52 dah Exp $
/*
 * @(#)ServerException.java
 */

package StratmasClient.communication;

/**
 * An object representing any error in a client-server exchange
 *
 * @version 1, $Date: 2005/10/28 18:47:52 $
 * @author  Daniel Ahlin
*/

public class ServerException extends Exception
{
    /**
     * A string describing the exception.
     */
    String description;

    /**
     * Creates a new ServerException.
     * @param description a string describing the exception.
     */
    public ServerException(String description)
    {
	this.description = description;
    }

    /**
     * Returns a message detailing the cause of the exception.
     */
    public String getMessage()
    {
	return description;
    }

    /**
     * Returns a string representation of this object.
     */
    public String toString()
    {
	return getMessage();
    }
}
