package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask33: one-or-more rows each with an anchor VAL cell
 * (REC same-row + CONCAT below-same-col-same-str) followed by one-or-more VAL cells.
 */
class RtlTask33Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "33"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL : (CL(ROW+0))->REC, (DW(STR))->CONCAT] [VAL]+ ]+
                """;
    }
}
