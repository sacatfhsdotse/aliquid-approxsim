//         $Id: GLPlotterMouseListener.java,v 1.2 2005/11/24 09:02:34 dah Exp $
/*
 * @(#)GLPlotterMouseListener.java
 */

package StratmasClient.evolver;

import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseWheelEvent;
import java.awt.Point;

import java.awt.Cursor;

/**
 * Mouse controls for a GLPlotter
 *
 * @version 1, $Date: 2005/11/24 09:02:34 $
 * @author  Daniel Ahlin
 */
class GLPlotterMouseListener extends MouseInputAdapter implements MouseWheelListener
{
    /**
     * The plotter this listener use used for.
     */
    GLPlotter plotter;

    /**
     * Whether dragging is in progress
     */
    boolean isDragging = false;

    /**
     * Whether panning is in progress
     */
    boolean isPanning = false;

    /**
     * Whether turning is in progress
     */
    boolean isTurning = false;

    /**
     * The point where the mouse cursor was last seen.
     */
    Point mouseLocation = new Point(0, 0);

    /**
     * The point where the mouse was pressed or null if not currently
     * pressed.
     */
    Point mousePressedLocation = new Point(0, 0);

    /**
     * Whether the mouse is over the component.
     */
    boolean isMouseIn = false;

    /**
     * Creates a new mouselistener for the provided plotter.
     *
     * @param plotter the plotter.
     */
    public GLPlotterMouseListener(GLPlotter plotter)
    {
        this.plotter = plotter;
    }

    /**
     * Returns the plotter this listener use used for.
     */    
    protected GLPlotter getPlotter()
    {
        return this.plotter;
    }

    /**
     * Returns the camera this listener controls.
     */    
    protected Camera getCamera()
    {
        return getPlotter().getCamera();
    }

    /**
     * Called when mouse wheel is moved.
     *
     * @param e the event.
     */
    public void mouseWheelMoved(MouseWheelEvent e) 
    {
        getCamera().move(e.getScrollAmount() * e.getWheelRotation());
    }

    /**
     * Called when mouse is pressed. Starts the following actions:
     * Button  Action
     *      1  Starts dragging
     * 
     * @param e the event.
     */
    public void mousePressed(MouseEvent e)
    {
        setMousePressedLocation(e.getPoint());
        switch (e.getButton()) {
        case MouseEvent.BUTTON1:
            getPlotter().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setIsTurning(true);
            break;
        case MouseEvent.BUTTON2:
            getPlotter().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            setIsPanning(true);
            break;
        case MouseEvent.BUTTON3:
            break;
        default:
            break;
        }
    }

    /**
     * Called when mouse is released. Stops the following actions:
     * Button  Action
     *    Any  Turning
     *    Any  Panning
     *    Any  Dragging
     * 
     * @param e the event.
     */
    public void mouseReleased(MouseEvent e)
    {
        getPlotter().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        setIsDragging(false);
        setIsTurning(false);
        setIsPanning(false);
    }
    
    /**
     * Called when mouse is moved. Action depends on whether
     * currently, rotating, translating, or zooming. 
     * 
     * @param e the event.
     */
    public void mouseMoved(MouseEvent e)
    {
        Point oldPoint = getMouseLocation();
        setMouseLocation(e.getPoint());
        
        if (isTurning()) {
            double dx = getMouseLocation().getX() - oldPoint.getX();
            double dy = getMouseLocation().getY() - oldPoint.getY();

            getCamera().turn(dx, dy);
        } else if (isPanning()) {
            double dx = getMouseLocation().getX() - oldPoint.getX();
            double dy = getMouseLocation().getY() - oldPoint.getY();

            getCamera().pan(dx, dy);
        }
    }

    /**
     * Called when mouse is dragged, does the same thing as mouseMoved.
     * 
     * @param e the event.
     */
    public void mouseDragged(MouseEvent e)
    {
        mouseMoved(e);
    }

    /**
     * Called when mouse enters the component.
     * 
     * @param e the event.
     */    
    public void mouseEntered(MouseEvent e)
    {
        setIsMouseIn(true);
    }

    /**
     * Called when mouse leaves the component.
     * 
     * @param e the event.
     */        
    public void mouseExited(MouseEvent e)
    {
        setIsMouseIn(false);
    }

    /**
     * Updates the location of the mouse pointer
     *
     * @param point the new point
     */
    private void setMouseLocation(Point point)
    {
        this.mouseLocation = point;
    }

    /**
     * Returns the location of the mouse pointer
     */
    private Point getMouseLocation()
    {
        return this.mouseLocation;
    }

    /**
     * Sets the location of where a mouse button was pressed last.
     *
     * @param point the new point
     */
    private void setMousePressedLocation(Point point)
    {
        this.mousePressedLocation = point;
    }

    /**
     * Sets the indicator that the mouse pointer is over the component
     *
     * @param flag true if the mouse is inside the component.
     */
    private void setIsMouseIn(boolean flag)
    {
        this.isMouseIn = flag;
    }

    /**
     * Sets whether a dragging action is occuring.
     *
     * @param flag true if dragging is in progress.
     */
    private void setIsDragging(boolean flag)
    {
        this.isDragging = flag;
    }

    /**
     * Sets whether a turning action is occuring.
     *
     * @param flag true if turning is in progress.
     */
    private void setIsTurning(boolean flag)
    {
        this.isTurning = flag;
    }

    /**
     * Sets whether a panning action is occuring.
     *
     * @param flag true if panning is in progress.
     */
    private void setIsPanning(boolean flag)
    {
        this.isPanning = flag;
    }

    /**
     * Returns true if turning is in progress.
     */
    private boolean isTurning()
    {
        return this.isTurning;
    }

    /**
     * Returns true if panning is in progress.
     */
    private boolean isPanning()
    {
        return this.isPanning;
    }
};
