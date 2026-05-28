package ru.icc.regtab.rtl;

/**
 * Task 79: single header row of VAL anchors (BW*-&gt;REC) followed by one-or-more data rows
 * of plain VAL cells.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_79/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask79Test}
 * <pre>
 * [ [VAL: BW*-&gt;REC]+ ]
 * [ [VAL]+ ]+
 * </pre>
 * The header row anchors one record per column by collecting all below-items (BW*). Each
 * data cell in subsequent rows is automatically linked to the anchor in the same column.
 */
public class RtlTask79Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "79"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL: BW*->REC]+ ]
                [ [VAL]+ ]+
                """;
    }
}
