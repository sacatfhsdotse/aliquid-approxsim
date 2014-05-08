package ApproxsimClient.substrate;

import java.util.Vector;
import java.util.Enumeration;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.BorderFactory;

import ApproxsimClient.object.ApproxsimObject;
import ApproxsimClient.object.ApproxsimObjectFactory;
import ApproxsimClient.object.FactoryListener;
import ApproxsimClient.object.ApproxsimEvent;
import ApproxsimClient.object.ApproxsimEventListener;

/**
 * This class takes care of the factions used with SubstrateEditor. The list of factions is displayed by using JComboBox component and is
 * updated when a faction is created, modified or removed.
 */
class FactionHandler implements FactoryListener, ApproxsimEventListener,
        ActionListener {
    /**
     * Reference to the substrate editor.
     */
    private SubstrateEditor substrateEditor;
    /**
     * GUI implementation of the list of factions.
     */
    private JComboBox factionBox = new JComboBox();
    /**
     * The panel which contains the list of factions.
     */
    private JPanel factionPanel = new JPanel();

    /**
     * Returns new faction handler.
     */
    public static FactionHandler getHandler(SubstrateEditor substrateEditor) {
        return new FactionHandler(substrateEditor);
    }

    /**
     * Creates new handler of factions.
     */
    public FactionHandler(SubstrateEditor substrateEditor) {
        this.substrateEditor = substrateEditor;
        ApproxsimObjectFactory.addEventListener(this);
        factionBox.setFont(factionBox.getFont().deriveFont(Font.PLAIN));
        factionBox.setLightWeightPopupEnabled(false);
        factionBox.addItem("");
        factionBox.addItem("Each faction");
        // check for factions in the simulation
        ApproxsimObject simulation = (ApproxsimObject) substrateEditor
                .getClient().getRootObject().children().nextElement();
        ApproxsimObject factions = (ApproxsimObject) simulation
                .getChild("scenario").getChild("factions");
        for (Enumeration e = factions.children(); e.hasMoreElements();) {
            ApproxsimObject sObj = (ApproxsimObject) e.nextElement();
            if (sObj.getType().canSubstitute("EthnicFaction")) {
                sObj.addEventListener(this);
                factionBox.addItem(sObj);
            }
        }
        factionBox.setEnabled(false);
        factionBox.addActionListener(this);
        // initialize the panel
        factionPanel.add(factionBox);
        factionPanel.setBorder(BorderFactory.createTitledBorder("Faction"));
    }

    /**
     * Part of FactoryListener interface.
     */
    public void approxsimObjectCreated(ApproxsimObject object) {
        if (object.getType().canSubstitute("EthnicFaction")) {
            ApproxsimObject simulation = (ApproxsimObject) substrateEditor
                    .getClient().getRootObject().children().nextElement();
            ApproxsimObject factions = (ApproxsimObject) simulation
                    .getChild("scenario").getChild("factions");
            factions.add(object);
        }
    }

    /**
     * Adds newly attached faction to the list of factions. Part of FactoryListener interface.
     */
    public void approxsimObjectAttached(ApproxsimObject object) {
        if (object.getType().canSubstitute("EthnicFaction")) {
            if (substrateEditor.getProcessVariable() != null
                    && substrateEditor.getProcessVariable().hasFactions()
                    && !factionBox.isEnabled()) {
                enableSelection(true);
                substrateEditor.setFaction(getSelectedFaction());
                substrateEditor.enableEditComponents(true);
            }
            object.addEventListener(this);
            factionBox.addItem(object);
        }
    }

    /**
     * Responds to the events in the factions contained in the list. Part of ApproxsimEventListener interface.
     */
    public void eventOccured(ApproxsimEvent event) {
        // remove faction from the list
        if (event.isRemoved()) {
            substrateEditor.removeFactionValues((ApproxsimObject) event
                    .getSource());
            factionBox.removeItem((ApproxsimObject) event.getSource());
            if (substrateEditor.getProcessVariable() != null
                    && substrateEditor.getProcessVariable().hasFactions()
                    && getFactions().isEmpty()) {
                enableSelection(false);
                substrateEditor.setFaction(null);
                substrateEditor.enableEditComponents(false);
            }
        }
        // update the list of factions
        else if (event.isIdentifierChanged()) {
            factionBox.validate();
            factionBox.repaint();
        }
    }

    /**
     * Updates SubstrateEditor when new faction is selected.
     */
    public void actionPerformed(ActionEvent event) {
        if (factionBox.isEnabled()) {
            substrateEditor.updateProcessVariableValues(getSelectedFaction());
        }
    }

    /**
     * Returns the selected faction.
     * 
     * @return a ApproxsimObject if a faction is selected otherwise a String.
     */
    public Object getSelectedFaction() {
        return factionBox.getSelectedItem();
    }

    /**
     * Returns all the factions.
     * 
     * @return a list of ApproxsimObject objects.
     */
    public Vector getFactions() {
        Vector factions = new Vector();
        for (int i = 0; i < factionBox.getItemCount(); i++) {
            Object obj = factionBox.getItemAt(i);
            if (obj instanceof ApproxsimObject) {
                factions.add(obj);
            }
        }
        return factions;
    }

    /**
     * Returns a faction with the given identifier. If no such faction exists in the list null is returned.
     */
    public ApproxsimObject getFaction(String identifier) {
        Vector factions = getFactions();
        for (int i = 0; i < factions.size(); i++) {
            ApproxsimObject faction = (ApproxsimObject) factions.get(i);
            if (faction.getIdentifier().equals(identifier)) {
                return faction;
            }
        }
        return null;
    }

    /**
     * Returns true if a faction with the given identifier contains in the list of factions.
     */
    public boolean containsFaction(String identifier) {
        return getFaction(identifier) != null;
    }

    /**
     * Returns true if the list contains any factions, false otherwise.
     */
    public boolean hasFactions() {
        return !getFactions().isEmpty();
    }

    /**
     * Returns the panel of factions.
     */
    public JPanel getPanel() {
        return factionPanel;
    }

    /**
     * Enables/disables selection of the factions.
     */
    public void enableSelection(boolean enable) {
        // enable selection
        if (enable) {
            String firstItem = (String) factionBox.getItemAt(0);
            if (firstItem.length() == 0) {
                factionBox.removeItemAt(0);
            }
            factionBox.setEnabled(true);
        }
        // disable selection
        else {
            factionBox.setEnabled(false);
            String firstItem = (String) factionBox.getItemAt(0);
            if (firstItem.length() != 0) {
                factionBox.insertItemAt(new String(""), 0);
            }
            factionBox.setSelectedIndex(0);
        }
    }

}
