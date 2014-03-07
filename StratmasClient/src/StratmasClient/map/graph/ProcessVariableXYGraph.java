package StratmasClient.map.graph;

import java.text.DecimalFormat;
import java.util.Vector;
import java.util.Hashtable;
import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import javax.swing.JPopupMenu;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;
import javax.swing.BorderFactory;
import javax.imageio.ImageIO;

import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;


import StratmasClient.Client;
import StratmasClient.FileExtensionFilter;
import StratmasClient.ProcessVariableDescription;
import StratmasClient.object.StratmasEvent;
import StratmasClient.object.StratmasEventListener;
import StratmasClient.communication.RegionData;
import StratmasClient.timeline.Timeline;
import StratmasClient.map.Visualizer;

/**
 * This is implementation of a graph for a process variable over a time interval. One or all factions
 * can be displayed in the same graph. The used scale can be linear or logarithmic.
 *
 * @version 1.0
 * @author Amir Filipovic 
 */
public class ProcessVariableXYGraph extends JPanel implements StratmasEventListener{
    /**
     * The actual frame.
     */
    final JFrame frame = new JFrame("Stratmas Graph");
    /**
     * The initial width of the frame.
     */
    public static final int DEFAULT_WIDTH = 350;
    /**
     * The initial height of the frame.
     */
    public static final int DEFAULT_HEIGHT = 320;
    /**
     * The actual process variable.
     */
    private ProcessVariableDescription processVariable;
    /**
     * The actual factions.
     */
    private String[] factions;
    /**
     * Colors for the factions.
     */
    private Color[] factionColors;
    /**
     * The current simulation run values.
     */
    private SimulationPVValues currentValues;
    /**
     * The previous simulation run values.
     */
    private SimulationPVValues previousValues;
    /**
     * The actual region name.
     */
    private String regionId;
     /**
     * The latest time the values are delivered. 
     */
    private long latestTime;
    /**
     * The reference to the actual values.
     */
    private RegionData regionData;
    /**
     * The actual scale.
     */
    private String actualScale;
    /**
     * The upper y-bound.
     */
    private double upperYBound;
    /**
     * The lower y-bound.
     */
    private double lowerYBound;
    /**
     * The y-value nearest the lower y-bound. Significant only for the logarithmic scale. 
     */
    private double secondLowerYBound;
    /**
     * The start time.
     */
    private long startTime;
    /**
     * The end time.
     */
    private long endTime;
    /**
     * Indicates if the y-bounds are initialized.
     */
    private boolean initializedYBounds = false;
    /**
     * Indicates if the values from the previous run vill be displayed in the graph.
     */
    private boolean showPrevious = true;
    /**
     * The legend panel.
     */
    private JPanel legendPanel;
    /**
     * The part of the legend panel significant for the previous simulation run. 
     */
    private JPanel previousLegendPanel;
    /**
     * The drawing area.
     */
    private XYDrawingArea xyGraph;
    /**
     * The text label for the time axis.
     */
    private XScaleTextLabel xTextLabel; 
    /**
     * The tick label for the y-axis.
     */
    private YTickLabel yTickLabel;
    /**
     * The text label for the y-axis.
     */
    private YScaleTextLabel yTextLabel; 
    /**
     * Name of the y-axis.
     */
    private JLabel yStringLabel;
    /**
     * The format used for the values displayed on the y-axis.
     */
    private DecimalFormat yScaleFormat;
     /**
     * The number of displayed values on the time axis.
     */
    public static final int NR_OF_DISPLAYED_TIME_VALUES = 5;
    /**
     * The number of displayed values on the y axis.
     */
    private int NR_OF_DISPLAYED_Y_VALUES = 4;
    /**
     * The maximum number of displayed values on the y axis.
     */
    private int MAX_NR_OF_DISPLAYED_Y_VALUES = 6;
    /**
     * The middle y-values used when the scale is logarithmic and the diiference between the bounds is
     * one power of ten.  
     */
    public static double[] MIDDLE_LOG_VALUES = {2.5, 5};
     /**
     * The minimum y value (logarithmic scale).
     */
    public static final double MINIMUM_LOGARITHMIC_Y_VALUE = 0.0000000001;
    /**
     * The maximum y value (logarithmic scale).
     */
    public static final double MAXIMUM_LOGARITHMIC_Y_VALUE = 100000000000.0;
    /**
     * The reference to the timeline.
     */
    private Timeline timeline;
    
    /**
     * Creates a graph for one or all factions of a process variable.
     * 
     * @param timeline the reference to the timeline.
     * @param regionData values of the sctual process variable and factionn(s). 
     * @param processVariable actual process variable for the graph.
     * @param factions actual factions for the graph.
     * @param factionColors colors used to visualze the different factions.
     * @param regionId actual region.
     */
    public ProcessVariableXYGraph(Timeline timeline, RegionData regionData, ProcessVariableDescription processVariable, 
                                  String[] factions, Color[] factionColors, String regionId) {
        // get reference of the timeline
        this.timeline = timeline;
        // get reference to the processVariable values
        this.regionData = regionData;
        // prenumerate
        regionData.addListener(this);
        
        // get the scale
        actualScale = processVariable.getScale();
        // the actual process variable
        this.processVariable = processVariable;
        // the actual factions
        this.factions = factions;
        // colors for each faction
        this.factionColors = factionColors;
        // the actual region
        this.regionId = regionId;
        
        // create lists for the values
        currentValues = new SimulationPVValues(factions);

        // initialize the start and the end times
        startTime = (regionData.getTimestamp() == null)? 0 : getRelativeGraphTime(regionData.getTimestamp().getMilliSecs());
        endTime   = startTime + 20;
        
        // initialize the y-bounds
        setInitialYBounds();
        
        // create the panel for the drawing area and the xy-axis
        JPanel gPanel = createGraphPanel();

        // add the initial values
        Hashtable initValues = regionData.getPV(processVariable.getName());
         if (initValues != null) {
             latestTime = regionData.getTimestamp().getMilliSecs();
             update(initValues, latestTime);
         }
                
        // legend panel
        previousLegendPanel = DisplayControl.getPreviousLegendPanel(this);
        legendPanel = new JPanel(new GridLayout(1, 2));
        legendPanel.add(DisplayControl.getCurrentLegendPanel(this));
        legendPanel.add(previousLegendPanel);
        
        // set the title
        JPanel titlePanel =  DisplayControl.getTitlePanel(processVariable.getName(), factions, factionColors, regionId);
        titlePanel.setPreferredSize(new Dimension(100, DEFAULT_HEIGHT / 7));
        
        // set panel components
        setLayout(new BorderLayout(0, 0));
        add(titlePanel, BorderLayout.NORTH);
        add(gPanel, BorderLayout.CENTER);
        add(legendPanel, BorderLayout.SOUTH);
        
        // set the border
        this.setBorder(BorderFactory.createEmptyBorder(5, 2, 2, 5));
        
        // create GUI
        createGUI(); 
    }
    
    /**
     * Initializes the panel containing the drawing area and the time and y axis. 
     */
    private JPanel createGraphPanel() {
        // get the drawing area
        xyGraph = new XYDrawingArea(this, factionColors);
        
        // the tick label for the time axis
        XTickLabel xTickLabel = new XTickLabel();
        // the tick label for the y-axis
        yTickLabel = new YTickLabel(this);
        // the text label for the time axis
        xTextLabel = new XScaleTextLabel(this);
        // the text label for the y-axis
        yTextLabel = new YScaleTextLabel(this);
        // the description label for the time axis
        JLabel xStringLabel = new JLabel("Time", SwingConstants.CENTER);
        xStringLabel.setFont(this.getFont().deriveFont(Font.PLAIN));
        // the description label for the y-axis
        String yAxisName = (isLinearScale())? new String(processVariable.getName()) : 
            new String("Log("+processVariable.getName()+")");
        yStringLabel = new JLabel(yAxisName, SwingConstants.CENTER);
        yStringLabel.setFont(this.getFont().deriveFont(Font.PLAIN));
        yStringLabel.setUI(new VerticalLabelUI(false));

        // the label used to adjust time axis
        final JLabel tmpLabel = new JLabel();
        
        // the panel for the time axis
        JPanel xScalePanel = new JPanel(new BorderLayout());
        xScalePanel.add(xTickLabel, BorderLayout.NORTH);
        xScalePanel.add(xTextLabel, BorderLayout.CENTER);
        xScalePanel.add(xStringLabel, BorderLayout.SOUTH);

        // the panel for the adjusted time axis
        final JPanel xPanel = new JPanel(new BorderLayout());
        xPanel.add(xScalePanel, BorderLayout.CENTER);
        xPanel.add(tmpLabel, BorderLayout.WEST);

        // the panel for the y-axis
        JPanel yScalePanel = new JPanel(new BorderLayout(3, 3));
        yScalePanel.add(yTickLabel, BorderLayout.EAST);
        yScalePanel.add(yTextLabel, BorderLayout.CENTER);
        yScalePanel.add(yStringLabel, BorderLayout.WEST);
        yScalePanel.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    tmpLabel.setPreferredSize(new Dimension(((JPanel)e.getSource()).getWidth(), 1));
                    tmpLabel.invalidate();
                    xPanel.validate();
                }  
            });
        
        // create the panel for the drawing area and the xy-axis
        JPanel gPanel = new JPanel(new BorderLayout());
        gPanel.add(yScalePanel, BorderLayout.WEST);
        gPanel.add(xyGraph, BorderLayout.CENTER);
        gPanel.add(xPanel, BorderLayout.SOUTH);
        
        return gPanel;
    }

    /**
     * Creates the GUI. 
     */
    private void createGUI() {
        // create and set up the window
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        // add window listener
        final ProcessVariableXYGraph self = this;
        frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    self.remove();
                }
            });
        
        // set up the content pane
        setOpaque(true); 
        frame.getContentPane().add(this);
        frame.setJMenuBar(DisplayControl.createMenuBar(this));

        // display the window
        frame.pack();
        frame.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        frame.setResizable(true);
    }
    
    /**
     * Removes all objects and listeners and closes the window.
     */
    public void remove() {
        synchronized (this) {
            // remove all values
            currentValues.remove();
            if (previousValues != null){
                previousValues.remove();
            }
            // remove this listener
            Visualizer.removeListenerFromRegionData(regionData, this);
            // remove the object form the list of graphs
            Visualizer.removeGraph(this);
            // dipose the frame
            frame.dispose();
        }
    }
    
    /**
     * Resets the graph
     */
    public void reset() {
        // reset the start and the end times
        startTime = getRelativeGraphTime(regionData.getTimestamp().getMilliSecs());
        endTime   = startTime + 20;
        // reset the values
        //previousValues.remove();
        previousValues = currentValues;
        currentValues  = new SimulationPVValues(factions);
        // reset the graph
        xyGraph.reset();
        // reset the labels
        yTickLabel.update();
        xTextLabel.update();
        yTextLabel.update();
    }
    
    /**
     * Updates the graph.
     */
    public void eventOccured(StratmasEvent e) {
        if (e.getSource() instanceof RegionData) {
            // get actual time
            long actTime = ((RegionData)e.getSource()).getTimestamp().getMilliSecs();
            // get actual values
            Hashtable val = ((RegionData)e.getSource()).getPV(processVariable.getName());
            // reset the graph - not the best way to reset the graph but probably the only way 
            // for now on. 
            if (actTime < latestTime) {
                reset();
            }
            // update the latest time
            latestTime = actTime;
            // do update
            update(val, actTime);
        }
    }
    
    /**
     * Sets the initial bounds for the y-axis.
     */
    private void setInitialYBounds() {
        lowerYBound = processVariable.getMin();
        upperYBound = processVariable.getMax();
        
        // the logarithmic scale
        if (isLogarithmicScale()) {
            // check the bounds
            lowerYBound = (lowerYBound <= 0)? 0 : 
                ((lowerYBound  < MINIMUM_LOGARITHMIC_Y_VALUE)? MINIMUM_LOGARITHMIC_Y_VALUE : lowerYBound);
            upperYBound = (upperYBound  > MAXIMUM_LOGARITHMIC_Y_VALUE)? MAXIMUM_LOGARITHMIC_Y_VALUE : upperYBound;
            // the lower bound is equal to zero
            if (lowerYBound == 0) {
                int nrOfDispVal = 2;
                if (upperYBound >= 10) {
                    nrOfDispVal = (int)Math.round(log10(upperYBound)) + 2;
                }
                else if (upperYBound >= MINIMUM_LOGARITHMIC_Y_VALUE) {
                    nrOfDispVal = (int)Math.round(log10(upperYBound) - log10(MINIMUM_LOGARITHMIC_Y_VALUE)) + 2;
                }
                nrOfDispVal = (nrOfDispVal > NR_OF_DISPLAYED_Y_VALUES)? NR_OF_DISPLAYED_Y_VALUES : nrOfDispVal;
                secondLowerYBound = upperYBound / Math.pow(10, nrOfDispVal - 2);
                setNrOfDisplayedYValues(nrOfDispVal); 
            }
            // the lower bound is positive
            else {
                int nrOfDispVal = (int)Math.round(log10(upperYBound) - log10(lowerYBound)) + 1;
                nrOfDispVal = (nrOfDispVal > NR_OF_DISPLAYED_Y_VALUES)? NR_OF_DISPLAYED_Y_VALUES : nrOfDispVal;
                secondLowerYBound = (nrOfDispVal == 2)? lowerYBound * 5 : lowerYBound * 10;
                setNrOfDisplayedYValues((nrOfDispVal == 2)? MIDDLE_LOG_VALUES.length + 2 : nrOfDispVal);
            }
        }
        // the linear scale
        else if (isLinearScale()) {
            setNrOfDisplayedYValues(MAX_NR_OF_DISPLAYED_Y_VALUES);
        }
    }
    
    /**
     * Returns the process variable.
     */
    public ProcessVariableDescription getProcessVariable() {
        return processVariable;
    }
    
    /**
     * Sets the location of the graph.
     */
    public void setGraphLocation(int x, int y) {
        frame.setLocation(x, y);
        // thread safety recommendation
        SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    frame.setVisible(true);
                }
            });
    }
    
    /**
     * Sets the size of the graph.
     */
    public void setGraphSize(Dimension size) {
        frame.setSize(size);
    }
    

    /**
     * Sets the lower y-bound.
     *
     * @param y the lower valye on the y-axis.
     */
    public void setLowerYBound(double y) {
        lowerYBound = y;
    }

     /**
     * Sets the upper y-bound.
     *
     * @param y the upper value on the y-axis.
     */
    public void setUpperYBound(double y) {
        upperYBound = y;
    }

    /**
     * Sets the start time axis.
     *
     * @param startTime minimum value on the time axis.
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
    /**
     * Sets the end time axis.
     *
     * @param endTime maximum value on the time axis.
     */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    /**
     * Sets the legend visible.
     *
     * @param visible if true the legend becomes visible, if false
     *                the legend becomes hidden.
     */
    public void setLegendVisible(boolean visible) {
        legendPanel.setVisible(visible);
    }
    
    /**
     * Returns the factions.
     */
    public String[] getFactions() {
        return factions;
    } 

    /**
     * Returns the faction colors.
     */
    public Color[] getFactionColors() {
        return factionColors;
    } 
    
    /**
     * Returns the actual scale.
     */
    public String getActualScale() {
        return actualScale;
    }
    
    /**
     * Returns the lower y-bound.
     */
    public double getLowerYBound() {
        return lowerYBound;
    }
    
    /**
     * Returns the y-value displayed on the y-axis which is nearest the lower bound.
     */
    public double getSecondLowerYBound() {
        return secondLowerYBound;
    }
    
    /**
     * Returns the upper y-bound.
     */
    public double getUpperYBound() {
        return upperYBound;
    }

    /**
     * Returns the start time.
     */
    public long getStartTime() {
        return startTime;
    }
    
    /**
     * Returns the end time.
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * Returns the current simulation values.
     */
    public SimulationPVValues getCurrentValues() {
        return currentValues;
    } 
    
    /**
     * Returns the simulation values from the previous run.
     */
    public SimulationPVValues getPreviousValues() {
        return previousValues;
    }                 
 
    /**
     * Updates the flag used to show / hide the results from the previos run.
     */
    public void setShowPrevious(boolean showPrevious) {
        this.showPrevious = showPrevious;
        previousLegendPanel.setVisible(showPrevious);
        if (isLinearScale()) {
            updateVerticalAxisLinearBounds();
        }
        else {
            updateVerticalAxisLogarithmicBounds(); 
        }
    } 

    /**
     * Returns true if the values from the previous run will be displayed in the graph.
     */
    public boolean getShowPrevious() {
        return showPrevious && previousValues != null;
    } 

    /**
     * Returns true if the scale is linear.
     */
    public boolean isLinearScale() {
        return (actualScale.equals("Linear Scale"))? true : false;
    }
    
    /**
     * Sets the actual scale to linear.
     */
    public void setLinearScale() {
        if (!isLinearScale()) {
            // set the scale
            actualScale = "Linear Scale";  
            // update the y-axis
            updateVerticalAxisLinearBounds();
            // update the name of the y-axis
            yStringLabel.setText(processVariable.getName());
        }
    }
    
    /**
     * Sets the actual scale to logarithmic.
     */
    public void setLogarithmicScale() {
        if (!isLogarithmicScale() && processVariable.getMin() >= 0) {
            // set the scale
            actualScale = "Logarithmic Scale";   
            // update the y-axis
            updateVerticalAxisLogarithmicBounds();
             // update the name of the y-axis
            yStringLabel.setText("Log("+processVariable.getName()+")");
        }
    }
    
    /**
     * Returns true if the scale is logarithmic.
     */
    public boolean isLogarithmicScale() {
        return (actualScale.equals("Logarithmic Scale"))? true : false;
    }
    
    /**
     * Sets the number of displayed values on the y-axis.
     */
    public void setNrOfDisplayedYValues(int nrOfValues) {
        NR_OF_DISPLAYED_Y_VALUES = nrOfValues;
    }

    /**
     * Returns the number of displayed values on the y-axis.
     */
    public int getNrOfDisplayedYValues() {
        return NR_OF_DISPLAYED_Y_VALUES;
    }
    
    /**
     *  Returns the relative time as displayed in the graph. 
     */
    public long getRelativeGraphTime(long time) {
        return  (time - timeline.getSimStartTime()) / timeline.getDT();
    }

    /**
     * Returns the parameters needed to re-create this graph.
     */
    public Hashtable getParameters() {
        Hashtable hTable = new Hashtable();
        hTable.put("timeline", timeline);
        hTable.put("processVariable", processVariable.getName());
        hTable.put("shape", regionData.getRegion());
        hTable.put("factions", factions);
        hTable.put("factionColors", factionColors);
        hTable.put("regionId", regionId);
        hTable.put("location", frame.getLocationOnScreen());
        hTable.put("size", frame.getSize());
        return hTable;
    }

    /**
     * Updates the graph with new values.
     *
     * @param values values for all actual factions.
     * @param actTime the time when the values are valid.
     */
    public void update(Hashtable values, long actTime) {
        // update the list of the current values
        currentValues.add(values, actTime);
        
        // check if the time axis has to be updated 
        long relTime = getRelativeGraphTime(actTime);
        // update time axis
        if (relTime > endTime) {
            long increment = 20 + (relTime - endTime) / 20 * 20;
            setEndTime(endTime + increment);
            // update the vertical lines
            xyGraph.updateVerticalLinesDisplayList();
            // update the text
            xTextLabel.update();
        }
        
        // get the maximum and minimum y values
        double minVal = (isLinearScale())? currentValues.getMinValue() : currentValues.getMinNonNegativeValue();
        double maxVal = currentValues.getMaxValue();
        
        if (minVal <= maxVal) {
            // check if the values are inside the allowed bounds
            minVal = (minVal <= processVariable.getMin())? processVariable.getMin() : 
                (minVal >= processVariable.getMax())? processVariable.getMax() : minVal;
            maxVal = (maxVal >= processVariable.getMax())? processVariable.getMax() : 
                (maxVal <= processVariable.getMin())? processVariable.getMin() : maxVal;
            
            // update the bounds on the y-axis
            if (minVal < lowerYBound || maxVal > upperYBound || !initializedYBounds) {
                // linear scale
                if (isLinearScale()) {
                    updateVerticalAxisLinearBounds(minVal, maxVal);
                }
                // logarithmic scale
                else {
                    updateVerticalAxisLogarithmicBounds(minVal, maxVal);        
                }
            }
        }
        //        
        update();
    }

    /**
     * Updates the y-axis when the scale is linear.
     */
    public void updateVerticalAxisLinearBounds(double minVal, double maxVal) {
        // the maximum and the minimum of the displayed values are equal
        if (minVal == maxVal) {
            double diff = Math.pow(10, Math.ceil(log10(processVariable.getMax() - processVariable.getMin())));
            double yMarg = (diff < 100000)? diff / 100 : 1000;
            double rMinVal = Math.round(minVal / yMarg)* yMarg;
            double rMaxVal = Math.round(maxVal / yMarg)* yMarg; 
            lowerYBound = rMinVal - yMarg / 2;
            upperYBound = rMaxVal + yMarg / 2;
            // check the allowed bounds
            if (lowerYBound < processVariable.getMin()) {
                lowerYBound = processVariable.getMin();
                upperYBound = (upperYBound < lowerYBound + yMarg)? lowerYBound + yMarg : upperYBound;
            }
            else if (upperYBound > processVariable.getMax()) {
                upperYBound = processVariable.getMax();
                lowerYBound = (lowerYBound > upperYBound - yMarg)? upperYBound - yMarg : lowerYBound;
            }
        }
        // the maximum and the minimum of the displayed values are not equal
        else {
            double yMarg = Math.pow(10, Math.ceil(log10(maxVal - minVal)));
            yMarg = (yMarg / 2 > maxVal - minVal)? yMarg / 2 : yMarg;
            // the pv limit is violated
            if (yMarg >= processVariable.getMax() - processVariable.getMin()) {
                lowerYBound = processVariable.getMin();
                upperYBound = processVariable.getMax();
            }
            else {
                double mm =  Math.pow(10, Math.ceil(log10(yMarg))) / 10;
                // set the lower bound
                lowerYBound = Math.floor(minVal / mm) * mm;
                // check the margin once again
                if (lowerYBound + yMarg < maxVal) {
                    yMarg = (Math.pow(10, Math.ceil(log10(yMarg))) == yMarg)? yMarg * 5 : yMarg * 2;
                }
                // set the upper bound
                if (lowerYBound + yMarg <= processVariable.getMax()) {
                    upperYBound = lowerYBound + yMarg;
                }
                // adapt if the pv limit is violated
                else {
                    upperYBound = processVariable.getMax();
                    lowerYBound = upperYBound - yMarg;
                }
            }
        }
        initializedYBounds = true;
        // ypdate the y label
        yTextLabel.update();
    }
    
    /**
     * Updates the y-axis when the scale is linear.
     */
    public void updateVerticalAxisLinearBounds() {
        // get min and max values
        double minVal = currentValues.getMinValue();
        double maxVal = currentValues.getMaxValue();
        if (getShowPrevious()) {
            minVal = (previousValues.getMinValue() < minVal)? previousValues.getMinValue() : minVal;
            maxVal = (previousValues.getMaxValue() > maxVal)? previousValues.getMaxValue() : maxVal;
        }
        // set the number of displayed values on the y-axis
        setNrOfDisplayedYValues(MAX_NR_OF_DISPLAYED_Y_VALUES);
        initializedYBounds = false;
        // update the bounds
        updateVerticalAxisLinearBounds(minVal, maxVal);
        // update the drawing area
        xyGraph.updateHorizontalLinesDisplayList();
        xyGraph.update();
        // ypdate the y label
        yTickLabel.update();
        yTextLabel.update();
    }
    
    /**
     * Updates the y-axis when the scale is logarithmic.
     */
    public void updateVerticalAxisLogarithmicBounds(double minVal, double maxVal) {
        // check the maximum value
        if (maxVal > upperYBound || !initializedYBounds) {
            upperYBound = Math.pow(10, Math.ceil(log10(maxVal)));
        }
        // check the minimum value
        if (minVal < lowerYBound || !initializedYBounds) {
            lowerYBound = (minVal == 0)? 0 : Math.pow(10, Math.floor(log10(minVal)));
        }
        // the maximum bound is equal to the minimum bound
        if (lowerYBound == upperYBound) {
            if (lowerYBound > 0) {
                upperYBound = lowerYBound * 10;
                setNrOfDisplayedYValues(MIDDLE_LOG_VALUES.length + 2);
                secondLowerYBound = lowerYBound * MIDDLE_LOG_VALUES[0];
            }
            else {
                upperYBound = (processVariable.getMax() >= 100)? 100 : processVariable.getMax();
                int nrOfDispVal = (int)Math.round(log10(upperYBound) - log10(MINIMUM_LOGARITHMIC_Y_VALUE)) + 2;
                setNrOfDisplayedYValues((nrOfDispVal > NR_OF_DISPLAYED_Y_VALUES)? NR_OF_DISPLAYED_Y_VALUES : nrOfDispVal); 
                secondLowerYBound = upperYBound / Math.pow(10, NR_OF_DISPLAYED_Y_VALUES - 2);
            }
        }
        // the maximum bound is different from the minimum bound
        else {
            int nrOfDispVal = (lowerYBound == 0)? (int)Math.round(log10(upperYBound) - log10(MINIMUM_LOGARITHMIC_Y_VALUE)) + 2 :
                (int)Math.round(log10(upperYBound) - log10(lowerYBound)) + 1;
            nrOfDispVal = (nrOfDispVal > MAX_NR_OF_DISPLAYED_Y_VALUES)? MAX_NR_OF_DISPLAYED_Y_VALUES : nrOfDispVal;
            if (nrOfDispVal == 2 && lowerYBound > 0) {
                setNrOfDisplayedYValues(MIDDLE_LOG_VALUES.length + 2);
                secondLowerYBound = lowerYBound * MIDDLE_LOG_VALUES[0];
            }
            else {
                setNrOfDisplayedYValues(nrOfDispVal);
                secondLowerYBound = upperYBound / Math.pow(10, nrOfDispVal - 2); 
            }
        }

        initializedYBounds = true;
        // update the drawing area
        xyGraph.updateHorizontalLinesDisplayList();
        // ypdate the y label
        yTickLabel.update();
        yTextLabel.update();
    }
    
    /**
     * Updates the y-axis when the scale is logarithmic.
     */
    public void updateVerticalAxisLogarithmicBounds() {
        // get min and max values
        double minVal = currentValues.getMinNonNegativeValue();
        double maxVal = currentValues.getMaxValue();
        if (getShowPrevious()) {
            minVal = (previousValues.getMinNonNegativeValue() < minVal)? previousValues.getMinNonNegativeValue() : minVal;
            maxVal = (previousValues.getMaxValue() > maxVal)? previousValues.getMaxValue() : maxVal;
        }
        // set the number of displayed values on the y-axis
        setNrOfDisplayedYValues(4);
        initializedYBounds = false;
        // update the bounds
        updateVerticalAxisLogarithmicBounds(minVal, maxVal);
        // update the drawing area
        xyGraph.updateHorizontalLinesDisplayList();
        xyGraph.update();
        // ypdate the y label
        yTickLabel.update();
        yTextLabel.update();
    }

    /**
     * Returns the base 10 logarithm of the input value.
     */
    public double log10(double value) {
        return Math.log(value) / Math.log(10);
    } 
    
    /**
     * Redraws the graph.
     */
    public void update() {
        xyGraph.update();
    }
    
    /**
     * Updates the y-label.
     */
    public void setYLabelUpdated() {
        this.validateTree();
    }
    
    /**
     * Writes the series as a semicolon separated list to the provided
     * stream.
     *
     * @param stream stream to write to
     * @throws IOException if write to stream throws.
     */
    void seriesToStream(OutputStream stream) throws IOException {
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        format.setTimeZone(new java.util.SimpleTimeZone(0, "UTC"));
        
        if (!showPrevious) {
            String header = "Timestamp (UTC)";
            for (int i = 0; i < factions.length; i++) {
                header += (";" + factions[i] + " " + processVariable.getName());
            }
            stream.write((header + "\n").getBytes());
            for (int j = 0; j < currentValues.times.size(); j++) {
                long time = ((Long)currentValues.times.get(j)).longValue();
                String line = "" + format.format(new java.util.Date(time));
                for (int i = 0; i < factions.length; i++) {
                    // get value
                    Hashtable htable = (Hashtable) currentValues.values.get(j);
                    Number val = (Number)htable.get(factions[i]);
                    if (val != null) {
                        line += ";" + val.toString();
                    }
                }           
                stream.write((line + "\n").getBytes());
            }
        } else {
            String header = "Run;Timestamp (UTC)";
            for (int i = 0; i < factions.length; i++) {
                header += (";" + factions[i] + " " + processVariable.getName());
            }
            stream.write((header + "\n").getBytes());
            for (int j = 0; j < currentValues.times.size(); j++) {
                long time = ((Long)currentValues.times.get(j)).longValue();
                String line = "Current;" + format.format(new java.util.Date(time));
                for (int i = 0; i < factions.length; i++) {
                    // get value
                    Hashtable htable = (Hashtable) currentValues.values.get(j);
                    Number val = (Number)htable.get(factions[i]);
                    if (val != null) {
                        line += ";" + val.toString();
                    }
                }           
                stream.write((line + "\n").getBytes());
            }
            if (previousValues != null) {
                for (int j = 0; j < previousValues.times.size(); j++) {
                    long time = ((Long)previousValues.times.get(j)).longValue();
                    String line = "Previous;" + format.format(new java.util.Date(time));
                    for (int i = 0; i < factions.length; i++) {
                        // get value
                        Hashtable htable = (Hashtable) previousValues.values.get(j);
                        Number val = (Number)htable.get(factions[i]);
                        if (val != null) {
                            line += ";" + val.toString();
                        }
                    }           
                    stream.write((line + "\n").getBytes());
                }
            }
        }
    }

    /**
     * Exports series to a file by using a JFileChooser to get a file.
     */
    void exportSeries()
    {
        try {
            String filename = Client.getFileNameFromDialog(".csv", JFileChooser.SAVE_DIALOG, frame);
            if (filename != null) {
                OutputStream stream = new FileOutputStream(filename);
                seriesToStream(stream);
                stream.close();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(getTopLevelAncestor(), "Error exporting data:\n" + 
                                          e.getMessage(),
                                          "Export error", 
                                          JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Saves the graph as image.
     */
    void saveImage() {
        // select the file
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        chooser.addChoosableFileFilter(new FileExtensionFilter(".jpg"));
        chooser.addChoosableFileFilter(new FileExtensionFilter(".png"));
        chooser.setAcceptAllFileFilterUsed(false);
        int retVal = chooser.showSaveDialog(frame);
        if(retVal == JFileChooser.APPROVE_OPTION) {
            // add extension to the file if necessary
            File file = chooser.getSelectedFile();
            String extension = (new String(".")).concat(((FileExtensionFilter)chooser.getFileFilter()).getExtension());
            if (!file.getName().endsWith(extension)) {
                file = new File(file.getParent(), file.getName() + extension);
            }
            
            // get the rectangle of the panel
            java.awt.Point loc = this.getLocationOnScreen();
            Rectangle rectangle = new Rectangle((int)loc.getX(), (int)loc.getY(), this.getWidth(), this.getHeight());
            
            //         BufferedImage image = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
            //                 Graphics g = image.getGraphics();
            //                 g.fillRect(0, 0, image.getWidth(), image.getHeight());
            //                 this.paint(g);
            //                 g.dispose();
            
            // start new thread
            final ProcessVariableXYGraph self = this;
            final Rectangle fRect = rectangle;
            final File fFile = file;
            final String fExtension = extension;
            Thread helpThread = new Thread () {
                    public void run() {
                        // the graph must not be covered with any component
                        synchronized(self) {
                            int callNr = 0;
                            do {
                                try {
                                    self.requestFocus();
                                    Thread.sleep(500);
                                    callNr++;
                                }
                                catch(InterruptedException e) {
                                }
                            }
                            while (!self.isFocusOwner() && callNr < 3);
                            
                            // take the screenshot and save the graph if it's completely visible
                            if (self.isFocusOwner()) {
                                try {
                                    // needed to create the image
                                    Robot robot = new Robot();        
                                    BufferedImage image = robot.createScreenCapture(fRect);
                                    
                                    // JPEG format
                                    if (fExtension.equals(".jpg")) {
                                        ImageIO.write(image, "jpg", fFile);
                                    } 
                                    // PNG format
                                    else if (fExtension.equals(".png")){
                                        ImageIO.write(image, "png", fFile);
                                        
                                    }  
                                } 
                                catch (Exception e) {
                                    System.out.println(e.getMessage());
                                }
                            }
                            // if the graph is not saved ie. it can't gain focus
                            else {
                                JOptionPane.showMessageDialog(getTopLevelAncestor(), 
                                                              "Error : Saving of file " + fFile.getAbsolutePath() + 
                                                              " failed!",
                                                              "Saving file error", 
                                                              JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                };
            // start the thread
            helpThread.start();
        }
    }
    
}    

/**
 * Stores the processVariable values and the times for these values for each particular simulation run.
 */
class SimulationPVValues {
    /**
     * The values for each faction.
     */
    Vector values = new Vector();
    /**
     * The times for the delivered values.
     */
    Vector times = new Vector();
    /**
     * The maximum value.
     */
    double maxValue;
    /**
     * The minimum value.
     */
    double minValue;
    /**
     * The minimum non negative value.
     */
    double minNonNegativeValue = Double.MAX_VALUE;   
    /**
     * Indicates if the min and max values are initialized.
     */
    boolean initialized = false;
    /**
     * The list of factions.
     */
    String[] factions;
    
    /**
     * Creates new list of values.
     *
     * @param factions the list of factions.
     */
    public SimulationPVValues (String[] factions) {
        this.factions = factions;
    }
    
    /**
     * Adds new values and the time when these values are obtained to
     * the lists.
     *
     * @param value processVariable values for each faction.
     * @param time the time when the values are obtained.
     */
    public void add(Hashtable value, long time) {
        // add the values
        values.add(value);
        // update the maximum value, the minimum value and the minimum positive value 
        double minVal = ((Number)value.get(factions[0])).doubleValue();
        double maxVal = minVal;
        minNonNegativeValue = (minVal >= 0 && minVal < minNonNegativeValue)? minVal : minNonNegativeValue;
        for (int i = 1; i < factions.length; i++) {
            double tmpValue = ((Number)value.get(factions[i])).doubleValue();
            minVal = (tmpValue < minVal)? tmpValue : minVal;
            maxVal = (tmpValue > maxVal)? tmpValue : maxVal;
            minNonNegativeValue = (tmpValue >= 0 && tmpValue < minNonNegativeValue)? tmpValue : minNonNegativeValue;
        }
        if (initialized) {
            minValue = (minVal < minValue)? minVal : minValue;
            maxValue = (maxVal > maxValue)? maxVal : maxValue;
        }
        else {
            minValue = minVal;
            maxValue = maxVal;
            initialized = true;
        }

        // add the time
        times.add(new Long(time));
    }
    
    /**
     * Returns the maximum value.
     */
    public double getMaxValue() {
        return maxValue;
    }
    
    /**
     * Returns the minimum value.
     */
    public double getMinValue() {
        return minValue;
    }
    
    /**
     * Returns the minimum non-negative value.
     */
    public double getMinNonNegativeValue() {
        return minNonNegativeValue;
    }
    
    /**
     * Removes all values.
     */
    public void remove() {
        values.removeAllElements();
        times.removeAllElements();
    }
}    
