package ApproxsimClient.substrate;

import java.util.Vector;

import ApproxsimClient.object.Line;
import ApproxsimClient.object.Point;
import ApproxsimClient.object.Polygon;
import ApproxsimClient.object.ApproxsimObjectFactory;

/**
 * Definition of a rectangle used in SubstrateEditor.
 */
class Rectangle {
    /**
     * The first line.
     */
    private Line line1;
    /**
     * The second line.
     */
    private Line line2;
    /**
     * The third line.
     */
    private Line line3;
    /**
     * The fourth line.
     */
    private Line line4;
    /**
     * The rectangle.
     */
    private Polygon polygon;

    /**
     * Creates new rectangle.
     * 
     * @param fixedLon longitude of the point which remains fixed once the rectangle is created.
     * @param fixedLat latitude of the point which remains fixed once the rectangle is created.
     */
    public Rectangle(double fixedLon, double fixedLat) {
        this(fixedLon, fixedLat, fixedLon + 0.1, fixedLat + 0.1);
    }

    /**
     * Creates new rectangle.
     * 
     * @param fixedLon longitude of the point which remains fixed once the rectangle is created.
     * @param fixedLat latitude of the point which remains fixed once the rectangle is created.
     * @param steerLon longitude of the point used to change size of the rectangle sizes.
     * @param steerLat latitude of the point used to change size of the rectangle sizes.
     */
    public Rectangle(double fixedLon, double fixedLat, double steerLon,
            double steerLat) {
        Point p1 = ApproxsimObjectFactory.createPoint("p1", fixedLat, fixedLon);
        Point p2 = ApproxsimObjectFactory.createPoint("p2", fixedLat, steerLon);
        Point p3 = ApproxsimObjectFactory.createPoint("p1", fixedLat, steerLon);
        Point p4 = ApproxsimObjectFactory.createPoint("p2", steerLat, steerLon);
        Point p5 = ApproxsimObjectFactory.createPoint("p1", steerLat, steerLon);
        Point p6 = ApproxsimObjectFactory.createPoint("p2", steerLat, fixedLon);
        Point p7 = ApproxsimObjectFactory.createPoint("p1", steerLat, fixedLon);
        Point p8 = ApproxsimObjectFactory.createPoint("p2", fixedLat, fixedLon);

        line1 = ApproxsimObjectFactory.createLine("line1", p1, p2);
        line2 = ApproxsimObjectFactory.createLine("line2", p3, p4);
        line3 = ApproxsimObjectFactory.createLine("line3", p5, p6);
        line4 = ApproxsimObjectFactory.createLine("line4", p7, p8);

        Vector lines = new Vector();
        lines.add(line1);
        lines.add(line2);
        lines.add(line3);
        lines.add(line4);

        polygon = ApproxsimObjectFactory.createPolygon("rectangle", lines);
    }

    /**
     * Changes the steering point of the rectangle.
     * 
     * @param lon the longitude of the steering point.
     * @param lat the latitude of the steering point.
     */
    public void setSteerPoint(double lon, double lat) {
        line1.getEndPoint().setLon(lon, this);
        line2.getStartPoint().setLon(lon, this);
        line2.getEndPoint().setLat(lat, this);
        line2.getEndPoint().setLon(lon, this);
        line3.getStartPoint().setLat(lat, this);
        line3.getStartPoint().setLon(lon, this);
        line3.getEndPoint().setLat(lat, this);
        line4.getStartPoint().setLat(lat, this);
    }

    /**
     * Returns the polygon.
     */
    public Polygon getPolygon() {
        return polygon;
    }

}
