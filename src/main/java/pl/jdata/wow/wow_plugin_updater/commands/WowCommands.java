package pl.jdata.wow.wow_plugin_updater.commands;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
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

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
public class WowCommands {

    private static final String TEMP_DIRECTORY_NAME = "build/temp-plugins";

    private List<String> pluginUrls = Arrays.asList(
            "https://wow.curseforge.com/projects/bagnon/files/latest",
            "https://wow.curseforge.com/projects/auctioneer/files/latest",
            "https://wow.curseforge.com/projects/deadly-boss-mods/files/latest",
            "https://wow.curseforge.com/projects/gatherer/files/latest",
            "https://wow.curseforge.com/projects/nugie-combo-bar/files/latest",
            "https://www.curseforge.com/wow/addons/bartender4/download/2585279/file",
            "https://wow.curseforge.com/projects/details/files/latest"
    );

    public static void main(String[] args) {
        try {
            final WowCommands wowCommands = new WowCommands();
            final List<WowPlugin> localPlugins = wowCommands.getLocalPlugins();

            wowCommands.downloadPlugins(TEMP_DIRECTORY_NAME, false);

            final List<Path> downloadedPaths = new ArrayList<>();

            Files.newDirectoryStream(Paths.get(TEMP_DIRECTORY_NAME),
                    path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".zip"))
                    .forEach(downloadedPaths::add);

            List<WowPlugin> downloadedPlugins = wowCommands.readDowloadedPlugins(downloadedPaths);

            final Set<String> ignoredPlugins = new HashSet<>(Arrays.asList("SlideBar", "!Swatter"));

            final Map<String, WowPlugin> downloadedPluginsByName = downloadedPlugins.stream()
                    .filter(plugin -> !ignoredPlugins.contains(plugin.getName()))
                    .collect(toMap(WowPlugin::getName, Function.identity()));

            List<String> errors = new ArrayList<>();

            localPlugins.forEach(localPlugin -> {
                final WowPlugin downloadedPlugin = downloadedPluginsByName.get(localPlugin.getName());

                if (downloadedPlugin == null) {
                    if (!ignoredPlugins.contains(localPlugin.getName())) {
                        errors.add("plugin not dowloaded: " + localPlugin.getName());
                    }
                } else if (!Objects.equals(localPlugin.getVersion(), downloadedPlugin.getVersion())) {
                    errors.add("new version availale for plugin " + localPlugin.getName() + ": "
                            + localPlugin.getVersion() + " -> " + downloadedPlugin.getVersion());
                }
            });

            if (errors.isEmpty()) {
                System.out.println("No errors!!! YAY");
            } else {
                System.out.println(errors.stream().collect(Collectors.joining("\n* ", "Errors:\n* ", "\n")));
            }
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

    private List<Path> downloadPlugins(String directoryName, boolean overwriteExisting) {
        return pluginUrls.stream()
                .map(s -> {
                    final Path temporaryDir = MyFileUtils.createDirectoryIfDoesNotExist(directoryName);
                    return HttpExample.downloadPlugin(s, temporaryDir, overwriteExisting);
                })
                .collect(toList());
    }

    public List<WowPlugin> getLocalPlugins() throws IOException {
        final Path basePluginDirectory = Paths.get(Constants.PLUGIN_DIR);

        return Files.list(basePluginDirectory)
                .map(path -> {
                    try {
                        return processPluginDirectory(path);
                    } catch (Exception e) {
                        throw new RuntimeException("Exception when processing directory " + path, e);
                    }
                })
                .filter(Objects::nonNull)
                .collect(toList());
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
