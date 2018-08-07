package pl.jdata.wow.wow_plugin_updater.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import pl.jdata.wow.wow_plugin_updater.Constants;
import pl.jdata.wow.wow_plugin_updater.MyFileUtils;
import pl.jdata.wow.wow_plugin_updater.TableStringPrinter;
import pl.jdata.wow.wow_plugin_updater.console.Command;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Component
public class WowCommands {

    private static final String TEMP_DIRECTORY_NAME = "temp";

    private List<String> pluginUrls = Arrays.asList("https://wow.curseforge.com/projects/bagnon/files/latest");

    public static void main(String[] args) {
        try {
            final WowCommands wowCommands = new WowCommands();
            // wowCommands.printPlugins();
            // final List<Path> downloadedPaths = wowCommands.downloadPlugins();
            final List<Path> downloadedPaths = Collections.singletonList(
                    Paths.get("C:\\Users\\jacek\\IdeaProjects\\wow-plugin-updater\\temp\\Bagnon_8.0.2.zip"));

            List<WowPlugin> downloadedPlugins = wowCommands.readDowloadedPlugins(downloadedPaths);

            TableStringPrinter.builder()
                    .withBorder()
                    .header("Name", "Version")
                    .rows(
                            downloadedPlugins.stream()
                            .map(p -> new String[]{p.getName(), p.getVersion()})
                    )
                    .print();

        } catch (Throwable e) {
            e.printStackTrace(System.out);
        }
    }

    private List<WowPlugin> readDowloadedPlugins(List<Path> downloadedPaths) {
        return downloadedPaths.stream()
                .flatMap(zipPath -> {
                    if (!Files.isRegularFile(zipPath)) {
                        throw new RuntimeException(zipPath + " is not regular file");
                    }

                    final Pattern pattern = Pattern.compile("^([^/]+)/[^/]+\\.toc$");

                    List<WowPlugin> plugins = new ArrayList<>();

                    final ZipInputStream zipInputStream;
                    try {
                        zipInputStream = new ZipInputStream(new FileInputStream(zipPath.toFile()));
                        try {
                            ZipEntry zipEntry = zipInputStream.getNextEntry();
                            while (zipEntry != null) {
                                final String zipEntryName = zipEntry.getName();
                                final Matcher matcher = pattern.matcher(zipEntryName);
                                if (matcher.matches()) {
                                    final String pluginDirectoryName = matcher.group(1);
                                    System.out.println(" * file: " + zipEntryName);


                                    final Stream<String> tocContent =
                                            Stream.of(IOUtils.toString(zipInputStream, "UTF-8").split("\n"));
                                    plugins.add(readToc(tocContent, matcher.group(1)));
                                }
                                zipEntry = zipInputStream.getNextEntry();
                            }
                        } finally {
                            try {
                                zipInputStream.close();
                            } catch (RuntimeException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    return plugins.stream();
                })
                .collect(toList());
    }

    private List<Path> downloadPlugins() {
        return pluginUrls.stream()
                .map(s -> {
                    final Path temporaryDir = MyFileUtils.createDirectoryIfDoesNotExist(TEMP_DIRECTORY_NAME);
                    return HttpExample.downloadPlugin(s, temporaryDir, true);
                })
                .collect(toList());
    }

    @Command(name = "p", description = "Print plugin versions")
    public void printPlugins() throws IOException {
        System.out.println("Printing plugins");

        final Path basePluginDirectory = Paths.get(Constants.PLUGIN_DIR);

        final List<WowPlugin> wowPlugins = Files.list(basePluginDirectory)
                .map(path -> {
                    try {
                        return processPluginDirectory(path);
                    } catch (Exception e) {
                        throw new RuntimeException("Exception when processing directory " + path, e);
                    }
                })
                .filter(Objects::nonNull)
                .collect(toList());

        TableStringPrinter.builder()
                .header("Name", "Version")
                .withBorder()
                .rows(
                        wowPlugins.stream()
                                .sorted(comparing((WowPlugin wowPlugin) -> wowPlugin.getVersion() == null)
                                        .thenComparing(WowPlugin::getName))
                                // .filter(p -> p.getVersion() != null)
                                .map(p -> new String[]{p.getName(), p.getVersion()})
                )
                .print();

        // final List<String[]> result = wowPlugins.stream()
        //         .filter(p -> p.getVersion() == null)
        //         .map(p -> new String[]{p.getName(), p.getVersion(), p.getExtendedProperties().toString()})
        //         .collect(toList());
        //
        // printTable(result);
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
            final String pluginName = extractModuleNameFromPath(tocPath).toString();
            return readToc(Files.lines(tocPath), pluginName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private WowPlugin readToc(Stream<String> tocContent, String pluginName) {
        final Map<String, String> properties = tocContent
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

        result.setName(pluginName);

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
                    TableStringPrinter.printMap(properties);
                    throw new RuntimeException("Unknown property: " + key);
                }
            }
        });
        return result;
    }

    private Path extractModuleNameFromPath(Path tocPath) {
        final Path parent = tocPath.getParent();

        if (parent == null) {
            throw new RuntimeException("Null parent");
        }

        return parent.getFileName();
    }

    private boolean isIgnored(Set<String> ignoredProperties, String key) {
        return ignoredProperties.stream()
                .anyMatch(s -> Pattern.compile(s).matcher(key).matches());
    }

}
