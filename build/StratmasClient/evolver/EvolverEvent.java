// 	$Id: EvolverEvent.java,v 1.1 2005/10/28 12:14:34 dah Exp $
/*
 * @(#)EvolverEvent.java
 */

package StratmasClient.evolver;

import java.util.EventObject;

/**
 * EvolverEvent is an event representing a noteworthy state change in
 * an evolver.
 *
 * @version 1, $Date: 2005/10/28 12:14:34 $
 * @author  Daniel Ahlin
*/
public class EvolverEvent extends EventObject
{
    /**
     * Constructs a new EvolverEvent representing the specified type.
     *
     * @param evolver the evolver causing the event.
     * @param type the type of the event.
     */
    EvolverEvent(Evolver evolver)
    {
	super(evolver);
    }

    /**
     * Convinience method returning the evolver causing this event.
     */
    public Evolver getEvolver()
    {
	return (Evolver) getSource();
    }
}
