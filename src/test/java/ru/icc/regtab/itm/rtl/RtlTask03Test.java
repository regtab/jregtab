package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask03: row key at COL0, two data cells look up COL0 via RM(COL0).
 */
class RtlTask03Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "03"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL] [VAL : SR->REC(1)]{2} ]+
                """;
    }
}
