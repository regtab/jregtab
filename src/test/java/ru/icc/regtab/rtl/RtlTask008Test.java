package ru.icc.regtab.rtl;

/**
 * Task 08: one skip row followed by data rows with a row-key anchor and
 * one-or-more value cells referencing it via same-subrow.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_008/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask008Test}
 * <pre>
 * [ []+ ]
 * [ [VAL] [VAL : SR->REC(1)]+ ]+
 * </pre>
 * Structurally identical to task 04: an initial skip row and then data rows
 * where each value cell uses REC(1) with provider SR (same subrow) to bind
 * the row-key anchor.
 */
public class RtlTask008Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "008"; }

    @Override
    protected String buildRtl() {
        return """
                [ []+ ]
                [ [VAL] [VAL : SR->REC(1)]+ ]+
                """;
    }
}
