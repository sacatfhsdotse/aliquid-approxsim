package StratmasClient.proj;

/**
 * This class implements the conversion between geodetic values (latitude and longitude) and MGRS coordinates.
 */
public class MGRSConversion {
    /*
     * Necessary parameters.
     */
    public static final double DEG_TO_RAD = 0.017453292519943295; /* PI/180 */
    public static final double RAD_TO_DEG = 57.29577951308232087; /* 180/PI */
    public static final int LETTER_A = 0; /* ARRAY INDEX FOR LETTER A */
    public static final int LETTER_B = 1; /* ARRAY INDEX FOR LETTER B */
    public static final int LETTER_C = 2; /* ARRAY INDEX FOR LETTER C */
    public static final int LETTER_D = 3; /* ARRAY INDEX FOR LETTER D */
    public static final int LETTER_E = 4; /* ARRAY INDEX FOR LETTER E */
    public static final int LETTER_F = 5; /* ARRAY INDEX FOR LETTER E */
    public static final int LETTER_G = 6; /* ARRAY INDEX FOR LETTER H */
    public static final int LETTER_H = 7; /* ARRAY INDEX FOR LETTER H */
    public static final int LETTER_I = 8; /* ARRAY INDEX FOR LETTER I */
    public static final int LETTER_J = 9; /* ARRAY INDEX FOR LETTER J */
    public static final int LETTER_K = 10; /* ARRAY INDEX FOR LETTER J */
    public static final int LETTER_L = 11; /* ARRAY INDEX FOR LETTER L */
    public static final int LETTER_M = 12; /* ARRAY INDEX FOR LETTER M */
    public static final int LETTER_N = 13; /* ARRAY INDEX FOR LETTER N */
    public static final int LETTER_O = 14; /* ARRAY INDEX FOR LETTER O */
    public static final int LETTER_P = 15; /* ARRAY INDEX FOR LETTER P */
    public static final int LETTER_Q = 16; /* ARRAY INDEX FOR LETTER Q */
    public static final int LETTER_R = 17; /* ARRAY INDEX FOR LETTER R */
    public static final int LETTER_S = 18; /* ARRAY INDEX FOR LETTER S */
    public static final int LETTER_T = 19; /* ARRAY INDEX FOR LETTER S */
    public static final int LETTER_U = 20; /* ARRAY INDEX FOR LETTER U */
    public static final int LETTER_V = 21; /* ARRAY INDEX FOR LETTER V */
    public static final int LETTER_W = 22; /* ARRAY INDEX FOR LETTER W */
    public static final int LETTER_X = 23; /* ARRAY INDEX FOR LETTER X */
    public static final int LETTER_Y = 24; /* ARRAY INDEX FOR LETTER Y */
    public static final int LETTER_Z = 25; /* ARRAY INDEX FOR LETTER Z */
    public static final int MGRS_LETTERS = 3; /* NUMBER OF LETTERS IN MGRS */
    public static final double PI_OVER_2 = (Math.PI / 2.0);

    public static final int MIN_EASTING = 100000;
    public static final int MAX_EASTING = 900000;
    public static final int MIN_NORTHING = 0;
    public static final int MAX_NORTHING = 10000000;
    public static final int MAX_PRECISION = 5; /* Maximum precision of easting & northing */
    public static final double MIN_UTM_LAT = ((-80 * Math.PI) / 180.0);/* -80 degrees in radians */
    public static final double MAX_UTM_LAT = ((84 * Math.PI) / 180.0); /* 84 degrees in radians */

    public static final int MIN_EAST_NORTH = 0;
    public static final int MAX_EAST_NORTH = 4000000;

    /**
     * The table of the latitude bands.
     */
    public static final LatitudeBand[] latitude_band_table = {
            new LatitudeBand(LETTER_C, 1100000.0, -72.0, -80.5),
            new LatitudeBand(LETTER_D, 2000000.0, -64.0, -72.0),
            new LatitudeBand(LETTER_E, 2800000.0, -56.0, -64.0),
            new LatitudeBand(LETTER_F, 3700000.0, -48.0, -56.0),
            new LatitudeBand(LETTER_G, 4600000.0, -40.0, -48.0),
            new LatitudeBand(LETTER_H, 5500000.0, -32.0, -40.0),
            new LatitudeBand(LETTER_J, 6400000.0, -24.0, -32.0),
            new LatitudeBand(LETTER_K, 7300000.0, -16.0, -24.0),
            new LatitudeBand(LETTER_L, 8200000.0, -8.0, -16.0),
            new LatitudeBand(LETTER_M, 9100000.0, 0.0, -8.0),
            new LatitudeBand(LETTER_N, 0.0, 8.0, 0.0),
            new LatitudeBand(LETTER_P, 800000.0, 16.0, 8.0),
            new LatitudeBand(LETTER_Q, 1700000.0, 24.0, 16.0),
            new LatitudeBand(LETTER_R, 2600000.0, 32.0, 24.0),
            new LatitudeBand(LETTER_S, 3500000.0, 40.0, 32.0),
            new LatitudeBand(LETTER_T, 4400000.0, 48.0, 40.0),
            new LatitudeBand(LETTER_U, 5300000.0, 56.0, 48.0),
            new LatitudeBand(LETTER_V, 6200000.0, 64.0, 56.0),
            new LatitudeBand(LETTER_W, 7000000.0, 72.0, 64.0),
            new LatitudeBand(LETTER_X, 7900000.0, 84.5, 72.0) };

    /**
     * The table of UPS constants.
     */
    public static final UPSConstant[] ups_constant_table = {
            new UPSConstant(LETTER_A, LETTER_J, LETTER_Z, LETTER_Z, 800000.0,
                    800000.0),
            new UPSConstant(LETTER_B, LETTER_A, LETTER_R, LETTER_Z, 2000000.0,
                    800000.0),
            new UPSConstant(LETTER_Y, LETTER_J, LETTER_Z, LETTER_P, 800000.0,
                    1300000.0),
            new UPSConstant(LETTER_Z, LETTER_A, LETTER_J, LETTER_P, 2000000.0,
                    1300000.0) };

    /*
     * This method receives a latitude band letter and uses the latitude_band_table to determine the minimum northing for that latitude band
     * letter.
     * @param letter latitude band letter.
     * @return minimum northing for that letter.
     */
    public static double getLatitudeBandMinNorthing(int letter) {
        double min_northing = -1;
        if (letter >= LETTER_C && letter <= LETTER_H) {
            min_northing = latitude_band_table[letter - 2].getMinNorthing();
        } else if (letter >= LETTER_J && letter <= LETTER_N) {
            min_northing = latitude_band_table[letter - 3].getMinNorthing();
        } else if (letter >= LETTER_P && letter <= LETTER_X) {
            min_northing = latitude_band_table[letter - 4].getMinNorthing();
        } else {
            ProjectionErrorHandler
                    .handleError(ProjectionErrorHandler.MGRS_STR_ERROR);
            return -1;
        }
        return min_northing;
    }

    /*
     * This method receives a latitude band letter and uses the latitude_band_table to determine the latitude band boundaries for that
     * latitude band letter.
     * @param letter latitude band letter.
     * @return [Northern latitude boundary for that letter, Southern latitude boundary for that letter].
     */
    public static double[] getLatitudeRange(int letter) {
        double north = 0.0;
        double south = 0.0;

        if (letter >= LETTER_C && letter <= LETTER_H) {
            north = latitude_band_table[letter - 2].getNorth() * DEG_TO_RAD;
            south = latitude_band_table[letter - 2].getSouth() * DEG_TO_RAD;
        } else if (letter >= LETTER_J && letter <= LETTER_N) {
            north = latitude_band_table[letter - 3].getNorth() * DEG_TO_RAD;
            south = latitude_band_table[letter - 3].getSouth() * DEG_TO_RAD;
        } else if (letter >= LETTER_P && letter <= LETTER_X) {
            north = latitude_band_table[letter - 4].getNorth() * DEG_TO_RAD;
            south = latitude_band_table[letter - 4].getSouth() * DEG_TO_RAD;
        } else {
            ProjectionErrorHandler
                    .handleError(ProjectionErrorHandler.MGRS_STR_ERROR);
            return null;
        }

        double[] ns = { north, south };
        return ns;
    }

    /*
     * This method receives a latitude value and uses the latitude_band_table to determine the latitude band letter for that latitude.
     * @param latitude latitude.
     * @return latitude band letter.
     */
    public static int getLatitudeLetter(double latitude) {
        int letter = -1;

        double lat_deg = latitude * RAD_TO_DEG;

        if (lat_deg >= 72 && lat_deg < 84.5) {
            letter = LETTER_X;
        } else if (lat_deg > -80.5 && lat_deg < 72) {
            double temp = ((latitude + (80.0 * DEG_TO_RAD)) / (8.0 * DEG_TO_RAD)) + 1.0e-12;
            letter = latitude_band_table[(int) temp].getLetter();
        } else {
            ProjectionErrorHandler
                    .handleError(ProjectionErrorHandler.LAT_ERROR);
            return -1;
        }
        //
        return letter;
    }

    /*
     * This method receives an MGRS coordinate string.If a zone is given, true is returned. Otherwise, false is returned.
     * @param mgrs MGRS coordinate string.
     * @return true if a zone is given, false if a zone is not given.
     */
    public static boolean checkZone(String mgrs) {
        int i = 0;

        // skip any leading blanks
        while (mgrs.charAt(i) == ' ') {
            i++;
        }
        int j = i;
        while (Character.isDigit(mgrs.charAt(i))) {
            i++;
        }
        int num_digits = i - j;
        if (num_digits <= 2) {
            boolean zone_exists = (num_digits > 0) ? true : false;
            return zone_exists;
        } else {
            ProjectionErrorHandler
                    .handleError(ProjectionErrorHandler.MGRS_STR_ERROR);
            return false;
        }
    }

    /*
     * This method rounds the input value to the nearest integer, using the standard engineering rule. The rounded integer value is then
     * returned.
     * @param value value to be rounded.
     */
    public static long roundMGRS(double value) {
        double fraction = value - Math.floor(value);
        long ival = (long) Math.floor(value);
        if (fraction > 0.5 || (fraction == 0.5 && ival % 2 == 1)) {
            ival++;
        }
        return ival;
    }

    /*
     * This method constructs an MGRS string from its component parts.
     * @param zone UTM Zone.
     * @param letters MGRS coordinate string letters.
     * @param easting easting value.
     * @param northing northing value.
     * @param precision precision level of MGRS string.
     * @return MGRS coordinate string.
     */
    public static String makeMGRSString(long zone, int[] letters,
            double easting, double northing, long precision) {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        String mgrs = (zone != 0) ? Long.toString(zone) : "  ";

        for (int j = 0; j < 3; j++) {
            mgrs += Character.toString(alphabet.charAt(letters[j]));
        }
        double divisor = Math.pow(10.0, 5 - precision);
        easting = easting % 100000.0;
        if (easting >= 99999.5) {
            easting = 99999.0;
        }
        String east = Long.toString((long) (easting / divisor));
        while (east.length() < precision) {
            east = "0" + east;
        }
        mgrs += east;
        northing = northing % 100000.0;
        if (northing >= 99999.5) {
            northing = 99999.0;
        }
        String north = Long.toString((long) (northing / divisor));
        while (north.length() < precision) {
            north = "0" + north;
        }
        mgrs += north;
        //
        return mgrs;
    }

    /*
     * This method breaks down an MGRS coordinate string into its component parts.
     * @param mgrs MGRS coordinate string.
     * @return MGRS coordinate.
     */
    public static MGRSCoordinate breakMGRSString(String mgrs) {
        long zone = 0;
        int[] letters = { -1, -1, -1 };
        double easting = -1.0;
        double northing = -1.0;
        long precision = -1;
        int i = 0;

        while (mgrs.charAt(i) == ' ') {
            i++; /* skip any leading blanks */
        }
        int j = i;
        while (Character.isDigit(mgrs.charAt(i))) {
            i++;
        }
        int num_digits = i - j;
        if (num_digits <= 2) {
            if (num_digits > 0) {
                String zone_string = mgrs.substring(j, j + 2);
                try {
                    zone = Integer.parseInt(zone_string);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid number format!");
                    return null;
                }
                if (zone < 1 || zone > 60) {
                    ProjectionErrorHandler
                            .handleError(ProjectionErrorHandler.MGRS_STR_ERROR);
                    return null;
                }
            } else {
                zone = 0;
            }
        } else {
            ProjectionErrorHandler
                    .handleError(ProjectionErrorHandler.MGRS_STR_ERROR);
            return null;
        }
        j = i;
        //
        while (Character.isLetter(mgrs.charAt(i))) {
            i++;
        }
        int num_letters = i - j;
        if (num_letters == 3) {
            // get letters
            letters[0] = (Character.toUpperCase(mgrs.charAt(j)) - 'A');
            if (letters[0] == LETTER_I || letters[0] == LETTER_O) {
                ProjectionErrorHandler
                        .handleError(ProjectionErrorHandler.MGRS_STR_ERROR);
                return null;
            }
            letters[1] = (Character.toUpperCase(mgrs.charAt(j + 1)) - 'A');
            if (letters[1] == LETTER_I || letters[1] == LETTER_O) {
                ProjectionErrorHandler
                        .handleError(ProjectionErrorHandler.MGRS_STR_ERROR);
                return null;
            }
            letters[2] = (Character.toUpperCase(mgrs.charAt(j + 2)) - 'A');
            if (letters[2] == LETTER_I || letters[2] == LETTER_O) {
                ProjectionErrorHandler
                        .handleError(ProjectionErrorHandler.MGRS_STR_ERROR);
                return null;
            }
        } else {
            ProjectionErrorHandler
                    .handleError(ProjectionErrorHandler.MGRS_STR_ERROR);
            return null;
        }
        j = i;
        while (i < mgrs.length() && Character.isDigit(mgrs.charAt(i))) {
            i++;
        }
        num_digits = i - j;
        if (num_digits <= 10 && num_digits % 2 == 0) {
            long east = -1;
            long north = -1;
            double multiplier;
            // get easting & northing
            int n = num_digits / 2;
            precision = n;
            if (n > 0) {
                String east_string = mgrs.substring(j, j + n);
                String north_string = mgrs.substring(j + n, j + 2 * n);
                try {
                    east = Integer.parseInt(east_string);
                    north = Integer.parseInt(north_string);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid number format!");
                    return null;
                }
                multiplier = Math.pow(10.0, 5 - n);
                easting = east * multiplier;
                northing = north * multiplier;
            } else {
                easting = 0.0;
                northing = 0.0;
            }
        } else {
            ProjectionErrorHandler
                    .handleError(ProjectionErrorHandler.MGRS_STR_ERROR);
            return null;
        }
        //
        return new MGRSCoordinate(zone, letters, easting, northing, precision);
    }

    /**
     * This method sets the letter range used for the 2nd letter in the MGRS coordinate string, based on the set number of the utm zone. It
     * also sets the false northing using a value of A for the second letter of the grid square, based on the grid pattern and set number of
     * the utm zone.
     * 
     * @param zone zone number.
     * @return 2nd letter low number, 2nd letter high number and false northing.
     */
    public static ReducedUPSConstant getGridValues(long zone) {
        long ltr2_low_value;
        long ltr2_high_value;
        double false_northing;
        boolean aa_pattern; /* Pattern based on ellipsoid code */

        long set_number = zone % 6; /* Set number (1-6) based on UTM zone number */

        if (set_number == 0) {
            set_number = 6;
        }

        if (!ProjectionConstants.getMGRSEllipsoidCode()
                .equals(ProjectionConstants.CLARKE_1866)
                || !ProjectionConstants.getMGRSEllipsoidCode()
                        .equals(ProjectionConstants.CLARKE_1880)
                || !ProjectionConstants.getMGRSEllipsoidCode()
                        .equals(ProjectionConstants.BESSEL_1841)
                || !ProjectionConstants.getMGRSEllipsoidCode()
                        .equals(ProjectionConstants.BESSEL_1841_NAMIBIA)) {
            aa_pattern = false;
        } else {
            aa_pattern = true;
        }

        if (set_number == 1 || set_number == 4) {
            ltr2_low_value = LETTER_A;
            ltr2_high_value = LETTER_H;
        } else if (set_number == 2 || set_number == 5) {
            ltr2_low_value = LETTER_J;
            ltr2_high_value = LETTER_R;
        } else {
            ltr2_low_value = LETTER_S;
            ltr2_high_value = LETTER_Z;
        }

        // False northing at A for second letter of grid square
        if (!aa_pattern) {
            if ((set_number % 2) == 0) {
                false_northing = 1500000.0;
            } else {
                false_northing = 0.0;
            }
        } else {
            if ((set_number % 2) == 0) {
                false_northing = 500000.0;
            } else {
                false_northing = 1000000.00;
            }
        }
        //
        return new ReducedUPSConstant(ltr2_low_value, ltr2_high_value,
                false_northing);
    }

    /*
     * This method calculates an MGRS coordinate string based on the zone, latitude, easting and northing.
     * @param utmc UTM coordinate.
     * @param latitude latitude in radians.
     * @param precision precision.
     * @return MGRS coordinate string.
     */
    public static String proceedUTMToMGRS(UTMCoordinate utmc, double latitude,
            long precision) {
        double false_northing; /* False northing for 3rd letter */
        double grid_easting; /* Easting used to derive 2nd letter of MGRS */
        double grid_northing; /* Northing used to derive 3rd letter of MGRS */
        long ltr2_low_value; /* 2nd letter range - low number */
        int[] letters = new int[MGRS_LETTERS]; /* Number location of 3 letters in alphabet */
        double divisor;
        //
        long zone = utmc.getZone();
        double easting = utmc.getEasting();
        double northing = utmc.getNorthing();

        // Round easting and northing values
        divisor = Math.pow(10.0, (5 - precision));
        easting = roundMGRS(easting / divisor) * divisor;
        northing = roundMGRS(northing / divisor) * divisor;

        ReducedUPSConstant rups = getGridValues(zone);
        ltr2_low_value = rups.getLtr2LowValue();
        rups.getLtr2HighValue();
        false_northing = rups.getFalseNorthing();

        letters[0] = getLatitudeLetter(latitude);

        if (letters[0] != -1) {
            grid_northing = northing;
            if (grid_northing == 1.e7) {
                grid_northing = grid_northing - 1.0;
            }

            while (grid_northing >= 2000000) {
                grid_northing = grid_northing - 2000000;
            }
            grid_northing = grid_northing - false_northing;

            if (grid_northing < 0.0) {
                grid_northing = grid_northing + 2000000;
            }

            letters[2] = (int) (grid_northing / 100000);
            if (letters[2] > LETTER_H) {
                letters[2] = letters[2] + 1;
            }

            if (letters[2] > LETTER_N) {
                letters[2] = letters[2] + 1;
            }

            grid_easting = easting;
            if ((letters[0] == LETTER_V && zone == 31)
                    && (grid_easting == 500000.0)) {
                grid_easting = grid_easting - 1.0; /* SUBTRACT 1 METER */
            }
            letters[1] = (int) ltr2_low_value + (int) (grid_easting / 100000)
                    - 1;
            if (ltr2_low_value == LETTER_J && letters[1] > LETTER_N) {
                letters[1] = letters[1] + 1;
            }
            //
            return makeMGRSString(zone, letters, easting, northing, precision);
        }
        //
        ProjectionErrorHandler.handleError(ProjectionErrorHandler.LAT_ERROR);
        return null;
    }

    /*
     * This method receives the ellipsoid parameters and sets the corresponding state variables.
     * @param ellipsoid_code 2-letter code for ellipsoid.
     * @return true if no error occured, false otherwise.
     */
    public boolean setMGRSParameters(String ellipsoid_code) {
        double inv_f = 1 / ProjectionConstants.ellipsFlattening;
        // Semi-major axis must be greater than zero
        if (ProjectionConstants.semiMajorAxis <= 0.0) {
            ProjectionErrorHandler.handleError(ProjectionErrorHandler.A_ERROR);
            return false;
        }
        // Inverse flattening must be between 250 and 350
        if (inv_f < 250 || inv_f > 350) {
            ProjectionErrorHandler
                    .handleError(ProjectionErrorHandler.INV_F_ERROR);
            return false;
        }
        ProjectionConstants.setMGRSEllipsoidCode(ellipsoid_code);
        return true;
    }

    /*
     * This method converts Geodetic (latitude and longitude) coordinates to an MGRS coordinate string, according to the current ellipsoid
     * parameters.
     * @param latitude latitude in radians.
     * @param longitude longitude in radians.
     * @param precision precision level of MGRS string.
     * @return MGRS coordinate string.
     */
    public static String convertGeodeticToMGRS(double longitude,
            double latitude, long precision) {
        // Latitude out of range
        if (latitude < -PI_OVER_2 || latitude > PI_OVER_2) {
            ProjectionErrorHandler
                    .handleError(ProjectionErrorHandler.LAT_ERROR);
            return null;
        }
        // Longitude out of range
        if (longitude < -Math.PI || longitude > (2 * Math.PI)) {
            ProjectionErrorHandler
                    .handleError(ProjectionErrorHandler.LON_ERROR);
            return null;
        }
        // Invalid precision parameter
        if (precision < 0 || precision > MAX_PRECISION) {
            ProjectionErrorHandler
                    .handleError(ProjectionErrorHandler.MGRS_PRECISION_ERROR);
            return null;
        }

        if (latitude < MIN_UTM_LAT || latitude > MAX_UTM_LAT) {
            UPSCoordinate upsc = UPSConversion.convertGeodeticToUPS(longitude,
                                                                    latitude);
            if (upsc != null) {
                return convertUPSToMGRS(upsc, precision);
            }
        } else {
            boolean succ = UTMConversion.setUTMParameters(0);
            UTMCoordinate utmc = UTMConversion.convertGeodeticToUTM(longitude,
                                                                    latitude);
            if (succ && utmc != null) {
                return proceedUTMToMGRS(utmc, latitude, precision);
            }
        }
        return null;
    }

    /*
     * This method converts an MGRS coordinate string to Geodetic (latitude and longitude) coordinates according to the current ellipsoid
     * parameters.
     * @param mgrs MGRS coordinate string.
     * @return [longitude in radians, latitude in radians].
     */
    public static double[] convertMGRSToGeodetic(String mgrs) {
        boolean zone_exists = checkZone(mgrs);
        if (zone_exists) {
            UTMCoordinate utmc = convertMGRSToUTM(mgrs);
            boolean succ = UTMConversion.setUTMParameters(0);
            if (utmc != null && succ) {
                return UTMConversion.convertUTMToGeodetic(utmc);
            }
        } else {
            UPSCoordinate upsc = convertMGRSToUPS(mgrs);
            if (upsc != null) {
                return UPSConversion.convertUPSToGeodetic(upsc);
            }
        }
        return null;
    }

    /**
     * This method converts UTM (zone, easting, and northing) coordinates to an MGRS coordinate string, according to the current ellipsoid
     * parameters.
     * 
     * @param utmc UTM coordinate.
     * @param precision precision level of MGRS string.
     * @return MGRS coordinate string.
     */
    public static String convertUTMToMGRS(UTMCoordinate utmc, long precision) {
        double latitude; /* Latitude of UTM point */
        double longitude; /* Longitude of UTM point */
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
        if (precision < 0 || precision > MAX_PRECISION) {
            ProjectionErrorHandler
                    .handleError(ProjectionErrorHandler.MGRS_PRECISION_ERROR);
            return null;
        }

        boolean succ = UTMConversion.setUTMParameters(0);
        if (succ) {
            double[] lon_lat = UTMConversion.convertUTMToGeodetic(utmc);
            if (lon_lat != null) {
                latitude = lon_lat[1];
                longitude = lon_lat[0];

                // Special check for rounding to (truncated) eastern edge of zone 31V
                if (zone == 31 && latitude >= 56.0 * DEG_TO_RAD
                        && latitude < 64.0 * DEG_TO_RAD
                        && longitude >= 3.0 * DEG_TO_RAD) {
                    // Reconvert to UTM zone 32
                    succ = UTMConversion.setUTMParameters(32);
                    if (!succ) {
                        return null;
                    }
                    utmc = UTMConversion.convertGeodeticToUTM(latitude,
                                                              longitude);
                }
                if (utmc != null) {
                    return proceedUTMToMGRS(utmc, latitude, precision);
                }
            }
        }
        return null;
    }

    /*
     * This method converts an MGRS coordinate string to UTM projection (zone, hemisphere, easting and northing) coordinates according to
     * the current ellipsoid parameters.
     * @param mgrs MGRS coordinate string.
     * @return UTM coordinate.
     */
    public static UTMCoordinate convertMGRSToUTM(String mgrs) {

        MGRSCoordinate mgrsc = breakMGRSString(mgrs);
        long zone = mgrsc.getZone();
        int[] letters = mgrsc.getLetters();
        double easting = mgrsc.getEasting();
        double northing = mgrsc.getNorthing();
        long in_precision = (long) mgrsc.getPrecision();

        if (zone == 0) {
            ProjectionErrorHandler
                    .handleError(ProjectionErrorHandler.MGRS_STR_ERROR);
            return null;
        } else {
            if (letters[0] == LETTER_X
                    && (zone == 32 || zone == 34 || zone == 36)) {
                ProjectionErrorHandler
                        .handleError(ProjectionErrorHandler.MGRS_STR_ERROR);
                return null;
            } else {
                char hemisphere = (letters[0] < LETTER_N) ? 'S' : 'N';

                ReducedUPSConstant rupsc = getGridValues(zone);
                long ltr2_low_value = rupsc.getLtr2LowValue();
                long ltr2_high_value = rupsc.getLtr2HighValue();
                double false_northing = rupsc.getFalseNorthing();

                /*
                 * Check that the second letter of the MGRS string is within the range of valid second letter values Also check that the
                 * third letter is valid
                 */
                if (letters[1] < ltr2_low_value || letters[1] > ltr2_high_value
                        || letters[2] > LETTER_V) {
                    ProjectionErrorHandler
                            .handleError(ProjectionErrorHandler.MGRS_STR_ERROR);
                    return null;
                }
                // Northing for 100,000 meter grid square
                double grid_northing = (double) (letters[2]) * 100000
                        + false_northing;
                // Easting for 100,000 meter grid square
                double grid_easting = (double) ((letters[1]) - ltr2_low_value + 1) * 100000;
                if (ltr2_low_value == LETTER_J && letters[1] > LETTER_O) {
                    grid_easting = grid_easting - 100000;
                }
                if (letters[2] > LETTER_O) {
                    grid_northing = grid_northing - 100000;
                }
                if (letters[2] > LETTER_I) {
                    grid_northing = grid_northing - 100000;
                }
                if (grid_northing >= 2000000) {
                    grid_northing = grid_northing - 2000000;
                }
                double min_northing = getLatitudeBandMinNorthing(letters[0]);
                if (min_northing != -1) {
                    double scaled_min_northing = min_northing;
                    while (scaled_min_northing >= 2000000) {
                        scaled_min_northing = scaled_min_northing - 2000000;
                    }

                    grid_northing = grid_northing - scaled_min_northing;
                    if (grid_northing < 0.0) {
                        grid_northing = grid_northing + 2000000;
                    }
                    grid_northing = min_northing + grid_northing;

                    easting = grid_easting + easting;
                    northing = grid_northing + northing;

                    // check that point is within Zone Letter bounds
                    boolean succ = UTMConversion.setUTMParameters(zone);
                    if (!succ) {
                        return null;
                    }
                    UTMCoordinate utmcoord = new UTMCoordinate(zone,
                            hemisphere, easting, northing);
                    double[] lon_lat = UTMConversion
                            .convertUTMToGeodetic(utmcoord);

                    if (lon_lat != null) {
                        double latitude = lon_lat[1];
                        double divisor = Math.pow(10.0, in_precision);
                        double[] range = getLatitudeRange(letters[0]);
                        if (range != null) {
                            double upper_lat_limit = range[0];
                            double lower_lat_limit = range[1];
                            if (!((lower_lat_limit - DEG_TO_RAD / divisor) <= latitude && latitude <= (upper_lat_limit + DEG_TO_RAD
                                    / divisor))) {
                                ProjectionErrorHandler
                                        .handleError(ProjectionErrorHandler.MGRS_STR_ERROR);
                                return null;
                            }
                            return new UTMCoordinate(zone, hemisphere, easting,
                                    northing);
                        }
                    }
                }
            }
        }
        //
        return null;
    }

    /**
     * This method converts UPS (hemisphere, easting, and northing) coordinates to an MGRS coordinate string according to the current
     * ellipsoid parameters.
     * 
     * @param upsc UPS coordinate.
     * @param precision precision level of MGRS string.
     * @return MGRS coordinate string.
     */
    public static String convertUPSToMGRS(UPSCoordinate upsc, long precision) {
        double false_easting; /* False easting for 2nd letter */
        double false_northing; /* False northing for 3rd letter */
        long ltr2_low_value; /* 2nd letter range - low number */
        int[] letters = new int[MGRS_LETTERS]; /* Number location of 3 letters in alphabet */

        char hemisphere = upsc.getHemisphere();
        double easting = upsc.getEasting();
        double northing = upsc.getNorthing();

        if (hemisphere != 'N' && hemisphere != 'S') {
            ProjectionErrorHandler
                    .handleError(ProjectionErrorHandler.HEMISPHERE_ERROR);
            return null;
        }
        if (easting < MIN_EAST_NORTH || easting > MAX_EAST_NORTH) {
            ProjectionErrorHandler
                    .handleError(ProjectionErrorHandler.EASTING_ERROR);
            return null;
        }
        if (northing < MIN_EAST_NORTH || northing > MAX_EAST_NORTH) {
            ProjectionErrorHandler
                    .handleError(ProjectionErrorHandler.NORTHING_ERROR);
            return null;
        }
        if (precision < 0 || precision > MAX_PRECISION) {
            ProjectionErrorHandler
                    .handleError(ProjectionErrorHandler.MGRS_PRECISION_ERROR);
            return null;
        }

        double divisor = Math.pow(10.0, (5 - precision));
        easting = roundMGRS(easting / divisor) * divisor;
        northing = roundMGRS(northing / divisor) * divisor;

        if (hemisphere == 'N') {
            letters[0] = (easting >= 2000000) ? LETTER_Z : LETTER_Y;

            int index = letters[0] - 22;
            ltr2_low_value = ups_constant_table[index].getLtr2LowValue();
            false_easting = ups_constant_table[index].getFalseEasting();
            false_northing = ups_constant_table[index].getFalseNorthing();
        } else {
            letters[0] = (easting >= 2000000) ? LETTER_B : LETTER_A;

            ltr2_low_value = ups_constant_table[letters[0]].getLtr2LowValue();
            false_easting = ups_constant_table[letters[0]].getFalseEasting();
            false_northing = ups_constant_table[letters[0]].getFalseNorthing();
        }

        double grid_northing = northing;
        grid_northing = grid_northing - false_northing;
        letters[2] = (int) (grid_northing / 100000);

        if (letters[2] > LETTER_H) {
            letters[2] = letters[2] + 1;
        }
        if (letters[2] > LETTER_N) {
            letters[2] = letters[2] + 1;
        }

        double grid_easting = easting;
        grid_easting = grid_easting - false_easting;
        letters[1] = (int) (ltr2_low_value + (grid_easting / 100000));

        if (easting < 2000000) {
            if (letters[1] > LETTER_L) {
                letters[1] = letters[1] + 3;
            }
            if (letters[1] > LETTER_U) {
                letters[1] = letters[1] + 2;
            }
        } else {
            if (letters[1] > LETTER_C) {
                letters[1] = letters[1] + 2;
            }
            if (letters[1] > LETTER_H) {
                letters[1] = letters[1] + 1;
            }
            if (letters[1] > LETTER_L) {
                letters[1] = letters[1] + 3;
            }
        }
        //
        return makeMGRSString(0, letters, easting, northing, precision);
    }

    /**
     * This method converts an MGRS coordinate string to UPS (hemisphere, easting, and northing) coordinates, according to the current
     * ellipsoid parameters.
     * 
     * @param mgrs MGRS coordinate string.
     * @return UPS coordinate.
     */
    public static UPSCoordinate convertMGRSToUPS(String mgrs) {
        long ltr2_high_value; /* 2nd letter range - high number */
        long ltr3_high_value; /* 3rd letter range - high number (UPS) */
        long ltr2_low_value; /* 2nd letter range - low number */
        double false_easting; /* False easting for 2nd letter */
        double false_northing; /* False northing for 3rd letter */
        //
        MGRSCoordinate mgrsc = breakMGRSString(mgrs);
        long zone = mgrsc.getZone();
        int[] letters = mgrsc.getLetters();
        double easting = mgrsc.getEasting();
        double northing = mgrsc.getNorthing();
        mgrsc.getPrecision();

        if (zone != 0) {
            ProjectionErrorHandler
                    .handleError(ProjectionErrorHandler.MGRS_STR_ERROR);
            return null;
        } else {
            char hemisphere;
            if (letters[0] >= LETTER_Y) {
                hemisphere = 'N';

                int index = letters[0] - 22;
                ltr2_low_value = ups_constant_table[index].getLtr2LowValue();
                ltr2_high_value = ups_constant_table[index].getLtr2HighValue();
                ltr3_high_value = ups_constant_table[index].getLtr3HighValue();
                false_easting = ups_constant_table[index].getFalseEasting();
                false_northing = ups_constant_table[index].getFalseNorthing();
            } else {
                hemisphere = 'S';

                ltr2_low_value = ups_constant_table[letters[0]]
                        .getLtr2LowValue();
                ltr2_high_value = ups_constant_table[letters[0]]
                        .getLtr2HighValue();
                ltr3_high_value = ups_constant_table[letters[0]]
                        .getLtr3HighValue();
                false_easting = ups_constant_table[letters[0]]
                        .getFalseEasting();
                false_northing = ups_constant_table[letters[0]]
                        .getFalseNorthing();
            }

            /*
             * Check that the second letter of the MGRS string is within the range of valid second letter values Also check that the third
             * letter is valid
             */
            if (letters[1] < ltr2_low_value
                    || letters[1] > ltr2_high_value
                    || (letters[1] == LETTER_D || letters[1] == LETTER_E
                            || letters[1] == LETTER_M || letters[1] == LETTER_N
                            || letters[1] == LETTER_V || letters[1] == LETTER_W)
                    || letters[2] > ltr3_high_value) {
                ProjectionErrorHandler
                        .handleError(ProjectionErrorHandler.MGRS_STR_ERROR);
                return null;
            }

            double grid_northing = (double) letters[2] * 100000
                    + false_northing;
            if (letters[2] > LETTER_I) {
                grid_northing = grid_northing - 100000;
            }
            if (letters[2] > LETTER_O) {
                grid_northing = grid_northing - 100000;
            }
            double grid_easting = (double) ((letters[1]) - ltr2_low_value)
                    * 100000 + false_easting;
            if (ltr2_low_value != LETTER_A) {
                if (letters[1] > LETTER_L) {
                    grid_easting = grid_easting - 300000.0;
                }
                if (letters[1] > LETTER_U) {
                    grid_easting = grid_easting - 200000.0;
                }
            } else {
                if (letters[1] > LETTER_C) {
                    grid_easting = grid_easting - 200000.0;
                }
                if (letters[1] > LETTER_I) {
                    grid_easting = grid_easting - 100000;
                }
                if (letters[1] > LETTER_L) {
                    grid_easting = grid_easting - 300000.0;
                }
            }

            easting = grid_easting + easting;
            northing = grid_northing + northing;
            //
            return new UPSCoordinate(hemisphere, easting, northing);
        }
    }

}

/**
 * Helper class. Used in MGRSConversion.java .
 */
class LatitudeBand {
    // letter representing latitude band
    int letter;
    // minimum northing for latitude band
    double min_northing;
    // upper latitude for latitude band
    double north;
    // lower latitude for latitude band
    double south;

    /**
     *
     */
    public LatitudeBand(int letter, double min_northing, double north,
            double south) {
        this.letter = letter;
        this.min_northing = min_northing;
        this.north = north;
        this.south = south;
    }

    /**
     *
     */
    public int getLetter() {
        return letter;
    }

    /**
     *
     */
    public double getMinNorthing() {
        return min_northing;
    }

    /**
     *
     */
    public double getNorth() {
        return north;
    }

    /**
     *
     */
    public double getSouth() {
        return south;
    }
}

/**
 * Helper class. Used in MGRSConversion.java .
 */
class UPSConstant {
    // letter representing latitude band
    int letter;
    // 2nd letter range - high number
    long ltr2_low_value;
    // 2nd letter range - low number
    long ltr2_high_value;
    // 3rd letter range - high number (UPS)
    long ltr3_high_value;
    // False easting based on 2nd letter
    double false_easting;
    // False northing based on 3rd letter
    double false_northing;

    /**
     *
     */
    public UPSConstant(int letter, long ltr2_low_value, long ltr2_high_value,
            long ltr3_high_value, double false_easting, double false_northing) {
        this.letter = letter;
        this.ltr2_low_value = ltr2_low_value;
        this.ltr2_high_value = ltr2_high_value;
        this.ltr3_high_value = ltr3_high_value;
        this.false_easting = false_easting;
        this.false_northing = false_northing;
    }

    /**
     *
     */
    public int getLetter() {
        return letter;
    }

    /**
     *
     */
    public long getLtr2LowValue() {
        return ltr2_low_value;
    }

    /**
     *
     */
    public long getLtr2HighValue() {
        return ltr2_high_value;
    }

    /**
     *
     */
    public long getLtr3HighValue() {
        return ltr3_high_value;
    }

    /**
     *
     */
    public double getFalseEasting() {
        return false_easting;
    }

    /**
     *
     */
    public double getFalseNorthing() {
        return false_northing;
    }
}

/**
 * Helper class. Used in MGRSConversion.java .
 */
class ReducedUPSConstant {
    // 2nd letter range - high number
    long ltr2_low_value;
    // 2nd letter range - low number
    long ltr2_high_value;
    // False northing based on 3rd letter
    double false_northing;

    /**
     *
     */
    public ReducedUPSConstant(long ltr2_low_value, long ltr2_high_value,
            double false_northing) {
        this.ltr2_low_value = ltr2_low_value;
        this.ltr2_high_value = ltr2_high_value;
        this.false_northing = false_northing;
    }

    /**
     *
     */
    public long getLtr2LowValue() {
        return ltr2_low_value;
    }

    /**
     *
     */
    public long getLtr2HighValue() {
        return ltr2_high_value;
    }

    /**
     *
     */
    public double getFalseNorthing() {
        return false_northing;
    }
}
