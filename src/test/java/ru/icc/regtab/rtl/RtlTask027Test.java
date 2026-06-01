package ru.icc.regtab.rtl;

/**
 * Task 27: repeated subtables with a single-cell header collecting all values
 * below, one skip row separator, and exactly 9 plain data rows.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_027/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask027Test}
 * <pre>
 * { [ [VAL : BW*->REC] ]
 *   [ [] ]
 *   [ [VAL] ]{9} }+
 * </pre>
 * Header row: a single VAL anchor with BW*->REC (unbounded collection of cells
 * below). A one-cell skip row acts as a separator. Then exactly 9 single-cell
 * data rows each contributing a plain VAL to the REC.
 */
public class RtlTask027Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "027"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL : BW*->REC] ] 
                  [ [] ] 
                  [ [VAL] ]{9} }+
                """;
    }
}
