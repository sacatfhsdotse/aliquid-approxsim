package StratmasClient.proj;

/**
 * This is implementation of the Polar Stereographic projection where (longitude/latitude) values
 * are converted into (easting/northing) values and vice versa.
 */
public class PolarStereographicProjection {

    // Polar Stereographic projection parameters
    public static double polar_origin_lat = ((Math.PI*90)/180);   /* Latitude of origin in radians */
    public static double polar_origin_lon = 0.0;                  /* Longitude of origin in radians */
    public static double polar_false_easting = 0.0;               /* False easting in meters */
    public static double polar_false_northing = 0.0;              /* False northing in meters */

    // Maximum variance for easting and northing values for WGS 84.
    static double polar_delta_easting = 12713601.0;
    static double polar_delta_northing = 12713601.0;
    
    // Flag variable 
    static double southern_hemisphere = 0; 
    
    // Ellipsoid Parameters, default to WGS 84
    static double mc = 1.0;                    
    static double tc = 1.0;
    static double e4 = 1.0033565552493;
    
    /**  
     * This method receives the ellipsoid parameters and Polar Stereograpic projection 
     * parameters as inputs, and sets the corresponding state variables.
     *
     * @param latitude_of_true_scale   latitude of true scale, in radians.
     * @param longitude_down_from_pole longitude down from pole, in radians.
     * @param false_easting            easting (X) at center of projection, in meters.
     * @param false_northing           northing (Y) at center of projection, in meters.
     *
     * @return true if no error occured, false otherwise.
     */
    public static boolean setPolarStereographicParameters (double latitude_of_true_scale,
                                                           double longitude_down_from_pole,
                                                           double false_easting,
                                                           double false_northing) {
        double inv_f = 1/ProjectionConstants.ellipsFlattening ;
        final double epsilon = 1.0e-2;
        
        // Semi-major axis must be greater than zero
        if (ProjectionConstants.semiMajorAxis <= 0.0) { 
            ProjectionErrorHandler.handleError(ProjectionErrorHandler.A_ERROR);
            return false;
        }
        // Inverse flattening must be between 250 and 350
        else if (inv_f < 250 || inv_f > 350) {
            ProjectionErrorHandler.handleError(ProjectionErrorHandler.INV_F_ERROR);
            return false;
        }
        /* Origin Latitude out of range */
        else if (latitude_of_true_scale < -Math.PI/2.0 || latitude_of_true_scale > Math.PI/2.0) { 
            ProjectionErrorHandler.handleError(ProjectionErrorHandler.LAT_ERROR);
            return false;
        }
        else if (longitude_down_from_pole < -Math.PI || longitude_down_from_pole > 2*Math.PI) {
            ProjectionErrorHandler.handleError(ProjectionErrorHandler.LON_ERROR);
            return false;
        }

        if (longitude_down_from_pole > Math.PI) {
            longitude_down_from_pole -= 2*Math.PI;
        }
        if (latitude_of_true_scale < 0) {
            southern_hemisphere = 1;
            polar_origin_lat = -latitude_of_true_scale;
            polar_origin_lon = -longitude_down_from_pole;
        }
        else {
            southern_hemisphere = 0;
            polar_origin_lat = latitude_of_true_scale;
            polar_origin_lon = longitude_down_from_pole;
        }
        polar_false_easting = false_easting;
        polar_false_northing = false_northing;
        
        if (Math.abs(Math.abs(polar_origin_lat) - Math.PI/2.0) > 1.0e-10) {
            double slat = Math.sin(polar_origin_lat);
            double essin = ProjectionConstants.eccentricity * slat;
            double pow_es = POLAR_POW(essin);
            double clat = Math.cos(polar_origin_lat);
            mc = clat/Math.sqrt(1.0 - essin * essin);
            tc = Math.tan(Math.PI/4.0-polar_origin_lat/2.0)/pow_es;
        }
        else {
            double one_plus_es = 1.0 + ProjectionConstants.eccentricity;
            double one_minus_es = 1.0 - ProjectionConstants.eccentricity;
            e4 = Math.sqrt(Math.pow(one_plus_es, one_plus_es)*Math.pow(one_minus_es, one_minus_es));
        }
  
        // Calculate Radius
        double[] east_north = convertGeodeticToPolarStereographic(polar_origin_lon, 0); 
         polar_delta_northing = Math.abs(east_north[1]) + epsilon;
        polar_delta_easting = polar_delta_northing;
        
        return true;
    }

    /**
     * Returns the latitude in radians at the origin of the projection.
     */
    public static double getOriginLatitude() {
        return polar_origin_lat;
    }
    
    /**
     * Returns the longitude in radians at the center of the projection.
     */
    public static double getOriginLongitude() {
        return polar_origin_lon;
    }
    
    /**
     * Returns the false easting in meters. 
     */
    public static double getFalseEasting() {
        return polar_false_easting;
    }
    
    /**
     * Returns the false northing in meters.
     */
    public static double getFalseNorthing() {
        return polar_false_northing;
    }
    
    /**
     * Help function.
     */
    public static double POLAR_POW(double essin) {
        return  Math.pow((1.0-essin)/(1.0+essin),  ProjectionConstants.eccentricity/2.0);
    }

    /**
     * This method converts geodetic coordinates (latitude and longitude) to Polar 
     * Stereographic coordinates (easting and northing), according to the current ellipsoid
     * and Polar Stereographic projection parameters. 
     *
     * @param latitude  latitude, in radians.
     * @param longitude longitude, in radians.
     *
     * @return [Easting (X), Northing (Y)] in meters.
     */
    public static double[] convertGeodeticToPolarStereographic(double longitude, double latitude) {
        double rho;
        
        // Latitude out of range 
        if (latitude < -Math.PI/2.0 || latitude > Math.PI/2.0) {
            ProjectionErrorHandler.handleError(ProjectionErrorHandler.LAT_ERROR);
            return null;
        }
        // Latitude and Origin Latitude in different hemispheres
        else if (latitude < 0 && southern_hemisphere == 0){
            ProjectionErrorHandler.handleError(ProjectionErrorHandler.LAT_ERROR);
            return null;
        }
        // Latitude and Origin Latitude in different hemispheres
        else if (latitude > 0 && southern_hemisphere == 1) {
            ProjectionErrorHandler.handleError(ProjectionErrorHandler.LAT_ERROR);
            return null;
        }
        // Longitude out of range
        else if (longitude < -Math.PI || longitude > 2*Math.PI) {
            ProjectionErrorHandler.handleError(ProjectionErrorHandler.LON_ERROR);
            return null;
        }

        if (Math.abs(Math.abs(latitude) - Math.PI/2.0) < 1.0e-10) {
            double[] east_north = {0, 0};
            return east_north;
        }
        else {
            if (southern_hemisphere != 0) {
                longitude *= -1.0;
                latitude *= -1.0;
            }
            double dlam = longitude - polar_origin_lon;
            if (dlam > Math.PI) {
                dlam -= 2*Math.PI;
            }
            if (dlam < -Math.PI) {
                dlam += 2*Math.PI;
            }
            
            double slat = Math.sin(latitude);
            double essin = ProjectionConstants.eccentricity * slat;
            double pow_es = POLAR_POW(essin);
            double t = Math.tan(Math.PI/4.0-latitude/2.0)/pow_es;

            if (Math.abs(Math.abs(polar_origin_lat)-Math.PI/2.0) > 1.0e-10) {
                rho = ProjectionConstants.semiMajorAxis*mc*t/tc;
            }
            else {
                rho = 2*ProjectionConstants.semiMajorAxis*t/e4;
            }
      
            double easting = rho*Math.sin(dlam)+polar_false_easting;
            double northing;
            if (southern_hemisphere != 0) {
                easting *= -1.0;
                northing = rho*Math.cos(dlam)+polar_false_northing;
            }
            else {
                northing = -rho*Math.cos(dlam)+polar_false_northing;
            }
            //
            double[] east_north = {easting, northing};
            return east_north;
        }
    }
    
    /**
     * This method converts Polar Stereographic coordinates (easting and northing) to 
     * geodetic coordinates (latitude and longitude) according to the current ellipsoid
     * and Polar Stereographic projection Parameters.
     *
     * @param easting  easting (X), in meters.
     * @param northing northing (Y), in meters.
     *
     * @return [longitude, latitude] in radians.
     */
    public static double[] convertPolarStereographicToGeodetic (double easting, double northing) {
        // Easting out of range 
        if (easting > (polar_false_easting + polar_delta_easting) ||
            easting < (polar_false_easting - polar_delta_easting)) { 
            ProjectionErrorHandler.handleError(ProjectionErrorHandler.EASTING_ERROR);
            return null;
        }
        else if (northing > (polar_false_northing + polar_delta_northing) ||
                 northing < (polar_false_northing - polar_delta_northing)) {
            ProjectionErrorHandler.handleError(ProjectionErrorHandler.NORTHING_ERROR);
            return null;
        }
        double temp = Math.sqrt(easting * easting + northing * northing);     
        // Point is outside of projection area
        if (temp > (polar_false_easting + polar_delta_easting) || 
            temp > (polar_false_northing + polar_delta_northing) ||
            temp < (polar_false_easting - polar_delta_easting) || 
            temp < (polar_false_northing - polar_delta_northing)) {
            ProjectionErrorHandler.handleError(ProjectionErrorHandler.POLAR_RADIUS_ERROR);
            return null;
        }

        double dy = northing - polar_false_northing;
        double dx = easting - polar_false_easting;
        if (dy == 0.0 && dx == 0.0) {
            double[] lon_lat = {polar_origin_lon, Math.PI/2.0};
            return lon_lat;
        }
        else {
            if (southern_hemisphere != 0) {
                dy *= -1.0;
                dx *= -1.0;
            }
            double t;
            double rho = Math.sqrt(dx * dx + dy * dy);
            if (Math.abs(Math.abs(polar_origin_lat) - Math.PI/2.0) > 1.0e-10) {
                t = rho * tc / (ProjectionConstants.semiMajorAxis*mc);
            }
            else {
                t = rho * e4 / (2*ProjectionConstants.semiMajorAxis);
            }
            double phi = Math.PI/2.0 - 2.0 * Math.atan(t);
            double tempphi = 0.0;
            while (Math.abs(phi - tempphi) > 1.0e-10) {
                tempphi = phi;
                double essin = ProjectionConstants.eccentricity * Math.sin(phi);
                double pow_es = POLAR_POW(essin);
                phi = Math.PI/2.0 - 2.0 * Math.atan(t * pow_es);
            }
            double latitude = phi;
            double longitude = polar_origin_lon + Math.atan2(dx, -dy);
            
            if (longitude > Math.PI)
                longitude -= 2*Math.PI;
            else if (longitude < -Math.PI)
                longitude += 2*Math.PI;


            if (latitude > Math.PI/2.0) {  /* force distorted values to 90, -90 degrees */
                latitude = Math.PI/2.0;
            }
            else if (latitude < -Math.PI/2.0) {
                latitude = -Math.PI/2.0;
            }

            if (longitude > Math.PI) { /* force distorted values to 180, -180 degrees */
                longitude = Math.PI;
            }
            else if (longitude < -Math.PI) {
                longitude = -Math.PI;
            }

            if (southern_hemisphere != 0) {
                latitude *= -1.0;
                longitude *= -1.0;
            }
            //    
            double[] lon_lat = {longitude, latitude};
            return lon_lat;
        }
    }

}
