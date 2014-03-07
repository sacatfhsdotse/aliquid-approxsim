//         $Id: RenderSelection.java,v 1.5 2006/04/18 13:01:16 dah Exp $

package StratmasClient.map;

import java.util.Hashtable;
import java.util.Vector;

import java.nio.IntBuffer;

/**
 * Encapsulates the information in an render selection buffer.
 *
 * @version 1, $Date: 2006/04/18 13:01:16 $
 * @author Daniel Ahlin 
 */
public class RenderSelection extends Vector
{
    /**
     * The result of the selection.
     */
    int[] selectionBuffer = null;

    /**
     * Index to top entries.
     */
    int[] topSelectionIndex = null;

    /**
     * Names of top entries.
     */
    int[] topSelectionNames = null;
    
    /**
     * Names of the second level entries.
     */
    int[] secondLevelNames = null;

    /**
     * The number of top selections
     */
    int hits = 0;
    
    /**
     * A mapping of selectionNames to Objects
     */
    Hashtable mapper = null;

    /**
     * Creates a new RenderSelection, expectes a complete selectionBuffer.
     *
     * @param hits number of hits
     * @param selectionBuffer the selected items.
     * @param mapper a mapping of selectionNames to Objects.
     */
    public RenderSelection(int hits, IntBuffer selectionBuffer, Hashtable mapper)
    {
        if (hits == 0) {
            this.hits = 0;
            this.selectionBuffer = new int[0];
            this.topSelectionIndex = this.selectionBuffer;
            this.topSelectionNames = this.selectionBuffer;
        } else  {
            this.hits = hits;
            this.mapper = mapper;
            
            this.topSelectionIndex = new int[hits];
            
            // Find out useful length of selectionBuffer and fill topEntriesIndex
            this.topSelectionIndex[0] = 0;
            for (int i = 1; i < hits; i++) {
                this.topSelectionIndex[i] = 3 + this.topSelectionIndex[i - 1] + 
                    selectionBuffer.get(this.topSelectionIndex[i - 1]);
            }
            
            this.selectionBuffer = new int[selectionBuffer.get(topSelectionIndex[hits - 1]) + 
                                           topSelectionIndex[hits - 1] + 3];
            selectionBuffer.rewind();
            selectionBuffer.get(this.selectionBuffer);
            
            //Fill topSelectionNames and secondLevelEntries
            this.topSelectionNames = new int[hits];
            this.secondLevelNames  = new int[hits];
            for(int i = 0; i < this.hits; i++) {
                if (this.selectionBuffer[topSelectionIndex[i]] != 0) {
                    topSelectionNames[i] = this.selectionBuffer[topSelectionIndex[i] + 3];
                }
                if (this.selectionBuffer[topSelectionIndex[i]] > 1) {
                    secondLevelNames[i] = this.selectionBuffer[topSelectionIndex[i] + 4];
                }
            }
        }
    }

    /**
     * Creates a new RenderSelection
     *
     * @param hits number of hits
     * @param selectionBuffer the selected items.
     */
    public RenderSelection(int hits, IntBuffer selectionBuffer)
    {
        this(hits, selectionBuffer, null);
    }

    /**
     * Creates a new empty RenderSelection
     */
    public RenderSelection()
    {
        this(0, null);
    }

    /**
     * Returns the toplevel (i. e. first pushed) selection names
     */
    public int[] getTopSelectionNames()
    {
        int[] res = new int[this.hits];
        System.arraycopy(topSelectionNames, 0, res, 0, res.length);
        return res;
    }

    /**
     * Returns the secondlevel (i. e. second pushed) selection names
     */
    public int[] getSecondLevelSelectionNames()
    {
        int[] res = new int[this.hits];
        if (res.length > 0) {
            System.arraycopy(secondLevelNames, 0, res, 0, res.length);
        }
        return res;
    }

    /**
     * Returns objects corresponding to the toplevels. 
     */
    public Vector getTopSelectionObjects()
    {
        Vector res = new Vector();
        // If no mapper provided we are not able to match
        if (mapper != null) {
            Hashtable hack = (Hashtable) mapper.clone();
            for (int i = 0; i < topSelectionNames.length; i++) {
                Object o = hack.remove(new Integer(topSelectionNames[i]));
                if (o != null) {
                    res.add(o);
                }
            }
        }

        return res;
    }

}
