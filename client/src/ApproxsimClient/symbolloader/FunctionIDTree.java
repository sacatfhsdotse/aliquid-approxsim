/*
 * A tree that can hold the symbol hierarchy. Meant to be initialized from a copy-and-pasted textfile holding lines looking like this (from
 * the beginning): 1.X S * U * -- -- -- * * * UNKNOWN/UNKNOWN 1.X.1 S * P * -- -- -- ** ** * SPACE TRACK 1.X.1.1 S * P * S- -- -- ** ** *
 * SATELLITE 1.X.1.2 S * P * V- -- -- ** ** * CREWED SPACE VEHICLE 1.X.1.3 S * P * T- -- -- ** ** * SPACE STATION And so on. This is not
 * very elegant or "industrial strength", but it still beats typing them in all by hand, and is a pretty quick way to extract the text from
 * the PDFs of the MIL-STD-2525B standard (TABLE A-III and friends). The fields we care about of the above, are: address The first field,
 * the dot-separated string of symbols. Locates each node in the hierarchy, and also defines the hierarchy itself. symbol id Joined together
 * from the second, fourth, and fifth through seventh fields, so for the SPACE STATION it is "SPT-----", formatted just like that. function
 * name Joined together from all words after the first 11 fields, with spaces in between.
 */

package ApproxsimClient.symbolloader;

import java.io.*;
import java.util.*;
import javax.swing.tree.*;

class FunctionIDTree {
    private class FunctionInfo {
        public String address;                // Hierarchy address component, e.g. "3".
        public String id;                // SymbolID function-id sub-field (1+1+6 chars).
        public String function;                // Function name of this node.
        public Object user;                // User-associated data.

        FunctionInfo(String addressPart, String idPart, String aFunc) {
            address = addressPart;
            id = idPart;
            function = aFunc;
            user = null;
        }
    }

    // A little wrapper class to save on type casts and things.
    public class FunctionIDNode extends DefaultMutableTreeNode {
        /**
			 * 
			 */
        private static final long serialVersionUID = 7473416659964513554L;
        private String fullAddress;

        FunctionIDNode(String address, String id, String func) {
            super(new FunctionInfo(address, id, func));
        }

        public FunctionIDNode getChild(int index) {
            return (FunctionIDNode) super.getChildAt(index);
        }

        public FunctionInfo getFI() {
            return (FunctionInfo) getUserObject();
        }

        // Return full dot-separated address for this node, e.g. "1.X.3.1.1.5.1" for light infantry.
        private String computeAddress() {
            TreeNode[] p = getPath();
            StringBuffer path = new StringBuffer("");
            for (int i = 0; i < p.length - 1; i++) {
                if (i > 0) path.append(".");
                path.append(((FunctionIDNode) p[i + 1]).getFI().address);
            }
            return path.toString();
        }

        public void bufferAddress() {
            fullAddress = computeAddress();
        }

        public String getAddress() {
            return fullAddress;
        }

        // Return function identifier from symbol ID for this node. Fully qualified.
        public String getSymbolID() {
            return getFI().id;
        }

        // Return function name for this node. Not fully qualified, this node only.
        public String getFunction() {
            return getFI().function;
        }

        public String toString() {
            return fullAddress + " " + getFunction();
        }
    }

    // A hash to map symbol IDs to FunctionIDNodes. Symbol IDs are non-hierarchical.
    private HashMap<String, FunctionIDNode> hash;
    // This tree organizes the FunctionIDNodes into the proper hierarchy.
    private FunctionIDNode root;
    private long size;

    FunctionIDTree() {
        hash = new HashMap<String, FunctionIDNode>(600);
        root = new FunctionIDNode("", "", "");
        size = 0;
    }

    // Read in table data from fName, and build tree data structure for searching.
    FunctionIDTree(String fName) {
        this();

        FunctionIDNode c = new FunctionIDNode("1", "S", "WAR");
        root.add(c);

        InputStream stream = FunctionIDTree.class.getResourceAsStream(fName);
        LineNumberReader r = new LineNumberReader(new InputStreamReader(stream));

        try {
            for (String line; (line = r.readLine()) != null;) {
                if (line.equals("--halt--")) break;
                if (line.length() == 0) continue;
                if (line.charAt(0) == '#') continue;
                String[] words = line.split(" ");
                // Compute symbol ID substring, by appending various fields.
                StringBuffer symid = new StringBuffer(words[1]);
                symid.append(words[3]);
                symid.append(words[5]);
                symid.append(words[6]);
                symid.append(words[7]);
                // Compute function name, by appending variable # of suffix words.
                StringBuffer func = new StringBuffer("");
                for (int i = 11; i < words.length; i++) {
                    if (i > 11) func.append(" ");
                    func.append(words[i]);
                }
                // Compute parent and this addresses, and look up parent in tree.
                int dp = words[0].lastIndexOf('.');
                StringBuffer parent = new StringBuffer(words[0]);
                parent.setLength(dp);
                String adr = words[0].substring(dp + 1);
                FunctionIDNode p = findByAddress(parent.toString());
                // Find where to add this node, and add it to parent.
                if (p != null) {
                    int i = 0;
                    for (i = 0; i < p.getChildCount(); i++) {
                        FunctionInfo fi = p.getChild(i).getFI();
                        int cmp = fi.address.compareTo(adr);
                        if (cmp > 0) break;
                    }
                    FunctionIDNode node = new FunctionIDNode(adr,
                            symid.toString(), func.toString());
                    hash.put(symid.toString(), node);
                    p.insert(node, i);
                    node.bufferAddress();
                    size++;
                } else {
                    System.out.println("FunctionIDTree: Bad hierarchy parent '"
                            + parent + "' at line " + r.getLineNumber());
                    return;
                }
            }
        } catch (IOException ioe) {
            System.out.println("FunctionIDTree: I/O error");
            return;
        }
    }

    /** Associate user data object with a given node, identified by hierarchy address. */
    public boolean setDataByAddress(String address, Object user) {
        FunctionIDNode n = findByAddress(address);
        if (n != null) {
            n.getFI().user = user;
            return true;
        }
        return false;
    }

    public Object getDataByAddress(String address) {
        FunctionIDNode n = findByAddress(address);
        if (n != null) return n.getFI().user;
        return null;
    }

    /** Find a node whose full address is given in dot-separated form. */
    public FunctionIDNode findByAddress(String address) {
        String[] adr = address.split("\\.");
        FunctionIDNode n = root, nn;
        for (int i = 0, j; i < adr.length; i++) {
            for (j = 0, nn = null; j < n.getChildCount(); j++) {
                FunctionInfo fi = n.getChild(j).getFI();
                int cmp = fi.address.compareTo(adr[i]);
                if (cmp > 0) return null;
                if (cmp == 0) {
                    nn = n.getChild(j);
                    break;
                }
            }
            if (nn != null)
                n = nn;
            else return null;
        }
        return n;
    }

    /** Find a node from a symbol ID substring. */
    public FunctionIDNode findBySymbolID(String sym) {
        // Make sure string has length 8, is all upper case, and ends with dashes.
        StringBuffer sb = new StringBuffer(sym);
        if (sb.length() > 8)
            sb.setLength(8);
        else while (sb.length() < 8)
            sb.append("-");
        for (int i = 0; i < sb.length(); i++)
            sb.setCharAt(i, Character.toUpperCase(sb.charAt(i)));
        // Once the string has been massaged, do a hash lookup.
        return hash.get(sb.toString());
    }

    public Vector findByFunctionName(String str) {
        Enumeration e = root.breadthFirstEnumeration();
        e.nextElement();
        str = str.toUpperCase();
        Vector result = new Vector(16);
        for (; e.hasMoreElements();) {
            FunctionIDNode n = (FunctionIDNode) e.nextElement();
            FunctionInfo fi = n.getFI();
            if (fi.function.indexOf(str) >= 0)
                result.add(n.getAddress() + " " + fi.function);
        }
        return result;
    }

    public long getSize() {
        return size;
    }

    public FunctionIDNode getRoot() {
        return root;
    }
}
