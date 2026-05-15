package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask20: first cell of header row provides records via CL(ST)->REC
 * (all VAL items in the same subtable); followed by one-or-more data rows with two cells each.
 */
class RtlTask20Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "20"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL : ST->REC] [VAL] ] [ [VAL] [VAL] ]+
                """;
    }
}
