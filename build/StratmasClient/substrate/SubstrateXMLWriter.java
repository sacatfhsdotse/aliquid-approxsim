package StratmasClient.substrate;

import java.util.Enumeration;
import java.util.Vector;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JFileChooser;

import StratmasClient.Client;
import StratmasClient.StratmasConstants;
import StratmasClient.ProcessVariableDescription;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.Shape;
import StratmasClient.object.Polygon;
import StratmasClient.object.Circle;
import StratmasClient.object.Line;

/**
 * This class is used to write XML files needed for SubstrateEditor.
 */
public class SubstrateXMLWriter {
    
    /**
     * Creates an XML representation of the modified values in the editor.
     *
     * @param writer the stream to write the values to.
     * @param values the values obtained from the editor.
     */
    public static void convertToXML(BufferedWriter writer, Vector values) throws IOException {
	writeLine(writer, "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
	writeLine(writer, "<pvivaluesset xmlns:sp=\"".concat(StratmasConstants.stratmasNamespace).concat("\" "));
	writeLine(writer, "xmlns:xsi=\"".concat(StratmasConstants.xmlnsNamespace).concat("\" "));
	writeLine(writer, "xsi:type=\"sp:ProcessVariableInitialValuesSet\">");
	for (int i = 0; i < values.size(); i++) {
	    convertToXML(writer, (ProcessVariableInitialValues)values.get(i));
	}
	writeLine(writer, "</pvivaluesset>");
    }
    
    /**
     * Creates an XML representation of the initial values for a process variable and a faction.
     *
     * @param writer the stream to write the values to.
     * @param values initial values obtained from the editor.
     */
    private static void convertToXML(BufferedWriter writer, ProcessVariableInitialValues values) throws IOException {
	writeLine(writer, "<pviv xsi:type=\"sp:ProcessVariableInitialValuesForFile\">");
	// process variable
	convertToXML(writer, values.getProcessVariable());
	// faction
	if (values.getProcessVariable().hasFactions()) {
	    convertToXML(writer, values.getFaction());
	}
	// shapes with values
	Vector shapes = values.getOrderedListOfShapes();
	for (int i = 0; i < shapes.size(); i++) {
	    convertToXML(writer, (ShapeValuePair)shapes.get(i));
	}
	writeLine(writer, "</pviv>");
    }
    
    /**
     * Creates an XML representation of a process variable.
     *
     * @param writer the stream to write the values to.
     * @param pv a process variable.
     */
    private static void convertToXML(BufferedWriter writer, ProcessVariableDescription pv) throws IOException {
	writeLine(writer, "<pv xsi:type=\"sp:ProcessVariableDescription\">");
	writeLine(writer, "<name>" + pv.getName() + "</name>");
	writeLine(writer, "<category>" + pv.getCategory() + "</category>");
	writeLine(writer, "<factions>" + pv.hasFactions() + "</factions>");
	writeLine(writer, "<range xsi:type=\"sp:DoubleRange\">");
	writeLine(writer, "<min>" + pv.getMin() + "</min>");
	writeLine(writer, "<max>" + pv.getMax() + "</max>");
	writeLine(writer, "</range>");
	writeLine(writer, "</pv>");
    }
    
    /**
     * Creates an XML representation of a faction.
     *
     * @param writer the stream to write the values to.
     * @param faction a faction.
     */
    private static void convertToXML(BufferedWriter writer, StratmasObject faction) throws IOException {
	writeLine(writer, "<faction xsi:type=\"sp:" + faction.getType().getName()+ "\" identifier=\"" + faction.getIdentifier() + "\">");
	for (Enumeration e = faction.getChild("enemies").children(); e.hasMoreElements(); ) {
	    StratmasObject enemy = (StratmasObject)e.nextElement();
	    writeLine(writer, "<enemies xsi:type=\"sp:FactionReference\">");
	    writeLine(writer, "<name>" + enemy.getIdentifier() + "</name>");
	    recIdentifierToXML(writer, faction.getParent());
	    writeLine(writer, "</enemies>");
	}
	writeLine(writer, "</faction>");
    }
    
    /**
     * Used to convert Reference to XML. 
     */
    private static void recIdentifierToXML(BufferedWriter writer, StratmasObject object) throws IOException {
	writeLine(writer, "<scope>");
	writeLine(writer, "<name>" + object.getIdentifier() + "</name>");
	if (object.getParent() != null) {
	    recIdentifierToXML(writer, object.getParent()); 
	}
	writeLine(writer, "</scope>");
    }
    
    /**
     * Creates an XML representation of a shape and the assigned value.
     *
     * @param writer the stream to write the values to.
     * @param svp a shape and the assigned value.
     */
    private static void convertToXML(BufferedWriter writer, ShapeValuePair svp) throws IOException {
	if (svp.isEsri()) {
	    writeLine(writer, "<regions xsi:type=\"sp:ESRIRegion\">");
	    writeLine(writer, "<value>" + svp.getValue() + "</value>");
	    writeLine(writer, "<reference>");
	    writeLine(writer, "<name>" + svp.getShape().getIdentifier() + "</name>");
	    recIdentifierToXML(writer, svp.getShape().getParent());
	    writeLine(writer, "</reference>");
	}
	else{
	    writeLine(writer, "<regions xsi:type=\"sp:CreatedRegion\">"); 
	    writeLine(writer, "<value>" + svp.getValue() + "</value>");
	    if (svp.getShape() instanceof Circle) {
		convertToXML(writer, (Circle)svp.getShape());
	    }
	    else if (svp.getShape() instanceof Polygon) {
		convertToXML(writer, (Polygon)svp.getShape());
	    }
	}
	writeLine(writer, "</regions>");
    }
    
    /**
     * Creates an XML representation of a circle.
     *
     * @param writer the stream to write the values to.
     * @param circle a circle.
     */
    private static void convertToXML(BufferedWriter writer, Circle circle) throws IOException {
	writeLine(writer, "<shape xsi:type=\"sp:Circle\" identifier=\"" + circle.getIdentifier() + "\">");
	writeLine(writer, "<radius><value>" + circle.getRadius() + "</value></radius>");
        writeLine(writer, "<center><lat>" + circle.getCenter().getLat() + "</lat><lon>" + circle.getCenter().getLon() + "</lon></center>");
        writeLine(writer, "</shape>");
    }
    
    /**
     * Creates an XML representation of a polygon.
     *
     * @param writer the stream to write the values to.
     * @param polygon a polygon.
     */
    private static void convertToXML(BufferedWriter writer, Polygon polygon) throws IOException {
	writeLine(writer, "<shape xsi:type=\"sp:Polygon\" identifier=\"" + polygon.getIdentifier() + "\">");
	for (Enumeration e = polygon.getCurves(); e.hasMoreElements(); ) {
	    Line line = (Line)e.nextElement();
	    writeLine(writer, "<curves xsi:type=\"sp:Line\" identifier=\"" + line.getIdentifier() + "\">");
	    writeLine(writer, "<p1><lat>" + line.getStartPoint().getLat() + "</lat><lon>" + line.getStartPoint().getLon() + "</lon></p1>");
	    writeLine(writer, "<p2><lat>" + line.getEndPoint().getLat() + "</lat><lon>" + line.getEndPoint().getLon() + "</lon></p2>");
	    writeLine(writer, "</curves>"); 
	}
        writeLine(writer, "</shape>");
    }
    
    /**
     * Creates an XML representation of the modified values in the editor.
     *
     * @param writer the stream to write the values to.
     * @param values the values obtained from the editor.
     */
    public static void convertToXMLForServer(BufferedWriter writer, Vector values) throws IOException {
	for (int i = 0; i < values.size(); i++) {
	    ProcessVariableInitialValues pviv = (ProcessVariableInitialValues)values.get(i);
	    writeLine(writer, "<pvinitvalues xsi:type=\"sp:ProcessVariableInitialValuesForServer\">");
	    // process variable
	    convertToXML(writer, pviv.getProcessVariable());
	    // faction
	    if (pviv.getProcessVariable().hasFactions()) {
		convertToXMLForServer(writer, pviv.getFaction());
	    }
	    // shapes with values
	    Vector shapes = pviv.getOrderedListOfShapes();
	    for (int j = 0; j < shapes.size(); j++) {
		convertToXML(writer, (ShapeValuePair)shapes.get(j));
	    }
	    writeLine(writer, "</pvinitvalues>");
	}
    }
    
     /**
     * Creates an XML representation of a faction.
     *
     * @param writer the stream to write the values to.
     * @param faction a faction.
     */
    private static void convertToXMLForServer(BufferedWriter writer, StratmasObject faction) throws IOException {
	writeLine(writer, "<faction xsi:type=\"sp:EthnicFactionReference\" identifier=\"" + faction.getIdentifier() + "\">");
	writeLine(writer, "<name>" + faction.getIdentifier() + "</name>");
	recIdentifierToXML(writer, faction.getParent());
	writeLine(writer, "</faction>");
    }

    /**
     * Write a line to a stream and change the row.
     */
    private static void writeLine(BufferedWriter writer, String value) throws IOException {
	writer.write(value);
	writer.newLine();
    }
    
 }
