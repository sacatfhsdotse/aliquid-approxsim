package StratmasClient.map.graph;

import java.awt.Font;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.AbstractAction;
import javax.swing.SpringLayout;
import javax.swing.BorderFactory;

import StratmasClient.timeline.SpringUtilities;

/**
 * This class is used to create various GUI objects used in ProcessVariableXYGraph.
 */
public class DisplayControl {
    
    /**
     * Returns the panel which displays the title of the graph
     */
    public static JPanel getTitlePanel(String pvName, String[] factions, Color[] factionColors, String regionId) {
	JPanel titlePanel = new JPanel(new FlowLayout());
	JLabel pvLabel = new JLabel(pvName);
	titlePanel.add(pvLabel);
	if (factions.length == 1) {
	    JLabel firstMiddleLabel = new JLabel(" for faction ");
	    firstMiddleLabel.setFont(firstMiddleLabel.getFont().deriveFont(Font.PLAIN));
	    titlePanel.add(firstMiddleLabel);
	    JLabel factionLabel = new JLabel(factions[0]);
	    factionLabel.setForeground(factionColors[0]);
	    titlePanel.add(factionLabel);
	    JLabel secondMiddleLabel = new JLabel(" over ");
	    secondMiddleLabel.setFont(secondMiddleLabel.getFont().deriveFont(Font.PLAIN));
	    titlePanel.add(secondMiddleLabel);
	}
	else {
	    JLabel middleLabel = new JLabel(" for all factions over ");
	    middleLabel.setFont(middleLabel.getFont().deriveFont(Font.PLAIN));
	    titlePanel.add(middleLabel);
	}
	JLabel regionLabel = new JLabel(regionId);
	titlePanel.add(regionLabel);
	titlePanel.setBorder(BorderFactory.createEmptyBorder(5, 2, 5, 2));
	
	return titlePanel;
    }


    /**
     * Returns the panel which displays the region name.
     */
    public static JPanel getLabelPanel(String regionName) {
	JPanel labelPanel = new JPanel(new SpringLayout());
	// region label
	JLabel reglabel = new JLabel("Region :");
	reglabel.setFont(reglabel.getFont().deriveFont(Font.PLAIN));
	JLabel regionLabel = new JLabel(regionName);
	regionLabel.setFont(regionLabel.getFont().deriveFont(Font.ITALIC));
	// set up the panel
	labelPanel.add(reglabel);
	labelPanel.add(regionLabel);
	SpringUtilities.makeCompactGrid(labelPanel, 1, 2, 0, 0, 2, 2);
	
	return labelPanel;
    }
    
    /**
     * Returns the panel which displays the legend for the current simulation run.
     */
    public static JPanel getCurrentLegendPanel(ProcessVariableXYGraph graph) {
	String[] factions = graph.getFactions();
	Color[] factionColors = graph.getFactionColors();
	// current values
	JPanel currentPanel = new JPanel(new SpringLayout());
	String sign = (new Character('\u2014')).toString();
	for (int i = 0; i < factions.length; i++) {
	    JLabel facLabel = new JLabel(factions[i]);
	    facLabel.setFont(facLabel.getFont().deriveFont(Font.PLAIN));  
	    facLabel.setForeground(factionColors[i]);
	    currentPanel.add(facLabel);
	    JLabel sigLabel = new JLabel(sign + " ");
	    sigLabel.setForeground(factionColors[i]);
	    currentPanel.add(sigLabel);
	}
	SpringUtilities.makeCompactGrid(currentPanel, 1, factions.length * 2, 0, 0, 2, 2);
	currentPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Current"),
								  BorderFactory.createEmptyBorder(2, 2, 2, 2)));
	return currentPanel;
    }
    
    /**
     * Returns the panel which displays the legend for the previous simulation run.
     */
    public static JPanel getPreviousLegendPanel(ProcessVariableXYGraph graph) {
	String[] factions = graph.getFactions();
	Color[] factionColors = graph.getFactionColors();
	// previous values
	JPanel previousPanel = new JPanel(new SpringLayout());
	String sign = (new Character('\u002E')).toString();
	for (int i = 0; i < factions.length; i++) {
	    JLabel facLabel = new JLabel(factions[i]);
	    facLabel.setFont(facLabel.getFont().deriveFont(Font.PLAIN));  
	    facLabel.setForeground(factionColors[i]);
	    previousPanel.add(facLabel);
	    JLabel sigLabel = new JLabel(sign + sign + sign + " ");
	    sigLabel.setForeground(factionColors[i]);
	    previousPanel.add(sigLabel);
	}
	SpringUtilities.makeCompactGrid(previousPanel, 1, factions.length * 2, 0, 0, 2, 2);
	previousPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Previous"),
								   BorderFactory.createEmptyBorder(2, 2, 2, 2)));
	return previousPanel;
    }
    
    /**
     * Returns the menu for saving, exporting and exiting the graph.
     */
    public static JMenu createFileMenu(ProcessVariableXYGraph graph) {
	final ProcessVariableXYGraph fgraph = graph;
	JMenu fileMenu = new JMenu("File");
	fileMenu.setFont(fileMenu.getFont().deriveFont(Font.PLAIN));
	// save the graph
	JMenuItem shotMenuItem = new JMenuItem(new AbstractAction("Save image as ...") {
		public void actionPerformed(ActionEvent e) {
		    fgraph.saveImage();  
		}
	    });
	shotMenuItem.setFont(shotMenuItem.getFont().deriveFont(Font.PLAIN));
	fileMenu.add(shotMenuItem);
	// export the graph
	JMenuItem exportMenuItem = new JMenuItem(new AbstractAction("Export values as ...") {
		public void actionPerformed(ActionEvent e) {
		    fgraph.exportSeries(); 
		}
	    });
	exportMenuItem.setFont(exportMenuItem.getFont().deriveFont(Font.PLAIN));
	fileMenu.add(exportMenuItem);
	fileMenu.addSeparator();
	// exit the graph
	JMenuItem exitMenuItem = new JMenuItem(new AbstractAction("Exit") {
		public void actionPerformed(ActionEvent e) {
		    fgraph.remove();
		}
	    });
	exitMenuItem.setFont(exitMenuItem.getFont().deriveFont(Font.PLAIN));
	fileMenu.add(exitMenuItem);
	
	return fileMenu;
    } 
    
    /**
     * Returns the menu where the scale (linear or logarithmic) can be selected.
     */
    public static JMenu createOptionsMenu(ProcessVariableXYGraph graph) {
	final ProcessVariableXYGraph fgraph = graph;
	JMenu optionsMenu = new JMenu("Options");
	optionsMenu.setFont(optionsMenu.getFont().deriveFont(Font.PLAIN));
	JMenu scaleSubmenu = new JMenu("Scale");
	scaleSubmenu.setFont(scaleSubmenu.getFont().deriveFont(Font.PLAIN));
	optionsMenu.add(scaleSubmenu);
	// set linear scale
	JRadioButtonMenuItem linScaleButton = new JRadioButtonMenuItem(new AbstractAction("Linear") {
		public void actionPerformed(ActionEvent e) {
		    fgraph.setLinearScale();
		}
	    });
	linScaleButton.setFont(linScaleButton.getFont().deriveFont(Font.PLAIN));
	linScaleButton.setSelected(graph.isLinearScale());
	scaleSubmenu.add(linScaleButton);
	// set logarithmic scale
	JRadioButtonMenuItem logScaleButton = new JRadioButtonMenuItem(new AbstractAction("Logarithmic") {
		public void actionPerformed(ActionEvent e) {
		    fgraph.setLogarithmicScale();
		}
	    });
	logScaleButton.setFont(logScaleButton.getFont().deriveFont(Font.PLAIN));
	logScaleButton.setSelected(graph.isLogarithmicScale());
	logScaleButton.setEnabled(graph.getProcessVariable().getMin() >= 0);
	scaleSubmenu.add(logScaleButton);
	ButtonGroup scaleButtons = new ButtonGroup();
	scaleButtons.add(linScaleButton);
	scaleButtons.add(logScaleButton);

	return optionsMenu;
    }

    /**
     * Returns the menu for the display selection on the graph.
     */
    public static JMenu createShowMenu(ProcessVariableXYGraph graph) {
	final ProcessVariableXYGraph fgraph = graph;
	JMenu showMenu = new JMenu("Show");
	showMenu.setFont(showMenu.getFont().deriveFont(Font.PLAIN));
	JMenu showRunSubmenu = new JMenu("Results");
	showRunSubmenu.setFont(showRunSubmenu.getFont().deriveFont(Font.PLAIN));
	showMenu.add(showRunSubmenu);
	// show the current simulation run only
	JRadioButtonMenuItem currentRunButton = new JRadioButtonMenuItem(new AbstractAction("Current Run") {
		public void actionPerformed(ActionEvent e) {
		    fgraph.setShowPrevious(false);  
		}
	    });
	currentRunButton.setFont(currentRunButton.getFont().deriveFont(Font.PLAIN));
	currentRunButton.setSelected(false);
	showRunSubmenu.add(currentRunButton);
	// show both the current and the previous simulations
	JRadioButtonMenuItem twoConsRunsButton = new JRadioButtonMenuItem(new AbstractAction("Current and Previous Run") {
		public void actionPerformed(ActionEvent e) {
		    fgraph.setShowPrevious(true); 
		}
	    });
	twoConsRunsButton.setFont(twoConsRunsButton.getFont().deriveFont(Font.PLAIN));
	twoConsRunsButton.setSelected(true);
	showRunSubmenu.add(twoConsRunsButton);
	ButtonGroup runButtons = new ButtonGroup();
	runButtons.add(currentRunButton);
	runButtons.add(twoConsRunsButton);
	// show the legend
	JCheckBoxMenuItem legendMenuItem = new JCheckBoxMenuItem(new AbstractAction("Legend") {
		public void actionPerformed(ActionEvent e) {
		    JCheckBoxMenuItem thisItem = (JCheckBoxMenuItem)e.getSource();
		    fgraph.setLegendVisible(thisItem.isSelected());
		}
	    });
	legendMenuItem.setFont(legendMenuItem.getFont().deriveFont(Font.PLAIN));
	legendMenuItem.setSelected(true);
	showMenu.add(legendMenuItem);
	
	return showMenu;
    }

    /**
     * Creates the menu bar for the graph.
     */
    public static JMenuBar createMenuBar(ProcessVariableXYGraph graph) {
	final ProcessVariableXYGraph fgraph = graph;
	// file menu bar
	JMenuBar menuBar = new JMenuBar();
	menuBar.add(createFileMenu(graph));
	menuBar.add(createOptionsMenu(graph));
	menuBar.add(createShowMenu(graph));
	
	return menuBar;
    } 


}
