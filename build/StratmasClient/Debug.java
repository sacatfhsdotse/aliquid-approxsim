// 	$Id: Debug.java,v 1.9 2006/04/10 09:45:47 dah Exp $
/*
 * @(#)Debug.java
 */

package StratmasClient;

import StratmasClient.object.Shape;

import java.io.OutputStream;
import java.io.PrintStream;

import java.util.Vector;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Action;

import java.awt.event.ActionEvent;

/**
 * Debug is a class used for debug operations of different kinds. The
 * point of the class is to allow easy on and off switching of, for
 * instance, debug outputs and options.
 *
 * @version 1, $Date: 2006/04/10 09:45:47 $
 * @author  Daniel Ahlin
*/

public class Debug
{
    /**
     * Are we in debugMode or not?.
     */
    public static boolean isInDebugMode = System.getProperty("StratmasClientDebug") != null;

    /**
     * The debug output stream.
     */
    public static PrintStream err = System.getProperty("StratmasClientDebug") != null ? 
	getSystemErr(System.getProperty("StratmasClientDebug")) : getNullStream();

    /**
     * Vector containing debugActions.
     */
    static Vector debugActions = createDebugActions();

    /**
     * Vector containing debugActions.
     */
    static Vector debugComponents = createDebugComponents();

    /**
     * Disables debug outputs to stderr.
     */
    private static void disableDebugOutput()
    {
	Debug.err = getNullStream();
    }

    /**
     * Returns an instance of an empty stream
     */
    private static PrintStream getNullStream()
    {
	return new PrintStream(new OutputStream() {
		/**
		 * Ignores any bytes written to it.
		 *
		 * @param      b   the <code>byte</code> to ignore.
		 */
		public void write(int b)
		{
		    // Ignoring
		}	    
	    });
    }

    /**
     * Returns an adaption of stderr that allows printing from classes
     * whose package name matches the provided regex.
     *
     * @param pattern the pattern to match the package name against if
     * pattern.equals("") or pattern.equals(".*") all messages are let
     * through.
     */
    private static PrintStream getSystemErr(final String pattern)
    {
	if (pattern.equals(".*") || pattern.equals("")) {
	    //Special handling of any matcher
	    return System.err;	    
	}  else {
	    return new PrintStream(new OutputStream() 
		{
		    String regex = pattern;
		    
		    /**
		     * Ignores any bytes written to it.
		     *
		     * @param b the <code>byte</code> to write.
		     */
		    public void write(int b)
		    {
			if (doWrite()) {
			    System.err.write(b);
			}
		    }
		    
		    /**
		     * This method finds frames in the stacktrace
		     * belonging to a method that belongs to a class
		     * that belongs to StratmasClient or a package
		     * beneath that package. (Ignoring this class).
		     *
		     * If all of these packages matches regex return
		     * true, else false. If no such frame is found
		     * return true.
		     */
		    public boolean doWrite()
		    {
			//1.5 can do this: Thread.currentThread().getStackTrace();
			StackTraceElement[] frames = new Throwable().getStackTrace();
			
			for (int i = 0; i < frames.length; i++) {
			    // Skip frames created in this class.
			    if (!frames[i].getClassName().equals(getClass().getName())) {
				String fields[] = frames[i].getClassName().split("\\.");
				if (fields.length > 1 && 
				    fields[0].equals(Debug.class.getPackage().getName())) {
				    // find first non matching package
				    // if any.
				    for (int j = 0; j < (fields.length - 1); j++) {
					if (!fields[j].matches(regex)) {
					    return false;
					}
				    }
				}
			    }
			}
			
			return true;
		    }
		});
	}
	    
    }

    /**
     * Enables debug output to stderr.
     */
    private static void enableDebugOutput()
    {
	Debug.err = System.err;
    }

    /**
     * Returns true if in  debug mode.
     */
    static public boolean isInDebugMode()
    {
	return isInDebugMode;
    }

    /**
     * Puts application in  debug mode.
     */
    static public void enableDebugMode()
    {
	isInDebugMode = true;
	enableDebugOutput();
    }

    /**
     * Puts application out of debug mode.
     */
    static public void disableDebugMode()
    {
	isInDebugMode = false;
	disableDebugOutput();
    }

    /**
     * Returns a panel containing whatever controls are needed for debugging.
     */    
    public static JPanel createDebugPanel()
    {
	JPanel panel = new JPanel();
	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	for (Iterator it = getDebugActions().iterator(); 
	     it.hasNext();
	     panel.add(new JButton((Action) it.next())));
	return panel;
    }

    /**
     * Creates a debug frame exposing the debug actions.
     */
    public static void createDebugFrame()
    {
	SwingUtilities.invokeLater(new Runnable() 
	    {
		public void run() {
		    JFrame debugFrame = new JFrame();
		    debugFrame.getContentPane().add(createDebugPanel());
		    debugFrame.pack();
		    debugFrame.setVisible(true);
		}
	    });
    }

    /**
     * Adds a debug action that will be visible in debug frames.
     *
     * @param action action to add.
     */
    public static void addDebugAction(Action action)
    {
	synchronized(debugActions) {
	    debugActions.add(action);
	}
    }

    /**
     * Adds a debug component that will be visible in debug frames.
     * NOTE: it is much preferable to, if possible, define an action
     * instead.
     *
     * @param component compoenent to add.
     */
    public static void addDebugComponent(JComponent component)
    {
	synchronized(debugComponents) {
	    debugComponents.add(component);
	}
    }

    /**
     * Returns a vector of debug actions.
     */
    public static Vector getDebugActions()
    {
	return debugActions;
    }

    /**
     * Returns a vector of debug actions.
     */
    public static Vector getDebugComponents()
    {
	return debugComponents;
    }

    /**
     * Creates a vector of default debug actions.
     */
    private static Vector createDebugActions()
    {
	Vector actions = new Vector();

	actions.add(new AbstractAction("export PVs") 
	    {
		public void actionPerformed(ActionEvent e)
		{
		    Client.getClient().createStreamPVExporter();
		}
	    });

	actions.add(new AbstractAction("toXML (ms)") 
	    {
		public void actionPerformed(ActionEvent e)
		{
		    long start = System.currentTimeMillis();
		    String xml = Client.getClient().getRootObject().toXML();
		    long duration = System.currentTimeMillis() - start;
		    System.err.println("XML creation took " + duration + "ms");
		}
	    });

	actions.add(new AbstractAction("gc()") 
	    {
		public void actionPerformed(ActionEvent e)
		{
		    System.gc();
		    System.runFinalization();
		}
	    });

	return actions;
    }

    /**
     * Creates a vector of default debug components.
     */
    private static Vector createDebugComponents()
    {
	return new Vector();
    }

    /**
     * Returns the StackTraceElement of the calling method of the calling
     * method, or null if no such method.
     */
    static public StackTraceElement getCaller()
    {
	StackTraceElement[] stack = new Exception().getStackTrace();
	if (stack.length > 2) {
	    return stack[2];
	} else {
	    return null;
	}
    }

    /**
     * Returns the StackTraceElement of the first function that is
     * defined outside the calling class, or null if none.
     */
    static public StackTraceElement getOutsideCaller()
    {
	StackTraceElement[] stack = new Exception().getStackTrace();
	if (stack.length < 2) {
	    return null;
	}
	// Need regexp to account for anonymous classes.
	String exp = stack[1].getClassName() + "\\$[0-9]+";
	
	for (int i = 2; i < stack.length; i++) {
	    if (!stack[i].getClassName().matches(exp)) {
		return stack[i];
	    }
	}
	return null;
    }
}
