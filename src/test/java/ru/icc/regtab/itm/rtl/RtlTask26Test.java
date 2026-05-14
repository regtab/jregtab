package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask26: subtables with a header row (VAL: empty-AVP + col-2-REC, ATTR, VAL: AVP)
 * followed by exactly 5 data rows (SKIP, ATTR, VAL: AVP).
 */
class RtlTask26Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "26"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL : ('')->AVP, (CL(ST, COL2))->REC] [ATTR] [VAL : (CL(ROW+0))->AVP] ] [ [SKIP] [ATTR] [VAL : (CL(ROW+0))->AVP] ]{5} }+
                """;
    }
}
