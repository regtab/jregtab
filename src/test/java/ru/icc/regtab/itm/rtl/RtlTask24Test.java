package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask24: single header row uses DW->REC for all values below.
 */
class RtlTask24Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "24"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL : BW->REC] ]
                [ [VAL] ]+
                """;
    }
}
