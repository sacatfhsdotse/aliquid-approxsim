package StratmasClient.proj;

/**
 * This class implements the MGRS coordinate.
 */
class MGRSCoordinate {
    // UTM zone.
    private long zone;
    // MGRS coordinate string letters
    private int[] letters;
    // Easting (X) in meters.
    private double easting;
    // Northing (Y) in meters.
    private double northing;
    // Precision level of MGRS string.
    private long precision;

    /**
     * Creates new MGRS coordinate.
     */
    public MGRSCoordinate(long zone, int[] letters, double easting,
            double northing, long precision) {
        this.zone = zone;
        this.letters = new int[letters.length];
        for (int i = 0; i < letters.length; i++) {
            this.letters[i] = letters[i];
        }
        this.easting = easting;
        this.northing = northing;
        this.precision = precision;
    }

    /**
     * Returns the MGRS zone.
     */
    public long getZone() {
        return zone;
    }

    /**
     * Returns the MGRS letters.
     */
    public int[] getLetters() {
        return letters;
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

    /**
     * Returns the precision.
     */
    public double getPrecision() {
        return precision;
    }
}
