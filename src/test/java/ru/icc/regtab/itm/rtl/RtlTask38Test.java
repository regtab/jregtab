package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask38: forward-fill blank value cells.
 * Each row: VAL (rec same-row), VAL, conditional (blank → VAL: UW->FILL | VAL).
 * UW picks the nearest non-blank cell above in the same column (REVERSE_COLUMN_MAJOR, card=1).
 */
public class RtlTask38Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "38"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL : SR*->REC] [VAL] [(BLANK ? VAL : -AV->FILL | VAL)] ]+
                """;
    }
}
