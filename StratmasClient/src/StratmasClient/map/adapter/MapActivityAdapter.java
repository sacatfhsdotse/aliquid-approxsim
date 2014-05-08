package StratmasClient.map.adapter;

import java.nio.DoubleBuffer;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import com.jogamp.common.nio.Buffers;

import StratmasClient.BoundingBox;
import StratmasClient.map.Projection;
import StratmasClient.object.Shape;
import StratmasClient.object.StratmasEvent;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.SymbolIDCode;

/**
 * MapActivityAdapter adapts StratmasObjects descendants of Activity for viewing on a map window.
 * 
 * @version 1,
 * @author Amir Filipovic
 */
public class MapActivityAdapter extends MapElementAdapter {
    /**
     * True if the connection arrow is drawn.
     */
    boolean arrowDisplayed = false;

    /**
     * Creates a new MapActivityAdapter.
     * 
     * @param mapElement the object to adapt.
     */
    protected MapActivityAdapter(StratmasObject mapElement) {
        super(mapElement);
        if (ownedByMilitaryUnit()) {
            getObject().getParent().getParent().addEventListener(this);
        }
    }

    /**
     * Creates a new MapActivityAdapter.
     * 
     * @param mapElement the object to adapt.
     * @param renderSelectionName the integer to use as the base for names in RENDER_SELECTION
     */
    protected MapActivityAdapter(StratmasObject mapElement,
            int renderSelectionName) {
        super(mapElement, renderSelectionName);
        if (ownedByMilitaryUnit()) {
            getObject().getParent().getParent().addEventListener(this);
        }
    }

    /**
     * Updates (recreates) the displayList that draws the connection arrow between the activity this adapter represents and the military
     * unit which owns it.
     * 
     * @param proj the projection that maps lat and long into GL coordinates.
     * @param gld the gl drawable targeted.
     */
    protected void updateConnectionArrowDisplayList(Projection proj,
            GLAutoDrawable gld) {
        GL2 gl = (GL2) gld.getGL();
        displayListsBuf
                .put(CONNECTION_ARROW_POS,
                     (gl.glIsList(displayListsBuf.get(CONNECTION_ARROW_POS))) ? displayListsBuf
                             .get(CONNECTION_ARROW_POS) : gl.glGenLists(1));

        gl.glNewList(displayListsBuf.get(CONNECTION_ARROW_POS), GL2.GL_COMPILE);
        // Should be fixed ...
        gl.glPopName();
        // Pushes the name for RenderSelection mode.
        if (arrowDisplayed()) {
            double arrowSize = 0.1 * getSymbolScale() * horizontalSymbolSize;
            if (getInvariantSymbolSize()) {
                gl.glMatrixMode(GL2.GL_PROJECTION);
                DoubleBuffer buf = Buffers.newDirectDoubleBuffer(16);
                gl.glGetDoublev(GL2.GL_PROJECTION_MATRIX, buf);
                arrowSize *= 0.00003d / buf.get(0);
            }
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glPushMatrix();
            // get location center of the military unit
            BoundingBox muBox = ((Shape) getObject().getParent().getParent()
                    .getChild("location")).getBoundingBox();
            double muLon = (muBox.getEastLon() + muBox.getWestLon()) / 2;
            double muLat = (muBox.getNorthLat() + muBox.getSouthLat()) / 2;
            double[] muXY = proj.projToXY(muLon, muLat);
            // get location center of the activity
            double[] aXY = proj.projToXY(getLon(), getLat());
            //
            gl.glTranslated(-aXY[0], -aXY[1], 0);
            // get color of the owner
            SymbolIDCode id = (SymbolIDCode) getObject().getParent()
                    .getParent().getChild("symbolIDCode");
            double[] col = getLocationColor(id);
            // draw the line
            gl.glEnable(GL2.GL_LINE_SMOOTH);
            gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_DONT_CARE);
            gl.glColor3d(col[0], col[1], col[2]);
            gl.glLineWidth(2);
            gl.glBegin(GL2.GL_LINES);
            gl.glVertex2d(muXY[0], muXY[1]);
            gl.glVertex2d(aXY[0], aXY[1]);
            gl.glEnd();
            gl.glPopMatrix();
            // drawing the arrow
            
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glPushMatrix();
            // compute the rotation angle
            double ang = Math.acos((muXY[0] - aXY[0])
                    / Math.sqrt(Math.pow(muXY[0] - aXY[0], 2)
                            + Math.pow(muXY[1] - aXY[1], 2)));
            ang = (muXY[1] >= aXY[1]) ? ang : Math.PI + (Math.PI - ang);
            double ang1 = (ang - 30 * Math.PI / 180 < 0) ? ang - 30 * Math.PI
                    / 180 + 2 * Math.PI : ang - 30 * Math.PI / 180;
            double ang2 = (ang + 30 * Math.PI / 180 > 2 * Math.PI) ? ang + 30
                    * Math.PI / 180 - 2 * Math.PI : ang + 30 * Math.PI / 180;
            // gl.glRotated(ang, 0.0, 0.0, 1.0);
            gl.glBegin(GL2.GL_POLYGON);
            gl.glVertex2d(0, 0);
            gl.glVertex2d(arrowSize * Math.cos(ang1),
                          arrowSize * Math.sin(ang1));
            gl.glVertex2d(arrowSize * Math.cos(ang2),
                          arrowSize * Math.sin(ang2));
            gl.glEnd();
            gl.glPopMatrix();
        }
        gl.glEndList();
        connectionArrowUpdated = true;
    }

    /**
     * Returns true if the activity this adapter adapts owns by a military unit, false otherwise.
     */
    public boolean ownedByMilitaryUnit() {
        return getObject().getParent().getParent().getType().getName()
                .equals("MilitaryUnit");
    }

    /**
     * Returns the military unit that owns the activity this adapter adapts.If the activity is not owned by a military unit, null is
     * returned.
     */
    public StratmasObject getOwner() {
        if (ownedByMilitaryUnit()) {
            return (StratmasObject) getObject().getParent().getParent();
        } else {
            return null;
        }
    }

    /**
     * Returns true if the activity this adapter adapts has location, false otherwise.
     */
    public boolean hasLocation() {
        return getObject().getChild("location") != null;
    }

    /**
     * Returns a color used to draw outline of the location of an adapted object.
     */
    public double[] getOutlineColor() {
        if (ownedByMilitaryUnit()) {
            SymbolIDCode id = (SymbolIDCode) getObject().getParent()
                    .getParent().getChild("symbolIDCode");
            return getLocationColor(id);
        } else {
            return super.getOutlineColor();
        }
    }

    /**
     * Returns true if the connection arrow between the avtivity and it's owner is displayed, false otherwise.
     */
    public boolean arrowDisplayed() {
        return arrowDisplayed;
    }

    /**
     * Updates the state of the adapter.
     * 
     * @param displayed if true the connection arrow has to be displayed on the map, otherwise not.
     */
    public void setArrowDisplayed(boolean displayed) {
        arrowDisplayed = displayed;
        if (!hasLocation() || !ownedByMilitaryUnit()) {
            arrowDisplayed = false;
        }
        this.connectionArrowUpdated = false;
        fireAdapterUpdated();
    }

    /**
     * Invalidates all lists (useful for GL switches)
     */
    public void invalidateAllLists() {
        this.isSymbolUpdated = false;
        this.isSelectionMarkerUpdated = false;
        this.isOutlineMarkerUpdated = false;
        this.isLocationUpdated = false;
        this.displayListUpdated = false;
        this.connectionArrowUpdated = false;

        fireAdapterUpdated();
    }

    /**
     * Redraws recompiles display lists.
     * 
     * @param proj the projection used in the map.
     * @param gld the drawable to use.
     */
    public void reCompile(Projection proj, GLAutoDrawable gld) {
        if (!isSymbolUpdated) {
            updateSymbolDisplayList(proj, gld);
        }
        if (!isOutlineMarkerUpdated) {
            updateOutlineMarkerDisplayList(proj, gld);
        }
        if (!isSelectionMarkerUpdated) {
            updateSelectionMarkerDisplayList(proj, gld);
        }
        if (!isLocationUpdated && hasLocation()) {
            updateLocationDisplayList(proj, gld);
        }
        if (!connectionArrowUpdated) {
            updateConnectionArrowDisplayList(proj, gld);
        }
        if (!displayListUpdated) {
            updateDisplayList(proj, gld);
        }
    }

    /**
     * Called when the object this adapter adapts changes.
     * 
     * @param event the event causing the call.
     */
    public void eventOccured(StratmasEvent event) {
        // update the element if the event source is either the activity or the military unit
        // that owns the activity
        if (event.isChildChanged()) {
            childChanged(event);
        }
        // update the element only if the event source is the activity
        else if (event.isRemoved()) {
            if (StratmasObject.class.isAssignableFrom(event.getSource()
                    .getClass())) {
                if (((StratmasObject) event.getSource()).getType()
                        .canSubstitute("Activity")) {
                    getObject().removeEventListener(this);
                    fireAdapterRemoved();
                }
            }
        }
        // update the element only if the event source is the activity
        else if (event.isSelected()) {
            if (StratmasObject.class.isAssignableFrom(event.getSource()
                    .getClass())) {
                if (((StratmasObject) event.getSource()).getType()
                        .canSubstitute("Activity")) {
                    setSelected(true);
                }
            }
        }
        // update the element only if the event source is the activity
        else if (event.isUnselected()) {
            if (StratmasObject.class.isAssignableFrom(event.getSource()
                    .getClass())) {
                if (((StratmasObject) event.getSource()).getType()
                        .canSubstitute("Activity")) {
                    setSelected(false);
                }
            }
        } else if (event.isReplaced()) {
            throw new AssertionError("Replace behavior not implemented");
        }
    }

    /**
     * Updates this adapter when one of the adapted objects children changes.
     * 
     * @param event the event causing the change.
     */
    protected void childChanged(StratmasEvent event) {
        StratmasObject child = (StratmasObject) event.getArgument();
        if (child.getIdentifier().equals("location")) {
            displayListUpdated = false;
            isLocationUpdated = false;
            connectionArrowUpdated = false;
            fireAdapterUpdated();
        }
    }

}
