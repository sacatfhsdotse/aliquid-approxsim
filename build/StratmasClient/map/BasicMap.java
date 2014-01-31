package StratmasClient.map;

import StratmasClient.Client;
import StratmasClient.object.Shape;

/**
 * Basic STRATMAS map controller.
 */
public class BasicMap {
    /**
     * Reference to the client.
     */
    protected Client client;
    /**
     * The main map drawer.
     */
    protected BasicMapDrawer drawer;
    /**
     * The shape displayed in the map.
     */
    protected Shape shape;
    /**
     * The region described by actual shape.
     */
    protected Region region;
    /**
     * The actual projection.
     */
    protected Projection proj;
    
    /**
     * Creates the basic map.
     *
     * @param client reference to the client.
     * @param shape shapes defining geographical region.
     */
    public BasicMap(Client client, Shape shape) {
	// reference to the client
	this.client = client;

	// shape to be displayed
	this.shape = shape;
	
	// Azimuthal Equal Area Projection by default
	proj = new AzEqAreaProj(shape.getBoundingBox());

	// region defined by shape objects
	region = new Region(this, shape);
    }
    
    /**
     * Sets projection of the map.
     */
    public void setProjection(Projection proj) {
	this.proj = proj;
    }
    
    /**
     * Returns actual projection.
     */
    public Projection getProjection() {
	return proj;
    }
    
    /**
     * Returns the client.
     */
    public Client getClient() {
	return client;
    }
    
    /**
     * Returns the shape.
     */
    public Shape getShape() {
	return shape;
    }
    
    /**
     * Removes all objects used in StratMap.
     */
    public void remove() {
	// remove region
	region.remove();
	// remove map drawer
	//drawer.remove();
    }
    
    /**
     * Resets all the map components.
     */
    public void reset() {
	drawer.reset();
    }
    
    /**
     * Returns displayed region.
     */ 
    public Region getRegion() {
	return region;
    }
}
