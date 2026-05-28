package ru.icc.regtab.rtl;

/**
 * Task 71:
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_71/}
 */
public class RtlTask71Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "71"; }

    @Override
    protected String buildRtl() {
        return """
                [ [BLANK?]+       [VAL#'H': (BW & #'H')*->SUFFIX('/')]+ ]+
                [ [!'\\d+'? VAL#'S': (RT & #'S')*->SUFFIX('/')]+ ['\\d+'? VAL: (COL, ROW)->REC]+ ]+
                """;
    }
}
