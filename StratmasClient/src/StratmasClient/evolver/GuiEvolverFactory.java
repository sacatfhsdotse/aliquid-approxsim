//         $Id: GuiEvolverFactory.java,v 1.9 2006/04/10 09:45:55 dah Exp $
/*
 * @(#)GuiEvolverFactory.java
 */

package StratmasClient.evolver;


import StratmasClient.object.StratmasObject;
import StratmasClient.object.Shape;
import StratmasClient.object.StratmasObjectFactory;

import StratmasClient.dispatcher.StratmasDispatcher;

import StratmasClient.communication.ServerException;
import StratmasClient.treeview.TreeView;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.BoxLayout;
import javax.swing.SwingUtilities;
import java.util.Vector;
import java.util.Enumeration;
import java.io.IOException;

import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;

/**
 * Provides Evolver instances by collecting bits and pieces from Gui
 * components.
 *
 * @version 1, $Date: 2006/04/10 09:45:55 $
 * @author  Daniel Ahlin
*/
public class GuiEvolverFactory extends JPanel implements EvolverFactory 
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 7658709396373524971L;

	/**
     * The targetParameter of this factory
     */
    Parameter targetParameter;

    /**
     * The evolve parameters of this factory
     */
    Vector evolveParameters = new Vector();

    /**
     * The sampler factory used to create the sampler
     */
    SamplerFactory samplerFactory;

    /**
     * The sampler stopper used to create the stoppers for the evolver.
     */
    StopperFactory stopperFactory;

    /**
     * The stopper factory  used to create stoppers for evaluators.
     */
    StopperFactory evaluatorStopperFactory;

    /**
     * If this simulation has a specific root this is it.
     */
    StratmasObject root;

    /**
     * Creates a new EvolverFactory letting the user configure the
     * various components of an Evolver.
     */
    public GuiEvolverFactory()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        setSamplerFactory(new GuiSamplerFactory());
        setStopperFactory(new GuiIterationsStopperFactory("Evolver")
            {
                /**
				 * 
				 */
				private static final long serialVersionUID = -6831043980046147256L;

				public Stopper getStopper() {
                    return new IterationsStopper(getIterations())
                        {
                            /**
                             * Returns the number of iterations for the object o
                             *
                             * @param o the object to get iterations from.
                             */
                            public int getIterations(Object o) {
                                return ((Evolver) o).getEvaluations().size();
                            }
                        };
                }
            });
        JPanel panel = new JPanel();
        panel.add((JComponent) getSamplerFactory());
        panel.add((JComponent) getStopperFactory());
                        
        // Add drop point for parameters
        add(new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                           createTargetChoicePanel(),
                           createParameterChoicePanel()));        
    }

    /**
     * Creates a new using the provided object as root
     *
     * @param root the root to use
     */
    public GuiEvolverFactory(StratmasObject root)
    {
        //setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setRoot(root);

        //setLayout(new java.awt.GridBagLayout());
        setLayout(new BorderLayout());

        setSamplerFactory(new GuiSamplerFactory());
        setStopperFactory(new GuiIterationsStopperFactory("Evolver")
            {
                /**
				 * 
				 */
				private static final long serialVersionUID = 8385856821573978481L;

				public Stopper getStopper() {
                    return new IterationsStopper(getIterations())
                        {
                            /**
                             * Returns the number of iterations for the object o
                             *
                             * @param o the object to get iterations from.
                             */
                            public int getIterations(Object o) {
                                return ((Evolver) o).getEvaluations().size();
                            }
                        };
                }
            });
        JPanel panel = new JPanel();
        panel.add((JComponent) getSamplerFactory());
        panel.add((JComponent) getStopperFactory());
        add(panel, BorderLayout.NORTH);
                        
        // Add drop point for parameters
        TreeView treeView = TreeView.getDefaultTreeView(root);
        add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                           new JScrollPane(treeView),
                           new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                          createTargetChoicePanel(),
                                          createParameterChoicePanel())),
            BorderLayout.CENTER);
    }

    /**
     * Sets the sampler factory used to create the sampler
     */
    void setSamplerFactory(SamplerFactory factory)
    {
        this.samplerFactory = factory;
    }
    
    /**
     * Returns the sampler factory used to create the sampler
     */
    public SamplerFactory getSamplerFactory()
    {
        return this.samplerFactory;
    }

    /**
     * Sets the Stopper factory used to create the stopper for
     * evaluators.
     */
    void setEvaluatorStopperFactory(StopperFactory stopperFactory)
    {
        this.evaluatorStopperFactory = stopperFactory;
    }
    
    /**
     * Returns the the Stopper factory used to create the stopper for
     * evaluators.
     */
    public StopperFactory getEvaluatorStopperFactory()
    {
        return this.evaluatorStopperFactory;
    }

    /**
     * Sets the root object to be evolved
     */
    void setRoot(StratmasObject root)
    {
        this.root = root;
    }

    /**
     * Returns the root object to be evolved
     */
    StratmasObject getRoot()
    {
        return this.root;
    }
    
    /**
     * Sets the stopper factory used to create the stopper
     */
    void setStopperFactory(StopperFactory factory)
    {
        this.stopperFactory = factory;
    }
    
    /**
     * Returns the stopper factory used to create the stopper
     */
    public StopperFactory getStopperFactory()
    {
        return this.stopperFactory;
    }

    /**
     * Creates a panel in which the user can drop new Parameters
     */
    public JPanel createParameterChoicePanel()
    {
        JPanel res = new JPanel();
        res.setLayout(new BoxLayout(res, BoxLayout.Y_AXIS));
        res.add(new JLabel("Drop the parameters you wish to evolve here."));
        final DefaultTableModel parameterTableModel = new DefaultTableModel();
        parameterTableModel.addColumn("Parameter");
        res.add(new JScrollPane(new JTable(parameterTableModel)));

        res.setDropTarget(new DropTarget(res, new DropSink() 
            {
                ParameterFactory parameterFactory = new StratmasParameterFactory();
                public boolean checkDrop(final StratmasObject object)
                {
                    if (getRoot() != null &&
                        getRoot().getRoot() != object.getRoot()) {
                        JOptionPane.showMessageDialog(null, 
                                                      "Please only use objects from " + 
                                                      "the tree in this window",
                                                      "Parameter Error", 
                                                      JOptionPane.WARNING_MESSAGE);
                        return false;
                    }

                    Parameter parameter = parameterFactory.getParameter(object);
                    if (parameter != null) {
                        if (!hasParameter(parameter)) {
                            int lastRow = parameterTableModel.getRowCount();
                            parameterTableModel.setRowCount(lastRow + 1);
                            parameterTableModel.setValueAt(parameter, lastRow, 
                                                           parameterTableModel.findColumn("Parameter"));

                            Vector vector = new Vector();
                            for (int i = 0; i < parameterTableModel.getRowCount(); i++) {
                                vector.add(parameterTableModel.getValueAt(i, 
                                                                          parameterTableModel.findColumn("Parameter")));
                            }
                            setEvolveParameters(vector);
                            return true;
                        } else {
                            SwingUtilities.invokeLater(new Runnable()
                            {
                                public void run()
                                {
                                    JOptionPane.showMessageDialog(null, 
                                                                  object.getReference() + " is " + 
                                                                  "already included in the " + 
                                                                  "parameter set.",
                                                                  "Parameter error", 
                                                                  JOptionPane.WARNING_MESSAGE);
                                }
                            });
                            return false;
                        }
                    } else {
                        SwingUtilities.invokeLater(new Runnable()
                            {
                                public void run()
                                {
                                    JOptionPane.showMessageDialog(null, 
                                                                  "It is presently not possible to " + 
                                                                  "evolve parameters of the type '" + 
                                                                  object.getType().getName() + "'", 
                                                                  "Parameter error", 
                                                                  JOptionPane.ERROR_MESSAGE);
                                }
                            });
                        return false;
                    }
                }

                public boolean hasParameter(Parameter parameter)
                {
                    for (int i = 0; i < parameterTableModel.getRowCount(); i++) {
                        Object entry = 
                            parameterTableModel.getValueAt(i, 
                                                           parameterTableModel.findColumn("Parameter"));
                        if (entry != null && parameter instanceof StratmasObjectParameter && 
                            entry instanceof StratmasObjectParameter && 
                            ((StratmasObjectParameter) parameter).getReference().equals(((StratmasObjectParameter) entry).getReference())) {
                            return true;
                        }
                    }

                    return false;
                }
            }));
        
        return res;
    }

    /**
     * Creates a panel in which the user can drop new Parameters
     */
    public JPanel createTargetChoicePanel()
    {
        final JPanel res = new JPanel();
        res.setLayout(new BoxLayout(res, BoxLayout.Y_AXIS));
        res.add(new JLabel("Drop the performance measure you wish to use here."));
        final DefaultTableModel measureTableModel = new DefaultTableModel();
        measureTableModel.addColumn("Measure");
        res.add(new JScrollPane(new JTable(measureTableModel)));

        res.setDropTarget(new DropTarget(res, new DropSink() 
            {
                ParameterFactory parameterFactory = new StratmasParameterFactory();

                public boolean checkDrop(final StratmasObject object)
                {
                    if (getRoot() != null &&
                        getRoot().getRoot() != object.getRoot()) {
                        JOptionPane.showMessageDialog(null, 
                                                      "Please only use objects from " + 
                                                      "the tree in this window",
                                                      "Parameter Error", 
                                                      JOptionPane.WARNING_MESSAGE);
                        return false;
                    }

                    // Special handling for shapes:
                    Parameter parameter = null;
                    if (object.getType().canSubstitute("Shape") && 
                        (JOptionPane.showConfirmDialog(null,
                                                      object.getIdentifier() + " defines " + 
                                                      "a region. The currently supported " + 
                                                      "meassure of this type is a meassure of a " + 
                                                      "process variable within that region.\n" + 
                                                      "To get a listing of availiable process variables " +
                                                      "it is necessary to connect to a server. Do you want " + 
                                                      "to try this?",
                                                      "Connect to server",
                                                      JOptionPane.YES_NO_OPTION) == 0)) {
                        ProcessVariableParameter pvParameter = 
                            new ProcessVariableParameter((Shape) object, null, null);
                        try {
                            ServerSession session = DefaultServerSession.allocateSession();
                            if (session != null) {
                                session.open();
                                pvParameter.setProcessVariables(session);
                                session.close();
                                if (pvParameter.showProcessVariablesOptionPane()) {
                                    parameter = pvParameter;
                                } else {
                                    return false;
                                }
                            } else {
                                JOptionPane.showMessageDialog(null, 
                                                              "Unable to allocate server to get variables.",
                                                              "Server error", 
                                                              JOptionPane.ERROR_MESSAGE);
                                return false;
                            }
                        } catch (ServerException e) {
                            JOptionPane.showMessageDialog(null, 
                                                          "Unable to get variables from server.",
                                                          "Server error", 
                                                          JOptionPane.ERROR_MESSAGE);
                            return false;
                        }
                    } else {
                        parameter = parameterFactory.getParameter(object);
                    }
                    if (parameter != null) {
                        if (!hasParameter() || 
                            JOptionPane.showConfirmDialog(null,
                                                          getTargetParameter() + " is " + 
                                                          "already defined as performance " + 
                                                          "meassure. Do you want to " + 
                                                          "replace it?",
                                                          "Replace current meassure",
                                                          JOptionPane.YES_NO_OPTION) == 0) {
                            setTargetParameter(parameter);
                            measureTableModel.setRowCount(1);
                            measureTableModel.setValueAt(getTargetParameter().toString(), 
                                                         0, 0);
                            if (getEvaluatorStopperFactory() != null) {
                                res.remove((JComponent) getEvaluatorStopperFactory());
                            }

                            setEvaluatorStopperFactory(new GuiIterationsStopperFactory("Simulation")
                                {
                                    /**
									 * 
									 */
									private static final long serialVersionUID = 3762415824715475892L;

									public Stopper getStopper() {
                                        return new IterationsStopper(getIterations())
                                            {
                                                /**
                                                 * Returns the number of iterations for the object o
                                                 *
                                                 * @param o the object to get iterations from.
                                                 */
                                                public int getIterations(Object o) {
                                                    return ((SimulationEvaluator) o).getTarget().getUpdateCount();
                                                }
                                            };
                                    }
                                });
                            res.add((JComponent) getEvaluatorStopperFactory(), 0);
                            res.validate();
                            
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        SwingUtilities.invokeLater(new Runnable()
                            {
                                public void run()
                                {
                                    JOptionPane.showMessageDialog(null, 
                                                                  "It is presently not possible to " + 
                                                                  "use parameters of the type '" + 
                                                                  object.getType().getName() + "' " +
                                                                  "as a perfomance measure.", 
                                                                  "Parameter error", 
                                                                  JOptionPane.ERROR_MESSAGE);
                                }
                            });
                        return false;
                    }
                }

                public boolean hasParameter()
                {
                    return getTargetParameter() != null;
                }
            }));

        return res;
    }

    /**
     * Returns the current targetParameter of this factory.
     */
    Parameter getTargetParameter()
    {
        return this.targetParameter;
    }

    /**
     * Sets the current targetParameter of this factory.
     */
    void setTargetParameter(Parameter targetParameter)
    {
        this.targetParameter = targetParameter;
    }

    /**
     * Returns the current evolveParameters of this factory.
     */
    Vector getEvolveParameters()
    {
        return this.evolveParameters;
    }

    /**
     * Sets the current evolveParameters of this factory.
     */
    void setEvolveParameters(Vector evolveParameters)
    {
        this.evolveParameters = evolveParameters;
    }
    
    /**
     * Returns an instance of an Evolver. 
     */
    public Evolver getEvolver()
    {
        final Parameter targetParameter = getTargetParameter();

        ParameterInstanceSet initialValues = new ParameterInstanceSet();
        StratmasObject yca = null;
        boolean initYca = true; // Hack
        for (Enumeration e = getEvolveParameters().elements(); 
             e.hasMoreElements();) {
            Parameter parameter = (Parameter) e.nextElement();
            StratmasObject object = 
                ((StratmasObjectParameter) parameter).getStratmasObject();
            if (initYca) {
                yca = object;
                initYca = false;
            } else {
                yca = object.getYoungestCommonAncestor(yca);
            }

            initialValues.add(parameter.getParameterInstance(StratmasObjectFactory.cloneObject(object)));
        }
        
        // Find the simulation the parameters belong to:
        StratmasObject simulation = null;
        for(simulation = yca; 
            simulation != null && 
                !simulation.getType().canSubstitute("Simulation"); 
            simulation = simulation.getParent());

        if (simulation == null) {
            return null;
        }

        EvaluatorFactory evaluatorFactory =
            new SimulationEvaluatorFactory(StratmasDispatcher.getDefaultDispatcher(), simulation)
            {
                SimulationEvaluatorTarget createTarget()
                {
                    SimulationEvaluatorTarget target = ((SimulationEvaluatorTargetFactory) getTargetParameter()).createSimulationEvaluatorTarget();
                    target.setStopper(getEvaluatorStopperFactory().getStopper());
                    return target;
                }
            };

        Sampler sampler = getSamplerFactory().getSampler();

        Stopper stopper = getStopperFactory().getStopper();

        if (evaluatorFactory != null &&
            sampler != null &&
            stopper != null) {
            return new Evolver(initialValues, targetParameter, evaluatorFactory, 
                               sampler, stopper);
        } else {
            return null;
        }
    }
}

/**
 * class that provides a convinient minimal droptargetadapter to drop
 * things in.
 */
abstract class DropSink extends DropTargetAdapter
{
    /**
     * Creates a new DropSink
     */
    public DropSink()
    {
    } 

    
    public void dragEnter(DropTargetDragEvent dtde) 
    {
        dtde.acceptDrag(dtde.getDropAction());
    }
    
    public void dragExit(DropTargetEvent dtde) 
    {
    }
    
    public void dragOver(DropTargetDragEvent dtde) 
    {
    }
    
    public void drop(DropTargetDropEvent dtde) 
    {
        if (dtde.isDataFlavorSupported(StratmasObject.STRATMAS_OBJECT_FLAVOR)) {
            dtde.acceptDrop(DnDConstants.ACTION_LINK);
            try {
                Object object = 
                    dtde.getTransferable().getTransferData(StratmasObject.STRATMAS_OBJECT_FLAVOR);
                // Apple's dnd implementation needs some
                // looking over... Call the
                // getTransferData method for the string
                // flavor in order to get a valid
                // callback.
                if (System.getProperty("os.name").equals("macosx")) {
                    dtde.getTransferable().getTransferData(DataFlavor.stringFlavor);
                }
                
                if (object instanceof StratmasObject) {
                    dtde.dropComplete(checkDrop((StratmasObject) object));
                } else {
                    dtde.dropComplete(false);
                }
            } catch (UnsupportedFlavorException e) {
                // Since we checked this in the preceeding
                // if-clause this should not be possible.
                throw new AssertionError("Apparent inconsistency in DnD implementation");
            } catch (IOException e) {
                System.err.println("Error completing drop: " + e.getMessage());
                dtde.dropComplete(false);
            }
        } else {
            dtde.rejectDrop();
        }
    }

    /**
     * Function called with the StratmasObject dropped on this panel.
     * 
     * @param stratmasObject the object dropped.
     *
     * @return true if the drop is accepted.
     */
    abstract boolean checkDrop(StratmasObject stratmasObject);
}
