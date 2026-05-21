package ru.icc.regtab.itm.rtl;

/**
 * Task 31: repeated subtables with a single-cell header collecting values below,
 * exactly 4 plain data rows, and a trailing skip row separator.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_31/}
 * ATP: {@link ru.icc.regtab.itm.atp.AtpTask31Test}
 * <pre>
 * { [ [VAL : BW*->REC] ]
 *   [ [VAL] ]{4}
 *   [ [] ] }+
 * </pre>
 * Header row: a single VAL anchor with BW*->REC (unbounded collection of cells
 * below). Exactly 4 single-cell data rows follow, each contributing a plain VAL.
 * A trailing one-cell skip row acts as a subtable separator.
 */
public class RtlTask31Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "31"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL : BW*->REC] ] 
                  [ [VAL] ]{4} 
                  [ [] ] }+
                """;
    }
}
