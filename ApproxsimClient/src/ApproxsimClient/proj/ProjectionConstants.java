package ApproxsimClient.proj;

/**
 * This class is used to define the global variables used in the coordinate conversion process.
 */
public class ProjectionConstants {
    // Ellipsoid Parameters, default to WGS 84
    /**
     * Semi-major axis of ellipsoid i meters.
     */
    public static double semiMajorAxis = 6378137.0;
    /**
     * Flattening of ellipsoid.
     */
    public static double ellipsFlattening = 1 / 298.257223563;
    /**
     * Eccentricity.
     */
    public static double eccentricity = 0.08181919084262188000;
    /**
     * Isometeric to geodetic latitude parameters, default to WGS 84.
     */
    public static double ap = 6367449.1458008;
    public static double bp = 16038.508696861;
    public static double cp = 16.832613334334;
    public static double dp = 0.021984404273757;
    public static double ep = 3.1148371319283e-005;
    /**
     * Maximum variance for easting values for WGS 84.
     */
    public static double deltaEasting = 40000000.0;
    /**
     * Maximum variance for northing values for WGS 84.
     */
    public static double deltaNorthing = 40000000.0;
    /**
     * The actual ellipsoid code.
     */
    public static String mgrsEllipsoidCode = "WE";
    /**
     * CLARKE_1866 : Ellipsoid code for CLARKE_1866
     */
    public static final String CLARKE_1866 = "CC";
    /**
     * CLARKE_1880 : Ellipsoid code for CLARKE_1880
     */
    public static final String CLARKE_1880 = "CD";
    /**
     * BESSEL_1841 : Ellipsoid code for BESSEL_1841
     */
    public static final String BESSEL_1841 = "BR";
    /**
     * BESSEL_1841_NAMIBIA : Ellipsoid code for BESSEL 1841 (NAMIBIA)
     */
    public static final String BESSEL_1841_NAMIBIA = "BN";

    /**
     * Sets the semi-major axis.
     */
    public static void setSemiMajorAxis(double smAxis) {
        semiMajorAxis = smAxis;
        updateGeodeticLatParameters();
    }

    /**
     * Sets the ellipsoid flattening.
     */
    public static void setEllipsFlattening(double eFlattening) {
        ellipsFlattening = eFlattening;
        updateGeodeticLatParameters();
    }

    /**
     * Sets the eccentricity.
     */
    public static void setEccentricity(double ecc) {
        eccentricity = ecc;
    }

    /**
     * Updades the parameters
     */
    private static void updateGeodeticLatParameters() {
        // Semi-minor axis of ellipsoid, in meters
        double tranMerc_b = ProjectionConstants.semiMajorAxis
                * (1 - ProjectionConstants.ellipsFlattening);

        // True meridianal constants
        double tn = (ProjectionConstants.semiMajorAxis - tranMerc_b)
                / (ProjectionConstants.semiMajorAxis + tranMerc_b);
        double tn2 = tn * tn;
        double tn3 = tn2 * tn;
        double tn4 = tn3 * tn;
        double tn5 = tn4 * tn;

        ap = ProjectionConstants.semiMajorAxis
                * (1.e0 - tn + 5.e0 * (tn2 - tn3) / 4.e0 + 81.e0 * (tn4 - tn5) / 64.e0);
        bp = 3.e0 * ProjectionConstants.semiMajorAxis
                * (tn - tn2 + 7.e0 * (tn3 - tn4) / 8.e0 + 55.e0 * tn5 / 64.e0)
                / 2.e0;
        cp = 15.e0 * ProjectionConstants.semiMajorAxis
                * (tn2 - tn3 + 3.e0 * (tn4 - tn5) / 4.e0) / 16.0;
        dp = 35.e0 * ProjectionConstants.semiMajorAxis
                * (tn3 - tn4 + 11.e0 * tn5 / 16.e0) / 48.e0;
        ep = 315.e0 * ProjectionConstants.semiMajorAxis * (tn4 - tn5) / 512.e0;
    }

    /**
     * Sets the maximum variance for easting values.
     */
    public static void setDeltaEasting(double dEasting) {
        deltaEasting = dEasting;
    }

    /**
     * Sets the maximum variance for northing values.
     */
    public static void setDeltaNorthing(double dNorthing) {
        deltaNorthing = dNorthing;
    }

    /**
     * Sets the actual ellipsoid code.
     */
    public static void setMGRSEllipsoidCode(String code) {
        mgrsEllipsoidCode = code;
    }

    /**
     * Returns the actual ellipsoid code.
     */
    public static String getMGRSEllipsoidCode() {
        return mgrsEllipsoidCode;
    }

}
