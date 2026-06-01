package ru.icc.regtab.rtl;

/**
 * Task 41: repeated subtables each with two optional rows — a compound fill+REC
 * row and a right-then-context REC row — for key-value pair extraction.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_041/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask041Test}
 * <pre>
 * { [ [!BLANK? VAL : ''->FILL, (CL, RT)->REC "" VAL] [!BLANK? VAL] ]?
 *   [ [!BLANK? VAL : (RT, '')->REC] [BLANK?] ]? }+
 * </pre>
 * First optional row: non-blank compound cell with empty-literal FILL and REC
 * from both same-cell (CL) and right-of (RT) providers, concatenated with a
 * plain VAL; plus a non-blank plain VAL cell. Second optional row: non-blank
 * anchor VAL with REC from right-of (RT) and an empty-string context provider,
 * followed by an optional blank cell.
 */
public class RtlTask041Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "041"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [!BLANK? VAL : ''->FILL, (CL, RT)->REC "" VAL] [!BLANK? VAL] ]?
                  [ [!BLANK? VAL : (RT, '')->REC] [BLANK?] ]? }+
                """;
    }
}
