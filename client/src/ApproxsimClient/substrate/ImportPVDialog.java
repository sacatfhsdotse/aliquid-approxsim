package ApproxsimClient.substrate;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.JTextField;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;

import ApproxsimClient.Client;
import ApproxsimClient.ApproxsimDialog;

/**
 * The dialog used to import process variables for SubstrateEditor.
 */
class ImportPVDialog extends JDialog {
    /**
	 * 
	 */
    private static final long serialVersionUID = 4376449241852035073L;
    /**
     * Reference to the client.
     */
    private Client client;
    /**
     * Reference to SubstrateEditor.
     */
    private SubstrateEditor substrateEditor;
    /**
     * Instance of the actual dialog.
     */
    private static ImportPVDialog dialog;
    /**
     * The text filed which contains the name of the file which contains process variables.
     */
    final JTextField pvFileTextField = new JTextField(10);
    /**
     * The text filed which contains the name of the server.
     */
    final JTextField serverNameTextField = new JTextField(10);

    /**
     * Displays the dialog.
     */
    public static void showDialog(SubstrateEditor substrateEditor) {
        dialog = new ImportPVDialog(substrateEditor);
        dialog.setVisible(true);
    }

    /**
     * Creates the dialog.
     */
    public ImportPVDialog(SubstrateEditor substrateEditor) {
        super(new JFrame(), "Import shapes and process variables");
        this.substrateEditor = substrateEditor;
        this.client = substrateEditor.getClient();

        JPanel importPanel = new JPanel();
        importPanel.setLayout(new BoxLayout(importPanel, BoxLayout.PAGE_AXIS));
        importPanel.add(createProcessVariableImportPanel());
        importPanel.add(createButtonPanel());
        importPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        setContentPane(importPanel);
        setSize(new Dimension(300, 250));
        setLocationRelativeTo(null);
    }

    /**
     * Creates the panel used to select the source for the process variables. The source can be either a file or a server.
     */
    private JPanel createProcessVariableImportPanel() {
        final JButton choiceButton = new JButton("...");
        // select server as source for process variables
        final JRadioButton serverButton = new JRadioButton("Server");
        serverButton.setFont(serverButton.getFont().deriveFont(Font.PLAIN));
        serverButton.addActionListener(new AbstractAction() {
            /**
			 * 
			 */
            private static final long serialVersionUID = -2504123397294942884L;

            public void actionPerformed(ActionEvent e) {
                serverNameTextField.setEnabled(true);
                pvFileTextField.setEnabled(false);
                choiceButton.setEnabled(false);
            }
        });
        serverButton.setSelected(true);
        // name of the server
        serverNameTextField.setText("localhost");
        // select file as source for process variables
        final JRadioButton fileButton = new JRadioButton("File");
        fileButton.setFont(fileButton.getFont().deriveFont(Font.PLAIN));
        fileButton.addActionListener(new AbstractAction() {
            /**
			 * 
			 */
            private static final long serialVersionUID = -618242913512535543L;

            public void actionPerformed(ActionEvent e) {
                serverNameTextField.setEnabled(false);
                pvFileTextField.setEnabled(true);
                choiceButton.setEnabled(true);
            }
        });
        // name of the file
        // pvFileTextField.setText("C:\\Projects\\Approxsim\\development\\ApproxsimClient\\samples\\processVariables.prv");
        // pvFileTextField.setText("/afs/nada.kth.se/home/ass/amfi/PDC_part/APPROXSIM/client/development/ApproxsimClient/samples/processVariables.prv");
        // choose the file
        choiceButton.addActionListener(new AbstractAction() {
            /**
			 * 
			 */
            private static final long serialVersionUID = 8293902011842438051L;

            public void actionPerformed(ActionEvent e) {
                String filename = Client
                        .getFileNameFromDialog(".prv", JFileChooser.OPEN_DIALOG);
                if (filename != null) {
                    pvFileTextField.setText(filename);
                }
            }
        });
        choiceButton.setEnabled(false);
        pvFileTextField.setEnabled(false);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(serverButton);
        buttonGroup.add(fileButton);

        // set the panel
        JPanel serverPanel = new JPanel(new BorderLayout(10, 10));
        serverPanel.add(new JLabel("   "), BorderLayout.WEST);
        serverPanel.add(serverNameTextField, BorderLayout.CENTER);
        JPanel filePanel = new JPanel(new BorderLayout(10, 10));
        filePanel.add(new JLabel("   "), BorderLayout.WEST);
        filePanel.add(pvFileTextField, BorderLayout.CENTER);
        filePanel.add(choiceButton, BorderLayout.EAST);
        JPanel pvPanel = new JPanel(new GridLayout(4, 1, 2, 2));
        pvPanel.add(serverButton);
        pvPanel.add(serverPanel);
        pvPanel.add(fileButton);
        pvPanel.add(filePanel);
        pvPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                                                                     .createTitledBorder("Select source for process variables"),
                                                             BorderFactory
                                                                     .createEmptyBorder(5,
                                                                                        5,
                                                                                        5,
                                                                                        5)));

        return pvPanel;
    }

    /**
     * Creates the panel which contains the buttons.
     */
    private JPanel createButtonPanel() {
        final ImportPVDialog self = this;
        // the canceling button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(cancelButton.getFont().deriveFont(Font.PLAIN));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        self.setVisible(false);
                        self.dispose();
                    }
                });
            }
        });
        // the approving button
        JButton okButton = new JButton("OK");
        okButton.setFont(okButton.getFont().deriveFont(Font.PLAIN));
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                final String pvSourceName = (serverNameTextField.isEnabled()) ? serverNameTextField
                        .getText() : pvFileTextField.getText();
                if (pvSourceName.length() > 0) {
                    ApproxsimDialog
                            .showProgressBarDialog(null,
                                                   "Importing process variables ...");
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            self.setVisible(false);
                            self.dispose();
                            importProcessVariables(pvSourceName);
                        }
                    });
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
     * Imports process variables from either a file of the server.
     * 
     * @param pvSourceName name of the server if the server is the source of the process variables. In other case the name of the file with
     *            the process variables.
     */
    private void importProcessVariables(final String pvSourceName) {
        client.setSubstrateEditorMode(true);
        // get the process variables from the server
        if (serverNameTextField.isEnabled()) {
            Thread worker = new Thread() {
                public void run() {
                    boolean success = client
                            .getProcessVariablesFromServer(pvSourceName);
                    if (success) {
                        substrateEditor.importProcessVariablesFromClient();
                    }
                    ApproxsimDialog.quitProgressBarDialog();
                }
            };
            worker.start();
        }
        // get the process variables from the file
        else {
            Thread worker = new Thread() {
                public void run() {
                    substrateEditor
                            .importProcessVariablesFromFile(pvSourceName);
                    ApproxsimDialog.quitProgressBarDialog();
                }
            };
            worker.start();

        }
    }

}
