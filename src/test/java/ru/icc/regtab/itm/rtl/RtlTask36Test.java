package ru.icc.regtab.itm.rtl;

/**
 * Task 36: repeated student-grade subtables — header row carries the student
 * name anchor, then exactly 11 subject/grade rows share the same left-attr AVP pattern.
 * <p>
 * ATP: {@link ru.icc.regtab.itm.atp.AtpTask36Test}
 * <pre>
 * { [ [VAL : ''->AVP, (ST & C2)*->REC] [ATTR] [VAL : LT->AVP] ]
 *   [ [] [ATTR] [VAL : LT->AVP] ]{11} }+
 * </pre>
 * Header row: anchor VAL with empty-literal AVP and unbounded REC over same-subtable
 * column 2 (ST & C2), a plain ATTR cell, and a VAL cell whose attribute is the
 * immediately left-of ATTR (LT->AVP). Each of the 11 data rows has a skip cell
 * instead of an anchor, followed by the same ATTR and LT->AVP VAL.
 */
public class RtlTask36Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "36"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL : ''->AVP, (ST & C2)*->REC] [ATTR] [VAL : LT->AVP] ] 
                  [ [] [ATTR] [VAL : LT->AVP] ]{11} }+
                """;
    }
}
