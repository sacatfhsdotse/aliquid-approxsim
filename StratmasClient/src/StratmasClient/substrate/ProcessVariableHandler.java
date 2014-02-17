package StratmasClient.substrate;

import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;

import StratmasClient.Client;
import StratmasClient.ProcessVariableDescription;

/**
 * This class takes care of the process variables used with SubstrateEditor.
 */
class ProcessVariableHandler implements ActionListener {
    /**
     * Reference to the substrate editor.
     */
    private SubstrateEditor substrateEditor;
    /**
     * GUI implementation of the list of process variables.
     */
    private JComboBox pvBox = new JComboBox();
    /**
     * The panel which contains the list of process variables.
     */
    private JPanel pvPanel = new JPanel();
  
    /**
     * Returns new process variable handler. 
     */
    public static ProcessVariableHandler getHandler(SubstrateEditor substrateEditor) {
        return new ProcessVariableHandler(substrateEditor);
    }
    
    /**
     * Creates new process variable handler. 
     */
    public ProcessVariableHandler(SubstrateEditor substrateEditor) {
        this.substrateEditor = substrateEditor;
        // set the combo box
        pvBox.setFont(pvBox.getFont().deriveFont(Font.PLAIN));
        pvBox.setLightWeightPopupEnabled(false);
        pvBox.addActionListener(this);

        // set the panel
        pvPanel.add(pvBox);
        pvPanel.setBorder(BorderFactory.createTitledBorder("Process Variable"));
    }
    
    /**
     * Imports process variables from the client.
     *
     * @param client the client.
     */
    public void importProcessVariables(Client client) {
        // extract process variable names
        Vector processVariables = client.getProcessVariables();
        if (processVariables != null) {
            importProcessVariables(processVariables);
        }
    }
    
    /**
     * Imports process variables.
     *
     * @param processVariables a list of process variables.
     */
    public void importProcessVariables(Vector processVariables) {
        // extract process variable names
        for (int i = 0; i < processVariables.size(); i++) {
            importProcessVariable((ProcessVariableDescription)processVariables.get(i));
        }
        //        pvBox.addActionListener(this);
    }
    
    /**
     * Imports a process variables.
     *
     * @param processVariable a process variable.
     */
    public void importProcessVariable(ProcessVariableDescription processVariable) {
        if (!contains(processVariable)) {
            // set initial color map
            processVariable.setColorMap(ColorMap.COLOR_MAPS[0]);
            pvBox.addItem(processVariable);
        }
    }
    
    /**
     * Updates SubstrateEditor when new process variable is selected.
     */
    public void actionPerformed(ActionEvent event) {
        substrateEditor.updateProcessVariableValues(getSelectedProcessVariable());
    }
    
    /**
     * Returs the selected process variable.
     */
    public ProcessVariableDescription getSelectedProcessVariable() {
        if (pvBox.getItemCount() > 0) {
            return (ProcessVariableDescription)pvBox.getSelectedItem();
        }
        return null;
    }
    
    /**
     * Returns true if the process variable is contained in the list false otherwise.
     */
    private boolean contains(ProcessVariableDescription  processVariable) {
        for (int i = 0; i < pvBox.getItemCount(); i++) {
            ProcessVariableDescription  pv = (ProcessVariableDescription)pvBox.getItemAt(i);
            if (pv.getName().equals(processVariable.getName())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns a process variable with the given name if it's contained in the list..
     */
    protected ProcessVariableDescription get(String  pvName) {
        for (int i = 0; i < pvBox.getItemCount(); i++) {
            ProcessVariableDescription  pv = (ProcessVariableDescription)pvBox.getItemAt(i);
            if (pvName.equals(pv.getName())) {
                return pv;
            }
        }
        return null;
    }
    
    
    /**
     * Returns the panel of process variables.
     */
    public JPanel getPanel() {
        return pvPanel;
    }
}
