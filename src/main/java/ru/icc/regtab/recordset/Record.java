package ru.icc.regtab.recordset;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A record conforming to a schema: an ordered sequence of attribute-value pairs.
 * <p>
 * Values are stored positionally (one slot per schema attribute), so a record costs one small
 * array instead of a map — the recordset of a large table holds hundreds of thousands of them.
 */
public final class Record {

    private final Schema schema;
    private final String[] values;

    public Record(Schema schema, Map<String, String> values) {
        this.schema = Objects.requireNonNull(schema, "schema");
        Objects.requireNonNull(values, "values");
        this.values = new String[schema.size()];
        for (int i = 0; i < this.values.length; i++) {
            this.values[i] = values.get(schema.attributes().get(i));
        }
    }

    /**
     * Positional constructor: {@code values[i]} is the value of the i-th schema attribute
     * ({@code null} for a missing value). The array is copied.
     */
    public Record(Schema schema, String[] values) {
        this.schema = Objects.requireNonNull(schema, "schema");
        Objects.requireNonNull(values, "values");
        if (values.length != schema.size()) {
            throw new IllegalArgumentException("values.length must equal schema size: "
                    + values.length + " != " + schema.size());
        }
        this.values = values.clone();
    }

    public Schema schema() { return schema; }

    public String get(String attribute) {
        int i = schema.indexOf(attribute);
        return i < 0 ? null : values[i];
    }

    public String get(int index) { return values[index]; }

    /** Attribute → value map in schema order (snapshot; rejects {@code null} values like {@link Map#copyOf}). */
    public Map<String, String> values() {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i++) {
            map.put(schema.attributes().get(i), values[i]);
        }
        return Map.copyOf(map);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Record{");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(schema.attributes().get(i)).append("=").append(values[i]);
        }
        return sb.append("}").toString();
    }
}
