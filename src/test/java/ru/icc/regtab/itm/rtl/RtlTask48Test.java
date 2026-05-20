package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask48: header block (2×2 skip) followed by repeating person blocks
 * (NOT_BLANK VAL avp+rec, compound ATTR:VAL; BLANK + compound; optional blank row).
 */
public class RtlTask48Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "48"; }

    @Override
    protected String buildRtl() {
        return """
                [ []{2} ]{2}
                { [ [!BLANK? VAL : ''->AVP, (ST & C1)*->REC] [!BLANK? ATTR ":" VAL : CL->AVP] ]
                  [ [BLANK?] [!BLANK? ATTR ":" VAL : CL->AVP] ]
                  [ [BLANK?]{2} ]? }+
                """;
    }
}
