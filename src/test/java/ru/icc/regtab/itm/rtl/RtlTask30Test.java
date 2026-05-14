package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask30: header uses DW->REC, exactly 3 data rows per subtable.
 */
class RtlTask30Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "30"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL : DW->REC] ] [ [VAL] ]{3} }+
                """;
    }
}
