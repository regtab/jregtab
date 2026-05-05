package ru.icc.regtab.itm.tasks;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import ru.icc.regtab.itm.model.syntax.TableSyntax;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Loads a CSV file into TableSyntax.
 * Format: no header, delimiter ',', values in quotes, UTF-8.
 */
public final class CsvTableLoader {

    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setDelimiter(',')
            .setQuote('"')
            .setRecordSeparator("\n")
            .setIgnoreEmptyLines(false)
            .build();

    /**
     * Loads CSV from path. First row = data (no header).
     */
    public static TableSyntax load(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, FORMAT)) {
            List<CSVRecord> records = parser.getRecords();
            if (records.isEmpty()) {
                throw new IllegalArgumentException("Empty CSV: " + path);
            }
            int numRows = records.size();
            int numCols = records.getFirst().size();
            for (CSVRecord r : records) {
                if (r.size() != numCols) {
                    throw new IllegalArgumentException("Inconsistent column count at row " + r.getRecordNumber());
                }
            }
            TableSyntax syntax = new TableSyntax(numRows, numCols);
            for (int row = 0; row < numRows; row++) {
                CSVRecord rec = records.get(row);
                for (int col = 0; col < numCols; col++) {
                    String val = rec.get(col);
                    syntax.getCell(row, col).setText(val != null ? val : "");
                }
            }
            return syntax;
        }
    }
}
