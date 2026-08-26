package ru.icc.regtab.interpret;

import ru.icc.regtab.recordset.Record;
import ru.icc.regtab.recordset.Recordset;
import ru.icc.regtab.recordset.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * Moves the anchor attribute (first in schema) to the given 0-based position.
 * <p>
 * The <em>attribute</em> is moved — its name travels together with its values, so the
 * attribute-value binding of every record is preserved: only the order of the schema changes.
 * The rule is the same for named attributes (produced by AVP) and for anonymous ones:
 * an anonymous name is <em>not</em> renumbered, so after {@code ANCH(2)} a schema
 * {@code $a_1, $a_2, $a_3} becomes {@code $a_2, $a_3, $a_1} and the moved attribute stays
 * visible under its original name.
 * <p>
 * This is {@link SchemaReordering} with the order derived from the anchor position.
 * A position of 0, a position beyond the schema, or a schema of at most one attribute
 * leaves the recordset unchanged.
 */
public record AnchorAttributeAtPosition(int position) implements RecordsetTransformation {

    public AnchorAttributeAtPosition {
        if (position < 0) {
            throw new IllegalArgumentException("position must be non-negative: " + position);
        }
    }

    @Override
    public Recordset apply(Recordset recordset) {
        List<String> attrs = recordset.schema().attributes();
        if (attrs.size() <= 1 || position == 0 || position >= attrs.size()) {
            return recordset;
        }
        List<String> reordered = new ArrayList<>(attrs.subList(1, attrs.size()));
        reordered.add(position, attrs.get(0));

        Schema newSchema = new Schema(reordered);
        List<Record> newRecords = new ArrayList<>(recordset.size());
        for (Record r : recordset.records()) {
            newRecords.add(new Record(newSchema, r.values()));
        }
        return new Recordset(newSchema, newRecords);
    }
}
