package StratmasClient.substrate;

import java.util.Vector;
import java.awt.Color;

import StratmasClient.object.Line;
import StratmasClient.object.Point;
import StratmasClient.object.Polygon;
import StratmasClient.object.StratmasObjectFactory;
import StratmasClient.map.adapter.MapDrawableAdapter;
import StratmasClient.map.adapter.MapShapeAdapter;

/**
 * This class implements methods used to construct and change rectangles.
 */
class RectangleMaker extends ShapeMaker {
    /**
     * Number of rectangles created.
     */
    public static int createdRectangleCounter = 0;
    /**
     * The first line.
     */
    protected Line line1;
    /**
     * The second line.
     */
    protected Line line2;
    /**
     * The third line.
     */
    protected Line line3;
    /**
     * The fourth line.
     */
    protected Line line4;
    /**
     * The rectangle.
     */
    protected Polygon polygon;

    /**
     * Creates new rectangle.
     * 
     * @param drawer reference to the drawing area.
     */
    public RectangleMaker(SubstrateMapDrawer drawer) {
        super(drawer);
    }

    /**
     * Creates new rectangle.
     * 
     * @param drawer reference to the drawing area.
     * @param fixedLon longitude of the point which remains fixed once the rectangle is created.
     * @param fixedLat latitude of the point which remains fixed once the rectangle is created.
     */
    public RectangleMaker(SubstrateMapDrawer drawer, double fixedLon,
            double fixedLat) {
        super(drawer);
        createRectangle(fixedLon, fixedLat, fixedLon + 0.1, fixedLat + 0.1);
    }

    /**
     * Creates new rectangle.
     * 
     * @param drawer reference to the drawing area.
     * @param fixedLon longitude of the point which remains fixed once the rectangle is created.
     * @param fixedLat latitude of the point which remains fixed once the rectangle is created.
     * @param steerLon longitude of the point used to change size of the rectangle sizes.
     * @param steerLat latitude of the point used to change size of the rectangle sizes.
     */
    public RectangleMaker(SubstrateMapDrawer drawer, double fixedLon,
            double fixedLat, double steerLon, double steerLat) {
        super(drawer);
        createRectangle(fixedLon, fixedLat, steerLon, steerLat);
    }

    /**
     * Creates new rectangle.
     * 
     * @param fixedLon longitude of the point which remains fixed once the rectangle is created.
     * @param fixedLat latitude of the point which remains fixed once the rectangle is created.
     */
    protected void createRectangle(double fixedLon, double fixedLat) {
        createRectangle(fixedLon, fixedLat, fixedLon + 0.1, fixedLat + 0.1);
    }

    /**
     * Returns true if a rectangle is created.
     */
    public boolean isRectangleCreated() {
        return polygon != null;
    }

    /**
     * Creates new rectangle.
     * 
     * @param fixedLon longitude of the point which remains fixed once the rectangle is created.
     * @param fixedLat latitude of the point which remains fixed once the rectangle is created.
     * @param steerLon longitude of the point used to change size of the rectangle sizes.
     * @param steerLat latitude of the point used to change size of the rectangle sizes.
     */
    protected void createRectangle(double fixedLon, double fixedLat,
            double steerLon, double steerLat) {
        Point p1 = StratmasObjectFactory.createPoint("p1", fixedLat, fixedLon);
        Point p2 = StratmasObjectFactory.createPoint("p2", fixedLat, steerLon);
        Point p3 = StratmasObjectFactory.createPoint("p1", fixedLat, steerLon);
        Point p4 = StratmasObjectFactory.createPoint("p2", steerLat, steerLon);
        Point p5 = StratmasObjectFactory.createPoint("p1", steerLat, steerLon);
        Point p6 = StratmasObjectFactory.createPoint("p2", steerLat, fixedLon);
        Point p7 = StratmasObjectFactory.createPoint("p1", steerLat, fixedLon);
        Point p8 = StratmasObjectFactory.createPoint("p2", fixedLat, fixedLon);

        line1 = StratmasObjectFactory.createLine("0", p1, p2);
        line2 = StratmasObjectFactory.createLine("1", p3, p4);
        line3 = StratmasObjectFactory.createLine("2", p5, p6);
        line4 = StratmasObjectFactory.createLine("3", p7, p8);

        Vector lines = new Vector();
        lines.add(line1);
        lines.add(line2);
        lines.add(line3);
        lines.add(line4);

        polygon = StratmasObjectFactory
                .createPolygon("rectangle"
                                       + String.valueOf(createdRectangleCounter++),
                               lines);
    }

    /**
     * Changes the steering point of the rectangle.
     * 
     * @param lon the longitude of the steering point.
     * @param lat the latitude of the steering point.
     */
    protected void setSteerPoint(double lon, double lat) {
        if (polygon != null) {
            line1.getEndPoint().setLon(lon, this);
            line2.getStartPoint().setLon(lon, this);
            line2.getEndPoint().setLat(lat, this);
            line2.getEndPoint().setLon(lon, this);
            line3.getStartPoint().setLat(lat, this);
            line3.getStartPoint().setLon(lon, this);
            line3.getEndPoint().setLat(lat, this);
            line4.getStartPoint().setLat(lat, this);
        }
    }

    /**
     * Creates new MapShapeAdapter.
     */
    protected void createMapShapeAdapter(Color color) {
        if (polygon != null) {
            shapeAdapter = (MapShapeAdapter) MapDrawableAdapter
                    .getMapDrawableAdapter(polygon);
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
        line1 = null;
        line2 = null;
        line3 = null;
        line4 = null;
        polygon = null;
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
        polygon.moveTo(lon, lat);
    }
}
