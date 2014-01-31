/*
 * A SymbolLoader class, whose purpose is to provide bitmaps of symbols given a
 * 15-character SymbolID code. Returns a Java standard BufferedImage, or null.
*/

package StratmasClient.symbolloader;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

public class SymbolLoader implements MRUCache.Getter 
{
    static public FunctionIDTree ftree = null;
    
    private MRUCache cache;			// Cache complete SymbolID -> Image mappings.

    private Hashtable fooCache = new Hashtable();

    private ImageSymbolLoader loader;
    
    private static String VERSION = "0.1";
    
    static public void loadFunctionTree(String std) 
    {
	if(ftree != null)
	    return;
	ftree = new FunctionIDTree(std + "-functiontable.txt");
    }

    /**
     * Creates a new SymbolLoader using the provided parameters.
     *
     * @param standard the standard to use (e.g. "app6a").
     * @param symroot the location of the symbols (e.g. "/usr/local/images").
     * @param cacheSize the size of the cache.
     * 
     */
    public SymbolLoader(String standard, String symroot, int cacheSize)
    {
	loadFunctionTree(standard);
	cache = new MRUCache(cacheSize, this);
	loader = new ImageSymbolLoader(symroot, cacheSize);
    }

    /**
     * Creates a new SymbolLoader using the provided parameters.
     *
     * @param standard the standard to use (e.g. "app6a").
     * @param symroot the location of the symbols (e.g. "/usr/local/images").
     * 
     */
    public SymbolLoader(String standard, String symroot) 
    {
	this(standard, symroot, 8);
    }
    
    /** 
     * Returns the symbol associated with the provided symbol-id.
     * 
     * This is the offical interface to use to load symbols. Cached
     * in several levels.
     *
     * @param symbolID the symbolID to get the image for.
     */
    public BufferedImage load(String symbolID) {
	BufferedImage candidate = null;
	synchronized(fooCache) {
	    candidate = (BufferedImage) fooCache.get(symbolID);
	    if (candidate == null) {
		candidate = (BufferedImage) loader.load(symbolID);
		if (candidate != null) {
		    fooCache.put(symbolID, candidate);
		}
	    }
	}
	return candidate;
	//return (BufferedImage) cache.get(symbolID);
    }
    
    // The MRUCache.Getter interface. Never call directly.
    public Object get(Object key) {
	return loader.load((String) key);
    }
    
    // ----------------------------------------------------------------------------------------
    
    // Test driver code.
    public static final void main(String[] arg)
    {
	
	class TestWindow extends JFrame {
	    public JComponent body;
	    public TestWindow() {
		body = new JPanel();
		getContentPane().add(body);
		pack();
		setSize(450, 450);
		setVisible(true);
	    }
	    
	    public int getWidth() {
		return 450;
	    }
	    
	    public int getHeight() {
		return 450;
	    }
	    
	    public void draw(Image img, int x, int y) {
		body.getGraphics().drawImage(img, x, y, null);
	    }
	    
	    public void drawLine(int x, int y, int x1, int y1) {
		body.getGraphics().drawLine(x, y, x1, y1);
	    }
	}
	
	if (arg.length > 0) {
	    SymbolLoader sl = new SymbolLoader("app6a", arg[0]);
	
	    TestWindow tw = new TestWindow();
	    int x = 0, y = 0, lasth = 100;
	    for(int i = 1; i < arg.length; i++) {
		System.err.println("foo");
		BufferedImage test = sl.load(arg[i]);
		System.err.println(test);
		if(test == null) {
		    tw.drawLine(x, y, x + 150, y + lasth);
		    tw.drawLine(x + 150, y, x, y + lasth);
		    x += 150;
		    continue;
		}
		else {
		    tw.draw(test, x, y);
		    x += test.getWidth();
		    lasth = test.getHeight();
		}
		if(x >= tw.getWidth()) {
		    x = 0;
		    y += lasth;
		}
	    }
	}
	else {
	    System.err.println("Usage: java " + SymbolLoader.class.getName() + " image-dir <syms...>");
	    System.exit(1);
	}
    }
}
