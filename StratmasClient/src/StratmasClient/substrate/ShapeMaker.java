package StratmasClient.substrate;

import java.awt.Color;
import java.util.Vector;
import java.util.Hashtable;

import StratmasClient.map.adapter.MapDrawableAdapter;
import StratmasClient.map.adapter.MapShapeAdapter;
import StratmasClient.map.adapter.MapPointAdapter;
import StratmasClient.map.adapter.MapLineAdapter;
import StratmasClient.object.Point;

/**
 * This class implements methods used to construct, change and move shapes. 
 */
public abstract class ShapeMaker {
    /**
     * Number of shapes created. 
     */
    public static int createdShapesCounter = 0;
    /**
     * Reference to the drawing area.
     */
    protected SubstrateMapDrawer drawer;
    /**
     * The adapter of the shape which is under creation.
     */
    protected MapShapeAdapter shapeAdapter;
    /**
     * The list of the overlapping points.
     */ 
    protected Hashtable<Point, Point> overlappingPoints = new Hashtable<Point, Point>();
    /**
     * The list of line adapters.
     */
    protected Vector<MapLineAdapter> lineAdapters = new Vector<MapLineAdapter>();
    /**
     * The list of point adapters.
     */
    protected Vector<MapDrawableAdapter> pointAdapters = new Vector<MapDrawableAdapter>();
    
    /**
     * Creates new shape constructor.
     */
    public ShapeMaker(SubstrateMapDrawer drawer) {
        this.drawer = drawer;
    }

    /**
     * Creates new MapShapeAdapter.
     */
    protected abstract void createMapShapeAdapter(Color color); 
    
    /**
     * Returns the shape adapter.
     */
    protected MapShapeAdapter getShapeAdapter() {
        return shapeAdapter;
    } 
    
    /**
     * Removes all MapPointAdapter objects.
     */
    protected void removeMapPointAdapters() {
        while(!pointAdapters.isEmpty()) {
            MapPointAdapter mpa = (MapPointAdapter)pointAdapters.remove(0);
            drawer.removeMapPointAdapter(mpa);
        }
    }

    /**
     * Removes all MapLineAdapter objects.
     */
    protected void removeMapLineAdapters() {
        while(!lineAdapters.isEmpty()) {
            MapLineAdapter mla = (MapLineAdapter)lineAdapters.remove(0);
            drawer.removeMapLineAdapter(mla);
        }
    }

    /**
     * Updates the color of the shape.
     */
    protected abstract void updateShapeColor(Color color);

    /**
     * Removes all lines and points.
     */
    protected abstract void clearAll();

    /**
     * Checks if the shape is simple.
     */
    protected abstract boolean isShapeSimple();

    
    /**
     * Moves the area in the chosen direction.
     */
    public abstract void moveShape(double lon, double lat);
}
