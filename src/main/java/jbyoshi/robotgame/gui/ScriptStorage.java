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

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;

public final class ScriptStorage implements Comparable<ScriptStorage> {
    private final Path root, srcDir;
    private final String mainClassName;

    ScriptStorage(String line) throws IOException {
        this(Paths.get(line.substring(0, line.lastIndexOf(' '))), line.substring(line.lastIndexOf(' ') + 1));
    }

    ScriptStorage(Path root, String mainClassName) throws IOException {
        this.root = root;
        srcDir = root.resolve("src");

        this.mainClassName = mainClassName;
        if (!Files.isRegularFile(getMainFile())) throw new NoSuchFileException(getMainFile().toString());

        if (GameJar.getGameJar() != null) {
            IdeProjectGenerator.generateIdePaths(root);
        } else {
            System.out.println("Running outside of JAR, cannot setup IDE projects.");
            System.out.println("To test the creation of IDE projects, please use gradlew runShadow.");
        }
    }

    public String getMainClassName() {
        return mainClassName;
    }

    public Path getRootDir() {
        return root;
    }

    public Path getMainFile() {
        return srcDir.resolve(mainClassName.replace('.', '/') + ".java");
    }

    public List<Path> getAuxiliaryFiles() throws IOException {
        List<Path> files = new LinkedList<>();
        Files.walkFileTree(srcDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                files.add(file);
                return FileVisitResult.CONTINUE;
            }
        });
        files.remove(getMainFile());
        return files;
    }

    @Override
    public String toString() {
        return root + " " + mainClassName;
    }

    @Override
    public int compareTo(ScriptStorage o) {
        return this.mainClassName.compareTo(o.mainClassName);
    }

    public Path getSourceDir() {
        return srcDir;
    }
}
