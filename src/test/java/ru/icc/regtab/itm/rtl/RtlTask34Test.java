package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask34: one-or-more subtables each with a header row
 * (VAL: DW(ST)->REC) followed by exactly 4 data rows (VAL).
 */
class RtlTask34Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "34"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL : ^ST*->REC] ] 
                  [ [VAL] ]{4} }+
                """;
    }
}
