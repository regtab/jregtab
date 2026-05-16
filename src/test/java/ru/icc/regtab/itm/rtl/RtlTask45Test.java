package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask45: NOT_BLANK val + delimited(",") val rec(same-row col0).
 * Post-processed by AnchorAttributeAtPosition(1).
 */
class RtlTask45Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "45"; }

    @Override
    protected String buildRtl() {
        return """
                [ [!BLANK? VAL] [!BLANK? (VAL : (R+0, C0)->REC(1)){','}] ]+
                """;
    }
}
