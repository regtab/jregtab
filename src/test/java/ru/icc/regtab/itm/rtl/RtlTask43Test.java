package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask43: same as Task42 but with exactly 3 compound cells per row.
 */
class RtlTask43Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "43"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL : ''->AVP, RW->REC] [ATTR ":" VAL : CL{1}->AVP]{3} ]+
                """;
    }
}
