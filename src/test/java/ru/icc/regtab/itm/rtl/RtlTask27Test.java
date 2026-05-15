package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask27: header uses DW->REC, skip row, exactly 9 data rows per subtable.
 */
class RtlTask27Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "27"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL : ^BW->REC] ] [ [] ] [ [VAL] ]{9} }+
                """;
    }
}
