package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask49: header skip+val row followed by data rows with
 * VAL and VAL rec(first-same-row, first-same-col). Post-processed by AnchorAttributeAtPosition(2).
 */
class RtlTask49Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "49"; }

    @Override
    protected String buildRtl() {
        return """
                [ [] [!BLANK? VAL]+ ]
                [ [!BLANK? VAL] [!BLANK? VAL : (SR, SC)->REC(2)]+ ]+
                """;
    }
}
