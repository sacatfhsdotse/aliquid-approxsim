// $Id: ProcessVariableParameter.java,v 1.5 2006/03/22 14:30:50 dah Exp $
/*
 * @(#)ProcessVariableParameter.java
 */

package ApproxsimClient.evolver;

import java.util.Vector;
import java.util.Enumeration;

import ApproxsimClient.ProcessVariableDescription;
import ApproxsimClient.object.ApproxsimObject;

import ApproxsimClient.object.Shape;
import ApproxsimClient.object.type.TypeFactory;
import ApproxsimClient.ApproxsimConstants;

import ApproxsimClient.communication.ServerException;
import ApproxsimClient.communication.RegionData;
import ApproxsimClient.communication.ApproxsimMessage;
import ApproxsimClient.communication.Subscription;
import ApproxsimClient.communication.RegionSubscription;
import ApproxsimClient.communication.ApproxsimMessageEvent;
import ApproxsimClient.communication.ServerCapabilitiesMessage;
import ApproxsimClient.communication.DefaultApproxsimMessageListener;

import ApproxsimClient.filter.TypeFilter;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import java.awt.event.ActionEvent;

/**
 * A parameter subclass usable for parameters backed by process variables.
 * 
 * @version 1, $Date: 2006/03/22 14:30:50 $
 * @author Daniel Ahlin
 */

public class ProcessVariableParameter extends DoubleParameter implements
        SimulationEvaluatorTargetFactory {
    /**
     * The region to sample
     */
    Shape region;

    /**
     * The PV to sample.
     */
    String processVariable;

    /**
     * The faction to sample.
     */
    String faction;

    /**
     * Known process variable descriptions.
     */
    Vector processVariables;

    /**
     * Creates a new ProcessVariableParameter defined on the scecified region concerning the specified process variable for the specified
     * faction.
     * 
     * @param region the region to sample.
     * @param processVariable the variable to sample (may be null)
     * @param faction the faction to sample (may be null)
     */
    public ProcessVariableParameter(Shape region, String processVariable,
            String faction) {
        super(region.getReference().toString());
        setRegion(region);
        setProcessVariable(processVariable);
        setFaction(faction);
    }

    /**
     * Sets the region of this parameter
     * 
     * @param region the new region.
     */
    void setRegion(Shape region) {
        this.region = region;
    }

    /**
     * Returns the region of this parameter
     */
    Shape getRegion() {
        return this.region;
    }

    /**
     * Returns the faction of this parameter
     */
    String getFaction() {
        return this.faction;
    }

    /**
     * Returns the process variable of this parameter
     */
    String getProcessVariable() {
        return this.processVariable;
    }

    /**
     * Sets the process variable of this parameter
     * 
     * @param processVariable the new process variable.
     */
    void setProcessVariable(String processVariable) {
        this.processVariable = processVariable;
    }

    /**
     * Sets the faction of this parameter
     * 
     * @param faction the new faction.
     */
    void setFaction(String faction) {
        this.faction = faction;
    }

    /**
     * Returns an instance of a SimulatorEvaluatorTarget that targets this parameter.
     */
    public SimulationEvaluatorTarget createSimulationEvaluatorTarget() {
        return new DefaultSimulationEvaluatorTarget() {
            /**
             * Creates the subscription of this target. This subscription is required to, in some way, make sure update() is called whenever
             * the subscription is updated.
             */
            Subscription createSubscription() {
                return new RegionSubscription(new RegionData(getRegion()));
            }

            /**
             * Creates the ParameterInstance acting as evaluation in the Evaluations created by the SimulationEvaluator.
             */
            public ParameterInstance createEvaluation() {
                return getParameterInstance(((RegionSubscription) getSubscription())
                        .getRegionData().getPV(getProcessVariable(),
                                               getFaction()));
            }
        };
    }

    /**
     * Returns a vector containg ProcessVariableDescription's, or null if no process variables have been obtained.
     */
    Vector getProcessVariables() {
        return this.processVariables;
    }

    /**
     * Sets hashtable containg processVariables as keys and vectors containing factions. Or null if none set using the input form from
     * XMLHandler.
     * 
     * @param processVariables the new process variables.
     */
    void setProcessVariables(Vector processVariables) {
        this.processVariables = processVariables;
    }

    /**
     * Uses the provided ServerSession to set process variables.
     * 
     * @param session the session to ask for capabilities.
     */
    void setProcessVariables(ServerSession session) throws ServerException {
        ApproxsimMessage message = new ServerCapabilitiesMessage();

        message.addEventListener(new DefaultApproxsimMessageListener() {
            /**
             * Called when the XMLHandler has processed the data in the answer message received from the server.
             * 
             * @param e The event that occured.
             * @param reply the reply, if any, else null
             */
            public void messageHandled(ApproxsimMessageEvent e, Object reply) {
                if (reply instanceof Vector) {
                    setProcessVariables((Vector) reply);
                }
            }
        });

        // Blocking send
        session.send(message);
    }

    /**
     * Returns a menu that on actionPerformed will set the pv and faction for this parameter.
     */
    JPopupMenu getProcessVariablesMenu() {
        JPopupMenu res = null;
        if (getProcessVariables() != null) {
            // Build a vector with all factions.
            Vector factions = new TypeFilter(
                    TypeFactory.getType("EthnicFaction"))
                    .filterTree(getRegion().getRoot(), new Vector());

            res = new JPopupMenu();
            for (Enumeration e = getProcessVariables().elements(); e
                    .hasMoreElements();) {
                final ProcessVariableDescription pvd = (ProcessVariableDescription) e
                        .nextElement();
                if (pvd.hasFactions()) {
                    JMenu menu = new JMenu(pvd.getName());
                    menu.add(new AbstractAction(ApproxsimConstants.factionAll) {
                        /**
							 * 
							 */
                        private static final long serialVersionUID = 3068517775357377746L;

                        public void actionPerformed(ActionEvent event) {
                            setProcessVariable(pvd.getName());
                            setFaction(ApproxsimConstants.factionAll);
                        }
                    });
                    for (Enumeration f = factions.elements(); f
                            .hasMoreElements();) {
                        final ApproxsimObject obj = (ApproxsimObject) f
                                .nextElement();
                        menu.add(new AbstractAction(obj.getIdentifier()) {
                            /**
								 * 
								 */
                            private static final long serialVersionUID = -2659906693791684061L;

                            public void actionPerformed(ActionEvent event) {
                                setProcessVariable(pvd.getName());
                                setFaction(obj.getReference().toString());
                            }
                        });
                    }
                } else {
                    res.add(new AbstractAction(pvd.getName()) {
                        /**
							 * 
							 */
                        private static final long serialVersionUID = -9220062326430754626L;

                        public void actionPerformed(ActionEvent event) {
                            setProcessVariable(pvd.getName());
                            setFaction(ApproxsimConstants.factionAll);
                        }
                    });
                }
            }
        }

        return res;
    }

    /**
     * Shows a dialog that sets faction and pv parameter. Returns false if user cancels the dialog. Else true.
     */
    boolean showProcessVariablesOptionPane() {
        class Entry {
            String processVariable;
            String faction;

            Entry(String processVariable, String faction) {
                this.processVariable = processVariable;
                this.faction = faction;
            }

            public String toString() {
                return processVariable + " - " + faction;
            }
        }
        Vector entries = new Vector();
        Vector factions = new TypeFilter(TypeFactory.getType("EthnicFaction"))
                .filterTree(getRegion().getRoot(), new Vector());

        for (Enumeration e = getProcessVariables().elements(); e
                .hasMoreElements();) {
            ProcessVariableDescription pvd = (ProcessVariableDescription) e
                    .nextElement();
            entries.add(new Entry(pvd.getName(), ApproxsimConstants.factionAll));
            if (pvd.hasFactions()) {
                for (Enumeration f = factions.elements(); f.hasMoreElements();) {
                    ApproxsimObject obj = (ApproxsimObject) f.nextElement();
                    entries.add(new Entry(pvd.getName(), obj.getIdentifier()));
                }
            }
        }

        Entry selected = (Entry) JOptionPane
                .showInputDialog(null, "Please choose process variable and \n"
                                         + "faction to use as measure.",
                                 "Choose measure",
                                 JOptionPane.QUESTION_MESSAGE, null,
                                 entries.toArray(), entries.get(0));
        if (selected != null) {
            this.setFaction(selected.faction);
            this.setProcessVariable(selected.processVariable);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Return a string representation of this.
     */
    public String toString() {
        if (getFaction() != null && getProcessVariable() != null) {
            return super.toString() + " - " + getProcessVariable() + " - "
                    + getFaction();
        } else {
            return super.toString();
        }
    }
}
