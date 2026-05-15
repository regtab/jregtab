package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask46: NOT_BLANK rows with VAL(avp+rec sameRow+concat DW(STR)),
 * ATTR, and VAL(avp attr-same-row).
 */
class RtlTask46Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "46"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [!BLANK? VAL : ''->AVP, SR->REC, ^(BW, STR)->CONCAT] [!BLANK? ATTR] [!BLANK? VAL : SR{1}->AVP] ]+ }+
                """;
    }
}
