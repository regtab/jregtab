package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask32: header row (SKIP, VAL+), data rows (VAL,
 * (BLANK?SKIP|VAL: first-in-row + first-in-col ->REC)+); post-processed by
 * AnchorAttributeAtPosition(2).
 */
class RtlTask32Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "32"; }

    @Override
    protected String buildRtl() {
        return """
                [ [] [VAL]+ ] 
                [ [VAL] [(BLANK ? _ | VAL : (SR, SC)->REC(2))]+ ]+
                """;
    }
}
