package StratmasClient.proj;

/**
 * This class implements the UTM coordinate.
 */
public class UTMCoordinate {
    // UTM zone.
    private long zone; 
    // North or South hemisphere.
    private char hemisphere;
    //Easting (X) in meters.
    private double easting;
    //Northing (Y) in meters.
    private double northing;
    
    /**
     * Creates new coordinate.
     */
    public UTMCoordinate(long zone, char hemisphere, double easting, double northing) {
        this.zone = zone;
        this.hemisphere = hemisphere;
        this.easting = easting;
        this.northing = northing;
    }
    
    /**
     * Returns the zone.
     */
    public long getZone() {
        return zone;
    }
    
    /**
     * Returns the hemisphere ('N' or 'S').
     */
    public char getHemisphere() {
        return hemisphere;
    }
    
    /**
     * Returns the easting value.
     */
    public double getEasting() {
        return easting; 
    }
    
    /**
     * Returns the northing value.
     */
    public double getNorthing() {
        return northing;
    }
    
}
