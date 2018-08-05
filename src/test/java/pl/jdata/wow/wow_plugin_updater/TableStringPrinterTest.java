package pl.jdata.wow.wow_plugin_updater;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TableStringPrinterTest {

    @Test
    public void printsTable() {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        TableStringPrinter.builder()
                .header("A", "Bam", "Moo")
                .withBorder()
                .row("Data1", "C", "F")
                .row("D", "Cu", "Value")
                .row("N")
                .print(new PrintStream(output));

        assertThat(output.toString()).isEqualTo(
                  "+-----+---+-----+" + System.lineSeparator()
                + "|A    |Bam|Moo  |" + System.lineSeparator()
                + "+-----+---+-----+" + System.lineSeparator()
                + "|Data1|C  |F    |" + System.lineSeparator()
                + "|D    |Cu |Value|" + System.lineSeparator()
                + "|N    |   |     |" + System.lineSeparator()
                + "+-----+---+-----+" + System.lineSeparator()
        );
    }

}