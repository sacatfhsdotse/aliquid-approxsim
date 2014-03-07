//         $Id: LSJarXSDResolver.java,v 1.4 2006/03/10 17:19:11 alexius Exp $
/*
 * @(#)Duration.java
 */

package StratmasClient;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.XNIException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.xerces.xni.XMLResourceIdentifier;

/**
 * Resolves xsd-requests by also using class.getResourceAsStream
 *
 * (Acknowledgement: Comments in this file copied and or adapted from
 * the Xerces library.)
 *
 * @version 1, $Date: 2006/03/10 17:19:11 $
 * @author  Daniel Ahlin
*/

public class LSJarXSDResolver implements LSResourceResolver, XMLEntityResolver
{
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
         InputStream stream = LSJarXSDResolver.class.getResourceAsStream(StratmasConstants.JAR_SCHEMA_LOCATION + 
                                                                         resourceIdentifier.getLiteralSystemId());
         if (stream != null) {
              return new StreamInput(stream, resourceIdentifier.getLiteralSystemId());
         } else {
              return null;
         }
    }

    /**
     * Returns a StreamLSInput using the specified stream.
     *
     * @param systemId the systemId of the object
     */
    public static StreamInput getStreamInput(String systemId)
    {
         InputStream stream = LSJarXSDResolver.class.getResourceAsStream(StratmasConstants.JAR_SCHEMA_LOCATION + 
                                                                         systemId);
         if (stream != null) {
              return new StreamInput(stream, systemId);
         } else {
              return null;
         }
    }
}

class StreamInput extends XMLInputSource implements LSInput
{
    /**
     * The stringData of this object.
     */    
    String stringData = null;

    /**
     * The baseURI of this object.
     */    
    String baseURI = null;

    /**
     * The certifiedText of this object.
     */    
    boolean certifiedText = true;

    /**
     * Creates a new LSInput using the provided stream
     *
     * @param stream the stream this object wraps.
     * @param systemId the system id of the stream;
     */
    StreamInput(InputStream stream, String systemId)
    {
        super(null, systemId, null);
        setByteStream(stream);
    }

    /**
     * Creates a new LSInput using the provided stream
     *
     * @param stream the stream this object wraps.
     * @param identifer the identifier to get other values from 
     */
    StreamInput(InputStream stream, XMLResourceIdentifier identifier)
    {
        super(identifier);
        setByteStream(stream);
    }

    /**
     *  String data to parse. If provided, this will always be treated as a 
     * sequence of 16-bit units (UTF-16 encoded characters). 
     */
    public String getStringData()
    {
        return this.stringData;
    }

    /**
     *  String data to parse. If provided, this will always be treated as a 
     * sequence of 16-bit units (UTF-16 encoded characters). 
     */
    public void setStringData(String stringData)
    {
        this.stringData = stringData;
    }

    /**
     *  The base URI to be used (see section 5.1.4 in [<a href='http://www.ietf.org/rfc/rfc2396.txt'>IETF RFC 2396</a>]) for 
     * resolving a relative <code>systemId</code> to an absolute URI. 
     * <br> If, when used, the base URI is itself a relative URI, an empty 
     * string, or null, the behavior is implementation dependent. 
     */
    public String getBaseURI()
    {
        return this.baseURI;
    }

    /**
     *  The base URI to be used (see section 5.1.4 in [<a href='http://www.ietf.org/rfc/rfc2396.txt'>IETF RFC 2396</a>]) for 
     * resolving a relative <code>systemId</code> to an absolute URI. 
     * <br> If, when used, the base URI is itself a relative URI, an empty 
     * string, or null, the behavior is implementation dependent. 
     */
    public void setBaseURI(String baseURI)
    {
        this.baseURI = baseURI;
    }

    /**
     *  If set to true, assume that the input is certified (see section 2.13 
     * in [<a href='http://www.w3.org/TR/2003/PR-xml11-20031105/'>XML 1.1</a>]) when 
     * parsing [<a href='http://www.w3.org/TR/2003/PR-xml11-20031105/'>XML 1.1</a>]. 
     */
    public boolean getCertifiedText()
    {
        return this.certifiedText;
    }
    
    /**
     *  If set to true, assume that the input is certified (see section 2.13 
     * in [<a href='http://www.w3.org/TR/2003/PR-xml11-20031105/'>XML 1.1</a>]) when 
     * parsing [<a href='http://www.w3.org/TR/2003/PR-xml11-20031105/'>XML 1.1</a>]. 
     */
    public void setCertifiedText(boolean certifiedText)
    {
        this.certifiedText = certifiedText;
    }
}
