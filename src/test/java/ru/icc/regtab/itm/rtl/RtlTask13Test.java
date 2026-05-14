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
                [ [ATTR]{5} [SKIP]+ ]
                [ [VAL : (CM{1})->AVP, (RM{1}(C2), RM{1}(C4), RM{1}(C1), RM{1}(C3))->REC] [VAL : (CM{1})->AVP]{4} [SKIP]+ ]+
                """;
    }
}
