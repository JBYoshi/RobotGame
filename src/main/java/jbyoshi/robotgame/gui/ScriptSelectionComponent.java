/*
 * Copyright (C) 2016 JBYoshi.
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package jbyoshi.robotgame.gui;

import jbyoshi.robotgame.api.Game;
import jbyoshi.robotgame.graphics.RGColors;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;
import java.util.function.Consumer;

public final class ScriptSelectionComponent extends JPanel {
    private static final Path SCRIPT_LIST_FILE = Paths.get(System.getProperty("user.home"), "rg-scripts");

    public ScriptSelectionComponent(Consumer<ScriptStorage> resultListener) {
        super(new BorderLayout());

        Vector<ScriptStorage> scriptList = loadScriptList();

        JList<ScriptStorage> list = new JList<>(scriptList);
        list.setCellRenderer((listSelf, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value.getMainClassName());
            Color defaultColor = label.getForeground();
            if (isSelected) {
                label.setOpaque(true);
                label.setBackground(RGColors.HIGHLIGHT);
                label.setForeground(Color.WHITE);
            } else {
                label.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        label.setOpaque(true);
                        label.setBackground(RGColors.HIGHLIGHT);
                        label.setForeground(Color.WHITE);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        label.setOpaque(false);
                        label.setForeground(defaultColor);
                    }
                });
            }
            return label;
        });
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    resultListener.accept(list.getSelectedValue());
                    list.setSelectedValue(null, false);
                }
            }
        });

        add(new JScrollPane(list));

        JButton createNew = new JButton("Create");
        createNew.addActionListener(event -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            FileFilter filter = new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory();
                }

                @Override
                public String getDescription() {
                    return "Directories";
                }
            };
            chooser.addChoosableFileFilter(filter);
            chooser.setFileFilter(filter);
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setMultiSelectionEnabled(false);

            chooser.setDialogTitle("Choose location");
            if (chooser.showDialog(this, "Create") != JFileChooser.APPROVE_OPTION) return;

            String mainClassName = JOptionPane.showInputDialog(this, "Enter a main class name, including package",
                    "Create Script", JOptionPane.PLAIN_MESSAGE);
            while (true) {
                if (mainClassName == null) return;
                String error = null;
                nameCheck: for (String part : mainClassName.split("\\.")) {
                    switch (part) {
                        case "abstract":
                        case "assert":
                        case "boolean":
                        case "break":
                        case "byte":
                        case "case":
                        case "catch":
                        case "char":
                        case "class":
                        case "const":
                        case "continue":
                        case "default":
                        case "do":
                        case "double":
                        case "else":
                        case "enum":
                        case "extends":
                        case "false":
                        case "final":
                        case "finally":
                        case "float":
                        case "for":
                        case "goto":
                        case "if":
                        case "implements":
                        case "import":
                        case "instanceof":
                        case "int":
                        case "interface":
                        case "long":
                        case "native":
                        case "new":
                        case "null":
                        case "package":
                        case "private":
                        case "protected":
                        case "public":
                        case "return":
                        case "short":
                        case "static":
                        case "strictfp":
                        case "super":
                        case "switch":
                        case "synchronized":
                        case "this":
                        case "throw":
                        case "throws":
                        case "transient":
                        case "true":
                        case "try":
                        case "void":
                        case "volatile":
                        case "while":
                            error = "Cannot use the keyword " + part;
                            break nameCheck;
                        case "":
                            error = "Must not start or end with a period, and cannot contain two dots in a row";
                            break nameCheck;
                        default:
                            if (!Character.isJavaIdentifierStart(part.charAt(0))) {
                                error = "Parts must start with a letter, $, or _ (found " + part.charAt(0) + ")";
                                break nameCheck;
                            }
                            for (int i = 1; i < part.length(); i++) {
                                if (!Character.isJavaIdentifierPart(part.charAt(i))) {
                                    error = "Parts may only contain letters, digits, $, or _ (found " + part.charAt(i)
                                            + ")";
                                    break nameCheck;
                                }
                            }
                    }
                }
                if (error == null) break;
                mainClassName = JOptionPane.showInputDialog(this, new String[] {
                        "Enter a main class name, including package",
                        "Invalid class name: " + error},
                        "Create Script", JOptionPane.PLAIN_MESSAGE);
            }

            Path dir = chooser.getSelectedFile().toPath();
            if (!Files.isDirectory(dir)) {
                try {
                    Files.createDirectories(dir);
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Failed to create project: Failed to create folder",
                            "Create Project", JOptionPane.ERROR_MESSAGE);
                }
            }
            ScriptStorage script = null;
            try {
                script = new ScriptStorage(dir, mainClassName);
                try {
                    Files.createDirectories(script.getMainFile().getParent());
                } catch (FileAlreadyExistsException e) {
                    // Ignore
                }
                try (Writer writer = Files.newBufferedWriter(script.getMainFile())) {
                    String shortName;
                    int split = mainClassName.indexOf('.');
                    if (split >= 0) {
                        writer.write("package " + mainClassName.substring(0, split) + ";\n\n");
                        shortName = mainClassName.substring(split + 1);
                    } else {
                        shortName = mainClassName;
                    }
                    writer.write("import " + Game.class.getPackage().getName() + ".*;\n\n" +
                            "public final class " + shortName + " {\n" +
                            "    /**\n" +
                            "     * This method is called for every run through the game loop.\n" +
                            "     * @param game The current game state.\n" +
                            "     */\n" +
                            "    public static void tick(Game game) {\n" +
                            "        // Your code goes here. Good luck, and have fun!\n" +
                            "    }\n" +
                            "}\n");
                    writer.flush();
                }
                scriptList.add(script);
                saveScriptList(scriptList);

                int index = scriptList.indexOf(script);
                for (ListDataListener l : ((AbstractListModel<ScriptStorage>) list.getModel()).getListDataListeners()) {
                    l.intervalAdded(new ListDataEvent(list.getModel(), ListDataEvent.INTERVAL_ADDED, index, index));
                }
                list.setSelectedValue(script, true);
                revalidate();
            } catch (IOException e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to create project: " + e1, "Create Project",
                        JOptionPane.ERROR_MESSAGE);
                if (script != null) {
                    scriptList.remove(script);
                }
            }
        });

        JButton openInIde = new JButton("Open in IDE");
        openInIde.addActionListener(e -> showIdeDialog(list.getSelectedValue()));
        openInIde.setEnabled(false);

        Box toolbar = Box.createHorizontalBox();
        toolbar.add(createNew);
        toolbar.add(openInIde);
        add(toolbar, BorderLayout.SOUTH);

        add(new JLabel("Double click a script to use it"), BorderLayout.NORTH);

        list.addListSelectionListener(e -> {
            if (list.getSelectedValue() == null) {
                openInIde.setEnabled(false);
            } else {
                openInIde.setEnabled(true);
            }
        });
    }

    private Vector<ScriptStorage> loadScriptList() {
        Vector<ScriptStorage> files = new Vector<>();
        if (Files.isRegularFile(SCRIPT_LIST_FILE)) {
            try {
                Files.readAllLines(SCRIPT_LIST_FILE).stream().flatMap(line -> {
                    try {
                        return Arrays.stream(new ScriptStorage[] {new ScriptStorage(line)});
                    } catch (IOException e) {
                        e.printStackTrace();
                        return Arrays.stream(new ScriptStorage[0]);
                    }
                }).forEach(files::add);
                Collections.sort(files);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Path oldScript = Paths.get("RobotGameScript.java");
        if (Files.isReadable(oldScript)) {
            Path scriptDir = Paths.get("RobotGameScript");
            if (!Files.isDirectory(scriptDir)) {
                Path sourceDir = scriptDir.resolve("src");
                try {
                    Files.createDirectories(sourceDir);
                    Files.move(oldScript, sourceDir.resolve(oldScript.getFileName().toString()));
                    files.add(new ScriptStorage(scriptDir, "RobotGameScript"));
                } catch (IOException e) {
                    System.err.println("Failed to import legacy script file: ");
                    e.printStackTrace();
                }
            } else {
                System.err.println("Failed to import legacy script file: Target project already exists");
            }
        }

        try {
            saveScriptList(files);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }

    private static void saveScriptList(Vector<ScriptStorage> files) throws IOException {
        Collections.sort(files);
        Files.write(SCRIPT_LIST_FILE, files.stream().<CharSequence>map(ScriptStorage::toString)::iterator);
    }

    private void showIdeDialog(ScriptStorage script) {
        JTextField eclipseDirTextField = new JTextField(script.getRootDir().toAbsolutePath().toString());
        eclipseDirTextField.setEditable(false);
        JTextField intellijDirTextField = new JTextField(script.getRootDir().toAbsolutePath().toString());
        intellijDirTextField.setEditable(false);
        JOptionPane.showMessageDialog(this, new Object[] {
                "To open in Eclipse:",
                "- Go to File > Import > General > Existing projects into workspace.",
                "- Copy the following text into the \"Root directory\" text box:",
                eclipseDirTextField,
                "- Press Enter, then click Finish.",
                Box.createVerticalStrut(16),
                "To open in IntelliJ IDEA:",
                "- From the Welcome screen, click Open, or from an existing project, click File > Open.",
                "- Copy the following text into the text box:",
                intellijDirTextField,
                "- Press Enter.",
                Box.createVerticalStrut(16),
                "Whenever you change your code, be sure to click the Reload Script button to upload your changes."
        }, "Open in IDE", JOptionPane.PLAIN_MESSAGE);
    }
}
