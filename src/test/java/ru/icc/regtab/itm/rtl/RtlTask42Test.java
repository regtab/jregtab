package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask42: row with leading VAL (avp(""), rec right-same-row) followed by
 * exactly 2 compound cells (ATTR ":" VAL avp(attr-same-cell)).
 */
class RtlTask42Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "42"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL : ''->AVP, RW->REC] [ATTR ":" VAL : CL{1}->AVP]{2} ]+
                """;
    }
}
