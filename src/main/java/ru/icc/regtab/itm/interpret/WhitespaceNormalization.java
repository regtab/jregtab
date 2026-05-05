package ru.icc.regtab.itm.interpret;

import ru.icc.regtab.itm.recordset.Record;
import ru.icc.regtab.itm.recordset.Recordset;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Removes leading/trailing whitespace and collapses internal whitespace to a single space.
 */
public record WhitespaceNormalization() implements RecordsetTransformation {

    @Override
    public Recordset apply(Recordset recordset) {
        List<Record> normalized = new ArrayList<>();
        for (Record r : recordset.records()) {
            Map<String, String> vals = new LinkedHashMap<>();
            for (String attr : recordset.schema().attributes()) {
                String v = r.get(attr);
                vals.put(attr, v != null ? v.strip().replaceAll("\\s+", " ") : null);
            }
            normalized.add(new Record(recordset.schema(), vals));
        }
        return new Recordset(recordset.schema(), normalized);
    }
}
