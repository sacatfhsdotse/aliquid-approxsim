//         $Id: MatrixEventListener.java,v 1.1 2005/11/02 11:18:35 dah Exp $
/*
 * @(#)MatrixEventListener.java
 */

package StratmasClient.evolver;

import java.util.EventListener;

/**
 * Specifies the methods called on objects listening to MatrixEvents
 *
 * @version 1, $Date: 2005/11/02 11:18:35 $
 * @author  Daniel Ahlin
*/
public interface MatrixEventListener extends EventListener
{
    /**
     * Called when matrix is updated.
     *
     * @param event the event.
     */
    public void matrixUpdated(MatrixEvent event);
}
