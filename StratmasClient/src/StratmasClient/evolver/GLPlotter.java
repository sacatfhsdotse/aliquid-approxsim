// $Id: GLPlotter.java,v 1.9 2006/04/18 13:01:15 dah Exp $
/*
 * @(#)GLPlotter.java
 */

package ApproxsimClient.evolver;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.common.nio.Buffers;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import java.io.UnsupportedEncodingException;

import javax.swing.JPanel;
import java.nio.IntBuffer;

import java.awt.Component;
import java.awt.BorderLayout;

/**
 * Plots data using Jogl, uses trianglestrips since Jogl does not support GLU - Nurbs
 * 
 * @version 1, $Date: 2006/04/18 13:01:15 $
 * @author Daniel Ahlin
 */
public class GLPlotter extends JPanel implements GLEventListener,
        MatrixEventListener {
    /**
	 * 
	 */
    private static final long serialVersionUID = 2403926532839027050L;

    /**
     * Whether axes should be drawn.
     */
    boolean drawAxes = true;

    /**
     * Whether samples should be drawn.
     */
    boolean drawSamples = true;

    /**
     * Whether trace of the samples should be drawn.
     */
    boolean drawTrace = false;

    /**
     * Whether trace a box around the plot should be drawn.
     */
    boolean drawBox = false;

    /**
     * Whether interpolation should be drawn.
     */
    boolean drawInterpolation = true;

    /**
     * Whether interpolation outline should be drawn.
     */
    boolean drawInterpolationOutline = false;

    /**
     * Whether GL lighting should be used.
     */
    boolean enableLighting = false;

    /**
     * The toplevel display lists used.
     */
    IntBuffer topDisplayListsBuf = Buffers
            .newDirectIntBuffer(SAMPLES_DISPLAY_LIST_POS + 1);

    /**
     * Position of the display list drawing the box
     */
    static final int BOX_DISPLAY_LIST_POS = 0;

    /**
     * Whether box display list is up to date.
     */
    boolean boxListUpdated = false;

    /**
     * Position of the display list drawing the axes
     */
    static final int AXES_DISPLAY_LIST_POS = 1;

    /**
     * Whether axes display list is up to date.
     */
    boolean axesListUpdated = false;

    /**
     * Position of the display list drawing the interpolated surface
     */
    static final int INTERPOLATION_DISPLAY_LIST_POS = 2;

    /**
     * Whether interpolation surface display list is up to date.
     */
    boolean interpolationListUpdated = false;

    /**
     * Position of the display list drawing the samples and trace
     */
    static final int SAMPLES_DISPLAY_LIST_POS = 3;

    /**
     * Whether samples display list is up to date.
     */
    boolean samplesListUpdated = false;

    /**
     * The camera defining the view of this plot.
     */
    Camera camera;

    /**
     * Wheter to plot the X dimension.
     */
    boolean plotX = false;

    /**
     * Wheter to plot the X dimension.
     */
    boolean plotY = false;

    /**
     * Wheter to plot the X dimension.
     */
    boolean plotZ = false;

    /**
     * The number of gridtiles.
     */
    int xTiles = 40;

    /**
     * The number of gridtiles.
     */
    int yTiles = 40;

    /**
     * The grid approximation of the the surface plot.
     */
    double[] grid;

    /**
     * The data to plot.
     */
    EvaluationsMatrix matrix;

    /**
     * The parameter to display on the x-axis
     */
    Parameter xParameter;

    /**
     * Smallest x to plot
     */
    double xMin;

    /**
     * Largest x to plot
     */
    double xMax;

    /**
     * The parameter to display on the y-axis
     */
    Parameter yParameter;

    /**
     * Smallest y to plot
     */
    double yMin;

    /**
     * Largest y to plot
     */
    double yMax;

    /**
     * The parameter to display on the z-axis
     */
    Parameter zParameter;

    /**
     * Smallest z to plot
     */
    double zMin;

    /**
     * Largest z to plot
     */
    double zMax;

    /**
     * The parameter to display as color
     */
    Parameter cParameter;

    /**
     * Smallest c to plot
     */
    double cMin;

    /**
     * Largest c to plot
     */
    double cMax;

    /**
     * The values for the non-plotted value columns
     */
    double[] parameterSlice;

    /**
     * The GLAutoDrawable used to render the plot.
     */
    GLAutoDrawable gld;

    /**
     * The GLU used when rendering this plot.
     */
    GLU glu;

    /**
     * The function that maps the color value to a color.
     */
    ColorMapper colorMapper = new ColorMapper() {
        public double[] map(double d) {
            // Map interval to 1.5*pi
            double mappedValue = (d - getCMin())
                    * (1.5 * Math.PI / (getCMax() - getCMin()));

            return new double[] {// Blue
                    mappedValue > Math.PI ? Math
                            .cos((mappedValue - Math.PI) / 1.5) : 0,

                    // Green
                    mappedValue > 0.5 * Math.PI && mappedValue < 1.5 * Math.PI ? Math
                            .cos((mappedValue - 0.5 * Math.PI) / 1.5) : 0,

                    // Red
                    mappedValue < Math.PI ? Math.cos((mappedValue) / 1.5)
                            : 0.0d,

                    // Alpha
                    1.0d };
        }
    };

    /**
     * Creates a plotter plotting the provided evolver.
     */
    public GLPlotter(Evolver evolver) {
        super();
        setEvolver(evolver);
        GLCapabilities glCapabilities = new GLCapabilities(
                GLProfile.getDefault());
        glCapabilities.setHardwareAccelerated(true);
        this.glu = new GLU();
        this.gld = new GLCanvas(glCapabilities);
        this.camera = new Camera((Component) this.gld);
        this.gld.addGLEventListener(this);
        ((GLCanvas) this.gld).setSize(800, 800);
        setLayout(new BorderLayout());
        add((GLCanvas) gld, BorderLayout.CENTER);
        setupMouseListener((GLCanvas) gld);
    }

    /**
     * Sets the evolver of this plotter.
     */
    public void setEvolver(Evolver evolver) {
        if (evolver != null) {
            // Set default parameters
            setMatrix(new EvaluationsMatrix(evolver));
            int nParameters = evolver.getParameters().size();
            setXParameter((Parameter) evolver.getParameters().get(0));
            setYParameter((Parameter) evolver.getParameters()
                    .get(1 % nParameters));
            setZParameter(evolver.getEvaluationParameter());
            setCParameter(evolver.getEvaluationParameter());
        }
    }

    /**
     * Sets data matrix of this plotter.
     */
    public void setMatrix(EvaluationsMatrix matrix) {
        if (getMatrix() != null) {
            getMatrix().removeEventListener(this);
        }

        this.matrix = matrix;

        if (getMatrix() != null) {
            getMatrix().addEventListener(this);
        }

        setParameterSlice(new double[getMatrix().getColumnMap().getParameters()
                .size()]);

        invalidatePlot();
    }

    /**
     * Returns data matrix of this plotter.
     */
    EvaluationsMatrix getMatrix() {
        return this.matrix;
    }

    /**
     * Sets the parameter slice used to get the value for parameters that are not used for axes parameters. Note that the slice is expected
     * to be getMatrix().getColumnMap().getParameters().size() long.
     * 
     * @param slice the slice
     */
    void setParameterSlice(double[] slice) {
        this.parameterSlice = slice;
        updateGrid();
        invalidatePlot();
    }

    /**
     * Returns the parameter slice used to get the value for parameters that are not used for axes parameters. Note that the slice is
     * expected to be getMatrix().getColumnMap().getParameters().size() long.
     */
    double[] getParameterSlice() {
        return this.parameterSlice;
    }

    /**
     * Called when matrix is updated.
     * 
     * @param event the event.
     */
    public void matrixUpdated(MatrixEvent event) {
        setXSpan();
        setYSpan();
        setZSpan();
        setCSpan();
        updateGrid();
        invalidatePlot();
        update();
    }

    /**
     * Sets up a mouse listener for the canvas
     */
    protected void setupMouseListener(GLCanvas canvas) {
        GLPlotterMouseListener mouseListener = new GLPlotterMouseListener(this);

        canvas.addMouseListener(mouseListener);
        canvas.addMouseMotionListener(mouseListener);
        canvas.addMouseWheelListener(mouseListener);
    }

    /**
     * Returns the camera of the plot.
     */
    public Camera getCamera() {
        return this.camera;
    }

    /**
     * Invalidates entire plot.
     */
    void invalidatePlot() {
        setIsBoxListUpdated(false);
        setIsAxesListUpdated(false);
        setIsInterpolationListUpdated(false);
        setIsSamplesListUpdated(false);
    }

    /**
     * Whether axes should be drawn.
     * 
     * @param drawAxes true if axes should be drawn
     */
    public void drawAxes(boolean drawAxes) {
        this.drawAxes = drawAxes;
        setIsAxesListUpdated(false);
    }

    /**
     * Whether trace should be drawn
     * 
     * @param drawTrace true if trace should be drawn
     */
    public void drawTrace(boolean drawTrace) {
        this.drawTrace = drawTrace;
        setIsSamplesListUpdated(false);
    }

    /**
     * Whether box should be drawn
     * 
     * @param drawBox true if box should be drawn
     */
    public void drawBox(boolean drawBox) {
        this.drawBox = drawBox;
        setIsBoxListUpdated(false);
    }

    /**
     * Whether to enable GL lighting
     * 
     * @param flag true if gl lighting should be used
     */
    public void enableLighting(boolean flag) {
        this.enableLighting = flag;
        invalidatePlot();
    }

    /**
     * Returns true if box list is up to date.
     */
    boolean isBoxListUpdated() {
        return this.boxListUpdated;
    }

    /**
     * Sets whether box have been updated since last redraw.
     * 
     * @param flag true if box are up to date.
     */
    void setIsBoxListUpdated(boolean flag) {
        this.boxListUpdated = flag;
        if (!flag) {
            update();
        }
    }

    /**
     * Returns true if axes display list is up to date.
     */
    boolean isAxesListUpdated() {
        return this.axesListUpdated;
    }

    /**
     * Sets whether axes have been updated since last redraw.
     * 
     * @param flag true if axes are up to date.
     */
    void setIsAxesListUpdated(boolean flag) {
        this.axesListUpdated = flag;
        if (!flag) {
            update();
        }
    }

    /**
     * Returns true if interpolation surface display list is up to date.
     */
    boolean isInterpolationListUpdated() {
        return this.interpolationListUpdated;
    }

    /**
     * Sets whether interpolation have been updated since last redraw.
     * 
     * @param flag true if interpolations are up to date.
     */
    void setIsInterpolationListUpdated(boolean flag) {
        this.interpolationListUpdated = flag;
        if (!flag) {
            update();
        }
    }

    /**
     * Returns true if samples display list is up to date.
     */
    boolean isSamplesListUpdated() {
        return this.samplesListUpdated;
    }

    /**
     * Sets whether samples have been updated since last redraw.
     * 
     * @param flag true if samples are up to date.
     */
    void setIsSamplesListUpdated(boolean flag) {
        this.samplesListUpdated = flag;
        if (!flag) {
            update();
        }
    }

    /**
     * Whether interpolation should be drawn
     * 
     * @param drawInterpolation true if interpolation should be drawn
     */
    public void drawInterpolation(boolean drawInterpolation) {
        this.drawInterpolation = drawInterpolation;
        setIsInterpolationListUpdated(false);
    }

    /**
     * Whether interpolation outline should be drawn
     * 
     * @param drawInterpolationOutline true if interpolation outline should be drawn
     */
    public void drawInterpolationOutline(boolean drawInterpolationOutline) {
        this.drawInterpolationOutline = drawInterpolationOutline;
        setIsInterpolationListUpdated(false);
    }

    /**
     * Whether samples should be drawn
     * 
     * @param drawSamples true if samples should be drawn
     */
    public void drawSamples(boolean drawSamples) {
        this.drawSamples = drawSamples;
        setIsSamplesListUpdated(false);
    }

    /**
     * Whether axes should be drawn.
     */
    public boolean drawsAxes() {
        return this.drawAxes;
    }

    /**
     * Whether trace should be drawn
     */
    public boolean drawsTrace() {
        return this.drawTrace;
    }

    /**
     * Whether box should be drawn
     */
    public boolean drawsBox() {
        return this.drawBox;
    }

    /**
     * Whether to enable GL lighting
     */
    public boolean isLightingEnabled() {
        return this.enableLighting;
    }

    /**
     * Whether interpolation should be drawn
     */
    public boolean drawsInterpolation() {
        return this.drawInterpolation;
    }

    /**
     * Whether interpolation outline should be drawn
     */
    public boolean drawsInterpolationOutline() {
        return this.drawInterpolationOutline;
    }

    /**
     * Whether samples should be drawn
     */
    public boolean drawsSamples() {
        return this.drawSamples;
    }

    /**
     * Returns the parameter to plot on the x axis
     */
    public Parameter getXParameter() {
        return this.xParameter;
    }

    /**
     * Returns the parameter to plot on the y axis
     */
    public Parameter getYParameter() {
        return this.yParameter;
    }

    /**
     * Returns the parameter to plot on the z axis
     */
    public Parameter getZParameter() {
        return this.zParameter;
    }

    /**
     * Returns the parameter to use for coloring.
     */
    public Parameter getCParameter() {
        return this.cParameter;
    }

    /**
     * Sets the parameter to plot on the x axis
     * 
     * @param parameter the parameter to plot on the x axis.
     */
    public void setXParameter(Parameter parameter) {
        if (parameter != getXParameter()) {
            this.xParameter = parameter;
            // Force reset of span
            this.xMin = Double.POSITIVE_INFINITY;
            this.xMax = Double.NEGATIVE_INFINITY;
            setXSpan();
            updateGrid();
            invalidatePlot();
            update();
        }
    }

    /**
     * Sets the parameter to plot on the y axis
     * 
     * @param parameter the parameter to plot on the y axis.
     */
    public void setYParameter(Parameter parameter) {
        if (parameter != getYParameter()) {
            this.yParameter = parameter;
            // Force reset of span
            this.yMin = Double.POSITIVE_INFINITY;
            this.yMax = Double.NEGATIVE_INFINITY;
            setYSpan();
            updateGrid();
            invalidatePlot();
            update();
        }
    }

    /**
     * Sets the parameter to plot on the z axis
     * 
     * @param parameter the parameter to plot on the z axis.
     */
    public void setZParameter(Parameter parameter) {
        if (parameter != getZParameter()) {
            this.zParameter = parameter;
            // Force reset of span
            this.zMin = Double.POSITIVE_INFINITY;
            this.zMax = Double.NEGATIVE_INFINITY;
            setZSpan();
            updateGrid();
            invalidatePlot();
            update();
        }
    }

    /**
     * Sets the parameter to get plot color from
     * 
     * @param parameter the parameter to get plot color from
     */
    public void setCParameter(Parameter parameter) {
        if (parameter != getCParameter()) {
            this.cParameter = parameter;
            // Force reset of span
            this.cMin = Double.POSITIVE_INFINITY;
            this.cMax = Double.NEGATIVE_INFINITY;
            setCSpan();
            updateGrid();
            invalidatePlot();
            update();
        }
    }

    /**
     * Returns the smallest x
     */
    double getXMin() {
        return this.xMin;
    }

    /**
     * Returns the largest x
     */
    double getXMax() {
        return this.xMax;
    }

    /**
     * Returns the smallest y
     */
    double getYMin() {
        return this.yMin;
    }

    /**
     * Returns the largest y
     */
    double getYMax() {
        return this.yMax;
    }

    /**
     * Returns the smallest z
     */
    double getZMin() {
        return this.zMin;
    }

    /**
     * Returns the largest z
     */
    double getZMax() {
        return this.zMax;
    }

    /**
     * Returns the smallest c
     */
    double getCMin() {
        return this.cMin;
    }

    /**
     * Returns the largest c
     */
    double getCMax() {
        return this.cMax;
    }

    /**
     * Returns an array with the smallest and largest value for the parameter.
     * 
     * @param parameter the parameter to find min and max for.
     * @return {min, max} or {NaN, NaN} if no such parameter in data.
     */
    double[] findParameterSpan(Parameter parameter) {
        int j = getMatrix().getColumnMap().getIndex(parameter);
        if (j != -1) {
            double[] res = new double[] { Double.POSITIVE_INFINITY,
                    Double.NEGATIVE_INFINITY };
            double[][] data = getMatrix().getMatrix();
            for (int i = 0; i < data.length; i++) {
                if (data[i][j] < res[0]) {
                    res[0] = data[i][j];
                }
                if (data[i][j] > res[1]) {
                    res[1] = data[i][j];
                }
            }

            return res;
        } else {
            return new double[] { Double.NaN, Double.NaN };
        }
    }

    /**
     * Updates span if new span is outside current span, if so increase span by 20 % over and under the new value. If the span is smaller
     * than 20% over and under, decreases until exactly 20% over and under new value.
     * 
     * @param span the new span
     * @param oldMin the old min
     * @param oldMax the old max
     * @return new span.
     */
    double[] stepIncreaseSpan(double[] span, double oldMin, double oldMax) {
        double[] res = new double[] { span[0], span[1] };
        double increase = .5;

        // Only update if new any sample outside current span, if so
        // increase span by 20 % over and under the new value

        if (oldMin > span[0]) {
            res[0] = span[0] - (span[1] - span[0]) * increase;
        }

        if (oldMax < span[1]) {
            res[1] = span[1] + (span[1] - span[0]) * increase;
        }

        return res;
    }

    /**
     * Sets the min and max for the x parameter.
     */
    void setXSpan() {
        double[] span = stepIncreaseSpan(findParameterSpan(getXParameter()),
                                         getXMin(), getXMax());
        this.xMin = span[0];
        this.xMax = span[1];
    }

    /**
     * Sets the min and max for the y parameter.
     */
    void setYSpan() {
        double[] span = stepIncreaseSpan(findParameterSpan(getYParameter()),
                                         getYMin(), getYMax());
        this.yMin = span[0];
        this.yMax = span[1];
    }

    /**
     * Sets the min and max for the z parameter.
     */
    void setZSpan() {
        double[] span = stepIncreaseSpan(findParameterSpan(getZParameter()),
                                         getZMin(), getZMax());
        this.zMin = span[0];
        this.zMax = span[1];
    }

    /**
     * Sets the min and max for the c parameter.
     */
    void setCSpan() {
        double[] span = stepIncreaseSpan(findParameterSpan(getCParameter()),
                                         getCMin(), getCMax());
        this.cMin = span[0];
        this.cMax = span[1];
    }

    /**
     * Indicates whether to plot the x dimension
     */
    public boolean isEnabledX() {
        return this.plotX;
    }

    /**
     * Indicates whether to plot the y dimension
     */
    public boolean isEnabledY() {
        return this.plotY;
    }

    /**
     * Indicates whether to plot the z dimension
     */
    public boolean isEnabledZ() {
        return this.plotZ;
    }

    /**
     * Sets whether to plot the x dimension
     * 
     * @param flag true if the x dimension should be enabled
     */
    public void setEnableX(boolean flag) {
        if (flag != isEnabledX()) {
            this.plotX = flag;
            update();
        }
    }

    /**
     * Sets whether to plot the y dimension
     * 
     * @param flag true if the y dimension should be enabled
     */
    public void setEnableY(boolean flag) {
        if (flag != isEnabledY()) {
            this.plotY = flag;
            update();
        }
    }

    /**
     * Sets whether to plot the z dimension
     * 
     * @param flag true if the z dimension should be enabled
     */
    public void setEnableZ(boolean flag) {
        if (flag != isEnabledX()) {
            this.plotZ = flag;
            update();
        }
    }

    /**
     * Returns the grid from which to draw the plot.
     */
    protected double[] getGrid() {
        return this.grid;
    }

    /**
     * Updates the grid
     */
    protected double[] updateGrid() {
        if (getGrid() == null) {
            this.grid = new double[4 * (xTiles + 1) * (yTiles + 1)];
            updateGrid(xTiles, yTiles, grid);
            return getGrid();
        } else {
            updateGrid(xTiles, yTiles, getGrid());
            return getGrid();
        }
    }

    /**
     * Fills in a two-dimensional grid of specified resolution within [getXMin(), getXMax()] and [getYMin(), getYMax()].
     * 
     * @param xTiles number of grid cells along the x-axis.
     * @param yTiles number of grid cells along the z-axis.
     * @param grid the array to fill in, if (xTiles + 1) * (yTiles + 1) * 4 > grid.length the behaviour is undefined. The grid will be
     *            filled as follows: Point n: grid[n + 0] x component grid[n + 1] y component grid[n + 2] z component grid[n + 3] color
     *            component The points will be sorted on the x component and the z component (in that order).
     */
    protected double[] updateGrid(int xTiles, int yTiles, double[] grid) {
        // Make sure axes are enabled and that there is a metric for
        // each parameter.
        if (getXParameter() == null || getYParameter() == null
                || getZParameter() == null || getCParameter() == null) {
            return new double[0];
        }

        // Get a local reference to the matrix.
        double[][] samples = getMatrix().getMatrix();

        // Find the distance between each grid point
        double xRes = (getXMax() - getXMin()) / ((double) xTiles);
        double yRes = (getYMax() - getYMin()) / ((double) yTiles);

        // Construct arguments to interpolator.
        int xIndex = getMatrix().getColumnMap().getIndex(getXParameter());
        int yIndex = getMatrix().getColumnMap().getIndex(getYParameter());
        int zIndex = getMatrix().getColumnMap().getIndex(getZParameter());
        int cIndex = getMatrix().getColumnMap().getIndex(getCParameter());

        int[] iIndices = null;
        // Color and z axis are interpolated, but only if they not
        // coincide with each other or any tiling parameter (i. e x or
        // y)
        boolean interpolateC = (cIndex != xIndex) && (cIndex != yIndex)
                && (cIndex != zIndex);
        boolean interpolateZ = (zIndex != xIndex) && (zIndex != yIndex);

        if (interpolateZ && !interpolateC) {
            iIndices = new int[] { zIndex };
        } else if (interpolateZ && interpolateC) {
            if (zIndex < cIndex) {
                iIndices = new int[] { zIndex, cIndex };
            } else {
                iIndices = new int[] { cIndex, zIndex };
            }
        } else if (!interpolateZ && interpolateC) {
            iIndices = new int[] { cIndex };
        } else { // !interpolateZ && !interpolateC
            iIndices = new int[0];
        }

        // Get a copy of the point to use.
        double[] values = new double[getParameterSlice().length];
        System.arraycopy(getParameterSlice(), 0, values, 0, values.length);

        for (int i = 0; i < xTiles + 1; i++) {
            double x = getXMin() + ((double) i) * xRes;
            for (int j = 0; j < yTiles + 1; j++) {
                double y = getYMin() + ((double) j) * yRes;

                // note that interpolator currently changes values
                // directly.
                values[yIndex] = y;
                values[xIndex] = x;
                double[] point = Interpolator.interpolate(samples, iIndices,
                                                          values);
                grid[4 * (i * (yTiles + 1) + j) + 0] = x;
                grid[4 * (i * (yTiles + 1) + j) + 1] = y;
                grid[4 * (i * (yTiles + 1) + j) + 2] = point[zIndex];
                grid[4 * (i * (yTiles + 1) + j) + 3] = point[cIndex];
            }
        }

        return grid;
    }

    /**
     * Called by the drawable during the first repaint after the component has been resized.
     * 
     * @param drawable the drawable
     * @param x the horizontal position of the drawable.
     * @param y the vertical position of the drawable.
     * @param width the new width of the drawable.
     * @param height the new height of the drawable.
     */
    public void reshape(GLAutoDrawable drawable, int x, int y, int width,
            int height) {}

    /**
     * Called by the drawable when the display mode or the display device associated with the GLAutoDrawable has changed.
     * 
     * @param drawable
     * @param modeChanged
     * @param deviceChanged
     */
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
            boolean deviceChanged) {
        // This is noted as unimplemented in the jogl documentation,
        // so we do nothing.
    }

    /**
     * Called by the drawable immediately after the OpenGL context is initialized.
     * 
     * @param drawable the drawable to init.
     */
    public void init(GLAutoDrawable drawable) {
        GL2 gl = (GL2) drawable.getGL();

        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // enable smoothing for lines
        gl.glEnable(GL2.GL_LINE_SMOOTH);

        // Enable auto normalizing.
        gl.glEnable(GL2.GL_NORMALIZE);

        // enable shading
        gl.glShadeModel(GL2.GL_SMOOTH);

        // enable depth buffering
        gl.glEnable(GL2.GL_DEPTH_TEST);

        // enable blending
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

        // Enable material color tracking
        gl.glColorMaterial(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE);
        gl.glEnable(GL2.GL_COLOR_MATERIAL);
    }

    /**
     * Called by the drawable to initiate OpenGL rendering by the client.
     * 
     * @param drawable the drawable to display.
     */
    public void display(GLAutoDrawable drawable) {
        GL2 gl = (GL2) drawable.getGL();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
        gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);

        // Update view bounds
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45.0d, 1.0d, 0.01d, 10000.0d);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();

        // Set current view
        getCamera().setView(glu);

        // Enable lighting (or not).
        if (isLightingEnabled()) {
            gl.glLightModeli(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, 1);
            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, new float[] { 0.5f,
                    0.5f, 0.5f, 0.5f }, 0);
            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION,
                         new float[] { (float) getXMax(), (float) getYMax(),
                                 (float) getZMax(), 0f }, 0);

            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPOT_DIRECTION, new float[] {
                    0.0f, 0.0f, 0.0f }, 0);
            gl.glLightf(GL2.GL_LIGHT0, GL2.GL_LINEAR_ATTENUATION, 0.05f);
            gl.glLightf(GL2.GL_LIGHT0, GL2.GL_CONSTANT_ATTENUATION, 0.1f);
            gl.glEnable(GL2.GL_LIGHT0);
            gl.glEnable(GL2.GL_LIGHTING);
        } else {
            gl.glDisable(GL2.GL_LIGHTING);
        }

        // Recompile updated parts.
        if (!isBoxListUpdated()) {
            recompileBox(drawable);
        }
        if (!isAxesListUpdated()) {
            recompileAxes(drawable);
        }
        if (!isSamplesListUpdated()) {
            recompileSamples(drawable);
        }
        if (!isInterpolationListUpdated()) {
            recompileInterpolation(drawable);
        }

        // Call lists
        gl.glCallLists(topDisplayListsBuf.capacity(), GL2.GL_INT,
                       topDisplayListsBuf);

        // Force rendering
        gl.glFlush();
    }

    /**
     * Recompiles the list that draws the samples of the plot.
     * 
     * @param drawable the drawable on which to draw
     */
    public void recompileSamples(GLAutoDrawable drawable) {
        GL2 gl = (GL2) drawable.getGL();

        topDisplayListsBuf
                .put(SAMPLES_DISPLAY_LIST_POS,
                     (gl.glIsList(topDisplayListsBuf
                             .get(SAMPLES_DISPLAY_LIST_POS))) ? topDisplayListsBuf
                             .get(SAMPLES_DISPLAY_LIST_POS) : gl.glGenLists(1));

        double[][] data = getMatrix().getMatrix();
        int xIndex = getMatrix().getColumnMap().getIndex(getXParameter());
        int yIndex = getMatrix().getColumnMap().getIndex(getYParameter());
        int zIndex = getMatrix().getColumnMap().getIndex(getZParameter());
        int cIndex = getMatrix().getColumnMap().getIndex(getCParameter());

        // Start list
        gl.glNewList(topDisplayListsBuf.get(SAMPLES_DISPLAY_LIST_POS),
                     GL2.GL_COMPILE);
        if (drawsSamples()) {
            gl.glColor4d(0.7, 0.7, 0.7, 1.0);
            for (int i = 0; i < data.length; i++) {
                double x = data[i][xIndex];
                double y = data[i][yIndex];
                double z = data[i][zIndex];
                double sz = .1;

                gl.glBegin(GL2.GL_QUADS);
                gl.glVertex3d(x - sz, y - sz, z - sz);
                gl.glVertex3d(x - sz, y - sz, z + sz);
                gl.glVertex3d(x - sz, y + sz, z + sz);
                gl.glVertex3d(x - sz, y + sz, z - sz);

                gl.glVertex3d(x + sz, y - sz, z - sz);
                gl.glVertex3d(x + sz, y - sz, z + sz);
                gl.glVertex3d(x + sz, y + sz, z + sz);
                gl.glVertex3d(x + sz, y + sz, z - sz);

                gl.glVertex3d(x - sz, y - sz, z - sz);
                gl.glVertex3d(x - sz, y - sz, z + sz);
                gl.glVertex3d(x + sz, y - sz, z + sz);
                gl.glVertex3d(x + sz, y - sz, z - sz);

                gl.glVertex3d(x - sz, y + sz, z - sz);
                gl.glVertex3d(x - sz, y + sz, z + sz);
                gl.glVertex3d(x + sz, y + sz, z + sz);
                gl.glVertex3d(x + sz, y + sz, z - sz);

                gl.glVertex3d(x - sz, y - sz, z - sz);
                gl.glVertex3d(x - sz, y + sz, z - sz);
                gl.glVertex3d(x + sz, y + sz, z - sz);
                gl.glVertex3d(x + sz, y - sz, z - sz);

                gl.glVertex3d(x - sz, y - sz, z + sz);
                gl.glVertex3d(x - sz, y + sz, z + sz);
                gl.glVertex3d(x + sz, y + sz, z + sz);
                gl.glVertex3d(x + sz, y - sz, z + sz);
                gl.glEnd();
            }
        }
        if (drawsTrace() && data.length >= 2) {
            double arrowLength = 0.4d;
            double arrowSpread = 0.2d;
            gl.glColor4d(0.9, 0.9, 0.9, 1.0);

            double[] start = new double[4];
            double[] end = new double[] { data[0][xIndex], data[0][yIndex],
                    data[0][zIndex], data[0][cIndex], };

            for (int i = 1; i < data.length; i++) {
                System.arraycopy(end, 0, start, 0, start.length);
                end[0] = data[i][xIndex];
                end[1] = data[i][yIndex];
                end[2] = data[i][zIndex];
                end[3] = data[i][cIndex];
                drawArrow(gl, start, end, arrowLength, arrowSpread);
            }
        }

        gl.glEndList();
        setIsSamplesListUpdated(true);
    }

    /**
     * Recompiles the list that draws the axes of the plot.
     * 
     * @param drawable the drawable on which to draw
     */
    public void recompileAxes(GLAutoDrawable drawable) {
        GL2 gl = (GL2) drawable.getGL();

        topDisplayListsBuf
                .put(AXES_DISPLAY_LIST_POS,
                     (gl.glIsList(topDisplayListsBuf.get(AXES_DISPLAY_LIST_POS))) ? topDisplayListsBuf
                             .get(AXES_DISPLAY_LIST_POS) : gl.glGenLists(1));

        // Start list
        gl.glNewList(topDisplayListsBuf.get(AXES_DISPLAY_LIST_POS),
                     GL2.GL_COMPILE);
        if (drawsAxes() && getXParameter() != null && getYParameter() != null
                && getZParameter() != null) {
            GLUT glut = new GLUT();

            gl.glColor4d(0.7, 0.7, 0.7, 1.0);

            double arrowLength = 0.6d;
            double arrowSpread = 0.3d;
            drawArrow(gl, new double[] { getXMin(), 0.0d, 0.0d }, new double[] {
                    getXMax(), 0.0d, 0.0d }, arrowLength, arrowSpread);
            drawBanner(gl, glut, new double[] { getXMax(), 0.0d, 0.0d },
                       getXParameter().getName());

            drawArrow(gl, new double[] { 0.0d, getYMin(), 0.0d }, new double[] {
                    0.0d, getYMax(), 0.0d }, arrowLength, arrowSpread);
            drawBanner(gl, glut, new double[] { 0.0d, getYMax(), 0.0d },
                       getYParameter().getName());

            drawArrow(gl, new double[] { 0.0d, 0.0d, getZMin() }, new double[] {
                    0.0d, 0.0d, getZMax() }, arrowLength, arrowSpread);
            drawBanner(gl, glut, new double[] { 0.0d, 0.0d, getZMax() },
                       getZParameter().getName());
        }

        gl.glEndList();
        setIsAxesListUpdated(true);
    }

    /**
     * Recompiles the list that draws the box at the minimum of the plot.
     * 
     * @param drawable the drawable on which to draw
     */
    public void recompileBox(GLAutoDrawable drawable) {
        GL2 gl = (GL2) drawable.getGL();

        topDisplayListsBuf
                .put(BOX_DISPLAY_LIST_POS,
                     (gl.glIsList(topDisplayListsBuf.get(BOX_DISPLAY_LIST_POS))) ? topDisplayListsBuf
                             .get(BOX_DISPLAY_LIST_POS) : gl.glGenLists(1));

        // Start list
        gl.glNewList(topDisplayListsBuf.get(BOX_DISPLAY_LIST_POS),
                     GL2.GL_COMPILE);

        if (drawsBox()) {
            gl.glBegin(GL2.GL_QUADS);
            gl.glNormal3d(1.0d, 0.0d, 0.0d);
            gl.glColor4d(0.0, 0.0, 0.4, 1.0);
            gl.glVertex3d(getXMin(), getYMax(), getZMin());
            gl.glColor4d(0.0, 0.0, 1.0, 1.0);
            gl.glVertex3d(getXMin(), getYMax(), getZMax());
            gl.glColor4d(0.0, 0.0, 0.4, 1.0);
            gl.glVertex3d(getXMin(), getYMin(), getZMax());
            gl.glColor4d(0.0, 0.0, 0.2, 1.0);
            gl.glVertex3d(getXMin(), getYMin(), getZMin());

            gl.glNormal3d(0.0d, 1.0d, 0.0d);
            gl.glColor4d(0.0, 0.2, 0.0, 1.0);
            gl.glVertex3d(getXMin(), getYMin(), getZMin());
            gl.glColor4d(0.0, 0.4, 0.0, 1.0);
            gl.glVertex3d(getXMin(), getYMin(), getZMax());
            gl.glColor4d(0.0, 1.0, 0.0, 1.0);
            gl.glVertex3d(getXMax(), getYMin(), getZMax());
            gl.glColor4d(0.0, 0.4, 0.0, 1.0);
            gl.glVertex3d(getXMax(), getYMin(), getZMin());

            gl.glNormal3d(0.0d, 0.0d, 1.0d);
            gl.glColor4d(0.2, 0.0, 0.0, 1.0);
            gl.glVertex3d(getXMin(), getYMin(), getZMin());
            gl.glColor4d(0.4, 0.0, 0.0, 1.0);
            gl.glVertex3d(getXMax(), getYMin(), getZMin());
            gl.glColor4d(1.0, 0.0, 0.0, 1.0);
            gl.glVertex3d(getXMax(), getYMax(), getZMin());
            gl.glColor4d(0.4, 0.0, 0.0, 1.0);
            gl.glVertex3d(getXMin(), getYMax(), getZMin());

            gl.glEnd();
        }

        gl.glEndList();
        setIsBoxListUpdated(true);
    }

    /**
     * Draws a banner starting at the specified location
     * 
     * @param gl2 the gl to draw on.
     * @param glut the glut to use
     * @param start the point to start at.
     * @param string the string to draw.
     */
    protected void drawBanner(GL gl2, GLUT glut, double[] start, String string) {
        GL2 gl = (GL2) gl2;
        // Wash string, replace unknowns with '_';
        double textScale = 0.5 / (119.05 + 33.33);
        try {
            byte[] bytes = string.getBytes("ISO-8859-1");
            for (int k = 0; k < bytes.length; k++) {
                if (bytes[k] < 32 || bytes[k] > 127) {
                    bytes[k] = 95;
                }
            }
            String washed = new String(bytes, "US-ASCII");

            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glPushMatrix();
            double[] matrix = new double[16];
            gl.glGetDoublev(GL2.GL_MODELVIEW_MATRIX, matrix, 0);

            gl.glTranslated(start[0], start[1], start[2]);
            gl.glScaled(textScale, textScale, textScale);
            glut.glutStrokeString(GLUT.STROKE_MONO_ROMAN, washed);
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glPopMatrix();
        } catch (UnsupportedEncodingException ex) {
            System.err.println(ex.toString());
        }
    }

    /**
     * Draws an arrow between two points
     * 
     * @param gl2 the gl to draw on.
     * @param start the start of the arrow
     * @param end the end of the arrow (the pointy end)
     * @param arrowLength the length of the arrow
     * @param arrowSpread the spread of the arrow.
     */
    protected void drawArrow(GL gl2, double[] start, double[] end,
            double arrowLength, double arrowSpread) {
        GL2 gl = (GL2) gl2;
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3dv(start, 0);
        gl.glVertex3dv(end, 0);
        gl.glEnd();

        double[] v = { end[0] - start[0], end[1] - start[1], end[2] - start[2] };

        // Rotate model to make the provided line the new x-axis. Use
        // V x X to get a vector orthogonal to both, use that as
        // rotational axis. Since X = [1, 0, 0] some simplifications
        // can be made.

        double[] r = { 0.0d, // x[1]*v[2] - x[2]*v[1] = 0.0d
                v[0] - v[2], // x[0]*v[0] - x[0]*v[2]
                v[1] // x[0]*v[1] - x[1]*v[0]
        };

        // Find rotational angle
        double vNorm = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        double angle = Math.toDegrees(Math.acos(v[0] / vNorm));

//         Debug.err.println("glRotated(" + angle + ", " + r[0] + ", " + 
//                           r[1] + ", " + r[2] + ")");

//         Debug.err.println("From: " + 
//                           start[0] + ", " +  
//                           start[1] + ", " +  
//                           start[2]);

//         Debug.err.println("To: " + 
//                           end[0] + ", " +  
//                           end[1] + ", " +  
//                           end[2]);

//         Debug.err.println("Angle, norm: " + 
//                           angle + ", " +  
//                           vNorm);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glTranslated(start[0], start[1], start[2]);
        gl.glRotated(angle, r[0], r[1], r[2]);
        gl.glTranslated(vNorm, 0.0, 0.0);
        gl.glBegin(GL2.GL_TRIANGLES);
        gl.glVertex3d(-arrowLength, arrowLength * arrowSpread, arrowLength
                * arrowSpread);
        gl.glVertex3d(-arrowLength, -arrowLength * arrowSpread, arrowLength
                * arrowSpread);
        gl.glVertex3d(0.0d, 0.0d, 0.0d);

        gl.glVertex3d(-arrowLength, -arrowLength * arrowSpread, arrowLength
                * arrowSpread);
        gl.glVertex3d(-arrowLength, -arrowLength * arrowSpread, -arrowLength
                * arrowSpread);
        gl.glVertex3d(0.0d, 0.0d, 0.0d);

        gl.glVertex3d(-arrowLength, -arrowLength * arrowSpread, -arrowLength
                * arrowSpread);
        gl.glVertex3d(-arrowLength, arrowLength * arrowSpread, -arrowLength
                * arrowSpread);
        gl.glVertex3d(0.0d, 0.0d, 0.0d);

        gl.glVertex3d(-arrowLength, arrowLength * arrowSpread, -arrowLength
                * arrowSpread);
        gl.glVertex3d(-arrowLength, arrowLength * arrowSpread, arrowLength
                * arrowSpread);
        gl.glVertex3d(0.0d, 0.0d, 0.0d);

        gl.glEnd();
        gl.glPopMatrix();

    }

    /**
     * Recompiles the list that draws the interpolation of the plot.
     * 
     * @param drawable the drawable on which to draw
     */
    protected void recompileInterpolation(GLAutoDrawable drawable) {
        GL2 gl = (GL2) drawable.getGL();

        topDisplayListsBuf
                .put(INTERPOLATION_DISPLAY_LIST_POS,
                     (gl.glIsList(topDisplayListsBuf
                             .get(INTERPOLATION_DISPLAY_LIST_POS))) ? topDisplayListsBuf
                             .get(INTERPOLATION_DISPLAY_LIST_POS) : gl
                             .glGenLists(1));

        // Start list
        gl.glNewList(topDisplayListsBuf.get(INTERPOLATION_DISPLAY_LIST_POS),
                     GL2.GL_COMPILE);

        if (drawsInterpolation()) {
            double[] grid = getGrid();
            if (grid != null) {
                for (int i = 0; i < xTiles; i++) {
                    gl.glBegin(GL2.GL_TRIANGLE_STRIP);
                    for (int j = 0; j < yTiles + 1; j++) {
                        int p1Index = 4 * (i * (yTiles + 1) + j);
                        int p2Index = 4 * ((i + 1) * (yTiles + 1) + j);

                        gl.glColor4dv(colorMapper.map(grid[p1Index + 3]), 0);
                        gl.glVertex3d(grid[p1Index + 0], grid[p1Index + 1],
                                      grid[p1Index + 2]);
                        gl.glColor4dv(colorMapper.map(grid[p2Index + 3]), 0);
                        gl.glVertex3d(grid[p2Index + 0], grid[p2Index + 1],
                                      grid[p2Index + 2]);
                    }
                    gl.glEnd();
                }
            }
        }
        if (drawsInterpolationOutline()) {
            if (grid != null) {
                int[] oldMode = new int[2];
                gl.glGetIntegerv(GL2.GL_POLYGON_MODE, oldMode, 0);

                gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
                gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_LINE);
                gl.glPolygonMode(GL2.GL_BACK, GL2.GL_LINE);

                for (int i = 0; i < xTiles; i++) {
                    gl.glBegin(GL2.GL_TRIANGLE_STRIP);
                    for (int j = 0; j < yTiles + 1; j++) {
                        int p1Index = 4 * (i * (yTiles + 1) + j);
                        int p2Index = 4 * ((i + 1) * (yTiles + 1) + j);

                        gl.glColor4dv(colorMapper.map(grid[p1Index + 3]), 0);
                        gl.glVertex3d(grid[p1Index + 0], grid[p1Index + 1],
                                      grid[p1Index + 2]);
                        gl.glColor4dv(colorMapper.map(grid[p2Index + 3]), 0);
                        gl.glVertex3d(grid[p2Index + 0], grid[p2Index + 1],
                                      grid[p2Index + 2]);
                    }
                    gl.glEnd();
                }
                gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, oldMode[1]);
                gl.glPolygonMode(GL2.GL_FRONT, oldMode[0]);
                gl.glPolygonMode(GL2.GL_BACK, oldMode[0]);
            }
        }

        gl.glEndList();
        setIsInterpolationListUpdated(true);
    }

    /**
     * Called when the plot needs to be redrawn.
     */
    public void update() {
        if (gld != null) {
            ((GLCanvas) gld).repaint();
        }
    }

    /**
     * Called when the plot needs to be redrawn. Will redraw within specified time.
     * 
     * @param tm maximum time in milliseconds before update
     */
    public void update(long tm) {
        if (gld != null) {
            ((GLCanvas) gld).repaint(tm);
        }
    }

    public void dispose(GLAutoDrawable glad) {
        // TODO implement
    }
}
