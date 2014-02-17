//         $Id: IdConflictException.java,v 1.2 2005/05/23 07:50:41 alexius Exp $
/*
 * @(#)IdConflictException.java
 */

package StratmasClient.TaclanV2;

public class IdConflictException extends SemanticException
{
    /**
     * The first declared identifier.
     */
    ParsedIdentifier first;

    /**
     * The conflicing declared identifier.
     */
    ParsedIdentifier second;

    /**
     * Creates a new IdConflictException
     * @param first the first declaration of the identifier.
     * @param second the conflicting declared identifier.
     */
    public IdConflictException(ParsedIdentifier first, ParsedIdentifier second)
    {
        this.first = first;
        this.second = second;
    }

    public String getMessage()
    {
        return second.getPos().toString() + ": Declaration of " + second.toString() +
            " conflicts with previous declaration at " + first.getPos().toString();
    }    
}
