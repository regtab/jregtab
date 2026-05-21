package ru.icc.regtab.rtl;

/**
 * Task 48: a fixed 2-row×2-cell skip header, then repeated person subtables
 * each with a name anchor row, a blank+compound row, and an optional blank footer.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_48/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask48Test}
 * <pre>
 * [ []{2} ]{2}
 * { [ [!BLANK? VAL : ''->AVP, (ST & C1)*->REC] [!BLANK? ATTR ":" VAL : CL->AVP] ]
 *   [ [BLANK?] [!BLANK? ATTR ":" VAL : CL->AVP] ]
 *   [ [BLANK?]{2} ]? }+
 * </pre>
 * Two skip rows (2 cells each) precede the person blocks. Each person subtable:
 * first row has a non-blank anchor VAL with empty-literal AVP and unbounded REC
 * over same-subtable column 1 (ST & C1), plus a compound ATTR:VAL cell with
 * CL->AVP. Second row: blank guard and another ATTR:VAL compound. Optional third
 * row: two blank cells.
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
