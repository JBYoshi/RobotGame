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
package jbyoshi.robotgame.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;

public final class GameJar {
    public static File getGameJar() {
        URL thisClassFile = GameJar.class.getResource("GameJar.class");
        if (thisClassFile == null || !thisClassFile.getProtocol().equals("jar")) {
            return null;
        }
        String jarFile = thisClassFile.getPath();
        if (!jarFile.startsWith("file:")) {
            return null;
        }
        jarFile = jarFile.substring("file:".length());
        while (jarFile.startsWith("/")) jarFile = jarFile.substring(1);
        try {
            return new File(URLDecoder.decode(jarFile.substring(0, jarFile.indexOf("!/")), "US-ASCII"));
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    public static File getGameLocation() {
        URL thisClassFile = GameJar.class.getResource("GameJar.class");
        if (thisClassFile == null) {
            throw new AssertionError("This class file does not exist?!");
        }
        switch (thisClassFile.getProtocol()) {
            case "file":
                try {
                    File f = new File(thisClassFile.toURI());
                    f = f.getParentFile();

                    String name = GameJar.class.getName();
                    int index = 0;
                    while (true) {
                        index = name.indexOf('.', index);
                        if (index < 0) break;
                        index++;
                        f = f.getParentFile();
                    }

                    return f;
                } catch (URISyntaxException e) {
                    throw new AssertionError(e);
                }
            case "jar":
                String jarFile = thisClassFile.getPath();
                if (!jarFile.startsWith("file:")) {
                    int index = jarFile.indexOf(':');
                    if (index >= 0) {
                        throw new UnsupportedOperationException("Loading JAR from protocol "
                                + jarFile.substring(0, jarFile.indexOf(':')) + " is not supported");
                    }
                    throw new UnsupportedOperationException("Loading JAR from " + jarFile + " is not supported");
                }
                jarFile = jarFile.substring("file:".length());
                while (jarFile.startsWith("/")) jarFile = jarFile.substring(1);
                try {
                    return new File(URLDecoder.decode(jarFile.substring(0, jarFile.indexOf("!/")), "US-ASCII"));
                } catch (UnsupportedEncodingException e) {
                    throw new AssertionError(e);
                }
            default:
                throw new UnsupportedOperationException("Loading file from protocol " + thisClassFile.getProtocol()
                        + " is not supported");
        }
    }
}
