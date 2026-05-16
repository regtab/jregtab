package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask22: header uses CM(C2..5)->REC in column-major order.
 */
class RtlTask22Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "22"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL : ^(ST, C2..5)->REC] [] [VAL]+ ] [ []{2} [VAL]+ ] }+
                """;
    }
}
