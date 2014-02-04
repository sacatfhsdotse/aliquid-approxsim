//         $Id: EvolverFrame.java,v 1.4 2006/01/11 22:22:18 dah Exp $

/*
 * @(#)EvolverFrame.java
 */

package StratmasClient.evolver;

import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JOptionPane;
import javax.swing.JMenuBar;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JRootPane;
import javax.swing.JWindow;
import javax.swing.JProgressBar;
import javax.swing.JFileChooser;
import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JComponent;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JToolBar;
import javax.swing.JMenu;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.WindowConstants;
import javax.swing.event.MenuListener;
import javax.swing.event.MenuEvent;

import java.awt.event.ActionEvent;
import java.awt.Component;
import java.awt.Image;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ContainerListener;
import java.awt.event.ContainerEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;

/**
 * EvolverFrame is JFrame adapted to use as the main window for the
 * EvolverGUI.
 *
 * @version 1, $Date: 2006/01/11 22:22:18 $
 * @author  Daniel Ahlin
*/
public class EvolverFrame extends JFrame
{
    /**
     * The evolverGUI to use.
     */
    EvolverGUI evolverGUI;

    /**
     * The toolbar to use.
     */
    JToolBar toolBar;

    /**
     * Information about the evolver.
     */
    JTextField infoField;

    /**
     * Timer for removal of transient information from the infoField.
     */
    Timer infoFieldTimer = new Timer();

    /**
     * The life in ms of transient log messages
     */ 
    static final int LOG_LIFE_MS = 5000;

    /**
     * Creates a frame for the specified evolvergui
     *
     * @param evolverGUI the evolverGUI to visualize
     */
    public EvolverFrame(EvolverGUI evolverGUI)
    {
        super("StratmasClient Evolver");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.evolverGUI = evolverGUI;
        getContentPane().add(evolverGUI);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        this.infoField = createInfoField();
        panel.add(infoField);
        getContentPane().add(panel, BorderLayout.SOUTH);

        // Find a suitable image for this application
        //setIconImage(new ImageIcon(getClass().getResource("icons/evolver.png")).getImage());
        updateToolBar();
    }

    /**
     * Creates an infoField for this frame.
     */
    JTextField createInfoField()
    {
        JTextField field = new JTextField();
        field.setEditable(false);
        field.setBackground(this.getBackground());
        return field;
    }

    /**
     * Logs interesting messages for eventual consumption by the user.
     *
     * @param message the message.
     * @param temporary if the message should be removed after some time.
     */
    public void log(String message, boolean temporary)
    {
        synchronized (this.infoFieldTimer) {
            this.infoFieldTimer.cancel();
        }

        getInfoField().setText(message);

        if (temporary) {
            final Timer timer = new Timer();
            timer.schedule(new TimerTask() 
                {
                    public void run()
                    {
                        getInfoField().setText("");
                        timer.cancel();
                    }
                }, LOG_LIFE_MS);

            synchronized (this.infoFieldTimer) {
                this.infoFieldTimer = timer;
            }
        }
    }

    /**
     * Returns the infofield of this frame
     */
    public JTextField getInfoField()
    {
        return this.infoField;
    }
    
    /**
     * Updates the toolBar of this frame.
     */
    public void updateToolBar()
    {
        if (toolBar == null) {
            toolBar = new JToolBar();
            getContentPane().add(toolBar, BorderLayout.PAGE_START);
        } else {
            toolBar.removeAll();
        }

        toolBar.add(getEvolverGUI().getAction("Run"));
        toolBar.add(getEvolverGUI().getAction("Abort"));
    }

    /**
     * Returns the evolverGUI of this frame
     */
    EvolverGUI getEvolverGUI()
    {
        return this.evolverGUI;
    }
}
