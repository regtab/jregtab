package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask13: ATTR headers at R0, data cells use CM{1}->AVP
 * and RM{1}(Cn)->REC for four specific columns.
 */
class RtlTask13Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "13"; }

    @Override
    protected String buildRtl() {
        return """
                [ [ATTR]{5} []+ ]
                [ [VAL : SC{1}->AVP, ((SR, C2){1}, (SR, C4){1}, (SR, C1){1}, (SR, C3){1})->REC] [VAL : SC{1}->AVP]{4} []+ ]+
                """;
    }
}
