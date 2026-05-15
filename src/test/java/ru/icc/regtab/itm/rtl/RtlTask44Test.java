package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask44: data rows (NOT_BLANK rec(first-same-row), NOT_BLANK val, BLANK skip)
 * plus optional trailing row (2 blank skips + compound comma-pair).
 */
class RtlTask44Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "44"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [!BLANK? VAL : SR{1}->REC] [!BLANK? VAL] [BLANK?] ]+
                  [ [BLANK?]{2} [!BLANK? VAL "," VAL : CL->REC] ]? }+
                """;
    }
}
