package ApproxsimClient.map;

import java.util.Comparator;

import ApproxsimClient.map.adapter.MapDrawableAdapter;
import ApproxsimClient.map.adapter.MapElementAdapter;
import ApproxsimClient.map.adapter.MapShapeAdapter;
import ApproxsimClient.map.adapter.MapLineAdapter;
import ApproxsimClient.map.adapter.MapPointAdapter;
import ApproxsimClient.object.type.Type;

/**
 * MapDrawableComparator is used to compare adapters drawn in the map.
 */
public class MapDrawableComparator implements Comparator<MapDrawableAdapter> {

    /**
     * Returns -1, 0, 1 if d1 is less than, equal to or greater than d2. Order as follows (most significant discriminator to least
     * significant): Selection: {@code unSelected < Selected} Type: {@code Shape < Line < Point < Population <
     *  AgencyTeam < MilitaryUnit < Activity} Hiearchy: {@code child < parent} Identifier: {@code d1.getIdentifier.toString()
     *  .compare(d2.getApproxsimObject().getIdentifier().toString())} Note that the type discriminator takes inheritance into account,
     * meaning that any derived type of Population, AgencyTeam, MilitaryUnit, Activity and Shape is treated as above.
     * 
     * @param d1 the first object to be compared.
     * @param d2 the second object to be compared.
     */
    public int compare(MapDrawableAdapter d1, MapDrawableAdapter d2) {
        // TODO fix: in java 7 it throws "Comparsion method violates its general contract"
        if (d1 instanceof MapPointAdapter) {
            if (d2 instanceof MapPointAdapter) {
                return 0;
            }
            return 1;
        } else if (d2 instanceof MapPointAdapter) {
            return -1;
        } else if (d1 instanceof MapLineAdapter) {
            if (d2 instanceof MapLineAdapter) {
                return 0;
            }
            return 1;
        } else if (d2 instanceof MapLineAdapter) {
            return -1;
        } else if (d1 instanceof MapShapeAdapter) {
            if (d2 instanceof MapShapeAdapter) {
                return 0;
            }
            return -1;
        } else if (d2 instanceof MapShapeAdapter) {
            return 1;
        } else {
            MapElementAdapter a1 = (MapElementAdapter) d1;
            MapElementAdapter a2 = (MapElementAdapter) d2;
            if (a1.isSelected() ^ a2.isSelected()) {
                return a1.isSelected() ? 1 : -1;
            } else if (a1.getApproxsimObject().getType()
                    .canSubstitute(a2.getApproxsimObject().getType())
                    || a2.getApproxsimObject().getType()
                            .canSubstitute(a1.getApproxsimObject().getType())) {
                Type t1 = a1.getApproxsimObject().getType();
                Type t2 = a2.getApproxsimObject().getType();
                if (t1.canSubstitute("Activity")) {
                    if (t2.canSubstitute("Activity")) {
                        return 0;
                    }
                    return 1;
                } else if (t2.canSubstitute("Activity")) {
                    return -1;
                } else if (t1.canSubstitute("MilitaryUnit")) {
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
            } else if (a1.getApproxsimObject()
                    .isAncestor(a2.getApproxsimObject())) {
                return 1;
            } else if (a2.getApproxsimObject()
                    .isAncestor(a1.getApproxsimObject())) {
                return -1;
            } else {
                return a1.getApproxsimObject().getIdentifier()
                        .compareTo(a2.getApproxsimObject().getIdentifier());
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
