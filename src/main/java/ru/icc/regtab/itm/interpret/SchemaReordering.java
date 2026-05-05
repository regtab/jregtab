package ru.icc.regtab.itm.interpret;

import ru.icc.regtab.itm.recordset.Record;
import ru.icc.regtab.itm.recordset.Recordset;
import ru.icc.regtab.itm.recordset.Schema;

import java.util.*;

/**
 * Reorders the schema attributes according to a user-defined order.
 */
public record SchemaReordering(List<String> order) implements RecordsetTransformation {

    public SchemaReordering {
        order = List.copyOf(Objects.requireNonNull(order));
    }

    @Override
    public Recordset apply(Recordset recordset) {
        Schema oldSchema = recordset.schema();
        List<String> newAttrs = new ArrayList<>();
        for (String attr : order) {
            if (oldSchema.contains(attr)) {
                newAttrs.add(attr);
            }
        }
        for (String attr : oldSchema.attributes()) {
            if (!newAttrs.contains(attr)) {
                newAttrs.add(attr);
            }
        }
        Schema newSchema = new Schema(newAttrs);
        List<Record> newRecords = new ArrayList<>();
        for (Record r : recordset.records()) {
            newRecords.add(new Record(newSchema, r.values()));
        }
        return new Recordset(newSchema, newRecords);
    }
}
