package pl.jdata.wow.wow_plugin_updater.commands;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import pl.jdata.wow.wow_plugin_updater.MyFileUtils;

public class HttpExample {

    private static final String TEMP_DIRECTORY_NAME = "temp";

    public static void main(String[] args) {
        // final URL url = new URL("http://example.com/");
        final Path temporaryDirectory = MyFileUtils.createDirectoryIfDoesNotExist("temp");

        downloadPlugin("https://wow.curseforge.com/projects/bagnon/files/latest", temporaryDirectory, true);
    }

    public static Path downloadPlugin(String pluginUrl, Path temporaryDirectory, boolean overwriteExisting) {
        System.out.println("Processing " + pluginUrl);

        try {
            final URL url = new URL(pluginUrl);

            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            System.out.println(" * Status: " + connection.getResponseCode());

            final URL effectiveUrl = connection.getURL();
            System.out.println(" * URL: " + effectiveUrl);

            final String fullUrl = effectiveUrl.getPath();
            final String fileName = getFileName(fullUrl);

            final Path targetPath = temporaryDirectory.resolve(fileName);

            System.out.println(" * saving into: " + targetPath);

            if (Files.exists(targetPath)) {
                if (overwriteExisting) {
                    System.out.println(" * target file already exists, overwriting");
                } else {
                    System.out.println(" * target file already exists, skipping");
                    return targetPath;
                }
            }

            try (final InputStream input = connection.getInputStream()) {
                FileUtils.copyInputStreamToFile(input, targetPath.toFile());
            }

            final long fileSize = Files.size(targetPath);

            System.out.println(" * written " + (fileSize / 1024) + " kB");

            return targetPath;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getFileName(String fullUrl) {
        final Pattern pattern = Pattern.compile("^.*/([^/]+\\.zip)$");

        final Matcher matcher = pattern.matcher(fullUrl);

        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            throw new RuntimeException("Unexpected url: " + fullUrl);
        }
    }
}
