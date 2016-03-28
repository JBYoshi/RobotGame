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
package jbyoshi.robotgame.idetemplates;

import jbyoshi.robotgame.util.GameJar;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class IdeProjectGenerator {
    public static void generateIdePaths(Path dir) throws IOException {
        // Eclipse
        generateIdePath(dir, ".project");
        generateIdePath(dir, ".classpath");
        Path eclipseSettings = dir.resolve(".settings");
        try {
            Files.createDirectory(eclipseSettings);
        } catch (FileAlreadyExistsException e) {
            // Ignore
        }
        generateIdePath(dir, ".settings/org.eclipse.jdt.core.prefs");

        // IntelliJ
        generateIdePath(dir, "$PROJECT_NAME$.ipr");
        generateIdePath(dir, "$PROJECT_NAME$.iml");
        generateIdePath(dir, "$PROJECT_NAME$.iws");
    }

    private static void generateIdePath(Path dir, String name) throws IOException {
        StringBuilder data = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                IdeProjectGenerator.class.getResourceAsStream(name)))) {
            String line;
            while ((line = in.readLine()) != null) {
                line = line.replace("$PROJECT_NAME$", xmlEscape(dir.getFileName().toString()));
                line = line.replace("$GAME$", xmlEscape(GameJar.getGameJar().getAbsolutePath()
                        .replace(File.separatorChar, '/')));
                data.append(line).append("\n");
            }
        }
        try (Writer out = Files.newBufferedWriter(dir.resolve(
                name.replace("$PROJECT_NAME$",dir.getFileName().toString())))) {
            out.write(data.toString());
            out.flush();
        }
    }

    private static String xmlEscape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("'", "&apos;")
                .replace("\"", "&quot;");
    }
}
