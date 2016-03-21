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
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

public final class GameJar {
    public static File getGameJar() {
        URL updaterProperties = GameJar.class.getResource("GameJar.class");
        if (updaterProperties == null || !updaterProperties.getProtocol().equals("jar")) {
            return null;
        }
        String jarFile = updaterProperties.getPath();
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
}
