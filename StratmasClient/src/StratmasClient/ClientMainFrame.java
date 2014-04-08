//         $Id: ClientMainFrame.java,v 1.69 2007/01/24 14:25:50 amfi Exp $

/*
 * @(#)ClientMainFrame.java
 */

package StratmasClient;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.lang.ref.WeakReference;
import javax.swing.event.MouseInputAdapter;
import javax.swing.JOptionPane;
import javax.swing.JMenuBar;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JRootPane;
import javax.swing.JComboBox;
import javax.swing.JProgressBar;
import javax.swing.JFileChooser;
import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JToolBar;
import javax.swing.JMenu;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JPopupMenu;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.WindowConstants;
import javax.swing.event.MenuListener;
import javax.swing.event.MenuEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.Component;
import java.awt.Image;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Frame;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;

import StratmasClient.treeview.HierarchyImportTreeView;
import StratmasClient.treeview.OrderImportTreeView;
import StratmasClient.filter.TypeFilter;
import StratmasClient.evolver.EvolverGUI;
import StratmasClient.evolver.EvolverFrame;
import StratmasClient.dispatcher.ServerView;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasObjectFactory;
import StratmasClient.object.StratmasList;
import StratmasClient.object.type.TypeFactory;
import StratmasClient.object.primitive.Reference;
import StratmasClient.map.Visualizer;

/**
 * ClientMainFrame is JFrame adapted to use as the main window for the
 * StratmasClient.
 *
 * @version 1, $Date: 2007/01/24 14:25:50 $
 * @author  Daniel Ahlin
*/
public class ClientMainFrame extends JFrame
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 959130962719129281L;

	/**
     * The current client instance
     */
    Client client;

    /**
     * The frames of tabs included into this frame.
     */
    Hashtable<JFrame, PropertyChangeListener> tabFrames = new Hashtable<JFrame, PropertyChangeListener>();

    /**
     * The mapping of tabs to tabbed frames
     */
    Hashtable<JRootPane, JFrame> componentFrames = new Hashtable<JRootPane, JFrame>();
    
    /**
     * The ToolBar of the frame.
     */
    JToolBar toolBar;

    /**
     * The tabbed pane that is the main component of this frame
     */
    JTabbedPane tabPane = new JTabbedPane();

    /**
     * Outstanding work, used to maintain a measure of how busy the client is.
     */
    static long outstandingWork = 0;

    /**
     * Lock object for outstanding work.
     */
    static Object outstandingWorkLock = new Object();

    /**
     * If this frame is editable.
     */
    boolean isEditable = true;
    /**
     * Indicator for the communication between the client and the server.
     */
    static final JProgressBar progressBar = new JProgressBar();
    
    /**
     * Information about the current process (used for the time consuming processes).
     */
    static final JTextField infoField = new JTextField();

    /**
     * The filename of the last successful open command.
     */
    String filename = null;
    
    /**
     * The actual symbol id code mapping table.
     */ 
    static String actualSymbolIDCodeMappingTable = null;

    /**
     * Creates a frame for the specified client.
     *
     * @param client the client to visualize.
     */
    public ClientMainFrame(Client client) {
        super("Stratmas Client");        
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    dispose();
                }
            });
        this.client = client;
        // set initial filename to any given on the command line.
        setFilename(client.getFilename());
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(this.tabPane, BorderLayout.CENTER);
        // set the information text field
        infoField.setEditable(false);
        infoField.setBackground(this.getBackground());
        // set the comunication indicator
        progressBar.setStringPainted(true);
        progressBar.setBorderPainted(true);
        progressBar.setValue(0);
        progressBar.setString("");
        // layout the panel
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(infoField, BorderLayout.CENTER);
        panel.add(progressBar, BorderLayout.EAST);
        getContentPane().add(panel, BorderLayout.SOUTH);
        
        // find a suitable image for this application
        setIconImage(new ImageIcon(getClass().getResource("icons/stratmas.png")).getImage());
        
        // update ...
        updateTabs();        
        updateMenu();
        updateToolBar();
        createDnDHandler();
        createTabMouseListener();
    }
    
    /**
     * Returns the filename of the last succesful open, or null if none.
     */
    public String getFilename() {
        return this.filename;
    }
    
    /**
     * Sets the filename of the last succesful open, or null if none.
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    /**
     * Returns the progress bar.
     */
    public JProgressBar getProgressBar() {
        return progressBar;
    }
    
    /**
     * Returns the information filed.
     */
    public JTextField getInfoField() {
        return infoField;
    }
    
    /**
     * Sets the progress bar on/off.
     */
    public static void activateProgressBar(final boolean activate, final String info) {
        if (progressBar != null) {
            SwingUtilities.invokeLater (new Runnable() {
                    public void run() {
                        progressBar.setIndeterminate(activate);
                        progressBar.setValue(0);
                        progressBar.setString("");  
                        infoField.setText(info);   
                    }
                });
        }
    }
    
    /**
     * Creates the DnD Drop Interface that switches to tabs lying under the tab.
     */
    protected void createDnDHandler() {
        getTabPane().setDropTarget(new DropTarget(getTabPane(), new DropTargetAdapter() {
                Timer tabFocusDelayTimer = new Timer();
                int FOCUS_DELAY_MS = 200;
                
                public void dragEnter(DropTargetDragEvent dtde) {
                    tabFocusDelayTimer.cancel();
                    dtde.acceptDrag(dtde.getDropAction());
                }
                
                public void dragExit(DropTargetEvent dtde) {
                    tabFocusDelayTimer.cancel();
                }

                public void dragOver(DropTargetDragEvent dtde) {
                    tabFocusDelayTimer.cancel();
                    final int index = getTabPane().indexAtLocation((int) dtde.getLocation().getX(), 
                                                                   (int) dtde.getLocation().getY());
                    if (index != -1) {
                        tabFocusDelayTimer = new Timer();
                        tabFocusDelayTimer.schedule(new TimerTask() {
                                public void run() {
                                    getTabPane().setSelectedIndex(index);
                                }
                            }, FOCUS_DELAY_MS);
                    }
                    dtde.acceptDrag(dtde.getDropAction());
                }
                
                public void drop(DropTargetDropEvent dtde) {
                    tabFocusDelayTimer.cancel();
                    dtde.rejectDrop();
                }
            }));
    }

    /**
     * Register listener that handles popup clicks on tab-handles.
     */
    protected void createTabMouseListener() {
        getTabPane().addMouseListener(new MouseInputAdapter() {
                public void mousePressed(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        final int index = getTabPane().indexAtLocation(e.getX(), e.getY());
                        if (index != -1) {
                            JPopupMenu menu = new JPopupMenu();
                            menu.add(new AbstractAction("Evict") {
                                    /**
								 * 
								 */
								private static final long serialVersionUID = -2718205416779788574L;

									public void actionPerformed(ActionEvent ev) {
                                        Component component = getTabPane().getComponentAt(index);
                                        if (component != null) {
                                            frameTab(component);
                                        }
                                    }
                                });
                            menu.add(new AbstractAction("Close") {
                                    /**
								 * 
								 */
								private static final long serialVersionUID = 7730114120753963653L;

									public void actionPerformed(ActionEvent ev) {
                                        Component component = getTabPane().getComponentAt(index);
                                        if (component != null) {
                                            JFrame frame = componentFrames.get(component);
                                            if (frame != null) {
                                                frameTab(component);
                                                frame.dispose();
                                            } else {
                                                removeTab(component);
                                            }
                                        }
                                    }
                                });
                            menu.pack();
                            menu.show(getTabPane(), e.getX(), e.getY());
                        }
                    }
                }
            });
    }

    /**
     * Returns the root object of the client (or null if no such).
     */
    public StratmasObject getRootObject() 
    {
        return getClient().getRootObject();
    }

    /**
     * Updates the menu of this frame.
     */
    public void updateMenu()
    {        
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File", true);
        //JMenu editMenu = new JMenu("Edit", true);
        JMenu viewMenu = new JMenu("View", true);
        JMenu toolMenu = new JMenu("Tools", true);
        JMenu helpMenu = new JMenu("Help", true);


        viewMenu.addMenuListener(new MenuListener()
            {
                public void menuCanceled(MenuEvent e)
                {
                }
                public void menuDeselected(MenuEvent e)
                {
                }                
                public void menuSelected(MenuEvent e)
                {
                    JMenu menu = (JMenu) e.getSource();
                    updateViewMenu(menu);
                }
            });

        fileMenu.addMenuListener(new MenuListener()
            {
                public void menuCanceled(MenuEvent e)
                {
                }
                public void menuDeselected(MenuEvent e)
                {
                }                
                public void menuSelected(MenuEvent e)
                {
                    JMenu menu = (JMenu) e.getSource();
                    updateFileMenu(menu);
                }
            });
        toolMenu.addMenuListener(new MenuListener()
            {
                public void menuCanceled(MenuEvent e)
                {
                }
                public void menuDeselected(MenuEvent e)
                {
                }                
                public void menuSelected(MenuEvent e)
                {
                    JMenu menu = (JMenu) e.getSource();
                    updateToolMenu(menu);
                }
            });

        helpMenu.addMenuListener(new MenuListener()
            {
                public void menuCanceled(MenuEvent e)
                {
                }
                public void menuDeselected(MenuEvent e)
                {
                }                
                public void menuSelected(MenuEvent e)
                {
                    JMenu menu = (JMenu) e.getSource();
                    updateHelpMenu(menu);
                }
            });        
        
        menuBar.add(fileMenu);
        //menuBar.add(editMenu);
        menuBar.add(toolMenu);        
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);

        // Set up a frame with some debug buttons
        if (Debug.isInDebugMode()) {
            JMenu debugMenu = new JMenu("Debug", true);
            debugMenu.addMenuListener(new MenuListener()
                {
                    public void menuCanceled(MenuEvent e)
                    {
                    }
                    public void menuDeselected(MenuEvent e)
                    {
                    }                
                    public void menuSelected(MenuEvent e)
                    {
                        JMenu menu = (JMenu) e.getSource();
                        menu.removeAll();
                        for (Iterator<Action> it = Debug.getDebugActions().iterator(); 
                             it.hasNext();
                             menu.add((Action) it.next()));
                    }
                });
            menuBar.add(debugMenu);
        }
        
        setJMenuBar(menuBar);
        validate();
    }

    /**
     * Makes the provided menu into an fileMenu
     *
     * @param menu 
     */
    public void updateToolMenu(JMenu menu)
    {
        final ClientMainFrame self = this;
        menu.removeAll();

        Action importResourceAction = new AbstractAction("Import from IconFactory2") {
                /**
			 * 
			 */
			private static final long serialVersionUID = 7173282375223367665L;

				public void actionPerformed(ActionEvent e) {
                    SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                ImportSourceDialog.showDialog(self);
                            }
                        });
                }
            };
        importResourceAction.setEnabled(getClient().getRootObject().getChildCount() > 0);
        menu.add(importResourceAction);
        
        Action importOrderLib = new AbstractAction("Import Order Library") {
                  /**
			 * 
			 */
			private static final long serialVersionUID = -3401463626694788137L;

				public void actionPerformed(ActionEvent e) {
                       final String filename = Client.getFileNameFromDialog(".oli", JFileChooser.OPEN_DIALOG);
                       if (filename != null) {
                            StratmasDialog.showProgressBarDialog(self, "Importing an order library ...");
                            Thread thread = new Thread() {
                                      public void run() {
                                           StratmasObject o = Client.importXMLFile(filename);
                                           StratmasList list = (o == null ? null : (StratmasList)o.getChild("identifiables"));
                                           if (list != null) {        
                                                final TypeFilter filter = new TypeFilter(TypeFactory.getType("Order"));
                                                final JFrame frame = OrderImportTreeView.getDefaultFrame(list, filter);
                                                frame.setTitle(filename);
                                                frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                                                SwingUtilities.invokeLater(new Runnable() {
                                                          public void run() {
                                                               frame.setVisible(true);
                                                          }
                                                     });
                                           }
                                           StratmasDialog.quitProgressBarDialog(); 
                                      }
                                 };
                            thread.start();
                       } 
                  }
             };
        importOrderLib.setEnabled(getClient().getRootObject().getChildCount() > 0);
        menu.add(importOrderLib);

        Action importMUTac = new AbstractAction("Import Military Unit Library") {
                  /**
			 * 
			 */
			private static final long serialVersionUID = -3391479623087100926L;

				public void actionPerformed(ActionEvent e) {
                       final String filename = Client.getFileNameFromDialog(".uli", JFileChooser.OPEN_DIALOG);
                       if (filename != null) {
                            StratmasDialog.showProgressBarDialog(self, "Importing Military Units...");
                            Thread thread = new Thread() {
                                      public void run() {
                                           StratmasObject o = Client.importXMLFile(filename);
                                           StratmasList list = (o == null ? null : (StratmasList)o.getChild("identifiables"));
                                           if (list != null) {        
                                                final TypeFilter filter = new TypeFilter(TypeFactory.getType("MilitaryUnit"));
                                                final JFrame frame = HierarchyImportTreeView.getDefaultFrame(list, filter);
                                                frame.setTitle(filename);
                                                frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                                                SwingUtilities.invokeLater(new Runnable() {
                                                          public void run() {
                                                               frame.setVisible(true);
                                                          }
                                                     });
                                                StratmasDialog.quitProgressBarDialog(); 
                                           }
                                      }
                                 };
                            thread.start();
                       } 
                  }
             };
        importMUTac.setEnabled(getClient().getRootObject().getChildCount() > 0);
        menu.add(importMUTac);

        Action evolverAction = new AbstractAction("Evolve scenario")
            {
                /**
				 * 
				 */
				private static final long serialVersionUID = 663418595609287008L;

				public void actionPerformed(ActionEvent e) 
                {
                    SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                StratmasObject simulationList = 
                                    StratmasObjectFactory.cloneObject(getClient().getRootObject());
                                EvolverFrame frame = new EvolverFrame(new EvolverGUI((StratmasObject) simulationList.children().nextElement()));
                                frame.pack();
                                frame.setVisible(true);
                            }
                        });
                }
            };
        evolverAction.setEnabled(getClient().getStratmasDispatcher() != null &&  
                                 getClient().getRootObject().getChildCount() > 0);
        menu.add(evolverAction);
        
        menu.add(new AbstractAction("Preferences") {
                  /**
			 * 
			 */
			private static final long serialVersionUID = 3442973424707132550L;

				public void actionPerformed(ActionEvent e) {
                       Configuration.showConfigurationFrame(null);
                  }
             });
        
        Action knownServers = new AbstractAction("Known Servers") {
                  /**
			 * 
			 */
			private static final long serialVersionUID = -2925122339205165467L;

				public void actionPerformed(ActionEvent e) {
                       JFrame frame = ServerView.getDefaultFrame(getClient().getStratmasDispatcher());
                       frame.pack();
                       frame.setVisible(true);
                       tabFrame(frame);
                  }
             };
        knownServers.setEnabled(getClient().getStratmasDispatcher() != null);
        menu.add(knownServers);
        
        Action substrate = new AbstractAction("Edit in Substrate") {
        	/**
			 * 
			 */
			private static final long serialVersionUID = -935702147105928933L;

			public void actionPerformed(ActionEvent e) {
        		client.startSubstrateEditor(getFilename());
        	}
        };
        substrate.setEnabled(getClient().getRootObject().getChildCount() > 0);	// Can't edit nothing
        menu.add(substrate);
    }

    /**
     * Makes the provided menu into an fileMenu
     *
     * @param menu 
     */
    public void updateFileMenu(JMenu menu) {
        final ClientMainFrame self = this;
        menu.removeAll();
        
        Action createSimulation = new AbstractAction("Create") {
                /**
			 * 
			 */
			private static final long serialVersionUID = 6045024397470503167L;

				public void actionPerformed(ActionEvent e) {
                       getClient();
					final String filePath =  Client.getTemplateFilePath();
                       if (filePath != null) {
                            StratmasDialog.showProgressBarDialog(self, "Loading an empty scenario ...");
                            Thread thread = new Thread() {
                                      public void run() {
                                          StratmasObject obj = Client.getTemplateSimulation(filePath);
                                          if (obj != null) {
                                              getClient().getRootObject().add(obj);
                                          }
                                          StratmasDialog.quitProgressBarDialog();
                                      }
                                 };
                            thread.start();
                       }
                       //self.getClient().getRootObjectFromGUIConstructor();
                  }
             };
        createSimulation.setEnabled(getClient().getRootObject().getChildCount() == 0);
        menu.add(createSimulation);

        Action saveAsAction = new AbstractAction("Save as")  {
                  /**
			 * 
			 */
			private static final long serialVersionUID = 2342413793263399990L;

				public void actionPerformed(ActionEvent e) {
                       if (getClient().getRootObject().children().hasMoreElements()) {
                            final StratmasObject object = (StratmasObject) getRootObject().children().nextElement();
                            final String ext = "scn";
                            String filename = Client.getFileNameFromDialog(ext, JFileChooser.SAVE_DIALOG);
                            if (filename != null) {
                                 final File file = new File(filename);
                                 StratmasDialog.showProgressBarDialog(self, "Saving a scenario ...");
                                 Thread thread = new Thread() {
                                           public void run() {
                                                if (ext.equalsIgnoreCase(FileExtensionFilter.getExtension(file))) {
                                                     Client.exportToXML(object, file.getPath());
                                                } 
                                                else {
                                                     Client.exportToXML(object, file.getPath() + ".scn");
                                                }
                                                StratmasDialog.quitProgressBarDialog(); 
                                           }
                                      };
                                 thread.start();
                            }
                       }
                  }
             };
        saveAsAction.setEnabled(getClient().getRootObject().getChildCount() > 0);
        menu.add(saveAsAction);

        Action saveAction = new AbstractAction("Save") {
                  /**
			 * 
			 */
			private static final long serialVersionUID = 2469311998566638026L;

				public void actionPerformed(ActionEvent e) {
                       if (getClient().getRootObject().children().hasMoreElements()) {
                            final StratmasObject object = (StratmasObject) getRootObject().children().nextElement();
                            if (getFilename() != null) {
                                 StratmasDialog.showProgressBarDialog(self, "Saving scenario ...");
                                 Thread thread = new Thread() {
                                           public void run() {
                                                Client.exportToXML(object, getFilename());
                                                StratmasDialog.quitProgressBarDialog(); 
                                           }
                                      };
                                 thread.start();
                            }
                       }
                  }
             };
        saveAction.setEnabled(getClient().getRootObject().getChildCount() > 0 &&
                              getFilename() != null);
        menu.add(saveAction);
        
        Action openAction = new AbstractAction("Open") {
                  /**
			 * 
			 */
			private static final long serialVersionUID = 8045827972226536868L;

				public void actionPerformed(ActionEvent e) {
                       final String filename = Client.getFileNameFromDialog(".scn", JFileChooser.OPEN_DIALOG);
                       if (filename != null) {
                            StratmasDialog.showProgressBarDialog(self, "Loading a scenario ...");
                            Thread thread = new Thread() {
                                      public void run() {
                                           getClient();
										StratmasObject obj = Client.importXMLSimulation(filename);
                                           if (obj != null) {
                                                getClient().getRootObject().add(obj);
                                                setFilename(filename);
                                           }
                                           StratmasDialog.quitProgressBarDialog();
                                      }
                                 };
                            thread.start();
                       }
                  }
             };
        openAction.setEnabled(getClient().getRootObject().getChildCount() == 0);
        menu.add(openAction);

        Action closeAction = new AbstractAction("Close") {
                  /**
			 * 
			 */
			private static final long serialVersionUID = -6817846233558993065L;

				public void actionPerformed(ActionEvent e) {
                       StratmasDialog.showProgressBarDialog(self, "Closing a scenario ...");
                       Thread thread = new Thread() {
                               public void run() {
                                   Visualizer.remove();
                                   ((StratmasObject) getClient().getRootObject().children().nextElement()).remove();
                                   self.getClient().getTimeline().remove();
                                   StratmasDialog.quitProgressBarDialog();
                                 }
                            };
                       thread.start();
                  }
             };
        closeAction.setEnabled(getClient().getRootObject().getChildCount() > 0);
        menu.add(closeAction);

        menu.addSeparator();
        Action passiveMode = new AbstractAction("Passive mode") {
                  /**
			 * 
			 */
			private static final long serialVersionUID = -2696490218401276297L;

				public void actionPerformed(ActionEvent e) {
                       final String serverName = getClient().getServerName();
                       if (serverName != null) {
                            StratmasDialog.showProgressBarDialog(self, "Initializing - passive mode ...");
                            Thread thread = new Thread() {
                                      public void run() {
                                           getClient().getRootObjectFromServer();
                                           StratmasDialog.quitProgressBarDialog();
                                      }
                                 };
                            thread.start();        
                       }
                  }
             };
        passiveMode.setEnabled(getClient().getRootObject().getChildCount() == 0);
        menu.add(passiveMode);
                
        menu.addSeparator();
        // Look for existing close action.
        if (getRootPane().getActionMap().get("Quit") != null) {
             menu.add(getRootPane().getActionMap().get("Quit"));
        }
        else {
             menu.add(new AbstractAction("Quit") {
                       /**
				 * 
				 */
				private static final long serialVersionUID = 5197123888331774243L;

					public void actionPerformed(ActionEvent e) {
                           dispose();
                       }
                  });
        }
    }

    /**
     * Makes the provided menu into an viewMenu
     *
     * @param menu 
     */
    public void updateViewMenu(JMenu menu)
    {
        menu.removeAll();
        
        final JMenu showMenu = new JMenu("Show");
        final JMenu tabMenu = new JMenu("Tabs");

        JMenuItem showAll = new JMenuItem(new AbstractAction("All")
            {
                /**
				 * 
				 */
				private static final long serialVersionUID = 3222875167311783175L;

				public void actionPerformed(ActionEvent e)
                {
                    for (int i = 0; i < showMenu.getItemCount(); i++) {
                        JMenuItem item = showMenu.getItem(i);
                        if (!item.equals(e.getSource()) &&
                            item instanceof JCheckBoxMenuItem &&
                            !((JCheckBoxMenuItem) item).getState()) {
                            item.doClick();
                        }
                    }
                }
            });

        JMenuItem showNone = new JMenuItem(new AbstractAction("None")
            {
                /**
				 * 
				 */
				private static final long serialVersionUID = -783188624734069428L;

				public void actionPerformed(ActionEvent e)
                {
                    for (int i = 0; i < showMenu.getItemCount(); i++) {
                        JMenuItem item = showMenu.getItem(i);
                        if (!item.equals(e.getSource()) &&
                            item instanceof JCheckBoxMenuItem &&
                            ((JCheckBoxMenuItem) item).getState()) {
                            item.doClick();
                        }
                    }
                }
            });
        showMenu.add(showAll);
        showMenu.add(showNone);
        

        JMenuItem tabAll = new JMenuItem(new AbstractAction("All")
            {
                /**
				 * 
				 */
				private static final long serialVersionUID = -8106911070244413743L;

				public void actionPerformed(ActionEvent e)
                {
                    for (int i = 0; i < tabMenu.getItemCount(); i++) {
                        JMenuItem item = tabMenu.getItem(i);
                        if (!item.equals(e.getSource()) &&
                            item instanceof JCheckBoxMenuItem &&
                            !((JCheckBoxMenuItem) item).getState()) {
                            item.doClick();                            
                        }
                    }
                }
            });

        JMenuItem tabNone = new JMenuItem(new AbstractAction("None")
            {
                /**
				 * 
				 */
				private static final long serialVersionUID = -5319677451572099156L;

				public void actionPerformed(ActionEvent e)
                {
                    for (int i = 0; i < tabMenu.getItemCount(); i++) {
                        JMenuItem item = tabMenu.getItem(i);
                        if (!item.equals(e.getSource()) &&
                            item instanceof JCheckBoxMenuItem &&
                            ((JCheckBoxMenuItem) item).getState()) {
                            item.doClick();
                        }
                    }
                }
            });

        tabMenu.add(tabAll);
        tabMenu.add(tabNone);

        Frame[] frames = Frame.getFrames();
        for (int i = 0; i < frames.length; i++) {
            Frame frame = frames[i];
            if (frame == this || !frame.isDisplayable() ||
                tabFrames.containsKey(frame) || frame.getTitle() == null ||
                frame.getTitle().length() == 0) {
                // Don't include ourselves, any disposed frames, any
                // tabbed frames in window actions or any frames
                // without titles.
                continue;
            }
            // To avoid locking the frame in memory.
            final WeakReference<Frame> frameReference = new WeakReference<Frame>(frame);
            JCheckBoxMenuItem showMenuItem = 
                new JCheckBoxMenuItem(new AbstractAction(frame.getTitle())
                    {
                        /**
						 * 
						 */
						private static final long serialVersionUID = -727765262280277950L;

						public void actionPerformed(ActionEvent e) 
                        {
                            JCheckBoxMenuItem box = (JCheckBoxMenuItem) e.getSource();
                            Frame f = frameReference.get();
                            if (f != null) {
                                f.setVisible(box.isSelected());
                            } else {
                                box.setEnabled(false);
                            }
                        }
                    });
            showMenuItem.setSelected(frame.isVisible());
            showMenu.add(showMenuItem);

            if (frame instanceof JFrame) {
                JCheckBoxMenuItem tabMenuItem = 
                    new JCheckBoxMenuItem(new AbstractAction(frame.getTitle())
                        {
                            /**
							 * 
							 */
							private static final long serialVersionUID = -5660800283579931616L;

							public void actionPerformed(ActionEvent e) 
                            {
                                JCheckBoxMenuItem box = (JCheckBoxMenuItem) e.getSource();
                                JFrame f = (JFrame) frameReference.get();
                                if (f != null) {
                                    tabFrame(f);
                                } else {
                                    box.setEnabled(false);
                                }
                            }
                        });
                tabMenuItem.setSelected(false);
                tabMenu.add(tabMenuItem);
            }
        }

        for (int i = 0; i < this.tabPane.getTabCount(); i++) {
            final Component component = this.tabPane.getComponentAt(i);
            JCheckBoxMenuItem tabMenuItem = 
                new JCheckBoxMenuItem(new AbstractAction(this.tabPane.getTitleAt(i))
                    {
                        /**
						 * 
						 */
						private static final long serialVersionUID = -2449378619838935047L;

						public void actionPerformed(ActionEvent e) 
                        {
                            frameTab(component);
                        }
                    });
            tabMenuItem.setSelected(true);
            tabMenu.add(tabMenuItem);
        }

        if (showMenu.getItemCount() < 3) {
            showMenu.setEnabled(false);
        }
        if (tabMenu.getItemCount() < 3) {
            tabMenu.setEnabled(false);
        }

        menu.add(showMenu);
        menu.add(tabMenu);
    }

    /**
     * Makes the provided menu into a helpMenu
     *
     * @param menu 
     */
    public void updateHelpMenu(JMenu menu)
    {
        menu.removeAll();
        
        JMenuItem about = new JMenuItem(new AbstractAction("About...") {
                  /**
			 * 
			 */
			private static final long serialVersionUID = -7105990138902744708L;

				public void actionPerformed(ActionEvent e) {
                       AboutBox.show();
                  }
             });
        menu.add(about);
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

//         toolBar.addSeparator();
//         toolBar.add(new AbstractAction("Close", 
//                                        new ImageIcon(getClass().getResource("icons/close.png")))
//             {
//                 public void actionPerformed(ActionEvent e)
//                 {
//                     Component component = getTabPane().getSelectedComponent();
//                     if (component != null) {
//                         JFrame frame = (JFrame) componentFrames.remove(component);
//                         if (frame != null) {
//                             frameTab(component);
//                             frame.dispose();
//                         } else {
//                             removeTab(component);
//                         }
//                         }
//                 }
//             }).setToolTipText("Close current tab");
        
//         if (getTreeView().getActionMap().get("ZoomIn") != null) {
//             toolBar.add(getTreeView().getActionMap().get("ZoomIn"));
//         }
//         if (getTreeView().getActionMap().get("ZoomOut") != null) {
//             toolBar.add(getTreeView().getActionMap().get("ZoomOut"));
//         }
    }

    /**
     * Updates the tabs of this frame.
     */
    public void updateTabs()
    {
    }

    /**
     * Removes the specified component from the tabs of this frame.
     */
    public void removeTab(Component component)
    {
        tabPane.remove(component);
    }

    /**
     * Returns the tabpane of this frame.
     */
    public JTabbedPane getTabPane()
    {
        return this.tabPane;
    }

    /**
     * Imports a JFrame as tab in this frame.
     *
     * @param frame the frame to import
     */
    public void tabFrame(JFrame frame)
    {
        final JRootPane component = new JRootPane();
        component.setOpaque(true);
        ImageIcon icon = null;
        if (frame.getIconImage() != null) {
            icon = new ImageIcon(frame.getIconImage().getScaledInstance(16, 
                                                                        16, 
                                                                        Image.SCALE_SMOOTH));
        }

        //component.setGlassPane(frame.getRootPane().getGlassPane());
        //component.setLayeredPane(frame.getRootPane().getLayeredPane());
        component.setContentPane(frame.getRootPane().getContentPane());

        // This listener changes the name of the tab when the frame title changes.
        PropertyChangeListener pcl = new PropertyChangeListener() {
                  public void propertyChange(PropertyChangeEvent event) {
                       if (event.getPropertyName().equals("title")) {
                            int i = getTabPane().indexOfComponent(component);
                            if (i != -1) {
                                 getTabPane().setTitleAt(i, event.getNewValue().toString());
                            }
                       }
                  }
             };
     
        addTab(frame.getTitle(), icon, component);
        tabFrames.put(frame, pcl);
        componentFrames.put(component, frame);

        frame.setVisible(false);

        // Add listener that kills the pane if the frame is disposed
         frame.addWindowListener(new WindowAdapter() 
             {
                 public void windowClosed(WindowEvent e) 
                {
                    e.getWindow().removeWindowListener(this);
                     componentFrames.remove(component);
                     tabFrames.remove(e.getWindow());
                    removeTab(component);                    
                 }
             });
        
        // Update tab title when frame title changes.
        // FIXME: when should we unlisten to this?
        frame.addPropertyChangeListener(pcl);
    }

    /**
     * Adds the component as a tab to this frame
     *
     * @param title the title of the tab
     * @param icon the icon of the tab (may be null).
     * @param component the component to add.
     */
    public void addTab(String title, ImageIcon icon, Component component)
    {
        
        this.tabPane.addTab(title, 
                            icon,
                            component);
        this.tabPane.setSelectedComponent(component);
    }

    /**
     * Do some work common to frameTab(component) and frameTab(component, int, int).
     *
     * @param component the component to export
     * @return the resulting frame 
     */
    private JFrame frameTabImpl(Component component)
    {
        JFrame frame = componentFrames.remove(component);
        if (frame == null) {
            String title = this.tabPane.getTitleAt(this.tabPane.indexOfComponent(component));
            ImageIcon icon = (ImageIcon) this.tabPane.getIconAt(this.tabPane.indexOfComponent(component));
            frame = new JFrame();
            frame.setTitle(title);
            if (icon != null) {
                frame.setIconImage(icon.getImage());
            }
        } else {
             // Remove the tab name update listener.
             frame.removePropertyChangeListener(tabFrames.get(frame));
            // Unregister frame;
             tabFrames.remove(frame);
        }

        this.tabPane.remove(component);
        if (component instanceof JRootPane) {
            JRootPane rootPane = (JRootPane) component;
            frame.setContentPane(rootPane.getContentPane());
            frame.getRootPane().updateUI();
        } else {
            frame.getContentPane().add(component);
        }

        return frame;
//         //frame.pack();
//         final JFrame fFrame = frame;

//         javax.swing.SwingUtilities.invokeLater(new Runnable() 
//             {
//                 public void run() 
//                 {
//                     fFrame.setVisible(true);
//                 }
//             });
    }

    /**
     * Do some work common to frameTab(component) and frameTab(component, int, int).
     *
     * @param component the component to export
     * @return the resulting frame 
     */
    public JFrame frameTab(Component component)
    {
        JFrame frame = frameTabImpl(component);
        frame.setVisible(true);
        return frame;
    }


    /**
     * Returns true if editing is enabled in this frame.
     */
    public boolean isEditable()
    {
        return isEditable;
    }

    /**
     * Set to true to allow editing in this frame.
     *
     * @param editable true to allow editing in this frame.
     */
    public void setEditable(boolean editable)
    {
        isEditable = editable;
        updateMenu();
    }
    
    /**
     * Increases the number of unfinished tasks
     */
    public static void upOutstandingWork()
    {
        synchronized (outstandingWorkLock) {
            outstandingWork++;
        }
    }

    /**
     * Decreases the number of unfinished tasks
     */
    public static void downOutstandingWork()
    {
        synchronized (outstandingWorkLock) {
            outstandingWork--;
        }
    }
    
    /**
     * Gets the number of outstanding tasks.
     */
    public static long getOutstandingWork()
    {
        return outstandingWork;
    }

    /**
     * Returns the client of this frame
     */
    public Client getClient()
    {
        return this.client;
    }

    /**
     * Shows a confirmation dialog before disposing.
     */
    public void dispose() {
        if (JOptionPane.showConfirmDialog(this, "Really quit?", "Exiting...", JOptionPane.YES_NO_OPTION) == 0) {
            super.dispose();
            // For good measure:
            System.exit(0);
        }
    }
    
}


/**
 * This dialog is used to import military units from IconFactory2 and initialize these units with
 * default values from the table source file.
 */
class ImportSourceDialog extends JDialog {
    /**
	 * 
	 */
	private static final long serialVersionUID = 7991288015956992030L;
	/**
     * Reference to the client.
     */
    private Client client;
    /**
     * Reference to the client main frame.
     */
    private ClientMainFrame clientMainFrame;
    /**
     * Instance of the actual dialog.
     */
    private static ImportSourceDialog dialog;
    /**
     * The actual symbol id code mapping table.
     */ 
    static String actualSymbolIDCodeMappingTable = null;
    /**
     * The text filed which contains the name of the IF2 source file.
     */
    final JTextField fileTextField = new JTextField(10);
    /**
     * The text filed which contains the name of the table source file.
     */
    final JTextField tableTextField = new JTextField(10);
    /**
     * The list of factions.
     */
    final JComboBox factionBox = new JComboBox();
    
    public static void showDialog(ClientMainFrame clientMainFrame) {
        dialog = new ImportSourceDialog(clientMainFrame);
        dialog.setVisible(true);
    }    

    /**
     * Creates the dialog.
     */
    public ImportSourceDialog(ClientMainFrame clientMainFrame) {
        super(new JFrame(), "Import from IF2");
        this.clientMainFrame = clientMainFrame;
        this.client = clientMainFrame.getClient();
                
        JPanel importFromIF2Panel = new JPanel(new GridLayout(4, 1, 5, 5));
        importFromIF2Panel.add(createSourceFilePanel());
        importFromIF2Panel.add(createTableDefaultPanel());
        importFromIF2Panel.add(createFactionList());
        importFromIF2Panel.add(createButtonPanel());
        importFromIF2Panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        
        setContentPane(importFromIF2Panel);
        setSize(new Dimension(300, 300));
        setLocationRelativeTo(clientMainFrame);
    }
    
    /**
     * Creates the panel which shows the name of the source file.
     */
    private JPanel createSourceFilePanel() {
        // the file panel
        JButton fileButton = new JButton("...");
        fileButton.addActionListener(new AbstractAction() {
                /**
			 * 
			 */
			private static final long serialVersionUID = -5303759660663807065L;

				public void actionPerformed(ActionEvent e) {
                    String filename = Client.getFileNameFromDialog(".xml", JFileChooser.OPEN_DIALOG);
                    if (filename != null) {
                        fileTextField.setText(filename); 
                    }
                }
            });
        JPanel filePanel = new JPanel(new BorderLayout(10, 10));
        filePanel.add(fileTextField, BorderLayout.CENTER);
        filePanel.add(fileButton, BorderLayout.EAST);
        filePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Select file"),
                                                               BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return filePanel;
    }
    
    /**
     * Creates the panel which shows the name of the file with the default values for 
     * different military units.
     */
    private JPanel createTableDefaultPanel() {
        // the table panel
        if (actualSymbolIDCodeMappingTable != null) {
            tableTextField.setText(actualSymbolIDCodeMappingTable);
        }
        JButton tableButton = new JButton("...");
        tableButton.addActionListener(new AbstractAction() {
                /**
			 * 
			 */
			private static final long serialVersionUID = 1625812086132286049L;

				public void actionPerformed(ActionEvent e) {
                    String filename = Client.getFileNameFromDialog(".csv", JFileChooser.OPEN_DIALOG);
                    if (filename != null) {
                        tableTextField.setText(filename); 
                    }
                }
            });
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.add(tableTextField, BorderLayout.CENTER);
        tablePanel.add(tableButton, BorderLayout.EAST);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Select table"),
                                                                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return tablePanel;
    }
    
    /**
     * Creates the list of the factions.
     */
    private JComboBox createFactionList() {
            // the list of factions
        factionBox.setFont(factionBox.getFont().deriveFont(Font.PLAIN));
        try {
            StratmasObject root = (StratmasObject)client.getRootObject().children().nextElement();
            StratmasObject scenario = root.getChild("scenario");
            StratmasList facList = (StratmasList)scenario.getChild("factions");        
            for (Enumeration<StratmasObject> en = facList.children(); en.hasMoreElements(); ) {
                factionBox.addItem((StratmasObject)en.nextElement());
            }
        }
        catch (NullPointerException exc) {
            Debug.err.println("No factions found when importing from IF2.");
        }
        factionBox.setBorder(BorderFactory.
                             createCompoundBorder(BorderFactory.createTitledBorder("Select affiliation faction"),
                                                  BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return factionBox;
    }
    
    /**
     * Creates the panel which contains the buttons.
     */
    private JPanel createButtonPanel() {
        final ImportSourceDialog self = this;
        final ClientMainFrame frame = clientMainFrame;
        // the canceling button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(cancelButton.getFont().deriveFont(Font.PLAIN));
        cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    self.setVisible(false);
                    self.dispose();
                }
            });
        // the approving button
        JButton okButton = new JButton("OK");
        okButton.setFont(okButton.getFont().deriveFont(Font.PLAIN));
        okButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    final String filename = fileTextField.getText();
                    if (filename.length() > 0) { 
                        StratmasDialog.showProgressBarDialog(frame, "Importing from IconFactory2 ...");
                        Thread thread = new Thread() {
                                public void run() {
                                    self.importUnitsAndSetDefaults(filename);
                                }
                            };
                        thread.start();
                    }
                }
            });
        // set the panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        buttonPanel.add(okButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        
        return buttonPanel;
    }
    
    /**
     * Imports the military units from IconFactory2 and sets the default values according
     * to the table described in the table source file. 
     */
    private void importUnitsAndSetDefaults(String filename) {
        final ImportSourceDialog self = this;
        IF2Importer importer = new IF2Importer(client.getRootObject());
        Reference facRef = ((StratmasObject)factionBox.getSelectedItem()).getReference();
        StratmasObject so = importer.importFromFile(filename, facRef);
        String tableFilename = tableTextField.getText();
        final TypeFilter filter = new TypeFilter(TypeFactory.getType("MilitaryUnit"));
        // the source table is selected
        if (tableFilename.length() > 0) {
            try {
                UnitImportFilter unitFilter = new UnitImportFilter(new FileInputStream(tableFilename));
                applyFilterToSubtree(so, unitFilter, filter);
                actualSymbolIDCodeMappingTable = tableFilename;
            }
            catch (java.io.IOException exc) {
            }
            catch (java.text.ParseException exc) {
            }
        }
        // the subtree is created
        if (so != null) {
            final JFrame frame = HierarchyImportTreeView.getDefaultFrame(so, filter);
            frame.setTitle(filename);
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        self.setVisible(false);
                        self.dispose();
                        frame.setVisible(true);
                    }
                });
            StratmasDialog.quitProgressBarDialog(); 
        }
        else {
            StratmasDialog.quitProgressBarDialog(); 
            if (importer.errorOccurred()) {
                JOptionPane.showMessageDialog((JFrame) null, importer.getErrorMessage(),
                                              "Icon Factory 2 import Error", 
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Applies UnitImportFilter to each military unit in the tree.
     *
     * @param root the root of the subtree.
     * @param filter the actual UnitImportFilter.
     * @param typeFilter the filter used to separate military units from other nodes in the tree.
     */
    private void applyFilterToSubtree(StratmasObject root, UnitImportFilter filter, TypeFilter typeFilter) {
        if (typeFilter.pass(root) && !(root instanceof StratmasList)) {
            filter.apply(root);
        }
        for (Enumeration<StratmasObject> e = root.children(); e.hasMoreElements(); ) {
            StratmasObject obj = (StratmasObject)e.nextElement();
            applyFilterToSubtree(obj, filter, typeFilter);
        }
    }
    
}
