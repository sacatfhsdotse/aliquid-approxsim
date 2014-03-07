//         $Id: ScreenShotHandler.java,v 1.2 2006/09/04 15:51:07 amfi Exp $
/*
 * @(#)ScreenShotHandler.java
 */

package StratmasClient.map;

import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import java.io.File;
import java.io.IOException;

import java.awt.image.BufferedImage;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.JOptionPane;

/**
 * Class to handle screen-shots.
 *
 * @author Daniel Ahlin
 * @version 1 ($Date: 2006/09/04 15:51:07 $)
 */
public class ScreenShotHandler extends Thread
{
    /**
     * The screenshot to handle
     */
    BufferedImage image;

    /**
     * Creates a new ScreenShotHandler that will handle the specified image.
     *
     * @param image the image to handle.
     */
    public ScreenShotHandler(BufferedImage image)
    {
        this.image = image;
    }
    
    /**
     * The run methor of this thread, will show a filedialog and save
     * the image.
     */
    public void run()
    {
        JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
        fileChooser.setDialogTitle("Save screen shot...");
        
        // Try to add filters (implicitly also checking if the jre
        // imageio can save this image).
        boolean gotFilters = addFilters(fileChooser);

        if (gotFilters) {
            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION && 
                fileChooser.getSelectedFile() != null) {
                ImageFilter filter = (ImageFilter) fileChooser.getFileFilter();
                File file = filter.addSuffix(fileChooser.getSelectedFile());
                
                try {
                    javax.imageio.ImageIO.write(this.image, filter.getFormat(), file);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Error writing image:\n" + 
                                                  e.getMessage(), 
                                                  "Write error", 
                                                  JOptionPane.ERROR_MESSAGE); 
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "The Java runtime environment\n" + 
                                          System.getProperty("java.vendor") + " " +
                                          System.getProperty("java.version") + "\n" +
                                          "does not seem to support saving the screen-\n" + 
                                          "shot image format",
                                          "Unable to save image", 
                                          JOptionPane.ERROR_MESSAGE); 
        }
    }

    /**
     * Adds filters to the fileChooser. Returns true if any filters
     * are added (false otherwise).
     *
     * @param fileChooser the chooser to which the filters are added.
     */
    boolean addFilters(JFileChooser fileChooser)
    {
        String[] formats = ImageIO.getWriterFormatNames();
        boolean hasAdded = false;
        ImageTypeSpecifier type = 
            new ImageTypeSpecifier(this.image);

        // Remove the old default filter.
        if (fileChooser.getFileFilter() != null) {
            fileChooser.removeChoosableFileFilter(fileChooser.getFileFilter());
        }
        
        for (int i = 0; i < formats.length; i++) {
            if (ImageIO.getImageWriters(type, formats[i]).hasNext()) {
                ImageFilter filter = ImageFilter.getImageFilter(formats[i]);
                if (filter != null) {
                    // Use the first added filter as the default format.
                    if (!hasAdded) {
                        fileChooser.setFileFilter(filter);
                        hasAdded = true;
                    } else {
                        fileChooser.addChoosableFileFilter(filter);
                    }
                }
            }
        }

        return hasAdded;
    }
}

class ImageFilter extends FileFilter
{
    /**
     * The format this filter represents.
     */
    String format;

    /**
     * The suffixes this filter represents.
     */
    String[] suffixes;

    /**
     * Creates a new ImageFilter
     *
     * @param format the name of this format.
     */
    private ImageFilter(String format)
    {
        this.format = format;
        this.suffixes = mapSuffixes(format);
    }

    /**
     * Returns a filter for the provided format
     *
     * @param format the name of the format
     */
    public static ImageFilter getImageFilter(String format) 
    {
        if (mapSuffixes(format) != null) {
            return new ImageFilter(format);
        } else {
            return null;
        }
    }
    
    /**
     * Creates a suffix array based upon known suffixes of the
     * provided format. (BUG: It appears to be a mystery how to get
     * imageio to perform this mapping.)
     *
     * @param format the format to build suffixes for
     */
    static String[] mapSuffixes(String format)
    {
        String[] res = null;
        
        if (format == "JPEG") {
            res = new String[] {"jpeg", "jpg"};
        } else if (format == "PNG") {
            res = new String[] {"png"};
        } else if (format == "BMP") {
            res = new String[] {"bmp"};
        } else if (format == "TARGA") {
            res = new String[] {"tga"};
        }

        return res;
    }

    /**
     * Returns true if the provided file matches any of the suffixes
     * of this filter.
     *
     * @param f the file to check
     */
    public boolean accept(File f)
    {
        if (f == null) {
            return false;
        }

        String suffix = getSuffix(f);
        if (suffix != null) {
            for (int i = 0; i < this.suffixes.length; i++) {
                if (suffix.equals(suffixes[i])) {
                    return true;
                }
            }
            return false;
        }

        return true;
    }

    /**
     * Get the suffix of a file.
     *
     * @param f the file to get suffix for.
     */
    public static String getSuffix(File f)
    {
        String suffix = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        
        if (i > 0 &&  i < s.length() - 1) {
            suffix = s.substring(i+1).toLowerCase();
        }

        return suffix;
    }

    /**
     * Returns the format of this filter
     */
    public String getFormat()
    {
        return this.format;
    }

    /**
     * Returns a instance of the specified file with suffix to the
     * provided file in case it can't find any.
     *
     * @param file the file to fix.
     */
    public File addSuffix(File file)
    {
        File res = file;

        if (getSuffix(file) == null && 
            suffixes != null && 
            suffixes.length > 0) {
            res = new File(file.getParentFile(), file.getName() + 
                           "." + suffixes[0]);
        }

        return res;
    }

    /**
     * Returns a description of the files accepted by this filter.
     */
    public String getDescription()
    {
        return this.format;
    }        
}



