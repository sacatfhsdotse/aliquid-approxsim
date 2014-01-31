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
public class MapDrawableComparator implements Comparator {
    
    /**
     * Returns -1, 0, 1 if o1 is less than, equal to or greater than o2.
     *
     * Order as follows (most significant discriminator to least significant):
     * Selection:  unSelected < Selected  
     * Type:       Shape < Line < Point < Population < AgencyTeam < MilitaryUnit < Activity
     * Hiearchy    child < parent
     * Identifier:  o1.getIdentifier.toString().compare(o2.getStratmasObject().getIdentifier().toString)).
     *
     * Note that the type discriminator takes inheritance into
     * account, meaning that any derived type of Population,
     * AgencyTeam, MilitaryUnit, Activity and Shape is treated as above.
     * 
     * 
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     */	    
    public int compare(Object o1, Object o2)
    {
	MapDrawableAdapter d1 = (MapDrawableAdapter) o1;
	MapDrawableAdapter d2 = (MapDrawableAdapter) o2;
	if (d1 instanceof MapPointAdapter) {
	    return 1;
	}
	else if (d2 instanceof MapPointAdapter) {
	    return -1;
	}
	else if (d1 instanceof MapLineAdapter) {
	    return 1;
	}
	else if (d2 instanceof MapLineAdapter) {
	    return -1;
	}
	else if (d1 instanceof MapShapeAdapter) {
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
	    } else if (!(a1.getStratmasObject().getType().canSubstitute(a2.getStratmasObject().getType())) || 
		       a2.getStratmasObject().getType().canSubstitute(a1.getStratmasObject().getType())) {
		Type t1 = a1.getStratmasObject().getType();
		Type t2 = a2.getStratmasObject().getType();
		if (t1.canSubstitute("Activity")) {
		    return 1;
		} else if (t2.canSubstitute("Activity")) {
		    return -1;
		}else if (t1.canSubstitute("MilitaryUnit")) {
		    return 1;
		} else if (t2.canSubstitute("MilitaryUnit")) {
		    return -1;
		} else if (t1.canSubstitute("AgencyTeam")) {
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
