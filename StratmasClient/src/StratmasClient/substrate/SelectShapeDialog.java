package StratmasClient.substrate;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.JTextField;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.AbstractAction;
import StratmasClient.Client;


/**
 * The dialog used to select input shape file for SubstrateEditor. 
 */
public class SelectShapeDialog extends JDialog {
    /**
     * The client.
     */
    private Client client;
    /**
     * Instance of the actual dialog.
     */
    private static SelectShapeDialog dialog;
    /**
     * The text filed which contains the name of the input file.
     */
    final JTextField pvFileTextField = new JTextField(20);
    
    /**
     * Displays the dialog.
     */
    public static void showDialog(Client client) {
        dialog = new SelectShapeDialog(client);
        dialog.setVisible(true);
    }  
    
    /**
     * Creates the dialog.
     */
    public SelectShapeDialog(Client client) {
        super(new JFrame(), "Input File");
        this.client = client;
        
        JPanel importPanel = new JPanel();
        importPanel.setLayout(new BoxLayout(importPanel, BoxLayout.PAGE_AXIS));
        importPanel.add(createImportPanel());
        importPanel.add(createButtonPanel());
        importPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        
        setContentPane(importPanel);
        //setSize(new Dimension(300, 150));
        setLocationRelativeTo(null);
        pack();
    }
    
    /**
     * Creates the panel used to select the input file. 
     */
    private JPanel createImportPanel() {
        final Client fclient = client;
        // choose the file
        final JButton choiceButton = new JButton("...");
        choiceButton.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    String filename = fclient.getFileNameFromOpenDialog(new String[]{"scn", "shp"}, ".scn, .shp");
                    if (filename != null) {
                        pvFileTextField.setText(filename); 
                    }
                }
            });
        
        // set the panel
        JPanel filePanel = new JPanel(new BorderLayout(10, 10));
        filePanel.add(pvFileTextField, BorderLayout.CENTER);
        filePanel.add(choiceButton, BorderLayout.EAST);
        filePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Select input file"),
                                                               BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        
        return filePanel;
    }
    
    /**
     * Creates the panel which contains the buttons.
     */
    private JPanel createButtonPanel() {
        final SelectShapeDialog self = this;
        final Client fclient = client;
        // the canceling button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(cancelButton.getFont().deriveFont(Font.PLAIN));
        cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                self.setVisible(false);
                                self.dispose();
                                System.exit(0);
                            }
                        });
                }
            });
        // the approving button
        JButton okButton = new JButton("OK");
        okButton.setFont(okButton.getFont().deriveFont(Font.PLAIN));
        okButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    final String sourceName = pvFileTextField.getText();
                    if (sourceName.length() > 0) { 
                        SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    self.setVisible(false);
                                    self.dispose();
                                }
                            });
                        fclient.startSubstrateEditor(sourceName);
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
}

