package StratmasClient.substrate;

import java.awt.Color;
import java.util.Vector;
import java.util.Enumeration;

import StratmasClient.object.Line;
import StratmasClient.object.Point;
import StratmasClient.object.Polygon;
import StratmasClient.object.StratmasObjectFactory;
import StratmasClient.BoundingBox;
import StratmasClient.map.adapter.MapDrawableAdapter;
import StratmasClient.map.adapter.MapShapeAdapter;
import StratmasClient.map.adapter.MapLineAdapter;
import StratmasClient.map.adapter.MapPointAdapter;

/**
 * This class implements methods used to construct, change and move polygonials. 
 */
public class PolygonMaker extends ShapeMaker {
    /**
     * Number of polygons created. 
     */
    public static int createdPolygonCounter = 0;
    /**
     * The first inserted point in the polygonial.
     */
    protected Point firstPoint;
    /**
     * The last inserted point in the polygonial.
     */
    protected Point lastPoint;
    /**
     * Indicates if the first and the last added points are connected with a line.
     */
    protected boolean polygonCompleted = false;
    
    /**
     * Creates new polygonial constructor.
     */
    public PolygonMaker(SubstrateMapDrawer drawer) {
        super(drawer);
    }

    /**
     * Returns true if the creation of the polygonial is completed.
     */
    protected boolean isPolygonCompleted() {
        return polygonCompleted;
    }
    
    /**
     * Returns the first inserted polygonial point.
     */
    protected Point getFirstPoint() {
        return firstPoint;
    }

    /**
     * Sets the first point of the polygonial.
     */
    protected void setFirstPoint(Point firstPoint) {
        this.firstPoint = firstPoint;
    }
    
    /**
     * Returns the last inserted polygonial point.
     */
    protected Point getLastPoint() {
        return lastPoint;
    }
    
    /**
     * Sets the last point of the polygonial.
     */
    protected void setLastPoint(Point lastPoint) {
        this.lastPoint = lastPoint;
    }
    
    /**
     * Adds new point to the polygonial. This method is used while the polygonial is beeing created.
     *
     * @param point point to be added to the polygonial.
     */
    protected  void addPoint(Point point) {
        // add first point to the polygonial
        if (firstPoint == null) {
            firstPoint = point;
        }
        // add new line to the polygonial
        if (lastPoint != null) {
            Point p2 = StratmasObjectFactory.createPoint("p2", point.getLat(), point.getLon());
            addMapPointAdapter(p2); 
            addLine(lastPoint, p2);
            overlappingPoints.put(point, p2);
            overlappingPoints.put(p2, point);
        }
        lastPoint = point;
        lastPoint.setIdentifier("p1");
        MapPointAdapter mpad = drawer.addMapPointAdapter(lastPoint);  
        pointAdapters.add(mpad);
    }
    
    /**
     * Adds a MapPointAdapter to the list.
     */
    protected void addMapPointAdapter(Point point) {
        MapPointAdapter adapter = drawer.addMapPointAdapter(point);
        pointAdapters.add(adapter);
    }

    /**
     * Adds a line created of the two given points to the polygonial.
     *
     * @param p1 the start point of the line.
     * @param p2 the end point of the line.
     */
    private void addLine(Point p1, Point p2) {
        Line line = StratmasObjectFactory.createLine(String.valueOf(lineAdapters.size()), p1, p2);
        addMapLineAdapter(line);
    }
    
    /**
     * Adds a MapLineAdapter to the list.
     */
    protected void addMapLineAdapter(Line line) {
        MapLineAdapter adapter = drawer.addMapLineAdapter(line);
        lineAdapters.add(adapter);
    }
      
    /**
     * Completes the polygonial by adding a line between the last and the first points in the list. 
     * If the polygonial cannot be created all the points and lines are removed from the creator.
     */
    protected void addLastLine() {
        if (!isPolygonCompleted()) {
            if (lineAdapters.size() >= 2) {
                Point p2 = StratmasObjectFactory.createPoint("p2", firstPoint.getLat(), firstPoint.getLon());
                addMapPointAdapter(p2);
                addLine(lastPoint, p2);
                overlappingPoints.put(firstPoint, p2);
                overlappingPoints.put(p2, firstPoint);
                polygonCompleted = true;
                firstPoint = null;
                lastPoint = null;
            }
            else {
                // if no polygon can be created
                clearAll();
            }
        }
    }
    
    /**
     * Moves a point of the polygon.
     *
     * @param point the actual point.
     * @param lon new longitude of the point.
     * @param lat new latitude of the point.
     */
    protected void movePolygonPoint(Point point, double lon, double lat) {
        // find another Point objects which represents the same point 
        Point twinP = overlappingPoints.get(point);
        if (twinP != null) {
            point.setLat(lat, this);
            point.setLon(lon, this);
            twinP.setLat(lat, this);
            twinP.setLon(lon, this); 
        }
    }
    
    /**
     * Inserts new point into the polygonial. 
     *
     * @param point the point to be inserted.
     * @param lineAdapter line adapter of the line which is divided by the inserted point into 
     *                    two new lines. 
     */
    public void insertPoint(Point point, MapLineAdapter lineAdapter) {
        try {
            Line line = (Line)lineAdapter.getObject();
            //change identifiers for all the lines that come after the actual line 
            int lineId = Integer.parseInt(lineAdapter.getObject().getIdentifier().toString());
            for (int i = 0; i < lineAdapters.size(); i++) {
                MapLineAdapter ad = lineAdapters.get(i);
                int id = Integer.parseInt(ad.getObject().getIdentifier().toString());
                if (id > lineId) {
                    ((Line)ad.getObject()).setIdentifier(String.valueOf(id+1));
                }
            }
            // remove the actual line
            drawer.removeMapLineAdapter(lineAdapter);
            lineAdapters.remove(lineAdapter);
            
            // add new lines 
            // create and add the first line
            Point p2 = point;
            point.setIdentifier("p2");
            addMapPointAdapter(p2);
            Line l1 =  StratmasObjectFactory.createLine(String.valueOf(lineId), line.getStartPoint(), p2);
            addMapLineAdapter(l1);

            // create and add the second line
            Point p1 = StratmasObjectFactory.createPoint("p1", point.getLat(), point.getLon());
            addMapPointAdapter(p1);
            Line l2 =  StratmasObjectFactory.createLine(String.valueOf(lineId+1), p1, line.getEndPoint());
            addMapLineAdapter(l2);
            
            overlappingPoints.put(p1, p2);
            overlappingPoints.put(p2, p1);
            
            // update the shape
            Color color = shapeAdapter.getShapeAreaColor();
            createMapShapeAdapter(color);
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Moves the area in the chosen direction.
     *
     * @param lon longitude of the new center of the polygons bounding box.
     * @param lat latitude of the new center of the polygons bounding box.
     */
    public void moveShape(double lon, double lat) {
        BoundingBox box = getAreaBoundingBox();
        if (box != null) {
            double lonCenter = (box.getEastLon() + box.getWestLon()) / 2;
            double latCenter = (box.getNorthLat() + box.getSouthLat()) / 2;
            // polygonial area
            for (Enumeration<MapLineAdapter> e = lineAdapters.elements(); e.hasMoreElements(); ) {
                Line line = (Line)e.nextElement().getObject();
                line.move(lon - lonCenter, lat - latCenter);
            }
        }
    }
        
    /**
     * Returns the bounding box of the area.
     *
     * @return the bounding box of the area.
     */
    protected BoundingBox getAreaBoundingBox() {
        // bounding box of the polygonial
        if (!pointAdapters.isEmpty()) {
            Point p = (Point)((MapDrawableAdapter) pointAdapters.firstElement()).getObject();
            double minLon = p.getLon();
            double maxLon = p.getLon();
            double minLat = p.getLat();
            double maxLat = p.getLat();
            for (int i = 1; i < pointAdapters.size(); i++) {
                p = (Point)((MapDrawableAdapter) pointAdapters.get(i)).getObject();
                minLon = (p.getLon() < minLon)? p.getLon() : minLon;
                maxLon = (p.getLon() > maxLon)? p.getLon() : maxLon;
                minLat = (p.getLat() < minLat)? p.getLat() : minLat;
                maxLat = (p.getLat() > maxLat)? p.getLat() : maxLat;
            }
            return new BoundingBox(minLon, minLat, maxLon, maxLat);
        }
        //
        return null;
    }
    
    /**
     * Checks if the polygon is simple.
     *
     * @return true if the polygon is simple, false otherwise.
     */
    protected boolean isShapeSimple() {
        if (shapeAdapter == null) {
            return false;
        } 
        Vector tempLines = new Vector();
        for (Enumeration e = ((Polygon)shapeAdapter.getObject()).getCurves(); e.hasMoreElements(); ) {
            tempLines.add(e.nextElement());
        }
        for (int i = 0; i < tempLines.size()-2; i++) {
            int lastLine = (i == 0)? tempLines.size()-1 : tempLines.size();
            for (int j = i+2; j < lastLine; j++) {
                Line l1 = (Line)tempLines.get(i);
                Line l2 = (Line)tempLines.get(j);
                if (l1.intersects(l2, drawer.getProjection())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Removes all adapters and resets this object.
     */
    protected void clearAll() {
        firstPoint = null;
        lastPoint  = null;
        polygonCompleted = false;
        removeMapLineAdapters();
        removeMapPointAdapters();
        drawer.setIsDrawnMapDrawablesListUpdated(false);
        overlappingPoints.clear();
        shapeAdapter = null;
        drawer.update();
    }
    
     /**
     * Creates the polygonial shape from the list of lines.
     *
     * @return the polygonial shape.
     */
    protected Polygon createPolygon() {
        // add the last line if the polygonial is not completed
        if (!polygonCompleted) {
            addLastLine();
        }
        // create the polygonial
        if (lineAdapters.size() >= 3) {
            // get all the lines
            Vector<Line> lines = new Vector<Line>();
            lines.setSize(lineAdapters.size());
            for (int i = 0; i < lineAdapters.size(); i++) {
                Line line = (Line)lineAdapters.get(i).getObject();
                lines.removeElementAt(Integer.parseInt(line.getIdentifier()));
                lines.add(Integer.parseInt(line.getIdentifier()), line);
            }
            // create the polygonial shape
            return StratmasObjectFactory.createPolygon("polygon" + String.valueOf(createdPolygonCounter++), lines);
        }
        //
        return null;
    }
    
    /**
     * Creates new MapShapeAdapter.
     */
    protected void createMapShapeAdapter(Color color) {
        Polygon polygon = createPolygon();
        if (polygon != null) {
            shapeAdapter = (MapShapeAdapter)MapDrawableAdapter.getMapDrawableAdapter(polygon);
            shapeAdapter.setShapeAreaColor(color);
            shapeAdapter.setShapeLineWidth(2.0f);
        }
    }

    /**
     * Updates the color of the shape.
     */
    protected void updateShapeColor(Color color) {
        for (int i = 0; i < lineAdapters.size(); i++) {
            lineAdapters.get(i).setLineColor(color);
            if (shapeAdapter != null) {
                shapeAdapter.setShapeAreaColor(color);
            }
        } 
    }

}
