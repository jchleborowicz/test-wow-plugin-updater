package pl.jdata.wow.wow_plugin_updater;

public final class Constants {

    public static final String PLUGIN_DIR;

    static {
        PLUGIN_DIR = System.getProperty("pluginDir", "c:\\Program Files (x86)\\World of Warcraft\\Interface\\AddOns");
    }

    private Constants() {
    }
}
