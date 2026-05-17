package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask13: ATTR headers at R0, data cells use CM->AVP
 * and RM(Cn)->REC for four specific columns.
 */
class RtlTask13Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "13"; }

    @Override
    protected String buildRtl() {
        return """
                [ [ATTR]{5} []+ ]
                [ [VAL : SC->AVP, ((SR, C2), (SR, C4), (SR, C1), (SR, C3))->REC] [VAL : SC->AVP]{4} []+ ]+
                """;
    }
}
