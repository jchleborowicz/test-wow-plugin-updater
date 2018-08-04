package pl.jdata.wow.wow_plugin_updater;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class WowPluginUpdater {

    private final LineReader lineReader;

    public WowPluginUpdater(LineReader lineReader) {
        this.lineReader = lineReader;
    }

    void run() {
        while (true) {
            try {
                final String line = lineReader.readLine();
                processLine(line);
            } catch (UserInterruptException e) {
                //Ignore
            } catch (EndOfFileException e) {
                return;
            }
        }
    }

    private static void processLine(String line) {
        requireNonNull(line);

        line = line.trim();


    }
}

