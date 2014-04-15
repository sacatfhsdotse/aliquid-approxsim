//         $Id: StratmasObjectFactory.java,v 1.11 2006/10/02 16:12:43 alexius Exp $
/*
 * @(#)StratmasObjectFactory.java
 */

package StratmasClient.object;

import StratmasClient.object.type.Type;
import StratmasClient.object.type.TypeFactory;
import StratmasClient.object.type.Declaration;

import StratmasClient.StratmasConstants;

import org.w3c.dom.Element;

import java.util.Hashtable;
import java.util.Vector;

/**
 * StratmasObjectFactory is mapping of different sources of
 * StratmasObjects to instances of the same.
 *
 * @version 1, $Date: 2006/10/02 16:12:43 $
 * @author  Daniel Ahlin
*/

public class StratmasObjectFactory
{
    /**
     * The table holding the string -> creator mapping.
     */
    static Hashtable stringMap = null;

    /**
     * The table holding the string1 -> string2 mappings for using
     * constructor of string2 if no constructor for string1 can be found.
     */
    static Hashtable substitutions = null;
    
    /**
     * List of FactoryListeners. By default allocate 0 places
     */
    static FactoryListener[] factoryListeners = new FactoryListener[0];
    
    /**
     * Lock for listeners
     */
    static Object factoryListenersLock = new Object();

    /**
     * A class template for the creator objects.
     */
    abstract static class StratmasObjectConstructor 
    {
        /**
         * Returns a StratmasGUIConstructor whose getStratmasObject will
         * return the object.
         *
         * @param declaration the declaration for which this object is
         * created.
         */
        public StratmasGUIConstructor guiCreate(Declaration declaration)
        {
            throw new AssertionError("GUI creation of " + 
                                     declaration.getType().getName() + 
                                     " is not implemented yet");
        }

        /**
         * Returns a StratmasVectorConstructor whose getStratmasObject will
         * return the object.
         *
         * @param declaration the declaration for which this object is
         * created.
         */
        public StratmasVectorConstructor vectorCreate(Declaration declaration) 
        {
            throw new AssertionError("Vector creation of " + 
                                     declaration.getType().getName() + 
                                     " is not implemented yet");
        }
        
        /**
         * Returns a StratmasObject built from the supplied DOM element.
         *
         * @param r The reference to the object to be created.
         * @param element the element to build the object from.
         */
        public StratmasObject domCreate(Element element) 
        {
            throw new AssertionError("Creation from XML Element not implemented yet.");
        }

         /**
          * Returns a StratmasObject with default values.
          *
          * @param declaration the declaration for which this object is
          * created.
          */
         public StratmasObject defaultCreate(Declaration declaration) {
            throw new AssertionError("Default creation of " + 
                                     declaration.getType().getName() + 
                                     " is not implemented yet");
         }
    }
    
    /**
     * Creates the mapping of the factory.
     */
    protected static void createMappings() 
    {
        if (substitutions != null && 
            stringMap != null) {
            return;
        } else {
            stringMap = new Hashtable();
            substitutions = new Hashtable();
            substitutions.put(StratmasConstants.stratmasNamespace + 
                              ":Root", "DefaultComplex");
            substitutions.put(StratmasConstants.stratmasNamespace + 
                              ":Simulation", "DefaultComplex");
            substitutions.put(StratmasConstants.stratmasNamespace + 
                              ":TimeStepper", "DefaultComplex");
            substitutions.put(StratmasConstants.stratmasNamespace + 
                              ":GridPartitioner", "DefaultComplex");
            substitutions.put(StratmasConstants.stratmasNamespace + 
                              ":ModelParameters", "DefaultComplex");
            substitutions.put(StratmasConstants.stratmasNamespace + 
                              ":ParameterGroup", "DefaultComplex");
            substitutions.put(StratmasConstants.stratmasNamespace + 
                              ":Scenario", "DefaultComplex");
            substitutions.put(StratmasConstants.stratmasNamespace + 
                              ":Region", "DefaultComplex");
            substitutions.put(StratmasConstants.stratmasNamespace + 
                              ":Disease", "DefaultComplex");
            substitutions.put(StratmasConstants.stratmasNamespace + 
                              ":Activity", "DefaultComplex");
            substitutions.put(StratmasConstants.stratmasNamespace + 
                              ":Shape", "DefaultComplex");
            substitutions.put(StratmasConstants.stratmasNamespace + 
                              ":Element", "DefaultComplex");
            substitutions.put(StratmasConstants.stratmasNamespace + 
                              ":PopulationGroup", "DefaultComplex");
            substitutions.put(StratmasConstants.stratmasNamespace + 
                              ":Faction", "DefaultComplex");
            substitutions.put(StratmasConstants.stratmasNamespace + 
                              ":FactionRelation", "DefaultComplex");
            substitutions.put(StratmasConstants.stratmasNamespace + 
                              ":Equipment", "DefaultComplex");
            substitutions.put(StratmasConstants.stratmasNamespace + 
                              ":Distribution", "DefaultComplex");
            substitutions.put(StratmasConstants.stratmasNamespace + 
                              ":StratmasCityDistribution", "DefaultComplex");
            substitutions.put(StratmasConstants.stratmasNamespace + 
                              ":UniformDistribution", "DefaultComplex");
            substitutions.put(StratmasConstants.stratmasNamespace + 
                              ":NormalDistribution", "DefaultComplex");
            substitutions.put(StratmasConstants.stratmasNamespace + 
                              ":NonNegativeInteger", 
                              StratmasConstants.xsdNamespace + ":integer");
             substitutions.put(StratmasConstants.stratmasNamespace + 
                               ":String", 
                               StratmasConstants.xsdNamespace + ":string");
            substitutions.put(StratmasConstants.stratmasNamespace + 
                              ":Double", 
                              StratmasConstants.xsdNamespace + ":double");
            substitutions.put(StratmasConstants.stratmasNamespace + 
                              ":Node", "DefaultComplex");
            substitutions.put(StratmasConstants.stratmasNamespace + 
                              ":Edge", "DefaultComplex");
            substitutions.put(StratmasConstants.stratmasNamespace + 
                              ":Graph", "DefaultComplex");
            
            stringMap.put(StratmasConstants.xsdNamespace + ":string", 
                          new StratmasObjectConstructor() {
                              public StratmasGUIConstructor guiCreate(Declaration declaration)
                              {
                                  return StratmasString.getGUIConstructor(declaration);
                              }
                              public StratmasObject domCreate(Element n)
                               {
                                  return StratmasString.domCreate(n);
                               }
                              public StratmasObject defaultCreate(Declaration declaration)
                              {
                                  return StratmasString.defaultCreate(declaration);
                              }
                          });
            stringMap.put(StratmasConstants.stratmasNamespace + ":Point", 
                          new StratmasObjectConstructor() {
                              public StratmasGUIConstructor guiCreate(Declaration declaration)
                              {
                                  return Point.getGUIConstructor(declaration);
                              }
                              public StratmasVectorConstructor vectorCreate(Declaration declaration)
                              {
                                  return Point.getVectorConstructor(declaration);
                              }
                              public StratmasObject domCreate(Element n)
                               {
                                  return Point.domCreate(n);
                               }
                              public StratmasObject defaultCreate(Declaration declaration)
                              {
                                  return Point.defaultCreate(declaration);
                              }
                          });
            stringMap.put(StratmasConstants.xsdNamespace + ":double", 
                          new StratmasObjectConstructor() {
                              public StratmasGUIConstructor guiCreate(Declaration declaration)
                              {
                                  return StratmasDecimal.getGUIConstructor(declaration);
                              }
                              public StratmasVectorConstructor vectorCreate(Declaration declaration)
                              {
                                  return StratmasDecimal.getVectorConstructor(declaration);
                              }
                              public StratmasObject domCreate(Element n)
                               {
                                  return StratmasDecimal.domCreate(n);
                               }
                              public StratmasObject defaultCreate(Declaration declaration)
                              {
                                  return StratmasDecimal.defaultCreate(declaration);
                              }
                          });
            stringMap.put(StratmasConstants.xsdNamespace + ":integer", 
                          new StratmasObjectConstructor() {
                              public StratmasGUIConstructor guiCreate(Declaration declaration)
                              {
                                  return StratmasInteger.getGUIConstructor(declaration);
                              }
                              public StratmasObject domCreate(Element n)
                               {
                                  return StratmasInteger.domCreate(n);
                               }
                              public StratmasObject defaultCreate(Declaration declaration)
                              {
                                  return StratmasInteger.defaultCreate(declaration);
                              }
                          });
            stringMap.put("DefaultComplex", 
                          new StratmasObjectConstructor() {
                              public StratmasGUIConstructor guiCreate(Declaration declaration)
                              {
                                  return DefaultComplex.getGUIConstructor(declaration);
                              }
                              public StratmasVectorConstructor vectorCreate(Declaration declaration)
                              {
                                  return DefaultComplex.getVectorConstructor(declaration);
                              }
                              public StratmasObject domCreate(Element n)
                               {
                                  return DefaultComplex.domCreate(n);
                               }
                              public StratmasObject defaultCreate(Declaration declaration)
                              {
                                  return DefaultComplex.defaultCreate(declaration);
                              }
                          });
            stringMap.put(StratmasConstants.stratmasNamespace + ":Composite", 
                          new StratmasObjectConstructor() {
                              public StratmasVectorConstructor vectorCreate(Declaration declaration)
                              {
                                  return Composite.getVectorConstructor(declaration);
                              }
                              public StratmasObject domCreate(Element n)
                               {
                                  return Composite.domCreate(n);
                               }
                              public StratmasGUIConstructor guiCreate(Declaration declaration)
                              {
                                  return Composite.getGUIConstructor(declaration);
                              }
                              public StratmasObject defaultCreate(Declaration declaration)
                              {
                                  return Composite.defaultCreate(declaration);
                              }
                          });
            stringMap.put(StratmasConstants.stratmasNamespace + ":Polygon", 
                          new StratmasObjectConstructor() {
                              public StratmasVectorConstructor vectorCreate(Declaration declaration)
                              {
                                  return Polygon.getVectorConstructor(declaration);
                              }
                              public StratmasObject domCreate(Element n)
                               {
                                  return Polygon.domCreate(n);
                               }
                              public StratmasGUIConstructor guiCreate(Declaration declaration)
                              {
                                  return Polygon.getGUIConstructor(declaration);
                              }
                              public StratmasObject defaultCreate(Declaration declaration)
                              {
                                  return Polygon.defaultCreate(declaration);
                              }
                          });
            stringMap.put(StratmasConstants.stratmasNamespace + ":Line", 
                          new StratmasObjectConstructor() {
                              public StratmasVectorConstructor vectorCreate(Declaration declaration)
                              {
                                  return Line.getVectorConstructor(declaration);
                              }
                              public StratmasGUIConstructor guiCreate(Declaration declaration)
                              {
                                  return Line.getGUIConstructor(declaration);
                              }
                              public StratmasObject domCreate(Element n)
                              {
                                  return Line.domCreate(n);
                              }
                              public StratmasObject defaultCreate(Declaration declaration)
                              {
                                  return Line.defaultCreate(declaration);
                              }

                          });
            stringMap.put(StratmasConstants.stratmasNamespace + ":Circle", 
                          new StratmasObjectConstructor() {
                              public StratmasVectorConstructor vectorCreate(Declaration declaration)
                              {
                                  return Circle.getVectorConstructor(declaration);
                              }
                              public StratmasObject domCreate(Element n)
                               {
                                  return Circle.domCreate(n);
                               }
                              public StratmasGUIConstructor guiCreate(Declaration declaration)
                              {
                                  return Circle.getGUIConstructor(declaration);
                              }
                              public StratmasObject defaultCreate(Declaration declaration)
                              {
                                  return Circle.defaultCreate(declaration);
                              }
                          });
            stringMap.put(StratmasConstants.stratmasNamespace + ":Timestamp", 
                          new StratmasObjectConstructor() {
                              public StratmasGUIConstructor guiCreate(Declaration declaration)
                              {
                                  return StratmasTimestamp.getGUIConstructor(declaration);
                              }
                              public StratmasObject domCreate(Element n)
                               {
                                  return StratmasTimestamp.domCreate(n);
                               }
                              public StratmasObject defaultCreate(Declaration declaration)
                              {
                                  return StratmasTimestamp.defaultCreate(declaration);
                              }
                          });
            stringMap.put(StratmasConstants.stratmasNamespace + ":Reference", 
                          new StratmasObjectConstructor() {
                              public StratmasObject domCreate(Element n)
                               {
                                  return StratmasReference.domCreate(n);
                               }
                              public StratmasGUIConstructor guiCreate(Declaration declaration)
                              {
                                  return StratmasReference.getGUIConstructor(declaration);
                              }
                              public StratmasObject defaultCreate(Declaration declaration)
                              {
                                  return StratmasReference.defaultCreate(declaration);
                              }
                          });
            stringMap.put(StratmasConstants.stratmasNamespace + ":Boolean", 
                          new StratmasObjectConstructor() {
                              public StratmasObject domCreate(Element n)
                               {
                                  return StratmasBoolean.domCreate(n);
                               }
                              public StratmasGUIConstructor guiCreate(Declaration declaration)
                              {
                                  return StratmasBoolean.getGUIConstructor(declaration);
                              }
                              public StratmasObject defaultCreate(Declaration declaration)
                              {
                                  return StratmasBoolean.defaultCreate(declaration);
                              }
                          });
            stringMap.put(StratmasConstants.stratmasNamespace + ":SymbolIDCode", 
                          new StratmasObjectConstructor() {
                              public StratmasGUIConstructor guiCreate(Declaration declaration)
                              {
                                  return SymbolIDCode.getGUIConstructor(declaration);
                              }
                              public StratmasVectorConstructor vectorCreate(Declaration declaration)
                              {
                                  return SymbolIDCode.getVectorConstructor(declaration);
                              }
                              public StratmasObject domCreate(Element n)
                               {
                                  return SymbolIDCode.domCreate(n);
                               }
                              public StratmasObject defaultCreate(Declaration declaration)
                              {
                                  return SymbolIDCode.defaultCreate(declaration);
                              }
                          });
            stringMap.put(StratmasConstants.stratmasNamespace + ":Duration", 
                          new StratmasObjectConstructor() {
                              public StratmasVectorConstructor vectorCreate(Declaration declaration)
                              {
                                  return StratmasDuration.getVectorConstructor(declaration);
                              }
                              public StratmasObject domCreate(Element n)
                               {
                                  return StratmasDuration.domCreate(n);
                               }
                              public StratmasGUIConstructor guiCreate(Declaration declaration)
                              {
                                  return StratmasDuration.getGUIConstructor(declaration);
                              }
                              public StratmasObject defaultCreate(Declaration declaration)
                              {
                                  return StratmasDuration.defaultCreate(declaration);
                              }
                          });
            stringMap.put(StratmasConstants.stratmasNamespace + ":Shape", 
                          new StratmasObjectConstructor() {
                              public StratmasGUIConstructor guiCreate(Declaration declaration)
                              {
                                  return DefaultComplex.getGUIConstructor(declaration);
                              }
                              public StratmasVectorConstructor vectorCreate(Declaration declaration)
                              {
                                  return DefaultComplex.getVectorConstructor(declaration);
                              }
                              public StratmasObject defaultCreate(Declaration declaration)
                              {
                                  return Shape.defaultCreate(declaration);
                              }
                          });
            stringMap.put(StratmasConstants.stratmasNamespace + ":Distribution", 
                          new StratmasObjectConstructor() {
                              public StratmasGUIConstructor guiCreate(Declaration declaration)
                              {
                                  return DefaultComplex.getGUIConstructor(declaration);
                              }
                              public StratmasVectorConstructor vectorCreate(Declaration declaration)
                              {
                                  return DefaultComplex.getVectorConstructor(declaration);
                              }
                              public StratmasObject domCreate(Element n)
                               {
                                  return DefaultComplex.domCreate(n);
                               }
                              public StratmasObject defaultCreate(Declaration declaration)
                              {
                                  return DefaultComplex.defaultCreate(declaration.clone(TypeFactory.getType("UniformDistribution")));
                              }
                          });
        }
    }
    
    /**
     * Returns a object capable of creating the specified type, or
     * null if no such found.
     *
     * @param type the type to get the constructor for. 
     */
    private static StratmasObjectConstructor getConstructor(Type type)
    {
        createMappings();
        String typestr = type.getNamespace() + ":" + type.getName();

        //First, try to match directly against stringMap:
        StratmasObjectConstructor constructor = 
            (StratmasObjectConstructor) StratmasObjectFactory.stringMap.get(typestr);
        if (constructor != null) {
            return constructor;
        }

        //Next, try to match indirectly through substitution. This
        //method of resolving allows a maximum of one level of
        //indirection, which is intended. 
        String substitution = (String) StratmasObjectFactory.substitutions.get(typestr);
        if (substitution != null) {
            return (StratmasObjectConstructor) StratmasObjectFactory.stringMap.get(substitution);
        }

        // Next, try to match against the types supertype (only
        // necessary to check direct parent since this is a
        // reccurance.)
        if (type.getBaseType() != null && 
            !type.getBaseType().getName().equals("anyType")) {
            return getConstructor(type.getBaseType());
        }
         
        return null;
    }

    /**
     * Returns an instance of StratmasGUIConstructor 
     *
     * @param declaration the declaration to create an instance for.
     */
    public static StratmasGUIConstructor guiCreate(Declaration declaration)
    {
        if (declaration.isList()) {
            return StratmasList.getGUIConstructor(declaration);
        } else {
            StratmasObjectConstructor con = StratmasObjectFactory.getConstructor(declaration.getType());
            if (con != null) {
                return con.guiCreate(declaration);
            } else {
                // No match, too bad.
                throw new AssertionError("Unable to create instances of type: " + declaration.getType().getName());
            }
        }
    }

    /**
     * Returns an instance of StratmasVectorConstructor 
     *
     * @param declaration the declaration to create an instance for.
     */
    public static StratmasVectorConstructor vectorCreate(Declaration declaration)
    {
         StratmasObjectConstructor con = StratmasObjectFactory.getConstructor(declaration.getType());
         if (con != null) {
              return con.vectorCreate(declaration);
         } else {
              // No match, too bad.
              throw new AssertionError("Unable to create instances of type: " + declaration.getType().getName());
         }        
    }

    /**
     * Returns an instance of StratmasListVectorConstructor 
     *
     * @param declaration the declaration to create an instance for.
     */
    public static StratmasVectorConstructor vectorCreateList(Declaration declaration)
    {
         if (declaration.isList()) {
              return StratmasList.getVectorConstructor(declaration);
         }
        else {
             throw new AssertionError("Tried to call vectorCreateList() with a Declaration that is not a list.");
        }
    }

    /**
     * Returns a StratmasObject created from a DOM Element.
     *
     * <p> author Per Alexius
     *
     * @param n The element to use to create a StratmasObject.
     */
    public static StratmasObject domCreate(Element n) 
    {
        Type t = XMLHelper.getType(n);
        StratmasObjectConstructor con = StratmasObjectFactory.getConstructor(t);
        if (con != null) {
            return register(con.domCreate(n)); 
        } else {
            // No match, too bad.
            throw new AssertionError("Unable to create instances of type: " + t.getName());
        }                    
    }
    
    /**
     * Returns a StratmasObject created with default values.
     *
     * <p> author Per Alexius
     *
     * @param declaration The declaration to create an instance for.
     */
    public static StratmasObject defaultCreate(Declaration declaration) 
    {
        StratmasObjectConstructor con = StratmasObjectFactory.getConstructor(declaration.getType());
        if (con != null) {
            return register(con.defaultCreate(declaration));
        } else {
            // No match, too bad.
            throw new AssertionError("Unable to create instances of type: " + declaration.getType().getName());
        }        
    }

    /**
     * Returns a StratmasObject of the specified type created with default values.
     *
     * @param type the type to create an instance for.
     */
    public static StratmasObject create(Type type) 
    {
        StratmasObjectConstructor constructor = 
            StratmasObjectFactory.getConstructor(type);
        if (constructor != null) {
            return register(constructor.defaultCreate(new Declaration(type)));
        } else {
            // No match, too bad.
            throw new AssertionError("Unable to create instances of type: " + type.getName());
        }
    }

    /**
     * Returns a StratmasObject of the specified type created using
     * StratmasObjects in the provided vector. It is currently not
     * possible to create stratmasLists with this function.
     *
     * @param type the declaration to create an instance for.
     * @param v StratmasObjects to use when creating
     */
    public static StratmasObject create(Type type, Vector v)
    {
        StratmasObjectConstructor constructor = StratmasObjectFactory.getConstructor(type);
        if (constructor != null) {
            return register(constructor.vectorCreate(new Declaration(type)).getStratmasObject(v));
        } else {
            // No match, too bad.
            throw new AssertionError("Unable to create instances of type: " + type.getName());
        }
    }

    /**
     * Returns a StratmasObject of the specified type created using
     * StratmasObjects in the provided vector. It is currently not
     * possible to create stratmasLists with this function.
     *
     * @param type the declaration to create an instance for.
     * @param v StratmasObjects to use when creating
     */
    public static StratmasObject create(String identifier, Type type, Vector v)
    {
        StratmasObjectConstructor constructor = StratmasObjectFactory.getConstructor(type);
        if (constructor != null) {
            return register(constructor.vectorCreate(new Declaration(type, identifier)).getStratmasObject(v));
        } else {
            // No match, too bad.
            throw new AssertionError("Unable to create instances of type: " + type.getName());
        }
    }

    /**
     * Creates and returns a new Point using the specified values.
     *
     * @param identifier the identifier to use.
     * @param lat the latitude of the point.
     * @param lon the longitude of the point.
     */
    public static Point createPoint(String identifier, double lat, double lon)
    {
        Point res = new Point(identifier, lat, lon);
        register(res);
        return res;
    }

    /**
     * Creates and returns a new Line using the specified Points.
     *
     * @param identifier the identifier to use.
     * @param p1 the first point of the line.
     * @param p2 the second point of the line.
     */
    public static Line createLine(String identifier, Point p1, Point p2)
    {
        p1.setIdentifier("p1");
        p2.setIdentifier("p2");
        Line res = new Line(identifier, p1, p2);
        register(res);
        return res;
    }

    /**
     * Creates and returns a new String using the specified string.
     *
     * @param identifier the identifier to use.
     * @param type the type to use.
     * @param val the value.
     */
    public static StratmasString createString(String identifier, Type type, String val)
    {
        StratmasString res = new StratmasString(identifier, 
                                                type, 
                                                val);
        register(res);
        return res;
    }

    /**
     * Creates and returns a new String using the specified string.
     *
     * @param declaration declaration to use
     * @param val the value.
     */
    public static StratmasString createString(Declaration declaration, 
                                              String val)
    {
         return createString(declaration.getName(), 
                             declaration.getType(), 
                             val);         
    }

    /**
     * Creates and returns a new String using the specified string.
     *
     * @param identifier the identifier to use.
     * @param val the value.
     */
    public static StratmasString createString(String identifier, String val)
    {
        return createString(identifier, 
                            TypeFactory.getType("String"), 
                            val);
    }

    /**
     * Creates and returns a new Decimal using the specified decimal.
     *
     * @param identifier the identifier to use.
     * @param type the type to use.
     * @param val the value.
     */
    public static StratmasDecimal createDecimal(String identifier, Type type, double val)
    {

        StratmasDecimal res = new StratmasDecimal(identifier, 
                                                  type, 
                                                  val);
        register(res);
        return res;
    }

    /**
     * Creates and returns a new Decimal using the specified decimal.
     *
     * @param declaration declaration to use
     * @param val the value.
     */
    public static StratmasDecimal createDecimal(Declaration declaration, 
                                                double val)
    {
         return createDecimal(declaration.getName(), 
                             declaration.getType(), 
                             val);
    }

    /**
     * Creates and returns a new Decimal using the specified decimal.
     *
     * @param identifier the identifier to use.
     * @param val the value.
     */
    public static StratmasDecimal createDecimal(String identifier, double val)
    {
        return createDecimal(identifier, 
                             TypeFactory.getType("Double"), 
                             val);
    }

    /**
     * Creates a polygon with the specified identifier and lines.
     *
     * @param identifier the identifier of the polygon.
     * @param lines lines of the polygon, note that lines should not
     * contain a StratmasList.
     */
    public static Polygon createPolygon(String identifier, Vector lines)
    {
        Vector lists = new Vector();
        lists.add(createList(TypeFactory.getType("Polygon").getSubElement("curves"), 
                             lines));
        Polygon res = new Polygon(identifier, lists);
        register(res);
        return res;
    }

    /**
     * Creates a circle with the specified centre and radius.
     *
     * @param identifier the identifier of the circle.
     * @param lat the latitude of the center of the circle
     * @param lon the longitude of the center of the circle
     * @param radius the radius of the circle.
     */
    public static Circle createCircle(String identifier, double lat, 
                                      double lon, double radius)
    {
        Circle res = new Circle(identifier, 
                                   StratmasObjectFactory.createPoint("center", lat, lon),
                                StratmasObjectFactory.createDecimal("radius", radius));
        register(res);
        return res;
    }

    /**
     * Creates a StratmasList of the specified declaration.
     *
     * @param declaration the declaration to use.
     * @param vector the children to add to the list.
     */
    public static StratmasObject createList(Declaration declaration, Vector vector)
    {
        return register(new StratmasList(declaration, vector));
    }

    /**
     * Creates an empty StratmasList of the specified declaration.
     *
     * @param declaration the declaration to use.
     */
    public static StratmasObject createList(Declaration declaration)
    {
        return register(new StratmasList(declaration));
    }

    /**
     * Returns a clone of the provided object.
     *
     * @param object the object to clone.
     */
    public static StratmasObject cloneObject(StratmasObject object)
    {
        return register((StratmasObject) object.clone());
    }

    /**
     * This method is the last one called on each constructed object
     * prior to its return to the requestor.
     *
     * @param object the object constructed
     * @return the provided object as a convinience
     */
    protected static StratmasObject register(StratmasObject object)
    {
        // Consider using a thread to do this.
        // Get a local reference to the listeners array.
        FactoryListener[] ref = factoryListeners;
        for (int i = 0; i < ref.length; i++) {
            if (ref[i] != null) {
                ref[i].stratmasObjectCreated(object);
            }
        }

        return object;
    }

    /**
     * This method is called by implementors of
     * StratmasObject.setParent to enable notifications of additions
     * to the tree. 
     *
     * @param object the object constructed
     */
    protected static void attached(StratmasObject object)
    {
        // Consider using a thread to do this.
        // Get a local reference to the listeners array.
        FactoryListener[] ref = factoryListeners;
        for (int i = 0; i < ref.length; i++) {
            if (ref[i] != null) {
                ref[i].stratmasObjectAttached(object);
            }
        }
    }

    /**
     * Registers the listener for notifications on
     * StratmasObject-creation. Note that the implementation of this
     * event class is geared towards long-term listeners, for
     * transient listening, consider reimplementing the listener list.
     *
     * @param listener listener to add
     */
    public static void addEventListener(FactoryListener listener)
    {
        synchronized (factoryListenersLock) {
            for (int i = 0; i < factoryListeners.length; i++) {
                if (factoryListeners[i] == null) {
                    factoryListeners[i] = listener;
                    return;
                }
            }
            // No free space.
            FactoryListener[] newListeners = 
                new FactoryListener[factoryListeners.length + 1];
            System.arraycopy(factoryListeners, 0, 
                             newListeners, 0, factoryListeners.length);
            newListeners[newListeners.length - 1] = listener;
            StratmasObjectFactory.factoryListeners = newListeners;
        }
    }

    /**
     * Removes the listener from listeners getting notifications on
     * StratmasObject-creation.
     *
     * @param listener listener to add
     */
    public static void removeEventListener(FactoryListener listener)
    {
        synchronized (factoryListenersLock) {
            int nonNulls = 0;
            for (int i = 0; i < factoryListeners.length; i++) {
                if (factoryListeners[i] == listener) {
                    factoryListeners[i] = null;
                } else if (factoryListeners[i] != null) {
                    nonNulls++;
                }
            }
            
            if (factoryListeners.length > 5 && 
                nonNulls < factoryListeners.length / 2) {
                FactoryListener[] newListeners = new FactoryListener[nonNulls + 1];
                int j = 0;
                for (int i = 0; i < factoryListeners.length; i++) {
                    if (factoryListeners[i] != null) {
                        newListeners[j++] = factoryListeners[i];
                    }
                }

                StratmasObjectFactory.factoryListeners = newListeners;
            }
        }
    }

}
