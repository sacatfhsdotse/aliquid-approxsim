package StratmasClient;

import java.util.Vector;
import java.awt.Font;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import StratmasClient.object.StratmasEvent;
import StratmasClient.object.StratmasEventListener;
import StratmasClient.timeline.SpringUtilities;

/**
 * This panel controls the simulation. It contains buttons for connect/disconnect the client to/from the server, start/stop the simulation,
 * pause/continue the simulation, step forward one time step and exit the simulation. It should exist one control panel for each client.
 * 
 * @version 1.0
 * @author Amir Filipovic
 * @see <code>Client</code>
 */
public class ControllerPanel extends JPanel implements ActionListener {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1206772052563346909L;
    /**
     * The start/stop button.
     */
    final private JButton startStop = new JButton();
    /**
     * The pause/continue button.
     */
    private JButton pauseCont;
    /**
     * The step button.
     */
    private JButton step;
    /**
     * The connect/disconnect button.
     */
    final private JButton connect = new JButton();
    /**
     * The exit button.
     */
    private JButton exit;
    /**
     * Path to the images displayed on the buttons.
     */
    private String path = "map/images/";
    /**
     * Reference to the stratmas client.
     */
    private Client client;
    /**
     * Reference to the controller.
     */
    private Controller controller;
    /**
     * The list of listeners.
     */
    private Vector<StratmasEventListener> listeners = new Vector<StratmasEventListener>();
    /**
     * The frame in which this panel is shown.
     */
    private JFrame frame;

    /**
     * Creates control panel. Only connect/disconnect and exit buttons are enabled at first.
     * 
     * @param client reference to the client.
     * @param controller reference to the controller.
     */
    public ControllerPanel(Client client, Controller controller) {
        this.client = client;
        this.controller = controller;

        JPanel mainPanel = new JPanel(new GridLayout(1, 3, 2, 0));
        // start/stop button
        JPanel startStopPanel = new JPanel();
        startStop.setIcon(new ImageIcon(ControllerPanel.class.getResource(path
                + "Play16.gif")));
        startStop.setActionCommand("start");
        startStop.setToolTipText("Start the simulation");
        startStop.addActionListener(this);
        startStopPanel.add(startStop);
        startStopPanel.setBorder(BorderFactory
                .createCompoundBorder(BorderFactory
                        .createTitledBorder("Start/Stop"), BorderFactory
                        .createEmptyBorder(0, 10, 0, 10)));
        // puase/continue button
        JPanel pauseContPanel = new JPanel();
        pauseCont = new JButton(new ImageIcon(
                ControllerPanel.class.getResource(path + "Pause16.gif")));
        pauseCont.setActionCommand("pause");
        pauseCont.setToolTipText("Pause the simulation");
        pauseCont.addActionListener(this);
        pauseContPanel.add(pauseCont);
        TitledBorder pauseBorder = BorderFactory
                .createTitledBorder("Pause/Continue");
        pauseContPanel.setBorder(BorderFactory
                .createCompoundBorder(pauseBorder, BorderFactory
                        .createEmptyBorder(0, 10, 0, 10)));
        pauseContPanel.setPreferredSize(pauseBorder
                .getMinimumSize(pauseContPanel));

        // step forward button
        JPanel stepPanel = new JPanel();
        step = new JButton(new ImageIcon(ControllerPanel.class.getResource(path
                + "StepForward16.gif")));
        step.setToolTipText("Step forward");
        step.addActionListener(this);
        stepPanel.add(step);
        stepPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createTitledBorder("Step Forward"), BorderFactory
                .createEmptyBorder(0, 10, 0, 10)));
        mainPanel.add(startStopPanel);
        mainPanel.add(pauseContPanel);
        mainPanel.add(stepPanel);

        // connect/disconnect button
        JPanel connectPanel = new JPanel();
        if (client.getServerConnection() != null
                && client.getServerConnection().isAlive()) {
            connect.setText("Disconnect");
            connect.setToolTipText("Disconnect from the server");
        } else {
            connect.setText("Connect");
            connect.setToolTipText("Connect to the server");
        }
        connect.setFont(connect.getFont().deriveFont(Font.PLAIN));
        connect.addActionListener(this);
        connectPanel.add(connect);
        TitledBorder connectBorder = BorderFactory
                .createTitledBorder("Connect/Disconnect");
        connectPanel.setBorder(BorderFactory
                .createCompoundBorder(connectBorder, BorderFactory
                        .createEmptyBorder(0, 10, 0, 10)));
        connectPanel.setPreferredSize(connectBorder
                .getMinimumSize(connectPanel));

        // exit button
        JPanel exitPanel = new JPanel();
        exit = new JButton("Exit");
        exit.setToolTipText("Exit the simulation");
        exit.setFont(exit.getFont().deriveFont(Font.PLAIN));
        exit.setForeground(new Color(0.9f, 0.1f, 0.1f));
        exit.addActionListener(this);
        exitPanel.add(exit);
        exitPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createTitledBorder("Exit"), BorderFactory
                .createEmptyBorder(0, 10, 0, 10)));
        JPanel commPanel = new JPanel(new SpringLayout());
        commPanel.add(connectPanel);
        commPanel.add(exitPanel);
        SpringUtilities.makeCompactGrid(commPanel, 1, 2, 0, 0, 0, 0);

        // put it all together
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.WEST);
        add(new JLabel(), BorderLayout.CENTER);
        add(commPanel, BorderLayout.EAST);

        // set control buttons disabled
        enableControlButtons(false);

        // set the size for "connect" button - prevents the visual artifacts after the change of the text
        connect.setMaximumSize(connect.getPreferredSize());
        connect.setMinimumSize(connect.getPreferredSize());

        // show GUI
        // createAndShowGUI();
    }

    /**
     * Fires actions caused by pressing the buttons.
     */
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        // start/stop button
        if (source.equals(startStop)) {
            if (((JButton) source).getActionCommand().equals("start")) {
                startStop.setIcon(new ImageIcon(ControllerPanel.class
                        .getResource(path + "Stop16.gif")));
                startStop.setToolTipText("Stop the simulation");
                startStop.setActionCommand("stop");
                // start the simulation
                controller.updateSimulationMode("continuous");
                controller.start();
            } else {
                int answer = stopMess();
                if (answer == JOptionPane.YES_OPTION) {
                    enableControlButtons(false);
                    startStop.setIcon(new ImageIcon(ControllerPanel.class
                            .getResource(path + "Play16.gif")));
                    startStop.setToolTipText("Start the simulation");
                    startStop.setActionCommand("start");
                    // dismiss the continious batch mode
                    client.setBatchContinuousMode(false);
                    // stop the simulation
                    controller.updateSimulationMode("stop");

                    // reset pause button
                    pauseCont.setIcon(new ImageIcon(ControllerPanel.class
                            .getResource(path + "Pause16.gif")));
                    pauseCont.setToolTipText("Pause the simulation");
                    pauseCont.setActionCommand("pause");
                }
            }
        }
        // pause/continue button
        else if (source.equals(pauseCont)
                && startStop.getActionCommand().equals("stop")) {
            if (((JButton) source).getActionCommand().equals("pause")) {
                pauseCont.setIcon(new ImageIcon(ControllerPanel.class
                        .getResource(path + "Play16.gif")));
                pauseCont.setToolTipText("Continue the simulation");
                pauseCont.setActionCommand("continue");
                // pause the simulation
                controller.updateSimulationMode("pause");
            } else {
                pauseCont.setIcon(new ImageIcon(ControllerPanel.class
                        .getResource(path + "Pause16.gif")));
                pauseCont.setToolTipText("Pause the simulation");
                pauseCont.setActionCommand("pause");
                // continue the simulation
                controller.updateSimulationMode("continuous");
            }
        }
        // step button
        else if (source.equals(step)) {
            // step forward
            controller.updateSimulationMode("onestep");
            // if the simulation hasn't started yet
            if (!startStop.getActionCommand().equals("stop")) {
                startStop.setIcon(new ImageIcon(ControllerPanel.class
                        .getResource(path + "Stop16.gif")));
                startStop.setToolTipText("Stop the simulation");
                startStop.setActionCommand("stop");
                // start the simulation
                controller.start();
            }
            // update the pause/continue button if necessary
            if (!pauseCont.getActionCommand().equals("continue")) {
                pauseCont.setIcon(new ImageIcon(ControllerPanel.class
                        .getResource(path + "Play16.gif")));
                pauseCont.setToolTipText("Continue the simulation");
                pauseCont.setActionCommand("continue");
            }
        }
        // connect/disconnect button
        else if (source.equals(connect)) {
            if (connect.isEnabled()) {
                enableConnectButton(false);
                if (!client.isConnected()) {
                    final Controller tmpController = controller;
                    final Client tmpClient = client;
                    final JButton tmpConnect = connect;
                    final Thread worker = new Thread() {
                        public void run() {
                            tmpController.connectToServer();
                            enableConnectButton(true);
                            // check once again if the connection succeeded
                            if (tmpClient.isConnected()) {
                                tmpConnect.setText("Disconnect");
                                tmpConnect
                                        .setToolTipText("Disconnect from the server");
                            }
                        }
                    };
                    worker.start();
                } else {
                    int answer = disconnectMess();
                    if (answer == JOptionPane.YES_OPTION) {
                        // disconnect from the server
                        if (controller.isSimulationOn()) {
                            // if the thread in the client is running
                            controller.updateSimulationMode("disconnect");
                        } else {
                            // if the thread in the client is not running
                            controller.disconnectFromServer();
                        }
                        // disable control buttons
                        enableControlButtons(false);
                        connect.setText("Connect");
                        connect.setToolTipText("Connect to the server");
                    }
                    enableConnectButton(true);
                }
            }
        }
        // exit button
        else if (source.equals(exit)) {
            int answer = exitMess();
            if (answer == JOptionPane.YES_OPTION) {
                controller.setExitSimulation();
            }
        }
        revalidate();
        repaint();
        updateUI();
    }

    /**
     * Resets the buttons.
     */
    public void reset() {
        startStop.setIcon(new ImageIcon(ControllerPanel.class.getResource(path
                + "Play16.gif")));
        startStop.setToolTipText("Start the simulation");
        startStop.setActionCommand("start");
        // stop the simulation
        controller.setSimulationMode("inactive");

        // reset pause button
        pauseCont.setIcon(new ImageIcon(ControllerPanel.class.getResource(path
                + "Pause16.gif")));
        pauseCont.setToolTipText("Pause the simulation");
        pauseCont.setActionCommand("pause");
        //
        enableControlButtons(false);
        connect.setText("Connect");
        connect.setToolTipText("Connect to the server");
    }

    /**
     * Enables/disables start/stop, pause/continue and step buttons.
     * 
     * @param b enables the buttons if true, disables the buttons otherwise.
     */
    public void enableControlButtons(boolean b) {
        startStop.setEnabled(b);
        pauseCont.setEnabled(b);
        step.setEnabled(b);
        synchronized (this) {
            notifyAll();
        }
    }

    /**
     * Enables/disables the connect button.
     */
    public void enableConnectButton(final boolean b) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                connect.setEnabled(b);
            }
        });
    }

    /**
     * Connects to the server.
     */
    public void doConnect() {
        connect.doClick();
    }

    /**
     * Starts the simulation.
     */
    public void doStart() {
        if (startStop.isEnabled()) {
            startStop.doClick();
        } else {
            synchronized (this) {
                while (!startStop.isEnabled()) {
                    try {
                        wait();
                    } catch (InterruptedException e) {}
                }
                startStop.doClick();
            }
        }
    }

    /**
     * An exit dialog.
     */
    private int exitMess() {
        Object[] options = { "Yes", "No" };
        int answer = StratmasDialog
                .showOptionDialog(null, "Are you sure you want to exit?",
                                  "Question message",
                                  JOptionPane.YES_NO_OPTION,
                                  JOptionPane.QUESTION_MESSAGE, null, options,
                                  options[0]);
        return answer;
    }

    /**
     * A disconnect dialog.
     */
    private int disconnectMess() {
        Object[] options = { "Yes", "No" };
        int answer = StratmasDialog
                .showOptionDialog(null, "Are you sure you want to disconnect?",
                                  "Question message",
                                  JOptionPane.YES_NO_OPTION,
                                  JOptionPane.QUESTION_MESSAGE, null, options,
                                  options[0]);
        return answer;
    }

    /**
     * A stop dialog.
     */
    private int stopMess() {
        Object[] options = { "Yes", "No" };
        String msg = "Are you sure you want to restart the simulation? The simulation will be reset\n"
                + "to the same state as when the connection to the server was established.";
        int answer = StratmasDialog
                .showOptionDialog(null, msg, "Question message",
                                  JOptionPane.YES_NO_OPTION,
                                  JOptionPane.QUESTION_MESSAGE, null, options,
                                  options[0]);
        return answer;
    }

    /**
     * Diposes the frame and updates all the listeners.
     */
    public void close() {
        setVisible(false);
        if (frame != null) {
            frame.dispose();
        }
        for (int i = 0; i < listeners.size(); i++) {
            if (listeners.get(i) != null) {
                (listeners.get(i)).eventOccured(StratmasEvent.getRemoved(this,
                                                                         null));
            }
        }
    }

    /**
     * Adds an event listener for to the eventlistenerlist.
     * 
     * @param listener the listener to add.
     */
    public void addStratmasEventListener(StratmasEventListener listener) {
        listeners.add(listener);
    }
}
