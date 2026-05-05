package ru.icc.regtab.itm.interpret;

import ru.icc.regtab.itm.recordset.Record;
import ru.icc.regtab.itm.recordset.Recordset;
import ru.icc.regtab.itm.recordset.Schema;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Moves the anchor attribute (first in schema) to the given 0-based position.
 * Reassigns values so that $a_0, $a_1, … receive the values from the reordered columns
 * (e.g. for position 2: $a_0←col1, $a_1←col2, $a_2←anchor).
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
        if (attrs.size() <= 1 || position >= attrs.size()) {
            return recordset;
        }
        if (position == 0) {
            return recordset;
        }
        String anchor = attrs.get(0);
        List<String> rest = attrs.subList(1, attrs.size());
        List<String> reordered = new ArrayList<>(attrs.size());
        for (int i = 0; i < position; i++) {
            reordered.add(rest.get(i));
        }
        reordered.add(anchor);
        for (int i = position; i < rest.size(); i++) {
            reordered.add(rest.get(i));
        }
        List<String> canonicalOrder = new ArrayList<>(attrs);
        Schema newSchema = new Schema(canonicalOrder);
        List<Record> newRecords = new ArrayList<>(recordset.size());
        for (Record r : recordset.records()) {
            Map<String, String> values = new LinkedHashMap<>();
            for (int i = 0; i < reordered.size(); i++) {
                values.put(canonicalOrder.get(i), r.get(reordered.get(i)));
            }
            newRecords.add(new Record(newSchema, values));
        }
        return new Recordset(newSchema, newRecords);
    }
}
