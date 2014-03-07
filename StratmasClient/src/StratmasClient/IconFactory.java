//         $Id: IconFactory.java,v 1.22 2006/10/02 16:11:55 alexius Exp $
/*
 * @(#)IconFactory.java
 */

package StratmasClient;

import StratmasClient.object.type.Type;
import StratmasClient.symbolloader.SymbolLoader;
import StratmasClient.object.SymbolIDCode;
import StratmasClient.object.StratmasList;
import StratmasClient.object.StratmasObject;
import StratmasClient.filter.OrderColorFilter;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageFilter;
import java.awt.image.FilteredImageSource;
import java.util.Hashtable;
import java.lang.ref.WeakReference;

/**
 * IconFactory is a mapping of different StratmasObjects to
 * appropriate Icons.
 *
 * @version 1, $Date: 2006/10/02 16:11:55 $
 * @author  Daniel Ahlin
*/

public class IconFactory
{
    /**
     * Default icon for leafs;
     */
    static Icon leafIcon = new Icon(IconFactory.class.getResource("icons/leaf.png"));

    /**
     * Default icon for nonleafs;
     */
    static Icon nonLeafIcon = new Icon(IconFactory.class.getResource("icons/nonleaf.png"));

    /**
     * A static mapping of icons to types.
     */
    static Hashtable typeMapping;

    /**
     * A list of images for the activity symbols.
     */
    static Hashtable activitySymbols = new Hashtable();

    /**
     * A loader of app6a icons.
     */    
    static SymbolLoader symbolLoader = new SymbolLoader("app6a", "/App6A", 30);

    /**
     * Returns an icon representing the provided object.
     *
     * @param object the StratmasObject to get the icon for.
     */
    public static Icon getIcon(StratmasObject object)
    {
        Icon res = null;
        
        // Try symbolLoader first:
        res = useSymbolLoader(object);
        if (res != null) {
            return res;
        }

        // Next, try type loader;
        res = useTypeMapping(object.getType());
        if (res != null) {
            //set color on orders
            if (object.getType().canSubstitute("Activity")) {
                StratmasObject mu = null;
                if (object.getType().canSubstitute("Order")) {
                    StratmasObject aList = object.getParent();
                    mu = (aList != null) ? aList.getParent() : null;
                }
                else if (object instanceof StratmasList) {
                    mu = object.getParent();
                }
                if (mu != null) {
                    return getActivityIcon((SymbolIDCode)mu.getChild("symbolIDCode"), res.getImage());
                }
            }
            return res;
        }

        // If no luck, use default, differing only on nonleafs and leafs
        if (object.isLeaf()) {
            return leafIcon;
        } else {
            return nonLeafIcon;
        }
    }

    /**
     * If necessary creates the static type->icon mapping.
     */   
    static void createTypeMapping()
    {
        if (typeMapping == null) {
            typeMapping = new Hashtable();

            typeMapping.put("CommonSimulation", 
                            new Icon(IconFactory.class.getResource("icons/simulation.png")));
            typeMapping.put("Simulation", 
                            new Icon(IconFactory.class.getResource("icons/simulation.png")));
            typeMapping.put("CustomPVModification", 
                            new Icon(IconFactory.class.getResource("icons/activity.png")));
            typeMapping.put("Activity", 
                            new Icon(IconFactory.class.getResource("icons/activity.png")));
            typeMapping.put("Population", 
                            new Icon(IconFactory.class.getResource("icons/city.png")));
            typeMapping.put("MilitaryUnit", 
                            new Icon(IconFactory.class.getResource("icons/militaryunit.png")));
            typeMapping.put("HealthAgencyTeam", 
                            new Icon(IconFactory.class.getResource("icons/health.png")));
            typeMapping.put("PoliceAgencyTeam", 
                            new Icon(IconFactory.class.getResource("icons/police.png")));
            typeMapping.put("ShelterAgencyTeam", 
                            new Icon(IconFactory.class.getResource("icons/shelter.png")));
            typeMapping.put("WaterAgencyTeam", 
                            new Icon(IconFactory.class.getResource("icons/water.png")));
            typeMapping.put("FoodAgencyTeam", 
                            new Icon(IconFactory.class.getResource("icons/food.png")));
            typeMapping.put("CustomAgencyTeam", 
                            new Icon(IconFactory.class.getResource("icons/customagencyteam.png")));
            typeMapping.put("StratmasCityDistribution", 
                            new Icon(IconFactory.class.getResource("icons/stratmascitydistribution.png")));
            typeMapping.put("UniformDistribution", 
                            new Icon(IconFactory.class.getResource("icons/uniformdistribution.png")));
            typeMapping.put("Curve", 
                            new Icon(IconFactory.class.getResource("icons/curve.png")));
            typeMapping.put("Point", 
                            new Icon(IconFactory.class.getResource("icons/point.png")));
            typeMapping.put("Disease", 
                            new Icon(IconFactory.class.getResource("icons/disease.png")));
            typeMapping.put("GridPartitioner", 
                            new Icon(IconFactory.class.getResource("icons/grid.png")));
            typeMapping.put("NormalDistribution", 
                            new Icon(IconFactory.class.getResource("icons/stratmascitydistribution.png")));
            typeMapping.put("TimeStepper", 
                            new Icon(IconFactory.class.getResource("icons/timestepper.png")));
            typeMapping.put("Scenario", 
                            new Icon(IconFactory.class.getResource("icons/scenario.png")));
            typeMapping.put("Circle", 
                            new Icon(IconFactory.class.getResource("icons/circle.png")));
            typeMapping.put("Shape", 
                            new Icon(IconFactory.class.getResource("icons/shape.png")));
        }
    }

    /**
     * Returns an icon representing the type of the provided object
     * (or null if no such found)..
     *
     * @param type the type to get the icon for.
     */
    public static Icon useTypeMapping(Type type)
    {
        createTypeMapping();
        Icon res = (Icon) typeMapping.get(type.getName());

        // Next, try to match against the types supertype (only
        // necessary to check direct parent since this is a
        // reccurance.)
        if (res == null && type.getBaseType() != null && 
            !type.getBaseType().getName().equals("anyType")) {
            res =  useTypeMapping(type.getBaseType());
        }

        return res;
    }

    /**
     * Returns an app6a icon representing the provided object
     * (or null if no such found)..
     *
     * @param object the StratmasObject to get the icon for.
     */
    public static Icon useSymbolLoader(StratmasObject object)
    {
        if (object.getChild("symbolIDCode") != null) {
            String symbolString = ((SymbolIDCode)object.getChild("symbolIDCode")).valueToString();
            BufferedImage candidate = null;
            synchronized (symbolLoader) {
                candidate = symbolLoader.load(symbolString);
            }
            if (candidate != null) {
                return new Icon(candidate);
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
        
    }

    /**
     * Returns an app6a icon representing the provided symbolIDCode
     * (or null if no such found)..
     *
     * @param symbolIDCode The code to get the icon for.
     */
    public static Icon useSymbolLoader(String symbolIDCode) {
         BufferedImage candidate = null;
         synchronized (symbolLoader) {
              candidate = symbolLoader.load(symbolIDCode);
         }
         if (candidate != null) {
              return new Icon(candidate);
         }
         else {
              return null;
         }
    }

    /**
     * Returns the leaf icon.
     */
    public static Icon getLeafIcon() {
        return leafIcon;
    }

    /**
     * Returns the activity icon.
     * OBS. Potential memory leak - there is a risk that the keys and weak references in 
     * activitySymbols list are not collected by the garbage collector. 
     *
     * @param code the symbolIdCode.
     * @param src the image of the activity.
     */
    public static Icon getActivityIcon(SymbolIDCode code, Image src) {
        int affChar = (code == null || code.valueToString().length() < 2) ? '-' : code.valueToString().charAt(1);
        Integer key = new Integer(affChar);
        WeakReference reference = (WeakReference) activitySymbols.get(key);        
        Icon icon = null;
        if (reference != null) {
            icon = (Icon) reference.get();
        }
        if (icon != null) {
            return icon;  
        }
        else {
            // get the color of the resource
            Color muColor = getSymbolBackgroundColor(code);
            // set the color to the order
            ImageFilter colorFilter = OrderColorFilter.getFilter(muColor);
            Icon newIcon = new Icon(Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(src.getSource(),colorFilter)));
            activitySymbols.put(key, new WeakReference(newIcon));
            return newIcon;
        }
    }
    
    
    /**
     * Returns the correct color for each App6A affiliation
     *
     * @param code the symbolIdCode
     */
    public static Color getSymbolBackgroundColor(SymbolIDCode code)
    {
        String affString = null;

        if (code == null || 
            code.valueToString().length() < 2) {
            affString = "-";
        } else {
            affString = code.valueToString().substring(1, 2);
        }

        if (affString.equals("A") || affString.equals("F")) {
            // Friend / Assumed Friend
            return new Color(128, 224, 255);
        } else if (affString.equals("U") || affString.equals("P")) {
            // Unknown / Pending
            return new Color(255, 255, 128);
        } else if (affString.equals("S") || affString.equals("H") ||
                   affString.equals("J") || affString.equals("K")) {
            // Suspect / Hostile / Joker / Faker
            return new Color (255, 128, 128);
        } else if (affString.equals("N")) {
            // Neutral
            return new Color (170, 255, 170);
        } else {
            // None specied (not specified in the standard either, make it light gray)
            return new Color (40, 40, 40);
        }
    }
    
}



