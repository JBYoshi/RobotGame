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

import jbyoshi.robotgame.idetemplates.IdeProjectGenerator;
import jbyoshi.robotgame.util.GameJar;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public final class ScriptStorage implements Comparable<ScriptStorage> {
    private final File root, srcDir;
    private final String mainClassName;

    ScriptStorage(String line) throws IOException {
        this(new File(line.substring(0, line.lastIndexOf(' '))), line.substring(line.lastIndexOf(' ') + 1));
    }

    ScriptStorage(File root, String mainClassName) throws IOException {
        this.root = root;
        srcDir = new File(root, "src");

        this.mainClassName = mainClassName;
        if (!getMainFile().exists()) throw new FileNotFoundException(getMainFile().toString());

        if (GameJar.getGameJar() != null) {
            IdeProjectGenerator.generateIdeFiles(root);
        } else {
            System.out.println("Running outside of JAR, cannot setup IDE projects.");
            System.out.println("To test the creation of IDE projects, please use gradlew runShadow.");
        }
    }

    public String getMainClassName() {
        return mainClassName;
    }

    public File getRootDir() {
        return root;
    }

    public File getMainFile() {
        return new File(srcDir, mainClassName.replace('.', '/') + ".java");
    }

    public List<File> getAuxiliaryFiles() {
        List<File> files = new LinkedList<>();
        listRecursive(srcDir, files);
        files.remove(new File(srcDir, mainClassName.replace('.', File.separatorChar) + ".java"));
        return files;
    }

    @Override
    public String toString() {
        return root + " " + mainClassName;
    }

    private static void listRecursive(File dir, List<File> out) {
        File[] children = dir.listFiles();
        if (children == null) {
            if (dir.exists()) {
                out.add(dir);
            }
        } else {
            for (File f : children) {
                if (f.isDirectory()) {
                    listRecursive(f, out);
                } else {
                    out.add(f);
                }
            }
        }
    }

    @Override
    public int compareTo(ScriptStorage o) {
        return this.mainClassName.compareTo(o.mainClassName);
    }

    public File getSourceDir() {
        return srcDir;
    }
}
