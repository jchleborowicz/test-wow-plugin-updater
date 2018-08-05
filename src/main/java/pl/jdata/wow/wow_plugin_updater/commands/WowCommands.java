package pl.jdata.wow.wow_plugin_updater.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import pl.jdata.wow.wow_plugin_updater.Command;
import pl.jdata.wow.wow_plugin_updater.Constants;

import static java.util.stream.Collectors.toList;

@Component
public class WowCommands {

    public static void main(String[] args) {
        try {
            new WowCommands().printPlugins();
        } catch (Throwable e) {
            e.printStackTrace(System.out);
        }
    }

    @Command(name = "p", description = "Print plugin versions")
    public void printPlugins() throws IOException {
        System.out.println("Printing plugins");

        final List<String[]> result = Files.list(Paths.get(Constants.PLUGIN_DIR))
                .map(path -> {
                    try {
                        return processPluginDirectory(path);
                    } catch (Exception e) {
                        throw new RuntimeException("Exception when processing directory " + path, e);
                    }
                })
                .filter(Objects::nonNull)
                .filter(p -> p.getVersion() == null)
                .map(p -> new String[]{p.getName(), p.getVersion(), p.getExtendedProperties().toString()})
                .collect(toList());

        printTable(result);
    }

    private void printTable(List<String[]> result) {
        int columns = result.stream()
                .mapToInt(a -> a.length)
                .max()
                .orElse(0);

        int[] sizes = new int[columns];

        result.forEach(row -> {
            for (int i = 0; i < row.length; i++) {
                final String s = row[i];
                final int length = s == null ? 0 : s.length();
                if (sizes[i] < length) {
                    sizes[i] = length;
                }
            }
        });

        result.stream().map(row -> {
                    StringBuilder text = new StringBuilder();
                    for (int i = 0; i < row.length; i++) {
                        if (i > 0) {
                            text.append(" : ");
                        }
                        String s = row[i];
                        text.append(StringUtils.rightPad(s, sizes[i]));
                    }
                    return text.toString();
                }
        )
                .forEach(System.out::println);
    }

    private WowPlugin processPluginDirectory(Path path) {
        if (Files.exists(path) && Files.isDirectory(path)) {
            final Path tocPath = path.resolve(path.getFileName() + ".toc");

            if (!Files.exists(tocPath)) {
                throw new RuntimeException("TOC file does not exist for " + tocPath);
            }

            if (!Files.isRegularFile(tocPath)) {
                throw new RuntimeException("TOC file is not regular file: " + tocPath);
            }

            return readToc(tocPath);
        }
        return null;
    }

    private WowPlugin readToc(Path tocPath) {
        try {
            final Map<String, String> properties = Files.lines(tocPath)
                    .map(String::trim)
                    .filter(s -> s.startsWith("##"))
                    .filter(s -> !s.endsWith("##"))
                    .map(s -> s.substring(2))
                    .map(s -> {
                        final String[] result = s.split(":", 2);
                        if (result.length != 2) {
                            throw new RuntimeException("Error splitting property " + s);
                        }
                        return result;
                    })
                    .collect(Collectors.toMap(s -> s[0].trim(), s -> s[1].trim()));

            final WowPlugin result = new WowPlugin();

            result.setName(tocPath.getParent().getFileName().toString());

            final Map<String, BiConsumer<WowPlugin, String>> updaters = new HashMap<>();
            updaters.put("Author", WowPlugin::setAuthor);
            updaters.put("Interface", WowPlugin::setAnInterface);
            updaters.put("Notes", WowPlugin::setNotes);
            updaters.put("Revision", WowPlugin::setRevision);
            updaters.put("SavedVariables", WowPlugin::setSavedVariables);
            updaters.put("Title", WowPlugin::setTitle);
            updaters.put("Version", WowPlugin::setVersion);
            updaters.put("Dependencies", WowPlugin::setDependencies);
            updaters.put("Dependancies", WowPlugin::setDependencies);
            updaters.put("RequiredDeps", WowPlugin::setDependencies);
            updaters.put("Disabled", WowPlugin::setDisabled);
            updaters.put("OptionalDependencies", WowPlugin::setOptionalDependencies);
            updaters.put("OptionalDeps", WowPlugin::setOptionalDependencies);
            updaters.put("LoadOnDemand", WowPlugin::setLoadOnDemand);
            updaters.put("SavedVariablesPerCharacter", WowPlugin::setSavedVariablesPerCharacter);

            final Set<String> ignoredProperties =
                    new HashSet<>(Arrays.asList("Title-.*", "DefaultState", "Notes-.*", "URL"));

            properties.forEach((key, value) -> {

                if (!isIgnored(ignoredProperties, key)) {
                    final BiConsumer<WowPlugin, String> updater = updaters.get(key);

                    if (key.startsWith("X-")) {
                        result.getExtendedProperties().put(key, value);
                    } else if (updater != null) {
                        updater.accept(result, value);
                    } else {
                        printMap(properties);
                        throw new RuntimeException("Unknown property: " + key);
                    }
                }
            });
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isIgnored(Set<String> ignoredProperties, String key) {
        return ignoredProperties.stream()
                .anyMatch(s -> Pattern.compile(s).matcher(key).matches());
    }

    private void printMap(Map<String, String> properties) {
        int max = properties.keySet()
                .stream()
                .mapToInt(String::length)
                .max()
                .orElse(0);

        properties.entrySet().stream()
                .map(entry -> String.format("%-" + max + "s : %s", entry.getKey(), entry.getValue()))
                .sorted()
                .forEach(System.out::println);
    }

}
