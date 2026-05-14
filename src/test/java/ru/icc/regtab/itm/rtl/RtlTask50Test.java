package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask50: single subtable with NOT_BLANK rows —
 * VAL(avp+rec sameRow+concat DW(STR)), ATTR, VAL(avp attr-same-row).
 * Same as Task46 but with a single (non-repeating) subtable.
 */
class RtlTask50Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "50"; }

    @Override
    protected String buildRtl() {
        return """
                [ [!BLANK? VAL : ('')->AVP, (RM)->REC, (DW(STR))->CONCAT] [!BLANK? ATTR] [!BLANK? VAL : (RM{1})->AVP] ]+
                """;
    }
}
