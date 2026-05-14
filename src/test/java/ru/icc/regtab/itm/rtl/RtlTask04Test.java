package ru.icc.regtab.itm.rtl;

import ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.itm.recordset.Recordset;

/**
 * RTL equivalent of AtpTask04: skip row, then rows with row key at COL0.
 */
class RtlTask04Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "04"; }

    @Override
    protected String buildRtl() {
        return """
                [ [SKIP]+ ]
                [ [VAL] [VAL : RM{1}->REC]+ ]+
                """;
    }

    @Override
    protected Recordset transformActual(Recordset actual) {
        return new AnchorAttributeAtPosition(1).apply(actual);
    }
}
