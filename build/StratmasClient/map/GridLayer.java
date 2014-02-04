package StratmasClient.map;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;

import StratmasClient.BoundingBox;
import StratmasClient.Configuration;
import StratmasClient.object.StratmasEventListener;
import StratmasClient.object.StratmasEvent;
import StratmasClient.object.primitive.Reference;
import StratmasClient.ProcessVariableDescription;
import StratmasClient.object.primitive.Timestamp;
import StratmasClient.object.Shape;
import StratmasClient.object.SimpleShape;
import StratmasClient.object.Polygon;
import StratmasClient.object.Point;
import StratmasClient.object.Circle;
import StratmasClient.object.type.TypeFactory;
import StratmasClient.object.StratmasObjectFactory;
import StratmasClient.object.Line;
import StratmasClient.communication.GridData;
import StratmasClient.communication.LayerData;
import StratmasClient.proj.MGRSConversion;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GL;

/**
 * This class implements the grid of cells over a region defined by <code> Shape </code> objects.
 *
 * @version 1.0
 * @author Amir Filipovic 
 */
public class GridLayer implements StratmasEventListener {
    /**
     * The number of Render Selection names needed by this adapter.
     */
    protected static int NR_RENDER_SELECTION_NAMES;
    /**
     * The render selection name of the grid. This is used in RENDER_SELECTION mode.
     */
    protected int renderSelectionName = -1;
    /**
     * The values for the grid cells.
     */
    private double[] cellValues;
    /**
     * The list of cells.
     */
    private Cell[] cellInfo;
    /**
     * The reference to the color map.
     */
    private ColorMap color_map;
    /**
     * Reference to the actual region.
     */
    private Region region;
    /**
     * The reference to the actuel process variable.
     */
    private ProcessVariableDescription pvd;
    /**
     * The reference to the actual faction.
     */
    private Reference faction;
    /**
     * The list of listeners.
     */
    private Vector listeners = new Vector();
    /**
     * The actual timestamp of the grid values.
     */
    private Timestamp timestamp;
    /**
     * Reference to the map container.
     */
    private StratMap stratmap;
    /**
     * Reference to the grid information object.
     */
    private GridData gridData;
    /**
     * The display list of the grid.
     */
    private int displayList;
    /**
     * Whether the display list is updated since last redraw.
     */
    protected boolean displayListUpdated = false;
    /**
     * Indicator for the active cells in the grid. If a cell covers the actual region it's
     * value is true otherwise it's value is false. 
     */
    protected boolean[] activeCells;
    /**
     * The number of active cells in the grid that covers the displayed region..
     */
    private int nrOfActiveCells;
    /**
     * Indicator if the data values are valid.
     */
    private boolean dataValid = false;

    /**
     * Construct a grid layer of inner cells in a geographic region.
     *
     * @param stratmap the reference to the map container.
     * @param region the actual region.
     * @param gridData the grid information.
     */
    public GridLayer(StratMap stratmap, Region region, GridData gridData) {
        // set references
        this.stratmap = stratmap;
        this.region   = region;
        this.gridData = gridData;
        
        // number of RENDER_SELECTION_NAMES needed by the grid
        NR_RENDER_SELECTION_NAMES = gridData.getRows()*gridData.getCols()+1;

        // initialize the list of active cells
        activeCells = new boolean[gridData.getNrOfActiveCells()];
        
        // allocate array for cell values
        cellValues = new double[gridData.getNrOfActiveCells()];
    }

    /**
     * Sets reference to the color map.
     *
     * @param color_map the actual color map.
     */
    public void setColorMap(ColorMap color_map) {
        this.color_map = color_map;
    }
    
    /**
     * Sets the renderSelectionName of the grid.
     * 
     * @param renderSelectionName 
     */
    public void setRenderSelectionName(int renderSelectionName) {
        this.renderSelectionName = renderSelectionName;
    }
    
    /**
     * Returns the renderSelectionName of the grid.
     */
    public int getRenderSelectionName() {
        return renderSelectionName;
    }

    /**
     * Returns the number of renderSelectionNames needed for the grid.
     */
    public int getNrOfRenderSelectionNames() {
        return NR_RENDER_SELECTION_NAMES;
    }
    
    /**
     * Returns true if the given name is the render selection name of the grid.
     */
    public boolean isRenderSelectionName(int name) {
        return renderSelectionName == name;
    }
    
    /**
     * Returns true if the given name is the render selection name of any displayed 
     * cell in the grid.
     */
    public boolean isCellRenderSelectionName(int name) {
        if (cellInfo == null || cellInfo.length == 0) {
            return false;
        }
        return (name > renderSelectionName && name <= cellInfo[cellInfo.length - 1].renderSelectionName); 
    }
    
    /**
     * Updates the layer and notifies the listeners about the changes.
     *
     * @param e the event causing the changes.
     */
    public synchronized void eventOccured(StratmasEvent e) {
        // update the grid values
        if (e.getSource() instanceof LayerData) {
            // indicates that the data values are valid
            dataValid = true;
            // update actual process variable, faction and timestamp
            pvd = ((LayerData)e.getSource()).getProcessVariableDescription();
            faction = ((LayerData)e.getSource()).getFaction();
            timestamp = ((LayerData)e.getSource()).getTimestamp();
            // update the color map with the new process variable
            color_map.setProcessVariable(pvd);
            // update the color map with the new values
            color_map.updateGridData(cellValues);
            // notify all listeners
            notifyListeners(StratmasEvent.getGridUpdated(this));
        }
        // update the grid
        else if (e.isRegionUpdated()) {
            // update the active cells in the grid
            updateActiveCells();
            if (dataValid && stratmap.getClient().isConnected()) {
                // update the color map with the new values
                color_map.updateGridData(cellValues);
                // notify all listeners
                notifyListeners(StratmasEvent.getGridUpdated(this));
            }
        }
    }
    
    /**
     * Updates the list of active cells in the grid.
     */
    public void updateActiveCells() {
        // set all values to false
        for (int i = 0; i < activeCells.length; i++) {
            activeCells[i] = false;
        }
        // do update
        nrOfActiveCells = 0;
        for (Enumeration e = region.getShapes().elements(); e.hasMoreElements();) {
            Shape shape = (Shape)e.nextElement();
            // get list of simle shapes
            Vector simpleShapes = shape.constructSimpleShapes(new Vector());
            for (Enumeration en = simpleShapes.elements(); en.hasMoreElements();) {
                SimpleShape ssh = (SimpleShape)en.nextElement();
                int[] indices = gridData.getCellsForRegion(ssh.getReference());
                if (indices != null) {
                    for (int j = 0; j < indices.length; j++) {
                        if (activeCells[indices[j]] == false) {
                            activeCells[indices[j]] = true;
                            nrOfActiveCells++;
                        }
                    }
                }
            }
        }
        // update the information about the displayed cells        
        cellInfo = new Cell[nrOfActiveCells];
        int activeCellCounter = 0;
        int displayedCellCounter = 0;
        for (int i = 0; i < gridData.getActiveCells().length; i++) {
            // only if all active region cells are not drawn
            if (displayedCellCounter < nrOfActiveCells) {
                boolean activeInGrid   = ((int)(gridData.getActiveCells()[i]) == 1);
                boolean activeInRegion = activeCells[activeCellCounter];
                // update the counter
                activeCellCounter = (activeInGrid)? activeCellCounter + 1 : activeCellCounter;
                // active cells only
                if (activeInGrid && activeInRegion) {
                    Cell cell = new Cell();
                    cell.cellPos = i;
                    cell.activeCellPos = activeCellCounter - 1;
                    cell.renderSelectionName = getRenderSelectionName() + 1 + displayedCellCounter;
                    cellInfo[displayedCellCounter] = cell;
                    displayedCellCounter++;
                }
            }
        }
    }

    /**
     * Returns the average value of all the cells that cover the shape.
     *
     * @param shape the actual shape.
     *
     * @return the avearge value for the cells.
     */
    public double getAverageValueForCells(Shape shape) {
        // set all values to false
        boolean[] tmpActiveCells = new boolean[activeCells.length];
        for (int i = 0; i < tmpActiveCells.length; i++) {
            tmpActiveCells[i] = false;
        }
        double value  = 0;
        int nrOfCells = 0;
        // get list of simle shapes
        Vector simpleShapes = shape.constructSimpleShapes(new Vector());
        for (Enumeration en = simpleShapes.elements(); en.hasMoreElements();) {
            SimpleShape ssh = (SimpleShape)en.nextElement();
            int[] indices = gridData.getCellsForRegion(ssh.getReference());
            if (indices != null) {
                for (int j = 0; j < indices.length; j++) {
                    if (tmpActiveCells[indices[j]] == false) {
                        tmpActiveCells[indices[j]] = true;
                        value += cellValues[indices[j]];
                        nrOfCells++;
                    }
                }
            }
        }
        return (nrOfCells > 0)? value / nrOfCells : 0;
    }
    
    /**
     * Adds stratmas listener to the list of listeners.
     *
     * @param listener the stratmas listener.
     */
    public void addListener(StratmasEventListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Notifies all listeners that a stratmas event has occured.
     *
     * @param se the stratmas event.
     */
    public void notifyListeners(StratmasEvent se) {
        for(int i = 0; i < listeners.size(); i++) {
            ((StratmasEventListener)listeners.get(i)).eventOccured(se);
        }
    }
    
    /**
     * Remove all elements.
     */
    public void remove() {
        listeners.removeAllElements();
        
    }
    
    /**
     * Resets the grid layer.
     */
    public void reset() {
        dataValid = false;
        for (int i = 0; i < cellValues.length; i++) {
            cellValues[i] = 0;
        }
        color_map.reset();
    }
    
    /**
     * Returns the actual process variable.
     */
    public ProcessVariableDescription getProcessVariable() {
        return pvd;
    }
    
    /**
     * Returns the actual faction.
     */
    public Reference getFaction() {
        return faction;
    }
    
    /**
     * Returns the actual timestamp.
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }
    
    /**
     * Returns the cell values.
     *
     * @return values for grid cells.
     */
    public double[] getCellValues() {
        return cellValues;
    }

    /**
     * Returns the number of active cells in the grid.
     */
    public int getNrOfActiveCells() {
        return nrOfActiveCells;
    }

    /**
     * Returns the list of displayed cells.
     */
    public Cell[] getCells() {
        return cellInfo;
    }
    
    /**
     * Returns the cell with the given renderSelectionName.
     *
     * @param renderSelectionName the renderSelectionName.
     *
     * @return the cell with the given renderSelectionName.
     */
    public Cell getCell(int renderSelectionName) {
        int i = 0;
        while (i < cellInfo.length) {
            if (cellInfo[i].renderSelectionName == renderSelectionName) {
                return cellInfo[i];
            }
            i++;
        }
        return null;
    }
    
    /**
     * Returns a Shape for the cell.
     *
     * @param cell the given cell.
     *
     * @return the Shape of the cell.
     */
    public Shape getCellShape(Cell cell) {
        // get cell positions
        double[] pos = gridData.getCellPositions();
        // get row and column of the cell
        int row_nr = gridData.getRows();
        int col_nr = gridData.getCols();
        int row = cell.cellPos/col_nr;
        int col = cell.cellPos%col_nr;
        Point upper_left  = StratmasObjectFactory.createPoint("A", pos[2*(row*(col_nr+1)+col)], pos[2*(row*(col_nr+1)+col)+1]);
        Point lower_left  = StratmasObjectFactory.createPoint("B", pos[2*((row+1)*(col_nr+1)+col)], pos[2*((row+1)*(col_nr+1)+col)+1]);
        Point upper_right = StratmasObjectFactory.createPoint("C", pos[2*(row*(col_nr+1)+col+1)], pos[2*(row*(col_nr+1)+col+1)+1]);
        Point lower_right = StratmasObjectFactory.createPoint("D", pos[2*((row+1)*(col_nr+1)+col+1)], pos[2*((row+1)*(col_nr+1)+col+1)+1]);

        // create lines
        Vector lines = new Vector();
        lines.add(StratmasObjectFactory.createLine("0", upper_left, 
                                                   (Point) StratmasObjectFactory.cloneObject(upper_right)));
        lines.add(StratmasObjectFactory.createLine("1", upper_right, 
                                                   (Point) StratmasObjectFactory.cloneObject(lower_right)));
        lines.add(StratmasObjectFactory.createLine("2", lower_right, 
                                                   (Point) StratmasObjectFactory.cloneObject(lower_left)));
        lines.add(StratmasObjectFactory.createLine("3", lower_left, 
                                                   (Point) StratmasObjectFactory.cloneObject(upper_left)));
        // create the identifier for the shape
        double lon = (upper_right.getLon() + lower_left.getLon()) / 2;
        double lat = (upper_right.getLat() + lower_left.getLat()) / 2;
        String id;
        if (Configuration.getCoordinateSystem() == Configuration.MGRS) {
            String mgrs = MGRSConversion.convertGeodeticToMGRS(Math.toRadians(lon), Math.toRadians(lat), 5);
            id = new String("Cell area located at MGRS = " + mgrs); 
        }
        else {
            DecimalFormat resultFormat = new DecimalFormat("0.00");
            String lats = resultFormat.format(lat);
            String lons = resultFormat.format(lon);
            id = new String("cell at lat = " + lats + MapConstants.DEGREE_SYMBOL + ", lon = " + lons + MapConstants.DEGREE_SYMBOL);
        }
        // return the polygonial shape
        return StratmasObjectFactory.createPolygon(id, lines);
    }
    
    /**
     * Returns the circle located att the center of the cell with small radius.
     * This method is used for communication with the server.
     *
     * @param cell the cell in the grid.
     *
     * @return the circle representing the cell.
     */
    public Shape getCircularCellRepresentation(Cell cell) {
        // get the bounding box of the cell shape
        Shape s = getCellShape(cell);
        BoundingBox box = s.createBoundingBox();
        double lonCenter = (box.getEastLon()+box.getWestLon())/2;
        double latCenter = (box.getSouthLat()+box.getNorthLat())/2;        
        // get the circle
        return StratmasObjectFactory.createCircle(s.getIdentifier(), latCenter, lonCenter, 0.000001d);
    }

    /**
     * Updates the display list that draws the entire grid.
     *
     * @param proj the actual projection.
     * @param gld the gl drawable targeted.
     */
    protected void updateDisplayList(Projection proj, GLAutoDrawable gld) {
        GL gl = gld.getGL();
        // update the display list
         displayList = (gl.glIsList(displayList)) ? displayList : gl.glGenLists(1);
        gl.glNewList(displayList, GL.GL_COMPILE);
        // shape display list
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPushMatrix();
        // Pushes the name for RenderSelection mode.        
        gl.glPushName(getRenderSelectionName());
        
        // draw the cells of the grid
        // the counter of the active cells that cover the actual region
        int validCellCounter = 0;
        // the counter of all the active cells in the grid  
        int activeCellCounter = 0;
        // get cell values
        int[] grid_values = color_map.getScaledPV();
        // get the actual color table
        float[][] color_table = color_map.getColorTable();
        if (grid_values != null) {
            // get number of rows and columns
            int row_nr = gridData.getRows();
            int col_nr = gridData.getCols();
            // get position for all the cells
            double[] pos = gridData.getCellPositions();
            // for each cell
            byte[] ind = gridData.getActiveCells();
            // for each cell in the grid
            for (int ii = 0; ii < cellInfo.length; ii++) { 
                // get row and column of the cell
                int row = cellInfo[ii].cellPos/col_nr;
                int col = cellInfo[ii].cellPos%col_nr;
                // get the points
                double[] upper_left  = proj.projToXY(pos[2*(row*(col_nr+1)+col)+1], 
                                                     pos[2*(row*(col_nr+1)+col)]);
                double[] lower_left  = proj.projToXY(pos[2*((row+1)*(col_nr+1)+col)+1], 
                                                     pos[2*((row+1)*(col_nr+1)+col)]);
                double[] upper_right = proj.projToXY(pos[2*(row*(col_nr+1)+col+1)+1], 
                                                     pos[2*(row*(col_nr+1)+col+1)]);
                double[] lower_right = proj.projToXY(pos[2*((row+1)*(col_nr+1)+col+1)+1], 
                                                     pos[2*((row+1)*(col_nr+1)+col+1)]);
                // draw the cell
                gl.glMatrixMode(GL.GL_MODELVIEW);
                gl.glPushMatrix();
                gl.glPushName(cellInfo[ii].renderSelectionName);
                int col_ind = grid_values[cellInfo[ii].activeCellPos];
                gl.glColor3f(color_table[col_ind][0], color_table[col_ind][1], color_table[col_ind][2]); 
                gl.glBegin(GL.GL_POLYGON);
                gl.glVertex2d(upper_left[0], upper_left[1]);
                gl.glVertex2d(lower_left[0], lower_left[1]);
                gl.glVertex2d(lower_right[0], lower_right[1]);
                gl.glVertex2d(upper_right[0], upper_right[1]);
                gl.glEnd();
                gl.glPopName();
                gl.glMatrixMode(GL.GL_MODELVIEW);
                gl.glPopMatrix();
            }
        }
        gl.glPopName();
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPopMatrix();
        gl.glEndList();
        displayListUpdated = true;
    }

    /**
     * Invalidates the display list.
     */
    public void invalidateDisplayList() {
        displayListUpdated = false;
    }
    
    /**
     * Returns true if the display list is updated.
     */
    public boolean isDisplayListUpdated() {
        return displayListUpdated;
    }

    /**
     * Returns the display list of this grid.
     */   
    public int getDisplayList() {
        return displayList;
    }
}


/**
 * Necessary information for each displayed cell.
 */
class Cell {
    /**
     * The cell position in the grid array.
     */
    int cellPos;
    /**
     * The cell position in the array of active cells.
     */ 
    int activeCellPos;
    /**
     * The render selection name.
     */
    int renderSelectionName;
}
