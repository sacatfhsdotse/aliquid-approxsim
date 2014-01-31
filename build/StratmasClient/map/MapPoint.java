package StratmasClient.map;

import StratmasClient.map.Projection;
import StratmasClient.proj.MGRSConversion;

/**
 * Represents a point for use in the map.
 *
 */
public class MapPoint {
    /**
     * The latitude component of this point.
     */
    double lat;
    
    /**
     * The longitude component of this point.
     */
    double lon;
    
    /**
     * The projected longitude (x component)
     */
    double x;
    
    /**
     * The projected latitude(y component)
     */
    double y;
    
    /**
     * Actual used projection.
     */
    Projection usedProjection;

    /**
     * True if projection is valid.
     */
    boolean projectionValid = false;
    
    /**
     * Creates a new point.
     * 
     * @param lat the latitude component of this point.
     * @param lon the longitude component of this point.
     */
    public MapPoint(double lon, double lat) {
	this.lon = lon;
	this.lat = lat;
    }

    /**
     * Returns the vertical component of this point.     
     */
    public double getLat()
    {
	return this.lat;
    }

    /**
     * Returns the horizontal component of this point.     
     */
    public double getLon()
    {
    	return this.lon;
    }

    /**
     * Returns the MGRS value of this point.
     */
    public String getMGRSValue() {
	return MGRSConversion.convertGeodeticToMGRS(Math.toRadians(this.lon), 
						    Math.toRadians(this.lat), 5);
    }
    
    /**
     * Sets the latitude component of this point.     
     * @param lat the new latitude component of this point.
     */
    public void setLat(double lat)
    {
	this.lat = lat;
	invalidateProjection();
    }

    /**
     * Sets the longitude component of this point.     
     * @param lon the new longitude component of this point.
     */
    public void setLon(double lon)
    {
	this.lon = lon;
	invalidateProjection();
    }
    
    /**
     * Returns the projected horizontal component of this point.     
     */
    public double getX()
    {
	return this.x;
    }

    /**
     * Returns the projected vertical component of this point.     
     */
    public double getY()
    {
    	return this.y;
    }
    
    /**
     * Returns projected point.
     *
     * @param proj actual projection.
     */
    public MapPoint getProjectedPoint(Projection proj) {	
	if (!isProjectionValid() || usedProjection == null || 
	    !usedProjection.equals(proj)) {
	    double[] xy =  proj.projToXY(lon, lat);
	    x = xy[0];
	    y = xy[1];
	    usedProjection = proj;
	}
	return this;
    }
    
    /**
     * Invalidates the projected point
     */
    void invalidateProjection()
    {
	this.projectionValid = false;
    }
    
    /**
     * Returns true if the cached projection is valid.
     */
    boolean isProjectionValid()
    {
	return this.projectionValid;
    }
}
