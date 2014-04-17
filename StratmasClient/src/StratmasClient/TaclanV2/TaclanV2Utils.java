// $Id: TaclanV2Utils.java,v 1.1 2006/10/09 11:55:16 dah Exp $
/*
 * @(#)TaclanV2Utils.java
 */

package StratmasClient.TaclanV2;

import javax.swing.JFileChooser;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import StratmasClient.object.StratmasList;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.type.Declaration;
import StratmasClient.object.type.TypeFactory;

import StratmasClient.StratmasDialog;

/**
 * Util functions for handling TaclanV2 files.
 * 
 * @version 1, $Date: 2006/10/09 11:55:16 $
 * @author Daniel Ahlin
 */
public class TaclanV2Utils {
    /**
     * Returns the top object in the the xml file pointed out by filename. Expects to get a simulation as top object.
     * 
     * @param filename the name of the file to import
     */
    public static StratmasObject importTaclanV2Simulation(String filename) {
        try {
            // Try to parse the file. Expect to get a StratmasList
            // containing exactly one "Simulation"-type object.

            StratmasClient.TaclanV2.Parser parser = null;
            try {
                parser = StratmasClient.TaclanV2.Parser.getParser(filename,
                                                                  "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // Ok, try default encoding instead.
                parser = StratmasClient.TaclanV2.Parser.getParser(filename);
            }
            parser.doParse();
            parser.typeCheck(TypeFactory.getType("Root")
                                     .getSubElement("simulation"), TypeFactory
                                     .getTypeInformation());
            StratmasObject top = parser.getStratmasList(TypeFactory
                    .getType("Root").getSubElement("simulation"));

            if (!top.isLeaf()) {
                return (StratmasObject) top.children().nextElement();
            } else {
                StratmasDialog.showErrorMessageDialog(null, filename
                        + " is empty.", "Empty file");
                return null;
            }

        } catch (SyntaxException e) {
            StratmasDialog.showErrorMessageDialog(null,
                                                  "Syntax error(s) found:\n"
                                                          + e.getMessage(),
                                                  "Syntax error(s) found");
        } catch (SemanticException e) {
            StratmasDialog.showErrorMessageDialog(null,
                                                  "Semantic error(s) found\n"
                                                          + e.getMessage(),
                                                  "Semantic error(s) found");
        } catch (IOException e) {
            StratmasDialog.showErrorMessageDialog(null,
                                                  "File error\n"
                                                          + e.getMessage(),
                                                  "File error");
        }

        return null;

    }

    /**
     * Imports a TaclanV2 file specified by the user using a JFileChooser
     */
    public static StratmasObject importTaclanV2Simulation() {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        StratmasClient.TaclanV2.Taclan2FileFilter filter = new StratmasClient.TaclanV2.Taclan2FileFilter();
        chooser.setFileFilter(filter);
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            return null;
        } else {
            return importTaclanV2Simulation(chooser.getSelectedFile().getPath());
        }
    }

    /**
     * Returns a TaclanV2 file name specified by the user using a JFileChooser
     */
    public static String getTaclanV2File() {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        StratmasClient.TaclanV2.Taclan2FileFilter filter = new StratmasClient.TaclanV2.Taclan2FileFilter();
        chooser.setFileFilter(filter);
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            return null;
        } else {
            return chooser.getSelectedFile().getPath();
        }
    }

    /**
     * Returns a TaclanV2 file name specified by the user using a JFileChooser
     */
    public static String getScenarioFile() {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        StratmasClient.TaclanV2.Taclan2FileFilter filter = new StratmasClient.TaclanV2.Taclan2FileFilter();
        chooser.setFileFilter(filter);
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            return null;
        } else {
            return chooser.getSelectedFile().getPath();
        }
    }

    /**
     * Imports an ESRIFile
     */
    public static StratmasList importESRIFile() {

        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        StratmasClient.TaclanV2.Taclan2FileFilter filter = new StratmasClient.TaclanV2.Taclan2FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                String extension = getExtension(f);
                if (extension != null) {
                    if (extension.equalsIgnoreCase("shp")) {
                        return true;
                    }
                }
                return false;
            }

            public String getDescription() {
                return "ESRI Shape files";
            }
        };
        chooser.setFileFilter(filter);
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            String taclanCode = "import \""
                    + chooser.getSelectedFile().getPath() + "\"";
            try {
                // Try to parse the String. Expect to get a StratmasList
                // containing the shapes in the import.

                StratmasClient.TaclanV2.Parser parser = null;
                parser = StratmasClient.TaclanV2.Parser.getParser(chooser
                        .getSelectedFile().getPath(), new StringReader(
                        taclanCode));

                parser.doParse();
                StratmasList top = parser.getStratmasList(TypeFactory
                        .getType("Composite").getSubElement("shapes"));

                if (!top.isLeaf()) {
                    return top;
                } else {
                    StratmasDialog
                            .showErrorMessageDialog(null,
                                                    "Empty ESRI file.",
                                                    "No supported shapes found in "
                                                            + chooser
                                                                    .getSelectedFile()
                                                                    .getPath());
                    return null;
                }
            } catch (SemanticException e) {
                StratmasDialog
                        .showErrorMessageDialog(null,
                                                "Error imoporting ESRI file:\n"
                                                        + chooser
                                                                .getSelectedFile()
                                                                .getPath(),
                                                "Error imoporting ESRI file");
                return null;
            } catch (SyntaxException e) {
                StratmasDialog
                        .showErrorMessageDialog(null,
                                                "Error imoporting ESRI file:\n"
                                                        + chooser
                                                                .getSelectedFile()
                                                                .getPath(),
                                                "Error imoporting ESRI file");
                return null;
            }

        }

        return null;
    }

    /**
     * Imports an ESRIFile
     */
    public static StratmasList importESRIFile(String filename) {
        String taclanCode = "import \"" + filename + "\"";
        try {
            // Try to parse the String. Expect to get a StratmasList
            // containing the shapes in the import.

            StratmasClient.TaclanV2.Parser parser = null;
            parser = StratmasClient.TaclanV2.Parser
                    .getParser(filename, new StringReader(taclanCode));

            parser.doParse();
            StratmasList top = parser.getStratmasList(TypeFactory
                    .getType("Composite").getSubElement("shapes"));

            if (!top.isLeaf()) {
                return top;
            } else {
                StratmasDialog.showErrorMessageDialog(null, "Empty ESRI file.",
                                                      "No supported shapes found in "
                                                              + filename);
                return null;
            }
        } catch (SemanticException e) {
            StratmasDialog.showErrorMessageDialog(null,
                                                  "Error imoporting ESRI file:\n"
                                                          + filename,
                                                  "Error imoporting ESRI file");
            return null;
        } catch (SyntaxException e) {
            StratmasDialog.showErrorMessageDialog(null,
                                                  "Error imoporting ESRI file:\n"
                                                          + filename,
                                                  "Error imoporting ESRI file");
            return null;
        }
    }

    /**
     * Imports a Taclanfile
     */
    public static StratmasList importTaclanV2File() {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        StratmasClient.TaclanV2.Taclan2FileFilter filter = new StratmasClient.TaclanV2.Taclan2FileFilter();
        chooser.setFileFilter(filter);
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            return null;
        } else {
            try {
                String filename = chooser.getSelectedFile().getPath();
                // Try to parse the file. Expect to get a StratmasList
                // containing exactly one "Simulation"-type object.

                StratmasClient.TaclanV2.Parser parser = null;
                try {
                    parser = StratmasClient.TaclanV2.Parser.getParser(filename,
                                                                      "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    // Ok, try default encoding instead.
                    parser = StratmasClient.TaclanV2.Parser.getParser(filename);
                }
                parser.doParse();
                StratmasList top = parser.getStratmasList(new Declaration(
                        TypeFactory.getType("Identifiable"), filename, 0, 0,
                        true));

                if (!top.isLeaf()) {
                    return top;
                } else {
                    StratmasDialog.showErrorMessageDialog(null, filename
                            + " is empty.", "Empty file");
                    return null;
                }

            } catch (SyntaxException e) {
                StratmasDialog.showErrorMessageDialog(null,
                                                      "Syntax error(s) found:\n"
                                                              + e.getMessage(),
                                                      "Syntax error(s) found");
                return null;
            } catch (SemanticException e) {
                StratmasDialog
                        .showErrorMessageDialog(null,
                                                "Semantic error(s) found\n"
                                                        + e.getMessage(),
                                                "Semantic error(s) found");
                return null;
            } catch (IOException e) {
                StratmasDialog.showErrorMessageDialog(null,
                                                      "File error\n"
                                                              + e.getMessage(),
                                                      "File error");
                return null;
            }
        }
    }

    /**
     * Imports a Taclanfile
     */
    public static StratmasList importTaclanV2File(String filename) {
        try {
            // Try to parse the file. Expect to get a StratmasList
            // containing exactly one "Simulation"-type object.

            StratmasClient.TaclanV2.Parser parser = null;
            try {
                parser = StratmasClient.TaclanV2.Parser.getParser(filename,
                                                                  "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // Ok, try default encoding instead.
                parser = StratmasClient.TaclanV2.Parser.getParser(filename);
            }
            parser.doParse();
            StratmasList top = parser.getStratmasList(new Declaration(
                    TypeFactory.getType("Identifiable"), filename, 0, 0, true));

            if (!top.isLeaf()) {
                return top;
            } else {
                StratmasDialog.showErrorMessageDialog(null, filename
                        + " is empty.", "Empty file");
                return null;
            }

        } catch (SyntaxException e) {
            StratmasDialog.showErrorMessageDialog(null,
                                                  "Syntax error(s) found:\n"
                                                          + e.getMessage(),
                                                  "Syntax error(s) found");
            return null;
        } catch (SemanticException e) {
            StratmasDialog.showErrorMessageDialog(null,
                                                  "Semantic error(s) found\n"
                                                          + e.getMessage(),
                                                  "Semantic error(s) found");
            return null;
        } catch (IOException e) {
            StratmasDialog.showErrorMessageDialog(null,
                                                  "File error\n"
                                                          + e.getMessage(),
                                                  "File error");
            return null;
        }
    }

    /**
     * Creates a template (very empty) taclanv2 simulation
     */
    public static StratmasObject createTemplateSimulation() {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        StratmasClient.TaclanV2.Taclan2FileFilter filter = new StratmasClient.TaclanV2.Taclan2FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                String extension = getExtension(f);
                if (extension != null) {
                    if (extension.equals("shp")) {
                        return true;
                    }
                }
                return false;
            }

            public String getDescription() {
                return "ESRI Shape files";
            }
        };
        chooser.setFileFilter(filter);
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            String taclanCode = "CommonSimulation 'simulation' { \n"
                    + "timeStepper = ConstantStepper { dt = 86400000 } \n"
                    + "gridPartitioner = SquarePartitioner { cellSizeMeters = 10000.0 } \n"
                    + "CommonScenario 'scenario' { \n"
                    + "map = Composite { shape = { import \""
                    + chooser.getSelectedFile().getPath()
                    + "\" } } \n"
                    + "disease = Disease { description = \"None\" infectionRate = 0.0 recoveryRate = 0.0 "
                    + "mortalityRate = 0.0 } \n "
                    + "HDI = 0.0 unemployment = 0.0 }" + "startTime = 0 }";

            System.out.println("OK3");
            try {
                // Try to parse the String. Expect to get a StratmasList
                // containing a simulation import.

                StratmasClient.TaclanV2.Parser parser = null;

                parser = StratmasClient.TaclanV2.Parser
                        .getParser("template", new StringReader(taclanCode));
                parser.doParse();
                StratmasObject top = parser.getStratmasList(TypeFactory
                        .getType("Root").getSubElement("simulation"));

                return (StratmasObject) top.children().nextElement();
            } catch (SemanticException e) {
                StratmasDialog
                        .showErrorMessageDialog(null,
                                                "Error imoporting ESRI file:\n"
                                                        + chooser
                                                                .getSelectedFile()
                                                                .getPath(),
                                                "Error imoporting ESRI file");
                return null;
            } catch (SyntaxException e) {
                StratmasDialog
                        .showErrorMessageDialog(null,
                                                "Error imoporting ESRI file:\n"
                                                        + chooser
                                                                .getSelectedFile()
                                                                .getPath(),
                                                "Error imoporting ESRI file");
                return null;
            }
        }

        return null;
    }

    /**
     * Returns a template (very empty) taclanv2 simulation
     */
    public static StratmasObject oldGetTemplateSimulation(String filePath) {
        String taclanCode = "CommonSimulation 'simulation' { \n"
                + "timeStepper = ConstantStepper { dt = 86400000 } \n"
                + "gridPartitioner = SquarePartitioner { cellSizeMeters = 10000.0 } \n"
                + "CommonScenario 'scenario' { \n"
                + "map = Composite { shape = { import \""
                + filePath
                + "\" } } \n"
                + "disease = Disease { description = \"None\" infectionRate = 0.0 recoveryRate = 0.0 "
                + "mortalityRate = 0.0 } \n "
                + "HDI = 0.0 unemployment = 0.0 }" + "startTime = 0 }";

        try {
            // Try to parse the String. Expect to get a StratmasList
            // containing a simulation import.

            StratmasClient.TaclanV2.Parser parser = null;

            parser = StratmasClient.TaclanV2.Parser
                    .getParser("template", new StringReader(taclanCode));
            parser.doParse();
            StratmasObject top = parser.getStratmasList(TypeFactory
                    .getType("Root").getSubElement("simulation"));

            return (StratmasObject) top.children().nextElement();
        } catch (SemanticException e) {
            StratmasDialog.showErrorMessageDialog(null,
                                                  "Error imoporting ESRI file:\n"
                                                          + filePath,
                                                  "Error imoporting ESRI file");
            return null;
        } catch (SyntaxException e) {
            StratmasDialog.showErrorMessageDialog(null,
                                                  "Error imoporting ESRI file:\n"
                                                          + filePath,
                                                  "Error imoporting ESRI file");
            return null;
        }
    }

    /**
     * Import taclan code from a string
     * 
     * @param taclanCode the code to import.
     */
    public static StratmasObject importTaclanV2String(String taclanCode) {
        try {
            // Try to parse the String.
            StratmasClient.TaclanV2.Parser parser = StratmasClient.TaclanV2.Parser
                    .getParser("template", new StringReader(taclanCode));
            parser.doParse();
            StratmasObject top = parser.getStratmasList(new Declaration(
                    TypeFactory.getType("Identifiable"), "", 0, 0, true));
            if (!top.isLeaf()) {
                return top;
            } else {
                return null;
            }
        } catch (SemanticException e) {
            return null;
        } catch (SyntaxException e) {
            return null;
        }
    }

    /**
     * Saves the object to a taclanV2 file pointed out by filename.
     * 
     * @param object the object to save.
     * @param filename the name of the file to save to
     */
    public static void exportToTaclanV2(StratmasObject object, String filename) {
        try {
            OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(filename));
            writer.write(object.toTaclanV2());
            writer.close();
        } catch (IOException e) {
            StratmasDialog.showErrorMessageDialog(null,
                                                  "File error\n"
                                                          + e.getMessage(),
                                                  "File error");
        }
    }

    /**
     * Exports the provided object to a TaclanV2 file specified by the user using a JFileChooser
     * 
     * @param object the object to save.
     */
    public static void exportToTaclanV2(StratmasObject object) {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        chooser.addChoosableFileFilter(new StratmasClient.TaclanV2.Taclan2FileFilter());
        if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {} else {
            exportToTaclanV2(object, chooser.getSelectedFile().getPath());
        }
    }

    /**
     * Returns an ESRI file name specified by the user using a JFileChooser
     */
    public static String getESRIFile() {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        StratmasClient.TaclanV2.Taclan2FileFilter filter = new StratmasClient.TaclanV2.Taclan2FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                String extension = getExtension(f);
                if (extension != null) {
                    if (extension.equalsIgnoreCase("shp")) {
                        return true;
                    }
                }
                return false;
            }

            public String getDescription() {
                return "ESRI Shape files";
            }
        };
        chooser.setFileFilter(filter);
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getPath();
        } else {
            return null;
        }
    }

}
