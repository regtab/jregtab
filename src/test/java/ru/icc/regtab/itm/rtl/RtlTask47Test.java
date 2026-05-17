package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask47: NOT_BLANK rows with VAL(rec sameRow + concat DW(STR)) and plain VAL.
 */
class RtlTask47Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "47"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [!BLANK? VAL : SR*->REC, (BW & STR)*->CONCAT] [!BLANK? VAL] ]+ }+
                """;
    }
}
