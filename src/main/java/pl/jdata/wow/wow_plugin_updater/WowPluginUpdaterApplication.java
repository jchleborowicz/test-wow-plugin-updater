package pl.jdata.wow.wow_plugin_updater;

import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import pl.jdata.wow.wow_plugin_updater.commands.WowCommands;

@SpringBootApplication
public class WowPluginUpdaterApplication {

    public static void main(String[] args) throws IOException {
        final ConfigurableApplicationContext applicationContext =
                SpringApplication.run(WowPluginUpdaterApplication.class, args);

        try {
            applicationContext.getBean(WowCommands.class).printPlugins();
        } finally {
            try {
                applicationContext.close();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Bean
    PrintWriter output() {
        return new PrintWriter(System.out);
    }

}
