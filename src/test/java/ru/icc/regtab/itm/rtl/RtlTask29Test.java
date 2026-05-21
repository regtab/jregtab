package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask29: each row has a fixed 6-cell header subrow followed by
 * one-or-more 4-cell data subrows; the anchor cell collects first-6 and same-subrow items
 * via a two-provider REC action.
 */
public class RtlTask29Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "29"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL]{6} { [VAL : (ROW{6}, RT*)->REC(6)] [VAL]{3} }+ ]+
                """;
    }
}
