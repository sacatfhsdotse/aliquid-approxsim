// $Id: ApproxsimObjectFactory.java,v 1.11 2006/10/02 16:12:43 alexius Exp $
/*
 * @(#)ApproxsimObjectFactory.java
 */

package ApproxsimClient.object;

import ApproxsimClient.object.type.Type;
import ApproxsimClient.object.type.TypeFactory;
import ApproxsimClient.object.type.Declaration;

import ApproxsimClient.ApproxsimConstants;

import org.w3c.dom.Element;

import java.util.Hashtable;
import java.util.Vector;

/**
 * ApproxsimObjectFactory is mapping of different sources of ApproxsimObjects to instances of the same.
 * 
 * @version 1, $Date: 2006/10/02 16:12:43 $
 * @author Daniel Ahlin
 */

public class ApproxsimObjectFactory {
    /**
     * The table holding the string -> creator mapping.
     */
    static Hashtable stringMap = null;

    /**
     * The table holding the string1 -> string2 mappings for using constructor of string2 if no constructor for string1 can be found.
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
    abstract static class ApproxsimObjectConstructor {
        /**
         * Returns a ApproxsimGUIConstructor whose getApproxsimObject will return the object.
         * 
         * @param declaration the declaration for which this object is created.
         */
        public ApproxsimGUIConstructor guiCreate(Declaration declaration) {
            throw new AssertionError("GUI creation of "
                    + declaration.getType().getName()
                    + " is not implemented yet");
        }

        /**
         * Returns a ApproxsimVectorConstructor whose getApproxsimObject will return the object.
         * 
         * @param declaration the declaration for which this object is created.
         */
        public ApproxsimVectorConstructor vectorCreate(Declaration declaration) {
            throw new AssertionError("Vector creation of "
                    + declaration.getType().getName()
                    + " is not implemented yet");
        }

        /**
         * Returns a ApproxsimObject built from the supplied DOM element.
         * 
         * @param r The reference to the object to be created.
         * @param element the element to build the object from.
         */
        public ApproxsimObject domCreate(Element element) {
            throw new AssertionError(
                    "Creation from XML Element not implemented yet.");
        }

        /**
         * Returns a ApproxsimObject with default values.
         * 
         * @param declaration the declaration for which this object is created.
         */
        public ApproxsimObject defaultCreate(Declaration declaration) {
            throw new AssertionError("Default creation of "
                    + declaration.getType().getName()
                    + " is not implemented yet");
        }
    }

    /**
     * Creates the mapping of the factory.
     */
    protected static void createMappings() {
        if (substitutions != null && stringMap != null) {
            return;
        } else {
            stringMap = new Hashtable();
            substitutions = new Hashtable();
            substitutions.put(ApproxsimConstants.approxsimNamespace + ":Root",
                              "DefaultComplex");
            substitutions.put(ApproxsimConstants.approxsimNamespace
                    + ":Simulation", "DefaultComplex");
            substitutions.put(ApproxsimConstants.approxsimNamespace
                    + ":TimeStepper", "DefaultComplex");
            substitutions.put(ApproxsimConstants.approxsimNamespace
                    + ":GridPartitioner", "DefaultComplex");
            substitutions.put(ApproxsimConstants.approxsimNamespace
                    + ":ModelParameters", "DefaultComplex");
            substitutions.put(ApproxsimConstants.approxsimNamespace
                    + ":ParameterGroup", "DefaultComplex");
            substitutions
                    .put(ApproxsimConstants.approxsimNamespace + ":Scenario",
                         "DefaultComplex");
            substitutions.put(ApproxsimConstants.approxsimNamespace + ":Region",
                              "DefaultComplex");
            substitutions.put(ApproxsimConstants.approxsimNamespace + ":Disease",
                              "DefaultComplex");
            substitutions
                    .put(ApproxsimConstants.approxsimNamespace + ":Activity",
                         "DefaultComplex");
            substitutions.put(ApproxsimConstants.approxsimNamespace + ":Shape",
                              "DefaultComplex");
            substitutions.put(ApproxsimConstants.approxsimNamespace + ":Element",
                              "DefaultComplex");
            substitutions.put(ApproxsimConstants.approxsimNamespace
                    + ":PopulationGroup", "DefaultComplex");
            substitutions.put(ApproxsimConstants.approxsimNamespace + ":Faction",
                              "DefaultComplex");
            substitutions.put(ApproxsimConstants.approxsimNamespace
                    + ":FactionRelation", "DefaultComplex");
            substitutions.put(ApproxsimConstants.approxsimNamespace
                    + ":Equipment", "DefaultComplex");
            substitutions.put(ApproxsimConstants.approxsimNamespace
                    + ":Distribution", "DefaultComplex");
            substitutions.put(ApproxsimConstants.approxsimNamespace
                    + ":ApproxsimCityDistribution", "DefaultComplex");
            substitutions.put(ApproxsimConstants.approxsimNamespace
                    + ":UniformDistribution", "DefaultComplex");
            substitutions.put(ApproxsimConstants.approxsimNamespace
                    + ":NormalDistribution", "DefaultComplex");
            substitutions.put(ApproxsimConstants.approxsimNamespace
                    + ":NonNegativeInteger", ApproxsimConstants.xsdNamespace
                    + ":integer");
            substitutions.put(ApproxsimConstants.approxsimNamespace + ":String",
                              ApproxsimConstants.xsdNamespace + ":string");
            substitutions.put(ApproxsimConstants.approxsimNamespace + ":Double",
                              ApproxsimConstants.xsdNamespace + ":double");
            substitutions.put(ApproxsimConstants.approxsimNamespace + ":Node",
                              "DefaultComplex");
            substitutions.put(ApproxsimConstants.approxsimNamespace + ":Edge",
                              "DefaultComplex");
            substitutions.put(ApproxsimConstants.approxsimNamespace + ":Graph",
                              "DefaultComplex");
            substitutions.put(ApproxsimConstants.approxsimNamespace + ":Effect",
                    "DefaultComplex");

            stringMap.put(ApproxsimConstants.xsdNamespace + ":string",
                          new ApproxsimObjectConstructor() {
                              public ApproxsimGUIConstructor guiCreate(
                                      Declaration declaration) {
                                  return ApproxsimString
                                          .getGUIConstructor(declaration);
                              }

                              public ApproxsimObject domCreate(Element n) {
                                  return ApproxsimString.domCreate(n);
                              }

                              public ApproxsimObject defaultCreate(
                                      Declaration declaration) {
                                  return ApproxsimString
                                          .defaultCreate(declaration);
                              }
                          });
            stringMap.put(ApproxsimConstants.approxsimNamespace + ":Point",
                          new ApproxsimObjectConstructor() {
                              public ApproxsimGUIConstructor guiCreate(
                                      Declaration declaration) {
                                  return Point.getGUIConstructor(declaration);
                              }

                              public ApproxsimVectorConstructor vectorCreate(
                                      Declaration declaration) {
                                  return Point
                                          .getVectorConstructor(declaration);
                              }

                              public ApproxsimObject domCreate(Element n) {
                                  return Point.domCreate(n);
                              }

                              public ApproxsimObject defaultCreate(
                                      Declaration declaration) {
                                  return Point.defaultCreate(declaration);
                              }
                          });
            stringMap.put(ApproxsimConstants.xsdNamespace + ":double",
                          new ApproxsimObjectConstructor() {
                              public ApproxsimGUIConstructor guiCreate(
                                      Declaration declaration) {
                                  return ApproxsimDecimal
                                          .getGUIConstructor(declaration);
                              }

                              public ApproxsimVectorConstructor vectorCreate(
                                      Declaration declaration) {
                                  return ApproxsimDecimal
                                          .getVectorConstructor(declaration);
                              }

                              public ApproxsimObject domCreate(Element n) {
                                  return ApproxsimDecimal.domCreate(n);
                              }

                              public ApproxsimObject defaultCreate(
                                      Declaration declaration) {
                                  return ApproxsimDecimal
                                          .defaultCreate(declaration);
                              }
                          });
            stringMap.put(ApproxsimConstants.xsdNamespace + ":integer",
                          new ApproxsimObjectConstructor() {
                              public ApproxsimGUIConstructor guiCreate(
                                      Declaration declaration) {
                                  return ApproxsimInteger
                                          .getGUIConstructor(declaration);
                              }

                              public ApproxsimObject domCreate(Element n) {
                                  return ApproxsimInteger.domCreate(n);
                              }

                              public ApproxsimObject defaultCreate(
                                      Declaration declaration) {
                                  return ApproxsimInteger
                                          .defaultCreate(declaration);
                              }
                          });
            stringMap.put("DefaultComplex", new ApproxsimObjectConstructor() {
                public ApproxsimGUIConstructor guiCreate(Declaration declaration) {
                    return DefaultComplex.getGUIConstructor(declaration);
                }

                public ApproxsimVectorConstructor vectorCreate(
                        Declaration declaration) {
                    return DefaultComplex.getVectorConstructor(declaration);
                }

                public ApproxsimObject domCreate(Element n) {
                    return DefaultComplex.domCreate(n);
                }

                public ApproxsimObject defaultCreate(Declaration declaration) {
                    return DefaultComplex.defaultCreate(declaration);
                }
            });
            stringMap.put(ApproxsimConstants.approxsimNamespace + ":Composite",
                          new ApproxsimObjectConstructor() {
                              public ApproxsimVectorConstructor vectorCreate(
                                      Declaration declaration) {
                                  return Composite
                                          .getVectorConstructor(declaration);
                              }

                              public ApproxsimObject domCreate(Element n) {
                                  return Composite.domCreate(n);
                              }

                              public ApproxsimGUIConstructor guiCreate(
                                      Declaration declaration) {
                                  return Composite
                                          .getGUIConstructor(declaration);
                              }

                              public ApproxsimObject defaultCreate(
                                      Declaration declaration) {
                                  return Composite.defaultCreate(declaration);
                              }
                          });
            stringMap.put(ApproxsimConstants.approxsimNamespace + ":Polygon",
                          new ApproxsimObjectConstructor() {
                              public ApproxsimVectorConstructor vectorCreate(
                                      Declaration declaration) {
                                  return Polygon
                                          .getVectorConstructor(declaration);
                              }

                              public ApproxsimObject domCreate(Element n) {
                                  return Polygon.domCreate(n);
                              }

                              public ApproxsimGUIConstructor guiCreate(
                                      Declaration declaration) {
                                  return Polygon.getGUIConstructor(declaration);
                              }

                              public ApproxsimObject defaultCreate(
                                      Declaration declaration) {
                                  return Polygon.defaultCreate(declaration);
                              }
                          });
            stringMap.put(ApproxsimConstants.approxsimNamespace + ":Line",
                          new ApproxsimObjectConstructor() {
                              public ApproxsimVectorConstructor vectorCreate(
                                      Declaration declaration) {
                                  return Line.getVectorConstructor(declaration);
                              }

                              public ApproxsimGUIConstructor guiCreate(
                                      Declaration declaration) {
                                  return Line.getGUIConstructor(declaration);
                              }

                              public ApproxsimObject domCreate(Element n) {
                                  return Line.domCreate(n);
                              }

                              public ApproxsimObject defaultCreate(
                                      Declaration declaration) {
                                  return Line.defaultCreate(declaration);
                              }

                          });
            stringMap.put(ApproxsimConstants.approxsimNamespace + ":Circle",
                          new ApproxsimObjectConstructor() {
                              public ApproxsimVectorConstructor vectorCreate(
                                      Declaration declaration) {
                                  return Circle
                                          .getVectorConstructor(declaration);
                              }

                              public ApproxsimObject domCreate(Element n) {
                                  return Circle.domCreate(n);
                              }

                              public ApproxsimGUIConstructor guiCreate(
                                      Declaration declaration) {
                                  return Circle.getGUIConstructor(declaration);
                              }

                              public ApproxsimObject defaultCreate(
                                      Declaration declaration) {
                                  return Circle.defaultCreate(declaration);
                              }
                          });
            stringMap.put(ApproxsimConstants.approxsimNamespace + ":Timestamp",
                          new ApproxsimObjectConstructor() {
                              public ApproxsimGUIConstructor guiCreate(
                                      Declaration declaration) {
                                  return ApproxsimTimestamp
                                          .getGUIConstructor(declaration);
                              }

                              public ApproxsimObject domCreate(Element n) {
                                  return ApproxsimTimestamp.domCreate(n);
                              }

                              public ApproxsimObject defaultCreate(
                                      Declaration declaration) {
                                  return ApproxsimTimestamp
                                          .defaultCreate(declaration);
                              }
                          });
            stringMap.put(ApproxsimConstants.approxsimNamespace + ":Reference",
                          new ApproxsimObjectConstructor() {
                              public ApproxsimObject domCreate(Element n) {
                                  return ApproxsimReference.domCreate(n);
                              }

                              public ApproxsimGUIConstructor guiCreate(
                                      Declaration declaration) {
                                  return ApproxsimReference
                                          .getGUIConstructor(declaration);
                              }

                              public ApproxsimObject defaultCreate(
                                      Declaration declaration) {
                                  return ApproxsimReference
                                          .defaultCreate(declaration);
                              }
                          });
            stringMap.put(ApproxsimConstants.approxsimNamespace + ":Boolean",
                          new ApproxsimObjectConstructor() {
                              public ApproxsimObject domCreate(Element n) {
                                  return ApproxsimBoolean.domCreate(n);
                              }

                              public ApproxsimGUIConstructor guiCreate(
                                      Declaration declaration) {
                                  return ApproxsimBoolean
                                          .getGUIConstructor(declaration);
                              }

                              public ApproxsimObject defaultCreate(
                                      Declaration declaration) {
                                  return ApproxsimBoolean
                                          .defaultCreate(declaration);
                              }
                          });
            stringMap
                    .put(ApproxsimConstants.approxsimNamespace + ":SymbolIDCode",
                         new ApproxsimObjectConstructor() {
                             public ApproxsimGUIConstructor guiCreate(
                                     Declaration declaration) {
                                 return SymbolIDCode
                                         .getGUIConstructor(declaration);
                             }

                             public ApproxsimVectorConstructor vectorCreate(
                                     Declaration declaration) {
                                 return SymbolIDCode
                                         .getVectorConstructor(declaration);
                             }

                             public ApproxsimObject domCreate(Element n) {
                                 return SymbolIDCode.domCreate(n);
                             }

                             public ApproxsimObject defaultCreate(
                                     Declaration declaration) {
                                 return SymbolIDCode.defaultCreate(declaration);
                             }
                         });
            stringMap.put(ApproxsimConstants.approxsimNamespace + ":Duration",
                          new ApproxsimObjectConstructor() {
                              public ApproxsimVectorConstructor vectorCreate(
                                      Declaration declaration) {
                                  return ApproxsimDuration
                                          .getVectorConstructor(declaration);
                              }

                              public ApproxsimObject domCreate(Element n) {
                                  return ApproxsimDuration.domCreate(n);
                              }

                              public ApproxsimGUIConstructor guiCreate(
                                      Declaration declaration) {
                                  return ApproxsimDuration
                                          .getGUIConstructor(declaration);
                              }

                              public ApproxsimObject defaultCreate(
                                      Declaration declaration) {
                                  return ApproxsimDuration
                                          .defaultCreate(declaration);
                              }
                          });
            stringMap.put(ApproxsimConstants.approxsimNamespace + ":Shape",
                          new ApproxsimObjectConstructor() {
                              public ApproxsimGUIConstructor guiCreate(
                                      Declaration declaration) {
                                  return DefaultComplex
                                          .getGUIConstructor(declaration);
                              }

                              public ApproxsimVectorConstructor vectorCreate(
                                      Declaration declaration) {
                                  return DefaultComplex
                                          .getVectorConstructor(declaration);
                              }

                              public ApproxsimObject defaultCreate(
                                      Declaration declaration) {
                                  return Shape.defaultCreate(declaration);
                              }
                          });
            stringMap
                    .put(ApproxsimConstants.approxsimNamespace + ":Distribution",
                         new ApproxsimObjectConstructor() {
                             public ApproxsimGUIConstructor guiCreate(
                                     Declaration declaration) {
                                 return DefaultComplex
                                         .getGUIConstructor(declaration);
                             }

                             public ApproxsimVectorConstructor vectorCreate(
                                     Declaration declaration) {
                                 return DefaultComplex
                                         .getVectorConstructor(declaration);
                             }

                             public ApproxsimObject domCreate(Element n) {
                                 return DefaultComplex.domCreate(n);
                             }

                             public ApproxsimObject defaultCreate(
                                     Declaration declaration) {
                                 return DefaultComplex.defaultCreate(declaration.clone(TypeFactory
                                         .getType("UniformDistribution")));
                             }
                         });
        }
    }

    /**
     * Returns a object capable of creating the specified type, or null if no such found.
     * 
     * @param type the type to get the constructor for.
     */
    private static ApproxsimObjectConstructor getConstructor(Type type) {
        createMappings();
        String typestr = type.getNamespace() + ":" + type.getName();

        // First, try to match directly against stringMap:
        ApproxsimObjectConstructor constructor = (ApproxsimObjectConstructor) ApproxsimObjectFactory.stringMap
                .get(typestr);
        if (constructor != null) {
            return constructor;
        }

        // Next, try to match indirectly through substitution. This
        // method of resolving allows a maximum of one level of
        // indirection, which is intended.
        String substitution = (String) ApproxsimObjectFactory.substitutions
                .get(typestr);
        if (substitution != null) {
            return (ApproxsimObjectConstructor) ApproxsimObjectFactory.stringMap
                    .get(substitution);
        }

        // Next, try to match against the types supertype (only
        // necessary to check direct parent since this is a
        // reccurance.)
        if (type.getBaseType() != null
                && !type.getBaseType().getName().equals("anyType")) {
            return getConstructor(type.getBaseType());
        }

        return null;
    }

    /**
     * Returns an instance of ApproxsimGUIConstructor
     * 
     * @param declaration the declaration to create an instance for.
     */
    public static ApproxsimGUIConstructor guiCreate(Declaration declaration) {
        if (declaration.isList()) {
            return ApproxsimList.getGUIConstructor(declaration);
        } else {
            ApproxsimObjectConstructor con = ApproxsimObjectFactory
                    .getConstructor(declaration.getType());
            if (con != null) {
                return con.guiCreate(declaration);
            } else {
                // No match, too bad.
                throw new AssertionError("Unable to create instances of type: "
                        + declaration.getType().getName());
            }
        }
    }

    /**
     * Returns an instance of ApproxsimVectorConstructor
     * 
     * @param declaration the declaration to create an instance for.
     */
    public static ApproxsimVectorConstructor vectorCreate(Declaration declaration) {
        ApproxsimObjectConstructor con = ApproxsimObjectFactory
                .getConstructor(declaration.getType());
        if (con != null) {
            return con.vectorCreate(declaration);
        } else {
            // No match, too bad.
            throw new AssertionError("Unable to create instances of type: "
                    + declaration.getType().getName());
        }
    }

    /**
     * Returns an instance of ApproxsimListVectorConstructor
     * 
     * @param declaration the declaration to create an instance for.
     */
    public static ApproxsimVectorConstructor vectorCreateList(
            Declaration declaration) {
        if (declaration.isList()) {
            return ApproxsimList.getVectorConstructor(declaration);
        } else {
            throw new AssertionError(
                    "Tried to call vectorCreateList() with a Declaration that is not a list.");
        }
    }

    /**
     * Returns a ApproxsimObject created from a DOM Element.
     * <p>
     * author Per Alexius
     * 
     * @param n The element to use to create a ApproxsimObject.
     */
    public static ApproxsimObject domCreate(Element n) {
        Type t = XMLHelper.getType(n);
        ApproxsimObjectConstructor con = ApproxsimObjectFactory.getConstructor(t);
        if (con != null) {
            return register(con.domCreate(n));
        } else {
            // No match, too bad.
            throw new AssertionError("Unable to create instances of type: "
                    + t.getName());
        }
    }

    /**
     * Returns a ApproxsimObject created with default values.
     * <p>
     * author Per Alexius
     * 
     * @param declaration The declaration to create an instance for.
     */
    public static ApproxsimObject defaultCreate(Declaration declaration) {
        ApproxsimObjectConstructor con = ApproxsimObjectFactory
                .getConstructor(declaration.getType());
        if (con != null) {
            return register(con.defaultCreate(declaration));
        } else {
            // No match, too bad.
            throw new AssertionError("Unable to create instances of type: "
                    + declaration.getType().getName());
        }
    }

    /**
     * Returns a ApproxsimObject of the specified type created with default values.
     * 
     * @param type the type to create an instance for.
     */
    public static ApproxsimObject create(Type type) {
        ApproxsimObjectConstructor constructor = ApproxsimObjectFactory
                .getConstructor(type);
        if (constructor != null) {
            return register(constructor.defaultCreate(new Declaration(type)));
        } else {
            // No match, too bad.
            throw new AssertionError("Unable to create instances of type: "
                    + type.getName());
        }
    }

    /**
     * Returns a ApproxsimObject of the specified type created using ApproxsimObjects in the provided vector. It is currently not possible to
     * create approxsimLists with this function.
     * 
     * @param type the declaration to create an instance for.
     * @param v ApproxsimObjects to use when creating
     */
    public static ApproxsimObject create(Type type, Vector v) {
        ApproxsimObjectConstructor constructor = ApproxsimObjectFactory
                .getConstructor(type);
        if (constructor != null) {
            return register(constructor.vectorCreate(new Declaration(type))
                    .getApproxsimObject(v));
        } else {
            // No match, too bad.
            throw new AssertionError("Unable to create instances of type: "
                    + type.getName());
        }
    }

    /**
     * Returns a ApproxsimObject of the specified type created using ApproxsimObjects in the provided vector. It is currently not possible to
     * create approxsimLists with this function.
     * 
     * @param type the declaration to create an instance for.
     * @param v ApproxsimObjects to use when creating
     */
    public static ApproxsimObject create(String identifier, Type type, Vector v) {
        ApproxsimObjectConstructor constructor = ApproxsimObjectFactory
                .getConstructor(type);
        if (constructor != null) {
            return register(constructor.vectorCreate(new Declaration(type,
                                                             identifier))
                    .getApproxsimObject(v));
        } else {
            // No match, too bad.
            throw new AssertionError("Unable to create instances of type: "
                    + type.getName());
        }
    }

    /**
     * Creates and returns a new Point using the specified values.
     * 
     * @param identifier the identifier to use.
     * @param lat the latitude of the point.
     * @param lon the longitude of the point.
     */
    public static Point createPoint(String identifier, double lat, double lon) {
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
    public static Line createLine(String identifier, Point p1, Point p2) {
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
    public static ApproxsimString createString(String identifier, Type type,
            String val) {
        ApproxsimString res = new ApproxsimString(identifier, type, val);
        register(res);
        return res;
    }

    /**
     * Creates and returns a new String using the specified string.
     * 
     * @param declaration declaration to use
     * @param val the value.
     */
    public static ApproxsimString createString(Declaration declaration,
            String val) {
        return createString(declaration.getName(), declaration.getType(), val);
    }

    /**
     * Creates and returns a new String using the specified string.
     * 
     * @param identifier the identifier to use.
     * @param val the value.
     */
    public static ApproxsimString createString(String identifier, String val) {
        return createString(identifier, TypeFactory.getType("String"), val);
    }

    /**
     * Creates and returns a new Decimal using the specified decimal.
     * 
     * @param identifier the identifier to use.
     * @param type the type to use.
     * @param val the value.
     */
    public static ApproxsimDecimal createDecimal(String identifier, Type type,
            double val) {

        ApproxsimDecimal res = new ApproxsimDecimal(identifier, type, val);
        register(res);
        return res;
    }

    /**
     * Creates and returns a new Decimal using the specified decimal.
     * 
     * @param declaration declaration to use
     * @param val the value.
     */
    public static ApproxsimDecimal createDecimal(Declaration declaration,
            double val) {
        return createDecimal(declaration.getName(), declaration.getType(), val);
    }

    /**
     * Creates and returns a new Decimal using the specified decimal.
     * 
     * @param identifier the identifier to use.
     * @param val the value.
     */
    public static ApproxsimDecimal createDecimal(String identifier, double val) {
        return createDecimal(identifier, TypeFactory.getType("Double"), val);
    }

    /**
     * Creates a polygon with the specified identifier and lines.
     * 
     * @param identifier the identifier of the polygon.
     * @param lines lines of the polygon, note that lines should not contain a ApproxsimList.
     */
    public static Polygon createPolygon(String identifier, Vector lines) {
        Vector lists = new Vector();
        lists.add(createList(TypeFactory.getType("Polygon")
                                     .getSubElement("curves"), lines));
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
            double lon, double radius) {
        Circle res = new Circle(identifier,
                ApproxsimObjectFactory.createPoint("center", lat, lon),
                ApproxsimObjectFactory.createDecimal("radius", radius));
        register(res);
        return res;
    }

    /**
     * Creates a ApproxsimList of the specified declaration.
     * 
     * @param declaration the declaration to use.
     * @param vector the children to add to the list.
     */
    public static ApproxsimObject createList(Declaration declaration,
            Vector vector) {
        return register(new ApproxsimList(declaration, vector));
    }

    /**
     * Creates an empty ApproxsimList of the specified declaration.
     * 
     * @param declaration the declaration to use.
     */
    public static ApproxsimObject createList(Declaration declaration) {
        return register(new ApproxsimList(declaration));
    }

    /**
     * Returns a clone of the provided object.
     * 
     * @param object the object to clone.
     */
    public static ApproxsimObject cloneObject(ApproxsimObject object) {
        return register((ApproxsimObject) object.clone());
    }

    /**
     * This method is the last one called on each constructed object prior to its return to the requestor.
     * 
     * @param object the object constructed
     * @return the provided object as a convinience
     */
    protected static ApproxsimObject register(ApproxsimObject object) {
        // Consider using a thread to do this.
        // Get a local reference to the listeners array.
        FactoryListener[] ref = factoryListeners;
        for (int i = 0; i < ref.length; i++) {
            if (ref[i] != null) {
                ref[i].approxsimObjectCreated(object);
            }
        }

        return object;
    }

    /**
     * This method is called by implementors of ApproxsimObject.setParent to enable notifications of additions to the tree.
     * 
     * @param object the object constructed
     */
    protected static void attached(ApproxsimObject object) {
        // Consider using a thread to do this.
        // Get a local reference to the listeners array.
        FactoryListener[] ref = factoryListeners;
        for (int i = 0; i < ref.length; i++) {
            if (ref[i] != null) {
                ref[i].approxsimObjectAttached(object);
            }
        }
    }

    /**
     * Registers the listener for notifications on ApproxsimObject-creation. Note that the implementation of this event class is geared
     * towards long-term listeners, for transient listening, consider reimplementing the listener list.
     * 
     * @param listener listener to add
     */
    public static void addEventListener(FactoryListener listener) {
        synchronized (factoryListenersLock) {
            for (int i = 0; i < factoryListeners.length; i++) {
                if (factoryListeners[i] == null) {
                    factoryListeners[i] = listener;
                    return;
                }
            }
            // No free space.
            FactoryListener[] newListeners = new FactoryListener[factoryListeners.length + 1];
            System.arraycopy(factoryListeners, 0, newListeners, 0,
                             factoryListeners.length);
            newListeners[newListeners.length - 1] = listener;
            ApproxsimObjectFactory.factoryListeners = newListeners;
        }
    }

    /**
     * Removes the listener from listeners getting notifications on ApproxsimObject-creation.
     * 
     * @param listener listener to add
     */
    public static void removeEventListener(FactoryListener listener) {
        synchronized (factoryListenersLock) {
            int nonNulls = 0;
            for (int i = 0; i < factoryListeners.length; i++) {
                if (factoryListeners[i] == listener) {
                    factoryListeners[i] = null;
                } else if (factoryListeners[i] != null) {
                    nonNulls++;
                }
            }

            if (factoryListeners.length > 5
                    && nonNulls < factoryListeners.length / 2) {
                FactoryListener[] newListeners = new FactoryListener[nonNulls + 1];
                int j = 0;
                for (int i = 0; i < factoryListeners.length; i++) {
                    if (factoryListeners[i] != null) {
                        newListeners[j++] = factoryListeners[i];
                    }
                }

                ApproxsimObjectFactory.factoryListeners = newListeners;
            }
        }
    }

}
