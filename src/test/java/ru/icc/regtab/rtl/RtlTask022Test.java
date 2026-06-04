package ru.icc.regtab.rtl;

/**
 * Task 22: repeated subtables where the anchor collects values in columns 2–5
 * in column-major traversal order, plus a plain data row.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_022/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask022Test}
 * <pre>
 * { [ [VAL : ^ST&C2..5*->REC] [] [VAL]+ ] [ []{2} [VAL]+ ] }+
 * </pre>
 * Header row: anchor VAL with column-major REC over same-subtable columns 2–5
 * (^ST&C2..5*), one skip, then one-or-more plain VALs. Data row: two
 * skipped cells then one-or-more plain VALs contributing to the REC.
 */
public class RtlTask022Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "022"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL : ^ST&C2..5*->REC] [] [VAL]+ ] [ []{2} [VAL]+ ] }+
                """;
    }
}
