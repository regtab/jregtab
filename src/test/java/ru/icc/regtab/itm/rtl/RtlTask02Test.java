package ru.icc.regtab.itm.rtl;

import ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.itm.interpret.WhitespaceNormalization;
import ru.icc.regtab.itm.recordset.Recordset;

/**
 * RTL equivalent of AtpTask02: two header rows (#L1, #L2), data rows look up both headers
 * via CL(ST, TAG #L1 #L2) and same-row anchor via CL{1}(ROW+0).
 */
class RtlTask02Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "02"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL #L1] [SKIP] ]
                  [ [VAL #L2] [SKIP] ]
                  [ [!BLANK ? VAL : (CL(ST, TAG #L1 #L2), CL{1}(ROW+0))->REC] [VAL] ]+
                  [ [BLANK ? SKIP] [SKIP] ]? }+
                """;
    }

    @Override
    protected Recordset transformActual(Recordset actual) {
        Recordset normalized = new WhitespaceNormalization().apply(actual);
        return new AnchorAttributeAtPosition(2).apply(normalized);
    }
}
