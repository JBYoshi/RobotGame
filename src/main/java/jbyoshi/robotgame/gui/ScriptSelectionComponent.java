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
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import java.util.function.Consumer;

public final class ScriptSelectionComponent extends JPanel {
    public ScriptSelectionComponent(Consumer<ScriptStorage> resultListener) {
        super(new BorderLayout());

        final Path scriptListFile = Paths.get(System.getProperty("user.home"), "rg-scripts");

        Vector<ScriptStorage> files = new Vector<>();
        if (Files.isRegularFile(scriptListFile)) {
            try {
                Files.readAllLines(scriptListFile).stream().map(ScriptStorage::new).forEach(files::add);
                Collections.sort(files, Comparator.comparing(s -> s.mainClassName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (new File("RobotGameScript.java").exists()) {
            File dir = new File("RobotGameScript");
            if ((dir.isDirectory() || dir.mkdirs()) && new File("RobotGameScript.java")
                    .renameTo(new File(dir, "RobotGameScript.java"))) {
                files.add(new ScriptStorage(dir.getAbsolutePath() + " RobotGameScript"));
                Collections.sort(files, Comparator.comparing(s -> s.mainClassName));

                try {
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
            JLabel label = new JLabel(value.mainClassName);
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
        list.addListSelectionListener(e -> {
            resultListener.accept(list.getSelectedValue());
            list.setSelectedValue(null, false);
        });

        add(new JScrollPane(list));

        // TODO button to create a new script
    }
}
