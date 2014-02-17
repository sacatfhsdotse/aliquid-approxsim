//         $Id: ServerViewFrame.java,v 1.1 2005/09/27 19:08:08 dah Exp $

/*
 * @(#)ServerViewFrame.java
 */

package StratmasClient.dispatcher;

import java.util.Enumeration;
import java.util.Vector;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;
import javax.swing.JMenu;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.ImageIcon;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.WindowConstants;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ContainerListener;
import java.awt.event.ContainerEvent;


/**
 * ServerViewFrame is JFrame adapted to use with a ServerView
 *
 * @version 1, $Date: 2005/09/27 19:08:08 $
 * @author  Daniel Ahlin
*/
public class ServerViewFrame extends JFrame
{
    /**
     * The ServerView to frame 
     */
    ServerView serverView;

    /**
     * The ToolBar of the frame.
     */
    JToolBar toolBar;

    /**
     * Creates a frame for the specified ServerView
     *
     * @param serverView the ServerView to visualize
     */
    public ServerViewFrame(ServerView serverView)
    {
        super("Availiable Servers");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.serverView = serverView;
        getContentPane().add(new JScrollPane(getServerView()));
        setIconImage(new ImageIcon(ServerViewFrame.class.getResource("images/server.png")).getImage());
        updateMenu();
        updateToolBar();

        pack();
    }

    /**
     * Updates the menu of this frame.
     */
    public void updateMenu()
    {        
        JMenuBar res = new JMenuBar();
        setJMenuBar(res);
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
        
        if (getServerView().getActionMap().get("Refresh") != null) {
            toolBar.add(getServerView().getActionMap().get("Refresh"));
        }
        if (getServerView().getActionMap().get("ZoomIn") != null) {
            toolBar.add(getServerView().getActionMap().get("ZoomIn"));
        }
        if (getServerView().getActionMap().get("ZoomOut") != null) {
            toolBar.add(getServerView().getActionMap().get("ZoomOut"));
        }
    }

    /**
     * Returns the serverView of this frame
     */
    public ServerView getServerView()
    {
        return this.serverView;
    }
}
