package ru.icc.regtab.recordset;

import java.util.List;
import java.util.Objects;

/**
 * A recordset: a schema and a finite sequence of records conforming to it.
 */
public final class Recordset {

    private final Schema schema;
    private final List<Record> records;

    public Recordset(Schema schema, List<Record> records) {
        this.schema = Objects.requireNonNull(schema, "schema");
        this.records = List.copyOf(Objects.requireNonNull(records, "records"));
    }

    public Schema schema() { return schema; }
    public List<Record> records() { return records; }
    public int size() { return records.size(); }
    public Record get(int index) { return records.get(index); }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Recordset[schema=").append(schema).append(", records=[\n");
        for (Record r : records) {
            sb.append("  ").append(r).append("\n");
        }
        return sb.append("]]").toString();
    }
}
