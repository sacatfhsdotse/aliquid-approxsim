//         $Id: EvaluationsMatrix.java,v 1.4 2006/03/31 16:55:51 dah Exp $
/*
 * @(#)EvaluationsMatrix.java
 */

package StratmasClient.evolver;

import StratmasClient.Debug;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Collections;
import java.util.Arrays;

import javax.swing.event.EventListenerList;

/**
 * EvaluationsMatrix is a convinience class that by listening to an
 * evolver keeps a double matrix updated with the numeric parameters
 * of the evaluations produced by the evolver.
 *
 * @version 1, $Date: 2006/03/31 16:55:51 $
 * @author  Daniel Ahlin
*/

public class EvaluationsMatrix 
{
    /**
     * The matrix.
     */
    double [][] matrix = new double[0][0];

    /**
     * A map of parameters to column indexes in the matrix
     */
    ColumnMap columnMap;

    /**
     * Creates a new matrix that listens to the provided evolver.
     *
     * @param evolver the evolver to listen to.
     */   
    public EvaluationsMatrix(Evolver evolver)
    {
        setEvolver(evolver);
    }

    /**
     * The listeners of this matrix.
     */
    EventListenerList eventListenerList = new EventListenerList();

    /**
     * Sets the evolver to listen to.
     *
     * @param evolver the evolver.
     */
    public void setEvolver(Evolver evolver)
    {
        if (evolver != null) {
            evolver.addEventListener(new DefaultEvolverEventListener()
                {
                    /**
                     * Called when the evolver adds a new evaluation.
                     *
                     * @param event the event.
                     * @param newEvaluation the new evaluation
                     */
                    public void newEvaluation(EvolverEvent event, 
                                              Evaluation newEvaluation)
                    {
                        addEvaluation(event.getEvolver(), newEvaluation);
                    }

                    /**
                     * Called when the evolver adds a new evaluator.
                     *
                     * @param event the event.
                     * @param newEvaluation the new evaluation
                     */
                    public void newEvaluator(EvolverEvent event, Evaluator evaluator)
                    {
                        evaluator.addEventListener(new EvaluatorEventListener()
                            {
                                /**
                                 * Called when evaluator is finished with the
                                 * evaluation.
                                 *
                                 * @param event the event.
                                 */
                                public void finished(EvaluatorEvent event)
                                {
                                    removePreliminaryEvaluation(event.getEvaluator());
                                    event.getEvaluator().removeEventListener(this);
                                }
                                
                                /**
                                 * Called when evaluator has a new preliminart evaluation.
                                 *
                                 * @param evaluation the preliminary evaluation.
                                 * @param event the event.
                                 */
                                public void newPreliminaryEvaluation(EvaluatorEvent event, 
                                                                     Evaluation evaluation)
                                {
                                    addPreliminaryEvaluation(event.getEvaluator(), 
                                                             evaluation);
                                }
                                
                                /**
                                 * Called when an error has occured during the
                                 * evaluation.
                                 *
                                 * @param event the event.
                                 * @param errorMessage a string describing the error.
                                 */
                                public void error(EvaluatorEvent event, String errorMessage)
                                {
                                    removePreliminaryEvaluation(event.getEvaluator());
                                    event.getEvaluator().removeEventListener(this);
                                }
                            });
                    }
                });            
        }

        setColumnMap(createColumnMap(evolver));        
    }


    /**
     * Sets the parameter to column index map
     *
     * @param map the map
     */
    void setColumnMap(ColumnMap map)
    {
        this.columnMap = map;
    }

    /**
     * Sets the parameter to column index map
     */
    ColumnMap getColumnMap()
    {
        return this.columnMap;
    }

    /**
     * Creates the parameter to column index map
     *
     * @param evolver the evolver to create the map for.
     */
    ColumnMap createColumnMap(Evolver evolver)
    {
        ColumnMap map = new ColumnMap();
        int index = 0;
        
        for (Enumeration e = evolver.getParameters().elements(); 
             e.hasMoreElements();) {
            Parameter parameter = (Parameter) e.nextElement();
            // Need both comparator and metric to use this parameter.
            if (parameter.getComparator() != null && 
                parameter.getMetric() != null) {
                map.set(index++, parameter);
            }
        }

        // Need both comparator and metric to use this parameter.        
        if (evolver.getEvaluationParameter().getComparator() != null &&
            evolver.getEvaluationParameter().getMetric() != null) {
            map.set(index++, evolver.getEvaluationParameter());
        }

        return map;
    }

    /**
     * Called when a new evaluation has been added by an evolver. For
     * now, lazily recreates the matrix.
     *
     * @param evolver the evolver making the evaluation.
     * @param newEvaluation the new evaluation.
     */
    void addEvaluation(Evolver evolver, Evaluation newEvaluation)
    {
        // Sort different parameterInstances into different vectors.
        Hashtable columns = new Hashtable();
        for (Enumeration e = getColumnMap().getParameters().elements();
             e.hasMoreElements();) {
            columns.put(e.nextElement(), new Vector());
        }

        Vector evaluations = evolver.getEvaluations();
        for (int i = 0; i < evaluations.size(); i++) {
            Evaluation evaluation = (Evaluation) evaluations.get(i);
            for (Enumeration p = evaluation.getParameterInstanceSet().getParameterInstances();
                 p.hasMoreElements();) {
                ParameterInstance instance = 
                    (ParameterInstance) p.nextElement();
                Vector v = (Vector) columns.get(instance.getParameter());
                if (v != null) {
                    v.add(instance);
                }
            }

            ParameterInstance instance = 
                evaluation.getEvaluation();
            Vector v = (Vector) columns.get(instance.getParameter());
            if (v != null) {
                v.add(instance);
            }
        }

        // Create map with the minimums of each parameter.
        Hashtable mins = new Hashtable();
        for (Enumeration e = columns.keys();
             e.hasMoreElements();) {
            Parameter parameter = (Parameter) e.nextElement();
            mins.put(parameter, 
                     Collections.min((Vector) columns.get(parameter), parameter.getComparator()));
        }

        // Fill in new matrix with distances to the min instance.
        double[][] newMatrix = new double[evaluations.size()][mins.size()];
        for (Enumeration e = getColumnMap().getParameters().elements();
             e.hasMoreElements();) {
            Parameter parameter = (Parameter) e.nextElement();
            Vector v = (Vector) columns.get(parameter);
            ParameterInstance minInstance = 
                (ParameterInstance) mins.get(parameter);
            int j = getColumnMap().getIndex(parameter);
            for (int i = 0; i < v.size(); i++) {
                newMatrix[i][j] = parameter.getMetric().d(minInstance, 
                                                          (ParameterInstance) v.get(i));
            }
        }

        setMatrix(newMatrix);
    }

    /**
     * Sets the matrix
     *
     * @param newMatrix the new matrix
     */
    void setMatrix(double[][] newMatrix)
    {
        this.matrix = newMatrix;
        fireMatrixUpdated();
    }

    /**
     * Returns the matrix, don't write in it!
     */
    public double[][] getMatrix()
    {
        return this.matrix;
    }

    /**
     * Called when a new preliminary evaluation has been added by an evaluator.
     *
     * @param evaluator the evaluator making the evaluation.
     * @param evaluation the new evaluation.
     */
    public void addPreliminaryEvaluation(Evaluator evaluator, Evaluation evaluation)
    {
    }

    /**
     * Called when preliminary evaluation should be removed (because
     * the evaluator has finished).
     *
     * @param evaluator the evaluator making the evaluation.
     */
    public void removePreliminaryEvaluation(Evaluator evaluator)
    {
        
    }

    /**
     * Returns a list of the listeners of this object.
     */
    protected EventListenerList getEventListenerList()
    {
        return this.eventListenerList;
    }

    /**
     * Register a new MatrixEventListener. 
     *
     * @param listener the listener to add.
     */
    public void addEventListener(MatrixEventListener listener)
    {
        this.getEventListenerList().add(MatrixEventListener.class, listener);
    }

    /**
     * Removes a MatrixEventListener. 
     *
     * @param listener the listener to remove.
     */
    public void removeEventListener(MatrixEventListener listener)
    {
        this.getEventListenerList().remove(MatrixEventListener.class, listener);
    }

    /**
     * Notifies listeners that the matrix has been changed.
     */
    public void fireMatrixUpdated()
    {
        MatrixEvent event = new MatrixEvent(this);
        
        // Guaranteed to return a non-null array
        Object[] listeners = getEventListenerList().getListenerList();
        
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            ((MatrixEventListener) listeners[i + 1]).matrixUpdated(event);
        }
    }

    /**
     * Returns a string representation of this.
     */
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        double[][] data = getMatrix();

        for (int i = 0; i < data.length; i++) {
            buffer.append("\t");
            for (int j = 0; j < data[i].length; j++) {
                buffer.append(data[i][j] + "\t");
            }
            buffer.append("\n");
        }

        return buffer.toString();
    }
}
