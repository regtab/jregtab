package ru.icc.regtab.rtl;

/**
 * Task 34: repeated subtables with a single-cell header collecting all values
 * below (unbounded), followed by exactly 4 plain data rows (same as task 31
 * but without a trailing skip row).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_034/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask034Test}
 * <pre>
 * { [ [VAL : BW*->REC] ]
 *   [ [VAL] ]{4} }+
 * </pre>
 * Each subtable has a header row with a VAL anchor using BW*->REC (unbounded
 * collection of cells below), then exactly 4 single-cell data rows each with
 * a plain VAL.
 */
public class RtlTask034Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "034"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL : BW*->REC] ] 
                  [ [VAL] ]{4} }+
                """;
    }
}
