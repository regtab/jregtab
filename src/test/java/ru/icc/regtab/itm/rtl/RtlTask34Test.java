package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask34: one-or-more subtables each with a header row
 * (VAL: DW(ST)->REC) followed by exactly 4 data rows (VAL).
 */
public class RtlTask34Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "34"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL : BW*->REC] ] 
                  [ [VAL] ]{4} }+
                """;
    }
}
