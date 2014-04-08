//         $Id: ColumnMap.java,v 1.2 2006/03/31 16:55:51 dah Exp $
/*
 * @(#)ColumnMap.java
 */

package StratmasClient.evolver;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Arrays;

/**
 * Class maintaining a map of parameters to indexes.
 * @version 1, $Date: 2006/03/31 16:55:51 $
 * @author  Daniel Ahlin
 */
public class ColumnMap
{
    /**
     * Map from index -> parameter
     */
    Hashtable<Parameter, Integer> parameterToIndex = new Hashtable<Parameter, Integer>();

    /**
     * Map from parameter -> index
     */
    Hashtable<Integer, Parameter> indexToParameter = new Hashtable<Integer, Parameter>();

    /**
     * The current max index.
     */
    int maxIndex = -1;
    
    /**
     * Creates a new column map
     */    
    public ColumnMap()
    {
        super();
    }

    /**
     * Returns the index of the provided Parameter or -1 if none
     * exists.
     *
     * @param parameter the parameter to map
     */
    public synchronized int getIndex(Parameter parameter)
    {
        Integer i = parameterToIndex.get(parameter);
        if (i != null) {
            return i.intValue();
        } else {
            return -1;
        }
    }

    /**
     * Returns the parameter of the provided index or null if none
     * exists.
     *
     * @param index the index
     */
    public synchronized Parameter getParameter(int index)
    {
        return indexToParameter.get(new Integer(index));
    }

    /**
     * Returns the parameters of this map in the order of their
     * indexes.
     */
    public synchronized Vector<Parameter> getParameters()
    {
        int[] indices = getIndices();

        Vector<Parameter> v = new Vector<Parameter>();
        for (int i = 0; i < indices.length; i++) {
            v.add(getParameter(indices[i]));
        }

        return v;
    }

    /**
     * Returns the indices of this map in order.
     */
    public synchronized int[] getIndices()
    {
        int[] res = new int[indexToParameter.size()];
        int i = 0;
        for (Enumeration<Integer> e = indexToParameter.keys(); e.hasMoreElements();) {
            res[i++] = e.nextElement().intValue();
        }
        Arrays.sort(res);

        return res;
    }

    /**
     * Sets the index of the provided Parameter to the specified
     * value, replacing any index existing for that parameter and any
     * parameter existing for that index.
     *
     * @param parameter the parameter to map
     */
    public synchronized void set(int index, Parameter parameter)
    {
        Integer i = new Integer(index);

        // Remove any old mapping.
        Parameter oldParameter = getParameter(index);
        int oldIndex = getIndex(parameter);
        if (oldIndex != -1) {
            parameterToIndex.remove(oldParameter);
            indexToParameter.remove(new Integer(oldIndex));
        }

        // Set new mapping
        parameterToIndex.put(parameter, i);
        indexToParameter.put(i, parameter);
        if (getMaxIndex() < index) {
            setMaxIndex(index);
        }
    }

    /**
     * Returns the current max index. Note that this is not
     * necessarily the number of entries in the tables.
     */
    public synchronized int getMaxIndex()
    {
        return this.maxIndex;
    }

    /**
     * Sets the current max index.
     *
     * @param index the new max index
     */
    synchronized void setMaxIndex(int index)
    {
        this.maxIndex = index;
    }
}

