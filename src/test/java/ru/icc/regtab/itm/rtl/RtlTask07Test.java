package ru.icc.regtab.itm.rtl;

import ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.itm.recordset.Recordset;

/**
 * RTL equivalent of AtpTask07: three row keys at COL0..2, col key at ROW0.
 */
class RtlTask07Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "07"; }

    @Override
    protected String buildRtl() {
        return """
                [ [SKIP]{3} [VAL]+ ]
                [ [VAL]{3} [VAL : (RM{3}(COL0..2), CM{1})->REC]+ ]+
                """;
    }

    @Override
    protected Recordset transformActual(Recordset actual) {
        return new AnchorAttributeAtPosition(4).apply(actual);
    }
}
