package ru.icc.regtab.itm.rtl;

import ru.icc.regtab.itm.interpret.WhitespaceNormalization;
import ru.icc.regtab.itm.recordset.Recordset;

/**
 * RTL equivalent of AtpTask21: multi-column header with DW->REC, exactly 2 data rows per subtable.
 */
class RtlTask21Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "21"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL : (DW)->REC]+ ] [ [VAL]+ ]{2} }+
                """;
    }

    @Override
    protected Recordset transformActual(Recordset actual) {
        return new WhitespaceNormalization().apply(actual);
    }
}
