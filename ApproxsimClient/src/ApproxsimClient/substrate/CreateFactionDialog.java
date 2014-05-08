package ApproxsimClient.substrate;

import java.util.Vector;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.CompoundBorder;

import ApproxsimClient.ApproxsimDialog;
import ApproxsimClient.object.ApproxsimObject;
import ApproxsimClient.object.ApproxsimObjectFactory;
import ApproxsimClient.object.ApproxsimEventListener;
import ApproxsimClient.object.ApproxsimEvent;
import ApproxsimClient.object.FactoryListener;
import ApproxsimClient.object.type.TypeFactory;

/**
 * This dialog is used to create factions.
 */
public class CreateFactionDialog extends JDialog implements ActionListener,
        FactoryListener, ApproxsimEventListener {
    /**
	 * 
	 */
    private static final long serialVersionUID = 2649404440836166070L;
    /**
     * Instance of the actual dialog.
     */
    private static CreateFactionDialog dialog;
    /**
     * Reference to FactionHandler.
     */
    private FactionHandler factionHandler;
    /**
     * The list of factions.
     */
    private JList factionList;
    /**
     * The list model of the list of factions.
     */
    private DefaultListModel listModel;
    /**
     * The text field for the name of a faction.
     */
    private JTextField factionNameTextField;
    /**
     * The exit button.
     */
    private JButton closeButton;
    /**
     * The add faction button.
     */
    private JButton addFactionButton;
    /**
     * The remove faction button.
     */
    private JButton removeFactionButton;

    /**
     * Sets up and shows the dialog.
     * 
     * @param frameComp actual frame for the dialog.
     */
    public static void showDialog(Component frameComp,
            FactionHandler factionHandler) {
        Frame frame = JOptionPane.getFrameForComponent(frameComp);
        dialog = new CreateFactionDialog(frame, factionHandler);
        dialog.setVisible(true);
    }

    /**
     * Creates new dialog.
     * 
     * @param frameComp actual frame for the dialog.
     */
    private CreateFactionDialog(Frame frame, FactionHandler factionHandler) {
        super(frame, new String("Ethnic Factions"), true);
        this.factionHandler = factionHandler;
        ApproxsimObjectFactory.addEventListener(this);

        listModel = new DefaultListModel();
        Vector factions = factionHandler.getFactions();
        for (int i = 0; i < factions.size(); i++) {
            listModel.addElement(factions.get(i));
        }

        // create and initialize the close button
        closeButton = new JButton("Close");
        closeButton.setFont(closeButton.getFont().deriveFont(Font.PLAIN));
        closeButton.setMargin(new Insets(1, 5, 1, 5));
        closeButton.addActionListener(this);
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(new JLabel(), BorderLayout.CENTER);
        buttonPanel.add(closeButton, BorderLayout.EAST);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // put everything together
        JPanel factionPanel = new JPanel(new BorderLayout());
        factionPanel.add(createAddFactionsPanel(), BorderLayout.NORTH);
        factionPanel.add(crateRemoveFactionsPanel(), BorderLayout.CENTER);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(factionPanel, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        // initialize values
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Creates a panel used to add new factions.
     */
    private JPanel createAddFactionsPanel() {
        // faction name
        JPanel factionNamePanel = new JPanel();
        factionNamePanel.setLayout(new BorderLayout(5, 0));
        JLabel factionNameLabel = new JLabel("Name : ");
        factionNameLabel.setFont(factionNameLabel.getFont()
                .deriveFont(Font.PLAIN));
        // factionNamePanel.add(factionNameLabel, BorderLayout.WEST);
        factionNameTextField = new JTextField(10);
        factionNamePanel.add(factionNameTextField, BorderLayout.CENTER);
        factionNamePanel.setBorder(BorderFactory
                .createCompoundBorder(BorderFactory
                        .createTitledBorder("Faction Name"), BorderFactory
                        .createEmptyBorder(5, 5, 5, 5)));

        // create and initialize the button
        addFactionButton = new JButton("Add Faction");
        addFactionButton.setFont(addFactionButton.getFont()
                .deriveFont(Font.PLAIN));
        addFactionButton.setMargin(new Insets(1, 5, 1, 5));
        addFactionButton.addActionListener(this);
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(new JLabel(), BorderLayout.CENTER);
        buttonPanel.add(addFactionButton, BorderLayout.SOUTH);

        // the final panel
        JPanel finalPanel = new JPanel(new BorderLayout(5, 5));
        finalPanel.add(factionNamePanel, BorderLayout.CENTER);
        finalPanel.add(buttonPanel, BorderLayout.EAST);
        finalPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createTitledBorder("Add Factions"), BorderFactory
                .createEmptyBorder(5, 5, 5, 5)));
        return finalPanel;
    }

    /**
     * Creates a panel used to remove factions.
     */
    private JPanel crateRemoveFactionsPanel() {
        // create the list of factions
        factionList = new JList(listModel);
        factionList.setFont(factionList.getFont().deriveFont(Font.PLAIN));
        factionList
                .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        factionList.setLayoutOrientation(JList.VERTICAL);

        JScrollPane listScroller = new JScrollPane(factionList);
        listScroller.setPreferredSize(new Dimension(100, 100));
        listScroller.setAlignmentX(Component.LEFT_ALIGNMENT);
        CompoundBorder cBorder = BorderFactory
                .createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5,
                                                                      5),
                                      BorderFactory
                                              .createEtchedBorder(EtchedBorder.LOWERED));
        listScroller.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createTitledBorder("List of Factions"), cBorder));

        // create and initialize the button
        removeFactionButton = new JButton("Remove Faction");
        removeFactionButton.setFont(removeFactionButton.getFont()
                .deriveFont(Font.PLAIN));
        removeFactionButton.setMargin(new Insets(1, 5, 1, 5));
        removeFactionButton.addActionListener(this);
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(new JLabel(), BorderLayout.CENTER);
        buttonPanel.add(removeFactionButton, BorderLayout.SOUTH);

        // the final panel
        JPanel finalPanel = new JPanel(new BorderLayout(5, 5));
        finalPanel.add(listScroller, BorderLayout.CENTER);
        finalPanel.add(buttonPanel, BorderLayout.EAST);
        finalPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createTitledBorder("Remove Factions"), BorderFactory
                .createEmptyBorder(5, 5, 5, 5)));
        return finalPanel;
    }

    /**
     * Part of FactoryListener interface.
     */
    public void approxsimObjectCreated(ApproxsimObject object) {}

    /**
     * Adds newly attached faction to the list of factions. Part of FactoryListener interface.
     */
    public void approxsimObjectAttached(ApproxsimObject object) {
        if (object.getType().canSubstitute("Faction")) {
            object.addEventListener(this);
            listModel.addElement(object);
        }
    }

    /**
     * Responds to the events in the factions contained in the list. Part of ApproxsimEventListener interface.
     */
    public void eventOccured(ApproxsimEvent event) {
        // remove faction from the list
        if (event.isRemoved()) {}
        // update the list of factions
        else if (event.isIdentifierChanged()) {}
    }

    /**
     * Fires an action when a dialog button is pressed.
     * 
     * @param e action event generated by the dialog.
     */
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        // handle "Exit" button
        if (closeButton.equals(obj)) {
            CreateFactionDialog.dialog.setVisible(false);
        }
        // handle "Add Faction" button
        else if (addFactionButton.equals(obj)) {
            String facName = factionNameTextField.getText();
            if (facName.length() > 0) {
                ApproxsimObject so = ApproxsimObjectFactory.create(TypeFactory
                        .getType("EthnicFaction"));
                // set unique id to the faction
                String id = facName;
                int extension = 2;
                while (factionHandler.containsFaction(id)) {
                    id = facName.concat(String.valueOf(extension++));
                }
                so.setIdentifier(id);
                // clear the field
                factionNameTextField.setText("");
            } else {
                ApproxsimDialog
                        .showErrorMessageDialog(null,
                                                "Enter name of the faction!",
                                                "Error message");
            }
        }
        // handle "Remove Faction" button
        else if (removeFactionButton.equals(obj)) {
            for (Object val : factionList.getSelectedValues()) {
                if (!factionHandler.getSelectedFaction().equals(val)) {
                    listModel.removeElement(val);
                    ((ApproxsimObject) val).remove();
                } else {
                    ApproxsimDialog
                            .showErrorMessageDialog(null,
                                                    "Selected faction can't be removed!",
                                                    "Error message");
                }
            }
        }
    }

}
