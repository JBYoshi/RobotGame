package jbyoshi.robotgame.util.updater;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

public final class Update {
    private final URL url;
    private final File destFile;

    Update(URL downloadUrl, File destFile) {
        this.url = downloadUrl;
        this.destFile = destFile;
    }

    public void install(Component parent) throws Throwable {
        try (InputStream in = new ProgressMonitorInputStream(parent, "Updating, please wait...", url.openStream());
             FileOutputStream out = new FileOutputStream(destFile)) {
            // From this point on, no new code can be loaded!
            byte[] buf = new byte[65536];
            int read;
            while ((read = in.read(buf)) > 0) out.write(buf, 0, read);
            out.flush();
        }
    }
}
