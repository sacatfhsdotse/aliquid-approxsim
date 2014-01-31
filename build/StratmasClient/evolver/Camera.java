// 	$Id: Camera.java,v 1.2 2006/04/18 13:01:15 dah Exp $
/*
 * @(#)Camera.java
 */

package StratmasClient.evolver;

import javax.media.opengl.glu.GLU;
import java.awt.Component;

/**
 * Class holding camera-variables.
 * @version 1, $Date: 2006/04/18 13:01:15 $
 */

class Camera
{
    /**
     * The component to repaint() on updates.
     */
    Component component;

    /**
     * Yaw setting
     */
    double yaw;

    /**
     * Pitch setting
     */
    double pitch;

    /**
     * The x component of the position of the camera
     */
    double x;

    /**
     * The y component of the position of the camera
     */
    double y;

    /**
     * The z component of the position of the camera
     */
    double z;

    /**
     * The x component of the position of the camera.
     */
    double xDirection = 1.0d;

    /**
     * The y component of the position of the camera.
     */
    double yDirection = 0.0d;

    /**
     * The z component of the position of the camera.
     */
    double zDirection = 0.0d;

    /**
     * Creates a new Camera which will repaint() the provided
     * Component on update.
     *
     * @param component
     */
    public Camera(Component component)
    {
	this.component = component;
    }

    /**
     * Returns the pitch (in degrees)
     */
    public double getPitch()
    {
	return this.pitch;
    }

    /**
     * Returns the yaw (in degrees)
     */
    public double getYaw()
    {
	return this.yaw;
    }

    /**
     * Sets the pitch (in degrees)
     *
     * @param pitch the new pitch.
     */
    public void setPitch(double pitch)
    {
	this.pitch = pitch;
	directionRefresh();
    }

    /**
     * Sets the yaw (in degrees)
     *
     * @param yaw the new yaw.
     */
    public void setYaw(double yaw)
    {
	this.yaw = yaw;
	directionRefresh();
    }

    /**
     * Returns the x component of the position of the camera.
     */
    public double getX()
    {
	return this.x;
    }	

    /**
     * Returns the y component of the position of the camera.
     */
    public double getY() 
    {
	return this.y;
    }	

    /**
     * Returns the z component of the position of the camera.
     */
    public double getZ() 
    {
	return this.z;
    }	

    /**
     * Sets the x the position of the camera.
     *
     * @param x the new x.
     */
    public void setX(double x)
    {
	this.x = x;
	update();
    }	

    /**
     * Sets the y the position of the camera.
     *
     * @param y the new y.
     */
    public void setY(double y)
    {
	this.y = y;
	update();
    }	

    /**
     * Sets the z the position of the camera.
     *
     * @param z the new x.
     */
    public void setZ(double z)
    {
	this.z = z;
	update();
    }

    /**
     * Sets camera position.
     *
     * @param x x component
     * @param y y component
     * @param z z component
     */
    public void setPosition(double x, double y, double z)
    {
	this.x = x;
	this.y = y;
	this.z = z;

	update();
    }

    /**
     * Returns camera position vector [x, y, z].
     */
    public double[] getPosition()
    {
	return new double[] {getX(), getY(), getZ()};
    }

    /**
     * Returns the x component of the direction vector of the camera.
     */
    private double getXDirection()
    {
	return this.xDirection;
    }	

    /**
     * Returns the y component of the direction vector of the camera.
     */
    private double getYDirection()
    {
	return this.yDirection;
    }	

    /**
     * Returns the z component of the direction vector of the camera.
     */
    private double getZDirection()
    {
	return this.zDirection;
    }	

    /**
     * Returns camera direction vector [x, y, z].
     */
    private double[] getDirection()
    {
 	return new double[] {getXDirection(), getYDirection(), getZDirection()};
    }

    /**
     * Sets camera direction vector.
     *
     * @param x x component
     * @param y y component
     * @param z z component
     */
    private void setDirection(double x, double y, double z)
    {
	this.xDirection = x;
	this.yDirection = y;
	this.zDirection = z;
    }

    /**
     * Recalculates direction
     */
    private void directionRefresh() 
    {
	setDirection(Math.cos(getYaw() - Math.PI) * Math.cos(-(getPitch() + Math.PI)),
		     Math.sin(getYaw() - Math.PI) * Math.cos(-(getPitch() + Math.PI)),
		     Math.sin(-(getPitch() + Math.PI)));
	update();
    }
		
    public void lookAt(double x, double y, double z) 
    {
	double dx = x - getX();
	double dy = y - getY();
	double dz = z - getZ();
	double h = Math.sqrt(dx * dx + dy * dy);
	double as = Math.atan2(dy, dx);
	double angle = (180.0 * as) / Math.PI;

	setYaw((angle * Math.PI) / 180.0);
	
	angle = Math.PI + (180.0 * Math.acos(dz / h)) / Math.PI;
	angle = 90.0 - angle;
	setPitch((angle * Math.PI) / 180.0);
    }

    /**
     * Moves the camera the specified distance.
     *
     * @param distance
     */
    public void move(double distance)
    {
	setPosition(getX() + distance * getXDirection(),
		    getY() + distance * getYDirection(),
		    getZ() + distance * getZDirection());
    }

    /**
     * Turns the camera the specified distance.
     *
     * @param yawDistance
     * @param pitchDistance
     */
    public void turn(double yawDistance, double pitchDistance)
    {
	    setYaw(getYaw() - 
		   (((yawDistance * Math.PI) / 180.0) / 2.0));
	    setPitch(getPitch() - 
		     (((pitchDistance * Math.PI) / 180.0) / 2.0));
    }

    /**
     * Pans the camera the specified distance.
     *
     * @param hDistance horizontal distance
     * @param vDistance vertical distance
     */
    public void pan(double hDistance, double vDistance)
    {
	setPosition(getX() + hDistance * getXDirection(),
		    getY(),
		    getZ() + vDistance * getZDirection());
    }


    /**
     * Called when the view has changed.
     */
    void update()
    {
	Component c = getComponent();
	if (c != null) {
	    c.repaint();
	}
    }

    /**
     * Returns the component that should be repainted when the view
     * changes.
     */
    private Component getComponent()
    {
	return this.component;
    }

    /**
     * Sets the view using the provided glu.
     *
     * @param glu the glu to set the view in.
     */
    public void setView(GLU glu)
    {
	glu.gluLookAt(getX(), getY(), getZ(),
		      getX() + getXDirection(), 
		      getY() + getYDirection(), 
		      getZ() + getZDirection(),
		      0.0, 0.0, 1.0);
    }

}
