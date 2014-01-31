// 	$Id: ESRIInputStream.java,v 1.6 2006/09/18 09:58:02 alexius Exp $

package StratmasClient;

import java.net.URL;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.FileNotFoundException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

import com.linuxense.javadbf.DBFReader;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFException;

/**
 * ESRI import tools.
 *
 * @version 1, $Date: 2006/09/18 09:58:02 $
 * @author  Daniel Ahlin
*/
public class ESRIInputStream extends InputStream
{
    /**
     * Root attributes to use.
     */
    static String rootAttributes = 
	"xmlns:sp=\"http://pdc.kth.se/stratmasNamespace\" " +
	"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
        "xmlns:xi=\"http://www.w3.org/2001/XInclude\" " +
        "xsi:schemaLocation=\"http://pdc.kth.se/stratmasNamespace schemas/stratmasProtocol.xsd\"";
    
    /**
     * The stream containing the ESRI shapes.
     */
    InputStream shapeStream;

    /**
     * The factory producing the shapes.
     */
    ESRIShapeFactory factory;

    /**
     * The shape selection, or null if none.
     */
    String shapeSelection;

    /**
     * The part selection, or null if none.
     */
    String partSelection;

    /**
     * The output buffer.
     */
    byte[] buffer;

    /**
     * Default buffer size.
     */    
    static int BUFFER_SIZE = 1024;

    /**
     * The current position in the outBuffer, points to the first
     * unread point in the buffer.
     */
    int bufferStart;

    /**
     * The current number of unread data in buffer.
     */
    int bufferLength;

    /**
     * Indicates wheather this stream is closed.
     */
    boolean isClosed;

    /**
     * The encoding to use by toXML, default is ISO-8859-1
     */
    static String encoding = "ISO-8859-1";

    /**
     * An encoded version of the last atom.
     */
    byte[] atom;

    /**
     * The index into the encoded version of the last atom.
     */
    int atomIndex;

    /**
     * True if endTag has been written to the out buffer.
     */
    boolean endTagDone = false;

    /**
     * True if the shapes have been written to the out buffer.
     */
    boolean shapesDone = false;

    /**
     * Creates a new ESRIInputStream given the location of the data and the db.
     *
     * @param shapeUrl the url to the shape file.
     * @param dbUrl the url to the db file.
     */
    public ESRIInputStream(URL shapeUrl, URL dbUrl) throws IOException
    {
	InputStream dbStream = null;
	
	try {
	    this.shapeStream = shapeUrl.openStream();
	    
	    this.factory = new ESRIShapeFactory(shapeStream);

	    // Check for content specifications
	    if (shapeUrl.getQuery() != null) {
		String[] fields = shapeUrl.getQuery().split(":");
		// FIXME Generate error when asking for a part that
		// does not exist.
		if (fields.length == 1) {
		    factory.setShape(fields[0]);
		} else if (fields.length == 2) {
		    factory.setShape(fields[0]);
		    factory.setPart(fields[1]);
		} else {
		    throw new IOException("Unsupported shape selection request: " + 
					  shapeUrl.getQuery());
		}
	    }
	    
	    // Check for db
	    if (dbUrl != null) {	    
		try {
		    dbStream = dbUrl.openStream();
		    factory.setDB(new ESRIDBF(dbStream));
		} catch (ESRIDBFileException e) {
		    throw new IOException(e.getMessage());
		}
	    }
	    else {
		 // Try the same filename but with .dbf as suffix.
		 String shapeFileName = shapeUrl.getPath();
		 dbUrl = new URL(shapeUrl.getProtocol() + ":" +
				 shapeFileName.substring(0, shapeFileName.toLowerCase().lastIndexOf(".shp")) + ".dbf");
		 try {
		      dbStream = dbUrl.openStream();
		      factory.setDB(new ESRIDBF(dbStream));
		 } catch (ESRIDBFileException e) {
		      Debug.err.println(dbUrl.getPath() + " not found");
		 } catch (FileNotFoundException e) {
		     Debug.err.println(dbUrl.getPath() + " not found");
		 }
	    }

	    // Set document header as the first atom.
	    this.atom = getDocumentHeader();
	    this.atomIndex = 0;
	    // Create buffer.
	    this.buffer = new byte[BUFFER_SIZE];
	    // Start att buffer start:
	    this.bufferStart = 0;
	    // Set no clean data:
	    this.bufferLength = 0;
	} finally {
	    if (dbStream != null) {
		dbStream.close();		    
	    }
	}
    }

    /**
     * Reads and returns the next byte and returns it as an int.
     * @return next byte in stream (as an int).
     * @throws IOException if the underlying shape stream causes an
     * IOException during the skip, if the data read from the same
     * does not follow the ESRI specification or if the stream has
     * been closed.
     */
    public int read() throws IOException
    {
	if (isClosed) {
	    throw new IOException("Invalid operation on closed stream.");
	}
	
	int res = -1;

	refill(1);
	if (bufferLength > 0) {
	    res = (buffer[bufferStart] & 0xff);
	    bufferStart = (bufferStart + 1) % buffer.length;
	    bufferLength--;
	}
	
	return res;
    }

    /**
     * Reads at most specified number of bytes to b, begining at
     * off. Returns number of bytes actually read.
     *
     * @param b buffer to read into.
     * @param off start in b.
     * @param len max number of bytes to read.
     * @return number of bytes read.
     * @throws IOException if the underlying shape stream causes an
     * IOException during the skip or if the data read from the same
     * does not follow the ESRI specification.
     */
    public int read(byte[] b, int off, int len) throws IOException
    {
	// Check fringe cases
	if (isClosed) {
	    throw new IOException("Invalid operation on closed stream.");
	} else if (len == 0) {
	    return 0;
	}

	// Refill buffer
	refill(len);

	// Returned length
	int resLen;

	int available = available();
	if (available > 0) {
	    // Data in buffer, write min of len and available.
	    resLen = len < available ? len : available;
	    circularRead(buffer, bufferStart, b, off, resLen);
	    bufferStart = (bufferStart + resLen) % buffer.length;
	    bufferLength -= resLen;
	} else if (len == 0) {
	    resLen = 0;
	} else {
	    resLen = -1;
	}

	return resLen;
    }

    /**
     * Refills the buffer if possible and necessary.
     * 
     * @param n the number of bytes requested by the causing read.
     */
    void refill(int n) throws IOException
    {
	if (n <= bufferLength ||
	    bufferLength == buffer.length) {
	    // Request fulfilled by data in buffer or buffer filled,
	    // return.
	    return;
	}
	
	// While free space available and atoms pending.
	for (int freeSpace = buffer.length - bufferLength;
	     freeSpace > 0 && atom != null;) {
	    // Write min of what is left of current atom and free space.
	    int write = freeSpace < atom.length - atomIndex ?
		freeSpace : atom.length - atomIndex;
	    // Try write.
	    int written = circularWrite(atom, atomIndex, buffer, 
					(bufferStart + bufferLength) % buffer.length, write);
	    bufferLength += written;
	    atomIndex += written;
	    freeSpace -= written;
	    // If finished with this atom, try getting a new one.
	    if (atomIndex == atom.length) {
		atom = nextAtom();
		atomIndex = 0;
	    }
	}
    }
    
    /**
     * Returns the next atom to process, or null if none.
     */
    byte[] nextAtom() throws IOException
    {
	byte[] res = null;
	if (!shapesDone) {
	    res = factory.getNextAtom(shapeStream);
	    shapesDone = (res == null);
	    if (shapesDone && !endTagDone) { 
		res = getDocumentFooter();
		endTagDone = true;
	    }
	}

	return res;
    }

    /**
     * Writes provided buffer to the provided circular buffer and
     * returns the number of bytes written. Writes at most 
     * min(dest.length, length) bytes.
     *
     * @param src the source buffer.
     * @param srcpos the positition in the source buffer.
     * @param dest the destination buffer.
     * @param destpos the position in the destination buffer.
     * @param length the length in the destination buffer.
     */
    int circularWrite(byte[] src, int srcpos, byte[] dest, 
		       int destpos, int length)
    {
	// First write is special.
	int firstlen = dest.length - destpos < length ?
	    dest.length - destpos : length;
	System.arraycopy(src, srcpos, dest, destpos, firstlen);
	int secondlen = 0;
	if (length > firstlen) {
	    secondlen = (destpos - 1) < (length - firstlen) ?
		(destpos - 1) : (length - firstlen);
	    System.arraycopy(src, srcpos + firstlen, dest, 0, 
			     secondlen);
	}
	
	return firstlen + secondlen;	
    }

    /**
     * Writes provided circular buffer src to the provided buffer dest
     * and returns the number of bytes written. Writes at most
     * min(src.length, length) bytes.
     *
     * @param src the source buffer.
     * @param srcpos the positition in the source buffer.
     * @param dest the destination buffer.
     * @param destpos the position in the destination buffer.
     * @param length the length in the destination buffer.
     */
    int circularRead(byte[] src, int srcpos, byte[] dest, 
		     int destpos, int length)
    {
	// First write: min of length and whats left of src.
	int resLen = length < (src.length - srcpos) ?
	    length : (src.length - srcpos);
	System.arraycopy(src, srcpos, dest, destpos, resLen);
	// Second write: min of : (length - resLen) and (src.length - resLen).
	if (resLen < length) {
	    int secondLen = (length - resLen) < (src.length - resLen) ?
		(length - resLen) : (src.length - resLen);
	    System.arraycopy(src, 0, dest, destpos + resLen, secondLen);
	    resLen += secondLen;
	}

	return resLen;	    
    }
	
    /**
     * Returns the number of bytes available without blocking.
     */
    public int available()
    {
	return bufferLength;
    }

    /**
     * Closes this stream.
     * @throws IOException if the underlying ESRI stream throws an
     * IOException on close().
     */
    public void close() throws IOException
    {
	if (isClosed) {
	    throw new IOException("Invalid operation on closed stream.");
	}
	try {
	    shapeStream.close();
	    this.isClosed = true;
	} catch (IOException e) {
	    throw new IOException("Error closing esri stream: " + e.getMessage());
	}
    }

    /**
     * Skips at most the provided number of bytes of the input.
     *
     * @param n bytes to skip.
     * @return number of bytes skipped.
     * @throws IOException if the underlying shape stream causes an
     * IOException during the skip or if the data read from the same
     * does not follow the ESRI specification.
     */
    public long skip(long n) throws IOException
    {
	// Check fringe cases
	if (isClosed) {
	    throw new IOException("Invalid operation on closed stream.");
	}
	if (n <= 0) {
	    return 0;
	}

	long skipped = 0;

	while (skipped < n) {
	    int filler = (n - skipped) < Integer.MAX_VALUE ?
		(int) (n - skipped) : Integer.MAX_VALUE;
	    refill(filler);
	    // Number of positions to move start.	    
	    int skippable = available() < filler ?
		available() : filler;
	    if (skippable == 0) {
		break;
	    }
	    bufferStart = (bufferStart + skippable) % buffer.length;
	    bufferLength -= skippable;	    

	    skipped += skippable;
	}

	return skipped;
    }

    /**
     * Returns the document header as a byte array.
     */
    byte[] getDocumentHeader()
	throws IOException
    {
	return encode("<?xml version=\"1.0\"?>" +
		      "<map xsi:type=\"sp:Composite\" " + 
		      rootAttributes + ">");
    }

    /**
     * Returns the as a byte array.
     */
    byte[] getDocumentFooter()
	throws IOException
    {
	return encode("</map>");
    }

    /**
     * Encodes the provided String.
     *
     * @param string string to encode.
     */
    static byte[] encode(String string) throws IOException
    {
	return string.getBytes(encoding);
    }
        

    /**
     * Test main function.
     *
     * @param argv argv[0] = shapeURL, argv[1] = dbUrl
     */    
    public static void main(String argv[])
    {
	String usage = "Usage: java ESRIInputStream shapeUrl dbUrl";

	if (argv.length < 1 || 
	    argv.length > 2) {
	    System.err.println(usage);
	    System.exit(1);
	}
	
	try {
	    ESRIInputStream xmlStream = new ESRIInputStream(new URL(argv[0]), 
							    argv.length > 1 ? 
							    new URL(argv[1]) : null);

	    boolean testSingular = false;
	    if (testSingular) {
		for (int i = xmlStream.read(); i != -1; i = xmlStream.read()) {
		    System.out.write(i);
		}
	    }
	    
	    boolean testMultiple = false;
	    if (testMultiple) {
		java.util.Random random = new java.util.Random();
		
		while(true) {
		    int n = random.nextInt() % 4096;
		    if (n < 0) {
			n = -n;
		    }
		    byte[] b = new byte[n];
		    int read = xmlStream.read(b, 0, b.length);
		    if (read == -1) {
			break;
		    }
		    System.err.println(n + " " + read);
		    System.out.write(b, 0, read);		
		}
	    }

	    boolean testSkip = false;
	    if (testSkip) {
		java.util.Random random = new java.util.Random();
		
		while(true) {
		    int n = random.nextInt() % 4096;
		    if (n < 0) {
			n = -n;
		    }
		    long skipped = xmlStream.skip(n);
		    if (skipped == 0) {
			break;
		    }
		    System.err.println(n + " " + skipped);
		}
	    }
	    
	    System.out.println();
	} catch (Exception e) {
	    System.err.println(e.getMessage());
	    e.printStackTrace();
	    System.exit(1);
	}	
    }
}

/**
 * Represents the header of an ESRI file
 */
class ESRIShapefileHeader
{
    /**
     * Code of the file.
     */
    int code;

    /**
     * Length of the file
     */
    int fileln; // In 16-bit integers

    /**
     * Version of the file
     */
    int version;

    /**
     * Shape type in this file.
     */
    int shapetype; 

    /**
     * X min of bounding box.
     */
    double xmin;

    /**
     * X max of bounding box.
     */
    double xmax;

    /**
     * Y min of bounding box.
     */
    double ymin;

    /**
     * Y max of bounding box.
     */
    double ymax;

    /**
     * Z min of bounding box.
     */
    double zmin;

    /**
     * Z max of bounding box.
     */
    double zmax;

    /**
     * M min of bounding box.
     */
    double mmin;

    /**
     * M max of bounding box.
     */
    double mmax;

    /**
     * Creates a new header from the provided stream.
     */    
    public ESRIShapefileHeader(InputStream stream) throws IOException
    {
	// Read the header:
	ByteBuffer buffer = ByteBuffer.wrap(new byte[9*4 + 8*8]);
	ESRIShapeFactory.readStream(stream, buffer);


	// The first two are bigendian...
	buffer.order(ByteOrder.BIG_ENDIAN);
	code = buffer.getInt();
	// Skip unused values
	buffer.position(buffer.position() + (5 * 4));
	fileln = buffer.getInt();	
	// ...and the rest of the header is littleendian, yay!
	buffer.order(ByteOrder.LITTLE_ENDIAN);
	version = buffer.getInt();
	shapetype = buffer.getInt();
	xmin = buffer.getDouble();
	xmax = buffer.getDouble();
	ymin = buffer.getDouble();
	ymax = buffer.getDouble();
	zmin = buffer.getDouble();
	zmax = buffer.getDouble();
	mmin = buffer.getDouble();
	mmax = buffer.getDouble();	
    }

    /**
     * Returns the file code.
     */
    public int getFileCode()
    {
	return code;
    }

    /**
     * Returns the file length.
     */
    public int getFileLength()
    {
	return fileln;
    }

    /**
     * Returns the ESRI version.
     */
    public int getVersion()
    {
	return version;
    }

    /**
     * Returns the type of shapes stored in the file.
     */
    public int getShapeType()
    {
	return shapetype;
    }

    /**
     * Returns the X min of the bounding box of all shapes in this
     * file.
     */
    public double getXmin()
    {
	return xmin;
    }

    /**
     * Returns the X max of the bounding box of all shapes in this
     * file.
     */
    public double getXmax()
    {
	return xmax;
    }

    /**
     * Returns the Y min of the bounding box of all shapes in this
     * file.
     */
    public double getYmin()
    {
	return ymin;
    }

    /**
     * Returns the Y max of the bounding box of all shapes in this
     * file.
     */
    public double getYmax()
    {
	return ymax;
    }

    /**
     * Returns the Z min of the bounding box of all shapes in this
     * file.
     */
    public double getZmin()
    {
	return zmin;
    }

    /**
     * Returns the Z Max of the bounding box of all shapes in this
     * file.
     */
    public double getZmax()
    {
	return zmax;
    }

    /**
     * Returns the M min of the bounding box of all shapes in this
     * file.
     */
    public double getMmin()
    {
	return mmin;
    }

    /**
     * Returns the M max of the bounding box of all shapes in this
     * file.
     */
    public double getMmax()
    {
	return mmax;
    }
}

class ESRIShapeFactory
{
    /**
     * Table containing streamers for different shape types.
     */
    ESRIRecordStreamer[] streamers;

    /**
     * Shape specification, or null if none
     */
    String shape = null;

    /**
     * Part specification, or null if none.
     */
    String part = null;

    /**
     * The database this factory uses, or null if none.
     */
    ESRIDBF db = null;

    /**
     * The header of the shape file.
     */
    ESRIShapefileHeader header;

    /**
     * Buffer holding the last read shape record prefix.
     */
    ByteBuffer inBuffer = ByteBuffer.wrap(new byte[2*4]);

    /**
     * Last shape index read. 
     */
    int lastIndex = -1;

    /**
     * True if we are currently processing a shape.
     */
    boolean inShape = false;

    /**
     * Streamer to use.
     */
    ESRIRecordStreamer streamer;

    /**
     * Creates a new factory
     * @param stream stream to read esri from.
     */
    public ESRIShapeFactory(InputStream stream) throws IOException
    {
	streamers = new ESRIRecordStreamer[32];
	registerStreamer(new ESRINullShapeStreamer());
	registerStreamer(new ESRIPolygonStreamer());
	
	ESRIShapefileHeader header = new ESRIShapefileHeader(stream);
	streamer = streamers[header.getShapeType()];
    }

    /**
     * Registers a new streamer.
     *
     * @param streamer the new streamer
     */
    public void registerStreamer(ESRIRecordStreamer streamer)
    {
	try {
	    streamers[streamer.getType()] = streamer;
	} catch (IndexOutOfBoundsException e) {
	    throw new AssertionError("Unsupported shape type registred: " + streamer.getType());
	}
    }

    /**
     * Sets the db of this factory
     *
     * @param db the db to use.
     */
    public void setDB(ESRIDBF db)
    {
	this.db = db;
    }

    /**
     * Returns the db of this factory or null if none
     */
    public ESRIDBF getDB()
    {
	return this.db;
    }

    /**
     * Sets processing of a specific part.
     *
     * @param part the part to process
     */
    public void setPart(String part)
    {
	this.streamer.setPart(part);
    }

    /**
     * Sets processing of a specific shape.
     *
     * @param shape the shape to process
     */
    public void setShape(String shape)
    {
	this.shape = shape;	
    }

    /**
     * Returns the specific part to process or null if none.
     */
    public String getPart()
    {
	return this.part;
    }

    /**
     * Returns the specific shape to process or null if none.
     */
    public String getShape()
    {
	return this.shape;
    }

    /**
     * Fills provided array-backed buffer with data from stream.
     *
     * @param stream stream to read from.
     * @param buffer buffer to read from, note that buffer.hasArray()
     * have to be true.
     */
    static int readStream(InputStream stream, ByteBuffer buffer) 
	throws IOException
    {
	int i = 0;
	while (i < buffer.array().length) {
	    int read = stream.read(buffer.array(), i, buffer.array().length - i);
	    if (read == -1) {
		return read;
	    } else {
		i += read;
	    }
	}

	return i;
    }

    /**
     * Fills provided array-backed buffer with data from stream.
     *
     * @param stream stream to read from.
     * @param buffer buffer to read from, note that buffer.hasArray()
     * have to be true.
     * @param n number of bytes to read
     */
    static int readStream(InputStream stream, ByteBuffer buffer, int n) 
	throws IOException
    {
	int i = 0;
	while(i < n) {
	    int read = stream.read(buffer.array(), i, n - i);
	    if (read == -1) {
		return read;
	    } else {
		i += read;
	    }
	}

	return i;
    }

    /**
     * Skips requested number of bytes in stream
     *
     * @param stream stream to skip in.
     * @param skip number of bytes to skip.
     */
    static long skipStream(InputStream stream, long skip) 
	throws IOException
    {
	int skipped = 0;

	for(; skipped != skip; skipped += stream.skip(skip - skipped));

	return skipped;
    }



//     int a = 0;
//     public byte[] getNextAtom(InputStream stream) throws IOException
//     {
// 	if (a < 10000) {
// 	    return ESRIInputStream.encode(Integer.toString(a++) + "\n"); 
// 	} else {
// 	    return null;
// 	}
//     }


    /**
     * Returns a byte representation of the next atom.
     * 
     * @param stream the stream to read esri data from.
     */
    public byte[] getNextAtom(InputStream stream) throws IOException
    {
	if (inShape) {
	    byte[] res =  streamer.nextAtom(stream);
	    if (res != null) {
		return res;
	    } else {
		// No more atoms in this shape, terminate it and
		// recursively handle this.
		inShape = false;
		return getNextAtom(stream);
	    }
	} else {
	    // Find next wanted shape, if any.
	    String identifier;
	    // Not very nice, but this loop terminates either by
	    // internal returns/throws, or by break, if break a new
	    // polygon is ready to be read.
	    while (true) {
		// Read record header.
		int read = readStream(stream, inBuffer);
		if (read == -1) {
		    // EOF found, return null.
		    return null;
		} else if (read != inBuffer.array().length) {
		    // Not full read.
		    throw new IOException("Incomplete ESRI record");
		}
		
		// Check count, note that esri count records from 1:
		if (inBuffer.getInt(0) != (lastIndex + 1) + 1) {
		    throw new IOException("Error: Non-sequential " + 
					  "records found in ESRI input.");
		}
		this.lastIndex = inBuffer.getInt(0) - 1;

		// Create name for this shape, either from db or from sequence number.
		identifier = db != null ? db.getShapeName(inBuffer.getInt(0) - 1) : 
		    Integer.toString(inBuffer.getInt(0));

		// If specific shape set and this is not it, skip it.
		if (getShape() == null ||
		    getShape().equals(identifier)) {
		    break;
		} else {
		    // Length in 16-bit words.
		    skipStream(stream, 2 * inBuffer.getInt(4));
		}
	    }

	    // re-initialize streamer.
	    streamer.newShape(stream, "shapes", identifier);

	    // get and return first atom.
	    inShape = true;
	    return streamer.nextAtom(stream);
	}
    }
}

/**
 * Interface specifing capabilities of shapestreamers.
 */
interface ESRIRecordStreamer
{
    /**
     * Prepares streamer for a new shape.
     *
     * @param stream stream to read esri from.
     * @param tag tag to use, or null for default.
     * @param identifier identifier to use, or null for default.
     */
    public abstract void newShape(InputStream stream, 
				    String tag, String identifier) 
	throws IOException;

    /**
     * Returns the next atom of the current shape, or null if current
     * shape is finished.
     *
     * @param stream stream to read esri from.
     */
    public abstract byte[] nextAtom(InputStream stream)
	throws  IOException;
    
    /**
     * Returns the type this streamer handles.
     */
    public abstract int getType();

    /**
     * Sets the processing of just a specific part of the record.
     *
     * @param part the identifier of the part to process.
     */
    public abstract void setPart(String part);
}

/**
 * Base class for most shapestreamers.
 */
abstract class DefaultShapeStreamer implements ESRIRecordStreamer
{

    /**
     * The part to process, or null if none.
     */
    String part = null;

    /**
     * Prepares streamer for a new shape.
     *
     * @param stream stream to read esri from.
     * @param tag tag to use, or null for default.
     * @param identifier identifier to use, or null for default.
     */
    public abstract void newShape(InputStream stream, 
				    String tag, String identifier) 
	throws IOException;

    /**
     * Returns the next atom of the current shape, or null if current
     * shape is finished.
     *
     * @param stream stream to read esri from.
     */
    public abstract byte[] nextAtom(InputStream stream)
	throws  IOException;

    /**
     * Returns the type this streamer handles.
     */
    public abstract int getType();

    /**
     * Returns the XML type of this shape, or null if no type necessary.
     */
    public abstract String getXMLType();

    /**
     * Returns the header of the xml element to the provided stream.
     *
     * @param tag the tag to use.
     * @param identifier the identifier to use, or null if none.
     */
    byte[] encodeShapeHeader(String tag, String identifier) 
	throws IOException
    {
	return ESRIInputStream.encode("<" + tag +
				      (getXMLType() != null ? " xsi:type=\"" + getXMLType() + "\"" : "") +
				      (identifier != null ? " identifier=\"" + identifier + "\"" : "") +
				      ">");
    }

    /**
     * Sets the processing of just a specific part of the record.
     *
     * @param part the identifier of the part to process.
     */
    public void setPart(String part)
    {
	this.part = part;
    }

    /**
     * Returns the identifier of the part set for processing or null
     * if none.
     */
    public String getPart()
    {
	return this.part;
    }

    /**
     * Returns true if the streamer should process a part with the
     * supplied identifier.
     *
     * @param identifier
     */
    boolean isPartEnabled(String identifier)
    {
	return getPart() == null || getPart().equals(identifier);
    }
}


/**
 * Methods for the null shape.
 */
class ESRINullShapeStreamer implements ESRIRecordStreamer
{
    /**
     * The type identifier of the null shape.
     */
    static int type = 0;
 
    /**
     * Creates a new ESRINullShapeStreamer
     */
    public ESRINullShapeStreamer()
    {
    }

    /**
     * Returns the type this streamer handles.
     */
    public int getType() 
    {
	return type;
    }

    /**
     * Sets the part this streamer handles, does nothing.
     *
     * @param string ignored string.
     */
    public void setPart(String string) 
    {
    }

    /**
     * Prepares streamer for a new shape.
     *
     * @param stream stream to read esri from.
     * @param tag tag to use, or null for default.
     * @param identifier identifier to use, or null for default.
     */
    public void newShape(InputStream stream, 
			 String tag, String identifier) 
	throws IOException
    {}

    /**
     * Returns the next atom of the current shape, or null if current
     * shape is finished. Always returns null.
     *
     * @param stream stream to read esri from.
     */
    public byte[] nextAtom(InputStream stream)
	throws  IOException
    {
	return null;
    }
}

/**
 * Class containing methods for streaming ESRIPoints
 */
class ESRIPolygonStreamer extends DefaultShapeStreamer
{
    /**
     * Polygons type identifier.
     */
    static int type = 5;

    /**
     * The array used to read in header data.
     */
    ByteBuffer buffer = ByteBuffer.wrap(new byte[1*4 + 4*8 + 2*4]);

    /**
     * Current point index.
     */
    int pointIndex = 0;

    /**
     * Current part index.
     */
    int partIndex = 0;

    /**
     * Number of points in current part.
     */
    int currentPoints = 0;

    /**
     * Identifier for this shape.
     */
    String identifier = null;

    /**
     * Tag for this shape.
     */
    String tag = null;

    /**
     * Number of parts for current shape.
     */
    int numParts;

    /**
     * Number of points for this shape (all parts)
     */
    int numPoints;

    /**
     * Buffer contaning the part boundaries of the current shape.
     */
    ByteBuffer parts;

    /**
     * Buffer containig the current point.
     */
    ByteBuffer pointBuffer = ByteBuffer.wrap(new byte[2*8]);

    /**
     * Current parts first x coordinate.
     */
    double firstX;

    /**
     * Current parts first y coordinate.
     */
    double firstY;

    /**
     * Current parts previous x coordinate.
     */
    double prevX;

    /**
     * Current parts previous y coordinate.
     */
    double prevY;

    /**
     * Indicates if shape end tag is processed.
     */
    boolean shapeEndTagDone = false;

    /**
     * Indicates if shape header is processed.
     */
    boolean shapeHeaderDone = false;

    /**
     * Indicates if part end tag is processed.
     */
    boolean partEndTagDone = false;

    /**
     * Indicates if streamer is currently processing a part.
     */
    boolean inPart = false;

    /**
     * Creates a new ESRIPolygonStreamer.
     */
    public ESRIPolygonStreamer() throws IOException
    {
	buffer.order(ByteOrder.LITTLE_ENDIAN);
	pointBuffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Prepares streamer for a new shape.
     *
     * @param stream stream to read esri from.
     * @param tag tag to use, or null for default.
     * @param identifier identifier to use, or null for default.
     */
    public void newShape(InputStream stream, 
			 String tag, String identifier) 
	throws IOException
    {
	this.pointIndex = 0;
	this.partIndex = 0;
	this.identifier = identifier;
	this.tag = tag;
	this.shapeHeaderDone = false;
	this.shapeEndTagDone = false;
	this.partEndTagDone = false;
	this.inPart = false;

	// Read Header
	ESRIShapeFactory.readStream(stream, buffer);

	// Check type.
	if (buffer.getInt(0) != type) {
	    throw new IOException("Malformed Polygon found.");
	}
	
	// Get num part and points.
	this.numParts = buffer.getInt(36);
	this.numPoints = buffer.getInt(40);

	// Get array containing parts start points, add synthethic end
	// index for last part.
	this.parts = ByteBuffer.wrap(new byte[(numParts + 1) * 4]);
	this.parts.order(ByteOrder.LITTLE_ENDIAN);
	ESRIShapeFactory.readStream(stream, parts, numParts * 4);
	// Put stop index for last part into parts.
	parts.putInt(4 * numParts, numPoints);
	//System.err.println("Shape: " + identifier + " numParts: " + numParts + " numPoints: " + numPoints);
    }

    /**
     * Returns the next atom of the current shape, or null if current
     * shape is finished. Always returns null.
     *
     * @param stream stream to read esri from.
     */
    public byte[] nextAtom(InputStream stream)
	throws  IOException
    {
	if (!shapeHeaderDone) {
	    shapeHeaderDone = true;
	    return encodeShapeHeader(tag, identifier);
	} else if (partIndex < numParts) {
	    // Process parts.
	    if (!inPart) {
		// Skip while parts left and current part not enabled.
		while (partIndex < numParts && 
		       !isPartEnabled(Integer.toString(partIndex + 1))) {
		    currentPoints = parts.getInt((partIndex + 1) * 4) - parts.getInt(partIndex * 4);
		    ESRIShapeFactory.skipStream(stream, currentPoints * 2 * 8);
		    partIndex++;
		}
		if (partIndex < numParts && 		    
		    isPartEnabled(Integer.toString(partIndex + 1))) {
		    // Header
		    currentPoints = parts.getInt((partIndex + 1) * 4) - parts.getInt(partIndex * 4);
		    pointIndex = 0;
		    if (currentPoints < 3) {
			throw new IOException("Illegal polygon using less than 3 points found.");
		    }

		    inPart = true;

		    // If this is the only part in this shape, don't
		    // create a separate part for it. Note that if
		    // this is the only part due to selection, it will
		    // be a subshape of the shape.
		    if (numParts == 1) {
			return nextAtom(stream);
		    } else {
			return encodePartHeader(Integer.toString(partIndex + 1));
		    }		    
		} else {
		    // No matching part found.
		    return null;
		}
	    } else {
		// currently processing part content
		if (pointIndex < currentPoints) {
		    if (pointIndex == 0) {
			// Special treatment of first point:
			ESRIShapeFactory.readStream(stream, pointBuffer);
			this.firstX = pointBuffer.getDouble(0);
			this.firstY = pointBuffer.getDouble(8);
			this.prevX = firstX;
			this.prevY = firstY;
			pointIndex++;
		    }

		    // Find first point differing from last point.
		    double x;
		    double y;
		    do {
			pointIndex++;
			ESRIShapeFactory.readStream(stream, pointBuffer);
			x = pointBuffer.getDouble(0);
			y = pointBuffer.getDouble(8);
		    } while (pointIndex < currentPoints &&
			     prevX == x &&
			     prevY == y);

		    if (pointIndex < currentPoints) {
			byte[] res = encodeLine(Integer.toString(pointIndex - 1), 
						prevX, prevY, x, y);
			prevX = x;
			prevY = y;
			return res;
		    } else {
			// No points left, recursively take care of end of part.
			return nextAtom(stream);
		    }
		} else if (pointIndex == currentPoints &&
			   prevX != firstX &&
			   prevY != firstY) {
		    //If the last point was not same as the first, tie polygon together:		    
		    // Make check condition false.		    
		    byte res[] = encodeLine(Integer.toString(pointIndex - 1), prevX, prevY, firstX, firstY);
		    pointIndex++;
		    return res;		    
		} else {
		    // Finished, mark part as ended and return end tag.
		    partIndex++;
		    inPart = false;
		    // If this is the only part, end the shape
		    // instead, (see above)
		    if (numParts == 1) {
			return nextAtom(stream);
		    }
		    return ESRIInputStream.encode("</shapes>");
		}
	    }
	} else if (!shapeEndTagDone) {
	    // Close this composite
	    shapeEndTagDone = true;
	    return ESRIInputStream.encode("</" + tag + ">");
	} else {
	    return null;
	}
    }

    /**
     * Encodes a line from (x1,y1) -> (x2, y2).
     *
     * @param identifier the identifier of the line.
     * @param x1 starting x coordinate
     * @param y1 starting y coordinate
     * @param x2 ending x coordinate
     * @param y2 ending y coordinate
     */
    byte[] encodeLine(String identifier, double x1, double y1, double x2, double y2) throws IOException
    {
  	return ESRIInputStream.encode("<curves xsi:type=\"sp:Line\" identifier=\"" + identifier + "\">" +
  				      "<p1><lat>" + y1 + "</lat><lon>" + x1 + "</lon></p1>" +
  				      "<p2><lat>" + y2 + "</lat><lon>" + x2 + "</lon></p2>" +
  				      "</curves>");
    }

    /**
     * Encodes a part header.
     *
     * @param identifier the identifier of the header.
     */
    byte[] encodePartHeader(String partIdentifier) throws IOException
    {
	return ESRIInputStream.encode("<shapes xsi:type=\"sp:Polygon\"" + 
				      " identifier=\"" + partIdentifier + 
				      "\">");
    }

    

    /**
     * Returns the type this streamer handles.
     */
    public int getType() 
    {
	return type;
    }

    /**
     * Returns the type this streamer handles.
     */
    public String getXMLType() 
    {
	if (numParts == 1) {
	    return "sp:Polygon";
	} else {
	    return "sp:Composite";
	}
    }
}

/**
 * A class representing the DBase file specified in the ESRI
 * Specification.
 *
 * @version 1, $Date: 2006/09/18 09:58:02 $
 * @author  Daniel Ahlin
 */
class ESRIDBF
{
    /**
     * Field regex where we expect to get the name of the shape.
     */
    static String nameRegexp = ".*NAME.*";

    /**
     * The field name to use.
     */
    String nameField;

    /**
     * The records in the db.
     */
    Vector records = new Vector();

    /**
     * A mapping of attribute name -> field index table
     */
    Hashtable fieldNames = new Hashtable();

    /**
     * Tries to read a ESRIDBFile from the provided stream
     *
     * @param stream
     */
    ESRIDBF(InputStream stream) throws ESRIDBFileException, 
				       IOException
    {
	try {
	    DBFReader reader = new DBFReader(stream);
	    
	    // Find and index the name of the fields.
	    for (int i = 0; i < reader.getFieldCount(); i++) {
		DBFField field = reader.getField(i);
		this.fieldNames.put(field.getName(), new Integer(i));
	    }

	    // Read all records at this point. This may be a waste of
	    // memory, but on the other hand it allows us to ensure
	    // that any error caused by the dbfile itself is triggered
	    // here.  Note that this assumes concordance with the ESRI
	    // Specification stating that records must appear in the
	    // same order as the shape features they describe.
	    for (int i = 0; i < reader.getRecordCount(); i++) {
		this.records.add(reader.nextRecord());
	    }

	    Vector v = getMatchingFieldNames(nameRegexp);
	    if (v.size() != 0) {
		nameField = (String) v.get(0);
	    } else {
		nameField = null;
	    }
	    
	} catch (DBFException e) {
	    throw new ESRIDBFileException("Error reading ESRI database");
	}
    }

    /**
     * Returns the specified field of the specified record.
     *
     * @param field the name of the field to get
     * @param recordIndex the index of the record to get
     */
    public Object getValue(String field, int recordIndex) 
	throws ESRIDBNoSuchFieldException
    {
	Object[] record = getRecord(recordIndex);
	if (record != null) {
	    return  record[getFieldIndex(field)];
	} else {
	    return null;
	}
    }
    
    /**
     * Returns the index of the specified field, or -1 if no such
     * field exists.
     *
     * @param name the name of the field to get.
     */
    public int getFieldIndex(String name)
	throws ESRIDBNoSuchFieldException
    {
	Integer index = (Integer) this.fieldNames.get(name);
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
    public Vector getMatchingFieldNames(String regex)
    {
	Vector res = new Vector();

	for (Enumeration e = fieldNames.keys(); e.hasMoreElements();) {
	    String fieldName = (String) e.nextElement();
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
    public Object[] getRecord(int record)
    {
	if (record < records.size()) {
	    return (Object[]) this.records.get(record);
	} else {
	    return null;
	}
    }   

    /**
     * Creates a name for the specified shape by first looking in the
     * db-file, if it exists, failing that creates a string
     * representation of the index.
     *
     * @param index the index of the shape.
     */
    String getShapeName(int index) 
    {
	try {
	    // If namefield exist, try to get proper name.
	    if (nameField != null) {
		Object o = getValue(nameField, index);
		if (o != null && o instanceof String) {
		    return washShapeName((String) o);
		}
	    }
	} catch (ESRIDBNoSuchFieldException e) {
		// Don't care to much if not succesfull.
	    }       
	// Failed to return db-filename create fallback name;	
	return Integer.toString(index);
    }

    /**
     * Washes strings into lexical conformance with Taclan V2
     * identifiers.
     *
     * @param string the string to wash.
     */
    private String washShapeName(String string)
    {
	String res = string.replaceAll("'", "\'");
	res = res.replaceAll("\n", "\\n");
	res = res.replaceAll("[^\\p{L}]*\\z", "");
	res = res.replaceAll("\\A[^\\p{L}]*", "");

	return res;
    }
}


/**
 * Represents an error reading an ESRI DBase file.
 *
 * @version 1, $Date: 2006/09/18 09:58:02 $
 * @author  Daniel Ahlin
 */
class ESRIDBFileException extends Exception
{
    /**
     * Creates a new ESRIDBFileException with the provided message.
     *
     * @param message a description of the condition causing the
     * error.
     */
    ESRIDBFileException(String message)
    {
	super(message);
    }
}

/**
 * Represents the nonexistance of a field .
 *
 * @version 1, $Date: 2006/09/18 09:58:02 $
 * @author  Daniel Ahlin
 */
class ESRIDBNoSuchFieldException extends Exception
{
    /**
     * Creates a new ESRIDBNoSuchFieldException
     */
    ESRIDBNoSuchFieldException()
    {
	super();
    }
}
