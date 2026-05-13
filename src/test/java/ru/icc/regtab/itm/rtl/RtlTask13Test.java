package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask13: ATTR headers at ROW0, data cells use CM{1}(ROW0)->AVP
 * and RM{1}(COLn)->REC for four specific columns.
 */
class RtlTask13Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "13"; }

    @Override
    protected String buildRtl() {
        return """
                [ [ATTR]{5} [SKIP]+ ]
                [ [VAL : (CM{1}(ROW0))->AVP, (RM{1}(COL2), RM{1}(COL4), RM{1}(COL1), RM{1}(COL3))->REC] [VAL : (CM{1}(ROW0))->AVP]{4} [SKIP]+ ]+
                """;
    }
}
