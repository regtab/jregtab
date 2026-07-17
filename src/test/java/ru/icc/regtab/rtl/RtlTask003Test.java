package ru.icc.regtab.rtl;

/**
 * Task 03: flat table with a row-key anchor followed by exactly two value
 * cells per row, each referencing the anchor via same-subrow.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_003/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask003Test}
 * <pre>
 * [ [VAL] [VAL : SR->REC(1)]{2} ]+
 * </pre>
 * Each data row starts with a plain VAL anchor. The next two cells each carry
 * REC(1) with provider SR (same subrow, cardinality 1), binding the row-key
 * anchor to every value cell.
 */
public class RtlTask003Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "003"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
                [ [VAL] [VAL : SR->REC(1)]{2} ]+
                """;
    }
}
