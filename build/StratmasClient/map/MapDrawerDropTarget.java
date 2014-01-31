package StratmasClient.map;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JOptionPane;

import StratmasClient.Client;
import StratmasClient.Debug;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasObjectFactory;
import StratmasClient.object.Shape;
import StratmasClient.object.StratmasList;
import StratmasClient.StratmasDialog;
import StratmasClient.HierarchyImportSet;
import StratmasClient.map.adapter.MapElementAdapter;
import StratmasClient.map.adapter.MilitaryUnitAdapter;

/**
 * This class implements DropTargetListener for use in the MapDrawer.
 */
class MapDrawerDropTarget implements DropTargetListener {
    /**
     * Reference to the client.
     */
    private Client client;
    /**
     * Reference to the MapDrawer.
     */
    private MapDrawer drawer;
    /**
     * Reference to the Region shown in the map. 
     */
    private Region region; 
    /**
     * List of currenly outlined elements.
     */
    private Hashtable outlinedElements = new Hashtable();
    
    /**
     * Creates new MapDrawerDropTarget.
     */
    protected MapDrawerDropTarget(Client client, MapDrawer drawer, Region region) {
	this.client = client;
	this.drawer = drawer;
	this.region = region;
    }

    /**
     * Called while a drag operation is ongoing, when the mouse pointer enters the operable part 
     * of the drop site for the DropTarget registered with this listener.
     */
    public void dragEnter(DropTargetDragEvent dtde) {
	dtde.acceptDrag(dtde.getDropAction());
    }
    
    /**
     * Not implemented.
     */
    public void dragExit(DropTargetEvent dte) {
    }
    
    /**
     * Called when a drag operation is ongoing, while the mouse pointer is still over the operable 
     * part of the drop site for the DropTarget registered with this listener.
     */
    public void dragOver(DropTargetDragEvent dtde) {
	// get the dragged object
	try {
	    StratmasObject sObj = DraggedElement.getElement();
	    // update the outline of the currently pointed elements only if the dragged element is activity
	    if (sObj != null && sObj.getType().canSubstitute("ActorBasedActivity")) {
		// get the current location
		java.awt.Point pt = dtde.getLocation();
		drawer.setRenderSelectionArea((int)pt.getX(), (int)pt.getY());
		drawer.update();
		// get all MilitaryUnit element at the current location
		Vector elements = drawer.mapDrawableAdaptersUnderCursor(MilitaryUnitAdapter.class);
		Hashtable tmpOutlined = outlinedElements;
		outlinedElements = new Hashtable();
		for (Enumeration e = elements.elements(); e.hasMoreElements();) {
		    MapElementAdapter meAdapter = (MapElementAdapter)e.nextElement();
		    outlinedElements.put(meAdapter.getObject().getReference(), meAdapter);
		    if (tmpOutlined.contains(meAdapter)) {
			tmpOutlined.remove(meAdapter.getObject().getReference());
		    }
		    else {
			// set the outline for new elements
			meAdapter.setOutlined(true);	
		    }
		}
		// remove the outline for unvalid elements
		for (Enumeration e = tmpOutlined.elements(); e.hasMoreElements();) {
		    ((MapElementAdapter)e.nextElement()).setOutlined(false);   
		}
	    }
	    //
	    dtde.acceptDrag(dtde.getDropAction());
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    /**
     * Called when the drag operation has terminated with a drop on the operable part of the drop 
     * site for the DropTarget registered with this listener.
     */
    public void drop(DropTargetDropEvent dtde) {
	boolean dropAccepted = false;
	boolean complete     = false;
	// get the location
	java.awt.Point pt = dtde.getLocation();
	double lat = drawer.convertToLonLat((int)pt.getX(), (int)pt.getY()).getLat();
	double lon = drawer.convertToLonLat((int)pt.getX(), (int)pt.getY()).getLon();
	try {
	     if (dtde.isDataFlavorSupported(StratmasObject.STRATMAS_OBJECT_FLAVOR)) {
		dtde.acceptDrop(DnDConstants.ACTION_LINK);
		// accept the drop
		dropAccepted = true;
		// get the dropped object
		Object obj = dtde.getTransferable().getTransferData(StratmasObject.STRATMAS_OBJECT_FLAVOR);
		// Apple's dnd implementation sucks... We must call the getTransferData method for the string 
		// flavor in order to get a valid callback.
		dtde.getTransferable().getTransferData(DataFlavor.stringFlavor);
		// the dropped object has to be a StratmasObject ...
		if (obj instanceof StratmasObject) {
		    StratmasObject sc = (StratmasObject)obj;
		    StratmasObject loc = (StratmasObject)sc.getChild("location");
		    // special treatment for activities; check if the dropped object is Activity with or without "location"
		    if (sc.getType().canSubstitute("Order")) {
			// check if any military units are located at the location where the activity is dropped
			if (!outlinedElements.isEmpty()) {
			    // add the activity to the military unit
			    if (outlinedElements.size() == 1) {
				MapElementAdapter meAdapter = (MapElementAdapter)outlinedElements.elements().nextElement();
				addActivityToMilitaryUnit(sc, meAdapter.getObject());
				meAdapter.setOutlined(false);
			    }
			    // open a menu over military units at the location of the dropped activity 
			    else {
				// create new menu
				JPopupMenu menu = new JPopupMenu();
				// get the submenu for the pointed military units
				menu.add(getMenuForMilitaryUnits(outlinedElements, sc));
				// show the menu
				menu.show(drawer, (int)pt.getX(), (int)pt.getY());
			    }
			    complete = true;
			}
			// if the activity has "location"
			else if (loc != null && drawer.registredOnMap(sc)){
			    ((Shape)loc).moveTo(lon, lat);
			    complete = true;
			}
			// if the activity doesn't have "location"
			else if (loc == null && drawer.registredOnMap(sc)) {
			    // get the location of the military unit that "owns" the activity
			    StratmasObject l = StratmasObjectFactory.cloneObject(sc.getParent().getParent().getChild("location"));
			    // add "location" to the activity
			    sc.add(l);
			    // update the location of the activity 
			    ((Shape)sc.getChild("location")).moveTo(lon, lat);
			    //
			    complete = true;
			}
			// update the map
			drawer.update();
		    }
		    // if the dropped object has "location"
		    else if (loc != null && loc instanceof Shape) {
			complete = true;
			// If this element isn't part of the simulation then remove it from
			// its current parent and add it to the scenario's element list.
			if (sc.getType().canSubstitute("Element") && !drawer.registredOnMap(sc)) {
			    StratmasObject sim = (StratmasObject)client.getRootObject().children().nextElement();
			    StratmasObject scenario = sim.getChild("scenario");
			    StratmasList targetList;
			    if (sc.getType().canSubstitute("Population")) {
				 targetList = (StratmasList)scenario.getChild("populationCenters");
			    }
			    else if (sc.getType().canSubstitute("MilitaryUnit")) {
				 targetList = (StratmasList)scenario.getChild("militaryUnits");
			    }
			    else {
				 targetList = (StratmasList)scenario.getChild("agencyTeams");
			    }
			    if (targetList.getChild(sc.getIdentifier()) == null || replaceDroppedElement(sc)) {
				 ((Shape)loc).moveTo(lon, lat);
				 sc.remove();
				 targetList.add(sc);
				 sc.fireSelected(true);
			    }
			    else {
				complete = false;
			    }
			}
			if (complete) {
			     ((Shape)loc).moveTo(lon, lat);
			}
			// update the map
			drawer.update();
		    }
		    // if the dropped object is a Shape
		    else if (sc instanceof Shape) {
			boolean accept = false;
			// check if the Shape is a part of the map
			for (StratmasObject walker = sc; walker != null; walker = walker.getParent()) {
			    if (walker.getIdentifier().equals("map")) {
				accept = true;
			    }
			}
			// complete the drop if the Shape is a part of the map
			if (accept) {
			    region.addShape((Shape)sc);
			    drawer.addMapDrawable((Shape)sc);
			    complete = true;
			}
		    }
		}
		else {
		    Debug.err.println(obj.getClass() + " instead of StratmasObject");
		}
		//
		dtde.dropComplete(complete);	
	    }
	    else if (dtde.isDataFlavorSupported(HierarchyImportSet.HIERARCHYIMPORTSET_FLAVOR)) {
		dtde.acceptDrop(DnDConstants.ACTION_LINK);
		dropAccepted = true;
		// get the dropped object
		Object obj = dtde.getTransferable().getTransferData(HierarchyImportSet.HIERARCHYIMPORTSET_FLAVOR);
		// Apple's dnd implementation sucks... We must call the getTransferData method for the string 
		// flavor in order to get a valid callback.
		dtde.getTransferable().getTransferData(DataFlavor.stringFlavor);
		  HierarchyImportSet his = (HierarchyImportSet) obj;
		  // Transfer imported objects to simulation at position lat, lon
		  dtde.dropComplete(his.transferToSimulation(client.getRootObject(), lat, lon));
		  drawer.update();
	    }
	    else {
		dtde.rejectDrop();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    if (dropAccepted) {
		dtde.dropComplete(complete);
		Debug.err.println("Exception thrown - Drop complete false");
	    }
	    else {
		dtde.rejectDrop();
		Debug.err.println("Exception thrown - Drop rejected");
	    }
	}
	//
	DraggedElement.setElement(null);
    }

    /**
     * Not implemented.
     */
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    /**
     * This method opens a dialog when the dropped element already exists on the map.
     * The user can choose to replace the existing element with the dropped element or not.
     *
     * @param so the dropped element.
     *
     * @return true if the dropped element replaces the existing element, false otherwise.
     */
    private boolean replaceDroppedElement(StratmasObject so) {
	int res = StratmasDialog.showOptionDialog(null,
						  "The element '" + so.getIdentifier() +
						  " already exists. Replace it?",
						  "Replace Element?",
						  JOptionPane.YES_NO_OPTION,
						  JOptionPane.QUESTION_MESSAGE,
						  null,
						  null,
						  null);
	return res == JOptionPane.YES_OPTION;
    } 
    
    /**
     * Adds an activity to a military unit.
     *
     * @param activity the activity to add.
     * @param militaryUnit the military unit.
     */
    protected void addActivityToMilitaryUnit(StratmasObject activity, StratmasObject militaryUnit) {
	// check if the activity is registred on the map
	boolean alreadyOnMap = drawer.registredOnMap(activity);
	// add the activity to the military unit
	activity.remove();
	((StratmasList)militaryUnit.getChild("activities")).addWithUniqueIdentifier(activity);

	// add the activity to the map
	drawer.addMapDrawable(activity);
	// if the activity is imported to the simulation and has "location" 
	if (activity.getChild("location") != null && !alreadyOnMap) {
	    new AreaCreationDrawer(drawer.getBasicMap(), region, activity); 
	}

	// Select the activity
	activity.fireSelected(true);
    }


    /**
     * Returns the menu of the military units. The selected military unit gets a new activity.
     *
     * @param militaryUnits the list of military units.
     * @param activity the activity which will be added to the selected military unit.
     */
    protected JMenu getMenuForMilitaryUnits(Hashtable militaryUnits, StratmasObject activity) {
	JMenu submenu = new JMenu("Add activity to : ");
	for (Enumeration e = militaryUnits.elements(); e.hasMoreElements();) {
	    final StratmasObject act = activity; 
	    final StratmasObject sc = (StratmasObject)((MapElementAdapter)e.nextElement()).getObject();
	    final MapDrawerDropTarget self = this;
	    final Hashtable mUnits = militaryUnits;
	    final MapDrawer fdrawer = drawer;
	    JMenuItem item = new JMenuItem(sc.getIdentifier().trim());
	    item.addActionListener(new ActionListener() 
		{
		    public void actionPerformed(ActionEvent event) 
		    {
			for (Enumeration en = mUnits.elements(); en.hasMoreElements(); ) {
			    ((MapElementAdapter)en.nextElement()).setOutlined(false);
			}
			self.addActivityToMilitaryUnit(act, sc);
			fdrawer.update();
		    }
		});
	    submenu.add(item);	
	}	
    	return submenu;
    }
    
}
