package ru.icc.regtab.rtl;

/**
 * Task 80: header VAL row; repeating data rows where each VAL cell uses COL-&gt;REC(1) —
 * an inline anchor at position 1 (the first row) for each column.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_80/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask80Test}
 * <pre>
 * [ [VAL]+ ]
 * [ [VAL: COL-&gt;REC(1)]+ ]+
 * </pre>
 * COL-&gt;REC(1) specifies that the anchor position within the same-column provider is item
 * at index 1 (the header row VAL). This allows data cells to directly reference their
 * column header as the record anchor without a separate anchor cell per row.
 */
public class RtlTask80Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "80"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL]+ ]
                [ [VAL: COL->REC(1)]+ ]+
                """;
    }
}
