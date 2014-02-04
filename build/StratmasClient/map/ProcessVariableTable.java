package StratmasClient.map;

import java.text.DecimalFormat;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Random;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Container;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;

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
public class ProcessVariableTable extends JTable implements MouseListener, StratmasEventListener {  
    /**  
     * The process variables.
     */
    private Vector processVariables;
    /**
     * The factions.
     */
    private Vector factions;
    /**
     * The different colors used for the factions.
     */
    public static final Color[] colors = {Color.BLUE, Color.RED, Color.GRAY, Color.MAGENTA, Color.ORANGE, 
                                          Color.CYAN, Color.PINK, Color.YELLOW, Color.GREEN};
    /**
     * The used colors.
     */
    private Color[] usedColors;
    /**
     * The actual client.
     */
    private Client client; 
    /**
     * The actual values of the process variables and factions.
     */
    private RegionData actualValues;
    /**
     * The actual region id.
     */
    private String regionId;
    
    /**
     * Creates a table of process variables and faction over a region.
     *
     * @param client the actual client.
     * @param shape the shape of the actual region. 
     */
    public ProcessVariableTable(Client client, Shape shape) {
        super();

        // set references
        this.client = client;
        this.processVariables = client.getProcessVariables();
        this.factions = client.getFactions();
        this.regionId = shape.getReference().getIdentifier().trim();
        
        // colors used for the factions
        usedColors = new Color[factions.size() + 1];
        
        // subscribe to data from the region
        actualValues = Visualizer.addListenerToRegionData(shape, this);
        
        // set up the table
        setModel(new ProcessVariableTableModel(processVariables, factions));
        setShowGrid(false);
        setCellSelectionEnabled(true);
        setBackground(getTableHeader().getBackground());
        addMouseListener(this);
        setPreferredScrollableViewportSize(new Dimension(300, 100));
        
        // set up the columns
        initColumns();
    }

    /**
     * Removes the listener.
     */
    public void remove() {
         // remove this listener
        Visualizer.removeListenerFromRegionData(actualValues, this);
    }
    
    /**
     * Resets the table.
     */
    public void reset() {
        for (int i = 0; i < processVariables.size(); i++) {
            ProcessVariableDescription pvd = (ProcessVariableDescription)processVariables.get(i);
            int row = getRow(pvd.getName());
            int col = getColumn(StratmasConstants.factionAll);
            setValueAt("0", row, col);
            if (pvd.hasFactions()) {
                for (int j  = 0; j < factions.size(); j++) {
                    row = getRow(pvd.getName());
                    col = getColumn(((StratmasObject)factions.get(j)).getReference().getIdentifier().trim());
                    setValueAt("0", row, col);
                }
            }
        }
    }
    
    /**
     * Updates the table.
     */
    public void eventOccured(StratmasEvent e) {
        if (e.getSource().equals(actualValues)) {
            update(actualValues.getPV());
        }
    }
    
    /**
     * Initializes the columns of the table containing the factions such that each column gets a unique color.
     *
     * @param table actual table.
     */
    private void initColumns() {
        // local variables
            Random rand = new Random();
        // set different color for each column
        for (int i = 2; i < getModel().getColumnCount(); i++) {
            int j = i - 2;
            // get column
            TableColumn column = getColumnModel().getColumn(i);
            // get new color
            Color color = (j < colors.length)? colors[j] : new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
            // paint header and cell component
            DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
            cellRenderer.setForeground(color);
            cellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
            column.setCellRenderer(cellRenderer);
            column.setHeaderRenderer(new HeaderRenderer(color));
            usedColors[j] = color;
        }
        // sort the table
        ((ProcessVariableTableModel)getModel()).sort();
    }
    
    /**
     * Updates the table.
     *
     * @param elements list of values for all process variables and all factions. 
     */
    public void update(Hashtable elements) {
        //
        for (int i = 0; i < processVariables.size(); i++) {
            ProcessVariableDescription pvd = (ProcessVariableDescription)processVariables.get(i);
            Hashtable h = (Hashtable)elements.get(pvd.getName());
            if (h != null) {
                Number num = (Number)h.get(StratmasConstants.factionAll);
                // faction "All"
                if (num != null) {
                    int row = getRow(pvd.getName());
                    int col = getColumn(StratmasConstants.factionAll);
                    if (row != -1 && col != -1) {
                        setValueAt(numToString(num), row, col);
                    }
                }
                // the other factions
                if (pvd.hasFactions()) {
                    for (int j  = 0; j < factions.size(); j++) {
                        num = (Number)h.get(((StratmasObject)factions.get(j)).getReference().getIdentifier().trim());
                        if (num != null) {
                            int row = getRow(pvd.getName());
                            int col = getColumn(((StratmasObject)factions.get(j)).getReference().getIdentifier().trim());
                            if (row != -1 && col != -1) {
                                setValueAt(numToString(num), row, col);
                            }
                        }
                    }
                }
            }
        }
    }
        
    /**
     * Returns the  row which contains the given process variable.
     *
     * @param str name of the process variable.
     */
    private int getRow(String str) {
        for (int i = 0; i < getRowCount(); i++) {
            if (getValueAt(i, 0).equals(str)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Returns the column which contains the given faction.
     *
     * @param str name of the faction.
     */
    private int getColumn(String str) {
        for (int i = 0; i < getColumnCount(); i++) {
            if (getColumnName(i).equals(str)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Converts number to string using different decimal formats depending
     * on the type of the number (Double or Integer) and it's size.
     *
     * @param obj only Double or Integer can be handled.
     *
     * @return String representation of the number.
     */
    private String numToString(Object obj) {
        // the integers
        if (obj.getClass().getName().equals("java.lang.Integer")) {
            int i = ((Integer)obj).intValue();
            String s = ((Integer)obj).toString();
            // appropriate format if number is too long
            if (i > Math.abs(999999)) {
                DecimalFormat resultFormat = new DecimalFormat("00E0");
                s = resultFormat.format(i);
            }
            return s;
        }
        // the doubles
        else {
            double d = ((Double)obj).doubleValue();
            if (d > Math.abs(999999)) {
                DecimalFormat resultFormat = new DecimalFormat("00E0");
                return resultFormat.format(d);
            }
            else if (d >= Math.abs(0.1) || d == 0.0) {
                // only two decimals 
                DecimalFormat resultFormat = new DecimalFormat("0.00");
                return resultFormat.format(d);
            }
            else {
                //
                DecimalFormat resultFormat = new DecimalFormat("0.0E0");
                return  resultFormat.format(d);
            }
        }
    }
    
    /**
     * Creates new graph.
     *
     * @param pv the actual process variable.
     * @param fac the actual faction(s).
     * @param colors the colors for the curves in the graph.
     */
    public ProcessVariableXYGraph createGraph(ProcessVariableDescription pv, String[] fac, Color[] colors) {
        // create a graph 
        ProcessVariableXYGraph graph = new ProcessVariableXYGraph(client.getTimeline(), actualValues, pv, fac, colors, regionId);
        // add the graph to the list of all graphs
        Visualizer.addGraph(graph); 
        
        return graph;
    }
    
    /**
     * Lays out a graph.
     *
     * @param graph the actual graph.
     * @param pvTable the table where the sorting process is initialized. 
     */
    public static void layoutGraph(ProcessVariableXYGraph graph, ProcessVariableTable pvTable) {
        boolean done = false;
        // put the graph in the grid of graphs
        for (int i = 0; i < Visualizer.locationGrid.length; i++) {
            for (int j = 0; j < Visualizer.locationGrid[0].length; j++) {
                if (!done && Visualizer.locationGrid[i][j] == null) {
                    int x = j * ProcessVariableXYGraph.DEFAULT_WIDTH;
                    int y = i * ProcessVariableXYGraph.DEFAULT_HEIGHT;
                    Rectangle graphRectangle = new Rectangle(x, y, ProcessVariableXYGraph.DEFAULT_WIDTH, 
                                                             ProcessVariableXYGraph.DEFAULT_HEIGHT);
                    Rectangle thisRectangle = new Rectangle((int)pvTable.getTopLevelAncestor().getLocationOnScreen().getX(), 
                                                            (int)pvTable.getTopLevelAncestor().getLocationOnScreen().getY(), 
                                                            pvTable.getTopLevelAncestor().getWidth(), 
                                                            pvTable.getTopLevelAncestor().getHeight());
                    // check that the graph doesn't cover the table
                    if (!graphRectangle.intersects(thisRectangle)) {
                        graph.setGraphLocation(x, y);
                        Visualizer.locationGrid[i][j] = graph;
                        done = true;
                    }
                }
            }
        }
        // in case the grid is filled with graphs
        if (!done) {
            int x = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2;
            int y = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2;
            if (Visualizer.getGraphs().size() % 2 == 0) {
                x = x + 100;
                y = y + 100;
            }
            graph.setGraphLocation(x, y);
        }
    }
    
    /**
     * Lays out all the graphs.
     *
     * @param pvTable the table where the sorting process is initialized.  
     */
    public static void layoutAllGraphs(ProcessVariableTable pvTable) {
        // reset the grid of initial loactions for the graphs
        Visualizer.resetLocationGrid();
        for (int i = 0; i < Visualizer.getGraphs().size(); i++) {
            layoutGraph((ProcessVariableXYGraph)Visualizer.getGraphs().get(i), pvTable);
        }
    }
    
    /**
     * Opens one/several graphical window(s) when double clicking to a non-empty cell in the table.
     */
    public void mouseClicked(MouseEvent e) {
        // if double clicked
        if (e.getClickCount() == 2) {
            int row = getSelectedRow();
            int col = getSelectedColumn();
            // get the model
            ProcessVariableTableModel model = (ProcessVariableTableModel)getModel();
            // create default arrays
            String[] fac = new String[1];
            Color[] colors = new Color[1];
            // create one graph
            if (col != getColumnModel().getColumnIndex("Category")) {
                ProcessVariableDescription pv = model.getProcessVariable(row);
                // graph containing all the factions
                if (col == getColumnModel().getColumnIndex("PV") && pv.hasFactions()) {
                    layoutAllFactionsGraph(pv);
                }
                // graph containing faction "All"
                else if (col == 0 || col == 2){
                    layoutOneFactionGraph(pv, StratmasConstants.factionAll, usedColors[0]);
                }
                // graph containing specific faction
                else if (pv.hasFactions()) {
                    String actFaction = ((StratmasObject)factions.get(col-3)).getReference().getIdentifier().trim();
                    layoutOneFactionGraph(pv, actFaction, usedColors[col - 2]);
                }
            }
            // create graphs for all process variables within the category with all factions
            else {
                Vector pvs = model.getProcessVariables((String)model.getValueAt(row, col));
                for (int i = 0; i < pvs.size(); i++) {
                    ProcessVariableDescription pv = (ProcessVariableDescription)pvs.get(i);
                    if (pv.hasFactions()) {
                        layoutAllFactionsGraph(pv);
                    }
                    else{
                        layoutOneFactionGraph(pv, StratmasConstants.factionAll, usedColors[0]);
                    }
                }
            }
        }    
    }
    
    /**
     * Creates and lays out a graph with one faction.
     *
     * @param pv the actual process variable.
     * @param faction the actual faction
     * @param color the color of the faction curve in the graph.
     */
    private void layoutOneFactionGraph(ProcessVariableDescription pv, String faction, Color color) {
        String[] fac = {faction};
        Color[] colors = {color};
        ProcessVariableXYGraph graph = createGraph(pv, fac, colors); 
        layoutGraph(graph, this);
    }
    
    /**
     * Creates and lays out a graph with all the factions.
     *
     * @param pv the actual process variable.
     */
    private void layoutAllFactionsGraph(ProcessVariableDescription pv) {
        // get all factions incl. faction "All"
        String[] fac = new String[factions.size() + 1];
        fac[0] = StratmasConstants.factionAll;
        for (int i = 0; i < factions.size(); i++) {
            fac[i+1] = ((StratmasObject)factions.get(i)).getReference().getIdentifier().trim();
        }
        // create the graph
        ProcessVariableXYGraph graph = createGraph(pv, fac, usedColors); 
        // layout the graph
        layoutGraph(graph, this);
    }
    
    /**
     * Part of <code> MouseListener </code> . Not implemented.
     */
    public void mouseEntered(MouseEvent e) {
    }
    
    /**
     * Part of <code> MouseListener </code> . Not implemented.
     */
    public void mouseExited(MouseEvent e) {
    }
    
    /**
     * Part of <code> MouseListener </code> . Not implemented.
     */
    public void mousePressed(MouseEvent e) {
    }
    
    /**
     * Part of <code> MouseListener </code> . Not implemented.
     */
    public void mouseReleased(MouseEvent e) {
    }
    
}

/**
 * The header renderer.
 */
class HeaderRenderer extends DefaultTableCellRenderer {
    /**
     * The color of the text in the header
     */
    Color foregroundColor;
    
    /**
     * The constructor.
     */
    public HeaderRenderer(Color foregroundColor) {
        this.foregroundColor = foregroundColor;
        setHorizontalAlignment(SwingConstants.CENTER);
        setOpaque(true);
        
        // This call is needed because DefaultTableCellRenderer calls setBorder()
        // in its constructor, which is executed after updateUI()
        setBorder(UIManager.getBorder("TableHeader.cellBorder"));
    }
    
    /**
     * Updates UI.
     */
    public void updateUI() {
        super.updateUI();
        setBorder(UIManager.getBorder("TableHeader.cellBorder"));
    }

    /**
     * Returns the cell.
     */
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean selected, boolean focused, int row, int column) {
        JTableHeader h = table != null ? table.getTableHeader() : null;
        
        if (h != null) {
            setEnabled(h.isEnabled());         
            setComponentOrientation(h.getComponentOrientation());
            
            setForeground(foregroundColor);
            setBackground(h.getBackground());
            setFont(h.getFont());
        }
        else {
            /* Use sensible values instead of random leftover values from the last call */
            setEnabled(true);
            setComponentOrientation(ComponentOrientation.UNKNOWN);
            
            setForeground(foregroundColor);
            setBackground(UIManager.getColor("TableHeader.background"));
            setFont(UIManager.getFont("TableHeader.font"));
        }

        setValue(value);
        
        return this;
    }
}
