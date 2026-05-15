package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask21: multi-column header with DW->REC, exactly 2 data rows per subtable.
 */
class RtlTask21Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "21"; }

    @Override
    protected String buildRtl() {
        return """
                <NORM>
                { [ [VAL : BW->REC]+ ] [ [VAL]+ ]{2} }+
                """;
    }
}
