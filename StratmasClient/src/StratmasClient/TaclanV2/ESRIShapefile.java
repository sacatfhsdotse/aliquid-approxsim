// $Id: ESRIShapefile.java,v 1.10 2006/01/11 22:15:43 dah Exp $

package StratmasClient.TaclanV2;

import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Vector;
import java.nio.BufferOverflowException;
import java.util.Hashtable;
import java.nio.BufferUnderflowException;

import StratmasClient.TaclanV2.ESRIShapeFactory.ESRIShapeCreator;

import com.linuxense.javadbf.DBFReader;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFException;

/**
 * @version 1, $Date: 2006/01/11 22:15:43 $
 * @author Daniel Ahlin
 */
public class ESRIShapefile {
    private boolean parsed;
    public String filename;
    public ESRIShapefileHeader header;
    ESRIDBFFile esriDBFFile = null;
    String esriDBNameField = "NAME";
    public ByteBuffer buf;
    Vector<ESRIShape> shapes;

    public ESRIShapefile(String filename) throws FileNotFoundException,
            IOException {
        this.filename = filename;
        RandomAccessFile file = new RandomAccessFile(filename, "r");
        FileChannel channel = file.getChannel();
        MappedByteBuffer buf = channel
                .map(MapMode.READ_ONLY, 0, channel.size());
        // Create assumed dbfile name.
        String esridbffilename = filename
                .substring(0, filename.lastIndexOf('.') + 1)
                + "dbf";
        // Try to read the dbfile if it exists:
        try {
            this.esriDBFFile = new ESRIDBFFile(esridbffilename);
            Vector<String> v = this.esriDBFFile
                    .getMatchingFieldNames(".*NAME.*");
            if (!v.isEmpty()) {
                esriDBNameField = v.get(0);
            }
        } catch (ESRIDBFileException e) {
            StratmasClient.Debug.err.println("Error reading " + esridbffilename
                    + "\nNo metadata will be imported.");
        } catch (FileNotFoundException e) {
            StratmasClient.Debug.err.println("No ESRI database file found, "
                    + "no metadata will be imported.");
        }

        parsed = false;
        shapes = null;
        header = null;
        this.buf = buf;
    }

    public void parse() throws MalformedESRIRecordException,
            UnsupportedESRIShapeException {
        if (!isParsed()) {
            header = new ESRIShapefileHeader(buf);
            ESRIShapeFactory fact = new ESRIShapeFactory();
            shapes = fact.getAll(buf);
            this.parsed = true;
        }
    }

    public ESRIShape getShape(int index) throws MalformedESRIRecordException,
            UnsupportedESRIShapeException, IndexOutOfBoundsException {
        ESRIShape res;

        if (isParsed()) {
            res = shapes.elementAt(index);
        } else {
            header = new ESRIShapefileHeader(buf);
            ESRIShapeFactory fact = new ESRIShapeFactory();
            res = fact.get(buf, index);
            buf.rewind();
        }

        return res;
    }

    public ParsedDeclaration getParsedDeclaration(ParsedReference reference)
            throws MalformedESRIRecordException, UnsupportedESRIShapeException {
        try {
            int shapeIndex = Integer.parseInt(reference.getHead().getName());
            if (reference.getTail() != null) {
                if (reference.getTail().getTail() != null) {
                    throw new MalformedESRIRecordException(
                            "Reference contains three or more "
                                    + " components and ESRI Shapefiles allows"
                                    + "at most two");
                }
                try {
                    int partIndex = Integer.parseInt(reference.getTail()
                            .getHead().getName());
                    return getParsedDeclaration(shapeIndex, partIndex);
                } catch (NumberFormatException e) {
                    throw new AssertionError(e.getMessage()
                            + "\nESRIShapefile do "
                            + "not yet support nonnumeric identifiers.");
                }
            } else {
                return getParsedDeclaration(shapeIndex);
            }
        } catch (NumberFormatException e) {
            throw new AssertionError(
                    e.getMessage()
                            + "\nESRIShapefile do not yet support nonnumeric identifiers.");
        }
    }

    public ParsedDeclaration getParsedDeclaration(int shape, int part)
            throws MalformedESRIRecordException, UnsupportedESRIShapeException {
        SourcePosition pos = new SourcePosition(this.filename);
        return getShape(shape).getParsedDeclaration(pos, part);
    }

    public ParsedDeclaration getParsedDeclaration(int shape)
            throws MalformedESRIRecordException, UnsupportedESRIShapeException {
        SourcePosition pos = new SourcePosition(this.filename);
        ParsedDeclaration pd = getShape(shape).getParsedDeclaration(pos);
        pd.setIdentifier(new ParsedIdentifier(pos, getShapeName(shape)));
        return pd;
    }

    public ParsedDeclarationList getParsedDeclarationList()
            throws MalformedESRIRecordException, UnsupportedESRIShapeException {
        ParsedDeclarationList res = new ParsedDeclarationList();

        for (int i = 0; i < shapes.size(); i++) {
            try {
                res.add(getParsedDeclaration(i));
            } catch (IdConflictException e) {
                throw new MalformedESRIRecordException(e.getMessage());
            }
        }

        return res;
    }

    public boolean isParsed() {
        return parsed;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(filename + ":\n");

        if (isParsed()) {
            buf.append(header.toString());
            for (Enumeration<ESRIShape> ss = shapes.elements(); ss
                    .hasMoreElements();) {
                ESRIShape s = ss.nextElement();
                buf.append(s.toString());
            }
        } else {
            buf.append("Unparsed\n");
        }

        return buf.toString();
    }

    /**
     * Creates a name for the specified shape by first looking in the db-file, if it exists, failing that creates a string representation of
     * the index.
     * 
     * @param index the index of the shape.
     */
    String getShapeName(int index) {
        if (this.esriDBFFile != null) {
            try {
                Object o = this.esriDBFFile.getValue(this.esriDBNameField,
                                                     index);
                if (o != null && o instanceof String) {
                    return washShapeName((String) o);
                }
            } catch (ESRIDBNoSuchFieldException e) {
                // Don't care to much if not succesfull.
            }
        }

        // Failed to return db-filename create fallback name;

        return Integer.toString(index);
    }

    /**
     * Washes strings into lexical conformance with Taclan V2 identifiers.
     * 
     * @param string the string to wash.
     */
    private String washShapeName(String string) {
        String res = string.replaceAll("'", "\'");
        res = res.replaceAll("\n", "\\n");
        res = res.replaceAll("[^\\p{L}]*\\z", "");
        res = res.replaceAll("\\A[^\\p{L}]*", "");

        return res;
    }

    public static void main(String argv[]) {
        String usage = "Usage: java ESRIShapefile filename";

        if (argv.length != 1) {
            System.err.println(usage);
            System.exit(1);
        }

        try {
            ESRIShapefile shapefile = new ESRIShapefile(argv[0]);
            shapefile.parse();
            shapefile.toString();
            System.out.println(shapefile.toString());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

    }

}

class MalformedESRIRecordException extends Exception {
    /**
	 * 
	 */
    private static final long serialVersionUID = 5941648828671482856L;

    public MalformedESRIRecordException(String s) {
        super(s);
    }
}

class UnsupportedESRIShapeException extends Exception {
    /**
	 * 
	 */
    private static final long serialVersionUID = 8820345592266956374L;

    public UnsupportedESRIShapeException(String s) {
        super(s);
    }
}

class ESRIRecord {
    int position;

    public ESRIRecord(ByteBuffer buf) throws MalformedESRIRecordException {
        try {
            position = buf.position();
        } catch (BufferOverflowException e) {
            throw new MalformedESRIRecordException("Malformed ESRIRecord");
        }
    }

    public int getPosition() {
        return position;
    }

    public String toString() {
        return "File position:\t" + getPosition() + "\n";
    }
}

class ESRIShapefileHeader extends ESRIRecord {

    int code;
    int fileln; // In 16-bit integers
    int version;
    int shapetype;
    double xmin;
    double xmax;
    double ymin;
    double ymax;
    double zmin;
    double zmax;
    double mmin;
    double mmax;

    public ESRIShapefileHeader(ByteBuffer buf)
            throws MalformedESRIRecordException {
        super(buf);
        ByteOrder inorder = buf.order();

        // The first two are bigendian...
        buf.order(ByteOrder.BIG_ENDIAN);
        code = buf.getInt();
        // Skip unused values
        buf.position(buf.position() + (5 * 4));
        fileln = buf.getInt();
        // ...and the rest of the header is littleendian, yay!
        buf.order(ByteOrder.LITTLE_ENDIAN);
        version = buf.getInt();
        shapetype = buf.getInt();
        xmin = buf.getDouble();
        xmax = buf.getDouble();
        ymin = buf.getDouble();
        ymax = buf.getDouble();
        zmin = buf.getDouble();
        zmax = buf.getDouble();
        mmin = buf.getDouble();
        mmax = buf.getDouble();
        buf.order(inorder);
    }

    public String toString() {
        return "ESRIShapefileHeader {" + "\n\tFile Code:\t" + getFileCode()
                + "\n\tFile Length:\t" + getFileLength() + "\n\tVersion:\t"
                + getVersion() + "\n\tShape Type:\t" + getShapeType()
                + "\n\tXmin:\t" + getXmin() + "\n\tXmax:\t" + getXmax()
                + "\n\tYmin:\t" + getYmin() + "\n\tYmax:\t" + getYmax()
                + "\n\tZmin:\t" + getZmin() + "\n\tZmax:\t" + getZmax()
                + "\n\tMmin:\t" + getMmin() + "\n\tMmax:\t" + getMmax()
                + "\n}\n";
    }

    public int getFileCode() {
        return code;
    }

    public int getFileLength() {
        return fileln;
    }

    public int getVersion() {
        return version;
    }

    public int getShapeType() {
        return shapetype;
    }

    public double getXmin() {
        return xmin;
    }

    public double getXmax() {
        return xmax;
    }

    public double getYmin() {
        return ymin;
    }

    public double getYmax() {
        return ymax;
    }

    public double getZmin() {
        return zmin;
    }

    public double getZmax() {
        return zmax;
    }

    public double getMmin() {
        return mmin;
    }

    public double getMmax() {
        return mmax;
    }
}

class ESRIRecordHeader extends ESRIRecord {
    int recordnum;
    int recordln;

    public ESRIRecordHeader(ByteBuffer buf) throws MalformedESRIRecordException {
        super(buf);
        ByteOrder inorder = buf.order();
        buf.order(ByteOrder.BIG_ENDIAN);
        recordnum = buf.getInt();
        recordln = buf.getInt();
        buf.order(inorder);
    }

    public int getRecordNumber() {
        return recordnum;
    }

    public int getContentLength() {
        return recordln;
    }

    public String toString() {
        return "ESRIRecordHeader {" + "\n\tRecord Number:\t"
                + getRecordNumber() + "\n\tRecord Length:\t"
                + getContentLength() + "\n}\n";
    }
}

abstract class ESRIShape extends ESRIRecord {
    static int type;

    public ESRIShape(ByteBuffer buf) throws MalformedESRIRecordException {
        super(buf);
    }

    static public int getType() {
        return type;
    }

    public String toString() {
        return "\n\tType:\t" + getType();
    }

    public ParsedDeclaration getParsedDeclaration(SourcePosition pos)
            throws UnsupportedESRIShapeException, MalformedESRIRecordException {
        throw new UnsupportedESRIShapeException("Not implemented.");
    }

    public ParsedDeclaration getParsedDeclaration(SourcePosition pos, int part)
            throws UnsupportedESRIShapeException, MalformedESRIRecordException {
        throw new UnsupportedESRIShapeException("Not implemented.");
    }
}

class ESRIPoint extends ESRIShape {
    static int type = 1;
    double x;
    double y;

    public ESRIPoint(ByteBuffer buf) throws MalformedESRIRecordException {
        super(buf);
        ByteOrder inorder = buf.order();
        buf.order(ByteOrder.LITTLE_ENDIAN);

        x = buf.getDouble();
        y = buf.getDouble();

        buf.order(inorder);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public boolean same(ESRIPoint p) {
        return (getX() == p.getX()) && (getY() == p.getY());
    }

    public double d(ESRIPoint p) {
        double xd = getX() > p.getX() ? getX() - p.getX() : p.getX() - getX();
        double yd = getY() > p.getY() ? getY() - p.getY() : p.getY() - getY();
        return Math.sqrt(xd * xd + yd * yd);
    }

    public String toString() {
        return "ESRIPoint {" + super.toString() + "\n\tX:\t" + getX()
                + "\n\tY:\t" + getY() + "\n}\n";
    }

    public static int getType() {
        return type;
    }
}

class ESRIPolyLine extends ESRIShape {
    static int type = 3;

    double box[];
    int numparts;
    int numpoints;
    int parts[];
    ESRIPoint points[];

    public ESRIPolyLine(ByteBuffer buf) throws MalformedESRIRecordException {
        super(buf);

        ByteOrder inorder = buf.order();
        buf.order(ByteOrder.LITTLE_ENDIAN);

        box = new double[4];
        box[0] = buf.getDouble();
        box[1] = buf.getDouble();
        box[2] = buf.getDouble();
        box[3] = buf.getDouble();
        numparts = buf.getInt();
        numpoints = buf.getInt();

        parts = new int[getNumParts()];
        for (int i = 0; i < getNumParts(); i++) {
            parts[i] = buf.getInt();
        }

        points = new ESRIPoint[getNumPoints()];
        if (getNumPoints() > 0) {
            int offset = 0;
            points[offset] = new ESRIPoint(buf);
            for (int i = 1; i < getNumPoints(); i++) {
                ESRIPoint point = new ESRIPoint(buf);
                if (!points[offset].same(point)) {
                    points[++offset] = point;
                }
            }
            // Check first and last:
            if (points[0].same(points[offset])) {
                offset--;
            }

            if ((offset + 1) != getNumPoints()) {
                ESRIPoint[] oldPoints = points;
                points = new ESRIPoint[offset + 1];
                numpoints = offset;
                System.arraycopy(oldPoints, 0, points, 0, offset + 1);
            }
        }

        buf.order(inorder);
    }

    public double[] getBox() {
        return box;
    }

    public int getNumParts() {
        return numparts;
    }

    public int getNumPoints() {
        return numpoints;
    }

    public int[] getParts() {
        return parts;
    }

    public ESRIPoint[] getPoints() {
        return points;
    }

    public static int getType() {
        return type;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("ESRIPolyLine {");
        buf.append(super.toString());
        for (int i = 0; i < box.length; i++) {
            buf.append("\n\tbox[" + i + "]:\t" + box[i]);
        }

        buf.append("\n\tNumParts:\t" + getNumParts());
        buf.append("\n\tNumPoints:\t" + getNumPoints());
        for (int i = 0; i < parts.length; i++) {
            buf.append("\n\tParts[" + i + "]:\t" + parts[i]);
        }

        for (int i = 0; i < points.length; i++) {
            buf.append("\n\tPoints[" + i + "]:\t" + points[i]);
        }

        buf.append("\n}\n");

        return buf.toString();
    }
}

class ESRIPolygon extends ESRIShape {
    static int type = 5;
    double box[];
    int numparts;
    int numpoints;
    int parts[];
    ESRIPoint points[];

    public ESRIPolygon(ByteBuffer buf) throws MalformedESRIRecordException {
        super(buf);

        ByteOrder inorder = buf.order();
        buf.order(ByteOrder.LITTLE_ENDIAN);

        box = new double[4];
        box[0] = buf.getDouble();
        box[1] = buf.getDouble();
        box[2] = buf.getDouble();
        box[3] = buf.getDouble();
        numparts = buf.getInt();
        numpoints = buf.getInt();

        parts = new int[getNumParts()];
        for (int i = 0; i < getNumParts(); i++) {
            parts[i] = buf.getInt();
        }

        points = new ESRIPoint[getNumPoints()];
        if (getNumPoints() > 0) {
            int offset = 0;
            points[offset] = new ESRIPoint(buf);
            for (int i = 1; i < getNumPoints(); i++) {
                ESRIPoint point = new ESRIPoint(buf);
                if (!points[offset].same(point)) {
                    points[++offset] = point;
                }
            }
            // Check first and last:
            if (points[0].same(points[offset])) {
                offset--;
            }

            if ((offset + 1) != getNumPoints()) {
                ESRIPoint[] oldPoints = points;
                points = new ESRIPoint[offset + 1];
                numpoints = offset;
                System.arraycopy(oldPoints, 0, points, 0, offset + 1);
            }
        }

        buf.order(inorder);
    }

    public double[] getBox() {
        return box;
    }

    public int getNumParts() {
        return numparts;
    }

    public int getNumPoints() {
        return numpoints;
    }

    public int[] getParts() {
        return parts;
    }

    public ESRIPoint[] getPoints() {
        return points;
    }

    public static int getType() {
        return type;
    }

    protected ParsedDeclaration createTaclanV2Point(SourcePosition pos,
            String name, ESRIPoint point) throws MalformedESRIRecordException {
        try {
            ParsedFloat lon = new ParsedFloat(pos,
                    Double.toString(point.getX()));
            ParsedFloat lat = new ParsedFloat(pos,
                    Double.toString(point.getY()));

            lon.setIdentifier(new ParsedIdentifier(pos, "lon"));
            lat.setIdentifier(new ParsedIdentifier(pos, "lat"));

            ParsedDeclarationList coords = new ParsedDeclarationList();
            coords.add(lon);
            coords.add(lat);
            return new ParsedDeclaration(pos,
                    new ParsedIdentifier(pos, "Point"), new ParsedIdentifier(
                            pos, name), coords);
        } catch (IdConflictException e) {
            throw new MalformedESRIRecordException(e.getMessage());
        } catch (SemanticException e) {
            throw new MalformedESRIRecordException(e.getMessage());
        }
    }

    protected ParsedDeclaration createTaclanV2Line(SourcePosition pos,
            ESRIPoint p1, ESRIPoint p2) throws MalformedESRIRecordException {
        ParsedDeclarationList points = new ParsedDeclarationList();
        try {
            points.add(createTaclanV2Point(pos, "p1", p1));
            points.add(createTaclanV2Point(pos, "p2", p2));
            return new ParsedDeclaration(pos,
                    new ParsedIdentifier(pos, "Line"),
                    ParsedIdentifier.getAnonymous(), points);
        } catch (IdConflictException e) {
            throw new MalformedESRIRecordException(e.getMessage());
        } catch (SemanticException e) {
            throw new MalformedESRIRecordException(e.getMessage());
        }
    }

    protected ParsedDeclarationList getParsedDeclarationList(
            SourcePosition pos, int index, int first, int last)
            throws MalformedESRIRecordException {
        ParsedDeclarationList lines = new ParsedDeclarationList();
        try {
            for (int i = first; i < last; i++) {
                ParsedDeclaration line = createTaclanV2Line(pos, points[i],
                                                            points[i + 1]);
                line.setIdentifier(new ParsedIdentifier(pos, Integer
                        .toString(i)));
                lines.add(line);
            }
            // Tie start and endpoint together.
            ParsedDeclaration line = createTaclanV2Line(pos, points[last],
                                                        points[first]);
            line.setIdentifier(new ParsedIdentifier(pos, Integer.toString(last)));
            lines.add(line);
        } catch (IdConflictException e) {
            throw new MalformedESRIRecordException(e.getMessage());
        }

        return lines;
    }

    public ParsedDeclaration getParsedDeclaration(SourcePosition pos, int part)
            throws MalformedESRIRecordException {
        ParsedDeclarationList lines;

        if (part < parts.length - 1) {
            lines = getParsedDeclarationList(pos, part, parts[part],
                                             parts[part + 1] - 1);
        } else {
            // Special handling for last part (and yes, we want a
            // outofboundsexception if part > parts.length)
            lines = getParsedDeclarationList(pos, part, parts[part],
                                             points.length - 1);
        }

        try {
            ParsedList list = new ParsedList(pos, lines);
            list.setIdentifier(new ParsedIdentifier(pos, "curve"));
            ParsedDeclarationList res = new ParsedDeclarationList();
            res.add(list);

            return new ParsedDeclaration(pos, new ParsedIdentifier(pos,
                    "Polygon"), new ParsedIdentifier(pos,
                    Integer.toString(part)), res);
        } catch (SemanticException e) {
            throw new MalformedESRIRecordException(e.getMessage());
        }
    }

    protected ParsedDeclarationList getParsedDeclarationList(SourcePosition pos)
            throws MalformedESRIRecordException {
        ParsedDeclarationList res = new ParsedDeclarationList();
        try {
            for (int i = 0; i < parts.length; i++) {
                res.add(getParsedDeclaration(pos, i));
            }
        } catch (IdConflictException e) {
            throw new MalformedESRIRecordException(e.getMessage());
        }

        return res;
    }

    public ParsedDeclaration getParsedDeclaration(SourcePosition pos)
            throws MalformedESRIRecordException {
        try {
            ParsedDeclarationList dList = getParsedDeclarationList(pos);

            if (dList.getSize() > 1) {
                // If Shape had several parts, return it as Composite
                ParsedList list = new ParsedList(pos, dList);
                list.setIdentifier(new ParsedIdentifier(pos, "shape"));
                ParsedDeclarationList res = new ParsedDeclarationList();
                res.add(list);
                return new ParsedDeclaration(pos, new ParsedIdentifier(pos,
                        "Composite"), ParsedIdentifier.getAnonymous(), res);
            } else {
                return (ParsedDeclaration) dList.getParts().get(0);
            }
        } catch (SemanticException e) {
            throw new MalformedESRIRecordException(e.getMessage());
        }
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("ESRIPolygon {");
        buf.append(super.toString());
        for (int i = 0; i < box.length; i++) {
            buf.append("\n\tbox[" + i + "]:\t" + box[i]);
        }

        buf.append("\n\tNumParts:\t" + getNumParts());
        buf.append("\n\tNumPoints:\t" + getNumPoints());
        for (int i = 0; i < parts.length; i++) {
            buf.append("\n\tParts[" + i + "]:\t" + parts[i]);
        }

        for (int i = 0; i < points.length; i++) {
            buf.append("\n\tPoints[" + i + "]:\t" + points[i]);
        }

        buf.append("\n}\n");

        return buf.toString();
    }
}

class ESRINullShape extends ESRIShape {
    static int type = 0;

    public ESRINullShape(ByteBuffer buf) throws MalformedESRIRecordException {
        super(buf);
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("ESRINullShape {");
        buf.append(super.toString());
        buf.append("\n}\n");
        return buf.toString();
    }

    public static int getType() {
        return type;
    }
}

class ESRIShapeFactory {
    Hashtable<Integer, ESRIShapeCreator> table;

    abstract class ESRIShapeCreator {
        public abstract ESRIShape get(ByteBuffer buf)
                throws MalformedESRIRecordException;
    }

    public ESRIShapeFactory() {
        table = new Hashtable<Integer, ESRIShapeCreator>();
        table.put(new Integer(ESRINullShape.getType()), new ESRIShapeCreator() {
            public ESRIShape get(ByteBuffer buf)
                    throws MalformedESRIRecordException {
                return new ESRINullShape(buf);
            }
        });
        table.put(new Integer(ESRIPoint.getType()), new ESRIShapeCreator() {
            public ESRIShape get(ByteBuffer buf)
                    throws MalformedESRIRecordException {
                return new ESRIPoint(buf);
            }
        });
        table.put(new Integer(ESRIPolyLine.getType()), new ESRIShapeCreator() {
            public ESRIShape get(ByteBuffer buf)
                    throws MalformedESRIRecordException {
                return new ESRIPolyLine(buf);
            }
        });
        table.put(new Integer(ESRIPolygon.getType()), new ESRIShapeCreator() {
            public ESRIShape get(ByteBuffer buf)
                    throws MalformedESRIRecordException {
                return new ESRIPolygon(buf);
            }
        });
    }

    public Vector<ESRIShape> getAll(ByteBuffer buf)
            throws MalformedESRIRecordException, UnsupportedESRIShapeException {
        Vector<ESRIShape> shapes = new Vector<ESRIShape>();

        try {
            while (buf.hasRemaining()) {
                ESRIShape shape = getNext(buf);
                shapes.add(shape);
            }
        } catch (BufferUnderflowException e) {
            throw new MalformedESRIRecordException(
                    "Malformed ESRIShape, file ended prematurely.");
        }

        return shapes;
    }

    public ESRIShape getNext(ByteBuffer buf)
            throws MalformedESRIRecordException, UnsupportedESRIShapeException {
        new ESRIRecordHeader(buf);
        return getShape(buf);

    }

    private ESRIShape getShape(ByteBuffer buf)
            throws MalformedESRIRecordException, UnsupportedESRIShapeException {
        ByteOrder inorder = buf.order();
        buf.order(ByteOrder.LITTLE_ENDIAN);
        Integer key = new Integer(buf.getInt());
        if (table.containsKey(key)) {
            ESRIShapeCreator c = table.get(key);
            buf.order(inorder);
            return c.get(buf);
        } else {
            buf.order(inorder);
            throw new UnsupportedESRIShapeException("Shapes of type " + key
                    + " not supported in this version");
        }
    }

    public ESRIShape get(ByteBuffer buf, int index)
            throws MalformedESRIRecordException, UnsupportedESRIShapeException {
        int recordnumber = 0;

        if (index < 1) {
            throw new IndexOutOfBoundsException(
                    "Index has to be greater than 0.");
        }

        while (buf.hasRemaining()) {
            ESRIRecordHeader rechdr = new ESRIRecordHeader(buf);
            recordnumber = rechdr.getRecordNumber();
            if (recordnumber == index) {
                return getShape(buf);
            }
        }

        if (index > recordnumber) {
            throw new IndexOutOfBoundsException("Trying to get shape " + index
                    + " of " + recordnumber);
        } else {
            throw new MalformedESRIRecordException("Missing shape in ESRIFile");
        }

    }
}

/**
 * A class representing the DBase file specified in the ESRI Specification.
 * 
 * @version 1, $Date: 2006/01/11 22:15:43 $
 * @author Daniel Ahlin
 */
class ESRIDBFFile {
    /**
     * The name of the file.
     */
    String filename;

    /**
     * The records in the file.
     */
    Vector records = new Vector();

    /**
     * A mapping of attribute name -> field index table
     */
    Hashtable<String, Integer> fieldNames = new Hashtable<String, Integer>();

    /**
     * Tries to read a ESRIDBFile from the provided filename
     * 
     * @param filename
     */
    public ESRIDBFFile(String filename) throws ESRIDBFileException,
            FileNotFoundException {
        this.filename = filename;
        try {
            DBFReader reader = new DBFReader(new FileInputStream(filename));

            // Find and index the name of the fields.
            for (int i = 0; i < reader.getFieldCount(); i++) {
                DBFField field = reader.getField(i);
                this.fieldNames.put(field.getName(), new Integer(i));
            }

            // Read all records at this point. This may be a waste of
            // memory, but on the other hand it allows us to ensure
            // that any error caused by the dbfile itself is triggered
            // here. Note that this assumes concordance with the ESRI
            // Specification stating that records must appear in the
            // same order as the shape features they describe.
            for (int i = 0; i < reader.getRecordCount(); i++) {
                this.records.add(reader.nextRecord());
            }

        } catch (DBFException e) {
            throw new ESRIDBFileException("Error reading ESRI database file: "
                    + filename);
        } catch (IOException e) {
            throw new ESRIDBFileException("Error reading ESRI database file: "
                    + filename);
        }
    }

    /**
     * Returns the specified field of the specified record.
     * 
     * @param field the name of the field to get
     * @param record the index of the record to get
     */
    public Object getValue(String field, int record)
            throws ESRIDBNoSuchFieldException {
        return getRecord(record)[getFieldIndex(field)];
    }

    /**
     * Returns the index of the specified field, or -1 if no such field exists.
     * 
     * @param name the name of the field to get.
     */
    public int getFieldIndex(String name) throws ESRIDBNoSuchFieldException {
        Integer index = this.fieldNames.get(name);
        if (index == null) {
            throw new ESRIDBNoSuchFieldException();
        } else {
            return index.intValue();
        }
    }

    /**
     * Returns the fieldnames matching the provided regular expression.
     * 
     * @param regex the regex to match.
     */
    public Vector<String> getMatchingFieldNames(String regex) {
        Vector<String> res = new Vector<String>();

        for (Enumeration<String> e = fieldNames.keys(); e.hasMoreElements();) {
            String fieldName = e.nextElement();
            if (fieldName.matches(regex)) {
                res.add(fieldName);
            }
        }

        return res;
    }

    /**
     * Fetches a record from the database.
     * 
     * @param record, the record to get.
     */
    public Object[] getRecord(int record) {
        return (Object[]) this.records.get(record);
    }
}

/**
 * Represents an error reading an ESRI DBase file.
 * 
 * @version 1, $Date: 2006/01/11 22:15:43 $
 * @author Daniel Ahlin
 */
class ESRIDBFileException extends Exception {
    /**
	 * 
	 */
    private static final long serialVersionUID = 2480055653453979557L;

    /**
     * Creates a new ESRIDBFileException with the provided message.
     * 
     * @param message a description of the condition causing the error.
     */
    ESRIDBFileException(String message) {
        super(message);
    }
}

/**
 * Represents the nonexistance of a field .
 * 
 * @version 1, $Date: 2006/01/11 22:15:43 $
 * @author Daniel Ahlin
 */
class ESRIDBNoSuchFieldException extends Exception {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1135459522188706082L;

    /**
     * Creates a new ESRIDBNoSuchFieldException
     */
    ESRIDBNoSuchFieldException() {
        super();
    }
}
