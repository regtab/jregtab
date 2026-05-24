package ru.icc.regtab.rtl;

/**
 * Task 56:
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_56/}
 */
public class RtlTask56Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "56"; }

    @Override
    protected String buildRtl() {
        return """
                [ [ATTR] [VAL : BW*->REC, ROW->AVP]+ ]
                [ [ATTR] [VAL : ROW->AVP]+ ]+
                """;
    }
}
