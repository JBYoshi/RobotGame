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
