package pl.jdata.wow.wow_plugin_updater.console;

public @interface Command {

    String name();
    String description() default "";

}
