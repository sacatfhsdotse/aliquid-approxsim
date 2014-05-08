package ApproxsimClient;

import java.awt.Frame;
import java.awt.Container;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JDialog;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;

/**
 * This class implements the different dialogs used in APPROXSIM.
 * 
 * @author Amir Filipovic
 */
public class ApproxsimDialog {
    /**
     * Instance of the progress bar dialog.
     */
    private static JDialog progressBarDialog;
    /**
     * Indicates if the progress bar dialog is the top component.
     */
    private static boolean progressBarOnTop = true;

    /**
     * This method shows the progress bar dialog.
     * 
     * @param f owner of the dialog.
     * @param info the message shown in the dialog.
     */
    public static void showProgressBarDialog(Frame f, String info) {
        progressBarDialog = createProgressBarDialog(null, info);
        final boolean fprogressBarOnTop = progressBarOnTop;
        final JDialog dialog = progressBarDialog;
        JOptionPane.getFrameForComponent(f);
        final Dimension screen_size = Toolkit.getDefaultToolkit()
                .getScreenSize();
        progressBarDialog
                .setSize(screen_size.width / 5, screen_size.height / 9);
        progressBarDialog.setLocation(screen_size.width / 2,
                                      screen_size.height / 2);
        progressBarDialog.validate();

        progressBarDialog.setAlwaysOnTop(fprogressBarOnTop);

        SwingWorker worker = new SwingWorker() {
            public Object construct() {
                dialog.setVisible(true);
                return null;
            }
        };
        worker.start();
    }

    /**
     * Creates the progress bar dialog.
     * 
     * @param frame owner of the dialog.
     * @param info the message shown in the dialog.
     * @return the progress bar dialog.
     */
    private static JDialog createProgressBarDialog(Frame frame, String info) {
        // create text field
        JTextField infoField = new JTextField(info);
        infoField.setEditable(false);
        infoField.setFont(infoField.getFont().deriveFont(15.0f));
        // create progress bar
        JProgressBar progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setValue(0);
        progressBar.setString("");
        progressBar.setIndeterminate(true);
        // put everything together
        Container contentPane = new Container();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.add(infoField);
        contentPane.add(progressBar);
        // initialize values
        JDialog dialog = new JDialog(frame, "Approxsim Info Dialog", true);
        dialog.setContentPane(contentPane);
        dialog.pack();
        //
        return dialog;
    }

    /**
     * Terminates the progress bar dialog.
     */
    public static void quitProgressBarDialog() {
        if (progressBarDialog != null) {
            progressBarDialog.dispose();
        }
    }

    /**
     * Shows an error message dialog and quits the progress bar dialog if active.
     * 
     * @param parentComponent determines the Frame in which the dialog is displayed.
     * @param message the message to display.
     * @param title the title of the dialog.
     */
    public static void showErrorMessageDialog(Component parentComponent,
            Object message, String title) {
        ApproxsimDialog.quitProgressBarDialog();
        JOptionPane.showMessageDialog(parentComponent,
                                      preprocessMessage(message), title,
                                      JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Shows a warning message dialog.
     * 
     * @param parentComponent determines the Frame in which the dialog is displayed.
     * @param message the message to display.
     * @param title the title of the dialog.
     */
    public static void showWarningMessageDialog(Component parentComponent,
            Object message, String title) {
        ApproxsimDialog.quitProgressBarDialog();
        JOptionPane.showMessageDialog(parentComponent,
                                      preprocessMessage(message), title,
                                      JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Shows an option dialog.
     * 
     * @param parentComponent determines the Frame in which the dialog is displayed.
     * @param message the Object to display.
     * @param title the title string for the dialog.
     * @param optionType an integer designating the options available on the dialog: YES_NO_OPTION, or YES_NO_CANCEL_OPTION.
     * @param messageType an integer designating the kind of message this is : ERROR_MESSAGE, INFORMATION_MESSAGE, WARNING_MESSAGE,
     *            QUESTION_MESSAGE or PLAIN_MESSAGE.
     * @param icon the icon to display in the dialog.
     * @param options an array of objects indicating the possible choices the user can make.
     * @param initialValue the object that represents the default selection for the dialog; only meaningful if options is used.
     * @return an integer indicating the option chosen by the user, or CLOSED_OPTION if the user closed the dialog.
     */
    public static int showOptionDialog(Component parentComponent,
            Object message, String title, int optionType, int messageType,
            Icon icon, Object[] options, Object initialValue) {
        ApproxsimDialog.quitProgressBarDialog();
        // set this dialog as the top component
        progressBarOnTop = false;
        int result = JOptionPane.showOptionDialog(parentComponent,
                                                  preprocessMessage(message),
                                                  title, optionType,
                                                  messageType, icon, options,
                                                  initialValue);
        progressBarOnTop = true;
        return result;
    }

    /**
     * Shows a question-message dialog and quits the progress bar dialog if active.
     * 
     * @param parentComponent the parent Component for the dialog.
     * @param message the message to display.
     * @param initialSelectionValue the value used to initialize the input field.
     */
    public static String showInputDialog(Component parentComponent,
            Object message, Object initialSelectionValue) {
        ApproxsimDialog.quitProgressBarDialog();
        return JOptionPane.showInputDialog(parentComponent, message,
                                           initialSelectionValue);
    }

    /**
     * Preprocesses a message to be shown in a dialog. If the message is a String it is wrapped inside a JTextArea so that it will be
     * possible to copy the text from the dialog.
     * 
     * @param msg The message Obejct to preprocess.
     * @return The processed message object.
     */
    public static Object preprocessMessage(Object msg) {
        Object ret = msg;
        if (msg instanceof String) {
            JTextArea tmsg = new JTextArea((String) msg);
            tmsg.setEditable(false);
            tmsg.setOpaque(false);
            ret = tmsg;
        }
        return ret;
    }
}
