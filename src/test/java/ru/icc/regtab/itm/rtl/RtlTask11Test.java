package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask11: header row, data rows use explicit subrow with
 * conditional content spec looking up first in same row and same column.
 */
public class RtlTask11Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "11"; }

    @Override
    protected String buildRtl() {
        return """
                [ [] [VAL]+ ]
                [ { [VAL] [(BLANK ? _ | VAL : (SR, SC)->REC(2))]+ } ]+
                """;
    }
}
