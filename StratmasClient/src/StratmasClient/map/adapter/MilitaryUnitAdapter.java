// $Id: MilitaryUnitAdapter.java,v 1.7 2006/04/18 13:01:16 dah Exp $
/*
 * @(#)MilitaryUnitAdapter.java
 */

package ApproxsimClient.map.adapter;

import ApproxsimClient.object.ApproxsimObject;
import ApproxsimClient.object.ApproxsimEvent;
import ApproxsimClient.object.SymbolIDCode;
import ApproxsimClient.object.ApproxsimInteger;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLUtessellatorCallbackAdapter;
import javax.media.opengl.glu.GLUtessellatorCallback;

/**
 * MilitaryUnitAdapter adapts ApproxsimObjects descendants of MilitaryUnit for viewing on a map window.
 * 
 * @version 1, $Date: 2006/04/18 13:01:16 $
 * @author Daniel Ahlin
 */
public class MilitaryUnitAdapter extends ElementAdapter {

    /**
     * Creates a new ElementAdapter.
     * 
     * @param element the Element to adapt.
     */
    protected MilitaryUnitAdapter(ApproxsimObject element) {
        super(element);
    }

    /**
     * Creates a new ElementAdapter.
     * 
     * @param element the Element to adapt.
     * @param renderSelectionName the integer to use as the base for names in RENDER_SELECTION
     */
    protected MilitaryUnitAdapter(ApproxsimObject element,
            int renderSelectionName) {
        super(element, renderSelectionName);
    }

    /**
     * Updates this adapter when one of the adapted elements children changes. Overriden to react to symbolIDCode changes and personell
     * changes.
     * 
     * @param event the event causing the change.
     */
    protected void childChanged(ApproxsimEvent event) {
        super.childChanged(event);

        ApproxsimObject child = (ApproxsimObject) event.getArgument();
        if (child.getIdentifier().equals("personnel")) {
            isLocationUpdated = false;
            fireAdapterUpdated();
        } else if (child.getIdentifier().equals("symbolIDCode")) {
            isSymbolUpdated = false;
            // Color may have changed, so update location as well.
            isLocationUpdated = false;
            fireAdapterUpdated();
        }
    }

    /**
     * Returns the tessellator callback to use for this ElementAdapter.
     * 
     * @param gld the glDrawable context to use.
     */
    protected GLUtessellatorCallback getLocationTessellatorCallback(
            GLAutoDrawable gld) {
        final GL2 gl = (GL2) gld.getGL();

        SymbolIDCode idCode = (SymbolIDCode) getApproxsimObject()
                .getChild("symbolIDCode");
        ((ApproxsimInteger) getApproxsimObject().getChild("personnel"))
                .getValue();

        getLocationColor(idCode);
        return new GLUtessellatorCallbackAdapter() {
            public void vertex(Object data) {
                double[] p = (double[]) data;
                gl.glColor4d(1.0d, 1.0d, 1.0d, 0.0d);
                gl.glVertex2dv(p, 0);
            }

            public void begin(int type) {
                gl.glBegin(type);
            }

            public void end() {
                gl.glEnd();
            }
        };
    }
}
