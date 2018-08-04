package pl.jdata.wow.wow_plugin_updater;

import java.io.PrintStream;

public @interface Command {

    String name();
    String description() default "";

}
