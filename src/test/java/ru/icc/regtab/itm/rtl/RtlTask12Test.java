package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask12: header cell at C0 collects all C5 values via CM(C5).
 */
class RtlTask12Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "12"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL : CM(C5)->REC] [SKIP]{4} [VAL] ]
                [ [SKIP]{5} [VAL] ]+
                """;
    }
}
