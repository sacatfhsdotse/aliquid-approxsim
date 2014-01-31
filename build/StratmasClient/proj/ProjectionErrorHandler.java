package StratmasClient.proj;

/**
 * This class is used to handle the errors obtained in the coordinate conversion process.
 */
public class ProjectionErrorHandler {
    /**
     * Latitude outside of valid range (-90 to 90 degrees).
     */
    public static long LAT_ERROR = 1;
    /**
     * Longitude outside of valid range (-180 to 360 degrees and within +/-90 of Central Meridian).
     */
    public static long LON_ERROR = 2;
    /**
     * Easting outside of valid range (depending on ellipsoid and projection parameters).
     */
    public static long EASTING_ERROR = 3;
    /**
     *  Northing outside of valid range (depending on ellipsoid and projection parameters).
     */
    public static long NORTHING_ERROR = 4;
    /**
     *  Origin latitude outside of valid range (-90 to 90 degrees).
     */
    public static long ORIGIN_LAT_ERROR = 5;
    /**
     *  Central meridian outside of valid range (-180 to 360 degrees).
     */
    public static long CENT_MER_ERROR = 6;
    /**
     *  Scale factor outside of valid range (0.3 to 3.0).
     */
    public static long SCALE_FACTOR_ERROR = 7;
    /**
     *  Semi-major axis less than or equal to zero.
     */
    public static long A_ERROR = 8;
    /**
     * Inverse flattening outside of valid range (250 to 350).
     */
    public static long INV_F_ERROR = 9;
    /**
     * Distortion will result if longitude is more than 9 degrees from the Central Meridian.
     */
    public static long LON_WARNING = 10;
    /**
     *  Zone outside of valid range (1 to 60).
     */
    public static long UTM_ZONE_ERROR = 11;
    /**
     *  Invalid hemisphere ('N' or 'S').
     */
    public static long HEMISPHERE_ERROR = 12;
    /**
     *  Zone outside of valid range (1 to 60) and within 1 of 'natural' zone.
     */
    public static long UTM_ZONE_OVERRIDE_ERROR = 13;
    /**
     *  An MGRS string error: string too long, too short, or badly formed.
     */
    public static final long MGRS_STR_ERROR = 14;
    /**
     * The precision must be between 0 and 5 inclusive.
     */
    public static final long MGRS_PRECISION_ERROR = 15;
    /**
     *  Coordinates too far from pole, depending on ellipsoid and projection parameters.
     */
    public static final long POLAR_RADIUS_ERROR = 16;


    /**
     * Writes out the error string.
     */
    public static void handleError(long error) {
	if (error == LAT_ERROR) {
	    System.err.println("ERROR : Latitude outside of valid range");
	}
	else if (error == LON_ERROR) {
	    System.err.println("ERROR : Longitude outside of valid range");
	}
	else if (error == EASTING_ERROR) {
	    System.err.println("ERROR : Easting outside of valid range ");
	}
	else if (error == NORTHING_ERROR) {
	    System.err.println("ERROR : Northing outside of valid range ");
	}
	else if (error == ORIGIN_LAT_ERROR) {
	    System.err.println("ERROR : Origin latitude outside of valid range ");
	}
	else if (error == CENT_MER_ERROR) {
	    System.err.println("ERROR : Central meridian outside of valid range ");
	}
	else if (error == SCALE_FACTOR_ERROR) {
	    System.err.println("ERROR : Scale factor outside of valid range ");
	}
	else if (error == A_ERROR) {
	    System.err.println("ERROR : Semi-major axis less than or equal to zero ");
	}
	else if (error == INV_F_ERROR) {
	    System.err.println("ERROR : Inverse flattening outside of valid range ");
	}
	else if (error == UTM_ZONE_ERROR) {
	    System.err.println("ERROR :  Zone outside of valid range (1 to 60) ");
	}
	else if (error == HEMISPHERE_ERROR) {
	    System.err.println("ERROR :  Invalid hemisphere ");
	}
	else if (error == UTM_ZONE_OVERRIDE_ERROR) {
	    System.err.println("ERROR :  Zone outside of valid range (1 to 60) and within 1 of 'natural' zone ");
	}
	else if (error == MGRS_STR_ERROR) {
	    System.err.println("ERROR : MGRS string is too long, too short, or badly formed ");
	}
	else if (error == MGRS_PRECISION_ERROR) {
	    System.err.println("ERROR : MGRS precision must be between 0 and 5 inclusive ");
	}
	else if (error == POLAR_RADIUS_ERROR) {
	    System.err.println("ERROR : Coordinates too far from pole.");
	}
    }
    
    /**
     * Writes out the warning string.
     */
    public static void handleWarning(long warning) {
	if (warning == LON_WARNING) {
	    //System.err.println("WARNING : Longitude is more than 9 degrees from the Central Meridian ");
	}
    }
    

}
