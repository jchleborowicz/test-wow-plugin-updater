package pl.jdata.wow.wow_plugin_updater;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class MyFileUtils {
    private MyFileUtils() {
    }

    public static Path createDirectoryIfDoesNotExist(String directoryName) {
        final Path directoryPath = Paths.get(directoryName).toAbsolutePath();

        if (Files.exists(directoryPath)) {
            if (!Files.isDirectory(directoryPath)) {
                throw new RuntimeException(directoryPath + " already exists and is not directory");
            }
        } else {
            try {
                Files.createDirectory(directoryPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return directoryPath;
    }
}
