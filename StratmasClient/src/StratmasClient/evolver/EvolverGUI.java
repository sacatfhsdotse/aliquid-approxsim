// $Id: EvolverGUI.java,v 1.13 2006/05/29 13:06:18 dah Exp $
/*
 * @(#)EvolverGUI.java
 */

package ApproxsimClient.evolver;

import ApproxsimClient.object.ApproxsimObject;
import ApproxsimClient.Debug;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.Action;

import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Hashtable;

import java.awt.event.ActionEvent;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;

/**
 * EvolverGUI is a class presenting the progress of an evolver. It also contains visual objects used to control and initialize the evolver.
 * 
 * @version 1, $Date: 2006/05/29 13:06:18 $
 * @author Daniel Ahlin
 */

public class EvolverGUI extends JTabbedPane {
    /**
	 * 
	 */
    private static final long serialVersionUID = -3199626341709273120L;
    /**
     * A map of string->AbstractActions targeted to this EvolverGUI
     */
    Hashtable actionMap;

    /**
     * Creates a new EvolverGUI.
     * 
     * @param evolver the evolver to use.
     */
    public EvolverGUI(Evolver evolver) {
        super();
        this.actionMap = createActionMap(evolver);
        setEvolver(evolver);
        createDnDHandler();
        if (evolver == null) {
            createConfigurationTab();
        }
        if (Debug.isInDebugMode()) {
            // createDebugPane();
        }
    }

    /**
     * Creates a new EvolverGUI showing a config tab.
     */
    public EvolverGUI() {
        this((Evolver) null);
    }

    /**
     * Creates a new EvolverGUI using the provided object as the root of the tree being evolved.
     */
    public EvolverGUI(ApproxsimObject object) {
        super();
        this.actionMap = createActionMap(null);
        setEvolver(null);
        createDnDHandler();
        createConfigurationTab(object);
    }

    /**
     * Sets the evolver controlled by this gui.
     * 
     * @param evolver the evolver to control (may be null for uncontrol).
     */
    public void setEvolver(Evolver evolver) {
        // update action hash:

        if (evolver != null) {
            evolver.addEventListener(new DefaultEvolverEventListener() {
                /**
                 * Called when the evolver adds a new evaluation.
                 * 
                 * @param event the event.
                 * @param newEvaluation the new evaluation
                 */
                public void newEvaluation(EvolverEvent event,
                        Evaluation newEvaluation) {
                    log("Added new evaluation: "
                            + newEvaluation.getEvaluation());
                }

                /**
                 * Called when the evolvers running state changes.
                 * 
                 * @param event the event.
                 */
                public void runningStateChanged(EvolverEvent event) {
                    if (event.getEvolver().isFinished()) {
                        log("Evolver finished.", false);
                    } else if (event.getEvolver().isAborted()) {
                        log("Evolver aborted.", false);
                    } else if (event.getEvolver().isPaused()) {
                        log("Evolver paused.", false);
                    } else if (event.getEvolver().isAlive()) {
                        log("Evolver (re)-started.", false);
                    }
                }
            });
        }

        for (Enumeration e = actionMap.elements(); e.hasMoreElements();) {
            ((EvolverAction) e.nextElement()).setEvolver(evolver);
        }

        if (evolver != null) {
            createEvaluationsTab(evolver);
            createPlotterTab(evolver);
            // createParameterTab(evolver);
        }
    }

    /**
     * Creates a panel whith debug controls.
     */
    protected void createDebugPane() {
        JPanel debugPanel = new JPanel();
        debugPanel.setLayout(new BoxLayout(debugPanel, BoxLayout.Y_AXIS));
        addTab("Debug Controls", null, new JScrollPane(debugPanel),
               "Controls used for development");
    }

    /**
     * Creates a panel whith evolver configuration controls.
     */
    protected void createConfigurationTab() {
        final GuiEvolverFactory factory = new GuiEvolverFactory();
        addTab("Configuration", null, new JScrollPane(factory),
               "Configuration of the Evolver.");
    }

    /**
     * Creates a panel whith evolver configuration controls using the provided object as root.
     * 
     * @param root the root to use.
     */
    protected void createConfigurationTab(ApproxsimObject root) {
        final GuiEvolverFactory factory = new GuiEvolverFactory(root);
        addTab("Configuration", null, new JScrollPane(factory),
               "Configuration of the Evolver.");
    }

    /**
     * Creates a tab listing the various parameters to optimize.
     * 
     * @param evolver the evolver to view.
     */
    protected void createParameterTab(Evolver evolver) {
        if (indexOfTab("Parameters") != -1) {
            remove(indexOfTab("Parameters"));
        }
        JList parameterList = new JList(evolver.getParameters());
        addTab("Parameters", null, new JScrollPane(parameterList),
               "Parameters to optimize");
    }

    /**
     * Creates a tab listing the various parameters to optimize.
     * 
     * @param evolver the evolver to view.
     */
    protected void createEvaluationsTab(Evolver evolver) {
        if (indexOfTab("Evaluations") != -1) {
            remove(indexOfTab("Evaluations"));
        }

        final DefaultTableModel evaluationsTableModel = new DefaultTableModel();
        final DefaultTableModel evaluatorTableModel = new DefaultTableModel();

        // Set evolver independent fields:
        evaluationsTableModel.addColumn("Step");
        evaluatorTableModel.addColumn("Resource");

        // The rest needs an evolver to work:
        if (evolver != null) {
            for (Enumeration e = evolver.getParameters().elements(); e
                    .hasMoreElements();) {
                evaluationsTableModel.addColumn(e.nextElement());
            }
            evaluationsTableModel.addColumn("Measure: "
                    + evolver.getEvaluationParameter());
            for (Enumeration e = evolver.getParameters().elements(); e
                    .hasMoreElements();) {
                evaluatorTableModel.addColumn(e.nextElement());
            }
            evaluatorTableModel.addColumn("Measure: "
                    + evolver.getEvaluationParameter());

            evolver.addEventListener(new DefaultEvolverEventListener() {
                int step = 1;

                /**
                 * Called when the evolver adds a new evaluation.
                 * 
                 * @param event the event.
                 * @param newEvaluation the new evaluation
                 */
                public void newEvaluation(EvolverEvent event,
                        Evaluation newEvaluation) {
                    int lastRow = evaluationsTableModel.getRowCount();
                    evaluationsTableModel.setRowCount(lastRow + 1);

                    evaluationsTableModel
                            .setValueAt(new Integer(step++), lastRow,
                                        evaluationsTableModel
                                                .findColumn("Step"));

                    for (Enumeration e = newEvaluation
                            .getParameterInstanceSet().getParameterInstances(); e
                            .hasMoreElements();) {
                        ParameterInstance parameterInstance = (ParameterInstance) e
                                .nextElement();
                        int column = evaluationsTableModel
                                .findColumn(parameterInstance.getParameter()
                                        .toString());
                        if (column != -1) {
                            evaluationsTableModel.setValueAt(parameterInstance
                                    .toString(), lastRow, column);
                        }
                    }

                    int column = evaluationsTableModel.findColumn("Measure: "
                            + newEvaluation.getEvaluation().getParameter()
                                    .toString());
                    if (column != -1) {
                        evaluationsTableModel.setValueAt(newEvaluation
                                .getEvaluation(), lastRow, column);
                    }
                }

                /**
                 * Called when the evolver adds a new evaluator.
                 * 
                 * @param event the event.
                 * @param newEvaluation the new evaluation
                 */
                public void newEvaluator(EvolverEvent event, Evaluator evaluator) {
                    int i = evaluatorTableModel.getRowCount();
                    evaluatorTableModel.setRowCount(i + 1);
                    evaluatorTableModel
                            .setValueAt(evaluator, i, evaluatorTableModel
                                    .findColumn("Resource"));

                    evaluator.addEventListener(new EvaluatorEventListener() {
                        /**
                         * Called when evaluator is finished with the evaluation.
                         * 
                         * @param event the event.
                         */
                        public void finished(EvaluatorEvent event) {
                            removeEntry(event.getEvaluator());
                        }

                        /**
                         * Called when evaluator has a new preliminart evaluation.
                         * 
                         * @param evaluation the preliminary evaluation.
                         * @param event the event.
                         */
                        public void newPreliminaryEvaluation(
                                EvaluatorEvent event, Evaluation evaluation) {
                            updateEntry(event.getEvaluator(), evaluation);
                        }

                        /**
                         * Called when the an error has occured during the evaluation.
                         * 
                         * @param event the event.
                         * @param errorMessage a string describing the error.
                         */
                        public void error(EvaluatorEvent event,
                                String errorMessage) {
                            removeEntry(event.getEvaluator());
                        }

                        int findRow(Evaluator evaluator) {
                            for (int i = 0; i < evaluatorTableModel
                                    .getRowCount(); i++) {
                                if (evaluatorTableModel.getValueAt(i, 0) == evaluator) {
                                    return i;
                                }
                            }

                            return -1;
                        }

                        void updateEntry(Evaluator evaluator,
                                Evaluation newEvaluation) {
                            int i = findRow(evaluator);
                            if (i != -1) {
                                for (Enumeration e = newEvaluation
                                        .getParameterInstanceSet()
                                        .getParameterInstances(); e
                                        .hasMoreElements();) {
                                    ParameterInstance parameterInstance = (ParameterInstance) e
                                            .nextElement();
                                    int column = evaluatorTableModel
                                            .findColumn(parameterInstance
                                                    .getParameter().toString());
                                    if (column != -1) {
                                        evaluatorTableModel
                                                .setValueAt(parameterInstance
                                                        .toString(), i, column);
                                    }
                                }

                                int column = evaluatorTableModel
                                        .findColumn("Measure: "
                                                + newEvaluation.getEvaluation()
                                                        .getParameter()
                                                        .toString());
                                if (column != -1) {
                                    evaluatorTableModel.setValueAt(newEvaluation
                                                                           .getEvaluation(),
                                                                   i, column);
                                }
                            }
                        }

                        void removeEntry(Evaluator evaluator) {
                            int i = findRow(evaluator);
                            if (i != -1) {
                                evaluatorTableModel.removeRow(i);
                            }
                        }
                    });
                    log("New evaluator: " + evaluator.toString());
                }

                /**
                 * Called when the evolver has something noteworthy to report.
                 * 
                 * @param event the event.
                 * @param information the information
                 */
                public void information(EvolverEvent event, String information) {
                    log(information, false);
                }
            });
        }
        JTable evaluationList = new JTable(evaluationsTableModel);
        JTable evaluatorList = new JTable(evaluatorTableModel);

        JPanel evaluationsPanel = new JPanel();
        evaluationsPanel.setLayout(new BoxLayout(evaluationsPanel,
                BoxLayout.Y_AXIS));
        evaluationsPanel.add(new JLabel("Finished Evaluations"));
        evaluationsPanel.add(new JScrollPane(evaluationList));

        JPanel evaluatorsPanel = new JPanel();
        evaluatorsPanel.setLayout(new BoxLayout(evaluatorsPanel,
                BoxLayout.Y_AXIS));
        evaluatorsPanel.add(new JLabel("Running Evaluations"));
        evaluatorsPanel.add(new JScrollPane(evaluatorList));
        addTab("Evaluations", null, new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                evaluationsPanel, evaluatorsPanel), "Completed evaluations");
    }

    /**
     * Creates a tab plotting the progress of the evolver.
     * 
     * @param evolver the evolver to view.
     */
    protected void createPlotterTab(Evolver evolver) {
        if (indexOfTab("Plot") != -1) {
            remove(indexOfTab("Plot"));
        }
        addTab("Plot", null, new GLPlotterPanel(new GLPlotter(evolver)),
               "Graphical progress monitor");
    }

    /**
     * Creates the DnD interface.
     */
    protected void createDnDHandler() {
        this.setDropTarget(new DropTarget(this, new DropTargetAdapter() {
            Timer tabFocusDelayTimer = new Timer();
            int FOCUS_DELAY_MS = 200;

            public void dragEnter(DropTargetDragEvent dtde) {
                tabFocusDelayTimer.cancel();
                dtde.acceptDrag(dtde.getDropAction());
            }

            public void dragExit(DropTargetEvent dtde) {
                tabFocusDelayTimer.cancel();
            }

            public void dragOver(DropTargetDragEvent dtde) {
                tabFocusDelayTimer.cancel();
                final int index = indexAtLocation((int) dtde.getLocation()
                        .getX(), (int) dtde.getLocation().getY());
                if (index != -1) {
                    tabFocusDelayTimer = new Timer();
                    tabFocusDelayTimer.schedule(new TimerTask() {
                        public void run() {
                            setSelectedIndex(index);
                        }
                    }, FOCUS_DELAY_MS);
                }
                dtde.acceptDrag(dtde.getDropAction());
            }

            public void drop(DropTargetDropEvent dtde) {
                tabFocusDelayTimer.cancel();
                boolean dropAccepted = false;
                try {
                    if (dtde.isDataFlavorSupported(ApproxsimObject.APPROXSIM_OBJECT_FLAVOR)) {
                        dtde.acceptDrop(DnDConstants.ACTION_LINK);
                        dropAccepted = true;
                        Object obj = dtde
                                .getTransferable()
                                .getTransferData(ApproxsimObject.APPROXSIM_OBJECT_FLAVOR);
                        // Apple's dnd implementation sucks... We must call the
                        // getTransferData method for the string flavor in order
                        // to get a valid callback.
                        dtde.getTransferable()
                                .getTransferData(DataFlavor.stringFlavor);
                        //
                        if (obj instanceof ApproxsimObject) {
                            dtde.dropComplete(true);
                        } else {
                            dtde.dropComplete(false);
                        }
                    } else {
                        dtde.rejectDrop();
                    }
                } catch (Exception e) {
                    if (dropAccepted) {
                        dtde.dropComplete(false);
                        Debug.err
                                .println("Exception thrown - Drop complete false");
                    } else {
                        dtde.rejectDrop();
                        Debug.err.println("Exception thrown - Drop rejected");
                    }
                }
            }
        }));
    }

    /**
     * Logs interesting messages for eventual consumption by the user.
     * 
     * @param message the message.
     * @param temporary if the message should be removed after some time.
     */
    public void log(String message, boolean temporary) {
        // Debug.err.println(message);
        // Semiugly hack:
        if (getTopLevelAncestor() instanceof EvolverFrame) {
            ((EvolverFrame) getTopLevelAncestor()).log(message, temporary);
        }
    }

    /**
     * Logs interesting temporary messages for eventual consumption by the user.
     * 
     * @param message the message.
     */
    public void log(String message) {
        log(message, true);
    }

    /**
     * Returns the evolverfactory or null if none set.
     */
    EvolverFactory getEvolverFactory() {
        if (indexOfTab("Configuration") != -1) {
            return (EvolverFactory) ((JScrollPane) getComponentAt(indexOfTab("Configuration")))
                    .getViewport().getView();
        } else {
            return null;
        }
    }

    /**
     * Creates the actionMap of this EvolverGUI
     * 
     * @param evolver the evolver to create the controls for (may be null).
     */
    Hashtable createActionMap(Evolver evolver) {
        Hashtable res = new Hashtable();
        final Icon playHack = new ImageIcon(getClass()
                .getResource("icons/play.png"));
        final EvolverGUI self = this;

        res.put("Run", new EvolverAction(evolver, "Run", playHack) {
            /**
				 * 
				 */
            private static final long serialVersionUID = 5843522612034027333L;
            Icon pause = new ImageIcon(getClass()
                    .getResource("icons/pause.png"));
            Icon play = playHack;

            public void initValues() {
                putValue(Action.SHORT_DESCRIPTION, "Start evolver");
                setEnabled(checkEnabled());
            }

            public void actionPerformed(ActionEvent e) {
                if (getEvolver() == null && getEvolverFactory() != null) {
                    Evolver evolver = getEvolverFactory().getEvolver();
                    if (evolver == null) {
                        JOptionPane
                                .showMessageDialog(null,
                                                   "Configuration incomplete",
                                                   "Configuration incomplete",
                                                   JOptionPane.ERROR_MESSAGE);
                    } else if (JOptionPane
                            .showConfirmDialog(null,
                                               "No more configuration "
                                                       + "changes will be "
                                                       + "possible.\n"
                                                       + "Do you want to continue?",
                                               "Confirm evolver creation",
                                               JOptionPane.YES_NO_OPTION) == 0) {
                        self.setEvolver(evolver);
                        if (self.indexOfTab("Configuration") != -1) {
                            self.remove(indexOfTab("Configuration"));
                        }
                        getEvolver().start();
                        setEnabled(false);
                    }
                } else if (getEvolver().isAlive() && getEvolver().isPaused()) {
                    getEvolver().unPause();
                    setEnabled(false);
                } else if (getEvolver().isAlive()) {
                    getEvolver().pause();
                    setEnabled(false);
                } else {
                    getEvolver().start();
                    setEnabled(false);
                }
            }

            public boolean checkEnabled() {
                return !(getEvolver() != null && (getEvolver().isFinished() || getEvolver()
                        .isAborted()));
            }

            void evolverChanged() {
                if (checkEnabled() && getEvolver().isAlive()
                        && !getEvolver().isPaused()) {
                    putValue(Action.NAME, ""); // "Pause"
                    putValue(Action.SHORT_DESCRIPTION, "Pause evolver");
                    putValue(Action.SMALL_ICON, pause);
                } else {
                    if (checkEnabled() && getEvolver().isPaused()) {
                        putValue(Action.SHORT_DESCRIPTION, "Resume evolver");
                    } else {
                        putValue(Action.SHORT_DESCRIPTION, "Start evolver");
                    }
                    putValue(Action.NAME, ""); // "Run"
                    putValue(Action.SMALL_ICON, play);
                }

                // Hack to make sure things gets updated.
                setEnabled(checkEnabled());
            }
        });

        res.put("Abort", new EvolverAction(evolver, "Abort", new ImageIcon(
                getClass().getResource("icons/abort.png"))) {
            /**
				 * 
				 */
            private static final long serialVersionUID = 1926286491260057144L;

            public void initValues() {
                putValue(Action.SHORT_DESCRIPTION,
                         "Forcibly aborts evolver (no resume possible)");
                setEnabled(checkEnabled());
            }

            public void actionPerformed(ActionEvent e) {
                if (JOptionPane
                        .showConfirmDialog(null,
                                           "Are you sure you want to abort "
                                                   + "the evolver (no resume will be "
                                                   + "possible)?",
                                           "Confirm evolver abort",
                                           JOptionPane.YES_NO_OPTION) == 0) {
                    getEvolver().abort();
                    setEnabled(false);
                }
            }

            public boolean checkEnabled() {
                return getEvolver() != null && !getEvolver().isFinished()
                        && getEvolver().isAlive() && !getEvolver().isAborted();
            }
        });

        return res;
    }

    /**
     * Returns the AbstractAction for the string, or null if no such action.
     * 
     * @param action the name of the action.
     */
    public AbstractAction getAction(String action) {
        return (AbstractAction) actionMap.get(action);
    }
}

abstract class EvolverAction extends AbstractAction implements
        EvolverEventListener {
    /**
	 * 
	 */
    private static final long serialVersionUID = -7302824644199823764L;
    /**
     * The evolver this action controls
     */
    Evolver evolver;

    /**
     * Creates a new EvolverAction targeted to the specified evolver.
     * 
     * @param evolver the evolver this action controls (may be null in which case the action is disabled.)
     * @param name the name of the action
     * @param icon the icon of the action
     */
    public EvolverAction(Evolver evolver, String name, Icon icon) {
        super(name, icon);
        putValue(Action.SMALL_ICON, icon);
        initValues();
        setEvolver(evolver);
    }

    /**
     * Creates a new EvolverAction targeted to the provided evolver.
     * 
     * @param evolver the evolver this action controls (may be null in which case the action is disabled.)
     * @param name the name of the action
     */
    public EvolverAction(Evolver evolver, String string) {
        super(string);
        setEvolver(evolver);
    }

    /**
     * Used to specify which evolver that is controled by this action.
     */
    void setEvolver(Evolver newEvolver) {
        if (newEvolver != this.evolver) {
            if (this.evolver != null) {
                this.evolver.removeEventListener(this);
            }
            this.evolver = newEvolver;
            if (this.evolver != null) {
                this.evolver.addEventListener(this);
            }

            evolverChanged();
        }
    }

    /**
     * Returns the evolver this action uses.
     */
    Evolver getEvolver() {
        return this.evolver;
    }

    /**
     * Called when the evolver controlled by this action has changed
     */
    void evolverChanged() {
        setEnabled(checkEnabled());
    }

    /**
     * Extra enabledness.
     */
    boolean checkEnabled() {
        return isEnabled();
    }

    /**
     * Called when the evolver starts, stops, continues, pauses or aborts the evolving.
     * 
     * @param event the event.
     */
    public void runningStateChanged(EvolverEvent event) {
        evolverChanged();
    }

    /**
     * Called when the evolver adds a new evaluation.
     * 
     * @param event the event.
     * @param newEvaluation the new evaluation
     */
    public void newEvaluation(EvolverEvent event, Evaluation newEvaluation) {
        evolverChanged();
    }

    /**
     * Called when the evolver starts a new Evaluator.
     * 
     * @param event the event.
     * @param newEvaluation the new evaluation
     */
    public void newEvaluator(EvolverEvent event, Evaluator newEvaluator) {
        evolverChanged();
    }

    /**
     * Provides a way for subclasses to initialize values from the constructor.
     */
    void initValues() {}

    /**
     * Called when the evolver has something noteworthy to report.
     * 
     * @param event the event.
     * @param information the information
     */
    public void information(EvolverEvent event, String information) {}
}
