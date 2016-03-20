package jbyoshi.robotgame.util.updater;

import java.io.IOException;
import java.util.Optional;

public abstract class Updater {
    public abstract Optional<Update> checkForUpdates() throws IOException;
    public static Updater getUpdater(String repoApiUrl) {
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
