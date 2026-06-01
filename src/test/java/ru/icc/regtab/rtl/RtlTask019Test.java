package ru.icc.regtab.rtl;

/**
 * Task 19: repeated subtables with a single-cell header that collects all
 * values below (unbounded), followed by exactly 3 plain value data rows.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_019/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask019Test}
 * <pre>
 * { [ [VAL : BW*->REC] ] [ [VAL] ]{3} }+
 * </pre>
 * Each subtable has one header row with a VAL anchor using BW*->REC (unbounded
 * collection of all cells below). Then exactly 3 single-cell data rows each
 * with a plain VAL, all feeding into the header's REC.
 */
public class RtlTask019Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "019"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL : BW*->REC] ] [ [VAL] ]{3} }+
                """;
    }
}
