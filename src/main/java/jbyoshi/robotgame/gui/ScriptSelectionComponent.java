package jbyoshi.robotgame.gui;

import jbyoshi.robotgame.graphics.RGColors;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;
import java.util.function.Consumer;

public final class ScriptSelectionComponent extends JPanel {
    public ScriptSelectionComponent(Consumer<ScriptStorage> resultListener) {
        super(new BorderLayout());

        final Path scriptListFile = Paths.get(System.getProperty("user.home"), "rg-scripts");

        Vector<ScriptStorage> files = new Vector<>();
        if (Files.isRegularFile(scriptListFile)) {
            try {
                Files.readAllLines(scriptListFile).stream().flatMap(line -> {
                    try {
                        return Arrays.stream(new ScriptStorage[] {new ScriptStorage(line)});
                    } catch (IOException e) {
                        return Arrays.stream(new ScriptStorage[0]);
                    }
                }).forEach(files::add);
                Collections.sort(files);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (new File("RobotGameScript.java").exists()) {
            File dir = new File("RobotGameScript");
            if ((dir.isDirectory() || dir.mkdirs()) && new File("RobotGameScript.java")
                    .renameTo(new File(dir, "src/RobotGameScript.java"))) {
                try {
                    files.add(new ScriptStorage(dir.getAbsolutePath() + " RobotGameScript"));
                    Collections.sort(files);
                    Files.write(scriptListFile, files.stream().<CharSequence>map(ScriptStorage::toString)::iterator);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.err.println("Failed to import legacy script file");
            }
        }

        JList<ScriptStorage> list = new JList<>(files);
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
        createNew.addActionListener(e -> {
            // TODO
        });

        JButton openInIde = new JButton("Open in IDE");
        openInIde.addActionListener(e -> showIdeDialog(list.getSelectedValue()));
        openInIde.setEnabled(false);

        Box toolbar = Box.createHorizontalBox();
        toolbar.add(createNew);
        toolbar.add(openInIde);
        add(toolbar, BorderLayout.SOUTH);

        list.addListSelectionListener(e -> {
            if (list.getSelectedValue() == null) {
                openInIde.setEnabled(false);
            } else {
                openInIde.setEnabled(true);
            }
        });
    }

    private void showIdeDialog(ScriptStorage script) {
        JTextField eclipseDirTextField = new JTextField(script.getRootDir().getAbsolutePath());
        eclipseDirTextField.setEditable(false);
        JTextField intellijDirTextField = new JTextField(script.getRootDir().getAbsolutePath());
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
