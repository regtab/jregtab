package ru.icc.regtab.rtl;

/**
 * Task 04: one skip row followed by data rows with a row-key anchor and
 * one-or-more value cells referencing it via same-subrow.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_004/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask004Test}
 * <pre>
 * [ []+ ]
 * [ [VAL] [VAL : SR->REC(1)]+ ]+
 * </pre>
 * The first row skips one-or-more cells. Subsequent data rows begin with a
 * plain VAL anchor; each following cell uses REC(1) with provider SR (same
 * subrow) to attach the row-key to the value.
 */
public class RtlTask004Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "004"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
                [ []+ ]
                [ [VAL] [VAL : SR->REC(1)]+ ]+
                """;
    }
}
