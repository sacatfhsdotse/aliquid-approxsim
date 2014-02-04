package StratmasClient.proj;

/**
 * This class implements the UPS coordinate.
 */
public class UPSCoordinate {
    // Hemisphere either 'N' or 'S'.
    private char hemisphere;
    // Easting/X in meters.
    private double easting;
    // Northing/Y in meters.
    private double northing;
    
    /**
     * Creates new coordinate.
     */
    public UPSCoordinate(char hemisphere, double easting, double northing) {
        this.hemisphere = hemisphere;
        this.easting = easting;
        this.northing = northing;
    }

    /**
     * Returns the hemisphere.
     */
    public char getHemisphere() {
        return hemisphere;
    }
    
    /**
     * Returns the easting.
     */
    public double getEasting() {
        return easting; 
    }
    
    /**
     * Returns the northing.
     */
    public double getNorthing() {
        return northing;
    }
}

