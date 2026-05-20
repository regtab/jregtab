package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask37: corner skip + qual-header row, per-person rows with
 * conditional date cells (blank → skip, non-blank → val: first-in-row + first-in-col ->REC).
 * Post-processed by AnchorAttributeAtPosition(2).
 */
public class RtlTask37Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "37"; }

    @Override
    protected String buildRtl() {
        return """
                [ [] [VAL]+ ]
                [ [VAL] [(BLANK ? _ | VAL : (SR, SC)->REC(2))]+ ]+
                """;
    }
}
