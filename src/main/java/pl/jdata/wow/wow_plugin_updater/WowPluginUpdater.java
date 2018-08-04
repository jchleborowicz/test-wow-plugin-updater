package pl.jdata.wow.wow_plugin_updater;

import java.io.PrintWriter;
import java.util.stream.Stream;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;
import pl.jdata.wow.wow_plugin_updater.console.EndOfFileException;
import pl.jdata.wow.wow_plugin_updater.console.LineReader;

import static java.util.Objects.requireNonNull;

@Component
public class WowPluginUpdater {

    private final LineReader lineReader;
    private final PrintWriter output;

    public WowPluginUpdater(LineReader lineReader, PrintWriter output) {
        this.lineReader = lineReader;
        this.output = output;
    }

    void run() {
        while (true) {
            try {
                output.print("> ");
                final String line = lineReader.readLine();
                processLine(line);
            } catch (EndOfFileException e) {
                return;
            }
        }
    }

    private void processLine(String line) {
        requireNonNull(line);

        line = line.trim();

        output.println(line.toUpperCase());

    }
}

