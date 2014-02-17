//         $Id: StreamInput.java,v 1.1 2005/09/14 10:09:00 dah Exp $
/*
 * @(#)StreamInput.java
 */

package StratmasDispatcher;

import org.w3c.dom.ls.LSInput;
import org.apache.xerces.xni.parser.XMLInputSource;
import java.io.InputStream;
import org.apache.xerces.xni.XMLResourceIdentifier;

/**
 * An LSInput implementation using input streams.
 *
 * (Acknowledgement: Some comments in this file copied and or adapted from
 * the Xerces library.)
 *
 * @version 1, $Date: 2005/09/14 10:09:00 $
 * @author  Daniel Ahlin
*/

public class StreamInput extends XMLInputSource implements LSInput
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
