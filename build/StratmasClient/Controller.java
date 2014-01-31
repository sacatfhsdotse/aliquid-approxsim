package StratmasClient;

import java.util.Vector;
import java.util.Enumeration;

import StratmasClient.map.Visualizer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import StratmasClient.timeline.Timeline;
import StratmasClient.treeview.TreeView;
import StratmasClient.filter.TypeFilter;

import StratmasClient.communication.GetGridMessage;
import StratmasClient.communication.InitializationMessage;
import StratmasClient.communication.RegisterForUpdatesMessage;
import StratmasClient.communication.ResetMessage;
import StratmasClient.communication.StratmasMessage;
import StratmasClient.communication.ServerException;
import StratmasClient.communication.StepMessage;
import StratmasClient.communication.StratmasObjectSubscription;
import StratmasClient.communication.StratmasSocket;

import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasEventListener;
import StratmasClient.object.StratmasEvent;
import StratmasClient.object.type.TypeFactory;
import StratmasClient.object.StratmasReference;
import StratmasClient.object.StratmasList;


/**
 * This class is used to controll the simulation flow.
 */
public class Controller implements Runnable, StratmasEventListener {
    /**
     * Reference to the client.
     */
    private Client client;
    /**
     * Reference to the controlling panel.
     */
    private ControllerPanel control_panel;
    /**
     * Main activity thread.
     */
    private Thread activity;
    /**
     * Indicates the simulation mode of the client.
     */
    private String simulation_mode = "inactive";
    /**
     * Queue of commands for controlling the simulation.
     */ 
    private Vector commandQueue = new Vector();
    /**
     * Handles updates from client to server during simulations. 
     */
    private ServerUpdater mServerUpdater = null;
    /**
     * The filter used to check for the unresolved references.
     */
    TypeFilter unresolvedRefFilter = new TypeFilter(TypeFactory.getType("Reference"), true) {
	    /**
	     * Returns true if the provided StratmasObject passes the type
	     * constraint and can not be resolved to a valid target
	     *
	     * @param sObj the object to test
	     */
	    public boolean pass(StratmasObject sObj) {		    
		if (super.pass(sObj) && ! (sObj instanceof StratmasList)) {
		    return ((StratmasReference) sObj).getValue().resolve(sObj.getParent()) == null;
		} else {
		    return false;
		}
	    }
	};
    
    /**
     * Creates the controller.
     *
     * @param client reference to the client.
     */
    public Controller(Client client) {
	this.client = client;
	control_panel = new ControllerPanel(client, this);
    }
    
    /**
      * Creates the ControlPanel. This is an ugly solution but
      * passive clients must speak to the server before the
      * Client.init() method is called and thus, the controller mustn't
      * be created there but earlier. However, we don't want the
      * control panel to be visible from that time so... here we are...
      */
     public void createControlPanel() {
	  control_panel = new ControllerPanel(client, this);
     }
    
    /**
     * Connects the client to the server.
     *
     * @return true if the connection succesfull, otherwise false.
     */
    public boolean connectToServer() {
	String status_message;
	// check for the unresolved references
	Enumeration unResolved = unresolvedRefFilter.filterTree(client.getRootObject());
	if (unResolved.hasMoreElements()) {
	    StringBuffer buf = new StringBuffer("The following references could not be resolved:\n");
	    while(unResolved.hasMoreElements()) {
		StratmasObject sObj = (StratmasObject) unResolved.nextElement();
		buf.append(sObj.getReference().toString() + "\n");
	    }
	    buf.append("please correct these and try to connect again.");
	    
	    JOptionPane.showMessageDialog((JFrame) null, buf.toString(),
					  "Unresolved references", 
					  JOptionPane.ERROR_MESSAGE);	    
	    return false;
	}
	
	// get name of the server to connect to
	StratmasSocket socket = null;
	if (client.getStratmasDispatcher() != null) {
	    socket = client.getStratmasDispatcher().allocateServer(10);
	    if (socket == null) {
		JOptionPane.showMessageDialog((JFrame) null, 
					      "Unable to allocate server from server pool.\n" +
					      "All servers may be down or busy. Will try\n" +
					      "manual allocation instead.",
					      "Automatic allocation failed", 
					      JOptionPane.ERROR_MESSAGE); 
	    }
	    else {
		// get the port and the host
		client.setServerName(socket.getHost());
		client.setServerPort(socket.getPort());
		// establish connection
		if (!client.establishServerConnection(socket)) {
		    interruptConnection();
		    return false;
		}
	    }
	}    
	// connect without the dispatcher
	if (socket == null) {
	    if (client.getServerName() == null) {
		return false;
	    }
	    // establish connection
	    if (!client.establishServerConnection()) {
		interruptConnection();
		return false;
	    } 
	}
	// Quick fix dependent on knowledge that being here
	// implies that dispatcher succeded from which follows
	// that we have become an active client.
	client.setActiveClient(true);
		
	// initialize the server
	StratmasMessage sm = new InitializationMessage((StratmasObject)client.getRootObject().children().nextElement(), client);
	try {
	    client.getServerConnection().blockingSend(sm);
	}
	catch (ServerException e) {
	    // Error handling below as of yet.
	}
	status_message = (String)client.getStatus().remove(sm.getTypeAsString());
	if (status_message == null) {
	    status_message = (String)client.getStatus().remove("Unknown");
	}
	if (status_message == null || status_message.equals("error")) {
	    interruptConnection();
	    return false;
	}
	
	client.getServerConnection().send(new GetGridMessage());
	client.getServerConnection().send(new RegisterForUpdatesMessage(true));
	
	boolean success = client.getServerCapabilities();
	if (!success) {
	    client.getServerConnection().kill();
	}
	
	// the client is now connected to the server
	client.setConnected(success);
	
 	mServerUpdater = new ServerUpdater(client.getServerConnection(), client.getRootObject(), this);
	
	return success;
    }

    /**
     * Interrupts the connection. 
     */
    private void interruptConnection() {
	client.setConnected(false);
	// kill the connection
	if (client.getServerConnection() != null) {
	    client.getServerConnection().kill();
	}
	client.setServerName(null);
    }
    

    /**
     * Returns the controlpanel of this controller.
     */
    public ControllerPanel getControllerPanel()
    {
	return this.control_panel;
    }
       
    /**
     * Disconnects the client from the server.
     * OBS. No check is made to make sure that the disconnection has succeded.
     *
     * @return false if disconnected, true otherwise.
     */
    public synchronized boolean disconnectFromServer() {
	// reset the controller panel
	commandQueue.removeAllElements();
	if (control_panel != null) {
	     control_panel.reset();
	}
	// disconnect
	if (client.getServerConnection() != null) {
	    client.getServerConnection().disconnect();
	}

	if (mServerUpdater != null) {
	     mServerUpdater.inactivate();
	}
	mServerUpdater = null;

	// Must also check for closed server connection (i.e. the Unknown state)
	while(!client.getStatus().containsKey("DisconnectMessage") &&
	      !client.getStatus().containsKey("Unknown")) {
	    try {
		wait(5000);
	    }
	    catch (InterruptedException e) {
		e.printStackTrace();
	    }
	}
	
	if (client.getStatus().containsKey("DisconnectMessage")) {
	    client.getStatus().remove("DisconnectMessage");
	}
	if (client.getStatus().containsKey("Unknown")) {
	    client.getStatus().remove("Unknown");
	}
	// reset the timeline and all the maps
	client.resetTimeline();
	client.resetVisualizer();
	// dispose the list of process variables & factions for all the maps
	client.getVisualizer().saveOpenedTablesParameters();
	client.getVisualizer().saveOpenedGraphsParameters();
	client.getVisualizer().removeProcessVariablesAndFactions();
	client.getVisualizer().removeAllGraphs();
	client.getVisualizer().removeAllTables();
	// 
	simulation_mode = "inactive";
	// the client is now disconnected
	client.setActiveClient(true);
	client.setConnected(false);
	client.setServerName(null);
	return false;
    }

    
    /**
     * Run the client.
     */
    public synchronized void run() {
	while (true) {
	    // get the command
	    if (!commandQueue.isEmpty()) {
		setSimulationMode((String)commandQueue.remove(0));
	    }
	    // send step message
	    if (isSimulationMode("continuous")) {
		doContinuousStep();
	    }
	    // send step message and pause 
	    else if (isSimulationMode("onestep")) {
		doOneStep();
	    }
	    // stop the simulation
	    else if (isSimulationMode("stop")) {
		Thread thread = new Thread() {
			public void run() {
			    stopSimulation();
			}
		    };
		thread.start();
		break;
	    }
	    // disconnect the client
	    else if (isSimulationMode("disconnect")) {
		disconnectFromServer();
		break;
	    }
	    // wait for the command
	    else {
		try {
		    wait();
		}
		catch (InterruptedException e) {
		    Debug.err.println(e.toString());
		}
	    }
	}
	// interrupts the thread
	stop();
    }

    /**
     * Sends time step messages continuously.
     */
    private synchronized boolean doContinuousStep() {
	// wait while the server connection queue is emptying
	while (!client.getServerConnection().thresholdReached() && isSimulationMode("continuous")) {
	    boolean valid = waitToSendTimeStep();
	    if (!valid) {
		return false;
	    }
	}

	// check the simulation mode.
	if (!isSimulationMode("continuous")) {
	    return false;
	}

	// get the next time step
	int timestep;
	while((timestep = client.getTimeline().getNextTimeStep()) == -1 && isSimulationMode("continuous")) {
	    if (client.inBatchContinuousMode()) {
		setSimulationMode("stop");
		return false;
	    }
	    boolean valid = waitToSendTimeStep();
	    if (!valid) {
		return false;
	    }
	
	}
	// check the simulation mode.
	if (!isSimulationMode("continuous")) {
	    return false;
	}
	// send the message
	client.getServerConnection().send(new StepMessage(timestep, false));
	
	return true;
    }

    
    /**
     * Sends one time step and waits for the next command.
     */
    private void doOneStep() {
	int timestep;
	if ((timestep = client.getTimeline().getNextTimeStep()) != -1) {
	    // send the message
	    client.getServerConnection().send(new StepMessage(timestep, false));
	}
	updateSimulationMode("pause");
    }
    
    /**
     * Stops the simulation, resets the client and the server and initializes the server once again.
     *
     * @return true if the resetting and initializing of the server succeded, false otherwise.
     */
    private boolean stopSimulation() {
	// reset the server
	try {
	    client.getServerConnection().blockingSend(new ResetMessage());
	} 
	catch (ServerException e) {
	    Debug.err.println(e);
	    return false;
	}
	// reset the timeline
	client.resetTimeline();
	// reset the maps
	client.resetVisualizer();
	// renew the subscriptions
	client.renewSubscriptions();
	
	if (client.inBatchContinuousMode()) {
	    updateSimulationMode("continuous");
	    start();
	}
	
	return true;
    }
    
    /**
     * Starts a new thread.
     */
    public void start() {
	if (activity == null) {
	    activity = new Thread(this);
	    activity.start();
	}
    }
    
    /*
     * Interrupts the thread.
     */
    public void stop() {
	if (activity != null) {
	    activity.interrupt();
	    activity = null;
	}
    }
    
    /**
     * Notifies the controller.
     */
    public void setNotify() {
	synchronized(this) {
	    this.notify();
	}
    }
    
    /**
     * Changes the mode of the simulation. Used to control the simulation from the control panel.
     *
     * @param mode "continuos" - the simulation is running continuously,
     *             "onestep"   - the simulation is running one time step and then it's paused,
     *             "pause"     - the simulation is pused,
     *             "stop"      - the simulation is stoped,
     *             "disconnect"- the client is disconnected from the server.
     *             "inactive"  - the client is in undetermined mode (usualy it waits for the mode to change).
     */
    public synchronized void updateSimulationMode(String mode) {
	commandQueue.add(mode);
	// notify the client
	setNotify();
    }
    
    /**
     * Sets the simulation mode.
     */
    public void setSimulationMode(String mode) {
	simulation_mode = mode;
    }
    
    /**
     * Returns the simulation mode.
     */
    private String getSimulationMode() {
	return simulation_mode;
    }
    
    /**
     * Returns true if the actual simulation mode is equal to the input mode.
     */
    private boolean isSimulationMode(String mode) {
	return simulation_mode.equals(mode);
    } 

    /**
     * Terminates the program.      
     */
    public void setExitSimulation() {
	// if the simulation is off
	if (client.isConnected() && isSimulationOn() && client.isActive()) {
	    updateSimulationMode("disconnect");
	}
	// if the simulation is on
	else if (client.isConnected()) {
	    disconnectFromServer();
	}
	// exit the program
	System.exit(0);
    }
    
    /**
     * Returns true if the simulation is on.
     */
    public boolean isSimulationOn() {
	return (activity != null);
    }
    
    /**
     * Responds to the different stratmas events.
     */
    public void eventOccured(StratmasEvent e) {
	if (e.isRemoved()) {
	    if (client.getServerConnection() != null && 
		client.getServerConnection().isAlive()) {
		disconnectFromServer();
	    }
	    control_panel.close();
	    control_panel = null;
	    ((StratmasObject)e.getSource()).removeEventListener(this);
	} 
	else if (e.isReplaced()) {			
	    // UNTESTED - the replace code is untested 2005-09-22
	    Debug.err.println("FIXME - Replace behavior untested in Controller");
	    ((StratmasObject)e.getSource()).removeEventListener(this);
	    ((StratmasObject)e.getArgument()).addEventListener(this);
	}
	else if (e.isGridUpdated()) {
	    Timeline tline = client.getTimeline();
	    if (tline.getSimStartTime() == tline.getCurrentTime()) {
		control_panel.enableControlButtons(client.isActive());
	    }
	}
    }
    
    /**
     * Help method. Used to wait for an event before sending a time step.
     */
    private synchronized boolean waitToSendTimeStep() {
	// check if the connection to the server is still ok
	if (!client.getServerConnection().isAlive()) {
	    // clear all incomming messages
	    client.getXMLHandler().reset();
	    // let the XMLHandler finish
	    try {
		Thread.currentThread().sleep(2000);
	    } 
	    catch (java.lang.InterruptedException e) {
	    }
	    // register that we're disconnected
	    updateSimulationMode("disconnect");
	    return false;
	}
	
	try {
	    wait();
	    if (!commandQueue.isEmpty()) {
		setSimulationMode((String)commandQueue.remove(0));
	    }
	}
	catch (InterruptedException e) {
	    Debug.err.println(e.toString());
	    //return false;
	}
	return true;
    }
    
}
