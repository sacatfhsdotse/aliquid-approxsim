package StratmasClient.proj;

/**
 * This is implementation of the Transverse Mercator projection where (longitude/latitude) values
 * are converted into (easting/northing) values and vice versa.
 *
 */
class TransverseMercatorProjection {
    // Necessary parameters
    public static double MAX_LAT = (Math.PI * 89.99)/180.0;        /* 90 degrees in radians */
    public static double MAX_DELTA_LON = (Math.PI * 90)/180.0;     /* 90 degrees in radians */
    public static double MIN_SCALE_FACTOR = 0.3;
    public static double MAX_SCALE_FACTOR = 3.0;
    
    // Transverse Mercator projection parameters
    public static double tranMerc_origin_lat = 0.0;           /* Latitude of origin in radians */
    public static double tranMerc_origin_lon = 0.0;           /* Longitude of origin in radians */
    public static double tranMerc_false_northing = 0.0;       /* False northing in meters */
    public static double tranMerc_false_easting = 0.0;        /* False easting in meters */
    public static double tranMerc_scale_factor = 1.0;         /* Scale factor  */

    
    /*
     * This method receives the Tranverse Mercator projection parameters as inputs, 
     * and sets the corresponding state variables.
     *
     * @param origin_latitude  Latitude in radians at the origin of the projection.
     * @param central_meridian Longitude in radians at the center of the projection.
     * @param false_easting    Easting/X at the center of the projection.
     * @param false_northing   Northing/Y at the center of the projection.
     * @param scale_factor     Projection scale factor.
     *
     * @return true if no error occured, otherwise false.
     */
    public static boolean setTransverseMercatorParameters(double origin_latitude, double central_meridian, 
                                                          double false_easting, double false_northing, 
                                                          double scale_factor) { 
        double inv_f = 1/ProjectionConstants.ellipsFlattening;
        
        tranMerc_origin_lat = 0.0;
        tranMerc_origin_lon = 0.0;
        tranMerc_false_northing = 0.0;
        tranMerc_false_easting = 0.0; 
        tranMerc_scale_factor = 1.0;  
        
        //        System.out.println("orig lat = "+origin_latitude);
        //        System.out.println("cent mer = "+central_meridian);
        
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
        // Origin latitude out of range
        else if (origin_latitude < -MAX_LAT || origin_latitude > MAX_LAT) {
            ProjectionErrorHandler.handleError(ProjectionErrorHandler.ORIGIN_LAT_ERROR);
            return false;
        }
        // Origin longitude out of range
        else if (central_meridian < -Math.PI || central_meridian > (2*Math.PI)) {
            ProjectionErrorHandler.handleError(ProjectionErrorHandler.CENT_MER_ERROR);
            return false;
        }
        // Invalid scale factor
        else if (scale_factor < MIN_SCALE_FACTOR || scale_factor > MAX_SCALE_FACTOR) {
            ProjectionErrorHandler.handleError(ProjectionErrorHandler.SCALE_FACTOR_ERROR);
            return false;
        }
                
        // Set the maximum variance for easting and northing values
        double[] east_north = convertGeodeticToTransverseMercator(MAX_DELTA_LON, MAX_LAT);
        if (east_north == null) {
            return false;
        }
        ProjectionConstants.setDeltaEasting(east_north[0]);
        ProjectionConstants.setDeltaNorthing(east_north[1]);
        east_north = convertGeodeticToTransverseMercator(MAX_DELTA_LON, 0);
        if (east_north == null) {
            return false;
        }
        ProjectionConstants.setDeltaEasting(east_north[0]);
        // 
        tranMerc_origin_lat = origin_latitude;
        if (central_meridian > Math.PI) {
            central_meridian -= (2*Math.PI);
        }
        tranMerc_origin_lon = central_meridian;
        tranMerc_false_northing = false_northing;
        tranMerc_false_easting = false_easting; 
        tranMerc_scale_factor = scale_factor;
        //System.out.println(" tranMerc_origin_lon = "+tranMerc_origin_lon);
        //System.out.println(" tranMerc_origin_lat = "+tranMerc_origin_lat);
        //
        return true;
    }
    
    /**
     * Returns the latitude in radians at the origin of the projection.
     */
    public static double getOriginLatitude() {
        return tranMerc_origin_lat;
    }
    
    /**
     * Returns the longitude in radians at the center of the projection.
     */
    public static double getCentralMeridian() {
        return tranMerc_origin_lon;
    }

    /**
     * returns the easting/X at the center of the projection. 
     */
    public static double getFalseEasting() {
        return tranMerc_false_easting;
    }

    /**
     * Returns the northing/Y at the center of the projection. 
     */
    public static double getFalseNorthing() {
        return tranMerc_false_northing;
    }

    /**
     *  Returns the projection scale factor. 
     */
    public static double getScaleFactor() {
        return tranMerc_scale_factor;
    }

    /**
     * Help function.
     */
    private static double SPHTMD(double latitude) {
        return (double) (ProjectionConstants.ap * latitude - ProjectionConstants.bp * Math.sin(2.e0 * latitude) + 
                         ProjectionConstants.cp * Math.sin(4.e0 * latitude) - ProjectionConstants.dp * 
                         Math.sin(6.e0 * latitude) + ProjectionConstants.ep * Math.sin(8.e0 * latitude));
    }

    /**
     * Help function.
     */
    private static double SPHSN(double latitude) {
        return (double) (ProjectionConstants.semiMajorAxis / Math.sqrt( 1.e0 - 
                                                                        Math.pow(ProjectionConstants.eccentricity, 2) * 
                                                                        Math.pow(Math.sin(latitude), 2)));
    }
    
    /**
     * Help function.
     */
    private static double SPHSR(double latitude) {
        return (double) (ProjectionConstants.semiMajorAxis * (1.e0 - Math.pow(ProjectionConstants.eccentricity, 2)) / 
                         Math.pow(DENOM(latitude), 3));
    }
    
    /**
     * Help function.
     */
    private static double DENOM(double latitude) {
        return (double) (Math.sqrt(1.e0 - Math.pow(ProjectionConstants.eccentricity, 2) * Math.pow(Math.sin(latitude),2)));
    }

    /*
     * This method converts geodetic (latitude and longitude) coordinates to Transverse Mercator projection
     * (easting and northing) coordinates, according to the current ellipsoid and Transverse Mercator projection 
     * coordinates.
     *
     * @param latitude  latitude in radians.
     * @param longitude longitude in radians.
     *
     * @return [easting/X, northing/Y] in meters.
     */
    public static double[] convertGeodeticToTransverseMercator (double longitude, double latitude) {
        double temp_origin;
        double temp_lon;
        
        // Latitude out of range 
        if (latitude < -MAX_LAT || latitude > MAX_LAT) {
            ProjectionErrorHandler.handleError(ProjectionErrorHandler.LAT_ERROR);
            return null;
        }
        if (longitude > Math.PI) {
            longitude -= (2 * Math.PI);
        }
        //        System.out.println("longitude = "+Math.toDegrees(longitude)+", latitude = "+Math.toDegrees(latitude));
        //        System.out.println(" tranMerc_origin_lon = "+Math.toDegrees(tranMerc_origin_lon));
        if (longitude < (tranMerc_origin_lon - MAX_DELTA_LON)
            || longitude > (tranMerc_origin_lon + MAX_DELTA_LON)) {
            //
            if (longitude < 0) {
                temp_lon = longitude + 2 * Math.PI;
            }
            else {
                temp_lon = longitude;
            }
            if (tranMerc_origin_lon < 0) {
                temp_origin = tranMerc_origin_lon + 2 * Math.PI;
            }
            else {
                temp_origin = tranMerc_origin_lon;
            }
            if (temp_lon < (temp_origin - MAX_DELTA_LON)
                || temp_lon > (temp_origin + MAX_DELTA_LON)) {
                //System.out.println("Probably here");
                ProjectionErrorHandler.handleError(ProjectionErrorHandler.LON_ERROR);
                return null;
            }
        }
        double dlam = longitude - tranMerc_origin_lon; /* Delta Longitude */
        
        // Distortion will result if Longitude is more than 9 degrees from the Central Meridian
        if (Math.abs(dlam) > (9.0 * Math.PI / 180)) {
            ProjectionErrorHandler.handleWarning(ProjectionErrorHandler.LON_WARNING);
        }
        
        if (dlam > Math. PI) {
            dlam -= (2 * Math.PI);
        }
        if (dlam < -Math.PI) {
            dlam += (2 * Math.PI);
        }
        if (Math.abs(dlam) < 2.e-10) {
            dlam = 0.0;
        }

        double s = Math.sin(latitude);
        double c = Math.cos(latitude);
        double c2 = c * c;
        double c3 = c2 * c;
        double c5 = c3 * c2;
        double c7 = c5 * c2;
        double t = Math.tan(latitude);
        double tan2 = t * t;
        double tan3 = tan2 * t;
        double tan4 = tan3 * t;
        double tan5 = tan4 * t;
        double tan6 = tan5 * t;
        double eta = ((1 / (1 - Math.pow(ProjectionConstants.eccentricity, 2))) - 1) * c2;
        double eta2 = eta * eta;
        double eta3 = eta2 * eta;
        double eta4 = eta3 * eta;
            
        // radius of curvature in prime vertical
        double sn = SPHSN(latitude);

        // true Meridianal Distances
        double tmd = SPHTMD(latitude);

        // origin
        double tmdo = SPHTMD(tranMerc_origin_lat);

        // northing
        double t1 = (tmd - tmdo) * tranMerc_scale_factor;
        double t2 = sn * s * c * tranMerc_scale_factor/ 2.e0;
        double t3 = sn * s * c3 * tranMerc_scale_factor * (5.e0 - tan2 + 9.e0 * eta 
                                                           + 4.e0 * eta2) /24.e0; 
            
        double t4 = sn * s * c5 * tranMerc_scale_factor * (61.e0 - 58.e0 * tan2
                                                           + tan4 + 270.e0 * eta - 330.e0 * tan2 * eta + 445.e0 * eta2
                                                           + 324.e0 * eta3 -680.e0 * tan2 * eta2 + 88.e0 * eta4 
                                                           -600.e0 * tan2 * eta3 - 192.e0 * tan2 * eta4) / 720.e0;
            
        double t5 = sn * s * c7 * tranMerc_scale_factor * (1385.e0 - 3111.e0 * 
                                                           tan2 + 543.e0 * tan4 - tan6) / 40320.e0;
            
        double northing = tranMerc_false_northing + t1 + Math.pow(dlam,2.e0) * t2
            + Math.pow(dlam,4.e0) * t3 + Math.pow(dlam,6.e0) * t4 + Math.pow(dlam,8.e0) * t5; 
            
        // easting
        double t6 = sn * c * tranMerc_scale_factor;
        double t7 = sn * c3 * tranMerc_scale_factor * (1.e0 - tan2 + eta ) /6.e0;
        double t8 = sn * c5 * tranMerc_scale_factor * (5.e0 - 18.e0 * tan2 + tan4
                                                       + 14.e0 * eta - 58.e0 * tan2 * eta + 13.e0 * eta2 + 4.e0 * eta3 
                                                       - 64.e0 * tan2 * eta2 - 24.e0 * tan2 * eta3 )/ 120.e0;
        double t9 = sn * c7 * tranMerc_scale_factor * ( 61.e0 - 479.e0 * tan2
                                                        + 179.e0 * tan4 - tan6 ) /5040.e0;
            
        double easting = tranMerc_false_easting + dlam * t6 + Math.pow(dlam,3.e0) * t7 
            + Math.pow(dlam,5.e0) * t8 + Math.pow(dlam,7.e0) * t9;
            
        //
        double[] output = {easting, northing};
        return output;
    }

    /*
     * This method converts Transverse Mercator projection (easting and northing) 
     * coordinates to geodetic (latitude and longitude) coordinates, according to 
     * the current ellipsoid and Transverse Mercator projection parameters.
     *
     * @param easting  Easting/X in meters.
     * @param northing Northing/Y in meters.
     *
     * @return [latitude, longitude] in radians.
     */  
    public static double[] convertTransverseMercatorToGeodetic(double easting, double northing) {

        //System.out.println("easting = "+easting+", northing = "+northing);
        
        // Easting out of range
        if (easting < (tranMerc_false_easting - ProjectionConstants.deltaEasting)
            ||easting > (tranMerc_false_easting + ProjectionConstants.deltaEasting)) {
            ProjectionErrorHandler.handleError(ProjectionErrorHandler.EASTING_ERROR);
            return null;
        }
        // Northing out of range
        if (northing < (tranMerc_false_northing - ProjectionConstants.deltaNorthing)
            || northing > (tranMerc_false_northing + ProjectionConstants.deltaNorthing)) {
            ProjectionErrorHandler.handleError(ProjectionErrorHandler.NORTHING_ERROR);
            return null;
        }

        // True Meridional Distances for latitude of origin
        double tmdo = SPHTMD(tranMerc_origin_lat);
        
        // Origin
        double tmd = tmdo +  (northing - tranMerc_false_northing) / tranMerc_scale_factor; 
        
        // First Estimate
        double sr = SPHSR(0.e0);
        double ftphi = tmd/sr;
        
        for (int i = 0; i < 5 ; i++) {
            double t10 = SPHTMD (ftphi);
            sr = SPHSR(ftphi);
            ftphi = ftphi + (tmd - t10) / sr;
        }
        
        // Radius of Curvature in the meridian 
        sr = SPHSR(ftphi);
            
        // Radius of Curvature in the meridian 
        double sn = SPHSN(ftphi);
            
        Math.sin(ftphi);
        double c = Math.cos(ftphi);

        // Tangent Value
        double t = Math.tan(ftphi);
        double tan2 = t * t;
        double tan4 = tan2 * tan2;
        double eta = ((1 / (1 - Math.pow(ProjectionConstants.eccentricity, 2))) - 1) * Math.pow(c,2);
        double eta2 = eta * eta;
        double eta3 = eta2 * eta;
        double eta4 = eta3 * eta;
        double de = easting - tranMerc_false_easting;
        if (Math.abs(de) < 0.0001) {
            de = 0.0;
        }
        
        // Latitude
        double t10 = t / (2.e0 * sr * sn * Math.pow(tranMerc_scale_factor, 2));
        double t11 = t * (5.e0  + 3.e0 * tan2 + eta - 4.e0 * Math.pow(eta,2)
                          - 9.e0 * tan2 * eta) / (24.e0 * sr * Math.pow(sn,3) 
                                               * Math.pow(tranMerc_scale_factor,4));
        double t12 = t * (61.e0 + 90.e0 * tan2 + 46.e0 * eta + 45.E0 * tan4
                          - 252.e0 * tan2 * eta  - 3.e0 * eta2 + 100.e0 
                          * eta3 - 66.e0 * tan2 * eta2 - 90.e0 * tan4
                          * eta + 88.e0 * eta4 + 225.e0 * tan4 * eta2
                          + 84.e0 * tan2* eta3 - 192.e0 * tan2 * eta4)/
            ( 720.e0 * sr * Math.pow(sn,5) * Math.pow(tranMerc_scale_factor, 6) );
        double t13 = t * ( 1385.e0 + 3633.e0 * tan2 + 4095.e0 * tan4 + 1575.e0 
                           * Math.pow(t,6))/ (40320.e0 * sr * Math.pow(sn,7) * Math.pow(tranMerc_scale_factor,8));
        
        double latitude = ftphi - Math.pow(de,2) * t10 + Math.pow(de,4) * t11 - Math.pow(de,6) * t12 
            + Math.pow(de,8) * t13;

        double t14 = 1.e0 / (sn * c * tranMerc_scale_factor);
        double t15 = (1.e0 + 2.e0 * tan2 + eta) / (6.e0 * Math.pow(sn,3) * c * 
                                                   Math.pow(tranMerc_scale_factor,3));
        double t16 = (5.e0 + 6.e0 * eta + 28.e0 * tan2 - 3.e0 * eta2
                      + 8.e0 * tan2 * eta + 24.e0 * tan4 - 4.e0 
                      * eta3 + 4.e0 * tan2 * eta2 + 24.e0 
                      * tan2 * eta3) / (120.e0 * Math.pow(sn,5) * c  
                                        * Math.pow(tranMerc_scale_factor,5));
        double t17 = (61.e0 +  662.e0 * tan2 + 1320.e0 * tan4 + 720.e0 
                      * Math.pow(t,6)) / (5040.e0 * Math.pow(sn,7) * c 
                                          * Math.pow(tranMerc_scale_factor,7));
        
        // Difference in Longitude
        double dlam = de * t14 - Math.pow(de,3) * t15 + Math.pow(de,5) * t16 - Math.pow(de,7) * t17;
        
        // Longitude
        double longitude = tranMerc_origin_lon + dlam;
        while (latitude > (90.0 * Math.PI / 180.0)) {
            latitude = Math.PI - latitude;
            longitude += Math.PI;
            if (longitude > Math.PI) {
                longitude -= (2 * Math.PI);
            }
        }
        
        while (latitude < (-90.0 * Math.PI / 180.0)) {
            latitude = - (latitude + Math.PI);
            longitude += Math.PI;
            if (longitude > Math.PI) {
                longitude -= (2 * Math.PI);
            }
        }
        
        if (longitude > (2*Math.PI)) {
            longitude -= (2 * Math.PI);
        }
        if (longitude < -Math.PI) {
            longitude += (2 * Math.PI);
        }
        
        // Distortion will result if Longitude is more than 9 degrees from the Central Meridian 
        if (Math.abs(dlam) > (9.0 * Math.PI / 180)) {
            ProjectionErrorHandler.handleWarning(ProjectionErrorHandler.LON_WARNING);
        }
        //
        double[] output = {longitude, latitude};
        return output;
    }

}
