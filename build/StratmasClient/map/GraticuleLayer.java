package StratmasClient.map;

import StratmasClient.BoundingBox;

import javax.media.opengl.GL;

/**
 * The graticules which cover a region inside a bounding box are created. Density of graticules
 * can be set to 10, 5 and 1 degree(s).
 *
 * @version 1.0
 * @author Amir Filipovic 
 */
public class GraticuleLayer {
    /**
     * One degree spacing between graticules.
     */
    public static final int ONE_DEGREE = 1;    
    /**
     * Five degrees spacing between graticules.
     */
    public static final int FIVE_DEGREES = 5;
    /**
     * Ten degrees spacing between graticules.
     */
    public static final int TEN_DEGREES = 10;

    /**
     * Computes graticule lines over an area inside the bounding box.
     *
     * @param gl the gl to draw on.
     * @param box bounding box of the actual area. 
     * @param proj the projection from lat,lon to XY.
     * @param spacing spacing between graticules.
     */
    public static void drawGraticules(GL gl, BoundingBox box, Projection proj, double spacing)
    {
	// increase bounding box for the graticules
	double westLon = box.getWestLon() - (box.getEastLon() - box.getWestLon());
	double eastLon = box.getEastLon() + (box.getEastLon() - box.getWestLon());
	double southLat = box.getSouthLat() - (box.getNorthLat() - box.getSouthLat());
	double northLat = box.getNorthLat() + (box.getNorthLat() - box.getSouthLat());
	double step = 0.1d;
	
	// compute graticule bounds
	double lat_start = spacing * Math.floor(southLat / spacing);
	double lat_stop = spacing * Math.ceil(northLat / spacing);
	double lon_start = spacing * Math.floor(westLon / spacing);
	double lon_stop = spacing * Math.ceil(eastLon / spacing);
	
	// check that the graticules do not overlap
	lat_start = (lat_start < -90)? -90 : lat_start;
	lat_stop  = (lat_stop > 90)? 90 : lat_stop;
	if (lon_stop - lon_start > 359) {
	    lon_start = -180;
	    lon_stop  = 179;
	}
	
	//compute horizontal graticules
	for (double lat = lat_start; lat <= lat_stop; lat += spacing) {
	    gl.glBegin(GL.GL_LINE_STRIP);
	    gl.glVertex2dv(proj.projToXY(lon_start, lat), 0);
	    for (double lon = lon_start + step; lon <= lon_stop; lon += step) {
		gl.glVertex2dv(proj.projToXY(lon, lat), 0);  
	    }
	    gl.glVertex2dv(proj.projToXY(lon_stop, lat), 0);
	    gl.glEnd();		    
	}

	//compute vertical graticules
	for (double lon = lon_start; lon <= lon_stop; lon += spacing) {
	    gl.glBegin(GL.GL_LINE_STRIP);
	    gl.glVertex2dv(proj.projToXY(lon, lat_start), 0);
	    for (double lat = lat_start + step; lat <= lat_stop; lat += step) {
		gl.glVertex2dv(proj.projToXY(lon, lat), 0);  
	    }
	    gl.glVertex2dv(proj.projToXY(lon, lat_stop), 0);
	    gl.glEnd();
	}
    }
    
}
