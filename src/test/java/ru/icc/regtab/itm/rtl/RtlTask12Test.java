package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask12: header cell at COL0 collects all COL5 values via CM(COL5).
 */
class RtlTask12Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "12"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL : (CM(COL5))->REC] [SKIP]{4} [VAL] ]
                [ [SKIP]{5} [VAL] ]+
                """;
    }
}
