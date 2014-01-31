package StratmasClient.map;

import java.util.Hashtable;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import javax.swing.*;

import java.io.File;

import StratmasClient.Client;
import StratmasClient.object.Shape;
import StratmasClient.BoundingBox;
import StratmasClient.communication.GridData;


/**
 * STRATMAS map controller. <br>
 * This class contains references to the main map as well as the panel components which controls display of the map.
 *
 * @version 1.0
 * @author Amir Filipovic 
 */
public class StratMap extends BasicMap {
    /**
     * Reference to the color scale map.
     */ 
    private ColorMap color_map;
    /**
     * Reference to the position/navigation map.
     */
    private PositionMap position_map;
    /**
     * The zooming and scaling implementation.
     */
    private ZoomAndScale zoom;
    /**
     * The display control features.
     */
    private DisplayControl display;
    /**
     * The control map panel.
     */
    private JPanel control_panel;
    /**
     * The main panel.
     */
    private JPanel main_panel;
    /**
     * The panel of the process variables.
     */ 
    private PVPanel pv_panel;
    /**
     * The frame width.
     */
    private int frame_width = 420;
    /**
     * The frame height.
     */
    private int frame_height = 530;
    /**
     * The grid of cells for actual shape.
     */
    private GridLayer grid_layer;
    /**
     * The actual frame.
     */
    private JFrame frame = new JFrame();
    /**
     * The title of the frame.
     */
    private String map_title;
    
    /**
     * Create STRATMAS map controler.
     *
     * @param client reference to the client.
     * @param shape shapes defining geographical region.
     * @param map_title title of the map. 
     */
    public StratMap(Client client, Shape shape, String map_title) {
	super(client, shape);
	
	// title of the map
	this.map_title = map_title;

	// create position map
	position_map = new PositionMap(this, region);
	
	// create drawing object
	drawer = new MapDrawer(this, region, position_map);

	// create color map
	color_map = new ColorMap((MapDrawer)drawer);
	
	// create zoom & scale control window
	zoom = new ZoomAndScale(drawer, JSlider.VERTICAL);
	
	// create panel of process variables
	pv_panel = new PVPanel(client, this);

	// create display control
	display = new DisplayControl(client, (MapDrawer)drawer);
	
	// create control window
	createControlWindow();
	
	// create main panel
	JTabbedPane tabbed_pane = new JTabbedPane();
	tabbed_pane.setPreferredSize(new Dimension(frame_width-20, frame_height-40));
	main_panel = new JPanel();
	main_panel.setLayout(new BorderLayout());
	tabbed_pane.add("Map Control", control_panel);
	tabbed_pane.add("Simulation", display.getSimulationPanel());
	tabbed_pane.add("Topography", display.getTopographyPanel());
  	tabbed_pane.add("Preferences", display.getPreferenceDisplayPanel());
	main_panel.add(tabbed_pane, BorderLayout.CENTER);
	
	// show 
	show();
	drawer.createAndShowGUI(map_title);
    }
    
    /**
     * Create control panel for the main map manipulaton.
     */
    public void createControlWindow() {
	final StratMap self = this;
	// create panel for mapshot
	JPanel button_panel = new JPanel();
	button_panel.setLayout(new BorderLayout());
	JButton save_button = new JButton("Save");
	save_button.setFont(save_button.getFont().deriveFont(Font.PLAIN));
	save_button.setMargin(new Insets(1,5,1,5));
	save_button.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) 
		{
		    self.getMapDrawer().setDoScreenShot();
		}
	    }); 
	button_panel.add(save_button, BorderLayout.SOUTH);
	button_panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Mapshot"),
								  BorderFactory.createEmptyBorder(2,8,2,8)));
	// put naviagtion map and zoom&scale into a panel
	JPanel up_panel = new JPanel();
	up_panel.setLayout(new BorderLayout());
	up_panel.add(position_map.getPanel(), BorderLayout.CENTER);
	up_panel.add(zoom.getPanel(), BorderLayout.EAST);

	// put color map and process variable selection into a panel
	JPanel down_panel = new JPanel();
	down_panel.setLayout(new BorderLayout());
	JPanel down2_panel = new JPanel();
	down2_panel.setLayout(new BorderLayout());
	down2_panel.add(color_map.getPanel(), BorderLayout.CENTER);
	down2_panel.add(button_panel, BorderLayout.EAST);
	down_panel.add(down2_panel, BorderLayout.CENTER);
	down_panel.add(pv_panel, BorderLayout.SOUTH);
	down_panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.
								createTitledBorder("Process Variables Display Control"),
								BorderFactory.createEmptyBorder(2,2,2,2)));
	
	color_map.getPanel().setPreferredSize(new Dimension(100, 100));

	// put all components into a final panel
	control_panel = new JPanel();
 	control_panel.setLayout(new BorderLayout(0,10));
	control_panel.add(up_panel, BorderLayout.CENTER);
	control_panel.add(down_panel, BorderLayout.SOUTH);
	control_panel.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));

    }

    /**
     * Creates new grid.
     *
     * @param gridData contains information about the grid.
     */
    public void createGridLayer(GridData gridData) {
	grid_layer = new GridLayer(this, region, gridData);
	// set grid layer to listen to the region changes
	region.addListener(grid_layer);
	// set reference to the color map
	grid_layer.setColorMap(color_map);
	// set the map drawer to listen to the grid
	grid_layer.addListener(getMapDrawer());
	// set the panel of process variables to listen to the grid
	grid_layer.addListener(pv_panel);
	// set the controller to listen to the grid
	grid_layer.addListener(client.getController());
	// update the map with the grid layer
	getMapDrawer().setGridLayer(grid_layer);
    }
    
    /**
     * Removes all objects used in StratMap.
     */
    public void remove() {
	super.remove();
	// remove grid layer
	if (grid_layer != null) {
	    grid_layer.remove();
	}
    }

    /**
     * Resets all the map components.
     */
    public void reset() {
	super.reset();
	pv_panel.reset();
	if (grid_layer != null) {
	    grid_layer.reset();
	}
	
	final JPanel fcontrolPanel = control_panel;
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    fcontrolPanel.validate();
		    fcontrolPanel.repaint();
		}
	    });
    }
    
    /**
     * Returns actual grid layer reference.
     */
    public GridLayer getGridLayer() {
	return grid_layer;
    }
    
    /**
     * Returns reference to the current <code>MapDrawer</code> object.
     */
    public MapDrawer getMapDrawer() {
	return (MapDrawer)drawer;
    }
    
    /**
     * Returns reference to the current <code>PVPanel</code> object.
     */
    public PVPanel getPVPanel() {
	return pv_panel;
    }
    
    /**
     * Returns the DisplayControl.
     */
    public DisplayControl getDisplayControl() {
	return display;
    }

    /**
     * Returns the zoom & scale controller.
     */
    public ZoomAndScale getZoomAndScale() {
	return zoom;
    }

    /**
     * Returns the title of the map.
     */
    public String getTitle() {
	return map_title;
    }
    
    /**
     * Disposes the map window.
     */
    public void doDispose() {
	frame.dispose();
    }
    
    /**
     * Exits the map window
     */
    public void doExit() {
	Visualizer.removeMap(this);
	drawer.doDispose();
	doDispose();
    }
    
    /**
     * Shows the control panel.
     */
    public void show() {
	final StratMap self = this;
        // create and set up the window
        frame.setTitle("Stratmas Map Control for "+map_title);
	frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	
        // add listener to the frame
	frame.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		       if (JOptionPane.showConfirmDialog(self.frame, "Really close map?", "Closing map window...",
							 JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			   Visualizer.removeMap(self);
			   self.getMapDrawer().doDispose();
			   self.doDispose();
		       }
		}
	     });
	
	// frame size (test adapted for now on)
	Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
	frame.setSize(frame_width, frame_height); 
        frame.setLocation(screen_size.width-frame_width, screen_size.height/20);

        // set up the content pane
        main_panel.setOpaque(true);
        frame.setContentPane(main_panel);
	
        // display the window
	frame.setResizable(true);
	
	// thread safety recomendation
	final JFrame fframe = frame;
	SwingUtilities.invokeLater (
				    new Runnable() {
					public void run() {
					    fframe.setVisible(true);
					}
				    }
				    );
    }
    
}
