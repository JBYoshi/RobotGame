package jbyoshi.robotgame.util.updater;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Optional;
import java.util.Properties;

final class UpdaterImpl extends Updater {
    private final String commit;
    private final GitHubUrl tagUrl, releaseUrl;
    private final File currentJar;
    private static final JsonParser parser = new JsonParser();

    UpdaterImpl(String repoApiUrl) throws NotSupportedException, IOException {
        URL updaterProperties = getClass().getResource("updater.properties");
        if (updaterProperties == null || !updaterProperties.getProtocol().equals("jar")) {
            throw new NotSupportedException();
        }

        String jarFile = updaterProperties.getPath();
        if (!jarFile.startsWith("file:")) {
            throw new NotSupportedException();
        }
        jarFile = jarFile.substring("file:".length());
        while (jarFile.startsWith("/")) jarFile = jarFile.substring(1);
        currentJar = new File(URLDecoder.decode(jarFile.substring(0, jarFile.indexOf("!/")), "US-ASCII"));

        Properties props = new Properties();
        try (InputStream in = updaterProperties.openStream()) {
            props.load(in);
        }
        commit = props.getProperty("commit");
        tagUrl = new GitHubUrl(new URL(repoApiUrl + "/git/refs/tags/" + props.getProperty("tag")));
        releaseUrl = new GitHubUrl(new URL(repoApiUrl + "/releases/tags/" + props.getProperty("tag")));
    }

    @Override
    public Optional<Update> checkForUpdates() throws IOException {
        String newCommit = tagUrl.get().getAsJsonObject().getAsJsonObject("object").get("sha").getAsString();
        if (!newCommit.equals(commit)) {
            return Optional.of(new Update(new URL(releaseUrl.get().getAsJsonObject().getAsJsonArray("assets").get(0)
                    .getAsJsonObject().get("browser_download_url").getAsString()), currentJar));
        }
        return Optional.empty();
    }

    static final class NotSupportedException extends Exception {}

    private static final class GitHubUrl {
        private final URL url;
        private String etag;
        private JsonElement cache;

        GitHubUrl(URL url) {
            this.url = url;
        }

        JsonElement get() throws IOException {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (cache != null) {
                conn.addRequestProperty("If-None-Match", etag);
                if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                    return cache;
                }
            }
            JsonElement result = parser.parse(new InputStreamReader(conn.getInputStream()));
            if (conn.getHeaderField("ETag") != null) {
                etag = conn.getHeaderField("ETag");
                cache = result;
            }
            return result;
        }
    }
}
