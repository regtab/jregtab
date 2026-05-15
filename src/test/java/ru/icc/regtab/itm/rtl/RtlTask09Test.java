package ru.icc.regtab.itm.rtl;

import ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.itm.recordset.Recordset;

/**
 * RTL equivalent of AtpTask09: subrow pattern with REPL extractor and conditional.
 * Row key at COL0 (RM), col key at ROW0 (CM). Blank cells are skipped.
 */
class RtlTask09Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "09"; }

    @Override
    protected String buildRtl() {
        return """
                [ [] [VAL = REPL('\\s+', '')]{5} ]
                [ { [VAL] [(BLANK? _ | VAL : (SR{1}, SC{1})->REC)]+ } ]+
                """;
    }

    @Override
    protected Recordset transformActual(Recordset actual) {
        return new AnchorAttributeAtPosition(2).apply(actual);
    }
}
