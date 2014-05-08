// $Id: AboutBox.java,v 1.4 2006/03/22 14:30:40 dah Exp $

package ApproxsimClient;

import javax.swing.JOptionPane;

/**
 * Simple class for showing an about box for Approxsim.
 * 
 * @version 1, $Date: 2006/03/22 14:30:40 $
 * @author Per Alexius
 */
public class AboutBox {
    /**
     * The version string. Checking out a certian version will replace this string with the version name.
     */
    static final String version = "$Name: Approxsim_V_7_6 $";

    /**
     * Shows the about box.
     */
    static void show() {
        String theVersion = new String();
        if (version.matches("\\$" + "Name:\\s*\\$")) {
            theVersion = "under development...";
        } else {
            theVersion = version.replaceFirst("\\$" + "Name:\\s*", "")
                    .replaceFirst("\\s*\\$", "");
        }

        String message = "Approxsim Client\n" + "   version: " + theVersion;
        JOptionPane.showMessageDialog(null, message, "About Approxsim Client",
                                      JOptionPane.PLAIN_MESSAGE);
    }
}
