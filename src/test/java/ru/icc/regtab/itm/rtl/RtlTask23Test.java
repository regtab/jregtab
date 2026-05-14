package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask23: subtables of exactly-3 rows, each row has four cells —
 * VAL (empty-AVP + REC same-row + CONCAT below-same-str), ATTR (SUFFIX right-AUX),
 * AUX, VAL (AVP same-row-attr).
 */
class RtlTask23Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "23"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL : ('')->AVP, (CL(ROW+0))->REC, (DW(STR))->CONCAT] [ATTR : (RW{1}(ROW+0))->SUFFIX] [AUX] [VAL : (CL(ROW+0))->AVP] ]{3} }+
                """;
    }
}
