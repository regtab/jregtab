package ru.icc.regtab.rtl;

/**
 * Task 44: repeated subtables with one-or-more three-cell data rows (non-blank
 * anchor, non-blank value, blank guard) and an optional trailing compound row.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_44/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask44Test}
 * <pre>
 * { [ [!BLANK? VAL : SR->REC] [!BLANK? VAL] [BLANK?] ]+
 *   [ [BLANK?]{2} [!BLANK? VAL "," VAL : CL*->REC] ]? }+
 * </pre>
 * Data rows: non-blank anchor VAL with SR->REC (same-subrow, cardinality 1),
 * a non-blank plain VAL, and an optional blank guard. Optional footer row: two
 * blank skip cells then a non-blank compound cell VAL,VAL where the second part
 * uses CL*->REC (unbounded same-cell collection).
 */
public class RtlTask44Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "44"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [!BLANK? VAL : SR->REC] [!BLANK? VAL] [BLANK?] ]+
                  [ [BLANK?]{2} [!BLANK? VAL "," VAL : CL*->REC] ]? }+
                """;
    }
}
