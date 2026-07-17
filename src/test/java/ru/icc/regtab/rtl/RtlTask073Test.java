package ru.icc.regtab.rtl;

/**
 * Task 73: header VAL row; repeating data rows with conditional cells — blank cells are
 * skipped (_), non-blank cells become VAL anchors collecting same-column items into REC
 * (COL-&gt;REC).
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_073/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask073Test}
 * <pre>
 * [ [VAL]+ ]
 * [ [BLANK? _ | VAL: COL-&gt;REC]+ ]+
 * </pre>
 * COL-&gt;REC uses the header VAL row as the source of column-anchor items. Blank data cells
 * are silently skipped rather than creating empty records.
 */
public class RtlTask073Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "073"; }

    @Override
    protected String buildRtl() {
        return /* language=RTL */ """
                [ [VAL]+ ]
                [ [BLANK? _ | VAL: COL->REC]+ ]+
                """;
    }
}
