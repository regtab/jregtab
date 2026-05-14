package ru.icc.regtab.itm.rtl;

import ru.icc.regtab.itm.interpret.AnchorAttributeAtPosition;
import ru.icc.regtab.itm.recordset.Recordset;

/**
 * RTL equivalent of AtpTask29: each row has a fixed 6-cell header subrow followed by
 * one-or-more 4-cell data subrows; the anchor cell collects first-6 and same-subrow items
 * via a two-provider REC action.
 */
class RtlTask29Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "29"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL]{6} { [VAL : (CL{6}(R+0), RW)->REC] [VAL]{3} }+ ]+
                """;
    }

    @Override
    protected Recordset transformActual(Recordset actual) {
        return new AnchorAttributeAtPosition(6).apply(actual);
    }
}
