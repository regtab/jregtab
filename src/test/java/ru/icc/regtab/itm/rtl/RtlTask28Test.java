package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask28: header row with CL(ST)->REC and one-or-more value cells,
 * followed by one-or-more data rows each with one-or-more value cells.
 */
class RtlTask28Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "28"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL : ST->REC] [VAL]+ ] [ [VAL]+ ]+
                """;
    }
}
