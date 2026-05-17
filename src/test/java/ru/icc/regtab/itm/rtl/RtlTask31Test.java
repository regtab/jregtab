package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask31: one-or-more subtables each with a header row (VAL: DW(ST)->REC),
 * exactly 4 data rows (VAL), and a trailing skip row.
 */
class RtlTask31Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "31"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL : ^ST*->REC] ] 
                  [ [VAL] ]{4} 
                  [ [] ] }+
                """;
    }
}
