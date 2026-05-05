package ru.icc.regtab.itm.tasks;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import ru.icc.regtab.itm.recordset.Record;
import ru.icc.regtab.itm.recordset.Recordset;
import ru.icc.regtab.itm.recordset.Schema;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads expected Recordset from CSV.
 * Format: first row = attribute names, rest = records. Delimiter ',', values in quotes, UTF-8.
 */
public final class CsvRecordsetLoader {

    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setDelimiter(',')
            .setQuote('"')
            .setRecordSeparator("\n")
            .setIgnoreEmptyLines(false)
            .build();

    /**
     * Loads Recordset from path. First row = schema (attribute names), rest = records.
     */
    public static Recordset load(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, FORMAT)) {
            List<CSVRecord> records = parser.getRecords();
            if (records.isEmpty()) {
                throw new IllegalArgumentException("Empty CSV: " + path);
            }
            List<String> attributes = new ArrayList<>();
            for (String attr : records.getFirst()) {
                attributes.add(attr != null ? attr.trim() : "");
            }
            Schema schema = new Schema(attributes);

            List<Record> recordList = new ArrayList<>();
            for (int i = 1; i < records.size(); i++) {
                CSVRecord rec = records.get(i);
                Map<String, String> values = new LinkedHashMap<>();
                for (int j = 0; j < attributes.size(); j++) {
                    String val = j < rec.size() ? rec.get(j) : null;
                    values.put(attributes.get(j), val != null ? val : "");
                }
                recordList.add(new Record(schema, values));
            }
            return new Recordset(schema, recordList);
        }
    }
}
