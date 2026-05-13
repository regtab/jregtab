package ru.icc.regtab.itm.rtl;

import ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.itm.recordset.Recordset;

/**
 * RTL equivalent of AtpTask14: header row with blank sentinel, data rows look up
 * first items in COL0 and COL1 of same subtable, plus two items from same row.
 */
class RtlTask14Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "14"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [!BLANK ? VAL]{2} [BLANK ? SKIP] ]
                  [ [!BLANK ? VAL]{2} [!BLANK ? VAL : (CL{1}(ST, COL0), CL{1}(ST, COL1), CL{2}(ROW+0))->REC] ]+ }+
                """;
    }

    @Override
    protected Recordset transformActual(Recordset actual) {
        return new AnchorAttributeAtPosition(4).apply(actual);
    }
}
