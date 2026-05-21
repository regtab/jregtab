package ru.icc.regtab.itm.rtl;

/**
 * Task 01: two-row subtables — anchor row (VAL rec over whole subtable)
 * plus a plain value row.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_01/}
 * ATP: {@link ru.icc.regtab.itm.atp.AtpTask01Test}
 * <pre>
 * { [ [VAL : ST*->REC] [VAL]{2} []+ ]
 *   [ []               [VAL]{4} []+ ] }+
 * </pre>
 * Each subtable has two fixed rows. First row: anchor VAL with REC collecting
 * all values in the same subtable (ST, unbounded). Second row: 4 plain VALs.
 */
public class RtlTask01Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "01"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL : ST*->REC] [VAL]{2} []+ ]
                [ [] [VAL]{4} []+ ] }+
                """;
    }

}
