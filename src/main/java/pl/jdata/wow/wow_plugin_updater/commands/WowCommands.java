package pl.jdata.wow.wow_plugin_updater.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Component;
import pl.jdata.wow.wow_plugin_updater.Command;
import pl.jdata.wow.wow_plugin_updater.Constants;

@Component
public class WowCommands {

    public static void main(String[] args) throws IOException {
        new WowCommands().printPlugins();
    }

    @Command(name = "p", description = "Print plugin versions")
    public void printPlugins() throws IOException {
        System.out.println("Printing plugins");

        Files.list(Paths.get(Constants.PLUGIN_DIR))
                .forEach(this::processPluginDirectory);
    }

    private void processPluginDirectory(Path path) {
        if (Files.exists(path) && Files.isDirectory(path)) {
            System.out.println(path);
            System.out.println("    " + path.getFileName());
        }
    }

}
