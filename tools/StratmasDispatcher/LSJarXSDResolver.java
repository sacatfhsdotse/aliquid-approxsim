// 	$Id: LSJarXSDResolver.java,v 1.2 2005/10/07 12:57:21 dah Exp $
/*
 * @(#)LSJarXSDResolver.java
 */

package StratmasDispatcher;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.XNIException;
import java.io.IOException;
import java.io.Reader;
import java.io.InputStream;
import org.apache.xerces.xni.XMLResourceIdentifier;

/**
 * Resolves xsd-requests by also using class.getResourceAsStream
 *
 * (Acknowledgement: Comments in this file copied and or adapted from
 * the Xerces library.)
 *
 * @version 1, $Date: 2005/10/07 12:57:21 $
 * @author  Daniel Ahlin
*/

public class LSJarXSDResolver implements LSResourceResolver, XMLEntityResolver
{
    /**
     * The default location for scheamas in the jar file.
     */
    public static String JAR_SCHEMA_LOCATION = "/StratmasDispatcher/schemas/";

    /**
     * The XMLEntityResolver this object uses before own resolving.
     */
    XMLEntityResolver otherXMLEntityResolver = null;

    /**
     * The XMLEntityResolver this object uses before own resolving.
     */
    LSResourceResolver otherLSResourceResolver = null;

    /**
     * Creates a new LSJarXSDResolver
     * 
     * @param other XMLEntityResolver to ask before this (or null).
     */
    public LSJarXSDResolver(XMLEntityResolver other)
    {
	this.otherXMLEntityResolver = other;
    }

    /**
     * Creates a new LSJarXSDResolver
     * 
     * @param other LSResourceResolver to ask before this (or null).
     */
    public LSJarXSDResolver(LSResourceResolver other)
    {
	this.otherLSResourceResolver = other;
    }

    /**
     *  Allow the application to resolve external resources. 
     * <br> The <code>LSParser</code> will call this method before opening any 
     * external resource, including the external DTD subset, external 
     * entities referenced within the DTD, and external entities referenced 
     * within the document element (however, the top-level document entity 
     * is not passed to this method). The application may then request that 
     * the <code>LSParser</code> resolve the external resource itself, that 
     * it use an alternative URI, or that it use an entirely different input 
     * source. 
     * @param type  The type of the resource being resolved.
     * @param namespaceURI  The namespace of the resource being resolved.
     * @param publicId  The public identifier of the external entity being 
     *   referenced, or <code>null</code> if no public identifier was 
     *   supplied or if the resource is not an entity. 
     * @param systemId The system identifier, a URI reference [<a
     * href='http://www.ietf.org/rfc/rfc2396.txt'>IETF RFC 2396</a>],
     * of the external resource being referenced, or <code>null</code>
     * if no system identifier was supplied.
     * @param baseURI The absolute base URI of the resource being
     * parsed, or <code>null</code> if there is no base URI.
     * @return A <code>LSInput</code> object describing the new input
     * source, or <code>null</code> to request that the parser open a
     * regular URI connection to the resource.
     */
    public LSInput resolveResource(String type, 
                                   String namespaceURI, 
                                   String publicId, 
                                   String systemId, 
                                   String baseURI) {
	if (otherLSResourceResolver != null) {
	    LSInput res = otherLSResourceResolver.resolveResource(type, 
								  namespaceURI,
								  publicId,
								  systemId,
								  baseURI);
	    if (res != null && (res.getByteStream() != null ||
				res.getCharacterStream() != null)) {		
		return res;
	    }
	}

	// Failed try loading by resource:
	return getStreamInput(systemId);
    }

    /**
     * Resolves an external parsed entity. If the entity cannot be
     * resolved, this method should return null.
     *
     * @param resourceIdentifier location of the XML resource to resolve
     *
     * @throws XNIException Thrown on general error.
     * @throws IOException  Thrown if resolved entity stream cannot be
     *                      opened or some other i/o error occurs.
     * @see org.apache.xerces.xni.XMLResourceIdentifier
     */
    public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier)
        throws XNIException, IOException
    {
	// First try other entity resolver.
	if (otherXMLEntityResolver != null) {
	    XMLInputSource res = 
		otherXMLEntityResolver.resolveEntity(resourceIdentifier);
	    if (res != null && (res.getByteStream() != null ||
				res.getCharacterStream() != null)) {		
		return res;
	    }
	}
	
	// Failed try loading by resource:
	return getStreamInput(resourceIdentifier);
    }

    /**
     * Returns a StreamLSInput using the specified stream.
     *
     * @param resourceIdentifier  location of the XML resource to resolve
     */
    public static StreamInput getStreamInput(XMLResourceIdentifier resourceIdentifier)
    {
	return new StreamInput(LSJarXSDResolver.class.getResourceAsStream(JAR_SCHEMA_LOCATION + 
									  resourceIdentifier.getLiteralSystemId()), 
			       resourceIdentifier);
    }

    /**
     * Returns a StreamLSInput using the specified stream.
     *
     * @param systemId the systemId of the object
     */
    public static StreamInput getStreamInput(String systemId)
    {
	return new StreamInput(LSJarXSDResolver.class.getResourceAsStream(JAR_SCHEMA_LOCATION + 
									  systemId), systemId);
    }
}

