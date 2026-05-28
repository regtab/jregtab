package ru.icc.regtab.rtl;

/**
 * Task 77:
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_77/}
 */
public class RtlTask77Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "77"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL: (BW & R+2)->REC]+ ]{2}
                [ [VAL]+ ]{2}
                """;
    }
}
