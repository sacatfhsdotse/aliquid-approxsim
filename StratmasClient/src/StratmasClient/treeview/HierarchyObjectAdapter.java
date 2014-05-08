package StratmasClient.treeview;

import java.util.Enumeration;
import java.util.Vector;

import StratmasClient.object.StratmasList;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasObjectFactory;
import StratmasClient.filter.StratmasObjectFilter;
import StratmasClient.object.type.TypeFactory;
import StratmasClient.object.type.Type;

/**
 * HierarchyObject adapters are used to visualize an import in a HierarchyImportTree and to keep track of which of the imported objects that
 * are already in the simulation and also to where in the object hierarchy not yet imported objects should be imported.
 * <p>
 * A HierarchyObjectAdapter holds two StratmasObjects. One that was created by the HierarchyImporter and one that is / will be imported to
 * the simulation. The reason for having two copies is that the first one is used to save imformation about the hierarchy and the other to
 * keep track of to which parent an object should be added to when imported to the simulation.
 * 
 * @version 1, $Date: 2006/09/25 10:11:01 $
 * @author Per Alexius
 */
public class HierarchyObjectAdapter extends StratmasObjectAdapter {
    /**
     * Marks if the object this adapter holds has been imported to the simulation.
     */
    private boolean mUsed = false;
    /**
     * Holds a reference to the object that is or will be imported to the simulation.
     */
    private StratmasObject mCopyForSim;

    /**
     * Creates a new HierarchyObjectAdapter.
     * 
     * @param obj the object to adapt.
     * @param filter The filter used to filter out objects.
     */
    public HierarchyObjectAdapter(StratmasObject obj,
            StratmasObjectFilter filter) {
        if (!filter.pass(obj) && !(obj instanceof StratmasList)) {
            throw new AssertionError("Object " + obj + " of type "
                    + obj.getType().getName()
                    + " does not pass through filter " + filter);
        }

        this.setUserObject(obj);
        this.filter = filter;

        if (obj instanceof StratmasList) {
            mCopyForSim = StratmasObjectFactory.cloneObject(obj);
        } else {
            typeDependentInit(obj.getType());
        }

    }

    /**
     * Initializes the children of this object.
     */
    protected void createChildren() {
        if (stratmasObject == null) {
            this.children = new Vector();
        } else {
            this.children = new Vector();
            for (Enumeration e = stratmasObject.children(); e.hasMoreElements();) {
                StratmasObject sObj = (StratmasObject) e.nextElement();
                if (sObj instanceof StratmasList) {
                    for (Enumeration i = sObj.children(); i.hasMoreElements();) {
                        StratmasObject objInList = (StratmasObject) i
                                .nextElement();
                        if (filter.pass(objInList)) {
                            silentAdd(new HierarchyObjectAdapter(objInList,
                                    filter), this.children.size());
                        }
                    }
                } else if (filter.pass(sObj)) {
                    silentAdd(new HierarchyObjectAdapter(sObj, filter),
                              this.children.size());
                }
            }

            sort();
        }
    }

    /**
     * Helper for performing initialization that differs between types.
     * 
     * @param t The Type to perform initialization for.
     */
    private void typeDependentInit(Type t) {
        if (t.getName().equals("MilitaryUnit")) {
            StratmasObject o = getUserObject();
            mCopyForSim = StratmasObjectFactory.cloneObject(o);
            // But we don't want the children and it's easier to
            // remove the list and add a new one instead of
            // removing all the elements in the list.
            mCopyForSim.getChild("subunits").remove();
            mCopyForSim.add(StratmasObjectFactory.createList(TypeFactory
                    .getType("MilitaryUnit").getSubElement("subunits")));
        }
    }

    /**
     * Accessor for the simulation copy of the adapted object.
     * 
     * @return The simulation copy of the adapted object.
     */
    public StratmasObject getCopyForSim() {
        return mCopyForSim;
    }

    /**
     * Accessor for the used flag.
     * 
     * @return The used flag.
     */
    public boolean isUsed() {
        return mUsed;
    }

    /**
     * Mutator for the used flag
     * 
     * @param used New value for the used flag.
     */
    public void setUsed(boolean used) {
        mUsed = used;
    }

    public void unselectRecursively() {
        for (Enumeration en = children(); en.hasMoreElements();) {
            ((HierarchyObjectAdapter) en.nextElement()).unselectRecursively();
        }
        getCopyForSim().fireSelected(false);
    }

    /**
     * For debugging
     * 
     * @return Some info on the adapted object.
     */
    public String toString() {
        String ret = getUserObject().toString() + " --- "
                + getCopyForSim().toString();
        for (Enumeration en = children(); en.hasMoreElements();) {
            ret += "\n" + en.nextElement().toString();
        }
        return ret;
    }

    /**
     * Adds the provided adapter as a child to this.
     * 
     * @param child the child to add.
     */
    protected void add(StratmasObject child, int index) {
        add(new HierarchyObjectAdapter(child, filter), index);
    }

}
