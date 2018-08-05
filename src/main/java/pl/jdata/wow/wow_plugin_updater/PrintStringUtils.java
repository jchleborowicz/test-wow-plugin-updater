package pl.jdata.wow.wow_plugin_updater;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import static java.util.stream.Collectors.toList;

public final class PrintStringUtils {

    private PrintStringUtils() {
    }

    public static void printTable(Stream<String[]> result) {
        printTable(null, result);
    }

    public static void printTable(List<String[]> result) {
        printTable(null, result);
    }

    public static void printTable(String[] header, List<String[]> result) {
        printTable(header, result, System.out);
    }

    public static void printTable(String[] header, Stream<String[]> result) {
        printTable(header, result.collect(toList()), System.out);
    }

    public static void printTable(String[] header, List<String[]> result, PrintStream output) {
        int columns = result.stream()
                .mapToInt(a -> a.length)
                .max()
                .orElse(0);

        if (header != null) {
            columns = Math.max(columns, header.length);
        }

        int[] sizes = new int[columns];

        if (header != null) {
            for (int i = 0; i < header.length; i++) {
                sizes[i] = header[i].length();
            }
        }

        result.forEach(row -> {
            for (int i = 0; i < row.length; i++) {
                final String s = row[i];
                final int length = s == null ? 0 : s.length();
                if (sizes[i] < length) {
                    sizes[i] = length;
                }
            }
        });

        if (header != null) {
            StringBuilder firstLine = new StringBuilder();
            StringBuilder secondLine = new StringBuilder();
            for (int i = 0; i < sizes.length; i++) {
                int size = sizes[i];

                if (i > 0) {
                    firstLine.append("|");
                    secondLine.append("+");
                }

                final String columnName = header.length > i ? header[i] : "";

                firstLine.append(StringUtils.rightPad(columnName, size));
                secondLine.append(StringUtils.repeat('-', size));
            }

            output.println(firstLine);
            output.println(secondLine);
        }

        result.stream().map(row -> {
                    StringBuilder text = new StringBuilder();
                    for (int i = 0; i < row.length; i++) {
                        if (i > 0) {
                            text.append("|");
                        }
                        String s = row[i];
                        text.append(StringUtils.rightPad(s, sizes[i]));
                    }
                    return text.toString();
                }
        )
                .forEach(output::println);
    }

    public static void printMap(Map<String, String> values) {
        final List<String[]> valuesList = values.entrySet().stream()
                .map(entry -> new String[]{entry.getKey(), entry.getValue()})
                .collect(toList());

        printTable(valuesList);
    }
}
