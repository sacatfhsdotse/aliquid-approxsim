package ApproxsimClient;

import java.io.*;
import java.util.Hashtable;
import ApproxsimClient.communication.*;

import ApproxsimClient.object.ApproxsimEvent;
import ApproxsimClient.object.ApproxsimEventListener;
import ApproxsimClient.object.ApproxsimObject;
import ApproxsimClient.object.ApproxsimObjectFactory;
import ApproxsimClient.object.type.TypeFactory;

import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class CommTest {
    final private static String path = "/afs/pdc.kth.se/projects/approxsim/alexius/xml/CommTest_files/";
    final private static String mLoadQueryFileName = path + "loadQuery.xml";

    private static Hashtable<String, String> mPropHash = new Hashtable<String, String>();

    public static String getMsgFromFile(String filename) {
        String ret = null;
        try {
            FileInputStream fi = new FileInputStream(filename);
            int size = (int) fi.getChannel().size();
            byte[] buf = new byte[size];
            fi.read(buf);
            ret = new String(buf);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }

        return ret;
    }

    public static void main(String[] args) throws IOException {
        initPropertyHash();

        int port = 28444;
        String server = "localhost";

        // read arguments
        if (args.length > 0) {
            server = args[0];
        }
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (Exception e) {
                System.out.println("server port = " + port + " (default)");
            }
        }

        ApproxsimSocket sock = null;
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(
                System.in));

        boolean conn = false;
        boolean fromFile = false;
        String line = null;
        String old = null;
        ApproxsimObject root = null;
        MyClient fooClient = new MyClient("");
        MyXMLHandler xh = new MyXMLHandler(fooClient, "approxsimProtocol.xsd");
        SubscriptionHandler sh = new SubscriptionHandler();
        ServerConnection sc = null;
        xh.connect(sh);

        xh.start();
        sh.start();

        fooClient.addEventListener(new FooListener(fooClient));

        try {
            System.out.println("Enter command:");
            line = stdIn.readLine();
            while (line != null && !line.equalsIgnoreCase("q")) {
                if (line.equalsIgnoreCase("c") && conn == false) {
                    System.out.println("Connecting to " + server + ":" + port);
                    // Connect
                    sc = new ServerConnection(fooClient, xh, server, port);
                    sh.connect(sc);
                    sc.start();
                    sock = sc.socket();
                    conn = true;
                } else if (line.equalsIgnoreCase("cc") && conn == false) {
                    System.out.print("Host: ");
                    server = stdIn.readLine();
                    System.out.print("Port: ");
                    String portStr = stdIn.readLine();
                    if (!portStr.equals("")) {
                        port = Integer.parseInt(portStr);
                    }
                    System.out.println("Connecting to " + server + ":" + port);
                    // Connect
                    sc = new ServerConnection(fooClient, xh, server, port);
                    sh.connect(sc);
                    sc.start();
                    sock = sc.socket();
                    conn = true;
                } else if (line.equalsIgnoreCase("sc") && conn == true) {
                    // Server Capabilities
                    sc.blockingSend(new ServerCapabilitiesMessage());
                    System.out.print(".");
                } else if (line.equalsIgnoreCase("i") && conn == true) {
                    System.out.println("Filename in samples/misc: ");
                    String filename = stdIn.readLine();
                    root = ApproxsimObjectFactory.createList(TypeFactory
                            .getType("Root").getSubElement("identifiables"));
                    ApproxsimObject sim = Client.importXMLSimulation("misc/"
                            + filename);
                    if (sim.getParent() != null) {
                        sim.remove();
                    }
                    root.add(sim);
                    sc.blockingSend(new InitializationMessage(sim));
                } else if (line.equalsIgnoreCase("ic") && conn == true) {
                    System.out.print("Initializing combat.scn: ");
                    ApproxsimObject list = ApproxsimObjectFactory
                            .createList(TypeFactory.getType("Root")
                                    .getSubElement("identifiables"));
                    root = Client.importXMLSimulation("misc/combat.scn");
                    list.add(root);
                    if (root != null) {
                        sc.blockingSend(new InitializationMessage(root));
                    }
                } else if (line.equalsIgnoreCase("st") && conn == true) {
                    sc.send(new StepMessage(1, false));
                } else if (line.equalsIgnoreCase("st2") && conn == true) {
                    sc.send(new StepMessage(2, false));
                } else if (line.equalsIgnoreCase("r") && conn == true) {
                    sc.send(new ResetMessage());
                } else if (line.equalsIgnoreCase("p") && conn == true) {
                    // Set Property
                    System.out.println("Property <return> value <return> ");
                    String prop = stdIn.readLine();
                    if (mPropHash.containsKey(prop)) {
                        prop = mPropHash.get(prop);
                    }
                    sc.send(new SetPropertyMessage(prop, stdIn.readLine()));
                } else if (line.equalsIgnoreCase("d") && conn == true) {
                    sock.id();
                    sc.disconnect();
                    sock = null;
                    conn = false;
                } else if (line.equalsIgnoreCase("l")) {
                    // LoadQuery
                    ApproxsimSocket foo = new ApproxsimSocket();
                    foo.id(0);
                    foo.connect("localhost", 28444);
                    foo.sendMessage(getMsgFromFile(mLoadQueryFileName));
                    Debug.err.println(foo.recvMessage());
                    foo.close();
                } else if (line.equals("ff")) {
                    fromFile = !fromFile;
                    System.out.println("input now from "
                            + (fromFile ? "file" : "program"));
                } else if (line.equals("t")) {
                    xh.test(getMsgFromFile("/scratch/test.xml"));
                } else if (!line.equals("")) {
                    System.out.println("Command '" + line
                            + "' unknown or currently invalid");
                }
                // If no command given then repeat the latest.
                if (!line.equals("")) {
                    old = line;
                    line = stdIn.readLine();
                    while (line == null) {
                        stdIn = new BufferedReader(new InputStreamReader(
                                System.in));
                        line = stdIn.readLine();
                    }
                } else if (old == null) {
                    line = stdIn.readLine();
                } else {
                    line = old;
                    System.out.println(line);
                }
            }

        } catch (IOException e) {
            System.err.println("IOException while communicating");
        } catch (ServerException e) {
            System.err.println("ServerException while communicating: "
                    + e.getMessage());
        }

        if (sock != null) {
            sock.close();
        }
        stdIn.close();
        if (conn) {
            sc.kill();
        }
        sh.kill();
        xh.kill();
    }

    public static void initPropertyHash() {
        mPropHash.put("u", "unitRandomWalk");
        mPropHash.put("v", "validateXML");
//          mPropHash.put("", "");
    }
}

class FooListener implements ApproxsimEventListener {
    MyClient mClient = null;

    FooListener(MyClient client) {
        mClient = client;
    }

    public void eventOccured(ApproxsimEvent event) {
        System.err.println("Event occured!");
        mClient.tell();
    }
}

class MyXMLHandler extends XMLHandler {
    public MyXMLHandler(Client c, String s) {
        super(c, s);
    }

    public void test(String xml) {
        try {
            // Parse the Document
            mParser.parse(new InputSource(new StringReader(xml)));
            Element elem = mParser.getDocument().getDocumentElement();
            if (elem != null) {
                ApproxsimObject o = ApproxsimObjectFactory
                        .domCreate(getFirstChildByTag(elem, "object"));
                dumpToFile("theOutput.xml", o.toXML());
            } else {
                System.err.println("NO ELEM IN UGLY TEST");
            }
        } catch (SAXException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            dumpToFile("DISCARDED_MESSAGE.tmp", xml);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            dumpToFile("DISCARDED_MESSAGE.tmp", xml);
        }
    }
}
