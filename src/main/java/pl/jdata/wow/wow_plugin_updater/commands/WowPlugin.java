package pl.jdata.wow.wow_plugin_updater.commands;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class WowPlugin {
    private String name;
    private String author;
    private String anInterface;
    private String notes;
    private String revision;
    private String savedVariables;
    private String title;
    private String version;
    private String dependencies;
    private String optionalDependencies;
    private String savedVariablesPerCharacter;
    private String loadOnDemand;
    private String disabled;
    private Map<String, String> extendedProperties = new HashMap<>();
}
