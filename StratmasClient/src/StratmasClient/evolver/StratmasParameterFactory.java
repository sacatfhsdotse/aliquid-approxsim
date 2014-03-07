//         $Id: StratmasParameterFactory.java,v 1.4 2006/03/22 14:30:50 dah Exp $
/*
 * @(#)StratmasParametersFactory.java
 */

package StratmasClient.evolver;

import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasDecimal;
import StratmasClient.object.StratmasInteger;
import StratmasClient.object.type.Type;
import StratmasClient.object.type.TypeFactory;
import java.util.Hashtable;

/**
 * Maps StratmasObjects to suitable Parameter instances.
 *
 * @version 1, $Date: 2006/03/22 14:30:50 $
 * @author  Daniel Ahlin
*/
public class StratmasParameterFactory implements ParameterFactory
{
    /** A mapping between Type and Parameter.    
     *
     */
    Hashtable typeMapping;

    /**
     * Returns a new instance of a StratmasParameterFactory using the
     * provided TypeInformation.
     */
    public StratmasParameterFactory() 
    {
        this.typeMapping = createTypeMapping();
    }

    /**
     * The simulation to simulate
     */
    StratmasObject simulation = null;

    /**
     * Returns an instance of Parameter suitable for the provided
     * object (or null if none found).
     *
     * @param object the object to map
     */
    public Parameter getParameter(Object object)
    {
        // Only handle StratmasObjects.
        if (!(object instanceof StratmasObject)) {
            return null;
        }
        
        return getParameter((StratmasObject) object);
    }

    /**
     * Returns an instance of Parameter suitable for the provided
     * StratmasObject (or null if none found).
     *
     * @param stratmasObject the object to map
     */
    public Parameter getParameter(StratmasObject stratmasObject)
    {
        return getTypeParameter(stratmasObject);
    }
    
    /**
     * Returns the type mapping table
     */
    public Hashtable getTypeMapping()
    {
        return this.typeMapping;
    }

    /**
     * Returns the best instance of Parameter suitable for the
     * provided type (or null if none found).
     */
    public Parameter getTypeParameter(StratmasObject object)
    {
        // Find youngest base type with a mapping
        for (Type walker = object.getType(); 
             walker != null; walker = walker.getBaseType()) {
            ParameterFactory parameterFactory = (ParameterFactory) getTypeMapping().get(walker);
            if (parameterFactory != null) {
                return parameterFactory.getParameter(object);
            }
        }

        return null;
    }

    /**
     * Creates the mapping between XML type and Parameter.
     */
    Hashtable createTypeMapping()
    {
        Hashtable mapping = new Hashtable();
        
        mapping.put(TypeFactory.getType("Double"), 
                    new ParameterFactory() 
                    {
                        public Parameter getParameter(Object o) 
                        {
                            StratmasDecimal sObj = (StratmasDecimal) o;
                            if (isBadDecimal(sObj)) {
                                return null;
                            } else {
                                return new StratmasDecimalParameter((StratmasDecimal) o);
                            }
                        }
                    });
        mapping.put(TypeFactory.getType("double", "http://www.w3.org/2001/XMLSchema"), 
                    new ParameterFactory() 
                    {
                        public Parameter getParameter(Object o) 
                        {
                            StratmasDecimal sObj = (StratmasDecimal) o;
                            if (isBadDecimal(sObj)) {
                                return null;
                            } else {
                                return new StratmasDecimalParameter((StratmasDecimal) o);
                            }
                        }
                    });
        mapping.put(TypeFactory.getType("NonNegativeInteger"), 
                    new ParameterFactory() 
                    {
                        public Parameter getParameter(Object o) 
                        {
                            return new StratmasIntegerParameter((StratmasInteger) o);
                        }
                    });
        // Ground type type hiearchy.
        mapping.put(TypeFactory.getType("anyType", "http://www.w3.org/2001/XMLSchema"), 
                    new ParameterFactory() 
                    {
                        public Parameter getParameter(Object o) 
                        {
                            return null;
                        }
                    });

        return mapping;
    }

    /**
     * Temp hack to fix stuff.
     */
    private boolean isBadDecimal(StratmasDecimal d)
    {
        for (StratmasObject walker = d; walker != null; 
             walker = walker.getParent()) {
            if (walker.getType().canSubstitute("Shape")) {
                return true;
            }
        }

        return false;
    }
}
