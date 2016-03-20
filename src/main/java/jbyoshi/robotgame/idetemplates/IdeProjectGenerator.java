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

public final class IdeProjectGenerator {
    public static void generateIdeFiles(File dir) throws IOException {
        // Eclipse
        generateIdeFile(dir, ".project");
        generateIdeFile(dir, ".classpath");
        File eclipseSettings = new File(dir, ".settings");
        if (!eclipseSettings.exists() && !eclipseSettings.mkdir()) throw new IOException("Could not create .settings/");
        generateIdeFile(dir, ".settings/org.eclipse.jdt.core.prefs");

        // IntelliJ
        generateIdeFile(dir, "$PROJECT_NAME$.ipr");
        generateIdeFile(dir, "$PROJECT_NAME$.iml");
        generateIdeFile(dir, "$PROJECT_NAME$.iws");
    }

    private static void generateIdeFile(File dir, String name) throws IOException {
        StringBuilder data = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                IdeProjectGenerator.class.getResourceAsStream(name)))) {
            String line;
            while ((line = in.readLine()) != null) {
                line = line.replace("$PROJECT_NAME$", xmlEscape(dir.getName()));
                line = line.replace("$GAME$", xmlEscape(GameJar.getGameJar().getAbsolutePath()
                        .replace(File.separatorChar, '/')));
                data.append(line).append("\n");
            }
        }
        try (FileWriter out = new FileWriter(new File(dir, name.replace("$PROJECT_NAME$", dir.getName())))) {
            out.write(data.toString());
            out.flush();
        }
    }

    private static String xmlEscape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("'", "&apos;")
                .replace("\"", "&quot;");
    }
}
