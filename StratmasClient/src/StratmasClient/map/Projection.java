package StratmasClient.map;

import java.lang.Math;
import java.lang.String;
import java.lang.Class;
import java.util.Vector;

import StratmasClient.BoundingBox;

import StratmasClient.object.Point;

/**
 * This is a parent class of all projection classes. 
 *
 * @version 1.0
 * @author Amir Filipovic 
 */
public abstract class Projection {
    
    /**
     * Center of the projection in degrees.
     */
    protected double lat_cen, lon_cen;
    
    /**
     * Name of the projection.
     */
    protected String proj_name;
    
    /**
     * Default projection constructor. 
     */
    public Projection() {
        // set center of the projection
        lat_cen = 0;
        lon_cen = 0;
    }
    
    /**
     * Create projection of an area.
     *
     * @param box bounding box of the area.
     */
    public Projection(BoundingBox box) {
        // set center of the projection
        lat_cen = (box.getNorthLat()+box.getSouthLat())/2.0;
        lon_cen = (box.getEastLon()+box.getWestLon())/2.0;
    }

    /**
     * Create projection of an area.
     *
     * @param lon_min minimum longitude.
     * @param lat_min minimum latitude.
     * @param lon_max maximum longitude.
     * @param lat_max maximum latitude.
     */
    public Projection(double lon_min, double lat_min, double lon_max, double lat_max) {
        // set center of the projection
        lat_cen = (lat_max+lat_min)/2.0;
        lon_cen = (lon_max+lon_min)/2.0;
    }

    /**
     * Create projection of an area.
     *
     * @param bounds longitude and latitude bounds of the projected area ie.
     *               [min longitude, min latitude, max longitude, max latitude].  
     */
    public Projection(double[] bounds) {
        // set center of the projection
        lat_cen = (bounds[3]+bounds[1])/2.0;
        lon_cen = (bounds[2]+bounds[0])/2.0;
    }

    /**
     * Converts (lon,lat) values to (x,y) coordinates.
     *
     * @param lon longitude value.
     * @param lat latitude value.
     *
     * @return projected coordinates ie. [x, y].
     */
    public abstract double[] projToXY(double lon, double lat);

    /**
     * Converts (lon,lat) values to (x,y) coordinates.
     *
     * @param lon_lat [lon1, lat1, lon2, lat2, ...].
     *
     * @return projected coordinates ie. [x1, y1, x2, y2, ...].
     */
    public double[] projToXY(double[] lon_lat) {
        double[] xy = new double[lon_lat.length];
        for (int i = 0; i < lon_lat.length; i = i+2) {
            double[] xy2 = projToXY(lon_lat[i], lon_lat[i+1]);
            xy[i] = xy2[0];
            xy[i+1] = xy2[1];
        }
        return xy;
    }

    /**
     * Returns the (x,y) coordinates for the provided point.
     *
     * @param point the point to get x, y from.
     *
     * @return projected coordinates [x, y]
     */
    public double[] projToXY(Point point) 
    {
        return projToXY(point.getLon(), point.getLat());
    }

     /**
     * Converts (x,y) values to (lon,lat) coordinates.
     *
     * @param x projected x coordinate.
     * @param y projected y coordinate.
     *
     * @return [longitude, latitude].
     */
    public abstract double[] projToLonLat(double x, double y);

    /**
     * Sets the center of the actual projection.
      *
     * @param box bounding box of the area.
     */
    public void setProjectionCenter(BoundingBox box) {
        // set center of the projection
        lat_cen = (box.getNorthLat()+box.getSouthLat())/2.0;
        lon_cen = (box.getEastLon()+box.getWestLon())/2.0;
    }
    
    /**
     * Sets the center of the actual projection.
     *
     * @param lon_min minimum longitude.
     * @param lat_min minimum latitude.
     * @param lon_max maximum longitude.
     * @param lat_max maximum latitude.
     */
    public void setProjectionCenter(double lon_min, double lat_min, double lon_max, double lat_max) {
        // set center of the projection
        lat_cen = (lat_max+lat_min)/2.0;
        lon_cen = (lon_max+lon_min)/2.0;
    }

    /**
     * Center of actual projection.
     *
     * @return center of the projection ie. [longitude, latitude].
     */
    public double[] getProjectionCenter() {
        double[] center = {lon_cen, lat_cen};
        return center;
    }

    /**
     * Name of actual projection.
     *
     * @return name of the projection.
     */
    public String getProjectionName() {
        return proj_name;
    }
            
    /**
     * Great circle distance between two projected points in m.
     *
     * @param x1 x coordinate of first point.
     * @param y1 y coordinate of first point.
     * @param x2 x coordinate of second point.
     * @param y2 y coordinate of second point.
     *
     * @return great circle distance in meters.
     */
    public double getDistanceGC(double x1, double y1, double x2, double y2) {
        // project back to lon/lat
        double[] pt1 = projToLonLat(x1, y1);
        double[] pt2 = projToLonLat(x2, y2);
        // compute distance in km
        double dist = GeoMath.distanceGC(pt1[1], pt1[0], pt2[1], pt2[0]);
        return dist;
    }
    
}
