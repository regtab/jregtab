package ru.icc.regtab.rtl;

/**
 * Task 26: repeated subtables with a three-cell header row (anchor with AVP and
 * column-2 REC, plus ATTR and AVP cells) and exactly 5 three-cell data rows.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_026/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask026Test}
 * <pre>
 * { [ [VAL : ''->AVP, (ST & C2)*->REC] [ATTR] [VAL : SR->AVP] ]
 *   [ [] [ATTR] [VAL : SR->AVP] ]{5} }+
 * </pre>
 * Header row: anchor VAL with empty-literal AVP and unbounded REC over same-subtable
 * column 2 (ST & C2); then a plain ATTR cell; then a VAL cell whose attribute is
 * fetched from the same-subrow ATTR (SR->AVP). Each of the 5 data rows mirrors
 * the header but starts with a skip cell instead of an anchor.
 */
public class RtlTask026Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "026"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL : ''->AVP, (ST & C2)*->REC] [ATTR] [VAL : SR->AVP] ] 
                  [ [] [ATTR] [VAL : SR->AVP] ]{5} }+
                """;
    }
}
