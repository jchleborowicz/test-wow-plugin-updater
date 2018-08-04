package pl.jdata.wow.wow_plugin_updater;

import java.io.IOException;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WowPluginUpdaterApplication {

    public static void main(String[] args) {
//        System.setProperty("org.jline.terminal.dumb", "true");
//        System.setProperty("TERM", "xterm-256color");

        final ConfigurableApplicationContext applicationContext =
                SpringApplication.run(WowPluginUpdaterApplication.class, args);

        try {
            applicationContext.getBean(WowPluginUpdater.class).run();
        } finally {
            try {
                applicationContext.close();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Bean
    Terminal terminal() throws IOException {
        return TerminalBuilder.builder()
                .system(false)
                .build();
    }

    @Bean
    LineReader lineReader(Terminal terminal) {
        return LineReaderBuilder.builder()
                .terminal(terminal)
                .build();
    }
}
