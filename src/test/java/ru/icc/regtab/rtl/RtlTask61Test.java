package ru.icc.regtab.rtl;

/**
 * Task 61:
 * <p>
 * Fixtures: {@code src/test/resources/tasks/task_61/}
 */
public class RtlTask61Test extends RtlTaskBase {

    @Override
    protected String taskId() { return "61"; }

    @Override
    protected String buildRtl() {
        return """
                [ [VAL: CL*->REC, 'A'->AVP ' ' VAL: 'B'->AVP ' ' VAL: 'N'->AVP]+ ]+
                """;
    }
}
