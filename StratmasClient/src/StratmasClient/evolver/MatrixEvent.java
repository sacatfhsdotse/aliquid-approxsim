// $Id: MatrixEvent.java,v 1.1 2005/11/02 11:18:34 dah Exp $
/*
 * @(#)MatrixEvent.java
 */

package StratmasClient.evolver;

import java.util.EventObject;

/**
 * MatrixEvent is an event representing a noteworthy state change in a matrix.
 * 
 * @version 1, $Date: 2005/11/02 11:18:34 $
 * @author Daniel Ahlin
 */
public class MatrixEvent extends EventObject {
    /**
	 * 
	 */
    private static final long serialVersionUID = -2053066901785183479L;

    /**
     * Constructs a new MatrixEvent representing the specified type.
     * 
     * @param matrix the Matrix causing the event.
     */
    MatrixEvent(EvaluationsMatrix matrix) {
        super(matrix);
    }

    /**
     * Convinience method returning the Matrix causing this event.
     */
    public EvaluationsMatrix getMatrix() {
        return (EvaluationsMatrix) getSource();
    }
}
