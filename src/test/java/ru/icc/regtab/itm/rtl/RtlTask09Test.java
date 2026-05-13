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
                [ [SKIP] [VAL = REPL('\\s+', '')]{5} ]
                [ { [VAL] [(BLANK? SKIP | VAL : (RM{1}(COL0), CM{1}(ROW0))->REC)]+ } ]+
                """;
    }

    @Override
    protected Recordset transformActual(Recordset actual) {
        return new AnchorAttributeAtPosition(2).apply(actual);
    }
}
