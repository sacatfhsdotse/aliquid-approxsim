package StratmasClient.communication;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Hashtable;
import org.apache.xerces.impl.dv.util.Base64; 
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import StratmasClient.Debug;
import StratmasClient.object.primitive.Reference;

/**
 * A storage class for data about the grid.
 *
 * @version 1, $Date: 2006/03/31 16:55:50 $
 * @author  Per Alexius
*/
public class GridData {
    /** Number of rows in the grid. */
    private int mRows;
    /** Number of columns in the grid. */
    private int mCols;
    /**
     *        Positions for each cell corner from top left of top left cell
     * to bottom right of bottom rigth cell on the form:
     *        [lat0 lng0 lat1 lng1 ...]
     */
     private double [] mCellPositions = null;
    /**
     * Byte array where 1 means active and 0 means
     * passive. Cells are ordered from top left to bottom right.
     */
    private byte [] mActiveCells = null;
    /**
     * The number of active cells in the grid.
     */
    private int nrOfActiveCells;
    /**
     * Hashtable that maps a region's Reference to an array of
     * indices of the cells that are part of that region.
     */
    private Hashtable mRegionCells = new Hashtable();
    
    /**
     * Extracts grid data from a DOM Element.
     * 
     * @param elem The element to extract data from.
     */
    public GridData(Element elem) {
        mRows = XMLHandler.getInt(elem, "numberOfRows");
        mCols = XMLHandler.getInt(elem, "numberOfCols");
        
        // Number of different corners in a grid with mRows x mCols
        // cells times 2 (both lat and lon).
        int numDoubles      = (mRows + 1) * (mCols + 1) * 2; 
        
        // Positions for each corner from top left of top left cell
        // to bottom right of bottom rigth cell on the form:
        // [lat0 lng0 lat1 lng1 ...]
        mCellPositions = new double[numDoubles]; 
        
        // Byte array where 1 means active and 0 means
        // passive. Cells are ordered from top left to bottom right.
        mActiveCells = new byte[mRows * mCols];
        
        byte [] rawData     = null;
        DataInputStream dis = null;
        
        try {
            // Read cell positions.
            rawData = Base64.decode(XMLHandler.getString(elem, "positionData"));
            dis     = new DataInputStream(new ByteArrayInputStream(rawData));
            for (int i = 0; i < numDoubles; i++) {
                mCellPositions[i] = dis.readDouble();
            }
            
            // Read active cell info.
            rawData = Base64.decode(XMLHandler.getString(elem, "activeCells"));
            dis     = new DataInputStream(new ByteArrayInputStream(rawData));
            nrOfActiveCells = 0;
            for (int i = 0; i < mRows * mCols; i++) {
                mActiveCells[i] = dis.readByte();
                if ((int)mActiveCells[i] == 1) {
                    nrOfActiveCells++;
                }
            }
            
            // Read which cells that belongs to which region
            for (Node child = elem.getFirstChild(); child != null; child = child.getNextSibling()) {
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    Element rdElem = (Element)child;
                    if (rdElem.getTagName().equals("regionData")) {
                        Reference r = Reference.getReference(XMLHandler.getFirstChildByTag(rdElem, "reference"));
                        rawData = Base64.decode(XMLHandler.getString(XMLHandler.getFirstChildByTag(rdElem, "cells")));
                        dis     = new DataInputStream(new ByteArrayInputStream(rawData));
                        int [] cells = new int[rawData.length / 4];
                        for (int i = 0; i < rawData.length / 4; i++) {
                            cells[i] = dis.readInt();
                        }
                        mRegionCells.put(r, cells);
                    }
                }
            }
            
            //                for (java.util.Enumeration en = mRegionCells.keys(); en.hasMoreElements(); ) {
            //                     Reference foo = (Reference)en.nextElement();
            //                     Debug.err.println(foo + ", " +  ((int[])mRegionCells.get(foo)).length);
            //                }
            
            //                java.io.PrintWriter pw =
            //                     new java.io.PrintWriter(new java.io.FileWriter("gridData.tmp"));
            //                for (int i = 0; i < numDoubles; i+=2) {
            //                     pw.println(mCellPositions[i] + ", " + mCellPositions[i+1]);
            //                }
            //                pw.close();
            
        } catch (EOFException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
    }

    /**
     * Accessor for the number of rows.
     * 
     * @return The number of rows in the grid.
     */
    public int getRows() {
        return mRows;
    }
    
    /**
     * Accessor for the number of columns.
     * 
     * @return The number of columns in the grid.
     */
    public int getCols() {
        return mCols;
    }
    
    /**
     * Accessor for the cell position array.
     * 
     * @return The cell position array.
     */
    public double [] getCellPositions() {
        return mCellPositions;
    }
    
    /**
     * Accessor for the active cells array.
     * 
     * @return The active cells array.
     */
    public byte [] getActiveCells() {
        return mActiveCells;
    }

    /**
     * The number of active cells in the grid.
     * 
     * @return The number of active cells in the grid.
     */
    public int getNrOfActiveCells() {
        return nrOfActiveCells;
    }
    
    /**
     * Maps a region's Reference to the array containing the indices
     * of the cells that are part of that region.
     * 
     * @param regionReference A reference to a Region.
     * @return An array containing the indices of the cells that are
     * part of the region the provided Reference refers to or null if
     * the Reference couldn't be found.
     */
    public int [] getCellsForRegion(Reference regionReference) {
        return (int[])mRegionCells.get(regionReference);
    }
}
