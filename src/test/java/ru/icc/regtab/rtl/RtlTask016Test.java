package ru.icc.regtab.rtl;

/**
 * Task 16: flat table where each anchor cell in column 0 collects one value
 * to the right via REC and concatenates same-string cells below via JOIN(0).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_016/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask016Test}
 * <pre>
 * [ [VAL : RT->REC, BW&STR*->JOIN(0)] [VAL] ]+
 * </pre>
 * Data rows: anchor VAL uses RT->REC (1 value immediately to the right) and
 * BW&STR*->JOIN(0) (unbounded concatenation of cells that are both below
 * and have the same string as the anchor), followed by a plain VAL cell.
 */
public class RtlTask016Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "016"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL : RT->REC, BW&STR*->JOIN(0)] [VAL] ]+
                """;
    }
}
