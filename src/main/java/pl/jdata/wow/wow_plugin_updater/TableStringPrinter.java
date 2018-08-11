package pl.jdata.wow.wow_plugin_updater;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

public class TableStringPrinter {

    private static void printTable(TablePrintSpecification specification) {
        int columns = specification.getRows().stream()
                .mapToInt(a -> a.length)
                .max()
                .orElse(0);

        if (specification.getHeader() != null) {
            columns = Math.max(columns, specification.getHeader().length);
        }

        int[] sizes = new int[columns];

        if (specification.getHeader() != null) {
            for (int i = 0; i < specification.getHeader().length; i++) {
                sizes[i] = specification.getHeader()[i].length();
            }
        }

        specification.getRows().forEach(row -> {
            for (int i = 0; i < row.length; i++) {
                final String s = row[i];
                final int length = s == null ? 0 : s.length();
                if (sizes[i] < length) {
                    sizes[i] = length;
                }
            }
        });

        StringBuilder separatorLineBuilder = new StringBuilder();
        for (int i = 0; i < sizes.length; i++) {
            int size = sizes[i];
            if (i > 0) {
                separatorLineBuilder.append("+");
            }
            separatorLineBuilder.append(StringUtils.repeat("-", size));
        }

        final String separatorLine = specification.isHasBorder() ? "+" + separatorLineBuilder.toString() + "+" :
                separatorLineBuilder.toString();

        if (specification.isHasBorder()) {
            specification.getOutput().println(separatorLine);
        }

        if (specification.getHeader() != null) {
            StringBuilder firstLine = new StringBuilder();
            for (int i = 0; i < sizes.length; i++) {
                int size = sizes[i];

                if (i > 0) {
                    firstLine.append("|");
                }

                final String columnName = specification.getHeader().length > i ? specification.getHeader()[i] : "";

                firstLine.append(StringUtils.rightPad(columnName, size));
            }

            if (specification.hasBorder) {
                specification.getOutput().print("|");
            }
            specification.getOutput().print(firstLine);
            if (specification.hasBorder) {
                specification.getOutput().print("|");
            }
            specification.getOutput().println();
            specification.getOutput().println(separatorLine);
        }

        specification.getRows().forEach(row -> {
                    if (specification.hasBorder) {
                        specification.getOutput().print("|");
                    }

                    for (int i = 0; i < sizes.length; i++) {
                        int size = sizes[i];
                        if (i > 0) {
                            specification.getOutput().print("|");
                        }

                        final String value = row.length > i ? row[i] : "";

                        specification.getOutput().print(StringUtils.rightPad(StringUtils.defaultString(value), size));
                    }

                    if (specification.hasBorder) {
                        specification.getOutput().print("|");
                    }

                    specification.getOutput().println();
                }
        );

        if (specification.isHasBorder()) {
            specification.getOutput().println(separatorLine);
        }
    }

    public static TableStringPrinterBuilder builder() {
        return new TableStringPrinterBuilder();
    }

    public static void printMap(Map<?, ?> values) {
        TableStringPrinter.builder()
                .rows(values.entrySet().stream()
                        .map(entry -> new String[]{
                                entry.getKey() == null ? "" : entry.getKey().toString(),
                                entry.getValue() == null ? "" : entry.getValue().toString()
                        })
                )
                .print();
    }

    @Data
    private static class TablePrintSpecification {
        private String[] header;
        private boolean hasBorder;
        private List<String[]> rows = new ArrayList<>();
        private PrintStream output = System.out;
    }

    public static class TableStringPrinterBuilder {

        private TablePrintSpecification specification = new TablePrintSpecification();

        private TableStringPrinterBuilder() {
        }

        public TableStringPrinterBuilder header(String... values) {
            if (this.specification.getHeader() != null) {
                throw new RuntimeException("Header already specified");
            }

            this.specification.setHeader(values);

            return this;
        }

        public TableStringPrinterBuilder withBorder() {
            this.specification.setHasBorder(true);
            return this;
        }

        public TableStringPrinterBuilder row(String... values) {
            this.specification.getRows().add(values);
            return this;
        }

        public TableStringPrinterBuilder rows(Map<?, ?> values) {
            return rows(values.entrySet()
                    .stream()
                    .map(entry -> new String[]{
                            entry.getKey() == null ? "" : entry.getKey().toString(),
                            entry.getValue() == null ? "" : entry.getValue().toString()
                    })
            );
        }

        public TableStringPrinterBuilder rows(List<String[]> values) {
            this.specification.getRows().addAll(values);

            return this;
        }

        public TableStringPrinterBuilder rows(Stream<String[]> values) {
            values.forEach(this.specification.getRows()::add);

            return this;
        }

        public void print(PrintStream out) {
            this.specification.setOutput(out);
            this.print();
        }

        public void print() {
            TableStringPrinter.printTable(this.specification);
        }
    }
}
