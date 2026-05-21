package ru.icc.regtab.rtl;

/**
 * Task 49: cross-table unpivot with non-blank guards — a skip+header row followed
 * by data rows where each data cell references both row and column anchors.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_49/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask49Test}
 * <pre>
 * [ [] [!BLANK? VAL]+ ]
 * [ [!BLANK? VAL] [!BLANK? VAL : (SR, SC)->REC(2)]+ ]+
 * </pre>
 * Header row: one skip cell then one-or-more non-blank column-header VALs. Data
 * rows: a non-blank row-key anchor VAL followed by one-or-more non-blank data
 * cells each producing REC(2) with providers SR (same subrow) and SC (same
 * subcol) for a two-axis unpivot.
 */
public class RtlTask49Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "49"; }

    @Override
    protected String buildRtl() {
        return """
                [ [] [!BLANK? VAL]+ ]
                [ [!BLANK? VAL] [!BLANK? VAL : (SR, SC)->REC(2)]+ ]+
                """;
    }
}
