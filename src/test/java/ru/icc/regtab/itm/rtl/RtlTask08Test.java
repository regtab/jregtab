package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask08: skip row, then rows with row key at COL0.
 */
class RtlTask08Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "08"; }

    @Override
    protected String buildRtl() {
        return """
                [ []+ ]
                [ [VAL] [VAL : SR->REC(1)]+ ]+
                """;
    }
}
