// $Id: Taclan2FileFilter.java,v 1.1 2005/02/12 22:30:40 dah Exp $

/*
 * @(#)Taclan2FileFilter.java
 */

package StratmasClient.TaclanV2;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * Taclan2FileFilter filters out non taclan2 files.
 * 
 * @version 1, $Date: 2005/02/12 22:30:40 $
 * @author Daniel Ahlin
 */

public class Taclan2FileFilter extends FileFilter {

    /**
     * Accept all directories and all *.tl2
     * 
     * @param f the file to investigate.
     */
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = getExtension(f);
        if (extension != null) {
            if (extension.equals("tl2")) {
                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    /**
     * Get the extension of a file.
     * 
     * @param f the file to get extension for.
     */
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    /**
     * The description of this filter
     */
    public String getDescription() {
        return "Taclan2Files";
    }
}
