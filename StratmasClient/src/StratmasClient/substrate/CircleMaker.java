package ApproxsimClient.substrate;

import java.awt.Color;

import ApproxsimClient.object.ApproxsimObjectFactory;
import ApproxsimClient.object.Circle;
import ApproxsimClient.object.Point;
import ApproxsimClient.map.GeoMath;
import ApproxsimClient.map.adapter.MapShapeAdapter;
import ApproxsimClient.map.adapter.MapDrawableAdapter;

/**
 * This class implements methods used to construct and modify circles
 */
public class CircleMaker extends ShapeMaker {
    /**
     * Number of circles created.
     */
    public static int createdCircleCounter = 0;
    /**
     * The circle.
     */
    protected Circle circle;

    /**
     * Creates new CircleMaker.
     */
    public CircleMaker(SubstrateMapDrawer drawer) {
        super(drawer);
    }

    /**
     * Creates new circle.
     */
    protected void createCircle(double lon, double lat, double radius) {
        circle = (Circle) ApproxsimObjectFactory
                .createCircle("circle" + String.valueOf(createdCircleCounter++),
                              lat, lon, radius);
    }

    /**
     * Returns true if a circle is created.
     */
    public boolean isCircleCreated() {
        return circle != null;
    }

    /**
     * Sets the radius. Observe that the center is unchanged.
     * 
     * @param lon longitude of the point.
     * @param lat latitude of the point.
     */
    protected void setRadius(double lon, double lat) {
        Point center = circle.getCenter();
        // compute the radius
        double radius = GeoMath.distanceGC(center.getLat(), center.getLon(),
                                           lat, lon);
        circle.setRadius(radius);
    }

    /**
     * Creates new MapShapeAdapter.
     */
    protected void createMapShapeAdapter(Color color) {
        if (circle != null) {
            shapeAdapter = (MapShapeAdapter) MapDrawableAdapter
                    .getMapDrawableAdapter(circle);
            shapeAdapter.setShapeAreaColor(color);
            shapeAdapter.setShapeLineWidth(2.0f);
        }
    }

    /**
     * Updates the color of the shape.
     */
    protected void updateShapeColor(Color color) {
        if (shapeAdapter != null) {
            shapeAdapter.setShapeAreaColor(color);
        }
    }

    /**
     * Removes the shape adapter.
     */
    protected void clearAll() {
        shapeAdapter = null;
        circle = null;
        drawer.update();
    }

    /**
     * Checks if the polygon is simple.
     */
    protected boolean isShapeSimple() {
        return true;
    }

    /**
     * Moves the area in the chosen direction.
     * 
     * @param lon longitude of the new center of the polygons bounding box.
     * @param lat latitude of the new center of the polygons bounding box.
     */
    public void moveShape(double lon, double lat) {
        if (circle != null) {
            circle.moveTo(lon, lat);
        }
    }

}
