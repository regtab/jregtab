package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask26: subtables with a header row (VAL: empty-AVP + col-2-REC, ATTR, VAL: AVP)
 * followed by exactly 5 data rows (SKIP, ATTR, VAL: AVP).
 */
public class RtlTask26Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "26"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL : ''->AVP, (ST & C2)*->REC] [ATTR] [VAL : SR->AVP] ] 
                  [ [] [ATTR] [VAL : SR->AVP] ]{5} }+
                """;
    }
}
