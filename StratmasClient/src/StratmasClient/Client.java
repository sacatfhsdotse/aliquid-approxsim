// $Id: Client.java,v 1.194 2007/01/31 10:11:43 alexius Exp $

package StratmasClient;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import java.net.MalformedURLException;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.text.ParseException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.xml.sax.InputSource;

import StratmasClient.communication.DisconnectMessage;
import StratmasClient.communication.GridData;
import StratmasClient.communication.LayerData;
import StratmasClient.communication.LayerSubscription;
import StratmasClient.communication.RegionData;
import StratmasClient.communication.RegionSubscription;
import StratmasClient.communication.ResetMessage;
import StratmasClient.communication.ServerCapabilitiesMessage;
import StratmasClient.communication.ServerConnection;
import StratmasClient.communication.ServerException;
import StratmasClient.communication.StepMessage;
import StratmasClient.communication.StratmasSocket;
import StratmasClient.communication.SubscriptionCounter;
import StratmasClient.communication.SubscriptionHandler;
import StratmasClient.communication.Unsubscription;
import StratmasClient.communication.XMLHandler;
import StratmasClient.communication.InitializationMessage;
import StratmasClient.communication.StratmasMessage;
import StratmasClient.communication.GetGridMessage;
import StratmasClient.communication.RegisterForUpdatesMessage;

import StratmasClient.dispatcher.StratmasDispatcher;

import StratmasClient.filter.TypeFilter;

import StratmasClient.map.GridLayer;
import StratmasClient.map.StratMap;
import StratmasClient.map.Visualizer;

import StratmasClient.substrate.SubstrateEditor;
import StratmasClient.substrate.SelectShapeDialog;

import StratmasClient.object.Shape;
import StratmasClient.object.StratmasDuration;
import StratmasClient.object.StratmasEvent;
import StratmasClient.object.StratmasEventListener;
import StratmasClient.object.StratmasGUIConstructor;
import StratmasClient.object.StratmasGUIConstructorDialog;
import StratmasClient.object.StratmasList;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasObjectFactory;
import StratmasClient.object.StratmasTimestamp;

import StratmasClient.object.primitive.Duration;
import StratmasClient.object.primitive.Timestamp;

import StratmasClient.object.type.Declaration;
import StratmasClient.object.type.Type;
import StratmasClient.object.type.TypeFactory;

import StratmasClient.TaclanV2.TaclanV2Utils;

import StratmasClient.timeline.Timeline;

import StratmasClient.treeview.TreeView;

/**
 * STRATMAS client controls communication between the simulator, the xml parser and the STRATMAS map.
 *
 * @version 1.0 $Date: 2007/01/31 10:11:43 $
 *
 * @author Amir Filipovic, Daniel Ahlin, Per Alexius 
 */
public class Client implements StratmasEventListener {
    /**
     * Default port to connect to.
     */
    private int port = 28444;
    /**
     * Contains all the maps and the corresponding components. 
     */
    private Visualizer visualizer;
    /**
     * Timeline.
     */
    private Timeline timeline;
    /**
     * Controls the simulation with the controlling panel.
     */
    private Controller controller;
    /**
     * List of process variables.
     */
    private Vector process_variables;
    /**
     * The name of the server to use.
     */
    private String serverName = null;
    /**
     * Indicates the status of the server. 
     */
    private Hashtable status = new Hashtable();
    /**
     * Indicates if this client is an active client.
     */
    private boolean isActiveClient;
    /**
     * Indicates if the client is connected to the server.
     */
    private boolean connected = false;
    /**
     * Arguments to client
     */
    private String[] parameters;
    /**
     * Root object
     */
    private StratmasObject rootObject;
    /**
     * Clone of the Root object uset to reset the simulation.
     */
    private StratmasObject rootClone;
    /**
     * Handles subscription to and export of pv to file for a certain region.
     */
    StreamPVExporter mPVExporter = null;
    /**
     * Handles xml messages.
     */
    private XMLHandler xml_handler;
    /**
     * Handles subscriptions.
     */
    private SubscriptionHandler subscription_handler;
    /**
     * Handles connection to the server. 
     */
    private ServerConnection server_connection;
    /**
     * Contains eventListeners listening to this object.
     */
    private Hashtable eventListeners = new Hashtable();
    /**
     * If the client was started in batch mode.
     */
    private boolean inBatchMode = false;
    /**
     * If the client was started in batch continuous mode.
     */
    private boolean inBatchContinuousMode = false;
    /**
     * If the client was started in batch passive mode.
     */
    private boolean inBatchPassiveMode = false;
    /**
     * If the client was started in default mode.
     */
    private boolean inDefaultMode = false;
    /**
     * If the client was started in the substrate editor mode.
     */
    private boolean substrateEditorMode = false;
    /**
     * The the stream to which batchmode outputs the data.
     */
    private OutputStreamWriter batchModeOutputWriter = null;
    /**
     * The duration of batchmode.
     */
    private Duration batchModeDuration = null;
    /**
     * The name of the indata file or null if there is no such file.
     */
    private String fileName = null;
    /**
     * The GridData
     */
    private GridData gridData = null;
    /**
     * The dispatcher used to handle out servers.
     */
    StratmasDispatcher stratmasDispatcher = null;
    /**
     * Currently running Client instance.
     */
    static Client client = null;
    /**
     * The substrate editor.
     */
    private SubstrateEditor substrateEditor;
    /**
     * The main frame for the client.
     */
    private ClientMainFrame clientMainFrame;
    
    /**
     * Creates stratmas client.
     *
     * @param parameters the parameters to the client.
     */
    public Client(String[] parameters) {
        if (Client.client != null) {
            Debug.err.println("Warning: At least two instances of " + getClass().getName());
        } 
        else {
            Client.client = this;
        }
        String portStr = System.getProperty("PORT");
        if (portStr != null) {
            try {
                port = Integer.parseInt(portStr);
            } 
            catch (NumberFormatException e) {
            }
        }
        //Debug.err.println("Port: " + port);
        
        // create root list
        this.rootObject = StratmasObjectFactory.createList(TypeFactory.getType("Root").getSubElement("identifiables"));
        // set up listener
        rootObject.addEventListener(this);
        
        // create subscription handler.
        subscription_handler = new SubscriptionHandler();
        subscription_handler.start();
        
        // create xml handler.
        xml_handler = new XMLHandler(this, "stratmasProtocol.xsd");
        xml_handler.connect(subscription_handler);
        xml_handler.start();
        
              // sets inBatchMode and possibly fileName
        handleArguments(parameters);

        initClientMainFrame((substrateEditorMode)? false : true);
        
        // passive client
        if (inBatchPassiveMode) {
            StratmasDialog.showProgressBarDialog(null, "Initializing - passive mode ...");
            Thread thread = new Thread() {
                    public void run() {
                        Client.client.getRootObjectFromServer();
                        StratmasDialog.quitProgressBarDialog();
                    }
                };
            thread.start();        
        } 
        // active client - batch mode
        else if (inBatchMode || inBatchContinuousMode || (fileName != null && !substrateEditorMode)) {
            getRootObjectFromFile();
        }
    }
    
    /**
     * Creates stratmas client.
     */
    public Client() {
        this(new String[0]);
    }
    
    /**
     * For CommTest PLEASE DON'T REMOVE!!!!!!
     */
    public Client(String foo) {
    }
    
    /**
     * Handles the command line arguments. 
     *
     * @param args the list of arguments. 
     */
    public void handleArguments(String [] args) {
        this.parameters = args;
        if (parameters.length > 2) {
            System.err.println("Usage: " + Client.class.toString() + 
                               " [-batch=server,simulationtime,batchmode,outfile file.tl2] [file.tl2]");
            System.exit(1);
        } 
        else if (parameters.length == 2) {
            // batch mode (or error)
            if (parameters[0].matches("-batch=.*,.*")) {
                if (!parameters[1].equals("nofile")) {
                    this.fileName = parameters[1];
                }
                // Cut out simulationtime and outfile:
                String[] parts = parameters[0].split("[=,]");
                if (parts.length == 5) {
                    try {
                        batchModeOutputWriter = new OutputStreamWriter(new FileOutputStream(parts[3]));
                        batchModeDuration = Duration.parseDuration(parts[2]);
                    } catch (ParseException e) {
                        System.err.println("Unable to parse \"" + parts[2] + "\" as a duration");
                        System.exit(1);
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                        System.exit(1);
                    }   
                    if (parts[1].equals("?")) {
                        setServerNameGUI();
                    } else {
                        setServerName(parts[1]);
                    }
                    if (parts[3].equals("default")) {
                        this.inDefaultMode = true;
                    }
                    else if (parts[3].equals("batch")){
                        this.inBatchMode = true;
                    }
                    else if (parts[3].equals("continuous")){
                        this.inBatchContinuousMode = true;
                    }
                    else if (parts[3].equals("passive")){
                        this.inBatchPassiveMode = true;
                    }
                } else {
                    System.err.println("Unable to parse arguments to batch-mode.");
                    System.exit(1);
                }
            }
            else {
                System.err.println("Usage: " + Client.class.toString() + 
                                   " <-batch=simulationtime;outfile> [file.tl2]");
                System.exit(1);                
            }
        }
        else if (parameters.length == 1) {
            if (parameters[0].matches("-substrate=.*")) {
                setSubstrateEditorMode(true);
                String[] parts = parameters[0].split("=");
                if (parts[1].equals("?")) {
                    SelectShapeDialog.showDialog(this);
                } 
                else {
                    startSubstrateEditor(parts[1]); 
                }
            }
            else {
                this.fileName = parameters[0];
            }
        }
    }

    /**
     * Creates new substrate editor.
     *
     * @param filename name of the shape file.
     */
    public void startSubstrateEditor(final String filename) {
        final Client self = this;
        // check if the file is scenario
        if (filename.endsWith(".scn")) {
            this.fileName = filename;        
        }
        setSubstrateEditorMode(true);
        Thread thread = new Thread() {
                public void run() {
                    StratmasDialog.showProgressBarDialog(null, "Starting substrate editor ...");
                    StratmasObject obj = (self.fileName != null)? importXMLSimulation(self.fileName) : Client.getTemplateSimulation(filename);
                    if (obj != null) {
                        if (obj.getParent() != null) {
                            obj.remove();
                        }
                        self.getRootObject().add(obj);
                        final Shape shape = (Shape) ((StratmasObject)self.getRootObject().children().
                                                     nextElement()).getChild("scenario").getChild("map");
                        // create new editor 
                        self.setSubstrateEditor(new SubstrateEditor(self, shape));
                    }
                    else {
                        self.setSubstrateEditorMode(false); 
                        StratmasDialog.quitProgressBarDialog();
                        return;
                    }
                    self.setActiveClient(true);
                    // quit the progress bar
                    StratmasDialog.quitProgressBarDialog();
                }
            };
        thread.start();
    } 
    
    /**
     * Sets the substrate editor.
     */
    public void setSubstrateEditor(SubstrateEditor substrateEditor) {
        this.substrateEditor = substrateEditor;
    }
    
    /**
     * Returns the main frame.
     */
    public ClientMainFrame getClientMainFrame() {
        return clientMainFrame;
    }
    
    /**
     * Returns the initial values for the process variables.
     */
    public StringBuffer getInitialValuesForProcessVariables() {
        if (substrateEditor != null) {
            return substrateEditor.getXMLValuesForServer();
        }
        return null;
    }
    
    /**
     * Establishes a server connection using the provided socket, returns false on failure.
     *
     * @param socket the (already connected) socket to use.
     */
    public boolean establishServerConnection(StratmasSocket socket) {
        // new conection to the server
        server_connection = new ServerConnection(this, xml_handler, socket);
        server_connection.start();
        subscription_handler.connect(server_connection);

        setConnected(true);
        return true;
    }
    
    /**
     * Establishes a server connection, returns false on failure.
     */
    public boolean establishServerConnection() {
        // new conection to the server
        server_connection = new ServerConnection(this, xml_handler, serverName, port);
        subscription_handler.connect(server_connection);
        server_connection.start();

        String toRemove = "ConnectResponseMessage";

        // Wait until response arrives
        synchronized(this) {
            while(!status.containsKey("ConnectResponseMessage")) {
                if (status.containsKey("Unknown")) {
                    toRemove = "Unknown";
                    break;
                }
                else if (status.containsKey("ConnectMessage")) {
                    toRemove = "ConnectMessage";
                    break;
                }
                try {
                    Debug.err.println("waits");
                    this.wait();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        String status_message = (String)status.remove(toRemove);
        // if connection unsuccesfull
        if (status_message.equals("error")) {
            serverName = null;
            server_connection.kill();
            return false;
        }
        else {
            setConnected(true);
            return true;
        }
    }
    
    /**
     * Gets the root object from the server - used for passive clients.
     */
    public void getRootObjectFromServer() {
        this.serverName = getServerName();
        if (this.serverName == null) {
            return;
        }
        
        if (!establishServerConnection()) {
            return;
        }
        else if (isActiveClient) {
            StratmasDialog.showErrorMessageDialog(null, 
                                                  "There was no initialized Simulation on server " + 
                                                  serverName,
                                                  "No Simulation Found"); 
            // disconnect
            client.getServerConnection().disconnect(1000);
            client.setConnected(false);
            serverName = null;
            status.clear();
            return;
        }
            
        try {
            getServerConnection().blockingSend(new StratmasClient.communication.GetGridMessage());
            getServerConnection().blockingSend(new StratmasClient.communication.RegisterForUpdatesMessage(true));
        } catch (ServerException e) {
            Debug.err.println(e);
            return;
        }

        createGrid();

        // get ServerCapabilities
        if (!getServerCapabilities()) {
            System.err.println("Couldn't get ServerCapabilities. Quitting...");
            System.exit(0);
        }
        setActiveClient(true); // ????
        controller.updateSimulationMode("continuous");
        controller.start();
    }
    
    /**
     * Creates new root object by showing the gui constructor for the root object type.
     */    
    public void getRootObjectFromGUIConstructor() {        
        final Declaration rootDeclaration = TypeFactory.getType("Root").getSubElement("identifiables");
        
        final JDialog chooser = new JDialog((Frame) null, "Create " + rootDeclaration.getType().getName(), true);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Choose the type of simulation to create."));
        
        final TypeSelector typeSelector = new TypeSelector(rootDeclaration.getType());
        // Hack to get selected item implicitly selected item.
        typeSelector.setSelectedIndex(0);
        JPanel subpanel = new JPanel();
        subpanel.add(typeSelector);
        subpanel.add(new JButton(new AbstractAction("Create") {
                public void actionPerformed(ActionEvent e) {
                    Declaration declaration = rootDeclaration.clone(typeSelector.getSelectedType());
                    declaration.setMinOccurs(1);
                    declaration.setMaxOccurs(1);
                    declaration.setUnbounded(false);
                    StratmasGUIConstructorDialog deferedDialog = 
                        StratmasGUIConstructor.buildDialog(StratmasObjectFactory.guiCreate(declaration), false);
                    deferedDialog.setVisible(true);
                    StratmasObject sObj = deferedDialog.getStratmasObject();
                    if (sObj != null) {
                        getRootObject().add(sObj);
                    }
                    chooser.dispose();
                }
            }));        
        
        panel.add(subpanel);
        panel.add(new JButton(new AbstractAction("Cancel") {
                public void actionPerformed(ActionEvent e) {
                    chooser.dispose();
                }            
            }));
        
        chooser.getContentPane().add(panel);
        chooser.pack();
        chooser.setVisible(true);
        
        return;
    }
    
    /**
     *
     */
    public void getRootObjectFromFile() {
        StratmasDialog.showProgressBarDialog(null, "Loading a scenario ...");
        final Client self = this;
        Thread thread = new Thread() {
                public void run() {
                    StratmasObject rootObj = (self.fileName != null)? importXMLSimulation(self.fileName) : importXMLSimulation();
                    
                    // if above did not succeed we return
                    if (rootObj == null) {
                        StratmasDialog.quitProgressBarDialog();
                         return;
                    }
                    
                    if (rootObj.getParent() != null) {
                        rootObj.remove();
                    }
                    
                     getRootObject().add(rootObj);
                     setActiveClient(true);
                     StratmasDialog.quitProgressBarDialog();
                 }
            };
        thread.start();
    }
    
    /**
     *
     */
    public boolean getServerCapabilities() {
        try {
            ServerCapabilitiesMessage scm = new ServerCapabilitiesMessage();
            server_connection.blockingSend(scm);
            
            String stat = (String)status.remove(scm.getTypeAsString());
            if (stat == null) {
                stat = (String)status.remove("Unknown");
                if (stat == null) {
                    throw new AssertionError("No matching status response to " + scm.getTypeAsString());
                }
            }
            // Return true if successful, false otherwise.
             return !stat.equals("error");
        } catch (ServerException e) {
            return false;
        }
    }
    
    /**
     * Sets up a default ClientMainFrame.
     */
    public void initClientMainFrame(boolean show) {
        try {
           //TODO make setting
           UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception e) {
            e.printStackTrace();
        }
    	
        clientMainFrame =  new ClientMainFrame(getClient());
        final ClientMainFrame mainFrame =  clientMainFrame;
         if (getRootObject() != null) {
            getRootObject().addEventListener(new StratmasEventListener() {
                    public void eventOccured(StratmasEvent event) {
                        if (event.isObjectAdded()) {                        
                            mainFrame.tabFrame(TreeView.getDefaultFrame((StratmasObject) event.getArgument()));
                        } 
                        else if (event.isRemoved()) {                        
                            ((StratmasObject) event.getSource()).removeEventListener(this);
                        } 
                        else if (event.isReplaced()) {                        
                            // UNTESTED - the replace code is untested 2005-09-22
                            Debug.err.println("FIXME - Replace behavior untested in Client");
                            ((StratmasObject)event.getSource()).removeEventListener(this);
                            ((StratmasObject)event.getArgument()).addEventListener(this);
                         }
                    }
                });
         }
        // show the frame
        if (show) {
            SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        mainFrame.pack();
                        mainFrame.setSize(400, 600);
                        mainFrame.setVisible(true);
                    }
                });
        }
    }
    
    /**
     * Called when a new simulation is added to this client.
     */
    public void importSimulation(StratmasObject simulation) {
        // Cloning could be done here if needed.
        try {              
            // initialize the map
            boolean showGl = true;
            if (showGl) {
                initMap(simulation);
                // initialize the timeline
                initTimeline(simulation);
                // create controller
                controller = new Controller(this);
                simulation.addEventListener(controller);
                // add new frame for the controller and the timeline
                final JFrame fframe = new JFrame(" Stratmas Time Control");
                final JPanel tpanel = getTimeline().getTimelinePanel();
                final ControllerPanel cpanel = getController().getControllerPanel();
                cpanel.addStratmasEventListener(new StratmasEventListener () {
                        public void eventOccured(StratmasEvent e) {
                            fframe.dispose();
                        }
                    });
                createAndShowGUI(fframe, tpanel, cpanel);
            }
            
            // batch mode
            if (inBatchMode || inBatchContinuousMode) {
                initBatchMode(simulation, true);
            }
            // default mode
            else if (inDefaultMode) {
                initBatchMode(simulation, false);
            }
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    
    /**
     * Container for the timeline and the controller. 
     */
    private void createAndShowGUI(JFrame frame, JPanel panel1, JPanel panel2) {
        //
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(panel1, BorderLayout.CENTER);
        panel.add(panel2, BorderLayout.SOUTH);

        // 
        final int frame_width = 700;
        final int frame_height = 400;
                
        // create and set up the window
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        //
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        
        // frame size (test adapted for now on)
        Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(frame_width, frame_height); 
        frame.setLocation(screen_size.width-frame_width, screen_size.height-frame_height-screen_size.height/20);

        // set up the content pane
        panel.setOpaque(true); //content panes must be opaque
        frame.getContentPane().add(panel);
        
        // display the window
        frame.setSize(frame_width, frame_height);
        frame.setResizable(true);
        final int minimumWidth = 600;
        final int minimumHeight = 300;
        final JFrame fframe = frame;
        frame.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    int width = (e.getComponent().getWidth() < minimumWidth)? minimumWidth : 
                        e.getComponent().getWidth();
                    int height = (e.getComponent().getHeight() < minimumHeight)? minimumHeight : 
                        e.getComponent().getHeight();
                    fframe.setSize(width, height);
                    fframe.validate();
                } 
            });
        
        // thread safety recommendation
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    fframe.setVisible(true);
                }
            });
    }
    
    /**
     * Initializes batch mode for the provided simulation object.
     *
     * @param simulation the simulation to run in batchmode
     * @param runBatch HACK - whether to actually do batchmode, or just use it to initialize a server connection.
     */
    protected void initBatchMode(StratmasObject simulation, boolean runBatch) {
        if (getController() != null && getTimeline() != null) {
            if (runBatch) {
                Timestamp startTime = ((StratmasTimestamp) simulation.getChild("startTime")).getValue();
                getTimeline().setUpTimeline(startTime.add(batchModeDuration).getMilliSecs());
            }
            getController().connectToServer();
            if (runBatch) {
                mPVExporter = new StreamPVExporter(batchModeOutputWriter, 
                                                   getSubscriptionHandler(), 
                                                   (Shape) simulation.getChild("scenario").getChild("map"));
                 getController().getControllerPanel().doStart();
            }
        } 
        else {
            System.err.println("Unable to initialize batch mode.");
            StratmasDialog.showErrorMessageDialog((JFrame) null, 
                                                  "Unable to initialize batch mode. Exiting...", 
                                                  "Batch mode error");
            System.exit(1);
        }
    }
    
    /**
     * Initializes a timeline for the provided simulation.
     *
     * @param simulation the simulation to act as timeline for.
     */
    protected void initTimeline(StratmasObject simulation) {
        // get dt
        StratmasDuration dt_msecs = (StratmasDuration) simulation.getChild("timeStepper").getChild("dt");
        // get start time
        StratmasTimestamp stime_msecs = (StratmasTimestamp) simulation.getChild("startTime");
        // create timeline
        timeline = new Timeline(dt_msecs, stime_msecs);
        // set this client as the timeline's reference
        timeline.setClient(this);
        // add all activities to the timeline
        timeline.importActivities(simulation);
    }
    
    /**
     * Initializes a map for the current root object.
     *
     * @param simulation the simulation to act as timeline for.
     */
    protected void initMap(StratmasObject simulation) {
        // get shape from parser
        // Preliminary Version :) /dah
        Shape map = (Shape) simulation.getChild("scenario").getChild("map");
        // create new visualizer
        visualizer = new Visualizer(this, map);
    }
    
    /**
     * Returns the SubscriptionHandler of this client (or null if none
     * created yet).
     */
    public SubscriptionHandler getSubscriptionHandler() {
        return subscription_handler;
    }
    
    /**
     * Returns the XMLHandler of this client (or null if none
     * created yet).
     */
    public XMLHandler getXMLHandler() {
        return xml_handler;
    }
    
    /**
     * Returns the ServerConnection of this client (or null if none
     * created yet).
     */
    public ServerConnection getServerConnection() {
        return server_connection;
    }
    
    /**
     * Renews the subscriptions.
     */
    public void renewSubscriptions() {
        // XMLHandler
        xml_handler.reset();
        xml_handler.connect(subscription_handler);
        //
        if (mPVExporter != null) {
            mPVExporter.kill();
            Shape map = (Shape) ((StratmasObject) (this.getRootObject().children().nextElement())).getChild("scenario").getChild("map");
            mPVExporter = new StreamPVExporter(batchModeOutputWriter, getSubscriptionHandler(), map);
        }
        // set initial pv and faction
        visualizer.setInitialView();
    }
    
    /**
     * Registers a subscription to a region.
     *
     * @param rdata the actual region. 
     */
    public void subscribeRegion(RegionData rdata) {
        // subscribe
        subscription_handler.regSubscription(rdata.createSubscription(this));
    }
    
    
    /**
     * Unsubscribes a region.
     *
     * @param id the id for the region subscription we want to unsubscribe. 
     */
    public void unsubscribe(int id) {
        // unsubscribe
        subscription_handler.regSubscription(new Unsubscription(id));        
    }
    
    /**
     * Returns the name of the server to use;
     */
    public String getServerName() {
        if (this.serverName == null) {
            setServerNameGUI();
        }
        
        return this.serverName;
    }

    /**
     * Returns the StratmasDispatcher to use.
     */
    StratmasDispatcher getStratmasDispatcher() {
        if (this.stratmasDispatcher == null) {
            String dispatcherString = System.getProperty("DISPATCHER");
            if (dispatcherString != null && dispatcherString != "") {
                String[] parts = dispatcherString.split(":");
                int port = 4181;
                if (parts.length == 2 && parts[1].matches("\\A\\p{Digit}+\\z")) {
                    port = Integer.parseInt(parts[1]);
                }
                this.stratmasDispatcher = new StratmasDispatcher(parts[0], port);
            }
        }

        return this.stratmasDispatcher;
    }

    /**
     * Creates the grid from the currently stored GridData.
     */
    private void createGrid() {
          if (gridData != null) {
              Shape map = (Shape) ((StratmasObject) (this.getRootObject().children().nextElement())).getChild("scenario").getChild("map");
              visualizer.createGrid(gridData);
          }
          else {
              throw new AssertionError("No GridData in createGrid");
          }
    }
    
    /**
     * Sets the GridData object.
     */
    public void setGrid(GridData gd) {
         gridData = gd;
         if (getRootObject().getChildCount() > 0) {
             createGrid();
         }
    }

    /**
     * Shows a gui that sets the name of the server to use.
     */
    public void setServerNameGUI() {
        setServerName(StratmasDialog.showInputDialog(null, "Choose simulation server", "localhost"));
    }
    
    /**
     * Sets the name of the server to use;
     */
    public void setServerName(String str) {
        if (str != null) {
            String[] parts = str.split(":");
            if (parts.length == 2) {
                try {
                    setServerPort(Integer.parseInt(parts[1]));
                    this.serverName = parts[0];
                } catch (NumberFormatException e) {
                    // Silent...
                    this.serverName = str;
                }
            }
            else {
                this.serverName = str;
            }
        }
        else {
            this.serverName = str;
        }
    }

    /**
     * Resets the timeline.
     */
    public void resetTimeline() {
        if (getRootObject().getChildCount() > 0) {
            StratmasObject simulation = (StratmasObject)getRootObject().children().nextElement();
            timeline.reset((StratmasDuration)simulation.getChild("timeStepper").getChild("dt"),
                           (StratmasTimestamp)simulation.getChild("startTime"));
            // add all activities to the timeline
            timeline.importActivities(simulation);
        }
    }
    
    /**
     * Resets all the maps with its components.
     */
    public void resetVisualizer() {
        visualizer.reset();
    }
    
    /**
     * Returns the visualizer.
     */
    public Visualizer getVisualizer() {
        return visualizer;
    }
    
    /**
     * Notifies the controller and the client.
     */
    public void setNotify() {
        if (controller != null) {
            controller.setNotify();
        }

        synchronized(this) {
            this.notify();
        }
    }
    
    /**
     * Adds event listener.
     *
     * @param sel the listener to add.
     */
    public void addEventListener(StratmasEventListener sel) {
         eventListeners.put(sel, sel);
    }
    
    /**
     * Removes an event listener.
     */
    public void removeEventListener(StratmasEventListener sel) {
         eventListeners.remove(sel);
    }

    /**
     * Sets the client to be "active" or "passive".
     *
     * @param active if true the client is "active", otherwise it's "passive".
     */
    public void setActiveClient(boolean active) {
        isActiveClient = active;
    }
    
    /**
     * Returns true if the client is in batch mode.
     */
    public boolean inBatchMode() {
        return inBatchMode;
    }
    
    /**
     * Returns true if the client is in batch continuous mode.
     */
    public boolean inBatchContinuousMode() {
        return inBatchContinuousMode;
    }
    
    /**
     * Sets batchContinuousMode of the client.
     */
    public void setBatchContinuousMode(boolean mode) {
        inBatchContinuousMode = mode;
    }

    /**
     * Sets the client in substrateEditorMode.
     */
    public void setSubstrateEditorMode(boolean mode) {
        substrateEditorMode = mode;
    }
    
    /**
     * Returns true if the client is in substrateEditorMode.
     */
    public boolean inSubstrateEditorMode() {
        return substrateEditorMode;
    }

    /**
     * Updates the status flag and displays errors and warnings if any.
     *
     * @param errors list of errors and warnings if any.
     */
    public void updateStatus(Hashtable errors, String msg_type) {
        // collect all messages
        Vector fatal = (Vector)errors.get("fatal");
        final Vector general = (Vector)errors.get("general");
        Vector warning = (Vector)errors.get("warning");
        // fatal errors
        if (fatal != null && !fatal.isEmpty()) {
            status.put(msg_type, new String("error"));
            setNotify();
            SubscriptionCounter.endTimer();
            for (int i = 0; i < fatal.size(); i++) {
                final String message = (String)fatal.get(i);
                SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            StratmasDialog.showErrorMessageDialog(new JFrame(), message, "Fatal Error");
                        }
                    });
            }
        }
        // general errors
        else if (general != null && !general.isEmpty()) {
            status.put(msg_type, new String("error"));
            setNotify();
            SubscriptionCounter.endTimer();
            for (int i = 0; i < general.size(); i++) {
                final String message = (String)general.get(i);
                SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            StratmasDialog.showErrorMessageDialog(new JFrame(), message, "General Error");
                        }
                    });
            }
        }
        // warnings
        else if (warning != null && !warning.isEmpty()) {
            status.put(msg_type, new String("warning"));
            setNotify();
            for (int i = 0; i < warning.size(); i++) {
                final String message = (String)warning.get(i);
                SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            StratmasDialog.showWarningMessageDialog(new JFrame(), message, "Warning");
                        }
                    });
            }
        }
        // everything as it should be
        else {
            status.put(msg_type, new String(""));
            setNotify();
        } 
    }

    /**
     * Returns the status of the client.
     */
    public Hashtable getStatus() {
        return status;
    }
    
    /**
     * Updates the client with process variables.
     *
     * @param pv list of process variables. All elements are of type <code>ProcessVariableDescription</code>.
     */
    public void setProcessVariables(Vector pv) {
        process_variables = pv;
        
        if (visualizer != null) {
            // update visualizer with process variables
            for (int i = 0; i < process_variables.size(); i++) {
                visualizer.importProcessVariable((ProcessVariableDescription)process_variables.get(i));
            }
            // get factions
            TypeFilter factionFilter = new TypeFilter(TypeFactory.getType("EthnicFaction"));
            Enumeration ethnicFactions = rootObject.getFilteredChildren(factionFilter);
            // extract factions
            while(ethnicFactions.hasMoreElements()) {
                visualizer.importFaction((StratmasObject)ethnicFactions.nextElement());
            }
            // set initial pv and faction
            visualizer.setInitialView();
        }
    }
    
    /**
     * This method is used to obtain the process variables from the server. After the process variables
     * are obtained the client is disconnected from the server.
     *
     * @param serverName the name of the server.
     *
     * @return true if the process variables are obtained, false otherwise.
     */
    public boolean getProcessVariablesFromServer(String serverName) {
        setServerName(serverName);
        // connect to the server
        if (!establishServerConnection()) {
            interruptConnection();
            return false;
        } 
         // get the process variables
        if (!getServerCapabilities()) {
            interruptConnection();
            return false;
        }
        
        // disconnect from the server
        client.getServerConnection().disconnect(1000);
        client.setConnected(false);
        setServerName(null);
        status.clear();
        
        return true;
    }

    /**
     * Interrupts the server connection. 
     */
    private void interruptConnection() {
        setConnected(false);
        // kill the connection
        if (getServerConnection() != null) {
            getServerConnection().kill();
        }
        setServerName(null);
        status.clear();
    }

    /**
     * Sets the flag which shows if the client is connected to the server.
     *
     * @param connected if true the client is connected to the server, if false it's not.
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    /**
     *
     */
    public void notifyHandledSubs(Timestamp t) {
        StratmasEvent e = StratmasEvent.getSubscriptionHandled(this, t);
        for (Enumeration en = eventListeners.elements(); en.hasMoreElements(); ) {
            ((StratmasEventListener)en.nextElement()).eventOccured(e);
        }
    }
    
    /**
     * Returns true if the client is connected to the server.
     */
    public boolean isConnected() {
        return connected;
    }
    
    /**
     * Returns true if the client is active.
     */
    public boolean isActive() {
        return isActiveClient;
    }
    
    /**
     * Returns the actual timeline.
     */
    public Timeline getTimeline() {
        return timeline;
    }
    
    /**
     * Returns the list of process variables.
     */
    public Vector getProcessVariables() {
        return process_variables;
    }
    
    /**
     * Return the process variable with then given name.
     */
    public ProcessVariableDescription getProcessVariable(String name) {
        for (int i = 0; i < process_variables.size(); i++) {
            ProcessVariableDescription pv = (ProcessVariableDescription)process_variables.get(i);
            if (pv.getName().equals(name)) {
                return pv;
            }
        }
        return null;
    }
    
    /**
     * Returns the list of factions.
     */
    public Vector getFactions() {
        Vector v = new Vector();
        TypeFilter factionFilter = new TypeFilter(TypeFactory.getType("EthnicFaction"));
        Enumeration ethnicFactions = rootObject.getFilteredChildren(factionFilter);
        while(ethnicFactions.hasMoreElements()) {
            v.add(ethnicFactions.nextElement());
        }
        
        return v;
    }
    
    /**
     * Returns the root object of this Client.
     */   
    public StratmasObject getRootObject() {
        return this.rootObject;
    }

    /**
     * Returns the filename used on the command line, if any, else null
     */
    public String getFilename() {
        return this.fileName;
    }
    
    /**
     * Returns the path to a template (very empty) simulation.
     */
    public static String getTemplateFilePath() {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        StratmasClient.TaclanV2.Taclan2FileFilter filter = 
            new StratmasClient.TaclanV2.Taclan2FileFilter() {
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    }
                    String extension = getExtension(f);
                    if (extension != null) {
                        if (extension.equals("shp")) {
                            return true;
                        }
                    }
                    return false;
                }

                public String getDescription() {
                    return "ESRI Shape files";
                }
            };
        chooser.setFileFilter(filter);
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getPath();
        }
        else {
            return null;
        }
    }
    
    /**
     * Returns a template (very empty) xml simulation
     *
     * @param filePath The path to the ESRI file to use as map.
     * @return The newly created simulation.
     */
    public static StratmasObject getTemplateSimulation(String filePath) {
        String xmlCode = StratmasConstants.xmlFileHeader +
            "<identifiables xsi:type=\"sp:CommonSimulation\" identifier=\"Empty Simulation\">" + 
              "<timeStepper xsi:type=\"sp:ConstantStepper\">" +
              "<dt xsi:type=\"sp:Duration\">" +
              "<value>86400000</value>" +
              "</dt>" +
              "</timeStepper>" +
              "<gridPartitioner xsi:type=\"sp:SquarePartitioner\">" +
              "<cellSizeMeters xsi:type=\"sp:Double\">" +
              "<value>10000</value>" +
              "</cellSizeMeters>" +
              "</gridPartitioner>" +
              "<scenario xsi:type=\"sp:CommonScenario\">" +
              "<xi:include href=\"file:" + filePath + "\"/>" +
              "<disease xsi:type=\"sp:Disease\">" +
              "<description xsi:type=\"sp:String\">" +
              "<value>None</value>" +
              "</description>" +
              "<infectionRate xsi:type=\"sp:Double\">" +
              "<value>0</value>" +
              "</infectionRate>" +
              "<recoveryRate xsi:type=\"sp:Double\">" +
              "<value>0</value>" +
              "</recoveryRate>" +
              "<mortalityRate xsi:type=\"sp:Double\">" +
              "<value>0</value>" +
              "</mortalityRate>" +
              "</disease>" +
              "<HDI xsi:type=\"sp:Double\">" +
              "<value>0</value>" +
              "</HDI>" +
              "<unemployment xsi:type=\"sp:Double\">" +
              "<value>0</value>" +
              "</unemployment>" +
              "</scenario>" +
              "<startTime xsi:type=\"sp:Timestamp\">" +
              "<value>1970-01-01T00:00:00Z</value>" +
              "</startTime>" +
              "<modelParameters xsi:type=\"sp:ModelParameters\">" +
              "<fractionPotentialInsurgents xsi:type=\"sp:Double\">" +
              "<value>0.03</value>" +
              "</fractionPotentialInsurgents>" +
              "<insurgentDisaffectionThreshold xsi:type=\"sp:Double\">" +
              "<value>30</value>" +
              "</insurgentDisaffectionThreshold>" +
              "<insurgentGenerationCoefficient xsi:type=\"sp:Double\">" +
              "<value>0.01</value>" +
              "</insurgentGenerationCoefficient>" +
              "<insurgentStrengthFactor xsi:type=\"sp:Double\">" +
              "<value>0.0375</value>" +
              "</insurgentStrengthFactor>" +
              "</modelParameters>" +
              "</identifiables>" +
              StratmasConstants.xmlFileFooter;
        try {
            StratmasObject sim = (StratmasObject)importXMLString(xmlCode).getChild("identifiables").children().nextElement();
            return sim;
        }
        catch (NullPointerException e) {
            return null;
        }
    }

    /**
      * Saves the object to a XML file pointed out by
      * filename. 
      *
      * @param object the object to save.
      * @param filename the name of the file to save to
      */
    static protected void exportToXML(StratmasObject object, String filename) {
        try {
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filename));
            writer.write(StratmasConstants.xmlFileHeader);
            writer.write(object.toXML());
            writer.write(StratmasConstants.xmlFileFooter);
            writer.close();
        } catch (IOException e) {
            StratmasDialog.showErrorMessageDialog(null, "File error\n" + e.getMessage(), "File error"); 
        }
    }
    
    /**
     * Exports the provided object to a file specified by the user
     * using a JFileChooser
     *
     * @param object the object to save.
     */
    static public void exportToFile(StratmasObject object) {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        StratmasClient.TaclanV2.Taclan2FileFilter t2filter = 
            new StratmasClient.TaclanV2.Taclan2FileFilter();
        StratmasClient.TaclanV2.Taclan2FileFilter xmlfilter = 
            new StratmasClient.TaclanV2.Taclan2FileFilter() {
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    }
                    String extension = getExtension(f);
                    if (extension != null) {
                        if (extension.equals("xml")) {
                            return true;
                        }
                    }
                    return false;
                }

                public String getDescription() {
                    return "XML files";
                }
            };
        chooser.addChoosableFileFilter(xmlfilter);
        chooser.setFileFilter(t2filter);
        if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
        } 
        else {
            if (chooser.getFileFilter() == xmlfilter) {
                if (xmlfilter.accept(chooser.getSelectedFile())) {
                    exportToXML(object, chooser.getSelectedFile().getPath());
                } 
                else {
                    exportToXML(object, chooser.getSelectedFile().getPath() + ".xml");
                }
            } 
            else if (chooser.getFileFilter() == t2filter) {
                if (t2filter.accept(chooser.getSelectedFile())) {
                    TaclanV2Utils.exportToTaclanV2(object, chooser.getSelectedFile().getPath());
                } 
                else {
                    TaclanV2Utils.exportToTaclanV2(object, chooser.getSelectedFile().getPath() + ".tl2");
                }
            }
            else {
                TaclanV2Utils.exportToTaclanV2(object, chooser.getSelectedFile().getPath());
            }
        }
    }
    
    /**
     * 
     */
    static protected File getSelectedFile(JFileChooser chooser) {
        StratmasClient.TaclanV2.Taclan2FileFilter t2filter = new StratmasClient.TaclanV2.Taclan2FileFilter();
        StratmasClient.TaclanV2.Taclan2FileFilter xmlfilter = new StratmasClient.TaclanV2.Taclan2FileFilter() {
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    }
                    String extension = getExtension(f);
                    if (extension != null) {
                        if (extension.equals("xml")) {
                            return true;
                        }
                    }
                    return false;
                }

                public String getDescription() {
                    return "XML files";
                }
            };
        chooser.addChoosableFileFilter(xmlfilter);
        chooser.setFileFilter(t2filter);
        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        } 
        else {
            return null;
        }
    }
    
    /**
     * Exports the root object to a file.
     */
    protected void exportToFile() {
        if (getRootObject().children().hasMoreElements()) {
            exportToFile((StratmasObject) getRootObject().children().nextElement());
        }
    }
    
    /**
     * Exports the root object to a TaclanV2 file specified by the
     * user using a JFileChooser
     */
    protected void exportToTaclanV2() {
        if (getRootObject().children().hasMoreElements()) {
            TaclanV2Utils.exportToTaclanV2((StratmasObject) getRootObject().children().nextElement());
        }
    }
    
    /**
     * Returns the controller of this Client.
     */
    public Controller getController() {
        return this.controller;
    }

    /**
     * Called when the StratmasObject that is the root of the
     * TreeView framed is called.
     *
     * @param event the event causing the call.
     */
    public void eventOccured(StratmasEvent event) {
        if (event.getSource().equals(getRootObject())) {
            if (event.isObjectAdded() && !inSubstrateEditorMode()) {
                StratmasObject sObj = (StratmasObject) event.getArgument();
                importSimulation(sObj);
            }
        }
    }

    /**
     * Sets the port number of the server to connect to.
     *
     * @param port the portnumber
     */
    public void setServerPort(int port) {
        this.port = port;
    }
    
    /**
     * Returns the currently running instance of Client, or null if none.
     */
    public static Client getClient() {
        return Client.client;
    }    
    
    /**
     * Creates and registers a new StreamPVExporter, or fails with a dialog..
     */
    public void createStreamPVExporter() {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                createStreamPVExporter(new OutputStreamWriter(new FileOutputStream(chooser.getSelectedFile().getPath())));
            } 
            catch (IOException ex) {
                StratmasDialog.showErrorMessageDialog(null, "File error:\n" + ex.getMessage(), "IO error");
            }
        }
    }

    /**
     * Creates and registers a new StreamPVExporter using the provided
     * OutputStream, or fails with a dialog..
     *
     * @param writer where to write pv's
     */
    public void createStreamPVExporter(OutputStreamWriter writer) {
        if (getClient().getSubscriptionHandler() == null) {
            StratmasDialog.showErrorMessageDialog(null, "No SubscriptionHandler", "No SubscriptionHandler");
        } 
        else if (!getClient().getRootObject().hasChildren() ||
                 !((StratmasObject) getRootObject().children().nextElement()).getType().canSubstitute("Simulation")) {
            StratmasDialog.showErrorMessageDialog(null, "No map to export PV's for", "No map to export PV's for");
        } 
        else {
            batchModeOutputWriter = writer;
            mPVExporter = new StreamPVExporter(batchModeOutputWriter, 
                                               getSubscriptionHandler(),
                                               (Shape) ((StratmasObject) getRootObject().children().nextElement()).getChild("scenario").getChild("map"));
        }
    }
    
    /**
     * Gets a StratmasObject from the specified InputSource.
     *
     * @param source The source to read from.
     * @return The StratmasObject from the source or null if
     * something failed.
     */
    public static StratmasObject importXML(InputSource source) {
          StratmasObject root = null;
          try {
              root = XMLImporter.saxParse(source);
          } 
          catch (IOException e) {
              StratmasDialog.quitProgressBarDialog();
              StratmasDialog.showErrorMessageDialog(null, "File error\n" + e.getMessage(), "File error"); 
          } 
          catch (ExceptionCollection e) {
              StratmasDialog.quitProgressBarDialog();
              Vector v = e.getExceptions();
              for (int i = 0; i < v.size(); i++) {
                   String [] options = new String[2];
                   options[0] = (i == v.size() - 1 ? "Ok" : "View next");
                   options[1] = "Cancel";
                   if (StratmasDialog.showOptionDialog(null,
                                                       ((Exception) v.elementAt(i)).getMessage(),
                                                       "Error " + ( i + 1 ) + " of " + v.size(),
                                                       JOptionPane.YES_NO_OPTION,
                                                       JOptionPane.ERROR_MESSAGE,
                                                       null,
                                                       options,
                                                       options[0]) == 1) {
                       break;
                   }
              }
          }
          return root;
     }
    
    /**
     * Convenience method that gets a StratmasObject from the specified xmlfile.
     *
     * @param filename the name of the file.
     *
     * @return the StratmasObject from the file or null if something failed.
     */
    public static StratmasObject importXMLFile(String filename) {
        StratmasObject ret = null;
        if (filename != null) {
            try {
                InputSource source = new InputSource(new BufferedInputStream(new FileInputStream(filename)));
                try {
                    source.setSystemId(new File(filename).toURI().toURL().toString());
                } catch (MalformedURLException e) {
                    // Ok, don't set system id...
                }
                ret = importXML(source);
            } catch (FileNotFoundException e) {
                StratmasDialog.quitProgressBarDialog();
                StratmasDialog.showErrorMessageDialog(null, "File '" + filename + "' not found.", "File Not Found"); 
            }
        }
        return ret;
    }
    
    /**
     * Convenience method that gets a StratmasObject from the specified xml string.
     *
     * @param xml a string containing xml.
     *
     * @return the StratmasObject from the string or null if something failed.
     */
    public static StratmasObject importXMLString(String xml) {
        StratmasObject ret = null;
        if (xml != null) {
            ret = importXML(new InputSource(new StringReader(xml)));
        }
        return ret;
    }
    
    /**
     * Gets the root element from the specified file and tries to extract a Simulation descendant from it.
     *
     * @param filename the name of the file to import.
     *
     * @return the first Simulation descendant instance in the specified file or null if no simulation was found.
     */
    public static StratmasObject importXMLSimulation(String filename) {
        StratmasObject ret = null;
        StratmasObject root = importXMLFile(filename);
        if (root != null) {
            StratmasObject list = root.getChild("identifiables");
            if (list != null) {
                StratmasObject sim = (StratmasObject)list.children().nextElement();
                if (sim != null && sim.getType().canSubstitute("Simulation")) {
                    ret = sim;
                }
            }
        }
        return ret;
    }

     /**
      * Imports a simulation file specified by the user using a
      * JFileChooser
      *
      * @return The first Simulation descendant instance in the user
      * chosen file or null if user aborted or no simulation was
      * found.
      */
     public static StratmasObject importXMLSimulation() {
         String filename = getFileNameFromDialog(".scn", JFileChooser.OPEN_DIALOG);
         return (filename == null ? null : importXMLSimulation(filename));
     }

    /**
     * Convenience method that gets a file name with the specified
     * extension by opening a JFileChooser.
     *
     * @param extension the extension of the file to get or null if no special extension is wanted.
     * @param type the type of the dialog.
     *
     * @return the name of the chosen file or null if user aborted.
     */
    public static String getFileNameFromDialog(String extension, int type) {
        return getFileNameFromDialog(extension, type, null);
    }
    
    /**
     * Convenience method that gets a file name with the specified
     * extension by opening a JFileChooser.
     *
     * @param extension the extension of the file to get or null if no special extension is wanted.
     * @param type the type of the dialog.
     * @param component the component used to determine location of the dialog.
     *
     * @return the name of the chosen file or null if user aborted.
     */
    public static String getFileNameFromDialog(String extension, int type, Component component) {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        if (extension != null) {
            chooser.setFileFilter(new FileExtensionFilter(extension));
        }
        int choice;
        if (type == JFileChooser.OPEN_DIALOG) {
            choice = chooser.showOpenDialog(component);
        }
        else if (type == JFileChooser.SAVE_DIALOG) {
            choice = chooser.showSaveDialog(component);
        }
        else {
               throw new AssertionError("Unknown JFileChooser type.");
        }
        
        if (choice != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        else {
            // If no extension, add the extension to the filename.
            File file = chooser.getSelectedFile();
            int i = file.getName().lastIndexOf('.');
            if(i == -1) {
                // Add extension.
                file = new File(file.getParent(), file.getName() + extension);
            }
            
            return file.getPath();
        }
    }

    /**
     * Used for open file dialog when several file extensions are allowed.
     */
    public static String getFileNameFromOpenDialog(String[] extensions, String description) {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        if (extensions != null) {
            chooser.setFileFilter(new FileExtensionFilter2(extensions, description));
        }
        int choice = chooser.showOpenDialog(null);
        
        return (choice != JFileChooser.APPROVE_OPTION)? null : chooser.getSelectedFile().getPath();
    }
    
    /*
     * Main program.
     *
     * @param args an xml-file
     */
    public static void main(String[] args) {
        if (! JoglLibExtractor.initializeJoglLibs(args)) {            
            System.err.println("Failed automatic allocation of jogl libraries.\n" + 
                               "Will try to use implicitly availiable libraries instead.");
        }
        
        if (args.length > 0 && args[0].equals("-noJoglResolve")) {
            String[] newArgs = new String[args.length - 1];
            System.arraycopy(args, 1, newArgs, 0, args.length - 1);
            args = newArgs;
        } 

        Client client = new Client(args);
    }     

}

