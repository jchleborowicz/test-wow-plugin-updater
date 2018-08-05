package pl.jdata.wow.wow_plugin_updater;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public final class StringUtils {

    private StringUtils() {
    }

    public static void printTable(List<String[]> result) {
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
                        text.append(org.apache.commons.lang3.StringUtils.rightPad(s, sizes[i]));
                    }
                    return text.toString();
                }
        )
                .forEach(System.out::println);
    }

    public static void printMap(Map<String, String> values) {
        final List<String[]> valuesList = values.entrySet().stream()
                .map(entry -> new String[]{entry.getKey(), entry.getValue()})
                .collect(toList());

        printTable(valuesList);
    }
}
