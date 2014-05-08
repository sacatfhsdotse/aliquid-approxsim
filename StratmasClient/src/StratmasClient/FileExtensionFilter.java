package StratmasClient;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * FileFilter implementation for file extensions.
 * 
 * @version 1.0 $Date: 2007/01/29 12:01:18 $
 * @author Per Alexius
 */
public class FileExtensionFilter extends FileFilter {
    /**
     * The extension to pass.
     */
    protected String extension;

    /**
     * The description string..
     */
    protected String description;

    /**
     * Creates a filter that passes files with the specified extension.
     * 
     * @param extension The extension to pass.
     */
    public FileExtensionFilter(String extension) {
        this(extension, null);
    }

    /**
     * Creates a filter that passes files with the specified extension.
     * 
     * @param extension The extension to pass.
     * @param description The description of the filter.
     */
    public FileExtensionFilter(String extension, String description) {
        if (extension == null) {
            throw new AssertionError(
                    "Not allowed to create FileExtensionFilter with 'null' as extension.");
        } else if (extension.charAt(0) == '.') {
            extension = extension.substring(1);
        }
        this.extension = extension;
        this.description = description;
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
        } else if (extension == null) {
            return (f.getName().indexOf('.') == -1);
        } else {
            return extension.equalsIgnoreCase(getExtension(f));
        }
    }

    /**
     * Accessor for the description.
     * 
     * @return The description of the filter.
     */
    public String getDescription() {
        if (description == null) {
            description = (extension == null ? "Files without extension" : "."
                    + extension + " files");
        }
        return description;
    }

    /**
     * Returns the extension of the files.
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Get the extension of a file.
     * 
     * @param f the file to get extension for.
     * @return The extension of the provided file.
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
}
