package StratmasClient.map;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;

import StratmasClient.Client;
import StratmasClient.Configuration;
import StratmasClient.BoundingBox;
import StratmasClient.Debug;
import StratmasClient.object.Point;
import StratmasClient.object.Shape;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.primitive.Reference;
import StratmasClient.object.type.TypeFactory;
import StratmasClient.treeview.TreeView;
import StratmasClient.treeview.TreeViewFrame;
import StratmasClient.filter.TypeFilter;
import StratmasClient.filter.StratmasObjectFilter;
import StratmasClient.map.adapter.MapElementAdapter;
import StratmasClient.map.adapter.MapShapeAdapter;
import StratmasClient.proj.MGRSConversion;

/**
 * This class is used to create different kinds of menus used in the map.
 */
class MapDrawerMenuCreator {
    /**
     * Reference to the MapDrawer.
     */
    private MapDrawer drawer;
     /**
     * The region shown in the map.
     */
    private Region region;
    /**
     * The layer of cells that cover the region.
     */
    private GridLayer cellLayer;
    /**
     * Reference to the client.
     */
    private Client client; 
    
    /**
     * Creates a MapDrawerMenuCreator.
     */
    protected MapDrawerMenuCreator(Client client, MapDrawer drawer, Region region) {
	this.client = client;
	this.drawer = drawer;
	this.region = region;
    }

    /**
     * Sets reference to the grid layer.
     */
    protected void setGridLayer(GridLayer layer) {
	cellLayer = layer;
    }
    
    /**
     * Returns the submenu with all the military units at the location in the map pointed by the mouse.
     *
     * @return the submenu with the units.
     */
    protected JMenu getMenuForAOR() {
	JMenu submenu = null;
	Vector adVec = drawer.mapDrawableAdaptersUnderCursor(MapElementAdapter.class);
	if (!adVec.isEmpty()) {
	    submenu = new JMenu("Define area for :");
	    for (Enumeration en = adVec.elements(); en.hasMoreElements();) {
		final MapElementAdapter sea = ((MapElementAdapter)en.nextElement());
		final MapDrawer fdrawer = drawer;
		final Region fregion = region;
		JMenuItem item = new JMenuItem(sea.getStratmasObject().getIdentifier().trim());
		item.addActionListener(new ActionListener() 
		    {
			public void actionPerformed(ActionEvent event) 
			{
			    new AreaCreationDrawer(fdrawer.getBasicMap(), fregion, sea.getObject()); 
			}
		    });
		submenu.add(item);
	    }
	}
	return submenu;
    }
    
    /**
     * Returns the submenu with all the elements at the location pointed by the mouse.
     *
     * @param pointedElements list of elements.
     *
     * @return the submenu with the elements.
     */
    protected JMenu getMenuForElements(Vector pointedElements) {
	JMenu submenu = null;
	// add all elements at the pointed location
	submenu = new JMenu("Show information for : ");
	for (Enumeration en = pointedElements.elements(); en.hasMoreElements();) {
	    final StratmasObject so = ((StratmasObject)en.nextElement());
	    JMenuItem item = new JMenuItem(so.getIdentifier().trim());
	    item.addActionListener(new ActionListener() 
		{
		    public void actionPerformed(ActionEvent event) 
		    {
			final TreeViewFrame frame = TreeView.getDefaultFrame(so);
			frame.setEditable(true);
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
				    frame.setVisible(true);
				}
			    });
		    }
		});
	    submenu.add(item);
	}	
   	return submenu;
    }
    
    /**
     * Returns the submenu with all selected (or unselected) elements at the location 
     * pointed by the mouse. The input argument decides if the submenu contains selected 
     * or unselected elements.
     *
     * @param selected if true the submenu contains selected elements, otherwise it contains 
     *                 the unselected elements.
     *
     * @return the submenu with the elements.
     */
    protected JMenu getMenuForElementsForSelection(boolean selected) {
	JMenu submenu = null;
	// find all elemets located at the pointed location 
	Vector pointedMapElementAdapters = drawer.mapDrawableAdaptersUnderCursor(MapElementAdapter.class);
	// get all selected (or unselected) objects
	Vector validAdapters = new Vector();
	for (int i = 0; i < pointedMapElementAdapters.size(); i++) {
	    MapElementAdapter meAdapter = (MapElementAdapter)pointedMapElementAdapters.get(i);
	    if ((selected && meAdapter.isSelected()) || (!selected && !meAdapter.isSelected())) {
		validAdapters.add(meAdapter);
	    }
	}
	if (!validAdapters.isEmpty()) {
	    // add all elements at the pointed location
	    submenu = (selected)? new JMenu("Unselect : ") : new JMenu("Select : ");
	    for (Enumeration en = validAdapters.elements(); en.hasMoreElements();) {
		final MapElementAdapter meAdapter = ((MapElementAdapter)en.nextElement());
		final StratmasObject so = meAdapter.getObject();
		final boolean fselected = selected;
		JMenuItem item = new JMenuItem(so.getIdentifier().trim());
		item.addActionListener(new ActionListener() 
		    {
			public void actionPerformed(ActionEvent event) 
			{
			    meAdapter.getObject().fireSelected(!fselected);
			}
		    });
		submenu.add(item);
	    }
	}	
   	return submenu;
    }
    
    /**
     * Returns the submenu with all the miltary units at the location pointed by the mouse.
     * When a military unit is selected from the menu, all it's subunits are shown in the map.
     *
     * @return the submenu with the units.
     */
    protected JMenu getMenuForMilitaryUnits() {
	JMenu submenu = null;
	// add all military units at the pointed location
	Vector v = (new TypeFilter(TypeFactory.getType("MilitaryUnit"), true)).filter(drawer.mapElementsUnderCursor());
	if (!v.isEmpty()) {
	    submenu = new JMenu("Show subunits for : ");
	    for (Enumeration en = v.elements(); en.hasMoreElements();) {
		final MapDrawer fdrawer = drawer;
		final StratmasObject so = ((StratmasObject)en.nextElement());
		JMenuItem item = new JMenuItem(so.getIdentifier().trim());
		item.addActionListener(new ActionListener() 
		    {
			public void actionPerformed(ActionEvent event) 
			{
			    fdrawer.showSubunits(so.getChild("subunits"));  
			}
		    });
		submenu.add(item);	
	    }	
	}
	return submenu;
    } 
    
    /**
     * Returns the submenu with all the regions currenly under the cursor.
     *
     * @return the submenu with the shapes.
     */
    protected JMenu getMenuForRegions() {
	JMenu submenu = null;
	// get the actual cell
	if (cellLayer != null && client.isConnected()) {
	    Hashtable shape_list = new Hashtable();
	    // get the cell under the cursor
	    int[] selectionNames = drawer.getRenderSelection().getSecondLevelSelectionNames();
	    for (int ii = 0; ii < selectionNames.length; ii++) {
		if (cellLayer.isCellRenderSelectionName(selectionNames[ii])) {
		    Shape s = cellLayer.getCircularCellRepresentation(cellLayer.getCell(selectionNames[ii]));
		    shape_list.put(s.getReference(), s);
		}
	    }
	    // get all simple shapes at the actual location
	    Vector shAdapters = drawer.mapDrawableAdaptersUnderCursor(MapShapeAdapter.class);
	    if (shAdapters != null && shAdapters.size() > 0) {
		for (int i = 0; i < shAdapters.size(); i++) {
		    Shape s = (Shape)((MapShapeAdapter)shAdapters.get(i)).getObject();
		    if (region.contains(s)) {
			shape_list.put(s.getReference(), s);
			Vector ancestors = s.getAncestralShapes();
			for (int j = 0; j < ancestors.size(); j++) {
			    s = (Shape)ancestors.get(j);
			    shape_list.put(s.getReference(), s);
			}
		    }
		}
	    }
	    // add all regions that contain the pointed location 
	    if (!shape_list.isEmpty() && client.getProcessVariables() != null && client.getFactions() != null) {
		submenu = new JMenu("Choose region for PV analysis:");
		for (Enumeration en = shape_list.keys(); en.hasMoreElements();) {
		    final Reference ref = (Reference)en.nextElement();
		    final Shape s = (Shape)shape_list.get(ref);
		    JMenuItem item = new JMenuItem(ref.getIdentifier().trim());
		    item.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent event) {
				if (client.isConnected()) {
				    ProcessVariableTablePanel tablePanel = new ProcessVariableTablePanel(client, s);
				    Visualizer.addTable(tablePanel);
				}
			    }
			});
			// want cell to be displayed first :-)
		    if (ref.getIdentifier().trim().startsWith("cell")) {
			submenu.add(item, 0);
		    }
		    else {
			submenu.add(item);
		    }
		}
	    }
	}
	return submenu;
    }

    /**
     * Returns the submenu with all the elements at the location pointed by the mouse. 
     * Selecting an element from the menu opens a new window with the elements position
     * on the map. 
     *
     * @return the submenu with the elements.
     */
    protected JMenu getMenuForElementsPosition() {
	JMenu submenu = null;
	// find all elemets located at the pointed location 
	Vector pointedElements = drawer.mapElementsUnderCursor();
	if (!pointedElements.isEmpty()) {
	    // add all elements at the pointed location
	    submenu = new JMenu("Show position for : ");
	    for (Enumeration en = pointedElements.elements(); en.hasMoreElements();) {
		final StratmasObject so = ((StratmasObject)en.nextElement());
		final MapDrawerMenuCreator self = this;
		JMenuItem item = new JMenuItem(so.getIdentifier().trim());
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
			    Shape loc = (Shape)so.getChild("location");
			    if (loc != null) {
				final JDialog dialog = self.getElementPositionDialog(so);
				dialog.setSize(new Dimension(250, 200));
				dialog.setLocationRelativeTo(drawer.getFrame());
				SwingUtilities.invokeLater (new Runnable() {
					public void run() {
					    dialog.setVisible(true);
					}
				    });
			    }
			}
		    });
		submenu.add(item);
	    }	
	}
   	return submenu;
    }
   
    /**
     * Returns the menu with all the elements that can be dragged.
     *
     * @param dragFilter the actual filter.
     *
     * @return the menu with the elements.
     */
    protected JPopupMenu getDraggedElementsMenu(StratmasObjectFilter dragFilter) {
	Vector elements = dragFilter.filter(drawer.mapElementsUnderCursor());
	JPopupMenu menu = new JPopupMenu();
	for (int i = 0; i < elements.size(); i++) {
	    menu.add(new DraggableJMenuItem((StratmasObject)elements.get(i)));
	}
	return menu;
    }
    
    /**
     * Returns the dialog which displays the elements position on the map.
     */
    public JDialog getElementPositionDialog(StratmasObject so) {
	// create the dialog
	final JDialog dialog = new JDialog(new JFrame(), "Current Position");
	
	Shape loc = (Shape)so.getChild("location");
	BoundingBox box = loc.getBoundingBox();
	double lat = (box.getNorthLat() + box.getSouthLat()) / 2;
	double lon = (box.getWestLon() + box.getEastLon()) / 2;
	// information panel
	JPanel infoPanel = new JPanel();
	infoPanel.setLayout(new GridLayout(2, 1, 3, 3));
	JLabel coordLabel = new JLabel();
	coordLabel.setFont(coordLabel.getFont().deriveFont(Font.PLAIN));
	infoPanel.add(coordLabel);
	final JTextField valueTextField = new JTextField(10);
	valueTextField.setEditable(false);
	valueTextField.setBorder(null);
	infoPanel.add(valueTextField); 
	// geodetic coordinates
	if (Configuration.getCoordinateSystem() == Configuration.GEODETIC) {
	    DecimalFormatSymbols dfs = new DecimalFormatSymbols();
	    dfs.setDecimalSeparator('.');
	    DecimalFormat resultFormat = new DecimalFormat("0.00", dfs);
	    String lats = resultFormat.format(lat);
	    String lons = resultFormat.format(lon);
	    coordLabel.setText(new String("Latitude & Longitude"));
	    valueTextField.setText(new String("lat = " + lats +" , lon = " + lons));
	}
	else {
	    String mgrs = MGRSConversion.
		convertGeodeticToMGRS(Math.toRadians(lon), Math.toRadians(lat), 5);
	    coordLabel.setText(new String("MGRS"));
	    valueTextField.setText(mgrs);
	}
	String titleString = new String("Position for "+so.getIdentifier());
	infoPanel.setBorder(BorderFactory.
			    createCompoundBorder(BorderFactory.createTitledBorder(titleString),
						 BorderFactory.createEmptyBorder(5,5,5,5)));
	// create and initialize the buttons
	JButton okButton = new JButton(new AbstractAction("OK") {
		public void actionPerformed(ActionEvent e) {
		    dialog.setVisible(false);
		    dialog.dispose(); 
		}
	    });
	okButton.setFont(okButton.getFont().deriveFont(Font.PLAIN));
	okButton.setMargin(new Insets(1,5,1,5));
	
	// create menu bar
	JMenuBar menuBar = new JMenuBar();
	JMenu editMenu = new JMenu("Edit");
	editMenu.setFont(editMenu.getFont().deriveFont(Font.PLAIN));
	menuBar.add(editMenu);
	JMenuItem copyMenuItem = new JMenuItem(new AbstractAction("Copy") {
		public void actionPerformed(ActionEvent e) {
		    valueTextField.copy();
		}
	    });
	copyMenuItem.setFont(copyMenuItem.getFont().deriveFont(Font.PLAIN));
	editMenu.add(copyMenuItem);
	dialog.setJMenuBar(menuBar);
	
	// lay out the buttons from left to right.
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttonPanel.add(Box.createHorizontalGlue());
	buttonPanel.add(okButton);

	// compose the dialog	    
	JPanel contentPane = new JPanel();
	contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
	contentPane.add(infoPanel);
	contentPane.add(Box.createRigidArea(new Dimension(0, 5)));
	contentPane.add(buttonPanel);
	contentPane.setOpaque(true);

	dialog.setContentPane(contentPane);
	return dialog;
    }
}

