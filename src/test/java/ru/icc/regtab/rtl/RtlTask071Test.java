package ru.icc.regtab.rtl;

/**
 * Task 71: header rows with blank skip cells and #H VAL cells that aggregate below #H
 * peers as a '/'-suffix; data rows with non-digit #S VAL cells (aggregating right #S
 * peers as '/'-suffix) and digit VAL cells collecting (COL, ROW) into REC.
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_071/}
 * ATP: {@link ru.icc.regtab.atp.AtpTask071Test}
 * <pre>
 * [ [BLANK?]+       [VAL#'H': (BW &amp; #'H')*-&gt;SUFFIX('/')]+ ]+
 * [ [!'\\d+'? VAL#'S': (RT &amp; #'S')*-&gt;SUFFIX('/')]+ ['\\d+'? VAL: (COL, ROW)-&gt;REC]+ ]+
 * </pre>
 * SUFFIX('/') builds a slash-separated path from multiple #H or #S neighbours, allowing
 * hierarchical header paths. Data VAL cells then reference column (COL) and row (ROW) items.
 */
public class RtlTask071Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "071"; }

    @Override
    protected String buildRtl() {
        return """
                [ [BLANK?]+       [VAL#'H': (BW & #'H')*->SUFFIX('/')]+ ]+
                [ [!'\\d+'? VAL#'S': (RT & #'S')*->SUFFIX('/')]+ ['\\d+'? VAL: (COL, ROW)->REC]+ ]+
                """;
    }
}
