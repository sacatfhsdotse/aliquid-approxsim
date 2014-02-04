package StratmasClient;


import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.Enumeration;
import java.util.Vector;
import StratmasClient.treeview.HierarchyObjectAdapter;

import StratmasClient.object.Shape;
import StratmasClient.object.type.TypeFactory;

import StratmasClient.object.StratmasList;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasObjectFactory;



/**
 * HierarchyImportSet is used to transfer objects that should keep
 * their hierarchy to the simulation via drag and drop. It
 * encapsulates a hierarcy of objects where only the 'selected' ones
 * that are not already imported will be imported but where the
 * hierarchy is kept intact for use in future imports. Also see {@link
 * StratmasClient.treeview.HierarchyObjectAdapter}.
 *
 * @version 1, $Date: 2006/05/22 12:14:51 $
 * @author  Per Alexius
*/
public class HierarchyImportSet implements Transferable {
    /**
     * DataFlavor type used to transfer the reference of the object.
     */
     public static final DataFlavor HIERARCHYIMPORTSET_FLAVOR = createHierarchyImportSetFlavor();

     /**
      * The root of the hierarchy.
      */
     private HierarchyObjectAdapter mRoot;
     
     /**
      * Constructs a new import set.
      *
      * @param root The root of the hierarchy.
      */
     public HierarchyImportSet(HierarchyObjectAdapter root) {
          mRoot = root;
     }
     
     /**
      * Accessor for the root of the objects that will be / are
      * imported to the simulation.
      *
      * @return The root of the objects that will be / are
      * imported to the simulation.
      */
     public StratmasObject getRoot(){
          return mRoot.getCopyForSim();
     }

     /**
      * Accessor for the root of the objects that will be / are
      * imported to the simulation.
      *
      * @return The root of the objects that will be / are
      * imported to the simulation.
      */
     public HierarchyObjectAdapter getRootAdapter(){
          return mRoot;
     }

     public void destroy() {
          mRoot.unselectRecursively();
     }

     /**
      * Transfers selected objects in the hierarcy to the simulation
      *  at the specified position.
      *
      * @param root the root where the simulation to transfer objects
      * to lives.
      * @param lat The latitude of the transfered objects.
      * @param lon The longitude of the transfered objects.
      *
      * @return true if the transfer was succesful - false otherwise.
      */
     public boolean transferToSimulation(StratmasObject root, double lat, double lon) {
          StratmasObject simulation = (StratmasObject) root.children().nextElement();

          // If root isn't a list create a temporary list.
          Enumeration en;
          if (mRoot.getUserObject() instanceof StratmasList) {
               en = mRoot.children();
          }
          else {
               Vector v = new Vector();
               v.add(mRoot);
               en = v.elements();
          }
          for ( ; en.hasMoreElements(); ) {
               HierarchyObjectAdapter hoa = (HierarchyObjectAdapter)en.nextElement();
               if (hoa.isSelected() && !hoa.isUsed()) {
                    StratmasObject scenario = (StratmasObject)simulation.getChild("scenario");
                    StratmasObject unitList = (StratmasObject)scenario.getChild("militaryUnits");

                    StratmasObject toAdd = hoa.getCopyForSim();

                    // Try to add the root object.
                    if (unitList.getChild(toAdd.getIdentifier()) == null) {
                         // Set location
                         setLocation(toAdd, lat, lon);

                         unitList.add(toAdd);
                         hoa.setUsed(true);
                         
                         toAdd.fireSelected(true);
                    }
                    else {
                         // Tried to add a root element with the same
                         // identifier as an existing one.
                         Debug.err.println("Object with identifier " + toAdd.getIdentifier() + " already exists.");
                         return false;
                    }
               }
               for (Enumeration en2 = hoa.children(); en2.hasMoreElements(); ) {
                    transferToSimulation((HierarchyObjectAdapter)en2.nextElement(), lat, lon);
               }
          }

          return true;
     }

     /**
      * Helper for transfering objects recursively.
      *
      * @param obj The adapter holding the object to be transfered.
      * @param lat The latitude of the transfered objects.
      * @param lon The longitude of the transfered objects.
      *
      * @return true if the transfer was succesful - false otherwise.
      */
     private void transferToSimulation(HierarchyObjectAdapter obj, double lat, double lon) {
          if (obj.isSelected() && !obj.isUsed()) {
               obj.setUsed(true);
               HierarchyObjectAdapter parentAdapter = (HierarchyObjectAdapter)obj.getParent();
               if (parentAdapter != null) {
                    // Get the StratmasObject that belongs to the simulation
                    StratmasObject parent = (StratmasObject)parentAdapter.getCopyForSim();

                    StratmasObject subunits = parent.getChild("subunits");

                    // If there were no subunits list - create one
                    if (subunits == null) {
                        subunits = StratmasObjectFactory.createList(TypeFactory.getType("MilitaryUnit").getSubElement("subunits"));
                        parent.add(subunits);
                    }
                    // Set location
                    setLocation(obj.getCopyForSim(), lat, lon);
                    
                    // Add unit
                    subunits.add(obj.getCopyForSim());

                    obj.getCopyForSim().fireSelected(true);
               }
          }
          for (Enumeration en = obj.children(); en.hasMoreElements(); ) {
               transferToSimulation((HierarchyObjectAdapter)en.nextElement(), lat, lon);
          }
     }

     /**
      * Helper for setting the location of objects.
      *
      * @param obj The object to set the location for.
      * @param lat The latitude 
      * @param lon The longitude
      *
      * @return true if the transfer was succesful - false otherwise.
      */
     private void setLocation(StratmasObject obj, double lat, double lon) {
          StratmasObject location = obj.getChild("location");
          if (location != null) {
               if (location instanceof Shape) {
                    ((Shape)location).moveTo(lon, lat);
               }
               else {
                    System.err.println("Location not a Shape. Cannot set location for type" + 
                                       location.getType().getName());
               }
          }
          else {
               System.err.println("No 'location' child in object " + obj);
          }
     }

     /**
      * Returns data flavors.
      *
      * @return Supported DataFlavors
      */
     public synchronized DataFlavor[] getTransferDataFlavors() {
          return new DataFlavor[] {HIERARCHYIMPORTSET_FLAVOR, DataFlavor.stringFlavor};
     }


     /**
      * Checks if the given data flavor is supported.
      *
      * @param flavor The flavor to check.
      * @return true if the specified flavor is supported, false
      * otherwise.
      */
     public boolean isDataFlavorSupported(DataFlavor flavor) {
          return HIERARCHYIMPORTSET_FLAVOR.match(flavor) ||
               DataFlavor.stringFlavor.match(flavor);
     }
    
    
     /**
      * Return this object.
      */
     public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
          if (!isDataFlavorSupported(flavor)) {
               throw new UnsupportedFlavorException(flavor);
          }
          if (flavor.match(DataFlavor.stringFlavor)) {
               return getRoot().toXML();
          }
          else {
               return this;
          }
     }

     /**
      * Initializes the HIERARCHYIMPORTSET_FLAVOR.
      *
      * @return The HIERARCHYIMPORTSET_FLAVOR.
      */
     public static final DataFlavor createHierarchyImportSetFlavor() {
          DataFlavor flavor = null;
          try {
               flavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
                                       ";class=StratmasClient.HierarchyImportSet");
          }
          catch (ClassNotFoundException e) {
               System.err.println("Couldn't create HIERARCHYIMPORTSET_FLAVOR");
          }
          return flavor;
     }
}
