// $Id: ApproxsimParameterFactory.java,v 1.4 2006/03/22 14:30:50 dah Exp $
/*
 * @(#)ApproxsimParametersFactory.java
 */

package ApproxsimClient.evolver;

import ApproxsimClient.object.ApproxsimObject;
import ApproxsimClient.object.ApproxsimDecimal;
import ApproxsimClient.object.ApproxsimInteger;
import ApproxsimClient.object.type.Type;
import ApproxsimClient.object.type.TypeFactory;
import java.util.Hashtable;

/**
 * Maps ApproxsimObjects to suitable Parameter instances.
 * 
 * @version 1, $Date: 2006/03/22 14:30:50 $
 * @author Daniel Ahlin
 */
public class ApproxsimParameterFactory implements ParameterFactory {
    /**
     * A mapping between Type and Parameter.
     */
    Hashtable typeMapping;

    /**
     * Returns a new instance of a ApproxsimParameterFactory using the provided TypeInformation.
     */
    public ApproxsimParameterFactory() {
        this.typeMapping = createTypeMapping();
    }

    /**
     * The simulation to simulate
     */
    ApproxsimObject simulation = null;

    /**
     * Returns an instance of Parameter suitable for the provided object (or null if none found).
     * 
     * @param object the object to map
     */
    public Parameter getParameter(Object object) {
        // Only handle ApproxsimObjects.
        if (!(object instanceof ApproxsimObject)) {
            return null;
        }

        return getParameter((ApproxsimObject) object);
    }

    /**
     * Returns an instance of Parameter suitable for the provided ApproxsimObject (or null if none found).
     * 
     * @param approxsimObject the object to map
     */
    public Parameter getParameter(ApproxsimObject approxsimObject) {
        return getTypeParameter(approxsimObject);
    }

    /**
     * Returns the type mapping table
     */
    public Hashtable getTypeMapping() {
        return this.typeMapping;
    }

    /**
     * Returns the best instance of Parameter suitable for the provided type (or null if none found).
     */
    public Parameter getTypeParameter(ApproxsimObject object) {
        // Find youngest base type with a mapping
        for (Type walker = object.getType(); walker != null; walker = walker
                .getBaseType()) {
            ParameterFactory parameterFactory = (ParameterFactory) getTypeMapping()
                    .get(walker);
            if (parameterFactory != null) {
                return parameterFactory.getParameter(object);
            }
        }

        return null;
    }

    /**
     * Creates the mapping between XML type and Parameter.
     */
    Hashtable createTypeMapping() {
        Hashtable mapping = new Hashtable();

        mapping.put(TypeFactory.getType("Double"), new ParameterFactory() {
            public Parameter getParameter(Object o) {
                ApproxsimDecimal sObj = (ApproxsimDecimal) o;
                if (isBadDecimal(sObj)) {
                    return null;
                } else {
                    return new ApproxsimDecimalParameter((ApproxsimDecimal) o);
                }
            }
        });
        mapping.put(TypeFactory.getType("double",
                                        "http://www.w3.org/2001/XMLSchema"),
                    new ParameterFactory() {
                        public Parameter getParameter(Object o) {
                            ApproxsimDecimal sObj = (ApproxsimDecimal) o;
                            if (isBadDecimal(sObj)) {
                                return null;
                            } else {
                                return new ApproxsimDecimalParameter(
                                        (ApproxsimDecimal) o);
                            }
                        }
                    });
        mapping.put(TypeFactory.getType("NonNegativeInteger"),
                    new ParameterFactory() {
                        public Parameter getParameter(Object o) {
                            return new ApproxsimIntegerParameter(
                                    (ApproxsimInteger) o);
                        }
                    });
        // Ground type type hiearchy.
        mapping.put(TypeFactory.getType("anyType",
                                        "http://www.w3.org/2001/XMLSchema"),
                    new ParameterFactory() {
                        public Parameter getParameter(Object o) {
                            return null;
                        }
                    });

        return mapping;
    }

    /**
     * Temp hack to fix stuff.
     */
    private boolean isBadDecimal(ApproxsimDecimal d) {
        for (ApproxsimObject walker = d; walker != null; walker = walker
                .getParent()) {
            if (walker.getType().canSubstitute("Shape")) {
                return true;
            }
        }

        return false;
    }
}
