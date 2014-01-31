package StratmasClient.map;

import javax.swing.JSlider;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.BoxLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.AbstractAction;

import javax.media.opengl.GL;

import java.util.Enumeration;
import java.io.File;

import java.awt.Window;
import java.awt.event.ItemListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowStateListener;
import javax.swing.WindowConstants;

import StratmasClient.map.adapter.MapElementAdapter;
import StratmasClient.map.adapter.ElementAdapter;

import StratmasClient.Debug;

/**
 * Debug frame for the MapDrawer class 
 *
 * @version 1.0
 * @author Daniel Ahlin
 */
public class MapDrawerDebugFrame extends JFrame
{
    public static int locationLoops = 10;
    public static int locationSlices = 10;
    public static double locationAlphaAdjust = 0.0d;



    /**
     * Creates a new frame with debug controls for the specified drawer.
     *
     * @param drawer the MapDrawer controlled by the frame
     */
    public MapDrawerDebugFrame(MapDrawer drawer)
    {
	super("MapDrawerDebugFrame");
	final MapDrawerDebugFrame self = this;

	setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	
	// Add listener that kills the pane if the frame is disposed
 	drawer.frame.addWindowListener(new WindowAdapter() 
 	    {
 		public void windowClosed(WindowEvent e) 
		{
		    dispose();
		    e.getWindow().removeWindowListener(this);
 		}
 	    });


	final MapDrawer mapDrawer = drawer;
	
	// Location drawing
// 	JCheckBox locationCheckBox = new JCheckBox(new AbstractAction("Draw Location") 
// 	    {
// 		public void actionPerformed(ActionEvent e)
// 		{
// 		    JCheckBox box = (JCheckBox) e.getSource();
// 		    for (Enumeration adapters = mapDrawer.mapDrawableAdapters.elements(); 
// 			 adapters.hasMoreElements();) {
// 			((MapElementAdapter)adapters.nextElement()).setDrawLocation(box.isSelected());
// 		    }
// 		}
// 	    });
// 	locationCheckBox.setSelected(false);
// 	locationCheckBox.doClick();

// 	JCheckBox locationOutlineCheckBox = new JCheckBox(new AbstractAction("Draw Location Outline") 
// 	    {
// 		public void actionPerformed(ActionEvent e)
// 		{
// 		    JCheckBox box = (JCheckBox) e.getSource();
// 		    for (Enumeration adapters = mapDrawer.mapDrawableAdapters.elements(); 
// 			 adapters.hasMoreElements();) {
// 			 ((MapElementAdapter)adapters.nextElement()).setDrawLocationOutline(box.isSelected());
// 		    }
// 		}
// 	    });
// 	locationOutlineCheckBox.setSelected(false);
// 	locationOutlineCheckBox.doClick();

	JPanel locationPanel = new JPanel();	
// 	locationPanel.add(locationCheckBox);
// 	locationPanel.add(locationOutlineCheckBox);

	JSlider locationLoopSlider = new JSlider(1, 100, locationLoops);
	final JLabel locationLoopLabel = new JLabel(Integer.toString(locationLoops));
	locationLoopSlider.addChangeListener(new ChangeListener() 
	    {
		public void stateChanged(ChangeEvent e) 
		{
		    JSlider source = (JSlider) e.getSource();
		    if (!source.getValueIsAdjusting()) {
			locationLoops = source.getValue();
			locationLoopLabel.setText(Integer.toString(locationLoops));
			for (Enumeration adapters = mapDrawer.mapDrawableAdapters.elements(); 
			     adapters.hasMoreElements();) {
			    Object o = adapters.nextElement();
			    if (o  instanceof MapElementAdapter) {
				MapElementAdapter adapter = (MapElementAdapter) o;
				if(adapter.getDrawLocation()) {
				    adapter.setDrawLocation(false);
				    adapter.setDrawLocation(true);
				}
			    }
			}
		    }
		}
	    });
	JPanel locationLoopPanel = new JPanel();
	locationLoopPanel.setLayout(new BoxLayout(locationLoopPanel, BoxLayout.Y_AXIS));
	JLabel locationLoopSliderLabel = new JLabel("Location loops (0 - 100)");
	locationLoopPanel.add(locationLoopSliderLabel);
	locationLoopPanel.add(locationLoopSlider);
	locationLoopPanel.add(locationLoopLabel);


	JSlider locationSliceSlider = new JSlider(1, 100, locationSlices);
	final JLabel locationSliceLabel = new JLabel(Integer.toString(locationSlices));
	locationSliceSlider.addChangeListener(new ChangeListener() 
	    {
		public void stateChanged(ChangeEvent e) 
		{
		    JSlider source = (JSlider) e.getSource();
		    if (!source.getValueIsAdjusting()) {
			locationSlices = source.getValue();
			locationSliceLabel.setText(Integer.toString(locationSlices));
			for (Enumeration adapters = mapDrawer.mapDrawableAdapters.elements(); 
			     adapters.hasMoreElements();) {
			    Object o = adapters.nextElement();
			    if (o  instanceof MapElementAdapter) {
				MapElementAdapter adapter = (MapElementAdapter) o;
				if(adapter.getDrawLocation()) {
				    adapter.setDrawLocation(false);
				    adapter.setDrawLocation(true);
				}
			    }
			}
		    }
		}
	    });
	JPanel locationSlicePanel = new JPanel();
	locationSlicePanel.setLayout(new BoxLayout(locationSlicePanel, BoxLayout.Y_AXIS));
	JLabel locationSliceSliderLabel = new JLabel("Location slices (0 - 100)");
	locationSlicePanel.add(locationSliceSliderLabel);
	locationSlicePanel.add(locationSliceSlider);
	locationSlicePanel.add(locationSliceLabel);

// 	JSlider locationAlphaAdjustSlider = new JSlider(0, 100, 0);
// 	final JLabel locationAlphaAdjustLabel = new JLabel(Double.toString(locationAlphaAdjust));
// 	locationAlphaAdjustSlider.addChangeListener(new ChangeListener() 
// 	    {
// 		public void stateChanged(ChangeEvent e) 
// 		{
// 		    JSlider source = (JSlider) e.getSource();
// 		    if (!source.getValueIsAdjusting()) {
// 			int scale = source.getValue();
// 			locationAlphaAdjust = 0.0 + 1.0 * (((double) scale) / 
// 							   (source.getMaximum() - 
// 							    source.getMinimum()));
// 			locationAlphaAdjustLabel.setText(Double.toString(locationAlphaAdjust));
// 			for (Enumeration adapters = mapDrawer.mapDrawableAdapters.elements(); 
// 			     adapters.hasMoreElements();) {
// 			    MapElementAdapter adapter = (MapElementAdapter)adapters.nextElement();
// 			    if(adapter.getDrawLocation()) {
// 				adapter.setDrawLocation(false);
// 				adapter.setDrawLocation(true);
// 			    }
// 			}
// 		    }
// 		}
// 	    });
// 	JPanel locationAlphaAdjustPanel = new JPanel();
// 	locationAlphaAdjustPanel.setLayout(new BoxLayout(locationAlphaAdjustPanel, BoxLayout.Y_AXIS));
// 	JLabel locationAlphaAdjustSliderLabel = new JLabel("Location alpha adjust (0.0 - 1.0)");
// 	locationAlphaAdjustPanel.add(locationAlphaAdjustSliderLabel);
// 	locationAlphaAdjustPanel.add(locationAlphaAdjustSlider);
// 	locationAlphaAdjustPanel.add(locationAlphaAdjustLabel);


	JPanel locationTunePanel = new JPanel();
	locationTunePanel.setLayout(new BoxLayout(locationTunePanel, 
						  BoxLayout.Y_AXIS));
	locationTunePanel.add(locationLoopPanel);
	locationTunePanel.add(locationSlicePanel);
// 	locationTunePanel.add(locationAlphaAdjustPanel);

	// Texture filters
	// MinFilter
	JComboBox texMinFilterChooser = new JComboBox();
	texMinFilterChooser.addItem(new String("GL_NEAREST_MIPMAP_NEAREST"));
	texMinFilterChooser.addItem(new String("GL_LINEAR_MIPMAP_NEAREST"));
	texMinFilterChooser.addItem(new String("GL_NEAREST_MIPMAP_LINEAR"));
	texMinFilterChooser.addItem(new String("GL_LINEAR_MIPMAP_LINEAR"));
	texMinFilterChooser.setSelectedItem("GL_LINEAR_MIPMAP_LINEAR");
	texMinFilterChooser.addItemListener(new ItemListener() 
	    {
		public void itemStateChanged(ItemEvent e) 
		{
		    String str = (String) e.getItem();
		    if (str.equals("GL_NEAREST_MIPMAP_NEAREST")) {
			SymbolToTextureMapper.textureMinFilter = GL.GL_NEAREST_MIPMAP_NEAREST;
		    } else if (str.equals("GL_NEAREST_MIPMAP_LINEAR")) {
			SymbolToTextureMapper.textureMinFilter = GL.GL_NEAREST_MIPMAP_LINEAR;
		    }  else if (str.equals("GL_LINEAR_MIPMAP_NEAREST")) {
			SymbolToTextureMapper.textureMinFilter = GL.GL_LINEAR_MIPMAP_NEAREST;
		    } else if (str.equals("GL_LINEAR_MIPMAP_LINEAR")) {
			SymbolToTextureMapper.textureMinFilter = GL.GL_LINEAR_MIPMAP_LINEAR;
		    } else {
			throw new AssertionError("Should not be here.");
		    }
		    for (Enumeration adapters = mapDrawer.mapDrawableAdapters.elements(); 
			 adapters.hasMoreElements();) {
			((ElementAdapter) adapters.nextElement()).updateSymbol();
		    }
		    mapDrawer.mapDrawableAdapterRecompilation.addAll(mapDrawer.mapDrawableAdapters.values());
		    mapDrawer.update();
		}
	    });
	// MagFilter
	JComboBox texMagFilterChooser = new JComboBox();
	texMagFilterChooser.addItem(new String("GL_NEAREST"));
	texMagFilterChooser.addItem(new String("GL_LINEAR"));
	texMagFilterChooser.setSelectedItem("GL_LINEAR");
	texMagFilterChooser.addItemListener(new ItemListener() 
	    {
		public void itemStateChanged(ItemEvent e) 
		{
		    String str = (String) e.getItem();
		    if (str.equals("GL_NEAREST")) {
			SymbolToTextureMapper.textureMagFilter = GL.GL_NEAREST;
		    } else if (str.equals("GL_LINEAR")) {
			SymbolToTextureMapper.textureMagFilter = GL.GL_LINEAR;
		    } else {
			throw new AssertionError("Should not be here.");
		    }
		    for (Enumeration adapters = mapDrawer.mapDrawableAdapters.elements(); 
			 adapters.hasMoreElements();) {
			((ElementAdapter) adapters.nextElement()).updateSymbol();
		    }
		    mapDrawer.mapDrawableAdapterRecompilation.addAll(mapDrawer.mapDrawableAdapters.values());
		    mapDrawer.update();		    		    
		}
	    });
	JPanel filterPanel = new JPanel();
	filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.X_AXIS));
	filterPanel.add(texMinFilterChooser);
	filterPanel.add(texMagFilterChooser);
	
	// Packaging
	JPanel subpanel = new JPanel();
	subpanel.setLayout(new BoxLayout(subpanel, BoxLayout.Y_AXIS));
 	subpanel.add(locationPanel);
 	subpanel.add(locationTunePanel);
 	subpanel.add(filterPanel);

	getContentPane().add(subpanel);
    }

    /**
     * Starts a thread with a debugframe for this mapdrawer.
     */
    public static void openMapDrawerDebugFrame(MapDrawer drawer)
    {
	final MapDrawerDebugFrame frame = new MapDrawerDebugFrame(drawer);

	javax.swing.SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    frame.pack();
		    frame.setVisible(true);
		}
	    });
    }

}
