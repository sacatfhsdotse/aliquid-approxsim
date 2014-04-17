package StratmasClient.map;

/**
 * This class contains necessary measures and methods used in STRATMAS map manipulations.
 * 
 * @version 1.0
 * @author Amir Filipovic
 */
public class GeoMath {
    /**
     * Radius of the earth (m).
     */
    public static final double R = 6371000;
    /**
     * Circumference of the earth (m) ie. C = 2*pi*R.
     */
    public static final double C = 40030174;
    /**
     * One degree latitude (m).
     */
    public static final double oneDegreeLat = 111000;

    /**
     * Computes great circle distance between two points. The input parameters are assumed to be degrees.
     * 
     * @param lat1 latitude of first point.
     * @param lon1 longitude of first point.
     * @param lat2 latitude of second point.
     * @param lon2 longitude of second point.
     * @return great circle distance between the points in meters.
     */
    public static double distanceGC(double lat1, double lon1, double lat2,
            double lon2) {
        // convert degrees to radians
        double a = Math.toRadians(lat1);
        double b = Math.toRadians(lat2);
        double P = Math.toRadians(lon1) - Math.toRadians(lon2);
        P = (Math.abs(P) > 180) ? 360 - Math.abs(P) : Math.abs(P);
        // convert back to degrees
        double cosD = Math.sin(a) * Math.sin(b) + Math.cos(a) * Math.cos(b)
                * Math.cos(P);
        double D = Math.toDegrees(Math.acos(cosD));
        return D * oneDegreeLat;
    }

}
