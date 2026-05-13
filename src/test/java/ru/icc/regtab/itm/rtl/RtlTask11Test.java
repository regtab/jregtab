package ru.icc.regtab.itm.rtl;

import ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.itm.recordset.Recordset;

/**
 * RTL equivalent of AtpTask11: header row, data rows use explicit subrow with
 * conditional content spec looking up first in same row and same column.
 */
class RtlTask11Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "11"; }

    @Override
    protected String buildRtl() {
        return """
                [ [SKIP] [VAL]+ ]
                [ { [VAL] [(BLANK ? SKIP | VAL : (CL{1}(ROW+0), CL{1}(COL+0))->REC)]+ } ]+
                """;
    }

    @Override
    protected Recordset transformActual(Recordset actual) {
        return new AnchorAttributeAtPosition(2).apply(actual);
    }
}
