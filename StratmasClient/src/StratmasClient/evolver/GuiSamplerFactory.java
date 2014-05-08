// $Id: GuiSamplerFactory.java,v 1.1 2005/11/01 16:50:47 dah Exp $
/*
 * @(#)GuiSamplerFactory.java
 */

package ApproxsimClient.evolver;

import javax.swing.AbstractAction;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;

import java.awt.event.ActionEvent;

/**
 * Provides Sampler instances by collecting bits and pieces from Gui components.
 * 
 * @version 1, $Date: 2005/11/01 16:50:47 $
 * @author Daniel Ahlin
 */
public class GuiSamplerFactory extends JPanel implements SamplerFactory {
    /**
	 * 
	 */
    private static final long serialVersionUID = 3133753260821134209L;
    /**
     * Indicates whether the samplers created by this factory should be minimizing or not. Default is true.
     */
    boolean isMinimizing = true;

    /**
     * Creates a new SamplerFactory letting the user configure the various components of an Sampler.
     */
    public GuiSamplerFactory() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        ButtonGroup group = new ButtonGroup();

        JRadioButton min = new JRadioButton(new AbstractAction("Minimize") {
            /**
				 * 
				 */
            private static final long serialVersionUID = 7592968722343883850L;

            public void actionPerformed(ActionEvent event) {
                setMinimizing(true);
            }
        });
        JRadioButton max = new JRadioButton(new AbstractAction("Maximize") {
            /**
				 * 
				 */
            private static final long serialVersionUID = -4411530815821390978L;

            public void actionPerformed(ActionEvent event) {
                setMinimizing(false);
            }
        });
        group.add(min);
        group.add(max);
        min.setSelected(isMinimizing());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(min);
        panel.add(max);

        add(panel);

        setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createTitledBorder("Search Strategy"), BorderFactory
                .createEmptyBorder(5, 5, 5, 5)));
    }

    /**
     * Returns true if the samplers created by this factory should be minimizing
     */
    public boolean isMinimizing() {
        return this.isMinimizing;
    }

    /**
     * Sets whether the samplers created by this factory should be minimizing or not.
     * 
     * @param flag true if samplers should be minimizing.
     */
    public void setMinimizing(boolean flag) {
        this.isMinimizing = flag;
    }

    /**
     * Returns an instance of an Sampler.
     */
    public Sampler getSampler() {
        return new GradientSampler(isMinimizing());
    }
}
