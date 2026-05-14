package ru.icc.regtab.itm.rtl;

import ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.itm.recordset.Recordset;

/**
 * RTL equivalent of AtpTask32: header row (SKIP, VAL+), data rows (VAL,
 * (BLANK?SKIP|VAL: first-in-row + first-in-col ->REC)+); post-processed by
 * AnchorAttributeAtPosition(2).
 */
class RtlTask32Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "32"; }

    @Override
    protected String buildRtl() {
        return """
                [ [] [VAL]+ ] [ [VAL] [(BLANK ? _ | VAL : (RM{1}, CM{1})->REC)]+ ]+
                """;
    }

    @Override
    protected Recordset transformActual(Recordset actual) {
        return new AnchorAttributeAtPosition(2).apply(actual);
    }
}
