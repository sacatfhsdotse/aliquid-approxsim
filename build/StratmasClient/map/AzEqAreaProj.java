package StratmasClient.map;

import java.lang.Math;

import StratmasClient.BoundingBox;

/**
 * Azimuthal Equal Area Projection is implemented in this class.
 *
 * @version 1.0
 * @author Amir Filipovic 
 */
public class AzEqAreaProj extends Projection {
    
    /**
     * Default constructor.
     */
    public AzEqAreaProj() {
        super();
        
        // set name of the projection
        proj_name = "Azimuthal Equal Area";
    }
    
    /**
     * Create projection of an area.
     *
     * @param box bounding box of the area..
     */
    public AzEqAreaProj(BoundingBox box) {
        super(box);
        
        // set name of the projection
        proj_name = "Azimuthal Equal Area";
    }

    /**
     * Create projection of an area.
     *
     * @param lon_min minimum longitude.
     * @param lat_min minimum latitude.
     * @param lon_max maximum longitude.
     * @param lat_max maximum latitude.
     */
    public AzEqAreaProj(double lon_min, double lat_min, double lon_max, double lat_max) {
        super(lon_min, lat_min, lon_max, lat_max);
        
        // set name of the projection
        proj_name = "Azimuthal Equal Area";
    }
    
    /**
     * Create projection of an area.
     *
     * @param bounds longitude and latitude bounds of the projected area ie.
     *               [min longitude, min latitude, max longitude, max latitude].  
     */
    public AzEqAreaProj(double[] bounds) {
        super(bounds);
        
        // set name of the projection
        proj_name = "Azimuthal Equal Area";
    }
    
    /**
     * Converts (lon,lat) values to (x,y) coordinates.
     *
     * @param lon longitude value.
     * @param lat latitude value.
     *
     * @return projected coordinates ie. [x, y].
     */ 
    public double[] projToXY(double lon, double lat) {
        double[] xy_coord = new double[2];
        double phi1 = Math.toRadians(lat_cen);
        double lam0 = Math.toRadians(lon_cen);
        double sin_phi1 = Math.sin(phi1);
        double cos_phi1 = Math.cos(phi1);
        double lam = Math.toRadians(lon);
        double phi = Math.toRadians(lat);
        double sin_phi = Math.sin(phi);
        double cos_phi = Math.cos(phi);
        double cos_lam = Math.cos(lam-lam0);
        double sin_lam = Math.sin(lam-lam0);
        double kprim = Math.sqrt(2.0/(1.0+sin_phi1*sin_phi+
                                          cos_phi1*cos_phi*cos_lam));
        xy_coord[0] = GeoMath.R*kprim*cos_phi*sin_lam;
        xy_coord[1] = GeoMath.R*kprim*(cos_phi1*sin_phi-sin_phi1*cos_phi*cos_lam);
        return xy_coord;
    }
    
    /**
     * Converts (x,y) values to (lon,lat) coordinates.
     *
     * @param x projected x coordinate.
     * @param y projected y coordinate.
     *
     * @return [longitude, latitude].
     */  
    public double[] projToLonLat(double x, double y) {
        double[] lon_lat = new double[2];        
        double phi1 = Math.toRadians(lat_cen);
        double lam0 = Math.toRadians(lon_cen);
        double sin_phi1 = Math.sin(phi1);
        double cos_phi1 = Math.cos(phi1);
        double ro = Math.sqrt(x*x+y*y);
        double tmp = 0.5*ro/GeoMath.R;
        // double c = 2.0*Math.asin(0.5*ro/GeoMath.R);
        double c = 2.0*Math.asin((tmp <= -1)? -0.99 : ((tmp >= 1)? 0.99 : tmp));
        double sinc = Math.sin(c);
        double cosc = Math.cos(c);
        double denom = ro*cos_phi1*cosc-y*sin_phi1*sinc;
        double phi = Math.asin(cosc*sin_phi1+y*sinc*cos_phi1/ro);
        double lam = lam0+Math.atan(x*sinc/denom);
        lon_lat[0] = Math.toDegrees(lam);
        lon_lat[1] = Math.toDegrees(phi);
        // check for sign change
        if (denom < 0 && lon_lat[0] > lam0) {
            lon_lat[0] -= 180; 
        } 
        else if (denom < 0 && lon_lat[0] < lam0) {
            lon_lat[0] += 180; 
        } 
        // correct latitude if necessary
        if (lon_lat[1] > 90.0)
            lon_lat[1] = 90.0;
        else if (lon_lat[1] < -90.0)
            lon_lat[1] = -90.0;
        return lon_lat;
    }
    
}
