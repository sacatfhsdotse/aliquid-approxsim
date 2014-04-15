package StratmasClient.map;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Collections;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Color;
import StratmasClient.Client;
import StratmasClient.object.StratmasEventListener;
import StratmasClient.object.Shape;
import StratmasClient.ProcessVariableDescription;
import StratmasClient.object.StratmasObject;
import StratmasClient.communication.GridData;
import StratmasClient.communication.RegionData;
import StratmasClient.map.graph.ProcessVariableXYGraph;
import StratmasClient.timeline.Timeline;

/**
 * This class containes all the maps as well as the control windows for controlling these maps.
 *
 * @author Amir Filipovic
 */
public class Visualizer {
    /**
     * Reference to the main client.
     */
    private static Client client;
    /**
     * Reference to GridData.
     */
    private static GridData gridData;
    /**
     * List of all the maps.
     */
    private static Hashtable<String, StratMap> stratmaps = new Hashtable<String, StratMap>();    
    /**
     * List of all opened process variable graphs.
     */
    private static Vector<ProcessVariableXYGraph> pvGraphs = new Vector<ProcessVariableXYGraph>();
    /**
     * List of all opened process variable tables.
     */
    private static Vector<ProcessVariableTablePanel> pvTables = new Vector<ProcessVariableTablePanel>();
    /**
     * The list of parameters needed to re-open closed tables.
     */
    private static Vector<Hashtable> savedTableParameters = new Vector<Hashtable>();
    /**
     * The list of parameters needed to re-open closed graphs.
     */
    private static Vector<Hashtable> savedGraphParameters = new Vector<Hashtable>();
    /**
     * The list of all the regions the maps, graphs and tables are subscribed to.
     */
    private static Hashtable<Shape, RegionData> subscribedRegions = new Hashtable<Shape, RegionData>();
    /**
     * The grid used to indicate the initial locations of the opened graphs.
     */
    public static ProcessVariableXYGraph[][] locationGrid;
    
    /**
     * Creates default visualizer.
     *
     * @param client the client.
     */
    public Visualizer(Client client) {
        Visualizer.client = client;
    }
    
    /**
     * Creates visualizer and initializes the map.
     *
     * @param client the client.
     * @param shape shape of the visualized area.
     */
    public Visualizer(Client client, Shape shape) {
        this(client);
        StratMap stratmap = new StratMap(client, shape, "Stratmas Map No 1");
        stratmaps.put("Stratmas Map No 1", stratmap);
        // set the map to listen to the client
        client.addEventListener(stratmap.getMapDrawer());
        // initialize the grid of graphs
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int graphRows = (int) (screenSize.getHeight() / ProcessVariableXYGraph.DEFAULT_HEIGHT);
        int graphCols = (int) (screenSize.getWidth() / ProcessVariableXYGraph.DEFAULT_WIDTH);
        locationGrid = new ProcessVariableXYGraph[graphRows][graphCols];
    }
    
    /**
     * Creates new map with corresponding components.
     *
     * @param shape shape of the visualized area.
     */
    public static void createNewStratMap(Shape shape) {
        int nr = stratmaps.size()+1;
        StratMap stratmap = new StratMap(client, shape, "Stratmas Map No "+nr);
        // if grid exists
        if (gridData != null) {
            // create grid layer 
            stratmap.createGridLayer(gridData);
            // update the map with process variables
            stratmap.getPVPanel().addPVs(client.getProcessVariables());
            // update the map with factions
            stratmap.getPVPanel().addFactions(client.getFactions());
        }
        stratmaps.put("Stratmas Map No "+nr, stratmap);
        // set the map to listen to the client
        client.addEventListener(stratmap.getMapDrawer());
    }
    
    /**
     * Creates the grid that covers the defined region.
     *
     * @param gridData information needed to create the grid.
     */
    public void createGrid(GridData gridData) {
        Visualizer.gridData = gridData;
        // update the maps with the cells
        for (Enumeration<String> e = stratmaps.keys(); e.hasMoreElements(); ) {
            Object key = e.nextElement();
            stratmaps.get(key).createGridLayer(gridData);
        }
    }
    
    /**
     * Removes a map and the corresponding control window from the list.
     *
     * @param stratmap a stratmap object.
     */
    public static void removeMap(StratMap stratmap) {
        client.removeEventListener(stratmap.getMapDrawer());
        stratmap.remove();
        stratmaps.remove(stratmap.getTitle());
    }
    
    /**
     * Imports a process variable 
     *
     * @param pvd a process variable.
     */
    public void importProcessVariable(ProcessVariableDescription pvd) {
        for (Enumeration<String> e = stratmaps.keys(); e.hasMoreElements(); ) {
            stratmaps.get(e.nextElement()).getPVPanel().addPV(pvd);
        }
    }
    
    /**
     * Imports a faction.
     *
     * @param faction a faction.
     */
    public void importFaction(StratmasObject faction) {
        for (Enumeration<String> e = stratmaps.keys(); e.hasMoreElements(); ) {
            stratmaps.get(e.nextElement()).getPVPanel().addFaction(faction);
        }
    }
    
    /**
     * Removes all process variables and factions.
     */
    public void removeProcessVariablesAndFactions() {
        for (Enumeration<String> e = stratmaps.keys(); e.hasMoreElements(); ) {
            stratmaps.get(e.nextElement()).getPVPanel().clear();
        }
    }
    
    /**
     * Adds a graph to the list of graphs.
     *
     * @param graph the object to add to the list.  
     */
    public static void addGraph(ProcessVariableXYGraph graph) {
        pvGraphs.add(graph);
    }
    
    /**
     * Removes a graph from the list of graphs.
     *
     * @param graph the object to remove from the list.  
     */
    public static void removeGraph(ProcessVariableXYGraph graph) {
        pvGraphs.remove(graph);
        removeGraphFromGrid(graph);
    }
    
    /**
     * Adds a table to the list of tables.
     *
     * @param table the object to add to the list.     
     */
    public static void addTable(ProcessVariableTablePanel table) {
        pvTables.add(table);
    }
    
    /**
     * Removes a table from the list of tables.
     *
     * @param table the object to remove from the list.     
     */
    public static void removeTable(ProcessVariableTablePanel table) {
        pvTables.remove(table);
    }
    
    /**
     * Saves prameters for all opened graphs. These parameters are used to re-open the 
     * graphs later.
     */
    public static void saveOpenedGraphsParameters() {
        for (int i = 0; i < pvGraphs.size(); i++) {
            savedGraphParameters.add(pvGraphs.get(i).getParameters());
        }
    }
    
    /**
     * Re-opens the graphs from the saved parameters.
     */
    public static void reOpenGraphs() {
        while (!savedGraphParameters.isEmpty()) {
            Hashtable param = savedGraphParameters.remove(0);
            ProcessVariableDescription pv = client.getProcessVariable((String)param.get("processVariable"));
            if (pv != null) {
                Shape regShape = (Shape)param.get("shape");
                RegionData regionData = subscribedRegions.get(regShape);
                
                //  create new subscription
                if (regionData == null) {
                    regionData = new RegionData(regShape);
                    subscribedRegions.put(regShape, regionData);
                    client.subscribeRegion(regionData);
                }
                
                ProcessVariableXYGraph graph = new  ProcessVariableXYGraph((Timeline)param.get("timeline"),
                                                                           regionData,
                                                                           pv,
                                                                           (String[])param.get("factions"),
                                                                           (Color[])param.get("factionColors"),
                                                                           (String)param.get("regionId"));
                java.awt.Point p = (java.awt.Point)param.get("location");
                graph.setGraphLocation((int)p.getX(), (int)p.getY());
                graph.setGraphSize((Dimension)param.get("size"));
                addGraph(graph);
            }
        }
    }
    
    /**
     * Removes all opened graphs. 
     */
    public static void removeAllGraphs() {
        while (!pvGraphs.isEmpty()) {
            ProcessVariableXYGraph graph = pvGraphs.remove(0);
            graph.remove();
        }
    }
  
    /**
     * Saves prameters for all opened tables. These parameters are used to re-open the 
     * tables later.
     */
    public static void saveOpenedTablesParameters() {
        for (int i = 0; i < pvTables.size(); i++) {
            savedTableParameters.add(pvTables.get(i).getParameters());
        }
    }

    /**
     * Re-opens the tables from the saved parameters.
     */
    public static void reOpenTables() {
        while (!savedTableParameters.isEmpty()) {
            Hashtable param = savedTableParameters.remove(0);
            ProcessVariableTablePanel tablePanel = new ProcessVariableTablePanel(client, (Shape)param.get("shape"));
            tablePanel.setTableLocation((java.awt.Point)param.get("location"));
            tablePanel.setTableSize((Dimension)param.get("size"));
            addTable(tablePanel);
        }
    }
    
    /**
     * Removes all opened tables. 
     */
    public static void removeAllTables() {
        while (!pvTables.isEmpty()) {
            ProcessVariableTablePanel table = pvTables.remove(0);
            table.remove();
        }
    }
    
    /**
     * Removes a graph from the grid.
     *
     * @param graph the graph.
     */
    private static void removeGraphFromGrid(ProcessVariableXYGraph graph) {
        for (int i = 0; i < locationGrid.length; i++) {
            for (int j = 0; j < locationGrid[0].length; j++) {
                if (locationGrid[i][j] != null && locationGrid[i][j].equals(graph)) {
                    locationGrid[i][j] = null;
                }
            }
        }
    }
    
    /**
     * Returns the client this visualizer is connected to.
     */
    public Client getClient() {
        return Visualizer.client;
    }
    
    /**
     * Resets all the maps, graphs and tables.
     */
    public void reset() {
        // reset the maps
        for (Enumeration<String> e = stratmaps.keys(); e.hasMoreElements(); ) {
            stratmaps.get(e.nextElement()).reset();
        }
        // reset the tables
        for (Enumeration<ProcessVariableTablePanel> e = pvTables.elements(); e.hasMoreElements(); ) {
            e.nextElement().reset();
        }
    }
    
    /**
     * Resets the initial location grid.
     */
    public static void resetLocationGrid() {
        for (int i = 0; i < locationGrid.length; i++) {
            for (int j = 0; j < locationGrid[0].length; j++) {
                locationGrid[i][j] = null;
            }
        }
    }
    
    /**
     * Initializes the display of the process variables and factions on the map.
     */
    public static void setInitialView() {
        // set initial process variable and faction
        for (Enumeration<String> e = stratmaps.keys(); e.hasMoreElements(); ) {
            stratmaps.get(e.nextElement()).getPVPanel().setInitialView();
        }
        // re-open the tables
        reOpenTables();
        // re-open the graphs
        reOpenGraphs();
    }
    
    /**
     * Returns all the maps.
     */
    public static Hashtable<String, StratMap> getMaps() {
        return stratmaps;
    }
    
    /**
     * Returns all the graphs.
     */
    public static Vector<ProcessVariableXYGraph> getGraphs() {
        return pvGraphs;
    }
    
    /**
     * Sorts each table in the list of tables.
     */
    public static void sortTables() {
        for (int i = 0; i < pvTables.size(); i++) {
            ProcessVariableTablePanel pvtPanel = pvTables.get(i);
            ((ProcessVariableTableModel)pvtPanel.getTable().getModel()).sort();
        }
    }
    
    /**
     * Sorts the graphs in the list of graphs.
     */
    public static void sortGraphs() {
        Collections.sort(pvGraphs, ProcessVariableTablePanel.GRAPH_COMPARATOR);
    }
    
    /**
     * Adds a listener to a RegionData object. If the object doesn't exists, a new one is created and
     * subscribed to the server.
     *
     * @param region the shape which defines the region.
     * @param listener the object which listens to the RegionData object.
     */
    public static RegionData addListenerToRegionData(Shape region, StratmasEventListener listener) {
        // check if the region subscription exists
        RegionData regionData = subscribedRegions.get(region);
        
        //  create new subscription
        if (regionData == null) {
            regionData = new RegionData(region);
            subscribedRegions.put(region, regionData);
            client.subscribeRegion(regionData);
        }
        // add listener to the region data
        regionData.addListener(listener);
        
        return regionData;
    }
    
    /**
     * Removes a listener from a Region Data object. If the object has no more listeners it is removed from
     * the list of RegionData objects.
     *
     * @param regionData the RegionData object.
     * @param listener the listener which is to be removed from the RegionData object.
     */
    public static void removeListenerFromRegionData(RegionData regionData, StratmasEventListener listener) {
        regionData.removeListener(listener);
        if (!regionData.subscriptionExists()) {
            subscribedRegions.remove(regionData.getRegion());
        }
    }
    
    /**
     * Removes all maps, graphs and tables.
     */
    public static void remove() {
        // remove the maps 
        for (Enumeration<StratMap> e = stratmaps.elements(); e.hasMoreElements(); ) {
            e.nextElement().doExit();
        }
        // remove the graphs
        removeAllGraphs();
        savedGraphParameters.removeAllElements();
        // remove the tables
        removeAllTables();
        savedTableParameters.removeAllElements();
        
        subscribedRegions.clear();
    }
}

