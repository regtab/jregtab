package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask19: header with DW->REC, exactly 3 data rows per subtable.
 */
public class RtlTask19Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "19"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL : BW*->REC] ] [ [VAL] ]{3} }+
                """;
    }
}
