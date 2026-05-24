package ru.icc.regtab.rtl;

/**
 * Task 55:
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_55/}
 */
public class RtlTask55Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "55"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL: CL*->REC ',' (VAL){', '}] ]+
                """;
    }
}
