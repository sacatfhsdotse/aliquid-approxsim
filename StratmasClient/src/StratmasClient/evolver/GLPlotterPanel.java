//         $Id: GLPlotterPanel.java,v 1.8 2006/03/31 16:55:51 dah Exp $
/*
 * @(#)GLPlotterPanel.java
 */

package StratmasClient.evolver;

import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.Box;

import java.util.Vector;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;

/**
 * A class providing a panel for the GLPlotter plotter
 * implementation.
 * @version 1, $Date: 2006/03/31 16:55:51 $
 * @author  Daniel Ahlin
 */
public class GLPlotterPanel extends JPanel
{
    /**
     * The GLPlotter this PlotterPanel encapsulates.
     */
    GLPlotter glPlotter;

    /**
     * The GLPlotterControl this PlotterPanel encapsulates.
     */
    GLPlotterControl glPlotterControl;

    /**
     * Creates a new GLPlotterPanel
     *
     * @param plotter the plotter to encapsulate
     */
    public GLPlotterPanel(GLPlotter plotter)
    {
        super();
        this.glPlotter = plotter;
        this.glPlotter.setPreferredSize(null);
        this.glPlotterControl = new GLPlotterControl(getGLPlotter());
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                              getGLPlotter(),
                                              new JScrollPane(getGLPlotterControl()));
        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);        
    }
    
    /**
     * Returns the GLPlotter this plotterpanel encapsulates.
     */
    public GLPlotter getGLPlotter() 
    {
        return this.glPlotter;
    }

    /**
     * Returns the plotterControl this plotterpanel encapsulates.
     */
    public GLPlotterControl getGLPlotterControl() 
    {
        return this.glPlotterControl;
    }
}

/**
 * Panel containing controls for a GLPlotter
 */
class GLPlotterControl extends JPanel
{
    /**
     * The plotter this panel controls.
     */
    GLPlotter plotter;

    /**
     * Creates a control panel for the given GLPlotter
     *
     * @param plotter the plotter to control
     */    
    public GLPlotterControl(GLPlotter plotter)
    {
        super();
        this.plotter = plotter;
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        createAxisChoosers();
        add(Box.createVerticalGlue());
        createCheckBoxes();
    }

    /**
     * Returns the plotter this panel controls
     */
    GLPlotter getPlotter()
    {
        return this.plotter;
    }

    /**
     * Returns the parameters this plotter plots
     */
    public Vector getParameters()
    {
        return getPlotter().getMatrix().getColumnMap().getParameters();
    }

    /**
     * Creates the panel used for choosing axes in the plot.
     */
    public void createAxisChoosers()
    {
        Box chooserPanel = Box.createVerticalBox();        
        JPanel xAxisChoice = new JPanel();
        xAxisChoice.setLayout(new BoxLayout(xAxisChoice, BoxLayout.Y_AXIS));
        xAxisChoice.add(new JLabel("Horizontal Axis"));
        JComboBox xChooser = new JComboBox(getParameters());
        xAxisChoice.add(xChooser);
        JPanel yAxisChoice = new JPanel();
        yAxisChoice.setLayout(new BoxLayout(yAxisChoice, BoxLayout.Y_AXIS));
        yAxisChoice.add(new JLabel("Depth Axis"));
        JComboBox yChooser = new JComboBox(getParameters());
        yAxisChoice.add(yChooser);
        JPanel zAxisChoice = new JPanel();
        zAxisChoice.setLayout(new BoxLayout(zAxisChoice, BoxLayout.Y_AXIS));
        zAxisChoice.add(new JLabel("Vertical Axis"));
        JComboBox zChooser = new JComboBox(getParameters());
        zAxisChoice.add(zChooser);
        JPanel cAxisChoice = new JPanel();
        cAxisChoice.setLayout(new BoxLayout(cAxisChoice, BoxLayout.Y_AXIS));
        cAxisChoice.add(new JLabel("Color"));
        JComboBox cChooser = new JComboBox(getParameters());
        cAxisChoice.add(cChooser);

        xChooser.setSelectedItem(getPlotter().getXParameter());
        yChooser.setSelectedItem(getPlotter().getYParameter());
        zChooser.setSelectedItem(getPlotter().getZParameter());
        cChooser.setSelectedItem(getPlotter().getCParameter());
        
         xChooser.addActionListener(new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    getPlotter().setXParameter((Parameter) ((JComboBox) e.getSource()).getSelectedItem());
                }
            });

         yChooser.addActionListener(new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    getPlotter().setYParameter((Parameter) ((JComboBox) e.getSource()).getSelectedItem());
                }
            });
         zChooser.addActionListener(new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    getPlotter().setZParameter((Parameter) ((JComboBox) e.getSource()).getSelectedItem());
                }
            });
         cChooser.addActionListener(new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    getPlotter().setCParameter((Parameter) ((JComboBox) e.getSource()).getSelectedItem());
                }
            });

        chooserPanel.add(xAxisChoice);
        chooserPanel.add(yAxisChoice);
        chooserPanel.add(zAxisChoice);
        chooserPanel.add(cAxisChoice);
        chooserPanel.add(Box.createVerticalGlue());

        add(chooserPanel);
    }
    
    /**
     * Creates the panel used for turning on and of various features
     * in the plot.
     */
    public void createCheckBoxes()
    {        
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));

        // Draw interpolation or not.
        JCheckBox interpolationCheckBox = new JCheckBox(new AbstractAction("Solid Surface") 
            {
                public void actionPerformed(ActionEvent e)
                {
                    getPlotter().drawInterpolation(!getPlotter().drawsInterpolation());
                }
            });
        interpolationCheckBox.setSelected(getPlotter().drawsInterpolation());
        checkBoxPanel.add(interpolationCheckBox);

        // Draw interpolation outline or not.
        JCheckBox interpolationOutlineCheckBox = new JCheckBox(new AbstractAction("Surface Outline") 
            {
                public void actionPerformed(ActionEvent e)
                {
                    getPlotter().drawInterpolationOutline(!getPlotter().drawsInterpolationOutline());
                }
            });
        interpolationOutlineCheckBox.setSelected(getPlotter().drawsInterpolationOutline());
        checkBoxPanel.add(interpolationOutlineCheckBox);

        // Draw axes or not.
        JCheckBox axesCheckBox = new JCheckBox(new AbstractAction("Show axes") 
            {
                public void actionPerformed(ActionEvent e)
                {
                    getPlotter().drawAxes(!getPlotter().drawsAxes());
                }
            });
        axesCheckBox.setSelected(getPlotter().drawsAxes());
        checkBoxPanel.add(axesCheckBox);

        // Draw samples or not.
        JCheckBox samplesCheckBox = new JCheckBox(new AbstractAction("Show samples") 
            {
                public void actionPerformed(ActionEvent e)
                {
                    getPlotter().drawSamples(!getPlotter().drawsSamples());
                }
            });
        samplesCheckBox.setSelected(getPlotter().drawsSamples());
        checkBoxPanel.add(samplesCheckBox);

        // Draw trace of samples or not.
        JCheckBox traceCheckBox = new JCheckBox(new AbstractAction("Show trace") 
            {
                public void actionPerformed(ActionEvent e)
                {
                    getPlotter().drawTrace(!getPlotter().drawsTrace());        
                }
            });
        traceCheckBox.setSelected(getPlotter().drawsTrace());
        checkBoxPanel.add(traceCheckBox);

        // Draw a box around graph or not
        JCheckBox boxCheckBox = new JCheckBox(new AbstractAction("Show box") 
            {
                public void actionPerformed(ActionEvent e)
                {
                    getPlotter().drawBox(!getPlotter().drawsBox());
                }
            });
        boxCheckBox.setSelected(getPlotter().drawsBox());
        checkBoxPanel.add(boxCheckBox);

        // Whether to use GL lighting or not.
        JCheckBox lightingCheckBox = new JCheckBox(new AbstractAction("Enable GL lighting") 
            {
                public void actionPerformed(ActionEvent e)
                {
                    getPlotter().enableLighting(!getPlotter().isLightingEnabled());
                }
            });
        lightingCheckBox.setSelected(getPlotter().isLightingEnabled());
        checkBoxPanel.add(lightingCheckBox);

        add(checkBoxPanel);
    }
}
