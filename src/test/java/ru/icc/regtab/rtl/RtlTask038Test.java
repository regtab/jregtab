package ru.icc.regtab.rtl;

/**
 * Task 38: flat table with forward-fill — each row has a same-row REC anchor,
 * a plain value, and a third cell that fills from above when blank.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_038/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask038Test}
 * <pre>
 * [ [VAL : SR*->REC] [VAL] [(BLANK ? VAL : -AV->FILL | VAL)] ]+
 * </pre>
 * Each data row: anchor VAL with SR*->REC (unbounded same-subrow collection);
 * a plain VAL; and a conditional third cell — if blank, uses -AV->FILL to take
 * the nearest non-blank cell above (reverse row-major, cardinality 1); otherwise
 * a plain VAL.
 */
public class RtlTask038Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "038"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL : SR*->REC] [VAL] [(BLANK ? VAL : -AV->FILL | VAL)] ]+
                """;
    }
}
