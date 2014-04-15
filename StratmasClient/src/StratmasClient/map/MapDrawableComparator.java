package StratmasClient.map;

import java.util.Comparator;

import StratmasClient.map.adapter.MapDrawableAdapter;
import StratmasClient.map.adapter.MapElementAdapter;
import StratmasClient.map.adapter.MapShapeAdapter;
import StratmasClient.map.adapter.MapLineAdapter;
import StratmasClient.map.adapter.MapPointAdapter;
import StratmasClient.object.type.Type;

/**
 * MapDrawableComparator is used to compare adapters drawn in the map.
 */
public class MapDrawableComparator implements Comparator<MapDrawableAdapter> {
    
    /**
     * Returns -1, 0, 1 if d1 is less than, equal to or greater than d2.
     *
     * Order as follows (most significant discriminator to least significant):
     * Selection:  {@code unSelected < Selected}
     * Type:       {@code Shape < Line < Point < Population <
     *  AgencyTeam < MilitaryUnit < Activity}
     * Hiearchy:    {@code child < parent}
     * Identifier:  {@code d1.getIdentifier.toString()
     *  .compare(d2.getStratmasObject().getIdentifier().toString())}
     *
     * Note that the type discriminator takes inheritance into
     * account, meaning that any derived type of Population,
     * AgencyTeam, MilitaryUnit, Activity and Shape is treated as above.
     * 
     * 
     * @param d1 the first object to be compared.
     * @param d2 the second object to be compared.
     */            
    public int compare(MapDrawableAdapter d1, MapDrawableAdapter d2)
    {
    	//TODO fix: in java 7 it throws "Comparsion method violates its general contract"
        if (d1 instanceof MapPointAdapter) {
            if (d2 instanceof MapPointAdapter) {
                return 0;
            }
            return 1;
        }
        else if (d2 instanceof MapPointAdapter) {
            return -1;
        }
        else if (d1 instanceof MapLineAdapter) {
            if (d2 instanceof MapLineAdapter) {
                return 0;
            }
            return 1;
        }
        else if (d2 instanceof MapLineAdapter) {
            return -1;
        }
        else if (d1 instanceof MapShapeAdapter) {
            if (d2 instanceof MapShapeAdapter) {
                return 0;
            }
            return -1;
        }
        else if (d2 instanceof MapShapeAdapter) {
            return 1;
        }
        else {
            MapElementAdapter a1 = (MapElementAdapter) d1;
            MapElementAdapter a2 = (MapElementAdapter) d2;
            if (a1.isSelected() ^ a2.isSelected()) {
                return a1.isSelected() ? 1 : -1;
            } else if (a1.getStratmasObject().getType().canSubstitute(a2.getStratmasObject().getType()) || 
                       a2.getStratmasObject().getType().canSubstitute(a1.getStratmasObject().getType())) {
                Type t1 = a1.getStratmasObject().getType();
                Type t2 = a2.getStratmasObject().getType();
                if (t1.canSubstitute("Activity")) {
                    if (t2.canSubstitute("Activity")) {
                        return 0;
                    }
                    return 1;
                } else if (t2.canSubstitute("Activity")) {
                    return -1;
                }else if (t1.canSubstitute("MilitaryUnit")) {
                    if (t2.canSubstitute("MilitaryUnit")) {
                        return 0;
                    }
                    return 1;
                } else if (t2.canSubstitute("MilitaryUnit")) {
                    return -1;
                } else if (t1.canSubstitute("AgencyTeam")) {
                    if (t2.canSubstitute("AgencyTeam")) {
                        return 0;
                    } 
                    return 1;
                } else {
                    return -1;
                }
            } else if (a1.getStratmasObject().isAncestor(a2.getStratmasObject())) {
                return 1;
            } else if (a2.getStratmasObject().isAncestor(a1.getStratmasObject())) {
                return -1;
            }
            else {
                return a1.getStratmasObject().getIdentifier().
                    compareTo(a2.getStratmasObject().getIdentifier());
            }
        }
    }
    
    /**
     * Not implemented.
     */
    public boolean equals(Object obj) {
        return false;
    }
    
}
