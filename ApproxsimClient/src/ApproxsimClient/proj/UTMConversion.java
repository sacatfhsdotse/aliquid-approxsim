package ApproxsimClient.proj;

/**
 * This class converts geodetic values (longitude, latitude) into UTM coordinates and vice versa.
 */
class UTMConversion {
    /**
     * Zone override flag.
     */
    public static long utm_override = 0;

    // Necessary parameters
    public static double MIN_LAT = ((-80.5 * Math.PI) / 180.0); /* -80.5 degrees in radians */
    public static double MAX_LAT = ((84.5 * Math.PI) / 180.0); /* 84.5 degrees in radians */
    public static long MIN_EASTING = 100000;
    public static long MAX_EASTING = 900000;
    public static long MIN_NORTHING = 0;
    public static long MAX_NORTHING = 10000000;

    /*
     * This method receives the UTM zone override parameter as input, and sets the corresponding state variables.
     * @param override UTM override zone, zero indicates no override.
     * @return true if no error occured, false otherwise.
     */
    public static boolean setUTMParameters(long override) {

        double inv_f = 1 / ProjectionConstants.ellipsFlattening;
        // Semi-major axis must be greater than zero
        if (ProjectionConstants.semiMajorAxis <= 0.0) {
            ProjectionErrorHandler.handleError(ProjectionErrorHandler.A_ERROR);
            return false;
        }
        // Inverse flattening must be between 250 and 350
        else if (inv_f < 250 || inv_f > 350) {
            ProjectionErrorHandler
                    .handleError(ProjectionErrorHandler.INV_F_ERROR);
            return false;
        }
        // Override zone
        else if (override < 0 || override > 60) {
            ProjectionErrorHandler
                    .handleError(ProjectionErrorHandler.UTM_ZONE_OVERRIDE_ERROR);
            return false;
        }
        //
        utm_override = override;
        //
        return true;
    }

    /**
     * Returns the UTM override zone, zero indicates no override.
     */
    public static long getUTMOverride() {
        return utm_override;
    }

    /*
     * This method converts geodetic (latitude and longitude) coordinates to UTM projection (zone, hemisphere, easting and northing)
     * coordinates according to the current ellipsoid and UTM zone override parameters.
     * @param latitude Latitude in radians.
     * @param longitude Longitude in radians.
     * @return UTM coordinate.
     */
    public static UTMCoordinate convertGeodeticToUTM(double longitude,
            double latitude) {
        char hemisphere;
        double origin_latitude = 0;
        double false_easting = 500000;
        double false_northing = 0;
        double scale = 0.9996;
        // Latitude out of range
        if (latitude < MIN_LAT || latitude > MAX_LAT) {
            ProjectionErrorHandler
                    .handleError(ProjectionErrorHandler.LAT_ERROR);
            return null;
        }

        // Longitude out of range
        if (longitude < -Math.PI || longitude > (2 * Math.PI)) {
            return null;
        }

        if (longitude < 0) {
            longitude += (2 * Math.PI) + 1.0e-10;
        }
        long lat_degrees = (long) (latitude * 180.0 / Math.PI);
        long lon_degrees = (long) (longitude * 180.0 / Math.PI);

        long temp_zone = (longitude < Math.PI) ? (long) (31 + ((longitude * 180.0 / Math.PI) / 6.0))
                : (long) (((longitude * 180.0 / Math.PI) / 6.0) - 29);

        if (temp_zone > 60) {
            temp_zone = 1;
        }
        // UTM special cases
        if ((lat_degrees > 55) && (lat_degrees < 64) && (lon_degrees > -1)
                && (lon_degrees < 3)) {
            temp_zone = 31;
        }
        if ((lat_degrees > 55) && (lat_degrees < 64) && (lon_degrees > 2)
                && (lon_degrees < 12)) {
            temp_zone = 32;
        }
        if ((lat_degrees > 71) && (lon_degrees > -1) && (lon_degrees < 9)) {
            temp_zone = 31;
        }
        if ((lat_degrees > 71) && (lon_degrees > 8) && (lon_degrees < 21)) {
            temp_zone = 33;
        }
        if ((lat_degrees > 71) && (lon_degrees > 20) && (lon_degrees < 33)) {
            temp_zone = 35;
        }
        if ((lat_degrees > 71) && (lon_degrees > 32) && (lon_degrees < 42)) {
            temp_zone = 37;
        }

        if (utm_override != 0) {
            if ((temp_zone == 1) && (utm_override == 60)) {
                temp_zone = utm_override;
            } else if ((temp_zone == 60) && (utm_override == 1)) {
                temp_zone = utm_override;
            } else if (((temp_zone - 1) <= utm_override)
                    && (utm_override <= (temp_zone + 1))) {
                temp_zone = utm_override;
            } else {
                ProjectionErrorHandler
                        .handleError(ProjectionErrorHandler.UTM_ZONE_OVERRIDE_ERROR);
                return null;
            }
        }
        double central_meridian = (temp_zone >= 31) ? (6 * temp_zone - 183)
                * Math.PI / 180.0 : (6 * temp_zone + 177) * Math.PI / 180.0;

        long zone = temp_zone;
        if (latitude < 0) {
            false_northing = 10000000;
            hemisphere = 'S';
        } else {
            hemisphere = 'N';
        }

        TransverseMercatorProjection
                .setTransverseMercatorParameters(origin_latitude,
                                                 central_meridian,
                                                 false_easting, false_northing,
                                                 scale);
        double[] east_north = TransverseMercatorProjection
                .convertGeodeticToTransverseMercator(longitude, latitude);
        if (east_north == null) {
            return null;
        }
        double easting = east_north[0];
        double northing = east_north[1];
        if (easting < MIN_EASTING || easting > MAX_EASTING) {
            ProjectionErrorHandler
                    .handleError(ProjectionErrorHandler.EASTING_ERROR);
            return null;
        }
        if (northing < MIN_NORTHING || northing > MAX_NORTHING) {
            ProjectionErrorHandler
                    .handleError(ProjectionErrorHandler.NORTHING_ERROR);
            return null;
        }
        //
        return new UTMCoordinate(zone, hemisphere, easting, northing);
    }

    /*
     * This method converts UTM projection (zone, hemisphere, easting and northing) coordinates to geodetic(latitude and longitude)
     * coordinates, according to the current ellipsoid parameters.
     * @param utmc UTM coordinate
     * @return [longitude in radians, latitude in radians]
     */
    public static double[] convertUTMToGeodetic(UTMCoordinate utmc) {
        double origin_latitude = 0;
        double central_meridian = 0;
        double false_easting = 500000;
        double false_northing = 0;
        double scale = 0.9996;
        //
        long zone = utmc.getZone();
        char hemisphere = utmc.getHemisphere();
        double easting = utmc.getEasting();
        double northing = utmc.getNorthing();

        if (zone < 1 || zone > 60) {
            ProjectionErrorHandler
                    .handleError(ProjectionErrorHandler.UTM_ZONE_ERROR);
            return null;
        }
        if (hemisphere != 'S' && hemisphere != 'N') {
            ProjectionErrorHandler
                    .handleError(ProjectionErrorHandler.HEMISPHERE_ERROR);
            return null;
        }
        if (easting < MIN_EASTING || easting > MAX_EASTING) {
            ProjectionErrorHandler
                    .handleError(ProjectionErrorHandler.EASTING_ERROR);
            return null;
        }
        if (northing < MIN_NORTHING || northing > MAX_NORTHING) {
            ProjectionErrorHandler
                    .handleError(ProjectionErrorHandler.NORTHING_ERROR);
            return null;
        }

        if (zone >= 31) {
            central_meridian = ((6 * zone - 183) * Math.PI / 180.0 /* + 0.00000005 */);
        } else {
            central_meridian = ((6 * zone + 177) * Math.PI / 180.0 /* + 0.00000005 */);
        }
        if (hemisphere == 'S') {
            false_northing = 10000000;
        }
        // System.out.println("central meridian = "+central_meridian);
        TransverseMercatorProjection
                .setTransverseMercatorParameters(origin_latitude,
                                                 central_meridian,
                                                 false_easting, false_northing,
                                                 scale);
        double[] lon_lat = TransverseMercatorProjection
                .convertTransverseMercatorToGeodetic(easting, northing);

        if (lon_lat == null) {
            return null;
        }
        double latitude = lon_lat[1];
        // Latitude out of range
        if (latitude < MIN_LAT || latitude > MAX_LAT) {
            ProjectionErrorHandler
                    .handleError(ProjectionErrorHandler.NORTHING_ERROR);
            return null;
        }
        //
        return lon_lat;
    }

}
