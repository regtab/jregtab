package ru.icc.regtab.rtl;

/**
 * Task 36: repeated student-grade subtables — header row carries the student
 * name anchor, then exactly 11 subject/grade rows share the same left-attr AVP pattern.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_036/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask036Test}
 * <pre>
 * { [ [VAL : ''->AVP, (ST & C2)*->REC] [ATTR] [VAL : LT->AVP] ]
 *   [ [] [ATTR] [VAL : LT->AVP] ]{11} }+
 * </pre>
 * Header row: anchor VAL with empty-literal AVP and unbounded REC over same-subtable
 * column 2 (ST & C2), a plain ATTR cell, and a VAL cell whose attribute is the
 * immediately left-of ATTR (LT->AVP). Each of the 11 data rows has a skip cell
 * instead of an anchor, followed by the same ATTR and LT->AVP VAL.
 */
public class RtlTask036Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "036"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL : ''->AVP, (ST & C2)*->REC] [ATTR] [VAL : LT->AVP] ] 
                  [ [] [ATTR] [VAL : LT->AVP] ]{11} }+
                """;
    }
}
