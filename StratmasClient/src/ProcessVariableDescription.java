package StratmasClient;

/**
 * Class that represents a description of a process variable.
 *
 * @version 1, $Date: 2007/01/24 14:25:50 $
 * @author Amir Filipovic, Per Alexius
 */
public class ProcessVariableDescription {
    /** Upper limit of the maximum value (10 000 000 000)*/
    public static final double UPPER_LIMIT = 10000000000.0;
    /** name of the process variable */
    private String mName;
    /** The category of the process variable */
    private String mCategory;
    /** indicates if factions exist for this variable */
    private boolean mFactions;
    /** minimum value the variable can have  */
    private double mMin;
    /** maximum value the variable can have */
    private double mMax;     
    /** color map used for the visualization purpose */
    private String colorMap;
    /** scale used for the visualization purpose (linear or logarithmic) */
    private String scale; 
    
    
     /**
      * Creates new process variable description.
      *
      * @param name name of the process variable.
      * @param fac true if factions exist for this variable, otherwise false.
      * @param min minmimum value.
      * @param max maximum value.
      */
     public ProcessVariableDescription(String name, String category, boolean fac, double min, double max) {
          mName     = name;
          mCategory = category;
          mFactions = fac;
          mMin      = min;
          mMax      = (max > UPPER_LIMIT)? UPPER_LIMIT : max;
          scale     = (mMin < 0 || (mMax-mMin) <= 100)? "Linear Scale" : "Logarithmic Scale";
     }
    
    
     /**
      * Returns the name of the variable.
      *
      * @return The name of the PV.
      */
     public String getName() {
          return mName;
     }
    
     /**
      * Returns the category of the variable.
      *
      * @return The category of the PV.
      */
     public String getCategory() {
          return mCategory;
     }
    

     /**
      * Returns true if the variable has factions, otherwise false.
      *
      * @return true if the variable may be split into factions, otherwise false.
      */
     public boolean hasFactions() {
          return mFactions;
     }
    
    
     /**
      * Sets minimum value.
      */
     public void setMin(double min) {
          mMin = min;
     }
    
    
     /**
      * Returns minumum value.
      *
      * @return The minimum value for the PV.
      */
     public double getMin() {
          return mMin;
     }
    
    
     /**
      * Sets maximum value.
      */
     public void setMax(double max) {
          mMax = max;
     }
    
    
     /**
      * Returns maximum value.
      *
      * @return The maximum value for the PV.
      */
     public double getMax() {
          return mMax;
     }
    
     /**
      * Sets color map.
      */
     public void setColorMap(String cmap) {
          colorMap = cmap;
     }
    
    
     /**
      * Returns string representation of the color map.
      */
     public String getColorMap() {
          return colorMap;
     }
    
    
     /**
      * Linear scale is used to scale this pv.
      */
     public void setLinearScale() {
          scale = "Linear Scale";
     }
    
    
     /**
      * Logarithmic scale is used to scale this pv.
      */
     public void setLogarithmicScale() {
          scale = "Logarithmic Scale";
     }

    
     /**
      * Returns the actual scale.
      */
     public String getScale() {
          return scale;
     }

     /**
      * Creates a string representation of this object.
      *
      * @return A string representation of this object.
      */
     public String toString() {
          return mName + " (" + mCategory + ")";
     }
        
}

