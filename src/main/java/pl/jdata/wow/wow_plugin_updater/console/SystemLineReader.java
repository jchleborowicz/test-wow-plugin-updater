package pl.jdata.wow.wow_plugin_updater.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.stereotype.Component;
import pl.jdata.wow.wow_plugin_updater.console.LineReader;

@Component
public class SystemLineReader implements LineReader {

    private static final BufferedReader INPUT = new BufferedReader(new InputStreamReader(System.in));

    @Override
    public String readLine() {
        try {
            return INPUT.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
