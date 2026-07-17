package ru.icc.regtab.rtl;

/**
 * Task 12: single-row header where the anchor collects all values in column 5
 * (unbounded), followed by data rows that only fill column 5.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_012/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask012Test}
 * <pre>
 * [ [VAL : C5*->REC] []{4} [VAL] ]
 * [ []{5} [VAL] ]+
 * </pre>
 * Header row: anchor VAL in column 0 with REC collecting all values in column 5
 * (C5, unbounded), four skipped cells, then the first column-5 VAL. Each
 * subsequent row skips five cells and contributes another column-5 VAL to the
 * REC collection.
 */
public class RtlTask012Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "012"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
                [ [VAL : C5*->REC] []{4} [VAL] ]
                [ []{5} [VAL] ]+
                """;
    }
}
