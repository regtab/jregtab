package ru.icc.regtab.rtl;

/**
 * Task 81:
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_81/}
 */
public class RtlTask81Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "81"; }

    @Override
    protected String buildRtl() {
        return """
                { [ [VAL: BW->REC]+ ]
                  [ [VAL]+ ] }+
                """;
    }
}
