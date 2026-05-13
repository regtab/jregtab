package ru.icc.regtab.itm.rtl;

import ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.itm.recordset.Recordset;

/**
 * RTL equivalent of AtpTask03: row key at COL0, two data cells look up COL0 via RM{1}(COL0).
 */
class RtlTask03Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "03"; }

    @Override
    protected String buildRtl() {
        return "[ [VAL] [VAL : (RM{1}(COL0))->REC]{2} ]+";
    }

    @Override
    protected Recordset transformActual(Recordset actual) {
        return new AnchorAttributeAtPosition(1).apply(actual);
    }
}
