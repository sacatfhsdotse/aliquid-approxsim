//         $Id: GuiIterationsStopperFactory.java,v 1.1 2005/11/01 16:50:47 dah Exp $
/*
 * @(#)GuiIterationsStopperFactory.java
 */

package StratmasClient.evolver;

import javax.swing.JSpinner;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.BoxLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.BorderFactory;


/**
 * Provides Stopper instances by collecting bits and pieces from Gui
 * components.
 *
 * @version 1, $Date: 2005/11/01 16:50:47 $
 * @author  Daniel Ahlin
*/
abstract public class GuiIterationsStopperFactory extends JPanel implements StopperFactory 
{
    /**
     * The min number of iterations for this stopper.
     */
    int iterations = 10;

    /**
     * Creates a new StopperFactory letting the user configure a
     * IterationsStopper.
     *
     * @param name the name of the component being stopped by this
     * stoppers created  by this class.
     */
    public GuiIterationsStopperFactory(String name)
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        SpinnerNumberModel spinnerModel = 
            new SpinnerNumberModel(getIterations(), 0, Integer.MAX_VALUE, 1);
        JSpinner spinner = new JSpinner(spinnerModel);

        spinnerModel.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent event)
                {
                    setIterations(((SpinnerNumberModel) event.getSource()).getNumber().intValue());
                }
            });                
        add(spinner);

        String string = name != null ? name + " " : "";
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(string + "Iterations"),
                                                     BorderFactory.createEmptyBorder(5,5,5,5)));
    }

    /**
     * Creates a new StopperFactory letting the user configure a
     * IterationsStopper.
     */
    public GuiIterationsStopperFactory()
    {
        this(null);
    }

    /**
     * Returns the number of iterations for the iterations stopper.
     */
    public int getIterations()
    {
        return this.iterations;
    }

    /**
     * Sets the number of iterations for the iterations stopper.
     *
     * @param iterations min number of iterations
     */
    public void setIterations(int iterations)
    {
        this.iterations = iterations;
    }

    /**
     * Returns an instance of a IterationsStopper. 
     */
    abstract public Stopper getStopper();
}
