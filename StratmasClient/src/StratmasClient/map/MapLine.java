package StratmasClient.map;

import StratmasClient.map.Projection;

/**
 * Represents a ordinary (straight) line.
 */
public class MapLine {
    /**
     * The start-point of the line.
     */
    MapPoint start;

    /**
     * The end-point of the line.
     */
    MapPoint end;

    /**
     * Creates a new MapLine.
     * 
     * @param start the start-point of the line.
     * @param end the end-point of the line.
     */
    public MapLine(MapPoint start, MapPoint end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Returns the startPoint of this line.
     */
    public MapPoint getStartPoint() {
        return start;
    }

    /**
     * Returns the endPoint of this line.
     */
    public MapPoint getEndPoint() {
        return end;
    }

    /**
     * Checks if this line intersects another line. Both lines are projected with the actual projection before the test is performed.
     * 
     * @param line the line to be tested.
     * @param projection the actual projection.
     * @return true if the lines intersect, false otherwise.
     */
    public boolean intersects(MapLine line, Projection projection) {
        // first line
        double x1 = getStartPoint().getProjectedPoint(projection).getX();
        double y1 = getStartPoint().getProjectedPoint(projection).getY();
        double x2 = getEndPoint().getProjectedPoint(projection).getX();
        double y2 = getEndPoint().getProjectedPoint(projection).getY();
        // second line
        double u1 = line.getStartPoint().getProjectedPoint(projection).getX();
        double v1 = line.getStartPoint().getProjectedPoint(projection).getY();
        double u2 = line.getEndPoint().getProjectedPoint(projection).getX();
        double v2 = line.getEndPoint().getProjectedPoint(projection).getY();

        double b1 = (y2 - y1) / (x2 - x1);
        double b2 = (v2 - v1) / (u2 - u1);
        double a1 = y1 - b1 * x1;
        double a2 = v1 - b2 * u1;

        double xi = -(a1 - a2) / (b1 - b2);
        double yi = a1 + b1 * xi;

        if ((x1 - xi) * (xi - x2) >= 0 && (u1 - xi) * (xi - u2) >= 0
                && (y1 - yi) * (yi - y2) >= 0 && (v1 - yi) * (yi - v2) >= 0) {
            return true;
        } else {
            return false;
        }
    }

}
