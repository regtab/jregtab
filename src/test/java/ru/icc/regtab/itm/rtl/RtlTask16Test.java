package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask16: anchor looks up one cell to the right (REC),
 * and concatenates cells below with the same string content (CONCAT).
 */
class RtlTask16Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "16"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL : RT{1}->REC, ^(BW, STR)->CONCAT] [VAL] ]+
                """;
    }
}
