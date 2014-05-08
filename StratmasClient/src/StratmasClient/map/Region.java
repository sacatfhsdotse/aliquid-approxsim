package ApproxsimClient.map;

import java.util.Vector;
import java.util.Enumeration;
import ApproxsimClient.object.ApproxsimObject;
import ApproxsimClient.object.Shape;
import ApproxsimClient.object.SimpleShape;
import ApproxsimClient.BoundingBox;
import ApproxsimClient.object.ApproxsimEvent;
import ApproxsimClient.object.ApproxsimEventListener;

/**
 * Region defined by <code>Shape</code> objects.
 * 
 * @version 1.0
 * @author Amir Filipovic
 */
public class Region implements ApproxsimEventListener {
    /**
     * Reference to the main container.
     */
    private BasicMap basicMap;
    /**
     * Region borders (shapes).
     */
    private Vector<Shape> shapes = new Vector<Shape>();
    /**
     * Boundary box of the region in longitude and latitude coordinates.
     */
    private BoundingBox lon_lat_box;
    /**
     * List of listeners.
     */
    private Vector<ApproxsimEventListener> listeners = new Vector<ApproxsimEventListener>();

    /**
     * Creates geographical region.
     * 
     * @param basicMap container of the map components.
     * @param shape borders of the region.
     */
    public Region(BasicMap basicMap, Shape shape) {
        // set reference to the map container
        this.basicMap = basicMap;
        // set actual region
        shapes.add(shape);
        // set the region to listen to all the shapes
        listenToShape(shape);
        // get lat and long the boundary points
        lon_lat_box = shape.getBoundingBox();
    }

    /**
     * Updates the Region when a Shape is added to/removed from it.
     * 
     * @param se the event to handle.
     */
    public void eventOccured(ApproxsimEvent se) {
        // a Shape is removed from the Region
        if (se.isRemoved()) {
            shapes.remove(se.getSource());
            // update the bounding boox
            updateLonLatBounds();
            // update projection
            basicMap.setProjection(new AzEqAreaProj(lon_lat_box));
            // notify all the listeners
            notifyListeners(ApproxsimEvent.getRegionUpdated(this));
        }
        // a Shape is added to the Region
        else if (se.isObjectAdded()) {
            // notify all the listeners
            notifyListeners(ApproxsimEvent.getRegionUpdated(this));
        } else if (se.isReplaced()) {
            throw new AssertionError("Replace behavior not implemented");
        }
    }

    /**
     * Adds new approxsim listener to the list.
     */
    public void addListener(ApproxsimEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a approxsim listener from the list.
     */
    public void removeListener(ApproxsimEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Removes all approxsim listeners from the list.
     */
    public void removeAllListeners() {
        listeners.removeAllElements();
    }

    /**
     * Notifies all listeners that a approxsim event has occured.
     */
    public void notifyListeners(ApproxsimEvent se) {
        for (int i = 0; i < listeners.size(); i++) {
            ((ApproxsimEventListener) listeners.get(i)).eventOccured(se);
        }
    }

    /**
     * Removes all the elements.
     */
    public void remove() {
        // remove all listeners
        for (int i = 0; i < listeners.size(); i++) {
            // FIXME THIS SHOULD NOT BE A REMOVE EVENT!!!
            ((ApproxsimEventListener) listeners.get(i))
                    .eventOccured(ApproxsimEvent.getRemoved(this, null));
        }
        removeAllListeners();
        // remove all shapes.
        shapes.removeAllElements();
    }

    /**
     * Adds new part to the region.
     */
    public void addShape(Shape shape) {
        if (!contains(shape)) {
            shapes.add(shape);
            listenToShape(shape);
            // update the bounding box
            updateLonLatBounds();
            // update projection
            basicMap.setProjection(new AzEqAreaProj(lon_lat_box));
            // notify all listeners that the region has been updated
            notifyListeners(ApproxsimEvent.getRegionUpdated(this));
        }
    }

    /**
     * Adds a listener to the shape and all it's children.
     * 
     * @param shape the actual shape.
     */
    private void listenToShape(ApproxsimObject shape) {
        if (!shape.getIdentifier().equals("curves")) {
            shape.addEventListener(this);
            for (Enumeration<ApproxsimObject> e = shape.children(); e
                    .hasMoreElements();) {
                listenToShape(e.nextElement());
            }
        }
    }

    /**
     * Resets the region.
     * 
     * @param shape the shape contained in the Region after reseting.
     */
    public void reset(Shape shape) {
        shapes.clear();
        shapes.add(shape);
        // get lat and long the boundary points
        lon_lat_box = shape.getBoundingBox();
        // update projection
        basicMap.getProjection().setProjectionCenter(lon_lat_box);
    }

    /**
     * Resets the region.
     * 
     * @param list list of <code>Shape</code> objects contained in the Region after reseting.
     */
    public void reset(Vector<Shape> list) {
        shapes.clear();
        try {
            // add first shape in the list
            shapes.add(list.get(0));
            lon_lat_box = list.get(0).getBoundingBox();
            // add remaining shapes
            for (int i = 1; i < list.size(); i++) {
                shapes.add(list.get(i));
                lon_lat_box.combine(list.get(i).getBoundingBox());
            }
            // update projection
            basicMap.getProjection().setProjectionCenter(lon_lat_box);
        } catch (RuntimeException re) {
            re.printStackTrace();
        }
    }

    /**
     * Checks if the region contains the given shape.
     * 
     * @param shape the actual shape.
     */
    public boolean contains(Shape shape) {
        // for all shapes
        for (int i = 0; i < shapes.size(); i++) {
            Shape sh = shapes.get(i);
            Vector<SimpleShape> simple_shapes = sh
                    .constructSimpleShapes(new Vector<SimpleShape>());
            for (int j = 0; j < simple_shapes.size(); j++) {
                Shape simple_shape = simple_shapes.get(j);
                if (simple_shape.getReference().equals(shape.getReference())) {
                    return true;
                }
            }
        }
        //
        return false;
    }

    /**
     * Updates the bounding box of the Region.
     */
    private void updateLonLatBounds() {
        if (shapes.isEmpty()) {
            lon_lat_box = new BoundingBox(-1, -1, 1, 1);
        } else {
            if (shapes.size() > 0) {
                lon_lat_box = shapes.get(0).getBoundingBox();
            }
            if (shapes.size() > 1) {
                for (int i = 1; i < shapes.size(); i++) {
                    lon_lat_box.combine(shapes.get(i).getBoundingBox());
                }
            }
        }
    }

    /**
     * Inner and outer borders of the region.
     * 
     * @return <code>Shape</code> objects that describe the region.
     */
    public Vector<Shape> getShapes() {
        Vector<Shape> sShapes = new Vector<Shape>();
        // for all shapes
        for (int i = 0; i < shapes.size(); i++) {
            Shape sh = shapes.get(i);
            sShapes.addAll(sh.constructSimpleShapes(new Vector<SimpleShape>()));
        }
        return sShapes;
    }

    /**
     * Boundary box of longitude and latitude coordinates.
     * 
     * @return boundary box in lon/lat coordinates.
     */
    public BoundingBox getLonLatBounds() {
        return lon_lat_box;
    }

    /**
     * Returns the boundary box with respect to the actual projection. The actual projection is used.
     * 
     * @return boundary box with respect to the actual projection.
     */
    public BoundingBox getProjectedBounds() {
        // get actual projection
        Projection proj = basicMap.getProjection();
        if (!shapes.isEmpty()) {
            Enumeration<Shape> e = getShapes().elements();
            BoundingBox res = e.nextElement().getBoundingBox(proj);
            for (; e.hasMoreElements();) {
                res = BoundingBox.combine(res,
                                          e.nextElement().getBoundingBox(proj),
                                          proj);
            }
            return res;
        } else {
            double[] xy_min = proj.projToXY(lon_lat_box.getWestLon(),
                                            lon_lat_box.getSouthLat());
            double[] xy_max = proj.projToXY(lon_lat_box.getEastLon(),
                                            lon_lat_box.getNorthLat());
            return new BoundingBox(xy_min[0], xy_min[1], xy_max[0], xy_max[1],
                    proj);
        }
    }

}
