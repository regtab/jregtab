package ru.icc.regtab.itm.rtl;

import ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.itm.recordset.Recordset;

/**
 * RTL equivalent of AtpTask37: corner skip + qual-header row, per-person rows with
 * conditional date cells (blank → skip, non-blank → val: first-in-row + first-in-col ->REC).
 * Post-processed by AnchorAttributeAtPosition(2).
 */
class RtlTask37Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "37"; }

    @Override
    protected String buildRtl() {
        return """
                [ [] [VAL]+ ] [ [VAL] [(BLANK ? SKIP | VAL : (RM{1}, CM{1})->REC)]+ ]+
                """;
    }

    @Override
    protected Recordset transformActual(Recordset actual) {
        return new AnchorAttributeAtPosition(2).apply(actual);
    }
}
