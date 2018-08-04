package pl.jdata.wow.wow_plugin_updater;

import java.io.IOException;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class WowPluginUpdater {

    public static void main(String[] args) throws IOException {
        final Terminal terminal = TerminalBuilder.builder()
                .system(true)
                .build();

        final LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();

        while (true) {
            String line = reader.readLine();

        }
    }

}

