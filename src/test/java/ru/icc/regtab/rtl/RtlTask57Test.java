package ru.icc.regtab.rtl;

/**
 * Task 57:
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_57/}
 */
public class RtlTask57Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "57"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL: RT->REC] [VAL=TRIM '-' VAL=TRIM] ]+
                """;
    }
}
