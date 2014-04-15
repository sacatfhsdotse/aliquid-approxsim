package StratmasClient.proj;

/**
 * This class converts geodetic values (longitude, latitude) into UPS coordinates and vice versa.
 */
public class UPSConversion {
    //Necessary parameters
    public static double MAX_LAT = ((Math.PI*90)/180.0);        /* 90 degrees in radians */
    public static double MAX_ORIGIN_LAT = ((81.114528*Math.PI)/180.0);
    public static double MIN_NORTH_LAT = (83.5*Math.PI/180.0);
    public static double MIN_SOUTH_LAT = (-79.5*Math.PI/180.0);
    public static double MIN_EAST_NORTH = 0;
    public static double MAX_EAST_NORTH = 4000000;

    // Ellipsoid Parameters, default to WGS 84 
    public static double ups_false_easting = 2000000;
    public static double ups_false_northing = 2000000;
    public static double ups_origin_latitude = MAX_ORIGIN_LAT;  /*set default = North Hemisphere */
    public static double ups_origin_longitude = 0.0;
    public static double false_easting = 0.0;
    public static double false_northing = 0.0;
    public static double ups_easting = 0.0;
    public static double ups_northing = 0.0;


    /**
     * This method converts geodetic (latitude and longitude) coordinates to 
     * UPS (hemisphere, easting, and northing) coordinates, according to the 
     * current ellipsoid parameters.
     *
     * @param latitude  latitude in radians.
     * @param longitude longitude in radians.
     *
     * @return UPS coordinate.
     */
    public static UPSCoordinate convertGeodeticToUPS (double longitude, double latitude) {
        char hemisphere;

        if (latitude < -MAX_LAT || latitude > MAX_LAT) {
            ProjectionErrorHandler.handleError(ProjectionErrorHandler.LAT_ERROR);
            return null;
        }
        else if (latitude < 0 && latitude > MIN_SOUTH_LAT) {
            ProjectionErrorHandler.handleError(ProjectionErrorHandler.LAT_ERROR);
            return null;
        }
        else if (latitude >= 0 && latitude < MIN_NORTH_LAT) {
            ProjectionErrorHandler.handleError(ProjectionErrorHandler.LAT_ERROR);
            return null;
        }
        else if (longitude < -Math.PI || longitude > (2 * Math.PI)) {
            ProjectionErrorHandler.handleError(ProjectionErrorHandler.LON_ERROR);
            return null;
        }
        
        if (latitude < 0) {
            ups_origin_latitude = -MAX_ORIGIN_LAT; 
            hemisphere = 'S';
        }
        else {
            ups_origin_latitude = MAX_ORIGIN_LAT; 
            hemisphere = 'N';
        }
        
        boolean succ = PolarStereographicProjection.setPolarStereographicParameters(ups_origin_latitude,
                                                                                    ups_origin_longitude,
                                                                                    false_easting,
                                                                                    false_northing);
        double[] east_north = PolarStereographicProjection.convertGeodeticToPolarStereographic(longitude, latitude);
        if (succ && east_north != null) {
            ups_easting = ups_false_easting + east_north[0];
            ups_northing = ups_false_northing + east_north[1];
            //
            return new UPSCoordinate(hemisphere, ups_easting, ups_northing);
        }
        //
        return null;
    }
    
    /**
     * This method converts ups (hemisphere, easting, and northing) coordinates to geodetic 
     * (latitude and longitude) coordinates according to the current ellipsoid parameters.
     *
     * @param upsc UPS coordinate.
     *
     * @return [longitude, latitude] in radians. 
     */
    public static double[] convertUPSToGeodetic(UPSCoordinate upsc) {
        
        char hemisphere = upsc.getHemisphere();
        double easting = upsc.getEasting();
        double northing = upsc.getNorthing();
        
        if (hemisphere != 'N' && hemisphere != 'S') {
            ProjectionErrorHandler.handleError(ProjectionErrorHandler.HEMISPHERE_ERROR);
            return null;
        }
        else if (easting < MIN_EAST_NORTH || easting > MAX_EAST_NORTH) {
            ProjectionErrorHandler.handleError(ProjectionErrorHandler.EASTING_ERROR);
            return null;
        }
        else if (northing < MIN_EAST_NORTH || northing > MAX_EAST_NORTH) {
            ProjectionErrorHandler.handleError(ProjectionErrorHandler.NORTHING_ERROR);
            return null;
        }

        ups_origin_latitude = (hemisphere =='N')? MAX_ORIGIN_LAT : -MAX_ORIGIN_LAT;

        boolean succ = PolarStereographicProjection.setPolarStereographicParameters(ups_origin_latitude,
                                                                                    ups_origin_longitude,
                                                                                    ups_false_easting,
                                                                                    ups_false_northing);
        double[] lon_lat = PolarStereographicProjection.convertPolarStereographicToGeodetic(easting, northing);

        if (succ && lon_lat != null) {
            double latitude =  lon_lat[1];
            if (latitude < 0 && latitude > MIN_SOUTH_LAT) {
                ProjectionErrorHandler.handleError(ProjectionErrorHandler.LAT_ERROR);
                return null;
            }
            else if (latitude >= 0 && latitude < MIN_NORTH_LAT) {
                ProjectionErrorHandler.handleError(ProjectionErrorHandler.LAT_ERROR);
                return null;
            }
            //
            return lon_lat;
        }
        //
        return null;
    }
    
}
