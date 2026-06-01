package ru.icc.regtab.rtl;

/**
 * Task 30: repeated subtables with a single-cell header collecting all values
 * below (unbounded), followed by exactly 3 plain data rows.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_030/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask030Test}
 * <pre>
 * { [ [VAL : BW*->REC] ]
 *   [ [VAL] ]{3} }+
 * </pre>
 * Structurally identical to task 19: each subtable has a header row with a VAL
 * anchor using BW*->REC (unbounded collection of cells below), then exactly 3
 * single-cell data rows each contributing a plain VAL.
 */
public class RtlTask030Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "030"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL : BW*->REC] ] 
                  [ [VAL] ]{3} }+
                """;
    }
}
