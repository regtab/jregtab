package ru.icc.regtab.rtl;

/**
 * Task 78:
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_78/}
 */
public class RtlTask78Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "78"; }

    @Override
    protected String buildRtl() {
        return """
                { ROW->AVP
                [ [ATTR] [VAL: BW*->REC]+ ]
                [ [ATTR] [VAL]+ ]+ }
                """;
    }
}
