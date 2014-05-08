package StratmasClient.substrate;

import StratmasClient.object.Shape;

/**
 * This class is used to represent shapes with their values.
 */
public class ShapeValuePair {
    /**
     * The actual shape.
     */
    private Shape shape;
    /**
     * The value of the shape area.
     */
    private double value;
    /**
     * The creation time of the object.
     */
    private long creationTime;
    /**
     * True if the shape is ESRI ie. not created in the editor.
     */
    private boolean isEsri;

    /**
     * Creates new pair.
     */
    public ShapeValuePair(Shape shape, double value, boolean esri) {
        this.shape = shape;
        this.value = value;
        isEsri = esri;
        creationTime = System.currentTimeMillis();
    }

    /**
     * Creates new pair.
     */
    public ShapeValuePair(Shape shape, double value, boolean esri, long cTime) {
        this.shape = shape;
        this.value = value;
        isEsri = esri;
        creationTime = cTime;
    }

    /**
     * Returns the value.
     */
    public double getValue() {
        return value;
    }

    /**
     * Returns the shape.
     */
    public Shape getShape() {
        return shape;
    }

    /**
     * Returns the time of creation of the object.
     */
    public long getCreationTime() {
        return creationTime;
    }

    /**
     * True for ESRI shapes.
     */
    public boolean isEsri() {
        return isEsri;
    }

    /**
     * Displays the values.
     */
    public String toString() {
        return shape.getReference().toString() + " - " + String.valueOf(value);
    }
}
