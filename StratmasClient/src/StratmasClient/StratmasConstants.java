// $Id: ApproxsimConstants.java,v 1.12 2006/09/12 07:40:02 alexius Exp $
/*
 * @(#)ApproxsimConstants.java
 */

package ApproxsimClient;

/**
 * Constants looking for a better place to live.
 * 
 * @version 1, $Date: 2006/09/12 07:40:02 $
 * @author Daniel Ahlin
 */

public class ApproxsimConstants {
    public static String approxsimNamespace = "http://pdc.kth.se/approxsimNamespace";
    public static String xsdNamespace = "http://www.w3.org/2001/XMLSchema";
    public static String xmlnsNamespace = "http://www.w3.org/2001/XMLSchema-instance";
    public static String JAR_SCHEMA_LOCATION = "/schemas/";
    public static String APPROXSIM_SIMULATION_SCHEMA = "taclan2sim.xsd";
    public static String APPROXSIM_PROTOCOL_SCHEMA = "approxsimProtocol.xsd";

    /**
     * String used to reference the all faction. This sucks. Should be improved when there's time available.
     */
    public static final String factionAll = "All";

    /**
     * The header to use in xml files.
     */
    public static final String xmlFileHeader = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
            + "<root xmlns:sp=\"http://pdc.kth.se/approxsimNamespace\" "
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
            + "xmlns:xi=\"http://www.w3.org/2001/XInclude\" "
            + "xsi:type=\"sp:Root\">";

    /**
     * The footer to use in xml files.
     */
    public static final String xmlFileFooter = "</root>";
}
