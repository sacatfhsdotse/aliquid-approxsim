package StratmasClient;

import StratmasClient.map.Projection;

/**
 * This is bounding box for an area.
 * 
 * @version 1.0
 * @author Amir Filipovic
 */
public class BoundingBox {
    /**
     * Most west longitude.
     */
    private double westLon;

    /**
     * Most east longitude.
     */
    private double eastLon;

    /**
     * Most south latitude.
     */
    private double southLat;

    /**
     * Most north latitude.
     */
    private double northLat;

    /**
     * Minimum horisontal coordinate.
     */
    private double xmin = 0;

    /**
     * Maximum horisontal coordinate.
     */
    private double xmax = 0;

    /**
     * Minimum vertical coordinate.
     */
    private double ymin = 0;

    /**
     * Maximum vertical coordinate.
     */
    private double ymax = 0;

    /**
     * The actual projection.
     */
    private Projection proj;

    /**
     * Create bounding box.
     * 
     * @param westLon the most west longitude.
     * @param southLat the most south latitude.
     * @param eastLon the most east longitude.
     * @param northLat the most north latitude.
     */
    public BoundingBox(double westLon, double southLat, double eastLon,
            double northLat) {
        this.westLon = westLon;
        this.eastLon = (westLon > eastLon) ? eastLon + 360 : eastLon;
        this.southLat = southLat;
        this.northLat = (southLat > northLat) ? northLat + 180 : northLat;
    }

    /**
     * Create bounding box.
     * 
     * @param xmin the minimum projected horizontal coordinate.
     * @param ymin the minimum projected vertical coordinate.
     * @param xmax the maximum projected horizontal coordinate.
     * @param ymax the maximum projected vertical coordinate.
     * @param proj the actual projection.
     */
    public BoundingBox(double xmin, double ymin, double xmax, double ymax,
            Projection proj) {
        this.xmin = xmin;
        this.xmax = xmax;
        this.ymin = ymin;
        this.ymax = ymax;
        this.proj = proj;
        // latitude and longitude coordinates
        double[] westSouth = proj.projToLonLat(xmin, ymin);
        double[] eastNorth = proj.projToLonLat(xmax, ymax);
        this.westLon = westSouth[0];
        this.eastLon = (westLon > eastNorth[0]) ? eastNorth[0] + 360
                : eastNorth[0];
        this.southLat = westSouth[1];
        this.northLat = (southLat > eastNorth[1]) ? eastNorth[1] + 180
                : eastNorth[1];
    }

    /**
     * Returns most west longitude
     */
    public double getWestLon() {
        return westLon;
    }

    /**
     * Returns most east longitude
     */
    public double getEastLon() {
        return eastLon;
    }

    /**
     * Returns most south latitude.
     */
    public double getSouthLat() {
        return southLat;
    }

    /**
     * Returns most north latitude.
     */
    public double getNorthLat() {
        return northLat;
    }

    /**
     * Returns minumum horisontal projected coordinate.
     */
    public double getXmin() {
        if (!this.existProjection()) {
            System.err.println("Projected coordinate not valid!");
        }
        return xmin;
    }

    /**
     * Returns maximum horisontal projected coordinate.
     */
    public double getXmax() {
        if (!this.existProjection()) {
            System.err.println("Projected coordinate not valid!");
        }
        return xmax;
    }

    /**
     * Returns minumum vertical projected coordinate.
     */
    public double getYmin() {
        if (!this.existProjection()) {
            System.err.println("Projected coordinate not valid!");
        }
        return ymin;
    }

    /**
     * Returns maximum vertical projected coordinate.
     */
    public double getYmax() {
        if (!this.existProjection()) {
            System.err.println("Projected coordinate not valid!");
        }
        return ymax;
    }

    /**
     * Sets the projection and updates the projected coordinates.
     */
    public void setProjection(Projection proj) {
        this.proj = proj;
        double[] xyMin = proj.projToXY(westLon, southLat);
        double[] xyMax = proj.projToXY(eastLon, northLat);
        xmin = xyMin[0];
        ymin = xyMin[1];
        xmax = xyMax[0];
        ymax = xyMax[1];
    }

    /**
     * Returns the actual projection.
     */
    public Projection getProjection() {
        return proj;
    }

    /**
     * Returns true if the projection exists ie. the projected coordinates are valid.
     */
    public boolean existProjection() {
        return proj != null;
    }

    /**
     * Returns true if the boxes intersect, false otherwise.
     * 
     * @param proj the projection used.
     * @param bbox the bounding box.
     */
    public boolean intersects(Projection proj, BoundingBox bbox) {
        double[] thisXYMin = proj.projToXY(westLon, southLat);
        double[] thisXYMax = proj.projToXY(eastLon, northLat);
        double[] bboxXYMin = proj.projToXY(bbox.getWestLon(),
                                           bbox.getSouthLat());
        double[] bboxXYMax = proj.projToXY(bbox.getEastLon(),
                                           bbox.getNorthLat());
        if (thisXYMin[0] >= bboxXYMax[0]) {
            return false;
        } else if (thisXYMin[1] >= bboxXYMax[1]) {
            return false;
        } else if (thisXYMax[0] <= bboxXYMin[0]) {
            return false;
        } else if (thisXYMax[1] <= bboxXYMin[1]) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Enlargens this BoundingBox to include the one of other.
     * 
     * @param other the other boundingbox.
     */
    public void combine(BoundingBox other) {
        this.westLon = this.getWestLon() < other.getWestLon() ? this
                .getWestLon() : other.getWestLon();
        this.southLat = this.getSouthLat() < other.getSouthLat() ? this
                .getSouthLat() : other.getSouthLat();

        this.eastLon = this.getEastLon() > other.getEastLon() ? this
                .getEastLon() : other.getEastLon();
        this.northLat = this.getNorthLat() > other.getNorthLat() ? this
                .getNorthLat() : other.getNorthLat();
    }

    /**
     * Returns the union of the two BoundingBoxes with respect to the given projection.
     * 
     * @param box1 the first BoundingBox.
     * @param box2 the second BoundingBox.
     * @param proj the projection used.
     * @return the union of the two BoundingBoxes.
     */
    public static BoundingBox combine(BoundingBox box1, BoundingBox box2,
            Projection proj) {
        box1.setProjection(proj);
        box2.setProjection(proj);

        double xmin = box1.getXmin() < box2.getXmin() ? box1.getXmin() : box2
                .getXmin();
        double ymin = box1.getYmin() < box2.getYmin() ? box1.getYmin() : box2
                .getYmin();

        double xmax = box1.getXmax() > box2.getXmax() ? box1.getXmax() : box2
                .getXmax();
        double ymax = box1.getYmax() > box2.getYmax() ? box1.getYmax() : box2
                .getYmax();

        return new BoundingBox(xmin, ymin, xmax, ymax, proj);
    }

    /**
     * Returns a string representation of this object
     */
    public String toString() {
        return westLon + "," + southLat + ";" + eastLon + "," + northLat;
    }

    /**
     * Returns the clone of this BoundingBox.
     */
    public Object clone() {
        BoundingBox box = new BoundingBox(westLon, southLat, eastLon, northLat);
        if (this.existProjection()) {
            box.setProjection(proj);
        }
        return box;
    }

    /**
     * Return true if all the bounds of the bounding box are well defined.
     */
    public boolean isValid() {
        return (!Double.isNaN(getWestLon()) && !Double.isNaN(getEastLon())
                && !Double.isNaN(getSouthLat()) && !Double.isNaN(getNorthLat()));
    }
}
