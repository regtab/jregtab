package ru.icc.regtab.itm.rtl;

import ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.itm.recordset.Recordset;

/**
 * RTL equivalent of AtpTask05: unpivot with row key at COL0 and col key at ROW0.
 */
class RtlTask05Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "05"; }

    @Override
    protected String buildRtl() {
        return """
                [ [SKIP] [VAL]+ ]
                [ [SKIP]+ ]
                [ [VAL] [VAL : (RM{1}, CM{1})->REC]+ ]+
                """;
    }

    @Override
    protected Recordset transformActual(Recordset actual) {
        return new AnchorAttributeAtPosition(2).apply(actual);
    }
}
