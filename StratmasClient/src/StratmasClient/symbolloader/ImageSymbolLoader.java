/*
 * A SymbolViewer that uses PNG images originally supplied by the Norwegian Defense and tweaked into PNGs by me. Not bound to PNG format
 * technically, uses javax.imageio.
 */

package StratmasClient.symbolloader;

import StratmasClient.Debug;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import javax.imageio.*;

class ImageSymbolLoader {
    private Rectangle lastFrame = null;

    private static final String suffix = ".png";

    private SymbolID id;                // ID of last loaded symbol.

    private BufferedImage canvas;                // Used to composite the various layers together.
    private Rectangle canvasBorder;        // Used to intersect before computing subimage.
    private Graphics2D canvas2D;        // Graphics2D for the canvas.

    static private abstract class LayerLoader implements MRUCache.Getter {
        protected String rootDir;
        protected MRUCache cache;
        protected String iconSize;

        public LayerLoader(String root, int cacheSize) {
            rootDir = root;
            cache = new MRUCache(cacheSize, this);
            // iconSize = "150%20pixels";
            iconSize = "150pixels";
        }

        protected String getSize() {
            return iconSize;
        }

        abstract public String getFilename(SymbolID id);

        public float getOffsetX() {
            return 0.0f;
        }

        public float getOffsetY() {
            return 0.0f;
        }

        public BufferedImage getImage(SymbolID id) {
            String f = getFilename(id);
            if (f != null && f.length() > 0) {
                // URI uri = null;
//                                 try {
//                                         uri = new URI(rootDir + "/" + f);
//                                 }
//                                 catch(URISyntaxException e) {
//                                         Debug.err.println("Generated bad URI for bitmap (" + uri + ")");
//                                         e.printStackTrace();
//                                         return null;
//                                 }
                // return (BufferedImage) cache.get(uri);
                return (BufferedImage) cache.get(rootDir + "/" + f);
            }
            return null;
        }

        static protected String getAffilSuffix(SymbolID id) {
            return SymbolID.AffiliationField
                    .getIconAlias(id.getFieldValue(SymbolID.F_AFFILIATION))
                    .toLowerCase()
                    + suffix;
        }

        // The MRUCache.Getter interface. Simply use javax.imageio to load bitmap.
        public Object get(Object key) {
            // URI uri = (URI) key;
            URL url = null;

//                 try {
//                     url = uri.toURL();
//                 } catch(MalformedURLException e) {
//                     e.printStackTrace();
//                 }
            BufferedImage img = null;
            try {
                // Debug.err.println("Loading " + url);
                // img = ImageIO.read(url);
                url = ImageSymbolLoader.class.getResource((String) key);
                if (url != null) {
                    Debug.err.println("Loading " + url);
                    img = ImageIO.read(url);
                } else {
                    return null;
                }

            } catch (javax.imageio.IIOException e) {
                if (e.getCause() instanceof FileNotFoundException)
                    Debug.err.println("File not found: " + url);
                else e.printStackTrace();
            } catch (FileNotFoundException e) {
                Debug.err.println("File not found: " + url);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return (Object) img;
        }
    }

    // Load core symbol, the icon that identifies a unit's function.
    static private class IconLoader extends LayerLoader {
        public IconLoader(String rootDir, int cacheSize) {
            super(rootDir, cacheSize);
        }

        public String getFilename(SymbolID sid) {
            String address = sid.getAddress();
            if (address == null) return null;
            StringBuffer filename = new StringBuffer(64);
            if (address.startsWith("1.X.1"))
                filename.append("1x1/");
            else if (address.startsWith("1.X.2"))
                filename.append("1x2/");
            else if (address.startsWith("1.X.3.1"))
                filename.append("1x31");
            else if (address.startsWith("1.X.3.2"))
                filename.append("1x32");
            else if (address.startsWith("1.X.3.3"))
                filename.append("1x33");
            else if (address.startsWith("1.X.4"))
                filename.append("1x4");
            else if (address.startsWith("1.X.5"))
                filename.append("1x5");
            else if (address.startsWith("1.X.6")) filename.append("1x6");
            filename.append("/" + getSize() + "/");
            String[] part = address.split("\\.");
            final String notHex = "0123456789abcdefghijklmnopqrstuvqxyz";
            for (int i = 2; i < part.length; i++) {
                filename.append(notHex.charAt(Integer.parseInt(part[i])));
            }
            String affil = SymbolID.AffiliationField.getIconAlias(sid
                    .getFieldValue(SymbolID.F_AFFILIATION));
            affil = affil.toLowerCase();
            filename.append(affil.charAt(0));
            filename.append(suffix);

            return filename.toString();
        }
    }

    // Load echelon, the I III X XX and so on that show a unit's size (platoon, division etc).
    static private class EchelonLoader extends LayerLoader {
        private float offset;                // Varies with getFilename()s return. Slightly hideous.

        public EchelonLoader(String rootDir, int cacheSize) {
            super(rootDir, cacheSize);
            offset = 0.0f;
        }

        public String getFilename(SymbolID sid) {
            String ech = sid.getFieldValue(SymbolID.F_SYMBOLMODIFIER);
            final String needSize = "-ABCDEFG";
            if (needSize.indexOf(ech.charAt(0)) >= 0) {
                char size = ech.charAt(1);
                final String sizeOrder = "ABCDEFGHIJKLM";
                int si = sizeOrder.indexOf(size);
                if (si >= 0) {
                    si++;
                    StringBuffer ind = new StringBuffer("indicators/"
                            + getSize() + "/");
                    if (si < 10) ind.append("0");
                    ind.append(si);
                    String a = sid.getFieldValue(SymbolID.F_AFFILIATION);
                    ind.append(SymbolID.AffiliationField.getIconAlias(a)
                            .toLowerCase());
                    ind.append(suffix);
                    // Compute offset depending on affiliation. Hackish.
                    char a0 = a.charAt(0);
                    if (a0 == 'A' || a0 == 'F' || a0 == 'N')
                        offset = 0.01f;
                    else if (a0 == 'H' || a0 == 'J' || a0 == 'K' || a0 == 'S')
                        offset = -0.15f;
                    else offset = -0.15f;
                    return ind.toString();
                }
            } else if (ech.charAt(0) == 'H') {        // Installation?
                String affil = getAffilSuffix(sid);
                offset = affil.charAt(0) == 'u' ? -0.1f
                        : affil.charAt(0) == 'h' ? -0.05f : 0.0f;
                return "indicators/" + getSize() + "/Inst_" + affil;
            }
            return null;
        }

        public float getOffsetY() {
            return offset;
        }
    }

    // Load dashed angled "roof" over symbol for feint/dummy units.
    static private class FeintDummyLoader extends LayerLoader {
        private float offset;

        public FeintDummyLoader(String rootDir, int cacheSize) {
            super(rootDir, cacheSize);
            offset = 0.0f;
        }

        public String getFilename(SymbolID sid) {
            if (sid.isFeintDummy()) {
                StringBuffer fn = new StringBuffer("indicators/" + getSize()
                        + "/Fd");
                String affil = getAffilSuffix(sid);
                // Compute affiliation part, which is rather irregular ("frd", "hos", "neu", "unk").
                char a = affil.charAt(0);
                fn.append(a);
                if (a == 'f') {
                    fn.append("rd");
                    offset = -0.23f;
                } else if (a == 'h') {
                    fn.append("os");
                    offset = -0.35f;
                } else if (a == 'n') {
                    fn.append("eu");
                    offset = -0.30f;
                } else if (a == 'u') {
                    fn.append("nk");
                    offset = -0.35f;
                }
                // Compute track part, "air", "gnd", "sub" or "srf" (?).
                final String address = sid.getAddress();
                final String domain;
                if (address.startsWith("1.X.2"))
                    domain = "air";
                else if (address.startsWith("1.X.3"))
                    domain = "gnd";
                else if (address.startsWith("1.X.4"))
                    domain = "srf";
                else if (address.startsWith("1.X.5"))
                    domain = "sub";
                else return null;
                fn.append(domain);
                fn.append(suffix);
                return fn.toString();
            }
            return null;
        }

        public float getOffsetY() {
            return offset;
        }
    }

    // Load the little task force bracket.
    static private class TaskForceLoader extends LayerLoader {
        private float offset;

        public TaskForceLoader(String rootDir, int cacheSize) {
            super(rootDir, cacheSize);
            offset = 0.0f;
        }

        public String getFilename(SymbolID sid) {
            if (sid.isTaskForce()) {
                char a = Character.toUpperCase(getAffilSuffix(sid).charAt(0));
                String base = "indicators/" + getSize() + "/" + a;
                if (a == 'F') {
                    offset = -0.06f;
                    return base + "_s_tf" + suffix;
                } else if (a == 'H') {
                    offset = -0.06f;
                    return base + "_s_tf" + suffix;
                } else if (a == 'N') {
                    offset = -0.07f;
                    return base + "_s_tf" + suffix;
                } else if (a == 'U') {
                    offset = -0.16f;
                    return base + "_s_tf" + suffix;
                }
            }
            return null;
        }

        public float getOffsetY() {
            return offset;
        }
    }

    // Load HQ-indicating flag line.
    static private class HQLoader extends LayerLoader {
        public HQLoader(String rootDir, int cacheSize) {
            super(rootDir, cacheSize);
        }

        public float getOffsetY() {
            return 0.25f;
        }

        public String getFilename(SymbolID sid) {
            if (sid.isHeadquarters()) {
                StringBuffer hq = new StringBuffer("indicators/" + getSize()
                        + "/Hq_");
                hq.append(sid.getFieldValue(SymbolID.F_AFFILIATION)
                        .toLowerCase());
                hq.append(suffix);
                return hq.toString();
            }
            return null;
        }
    }

    // Load equipment mobility indicator, all the wiggly lines under the frame.
    static private class MobilityLoader extends LayerLoader {
        public MobilityLoader(String rootDir, int cacheSize) {
            super(rootDir, cacheSize);
        }

        public float getOffsetY() {
            return 0.07f;
        }

        public String getFilename(SymbolID sid) {
            int m = sid.getMobility();
            String mob = "";
            if (m > 0 && m < 10)
                mob = "0" + m;
            else if (m >= 10
                    && m <= SymbolID.SymbolModifierField.EQUIP_AMPHIBIOUS)
                mob = "" + m;
            if (mob.length() > 0)
                return "indicators/" + getSize() + "/Mob_" + mob + suffix;
            return null;
        }
    }

    // Load affiliation modifier.
    static private class AffiliationModifierLoader extends LayerLoader {
        public AffiliationModifierLoader(String rootDir, int cacheSize) {
            super(rootDir, cacheSize);
        }

        public String getFilename(SymbolID sid) {
            String prefix = null;
            final char affil = sid.getFieldValue(SymbolID.F_AFFILIATION)
                    .charAt(0);
            if (affil == 'A')
                prefix = "Af_";
            else if (affil == 'J')
                prefix = "Jkr_";
            else if (affil == 'K')
                prefix = "Fkr_";
            else if (affil == 'S') prefix = "Sus_";
            if (prefix == null) return null;

            final String address = sid.getAddress();
            final String domain;
            if (address.startsWith("1.X.2"))
                domain = "air";
            else if (address.startsWith("1.X.3"))
                domain = "gnd";
            else if (address.startsWith("1.X.4"))
                domain = "sea";
            else if (address.startsWith("1.X.5"))
                domain = "sub";
            else return null;

            return "indicators/" + getSize() + "/" + prefix + domain + suffix;
        }
    }

    private LayerLoader[] layer;

    public ImageSymbolLoader(String rootDir, int cacheSize) {
        super();
        layer = new LayerLoader[] { new IconLoader(rootDir, cacheSize),
                new EchelonLoader(rootDir, cacheSize),
                new FeintDummyLoader(rootDir, cacheSize),
                new TaskForceLoader(rootDir, cacheSize),
                new HQLoader(rootDir, cacheSize),
                new MobilityLoader(rootDir, cacheSize),
                new AffiliationModifierLoader(rootDir, cacheSize), };
        id = new SymbolID();
        canvas = new BufferedImage(256, 320, BufferedImage.TYPE_4BYTE_ABGR);
        canvasBorder = new Rectangle(0, 0, canvas.getWidth(),
                canvas.getHeight());
        canvas2D = canvas.createGraphics();
    }

    public Image load(String symbolID) {
        id.set(symbolID);

        // Clear canvas so that all pixels have alpha = 0. I can't figure out a way to do this using
        // the Graphics2D drawing methods, they all refuse to render after setColor(alpha=0). :)
        for (int y = 0; y < canvas.getHeight(); y++) {
            for (int x = 0; x < canvas.getWidth(); x++)
                canvas.setRGB(x, y, 0);
        }
        Rectangle frame = null, bound = null;
        for (int i = 0; i < layer.length; i++) {
            Image img = layer[i].getImage(id);
            if (img != null) {
                if (frame == null) {
                    frame = new Rectangle(
                            (canvas.getWidth() - img.getWidth(null)) / 2,
                            (canvas.getHeight() - img.getHeight(null)) / 2,
                            img.getWidth(null), img.getHeight(null));
                    bound = new Rectangle(frame);
                }
                if (i == 0)
                    canvas2D.drawImage(img, frame.x, frame.y, null);
                else {
                    int lx = frame.x
                            + (int) (frame.width * layer[i].getOffsetX()), ly = frame.y
                            + (int) (frame.height * layer[i].getOffsetY());

                    canvas2D.drawImage(img, lx, ly, null);
                    bound.add(lx, ly);
                    bound.add(lx + ((BufferedImage) img).getWidth(), ly
                            + ((BufferedImage) img).getHeight());
                }
            }
        }
        if (bound != null) {
            lastFrame = bound;
            bound = bound.createIntersection(canvasBorder).getBounds();
            BufferedImage img = canvas.getSubimage(bound.x, bound.y,
                                                   bound.width, bound.height);
            BufferedImage res = new BufferedImage(img.getWidth(),
                    img.getHeight(), img.getType());
            res.setData(img.getData());
            return res;

        }
        return null;
    }

    public Rectangle getFrame() {
        return lastFrame;
    }
}
