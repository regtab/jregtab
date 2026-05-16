package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask15: compound spec with three CL{1}->REC segments.
 */
class RtlTask15Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "15"; }

    @Override
    protected String buildRtl() {
        return """
                <ANCH(1)>
                [ [VAL ' ' VAL : CL{1}->REC ' ' VAL : CL{1}->REC ' ' VAL : CL{1}->REC] ]+
                """;
    }
}
