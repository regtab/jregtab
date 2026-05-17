package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask05: unpivot with row key at COL0 and col key at ROW0.
 */
class RtlTask05Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "05"; }

    @Override
    protected String buildRtl() {
        return """
                [ [] [VAL]+ ]
                [ []+ ]
                [ [VAL] [VAL : (SR{1}, SC)->REC(2)]+ ]+
                """;
    }
}
