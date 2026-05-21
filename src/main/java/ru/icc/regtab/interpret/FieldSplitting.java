package ru.icc.regtab.interpret;

import ru.icc.regtab.recordset.Record;
import ru.icc.regtab.recordset.Recordset;
import ru.icc.regtab.recordset.Schema;

import java.util.*;

/**
 * Splits fields containing delimited values into multiple fields with atomic values.
 * <p>
 * By default, part {@code p} (0-based) becomes {@code attribute + "_" + (p + 1)} (e.g. {@code $a_0_1}, {@code $a_0_2}).
 * Pass {@link #partAttributeNames()} to assign explicit names (e.g. anonymous {@code $a_0}, {@code $a_1}, …) instead.
 */
public record FieldSplitting(String attribute, String delimiter, List<String> partAttributeNames)
        implements RecordsetTransformation {

    public FieldSplitting(String attribute, String delimiter) {
        this(attribute, delimiter, List.of());
    }

    public FieldSplitting {
        Objects.requireNonNull(attribute);
        Objects.requireNonNull(delimiter);
        partAttributeNames = partAttributeNames == null ? List.of() : List.copyOf(partAttributeNames);
    }

    @Override
    public Recordset apply(Recordset recordset) {
        Schema oldSchema = recordset.schema();
        int idx = oldSchema.indexOf(attribute);
        if (idx < 0) return recordset;

        int maxParts = 1;
        for (Record r : recordset.records()) {
            String val = r.get(attribute);
            if (val != null) {
                maxParts = Math.max(maxParts, val.split(delimiter, -1).length);
            }
        }
        if (maxParts <= 1) return recordset;

        if (!partAttributeNames.isEmpty() && partAttributeNames.size() != maxParts) {
            throw new IllegalArgumentException(
                    "partAttributeNames size (" + partAttributeNames.size() + ") must equal split part count ("
                            + maxParts + ")");
        }

        List<String> partNames = new ArrayList<>(maxParts);
        for (int p = 0; p < maxParts; p++) {
            if (!partAttributeNames.isEmpty()) {
                partNames.add(partAttributeNames.get(p));
            } else {
                partNames.add(attribute + "_" + (p + 1));
            }
        }

        List<String> newAttrs = new ArrayList<>();
        for (int i = 0; i < oldSchema.size(); i++) {
            if (i == idx) {
                newAttrs.addAll(partNames);
            } else {
                newAttrs.add(oldSchema.attributes().get(i));
            }
        }
        Schema newSchema = new Schema(newAttrs);

        List<Record> newRecords = new ArrayList<>();
        for (Record r : recordset.records()) {
            Map<String, String> vals = new LinkedHashMap<>();
            for (String attr : oldSchema.attributes()) {
                if (attr.equals(attribute)) {
                    String val = r.get(attr);
                    String[] parts = val != null ? val.split(delimiter, -1) : new String[0];
                    for (int p = 0; p < maxParts; p++) {
                        vals.put(partNames.get(p), p < parts.length ? parts[p] : null);
                    }
                } else {
                    vals.put(attr, r.get(attr));
                }
            }
            newRecords.add(new Record(newSchema, vals));
        }
        return new Recordset(newSchema, newRecords);
    }
}
