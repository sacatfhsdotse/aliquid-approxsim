package StratmasClient.map;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.Vector;
import java.lang.Math;
import java.lang.RuntimeException;
import javax.swing.*;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import com.jogamp.opengl.util.gl2.GLUT;

import StratmasClient.ProcessVariableDescription;

/**
 * Color map scale used for visualizing of STRATMAS simulator results on the map.
 * <p> 
 * The color scale can be created from the predefined colors or the user defined ones. 
 * Number of colors chosen to define the color scale can vary between 2 and 256. When the 
 * colors are chosen, a lookup table of 256 shades is created.
 * Bellow the color map scale, the numerical equivalent values are displayed. These values
 * depend on how the scale is interpreted ie., linearly or logarithmicaly. Further on, the
 * numerical values depends on the upper and lower bounds for the scale.
 * <p> 
 * The scale (linear or logarithmic), the upper and lower bounds and the color map can be 
 * chosen from the dialog implemented in <code>ColorMapDialog</code> class.
 *
 * @see ColorMapDialog
 * 
 * @version 1.0
 * @author Amir Filipovic 
 */
public class ColorMap implements GLEventListener, ActionListener {
    /**
     * Black color.
     */
    private final float[] black_rgb = {0.0f, 0.0f, 0.0f};
    /**
     * Brown color.
     */
    private final float[] brown_rgb = {0.6f, 0.3f, 0.1f};
    /**
     * Olivebrown color.
     */
    private final float[] olivebrown_rgb = {0.5f, 0.5f, 0.0f};
    /**
     * Lightgrey color.
     */
    private final float[] lightgrey_rgb = {0.8f, 0.8f, 0.8f};
    /**
     * Red color.
     */
    private final float[] red_rgb = {1.0f, 0.0f, 0.0f};
    /**
     * Green color.
     */
    private final float[] green_rgb = {0.0f, 1.0f, 0.0f};
    /**
     * Blue color.
     */
    private final float[] blue_rgb = {0.0f, 0.0f, 1.0f};
    /**
     * Lightblue color.
     */
    private final float[] lightblue_rgb = {0.8f, 1.0f, 0.8f};
    /**
     * Yellow color.
     */
    private final float[] yellow_rgb = {1.0f, 1.0f, 0.0f};
    /**
     * Orange color.
     */
    private final float[] orange_rgb = {1.0f, 0.5f, 0.1f};
    /**
     * Cyan color.
     */
    private final float[] cyan_rgb = {0.0f, 1.0f, 1.0f};
    /**
     * Magenta color.
     */
    private final float[] magenta_rgb = {1.0f, 0.0f, 1.0f};
    /**
     * White color.
     */
    private final float[] white_rgb = {1.0f, 1.0f, 1.0f};
    /**
     * The bounding box values.
     */
    private int xmin, xmax, ymin, ymax;
    /**
     * Bounding x-values for the colored area.
     */
    private int xlabel_min, xlabel_max;
    /**
     * Identifier for the display list.
     */
    private int col;
    /**
     * The  actual drawing area.
     */
    private GLCanvas glcolor;
    /**
     * The glu to use.
     */
    private GLU glu = new GLU();
    /**
     * The  number of color shades.
     */
    private int color_shades;
    /**
     * The color map table.
     */
    private float[][] color_table;
    /**
     * The actual displayed values.
     */
    private float[] displayed_values;
    /**
     * Name of the color table.
     */
    private String color_map_name;
    /**
     * The actual scale (logarithmic or linear).
     */
    private String actual_scale;
    /**
     * The  minimum and maximum values represented by the color map.
     */
    private float min_value, max_value;
    /**
     * The frame.
     */
    private JFrame frame;
    /**
     * The panel containing the color map.
     */
    private JPanel colorMapPanel;
    /**
     * The options button.
     */
    private JButton properties_button = new JButton("Options");
    /**
     * The OpenGL utility toolkit.
     */
    private GLUT glut = new GLUT();
    /**
     * The actual process variable.
     */
    private ProcessVariableDescription actual_pv;
    /**
     * The actual layer values.
     */
    private double[] actual_pv_values;
    /**
     * The scaled layer values.
     */
    private int[] scaled_pv_values;
    /**
     * The reference to the map drawer.
     */
    private MapDrawer drawer;
    /**
     * Parameter needed for correct positioning of the color map values.
     */
    private double xscale = 1;
    
    /**
     * Create new color map.
     */
    public ColorMap(MapDrawer drawer) {
        // set reference to the map drawer
        this.drawer = drawer;
        drawer.setColorMap(this);

        // create JOGL canvas
        GLCapabilities glcaps = new GLCapabilities(GLProfile.getDefault());
        glcolor= new GLCanvas(glcaps);
        glcolor.addGLEventListener(this);

        // initialize color map table (256 different shades)
        color_shades = 256;
        color_table = new float[color_shades][3];
        float[][] cmap = new float[4][3];
        cmap[0] = blue_rgb;
        cmap[1] = green_rgb;
        cmap[2] = yellow_rgb;
        cmap[3] = red_rgb;
        update(cmap);
        color_map_name = "blue-green-yellow-red";
        
        // initialize area bounds
        xmin = -30;
        xmax = color_shades+30;
        ymin = 0;
        ymax = 100;
        xlabel_min = 0;
        xlabel_max = color_shades;

        // initialize min and max values
        min_value = 0.0f;
        max_value = 100.0f;
        
        // initialize displayed values
        displayed_values = selectLinearScaleTable(min_value, max_value);
            
        // initialize scale
        actual_scale = "Linear Scale";
        
        // create the panel
        createColorMapPanel();
        
        // show GUI
        //createAndShowGUI();
    }

    /**
     * Create the GUI and show it. 
     */
    public void createAndShowGUI() {
        //Create and set up the window.
        frame = new JFrame("Color Map");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        //Set up the content pane.
        colorMapPanel.setOpaque(true); //content panes must be opaque
        frame.setContentPane(colorMapPanel);
        
        //Display the window.
        int frame_width = 300;
        int frame_height = 130;
        frame.pack();
        frame.setSize(frame_width, frame_height);
        frame.setResizable(true);
        frame.setVisible(true);
    }
    
    /**
     * Creates the panel containing the color map.
     */
    private void createColorMapPanel() {
        // add canvas to a panel
        JPanel canvas_panel = new JPanel();
        canvas_panel.setLayout(new BorderLayout());
        canvas_panel.add(glcolor, BorderLayout.CENTER);
        canvas_panel.setPreferredSize(new Dimension(0,0));
        
        // add button to a panel
        JPanel button_panel = new JPanel();
        button_panel.setLayout(new BorderLayout());
        button_panel.add(new JLabel(), BorderLayout.CENTER);
        properties_button.setFont(properties_button.getFont().deriveFont(Font.PLAIN));
        properties_button.setMargin(new Insets(1,3,1,3));
        properties_button.addActionListener(this);
        button_panel.add(properties_button, BorderLayout.EAST);
        
        // set panel components
        colorMapPanel = new JPanel();
        colorMapPanel.setLayout(new BorderLayout(5,5));
        colorMapPanel.add(canvas_panel, BorderLayout.CENTER);
        colorMapPanel.add(button_panel, BorderLayout.SOUTH);
        colorMapPanel.setBorder(BorderFactory.
                                createCompoundBorder(BorderFactory.createTitledBorder("Color Map"),
                                                     BorderFactory.createEmptyBorder(2,2,2,2)));
    }
    
    /**
     * Returns the panel containing the color map.
     */
    public JPanel getPanel() {
        return colorMapPanel;
    }
    
    /**
     * Initialization of the color map.
     *
     * @param gld needed when opengl is used. 
     */
    public void init(GLAutoDrawable gld) {
        GL2 gl = (GL2) gld.getGL();
        
        // set the background color
        Color c = colorMapPanel.getBackground();
        float r = c.getRed()/255.0f;
        float g = c.getGreen()/255.0f;
        float b = c.getBlue()/255.0f;
        gl.glClearColor(r, g, b, 1.0f);
        
        // enable blending
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
                
        // enable shading
        gl.glShadeModel(GL2.GL_SMOOTH);
        
        // set actual matrix
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        
        // initialize bounding box
        glu.gluOrtho2D(xmin, xmax, ymin, ymax);
        
    }

    /**
     * Draw color map.
     *
     * @param gld needed when opengl is used. 
     */
    public void display(GLAutoDrawable gld) {
        GL gl = gld.getGL();
        
        // draw the color map
        drawGraph(gl);
    }
        
    /**
     * Update color map table with user defined color map table. Number of shades is 
     * currently 256 where each shade consists of it's red, green and blue components.
     *
     * @param ctable array of 256 shades where each shade consists of its red, green and 
     *               blue components. The shades are scaled such that each component
     *               has to lie between 0.0 and 1.0 values (inclusive these two).
     *
     * @throws RuntimeException 1.if the dimension of <code>ctable</code> is unvalid ie.
     *                           it is not [256][3],
     *                          2.if elements of <code>ctable</code> are not in the 
     *                            range 0.0 - 1.0.
     *               
     */
    public void setColorMapTable(float[][] ctable) {
        try {
            for (int i = 0; i < color_table.length; i++) {
                for (int j = 0; j < 3; j++) {
                    if (ctable[i][j] < 0.0f || ctable[i][j] > 1.0f) {
                        throw new RuntimeException();
                    }
                    color_table[i][j] = ctable[i][j];
                }
            }
        }
        catch (RuntimeException e) {
            System.err.println("ERROR : Color map update invalid!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Update color map table with a given combination of colors. Minimum number of colors 
     * is two. Transition between colors is computed by using Hermite interpolation with 
     * slope zero. 
     *
     * @param cmap array of colors (between 2 and 256) where each color consists of its red, 
     *             green and blue components. The shades are scaled such that each component
     *             has to lie between 0.0 and 1.0 values (inclusive these two).
     *
     * @throws RuntimeException 1.if the dimension of <code>ctable</code> is unvalid ie.
     *                           it is not [X][3] with 1<X<257,
     *                          2.if elements of <code>ctable</code> are not in the 
     *                            range 0.0 - 1.0.
     */
    public void update(float[][] cmap) {
        try {
            //prepare x-scale for interpolation
            int nr_of_colors = cmap.length;
            int[] xvalues = new int[nr_of_colors];
            int delx = color_table.length/(nr_of_colors-1);
            for (int i = 0; i < nr_of_colors; i++) {
                xvalues[i] = i*delx;
            }
            // adjust for last x value
            xvalues[nr_of_colors-1] = color_table.length-1;
            // assign values to color map
            for (int i = 0; i < color_table.length; i++) {
                // Hermites algorithm
                float x = (float)i;
                int j = 0;
                while (!(i>=xvalues[j] && i<=xvalues[j+1])) {
                    j++;
                }
                float x1 = (float)xvalues[j];
                float x2 = (float)xvalues[j+1];
                for (int k = 0; k < 3; k++) {
                    float c1 = cmap[j][k];
                    float c2 = (cmap[j+1][k]-cmap[j][k])/(x2-x1);
                    float c3 = -c2/((float)Math.pow((x2-x1),2));
                    float c4 = c3;
                    float px = c1+c2*(x-x1)+(x-x1)*(x-x2)*(c3*(x-x1)+c4*(x-x2));
                    if (px > 1.0f) {
                        px = 1.0f;
                    }
                    else if (px < 0.0f) {
                        px = 0.0f;
                    }
                    color_table[i][k] = px;
                }
            }
            // update color map
            update();
        }
        catch (RuntimeException e) {
            System.err.println("ERROR : Color map update invalid!");
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Updates the scale parameter when the window is resized.
     */
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        xscale = (xmax-xmin)*1.0/width;
    }
    
    /**
     * Not implemented.
     */
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, 
                               boolean deviceChanged) {
    }
    
    /**
     *  Update the color map with the values obtained from the <code>ColorMapDialog</code>.
     *
     * @param ae action that updates the color map.
     *
     * @throws RuntimeException if values returned from the dialog are not valid.
     */
    public void actionPerformed (ActionEvent ae) {
        // get action
        String action = ae.getActionCommand();
        
        try {
            // change of map scale bounds
            if (ae.getSource().equals(properties_button)) {
                Vector v = ColorMapDialog.showDialog(frame, properties_button, actual_scale, 
                                                     min_value, max_value, color_map_name);
                if (v != null && !v.isEmpty()) {
                    // update scale
                    actual_scale = (String)v.get(0);
                    // update bounds
                    min_value = (Float.valueOf((String)v.get(1))).floatValue();
                    max_value = (Float.valueOf((String)v.get(2))).floatValue();
                    // update the current process variable
                    if (actual_pv != null) {
                        actual_pv.setMin(min_value);
                        actual_pv.setMax(max_value);
                    }
                    if (actual_scale.equals("Linear Scale")) {
                        displayed_values = selectLinearScaleTable(min_value, max_value);
                        // update the current process variable
                        if (actual_pv != null) {
                            actual_pv.setLinearScale();
                        }
                    }
                    else {
                        displayed_values = selectLogarithmicScaleTable(min_value, max_value);
                        // update the current process variable
                        if (actual_pv != null) {
                            actual_pv.setLogarithmicScale();
                        } 
                    }
                    // update color map
                    color_map_name = (String)v.get(3);
                    selectColorMap(color_map_name);
                    if (actual_pv != null) {
                        // scale pv layer
                        scalePV();
                        // update the current process variable
                        actual_pv.setColorMap(color_map_name);
                        // update the main map
                        drawer.setUpdatePVValues();
                    }
                }
            }
        }    
        catch (RuntimeException e) {
            System.err.println("ERROR : Invalid action!");
            e.printStackTrace();
        }
    }

    /**
     * Select actual color map. Four chices are available for now on.
     *
     * @param cmap_name name of the color map scale.
     */
    private void selectColorMap(String cmap_name) {
        float cmap[][];
        //
        if (cmap_name.equals("blue-green-yellow-red")) {
            cmap = new float[4][3];
            cmap[0] = blue_rgb;
            cmap[1] = green_rgb;
            cmap[2] = yellow_rgb;
            cmap[3] = red_rgb;
        }
        else if (cmap_name.equals("blue-green-yellow")) {
            cmap = new float[3][3];
            cmap[0] = blue_rgb;
            cmap[1] = green_rgb;
            cmap[2] = yellow_rgb;
        }
        else if (cmap_name.equals("lightblue-yellow-orange-red")) {
            cmap = new float[4][3];
            cmap[0] = lightblue_rgb;
            cmap[1] = yellow_rgb;
            cmap[2] = orange_rgb;
            cmap[3] = red_rgb;
        }
        else if (cmap_name.equals("white-lightgrey-olivebrown-black")) {
            cmap = new float[4][3];
            cmap[0] = white_rgb;
            cmap[1] = lightgrey_rgb;
            cmap[2] = olivebrown_rgb;
            cmap[3] = black_rgb;
        }
        else if (cmap_name.equals("blue-white")) {
            cmap = new float[2][3];
            cmap[0] = blue_rgb;
            cmap[1] = white_rgb;
        }
        else if (cmap_name.equals("red-white")) {
            cmap = new float[2][3];
            cmap[0] = red_rgb;
            cmap[1] = white_rgb;
        }
        else {
            cmap = new float[2][3];
            cmap[0] = black_rgb;
            cmap[1] = white_rgb;
        }
        // update the map
        update(cmap);
    }
    
    /**
     * Compute linear scale table.
     *
     * @param min minimum bound for the scale.
     * @param max maximum bound for the scale.
     *
     * @return numerical values displayed in the color map.
     */
    private float[] selectLinearScaleTable(float min, float max) {
        // check
        max = (max > 100000000000.0f)? 100000000000.0f : max;
        // initialize variables
        float[] temp = {1.0f, 2.0f, 5.0f};
        float[] tmp_table = new float[10];
        float[] lin_table;
        float min_width = 0.05f;
        float interval = (max-min)/3.0f;
        float width = interval;
        int nr_of_values = 0;
        // compute linear scale width
        for (int i = -2; i <= 12; i++) {
            for (int j = 0; j < 3; j++) {
                if (interval > temp[j]*Math.pow(10, i)) {
                    width = (float)(temp[j]*Math.pow(10,i));
                }
            }
        }
        // compute table
        if (width >= min_width) { 
            tmp_table[0] = min;
            nr_of_values++;
            int index = 0;
            while (index*width <= min) {
                index++;
            }
            // if first two values too close
            if (Math.abs(index*width-min) < (max-min)/10.0f) {
                index++;
            }
            while (index*width < max) {
                tmp_table[nr_of_values] = index*width;
                nr_of_values++;
                index++;
            }
            // if last two values too close
            if (Math.abs(tmp_table[nr_of_values-1]-max) < (max-min)/10.0f) {
                tmp_table[nr_of_values-1] = max;
            }
            else {
                tmp_table[nr_of_values] = max;
                nr_of_values++;
            }
        }
        else {
            // only two values
            tmp_table[0] = min;
            tmp_table[1] = max;
            nr_of_values = 2;
        }
        // copy the table
        lin_table = new float[nr_of_values];
        for (int i = 0; i < nr_of_values; i++) {
            lin_table[i] = tmp_table[i];
        }
        return lin_table;
    }

    /**
     * Compute logarithmic scale table. It is assumed that minimum value is nonnegative
     * and maximum value is larger then minimum value.
     *
     * @param min minimum bound.
     * @param max maximum bound.
     *
     * @return numerical values displayed in the color map.
     */
    private float[] selectLogarithmicScaleTable(float min, float max) {
        // check
        max = (max > 100000000000.0f)? 100000000000.0f : max;
        // find all table values
        float[] tmp_table = new float[20];
        int nr_of_values = 0;
        tmp_table[nr_of_values] = min;
        nr_of_values++;
        int i = 0;
        while (min > Math.pow(10,i)) {
            i++;
        }
        // all powers of ten between minimum and maximum value
        while (Math.pow(10,i) < max) {
            tmp_table[nr_of_values] = (float)Math.pow(10,i);
            nr_of_values++;
            i++;
        }
        tmp_table[nr_of_values] = max;
        nr_of_values++;
        // select final table values
        // adjust last two values
        int nr_of_selected_values = nr_of_values;
        if (nr_of_selected_values > 2 &&
            Math.abs(Math.log(max)/Math.log(10)-Math.log(tmp_table[nr_of_values-2])/Math.log(10)) < 0.2) {
            tmp_table[nr_of_values-2] = -1.0f;
            nr_of_selected_values--;
        }
        // adjust first two values
        float tmp_min = (min >= 0.1)? min : 0.1f;
        if (nr_of_selected_values > 2 &&
            Math.abs(Math.log(tmp_min)/Math.log(10)-Math.log(tmp_table[1])/Math.log(10)) < 0.2) {
            tmp_table[1] = -1.0f;
            nr_of_selected_values--;
        }
        int tmp = 2;
        while (nr_of_selected_values > 5) {
            for (int j = tmp/2; j < nr_of_values-1; j = j+tmp) {
                if (tmp_table[j] > 0) {
                    tmp_table[j] = -1.0f;
                    nr_of_selected_values--;
                }
                if (nr_of_selected_values == 5) {
                    j = tmp_table.length;
                }
            }
            tmp = 2*tmp;
        }
        float[] log_table = new float[nr_of_selected_values];
        int ind = 0;
        for (int j = 0; j < nr_of_values; j++) {
            if (tmp_table[j] >= 0) {
                log_table[ind] = tmp_table[j];
                ind++;
            }
        }
        //
        return log_table;
    }
        
    /**
     * Draw string on the given position in the color map.
     *
     * @param gl needed when opengl is used.
     * @param x x position in the canvas where the value is displayed.
     * @param y y position in the canvas where the value is displayed.
     * @param f value to be displayed.
     */
    private void drawString(GL gl2, int x, int y, float f) {
        GL2 gl = (GL2) gl2;
        // 
        DecimalFormat resultFormat;
        String s;
        if ((max_value-min_value) > 3.0f) {
            int i = (int)Math.round(f);
            s = (new Integer(i)).toString();
            // appropriate format if number is too long
            if (i > 99999) {
                resultFormat = new DecimalFormat("00E0");
                s = resultFormat.format(f);
            }
        }
        else {
            // only two decimals 
            resultFormat = new DecimalFormat("0.00");
            s = resultFormat.format(f);
        }
        // get length of the string
        int lng =(int) (xscale * glut.glutBitmapLength(GLUT.BITMAP_HELVETICA_10, s));
        // start position of text
        gl.glRasterPos2i(x-lng/2-1,y);
        // draw text
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, s);
    }
    
    /**
     * Scale given value such that it maps to color scale. It is assumed that minimum value 
     * is nonnegative if logarithmic scale is used. Zero value is scaled to 0.1 in logarithmic
     * scale. Further, it is assumed that maximum value is larger then minimum value for both scales.
     *
     * @param value value which is to be scaled.
     *
     * @return scaled value.
     */
    public int scaleValue(float value) {
        if (value <= min_value) {
            return 0;
        }
        else if (value >= max_value) {
            return color_shades-1;
        }
        // linear scale
        else if (actual_scale.equals("Linear Scale")) {
            return (int)((value-min_value)*(color_shades-1)/(max_value-min_value));
        }
        // logarihmic scale
        else {
            // adapt smallest and largest allowed values for logarithmic scale
            value = (value < 0.1f)? 0.1f : value;
            min_value = (min_value < 0.1f)? 0.1f : min_value;
            max_value = (max_value > 100000000000.0f)? 100000000000.0f : max_value;
            //
            float log_value = (float)(Math.log(value)/Math.log(10));
            float log_max_value = (float)(Math.log(max_value)/Math.log(10));
            float log_min_value = (float)(Math.log(min_value)/Math.log(10));
            return (int)((log_value-log_min_value)*(color_shades-1)/(log_max_value-log_min_value));
        }
    }
    
    /**
     * Sets actual process variable.
     */
    public void setProcessVariable(ProcessVariableDescription pv_desc) {
        // local variable
        boolean bounds_changed = false;
        if (!pv_desc.equals(actual_pv)) {
            // set actual process variable
            actual_pv = pv_desc;
            // update bounds
            if (Math.abs(min_value-(float)actual_pv.getMin()) > 0.001 ||
                Math.abs(max_value-(float)actual_pv.getMax()) > 0.001) {
                min_value = (float)actual_pv.getMin();
                max_value = (float)actual_pv.getMax();
                bounds_changed = true;
            }
            // update scale
            if (bounds_changed || !actual_scale.equals(actual_pv.getScale())) {
                if (actual_pv.getScale().equals("Linear Scale")) {
                    displayed_values = selectLinearScaleTable(min_value, max_value);
                    actual_scale = "Linear Scale";
                }
                else {
                    displayed_values = selectLogarithmicScaleTable(min_value, max_value);
                    actual_scale = "Logarithmic Scale";        
                }
            }
            // update color map
            if (actual_pv.getColorMap() == null) {
                selectColorMap(color_map_name);
            }
            else if (!actual_pv.getColorMap().equals(color_map_name)) {
                color_map_name = actual_pv.getColorMap();
                selectColorMap(color_map_name);
            }
            // update color map
            update();
        }
    }
    
    /**
     * Updates the actual values for the layer.
     */
    public void updateGridData(double[] layer) {
        // set actual layer
        actual_pv_values = layer;
        // scale pv values
        // allocate
        if (scaled_pv_values == null) {
            scaled_pv_values = new int[actual_pv_values.length];
        }
        scalePV();
        // update the main map
        drawer.setUpdatePVValues();
    }
    
    /**
     * Resets the color map.
     */
    public void reset() {
        // reset process variable
        actual_pv = null;
        // reset bounds
        min_value = 0.0f;
        max_value = 100.0f;
        // reset scale
        displayed_values = selectLinearScaleTable(min_value, max_value);
        actual_scale = "Linear Scale";
        // reset color map
        color_map_name = "blue-green-yellow-red";
        selectColorMap(color_map_name);
        //
        actual_pv_values = null;
        scaled_pv_values = null;
        drawer.setUpdatePVValues();
    }
 
    /**
     * Redraws the color map.
     */
    public void update() {
        glcolor.validate();
        glcolor.repaint();        
    }

    /**
     * Returns the actual color table. 
     */
    public float[][] getColorTable() {
        return color_table;
    }
    
    /**
     * Returns the color for the given value. The color is computed according to
     * the actual scale, bounds and color map.
     *
     * @param  value the given value.
     *
     * @return the color matching the value.
     */
    public Color getMappingColor(double value) {
        int scaledValue = scaleValue((float)value);
        return new Color(color_table[scaledValue][0], color_table[scaledValue][1], color_table[scaledValue][2]);
    }

    /**
     * Returns the actual scaled pv layer.
     * OBS! It has meaning only if used together with the current color table.
     *
     * @return indices of the current color table.
     */
    public int[] getScaledPV() {
        return scaled_pv_values;
    }
        
    /**
     * Scales the process variables according to the current color map values.
     */
    private void scalePV() {
        // scale
        for (int i = 0; i < scaled_pv_values.length; i++) {
            scaled_pv_values[i] = scaleValue((float)actual_pv_values[i]);
        }
    }
    
    /**
     * Draw color scale map with it's numerical equvalents.
     *
     * @param gl needed when opengl is used.
     */
    private void drawGraph(GL gl2) {
        GL2 gl = (GL2) gl2;
        // clear the window
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
        
        // draw color map
        int ii = 0;
        for (int x = xlabel_min; x < xlabel_max; x = x+xlabel_max/color_shades) {
            float del_r = color_table[ii][0];
            float del_g = color_table[ii][1];
            float del_b = color_table[ii][2];
            ii++;
            gl.glBegin(GL2.GL_POLYGON);
            gl.glColor3f(del_r, del_g, del_b);
            gl.glVertex2d(x, (3*ymax)/5);
            gl.glVertex2d(x, ymax);
            gl.glVertex2d(x+1, ymax);
            gl.glVertex2d(x+1, (3*ymax)/5);
            gl.glEnd();
        }
        
        // draw paint ticks
        gl.glLineWidth(1.0f);
        gl.glColor3f(black_rgb[0], black_rgb[1], black_rgb[2]);
        // horisontal line
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex2d(xlabel_min, ymax/2);
        gl.glVertex2d(xlabel_max, ymax/2);
        gl.glEnd();
        // draw table values
        // first value
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex2d(xlabel_min, ymax/4);
        gl.glVertex2d(xlabel_min, ymax/2);
        gl.glEnd();
        drawString(gl, xlabel_min, ymin, displayed_values[0]);
        // values in the middle
        for (int i = 1; i < displayed_values.length-1; i++) {
            int pos = scaleValue(displayed_values[i]);
            gl.glBegin(GL2.GL_LINES);
            gl.glVertex2d(pos, ymax/2-5);
            gl.glVertex2d(pos, ymax/2);
            gl.glEnd();
            drawString(gl, pos, ymax/5, displayed_values[i]);
        }
        // last value
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex2d(xlabel_max, ymax/4);
        gl.glVertex2d(xlabel_max, ymax/2);
        gl.glEnd();
        drawString(gl, xlabel_max, ymin, displayed_values[displayed_values.length-1]);
    }
    public void dispose(GLAutoDrawable glad){
      //TODO implement
     }
    
}
