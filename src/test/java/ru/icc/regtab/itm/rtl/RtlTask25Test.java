package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask25: each row has ID cell (SUFFIX+REC+CONCAT), account cell,
 * and one-or-more data cells; rows with same ID are grouped via CONCAT.
 * Result is post-processed by DelimitedFieldSplit("/").
 */
public class RtlTask25Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "25"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL : RT->SUFFIX('/'), (RT & C+2..)*->REC('/'), (BW & STR)*->CONCAT] [VAL]+ ]+
                """;
    }
}
