package ru.icc.regtab.itm.rtl;

import ru.icc.regtab.itm.interpret.DelimitedFieldSplit;
import ru.icc.regtab.itm.recordset.Recordset;

/**
 * RTL equivalent of AtpTask25: each row has ID cell (SUFFIX+REC+CONCAT), account cell,
 * and one-or-more data cells; rows with same ID are grouped via CONCAT.
 * Result is post-processed by DelimitedFieldSplit("/").
 */
class RtlTask25Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "25"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL : (RW{1})->SUFFIX('/'), (RW(COL+2..))->REC, (DW(STR))->CONCAT] [VAL] [VAL]+ ]+
                """;
    }

    @Override
    protected Recordset transformActual(Recordset actual) {
        return new DelimitedFieldSplit("/").apply(actual);
    }
}
