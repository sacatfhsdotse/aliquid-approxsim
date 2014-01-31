package StratmasClient.map;

import java.util.Enumeration;
import java.util.Vector;
import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;

import StratmasClient.Client;
import StratmasClient.ProcessVariableDescription;
import StratmasClient.object.StratmasEvent;
import StratmasClient.object.StratmasEventListener;
import StratmasClient.object.StratmasObject;
import StratmasClient.StratmasConstants;
import StratmasClient.object.primitive.Reference;
import StratmasClient.communication.LayerSubscription;
import StratmasClient.communication.LayerData;
import StratmasClient.communication.Unsubscription;
import StratmasClient.communication.SubscriptionHandler;

/**
 * This panel contains two <code>JComboBox</code> lists: the process variables and the factions for the actual
 * simulation. The selected process variable and faction are displayed on the map implemented in
 * <code>MapDrawer</code>.
 *
 * @version 1.0 
 * @author Amir Filipovic
 *
 * @see <code>MapDrawer</code>
 */
public class PVPanel extends JPanel implements ActionListener, StratmasEventListener {
    /**
     * Combo box for process variables.
     */
    private JComboBox pv_combo;
    /**
     * Combo box for factions.
     */
    private JComboBox fac_combo;
    /**
     * Reference to the client.
     */
    private Client client;
    /**
     * Reference to the map wrapper.
     */
    private StratMap stratmap;
    /**
     * Valid foreground color for the chosen process variable and faction.
     */
    private Color valid_foreground;
    /**
     * Indicates if the change of process variable or faction has been confirmed.
     */
    private boolean waiting = false;
    /**
     * Identifier for the current layer subscription.
     */
    private int layer_id = -1;
    /**
     * JPanel containing combo box with the process variables.
     */
    private JPanel pv_panel;
    /**
     * JPanel containing combo box with the factions.
     */
    private JPanel fac_panel;
     
    /**
     * Creates panel with no process variables or factions.
     *
     * @param client reference to the client.
     * @param stratmap reference to the map wrapper.
     */
    public PVPanel(Client client, StratMap stratmap) {
	this(client, stratmap, null, null);
	// 
	setEnabled(false);
    }
    
    /**
     * Creates a panel with process variables and factions.
     *
     * @param client reference to the client.
     * @param stratmap reference to the map wrapper.
     * @param pv list of process variables.
     * @param factions list of factions.
     */
    public PVPanel(Client client, StratMap stratmap, Enumeration pv, Enumeration factions) {
	this.client = client;
	this.stratmap = stratmap;
	
	// panel for process variables
	pv_panel = new JPanel();
	pv_panel.setLayout(new BoxLayout(pv_panel, BoxLayout.X_AXIS));
	// create combo box for process variables
	pv_combo = new JComboBox();
	pv_combo.setFont(pv_combo.getFont().deriveFont(Font.PLAIN));
	pv_combo.addItem("None");
	// extract process variable names
	if (pv != null) {
	    for (; pv.hasMoreElements();) {
		pv_combo.addItem((ProcessVariableDescription)pv.nextElement());
	    }
	}
	pv_combo.addActionListener(this);
	pv_combo.setLightWeightPopupEnabled(false);
	pv_panel.add(pv_combo);
	pv_panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.
							      createTitledBorder("PV"),
							      BorderFactory.createEmptyBorder(2,2,2,2)));

	// panel for factions
	fac_panel = new JPanel();
	fac_panel.setLayout(new BoxLayout(fac_panel, BoxLayout.X_AXIS));
	// create combo box for factions
	fac_combo = new JComboBox();
	fac_combo.setFont(fac_combo.getFont().deriveFont(Font.PLAIN));
	fac_combo.setEnabled(false);
	fac_combo.addItem("All");
	// extract factions
	if (factions != null) {
	    for (;factions.hasMoreElements() ;) {
		fac_combo.addItem(factions.nextElement());
	    }
	}
	fac_combo.addActionListener(this);
	fac_combo.setLightWeightPopupEnabled(false);
	fac_panel.add(fac_combo);
	fac_panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.
							       createTitledBorder("Factions"),
							       BorderFactory.createEmptyBorder(2,2,2,2)));
	// 
	valid_foreground = pv_combo.getForeground();
    }
    
    /**
     * Creates the GUI and show it. 
     */
    public void createAndShowGUI() {
        // create and set up the window
	final JFrame frame = new JFrame("Proccess Variables Window");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	
        // set up the content pane
        setOpaque(true); //content panes must be opaque
        frame.setContentPane(this);
	
        // display the window.
        frame.pack();
	frame.setSize(300, 160);
	frame.setResizable(true);
	
	// thread safety recomendation
	SwingUtilities.invokeLater (
				    new Runnable() {
					public void run() {
					    frame.setVisible(true);
					}
				    });
    }
    
    /**
     * Updates actual process variable and faction.
     */
    public void actionPerformed(ActionEvent e) {
	Object source = e.getSource();
	// process variables combo box
	if (source.equals(pv_combo)) {
	    // if process variable has chosen
	    if (pv_combo.getSelectedItem() instanceof ProcessVariableDescription) {
		ProcessVariableDescription item = (ProcessVariableDescription)pv_combo.getSelectedItem();
		// show the faction panel
		fac_panel.setVisible(true);
		// select "All"
		boolean hasFactions = (item.hasFactions()) ? true : false;
		fac_combo.setEnabled(hasFactions);
		fac_combo.setSelectedIndex(0);
	    }
	    // if no selection has been made (empty field is chosen)
	    else {
		// hide the faction panel
		fac_panel.setVisible(false);
		// send unsubscribe message to the server
		unsubscribePVLayer();
		// reset the layer
		if (stratmap.getGridLayer() != null) {
		    stratmap.getGridLayer().reset();
		}
	    }
	}
	// factions combo box
	else if (source.equals(fac_combo) && pv_combo.getSelectedIndex() > 0) {
	    // indicate that the map has not been updated yet
	    pv_combo.setForeground(Color.RED);
	    fac_combo.setForeground(Color.RED);
	    waiting = true;
	    // if a faction is selected
	    if (fac_combo.getSelectedItem() instanceof StratmasObject) {
		// send new subscription
		subscribePVLayer((ProcessVariableDescription)pv_combo.getSelectedItem(),
				 ((StratmasObject)fac_combo.getSelectedItem()).getReference());
	    }
	    // if "All" is selected
	    else {
		// send new subscription
		subscribePVLayer((ProcessVariableDescription)pv_combo.getSelectedItem(), null);
	    }
	}
    }
    
    /**
     * Checks if the layer values has been drawn on the map and updates color of the chosen variables.
     * Red color indicates that the selected values has not been displayed on the map yet.
     */
    public void eventOccured(StratmasEvent e) {
	// if the panel is waiting for event and if the event is the right one
	if (waiting && e.getSource() instanceof GridLayer && e.isGridUpdated()) {
	    // get the process variable and the faction which have been displayed on the map
	    ProcessVariableDescription pvd = ((GridLayer)e.getSource()).getProcessVariable();
	    Reference fac_ref = ((GridLayer)e.getSource()).getFaction();
	    // if the displayed and the selected pv and factions are equal 
	    if (pvd.equals((ProcessVariableDescription)pv_combo.getSelectedItem()) &&
		((fac_ref == null && fac_combo.getSelectedIndex() == 0) || 
		 (fac_ref != null && fac_ref.equals(((StratmasObject)fac_combo.getSelectedItem()).
						    getReference())))) {
		// indicate that the map has been updated
		pv_combo.setForeground(valid_foreground);
		fac_combo.setForeground(valid_foreground);
		// the panel is waiting no more
		waiting = false;
	    }
	}
    }
    
    /**
     * Resets the panel to the initial state.
     */
    public void reset() {
	// process variables
	pv_combo.setSelectedIndex(0);
	pv_combo.setEnabled(true);
	// factions
	fac_combo.setSelectedIndex(0);
	fac_combo.setEnabled(false);
	fac_panel.setVisible(false);
	// 
	waiting = false;
	layer_id = -1;
    }
    
    /**
     * Subscribes the new data layer.
     *
     * @param pvd chosen process variable.
     * @param faction if null then aggregate of all factions, otherwise the chosen faction.
     */
    private void subscribePVLayer(ProcessVariableDescription pvd, Reference faction) {
	// unsubscribe first ...
	unsubscribePVLayer();
	// ... then subscribe 
	LayerData layer_data = new LayerData(pvd, faction, stratmap.getGridLayer().getCellValues());
	// add listeners
	layer_data.addListener(stratmap.getGridLayer());
	// create subscription
	LayerSubscription layer_sub = new LayerSubscription(layer_data);
	layer_id = layer_sub.id();
	// register subscription
	SubscriptionHandler shandler = client.getSubscriptionHandler();
	if (shandler != null) {
	    shandler.regSubscription(layer_sub);
	}
    }
    
    /**
     * Unsubscribes the data layer of the actual process variable and faction. 
     */
    private void unsubscribePVLayer() {
	if (layer_id != -1) {
	    // unsubscribe
	    SubscriptionHandler shandler = client.getSubscriptionHandler();
	    if (shandler != null) {
		shandler.regSubscription(new Unsubscription(layer_id));
	    }
	    // set layer id to -1 (unused)
	    layer_id = -1;
	}
    }
    
    /**
     * Select the process variable and faction initialy displayed on the map. 
     */
    public void setInitialView() {
 	if (pv_combo.getItemCount() > 1) {
	    // thread safety recomendation
	    final JComboBox pvCombo = pv_combo; 
	    SwingUtilities.invokeLater (new Runnable() {
		    public void run() {
			pvCombo.setSelectedIndex(1);
		    }
		}
					);
	}
	
    }
    
    /**
     * Adds process variable to the list.
     */
    public void addPV(ProcessVariableDescription pvd) {
	setEnabled(true);
	pv_combo.addItem(pvd);
    }
    
    /**
     * Adds process variables to the list.
     */
    public void addPVs(Vector pv) {
	setEnabled(true);
	for (int i = 0; i < pv.size(); i++) {
	    pv_combo.addItem((ProcessVariableDescription)pv.get(i));
	}
    }
    
    /**
     * Adds faction to the list.
     */
    public void addFaction(StratmasObject faction) {
	setEnabled(true);
	fac_combo.addItem(faction);	
    }
    
    /**
     * Adds factions to the list.
     */
    public void addFactions(Vector factions) {
	setEnabled(true);
	for (int i = 0; i < factions.size(); i++) {
	    fac_combo.addItem(factions.get(i));	
	}
    }
    
    /**
     * Returns actual proces variable.
     *
     *@return actual process variable as a <code> ProcessVariableDescription </code> object.
     */
    public ProcessVariableDescription getSelectedPV() {
	if (pv_combo.getSelectedItem() instanceof ProcessVariableDescription) {
	    return (ProcessVariableDescription)pv_combo.getSelectedItem();
	}
	else {
	    return null;
	}
    }

    /**
     * Returns actual faction.
     */
    public StratmasObject getSelectedFaction() {
	if (fac_combo.getSelectedItem() instanceof StratmasObject) {
	    return (StratmasObject)fac_combo.getSelectedItem();
	}
	else {
	    return null;
	}
    }
    
    /**
     * Enables/disables selecting of process variable and faction.
     */
    public void setEnabled(boolean enabled) {
	// display process variables and factions when enabled
	if (!pv_combo.isEnabled() && enabled) {
	    removeAll();
	    JPanel pvfac_panel = new JPanel();
	    pvfac_panel.setLayout(new BoxLayout(pvfac_panel, BoxLayout.X_AXIS));
	    pvfac_panel.add(pv_panel);
	    pvfac_panel.add(fac_panel);
		
	    // 
	    valid_foreground = pv_combo.getForeground();
	    // set layout
	    setLayout(new BorderLayout());
	    add(pvfac_panel, BorderLayout.CENTER);
	    validate();
	}
	// do not display process variables and factions when disabled
	else if (pv_combo.isEnabled() && !enabled) {
	    removeAll();
	    validate();
	}
	//
	pv_combo.setEnabled(enabled);
	fac_combo.setEnabled(enabled);
    }
    
    /**
     * Returns true if process variable can be selected, otherwise false.
     */
    public boolean isEnabled() {
	return pv_combo.isEnabled();
    }
    
    /**
     * Removes all process variables and factions from the panel.
     */
    public void clear() {
	pv_combo.removeAllItems();
	pv_combo.addItem("None");
	fac_combo.removeAllItems();
	fac_combo.addItem("All");
	fac_panel.setVisible(false);
	setEnabled(false);
    }
}
