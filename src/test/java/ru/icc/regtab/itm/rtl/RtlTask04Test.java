package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask04: skip row, then rows with row key at COL0.
 */
public class RtlTask04Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "04"; }

    @Override
    protected String buildRtl() {
        return """
                [ []+ ]
                [ [VAL] [VAL : SR->REC(1)]+ ]+
                """;
    }
}
