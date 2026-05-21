package ru.icc.regtab.rtl;

/**
 * Task 10: repeated subtables each with zero-or-more structured skip rows,
 * one data row collecting values via same-subrow REC, and an optional blank footer.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_10/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask10Test}
 * <pre>
 * { [ []{4} [BLANK?] []{3} ]*
 *   [ [VAL : SR*->REC] [VAL]+ ]
 *   [ [BLANK?]+ ]? }+
 * </pre>
 * Each subtable starts with zero-or-more skip rows (4 skipped, 1 optional
 * blank guard, 3 skipped). The single data row has an anchor VAL with
 * unbounded REC over all same-subrow values (SR*), then one-or-more plain
 * VALs. An optional footer row contains one-or-more blank-guard cells.
 */
public class RtlTask10Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "10"; }

    @Override
    protected String buildRtl() {
        return """
                { [ []{4} [BLANK?] []{3} ]*
                  [ [VAL : SR*->REC] [VAL]+ ]
                  [ [BLANK?]+ ]? }+
                """;
    }
}
