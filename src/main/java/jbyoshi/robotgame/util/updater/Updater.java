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

import java.io.IOException;
import java.util.Optional;

public abstract class Updater {
    public abstract Optional<Update> checkForUpdates() throws IOException;
    public static Updater getUpdater(String repoApiUrl) {
        if (Boolean.getBoolean("updater.skip")) {
            return DUMMY;
        }
        try {
            return new UpdaterImpl(repoApiUrl);
        } catch (UpdaterImpl.NotSupportedException e) {
            return DUMMY;
        } catch (IOException e) {
            e.printStackTrace();
            return DUMMY;
        }
    }

    private static final Updater DUMMY = new Updater() {
        @Override
        public Optional<Update> checkForUpdates() {
            return Optional.empty();
        }
    };
}
