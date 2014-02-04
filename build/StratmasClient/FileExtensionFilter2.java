package StratmasClient;

import java.util.Vector;
import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * FileFilter implementation for file extensions.
 */
public class FileExtensionFilter2 extends FileExtensionFilter {
    /**
     * The extensions to pass.
     */
    private Vector extensions = new Vector();
    
    /**
     * Creates a filter that passes files with the specified extensions.
     *
     * @param extensions The extensions to pass.
     */
    public FileExtensionFilter2(String[] extensions) {
        this(extensions, null);
    }
    
    /**
     * Creates a filter that passes files with the specified extensions.
     *
     * @param extensions The extensions to pass.
     * @param description The description of the filter.
     */
    public FileExtensionFilter2(String[] extensions, String description) {
        super(extensions[0], description);
        for (int i = 0; i < extensions.length; i++) {
            this.extensions.add((extensions[i].charAt(0) == '.')? extensions[i].substring(1) : extensions[i]);
        }
    }

    /**
     * Checks if the specified file passes this filter.
     *
     * @param f The file to check.
     * @return True if the file passes, false otherwise.
     */
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        else if (extension == null) {
            return (f.getName().indexOf('.') == -1);
        }
        else {
            for (int i = 0; i < extensions.size(); i++) {
                if (((String)extensions.get(i)).equalsIgnoreCase(getExtension(f))) {
                    return true;
                }
            }
            return false;
        }
    }
}
