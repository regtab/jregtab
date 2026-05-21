package ru.icc.regtab.recordset;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A record conforming to a schema: an ordered sequence of attribute-value pairs.
 */
public final class Record {

    private final Schema schema;
    private final Map<String, String> values;

    public Record(Schema schema, Map<String, String> values) {
        this.schema = Objects.requireNonNull(schema, "schema");
        Objects.requireNonNull(values, "values");
        this.values = new LinkedHashMap<>();
        for (String attr : schema.attributes()) {
            this.values.put(attr, values.get(attr));
        }
    }

    public Schema schema() { return schema; }

    public String get(String attribute) { return values.get(attribute); }

    public String get(int index) { return values.get(schema.attributes().get(index)); }

    public Map<String, String> values() { return Map.copyOf(values); }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Record{");
        boolean first = true;
        for (String attr : schema.attributes()) {
            if (!first) sb.append(", ");
            sb.append(attr).append("=").append(values.get(attr));
            first = false;
        }
        return sb.append("}").toString();
    }
}
