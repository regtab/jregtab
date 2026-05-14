package ru.icc.regtab.itm.rtl;

import ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.itm.recordset.Recordset;

/**
 * RTL equivalent of AtpTask49: header skip+val row followed by data rows with
 * VAL and VAL rec(first-same-row, first-same-col). Post-processed by AnchorAttributeAtPosition(2).
 */
class RtlTask49Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "49"; }

    @Override
    protected String buildRtl() {
        return """
                [ [SKIP] [!BLANK? VAL]+ ]
                [ [!BLANK? VAL] [!BLANK? VAL : (CL{1}(ROW+0), CL{1}(COL+0))->REC]+ ]+
                """;
    }

    @Override
    protected Recordset transformActual(Recordset actual) {
        return new AnchorAttributeAtPosition(2).apply(actual);
    }
}
