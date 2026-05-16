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
                [ [VAL ' ' VAL : CL{1}->REC(1) ' ' VAL : CL{1}->REC(1) ' ' VAL : CL{1}->REC(1)] ]+
                """;
    }
}
