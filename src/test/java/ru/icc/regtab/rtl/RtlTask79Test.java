package ru.icc.regtab.rtl;

/**
 * Task 79:
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_79/}
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
