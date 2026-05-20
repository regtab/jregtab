package ru.icc.regtab.itm.rtl;

/**
 * RTL equivalent of AtpTask36: pivot student blocks — first row has name VAL
 * (avp("") + rec col-2 in subtable) then subject ATTR and grade VAL (avp left-attr);
 * exactly 11 data rows skip name, same ATTR/VAL pattern.
 */
public class RtlTask36Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "36"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL : ''->AVP, (ST & C2)*->REC] [ATTR] [VAL : -LT->AVP] ] 
                  [ [] [ATTR] [VAL : -LT->AVP] ]{11} }+
                """;
    }
}
