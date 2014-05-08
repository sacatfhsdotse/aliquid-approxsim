// $Id: SimpleShape.java,v 1.2 2006/03/31 16:55:51 dah Exp $
/*
 * @(#)SimpleShape.java
 */

package ApproxsimClient.object;

import ApproxsimClient.object.type.Type;

import java.util.Vector;

/**
 * A simpleshape defines a contigous set of two-dimensional points.
 * 
 * @version 1, $Date: 2006/03/31 16:55:51 $
 * @author Daniel Ahlin
 */

public abstract class SimpleShape extends Shape {
    /**
     * Cached polygon version of this object.
     */
    Polygon cachedPolygon;

    /**
     * Error of cached polygon.
     */
    double cachedPolygonError;

    /**
     * True if this is SimpleType represents a hole.
     */
    boolean isHole;

    /**
     * Creates a identified SimpleShape.
     * 
     * @param identifier the identifier of the simpleshape.
     * @param type the type of the object
     */
    protected SimpleShape(String identifier, Type type) {
        super(identifier, type);
    }

    /**
     * Returns a (possibly nonsimple) approximative Polygon representation of this simpleshape. N.B will only recalculate if old cached
     * value has a higher error than the previous.
     * 
     * @param error the maximum distance between the approximation and the closest point on the line.
     */
    public Polygon getPolygon(double error) {
        if (this.cachedPolygon == null || this.cachedPolygonError > error) {
            this.cachedPolygon = this.constructPolygon(error);
            this.cachedPolygonError = error;
        }

        return this.cachedPolygon;
    }

    /**
     * Returns true if this SimpleShape is a hole.
     */
    public boolean isHole() {
        return this.isHole;
    }

    /**
     * Sets the hole status of this SimpleShape
     * 
     * @param hole true means this shape is a hole.
     */
    public void setHole(boolean hole) {
        this.isHole = hole;
    }

    /**
     * Constructs an approximated polygon of this SimpleShape.
     */
    protected abstract Polygon constructPolygon(double error);

    /**
     * Reduces this SimpleShape and adds it to supplied Vector.
     * 
     * @param res vector to add result to.
     */
    public Vector constructSimpleShapes(Vector res) {
        res.add(this);
        return res;
    }

    /**
     * Called when a (direct) child of this has changed. Overriden in order to invalidate any cached approximated polygon
     * 
     * @param child the child that changed
     */
    public void childChanged(ApproxsimObject child, Object initiator) {
        if (this.cachedPolygon != null) {
            cachedPolygon.remove();
            this.cachedPolygon = null;
        }
        super.childChanged(child, initiator);
    }
}
