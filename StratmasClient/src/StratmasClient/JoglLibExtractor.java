//         $Id: JoglLibExtractor.java,v 1.7 2006/09/01 14:28:35 dah Exp $
/*
 * @(#)JoglLibExtractor.java
 */

package StratmasClient;


import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;

import com.jogamp.common.jvm.JNILibLoaderBase;

/**
 * Extracts and optionally loads jogl libraries from jarfile.
 *
 * @version 1, $Date: 2006/09/01 14:28:35 $
 * @author  Daniel Ahlin
*/


public class JoglLibExtractor
{
    /**
     * Extracts the jogl dynamic libraries, puts them in a temporary
     * directory.
     */
    protected synchronized static boolean initializeJoglLibs(String[] parameters)
    {
        if (parameters.length != 0 && 
            parameters[0].equals("-noJoglResolve")) {
            return true;
        } else if (System.getProperty("java.version").matches("1.5.*")) {
            return doReExec(parameters);
        } else {
            return doInitialize();
        }
    }


    /**
     * Tries to extract and load the specified libraries.
     */
    protected synchronized static boolean doInitialize()
    {            
        File tempdir = createTempDir();
        if (tempdir == null) {
            System.err.println("Unable to create temporary directory.");
            return false;
        }

        if (extractToDir(tempdir, true)) {
            String[] libNames = joglOsLibNames();
            String[] libPaths = new String[libNames.length];
            for (int i = 0; i < libNames.length; i++) {
                libPaths[i] = new File(tempdir, libNames[i]).getPath();
            }
            joglLoadLibrary(libPaths);
            for (int i = 0; i < libNames.length; i++) {
                System.err.println("Registering with JogAmp: " + libNames[i].replaceAll("\\..*$","").replaceAll("^lib",""));
                JNILibLoaderBase.addLoaded(libNames[i].replaceAll("\\..*$","").replaceAll("^lib",""));
            }
            return true;
        } else {
            return false;
        }                        
    }

    /**
     * Tries to extract and load the specified libraries.
     */
    protected synchronized static boolean doReExec(String[] parameters)
    {            
        File tempdir = createTempDir();
        if (tempdir == null) {
            System.err.println("Unable to create temporary directory.");
            return false;
        }

        if (extractToDir(tempdir, false)) {
            String[] libNames = joglOsLibNames();
            File library = new File(tempdir, libNames[0]);


            String[] envp = new String[] {joglOsLDName(tempdir)};
            if (envp[0] == null) {
                System.err.println("Unable to find environment settings for" + 
                                   System.getProperty("os.name"));
                return false;
            }

            String jarLocation = JoglLibExtractor.class.getResource("JoglLibExtractor.class").getPath();
            try {
                jarLocation = jarLocation.substring(jarLocation.indexOf(':') + 1, jarLocation.indexOf('!'));
            } catch (IndexOutOfBoundsException e) {
                System.err.println("Unable to construct command line for automatic JOGL library loading under " + 
                                   System.getProperty("os.name"));
                System.exit(1);
            }

            String[] args = new String[4 + parameters.length];
            args[0] = "java";
            args[1] = "-jar";
            args[2] = jarLocation;
            args[3] = "-noJoglResolve";
            
            System.arraycopy(parameters, 0, args, 4, parameters.length);

            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < args.length; i++) {
                buf.append(args[i] + " ");
            }
            System.err.println("Unable to load required libraries. Please try starting the program as follows:\n" +  envp[0] + buf.toString());
            
            
            javax.swing.JOptionPane.showMessageDialog(null, "Unable to load required libraries. Please try starting the program as follows:\n" +  envp[0] + buf.toString(), 
                                          "Library dependence error", 
                                          javax.swing.JOptionPane.ERROR_MESSAGE); 
            System.exit(1);
            return false;
        } else {
            return false;
        }                        
    }



    /**
     * Creates a temporary library
     */
    protected static File createTempDir() 
    {
        String tmpdir = System.getProperty("java.io.tmpdir");
        File tempdir = null;
        
        // Tries making a temporary directory, gives up after a hundred tries.
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 100; i++) {
            tempdir = new File(tmpdir + "/StratmasClientLibs" + random.nextInt(Integer.MAX_VALUE) + ".tmp");
            if (tempdir.mkdirs()) {
                tempdir.deleteOnExit();
                return tempdir;
            }
        }

        return null;
    }
    
    /**
     * Extracts libraries to dir
     *
     * @param dir the (preexisting) directory.
     * @param deleteOnExit if the files extracted should be deleted on exit of jvm.
     */
    protected static boolean extractToDir(File dir, boolean deleteOnExit) 
    {
            String[] libNames = joglOsLibNames();
        String path = joglOsPath();
        
        if (libNames == null || path == null) {
            System.err.println("Unable to find libraries for " + 
                               System.getProperty("os.name"));
            return false;
        }

        for (int i = 0; i < libNames.length; i++) {
            File newFile = new File(dir, libNames[i]);
            if(extractToDir(path + libNames[i], newFile)) {
                if (deleteOnExit) {
                    newFile.deleteOnExit();
                }
            }
            else {
                return false;
            }
        }
        return true;
    }

    /**
     * Extracts resource to file
     *
     * @param resource the file.
     * @param file the (preexisting) directory.
     */
    protected static boolean extractToDir(String resource, File file)
    {
        try {
            if (!file.createNewFile()) {
                System.err.println("Unable to create \"" + file.getPath() + "\"");
                return false;
            }
            System.err.println("Extracting: " + resource + " to " + file.getPath());
            FileOutputStream outputStream = new FileOutputStream(file);
            InputStream inputStream = Client.class.getResourceAsStream(resource);
            if (inputStream == null) {
                System.err.println("Unable to get resource: " + resource);
                return false;
            }
            byte[] buf = new byte[65536];
            int read = -1;
            while((read = inputStream.read(buf, 0, buf.length)) != -1) {
                outputStream.write(buf, 0, read);
            }
            outputStream.close();
            inputStream.close();
            return true;
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    /**
     * Prints string to file
     */
    protected static boolean stringToFile(String string, File file)
    {
        try {
            if (!file.createNewFile()) {
                System.err.println("Unable to create \"" + file.getPath() + "\"");
                return false;
            }
            System.err.println("Creating: " + file.getPath());
            java.io.OutputStreamWriter writer = new
                java.io.OutputStreamWriter(new java.io.FileOutputStream(file));
            writer.write(string);
            writer.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Returns the jogl library names for the running os.
     */    
    protected static String[] joglOsLibNames()
    {
        String os = System.getProperty("os.name");

        if (os.equals("Linux") || os.equals("SunOS")) {
            return new String[] {"libjogl_desktop.so", "libgluegen-rt.so","libnativewindow_awt.so","libnativewindow_x11.so" };
        } else if (os.equals("Mac OS X")) {
            return new String[] {"libjogl_desktop.jnilib", "libgluegen-rt.jnilib", "libnativewindow_awt.jnilib", "libnativewindow_macosx.jnilib" };
        } else if (os.matches("Windows.*")) {
            return new String[] {"jogl_desktop.dll", "gluegen-rt.dll", "nativewindow_awt.dll", "nativewindow_win32.dll" };
        } else {
            return null;
        }
    }

    /**
     * Returns the jogl library names for the running os.
     */    
    protected static String joglOsLDName(File dir)
    {
        String os = System.getProperty("os.name");

        if (os.equals("Linux") || os.equals("SunOS")) {
            String path = System.getProperty("LD_LIBRARY_PATH");
            if (path != null) {
                return "env LD_LIBRARY_PATH=" + dir.getPath() + File.pathSeparator + path + " ";
            } else {
                return "env LD_LIBRARY_PATH=" + dir.getPath() + " ";
            }           
        } else if (os.equals("Mac OS X")) {
            String path = System.getProperty("DYLD_LIBRARY_PATH");
            if (path != null) {
                return "env DYLD_LIBRARY_PATH=" + dir.getPath() + File.pathSeparator + path + " ";
            } else {
                return "env DYLD_LIBRARY_PATH=" + dir.getPath() + " ";
            }           
        } else if (os.matches("Windows.*")) {
            String path = System.getProperty("PATH");
            if (path != null) {
                return "PATH " + dir.getPath() + File.pathSeparator + path + "\n";
            } else {
                return "PATH " + dir.getPath() + "\n";
            }
        } else {
            return null;
        }
    }

    /**
     * Returns the jar path to the jogl libraries for the running os.
     */    
    protected static String joglOsPath()
    {
        String os = System.getProperty("os.name").toLowerCase();
        // Just keep "Windows"
        if (os.matches("windows.*")) {
            os = "windows";
        }else if (os.matches("mac os x")) {
            return "/lib/macosx-universal/";
        }

        String arch = System.getProperty("os.arch");
        if (arch.equals("i386")){
            arch = "i586";
        }

        return "/lib/" + os + "-" + arch + "/";
    }

    /**
     * Loads the jogl libraries.
     *
     * Acknowledgement: Taken directly from:
     * com.sun.opengl.impl.NativeLibLoader:load()
     *
     * @param paths the libraries to load.
     */    
    protected synchronized static void joglLoadLibrary(final String[] paths)
    {
        // Disable loading
        //JNILibLoaderBase.disableLoading();
        System.setProperty("jogamp.gluegen.UseTempJarCache", "false");
        
        java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {
                public Object run() {
                    boolean isOSX = System.getProperty("os.name").equals("Mac OS X");
                    boolean isLinux = System.getProperty("os.name").equals("Linux");
                    boolean isAmd64 = System.getProperty("os.arch").equals("amd64");
                    if (!isOSX) {
                        try {
                            System.loadLibrary("jawt");
                        } catch (UnsatisfiedLinkError e) {
                            // Accessibility technologies load JAWT themselves; safe to continue
                            // as long as JAWT is loaded by any loader
                            if (e.getMessage().indexOf("already loaded") == -1) {
                                // TODO make less ugly:
                                if (isLinux && isAmd64) {
                                    String[] mawts = {
                                        "/usr/lib/jvm/java-6-openjdk/jre/lib/amd64/xawt/libmawt.so",
                                        "/usr/lib/jvm/java-6-openjdk-amd64/jre/lib/amd64/xawt/libmawt.so",
                                        "/usr/lib/jvm/java-7-openjdk/jre/lib/amd64/xawt/libmawt.so",
                                        "/usr/lib/jvm/java-7-openjdk-amd64/jre/lib/amd64/xawt/libmawt.so"
                                    };
                                    for (String s : mawts) {
                                        try {
                                            System.load(s);
                                        } catch (UnsatisfiedLinkError e3) {
                                            // do nothing.
                                        }
                                    }
                                    try {
                                        System.loadLibrary("jawt");
                                    } catch (UnsatisfiedLinkError e2) {
                                        throw e2;
                                    }
                                } else {
                                    throw e;
                                }
                            }
                        }
                    }

                    for (int i = 0; i < paths.length; i++) {
                        if (paths[i] != null) {
                            System.err.println("Loading: " + paths[i]);
                            System.load(paths[i]);
                        }                
                    }

                    return null;
                }
            });
    }
}
