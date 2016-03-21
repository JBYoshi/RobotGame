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
