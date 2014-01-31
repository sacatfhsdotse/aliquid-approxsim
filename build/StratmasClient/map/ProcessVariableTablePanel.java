package StratmasClient.map;

import java.text.DecimalFormat;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Random;
import java.util.Comparator;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Container;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ButtonGroup;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.AbstractTableModel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.ListSelectionModel;

import StratmasClient.Client;
import StratmasClient.object.Shape;
import StratmasClient.object.StratmasObject;
import StratmasClient.StratmasConstants;
import StratmasClient.ProcessVariableDescription;
import StratmasClient.object.StratmasEvent;
import StratmasClient.object.StratmasEventListener;
import StratmasClient.StratmasWindowListener;
import StratmasClient.communication.RegionData;
import StratmasClient.map.graph.ProcessVariableXYGraph;

/**
 * This is implementation of a table of values for all process variables and all factions over an arbitrary
 * region. This table is user interactive in a way that a double clicking on a cell opens a graphical window
 * where the particular faction of the chosen process variable is displayed over a certain time interval.
 *
 * @version 1.0
 * @author Amir Filipovic 
 */
public class ProcessVariableTablePanel extends JPanel {  
    /**
     * The actual frame.
     */
    private final JFrame frame = new JFrame("Stratmas PV Table");
    /**
     * The actual table of process variables and factions.
     */
    private ProcessVariableTable table;
    /**
     * Name of the actual area.
     */
    private JLabel area;
    /**
     * The actual region id.
     */
    private String regionId;
    /**
     * The actual shape.
     */
    private Shape shape;
    /**
     * The exit button.
     */
    private JButton exit = new JButton("Close");
    /**
     * The currently chosen main category of the process variables.
     */
    public static String pvCategory = "Unspecified";
    /**
     * Used to compare the graphs wrt the category of the process variable.
     */
    public static Comparator GRAPH_COMPARATOR = new Comparator() {
	    public int compare(Object o1, Object o2) {
		ProcessVariableXYGraph graph1 = (ProcessVariableXYGraph)o1;
		ProcessVariableXYGraph graph2 = (ProcessVariableXYGraph)o2;
		String c1 = graph1.getProcessVariable().getCategory();
		String c2 = graph2.getProcessVariable().getCategory();
		if (c1.equals(c2)) {
		    String n1 = graph1.getProcessVariable().getName();
		    String n2 = graph2.getProcessVariable().getName();
		    return n1.compareTo(n2);
		}
		else {
		    if (c1.equals(ProcessVariableTablePanel.getMainCategory())) {
			return -1;
		    }
		    else if (c2.equals(ProcessVariableTablePanel.getMainCategory())) {
			return 1;
		    }
		    else {
			for (int i = 0; i < MapConstants.pvCategories.length; i++) {
			    if (c1.equals(MapConstants.pvCategories[i])) {
				return -1;
			    }
			    else if (c2.equals(MapConstants.pvCategories[i])) {
				return 1;
			    }
			}
			return c1.compareTo(c2); 
		    }
		}
	    }
	};
    
    /**
     * Creates a table of process variables and faction over a region.
     *
     * @param client the actual client.
     * @param shape the shape of the actual region. 
     */
    public ProcessVariableTablePanel(Client client, Shape shape) {
	// set references
	this.shape = shape;
	this.regionId = shape.getReference().getIdentifier().trim();
	
	// create the table
	JPanel tablePanel = new JPanel();
	tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
	table = new ProcessVariableTable(client, shape);
	
	// add scroll pane
	JScrollPane scrollPane = new JScrollPane(table);
	tablePanel.add(scrollPane);
	TitledBorder titledBorder = BorderFactory.createTitledBorder("PV Values");
	tablePanel.setBorder(BorderFactory.createCompoundBorder(titledBorder,
								 BorderFactory.createEmptyBorder(2, 5, 2, 5)));
	
	// area info
	JPanel areaPanel = new JPanel();
	areaPanel.setLayout(new BoxLayout(areaPanel, BoxLayout.X_AXIS));
	area = new JLabel(regionId);
	area.setFont(area.getFont().deriveFont(Font.PLAIN));
	areaPanel.add(area);
	areaPanel.add(Box.createHorizontalGlue());
	areaPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Actual Area"),
								BorderFactory.createEmptyBorder(2, 15, 2, 15)));
	
	// button panel
	JPanel buttonPanel = new JPanel();
	buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
	exit.setFont(exit.getFont().deriveFont(Font.PLAIN));
	final ProcessVariableTablePanel self = this;
	exit.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    self.remove();
		}
	    });
	buttonPanel.add(exit);
	buttonPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
	
	JPanel downPanel = new JPanel();
	downPanel.setLayout(new BoxLayout(downPanel, BoxLayout.X_AXIS));
	downPanel.add(areaPanel);
	downPanel.add(buttonPanel);
	
	// set layout
	setLayout(new BorderLayout());
	add(tablePanel, BorderLayout.CENTER);
	add(downPanel, BorderLayout.SOUTH);
	
	showGUI();
    }

    /**
     * Show the GUI.
     */
    public void showGUI() {
        // create and set up the window
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	
	final ProcessVariableTablePanel self = this;
	frame.addWindowListener(new java.awt.event.WindowAdapter() {
		public void windowClosing(java.awt.event.WindowEvent e) {
		    self.remove();
		  }
	    });
	
	//
	JMenuBar menuBar = new JMenuBar();
	menuBar.add(createOptionsMenu(table));
	frame.setJMenuBar(menuBar);

        // set up the content pane
        setOpaque(true); //content panes must be opaque
        frame.setContentPane(this);
	
        // display the window
	frame.pack();
	frame.setResizable(true);
	
	// thread safety recomendation
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    frame.setVisible(true);
		}
	    }
				   );
    }
    
    /**
     * Removes this panel.
     */
    public void remove() {
 	table.remove();
	// remove the object form the list of tables
 	Visualizer.removeTable(this);
 	// dispose the frame
	frame.dispose();
    }
    
    /**
     * Resets the table.
     */
    public void reset() {
	table.reset();
    }
    
    /**
     * Returns the option menu for the selection on the main category.
     */
    public static JMenu createOptionsMenu(ProcessVariableTable pvTable) {
	final ProcessVariableTable fTable = pvTable;
	JMenu optionsMenu = new JMenu("Options");
	optionsMenu.setFont(optionsMenu.getFont().deriveFont(Font.PLAIN));
	JMenu categorySubmenu = new JMenu("Main category");
	categorySubmenu.setFont(categorySubmenu.getFont().deriveFont(Font.PLAIN));
	optionsMenu.add(categorySubmenu);
	ButtonGroup categoryButtonGroup = new ButtonGroup();
	// the main category is unspecified
	JRadioButtonMenuItem unspecifiedButton = new JRadioButtonMenuItem(new AbstractAction("Unspecified") {
		public void actionPerformed(ActionEvent e) {
		    ProcessVariableTablePanel.setMainCategory("Unspecified");
		    Visualizer.sortTables();
		    Visualizer.sortGraphs();
		    ProcessVariableTable.layoutAllGraphs(fTable);
		}
	    });
	unspecifiedButton.setFont(unspecifiedButton.getFont().deriveFont(Font.PLAIN));
	categoryButtonGroup.add(unspecifiedButton);
	categorySubmenu.add(unspecifiedButton);
	if (ProcessVariableTablePanel.getMainCategory().equals("Unspecified")) {
	    unspecifiedButton.setSelected(true);
	}
	// the other categories
	for (int i = 0; i < MapConstants.pvCategories.length; i++) {
	    final int ii = i;
	    JRadioButtonMenuItem categoryButton = new JRadioButtonMenuItem(new AbstractAction(MapConstants.pvCategories[i]) {
		    public void actionPerformed(ActionEvent e) {
			ProcessVariableTablePanel.setMainCategory(MapConstants.pvCategories[ii]);
			Visualizer.sortTables();
			Visualizer.sortGraphs();
			ProcessVariableTable.layoutAllGraphs(fTable);
		    }
		});
	    categoryButton.setFont(categoryButton.getFont().deriveFont(Font.PLAIN));
	    categoryButtonGroup.add(categoryButton);
	    categorySubmenu.add(categoryButton);
	    if (ProcessVariableTablePanel.getMainCategory().equals(MapConstants.pvCategories[i])) {
		categoryButton.setSelected(true);
	    }
	}
	
	JMenu sortSubmenu = new JMenu("Sort graphs");
	sortSubmenu.setFont(sortSubmenu.getFont().deriveFont(Font.PLAIN));
	optionsMenu.add(sortSubmenu);
	JMenuItem sortByCategoryItem = new JMenuItem(new AbstractAction("By category") {
		public void actionPerformed(ActionEvent e) {
		    Visualizer.sortGraphs();
		    ProcessVariableTable.layoutAllGraphs(fTable);
		}
	    });
	sortByCategoryItem.setFont(sortByCategoryItem.getFont().deriveFont(Font.PLAIN));
	sortSubmenu.add(sortByCategoryItem);
	
	return optionsMenu;
    }
    
    /**
     * Sets the main category of the process variables.
     */
    public static void setMainCategory(String mainCategory) {
	pvCategory = mainCategory;
    }
    
    /**
     * Sets the location of the table panel.
     */
    public void setTableLocation(java.awt.Point point) {
	frame.setLocation(point);
    }
    
    /**
     * Sets the size of the table panel.
     */
    public void setTableSize(Dimension size) {
	frame.setSize(size);
    }
    
    /**
     * Returns the main category of the process variables. 
     */
    public static String getMainCategory() {
	return pvCategory;
    }
    
    /**
     * Returns the table. 
     */
    public ProcessVariableTable getTable() {
	return table;
    }
    
    /**
     * Returns the parameters needed to re-create this table panel.
     */
    public Hashtable getParameters() {
	Hashtable hTable = new Hashtable();
	hTable.put("shape", shape);
	hTable.put("location", frame.getLocationOnScreen());
	hTable.put("size", frame.getSize());
	return hTable;
    }
    
}
