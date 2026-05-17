package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask18: each subtable has a header row (ATTR=VAL with DW(ST)->REC)
 * followed by exactly 15 data rows (ATTR=VAL with CL->AVP).
 */
class RtlTask18Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "18"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [ATTR "=" VAL : ^ST*->REC, CL->AVP] ] [ [ATTR "=" VAL : CL->AVP] ]{15} }+
                """;
    }
}
