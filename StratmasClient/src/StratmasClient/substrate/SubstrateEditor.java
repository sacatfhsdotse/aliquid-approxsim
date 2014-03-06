package StratmasClient.substrate;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JPopupMenu;
import javax.swing.JButton;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.JSlider;
import javax.swing.JOptionPane;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;

import StratmasClient.Client;
import StratmasClient.ProcessVariableDescription;
import StratmasClient.object.Shape;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.primitive.Reference;
import StratmasClient.object.type.TypeFactory;
import StratmasClient.filter.TypeFilter;
import StratmasClient.map.BasicMap;
import StratmasClient.map.BasicMapDrawer;
import StratmasClient.map.adapter.MapDrawableAdapter;
import StratmasClient.map.adapter.MapShapeAdapter;

/**
 * This class implements the substrate editor.
 */
public class SubstrateEditor {
    /**
     * Path to the images.
     */
    private static String path = "images/";
    /**
     * Reference to the client.
     */
    private Client client;
    /**
     * Reference to the shape.
     */
    private Shape shape;
    /**
     * Reference to the substrate drawer.
     */
    private SubstrateMapDrawer substrateDrawer;
    /**
     * Reference to the color chooser.
     */
    private ColorChooser colorChooser;
    /**
     * The list of initialized process variables. 
     */
    private Hashtable initProcessVariables = new Hashtable();
    /**
     * The actual process variable.
     */
    private ProcessVariableDescription actualProcessVariable;
    /**
     * The actual faction.
     */
    private Object actualFaction;
    /**
     * The process variable handler.
     */
    private ProcessVariableHandler processVariableHandler;
    /**
     * The faction handler.
     */
    private FactionHandler factionHandler;
    /**
     * The creator of GUI components.
     */
    private DisplayControl displayControl;
    /**
     * The panel for process variables and factions.
     */
    private JPanel pvPanel;
    /**
     * Name of the output file for the values from the editor.
     */
    private String outFile;
    /**
     * Reference to the XML reader.
     */
    private SubstrateXMLReader xmlReader;
    
    /**
     * Creates the substrate editor.
     *
     * @param client reference to the client.
     * @param shape the shape of the actual region.
     */
    public SubstrateEditor(Client client, Shape shape) {
        this.client = client;
        this.shape = shape;
        
        // create the basic map 
        BasicMap basicMap = new BasicMap(client, shape);
        
        // create the substrate drawer
        substrateDrawer = new SubstrateMapDrawer(basicMap, basicMap.getRegion(), this); 
        
        // create the process variable handler
        processVariableHandler = new ProcessVariableHandler(this);
        actualProcessVariable = processVariableHandler.getSelectedProcessVariable();
        
        // create the faction handler
        factionHandler = new FactionHandler(this);
        actualFaction = factionHandler.getSelectedFaction();
        
        // create the color chooser
        colorChooser = new ColorChooser(this);
        
        // the GUI components creator
        displayControl = new DisplayControl(this);
        
        // the XML reader
        xmlReader = new SubstrateXMLReader(this);
        
        // show the editor 
        createAndShowGUI("Substrate editor");
    }
    
    /**
     * Create the GUI and show it. 
     */
    private void createAndShowGUI(String title) {
        final SubstrateMapDrawer drawer = substrateDrawer;
        // set up the window
        final JFrame frame = substrateDrawer.getFrame();
        frame.setTitle(title);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener( new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    if (JOptionPane.showConfirmDialog(frame, "Do you want to exit?", "Exiting ...", 
                                                      JOptionPane.YES_NO_OPTION) == 0) {
                        drawer.doDispose();
                        // finally
                        System.exit(0);
                    }
                }
            });
        
        // necessary when heavyweight and lightweight components intersect
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
        
        // frame size 
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int frameWidth = 2 * screenSize.width / 3;
        int frameHeight = 2 * screenSize.height / 3;
        frame.setSize(frameWidth, frameHeight); 
        frame.setLocation((screenSize.width-frameWidth) >> 1, (screenSize.height-frameHeight) >> 1);
        
        // the south panel        
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(colorChooser, BorderLayout.CENTER);
        southPanel.add(displayControl.createTextFieldPanel(), BorderLayout.SOUTH);
        
        // the process variables panel
        pvPanel = displayControl.createPVPanel();
        pvPanel.setVisible(false);
        
        // the shape creation panel
        JPanel tmpPanel = new JPanel();
        tmpPanel.add(displayControl.createModeSelectionPanel());
        JPanel shapeCreationPanel = new JPanel(new BorderLayout());
        shapeCreationPanel.add(tmpPanel, BorderLayout.WEST);
        shapeCreationPanel.add(new JLabel(), BorderLayout.CENTER);
        shapeCreationPanel.setBorder(BorderFactory.createTitledBorder("Create Area Tools"));
        
        // the north panel
        JPanel northPanel = new JPanel(new BorderLayout(10, 10));
        northPanel.add(pvPanel, BorderLayout.WEST);
        northPanel.add(shapeCreationPanel, BorderLayout.CENTER);
        northPanel.add(displayControl.createNavigationPanel(), BorderLayout.EAST);
        northPanel.setBorder(BorderFactory.createTitledBorder(""));
        
        // add the canvas and the text field to the main panel
        JPanel editorPanel = new JPanel(new BorderLayout());
        editorPanel.add(substrateDrawer.getGLCanvas(), BorderLayout.CENTER);
        //editorPanel.add(displayControl.createModeSelectionPanel(), BorderLayout.NORTH);
        editorPanel.add(northPanel, BorderLayout.NORTH);
        editorPanel.add(southPanel, BorderLayout.SOUTH);
        
        // add the panel to the frame
        frame.setJMenuBar(displayControl.createMenuBar());
        frame.getContentPane().add(editorPanel, BorderLayout.CENTER);
        //frame.getContentPane().add(northPanel, BorderLayout.NORTH);
        frame.setResizable(true);
        
        // thread safety recomendation
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    frame.setVisible(true);
                }
            });
    }
    
    /**
     * Returns the client. 
     */
    public Client getClient() {
        return client;
    }
    
    /**
     * Exits the editor.
     */
    public void exit() {
        //update the shape values for the actual process variable and faction
        saveCreatedShapeValues(actualProcessVariable, actualFaction);
        // exit the drawer
        substrateDrawer.remove();
        substrateDrawer.doDispose();
    }
    
    /**
     * Returns the substrate drawer.
     */
    public SubstrateMapDrawer getSubstrateDrawer() {
        return substrateDrawer;
    }
    
    /**
     * Returns the color map panel.
     */
    public ColorChooser getColorChooser() {
        return colorChooser;
    }

    /**
     * Returns the actual process variable.
     */
    public ProcessVariableDescription getProcessVariable() {
        return actualProcessVariable;
    }
    
    /**
     * Sets the actual faction.
     */
    public void setFaction(Object faction) {
        actualFaction = faction;
    }
    
    /**
     * Returns the color which represents the minimum value.
     */
    public Color getInitialColor() {
        return colorChooser.getMappingColor(colorChooser.getMinValue());
    }
    
    /**
     * Returns the actual color.
     */
    public Color getActualColor() {
        return colorChooser.getActualColor();
    }
    
    /**
     * Returns the value color.
     */
    public double getActualValue() {
        return colorChooser.getActualValue();
    }
    
    /**
     * Returns the color for the given value.
     *
     * @param  value the given value.
     *
     * @return the color matching the value.
     */
    public Color getMappingColor(double value) {
        return colorChooser.getMappingColor(value);
    }

    /**
     * Returns the process variable handler.
     */
    public ProcessVariableHandler getProcessVariableHandler() {
        return processVariableHandler;
    }
    
    /**
     * Returns the faction handler.
     */
    public FactionHandler getFactionHandler() {
        return factionHandler;
    }
    
    /**
     * Updates values for all the factions of a process variable.
     *
     * @param pv the actual process variable.
     * @param modifiedAdapters list of modified shape adapters.
     */
    private void updateEachFaction(ProcessVariableDescription pv, Vector modifiedAdapters) {
        Vector factions = factionHandler.getFactions();
        for (int i = 0; i < factions.size(); i++) {
            Hashtable shapeValues = createShapeValuePairList(modifiedAdapters);
            Hashtable facTable = (initProcessVariables.containsKey(pv))? (Hashtable)initProcessVariables.get(pv) : new Hashtable();  
            if (facTable.containsKey(factions.get(i))) {
                Hashtable vals = (Hashtable)facTable.get(factions.get(i));
                for (Enumeration e = shapeValues.keys(); e.hasMoreElements();) {
                    StratmasObject shape = (StratmasObject)e.nextElement();
                    if (vals.containsKey(shape)) {
                        Vector savedPairValues = (Vector)vals.get(shape);
                        Vector newPairValues = (Vector)shapeValues.get(shape);
                        if (((ShapeValuePair)newPairValues.firstElement()).getShape().equals(shape)) {
                            vals.put(shape, newPairValues);
                        }
                        else {
                            for (int j = 0; j < newPairValues.size(); j++) {
                                savedPairValues.add(newPairValues.get(j));
                            }
                        }
                    }
                    else {
                        vals.put(shape, shapeValues.get(shape));
                    }
                }
            }
            else {
                facTable.put(factions.get(i), shapeValues);
            }
            initProcessVariables.put(pv, facTable); 
        }
    }
    
    /**
     * Updates values for a process variable and a faction.
     *
     * @param pv the actual process variable.
     * @param faction the actual faction.
     * @param modifiedAdapters list of modified shape adapters.
     */
    private void updateInitProcessVariables(ProcessVariableDescription pv, StratmasObject faction, Vector modifiedAdapters) {
        Hashtable facTable = (initProcessVariables.containsKey(pv))? (Hashtable)initProcessVariables.get(pv) : new Hashtable();  
        facTable.put(faction, createShapeValuePairList(modifiedAdapters));
        initProcessVariables.put(pv, facTable); 
    }
    
    /**
     * Updates values for a process variable without factions.
     *
     * @param pv the actual process variable.
     * @param modifiedAdapters list of modified shape adapters.
     */
    private void updateInitProcessVariables(ProcessVariableDescription pv, Vector modifiedAdapters) {
        // process variables without factions
        initProcessVariables.put(pv, createShapeValuePairList(modifiedAdapters));
    }
    
    /**
     * Creates a list of ShapeValuePair objects for the modified adapters.
     *
     * @param modifiedAdapters list of modified shape adapters.
     */
    private Hashtable createShapeValuePairList(Vector modifiedAdapters) {
        Hashtable shapeValues = new Hashtable();
        // update the shape values for the process variable and faction
        for (int i = 0; i < modifiedAdapters.size(); i++) {
            MapShapeAdapter msa = (MapShapeAdapter)modifiedAdapters.get(i);
            Vector sValues = new Vector();
            // get the shape value first ...
            Object objVal = substrateDrawer.getShapeValues().get(msa);
            if (objVal != null) {
                sValues.add(objVal);        
            }
            // ... then the intersecting shapes and the values 
            Vector iShapes = msa.getIntersectingShapes();
            for (int j = 0; j < iShapes.size(); j++) {
                Shape sh = (Shape)iShapes.get(j);
                if (substrateDrawer.getCreatedShapeValues().get(sh) != null) {
                    sValues.add((ShapeValuePair)substrateDrawer.getCreatedShapeValues().get(sh));
                }
            }
            // update shapes with new values
            shapeValues.put(msa.getObject(), sValues);
        }
        return shapeValues;
    }
    
    /**
     * Updates the drawer with defined regions for the actual process variable (and faction).
     */
    private void updateSubstrateMapDrawer() {
        Hashtable sValues = new Hashtable();
        if(initProcessVariables.containsKey(actualProcessVariable)) {
            Hashtable hTable = (Hashtable)initProcessVariables.get(actualProcessVariable);
            if (!actualProcessVariable.hasFactions()) {
                sValues = hTable;
            }
            else if (actualFaction instanceof StratmasObject && hTable.containsKey(actualFaction)) {
                sValues = (Hashtable)hTable.get(actualFaction);
            }
        }
        substrateDrawer.resetShapeValues(sValues);
    }
    
    /**
     * Updates the process variables with new shape values. 
     */
    protected void updateProcessVariableValues(ProcessVariableDescription pv) {
        if (!pv.equals(actualProcessVariable)) {
            // reset the buttons
            displayControl.resetShapeCreatorButtons();
            // update the shape values for the previous process variable and faction
            saveCreatedShapeValues(actualProcessVariable, actualFaction);
            // update the actual process variable
            actualProcessVariable = pv;
            // update the color panel
            colorChooser.update(actualProcessVariable);
            // the actual process variable has factions and the factions exist
            if (pv.hasFactions() && factionHandler.hasFactions()) {
                // enable faction selection
                factionHandler.enableSelection(true);
                // set actual faction
                actualFaction = factionHandler.getSelectedFaction();
                // set editing
                enableEditComponents(true);
            }
            else {
                // set actual faction
                actualFaction = null;
                // disable faction selection
                factionHandler.enableSelection(false);
                // set editing
                enableEditComponents((actualProcessVariable.hasFactions())? false : true);
            }
            // update the drawing area
            updateSubstrateMapDrawer();
        }
    }
    
    /**
     * Updates the process variables with new shape values. 
     */
    protected void updateProcessVariableValues(Object faction) {
        if (!faction.equals(actualFaction)) {
            // reset the buttons
            displayControl.resetShapeCreatorButtons();
            // update the shape values for the previous process variable and faction
            saveCreatedShapeValues(actualProcessVariable, actualFaction);
            // update the actual faction
            actualFaction = faction;
            // update the drawing area
            updateSubstrateMapDrawer();
        }
    }
    
    /**
     * Saves the values for the process variable and faction. 
     */
    private void saveCreatedShapeValues(ProcessVariableDescription pvd, Object faction) {
        if (substrateDrawer.getSubstrateMode() == SubstrateMapDrawer.CREATE_CIRCLE_MODE ||
            substrateDrawer.getSubstrateMode() == SubstrateMapDrawer.CREATE_RECTANGLE_MODE ||
            substrateDrawer.getSubstrateMode() == SubstrateMapDrawer.CREATE_POLYGON_MODE) {
            substrateDrawer.addActualShapeArea();
        }
        Vector modifiedAdapters = new Vector();
        Vector shapeAdapters = substrateDrawer.mapDrawableAdapters(MapShapeAdapter.class);
        // get the modified adapters
        for (int i = 0; i < shapeAdapters.size(); i++) {
            MapShapeAdapter msa = (MapShapeAdapter)shapeAdapters.get(i);
            if  (substrateDrawer.getShapeValues().get(msa) != null || !msa.getIntersectingShapes().isEmpty()) {
                // add modified adapter
                modifiedAdapters.add(msa);
            }
        }
        // check if the process variable and the faction(s) has to be updated
        if (!modifiedAdapters.isEmpty()) {
            // save the shape values
            if (faction == null) {
                updateInitProcessVariables(pvd, modifiedAdapters);
            }
            else if (faction instanceof StratmasObject) {
                updateInitProcessVariables(pvd, (StratmasObject)faction, modifiedAdapters);
            }
            else {
                updateEachFaction(pvd, modifiedAdapters);
            }
        }
    }
    
    /**
     * Imports process variables from the client.
     */
    public void importProcessVariablesFromClient() {
        processVariableHandler.importProcessVariables(getClient());
        enableEditor();
    }
    
    /**
     * Imports process variables from a file.
     */
    public boolean importProcessVariablesFromFile(String filename) {
        Vector pvs = SubstrateXMLReader.getProcessVariables(filename);
        if (!pvs.isEmpty()) {
            processVariableHandler.importProcessVariables(pvs);
            enableEditor();
            return true;
        }
        return false;
    }
    
    /**
     * Enables the editor for editing. 
     */
    private void enableEditor() {
        enableFactionItems();
        enableSaveItems();
        //disableImportItems();
        actualProcessVariable = processVariableHandler.getSelectedProcessVariable();
        actualFaction = (factionHandler.hasFactions())? factionHandler.getSelectedFaction() : null;
        if (!actualProcessVariable.hasFactions() || factionHandler.hasFactions()) {
            enableEditComponents(true);        
        }
        colorChooser.update(actualProcessVariable);
        pvPanel.setVisible(true);
    }
    
    /**
     * Enables several components in the editor.
     */
    protected void enableEditComponents(boolean enable) {
        displayControl.enableFillButton(enable);
        //displayControl.enableRegionButton(enable);
        displayControl.enableUndoButton(enable);
        displayControl.setEnabledShapeCreatorButtons(enable);
        displayControl.enableColorMapOptionsItem(enable);
    } 
    
    /**
     * Enables the menu items used to create and remove factions.
     */
    private void enableFactionItems() {
        displayControl.enableCreateFactionItem(true);
    }
    
    /**
     * Enables the menu items used to save initialized process variables.
     */
    private void enableSaveItems() {
        displayControl.enableSaveItem(true);
        displayControl.enableSaveAsItem(true);
    }
    
    /**
     * Disables the menu items used to import process variables.
     */
    private void disableImportItems() {
        displayControl.enableImportPVItem(false);
    }
    
    /**
     * Removes all shape values for this faction. If the faction is currently selected nothing is done. 
     *
     * @param faction the faction.
     */
    protected void removeFactionValues(StratmasObject faction) {
        if (!faction.equals(actualFaction)) {
            for (Enumeration e = initProcessVariables.elements(); e.hasMoreElements();) {
                Hashtable hTable = (Hashtable)e.nextElement();
                hTable.remove(faction);
            }
        }
    }

    /**
     * Returns the values from the editor.
     */
    protected Vector getValuesForFile() {
        // update the shape values for the actual process variable and faction
        saveCreatedShapeValues(actualProcessVariable, actualFaction);
        // save values to file
        Vector pvValues = new Vector();
        for (Enumeration e = initProcessVariables.keys(); e.hasMoreElements();) {
            ProcessVariableDescription pvd = (ProcessVariableDescription)e.nextElement();
            if (pvd.hasFactions()) {
                Hashtable facTable = (Hashtable)initProcessVariables.get(pvd);
                for (Enumeration ee = facTable.keys(); ee.hasMoreElements();) {
                    StratmasObject faction = (StratmasObject)ee.nextElement();
                    pvValues.add(new ProcessVariableInitialValues(pvd, faction, (Hashtable)facTable.get(faction)));
                }
            }
            else {
                pvValues.add(new ProcessVariableInitialValues(pvd, null, (Hashtable)initProcessVariables.get(pvd)));
            }
        }
        return pvValues;
    }
    
    /**
     * Returns the values from the editor.
     */
    protected Vector getValuesForServer() {
        // save values to file
        Vector pvValues = new Vector();
        for (Enumeration e = initProcessVariables.keys(); e.hasMoreElements();) {
            ProcessVariableDescription pvd = (ProcessVariableDescription)e.nextElement();
            if (pvd.hasFactions()) {
                Hashtable facTable = (Hashtable)initProcessVariables.get(pvd);
                for (Enumeration ee = facTable.keys(); ee.hasMoreElements();) {
                    StratmasObject faction = (StratmasObject)ee.nextElement();
                    pvValues.add(new ProcessVariableInitialValues(pvd, faction, (Hashtable)facTable.get(faction)));
                }
            }
            else {
                pvValues.add(new ProcessVariableInitialValues(pvd, null, (Hashtable)initProcessVariables.get(pvd)));
            }
        }
        return pvValues;
    }


    /**
     * Saves values from the editor to a user defined file.
     */
    protected void saveValuesToNewFile() {
        String filename = Client.getFileNameFromDialog(".ini", JFileChooser.SAVE_DIALOG, new JFrame());
        saveToFile(filename);
    }
    
    /**
     * Saves values from the editor to an existing  file.
     */
    protected void saveValuesToExistingFile() {
        outFile = (outFile != null)? outFile : Client.getFileNameFromDialog(".ini", JFileChooser.SAVE_DIALOG, new JFrame());
        saveToFile(outFile);
    }
    
    /**
     * Saves values from the editor to a file.
     */
    protected void saveToFile(String filename) {
        try {
            if (filename != null) {
                BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
                SubstrateXMLWriter.convertToXML(writer, getValuesForFile());
                writer.close();
            }
        } 
        catch (IOException e) {
        }
    }
    
    /**
     * Read shape values from file.
     */
    protected void readValuesFromFile() {
        String filename = Client.getFileNameFromDialog(".ini", JFileChooser.OPEN_DIALOG, new JFrame());
        if (filename != null) {
            Vector values = xmlReader.getShapeValuesForProcessVariables(filename);
            for (int i = 0; i < values.size(); i++) {
                updateEditor((ProcessVariableInitialValues)values.get(i));
            }
            // update the drawing area
            updateSubstrateMapDrawer();
            
            if (!values.isEmpty()) {
                enableEditor();
            }
        }
    }
    
    /**
     * Updates the editor with shapes and values for a process variable and faction.
     *
     * @param pviValues values to update the editor.
     */
    protected void updateEditor(ProcessVariableInitialValues pviValues) {
        Vector shapeList = pviValues.getOrderedListOfShapes();
        for (int i = 0; i < shapeList.size(); i++) {
            if (pviValues.getFaction() == null) {
                addShapeValuePair(pviValues.getProcessVariable(), (ShapeValuePair)shapeList.get(i));  
            }
            else {
                addShapeValuePair(pviValues.getProcessVariable(), pviValues.getFaction(), (ShapeValuePair)shapeList.get(i));   
            }
        }
    } 
    
    
    /**
     * Adds a shape and the assigned value to the list of initialized shapes.
     */
    protected void addShapeValuePair(ProcessVariableDescription pv, ShapeValuePair svpair) {
        Hashtable values = (initProcessVariables.containsKey(pv))? (Hashtable)initProcessVariables.get(pv) : new Hashtable();
        addShapeValuePair(values, svpair);
        initProcessVariables.put(pv, values);
    }
    
    /**
     * Adds a shape and the assigned value to the list of initialized shapes.
     */
    protected void addShapeValuePair(ProcessVariableDescription pv, StratmasObject faction, ShapeValuePair svpair) {
        Hashtable values = (initProcessVariables.containsKey(pv))? (Hashtable)initProcessVariables.get(pv) : new Hashtable();
        Hashtable facTable = (values.containsKey(faction))? (Hashtable)values.get(faction) : new Hashtable();
        addShapeValuePair(facTable, svpair);
        values.put(faction, facTable);
        initProcessVariables.put(pv, values);
    }
    
    /**
     * Adds a shape and the assigned value to the list of initialized shapes. 
     */
    protected void addShapeValuePair(Hashtable hValues, ShapeValuePair svpair) {
        // add ESRI shape
        if (substrateDrawer.isEsri(svpair.getShape())) { 
            Vector svpList = new Vector();
            svpList.add(svpair);
                hValues.put(svpair.getShape(), svpList);
        }
        // add created shape
        else {
            Vector intShapes = substrateDrawer.getIntersectingShapes(svpair.getShape());   
            for (int i = 0; i < intShapes.size(); i++) {
                Shape sh = (Shape)intShapes.get(i);
                if (hValues.containsKey(sh)) {
                    ((Vector)hValues.get(sh)).add(svpair);
                }
                else {
                    Vector svpList = new Vector();
                    svpList.add(svpair);
                    hValues.put(sh, svpList); 
                }
            } 
        }
    }
    
    /**
     * Returns shape with the given reference. If no shape is found null is returned.
     */
    public Shape getESRIShape(Reference ref) {
        MapDrawableAdapter adapter = substrateDrawer.getMapDrawableAdapter(ref);
        if (adapter != null && adapter instanceof MapShapeAdapter) {
            return (Shape)adapter.getObject();
        }
        else {
            return null;
        }
    }
    
    /**
     * Returns the shape values in XML format. These values are uset to initialize the server.
     */
    public StringBuffer getXMLValuesForServer() {
        Vector values = getValuesForServer();
        // no values to deliver
        if (values.isEmpty()) {
            return null;
        }
        // write the values
        try {
            StringWriter stringWriter = new StringWriter();
            BufferedWriter writer = new BufferedWriter(stringWriter);
            SubstrateXMLWriter.convertToXMLForServer(writer, getValuesForFile());
            writer.close();
            stringWriter.flush();
            return stringWriter.getBuffer();
        } 
        catch (IOException e) {
        }
        return null;
    }
    
}

