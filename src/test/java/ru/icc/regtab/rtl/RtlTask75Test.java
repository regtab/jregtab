package ru.icc.regtab.rtl;

/**
 * Task 75:
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_75/}
 */
public class RtlTask75Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "75"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL: RT*->REC] [(BLANK? VAL: -^(LT & !BLANK)->FILL | VAL)]+ ]+
                """;
    }
}
